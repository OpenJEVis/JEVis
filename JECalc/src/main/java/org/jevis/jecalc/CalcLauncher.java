/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author broder
 */
public class CalcLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(CalcLauncher.class);
    private final Command commands = new Command();
    private static final String APP_INFO = "JECalc";
    private final String APP_SERVICE_CLASS_NAME = "JECalc";

    public CalcLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        logger.info("-------Start JECalc-------");
        CalcLauncher app = new CalcLauncher(args, APP_INFO);
        app.execute();
    }

    @Override
    protected void runServiceHelp() {
        try {
            ds.preload();
            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error(e);
        }

        if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
            logger.info("Service is enabled.");
            List<JEVisObject> dataSources = getEnabledCalcObjects();
            executeCalcJobs(dataSources);
        } else {
            logger.info("Service is disabled.");
        }
        try {
            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            logger.info("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    private void executeCalcJobs(List<JEVisObject> enabledCalcObject) {

        initializeThreadPool(APP_SERVICE_CLASS_NAME);

        logger.info("Number of Calc Jobs: " + enabledCalcObject.size());
        try {
            forkJoinPool.submit(() -> enabledCalcObject.parallelStream().forEach(object -> {
                if (!runningJobs.containsKey(object.getID().toString())) {

                    runningJobs.put(object.getID().toString(), "true");

                    try {
                        LogTaskManager.getInstance().buildNewTask(object.getID(), object.getName());
                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.STARTED);
                        CalcJob calcJob;
                        CalcJobFactory calcJobCreator = new CalcJobFactory();
                        do {
                            calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds, object);
                            calcJob.execute();
                        } while (!calcJob.hasProcessedAllInputSamples());
                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FINISHED);
                    } catch (Exception e) {
                        if (logger.isDebugEnabled() || logger.isTraceEnabled()) {
                            logger.error("[{}] Error in process: \n {} \n ", object.getID(), org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
                        } else {
                            logger.error("[{}] Error in process: \n {} message: {}", object.getID(), LogTaskManager.getInstance().getShortErrorMessage(e), e.getMessage());
                        }
                        LogTaskManager.getInstance().getTask(object.getID()).setExeption(e);
                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FAILED);
                    }
                    runningJobs.remove(object.getID().toString());

                } else {
                    logger.error("Still processing DataSource " + object.getName() + ":" + object.getID());
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Thread Pool was interrupted or execution was stopped: " + e);
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown();
                System.gc();
            }
        }
        logger.info("---------------------finish------------------------");
    }

    private List<JEVisObject> getEnabledCalcObjects() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            JEVisClass calcClass = ds.getJEVisClass(CalcJobFactory.Calculation.CLASS.getName());
            jevisObjects = ds.getObjects(calcClass, false);
        } catch (JEVisException ex) {
            logger.error(ex.getMessage());
        }

        List<JEVisObject> jevisCalcObjects = jevisObjects;
        logger.info("{} calc task found", jevisCalcObjects.size());
        List<JEVisObject> enabledObjects = new ArrayList<>();
        SampleHandler sampleHandler = new SampleHandler();
        for (JEVisObject curObj : jevisCalcObjects) {
            Boolean valueAsBoolean = sampleHandler.getLastSampleAsBoolean(curObj, CalcJobFactory.Calculation.ENABLED.getName(), false);
            if (valueAsBoolean) {
                enabledObjects.add(curObj);
            }
        }
        return enabledObjects;
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
    }

    @Override
    protected void runSingle(Long id) {
        logger.info("Start Single Mode");
        try {
            JEVisObject calcObject = ds.getObject(id);
            List<JEVisObject> jeVisObjectList = new ArrayList<>();
            jeVisObjectList.add(calcObject);

            executeCalcJobs(jeVisObjectList);
        } catch (Exception ex) {
            logger.error("JECalc: Single mode failed", ex);
        }
    }

    @Override
    protected void runComplete() {
        logger.info("Start Complete Mode");
        List<JEVisObject> filterForEnabledCalcObjects = getEnabledCalcObjects();
        logger.info("{} enabled calc task found", filterForEnabledCalcObjects.size());

        executeCalcJobs(filterForEnabledCalcObjects);
    }

}
