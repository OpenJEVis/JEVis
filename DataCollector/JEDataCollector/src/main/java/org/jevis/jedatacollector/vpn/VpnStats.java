package org.jevis.jedatacollector.vpn;

/**
 * Traffic counters of a tunnel session as reported by the OpenVPN management interface.
 */
public class VpnStats {

    public static final VpnStats EMPTY = new VpnStats(0, 0);

    private final long bytesIn;
    private final long bytesOut;

    public VpnStats(long bytesIn, long bytesOut) {
        this.bytesIn = bytesIn;
        this.bytesOut = bytesOut;
    }

    public long getBytesIn() {
        return bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    @Override
    public String toString() {
        return "in=" + bytesIn + " B, out=" + bytesOut + " B";
    }
}
