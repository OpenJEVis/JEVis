package org.jevis.datacollector.sqldriver;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

public class Parameters {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Parameters.class);

    private int second = 1000;
    private String username;
    private String password;
    private String connectionURI;
    private Integer port;
    private Integer connectionTimeout = 30 * second;
    private Integer readTimeout = 60 * second * 10;
    private Boolean enabled = false;
    private DateTimeZone timezone = DateTimeZone.UTC;
    private boolean sslEnabled;
    private String driver;

    //private String query;

    private JEVisObject sqlServerObj;

    public Parameters(JEVisObject sqlServerObj) throws Exception {

        this.sqlServerObj = sqlServerObj;

        enabled = getAttValue(sqlServerObj, JC.DataSource.DataServer.a_Enabled, false, true);
        /*
        if (enabled) {
            logger.warn("Server: {}:{} is disabled", sqlServerObj.getID(), sqlServerObj.getName());
            return;
        }


         */
        connectionURI = getAttValue(sqlServerObj, "Connection String", "", false);
        driver = getAttValue(sqlServerObj, "Driver Class", "", false);
        // query = getAttValue(sqlServerObj, "Query", "", false);

        if (connectionURI.contains("mysql")) {
            port = getAttValue(sqlServerObj, JC.DataSource.DataServer.a_Port, new Integer(3306), true);
        } else if (connectionURI.contains("sqlserver")) {
            port = getAttValue(sqlServerObj, JC.DataSource.DataServer.a_Port, new Integer(1433), true);
        } else {
            port = getAttValue(sqlServerObj, JC.DataSource.DataServer.a_Port, new Integer(3306), false);
        }

        password = getAttValue(sqlServerObj, JC.DataSource.DataServer.SFTPServer.a_Password, "", true);
        username = getAttValue(sqlServerObj, JC.DataSource.DataServer.SFTPServer.a_User, "", true);
        sslEnabled = getAttValue(sqlServerObj, JC.DataSource.DataServer.EMailServer.a_SSL, false, true);
        timezone = getAttValue(sqlServerObj, JC.DataSource.DataServer.EMailServer.a_Timezone, DateTimeZone.UTC, true);
        connectionTimeout = getAttValue(sqlServerObj, JC.DataSource.DataServer.a_ConnectionTimeout, new Integer(30 * second), true);
        readTimeout = getAttValue(sqlServerObj, JC.DataSource.DataServer.a_ReadTimeout, new Integer(60 * second * 10), true);


    }

    public static <T> T getAttValue(JEVisObject obj, String attType, Object defaultValue, boolean ignoredError) throws Exception {

        try {
            JEVisAttribute attribute = obj.getAttribute(attType);
            if (attribute != null) {
                JEVisSample lSample = attribute.getLatestSample();
                if (lSample != null) {

                    if (defaultValue instanceof DateTimeZone) {
                        return (T) DateTimeZone.forID(lSample.getValueAsString());
                    }

                    if (defaultValue instanceof String) {
                        return (T) lSample.getValueAsString();
                    }

                    if (defaultValue instanceof Long) {
                        return (T) lSample.getValueAsLong();
                    }

                    if (defaultValue instanceof Double) {
                        return (T) lSample.getValueAsDouble();
                    }
                    if (defaultValue instanceof Boolean) {
                        return (T) lSample.getValueAsBoolean();
                    }
                } else {
                    logger.warn("Attribute '{}:{}' not set, using default '{}'", obj, attType, defaultValue);
                }
            } else {
                logger.warn("Attribute '{}:{}' not set, using default '{}'", obj, attType, defaultValue);
            }


        } catch (Exception ex) {
            if (ignoredError == true) {
                return (T) defaultValue;
            } else {
                logger.error("Empty Attribute '{}:{}' can not be ignored cancel", obj, attType, ex);
                throw ex;
            }
        }

        return (T) defaultValue;
    }


    public List<RequestParameters> getRequest() throws JEVisException {
        List<RequestParameters> requestParametersList = new ArrayList<>();
        JEVisClass requestDirClass = sqlServerObj.getDataSource().getJEVisClass("SQL Request Directory");
        JEVisClass requestClass = sqlServerObj.getDataSource().getJEVisClass("SQL Request");

        logger.info("Found {} request Directory's", sqlServerObj.getChildren(requestDirClass, true));
        sqlServerObj.getChildren(requestDirClass, true).forEach(dir -> {
            try {
                logger.info("Enter SQL Dir: {}", dir.getName());
                dir.getChildren(requestClass, true).forEach(jeVisObject -> {
                    try {
                        RequestParameters requestParameters = new RequestParameters(jeVisObject);


                        requestParametersList.add(requestParameters);


                    } catch (Exception ex) {
                        logger.error("Error in SQL Request: {}:{}", jeVisObject.getID(), jeVisObject.getName(), ex);
                    }
                });
            } catch (Exception ex) {
                logger.error("Error in SQL Request Directory: {}:{}", dir.getID(), dir.getName(), ex);
            }

        });
        return requestParametersList;
    }


    public String getFullConnectionURI() {
        return connectionURI;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String connectionURI() {
        return connectionURI;
    }

    public Integer port() {
        return port;
    }

    public Integer connectionTimeout() {
        return connectionTimeout;
    }

    public Integer readTimeout() {
        return readTimeout;
    }

    public Boolean enabled() {
        return enabled;
    }

    public DateTimeZone timezone() {
        return timezone;
    }

    public boolean sslEnabled() {
        return sslEnabled;
    }

    public String driver() {
        return driver;
    }

}
