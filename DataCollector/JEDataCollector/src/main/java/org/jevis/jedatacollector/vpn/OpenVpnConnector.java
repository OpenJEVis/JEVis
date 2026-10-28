package org.jevis.jedatacollector.vpn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * One OpenVPN session: starts the tunnel through the wrapper, drives it over the management socket
 * and stops it again.
 */
public class OpenVpnConnector implements VpnConnector {

    private static final Logger logger = LogManager.getLogger(OpenVpnConnector.class);
    private static final long START_TIMEOUT = 20000;
    private static final long ATTACH_RETRY = 250;
    private static final long EXTERNAL_STATE_TIMEOUT = 3000;
    private static final long SIGTERM_TIMEOUT = 10000;
    private static final long STOP_POLL = 5000;
    private static final int LOG_TAIL_LINES = 20;

    private final OpenVpnBackend backend;
    private final String profile;
    private volatile ManagementClient client;
    private volatile boolean started;
    private volatile boolean external;

    OpenVpnConnector(OpenVpnBackend backend, String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    static List<String> tail(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long length = raf.length();
            long start = Math.max(0, length - 8192);
            byte[] buffer = new byte[(int) (length - start)];
            raf.seek(start);
            raf.readFully(buffer);
            List<String> lines = new ArrayList<>(Arrays.asList(new String(buffer, StandardCharsets.UTF_8).split("\n")));
            if (start > 0 && !lines.isEmpty()) lines.remove(0);
            return lines.subList(Math.max(0, lines.size() - LOG_TAIL_LINES), lines.size());
        } catch (IOException e) {
            return Collections.singletonList("(not readable: " + e.getMessage() + ")");
        }
    }

    @Override
    public void connect(VpnConfig config) throws VpnException, InterruptedException {
        long deadline = System.currentTimeMillis() + config.getConnectTimeout() * 1000;

        switch (backend.status(profile)) {
            case UNKNOWN:
                throw new VpnException("Cannot query tunnel status of profile '" + profile
                        + "'. Is the wrapper installed and the sudoers rule for the collector user in place?");
            case RUNNING:
                if (attachToRunningTunnel()) {
                    logger.info("VPN profile {} is already connected outside of the collector, using it without closing it afterwards", profile);
                    external = true;
                    checkReachability(config, deadline);
                    return;
                }
                logger.warn("VPN profile {} has a stale tunnel process, stopping it before reconnecting", profile);
                if (!backend.forceStop(profile)) {
                    throw new VpnException("Stale tunnel process of profile '" + profile + "' could not be stopped");
                }
                break;
            default:
                break;
        }

        backend.writeMarker(profile);
        started = true;
        CommandRunner.Result result = backend.wrapper("start", profile);
        if (!result.isSuccess()) {
            throw new VpnException("Starting OpenVPN for profile '" + profile + "' failed: " + result);
        }

        attach(deadline);
        try {
            client.send("state on");
            client.send("bytecount 5");
            client.send("hold release");
        } catch (IOException e) {
            throw new VpnException("Management connection lost: " + e.getMessage(), e);
        }

        waitForConnected(config, deadline);
        checkReachability(config, deadline);
        logger.info("VPN profile {} connected", profile);
    }

    private boolean attachToRunningTunnel() throws InterruptedException {
        try {
            client = backend.openManagement(profile);
            client.send("state");
            long deadline = System.currentTimeMillis() + EXTERNAL_STATE_TIMEOUT;
            while (client.getState() == null && !client.isClosed() && System.currentTimeMillis() < deadline) {
                client.awaitEvent(deadline - System.currentTimeMillis());
            }
            if ("CONNECTED".equals(client.getState())) {
                client.send("bytecount 5");
                return true;
            }
        } catch (IOException e) {
            logger.debug("Could not attach to running tunnel of profile {}: {}", profile, e.getMessage());
        }
        closeClient();
        return false;
    }

    private void attach(long deadline) throws VpnException, InterruptedException {
        IOException last = null;
        while (System.currentTimeMillis() < deadline) {
            if (backend.socketFile(profile).exists()) {
                try {
                    client = backend.openManagement(profile);
                    return;
                } catch (IOException e) {
                    last = e;
                }
            }
            Thread.sleep(ATTACH_RETRY);
        }
        throw new VpnException("Management socket " + backend.socketFile(profile) + " not available"
                + (last != null ? ": " + last.getMessage() : ""), last);
    }

