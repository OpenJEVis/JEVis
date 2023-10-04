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
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * @author broder
 */
public class CalcLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(CalcLauncher.class);
    private final Command commands = new Command();
    private static final String APP_INFO = "JECalc";

    private final boolean firstRun = true;

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

        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
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
                    logger.info("Service is enabled.");
                    List<JEVisObject> dataSources = getEnabledCalcObjects();
                    this.executeCalcJobs(dataSources);
                } else {
                    logger.info("Service is disabled.");
                }
            } else {
                logger.info("Still running queue. Going to sleep again.");
            }
        }

        sleep();
    }


    private void executeCalcJobs(List<JEVisObject> enabledCalcObject) {

        logger.info("Number of Calc Jobs: {}", enabledCalcObject.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        enabledCalcObject.forEach(object -> {
            if (!runningJobs.containsKey(object.getID())) {
                Runnable runnable = () -> {
                    try {
                        Thread.currentThread().setName(object.getName() + ":" + object.getID().toString());
                        runningJobs.put(object.getID(), new DateTime());

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
                        LogTaskManager.getInstance().getTask(object.getID()).setStatus(Task.Status.FAILED);

                        logger.error("Failed Job: {}:{}", object.getName(), object.getID(), e);

                    } finally {
                        removeJob(object);

                        StringBuilder running = new StringBuilder();
                        runningJobs.forEach((aLong, dateTime) -> running.append(aLong).append(" - started: ").append(dateTime).append(" "));

                        logger.info("Queued Jobs: {} running Jobs: {}", plannedJobs.size(), running.toString());

                        checkLastJob();
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);

                runnables.put(object.getID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing Job {}:{}", object.getName(), object.getID());
            }
        });
    }

    private List<JEVisObject> getEnabledCalcObjects() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            JEVisClass calcClass = ds.getJEVisClass(CalcJobFactory.Calculation.CLASS.getName());
            JEVisDataSourceWS dsWS = (JEVisDataSourceWS) ds;
            jevisObjects = dsWS.getObjectsWS(calcClass, false);
        } catch (JEVisException ex) {
            logger.error(ex.getMessage());
        }

        List<JEVisObject> jevisCalcObjects = jevisObjects;
        logger.info("{} calc task found", jevisCalcObjects.size());
        List<JEVisObject> enabledObjects = new ArrayList<>();
        SampleHandler sampleHandler = new SampleHandler();
        for (JEVisObject curObj : jevisCalcObjects) {
            ds.reloadAttribute(curObj);
            Boolean valueAsBoolean = sampleHandler.getLastSample(curObj, CalcJobFactory.Calculation.ENABLED.getName(), false);
            if (valueAsBoolean) {
                enabledObjects.add(curObj);
                if (!plannedJobs.containsKey(curObj.getID())) {
                    plannedJobs.put(curObj.getID(), new DateTime());
                }
            }
        }

        Collections.shuffle(enabledObjects);

        return enabledObjects;
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JECalc";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        logger.info("Start Single Mode");
        for (Long id : ids) {
            try {
                JEVisObject calcObject = ds.getObject(id);

                try {
                    CalcJob calcJob;
                    CalcJobFactory calcJobCreator = new CalcJobFactory();
                    do {
                        calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds, calcObject);
                        calcJob.execute();
                    } while (!calcJob.hasProcessedAllInputSamples());

                } catch (Exception e) {
                    logger.error(e);
                }
            } catch (Exception ex) {
                logger.error("JECalc: Single mode failed", ex);
            }
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