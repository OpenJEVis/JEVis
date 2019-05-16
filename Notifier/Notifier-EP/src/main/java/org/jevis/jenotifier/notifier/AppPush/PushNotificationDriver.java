/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.AppPush;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author gf
 */
public class PushNotificationDriver implements NotificationDriver {
    private static final Logger logger = LogManager.getLogger(PushNotificationDriver.class);
    private JEVisObject _jeDri;
    private URL _url;
    private int _port;
    private String _host;
    private final String _type = "Push Plugin";
    private String _path;
    private String _apiKey;
    private String _schema;
    private String _pushWay;
    //
    public static final String APPLICATIVE_NOTI_TYPE = "Push Notification";
    //
    public static final String CONTENT_TYPE = "application/json";
    public static final String ACCEPT = "application/json";
    public static final String ACCEPT_ENCODING = "gzip,deflate";
    public static final String REQUEST_METHOD = "POST";
    //
    public static final String API_KEY = "API Key";
    public static final String HOST = "Host";
    public static final String PATH = "Path";
    public static final String PORT = "Port";
    public static final String SCHEMA = "Schema";
    public static final String URL = "URL";
    public static final String PUSH_WAY = "Push Way";

    public PushNotificationDriver() {
    }

    /**
     * This constructor is used to creat a new variable of type
     * PushNotificationDriver by copying a existed variable of type
     * PushNotificationDriver.
     *
     * @param pnotidrv
     */
    public PushNotificationDriver(PushNotificationDriver pnotidrv) {
        if (pnotidrv.isDriverConfigured()) {
            _jeDri = pnotidrv.getJEVisObjectDriver();
            _url = pnotidrv.getURL();
            _port = pnotidrv.getPort();
            _host = pnotidrv.getHost();
//        _ip = pnotidriver.getIP();
            _path = pnotidrv.getPath();
            _apiKey = pnotidrv.getAPIKey();
            _schema = pnotidrv.getSchema();
            _pushWay = pnotidrv.getPushWay();
        }
    }

    /**
     * To set the global variable _url. If the param is null or "", _url remains
     * null.
     *
     * @param url
     * @throws MalformedURLException
     */
    public void setURL(String url) throws MalformedURLException {
        if (url != null && !url.isEmpty()) {
            _url = new URL(url);
        }
    }

    /**
     * To set the global variable _url with schema, host, port, path.
     *
     * @param schema
     * @param host
     * @param port
     * @param path
     * @throws MalformedURLException
     */
    public void setURL(String schema, String host, int port, String path) throws MalformedURLException {
        if (schema != null && host != null && path != null && !schema.isEmpty() && !host.isEmpty() && !path.isEmpty()) {
            _url = new URL(schema, host, port, path);
        }
    }

    /**
     * To set the global variable _port. If it is not set, _port will be 0
     *
     * @param port
     */
    public void setPort(int port) {
        _port = port;
    }

    /**
     * To set the global variable _apiKey. If the param is null or "", _apiKey
     * remains null.
     *
     * @param apiKey
     */
    public void setAPIKey(String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            _apiKey = apiKey;
        }
    }

    /**
     * To set the global variable _host. If the param is null or "", _host
     * remains null.
     *
     * @param host
     */
    public void setHost(String host) {
        if (host != null && !host.isEmpty()) {
            _host = host;
        }
    }

    /**
     * To set the global variable _schema. If the param is null or "", _schema
     * remains null.
     *
     * @param schema
     */
    public void setSchema(String schema) {
        if (schema != null && !schema.isEmpty()) {
            _schema = schema;
        }
    }

    /**
     * To set the global variable _path. If the param is null or "", _path
     * remains null.
     *
     * @param path
     */
    public void setPath(String path) {
        if (path != null && !path.isEmpty()) {
            if (path.startsWith("/")) {
                _path = path;
            } else {
                _path = "/" + path;
            }
        }
    }

    /**
     * To set the global variable _pushWay. If the param is null or "", _ip
     * remains
     *
     * @param pushWay
     */
    public void setPushWay(String pushWay) {
        if (pushWay != null && !pushWay.isEmpty()) {
            _pushWay = pushWay;
        }
    }

