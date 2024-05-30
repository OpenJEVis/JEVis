/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.jevis.jenotifier.exporter.CSVExport;
import org.jevis.jenotifier.exporter.Export;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;


/**
 * @author broder
 */
public class ExporterLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(ExporterLauncher.class);
    private static final String APP_INFO = "JENotifier";
    private final Command commands = new Command();
    private final JENotifierConfig jeNotifierConfig = new JENotifierConfig();

    public ExporterLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JENotifier-------");
        ExporterLauncher app = new ExporterLauncher(args, APP_INFO);
        app.execute();
    }


    private void executeReports(List<JEVisObject> exportObjects) {


        logger.info("Number of Exports: " + exportObjects.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        List<Export> exportJobs = new ArrayList<>();
        exportObjects.forEach(exporterObject -> {
            Export exporter = null;
            try {
                if (exporterObject.getJEVisClassName().equals(CSVExport.CLASS_NAME)) {
                    exporter = new CSVExport(jeNotifierConfig, exporterObject);
                    exportJobs.add(exporter);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exportJobs.forEach(exporterObject -> {
            if (!runningJobs.containsKey(exporterObject.getObjectID())) {
                Runnable runnable = () -> {
                    try {
                        logger.error("exportJobs: {}", exporterObject.getObjectID());
                        Thread.currentThread().setName(exporterObject.toString());
                        runningJobs.put(exporterObject.getObjectID(), new DateTime());

                        LogTaskManager.getInstance().buildNewTask(exporterObject.getObjectID(), exporterObject.toString());
                        LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.STARTED);

                        exporterObject.executeExport();

                        if (exporterObject.hasNewData()) {
                            logger.error("Send Mail");
                            exporterObject.sendNotification();
                        } else {
                            logger.error("Has no new Data");
                        }

                    } catch (Exception e) {
                        LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.FAILED);
                        removeJob(exporterObject.getExportObject());

                        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                        checkLastJob();
                    } finally {
                        LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.FINISHED);
                        StringBuilder finished = new StringBuilder();
                        finished.append(exporterObject.getObjectID()).append(" in ");
                        String length = new Period(runningJobs.get(exporterObject.getObjectID()), new DateTime()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale()));
                        removeJob(exporterObject.getExportObject());
                        finished.append(length);

                        StringBuilder running = new StringBuilder();
                        runningJobs.forEach((aLong, dateTime) -> running.append(aLong).append(" - started: ").append(dateTime).append(" "));

                        logger.info("Queued Jobs: {} | Finished {} | running Jobs: {}", plannedJobs.size(), finished.toString(), running.toString());

                        checkLastJob();
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);

                runnables.put(exporterObject.getObjectID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing Job {}:{}", exporterObject.getExportObject().getName(), exporterObject.getExportObject().getID());
            }
        });
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JENotifier";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        logger.info("Start Single Mode");

        for (Long id : ids) {
            JEVisObject reportObject = null;

            try {
                logger.info("Try adding Single Mode for ID " + id);
                reportObject = ds.getObject(id);
            } catch (Exception ex) {
                logger.error("Could not find Object with id: " + id);
            }

            if (reportObject != null) {
                //TODO
            }
        }
    }

    @Override
    protected void runServiceHelp() {
        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.isEmpty() && runningJobs.isEmpty()) {

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {

                    getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

                    /** reload drivers and set Config context drivers **/
                    try {
                        ds.reloadObjects();

                        JEVisClass jenotifierClass = ds.getJEVisClass(APP_SERVICE_CLASS_NAME);
                        JEVisClass emailDriver = ds.getJEVisClass(EmailNotificationDriver._type);
                        JEVisObject jenotifierObjct = ds.getObjects(jenotifierClass, true).get(0);

                        for (JEVisObject eDriverObject : jenotifierObjct.getChildren(emailDriver, true)) {
                            JEVisAttribute defaultAttribute = eDriverObject.getAttribute("Default");
                            if (defaultAttribute != null && defaultAttribute.getLatestSample() != null) {
                                if (defaultAttribute.getLatestSample().getValueAsBoolean()) {

                                    EmailNotificationDriver emailNotificationDriver = new EmailNotificationDriver();
                                    emailNotificationDriver.setNotificationDriverObject(eDriverObject);
                                    logger.debug("---------------------- user: {}", emailNotificationDriver.getUser());
                                    this.jeNotifierConfig.setDefaultEmailNotificationDriver(emailNotificationDriver);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                    List<JEVisObject> reports = getAllExports();
                    executeReports(reports);
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

    private List<JEVisObject> getAllExports() {
        List<JEVisObject> filteredObjects = new ArrayList<>();
        try {
            JEVisClass exportClass = ds.getJEVisClass(Export.CLASS_NAME);
            List<JEVisObject> reportObjects = ds.getObjects(exportClass, true);

            reportObjects.forEach(jeVisObject -> {
                if (isEnabled(jeVisObject)) {
                    filteredObjects.add(jeVisObject);
                    if (!plannedJobs.containsKey(jeVisObject.getID())) {
                        plannedJobs.put(jeVisObject.getID(), new DateTime());
                    }
                }
            });
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return filteredObjects;
    }
}