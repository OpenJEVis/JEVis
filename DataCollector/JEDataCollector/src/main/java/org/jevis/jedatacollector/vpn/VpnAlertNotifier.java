package org.jevis.jedatacollector.vpn;

/**
 * Receives VPN incidents which need human attention.
 */
public interface VpnAlertNotifier {

    VpnAlertNotifier NONE = new VpnAlertNotifier() {
        @Override
        public void raise(AlertType type, String profile, VpnConfig config, String details) {
        }

        @Override
        public void resolve(AlertType type, String profile, VpnConfig config, String details) {
        }
    };

    void raise(AlertType type, String profile, VpnConfig config, String details);

    /**
     * Signals that a condition alert no longer applies. Ignored if the alert is not active.
     */
    void resolve(AlertType type, String profile, VpnConfig config, String details);

    /**
     * Waits until queued alerts are delivered, used on shutdown.
     */
    default void flush(long timeoutMillis) {
    }

    enum AlertType {
        TEARDOWN_FAILED(true, true, "VPN connection could not be closed"),
        SESSION_TIMEOUT(true, false, "VPN session exceeded its maximum duration"),
        ORPHAN_FOUND(true, false, "Leftover VPN connection found after restart"),
        SHUTDOWN_FAILED(true, false, "VPN connection could not be closed on shutdown"),
        CONNECT_FAILED(false, true, "VPN connection could not be established");

        private final boolean critical;
        private final boolean condition;
        private final String title;

        AlertType(boolean critical, boolean condition, String title) {
            this.critical = critical;
            this.condition = condition;
            this.title = title;
        }

        /**
         * @return true if mails are sent with high priority
         */
        public boolean isCritical() {
            return critical;
        }

        /**
         * @return true if the alert describes a lasting state which is resolved later; false for one-time events
         */
        public boolean isCondition() {
            return condition;
        }

        public String getTitle() {
            return title;
        }
    }
}
