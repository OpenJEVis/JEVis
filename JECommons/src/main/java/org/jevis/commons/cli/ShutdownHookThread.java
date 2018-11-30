package org.jevis.commons.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class ShutdownHookThread extends Thread {
    public static final String THREAD_NAME = "Shutdown Hook";
    private static final Logger logger = LogManager.getLogger(ShutdownHookThread.class);

    public static final int GRACE_PERIOD = 0;
    public static final TimeUnit GRACE_PERIOD_TIME_UNIT = TimeUnit.SECONDS;

    private Thread thread;

    /**
     * @param service The Thread to shut down
     */
    public ShutdownHookThread(Thread service) {
        this.thread = service;
        setName(THREAD_NAME);
    }

    @Override
    public void run() {
        logger.info("Interrupting current Thread");

        logger.info("Waiting for thread to shut down... Grace period is " + GRACE_PERIOD + GRACE_PERIOD_TIME_UNIT);
        thread.interrupt();

        logger.info("Thread stopped.");
    }
}
