/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.driver.DataSourceFactory;
import org.jevis.commons.driver.DriverHelper;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 * @author ai
 */
public class Launcher extends AbstractCliApp {

    private static final String APP_INFO = "JEDataCollector";
    private final String APP_SERVICE_CLASS_NAME = "JEDataCollector";
    public static String KEY = "process-id";
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private final Command commands = new Command();


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEDataCollector-------");
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
    private void executeDataSources(List<JEVisObject> dataSources) {

        logger.info("Number of Requests: " + dataSources.size());

        dataSources.parallelStream().forEach(object -> {
            forkJoinPool.submit(() -> {
                if (!runningJobs.containsKey(object.getID())) {
                    Thread.currentThread().setName(object.getName() + ":" + object.getID().toString());

                    DataSource dataSource = DataSourceFactory.getDataSource(object);
                    if (dataSource.isReady(object)) {
                        runningJobs.put(object.getID(), "true");
                        LogTaskManager.getInstance().buildNewTask(object.getID(), object.getName());

                        logger.info("----------------Execute DataSource " + object.getName() + "-----------------");
                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.STARTED);

                        dataSource.initialize(object);
                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.RUNNING);
                        dataSource.run();

                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FINISHED);
                        runningJobs.remove(object.getID());
                        plannedJobs.remove(object.getID());
                        dataSource.finishCurrentRun(object);
                        logger.info("----------------Finished DataSource " + object.getName() + "-----------------");

                        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                            logger.info("Last job. Clearing cache.");
                            ds.clearCache();
                        }
                    } else {
                        logger.error("Still waiting for next DataSource Cycle " + object.getName() + ":" + object.getID());
                    }

                } else {
                    logger.error("Still processing DataSource " + object.getName() + ":" + object.getID());
                }
            });
        });

        logger.info("---------------------finish------------------------");

    }


    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
        DriverHelper.loadDriver(ds, commands.driverFolder);
    }

    @Override
    protected void runSingle(Long id) {
        logger.info("Start Single Mode");

        try {
            ds.clearCache();
            ds.preload();
        } catch (JEVisException e) {
            logger.error(e);
        }

        try {
            logger.info("Try adding Single Mode for ID " + id);
            JEVisObject dataSourceObject = ds.getObject(id);
            DataSource dataSource = DataSourceFactory.getDataSource(dataSourceObject);
            dataSource.initialize(dataSourceObject);
            dataSource.run();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }


    @Override
    protected void runServiceHelp() {

        try {
            ds.clearCache();
            ds.preload();
            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error(e);
        }

        if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
            logger.info("Service is enabled.");
            List<JEVisObject> dataSources = getEnabledDataSources(ds);
            executeDataSources(dataSources);
        } else {
            logger.info("Service is disabled.");
        }
        try {
            logger.info("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);
            TaskPrinter.printJobStatus(LogTaskManager.getInstance());

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
        executeDataSources(dataSources);
    }


    private List<JEVisObject> getEnabledDataSources(JEVisDataSource client) {
        List<JEVisObject> enabledDataSources = new ArrayList<JEVisObject>();
        try {
            SampleHandler sampleHandler = new SampleHandler();
            JEVisClass dataSourceClass = client.getJEVisClass(DataCollectorTypes.DataSource.NAME);
            List<JEVisObject> allDataSources = client.getObjects(dataSourceClass, true);
            for (JEVisObject dataSource : allDataSources) {
                try {
                    Boolean enabled = sampleHandler.getLastSample(dataSource, DataCollectorTypes.DataSource.ENABLE, false);
                    if (enabled && DataSourceFactory.containDataSource(dataSource)) {
                        enabledDataSources.add(dataSource);
                        if (!plannedJobs.containsKey(dataSource.getID())) {
                            plannedJobs.put(dataSource.getID(), "true");
                        }
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


}
