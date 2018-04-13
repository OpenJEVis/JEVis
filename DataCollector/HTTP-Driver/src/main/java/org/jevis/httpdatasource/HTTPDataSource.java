package org.jevis.httpdatasource;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.driver.DataSourceHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author bf
 */
public class HTTPDataSource {

    // interfaces
    interface HTTP extends DataCollectorTypes.DataSource.DataServer {

        public final static String NAME = "HTTP Server";
        public final static String PASSWORD = "Password";
        public final static String SSL = "SSL";
        public final static String USER = "User";
    }

    interface HTTPChannelDirectory extends DataCollectorTypes.ChannelDirectory {

        public final static String NAME = "HTTP Channel Directory";
    }

    interface HTTPChannel extends DataCollectorTypes.Channel {

        public final static String NAME = "HTTP Channel";
        public final static String PATH = "Path";
    }

    // member variables
    private Long _id;
    private String _name;
    private String _serverURL;
    private Integer _port;
    private Integer _connectionTimeout;
    private Integer _readTimeout;
    private String _userName;
    private String _password;
    private DateTimeZone _timeZone;
    private Boolean _ssl = false;

    /**
     * komplett überarbeiten!!!!!
     *
     * @param channel
     * @return
     */
    public List<InputStream> sendSampleRequest(Channel channel) {
        List<InputStream> answer = new ArrayList<InputStream>();
        try {

            String path = channel.getPath();
            DateTime lastReadout = channel.getLastReadout();

            if (path.startsWith("/")) {
                path = path.substring(1, path.length());
            }

            URL requestUrl;
            if (_userName == null || _password == null || _userName.equals("") || _password.equals("")) {

                path = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), path);
                HttpURLConnection request = null;
                if (!_serverURL.contains("://")) {
                    _serverURL = "http://" + _serverURL;
                }

                if (_ssl) {
                    _serverURL = _serverURL.replace("http", "https");
                }

                if (_port != null) {
                    requestUrl = new URL(_serverURL + ":" + _port + "/" + path);
                } else {
                    requestUrl = new URL(_serverURL + "/" + path);
                }
                if (_ssl) {
                    DataSourceHelper.doTrustToCertificates();
                }
                Logger.getLogger(HTTPDataSource.class.getName()).log(Level.INFO, "Connection URL: " + requestUrl);
                request = (HttpURLConnection) requestUrl.openConnection();

//                    if (_connectionTimeout == null) {
                int connTimeoutInMSec = _connectionTimeout * 1000;
//                    }
                //  System.out.println("Connect timeout: " + _connectionTimeout+ " s");
                request.setConnectTimeout(connTimeoutInMSec);

//                    if (_readTimeout == null) {
                int readTimeoutInMSec = _readTimeout * 1000;
//                    }
                //   System.out.println("read timeout: " + _readTimeout + " s");
                request.setReadTimeout(readTimeoutInMSec);
                answer.add(request.getInputStream());
            } else {
                DefaultHttpClient _httpClient;
                HttpHost _targetHost;
                HttpGet _httpGet;
                BasicHttpContext _localContext = new BasicHttpContext();
                _httpClient = new DefaultHttpClient();

                path = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), path);
                if (_ssl) {
                    DataSourceHelper.doTrustToCertificates();
                    _targetHost = new HttpHost(_serverURL, ((int) (long) _port), "https");
                } else {
                    _targetHost = new HttpHost(_serverURL, ((int) (long) _port), "http");
                }
                /*
                 * set the sope for the authentification
                 */
                _httpClient.getCredentialsProvider().setCredentials(
                        new AuthScope(_targetHost.getHostName(), _targetHost.getPort()),
                        new UsernamePasswordCredentials(_userName, _password));

                // Create AuthCache instance
                AuthCache authCache = new BasicAuthCache();

                //set Authenticication scheme
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(_targetHost, basicAuth);

                path = DataSourceHelper.replaceDateFromUntil(lastReadout, new DateTime(), path);

                _httpGet = new HttpGet(path);
                //TODO: Connection timeouts and error handling

                HttpResponse oResponse = _httpClient.execute(_targetHost, _httpGet, _localContext);

                HttpEntity oEntity = oResponse.getEntity();
                String oXmlString = EntityUtils.toString(oEntity);
                EntityUtils.consume(oEntity);
                InputStream stream = new ByteArrayInputStream(oXmlString.getBytes("UTF-8"));
                answer.add(stream);
            }
//        List<InputHandler> answerList = new ArrayList<InputHandler>();
//        answerList.add(InputHandlerFactory.getInputConverter(answer));
        } catch (JEVisException ex) {
            Logger.getLogger(HTTPDataSource.class.getName()).log(Level.ERROR, ex.getMessage());
            java.util.logging.Logger.getLogger(HTTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HTTPDataSource.class.getName()).log(Level.ERROR, ex.getMessage());
            java.util.logging.Logger.getLogger(HTTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(HTTPDataSource.class.getName()).log(Level.ERROR, ex.getMessage());
            java.util.logging.Logger.getLogger(HTTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return answer;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public void setServerURL(String _serverURL) {
        this._serverURL = _serverURL;
    }

    public void setPort(Integer _port) {
        this._port = _port;
    }

    public void setConnectionTimeout(Integer _connectionTimeout) {
        this._connectionTimeout = _connectionTimeout;
    }

    public void setReadTimeout(Integer _readTimeout) {
        this._readTimeout = _readTimeout;
    }

    public void setUserName(String _userName) {
        this._userName = _userName;
    }

    public void setPassword(String _password) {
        this._password = _password;
    }

    public void setSsl(Boolean _ssl) {
        this._ssl = _ssl;
    }
    
    public void setDateTimeZone(String timeZone){
        System.out.println("TIMEZONE: "+timeZone);
        this._timeZone = DateTimeZone.forID(timeZone);
    }
    
    public DateTimeZone getDateTimeZone() {
        return _timeZone;
    }
}
