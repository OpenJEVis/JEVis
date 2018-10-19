package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.TaskPrinter;
import org.jevis.jecalc.workflow.ProcessManager;
import org.jevis.jecalc.workflow.ProcessManagerFactory;

import java.util.List;

public class ServiceMode {
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private Integer cycleTime = 900000;

    public ServiceMode(Integer cycleTime) {
        this.cycleTime = cycleTime;
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

        logger.info("{} cleaning task found starting with 4 threads", processes.size());
        processes.stream().limit(4).forEach((currentProcess) -> {
            try {
                currentProcess.start();
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                logger.debug(ex.getMessage(), ex);
            }
        });
        logger.info("Cleaning finished.");
    }
}
