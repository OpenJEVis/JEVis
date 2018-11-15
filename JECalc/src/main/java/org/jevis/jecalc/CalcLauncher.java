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
import org.jevis.commons.utils.Benchmark;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class CalcLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(CalcLauncher.class);
    private final Command commands = new Command();
    private static final String APP_INFO = "JECalculation ver. 2018-07-11 - JEVis - Energy Monitring Software";
    private Benchmark bench;
    private int cycleTime = 900000;

    public CalcLauncher(String[] args) {
        super(args, APP_INFO);
    }

    public static void main(String[] args) {
        logger.info(APP_INFO);
        CalcLauncher app = new CalcLauncher(args);
        app.execute();
    }

    @Override
    protected void runService(Integer cycle_time) {
        logger.info("JECalc: service mode started");

        if (cycle_time != null) {
            ServiceMode sm = new ServiceMode(ds, cycle_time);
            sm.run();
        } else {
            ServiceMode sm = new ServiceMode(ds);

            sm.run();
        }

    }

    private void run() {

        getEnabledCalcObjects().forEach(object -> {
            bench = new Benchmark();
            try {
                CalcJob calcJob;
                CalcJobFactory calcJobCreator = new CalcJobFactory();
                do {
                    ds.reloadAttributes();
                    calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds, object);
                    calcJob.execute();
                } while (!calcJob.hasProcessedAllInputSamples());
                bench.printBechmark("Calculation (ID: " + calcJob.getCalcObjectID() + ") finished");
            } catch (Exception ex) {
                logger.error("error with calculation job, aborted", ex);
            }
        });
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

            CalcJobFactory calcJobCreator = new CalcJobFactory();
            bench = new Benchmark();
            try {
                CalcJob calcJob;
                do {
                    ds.reloadAttributes();
                    calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds, calcObject);
                    calcJob.execute();
                } while (!calcJob.hasProcessedAllInputSamples());
                bench.printBechmark("Calculation (ID: " + calcJob.getCalcObjectID() + ") finished");
            } catch (Exception ex) {
                logger.error("error with calculation job, aborted", ex);
            }
        } catch (Exception ex) {
            logger.error("JECalc: Single mode failed", ex);
        }
    }

    protected class Command {


    }

    @Override
    protected void runComplete() {
        logger.info("Start Complete Mode");
        List<JEVisObject> jevisCalcObjects = getCalcObjects();
        logger.info("{} calc task found", jevisCalcObjects.size());
        List<JEVisObject> filterForEnabledCalcObjects = getEnabledCalcJobs(jevisCalcObjects);
        logger.info("{} enabled calc task found", filterForEnabledCalcObjects.size());

        filterForEnabledCalcObjects.forEach(object -> {
            run();
        });
    }

    private List<JEVisObject> getCalcObjects() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            JEVisClass calcClass = ds.getJEVisClass(CalcJobFactory.Calculation.CLASS.getName());
            jevisObjects = ds.getObjects(calcClass, false);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return jevisObjects;
    }

    private List<JEVisObject> getEnabledCalcJobs(List<JEVisObject> jevisCalcObjects) {
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
}