//    /**
//     * To set the global variable _ip. If the param is null or "", _ip remains
//     * null.
//     *
//     * @param ip
//     */
//    public void setIP(String ip) {
//        if (ip != null && !ip.equals("")) {
//            _ip = ip;
//        }
//    }

    /**
     * return the global variable _jeDri.
     *
     * @return
     */
    public JEVisObject getJEVisObjectDriver() {
        return _jeDri;
    }

    /**
     * return the global variable _url.
     *
     * @return
     */
    public URL getURL() {
        return _url;
    }

    /**
     * return the global variable _port.
     *
     * @return
     */
    public int getPort() {
        return _port;
    }

    /**
     * return the global variable _apiKey.
     *
     * @return
     */
    public String getAPIKey() {
        return _apiKey;
    }

    /**
     * return the global variable _host.
     *
     * @return
     */
    public String getHost() {
        return _host;
    }

    /**
     * return the global variable _schema.
     *
     * @return
     */
    public String getSchema() {
        return _schema;
    }

    /**
     * return the global variable _path.
     *
     * @return
     */
    public String getPath() {
        return _path;
    }

    /**
     * return the global variable _pushWay.
     *
     * @return
     */
    public String getPushWay() {
        return _pushWay;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "Push Plugin".
     *
     * @return
     */
    public String getDriverType() {
        return _type;
    }

//    /**
//     * return the global variable _ip.
//     *
//     * @return
//     */
//    public String getIP() {
//        return _ip;
//    }

    /**
     * To send the Notification, the Notification must have the type: Push
     * Notification. If the notification is sucessfully sent, returns true.
     * Else, returns false.
     *
     * @param jenoti
     * @return
     */
    public boolean sendNotification(Notification jenoti) {
        boolean successful = false;
        if (jenoti.getType().equals(APPLICATIVE_NOTI_TYPE)) {
            PushNotification pnoti = (PushNotification) jenoti;
            try {
                successful = sendPushNotification(pnoti);
            } catch (IOException ex) {
                logger.error(ex);
            } catch (JSONException ex) {
                logger.error(ex);
            } catch (Exception ex) {
                logger.error(ex);
            }
            return successful;
        } else {
            logger.info("This Notification is not the PushNotification.");
            logger.info("This Notification is" + jenoti.getType() + ".");
//            logger.info("This Notification is not the PushNotification.");
//            logger.info("This Notification is" + jenoti.getType() + ".");
            return successful;
        }
    }

    /**
     * All necessary parameters will be configured to send the PushNotification.
     * If the PushNotification is sucessfully sent, returns true. Else, returns
     * false.
     *
     * @param pnoti
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws Exception
     */
    private boolean sendPushNotification(PushNotification pnoti) throws IOException, JSONException, Exception {
        boolean success = false;
        HttpURLConnection connection;
        synchronized (PushNotificationDriver.class) {
            connection = setHttpHeader(); //set the propertier of the HTTP Header
        }
        JSONObject httpBody = pnoti.toJSON(); //set the request
        OutputStream out = connection.getOutputStream();
        out.write(httpBody.toString().getBytes()); // write http-body
        out.flush();
        out.close();
// read the responce: status code
        int statuscode = connection.getResponseCode();
        if (statuscode == 200) { //only means:the request is commit. But whether the push is really sent and Whether the user receives the push, can not be known.
            pnoti.setSuccessfulSend(true, new DateTime(new Date()));
            success = true;
//            logger.info("Ok - Broadcast was sent");
        }
//        else if (statuscode == 400) {
//            logger.info("Bad Request - Check JSON syntax");
//        } else if (statuscode == 401) {
//            logger.info("Unauthorized - No 'Api-Key' supplied");
//        } else if (statuscode == 500) {
//            logger.info("Internal server error - Please report the problem");
//        }
// read the returned message
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String lines;
        StringBuffer sb = new StringBuffer();
        while ((lines = reader.readLine()) != null) {
            lines = new String(lines.getBytes(), StandardCharsets.UTF_8);
            sb.append(lines);
        }
        logger.info(sb);
//        logger.info(sb);
        reader.close();
// disconnect
        connection.disconnect();
        return success;
    }

    /**
     * Set the necessary properties to connect to URL. And returns the variable
     * of type HttpURLConnection.
     *
     * @return
     * @throws Exception
     */
    private HttpURLConnection setHttpHeader() throws Exception {
        Certificate.doTrustToCertificates(); // get the Certificates
        HttpURLConnection connection = null;

//            URL url = new URL(getURL());
        synchronized (PushNotificationDriver.class) {
            connection = (HttpURLConnection) getURL().openConnection();
//set the http connection properties
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
//set Http-Header
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);//set request form: json. XML can theoretically also be sett, but cap3 uses only JSON.
//connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("Accept", ACCEPT);//set response form: json. XML can theoretically also be sett, but cap3 uses only JSON.
//connection.setRequestProperty("X-Auth-Token","xx");
            connection.setRequestProperty("Api-Key", getAPIKey()); //set API-Key. API-Key form cap3 has deadline (now 3 months).
            connection.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
            connection.connect(); //connect

            return connection;
        }
    }

    /**
     * If the notification has the type: Push Notification and the PUSH API of
     * Driver equals the PUSH API of notification, then the driver can support
     * the notification. If supported, it only means, this driver can send the
     * push. But if the driver is not configured or rightly configured, the
     * driver can not send the push, even if it is supported.
     *
     * @param jenoti
     * @return
     */
    public boolean isSupported(Notification jenoti) {
        boolean support;
        if (jenoti.getType().equals(APPLICATIVE_NOTI_TYPE)) {
            PushNotification pnoti = (PushNotification) jenoti;
            support = pnoti.getPushWay().equals(getPushWay().toLowerCase());
        } else {
            support = false;
        }
        return support;
    }

    /**
     * Until now, if _url and _apiKey are not null and empty, the driver can be
     * considered as configured. If configured, returns true. Else, returns
     * false. But the effectiveness of this two variables is not taken into
     * account.
     *
     * @return
     */
    public boolean isDriverConfigured() {
        boolean configured = false;
        if (_url != null && _apiKey != null) {
            if (!_apiKey.isEmpty()) {
                configured = true;
            }
        }
        return configured;
    }

    @Override
    public String toString() {
        return "PushNotificationDriver{" + "_jeDri=" + _jeDri + ", _url=" + _url + ", _host=" + _host + ", _pushWay=" + _pushWay + ", _path=" + _path + ", _port=" + _port + ", _schema=" + _schema + ", _apiKey=" + _apiKey + ", _type=" + _type + '}';
    }

    /**
     * To get the value of the attribute of a JevisObject
     *
     * @param obj     the JEVis Object
     * @param attName the name of the attribute
     * @return the value of the attribute
     * @throws JEVisException
     */
    public Object getAttribute(JEVisObject obj, String attName) throws JEVisException {
        JEVisAttribute att = obj.getAttribute(attName);
        if (att != null) { //check, if the attribute exists.
            if (att.hasSample()) { //check, if this attribute has values.
                JEVisSample sample = att.getLatestSample();
                if (sample.getValue() != null) { //check, if the value of this attribute is null.
                    return sample.getValue();
                } else {
                    throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute'value is not filled: " + attName);
                }
            } else {
                throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute'value is not filled: " + attName);
            }
        } else {
            throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute is missing: " + attName);
        }
    }

    /**
     * Call the function getAttribute(,) to get parameters of the notification
     * in Database and use the setter to assign the global variables. If there
     * is an IllegalArgumentException, the complex variable will be assigned
     * with null and the simple variables will not be dealed. The information of
     * the exception will also be printed.
     *
     * @param notiObj
     * @throws JEVisException
     */
    public void setNotificationDriverObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jeDri = notiObj;
            try {
                setURL(String.valueOf(getAttribute(notiObj, URL)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setAPIKey(String.valueOf(getAttribute(notiObj, API_KEY)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setPort(Integer.valueOf(String.valueOf(getAttribute(notiObj, PORT))));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setHost(String.valueOf(getAttribute(notiObj, HOST)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setPath(String.valueOf(getAttribute(notiObj, PATH)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setSchema(String.valueOf(getAttribute(notiObj, SCHEMA)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            if (_url == null) {
                try {
                    setURL(getSchema(), getHost(), getPort(), getPath());
                } catch (MalformedURLException ex) {
                    logger.error(ex);
                }
            }
            try {
                setPushWay(String.valueOf(getAttribute(notiObj, PUSH_WAY)));
            } catch (Exception ex) {
                logger.error(ex);
            }
        } else {
            logger.info(notiObj + " is not suitable for Push Notification Driver");
        }
    }

    public void setNotificationDriver(List<String> str) {
        try {
            setURL(str.get(0));
        } catch (MalformedURLException ex) {
            logger.error(ex);
        }
        setAPIKey(str.get(1));
        setPort(Integer.valueOf(str.get(2)));
        setHost(str.get(3));
//        setIP(str.get(4));
        setPath(str.get(4));
        setSchema(str.get(5));
        setPushWay(str.get(6));
    }

    /**
     * store the send time into JEConfig
     *
     * @param noti
     * @return
     */
    public boolean sendTimeRecorder(Notification noti) {
        boolean re = false;
        if (noti.isSendSuccessfully()) {
            try {
                List<JEVisSample> ts = new ArrayList<JEVisSample>();
                JEVisAttribute recorder = noti.getJEVisObjectNoti().getAttribute(Notification.SENT_TIME);
                if (recorder != null) {
                    for (DateTime time : noti.getSendTime()) {
                        JEVisSample t = recorder.buildSample(time, noti.getJEVisObjectNoti().getID(), "Sent by Driver " + getJEVisObjectDriver().getID());
                        ts.add(t);
                    }
                    recorder.addSamples(ts);
                    re = true;
                } else {
                    logger.info("The attribute of the Notification " + noti.getJEVisObjectNoti().getID() + " does not exist.");
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        } else {
            logger.debug("The Notification " + noti.getJEVisObjectNoti().getID() + " has not been sent successfully.");
        }
        return re;
    }

    /**
     * check, whether the jevis object of type "Push Plugin" and can be used to
     * set
     *
     * @param driverObj
     * @return
     */
    public boolean isConfigurationObject(JEVisObject driverObj) {
        try {
            return driverObj.getJEVisClass().getName().equals(_type);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }
}
