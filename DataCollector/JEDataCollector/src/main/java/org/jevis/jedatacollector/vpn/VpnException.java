package org.jevis.jedatacollector.vpn;

/**
 * Raised when a VPN tunnel could not be configured, established or verified.
 */
public class VpnException extends Exception {

    public VpnException(String message) {
        super(message);
    }

    public VpnException(String message, Throwable cause) {
        super(message, cause);
    }
}
