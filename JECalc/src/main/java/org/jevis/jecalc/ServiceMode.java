package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.utils.Benchmark;

import java.util.ArrayList;
import java.util.List;

public class ServiceMode {
    private static final Logger logger = LogManager.getLogger(ServiceMode.class);
    private Integer cycleTime = 900000;
    private Benchmark bench;
    private JEVisDataSource ds;

    public ServiceMode(JEVisDataSource ds, Integer cycleTime) {
        this.cycleTime = cycleTime;
        this.ds = ds;
    }

    public ServiceMode(JEVisDataSource ds) {
        this.ds = ds;
    }

    public void run() {
        Thread service = new Thread(() -> runServiceHelp());
        Runtime.getRuntime().addShutdownHook(
                new JECalcShutdownHookThread(service)
        );

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

        this.runProcesses();
        try {
            Thread.sleep(cycleTime);
            runServiceHelp();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runProcesses() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            JEVisClass calcClass = ds.getJEVisClass(CalcJobFactory.Calculation.CLASS.getName());
            jevisObjects = ds.getObjects(calcClass, false);
        } catch (JEVisException ex) {
            logger.error(ex.getMessage());
        }
        List<JEVisObject> jevisCalcObjects = jevisObjects;
        logger.info("{} calc jobs found", jevisCalcObjects.size());

        List<JEVisObject> enabledObjects = new ArrayList<>();
        SampleHandler sampleHandler = new SampleHandler();
        for (JEVisObject curObj : jevisCalcObjects) {
            Boolean valueAsBoolean = sampleHandler.getLastSampleAsBoolean(curObj, CalcJobFactory.Calculation.ENABLED.getName(), false);
            if (valueAsBoolean) {
                enabledObjects.add(curObj);
            }
        }
        List<JEVisObject> filterForEnabledCalcObjects = enabledObjects;
        logger.info("{} enabled calc jobs found", filterForEnabledCalcObjects.size());


        CalcJobFactory calcJobCreator = new CalcJobFactory(filterForEnabledCalcObjects);

        while (calcJobCreator.hasNextJob()) {
            bench = new Benchmark();
            try {
                CalcJob calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds);
                calcJob.execute();
                bench.printBechmark("Calculation (ID: " + calcJob.getCalcObjectID() + ") finished");
            } catch (Exception ex) {
                logger.error("error with calculation job, aborted", ex);
            }
        }

    }


}
