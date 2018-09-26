/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JENotifier-EP.
 * <p>
 * JENotifier-EP is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JENotifier-EP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JENotifier-EP. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JENotifier-EP is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jenotifier.notifier.SQL;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.jenotifier.notifier.Notification;
import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jb
 */
public class SQLNotification implements Notification {
    private static final Logger logger = LogManager.getLogger(SQLNotification.class);

    private JEVisObject _jenoti;
    private List<DateTime> _sendTime;
    private boolean _enabled;
    private boolean _sendSuccessful = false;
    private JEVisDataSource _ds;
    private List<SQLQuery> _querys;
    private String _sqltype;
    private String _host;
    private Long _port;
    private String _schema;
    private String _username;
    private String _password;

    private final String _type = "SQL Notification";
    public static final String My_SQL = "MySQL Export";
    public static final String MS_SQL = "MSSQL Export";
    public static final String SQL_HOST = "SQL Host";
    public static final String SQL_PORT = "SQL Port";
    public static final String SQL_USER = "SQL User";
    public static final String SQL_PASSWORD = "SQL Password";
    public static final String SQL_SCHEMA = "SQL Schema";

    /**
     *
     */
    public SQLNotification() {
    }

    /**
     * This constructor is used to creat a new variable of type SQLNotification
     * by copying a existed variable of type SQLNotification.
     *
     * @param sqlnoti
     */
    public SQLNotification(SQLNotification sqlnoti) {
        _jenoti = sqlnoti.getJEVisObjectNoti();
        _enabled = sqlnoti.isSendEnabled();
        _querys = sqlnoti.getQuerys();
        _sqltype = sqlnoti.getSqltype();
        _host = sqlnoti.getHost();
        _port = sqlnoti.getPort();
        _schema = sqlnoti.getSchema();
        _username = sqlnoti.getUsername();
        _password = sqlnoti.getPassword();
    }

