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
import org.jevis.commons.task.TaskPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class Launcher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private static final String APP_INFO = "JEAlarm";
    public static String KEY = "process-id";
    private final String APP_SERVICE_CLASS_NAME = "JEAlarm";
    private final Command commands = new Command();

    private Launcher(String[] args, String appname) {
        super(args, appname);
    }

    public static void main(String[] args) {

        logger.info("-------Start JEAlarm-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    private void executeProcesses(List<AlarmConfiguration> processes) {

        processes.parallelStream().forEach(alarmConfiguration -> {
            forkJoinPool.submit(new Runnable() {

                @Override
                public void run() {
                    if (!runningJobs.containsKey(alarmConfiguration.getId())) {
                        Thread.currentThread().setName(alarmConfiguration.getName() + ":" + alarmConfiguration.getId().toString());
                        runningJobs.put(alarmConfiguration.getId(), "true");

                        try {
                            LogTaskManager.getInstance().buildNewTask(alarmConfiguration.getId(), alarmConfiguration.getName());
                            LogTaskManager.getInstance().getTask(alarmConfiguration.getId()).setStatus(Task.Status.STARTED);

                            AlarmProcess currentProcess = new AlarmProcess(alarmConfiguration);
                            currentProcess.start();
                        } catch (Exception ex) {
                            logger.debug(ex);
                            logger.error(LogTaskManager.getInstance().getTask(alarmConfiguration.getId()).getException());
                            LogTaskManager.getInstance().getTask(alarmConfiguration.getId()).setStatus(Task.Status.FAILED);
                        }

                        LogTaskManager.getInstance().getTask(alarmConfiguration.getId()).setStatus(Task.Status.FINISHED);
                        runningJobs.remove(alarmConfiguration.getId());
                        plannedJobs.remove(alarmConfiguration.getId());

                        logger.info("Planned Jobs: " + plannedJobs.size() + " running Jobs: " + runningJobs.size());

                        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                            logger.info("Last job. Clearing cache.");
                            ds.clearCache();
                        }

                    } else {
                        logger.error("Still processing Job " + alarmConfiguration.getName() + ":" + alarmConfiguration.getId());
                    }
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
    protected void runSingle(Long id) {

        try {
            AlarmConfiguration ac = new AlarmConfiguration(ds, ds.getObject(id));
            AlarmProcess ap = new AlarmProcess(ac);
            ap.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void runServiceHelp() {
        List<AlarmConfiguration> enabledAlarmConfigurations = new ArrayList<>();
        try {
            ds.clearCache();
            ds.preload();
            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error(e);
        }

        if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
            try {
                enabledAlarmConfigurations = getAllAlarmObjects();
            } catch (Exception e) {
                logger.error("Could not get cleaning objects. " + e);
            }

            this.executeProcesses(enabledAlarmConfigurations);
        } else {
            logger.info("Service is disabled.");
        }

        try {
            logger.info("Entering Sleep mode for " + cycleTime + "ms.");
            Thread.sleep(cycleTime);

            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    @Override
    protected void runComplete() {
        List<AlarmConfiguration> enabledAlarmConfigurations = new ArrayList<>();
        try {
            enabledAlarmConfigurations = getAllAlarmObjects();
        } catch (Exception e) {
            logger.error("Could not get enabled alarm configurations objects. " + e);
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
            alarmConfigs = ds.getObjects(alarmConfig, false);
            logger.info("Total amount of Alarm Configuration Objects: " + alarmConfigs.size());
            alarmConfigs.forEach(jeVisObject -> {
                AlarmConfiguration alarmConfiguration = new AlarmConfiguration(ds, jeVisObject);
                if (alarmConfiguration.isEnabled()) {
                    filteredObjects.add(alarmConfiguration);
                    if (!plannedJobs.containsKey(jeVisObject.getID())) {
                        plannedJobs.put(jeVisObject.getID(), "true");
                    }
                }
            });
            logger.info("Amount of enabled Alarm Configurations: " + filteredObjects.size());
        } catch (JEVisException ex) {
            throw new Exception("Process classes missing", ex);
        }
        return filteredObjects;
    }

}
