/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author bf
 */
public class DriverHelper {
    private static final Logger logger = LogManager.getLogger(DriverHelper.class);

    private static final String JEVIS_SOURCE = "jevis";
    private static String ROOT_FOLDER = null;

    public static void loadDriver(JEVisDataSource client, String rootFolder) {
        logger.info("loadDriver: {}, {}", client, rootFolder);
        Map<String, Class> parserClasses;
        Map<String, Class> dsClasses;
        Map<String, Class> converterClasses;
        Map<String, Class> importerClasses;
        if (rootFolder != null) {
            logger.info("load config from folder");
            ROOT_FOLDER = rootFolder;
            parserClasses = loadDriverFromFolder(client, DataCollectorTypes.ParserDriverDirectory.NAME, DataCollectorTypes.Driver.ParserDriver.NAME, ROOT_FOLDER + "/jedc_data/driver/parser/", ROOT_FOLDER + "/jedc_data/dev_driver/parser/");
            dsClasses = loadDriverFromFolder(client, DataCollectorTypes.DataSourceDriverDirectory.NAME, DataCollectorTypes.Driver.DataSourceDriver.NAME, ROOT_FOLDER + "/jedc_data/driver/datasource/", ROOT_FOLDER + "/jedc_data/dev_driver/datasource/");
            converterClasses = loadDriverFromFolder(client, DataCollectorTypes.ConverterDriverDirectory.NAME, DataCollectorTypes.Driver.ConverterDriver.NAME, ROOT_FOLDER + "/jedc_data/driver/converter/", ROOT_FOLDER + "/jedc_data/dev_driver/converter/");
            importerClasses = loadDriverFromFolder(client, DataCollectorTypes.ImporterDriverDirectory.NAME, DataCollectorTypes.Driver.ImporterDriver.NAME, ROOT_FOLDER + "/jedc_data/driver/importer/", ROOT_FOLDER + "/jedc_data/dev_driver/importer/");
        } else {
            logger.info("load config from db");
            parserClasses = loadDriverFromConfig(client, DataCollectorTypes.ParserDriverDirectory.NAME, DataCollectorTypes.Driver.ParserDriver.NAME);
            dsClasses = loadDriverFromConfig(client, DataCollectorTypes.DataSourceDriverDirectory.NAME, DataCollectorTypes.Driver.DataSourceDriver.NAME);
            converterClasses = loadDriverFromConfig(client, DataCollectorTypes.ConverterDriverDirectory.NAME, DataCollectorTypes.Driver.ConverterDriver.NAME);
            importerClasses = loadDriverFromConfig(client, DataCollectorTypes.ImporterDriverDirectory.NAME, DataCollectorTypes.Driver.ImporterDriver.NAME);

        }
        ParserFactory.setParserClasses(parserClasses);
        DataSourceFactory.setDataSourceClasses(dsClasses);
        ConverterFactory.setConverterClasses(converterClasses);
        ImporterFactory.setImporterClasses(importerClasses);
    }

    private static Map<String, Class> loadDriverFromConfig(JEVisDataSource client, String driverDirClassName, String driverClassName) {
        logger.debug("loadDriverFromConfig: jedb: {}, driverDir: {}, driverDirclass: {}", client, driverDirClassName, driverClassName);
        Set<DriverProperty> parserAttributes = initDriverAttributes(client, driverDirClassName, driverClassName);
        Map<String, Class> classes = new HashMap<String, Class>();
        for (DriverProperty driverProp : parserAttributes) {
            try {
                JEVisType fileType = driverProp.getDriver().getJEVisClass().getType(DataCollectorTypes.Driver.SOURCE_FILE);
                JEVisFile file = DatabaseHelper.getObjectAsFile(driverProp.getDriver(), fileType);
                Class c = ByteClassLoader.loadDriver(file, driverProp.getClassName());
                classes.put(driverProp.getJevisName(), c);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            } catch (FileNotFoundException ex) {
                logger.fatal(ex);
            } catch (IOException ex) {
                logger.fatal(ex);
            } catch (ClassNotFoundException ex) {
                logger.fatal(ex);
            }
        }
        return classes;
    }

