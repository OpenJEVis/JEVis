/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedatacollector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.driver.DataSourceFactory;
import org.jevis.commons.driver.DriverHelper;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

/**
 * @author broder
 * @author ai
 */
public class Launcher extends AbstractCliApp {

    private static final String APP_INFO = "JEDataCollector";
    public static String KEY = "process-id";
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private final Command commands = new Command();
    private boolean firstRun = true;
    private final ConcurrentHashMap<Long, FutureTask<?>> runnables = new ConcurrentHashMap<>();
    private final SampleHandler sampleHandler = new SampleHandler();

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

        logger.info("Number of Requests: {}", dataSources.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        dataSources.forEach(object -> {
            if (!runningJobs.containsKey(object.getID())) {
                Runnable runnable = () -> {
                    try {
                        Thread.currentThread().setName(object.getID().toString());

                        DataSource dataSource = DataSourceFactory.getDataSource(object);
                        if (dataSource.isReady(object)) {
                            logger.info("DataSource {}:{} is ready.", object.getName(), object.getID());
                            runDataSource(object, dataSource, true);
                        } else {
                            logger.info("DataSource {}:{} is not ready.", object.getName(), object.getID());
                            if (plannedJobs.containsKey(object.getID())) {
                                Boolean manualTrigger = sampleHandler.getLastSample(object, DataCollectorTypes.DataSource.MANUAL_TRIGGER, false);
                                if (manualTrigger) {
                                    logger.info("DataSource {}:{} has active manual trigger.", object.getName(), object.getID());
                                    runDataSource(object, dataSource, false);
                                    try {
                                        JEVisAttribute attribute = object.getAttribute(DataCollectorTypes.DataSource.MANUAL_TRIGGER);
                                        JEVisSample sample = attribute.buildSample(DateTime.now(), false);
                                        sample.commit();
                                    } catch (Exception e) {
                                        logger.error("Could not disable manual trigger for datasource {}:{}", object.getName(), object.getID());
                                        removeJob(object);
                                    }
                                } else {
                                    removeJob(object);
                                }
                            } else {
                                removeJob(object);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Unexpected exception in {}:{}", object.getName(), object.getID(), e);
                        removeJob(object);
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);
                runnables.put(object.getID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing DataSource {}:{}", object.getName(), object.getID());
            }
        });

        logger.info("---------------------finish------------------------");

    }

    private void runDataSource(JEVisObject object, DataSource dataSource, boolean finish) {
        try {
            runningJobs.put(object.getID(), new DateTime());
            LogTaskManager.getInstance().buildNewTask(object.getID(), object.getName());

            logger.info("----------------Execute DataSource " + object.getName() + "-----------------");
            LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.STARTED);

            dataSource.initialize(object);
            LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.RUNNING);
            dataSource.run();
        } catch (Exception e) {
            LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FAILED);
            logger.error("Error in job {}:{}", object.getName(), object.getID(), e);

        } finally {
            LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FINISHED);
            removeJob(object);

            if (finish) {
                dataSource.finishCurrentRun(object);
            }
            logger.info("----------------Finished DataSource " + object.getName() + "-----------------");

            StringBuilder running = new StringBuilder();
            runningJobs.forEach((aLong, dateTime) -> running.append(aLong).append(" - started: ").append(dateTime));
            logger.info("Planned Jobs: {} running Jobs: {}", plannedJobs.size(), running.toString());

            checkLastJob();
        }
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEDataCollector";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
        DriverHelper.loadDriver(ds, commands.driverFolder);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        logger.info("Start Single Mode");

        if (!firstRun) {
            try {
                ds.clearCache();
                ds.preload();
            } catch (JEVisException e) {
                logger.error(e);
            }
        } else firstRun = false;

        for (Long id : ids) {
            try {
                logger.info("Try adding Single Mode for ID " + id);
                JEVisObject dataSourceObject = ds.getObject(id);
                DataSource dataSource = DataSourceFactory.getDataSource(dataSourceObject);
                dataSource.initialize(dataSourceObject);
                dataSource.run();
            } catch (Exception ex) {
                logger.error("Error while loading DataSource", ex);
            }
        }
    }


    @Override
    protected void runServiceHelp() {

        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.isEmpty() && runningJobs.isEmpty()) {
                TaskPrinter.printJobStatus(LogTaskManager.getInstance());
//                if (!firstRun) {
//                    try {
//                        ds.clearCache();
//                        ds.preload();
//                    } catch (JEVisException e) {
//                        logger.error(e);
//                    }
//                } else firstRun = false;

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                    List<JEVisObject> dataSources = getEnabledDataSources(ds);
                    this.executeDataSources(dataSources);
                } else {
                    logger.info("Service is disabled.");
                }
            } else {
                logger.info("Still running queue. Going to sleep again.");
            }
        }

        sleep();
    }

    @Override
    protected void runComplete() {
        logger.info("Start Complete Mode");
        List<JEVisObject> dataSources = new ArrayList<JEVisObject>();
        dataSources = getEnabledDataSources(ds);
        executeDataSources(dataSources);
    }


    private List<JEVisObject> getEnabledDataSources(JEVisDataSource client) {
        List<JEVisObject> enabledDataSources = new ArrayList<JEVisObject>();
        try {
            SampleHandler sampleHandler = new SampleHandler();
            JEVisClass dataSourceClass = client.getJEVisClass(DataCollectorTypes.DataSource.NAME);
            JEVisDataSourceWS dsWS = (JEVisDataSourceWS) ds;
            List<JEVisObject> allDataSources = dsWS.getObjectsWS(dataSourceClass, true);
            for (JEVisObject dataSource : allDataSources) {
                try {
                    ds.reloadAttribute(dataSource);
                    Boolean enabled = sampleHandler.getLastSample(dataSource, DataCollectorTypes.DataSource.ENABLE, false);
                    Boolean manualTrigger = sampleHandler.getLastSample(dataSource, DataCollectorTypes.DataSource.MANUAL_TRIGGER, false);
                    if (enabled && DataSourceFactory.containDataSource(dataSource) || (manualTrigger && DataSourceFactory.containDataSource(dataSource))) {
                        if (!plannedJobs.containsKey(dataSource.getID())) {
                            enabledDataSources.add(dataSource);
                            plannedJobs.put(dataSource.getID(), new DateTime());
                        }
                    }
                } catch (Exception ex) {
                    logger.error("DataSource failed while checking enabled status:", ex);
                }
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        Collections.shuffle(enabledDataSources);

        return enabledDataSources;
    }


}
