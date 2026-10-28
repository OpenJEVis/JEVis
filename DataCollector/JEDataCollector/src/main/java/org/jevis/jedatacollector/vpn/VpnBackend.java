package org.jevis.jedatacollector.vpn;

import java.util.List;

/**
 * System side of the VPN handling: creates connectors and controls tunnel processes per profile.
 */
public interface VpnBackend {

    VpnConnector createConnector(String profile);

    /**
     * @return profiles for which a session started by the collector was never closed cleanly
     * (leftovers of a crash or kill -9)
     */
    List<String> findOrphanedSessions();

    TunnelStatus status(String profile);

    /**
     * Stops the tunnel process of the profile (SIGTERM, then SIGKILL). Must never throw.
     *
     * @return true if it is verified that the process is gone
     */
    boolean forceStop(String profile);

    void clearMarker(String profile);

    enum TunnelStatus {
        RUNNING,
        STOPPED,
        /**
         * The status could not be determined, e.g. because sudo or the wrapper is not set up.
         */
        UNKNOWN
    }
}
