package org.jevis.jedatacollector.vpn;

/**
 * One tunnel session for one profile. Instances are not reused after {@link #disconnect()}.
 */
public interface VpnConnector {

    /**
     * Establishes the tunnel and blocks until it is usable.
     *
     * @throws VpnException if the tunnel could not be established within the configured timeout
     */
    void connect(VpnConfig config) throws VpnException, InterruptedException;

    /**
     * Closes the tunnel. Must never throw and must work on an interrupted thread.
     *
     * @return true if it is verified that the tunnel process is gone
     */
    boolean disconnect();

    /**
     * @return true while the tunnel process is running and connected
     */
    boolean isAlive();

    /**
     * @return true if the tunnel was already running when connecting (e.g. started manually).
     * Such tunnels are used but never closed by the collector.
     */
    boolean isExternal();

    VpnStats getStats();

    /**
     * @return human readable details for logs and alert mails (pid, state, last log lines)
     */
    String getDiagnostics();
}