    public void setNotificationObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(My_SQL) || notiObj.getJEVisClass().getName().equals(MS_SQL)) {
            _jenoti = notiObj;
            if (notiObj.getJEVisClass().getName().equalsIgnoreCase(My_SQL)) {
                setSqltype(My_SQL);
            } else if (notiObj.getJEVisClass().getName().equalsIgnoreCase(MS_SQL)) {
                setSqltype(MS_SQL);
            }
            try {
                setHost(String.valueOf(getAttribute(notiObj, SQL_HOST)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setPort(Long.valueOf(String.valueOf(getAttribute(notiObj, SQL_PORT))));
            } catch (Exception ex) {
                logger.info(ex);
            }
            try {
                setSchema(String.valueOf(getAttribute(notiObj, SQL_SCHEMA)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setUsername(String.valueOf(getAttribute(notiObj, SQL_USER)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setPassword(String.valueOf(getAttribute(notiObj, SQL_PASSWORD)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                _enabled = Boolean.valueOf(String.valueOf(getAttribute(notiObj, ENABLED)));
            } catch (IllegalArgumentException ex) {
                _enabled = false;
                logger.info(ex);
            }

            List<JEVisObject> querys = notiObj.getChildren();
            if (querys != null && !querys.isEmpty()) {
                for (JEVisObject child : querys) {
                    if (child != null) {
                        String name = child.getJEVisClass().getName();
                        if (name.equalsIgnoreCase("SQL Query")) {
                            SQLQuery sqlquery = new SQLQuery();
                            sqlquery.setSQLQueryObject(child);
                            _querys = new ArrayList<SQLQuery>();
                            _querys.add(sqlquery);
                        } else {
                            logger.info(child + "is illegal.");
                        }
                    } else {
                        logger.info(child + "is null.");
                    }

                }
            } else {
                logger.info(_querys + "is null or empty.");
            }
        }
    }

    @Override
    public void setNotificationObject(JEVisObject notiObj, JEVisFile file) {

    }


    /**
     * Get the url, which depends on the type of the SQL database. The
     * correspond url will be returned.
     *
     * @return url
     */
    public String getURL() {
        String url = null;
        if (_sqltype != null && (_sqltype.equalsIgnoreCase(My_SQL) || _sqltype.equalsIgnoreCase(MS_SQL))) {
            if (_sqltype.equalsIgnoreCase(My_SQL)) {
                if (_port == null) {
                    url = "jdbc:mysql://" + _host + "/" + _schema;
                } else {
                    url = "jdbc:mysql://" + _host + ":" + _port + "/" + _schema;
                }
            } else if (_sqltype.equalsIgnoreCase(MS_SQL)) {
                if (_port == null) {
                    url = "jdbc:sqlserver://" + _host + ";" + "DatabaseName=" + _schema;
                } else {
                    url = "jdbc:sqlserver://" + _host + ":" + _port + ";" + "DatabaseName=" + _schema;
                }
            }
        }
        return url;
    }

    /**
     * Connect to the output database. The connection will be returned.
     *
     * @param sqltype the type of database
     * @return connection
     */
    public Connection getConnectedInputDataBase(String sqltype) {
        String driver = getSQLDriver();
        String url = getURL();
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, _username, _password);
        } catch (ClassNotFoundException ex) {
            logger.error(ex);
            conn = null;
        } catch (SQLException ex) {
            logger.fatal(ex);
            conn = null;
        }
        return conn;
    }

    /**
     * Get the different kinds of server drivers. Depends on the type of SQL
     * database.
     *
     * @return sqldriver
     */
    public String getSQLDriver() {
        String sqldriver = null;
        if (_sqltype != null && (_sqltype.equalsIgnoreCase(My_SQL) || _sqltype.equalsIgnoreCase(MS_SQL))) {
            if (_sqltype.equalsIgnoreCase(My_SQL)) {
                sqldriver = "com.mysql.jdbc.Driver";
            } else if (_sqltype.equalsIgnoreCase(MS_SQL)) {
                sqldriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            }
        }
        return sqldriver;
    }

    /**
     * Check if the database is configured. If all configured, return true. Else
     * return false.
     *
     * @return
     */
    public boolean isDataOutputconfigured() {
        return _sqltype != null && !_sqltype.isEmpty() && _host != null && _schema != null && _username != null && _password != null && !_host.isEmpty() && !_schema.isEmpty() && !_username.isEmpty() && !_password.isEmpty();
    }

    /**
     * To get the value of the attribute of a JEVisObject.
     *
     * @param obj the JEVis Object
     * @param attName the name of the attribute
     * @return the value of the attribute
     * @throws JEVisException
     */
    private Object getAttribute(JEVisObject obj, String attName) throws JEVisException {
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
     * Create the connection with database.
     *
     * @param ds
     */
    public void setNotificationDB(JEVisDataSource ds) {
        try {
            _ds = ds;
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * Return the global variable _type. Once this class is instantiated, _type
     * is fixed as "SQL Notification".
     *
     * @return
     */
    public String getType() {
        return _type;
    }

    /**
     * If the Email is successfully sent, sets _sendSucessful with true and
     * _sendTime with the send time.
     *
     * @param sendSuccessful the send status
     * @param date the time recorder
     */
    public void setSuccessfulSend(boolean sendSuccessful, DateTime date) {
        _sendSuccessful = sendSuccessful;
        if (_sendTime == null) { //If the notification is sent many times, all time will be recored.
            _sendTime = new ArrayList<DateTime>();
        }
        if (sendSuccessful == true && null != date) {
            _sendTime.add(date);
        }
    }

    /**
     * To check, if the SQLNotification has been sent.
     *
     * @return send status
     */
    public boolean isSendSuccessfully() {
        return _sendSuccessful;
    }

    /**
     * If data point exists and data base can be connected, it will be
     * considered as configured.
     *
     * @return
     */
    public boolean isNotiConfigured() {
        boolean isconfigured;
        isconfigured = (isDataOutputconfigured() && isSQLQuerysConfigured());
        return isconfigured;
    }


    /**
     *
     * @return
     */
    public boolean isSQLQuerysConfigured() {
        boolean is = true;
        for (SQLQuery sqq : _querys) {
            is = is && sqq.isSQLQueryConfigured();
        }
        return is;
    }

    /**
     * return the global variable _sendTime. Only the data in SQL is sent and
     * setSussesfulSend(boolean sendSucessful, DateTime date) is called,
     * _sendTime will have the value. Else, it will be null.
     *
     * @return
     */
    public List<DateTime> getSendTime() {
        return _sendTime;
    }

    /**
     * check, whether the jevis object of type "SQL Notification" and can be
     * used to set.
     *
     * @param notiObj
     * @return
     */
    public boolean isConfigurationObject(JEVisObject notiObj) {
        try {
            return notiObj.getJEVisClass().getName().equals(My_SQL) || notiObj.getJEVisClass().getName().equals(MS_SQL);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

    /**
     * return the global variable _jenoti.
     *
     * @return
     */
    public JEVisObject getJEVisObjectNoti() {
        return _jenoti;
    }

    /**
     * return _enabled
     *
     * @return
     */
    public boolean isSendEnabled() {
        return _enabled;
    }

    /**
     * Get the time records of successful sent from JEConfig. A List of time
     * records will be returned.
     *
     * @return
     */
    public List<DateTime> getSendDate() {
        List<DateTime> sendDate = new ArrayList<DateTime>();
        try {
            JEVisAttribute att = this.getJEVisObjectNoti().getAttribute(SQLNotification.SENT_TIME);
            if (att != null) {
                List<JEVisSample> times = att.getAllSamples();
                for (JEVisSample t : times) {
                    sendDate.add(t.getTimestamp());
                }
            } else {
                logger.info("The attribute " + SENT_TIME + " of " + getJEVisObjectNoti().getID() + " does not exist.");
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return sendDate;
    }

    @Override
    public String toString() {
        return "SQLNotification{" + "_sqltype" + _sqltype + "Database[" + "_host" + _host + ", _schema" + _schema + ", _port" + _port + "]"
                + "}";
    }

    /**
     * get database
     *
     * @return the _ds
     */
    public JEVisDataSource getDs() {
        return _ds;
    }

    /**
     * To set the global variable _sqltype. If the param is null or not setted
     * as MY_SQL or MS_SQL, _sqltype remains null.
     *
     * @param sqltype
     */
    public void setSqltype(String sqltype) {
//        logger.error("sqltype" + sqltype);
        if (sqltype != null && (sqltype.equalsIgnoreCase(MS_SQL) || sqltype.equalsIgnoreCase(My_SQL))) {
            _sqltype = sqltype;
        }
    }


    /**
     * @return the _sqltype
     */
    public String getSqltype() {
        return _sqltype;
    }

    /**
     *
     * @return the _host
     */
    public String getHost() {
        return _host;
    }

    /**
     * @return the _port
     */
    public Long getPort() {
        return _port;
    }

    /**
     * @return the _schema
     */
    public String getSchema() {
        return _schema;
    }

    /**
     * @return the _username
     */
    public String getUsername() {
        return _username;
    }

    /**
     * @return the _password
     */
    public String getPassword() {
        return _password;
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
     * To set the global variable _port. If the param is not setted, the _port
     * is 0.
     *
     * @param port
     */
    public void setPort(Long port) {
        _port = port;
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
     * To set the global variable _username. If the param is null or "",
     * _username remains null.
     *
     * @param username
     */
    public void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            _username = username;
        }
    }

    /**
     * To set the global variable _password. If the param is null or "",
     * _password remains null.
     *
     * @param password
     */
    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            _password = password;
        }
    }

    public void setNotification(List<String> str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<DateTime> getSendSchedule() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the _querys
     */
    public List<SQLQuery> getQuerys() {
        return _querys;
    }

    /**
     * @param querys
     */
    public void setQuerys(List<SQLQuery> querys) {
        _querys = querys;
    }
}
