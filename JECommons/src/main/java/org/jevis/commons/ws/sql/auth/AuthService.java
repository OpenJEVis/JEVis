package org.jevis.commons.ws.sql.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonApiKey;
import org.jevis.commons.ws.json.JsonLoginResult;
import org.jevis.commons.ws.json.JsonMfaStatus;
import org.jevis.commons.ws.sql.CachedAccessControl;
import org.jevis.commons.ws.sql.JEVisUserSQL;
import org.jevis.commons.ws.sql.SQLDataSource;

import javax.security.sasl.AuthenticationException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Central authentication logic of the JEWebService.
 *
 * <p>Supported credentials:
 * <ul>
 *     <li>{@code Authorization: Bearer jvs_...} access token, issued by {@code /auth/login}</li>
 *     <li>{@code Authorization: Bearer jvk_<id>.<secret>} API key of a service account</li>
 *     <li>{@code Authorization: Basic ...} legacy, only while {@link AuthConfig#isBasicEnabled()} and never for
 *     users with two-factor authentication</li>
 * </ul>
 *
 * <p>Tokens are random 256 bit values; only their SHA-256 hash is stored in the database. Validated credentials are
 * cached shortly in memory so the database and the (slow) password hash are not hit on every request.
 */
public final class AuthService {

    public static final String PREFIX_ACCESS = "jvs_";
    public static final String PREFIX_REFRESH = "jvr_";
    public static final String PREFIX_MFA = "jvm_";
    public static final String PREFIX_HANDOFF = "jvh_";
    public static final String PREFIX_API_KEY = "jvk_";
    public static final String MFA_ISSUER = "JEVis";

    private static final Logger logger = LogManager.getLogger(AuthService.class);
    private static final long MFA_TOKEN_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long HANDOFF_TOKEN_MS = TimeUnit.SECONDS.toMillis(60);
    /**
     * Absolute lifetime of an access token, independent of the sliding expiry.
     */
    private static final long ACCESS_TOKEN_MAX_MS = TimeUnit.DAYS.toMillis(7);
    private static final long TOUCH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long CLEANUP_INTERVAL_MS = TimeUnit.HOURS.toMillis(1);
    private static final int RECOVERY_CODES = 10;

    /**
     * token hash -> validated access token
     */
    private static final Cache<String, CachedToken> ACCESS_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10_000).expireAfterWrite(5, TimeUnit.MINUTES).build();
    /**
     * hash of the Basic credentials -> user id. Avoids an Argon2 calculation per request for legacy clients.
     */
    private static final Cache<String, Long> BASIC_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10_000).expireAfterWrite(60, TimeUnit.SECONDS).build();
    /**
     * hash of the full API key -> user id
     */
    private static final Cache<String, Long> API_KEY_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10_000).expireAfterWrite(60, TimeUnit.SECONDS).build();
    private static final Set<String> BASIC_WARNED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    /**
     * Argon2 hash of a random password, used to keep the response time for unknown users similar.
     */
    private static final String DUMMY_HASH = PasswordHasher.hash(AuthCrypto.base64Url(AuthCrypto.randomBytes(16)));
    private static volatile long lastCleanup = 0;

    private AuthService() {
    }

    private static AuthStore store(SQLDataSource ds) throws SQLException {
        return new AuthStore(ds.getConnection());
    }

    private static CachedAccessControl cac(SQLDataSource ds) throws AuthenticationException {
        try {
            return CachedAccessControl.getInstance(ds, false);
        } catch (Exception ex) {
            logger.error("Could not load user cache", ex);
            throw new AuthenticationException("Authentication is not available");
        }
    }

    /* =================================================================== request authentication */

    /**
     * @return {@code true} if the auth tables exist (migration V10 applied)
     */
    public static boolean isAvailable(SQLDataSource ds) {
        try {
            return store(ds).isSchemaAvailable();
        } catch (SQLException ex) {
            logger.error("Could not check auth tables", ex);
            return false;
        }
    }

    /**
     * Authenticates a request with an access token or an API key.
     */
    public static JEVisUserSQL authenticateBearer(SQLDataSource ds, String token) throws AuthenticationException {
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("Bearer token is missing");
        }
        if (token.startsWith(PREFIX_API_KEY)) {
            return authenticateApiKey(ds, token);
        }
        if (!token.startsWith(PREFIX_ACCESS)) {
            throw new AuthenticationException("Unknown token type");
        }
        try {
            String hash = AuthCrypto.sha256Hex(token);
            long now = System.currentTimeMillis();
            CachedToken cached = ACCESS_CACHE.getIfPresent(hash);
            if (cached == null) {
                AuthStore store = store(ds);
                if (!store.isSchemaAvailable()) throw new AuthenticationException("Token login is not available");
                AuthStore.TokenRow row = store.findToken(hash);
                if (row == null || !row.isValid(AuthStore.TOKEN_ACCESS)) {
                    throw new AuthenticationException("Token is invalid or expired");
                }
                cached = new CachedToken(row.userId, row.created, row.expires);
                ACCESS_CACHE.put(hash, cached);
            }
            if (cached.expires <= now) {
                ACCESS_CACHE.invalidate(hash);
                throw new AuthenticationException("Token is invalid or expired");
            }

            JEVisUserSQL user = cac(ds).getUserById(cached.userId);
            if (user == null || !user.isEnabled()) {
                throw new AuthenticationException("User is unknown or disabled");
            }

            /* sliding expiration, written to the database at most once per minute */
            if (now - cached.lastTouch > TOUCH_INTERVAL_MS) {
                long newExpires = Math.min(now + TimeUnit.MINUTES.toMillis(AuthConfig.getAccessTimeoutMinutes()), cached.created + ACCESS_TOKEN_MAX_MS);
                store(ds).touchToken(hash, newExpires);
                cached.expires = newExpires;
                cached.lastTouch = now;
            }
            return user;
        } catch (SQLException ex) {
            logger.error("Token check failed", ex);
            throw new AuthenticationException("Token check failed");
        }
    }

    private static JEVisUserSQL authenticateApiKey(SQLDataSource ds, String apiKey) throws AuthenticationException {
        String cacheKey = AuthCrypto.sha256Hex(apiKey);
        Long userId = API_KEY_CACHE.getIfPresent(cacheKey);
        if (userId == null) {
            try {
                String body = apiKey.substring(PREFIX_API_KEY.length());
                int dot = body.indexOf('.');
                if (dot <= 0) throw new AuthenticationException("Malformed API key");
                String keyId = body.substring(0, dot);
                String secret = body.substring(dot + 1);

                AuthStore store = store(ds);
                if (!store.isSchemaAvailable()) throw new AuthenticationException("API keys are not available");
                AuthStore.ApiKeyRow row = store.findApiKey(keyId);
                if (row == null || row.revoked
                        || (row.expires != null && row.expires <= System.currentTimeMillis())
                        || !AuthCrypto.constantTimeEquals(row.secretHash, AuthCrypto.sha256Hex(secret))) {
                    logger.warn("Rejected API key '{}'", keyId);
                    throw new AuthenticationException("API key is invalid");
                }
                store.touchApiKey(keyId);
                userId = row.userId;
                API_KEY_CACHE.put(cacheKey, userId);
            } catch (SQLException ex) {
                logger.error("API key check failed", ex);
                throw new AuthenticationException("API key check failed");
            }
        }
        JEVisUserSQL user = cac(ds).getUserById(userId);
        if (user == null || !user.isEnabled()) {
            throw new AuthenticationException("Service user is unknown or disabled");
        }
        return user;
    }

    /* =================================================================== passwords */

    /**
     * Legacy HTTP Basic authentication. Rejected for users with (required) two-factor authentication, otherwise
     * Basic auth would be a way around the second factor.
     */
    public static JEVisUserSQL authenticateBasic(final SQLDataSource ds, final String username, final String password) throws AuthenticationException {
        if (!AuthConfig.isBasicEnabled()) {
            throw new AuthenticationException("Basic authentication is disabled, use /JEWebService/v1/auth/login");
        }
        final String cacheKey = AuthCrypto.sha256Hex(username.toLowerCase(Locale.ROOT) + "\u0000" + password);
        Long userId;
        try {
            /* get() with loader: concurrent requests with the same credentials only calculate the hash once */
            userId = BASIC_CACHE.get(cacheKey, () -> {
                JEVisUserSQL user = cac(ds).getUser(username);
                PasswordCheck check = checkPassword(ds, user, password);
                if (check != PasswordCheck.OK) {
                    throw new AuthenticationException("Username/Password is not correct.");
                }
                if (isAvailable(ds) && (hasMfa(store(ds), user.getUserID()) || isMfaRequired(user))) {
                    logger.warn("Basic login rejected for '{}': two-factor authentication is enabled or required for this user. "
                            + "Use a current client or an API key.", username);
                    throw new AuthenticationException("Two-factor authentication required, Basic authentication is not allowed");
                }
                return user.getUserID();
            });
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof AuthenticationException) {
                throw (AuthenticationException) ex.getCause();
            }
            logger.error("Basic login failed", ex);
            throw new AuthenticationException("Username/Password is not correct.");
        } catch (com.google.common.util.concurrent.UncheckedExecutionException ex) {
            logger.error("Basic login failed", ex);
            throw new AuthenticationException("Username/Password is not correct.");
        }

        JEVisUserSQL user = cac(ds).getUserById(userId);
        if (user == null || !user.isEnabled()) {
            BASIC_CACHE.invalidate(cacheKey);
            throw new AuthenticationException("Username/Password is not correct.");
        }
        if (BASIC_WARNED.add(user.getAccountName())) {
            logger.warn("User '{}' uses deprecated HTTP Basic authentication. Update the client (JECC, launcher) or switch services to API keys.", user.getAccountName());
        }
        return user;
    }

    /**
     * Verifies the password of a user, including the enabled flag and the lockout.
     *
     * <p>Counts failures, resets the counter on success and transparently replaces outdated hashes. If the user has
     * no entry in {@code user_credential} yet the legacy password sample is used and migrated.
     */
    public static PasswordCheck checkPassword(SQLDataSource ds, JEVisUserSQL user, String password) throws SQLException {
        if (user == null) {
            /* calculate a hash anyway, so the response time does not reveal whether the user exists */
            PasswordHasher.verify(password, DUMMY_HASH);
            return PasswordCheck.UNKNOWN;
        }
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) {
            if (!PasswordHasher.verify(password, user.getPassword())) return PasswordCheck.WRONG;
            return user.isEnabled() ? PasswordCheck.OK : PasswordCheck.DISABLED;
        }

        AuthStore.Credential credential = store.getCredential(user.getUserID());
        if (credential == null) {
            /* user without migrated password (created before V10 or by direct import) */
            if (!PasswordHasher.verify(password, user.getPassword())) {
                logger.warn("Login failed, wrong password for: '{}'", user.getAccountName());
                return PasswordCheck.WRONG;
            }
            store.setPassword(user.getUserID(), PasswordHasher.hash(password), PasswordHasher.ALGO_ARGON2ID, false);
            logger.info("Migrated password of '{}' to user_credential", user.getAccountName());
        } else {
            if (credential.isLocked()) {
                logger.warn("Login failed, user is locked: '{}'", user.getAccountName());
                return PasswordCheck.LOCKED;
            }
            if (!PasswordHasher.verify(password, credential.passwordHash)) {
                logger.warn("Login failed, wrong password for: '{}'", user.getAccountName());
                store.recordFailure(user.getUserID(), AuthConfig.getLockoutThreshold(), AuthConfig.getLockoutMinutes());
                return PasswordCheck.WRONG;
            }
            store.resetFailures(user.getUserID());
            if (PasswordHasher.needsRehash(credential.passwordHash)) {
                store.updateHash(user.getUserID(), PasswordHasher.hash(password), PasswordHasher.ALGO_ARGON2ID);
                logger.info("Upgraded password hash of '{}' to argon2id", user.getAccountName());
            }
        }
        if (!user.isEnabled()) {
            logger.warn("Login failed, user is disabled: '{}'", user.getAccountName());
            return PasswordCheck.DISABLED;
        }
        return PasswordCheck.OK;
    }

    /**
     * Sets a new password and signs out all sessions of the user.
     *
     * @return {@code true} if the password was stored in {@code user_credential}, {@code false} if the auth tables
     * do not exist yet and the caller has to store the legacy hash
     */
    public static boolean setPassword(SQLDataSource ds, long userId, String newPassword) throws SQLException {
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) return false;
        store.setPassword(userId, PasswordHasher.hash(newPassword), PasswordHasher.ALGO_ARGON2ID, false);
        revokeAllForUser(ds, userId);
        return true;
    }

    /* =================================================================== login flow */

    /**
     * Revokes all tokens of a user and drops all cached credentials.
     */
    public static void revokeAllForUser(SQLDataSource ds, long userId) throws SQLException {
        store(ds).revokeAllTokens(userId);
        for (Map.Entry<String, CachedToken> entry : ACCESS_CACHE.asMap().entrySet()) {
            if (entry.getValue().userId == userId) {
                ACCESS_CACHE.invalidate(entry.getKey());
            }
        }
        BASIC_CACHE.invalidateAll();
    }

    /**
     * First login step with user name and password.
     */
    public static JsonLoginResult login(SQLDataSource ds, String username, String password, boolean remember, String clientInfo) throws SQLException, AuthenticationException {
        cleanup(ds);
        if (username == null || password == null) return JsonLoginResult.failure("Username/Password is not correct.");
        CachedAccessControl cac = cac(ds);
        JEVisUserSQL user = cac.getUser(username);
        PasswordCheck check = checkPassword(ds, user, password);
        switch (check) {
            case OK:
                break;
            case LOCKED:
                return JsonLoginResult.failure("Account is temporarily locked because of too many failed logins.");
            default:
                return JsonLoginResult.failure("Username/Password is not correct.");
        }

        AuthStore store = store(ds);
        if (hasMfa(store, user.getUserID())) {
            return mfaResult(store, user, JsonLoginResult.Status.MFA_REQUIRED, clientInfo);
        }
        if (isMfaRequired(user)) {
            return mfaResult(store, user, JsonLoginResult.Status.MFA_ENROLLMENT_REQUIRED, clientInfo);
        }
        return issueTokens(store, user, remember, clientInfo);
    }

    /**
     * Second login step with the code of the authenticator app or a recovery code.
     */
    public static JsonLoginResult completeMfa(SQLDataSource ds, String mfaToken, String code, boolean remember, String clientInfo) throws SQLException, AuthenticationException {
        AuthStore store = store(ds);
        JEVisUserSQL user = userFromMfaToken(ds, mfaToken);
        AuthStore.Credential credential = store.getCredential(user.getUserID());
        if (credential != null && credential.isLocked()) {
            return JsonLoginResult.failure("Account is temporarily locked because of too many failed logins.");
        }
        if (!verifySecondFactor(store, user.getUserID(), code)) {
            logger.warn("Wrong 2FA code for '{}'", user.getAccountName());
            if (credential != null) {
                store.recordFailure(user.getUserID(), AuthConfig.getLockoutThreshold(), AuthConfig.getLockoutMinutes());
            }
            return JsonLoginResult.failure("The code is not correct.");
        }
        if (!store.consumeToken(AuthCrypto.sha256Hex(mfaToken), AuthStore.TOKEN_MFA)) {
            return JsonLoginResult.failure("The login has expired, please login again.");
        }
        store.resetFailures(user.getUserID());
        return issueTokens(store, user, remember, clientInfo);
    }

    /**
     * Issues a new access token for a valid refresh token ("remember me"). The refresh token stays valid.
     */
    public static JsonLoginResult refresh(SQLDataSource ds, String refreshToken, String clientInfo) throws SQLException, AuthenticationException {
        if (refreshToken == null || !refreshToken.startsWith(PREFIX_REFRESH))
            return JsonLoginResult.failure("Invalid refresh token");
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) return JsonLoginResult.failure("Token login is not available");
        String hash = AuthCrypto.sha256Hex(refreshToken);
        AuthStore.TokenRow row = store.findToken(hash);
        if (row == null || !row.isValid(AuthStore.TOKEN_REFRESH))
            return JsonLoginResult.failure("Invalid refresh token");
        JEVisUserSQL user = cac(ds).getUserById(row.userId);
        if (user == null || !user.isEnabled()) return JsonLoginResult.failure("User is unknown or disabled");
        store.touchToken(hash, row.expires);

        JsonLoginResult result = issueTokens(store, user, false, clientInfo);
        result.setRefreshToken(refreshToken);
        return result;
    }

    /**
     * Revokes the given tokens. Unknown tokens are ignored.
     */
    public static void logout(SQLDataSource ds, String accessToken, String refreshToken) throws SQLException {
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) return;
        if (accessToken != null && accessToken.startsWith(PREFIX_ACCESS)) {
            String hash = AuthCrypto.sha256Hex(accessToken);
            store.revokeToken(hash);
            ACCESS_CACHE.invalidate(hash);
        }
        if (refreshToken != null && refreshToken.startsWith(PREFIX_REFRESH)) {
            store.revokeToken(AuthCrypto.sha256Hex(refreshToken));
        }
    }

    /**
     * Creates a single-use code (60 s) that another program of the same user can exchange for an own access token,
     * used by the launcher to start JECC without passing a password or token on the command line.
     */
    public static String createHandoff(SQLDataSource ds, JEVisUserSQL user, String clientInfo) throws SQLException {
        String code = AuthCrypto.randomToken(PREFIX_HANDOFF);
        store(ds).insertToken(AuthCrypto.sha256Hex(code), user.getUserID(), AuthStore.TOKEN_HANDOFF,
                System.currentTimeMillis() + HANDOFF_TOKEN_MS, clientInfo);
        return code;
    }

    public static JsonLoginResult redeemHandoff(SQLDataSource ds, String code, String clientInfo) throws SQLException, AuthenticationException {
        if (code == null || !code.startsWith(PREFIX_HANDOFF)) return JsonLoginResult.failure("Invalid code");
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) return JsonLoginResult.failure("Token login is not available");
        String hash = AuthCrypto.sha256Hex(code);
        AuthStore.TokenRow row = store.findToken(hash);
        if (row == null || !row.isValid(AuthStore.TOKEN_HANDOFF) || !store.consumeToken(hash, AuthStore.TOKEN_HANDOFF)) {
            return JsonLoginResult.failure("Invalid or expired code");
        }
        JEVisUserSQL user = cac(ds).getUserById(row.userId);
        if (user == null || !user.isEnabled()) return JsonLoginResult.failure("User is unknown or disabled");
        /* refresh token stays in memory of the started client, so its session can be renewed */
        return issueTokens(store, user, true, clientInfo);
    }

    /**
     * Issues new tokens for an already authenticated user, e.g. after a password change or 2FA activation, which
     * sign out all sessions.
     */
    public static JsonLoginResult issueTokens(SQLDataSource ds, JEVisUserSQL user, boolean remember, String clientInfo) throws SQLException {
        return issueTokens(store(ds), user, remember, clientInfo);
    }

    private static JsonLoginResult issueTokens(AuthStore store, JEVisUserSQL user, boolean remember, String clientInfo) throws SQLException {
        long now = System.currentTimeMillis();
        long accessMs = TimeUnit.MINUTES.toMillis(AuthConfig.getAccessTimeoutMinutes());
        String accessToken = AuthCrypto.randomToken(PREFIX_ACCESS);
        store.insertToken(AuthCrypto.sha256Hex(accessToken), user.getUserID(), AuthStore.TOKEN_ACCESS, now + accessMs, clientInfo);

        JsonLoginResult result = new JsonLoginResult();
        result.setStatus(JsonLoginResult.Status.SUCCESS);
        result.setAccessToken(accessToken);
        result.setExpiresIn(accessMs / 1000);
        result.setUserId(user.getUserID());
        result.setUserName(user.getAccountName());

        if (remember) {
            String refreshToken = AuthCrypto.randomToken(PREFIX_REFRESH);
            store.insertToken(AuthCrypto.sha256Hex(refreshToken), user.getUserID(), AuthStore.TOKEN_REFRESH,
                    now + TimeUnit.DAYS.toMillis(AuthConfig.getRefreshTimeoutDays()), clientInfo);
            result.setRefreshToken(refreshToken);
        }
        AuthStore.Credential credential = store.getCredential(user.getUserID());
        if (credential != null && credential.mustChange) {
            result.setMustChangePassword(true);
        }
        logger.info("Login successful: '{}' ({})", user.getAccountName(), clientInfo);
        return result;
    }

    private static JsonLoginResult mfaResult(AuthStore store, JEVisUserSQL user, JsonLoginResult.Status status, String clientInfo) throws SQLException {
        String mfaToken = AuthCrypto.randomToken(PREFIX_MFA);
        store.insertToken(AuthCrypto.sha256Hex(mfaToken), user.getUserID(), AuthStore.TOKEN_MFA,
                System.currentTimeMillis() + MFA_TOKEN_MS, clientInfo);
        JsonLoginResult result = new JsonLoginResult();
        result.setStatus(status);
        result.setMfaToken(mfaToken);
        result.setUserId(user.getUserID());
        result.setUserName(user.getAccountName());
        return result;
    }

    /* =================================================================== two-factor authentication */

    private static void cleanup(SQLDataSource ds) {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_INTERVAL_MS) return;
        lastCleanup = now;
        try {
            AuthStore store = store(ds);
            if (store.isSchemaAvailable()) {
                int deleted = store.deleteExpiredTokens();
                logger.debug("Deleted {} expired tokens", deleted);
            }
        } catch (SQLException ex) {
            logger.warn("Could not delete expired tokens: {}", ex.getMessage());
        }
    }

    /**
     * @return {@code true} if the user has confirmed two-factor authentication
     */
    public static boolean hasMfa(AuthStore store, long userId) throws SQLException {
        AuthStore.MfaRow row = store.getTotp(userId);
        return row != null && row.confirmed;
    }

    /**
     * @return {@code true} if the configured policy requires two-factor authentication for this user
     */
    public static boolean isMfaRequired(JEVisUserSQL user) {
        switch (AuthConfig.getMfaPolicy()) {
            case ALL:
                return true;
            case SYSADMIN:
                return user.isSysAdmin();
            default:
                return false;
        }
    }

    /**
     * Resolves the user of a not yet used login token of the first step (does not consume it).
     */
    public static JEVisUserSQL userFromMfaToken(SQLDataSource ds, String mfaToken) throws SQLException, AuthenticationException {
        if (mfaToken == null || !mfaToken.startsWith(PREFIX_MFA))
            throw new AuthenticationException("Invalid login token");
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) throw new AuthenticationException("Two-factor authentication is not available");
        AuthStore.TokenRow row = store.findToken(AuthCrypto.sha256Hex(mfaToken));
        if (row == null || !row.isValid(AuthStore.TOKEN_MFA))
            throw new AuthenticationException("The login has expired, please login again.");
        JEVisUserSQL user = cac(ds).getUserById(row.userId);
        if (user == null || !user.isEnabled()) throw new AuthenticationException("User is unknown or disabled");
        return user;
    }

    public static JsonMfaStatus getMfaStatus(SQLDataSource ds, JEVisUserSQL user) throws SQLException {
        AuthStore store = store(ds);
        JsonMfaStatus status = new JsonMfaStatus();
        status.setRequired(isMfaRequired(user));
        if (!store.isSchemaAvailable()) {
            status.setEnabled(false);
            return status;
        }
        boolean enabled = hasMfa(store, user.getUserID());
        status.setEnabled(enabled);
        if (enabled) status.setRecoveryCodesLeft(store.countUnusedRecoveryCodes(user.getUserID()));
        return status;
    }

    /**
     * Starts the TOTP setup: creates a new secret which is active after {@link #confirmTotp}.
     */
    public static JsonMfaStatus setupTotp(SQLDataSource ds, JEVisUserSQL user) throws SQLException, AuthenticationException {
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) throw new AuthenticationException("Two-factor authentication is not available");
        if (hasMfa(store, user.getUserID()))
            throw new AuthenticationException("Two-factor authentication is already enabled");
        byte[] secret = TotpService.generateSecret();
        if (!store.saveUnconfirmedTotp(user.getUserID(), MfaSecretCipher.encrypt(user.getUserID(), secret))) {
            throw new AuthenticationException("Two-factor authentication is already enabled");
        }
        JsonMfaStatus status = new JsonMfaStatus();
        status.setEnabled(false);
        status.setRequired(isMfaRequired(user));
        status.setSecret(AuthCrypto.base32Encode(secret));
        status.setOtpAuthUri(TotpService.buildOtpAuthUri(MFA_ISSUER, user.getAccountName(), secret));
        return status;
    }

    /**
     * Activates the TOTP secret of {@link #setupTotp} with a first valid code, signs out all other sessions and
     * returns the new recovery codes (shown once).
     */
    public static List<String> confirmTotp(SQLDataSource ds, JEVisUserSQL user, String code) throws SQLException, AuthenticationException {
        AuthStore store = store(ds);
        AuthStore.MfaRow row = store.getTotp(user.getUserID());
        if (row == null) throw new AuthenticationException("Two-factor setup was not started");
        if (row.confirmed) throw new AuthenticationException("Two-factor authentication is already enabled");
        byte[] secret = MfaSecretCipher.decrypt(user.getUserID(), row.secretEnc);
        long step = TotpService.verify(secret, code, row.lastUsedStep, TotpService.currentStep());
        if (step < 0 || !store.advanceTotpStep(user.getUserID(), step)) {
            throw new AuthenticationException("The code is not correct.");
        }
        store.confirmTotp(user.getUserID());

        List<String> codes = new ArrayList<>();
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODES; i++) {
            String raw = AuthCrypto.base32Encode(AuthCrypto.randomBytes(5));
            codes.add(raw.substring(0, 4) + "-" + raw.substring(4));
            hashes.add(AuthCrypto.sha256Hex(normalizeRecoveryCode(raw)));
        }
        store.replaceRecoveryCodes(user.getUserID(), hashes);
        revokeAllForUser(ds, user.getUserID());
        logger.info("Two-factor authentication enabled for '{}'", user.getAccountName());
        return codes;
    }

    /**
     * Removes two-factor authentication of a user (after the caller checked password and code, or for a sysadmin
     * reset).
     */
    public static void disableMfa(SQLDataSource ds, long userId) throws SQLException {
        store(ds).deleteMfa(userId);
        logger.info("Two-factor authentication removed for user {}", userId);
    }

    /**
     * Checks a TOTP code or a recovery code. Used codes can not be used again.
     */
    public static boolean verifySecondFactor(AuthStore store, long userId, String code) throws SQLException {
        if (code == null) return false;
        AuthStore.MfaRow row = store.getTotp(userId);
        if (row == null || !row.confirmed) return false;
        String clean = code.replace(" ", "").trim();
        if (clean.length() == TotpService.DIGITS) {
            byte[] secret = MfaSecretCipher.decrypt(userId, row.secretEnc);
            long step = TotpService.verify(secret, clean, row.lastUsedStep, TotpService.currentStep());
            return step >= 0 && store.advanceTotpStep(userId, step);
        }
        return store.useRecoveryCode(userId, AuthCrypto.sha256Hex(normalizeRecoveryCode(clean)));
    }

    /* =================================================================== API keys */

    private static String normalizeRecoveryCode(String code) {
        return code.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Creates a new API key for a (service) user. The returned object contains the key, it can not be read again.
     */
    public static JsonApiKey createApiKey(SQLDataSource ds, JEVisUserSQL user, String name, Long createdBy, Long expiresMs) throws SQLException, AuthenticationException {
        AuthStore store = store(ds);
        if (!store.isSchemaAvailable()) throw new AuthenticationException("API keys are not available");
        String keyId = AuthCrypto.base32Encode(AuthCrypto.randomBytes(10)).toLowerCase(Locale.ROOT);
        String secret = AuthCrypto.base64Url(AuthCrypto.randomBytes(32));
        store.insertApiKey(keyId, AuthCrypto.sha256Hex(secret), user.getUserID(), name, createdBy, expiresMs);
        logger.info("Created API key '{}' ({}) for '{}'", name, keyId, user.getAccountName());

        JsonApiKey key = toJson(store.findApiKey(keyId), user);
        key.setKey(PREFIX_API_KEY + keyId + "." + secret);
        return key;
    }

    public static List<JsonApiKey> listApiKeys(SQLDataSource ds) throws SQLException, AuthenticationException {
        AuthStore store = store(ds);
        List<JsonApiKey> keys = new ArrayList<>();
        if (!store.isSchemaAvailable()) return keys;
        CachedAccessControl cac = cac(ds);
        for (AuthStore.ApiKeyRow row : store.listApiKeys()) {
            keys.add(toJson(row, cac.getUserById(row.userId)));
        }
        return keys;
    }

    public static boolean revokeApiKey(SQLDataSource ds, String keyId) throws SQLException {
        boolean revoked = store(ds).revokeApiKey(keyId);
        API_KEY_CACHE.invalidateAll();
        if (revoked) logger.info("Revoked API key {}", keyId);
        return revoked;
    }

    /* =================================================================== types */

    private static JsonApiKey toJson(AuthStore.ApiKeyRow row, JEVisUserSQL user) {
        JsonApiKey key = new JsonApiKey();
        key.setId(row.keyId);
        key.setName(row.name);
        key.setUserId(row.userId);
        key.setUserName(user != null ? user.getAccountName() : null);
        key.setCreated(row.created);
        key.setCreatedBy(row.createdBy);
        key.setExpires(row.expires);
        key.setLastUsed(row.lastUsed);
        key.setRevoked(row.revoked);
        return key;
    }

    public enum PasswordCheck {
        OK, WRONG, LOCKED, DISABLED, UNKNOWN
    }

    private static final class CachedToken {
        final long userId;
        final long created;
        volatile long expires;
        volatile long lastTouch;

        CachedToken(long userId, long created, long expires) {
            this.userId = userId;
            this.created = created;
            this.expires = expires;
            this.lastTouch = 0;
        }
    }
}
