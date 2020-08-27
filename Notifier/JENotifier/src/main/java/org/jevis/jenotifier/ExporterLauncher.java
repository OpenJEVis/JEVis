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
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.jevis.jenotifier.exporter.CSVExport;
import org.jevis.jenotifier.exporter.Export;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;

import java.util.ArrayList;
import java.util.List;


/**
 * @author broder
 */
public class ExporterLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(ExporterLauncher.class);
    private static final String APP_INFO = "JENotifier";
    private final String APP_SERVICE_CLASS_NAME = "JENotifier";
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


    private void executeReports(List<JEVisObject> reportObjects) {


        logger.info("Number of Reports: " + reportObjects.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        List<Export> exportJobs = new ArrayList<>();
        reportObjects.forEach(exporterObject -> {
            Export exporter = null;
            try {
                if (exporterObject.getJEVisClassName().equals(CSVExport.CLASS_NAME)) {
                    exporter = new CSVExport(jeNotifierConfig, exporterObject);
                    exportJobs.add(exporter);
                    if (!plannedJobs.containsKey(exporterObject.getID())) {
                        plannedJobs.put(exporterObject.getID(), "true");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        exportJobs.forEach(exporterObject -> {
            forkJoinPool.submit(() -> {
                if (!runningJobs.containsKey(exporterObject.getObjectID())) {
                    Thread.currentThread().setName(exporterObject.toString());
                    runningJobs.put(exporterObject.getObjectID(), "true");


                    LogTaskManager.getInstance().buildNewTask(exporterObject.getObjectID(), exporterObject.toString());
                    LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.STARTED);

                    logger.info("---------------------------------------------------------------------");
                    logger.info("current report object: " + exporterObject.toString() + " with id: " + exporterObject.getObjectID());
                    //check if the report is enabled


                    try {

                        if (exporterObject.isEnabled()) {
                            try {
                                exporterObject.executeExport();
                                if (exporterObject.hasNewData()) {
                                    exporterObject.sendNotification();
                                }

                                LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.FINISHED);

                            } catch (Exception ex) {
                                LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.FAILED);
                                LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setException(ex);
                            }
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                        LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setStatus(Task.Status.FAILED);
                        LogTaskManager.getInstance().getTask(exporterObject.getObjectID()).setException(ex);
                    }

                    runningJobs.remove(exporterObject.getObjectID());
                    plannedJobs.remove(exporterObject.getObjectID());

                    logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                    if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                        logger.info("Last job. Clearing cache.");
                        setServiceStatus(APP_SERVICE_CLASS_NAME, 1L);
                        ds.clearCache();
                    }

                } else {
                    logger.error("Still processing Report " + exporterObject.toString() + ":" + exporterObject.getObjectID());
                }
            });
        });

        logger.info("---------------------finish------------------------");
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
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
        try {
            checkConnection();
        } catch (JEVisException | InterruptedException e) {
            e.printStackTrace();
        }

        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
            try {
                ds.clearCache();
                ds.preload();
                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {

                /** reload drivers and set Config context drivers **/
                try {
                    JEVisClass jenotifierClass = ds.getJEVisClass(APP_SERVICE_CLASS_NAME);
                    JEVisClass emailDriver = ds.getJEVisClass(EmailNotificationDriver._type);
                    JEVisObject jenotifierObjct = ds.getObjects(jenotifierClass, true).get(0);

                    for (JEVisObject eDriverObject : jenotifierObjct.getChildren(emailDriver, true)) {
                        JEVisAttribute defaultAttribute = eDriverObject.getAttribute("Default");
                        if (defaultAttribute != null && defaultAttribute.getLatestSample() != null) {
                            if (defaultAttribute.getLatestSample().getValueAsBoolean()) {

                                EmailNotificationDriver emailNotificationDriver = new EmailNotificationDriver();
                                emailNotificationDriver.setNotificationDriverObject(eDriverObject);
                                logger.info("---------------------- user: " + emailNotificationDriver.getUser());
                                this.jeNotifierConfig.setDefaultEmailNotificationDriver(emailNotificationDriver);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                List<JEVisObject> reports = getAllExports();
                executeReports(reports);

                logger.info("Queued all report objects, entering sleep mode for " + cycleTime + " ms.");

            } else {
                logger.info("Service was disabled.");
            }
        } else {
            logger.info("Still running queue. Going to sleep again.");
        }

        try {
            Thread.sleep(cycleTime);

            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            runServiceHelp();
        } catch (InterruptedException e) {
            logger.fatal("Thread was interrupted: " + e);
        }
    }

    @Override
    protected void runComplete() {

    }

    private List<JEVisObject> getAllExports() {
        JEVisClass exportClass = null;
        List<JEVisObject> reportObjects = new ArrayList<>();
        try {
            exportClass = ds.getJEVisClass(Export.CLASS_NAME);
            reportObjects = ds.getObjects(exportClass, true);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return reportObjects;
    }
}