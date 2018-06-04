/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEWebService.
 * <p>
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.ws.sql.SQLDataSource;

import java.io.File;

/**
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);

    //@Singleton
    public static String _dbport = "3306";
    public static String _dbip = "192.168.2.55";
    public static String _ip = "localhost";
    public static String _port = "5007";
    public static String _dbuser = "jevis";
    public static String _dbpw = "jevistest";
    public static String _schema = "jevis";
    public static String _rootUser = "jevis";
    public static String _rootPW = "jevis";
    public static String _uri = "http://localhost:8080/JEWebservice/";
    public static String _keyFile = "";
    public static String _keyFilePW = "";


    public static long _demoRoot = -1;
    public static long _demoGroup = -1;
    public static String _registratioKey = "";

    private static boolean _loadFromFile = true;
    private static boolean _fileIsLoaded = false;

    private static File _i18nDir;
    private static File _fileDir;


    public static String getDBHost() {
        return _dbip;
    }

    public static String getDBPort() {
        return _dbport;
    }

    public static String getDBUser() {
        return _dbuser;
    }

    public static String getDBPW() {
        return _dbpw;
    }

    public static String getSchema() {
        return _schema;
    }

    public static File getFileDir() {
        return _fileDir;
    }

    public static File getI18nDir() {
        return _i18nDir;
    }

    public static String getKeyStoreFile() {
        return _keyFile;
    }

    public static String getKeyStorePW() {
        return _keyFilePW;
    }

    public static String getURI() {
        return _uri;
    }

    public static void readConfigurationFile(File cfile) {
        try {
            if (!_fileIsLoaded) {
//                File cfile = new File("config.xml");
                if (cfile.exists()) {
//                    Logger.getLogger(Config.class.getName()).log(Level.INFO, "using Configfile: " + cfile.getAbsolutePath());
                    logger.info("using Configfile: {}", cfile.getAbsolutePath());
                    XMLConfiguration config = new XMLConfiguration(cfile);

                    _dbport = config.getString("datasource.port");
                    _dbip = config.getString("datasource.url");
                    _dbuser = config.getString("datasource.login");
                    _dbpw = config.getString("datasource.password");
                    _schema = config.getString("datasource.schema");

                    //Woraround solution for the registration service
                    _rootUser = config.getString("sysadmin.username");
                    _rootPW = config.getString("sysadmin.password");
                    _port = config.getString("webservice.port");
                    _uri = config.getString("webservice.uri");
                    _keyFile = config.getString("webservice.keystore");
                    _keyFilePW = config.getString("webservice.keystorepw");

                    System.out.println("i18ndir: " + config.getString("webservice.i18ndir"));
                    _i18nDir = new File(config.getString("webservice.i18ndir"));
                    _fileDir = new File(config.getString("webservice.filedir").replaceAll("%$",""));


                    _demoRoot = config.getLong("webservice.registration.root");
                    _demoGroup = config.getLong("webservice.registration.demogroup");
                    _registratioKey = config.getString("webservice.registration.apikey");


                    _fileIsLoaded = true;
                } else {
                    logger.fatal("Warning configfile does not exist: {}", cfile.getAbsolutePath());
//                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, "Warning configfile does not exist: " + cfile.getAbsolutePath());
                }

            }

        } catch (ConfigurationException ex) {
            logger.fatal("Unable to read config", ex);
//            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getRigestrationAPIKey() {
//        readConfigurationFile();
        return _registratioKey;
    }

    public static long getDemoGroup() {
//        readConfigurationFile();
        return _demoGroup;
    }

    public static long getDemoRoot() {
//        readConfigurationFile();
        return _demoRoot;
    }


    public static void CloseDS(JEVisDataSource ds) {
        try {
            if (ds != null) {
                ds.disconnect();
            }
        } catch (Exception ex) {
            logger.catching(org.apache.logging.log4j.Level.TRACE, ex);
        }
    }

    public static void CloseDS(SQLDataSource ds) {
        try {
            if (ds != null) {
                ds.getProfiler().printLog();
                ds.disconnect();
            }
        } catch (Exception ex) {
            logger.catching(org.apache.logging.log4j.Level.TRACE, ex);
        }
    }

}
