package org.jevis.httpdatasource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSourceHelper;
import org.jevis.commons.driver.ParameterHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    private String password;
    private DateTimeZone timeZone;
    private Boolean ssl = false;
    private DateTime lastReadout;
    private DateTime endDateTime;
    private StatusLine statusLine;
    private AUTH_SCHEME authScheme;
    private Long id;
    private String name;
    private boolean needUrlConfig = true;

    public static String FixURL(String url) {
        url = url.replaceAll("(?<!(http:|https:))/+", "/");
        url = url.replaceAll(" ", "%20");
        return url;
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
        List<InputStream> answer = new ArrayList<InputStream>();

        String path = channel.getPath();
        lastReadout = channel.getLastReadout();

        if (path == null || path.isEmpty()) {
            logger.debug("[{}] Path is null or empty, aborting sendSampleRequest", channelID);
            return answer;
        }
        if (lastReadout == null) {
            logger.debug("[{}] lastReadout is null, aborting sendSampleRequest", channelID);
            return answer;
        }

        endDateTime = getCurrentTime(channel.getChannelObject(), lastReadout);

        if (endDateTime == null) {
            logger.debug("[{}] endDateTime is null, aborting sendSampleRequest", channelID);
            return answer;
        }

        logger.debug("[{}] Time range: lastReadout={} endDateTime={}", channelID, lastReadout, endDateTime);

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        ParameterHelper parameterHelper = new ParameterHelper(lastReadout, endDateTime);
        path = parameterHelper.getNewPath(path, channel.getChannelObject());

        logger.debug("[{}] Connection setting: server={} user={} authScheme={} connectTimeout={}s readTimeout={}s",
                channelID, serverURL, userName, authScheme, connectionTimeout, readTimeout);
        PathFollower pathFollower = new PathFollower(channel.getChannelObject());

        if (needUrlConfig) {/*only the first channel needs to configure the server url*/
            if (ssl) {/* Workaround if the protocol is not in the url**/
                if (!serverURL.startsWith("https")) {
                    serverURL = "https://" + serverURL;
                    logger.debug("[{}] Added https:// prefix to serverURL", channelID);
                }
                /* We trust self signed certificates for now, this way is not save **/
                DataSourceHelper.doTrustToCertificates();
            } else {
                if (!serverURL.startsWith("http")) {
                    serverURL = "http://" + serverURL;
                    logger.debug("[{}] Added http:// prefix to serverURL", channelID);
                }
            }

            if (serverURL.endsWith("/")) {
                serverURL = serverURL.substring(0, serverURL.length() - 1);
            }

            if (port != null) {
                serverURL += ":" + port;
            }

            serverURL += "/";


            /** Fallback if the URL does contain the port and the Port attribute has none **/
            URL url = new URL(serverURL);
            if (port == null && url.getPort() > -1) {
                logger.info("[{}] Port not set in Attribute, using port from URL: {}", channelID, url.getPort());
                setPort(url.getPort());
            }
            logger.debug("[{}] Server URL configured: {}", channelID, serverURL);
            needUrlConfig = false;
        }


        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout * 1000)
                .setSocketTimeout(readTimeout * 1000)
                .build();

        //HttpHost targetHost = new HttpHost(url.getHost(), port, url.getProtocol());

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new String[]{"TLSv1.2", "TLSv1.3"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()
        );

        CloseableHttpClient httpClient;
        if (userName != null && !userName.isEmpty()) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(userName, password)
            );
            httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(sslsf)
                    .setDefaultCredentialsProvider(provider)
                    .build();
        } else {
            httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(sslsf)
                    .build();
        }

        String contentURL = path;
        contentURL = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), contentURL, timeZone);
        contentURL = HTTPDataSource.FixURL(contentURL);
        logger.debug("[{}] Channel URL after parameter substitution: {}", channelID, contentURL);

        String getRequest = "";
        if (pathFollower.isActive()) {
            logger.debug("[{}] Using Dynamic Link follower", channelID);
            pathFollower.setConnection(httpClient, requestConfig);
            getRequest = pathFollower.startFetching(serverURL, contentURL);
            logger.debug("[{}] Final target URL after following links: {}", channelID, getRequest);
        } else {
            getRequest = serverURL + contentURL;
        }
        logger.info("[{}] Sending HTTP GET: {}", channelID, getRequest);

        HttpGet get = new HttpGet(getRequest);
        get.setConfig(requestConfig);
        /* will ne needed soon for the login/session function*/
        //get.setHeader("Cookie","rumo.https.sid=s%3AFBq0m630dDxN_sxFE_mruwf0WZ1BwVCZ.wxdwlbmPdmIVjN3c35xDA%2BbaaO630r9F5USbw5oq7Po;");
        //System.out.println("Header: \n"+get.getAllHeaders());

        HttpResponse oResponse = httpClient.execute(get);

        statusLine = oResponse.getStatusLine();

        logger.info("[{}] HTTP response: {}", channelID, oResponse.getStatusLine());
        logger.debug("[{}] Response content-type: {}", channelID,
                oResponse.containsHeader("Content-Type") ? oResponse.getFirstHeader("Content-Type").getValue() : "n/a");

        if (oResponse.getStatusLine().getStatusCode() == 200) {
            channel.setNextReadout(endDateTime);
        }
        HttpEntity oEntity = oResponse.getEntity();
        String oXmlString = EntityUtils.toString(oEntity);
        logger.debug("[{}] Response content length: {} chars", channelID, oXmlString.length());
        logger.debug("[{}] Response content: {}", channelID, oXmlString);
        EntityUtils.consume(oEntity);
        InputStream stream = new ByteArrayInputStream(oXmlString.getBytes(StandardCharsets.UTF_8));
        answer.add(stream);

        logger.info("[{}] sendSampleRequest completed, {} stream(s) returned", channelID, answer.size());
        return answer;
    }

    public AUTH_SCHEME getAuthScheme() {
        logger.debug("getAuthScheme()");

        /* Fallback for older Configuration were we only had BASIC auth*/
        if (authScheme == null) {
            if (userName != null && !userName.isEmpty()) {
                return AUTH_SCHEME.BASIC;
            }
        }

        return authScheme;
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

    public void setPassword(String _password) {
        this.password = _password;
    }

    public void setSsl(Boolean _ssl) {
        this.ssl = _ssl;
    }

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    private DateTime getCurrentTime(JEVisObject channel, DateTime lastReadout) {
        try {

            if (channel.getAttribute("Chunk Size(s)").hasSample()) {
                if (DateTime.now().isBefore(lastReadout.plusSeconds(channel.getAttribute("Chunk Size(s)").getLatestSample().getValueAsDouble().intValue()))) {
                    return DateTime.now();
                } else {
                    logger.debug("plusSeconds(channel.getAttribute(\"Chunk Size(s)\").getLatestSample().getValueAsDouble().intValue()");
                    return lastReadout.plusSeconds(channel.getAttribute("Chunk Size(s)").getLatestSample().getValueAsDouble().intValue());
                }
            } else {
                DateTime.now();
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return DateTime.now().withZone(getDateTimeZone());
    }

    public DateTimeZone getDateTimeZone() {
        return timeZone;
    }

    public void setDateTimeZone(DateTimeZone timeZone) {
        logger.debug("TIMEZONE: {}", timeZone);
        this.timeZone = timeZone;
    }

    public enum AUTH_SCHEME {
        BASIC, DIGEST, NONE
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