    private void waitForConnected(VpnConfig config, long deadline) throws VpnException, InterruptedException {
        while (true) {
            String passwordRequest = client.takePasswordRequest();
            if (passwordRequest != null) answerPasswordRequest(passwordRequest, config);

            if (client.isAuthFailed()) {
                throw new VpnException("Authentication failed (AUTH_FAILED) for profile '" + profile + "'");
            }
            if (client.getFatal() != null) {
                throw new VpnException("OpenVPN fatal error: " + client.getFatal());
            }
            if ("CONNECTED".equals(client.getState())) {
                if ("SUCCESS".equals(client.getStateDetail())) return;
                throw new VpnException("Tunnel connected with errors (" + client.getStateDetail() + "), e.g. routes could not be set");
            }
            if ("EXITING".equals(client.getState()) || client.isClosed()) {
                throw new VpnException("OpenVPN exited while connecting: " + String.join(" | ", client.getRecentLines()));
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                throw new VpnException("Timeout after " + config.getConnectTimeout() + " s, last state: " + client.getState());
            }
            client.awaitEvent(Math.min(remaining, 1000));
        }
    }

    private void answerPasswordRequest(String type, VpnConfig config) throws VpnException {
        try {
            if ("Auth".equals(type)) {
                if (config.getUser() == null || config.getPassword() == null) {
                    throw new VpnException("Profile '" + profile + "' requires username/password but the VPN Connection has none");
                }
                client.send("username \"Auth\" " + ManagementClient.quote(config.getUser()));
                client.send("password \"Auth\" " + ManagementClient.quote(config.getPassword()));
            } else if ("Private Key".equals(type)) {
                if (config.getPassword() == null) {
                    throw new VpnException("Private key of profile '" + profile + "' is encrypted but the VPN Connection has no password");
                }
                client.send("password \"Private Key\" " + ManagementClient.quote(config.getPassword()));
            } else {
                throw new VpnException("Unsupported password request '" + type + "' from OpenVPN");
            }
        } catch (IOException e) {
            throw new VpnException("Management connection lost: " + e.getMessage(), e);
        }
    }

    private void checkReachability(VpnConfig config, long deadline) throws VpnException {
        if (config.getCheckHost() == null || config.getCheckPort() <= 0) return;
        int timeout = (int) Math.max(5000, deadline - System.currentTimeMillis());
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(config.getCheckHost(), config.getCheckPort()), timeout);
        } catch (IOException e) {
            throw new VpnException("Tunnel is up but " + config.getCheckHost() + ":" + config.getCheckPort()
                    + " is not reachable: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean disconnect() {
        try {
            if (external || !started) {
                // nothing started by this connector, nothing to stop
                closeClient();
                return true;
            }

            ManagementClient current = client;
            if (current != null && !current.isClosed()) {
                try {
                    current.send("signal SIGTERM");
                    current.awaitClosedUninterruptibly(SIGTERM_TIMEOUT);
                } catch (IOException e) {
                    logger.debug("Could not send SIGTERM over management socket of {}: {}", profile, e.getMessage());
                }
            }
            closeClient();

            boolean down = waitUntilStopped();
            if (!down) {
                logger.warn("VPN profile {} still running after SIGTERM, forcing stop", profile);
                down = backend.forceStop(profile);
            }
            if (down) {
                backend.clearMarker(profile);
            } else {
                logger.error("VPN profile {} could not be stopped", profile);
            }
            return down;
        } catch (RuntimeException e) {
            logger.error("Unexpected error while disconnecting VPN profile {}", profile, e);
            return false;
        }
    }

    private boolean waitUntilStopped() {
        long deadline = System.currentTimeMillis() + STOP_POLL;
        boolean interrupted = false;
        try {
            while (true) {
                VpnBackend.TunnelStatus status = backend.status(profile);
                if (status == VpnBackend.TunnelStatus.STOPPED) return true;
                if (status == VpnBackend.TunnelStatus.UNKNOWN || System.currentTimeMillis() >= deadline) return false;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    private void closeClient() {
        ManagementClient current = client;
        if (current != null) current.close();
    }

    @Override
    public boolean isAlive() {
        ManagementClient current = client;
        return current != null && !current.isClosed() && "CONNECTED".equals(current.getState());
    }

    @Override
    public boolean isExternal() {
        return external;
    }

    @Override
    public VpnStats getStats() {
        ManagementClient current = client;
        return current == null ? VpnStats.EMPTY : current.getStats();
    }

    @Override
    public String getDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Profile: ").append(profile).append('\n');
        sb.append("PID: ").append(readPid()).append('\n');
        ManagementClient current = client;
        if (current != null) {
            sb.append("State: ").append(current.getState()).append(' ').append(current.getStateDetail()).append('\n');
            sb.append("Management:\n");
            current.getRecentLines().forEach(line -> sb.append("  ").append(line).append('\n'));
        }
        sb.append("Log ").append(backend.logFile(profile)).append(":\n");
        tail(backend.logFile(profile)).forEach(line -> sb.append("  ").append(line).append('\n'));
        return sb.toString();
    }

    private String readPid() {
        try {
            return new String(Files.readAllBytes(backend.pidFile(profile).toPath()), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return "-";
        }
    }
}
