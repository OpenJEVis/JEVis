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
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private boolean firstRun = true;


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
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);
        for (JEVisObject dataSource : dataSources) {
            logger.info("obj: " + dataSource.getName() + ":" + dataSource.getID());
        }

        dataSources.parallelStream().forEach(object -> {
            SampleHandler sampleHandler = new SampleHandler();
            Long maxTime = sampleHandler.getLastSample(object, "Max thread time", 900000L);
            if (!runningJobs.containsKey(object.getID())) {
                try {
                    executor.submit(() -> {
                        Thread.currentThread().setName(object.getID().toString());

                        DataSource dataSource = DataSourceFactory.getDataSource(object);
                        if (dataSource.isReady(object)) {
                            logger.info("DataSource {}:{} is ready.", object.getName(), object.getID());
                            runDataSource(object, dataSource, true);
                        } else {
                            logger.info("DataSource {}:{} is not ready.", object.getName(), object.getID());
                            if (plannedJobs.containsKey(object.getID())) {
                                String value = plannedJobs.get(object.getID());
                                if (value.equals("manual")) {
                                    logger.info("DataSource {}:{} has active manual trigger.", object.getName(), object.getID());
                                    runDataSource(object, dataSource, false);
                                    try {
                                        JEVisAttribute attribute = object.getAttribute(DataCollectorTypes.DataSource.MANUAL_TRIGGER);
                                        JEVisSample sample = attribute.buildSample(DateTime.now(), false);
                                        sample.commit();
                                    } catch (JEVisException e) {
                                        logger.error("Could not disable manual trigger for datasource {}:{}", object.getName(), object.getID());
                                    }
                                } else {
                                    removeWaiting(object);
                                }
                            } else {
                                removeWaiting(object);
                            }
                        }

                    }).get(maxTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.error("Job {}:{} interrupted. ", object.getName(), object.getID());
                } catch (ExecutionException e) {
                    logger.error("Job {}:{} with error. ", object.getName(), object.getID());
                } catch (TimeoutException e) {
                    logger.error("Job {}:{} timed out. ", object.getName(), object.getID());
                }
            } else {
                logger.info("Still processing DataSource {}:{}", object.getName(), object.getID());
            }
        });

        logger.info("---------------------finish------------------------");

    }

    private void removeWaiting(JEVisObject object) {
        logger.error("Still waiting for next DataSource Cycle " + object.getName() + ":" + object.getID());

        plannedJobs.remove(object.getID());
        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

        checkLastJob();
    }

    private void runDataSource(JEVisObject object, DataSource dataSource, boolean finish) {
        runningJobs.put(object.getID(), DateTime.now().toString());
        LogTaskManager.getInstance().buildNewTask(object.getID(), object.getName());

        logger.info("----------------Execute DataSource " + object.getName() + "-----------------");
        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.STARTED);

        dataSource.initialize(object);
        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.RUNNING);
        dataSource.run();

        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FINISHED);
        runningJobs.remove(object.getID());
        plannedJobs.remove(object.getID());

        if (finish) {
            dataSource.finishCurrentRun(object);
        }
        logger.info("----------------Finished DataSource " + object.getName() + "-----------------");

        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

        checkLastJob();
    }

    private void checkLastJob() {
        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
            logger.info("Last job. Clearing cache.");
            setServiceStatus(APP_SERVICE_CLASS_NAME, 1L);
            ds.clearCache();
        }
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
                logger.error(ex);
            }
        }
    }


    @Override
    protected void runServiceHelp() {

        try {
            checkConnection();
        } catch (JEVisException | InterruptedException e) {
            e.printStackTrace();
        }

        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {

            if (!firstRun) {
                try {
                    ds.clearCache();
                    ds.preload();
                    getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
                } catch (JEVisException e) {
                    logger.error(e);
                }
            } else firstRun = false;

            if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                logger.info("Service is enabled.");
                List<JEVisObject> dataSources = getEnabledDataSources(ds);
                executeDataSources(dataSources);
            } else {
                logger.info("Service is disabled.");
            }
        } else {
            logger.info("Cycle not finished.");
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
            List<JEVisObject> allDataSources = client.getObjects(dataSourceClass, true);
            for (JEVisObject dataSource : allDataSources) {
                try {
                    Boolean enabled = sampleHandler.getLastSample(dataSource, DataCollectorTypes.DataSource.ENABLE, false);
                    Boolean manualTrigger = sampleHandler.getLastSample(dataSource, DataCollectorTypes.DataSource.MANUAL_TRIGGER, false);
                    if (enabled && DataSourceFactory.containDataSource(dataSource) || (manualTrigger && DataSourceFactory.containDataSource(dataSource))) {
                        enabledDataSources.add(dataSource);
                        if (!plannedJobs.containsKey(dataSource.getID())) {
                            if (enabled) {
                                if (!manualTrigger) {
                                    plannedJobs.put(dataSource.getID(), "true");
                                } else {
                                    plannedJobs.put(dataSource.getID(), "manual");
                                }
                            } else if (manualTrigger) {
                                plannedJobs.put(dataSource.getID(), "manual");
                            }
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
