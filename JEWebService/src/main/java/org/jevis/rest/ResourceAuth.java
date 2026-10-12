package org.jevis.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonApiKey;
import org.jevis.commons.ws.json.JsonLoginResult;
import org.jevis.commons.ws.json.JsonMfaStatus;
import org.jevis.commons.ws.sql.CachedAccessControl;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.JEVisUserSQL;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.auth.AuthService;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token based login, replaces HTTP Basic authentication.
 *
 * <pre>
 * POST /auth/login            {"username","password","remember"}  -> JsonLoginResult
 * POST /auth/login/mfa        {"mfaToken","code","remember"}      -> JsonLoginResult
 * POST /auth/refresh          {"refreshToken"}                    -> JsonLoginResult
 * POST /auth/logout           {"refreshToken"} + Bearer
 * POST /auth/handoff          Bearer                              -> {"code"}
 * POST /auth/handoff/redeem   {"code"}                            -> JsonLoginResult
 * POST /auth/password         {"oldPassword","newPassword","remember"} + Bearer -> JsonLoginResult
 * GET  /auth/me               Bearer
 * GET  /auth/mfa              Bearer                              -> JsonMfaStatus
 * POST /auth/mfa/totp/setup   {"password"} + Bearer, or {"mfaToken"} -> JsonMfaStatus (secret, otpAuthUri)
 * POST /auth/mfa/totp/confirm {"code","remember"} + Bearer, or {"mfaToken",...} -> JsonLoginResult + recoveryCodes
 * POST /auth/mfa/disable      {"password","code"} + Bearer
 * DELETE /auth/mfa/{userId}   sysadmin
 * GET/POST /auth/apikeys, DELETE /auth/apikeys/{id}  sysadmin
 * </pre>
 *
 * <p>All bodies are JSON. Failed logins answer 401 with a {@link JsonLoginResult} of status FAILURE.
 */
@Path("/JEWebService/v1/auth")
public class ResourceAuth {

