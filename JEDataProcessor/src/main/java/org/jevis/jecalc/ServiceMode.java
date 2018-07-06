package org.jevis.jecalc;

import org.jevis.jecalc.workflow.ProcessManager;
import org.jevis.jecalc.workflow.ProcessManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServiceMode {
    private static final Logger logger = LoggerFactory.getLogger(ServiceMode.class);
    private Integer cycleTime = 900000;

    public ServiceMode(Integer cycleTime) {
        this.cycleTime = cycleTime;
    }

    public ServiceMode() {
    }

    public void run() {
        Thread service = new Thread(() -> runServiceHelp());
        Runtime.getRuntime().addShutdownHook(
                new JEDataProcessorShutdownHookThread(service)
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

    private void runServiceHelp() {

        this.runProcesses();
        try {
            Thread.sleep(cycleTime);
            runServiceHelp();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runProcesses() {
        List<ProcessManager> processes = ProcessManagerFactory.getProcessManagerList();

        logger.info("{} cleaning jobs found", processes.size());
        processes.stream().forEach((currentProcess) -> {
            try {
                currentProcess.start();
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                logger.debug(ex.getMessage(), ex);
            }
        });
    }
}
