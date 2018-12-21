/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jedataprocessor.workflow.ProcessManager;
import org.jevis.jedataprocessor.workflow.ProcessManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author broder
 */
public class Launcher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private static final String APP_INFO = "JEDataProcessor";
    public static String KEY = "process-id";
    private final String APP_SERVICE_CLASS_NAME = "JEDataProcessor";
    private final Command commands = new Command();

    private Launcher(String[] args, String appname) {
        super(args, appname);
    }

    public static void main(String[] args) {

        logger.info("-------Start JEDataProcessor-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    private void executeProcesses(List<ProcessManager> processes) {

        initializeThreadPool(APP_SERVICE_CLASS_NAME);

        logger.info("{} cleaning task found starting", processes.size());

        try {
            forkJoinPool.submit(() -> processes.parallelStream().forEach(currentProcess -> {
                if (!runningJobs.containsKey(currentProcess.getId().toString())) {

                    runningJobs.put(currentProcess.getId().toString(), "true");

                    try {
                        currentProcess.start();
                    } catch (Exception ex) {
                        logger.debug(ex);
                    }
                    runningJobs.remove(currentProcess.getId().toString());

                } else {
                    logger.error("Still processing Job " + currentProcess.getName() + ":" + currentProcess.getId());
                }

            })).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Thread Pool was interrupted or execution was stopped: " + e);
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
        ProcessManagerFactory pmf = new ProcessManagerFactory(ds);

        try {
            List<ProcessManager> processList = pmf.initProcessManagersFromJEVisSingle(id);

            executeProcesses(processList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void runServiceHelp() {
        List<ProcessManager> processManagerList = new ArrayList<>();
        try {
            ds.clearCache();
            ds.preload();
            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error(e);
        }

        if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
            try {
                ProcessManagerFactory pmf = new ProcessManagerFactory(ds);
                processManagerList = pmf.initProcessManagersFromJEVisAll();
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.executeProcesses(processManagerList);
        } else {
            logger.info("Service is disabled.");
        }

        try {
            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            logger.info("Entering Sleep mode for " + cycleTime + "ms.");
            Thread.sleep(cycleTime);
            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    @Override
    protected void runComplete() {
        List<ProcessManager> processManagerList = new ArrayList<>();
        try {
            ProcessManagerFactory pmf = new ProcessManagerFactory(ds);
            processManagerList = pmf.initProcessManagersFromJEVisAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.executeProcesses(processManagerList);
    }


}
