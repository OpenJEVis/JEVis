package org.jevis.httpdatasource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSourceHelper;
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
    public enum AUTH_SCHEME {
        BASIC, DIGEST, NONE
    }
    private AUTH_SCHEME authScheme;
    private Long id;
    private String name;


    /**
     * @param channel
     * @return
     */
    public List<InputStream> sendSampleRequest(Channel channel) throws Exception {
        logger.info("sendSampleRequest to http channel: {}", channel.getChannelObject());
        List<InputStream> answer = new ArrayList<InputStream>();

        String path = channel.getPath();
        DateTime lastReadout = channel.getLastReadout();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        logger.info("Connection Setting: Server: {} User: {} PW: {}", serverURL, userName, password);
        PathFollower pathFollower = new PathFollower(channel.getChannelObject());

        /* Workaround if the protocol is not in the url**/
        if (ssl) {
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

        } else {
            serverURL += "/";
        }

        String contentURL = serverURL + path;

        contentURL = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), contentURL, timeZone);

        contentURL=HTTPDataSource.FixURL(contentURL);
        logger.debug("Channel URL: {}", contentURL);


        URL url = new URL(serverURL);
        if(url.getPort()>-1 && port==null){
            logger.info("Port not set in Attribute, using port from URL: {}",port);
            setPort(url.getPort());
        }
        HttpHost targetHost = new HttpHost(url.getHost(), port, url.getProtocol());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();

        if (getAuthScheme() != null) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

            AuthCache authCache = new BasicAuthCache();
            context.setAuthCache(authCache);

            setAuthScheme(AUTH_SCHEME.DIGEST);
            if (getAuthScheme() == AUTH_SCHEME.BASIC) {
                BasicScheme basicScheme = new BasicScheme();
                authCache.put(targetHost, basicScheme);
            } else if (getAuthScheme() == AUTH_SCHEME.DIGEST) {
                DigestScheme digestScheme = new DigestScheme();
                authCache.put(targetHost, digestScheme);
            }

            context.setCredentialsProvider(credsProvider);
        }


        if (pathFollower.isActive()) {
            logger.info("Using Dynamic Link");
            pathFollower.setConnection(httpClient, context);
            contentURL = pathFollower.startFetching(serverURL, contentURL);
            logger.info("Final target url after following links: {}", contentURL);
        }
        logger.info("Content URL: {}", contentURL);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(readTimeout)
                .build();
        HttpGet get = new HttpGet(contentURL);
        get.setConfig(requestConfig);

        HttpResponse oResponse = httpClient.execute(get, context);

        HttpEntity oEntity = oResponse.getEntity();
        String oXmlString = EntityUtils.toString(oEntity);
        logger.info("Content length to parse: {}",oXmlString.length());
        logger.debug("Content to parse: {}",oXmlString);
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

    public DateTimeZone getDateTimeZone() {
        return timeZone;
    }
}
