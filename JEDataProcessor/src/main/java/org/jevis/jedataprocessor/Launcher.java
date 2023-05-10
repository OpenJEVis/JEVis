/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessManager;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
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
    private int processingSize = 50000;
    private final boolean firstRun = true;

    private Launcher(String[] args, String appname) {
        super(args, appname);
    }

    public static void main(String[] args) {

        logger.info("-------Start JEDataProcessor-------");
        System.setProperty("user.timezone", "UTC");
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

                            ds.reloadAttribute(currentCleanDataObject);
                            currentProcess = new ProcessManager(currentCleanDataObject, new ObjectHandler(ds), processingSize);
                            currentProcess.start();
                        } catch (Exception ex) {
                            logger.debug("Error in job {}:{}", currentCleanDataObject.getName(), currentCleanDataObject.getID(), ex);
                            if (currentProcess != null) currentProcess.setFinished(true);
                            LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FAILED);
                            removeJob(currentCleanDataObject);
                        }

                        LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FINISHED);
                    } catch (Exception e) {
                        LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FAILED);

                        logger.error("Failed Job: {}:{}", currentCleanDataObject.getName(), currentCleanDataObject.getID(), e);

                    } finally {

                        removeJob(currentCleanDataObject);

                        logger.info("Planned Jobs: {} running Jobs: {} runnables: {}", plannedJobs.size(), runningJobs.size(), runnables.size());

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
        setMaxThreadTime(1800000L);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        if (checkConnection()) {
            for (Long l : ids) {
                try {
                    JEVisObject object = ds.getObject(l);
                    ProcessManager currentProcess = new ProcessManager(object, new ObjectHandler(ds), CommonMethods.getProcessingSizeFromService(ds, APP_SERVICE_CLASS_NAME));
                    currentProcess.start();
                } catch (Exception e) {
                    logger.error("Error in process of object {}", l, e);
                }
            }
            runSingle(ids);
        }
    }

    @Override
    protected void runServiceHelp() {

        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                TaskPrinter.printJobStatus(LogTaskManager.getInstance());
//                if (!firstRun) {
//                    try {
//                        ds.clearCache();
//                        ds.preload();
//                    } catch (JEVisException e) {
//                        logger.error("Could not preload.");
//                    }
//                } else firstRun = false;

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
                this.processingSize = CommonMethods.getProcessingSizeFromService(ds, APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                    try {
                        List<JEVisObject> enabledCleanDataObjects = getAllCleaningObjects();

                        executeProcesses(enabledCleanDataObjects);
                    } catch (Exception e) {
                        logger.error("Could not get cleaning objects. ", e);
                    }
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
        if (checkConnection()) {
            List<JEVisObject> enabledCleanDataObjects = new ArrayList<>();
            try {
                enabledCleanDataObjects = getAllCleaningObjects();
            } catch (Exception e) {
                logger.error("Could not get enabled clean data objects. ", e);
            }
            for (JEVisObject jeVisObject : enabledCleanDataObjects) {
                try {
                    ProcessManager currentProcess = new ProcessManager(jeVisObject, new ObjectHandler(ds), CommonMethods.getProcessingSizeFromService(ds, APP_SERVICE_CLASS_NAME));
                    currentProcess.start();
                } catch (Exception e) {
                    logger.error("Error in process of object {}", jeVisObject.getID(), e);
                }
            }

            runComplete();
        }
    }

    private List<JEVisObject> getAllCleaningObjects() throws Exception {
        JEVisClass cleanDataClass;
        JEVisClass forecastDataClass;
        JEVisClass mathDataClass;
        List<JEVisObject> cleanDataObjects;
        List<JEVisObject> forecastDataObjects;
        List<JEVisObject> mathDataObjects;
        List<JEVisObject> filteredObjects = new ArrayList<>();

        try {
            ((JEVisDataSourceWS) ds).getObjectsWS(false);
            cleanDataClass = ds.getJEVisClass(CleanDataObject.CLASS_NAME);
            cleanDataObjects = ds.getObjects(cleanDataClass, false);
            logger.info("Total amount of Clean Data Objects: {}", cleanDataObjects.size());
            forecastDataClass = ds.getJEVisClass(ForecastDataObject.CLASS_NAME);
            forecastDataObjects = ds.getObjects(forecastDataClass, false);
            logger.info("Total amount of Forecast Data Objects: {}", forecastDataObjects.size());
            mathDataClass = ds.getJEVisClass(MathDataObject.CLASS_NAME);
            mathDataObjects = ds.getObjects(mathDataClass, false);
            logger.info("Total amount of Math Data Objects: {}", forecastDataObjects.size());

            for (JEVisObject jeVisObject : cleanDataObjects) {
                if (isEnabled(jeVisObject)) {
                    if (!plannedJobs.containsKey(jeVisObject.getID())) {
                        filteredObjects.add(jeVisObject);
                        plannedJobs.put(jeVisObject.getID(), new DateTime());
                    }
                }
            }
            for (JEVisObject forecastDataObject : forecastDataObjects) {
                if (isEnabled(forecastDataObject)) {
                    if (!plannedJobs.containsKey(forecastDataObject.getID())) {
                        filteredObjects.add(forecastDataObject);
                        plannedJobs.put(forecastDataObject.getID(), new DateTime());
                    }
                }
            }
            for (JEVisObject object : mathDataObjects) {
                if (isEnabled(object)) {
                    if (!plannedJobs.containsKey(object.getID())) {
                        filteredObjects.add(object);
                        plannedJobs.put(object.getID(), new DateTime());
                    }
                }
            }

            logger.info("Amount of enabled Clean Data Objects: {}, enabled Forecast Data Objects: {}, enabled Math Data Objects: {}", cleanDataObjects.size(), forecastDataObjects.size(), mathDataObjects.size());
        } catch (Exception ex) {
            throw new Exception("Process classes missing", ex);
        }
        logger.info("{} objects found for cleaning", filteredObjects.size());

        Collections.shuffle(filteredObjects);

        return filteredObjects;
    }
}