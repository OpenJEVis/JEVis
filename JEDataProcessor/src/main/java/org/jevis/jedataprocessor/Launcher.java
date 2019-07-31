/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jedataprocessor.workflow.ProcessManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class Launcher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private static final String APP_INFO = "JEDataProcessor";
    public static String KEY = "process-id";
    private final String APP_SERVICE_CLASS_NAME = "JEDataProcessor";
    private final Command commands = new Command();

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

        processes.parallelStream().forEach(currentCleanDataObject -> {
            if (!runningJobs.containsKey(currentCleanDataObject.getID())) {
                executor.submit(() -> {
                            Thread.currentThread().setName(currentCleanDataObject.getName() + ":" + currentCleanDataObject.getID().toString());
                            runningJobs.put(currentCleanDataObject.getID(), "true");

                            ProcessManager currentProcess = null;
                            try {
                                LogTaskManager.getInstance().buildNewTask(currentCleanDataObject.getID(), currentCleanDataObject.getName());
                                LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.STARTED);

                                currentProcess = new ProcessManager(currentCleanDataObject, new ObjectHandler(ds));
                                currentProcess.start();
                            } catch (Exception ex) {
                                logger.debug(ex);
                                LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FAILED);
                            }

                            LogTaskManager.getInstance().getTask(currentCleanDataObject.getID()).setStatus(Task.Status.FINISHED);
                            runningJobs.remove(currentCleanDataObject.getID());
                            plannedJobs.remove(currentCleanDataObject.getID());
                            currentProcess = null;

                            logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                                logger.info("Last job. Clearing cache.");
                                setServiceStatus(APP_SERVICE_CLASS_NAME, 1L);
                                ds.clearCache();
                            }

                        }
                );
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
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(Long id) {

        try {
            JEVisObject object = ds.getObject(id);
            ProcessManager currentProcess = new ProcessManager(object, new ObjectHandler(ds));
            currentProcess.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void runServiceHelp() {
        List<JEVisObject> enabledCleanDataObjects = new ArrayList<>();

        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
            try {
                ds.clearCache();
                ds.preload();
            } catch (JEVisException e) {
                logger.error("Could not preload.");
            }

            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

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

        try {
            logger.info("Entering Sleep mode for " + cycleTime + "ms.");
            Thread.sleep(cycleTime);

            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            runServiceHelp();
        } catch (
                InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }

    }

    @Override
    protected void runComplete() {
        List<JEVisObject> enabledCleanDataObjects = new ArrayList<>();
        try {
            enabledCleanDataObjects = getAllCleaningObjects();
        } catch (Exception e) {
            logger.error("Could not get enabled clean data objects. " + e);
        }
        enabledCleanDataObjects.forEach(jeVisObject -> {
            try {
                ProcessManager currentProcess = new ProcessManager(jeVisObject, new ObjectHandler(ds));
                currentProcess.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<JEVisObject> getAllCleaningObjects() throws Exception {
        JEVisClass cleanDataClass;
        List<JEVisObject> cleanDataObjects;
        List<JEVisObject> filteredObjects = new ArrayList<>();

        try {
            cleanDataClass = ds.getJEVisClass(CleanDataObject.CLASS_NAME);
            cleanDataObjects = ds.getObjects(cleanDataClass, false);
            logger.info("Total amount of Clean Data Objects: " + cleanDataObjects.size());
            cleanDataObjects.forEach(jeVisObject -> {
                if (isEnabled(jeVisObject)) {
                    filteredObjects.add(jeVisObject);
                    if (!plannedJobs.containsKey(jeVisObject.getID())) {
                        plannedJobs.put(jeVisObject.getID(), "true");
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

    private boolean isEnabled(JEVisObject cleanObject) {
        ObjectHandler objectHandler = new ObjectHandler(ds);
        CleanDataObject cleanDataObject = new CleanDataObject(cleanObject, objectHandler);
        return cleanDataObject.getEnabled();
    }
}