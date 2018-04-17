/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author broder
 */
public class ConfigProperties {

    private String _folder;
    private String _logfile;
    private String _server;
    private String _user;
    private String _password;
    private Long _period;
    private String _logLevel;

    public ConfigProperties() {
        Properties prop = new Properties();

        try {
            //load a properties file
            prop.load(new FileInputStream("/opt/jevis/bin/jereport/config.properties"));

            //get the property value and print it out
            _folder = prop.getProperty("folder");
            _logfile = prop.getProperty("logfile");
            _server = prop.getProperty("server");
            _user = prop.getProperty("user");
            _password = prop.getProperty("pass");
            _period = Long.parseLong(prop.getProperty("period"));
            _logLevel = prop.getProperty("loglevel");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public ConfigProperties(String folder, String logfile, String server, String user, String password, long period, String logLevel) {
        _folder = folder;
        _logfile = logfile;
        _server = server;
        _user = user;
        _password = password;
        _period = period;
        _logLevel = logLevel;
    }

    public String getFolder() {
        return _folder;
    }

    public String getLogfile() {
        return _logfile;
    }

    public String getPassword() {
        return _password;
    }

    public Long getPeriod() {
        return _period;
    }

    public String getServer() {
        return _server;
    }

    public String getUser() {
        return _user;
    }

    String getLogLevel() {
        return _logLevel;
    }
}
