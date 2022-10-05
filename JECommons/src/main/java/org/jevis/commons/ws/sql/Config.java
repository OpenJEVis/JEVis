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
package org.jevis.commons.ws.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.commons.ws.json.JsonJEVisClass;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public static String _options = "";
    public static String _rootUser = "jevis";
    public static String _rootPW = "jevis";
    public static String _uri = "http://localhost:8080/JEWebservice/";
    public static String _keyFile = "";
    public static String _keyFilePW = "";
    public static String _keyType = "";


    public static long _demoRoot = -1;
    public static long _demoGroup = -1;
    public static String _registratioKey = "";

    private static final boolean _loadFromFile = true;
    private static boolean _fileIsLoaded = false;

    private static File _i18nDir;
    private static File _fileDir;
    private static File _classDir;
    private static File _freemarkerDir;
    private static String _jeccVersion = "0";
    private static String _jeccFile = "";
    private static String _javaVersion = "0";
    private static String _javaFile = "";
    private static String _webDir = "";
    private static ConcurrentHashMap<String, JsonJEVisClass> _classCache = new ConcurrentHashMap<>();


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

    public static String getConnectionOptions() {
        return _options;
    }

    private static final Map<String, JsonClassRelationship> _relationshipCache = Collections.synchronizedMap(new HashMap<String, JsonClassRelationship>());

    public static File getClassDir() {
        return _classDir;
    }

    public static File getFileDir() {
        return _fileDir;
    }

    public static File getI18nDir() {
        return _i18nDir;
    }

    public static String getURI() {
        return _uri;
    }

    public static String getKeyStoreFile() {
        return _keyFile;
    }

    public static String getKeyStorePW() {
        return _keyFilePW;
    }

    public static String getKeyType() {
        return _keyType;
    }

    public static File getFreemarkerDir() {
        return _freemarkerDir;
    }

    public static String getWebDir() {
        return _webDir;
    }

    public static void setFreemarkerDir(File _freemarkerDir) {
        Config._freemarkerDir = _freemarkerDir;
    }


    public static synchronized Map<String, JsonJEVisClass> getClassCache() {
        if (_classCache.isEmpty()) {
            logger.info("initialize class cache");
            try {
                //        Gson gson = new GsonBuilder().create();

                File classDir = Config.getClassDir();

                if (classDir.exists()) {
                    FileFilter jsonFilter = new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            return pathname.getName().endsWith(".json");
                        }
                    };
                    final ObjectMapper objectMapper = new ObjectMapper();
                    Arrays.stream(Objects.requireNonNull(classDir.listFiles(jsonFilter))).parallel().forEach(jsonFile -> {
                        try {
//                    JsonReader reader = new JsonReader(new FileReader(jsonFile));
//                    JsonJEVisClass data = gson.fromJson(reader, JsonJEVisClass.class);

                            JsonJEVisClass data = objectMapper.readValue(jsonFile, JsonJEVisClass.class);
                            _classCache.put(data.getName(), data);

                        } catch (Exception ex) {
                            logger.error("Error while loading Classfile: " + jsonFile.getName(), ex);
                        }
                    });
                }

                JEVisClassHelper.completeClasses(_classCache);
                logger.info("Done");
            } catch (Exception ex) {
                logger.error("Error while caching classes", ex);
            }

        }
        return _classCache;
    }

    public static void setClassCache(ConcurrentHashMap<String, JsonJEVisClass> map) {
        _classCache = map;
    }

    public static String getParameter(XMLConfiguration config, String key, String defaultValue) {
        try {
            return config.getString(key);
        } catch (NullPointerException nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }

    public static long getParameter(XMLConfiguration config, String key, long defaultValue) {
        try {
            return config.getLong(key);
        } catch (NullPointerException nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }

    public static void readConfigurationFile(File cfile) {
        try {

            if (!_fileIsLoaded) {
//                File cfile = new File("config.xml");
                if (cfile.exists()) {
                    String homeDir = System.getProperty("user.home");
//                    Logger.getLogger(Config.class.getName()).log(Level.INFO, "using Configfile: " + cfile.getAbsolutePath());
                    logger.info("using Configfile: {}", cfile.getAbsolutePath());
                    XMLConfiguration config = new XMLConfiguration(cfile);

                    _dbport = getParameter(config, "datasource.port", "8000");
                    _dbip = getParameter(config, "datasource.url", "localhost");
                    _dbuser = getParameter(config, "datasource.login", "jevis");
                    _dbpw = getParameter(config, "datasource.password", "jevispw");
                    _schema = getParameter(config, "datasource.schema", "jevis");
                    _options = getParameter(config, "datasource.options", "");

                    _rootUser = getParameter(config, "sysadmin.username", "Sys Admin");
                    _rootPW = getParameter(config, "sysadmin.password", "jevispw");
//                    _port = getParameter(config,"webservice.port","8000");//part of the uri now
                    _uri = getParameter(config, "webservice.uri", "http://127.0.0.1:8000/");

                    _keyFile = getParameter(config, "webservice.keystore", homeDir + "/etc/keystore.jks");
                    _keyFilePW = getParameter(config, "webservice.keystorepw", "jevispw");
                    _keyType = getParameter(config, "webservice.keystoretype", "");

                    _i18nDir = new File(getParameter(config, "webservice.i18ndir", homeDir + "/jevis/var/i18n/").replaceAll("%$", ""));
                    _fileDir = new File(getParameter(config, "webservice.filedir", homeDir + "/jevis/var/files/").replaceAll("%$", ""));
                    _classDir = new File(getParameter(config, "webservice.classdir", homeDir + "/jevis/var/classes/").replaceAll("%$", ""));
                    _freemarkerDir = new File(getParameter(config, "webservice.freemarkerdir", homeDir + "/jevis/var/freemarker/").replaceAll("%$", ""));

                    //Woraround solution for the registration service
                    getJECCVersion();
                    getJavaVersion();

                    _demoRoot = getParameter(config, "webservice.registration.root", -1);
                    _demoGroup = getParameter(config, "webservice.registration.demogroup", -1);
                    _registratioKey = getParameter(config, "webservice.registration.apikey", UUID.randomUUID().toString());

                    _webDir = getParameter(config, "webservice.webpage", "");

                    _fileIsLoaded = true;
                } else {
                    logger.fatal("Warning config file does not exist: {}", cfile.getAbsolutePath());
//                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, "Warning config file does not exist: " + cfile.getAbsolutePath());
                }

            }

        } catch (ConfigurationException ex) {
            logger.fatal("Unable to read config", ex);
//            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getJECCVersion() {
        try {
            String homeDirectory = System.getProperty("user.home");

            String jeccPathString = homeDirectory + File.separator + "/jevis/JEVisControlCenter/target/";
            File jeccPath = new File(jeccPathString);
            File[] folderContent = jeccPath.listFiles();

            if (folderContent != null) {
                for (final File fileEntry : folderContent) {
                    if (!fileEntry.isDirectory() && fileEntry.getName().contains("-jar-with-dependencies.jar")) {
                        _jeccVersion = fileEntry.getName().replace("JEVisControlCenter-", "").replace("-jar-with-dependencies.jar", "");
                        _jeccFile = fileEntry.getAbsolutePath();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return _jeccVersion;
    }

    public static String getJECCFilePath() {
        return _jeccFile;
    }

    public static String getJavaFilePath() {
        return _javaFile;
    }

    public static String getJavaVersion() {
        try {
            String homeDirectory = System.getProperty("user.home");

            String javaPathString = homeDirectory + File.separator + "/jevis/java/";
            File javaPath = new File(javaPathString);
            File[] folderContent = javaPath.listFiles();

            if (folderContent != null) {
                Arrays.sort(folderContent, Comparator.comparingLong(File::lastModified).reversed());

                File last = folderContent[0];

                _javaVersion = last.getName().replace(".zip", "");
                _javaFile = last.getAbsolutePath();

            }
        } catch (Exception e) {
            logger.error(e);
        }
        return _javaVersion;
    }

    public static void setJECCVersion(String jeccVersion) {
        Config._jeccVersion = _jeccVersion;
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
//                ds.getProfiler().printLog();
                ds.disconnect();
//                ds.clear();
//                ds = null;
            }
        } catch (Exception ex) {
            logger.catching(org.apache.logging.log4j.Level.TRACE, ex);
        }
    }


}
