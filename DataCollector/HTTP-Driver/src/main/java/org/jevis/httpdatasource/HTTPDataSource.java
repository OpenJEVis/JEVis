package org.jevis.httpdatasource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSourceHelper;
import org.jevis.commons.driver.ParameterHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf, FS
 */
public class HTTPDataSource {
    private static final Logger logger = LogManager.getLogger(HTTPDataSource.class);

    private String serverURL;
    private Integer port;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private String userName;
    private String bearerAuthLoginUrl;
    private String password;
    private DateTimeZone timeZone;
    private Boolean ssl = false;
    private Boolean trustAllCertificates = false;
    private DateTime lastReadout;
    private DateTime endDateTime;
    private StatusLine statusLine;
    private AUTH_SCHEME authScheme;
    private Long id;
    private String name;
    private String bearerToken;
    private boolean urlNormalized = false;
    private boolean bearerInitialized = false;
    private CloseableHttpClient httpClient;

    public static String FixURL(String url) {
        url = url.replaceAll("(?<!(http:|https:))/+", "/");
        url = url.replaceAll(" ", "%20");
        return url;
    }

    /**
     * Extract a bearer token from an auth-endpoint response body.
     * Handles plain text, JSON-quoted strings, and JSON objects with
     * common token field names (access_token, token, bearerToken, ...).
     */
    static String extractBearerToken(String body) {
        if (body == null) {
            return null;
        }
        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.startsWith("{")) {
            for (String key : new String[]{"access_token", "token", "bearerToken", "bearer_token", "id_token", "jwt"}) {
                String pattern = "\"" + key + "\"";
                int idx = trimmed.indexOf(pattern);
                if (idx < 0) continue;
                int colon = trimmed.indexOf(':', idx + pattern.length());
                if (colon < 0) continue;
                int q1 = trimmed.indexOf('"', colon + 1);
                if (q1 < 0) continue;
                int q2 = trimmed.indexOf('"', q1 + 1);
                if (q2 < 0) continue;
                return trimmed.substring(q1 + 1, q2);
            }
            return trimmed;
        }

        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        return trimmed;
    }

    public DateTime getLastReadout() {
        return lastReadout;
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * @param channel
     * @return
     */
    public List<InputStream> sendSampleRequest(Channel channel) throws Exception {
        logger.info("sendSampleRequest to http channel: {}:{}", channel.getChannelObject().getName(), channel.getChannelObject().getID());

        String channelID = channel.getChannelObject().getID().toString();
        List<InputStream> answer = new ArrayList<>();

        this.statusLine = null;

        CloseableHttpClient client = getOrBuildHttpClient(channelID);

        String path = channel.getPath();
        lastReadout = channel.getLastReadout();

        endDateTime = getCurrentTime(channel.getChannelObject(), lastReadout);

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        ParameterHelper parameterHelper = new ParameterHelper(lastReadout, endDateTime);
        path = parameterHelper.getNewPath(path, channel.getChannelObject());

        logger.debug("[{}] Connection Setting: Server: {} User: {} PW: {} authScheme: {}", channelID, serverURL, userName, password, authScheme);
        PathFollower pathFollower = new PathFollower(channel.getChannelObject());

        normalizeServerURL(channelID);
        ensureBearerToken(client, channelID);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout * 1000)
                .setSocketTimeout(readTimeout * 1000)
                .build();

        String contentURL = path;
        contentURL = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), contentURL, timeZone);
        contentURL = HTTPDataSource.FixURL(contentURL);
        logger.debug("[{}] Channel URL: {}", channelID, contentURL);

        String getRequest;
        if (pathFollower.isActive()) {
            logger.debug("[{}] Using Dynamic Link", channelID);
            pathFollower.setConnection(client, requestConfig);
            getRequest = pathFollower.startFetching(serverURL, contentURL);
            logger.debug("[{}] Final target url after following links: {}", channelID, getRequest);
        } else {
            getRequest = serverURL + contentURL;
        }
        logger.info("[{}] Sending HTTP GET: {}", channelID, getRequest);

        HttpGetWithBody get = new HttpGetWithBody(getRequest);
        get.setConfig(requestConfig);
        if (authScheme.equals(AUTH_SCHEME.BEARER)) {
            if (bearerToken == null || bearerToken.isEmpty()) {
                logger.error("[{}] BEARER auth: no token available — sending request without Authorization header (will likely fail with 401)", channelID);
            } else {
                get.addHeader("Authorization", "Bearer " + bearerToken);
            }
            get.addHeader("Accept", "application/json");
            get.addHeader("Content-Type", "application/json");
            if (channel.getGetRequestBody() != null) {
                String requestBody = parameterHelper.fillVariables(channel.getGetRequestBody(), channel.getChannelObject());
                get.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            }
        }

        try {
            URL parsedUrl = new URL(getRequest);
            java.net.InetAddress resolved = java.net.InetAddress.getByName(parsedUrl.getHost());
            logger.info("[{}] Resolved host '{}' -> {}", channelID, parsedUrl.getHost(), resolved.getHostAddress());
        } catch (Exception dnsEx) {
            logger.warn("[{}] DNS resolution failed for URL '{}': {}", channelID, getRequest, dnsEx.getMessage());
        }

        try (CloseableHttpResponse oResponse = client.execute(get)) {
            statusLine = oResponse.getStatusLine();
            logger.info("[{}] HTTP response status code: {}", channelID, oResponse.getStatusLine());

            if (oResponse.getStatusLine().getStatusCode() == 200) {
                channel.setNextReadout(endDateTime);
            }
            HttpEntity oEntity = oResponse.getEntity();
            String oXmlString = EntityUtils.toString(oEntity);
            logger.debug("[{}] Content length to parse: {}", channelID, oXmlString.length());
            logger.debug("[{}] Content to parse: {}", channelID, oXmlString);
            EntityUtils.consume(oEntity);
            InputStream stream = new ByteArrayInputStream(oXmlString.getBytes(StandardCharsets.UTF_8));
            answer.add(stream);
        }

        return answer;
    }

    private CloseableHttpClient getOrBuildHttpClient(String channelID) throws Exception {
        if (httpClient != null) {
            return httpClient;
        }

        SSLConnectionSocketFactory sslSocketFactory;
        if (ssl && trustAllCertificates) {
            TrustStrategy trustStrategy = (certs, authType) -> true;
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, trustStrategy).build();
            sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            logger.info("[{}] trustAllCertificates=true — disabling certificate trust AND hostname verification", channelID);
        } else {
            sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        }

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(registry);
        pool.setMaxTotal(10);
        pool.setDefaultMaxPerRoute(5);

        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManager(pool);

        if (userName != null && !userName.isEmpty()) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(userName, password)
            );
            builder.setDefaultCredentialsProvider(provider);
        }

        httpClient = builder.build();
        return httpClient;
    }

    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (java.io.IOException e) {
                logger.warn("Error closing HTTP client: {}", e.getMessage());
            }
            httpClient = null;
        }
    }

    private void normalizeServerURL(String channelID) throws Exception {
        if (urlNormalized) {
            return;
        }
        if (ssl) {
            if (!serverURL.startsWith("https")) {
                serverURL = "https://" + serverURL;
            }
            if (trustAllCertificates) {
                /* Self-signed cert workaround — only when explicitly allowed. **/
                DataSourceHelper.doTrustToCertificates();
            }
        } else {
            if (!serverURL.startsWith("http")) {
                serverURL = "http://" + serverURL;
            }
        }

        /* Parse the URL and rebuild it with the port placed between host and path,
         * regardless of whether the user entered just a host, a host+port, or a full
         * URL with a path (e.g. "https://example.com/api"). */
        URL parsedServerURL = new URL(serverURL);
        String protocol = parsedServerURL.getProtocol();
        String host = parsedServerURL.getHost();
        int urlPort = parsedServerURL.getPort();
        String urlPath = parsedServerURL.getPath();

        int finalPort = (port != null && port > 0) ? port : urlPort;

        StringBuilder rebuilt = new StringBuilder();
        rebuilt.append(protocol).append("://").append(host);
        if (finalPort > -1) {
            rebuilt.append(':').append(finalPort);
        }
        if (urlPath != null && !urlPath.isEmpty()) {
            rebuilt.append(urlPath);
        }
        if (rebuilt.charAt(rebuilt.length() - 1) != '/') {
            rebuilt.append('/');
        }
        serverURL = rebuilt.toString();
        logger.debug("[{}] Server URL after port/path normalization: {}", channelID, serverURL);

        if (port == null && urlPort > -1) {
            logger.info("[{}] Port not set in Attribute, using port from URL: {}", channelID, urlPort);
            setPort(urlPort);
        }

        urlNormalized = true;
    }

    private void ensureBearerToken(CloseableHttpClient client, String channelID) {
        if (bearerInitialized) {
            return;
        }
        if (!authScheme.equals(AUTH_SCHEME.BEARER)) {
            bearerInitialized = true;
            return;
        }

        String tokenUrl = bearerAuthLoginUrl;
        logger.info("[{}] BEARER auth: requesting token from {}", channelID, tokenUrl);

        HttpPost post = new HttpPost(tokenUrl);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");
        final String json = "{\"username\":\"" + userName + "\",\"password\":\"" + password + "\",\"caller\":\"\"}";
        logger.debug("[{}] BEARER auth: POST body (password redacted): {\"username\":\"{}\",\"password\":\"***\",\"caller\":\"\"}", channelID, userName);
        final StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
        post.setEntity(entity);

        try (CloseableHttpResponse response = client.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = "";
            if (response.getEntity() != null) {
                body = EntityUtils.toString(response.getEntity());
            }
            logger.info("[{}] BEARER auth: token endpoint responded status={} bodyLength={}", channelID, statusCode, body.length());
            logger.debug("[{}] BEARER auth: raw response body: {}", channelID, body);

            if (statusCode == HttpStatus.SC_OK) {
                bearerToken = extractBearerToken(body);
                if (bearerToken == null || bearerToken.isEmpty()) {
                    logger.error("[{}] BEARER auth: token endpoint returned 200 but no token could be extracted from body: {}", channelID, body);
                } else {
                    logger.info("[{}] BEARER auth: token obtained (length={})", channelID, bearerToken.length());
                }
            } else {
                logger.error("[{}] BEARER auth: token request FAILED — status={} body={}", channelID, statusCode, body);
            }
        } catch (Exception ex) {
            logger.error("[{}] BEARER auth: exception while requesting token from {}: {}", channelID, tokenUrl, ex.getMessage(), ex);
        }
        bearerInitialized = true;
    }

    public AUTH_SCHEME getAuthScheme() {
        logger.debug("getAuthScheme()");

        /* Fallback for older Configuration were we only had BASIC auth*/
        if (authScheme == null) {
            if (userName != null && !userName.isEmpty()) {
                return AUTH_SCHEME.BASIC;
            }
            return AUTH_SCHEME.NONE;
        }

        return authScheme;
    }

    public void setBearerAuthLoginUrl(String _bearerAuthLoginUrl) {
        this.bearerAuthLoginUrl = _bearerAuthLoginUrl;
    }

    public void setAuthScheme(AUTH_SCHEME authScheme) {
        this.authScheme = authScheme;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setServerURL(String _serverURL) {
        this.serverURL = _serverURL;
    }

    public void setPort(Integer _port) {
        this.port = _port;
    }

    public void setConnectionTimeout(Integer _connectionTimeout) {
        this.connectionTimeout = _connectionTimeout;
    }

    public void setReadTimeout(Integer _readTimeout) {
        this.readTimeout = _readTimeout;
    }

    public void setUserName(String _userName) {
        this.userName = _userName;
    }

    public void setTrustAllCertificates(Boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    public void setPassword(String _password) {
        this.password = _password;
    }

    public void setSsl(Boolean _ssl) {
        this.ssl = _ssl;
    }

    private DateTime getCurrentTime(JEVisObject channel, DateTime lastReadout) {
        try {
            if (lastReadout == null) {
                return DateTime.now().withZone(getDateTimeZone());
            }
            JEVisAttribute chunkAttr = channel.getAttribute("Chunk Size(s)");
            if (chunkAttr != null && chunkAttr.hasSample()) {
                int chunkSeconds = chunkAttr.getLatestSample().getValueAsDouble().intValue();
                if (DateTime.now().isBefore(lastReadout.plusSeconds(chunkSeconds))) {
                    logger.debug("Channel {}:{} using now as current time", channel.getName(), channel.getID());
                    return DateTime.now();
                } else {
                    logger.debug("Channel {}:{} using now + chunk size in seconds as current time", channel.getName(), channel.getID());
                    return lastReadout.plusSeconds(chunkSeconds);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return DateTime.now().withZone(getDateTimeZone());
    }

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public enum AUTH_SCHEME {
        BASIC, DIGEST, BEARER, NONE
    }

    public DateTimeZone getDateTimeZone() {
        return timeZone;
    }

    public void setDateTimeZone(DateTimeZone timeZone) {
        logger.debug("TIMEZONE: {}", timeZone);
        this.timeZone = timeZone;
    }

    static class HttpGetWithBody extends HttpEntityEnclosingRequestBase {
        public HttpGetWithBody(String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return "GET";
        }
    }

    // interfaces
    interface HTTP extends DataCollectorTypes.DataSource.DataServer {

        String NAME = "HTTP Server";
        String PASSWORD = "Password";
        String SSL = "SSL";
        String USER = "User";
    }

    interface HTTPChannelDirectory extends DataCollectorTypes.ChannelDirectory {

        String NAME = "HTTP Channel Directory";
    }


    interface HTTPChannel extends DataCollectorTypes.Channel {

        String NAME = "HTTP Channel";
        String PATH = "Path";
    }
}
