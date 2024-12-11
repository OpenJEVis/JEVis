package org.jevis.rest;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.util.MimeType;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.jevis.api.JEVisException;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.ConnectionFactory;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;

import javax.security.sasl.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static String VERSION = "JEWebService Version";

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) throws SQLException, AuthenticationException, JEVisException {
        //read Config
        File configfile;
        String newestVerion = "";
        String jarCreationDate = "";
        boolean cleanFiles = false;

        try {
            Path jarPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            newestVerion = jarPath.getFileName().toString().replace("JEWebService-", "").replace("-jar-with-dependencies.jar", "");
            VERSION += " " + newestVerion;
        } catch (Exception e) {
            logger.error(e);
        }

        try {
            Path jarPath = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            BasicFileAttributes basicFileAttributes = Files.readAttributes(jarPath, BasicFileAttributes.class);
            FileTime fileTime = basicFileAttributes.creationTime();
            DateTime fileDate = new DateTime(fileTime.toMillis());
            jarCreationDate = (fileDate.toString(JEVisDates.DEFAULT_DATE_FORMAT));
            VERSION += " " + jarCreationDate;
        } catch (URISyntaxException | IOException e) {
            logger.error(e);
        }

        logger.info("Start - {}", VERSION);

        if (args.length >= 1) {
            configfile = new File(args[0]);
        } else {
            //default workaround
            configfile = new File("config.xml");
            if (!configfile.exists()) {
                logger.info("No config file try: using ../config.xml");
                configfile = new File("../config.xml");
            } else {
                logger.info("No config file: try using config.xml");
            }
        }
        Config.readConfigurationFile(configfile);

        if (args.length >= 2) {
            cleanFiles = Boolean.parseBoolean(args[1]);
        }

        //Test Connection parameter
//        for (String para : args) {
//            logger.info("para: " + para);
//            //if (para.equalsIgnoreCase("-test")) {
//
//            // }
//        }
        logger.info("DBHost: {}\nDBPort: {}\nDBSchema: {}\nDBUSer: {}\nDBPW: {} \nDBOptions: '{}'",
                Config.getDBHost(), Config.getDBPort(), Config.getSchema(), Config.getDBUser(), Config.getDBPW(), Config.getConnectionOptions());
        ConnectionFactory.getInstance().registerMySQLDriver(Config.getDBHost(), Config.getDBPort(), Config.getSchema(), Config.getDBUser(), Config.getDBPW(), Config.getConnectionOptions());


        Connection dbConn = ConnectionFactory.getInstance().getConnection();
        if (dbConn.isValid(2000)) {
            logger.info("Database Connection is working");
        } else {
            logger.info("Database Connection is NOT working");
            System.exit(1);
        }


        final ResourceConfig rc = new ResourceConfig().packages("org.jevis.rest", "org.jevis.iso.rest", "org.jevis.web");
        rc.setApplicationName("JEWebservice");
        rc.register(MultiPartFeature.class);
        rc.register(GZipEncoder.class);
        rc.register(EncodingFilter.class);

        final HttpServer server;

        if (Config.getURI().toLowerCase().startsWith("https")) {
            SSLContextConfigurator sslCon = new SSLContextConfigurator();
            sslCon.setKeyStoreFile(Config.getKeyStoreFile());
            sslCon.setKeyStorePass(Config.getKeyStorePW());

            if (!Config.getKeyType().isEmpty()) {
                sslCon.setKeyStoreType(Config.getKeyType());//PKCS12
            }

            server = GrizzlyHttpServerFactory.createHttpServer(
                    URI.create(Config.getURI()),
                    rc,
                    true,
                    new SSLEngineConfigurator(sslCon, false, false, false)
            );
        } else {
            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(Config.getURI()), rc);
        }

        if (Config.getWebDir() != null && !Config.getWebDir().isEmpty()) {
            logger.info("Init webpage: " + Config.getWebDir());
            server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(
                    Config.getWebDir()), "/web");
        }


        MimeType.add("msix", "application/msix");
        MimeType.add("appx", "application/appx");
        MimeType.add("msixbundle", "application/msixbundle");
        MimeType.add("appxbundle", "application/appxbundle");
        MimeType.add("appinstaller", "application/appinstaller");

        CompressionConfig compressionConfig =
                server.getListener("grizzly").getCompressionConfig();
        compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.ON); // the mode
        compressionConfig.setCompressionMinSize(2048); // the min amount of bytes to compress
        compressionConfig.setCompressableMimeTypes("text/plain", "text/html"
                , "application/json", "application/msixbundle", "application/appxbundle", "application/appinstaller"
                , "application/msix", "application/appx");


        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(
                new GrizzlyServerShutdownHookThread(server)
        );

        if (cleanFiles) {
            cleanFilesFolder(dbConn);
        }

        // run
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Server error: ", e);
            throw new RuntimeException(e);
        }
        try {
            logger.info("{} is now running", VERSION);
            logger.info("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Server error: ", e);
            throw new RuntimeException(e);
        }
    }

    private static void cleanFilesFolder(Connection dbConn) {
        try {
            String dir = Config.getFileDir().getAbsolutePath();

            Set<String> folderContent = listFilesUsingFilesList(dir);
            List<String> deleteList = new ArrayList<>();

            SQLDataSource ds = new SQLDataSource(dbConn);
            for (String s : folderContent) {
                try {
                    long l = Long.parseLong(s);
                    if (ds.getObject(l) == null) {
                        deleteList.add(s);
                    }
                } catch (Exception e) {
                    logger.error("Folder name not a number: {}", s, e);
                }
            }

            logger.info("{} file folders without corresponding JEVis objects. Deleting content.", deleteList.size());

            for (String s : deleteList) {
                File folderToDelete = new File(dir + "/" + s);
                logger.info("Deleting folder with content: {}", folderToDelete);
                FileUtils.deleteDirectory(folderToDelete);
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private static Set<String> listFilesUsingFilesList(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }
}


/**
 * KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
 * <p>
 * char[] password = "some password".toCharArray();
 * ks.load(null, password);
 * <p>
 * // Store away the keystore.
 * FileOutputStream fos = new FileOutputStream("newKeyStoreFileName");
 * ks.store(fos, password);
 * fos.close();
 */
