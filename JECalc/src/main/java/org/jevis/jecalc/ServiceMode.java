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
import java.util.concurrent.ForkJoinPool;

public class ServiceMode {
    private static final Logger logger = LogManager.getLogger(ServiceMode.class);
    private Integer cycleTime = 900000;
    private Benchmark bench;
    private JEVisDataSource ds;
    private ForkJoinPool forkJoinPool;

    public ServiceMode(JEVisDataSource ds, Integer cycleTime) {
        this.cycleTime = cycleTime;
        this.ds = ds;

        getCycleTimeFromService();
        initializeThreadPool();
    }

    public ServiceMode(JEVisDataSource ds) {
        this.ds = ds;

        initializeThreadPool();
    }

    private void initializeThreadPool() {
        Integer threadCount = 4;
        try {
            JEVisClass calcClass = ds.getJEVisClass("JECalc");
            List<JEVisObject> listCalcObjects = ds.getObjects(calcClass, false);
            threadCount = listCalcObjects.get(0).getAttribute("Max Number Threads").getLatestSample().getValueAsLong().intValue();
            logger.info("Set Thread count to: " + threadCount);
        } catch (Exception e) {

        }
        forkJoinPool = new ForkJoinPool(threadCount);
    }

    private void getCycleTimeFromService() {
        try {
            JEVisClass calcClass = ds.getJEVisClass("JECalc");
            List<JEVisObject> listCalcObjects = ds.getObjects(calcClass, false);
            cycleTime = listCalcObjects.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong().intValue();
            logger.info("Service cycle time from service: " + cycleTime);
        } catch (Exception e) {

        }
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

        if (checkServiceStatus()) {
            logger.info("Service is enabled.");
            this.runProcesses();
        } else {
            logger.info("Service is disabled.");
        }
        try {
            Thread.sleep(cycleTime);
            try {
                ds.reloadAttributes();
            } catch (JEVisException e) {
            }
            getCycleTimeFromService();

            runServiceHelp();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkServiceStatus() {
        Boolean enabled = true;
        try {
            JEVisClass calcClass = ds.getJEVisClass("JECalc");
            List<JEVisObject> listCalcObjects = ds.getObjects(calcClass, false);
            enabled = listCalcObjects.get(0).getAttribute("Enable").getLatestSample().getValueAsBoolean();
            logger.info("Service is enabled is " + enabled);
        } catch (Exception e) {

        }
        return enabled;
    }

    private void runProcesses() {
        List<JEVisObject> jevisObjects = new ArrayList<>();
        try {
            ds.reloadAttributes();
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
        List<JEVisObject> filterForEnabledCalcObjects = enabledObjects;
        logger.info("{} enabled calc task found", filterForEnabledCalcObjects.size());


        CalcJobFactory calcJobCreator = new CalcJobFactory();

        forkJoinPool.submit(
                () -> enabledObjects.parallelStream().forEach(object -> {
                    bench = new Benchmark();
                    try {
                        CalcJob calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds, object);
                        calcJob.execute();
                        bench.printBechmark("Calculation (ID: " + calcJob.getCalcObjectID() + ") finished");
                    } catch (Exception ex) {
                        logger.error("error with calculation job, aborted", ex);
                    }
                }));

    }


}
