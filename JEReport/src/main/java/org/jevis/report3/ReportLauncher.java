/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.policy.ReportPolicy;

import java.util.ArrayList;
import java.util.List;


/**
 * @author broder
 */
public class ReportLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(ReportLauncher.class);
    private static Injector injector;
    private static final String APP_INFO = "JEReport";
    private final String APP_SERVICE_CLASS_NAME = "JEReport";
    private final Command commands = new Command();

    public ReportLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEReport-------");
        ReportLauncher app = new ReportLauncher(args, APP_INFO);
        app.execute();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void setInjector(Injector inj) {
        injector = inj;
    }

    private void executeReports(List<JEVisObject> reportObjects) {

        initializeThreadPool(APP_SERVICE_CLASS_NAME);

        logger.info("Number of Reports: " + reportObjects.size());

        try {
            reportObjects.parallelStream().forEach(reportObject -> {
                forkJoinPool.submit(() -> {
                    if (!runningJobs.containsKey(reportObject.getID().toString())) {

                        runningJobs.put(reportObject.getID().toString(), "true");

                        LogTaskManager.getInstance().buildNewTask(reportObject.getID(), reportObject.getName());
                        LogTaskManager.getInstance().getTask(reportObject.getID()).setStatus(Task.Status.STARTED);
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
                            if (logger.isDebugEnabled() || logger.isTraceEnabled()) {
                                logger.error("[{}] Error in process: \n {} \n ", reportObject.getID(), org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
                            } else {
                                logger.error("[{}] Error in process: \n {} message: {}", reportObject.getID(), LogTaskManager.getInstance().getShortErrorMessage(e), e.getMessage());
                            }
                            LogTaskManager.getInstance().getTask(reportObject.getID()).setException(e);
                            LogTaskManager.getInstance().getTask(reportObject.getID()).setStatus(Task.Status.FAILED);
                        }
                        LogTaskManager.getInstance().getTask(reportObject.getID()).setStatus(Task.Status.FINISHED);
                        runningJobs.remove(reportObject.getID().toString());

                    } else {
                        logger.error("Still processing Report " + reportObject.getName() + ":" + reportObject.getID());
                    }
                });
            });
        } finally {
            if (forkJoinPool != null) {
                forkJoinPool.shutdown();
                System.gc();
            }
        }
        logger.info("---------------------finish------------------------");
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
            logger.info("Try adding Single Mode for ID " + id);
            JEVisObject reportObject = ds.getObject(id);
            List<JEVisObject> jeVisObjectList = new ArrayList<>();
            jeVisObjectList.add(reportObject);

            executeReports(jeVisObjectList);

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    protected void runServiceHelp() {
        try {
            ds.clearCache();
            ds.preload();
            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
        } catch (JEVisException e) {
        }

        if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
            List<JEVisObject> reports = getEnabledReports();
            executeReports(reports);

            logger.info("Queued all report objects, entering sleep mode for " + cycleTime + " ms.");

        } else {
            logger.info("Service was disabled.");
        }

        try {
            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            Thread.sleep(cycleTime);

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.fatal("Thread was interrupted: " + e);
        }
    }

    @Override
    protected void runComplete() {

    }

    private List<JEVisObject> getEnabledReports() {
        JEVisClass reportClass = null;
        List<JEVisObject> reportObjects = null;
        try {
            reportClass = ds.getJEVisClass(ReportAttributes.NAME);
            reportObjects = ds.getObjects(reportClass, true);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        List<JEVisObject> enabledReports = new ArrayList<>();
        reportObjects.forEach(jeVisObject -> {
                    ReportPolicy reportPolicy = new ReportPolicy();
                    if (reportPolicy.isReportEnabled(jeVisObject)) enabledReports.add(jeVisObject);
                }
        );
        return enabledReports;
    }
}
