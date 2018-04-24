package org.jevis.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {

    public static final String VERSION = "JEWebService Version 2018-02-20";
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.info("Start - {}", VERSION);
        //read Config
        File configfile;
        if (args.length >= 1) {
            configfile = new File(args[0]);
        } else {
            //default workaround
            configfile = new File("config.xml");
            if (!configfile.exists()) {
                System.out.println("No config file try: using ../config.xml");
                configfile = new File("../config.xml");
            } else {
                System.out.println("No config file: try using config.xml");
            }
        }

        Config.readConfigurationFile(configfile);

        final ResourceConfig rc = new ResourceConfig().packages("org.jevis.rest", "org.jevis.iso.rest");
        rc.setApplicationName("JEWebservice");
        rc.register(MultiPartFeature.class);

        final HttpServer server;

        if (Config.getURI().toLowerCase().startsWith("https")) {
            SSLContextConfigurator sslCon = new SSLContextConfigurator();
            sslCon.setKeyStoreFile(Config.getKeyStoreFile());
            sslCon.setKeyStorePass(Config.getKeyStorePW());
            

            server = GrizzlyHttpServerFactory.createHttpServer(
                    URI.create(Config.getURI()),
                    rc,
                    true,
                    new SSLEngineConfigurator(sslCon, false, false, false)
            );
        }else{
            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(Config.getURI()), rc);
        }

        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping server..");
            server.stop();
        }, "shutdownHook"));

        // run
        try {
            server.start();
            System.out.println("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


/*
  KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

char[] password = "some password".toCharArray();
ks.load(null, password);

// Store away the keystore.
FileOutputStream fos = new FileOutputStream("newKeyStoreFileName");
ks.store(fos, password);
fos.close();

 */
