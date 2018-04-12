/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.cli;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author bf
 */
public class JEVisServerConnectionCLI {

    private String _db;
    private String _port;
    private String _schema;
    private String _user;
    private String _pw;
    private String _jevisUser;
    private String _jevisPW;

    public JEVisServerConnectionCLI(String path) {
        Properties prop = new Properties();

        try {
            //load a properties file
            prop.load(new FileInputStream(path));

            //get the property value and print it out
            _db = prop.getProperty("Server");
            _port = prop.getProperty("Port");
            _schema = prop.getProperty("Schema");
            _user = prop.getProperty("User");
            _pw = prop.getProperty("PW");
            _jevisUser = prop.getProperty("JEVisUser");
            _jevisPW = prop.getProperty("JEVisPW");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getDb() {
        return _db;
    }

    public String getPort() {
        return _port;
    }

    public String getSchema() {
        return _schema;
    }

    public String getUser() {
        return _user;
    }

    public String getPw() {
        return _pw;
    }

    public String getJevisUser() {
        return _jevisUser;
    }

    public String getJevisPW() {
        return _jevisPW;
    }
}
