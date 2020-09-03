/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.jedataprocessor.workflow.ProcessManager;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * @author broder
 */
public class Launcher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private static final String APP_INFO = "JEDataProcessor";
    public static String KEY = "process-id";
    private final Command commands = new Command();
    private int processingSize = 10000;
    private boolean firstRun = true;

    private Launcher(String[] args, String appname) {
        super(args, appname);
    }

    public static void main(String[] args) {

        logger.info("-------Start JEDataProcessor-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    private void executeProcesses(List<JEVisObject> processes) {

        logger.info("{} cleaning task found starting", processes.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        processes.forEach(currentCleanDataObject -> {
            if (!runningJobs.containsKey(currentCleanDataObject.getID())) {
                Runnable runnable = () -> {
                    try {
                        Thread.currentThread().setName(currentCleanDataObject.getName() + ":" + currentCleanDataObject.getID().toString());
                        runningJobs.put(currentCleanDataObject.getID(), new DateTime());

                        ProcessManager currentProcess = null;
                        try {
                            LogTaskManager.getInstance().buildNewTask(currentCleanDataObject.getID(), currentCleanDataObject.getName());
                            LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.STARTED);

                            currentProcess = new ProcessManager(currentCleanDataObject, new ObjectHandler(ds), processingSize);
                            currentProcess.start();
                        } catch (Exception ex) {
                            logger.debug(ex);
                            LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FAILED);
                            removeJob(currentCleanDataObject);
                        }
                    } catch (Exception e) {
                        LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FAILED);
                        removeJob(currentCleanDataObject);

                        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                        checkLastJob();
                    } finally {
                        LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FINISHED);
                        removeJob(currentCleanDataObject);

                        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                        checkLastJob();
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);

                runnables.put(currentCleanDataObject.getID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing Job {}:{}", currentCleanDataObject.getName(), currentCleanDataObject.getID());
            }
        });
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEDataProcessor";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        List<JEVisObject> processes = new ArrayList<>();

        if (checkConnection()) {

            for (Long l : ids) {
                try {
                    JEVisObject object = ds.getObject(l);
                    if (!plannedJobs.containsKey(object.getID())) {
                        plannedJobs.put(object.getID(), new DateTime());
                        processes.add(object);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            this.processingSize = getProcessingSizeFromService(APP_SERVICE_CLASS_NAME);

            this.executeProcesses(processes);

        }

        sleep();
    }

    @Override
    protected void runServiceHelp() {
        List<JEVisObject> enabledCleanDataObjects = new ArrayList<>();

        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                if (!firstRun) {
                    try {
                        ds.clearCache();
                        ds.preload();
                    } catch (JEVisException e) {
                        logger.error("Could not preload.");
                    }
                } else firstRun = false;

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
                this.processingSize = getProcessingSizeFromService(APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                    try {
                        enabledCleanDataObjects = getAllCleaningObjects();
                    } catch (Exception e) {
                        logger.error("Could not get cleaning objects. " + e);
                    }

                    this.executeProcesses(enabledCleanDataObjects);
                } else {
                    logger.info("Service is disabled.");
                }
            } else {
                logger.info("Still running queue. Going to sleep again.");
            }
        }

        sleep();
    }

    private int getProcessingSizeFromService(String serviceClassName) {
        int size = processingSize;
        try {
            JEVisClass serviceClass = ds.getJEVisClass(serviceClassName);
            List<JEVisObject> listServices = ds.getObjects(serviceClass, false);
            JEVisAttribute sizeAtt = listServices.get(0).getAttribute("Processing Size");
            if (sizeAtt != null && sizeAtt.hasSample()) {
                size = sizeAtt.getLatestSample().getValueAsLong().intValue();
            }

        } catch (Exception e) {
            logger.error("Couldn't get processsing size from the JEVis System. Using standard Size of {}", processingSize, e);
        }
        processingSize = size;
        return size;
    }

    @Override
    protected void runComplete() {

        List<JEVisObject> enabledCleanDataObjects = new ArrayList<>();
        try {
            enabledCleanDataObjects = getAllCleaningObjects();
        } catch (Exception e) {
            logger.error("Could not get enabled clean data objects. " + e);
        }
        for (JEVisObject jeVisObject : enabledCleanDataObjects) {
            if (jeVisObject.getID() > 19700)
                try {
                    ProcessManager currentProcess = new ProcessManager(jeVisObject, new ObjectHandler(ds), getProcessingSizeFromService(APP_SERVICE_CLASS_NAME));
                    currentProcess.start();
                } catch (Exception e) {
                    logger.error("Error in process of object {}", jeVisObject.getID(), e);
                }
        }

        runComplete();
    }

    private List<JEVisObject> getAllCleaningObjects() throws Exception {
        JEVisClass cleanDataClass;
        JEVisClass forecastDataClass;
        List<JEVisObject> cleanDataObjects;
        List<JEVisObject> forecastDataObjects;
        List<JEVisObject> filteredObjects = new ArrayList<>();

        try {
            cleanDataClass = ds.getJEVisClass(CleanDataObject.CLASS_NAME);
            cleanDataObjects = ds.getObjects(cleanDataClass, false);
            logger.info("Total amount of Clean Data Objects: " + cleanDataObjects.size());
            forecastDataClass = ds.getJEVisClass(ForecastDataObject.CLASS_NAME);
            forecastDataObjects = ds.getObjects(forecastDataClass, false);
            logger.info("Total amount of Forecast Data Objects: " + forecastDataObjects.size());

            cleanDataObjects.forEach(jeVisObject -> {
                if (isEnabled(jeVisObject)) {
                    filteredObjects.add(jeVisObject);
                    if (!plannedJobs.containsKey(jeVisObject.getID())) {
                        plannedJobs.put(jeVisObject.getID(), new DateTime());
                    }
                }
            });
            forecastDataObjects.forEach(object -> {
                if (isEnabled(object)) {
                    filteredObjects.add(object);
                    if (!plannedJobs.containsKey(object.getID())) {
                        plannedJobs.put(object.getID(), new DateTime());
                    }
                }
            });

            logger.info("Amount of enabled Clean Data Objects: " + cleanDataObjects.size());
        } catch (JEVisException ex) {
            throw new Exception("Process classes missing", ex);
        }
        logger.info("{} cleaning objects found", cleanDataObjects.size());
        return filteredObjects;
    }
}