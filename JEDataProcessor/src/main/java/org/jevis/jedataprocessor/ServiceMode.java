package org.jevis.jedataprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jedataprocessor.workflow.ProcessManager;
import org.jevis.jedataprocessor.workflow.ProcessManagerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceMode {
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private Integer cycleTime = 900000;
    private ConcurrentHashMap<String, String> runningJobs = new ConcurrentHashMap();

    public ServiceMode(Integer cycleTime) {
        this.cycleTime = cycleTime;
    }

    private void getCycleTimeFromService() throws JEVisException {
        JEVisClass dataProcessorClass = ProcessManagerFactory.jevisDataSource.getJEVisClass("JEDataProcessor");
        List<JEVisObject> listDataProcessorObjects = ProcessManagerFactory.jevisDataSource.getObjects(dataProcessorClass, false);
        cycleTime = listDataProcessorObjects.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong().intValue();
        logger.info("Service cycle time from service: " + cycleTime);
    }

    public ServiceMode() {
    }

    public void run() {


        try {
            Thread service = new Thread(() -> {
                try {
                    runEndlessService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            Runtime.getRuntime().addShutdownHook(
                    new JEDataProcessorShutdownHookThread(service)
            );

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

    private void runEndlessService() throws Exception {

        try {
            ProcessManagerFactory.jevisDataSource.reloadAttributes();
            getCycleTimeFromService();
        } catch (JEVisException e) {
            logger.error(e);
        }
        this.runProcesses();
        try {
            TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            logger.info("Entering Sleep mode for " + cycleTime + "ms.");
            Thread.sleep(cycleTime);
            runEndlessService();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    private void runProcesses() throws Exception {
        List<ProcessManager> processes = ProcessManagerFactory.getProcessManagerList();

        logger.info("{} cleaning task found starting", processes.size());
        ProcessManagerFactory.getForkJoinPool().submit(
                () -> processes.parallelStream().forEach(
                        currentProcess -> {
                            if (!runningJobs.containsKey(currentProcess.getName() + ":" + currentProcess.getId())) {

                                runningJobs.put(currentProcess.getName() + ":" + currentProcess.getId(), "true");

                                try {
                                    currentProcess.start();
                                } catch (Exception ex) {
                                    logger.debug(ex);
                                }
                                runningJobs.remove(currentProcess.getName());

                            } else {
                                logger.error("Still processing Job " + currentProcess.getName() + ":" + currentProcess.getId());
                            }

                        }));
    }
}
