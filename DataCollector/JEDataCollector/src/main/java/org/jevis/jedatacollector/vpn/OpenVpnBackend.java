package org.jevis.jedatacollector.vpn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controls OpenVPN processes through the root-owned wrapper script (bin/jevis-vpn) which the
 * collector user may run via sudo. The wrapper starts OpenVPN with a management unix socket in the
 * run directory; everything else is done over that socket.
 * <p>
 * Paths can be overridden with the system properties jevis.vpn.wrapper, jevis.vpn.runDir and jevis.vpn.logDir.
 */
public class OpenVpnBackend implements VpnBackend {

    static final int EXIT_NOT_RUNNING = 3;
    static final String MARKER_SUFFIX = ".collector";
    private static final Logger logger = LogManager.getLogger(OpenVpnBackend.class);
    private static final long COMMAND_TIMEOUT = 30000;
    private final CommandRunner runner;
    private final String wrapper;
    private final File runDir;
    private final File logDir;

    public OpenVpnBackend() {
        this(new CommandRunner.ProcessCommandRunner(),
                System.getProperty("jevis.vpn.wrapper", "/usr/local/sbin/jevis-vpn"),
                new File(System.getProperty("jevis.vpn.runDir", "/run/jevis-vpn")),
                new File(System.getProperty("jevis.vpn.logDir", "/var/log/jevis-vpn")));
    }

    public OpenVpnBackend(CommandRunner runner, String wrapper, File runDir, File logDir) {
        this.runner = runner;
        this.wrapper = wrapper;
        this.runDir = runDir;
        this.logDir = logDir;
    }

    @Override
    public VpnConnector createConnector(String profile) {
        return new OpenVpnConnector(this, profile);
    }

    CommandRunner.Result wrapper(String action, String profile) {
        List<String> command = Arrays.asList("sudo", "-n", wrapper, action, profile);
        CommandRunner.Result result = runner.run(command, COMMAND_TIMEOUT);
        logger.debug("{} -> {}", command, result);
        return result;
    }

    @Override
    public TunnelStatus status(String profile) {
        CommandRunner.Result result = wrapper("status", profile);
        if (result.getExitCode() == 0) return TunnelStatus.RUNNING;
        if (result.getExitCode() == EXIT_NOT_RUNNING) return TunnelStatus.STOPPED;
        logger.warn("Could not query status of VPN profile {}: {}", profile, result);
        return TunnelStatus.UNKNOWN;
    }

    @Override
    public boolean forceStop(String profile) {
        try {
            CommandRunner.Result result = wrapper("stop", profile);
            if (!result.isSuccess()) {
                logger.warn("Stopping VPN profile {} failed: {}", profile, result);
            }
            return status(profile) == TunnelStatus.STOPPED;
        } catch (RuntimeException e) {
            logger.error("Unexpected error while stopping VPN profile {}", profile, e);
            return false;
        }
    }

    @Override
    public List<String> findOrphanedSessions() {
        List<String> profiles = new ArrayList<>();
        File[] markers = runDir.listFiles((dir, name) -> name.endsWith(MARKER_SUFFIX));
        if (markers == null) return profiles;
        for (File marker : markers) {
            String profile = marker.getName().substring(0, marker.getName().length() - MARKER_SUFFIX.length());
            if (VpnConfig.PROFILE_PATTERN.matcher(profile).matches()) profiles.add(profile);
        }
        return profiles;
    }

    void writeMarker(String profile) throws VpnException {
        String content = ManagementFactory.getRuntimeMXBean().getName() + " " + System.currentTimeMillis() + "\n";
        try {
            Files.write(markerFile(profile).toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new VpnException("Cannot write session marker in " + runDir + " (check directory permissions): " + e.getMessage(), e);
        }
    }

    @Override
    public void clearMarker(String profile) {
        File marker = markerFile(profile);
        if (marker.exists() && !marker.delete()) {
            logger.warn("Could not delete VPN session marker {}", marker);
        }
    }

    private File markerFile(String profile) {
        return new File(runDir, profile + MARKER_SUFFIX);
    }

    File socketFile(String profile) {
        return new File(runDir, profile + ".sock");
    }

    File pidFile(String profile) {
        return new File(runDir, profile + ".pid");
    }

    File logFile(String profile) {
        return new File(logDir, profile + ".log");
    }

    ManagementClient openManagement(String profile) throws IOException {
        AFUNIXSocket socket = AFUNIXSocket.newInstance();
        socket.connect(AFUNIXSocketAddress.of(socketFile(profile)));
        return new ManagementClient(socket.getInputStream(), socket.getOutputStream(), socket);
    }
}
