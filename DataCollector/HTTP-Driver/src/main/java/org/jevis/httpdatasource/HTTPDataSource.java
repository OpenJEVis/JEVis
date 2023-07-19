package org.jevis.httpdatasource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.driver.*;
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

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }

    public enum AUTH_SCHEME {
        BASIC, DIGEST, NONE
    }

    private AUTH_SCHEME authScheme;
    private Long id;
    private String name;
    private boolean needUrlConfig = true;


    /**
     * @param channel
     * @return
     */
    public List<InputStream> sendSampleRequest(Channel channel) throws Exception {
        logger.info("sendSampleRequest to http channel: {}", channel);

        String channelID = channel.getChannelObject().getID().toString();
        List<InputStream> answer = new ArrayList<InputStream>();

        String path = channel.getPath();
        lastReadout = channel.getLastReadout();

        endDateTime = getCurrentTime(channel.getChannelObject(), lastReadout);

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        ParameterHelper parameterHelper = new ParameterHelper(lastReadout, endDateTime);
        path = parameterHelper.getNewPath(path, channel.getChannelObject().getAttribute(JC.Channel.HTTPChannel.a_ParameterConfig).getLatestSample());

        logger.debug("[{}] Connection Setting: Server: {} User: {} PW: {}", channelID, serverURL, userName, password);
        PathFollower pathFollower = new PathFollower(channel.getChannelObject());

        if (needUrlConfig) {/*only the first channel needs to configure the server url*/
            if (ssl) {/* Workaround if the protocol is not in the url**/
                if (!serverURL.startsWith("https")) {
                    serverURL = "https://" + serverURL;
                }
                /* We trust self signed certificates for now, this way is not save **/
                DataSourceHelper.doTrustToCertificates();
            } else {
                if (!serverURL.startsWith("http")) {
                    serverURL = "http://" + serverURL;
                }
            }

            if (serverURL.endsWith("/")) {
                serverURL = serverURL.substring(0, serverURL.length() - 1);
            }

            if (port != null) {
                serverURL += ":" + port;
            }

            serverURL += "/";


            /** Fallback if the URL does contain the port and the Port attribute has non **/
            URL url = new URL(serverURL);
            if (port == null && url.getPort() > -1) {
                logger.info("[{}] Port not set in Attribute, using port from URL: {}", channelID, port);
                setPort(url.getPort());
            }
            needUrlConfig = false;
        }


        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout * 1000)
                .setSocketTimeout(readTimeout * 1000)
                .build();

        //HttpHost targetHost = new HttpHost(url.getHost(), port, url.getProtocol());

        CloseableHttpClient httpClient;
        if (userName != null && !userName.isEmpty()) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(userName, password)
            );
            httpClient = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(provider)
                    .build();
        } else {
            httpClient = HttpClientBuilder.create()
                    .build();
        }

        String contentURL = path;
        contentURL = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), contentURL, timeZone);
        contentURL = HTTPDataSource.FixURL(contentURL);
        logger.debug("[{}] Channel URL: {}", channelID, contentURL);

        String getRequest = "";
        if (pathFollower.isActive()) {
            logger.debug("[{}] Using Dynamic Link", channelID, channelID);
            pathFollower.setConnection(httpClient, requestConfig);
            getRequest = pathFollower.startFetching(serverURL, contentURL);
            logger.debug("[{}] Final target url after following links: {}", channelID, getRequest);
        } else {
            getRequest = serverURL + contentURL;
        }
        logger.info("[{}] send HTTP.get: {}", channelID, getRequest);

        HttpGet get = new HttpGet(getRequest);
        get.setConfig(requestConfig);

        HttpResponse oResponse = httpClient.execute(get);

        statusLine = oResponse.getStatusLine();

        logger.info("[{}] HTTP response status code: {}", channelID, oResponse.getStatusLine());
        HttpEntity oEntity = oResponse.getEntity();
        String oXmlString = EntityUtils.toString(oEntity);
        logger.info("[{}] Content length to parse: {}", channelID, oXmlString.length());
        logger.debug("[{}] Content to parse: {}", channelID, oXmlString);
        EntityUtils.consume(oEntity);
        InputStream stream = new ByteArrayInputStream(oXmlString.getBytes(StandardCharsets.UTF_8));
        answer.add(stream);


        return answer;
    }

    public static String FixURL(String url) {
        url = url.replaceAll("(?<!(http:|https:))/+", "/");
        url = url.replaceAll(" ", "%20");
        return url;
    }


    public void setDateTimeZone(DateTimeZone timeZone) {
        logger.info("TIMEZONE: {}", timeZone);
        this.timeZone = timeZone;
    }

    public AUTH_SCHEME getAuthScheme() {
        logger.debug("getAuthScheme()");

        /* Fallback for older Configuration were we only had BASIC auth*/
        if (authScheme == null) {
            if (userName != null || userName.isEmpty()) {
                return AUTH_SCHEME.BASIC;
            }
        }


        return authScheme;
    }

    public void setAuthScheme(AUTH_SCHEME authScheme) {
        this.authScheme = authScheme;
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

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    interface HTTPChannel extends DataCollectorTypes.Channel {

        String NAME = "HTTP Channel";
        String PATH = "Path";
    }

    private DateTime getCurrentTime(JEVisObject channel, DateTime lastReadout) {
        try {

            if (channel.getAttribute("Chunk Size(s)").hasSample()) {
               return lastReadout.plusSeconds(channel.getAttribute("Chunk Size(s)").getLatestSample().getValueAsDouble().intValue());
            }else {
                DateTime.now();
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return DateTime.now();
    }





    public DateTimeZone getDateTimeZone() {
        return timeZone;
    }
}
