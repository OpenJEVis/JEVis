/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI.
 * <p>
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.emaildatasource;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;

/**
 * The EMailServerParameters class represents the settings required to establish
 * a connection to the email server.
 *
 * @author Artur Iablokov
 */
public class EMailServerParameters {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(EMailServerParameters.class);

    private static final Map<String, Integer> portDefMap;

    static {
        portDefMap = new HashMap<String, Integer>();
        portDefMap.put("imap", 143);
        portDefMap.put("imaps", 993);
        portDefMap.put("pop3", 110);
        portDefMap.put("pop3s", 995);
    }

    private final int SECOND = 1000;
    private final JEVisObject _mailObj;
    private String _protocol;
    private String _userEMail; //email adress
    private String _password;
    private String _host;
    private Integer _port;
    private String _folderName;
    private Integer _connectionTimeout;
    private Integer _readTimeout;
    private Boolean _enabled;
    private DateTimeZone _timezone;
    private String _ssl;
    private String _authentication;
    private boolean _sslEnabled;

    public EMailServerParameters(JEVisObject mailObj) throws Exception {
        _mailObj = mailObj;
        try {
            setAllEMailParameteres();
        } catch (JEVisException ex) {
            logger.error("Failed to get EMail Server settings", ex);
        }
    }

    /**
     * Sets all email parameters.
     */
    private void setAllEMailParameteres() throws JEVisException {
        _protocol = setProtocol();
        _host = DBHelper.getAttValue(DBHelper.RetType.STRING, _mailObj, EMailConstants.EMail.HOST, EMailConstants.Errors.HOST_ERR, null);
        _userEMail = DBHelper.getAttValue(DBHelper.RetType.STRING, _mailObj, EMailConstants.EMail.USER, EMailConstants.Errors.USER_ERR, null);
        _password = DBHelper.getAttValue(DBHelper.RetType.STRING, _mailObj, EMailConstants.EMail.PASSWORD, EMailConstants.Errors.PASS_ERR, null);
        //POP3 always default folder name - has no Folder parameter in JEConfig
        if (_protocol.equalsIgnoreCase(EMailConstants.Protocol.IMAP)) {
            _folderName = DBHelper.getAttValue(DBHelper.RetType.STRING, _mailObj, EMailConstants.EMail.FOLDER, EMailConstants.Errors.FOLD_ERR, EMailConstants.DefParameters.FOLDER_NAME);
        }
        _ssl = DBHelper.getAttValue(DBHelper.RetType.STRING, _mailObj, EMailConstants.EMail.SSL, EMailConstants.Errors.SSL_ERR, EMailConstants.DefParameters.SSL);
        _sslEnabled = setIsSsl();
        _port = setPort();
        _timezone = DBHelper.getAttValue(DBHelper.RetType.TIMEZONE, _mailObj, EMailConstants.EMail.TIMEZONE, EMailConstants.Errors.TIMEZ_ERR, EMailConstants.DefParameters.TIMEZONE);

        _readTimeout = DBHelper.getAttValue(DBHelper.RetType.INTEGER, _mailObj, EMailConstants.EMail.READ_TIMEOUT, EMailConstants.Errors.READ_ERR, EMailConstants.DefParameters.READ_TIMEOUT);
        _connectionTimeout = DBHelper.getAttValue(DBHelper.RetType.INTEGER, _mailObj, EMailConstants.EMail.CONNECTION_TIMEOUT, EMailConstants.Errors.CONN_ERR, EMailConstants.DefParameters.CONNECTION_TIMEOUT);
        _enabled = DBHelper.getAttValue(DBHelper.RetType.BOOLEAN, _mailObj, EMailConstants.EMail.ENABLE, EMailConstants.Errors.ENAB_ERR, EMailConstants.DefParameters.ENABLE);
        //_authentication = DBHelper.getAttValue(DBHelper.RetType.STRING, _mailObj, EMailConstants.EMail.AUTHENTICATION, EMailConstants.Errors.AUTH_ERR, EMailConstants.DefParameters.AUTHENTICATION);
        _authentication = "oauth2";
    }

    /**
     * Sets the EMail clients protocol.
     */
    private String setProtocol() throws JEVisException {
        if (_mailObj.getJEVisClass().getName().equalsIgnoreCase(EMailConstants.EMail.IMAPEMail.NAME)) {
            return EMailConstants.Protocol.IMAP;
        } else if (_mailObj.getJEVisClass().getName().equalsIgnoreCase(EMailConstants.EMail.POP3EMail.NAME)) {
            return EMailConstants.Protocol.POP3;
        } else {
            logger.error("EMail protocol is not received");
            throw new NullPointerException();
        }
    }

    /**
     * Sets SSL status.
     */
    private boolean setIsSsl() {
        return _ssl.equals(EMailConstants.ValidValues.CryptProtocols.SSL_TLS);
    }

    /**
     * Sets port.
     */
    private int setPort() {
        String key = "";
        int port = DBHelper.getAttValue(DBHelper.RetType.INTEGER, _mailObj, EMailConstants.EMail.PORT, EMailConstants.Errors.PORT_ERR, EMailConstants.DefParameters.PORT);
        if (port == EMailConstants.DefParameters.PORT) {
            if (_sslEnabled) {
                key = "s";
            }
            port = portDefMap.get(_protocol + key);
        }
        logger.info("set default value for port: {}", port);
        return port;
    }

    /**
     * @return email protocol
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * @return user email address
     */
    public String getUserEMail() {
        return _userEMail;
    }

    /**
     * @return email password
     */
    public String getPassword() {
        return _password;
    }

    /**
     * @return host
     */
    public String getHost() {
        return _host;
    }

    /**
     * @return port
     */
    public Integer getPort() {
        return _port;
    }

    /**
     * @return folder name
     */
    public String getFolderName() {
        return _folderName;
    }

    /**
     * @return connection timeout (in seconds)
     */
    public Integer getConnectionTimeout() {
        return (_connectionTimeout * SECOND);
    }

    /**
     * @return read timeout (in seconds)
     */
    public Integer getReadTimeout() {
        return (_readTimeout * SECOND);
    }

    /**
     * @return enabled status
     */
    public Boolean getEnabled() {
        return _enabled;
    }

    /**
     * @return time zone
     */
    public DateTimeZone getTimezone() {
        return _timezone;
    }

    /**
     * @return SSL type
     */
    public String getSsl() {
        return _ssl;
    }

    /**
     * @return SSL status
     */
    public boolean isSsl() {
        return _sslEnabled;
    }

    /**
     * @return authentication type
     */
    public String getAuthentication() {
        return _authentication;
    }
}