    private static final Logger logger = LogManager.getLogger(ResourceAuth.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MIN_PASSWORD_LENGTH = 8;
    /**
     * Failed login attempts per client address in a 5 minute window. Generous, because clients behind a proxy share
     * one address; the per-account lockout is the main protection.
     */
    private static final int MAX_FAILURES_PER_ADDRESS = 30;
    private static final Cache<String, AtomicInteger> FAILURES = CacheBuilder.newBuilder()
            .maximumSize(10_000).expireAfterWrite(5, TimeUnit.MINUTES).build();

    @Context
    private org.glassfish.grizzly.http.server.Request grizzlyRequest;
    private SQLDataSource ds;

    /* ------------------------------------------------------------------ login */

    /**
     * API keys are for services only and must not manage accounts or create interactive logins.
     */
    private static boolean isInteractive(SQLDataSource ds) {
        return ds.getAuthMethod() != SQLDataSource.AuthMethod.API_KEY;
    }

    private static Response json(Object entity) {
        return Response.ok(toJson(entity)).type(MediaType.APPLICATION_JSON)
                .header("Cache-Control", "no-store").build();
    }

    private static String toJson(Object entity) {
        try {
            return MAPPER.writeValueAsString(entity);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize response", ex);
        }
    }

    private static Response notAvailable() {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("Token login is not available, the database migration V10 is missing").build();
    }

    /* ------------------------------------------------------------------ handoff (launcher -> JECC) */

    private static Response tooManyAttempts() {
        return Response.status(429).entity("Too many failed attempts, try again later").build();
    }

    private static JsonNode parse(String body) {
        try {
            JsonNode node = MAPPER.readTree(body == null ? "" : body);
            if (node == null || !node.isObject()) throw new BadRequestException("JSON object expected");
            return node;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Invalid JSON");
        }
    }

    /* ------------------------------------------------------------------ own account */

    private static String text(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static String bearerToken(HttpHeaders httpHeaders) {
        List<String> header = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isEmpty() || header.get(0) == null) return null;
        String value = header.get(0).trim();
        return value.regionMatches(true, 0, "Bearer ", 0, 7) ? value.substring(7).trim() : null;
    }

    /* ------------------------------------------------------------------ two-factor authentication */

    @POST
    @Logged
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            if (isThrottled()) return tooManyAttempts();
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url, false);
            if (!AuthService.isAvailable(ds)) return notAvailable();
            JsonLoginResult result = AuthService.login(ds, text(json, "username"), text(json, "password"),
                    json.path("remember").asBoolean(false), clientInfo(httpHeaders));
            return loginResponse(result);
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Login failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/login/mfa")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginMfa(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            if (isThrottled()) return tooManyAttempts();
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url, false);
            JsonLoginResult result = AuthService.completeMfa(ds, text(json, "mfaToken"), text(json, "code"),
                    json.path("remember").asBoolean(false), clientInfo(httpHeaders));
            return loginResponse(result);
        } catch (AuthenticationException ex) {
            return loginResponse(JsonLoginResult.failure(ex.getMessage()));
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("2FA login failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url, false);
            return loginResponse(AuthService.refresh(ds, text(json, "refreshToken"), clientInfo(httpHeaders)));
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Token refresh failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            JsonNode json = body == null || body.trim().isEmpty() ? MAPPER.createObjectNode() : parse(body);
            ds = new SQLDataSource(httpHeaders, request, url, false);
            AuthService.logout(ds, bearerToken(httpHeaders), text(json, "refreshToken"));
            return Response.noContent().build();
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Logout failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/handoff")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHandoff(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            if (ds.getAuthMethod() == SQLDataSource.AuthMethod.API_KEY) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not allowed for API keys").build();
            }
            if (!AuthService.isAvailable(ds)) return notAvailable();
            Map<String, Object> answer = new LinkedHashMap<>();
            answer.put("code", AuthService.createHandoff(ds, ds.getCurrentUser(), clientInfo(httpHeaders)));
            return json(answer);
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Handoff failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    /* ------------------------------------------------------------------ API keys (sysadmin) */

    @POST
    @Logged
    @Path("/handoff/redeem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response redeemHandoff(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            if (isThrottled()) return tooManyAttempts();
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url, false);
            return loginResponse(AuthService.redeemHandoff(ds, text(json, "code"), clientInfo(httpHeaders)));
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Handoff redeem failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url);
            if (!isInteractive(ds)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not allowed for this login type").build();
            }
            if (!AuthService.isAvailable(ds)) return notAvailable();
            JEVisUserSQL user = ds.getCurrentUser();
            String newPassword = text(json, "newPassword");
            if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
                return Response.status(Response.Status.BAD_REQUEST).entity("The new password must have at least " + MIN_PASSWORD_LENGTH + " characters").build();
            }
            if (AuthService.checkPassword(ds, user, text(json, "oldPassword")) != AuthService.PasswordCheck.OK) {
                registerFailure();
                return loginResponse(JsonLoginResult.failure("The current password is not correct."));
            }
            AuthService.setPassword(ds, user.getUserID(), newPassword);
            CachedAccessControl.getInstance(ds, false).invalidate();
            logger.info("User '{}' changed the password", user.getAccountName());
            /* all sessions were signed out, the calling client gets new tokens */
            return loginResponse(AuthService.issueTokens(ds, user, json.path("remember").asBoolean(false), clientInfo(httpHeaders)));
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Password change failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @GET
    @Logged
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            JEVisUserSQL user = ds.getCurrentUser();
            Map<String, Object> answer = new LinkedHashMap<>();
            answer.put("id", user.getUserID());
            answer.put("name", user.getAccountName());
            answer.put("sysAdmin", user.isSysAdmin());
            answer.put("authMethod", ds.getAuthMethod() != null ? ds.getAuthMethod().name() : null);
            return json(answer);
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Could not read current user", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    /* ------------------------------------------------------------------ helpers */

    @GET
    @Logged
    @Path("/mfa")
    @Produces(MediaType.APPLICATION_JSON)
    public Response mfaStatus(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            return json(AuthService.getMfaStatus(ds, ds.getCurrentUser()));
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Could not read 2FA status", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/mfa/totp/setup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response totpSetup(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            JsonNode json = parse(body);
            JEVisUserSQL user;
            String mfaToken = text(json, "mfaToken");
            if (mfaToken != null) {
                /* enrollment during login, the password was checked in the first login step */
                ds = new SQLDataSource(httpHeaders, request, url, false);
                user = AuthService.userFromMfaToken(ds, mfaToken);
            } else {
                ds = new SQLDataSource(httpHeaders, request, url);
                if (!isInteractive(ds)) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Not allowed for this login type").build();
                }
                user = ds.getCurrentUser();
                if (AuthService.checkPassword(ds, user, text(json, "password")) != AuthService.PasswordCheck.OK) {
                    registerFailure();
                    return loginResponse(JsonLoginResult.failure("The current password is not correct."));
                }
            }
            JsonMfaStatus status = AuthService.setupTotp(ds, user);
            return json(status);
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("2FA setup failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/mfa/totp/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response totpConfirm(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            if (isThrottled()) return tooManyAttempts();
            JsonNode json = parse(body);
            JEVisUserSQL user;
            String mfaToken = text(json, "mfaToken");
            if (mfaToken != null) {
                ds = new SQLDataSource(httpHeaders, request, url, false);
                user = AuthService.userFromMfaToken(ds, mfaToken);
            } else {
                ds = new SQLDataSource(httpHeaders, request, url);
                if (!isInteractive(ds)) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Not allowed for this login type").build();
                }
                user = ds.getCurrentUser();
            }
            List<String> codes;
            try {
                codes = AuthService.confirmTotp(ds, user, text(json, "code"));
            } catch (AuthenticationException ex) {
                registerFailure();
                return loginResponse(JsonLoginResult.failure(ex.getMessage()));
            }
            /* activation signed out all sessions, the calling client gets new tokens */
            JsonLoginResult result = AuthService.issueTokens(ds, user, json.path("remember").asBoolean(false), clientInfo(httpHeaders));
            result.setRecoveryCodes(codes);
            return loginResponse(result);
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("2FA confirmation failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/mfa/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mfaDisable(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url);
            if (!isInteractive(ds)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Not allowed for this login type").build();
            }
            JEVisUserSQL user = ds.getCurrentUser();
            if (AuthService.isMfaRequired(user)) {
                return Response.status(Response.Status.FORBIDDEN).entity("Two-factor authentication is required for this account").build();
            }
            if (AuthService.checkPassword(ds, user, text(json, "password")) != AuthService.PasswordCheck.OK
                    || !AuthService.verifySecondFactor(new org.jevis.commons.ws.sql.auth.AuthStore(ds.getConnection()), user.getUserID(), text(json, "code"))) {
                registerFailure();
                return loginResponse(JsonLoginResult.failure("Password or code is not correct."));
            }
            AuthService.disableMfa(ds, user.getUserID());
            return Response.noContent().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Disabling 2FA failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @DELETE
    @Logged
    @Path("/mfa/{userId}")
    public Response mfaReset(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, @PathParam("userId") long userId) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            if (!isInteractive(ds) || !ds.getCurrentUser().isSysAdmin()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            AuthService.disableMfa(ds, userId);
            AuthService.revokeAllForUser(ds, userId);
            logger.warn("Sys Admin '{}' reset the two-factor authentication of user {}", ds.getCurrentUser().getAccountName(), userId);
            return Response.noContent().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("2FA reset failed", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @GET
    @Logged
    @Path("/apikeys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listApiKeys(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            if (!isInteractive(ds) || !ds.getCurrentUser().isSysAdmin()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            return json(AuthService.listApiKeys(ds));
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Could not list API keys", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @POST
    @Logged
    @Path("/apikeys")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApiKey(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, String body) {
        try {
            JsonNode json = parse(body);
            ds = new SQLDataSource(httpHeaders, request, url);
            if (!isInteractive(ds) || !ds.getCurrentUser().isSysAdmin()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            JEVisUserSQL owner = CachedAccessControl.getInstance(ds, false).getUserById(json.path("userId").asLong(-1));
            String name = text(json, "name");
            if (owner == null || name == null || name.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("userId of an existing user and name are required").build();
            }
            Long expires = json.hasNonNull("expires") ? json.get("expires").asLong() : null;
            JsonApiKey key = AuthService.createApiKey(ds, owner, name.trim(), ds.getCurrentUser().getUserID(), expires);
            return json(key);
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (BadRequestException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Could not create API key", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @DELETE
    @Logged
    @Path("/apikeys/{id}")
    public Response revokeApiKey(@Context HttpHeaders httpHeaders, @Context Request request, @Context UriInfo url, @PathParam("id") String keyId) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            if (!isInteractive(ds) || !ds.getCurrentUser().isSysAdmin()) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            if (!AuthService.revokeApiKey(ds, keyId)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.noContent().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Could not revoke API key", ex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    private Response loginResponse(JsonLoginResult result) {
        if (result.getStatus() == JsonLoginResult.Status.FAILURE) {
            registerFailure();
            return Response.status(Response.Status.UNAUTHORIZED).entity(toJson(result)).type(MediaType.APPLICATION_JSON).build();
        }
        return json(result);
    }

    private String remoteAddress() {
        try {
            return grizzlyRequest != null ? grizzlyRequest.getRemoteAddr() : "unknown";
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private String clientInfo(HttpHeaders httpHeaders) {
        String agent = httpHeaders.getHeaderString(HttpHeaders.USER_AGENT);
        return remoteAddress() + (agent != null ? " " + agent : "");
    }

    private boolean isThrottled() {
        AtomicInteger failures = FAILURES.getIfPresent(remoteAddress());
        return failures != null && failures.get() >= MAX_FAILURES_PER_ADDRESS;
    }

    private void registerFailure() {
        try {
            FAILURES.get(remoteAddress(), AtomicInteger::new).incrementAndGet();
        } catch (Exception ex) {
            logger.debug(ex);
        }
    }

    @PostConstruct
    public void postConstruct() {
        if (ds != null) {
            ds.clear();
            ds = null;
        }
    }
}
