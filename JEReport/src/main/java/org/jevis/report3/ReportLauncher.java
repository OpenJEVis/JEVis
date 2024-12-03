/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.report3.context.ContextBuilder;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportExecutor;
import org.jevis.report3.data.report.intervals.Finisher;
import org.jevis.report3.data.report.intervals.IntervalCalculator;
import org.jevis.report3.data.report.intervals.Precondition;
import org.jevis.report3.data.reportlink.ReportLinkFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.FutureTask;


/**
 * @author broder
 */
public class ReportLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(ReportLauncher.class);
    private static final String APP_INFO = "JEReport";
    private static Injector injector;
    private final Command commands = new Command();
    private boolean firstRun = true;

    public ReportLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEReport-------");
        System.setProperty("user.timezone", "UTC");
        ReportLauncher app = new ReportLauncher(args, APP_INFO);
        app.execute();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void setInjector(Injector inj) {
        injector = inj;
    }

    private static @NotNull ReportExecutor getReportExecutor(JEVisObject reportObject) {
        SampleHandler sampleHandler = new SampleHandler();
        Precondition precondition = new Precondition(sampleHandler);
        IntervalCalculator intervalCalculator = new IntervalCalculator(sampleHandler);
        ContextBuilder contextBuilder = new ContextBuilder();
        Finisher finisher = new Finisher(reportObject, sampleHandler);
        ReportLinkFactory reportLinkFactory = new ReportLinkFactory();

        return new ReportExecutor(precondition, intervalCalculator, contextBuilder, finisher, reportLinkFactory, reportObject);
    }

    private void executeReports(List<JEVisObject> reportObjects) {

        logger.info("Number of Reports: {}", reportObjects.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        reportObjects.forEach(reportObject -> {
            if (!runningJobs.containsKey(reportObject.getID())) {
                Runnable runnable = () -> {
                    try {
                        Thread.currentThread().setName(reportObject.getName() + ":" + reportObject.getID().toString());
                        runningJobs.put(reportObject.getID(), new DateTime());

                        LogTaskManager.getInstance().buildNewTask(reportObject.getID(), reportObject.getName());
                        LogTaskManager.getInstance().getTask(reportObject.getID()).setStatus(Task.Status.STARTED);

                        logger.info("---------------------------------------------------------------------");
                        logger.info("current report object: {} with id: {}", reportObject.getName(), reportObject.getID());

                        ReportExecutor reportExecutor = getReportExecutor(reportObject);
                        reportExecutor.executeReport();

                        LogTaskManager.getInstance().getTask(reportObject.getID()).setStatus(Task.Status.FINISHED);
                    } catch (Exception e) {
                        LogTaskManager.getInstance().getTask(reportObject.getID()).setStatus(Task.Status.FAILED);

                        logger.error("Error Job: {}:{}", reportObject.getName(), reportObject.getID(), e);

                    } finally {

                        StringBuilder finished = new StringBuilder();
                        finished.append(reportObject.getID()).append(" in ");
                        String length = new Period(runningJobs.get(reportObject.getID()), new DateTime()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale()));
                        removeJob(reportObject);
                        finished.append(length);

                        StringBuilder running = new StringBuilder();
                        runningJobs.forEach((aLong, dateTime) -> running.append(aLong).append(" - started: ").append(dateTime).append(" "));

                        logger.info("Queued Jobs: {} | Finished {} | running Jobs: {}", plannedJobs.size(), finished.toString(), running.toString());

                        checkLastJob();
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);

                runnables.put(reportObject.getID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing Job {}:{}", reportObject.getName(), reportObject.getID());
            }
        });
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEReport";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        logger.info("Start Single Mode");

        for (Long id : ids) {
            JEVisObject reportObject = null;

            try {
                logger.info("Try adding Single Mode for ID {}", id);
                reportObject = ds.getObject(id);
            } catch (Exception ex) {
                logger.error("Could not find Object with id: {}", id);
            }

            if (reportObject != null) {
                ReportExecutor executor = getReportExecutor(reportObject);
                Objects.requireNonNull(executor).executeReport();
            }
        }
    }

    @Override
    protected void runServiceHelp() {
        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                if (!firstRun) {
                    TaskPrinter.printJobStatus(LogTaskManager.getInstance());
                    try {
                        ds.clearCache();
                        ds.preload();
                    } catch (JEVisException e) {
                    }
                } else firstRun = false;

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {

                    List<JEVisObject> reports = getEnabledReports();
                    executeReports(reports);

                    logger.info("Queued all report objects, entering sleep mode for {} ms", cycleTime);

                } else {
                    logger.info("Service was disabled.");
                }
            } else {
                logger.info("Still running queue. Going to sleep again.");
            }
        }

        sleep();
    }

    @Override
    protected void runComplete() {

    }

    private List<JEVisObject> getEnabledReports() {
        JEVisClass reportClass = null;
        List<JEVisObject> reportObjects = new ArrayList<>();
        try {
            reportClass = ds.getJEVisClass(ReportAttributes.NAME);
            reportObjects = ds.getObjects(reportClass, true);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        List<JEVisObject> enabledReports = new ArrayList<>();
        reportObjects.forEach(jeVisObject -> {
            if (isEnabled(jeVisObject)) {
                enabledReports.add(jeVisObject);
                if (!plannedJobs.containsKey(jeVisObject.getID())) {
                    plannedJobs.put(jeVisObject.getID(), new DateTime());
                }
            }
                }
        );
        return enabledReports;
    }
}