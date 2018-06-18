package org.jevis.rest;

import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GrizzlyServerShutdownHookThread extends Thread {
    public static final String THREAD_NAME = "Grizzly Server Shutdown Hook";

    public static final int GRACE_PERIOD = 60;
    public static final TimeUnit GRACE_PERIOD_TIME_UNIT = TimeUnit.SECONDS;

    private final HttpServer server;

    /**
     * @param server The server to shut down
     */
    public GrizzlyServerShutdownHookThread(HttpServer server) {
        this.server = server;
        setName(THREAD_NAME);
    }

    @Override
    public void run() {
        System.out.println("Running Grizzly Server Shutdown Hook.");
        System.out.println("Shutting down server.");
        GrizzlyFuture<HttpServer> future = server.shutdown(GRACE_PERIOD, GRACE_PERIOD_TIME_UNIT);

        try {
            System.out.println("Waiting for server to shut down... Grace period is " + GRACE_PERIOD + GRACE_PERIOD_TIME_UNIT);
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error while shutting down server. " + e);
        }

        System.out.println("Server stopped.");
    }
}