    private static Map<String, Class> loadDriverFromFolder(JEVisDataSource client, String driverDirClassName, String driverClassName, String driverFolder, String driverDevFolder) {
        Set<DriverProperty> parserAttributes = initDriverAttributes(client, driverDirClassName, driverClassName);

        for (final File fileEntry : new File(driverFolder).listFiles()) {
            if (fileEntry.isFile()) {
                for (DriverProperty driverProp : parserAttributes) {
                    if (driverProp.getJevisName().equals(fileEntry.getName().replace(".jar", "").replace(" ", "_"))) {
                        try {
                            Path file = Paths.get(driverFolder + fileEntry.getName());
                            DateTime dateTime = new DateTime(Files.getLastModifiedTime(file).toMillis());
                            if (dateTime.isAfter(driverProp.getFileDate())) {
                                driverProp.setFileSource(driverFolder + fileEntry.getName());
                            }
                        } catch (IOException ex) {
                            logger.fatal(ex);
                        }
                    }
                }
            }
        }

        for (final File fileEntry : new File(driverDevFolder).listFiles()) {
            if (fileEntry.isFile()) {
                for (DriverProperty driverProp : parserAttributes) {
                    if (driverProp.getJevisName().equals(fileEntry.getName().replace(".jar", "").replace(" ", "_"))) {
                        driverProp.setFileSource(driverDevFolder + fileEntry.getName());
                    }
                }
            }
        }

        for (DriverProperty driverProp : parserAttributes) {
            if (driverProp.getFileSource().equals(JEVIS_SOURCE)) {
                try {
                    JEVisType fileType = driverProp.getDriver().getJEVisClass().getType(DataCollectorTypes.Driver.SOURCE_FILE);
                    JEVisFile file = DatabaseHelper.getObjectAsFile(driverProp.getDriver(), fileType);
                    String fileSource = driverFolder + driverProp.getJevisName().replace(" ", "_") + ".jar";
                    FileOutputStream fos = new FileOutputStream(fileSource, false);
                    fos.write(file.getBytes());
                    fos.close();
                    driverProp.setFileSource(fileSource);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                } catch (FileNotFoundException ex) {
                    logger.fatal(ex);
                } catch (IOException ex) {
                    logger.fatal(ex);
                }
            }
        }

        Map<String, Class> classes = new HashMap<String, Class>();
        for (DriverProperty driverProp : parserAttributes) {
            try {
                URL url = new File(driverProp.getFileSource()).toURI().toURL();
                ClassLoader cl = new ByteClassLoader(url);
                try {
                    Class c = cl.loadClass(driverProp.getClassName());
                    classes.put(driverProp.getJevisName(), c);
                    logger.info(String.format("Done loading driver: %s", c));
                } catch (ClassNotFoundException ex) {
                    logger.fatal(String.format("Fail loading driver: %s", driverProp.getClassName()), ex);
                }
            } catch (MalformedURLException ex) {
                logger.fatal(ex);
            }
        }
        return classes;
    }

    private static Set<DriverProperty> initDriverAttributes(JEVisDataSource client, String driverDirClassName, String driverClassName) {
        Set<DriverProperty> driverProperties = new HashSet<DriverProperty>();
        try {
            JEVisClass serviceClass = client.getJEVisClass(DataCollectorTypes.JEDataCollector.NAME);
            JEVisClass driverDirClass = client.getJEVisClass(driverDirClassName);
            JEVisClass jevisClass = client.getJEVisClass(driverClassName);
            List<JEVisObject> dataCollectorServices = client.getObjects(serviceClass, false);
            if (dataCollectorServices.size() == 1) {
                JEVisObject dataCollectorService = dataCollectorServices.get(0);
                List<JEVisObject> driverDirs = dataCollectorService.getChildren(driverDirClass, false);
                if (driverDirs.size() == 1) {
                    JEVisObject driverDir = driverDirs.get(0);
                    for (JEVisObject driver : driverDir.getChildren(jevisClass, true)) {
                        logger.debug("init driver: {}", driver);
                        JEVisType enableType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.ENABLED);
                        Boolean enabled = DatabaseHelper.getObjectAsBoolean(driver, enableType);
                        logger.debug("Driver is enabled: {}", enabled);
                        if (!enabled) {
                            continue;
                        }

                        JEVisType fileType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.SOURCE_FILE);
                        DateTime fileDate = driver.getAttribute(fileType).getTimestampOfLastSample();
                        logger.debug("Driver file date: {}", fileDate);

                        JEVisType classType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.MAIN_CLASS);
                        String className = DatabaseHelper.getObjectAsString(driver, classType);
                        logger.debug("Main Class: {}", className);

                        JEVisType jevisType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.JEVIS_CLASS);
                        String jevisName = DatabaseHelper.getObjectAsString(driver, jevisType);
                        logger.debug("JEVis Class: {}", className);

                        driverProperties.add(new DriverProperty(driver, fileDate, className, jevisName, JEVIS_SOURCE));
                    }
                }
            }
        } catch (Exception ex) {
            logger.fatal("Error while init driver attributes", ex);
        }
        return driverProperties;
    }
}
