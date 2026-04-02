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
import com.google.common.cache.CacheBuilder;
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
import java.util.concurrent.TimeUnit;

/**
 * Application-wide configuration holder for the JEWebService.
 * <p>
 * Configuration values are read once from a single XML config file via
 * {@link #readConfigurationFile(File)} and then stored as public static fields
 * for fast access throughout the application.
 *
 * <p>The class also manages the JEVis class definition cache
 * ({@link #getClassCache()}). The cache is populated lazily from JSON files
 * on disk and is bounded by the number of class definition files present in
 * the configured class directory.
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class Config {

    private static final Logger logger = LogManager.getLogger(Config.class);
    private static final List<String> jeccFiles = new ArrayList<>();
    private static final List<String> javaFiles = new ArrayList<>();
    private static final List<String> cors = new ArrayList<>();
    /**
     * Bounded Guava cache for JEVis class definitions. Entries expire 30 minutes
     * after write so stale class definitions are automatically evicted without a
     * service restart. Maximum size of 500 comfortably covers any realistic
     * deployment (typical installs have fewer than 200 classes).
     */
    private static final Cache<String, JsonJEVisClass> classCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    /**
     * MySQL port (default {@code "3306"}). Set by {@link #readConfigurationFile}.
     */
    public static String dbPort = "3306";
    /** MySQL host name or IP address. Set by {@link #readConfigurationFile}. */
    public static String dbIP = "127.0.0.1";
    /** HTTP port for the Grizzly embedded server (informational only). */
    public static String port = "80";
    /** MySQL user name. Set by {@link #readConfigurationFile}. */
    public static String dbUser = "jevis";
    /** MySQL password. Set by {@link #readConfigurationFile}. */
    public static String dbPw = "jevistest";
    /** MySQL schema/database name. Set by {@link #readConfigurationFile}. */
    public static String schema = "jevis";
    /** Extra JDBC connection options (appended to the JDBC URL). */
    public static String options = "";
    /** System-admin account name. Set by {@link #readConfigurationFile}. */
    public static String rootUser = "jevis";
    /** System-admin password. Set by {@link #readConfigurationFile}. */
    public static String rootPW = "jevis";
    /** Base URI of this JEWebService instance. Set by {@link #readConfigurationFile}. */
    public static String url = "http://localhost:8080/JEWebservice/";
    /** Path to the Java KeyStore file for HTTPS. Set by {@link #readConfigurationFile}. */
    public static String keyFile = "";
    /** Password for the Java KeyStore. Set by {@link #readConfigurationFile}. */
    public static String keyFilePW = "";
    /** KeyStore type (e.g., {@code "JKS"}, {@code "PKCS12"}). Set by {@link #readConfigurationFile}. */
    public static String keyType = "";
    /** Object ID of the demo root object (self-registration feature), or {@code -1}. */
    public static long demoRoot = -1;
    /** Object ID of the demo user group (self-registration feature), or {@code -1}. */
    public static long demoGroup = -1;
    private static boolean fileIsLoaded = false;
    private static File i18nDir;
    private static File fileDir;
    private static File classDir;
    private static File freemarkerDir;
    private static String latestJECCVersion = "0";
    private static String latestJavaVersion = "0";
    private static String webDir = "";
    private static String installerDir = "/var/www/html/installer";
    /** API key required for self-registration requests. Set by {@link #readConfigurationFile}. */
    public static String registrationKey = "";
    private static String latestJECCPath;
    private static String latestJavaPath;
    private static List<File> classFiles;
    private static boolean entraEnabled = false;
    private static String entraClientID = "";
    private static String entraTenantID = "";
    private static String entraAUTHORITY = "";
    private static String entraClientSecret = "";
    private static String entraConfigToken = "";
    /** Shared HTTP-session cache; entries are managed by {@link CachedAccessControl}. */
    public static Cache<String, Session> sessions;
    /** HTTP session idle-timeout in minutes, read from the config file. */
    public static int sessiontimeout;

    /** @return the configured MySQL host name or IP address. */
    public static String getDBHost() {
        return dbIP;
    }

    /** @return the configured MySQL port (default {@code "3306"}). */
    public static String getDBPort() {
        return dbPort;
    }

    /** @return the MySQL user name used for all connections. */
    public static String getDBUser() {
        return dbUser;
    }

    /** @return the MySQL password used for all connections. */
    public static String getDBPW() {
        return dbPw;
    }

    /** @return the MySQL schema/database name. */
    public static String getSchema() {
        return schema;
    }

    /** @return any extra JDBC connection options (may be empty). */
    public static String getConnectionOptions() {
        return options;
    }

    /** @return the directory containing JEVis class JSON definition files. */
    public static File getClassDir() {
        return classDir;
    }

    /** @return the root directory for file-sample storage. */
    public static File getFileDir() {
        return fileDir;
    }

    /** @return the directory containing i18n property files. */
    public static File getI18nDir() {
        return i18nDir;
    }

    /** @return the base URI of the running JEWebService instance. */
    public static String getURI() {
        return url;
    }

    /** @return the path to the Java KeyStore file used for HTTPS. */
    public static String getKeyStoreFile() {
        return keyFile;
    }

    /** @return the password for the Java KeyStore. */
    public static String getKeyStorePW() {
        return keyFilePW;
    }

    /** @return the KeyStore type (e.g., {@code "JKS"}, {@code "PKCS12"}). */
    public static String getKeyType() {
        return keyType;
    }

    /** @return the directory containing Freemarker templates. */
    public static File getFreemarkerDir() {
        return freemarkerDir;
    }

    /** @return the path to the optional embedded web application directory. */
    public static String getWebDir() {
        return webDir;
    }

    /** @return the list of allowed CORS origins (may be empty). */
    public static List<String> getCORS() {
        return cors;
    }

    /**
     * Returns the JEVis class definition cache as an unmodifiable view, initializing
     * it from disk on the first call (or after expiry).
     * <p>
     * The underlying cache is a bounded Guava {@link Cache} keyed by class name.
     * It is populated by reading all {@code *.json} files found recursively under
     * the configured class directory. Inheritance relationships between classes
     * are resolved by {@link JEVisClassHelper#completeClasses} after loading.
     *
     * <p>Entries expire 30 minutes after write, ensuring stale class definitions
     * are eventually evicted. Maximum capacity is 500 entries.
     *
     * @return a live {@link Map} view of the class definition cache
     */
    public static synchronized Map<String, JsonJEVisClass> getClassCache() {
        classCache.cleanUp();
        if (classCache.size() == 0) {
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

                JEVisClassHelper.completeClasses(classCache.asMap());
                logger.debug("initialized class cache");
            } catch (Exception ex) {
                logger.error("Error while caching classes", ex);
            }

        }
        return classCache.asMap();
    }

    /**
     * Reads a String configuration value, returning {@code defaultValue} if
     * the key is absent or any error occurs.
     *
     * @param config       the parsed XML configuration
     * @param key          the XPath key to look up
     * @param defaultValue the fallback value
     * @return the configured value or {@code defaultValue}
     */
    public static String getParameter(XMLConfiguration config, String key, String defaultValue) {
        try {
            if (config.getString(key) == null) return defaultValue;
            return config.getString(key);
        } catch (Exception nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Reads a {@code long} configuration value, returning {@code defaultValue} if
     * the key is absent or any error occurs.
     *
     * @param config       the parsed XML configuration
     * @param key          the XPath key to look up
     * @param defaultValue the fallback value
     * @return the configured value or {@code defaultValue}
     */
    public static long getParameter(XMLConfiguration config, String key, long defaultValue) {
        try {
            return config.getLong(key);
        } catch (Exception nex) {
            logger.error("Missing parameter in config file: '{}' using default value: '{}'", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Reads the XML configuration file and populates all static config fields.
     * <p>
     * This method is idempotent: once the file has been loaded successfully
     * ({@code fileIsLoaded == true}), subsequent calls are no-ops.
     *
     * @param cfile the XML config file to read (e.g., {@code config.xml})
     */
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

    /** @return the Microsoft Entra config-token string used for SSO. */
    public static String getEntraConfigToken() {
        return entraConfigToken;
    }

    /** @return the version string of the latest JEVisControlCenter JAR found on disk. */
    public static String getLatestJECCVersion() {
        return latestJECCVersion;
    }

    /** @return the absolute path to the latest JEVisControlCenter JAR on disk. */
    public static String getLatestJECCPath() {
        return latestJECCPath;
    }

    /**
     * Scans the standard deployment directory for the latest JEVisControlCenter JAR,
     * populates {@link #latestJECCVersion} and {@link #latestJECCPath}, and returns
     * the version string.
     *
     * @return the latest JECC version string, or {@code "0"} if none is found
     */
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

    /** @return the list of absolute paths to all JEVisControlCenter JARs found on disk. */
    public static List<String> getJECCFilesPath() {
        return jeccFiles;
    }

    /** @return the list of absolute paths to all Java runtime archives found on disk. */
    public static List<String> getJavaFilesPath() {
        return javaFiles;
    }

    /** @return the version string of the latest Java runtime archive found on disk. */
    public static String getLatestJavaVersion() {
        return latestJavaVersion;
    }

    /** @return the absolute path to the latest Java runtime archive on disk. */
    public static String getLatestJavaPath() {
        return latestJavaPath;
    }

    /**
     * Scans the standard deployment directory for the latest Java runtime archive,
     * populates {@link #latestJavaVersion} and {@link #latestJavaPath}, and returns
     * the version string.
     *
     * @return the latest Java version string, or {@code "0"} if none is found
     */
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

    /** @return the API key required for self-registration requests. */
    public static String getRegistrationAPIKey() {
        return registrationKey;
    }

    /** @return the JEVis object ID of the demo user's group, or {@code -1} if not configured. */
    public static long getDemoGroup() {
        return demoGroup;
    }

    /** @return the JEVis object ID of the demo root object, or {@code -1} if not configured. */
    public static long getDemoRoot() {
        return demoRoot;
    }

    /**
     * Disconnects the given {@link JEVisDataSource}, logging and swallowing any
     * exception. Safe to call with a {@code null} argument.
     *
     * @param ds the data source to disconnect, or {@code null}
     */
    public static void CloseDS(JEVisDataSource ds) {
        try {
            if (ds != null) {
                ds.disconnect();
            }
        } catch (Exception ex) {
            logger.catching(org.apache.logging.log4j.Level.TRACE, ex);
        }
    }

    /**
     * Disconnects the given {@link SQLDataSource}, logging and swallowing any
     * exception. Safe to call with a {@code null} argument.
     *
     * @param ds the data source to disconnect, or {@code null}
     */
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


    /** @return the directory used by the software installer endpoint. */
    public static String getInstallerDir() {
        return installerDir;
    }

    /** @return {@code true} if Microsoft Entra (Azure AD) SSO is enabled. */
    public static boolean isEntraEnabled() {
        return entraEnabled;
    }

    /** @param entraEnabled {@code true} to enable Entra SSO at runtime. */
    public static void setEntraEnabled(boolean entraEnabled) {
        Config.entraEnabled = entraEnabled;
    }

    /** @return the Entra/Azure AD application (client) ID. */
    public static String getEntraClientID() {
        return entraClientID;
    }

    /** @param entraClientID the Entra application (client) ID. */
    public static void setEntraClientID(String entraClientID) {
        Config.entraClientID = entraClientID;
    }

    /** @return the Entra/Azure AD tenant ID. */
    public static String getEntraTenantID() {
        return entraTenantID;
    }

    /** @param entraTenantID the Entra tenant ID. */
    public static void setEntraTenantID(String entraTenantID) {
        Config.entraTenantID = entraTenantID;
    }

    /** @return the Entra/Azure AD authority URL. */
    public static String getEntraAUTHORITY() {
        return entraAUTHORITY;
    }

    /** @param entraAUTHORITY the Entra authority URL. */
    public static void setEntraAUTHORITY(String entraAUTHORITY) {
        Config.entraAUTHORITY = entraAUTHORITY;
    }

    /** @return the Entra/Azure AD client secret. */
    public static String getEntraClientSecret() {
        return entraClientSecret;
    }

    /** @param entraClientSecret the Entra client secret. */
    public static void setEntraClientSecret(String entraClientSecret) {
        Config.entraClientSecret = entraClientSecret;
    }
}
