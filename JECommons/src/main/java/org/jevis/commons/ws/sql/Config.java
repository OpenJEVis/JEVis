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
import com.google.common.cache.Cache;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.ws.json.JsonJEVisClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);
    private static final List<String> jeccFiles = new ArrayList<>();
    private static final List<String> javaFiles = new ArrayList<>();
    private static final List<String> cors = new ArrayList<>();
    //@Singleton
    public static String dbPort = "3306";
    public static String dbIP = "127.0.0.1";
    public static String port = "80";
    public static String dbUser = "jevis";
    public static String dbPw = "jevistest";
    public static String schema = "jevis";
    public static String options = "";
    public static String rootUser = "jevis";
    public static String rootPW = "jevis";
    public static String url = "http://localhost:8080/JEWebservice/";
    public static String keyFile = "";
    public static String keyFilePW = "";
    public static String keyType = "";
    public static long demoRoot = -1;
    public static long demoGroup = -1;
    public static String registrationKey = "";
    private static boolean fileIsLoaded = false;
    private static File i18nDir;
    private static File fileDir;
    private static File classDir;
    private static File freemarkerDir;
    private static String latestJECCVersion = "0";
    private static String latestJavaVersion = "0";
    private static String webDir = "";
    private static String installerDir = "/var/www/html/installer";
    private static final ConcurrentHashMap<String, JsonJEVisClass> classCache = new ConcurrentHashMap<>();
    private static String latestJECCPath;
    private static String latestJavaPath;
    private static List<File> classFiles;
    private static boolean entraEnabled = false;
    private static String entraClientID = "";
    private static String entraTenantID = "";
    private static String entraAUTHORITY = "";
    private static String entraClientSecret = "";
    private static String entraConfigToken = "";


    public static String getDBHost() {
        return dbIP;
    }

    public static String getDBPort() {
        return dbPort;
    }

    public static String getDBUser() {
        return dbUser;
    }

    public static String getDBPW() {
        return dbPw;
    }

    public static String getSchema() {
        return schema;
    }

    public static String getConnectionOptions() {
        return options;
    }

    public static File getClassDir() {
        return classDir;
    }

    public static File getFileDir() {
        return fileDir;
    }

    public static File getI18nDir() {
        return i18nDir;
    }

    public static String getURI() {
        return url;
    }

    public static String getKeyStoreFile() {
        return keyFile;
    }

    public static String getKeyStorePW() {
        return keyFilePW;
    }

    public static String getKeyType() {
        return keyType;
    }

    public static File getFreemarkerDir() {
        return freemarkerDir;
    }

    public static String getWebDir() {
        return webDir;
    }

    public static List<String> getCORS() {
        return cors;
    }

    public static Cache<String, Session> sessions;
    public static int sessiontimeout;

    public static synchronized Map<String, JsonJEVisClass> getClassCache() {
        if (classCache.isEmpty()) {
            logger.debug("initializing class cache");
            try {
                File classDir = Config.getClassDir();

                if (classDir.exists()) {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    if (classFiles == null) {
                        classFiles = listClassFiles(Config.getClassDir());
                    }

                    for (File file : classFiles) {
                        try {
                            JsonJEVisClass data = objectMapper.readValue(file, JsonJEVisClass.class);
                            classCache.put(data.getName(), data);
                        } catch (Exception ex) {
                            logger.error("Error while loading Class file: {}", file.getName(), ex);
                        }
                    }
                }

                JEVisClassHelper.completeClasses(classCache);
                logger.debug("initialized class cache");
            } catch (Exception ex) {
                logger.error("Error while caching classes", ex);
            }

        }
        return classCache;
    }

    public static String getParameter(XMLConfiguration config, String key, String defaultValue) {
        try {
            if (config.getString(key) == null) return defaultValue;
            return config.getString(key);
        } catch (Exception nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }

    public static long getParameter(XMLConfiguration config, String key, long defaultValue) {
        try {
            return config.getLong(key);
        } catch (Exception nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }

    public static void readConfigurationFile(File cfile) {
        try {

            if (!fileIsLoaded) {
//                File cfile = new File("config.xml");
                if (cfile.exists()) {
                    String homeDir = System.getProperty("user.home");
//                    Logger.getLogger(Config.class.getName()).log(Level.INFO, "using Configfile: " + cfile.getAbsolutePath());
                    logger.info("using Configfile: {}", cfile.getAbsolutePath());
                    XMLConfiguration config = new XMLConfiguration(cfile);

                    dbPort = getParameter(config, "datasource.port", "8000");
                    dbIP = getParameter(config, "datasource.url", "localhost");
                    dbUser = getParameter(config, "datasource.login", "jevis");
                    dbPw = getParameter(config, "datasource.password", "jevispw");
                    schema = getParameter(config, "datasource.schema", "jevis");
                    options = getParameter(config, "datasource.options", "");

                    rootUser = getParameter(config, "sysadmin.username", "Sys Admin");
                    rootPW = getParameter(config, "sysadmin.password", "jevispw");
//                    _port = getParameter(config,"webservice.port","8000");//part of the uri now
                    url = getParameter(config, "webservice.uri", "http://127.0.0.1:8000/");

                    keyFile = getParameter(config, "webservice.keystore", homeDir + "/etc/keystore.jks");
                    keyFilePW = getParameter(config, "webservice.keystorepw", "jevispw");
                    keyType = getParameter(config, "webservice.keystoretype", "");

                    i18nDir = new File(getParameter(config, "webservice.i18ndir", homeDir + "/jevis/var/i18n/").replaceAll("%$", ""));
                    fileDir = new File(getParameter(config, "webservice.filedir", homeDir + "/jevis/var/files/").replaceAll("%$", ""));
                    classDir = new File(getParameter(config, "webservice.classdir", homeDir + "/jevis/var/classes/").replaceAll("%$", ""));
                    freemarkerDir = new File(getParameter(config, "webservice.freemarkerdir", homeDir + "/jevis/var/freemarker/").replaceAll("%$", ""));

                    //Workaround solution for the registration service
                    getJECCVersion();
                    getJavaVersion();

                    demoRoot = getParameter(config, "webservice.registration.root", -1);
                    demoGroup = getParameter(config, "webservice.registration.demogroup", -1);
                    registrationKey = getParameter(config, "webservice.registration.apikey", UUID.randomUUID().toString());

                    webDir = getParameter(config, "webservice.webpage", "");
                    installerDir = getParameter(config, "webservice.installer", "");
                    cors.addAll(Arrays.asList(getParameter(config, "webservice.cors", "").split(";")));

                    entraEnabled = Boolean.parseBoolean(getParameter(config, "entra.enabled", "false"));
                    entraAUTHORITY = getParameter(config, "entra.authority", "https://login.microsoftonline.com");
                    entraClientID = getParameter(config, "entra.clientid", "");
                    entraClientSecret = getParameter(config, "entra.clientsecret", "");
                    entraTenantID = getParameter(config, "entra.tenant", "");
                    entraConfigToken = getParameter(config, "entra.configtoken", "");
                    sessiontimeout = Integer.parseInt(getParameter(config, "webservice.sessiontimeout", "15"));

                    fileIsLoaded = true;
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

    public static String getEntraConfigToken() {
        return entraConfigToken;
    }

    public static String getLatestJECCVersion() {
        return latestJECCVersion;
    }

    public static String getLatestJECCPath() {
        return latestJECCPath;
    }

    public static String getJECCVersion() {
        try {
            String homeDirectory = System.getProperty("user.home");
            String relativePath = "/JEVisControlCenter/target/";

            String jeccPathString = homeDirectory + File.separator + "/jevis" + relativePath;
            File jeccPath = new File(jeccPathString);
            File[] folderContent = jeccPath.listFiles();

            if (folderContent == null) {
                Path jarPath = Paths.get(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent().getParent();
                folderContent = new File(jarPath.toString() + relativePath).listFiles();
            }

            if (folderContent != null) {
                Arrays.sort(folderContent, Comparator.comparingLong(File::lastModified).reversed());

                for (File file : folderContent) {
                    if (file.getName().contains("JEVisControlCenter-3.")) {
                        latestJECCPath = file.getAbsolutePath();
                        latestJECCVersion = file.getName().replace("JEVisControlCenter-", "").replace("-jar-with-dependencies.jar", "");
                        break;
                    }
                }


                for (final File fileEntry : folderContent) {
                    if (!fileEntry.isDirectory() && fileEntry.getName().contains("-jar-with-dependencies.jar")) {
                        jeccFiles.add(fileEntry.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return latestJECCVersion;
    }

    public static List<String> getJECCFilesPath() {
        return jeccFiles;
    }

    public static List<String> getJavaFilesPath() {
        return javaFiles;
    }

    public static String getLatestJavaVersion() {
        return latestJavaVersion;
    }

    public static String getLatestJavaPath() {
        return latestJavaPath;
    }

    public static String getJavaVersion() {
        try {
            String homeDirectory = System.getProperty("user.home");
            String relativePath = "/java/";

            String javaPathString = homeDirectory + File.separator + "/jevis" + relativePath;
            File javaPath = new File(javaPathString);
            File[] folderContent = javaPath.listFiles();

            if (folderContent == null) {
                Path jarPath = Paths.get(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent().getParent();
                folderContent = new File(jarPath.toString() + relativePath).listFiles();
            }

            if (folderContent != null) {
                Arrays.sort(folderContent, Comparator.comparingLong(File::lastModified).reversed());
                for (File file : folderContent) {
                    if (file.getName().contains("jre8")) {
                        latestJavaVersion = file.getName().replace(".zip", "");
                        latestJavaPath = file.getAbsolutePath();
                        break;
                    }
                }

                for (final File fileEntry : folderContent) {
                    if (!fileEntry.isDirectory()) {
                        javaFiles.add(fileEntry.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return latestJavaVersion;
    }

    public static String getRegistrationAPIKey() {
//        readConfigurationFile();
        return registrationKey;
    }

    public static long getDemoGroup() {
//        readConfigurationFile();
        return demoGroup;
    }

    public static long getDemoRoot() {
//        readConfigurationFile();
        return demoRoot;
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
                ds.disconnect();
            }
        } catch (Exception ex) {
            logger.catching(org.apache.logging.log4j.Level.TRACE, ex);
        }
    }

    private static List<File> listClassFiles(File dir) {
        List<File> fileTree = new ArrayList<>();
        if (dir == null || dir.listFiles() == null) {
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                if (entry.getName().endsWith(".json")) {
                    fileTree.add(entry);
                }
            } else {
                fileTree.addAll(listClassFiles(entry));
            }
        }
        return fileTree;
    }


    public static String getInstallerDir() {
        return installerDir;
    }

    public static boolean isEntraEnabled() {
        return entraEnabled;
    }

    public static void setEntraEnabled(boolean entraEnabled) {
        Config.entraEnabled = entraEnabled;
    }

    public static String getEntraClientID() {
        return entraClientID;
    }

    public static void setEntraClientID(String entraClientID) {
        Config.entraClientID = entraClientID;
    }

    public static String getEntraTenantID() {
        return entraTenantID;
    }

    public static void setEntraTenantID(String entraTenantID) {
        Config.entraTenantID = entraTenantID;
    }

    public static String getEntraAUTHORITY() {
        return entraAUTHORITY;
    }

    public static void setEntraAUTHORITY(String entraAUTHORITY) {
        Config.entraAUTHORITY = entraAUTHORITY;
    }

    public static String getEntraClientSecret() {
        return entraClientSecret;
    }

    public static void setEntraClientSecret(String entraClientSecret) {
        Config.entraClientSecret = entraClientSecret;
    }
}
