package org.jevis.report3;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.policy.ReportPolicy;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ServiceMode {
    private static final Logger logger = LogManager.getLogger(ServiceMode.class);
    private Long cycleTime = 900000L;
    private JEVisDataSource _ds;
    private ForkJoinPool forkJoinPool;
    private ConcurrentHashMap<String, String> runningJobs = new ConcurrentHashMap();

    public ServiceMode(JEVisDataSource ds, ForkJoinPool forkJoinPool, Long cycleTime) {
        this.cycleTime = cycleTime;
        this._ds = ds;

        this.forkJoinPool = forkJoinPool;
    }

    public ServiceMode() {
    }

    public void run() {
        Thread service = new Thread(() -> {
            try {
                runServiceHelp();
            } catch (JEVisException e) {
                logger.error("Failed to Start Report Thread");
            }
        });
        Runtime.getRuntime().addShutdownHook(
                new JEReportShutdownHookThread(service)
        );

        try {

            service.start();
        } catch (Exception e) {
            logger.fatal("Error in Thread: " + e);
            throw new RuntimeException(e);
        }
        try {
            logger.info("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.fatal("Service was stopped.");
            throw new RuntimeException(e);
        }
    }

    private void runServiceHelp() throws JEVisException {

        try {
            _ds.reloadAttributes();
            getCycleTimeFromService();
        } catch (JEVisException e) {
        }

        if (checkServiceStatus()) {
            this.runProcesses();

            logger.info("Queued all report objects, entering sleep mode for " + cycleTime + " ms.");

        } else {
            logger.info("Service was disabled.");
        }

        try {
            Thread.sleep(cycleTime);

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.fatal("Thread was interrupted: " + e);
        }
    }

    private Boolean checkServiceStatus() {
        Boolean enabled = true;
        try {
            JEVisClass reportClass = _ds.getJEVisClass("JEReport");
            List<JEVisObject> listReportObjects = _ds.getObjects(reportClass, false);
            enabled = listReportObjects.get(0).getAttribute("Enable").getLatestSample().getValueAsBoolean();
            logger.info("Service is enabled is " + enabled);
        } catch (Exception e) {

        }
        return enabled;
    }

    private void getCycleTimeFromService() throws JEVisException {
        JEVisClass dataProcessorClass = _ds.getJEVisClass("JEReport");
        List<JEVisObject> listDataProcessorObjects = _ds.getObjects(dataProcessorClass, false);
        cycleTime = listDataProcessorObjects.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong();
        logger.info("Service cycle time from service: " + cycleTime);
    }

    private void runProcesses() throws JEVisException {


        JEVisClass reportClass = _ds.getJEVisClass(ReportAttributes.NAME);
        final List<JEVisObject> reportObjects = _ds.getObjects(reportClass, true);

        //execute the report objects
        logger.info("Number of reports " + reportObjects.size());
        forkJoinPool.submit(
                () -> reportObjects.parallelStream().forEach(reportObject -> {
                    if (!runningJobs.containsKey(reportObject.getID().toString())) {

                        runningJobs.put(reportObject.getID().toString(), "true");
                        try {
                            logger.info("---------------------------------------------------------------------");
                            logger.info("current report object: " + reportObject.getName() + " with id: " + reportObject.getID());
                            //check if the report is enabled
                            ReportPolicy reportPolicy = new ReportPolicy(); //Todo inject in constructor
                            Boolean reportEnabled = reportPolicy.isReportEnabled(reportObject);
                            if (!reportEnabled) {
                                logger.info("Report is not enabled");
                            } else {

                                ReportExecutor executor = ReportExecutorFactory.getReportExecutor(reportObject);
                                executor.executeReport();

                                logger.info("---------------------------------------------------------------------");
                                logger.info("finished report object: " + reportObject.getName() + " with id: " + reportObject.getID());
                            }
                        } catch (Exception e) {
                            logger.error("Error while creating report", e);
                        }
                        runningJobs.remove(reportObject.getID().toString());

                    } else {
                        logger.error("Still processing Job " + reportObject.getName() + ":" + reportObject.getID());
                    }
                }));
    }
}
