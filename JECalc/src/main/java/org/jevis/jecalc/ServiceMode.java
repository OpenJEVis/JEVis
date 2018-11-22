package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ServiceMode {
    private static final Logger logger = LogManager.getLogger(ServiceMode.class);
    private Integer cycleTime = 900000;

    private JEVisDataSource ds;
    private ForkJoinPool forkJoinPool;
    private ConcurrentHashMap<String, String> runningJobs = new ConcurrentHashMap();

    public ServiceMode(JEVisDataSource ds, Integer cycleTime) {
        this.cycleTime = cycleTime;
        this.ds = ds;

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

    private void getCycleTimeFromService() throws JEVisException {
        JEVisClass calcClass = ds.getJEVisClass("JECalc");
        List<JEVisObject> listCalcObjects = ds.getObjects(calcClass, false);
        cycleTime = listCalcObjects.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong().intValue();
        logger.info("Service cycle time from service: " + cycleTime);
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

        try {
            ds.reloadAttributes();
            getCycleTimeFromService();
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        if (checkServiceStatus()) {
            logger.info("Service is enabled.");
            this.runProcesses();
        } else {
            logger.info("Service is disabled.");
        }
        try {
            Thread.sleep(cycleTime);

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
        } catch (Exception e) {

        }
        return enabled;
    }

    private void runProcesses() {
        if (runningJobs.isEmpty()) {
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
            List<JEVisObject> filterForEnabledCalcObjects = enabledObjects;
            logger.info("{} enabled calc task found", filterForEnabledCalcObjects.size());


            forkJoinPool.submit(
                    () -> enabledObjects.parallelStream().forEach(object -> {
                        if (!runningJobs.containsKey(object.getName() + ":" + object.getID())) {

                            runningJobs.put(object.getName() + ":" + object.getID(), "true");

                            try {
                                CalcJob calcJob;
                                CalcJobFactory calcJobCreator = new CalcJobFactory();
                                do {
                                    ds.reloadAttributes();
                                    calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), ds, object);
                                    calcJob.execute();
                                    Thread.sleep(500);
                                } while (!calcJob.hasProcessedAllInputSamples());

                            } catch (Exception ex) {
                                logger.error("error with calculation job, aborted", ex);
                            }
                            runningJobs.remove(object.getName());
                        } else {
                            logger.error("Still calculating Job " + object.getName() + ":" + object.getID());
                        }
                    }));
        }
    }
}
