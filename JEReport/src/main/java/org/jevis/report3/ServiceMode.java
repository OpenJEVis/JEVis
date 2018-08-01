package org.jevis.report3;

import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.policy.ReportPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ServiceMode {
    private static final Logger logger = LoggerFactory.getLogger(ServiceMode.class);
    private Long cycleTime = 900000L;
    private JEVisDataSource _ds;

    public ServiceMode(JEVisDataSource ds, Long cycleTime) {
        this.cycleTime = cycleTime;
        this._ds = ds;
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
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runServiceHelp() throws JEVisException {

        this.runProcesses();
        try {
            Thread.sleep(cycleTime);
            runServiceHelp();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runProcesses() throws JEVisException {
        List<JEVisObject> reportObjects = new ArrayList<>();
        JEVisClass reportClass = _ds.getJEVisClass(ReportAttributes.NAME);
        reportObjects = _ds.getObjects(reportClass, true);

        //execute the report objects
        logger.info("nr of reports {}", reportObjects.size());
        for (JEVisObject reportObject : reportObjects) {
            try {
                logger.info("---------------------------------------------------------------------");
                logger.info("current report object: " + reportObject.getName() + " with id: " + reportObject.getID());
                //check if the report is enabled
                ReportPolicy reportPolicy = new ReportPolicy(); //Todo inject in constructor
                Boolean reportEnabled = reportPolicy.isReportEnabled(reportObject);
                if (!reportEnabled) {
                    logger.info("Report is not enabled");
                    continue;
                }

                ReportExecutor executor = ReportExecutorFactory.getReportExecutor(reportObject);
                executor.executeReport();
            } catch (Exception e) {
                logger.error("Error while creating report", e);
                e.printStackTrace();
            }
        }
    }
}
