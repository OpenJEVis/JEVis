/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class Launcher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private static final String APP_INFO = "JEAlarm";
    public static String KEY = "process-id";
    private final Command commands = new Command();
    private final boolean firstRun = true;

    private Launcher(String[] args, String appname) {
        super(args, appname);
    }

    public static void main(String[] args) {

        logger.info("-------Start JEAlarm-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    private void executeProcesses(List<AlarmConfiguration> processes) {
        logger.info("Number of Alarm Jobs: {}", processes.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        processes.forEach(alarmConfiguration -> {
            if (!runningJobs.containsKey(alarmConfiguration.getId())) {
                Runnable runnable = () -> {
                    try {
                        Thread.currentThread().setName(alarmConfiguration.getName() + ":" + alarmConfiguration.getId().toString());
                        runningJobs.put(alarmConfiguration.getId(), new DateTime());

                        LogTaskManager.getInstance().buildNewTask(alarmConfiguration.getId(), alarmConfiguration.getName());
                        LogTaskManager.getInstance().getTask(alarmConfiguration.getId()).setStatus(Task.Status.STARTED);

                        AlarmProcess currentProcess = new AlarmProcess(alarmConfiguration);
                        currentProcess.start();

                    } catch (Exception e) {
                        LogTaskManager.getInstance().getTask(alarmConfiguration.getObject().getID()).setStatus(Task.Status.FAILED);
                        removeJob(alarmConfiguration.getObject());

                        logger.info("Planned Jobs: {} running Jobs: {}", plannedJobs.size(), runningJobs.size());

                        checkLastJob();
                    } finally {
                        LogTaskManager.getInstance().getTask(alarmConfiguration.getObject().getID()).setStatus(Task.Status.FINISHED);
                        removeJob(alarmConfiguration.getObject());

                        logger.info("Planned Jobs: {} running Jobs: {}", plannedJobs.size(), runningJobs.size());

                        checkLastJob();
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);

                runnables.put(alarmConfiguration.getObject().getID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing Job {}:{}", alarmConfiguration.getName(), alarmConfiguration.getObject().getID());
            }
        });
    }


    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEAlarm";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        for (Long id : ids) {
            try {
                AlarmConfiguration ac = new AlarmConfiguration(ds, ds.getObject(id));
                AlarmProcess ap = new AlarmProcess(ac);
                ap.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void runServiceHelp() {

        if (checkConnection()) {

            checkForTimeout();

            List<AlarmConfiguration> enabledAlarmConfigurations = new ArrayList<>();

            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
//                if (!firstRun) {
//                    try {
//                        ds.clearCache();
//                        ds.preload();
//                    } catch (JEVisException e) {
//                        logger.error(e);
//                    }
//                } else firstRun = false;

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                    try {
                        enabledAlarmConfigurations = getAllAlarmObjects();
                    } catch (Exception e) {
                        logger.error("Could not get cleaning objects. ", e);
                    }

                    this.executeProcesses(enabledAlarmConfigurations);
                } else {
                    logger.info("Service is disabled.");
                }
            } else {
                logger.info("Still running queue. Going to sleep again.");
            }
        }

        sleep();
    }

    @Override
    protected void runComplete() {
        List<AlarmConfiguration> enabledAlarmConfigurations = new ArrayList<>();
        try {
            enabledAlarmConfigurations = getAllAlarmObjects();
        } catch (Exception e) {
            logger.error("Could not get enabled alarm configurations objects. ", e);
        }
        enabledAlarmConfigurations.forEach(alarmConfiguration -> {
            try {
                AlarmProcess currentProcess = new AlarmProcess(alarmConfiguration);
                currentProcess.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<AlarmConfiguration> getAllAlarmObjects() throws Exception {
        JEVisClass alarmConfig;
        List<JEVisObject> alarmConfigs;
        List<AlarmConfiguration> filteredObjects = new ArrayList<>();

        try {
            alarmConfig = ds.getJEVisClass(AlarmConfiguration.CLASS_NAME);
            JEVisDataSourceWS dsWS = (JEVisDataSourceWS) ds;
            alarmConfigs = dsWS.getObjectsWS(alarmConfig, false);
            logger.info("Total amount of Alarm Configuration Objects: {}", alarmConfigs.size());
            alarmConfigs.forEach(jeVisObject -> {
                ds.reloadObject(jeVisObject);
                AlarmConfiguration alarmConfiguration = new AlarmConfiguration(ds, jeVisObject);
                if (alarmConfiguration.isEnabled()) {
                    filteredObjects.add(alarmConfiguration);
                    if (!plannedJobs.containsKey(jeVisObject.getID())) {
                        plannedJobs.put(jeVisObject.getID(), new DateTime());
                    }
                }
            });
            logger.info("Amount of enabled Alarm Configurations: {}", filteredObjects.size());
        } catch (JEVisException ex) {
            logger.error("Process classes missing", ex);
        }
        return filteredObjects;
    }

}