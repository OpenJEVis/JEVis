/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.driver.DataSourceFactory;
import org.jevis.commons.driver.DriverHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * @author broder
 * @author ai
 */
public class Launcher extends AbstractCliApp {

    public static final String APP_INFO = "JEDataCollector 2018-02-21";
    public static String KEY = "process-id";
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private int cycleTime = 900000;
    private final Command commands = new Command();
    private ForkJoinPool forkJoinPool;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEDataCollector 2018-02-01-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    public Launcher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * Run all datasources in Threads. The maximum number of threads is defined
     * in the JEVis System. There is only one thread per data source.
     * <p>
     *
     * @param dataSources
     */
    private void excecuteDataSources(List<JEVisObject> dataSources) {

        initializeThreadPool();

        logger.info("Number of Requests: " + dataSources.size());

        forkJoinPool.submit(
                () -> dataSources.parallelStream().forEach(object -> {
                    logger.info("----------------Execute DataSource " + object.getName() + "-----------------");
                    DataSource dataSource = DataSourceFactory.getDataSource(object);

                    dataSource.initialize(object);
                    dataSource.run();
                }));

        logger.info("---------------------finish------------------------");

    }

    private void initializeThreadPool() {
        Integer threadCount = 4;
        try {
            JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
            List<JEVisObject> listDataCollectorObjects = ds.getObjects(dataCollectorClass, false);
            threadCount = listDataCollectorObjects.get(0).getAttribute("Max Number Threads").getLatestSample().getValueAsLong().intValue();
            logger.info("Set Thread count to: " + threadCount);
        } catch (Exception e) {

        }
        forkJoinPool = new ForkJoinPool(threadCount);
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        DriverHelper.loadDriver(ds, commands.driverFolder);
    }

    private Boolean checkServiceStatus() {
        Boolean enabled = true;
        try {
            JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
            List<JEVisObject> listDataCollectorObjects = ds.getObjects(dataCollectorClass, false);
            enabled = listDataCollectorObjects.get(0).getAttribute("Enable").getLatestSample().getValueAsBoolean();
        } catch (JEVisException e) {

        }
        return enabled;
    }

    @Override
    protected void runSingle(Long id) {
        logger.info("Start Single Mode");

        List<JEVisObject> dataSources = new ArrayList<JEVisObject>();

        try {
            logger.info("Try adding Single Mode for ID " + id);
            dataSources.add(ds.getObject(id));

            excecuteDataSources(dataSources);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    protected void runService(Integer cycle_time) {
        logger.info("Start Service Mode");

        Thread service = new Thread(() -> runServiceHelp());
        Runtime.getRuntime().addShutdownHook(
                new JEDataCollectorShutdownHookThread(service)
        );

        if (cycle_time != null) cycleTime = cycle_time;

        try {
            service.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            logger.info("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runServiceHelp() {

        try {
            ds.reloadAttributes();
            getCycleTimeFromService();
        } catch (JEVisException e) {
            logger.error(e);
        }

        if (checkServiceStatus()) {
            logger.info("Service is enabled.");
            List<JEVisObject> dataSources = getEnabledDataSources(ds);
            excecuteDataSources(dataSources);
        } else {
            logger.info("Service is disabled.");
        }
        try {
            logger.info("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }

    }

    @Override
    protected void runComplete() {
        logger.info("Start Compete Mode");
        List<JEVisObject> dataSources = new ArrayList<JEVisObject>();
        dataSources = getEnabledDataSources(ds);
        excecuteDataSources(dataSources);
    }

    protected class Command {

        @Parameter(names = {"--driver-folder", "-df"}, description = "Sets the root folder for the driver structure")
        private String driverFolder;
    }

    private List<JEVisObject> getEnabledDataSources(JEVisDataSource client) {
        List<JEVisObject> enabledDataSources = new ArrayList<JEVisObject>();
        try {
            JEVisClass dataSourceClass = client.getJEVisClass(DataCollectorTypes.DataSource.NAME);
            JEVisType enabledType = dataSourceClass.getType(DataCollectorTypes.DataSource.ENABLE);
            List<JEVisObject> allDataSources = client.getObjects(dataSourceClass, true);
            for (JEVisObject dataSource : allDataSources) {
                try {
                    Boolean enabled = DatabaseHelper.getObjectAsBoolean(dataSource, enabledType);
                    if (enabled && DataSourceFactory.containDataSource(dataSource)) {
                        enabledDataSources.add(dataSource);
                    }
                } catch (Exception ex) {
                    logger.error("DataSource failed while checking enabled status:", ex);
                }
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return enabledDataSources;
    }

    private void getCycleTimeFromService() throws JEVisException {
        JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
        List<JEVisObject> listDataCollectorObjects = ds.getObjects(dataCollectorClass, false);
        cycleTime = listDataCollectorObjects.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong().intValue();
        logger.info("Service cycle time from service: " + cycleTime);
    }

}
