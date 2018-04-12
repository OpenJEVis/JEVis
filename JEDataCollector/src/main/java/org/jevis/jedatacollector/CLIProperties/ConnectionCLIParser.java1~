/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector.CLIProperties;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author broder
 */
public class ConnectionCLIParser {

    private String _connectionType;
    private String _ip;
    private String _dateFormat;
    private String _path;
    private String _file;
    private String _user;
    private String _password;
    private Integer _port;
    private Integer _connectionTimeout;
    private Integer _readTimeout;
    private Boolean _ssl;

    public ConnectionCLIParser(String path) {
        Properties prop = new Properties();

        try {
            //load a properties file
            prop.load(new FileInputStream(path));

            //get the property value and print it out
            _connectionType = prop.getProperty("type");
            _dateFormat = prop.getProperty("dateformat");
            _ip = prop.getProperty("ip");

            _port = getPropAsInteger(prop, "port");
            _path = prop.getProperty("path");
            _user = prop.getProperty("user");
            _password = prop.getProperty("password");
            _connectionTimeout = getPropAsInteger(prop, "connection-timeout");
            _readTimeout = getPropAsInteger(prop, "read-timeout");
            _file = prop.getProperty("file");
            _ssl = getPropAsBoolean(prop, "ssl");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private Integer getPropAsInteger(Properties prop, String propertyName) {
        if (prop.getProperty(propertyName) != null) {
            return Integer.parseInt(prop.getProperty(propertyName));
        } else {
            return null;
        }
    }

    public String getConnectionType() {
        return _connectionType;
    }

    public Integer getPort() {
        return _port;
    }

    public String getIP() {
        return _ip;
    }

    public String getPath() {
        return _path;
    }

    public Integer getConnectionTimeout() {
        return _connectionTimeout;
    }

    public Integer getReadTimeout() {
        return _readTimeout;
    }
    
    public Boolean getSecureConnection(){
        return _ssl;
    }

    public String getDateFormat() {
        return _dateFormat;
    }

    public String getFileName() {
        return _file;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _password;
    }

    private Boolean getPropAsBoolean(Properties prop, String propertyName) {
        if (prop.getProperty(propertyName) != null) {
            return Boolean.parseBoolean(prop.getProperty(propertyName));
        } else {
            return false;
        }
    }
}
