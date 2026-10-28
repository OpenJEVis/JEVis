package org.jevis.jedatacollector.vpn;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.object.plugin.TargetHelper;

import java.util.regex.Pattern;

/**
 * Settings of a "VPN Connection" object. Time values are configured in seconds.
 */
public class VpnConfig {

    public static final Pattern PROFILE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    static final long DEFAULT_CONNECT_TIMEOUT = 60;
    static final long DEFAULT_MAX_RETRIES = 2;
    static final long DEFAULT_RETRY_DELAY = 30;
    static final long DEFAULT_IDLE_TIMEOUT = 10;
    static final long DEFAULT_MAX_SESSION_TIME = 1800;

    private final JEVisObject object;
    private final String name;
    private final String profile;
    private String user;
    private String password;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int maxRetries = (int) DEFAULT_MAX_RETRIES;
    private long retryDelay = DEFAULT_RETRY_DELAY;
    private String checkHost;
    private int checkPort;
    private long idleTimeout = DEFAULT_IDLE_TIMEOUT;
    private long maxSessionTime = DEFAULT_MAX_SESSION_TIME;
    private boolean notifyOnConnectError = false;

    public VpnConfig(JEVisObject object, String name, String profile) throws VpnException {
        if (profile == null || !PROFILE_PATTERN.matcher(profile).matches()) {
            throw new VpnException("Invalid VPN profile name '" + profile + "' (allowed: A-Z, a-z, 0-9, '_', '-')");
        }
        this.object = object;
        this.name = name;
        this.profile = profile;
    }

    /**
     * Returns the VPN configuration referenced by the given data source, or null if the
     * data source does not use a VPN or the referenced VPN Connection is disabled.
     */
    public static VpnConfig forDataSource(JEVisObject dataSource) throws VpnException {
        JEVisObject vpnObject;
        try {
            JEVisAttribute attribute = dataSource.getAttribute(DataCollectorTypes.DataSource.VPN_CONNECTION);
            if (attribute == null) return null;
            JEVisSample sample = attribute.getLatestSample();
            if (sample == null || sample.getValueAsString() == null || sample.getValueAsString().trim().isEmpty()) {
                return null;
            }

            TargetHelper targetHelper = new TargetHelper(dataSource.getDataSource(), attribute);
            if (!targetHelper.isValid() || !targetHelper.targetObjectAccessible() || targetHelper.getObject().isEmpty()) {
                throw new VpnException("VPN Connection target '" + sample.getValueAsString() + "' of data source "
                        + dataSource.getID() + " is invalid or not accessible");
            }
            vpnObject = targetHelper.getObject().get(0);
        } catch (JEVisException e) {
            throw new VpnException("Could not read VPN Connection of data source " + dataSource.getID(), e);
        }

        SampleHandler sampleHandler = new SampleHandler();
        if (!sampleHandler.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.ENABLED, false)) {
            return null;
        }
        return fromObject(vpnObject);
    }

    public static VpnConfig fromObject(JEVisObject vpnObject) throws VpnException {
        SampleHandler sh = new SampleHandler();
        String profile = sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.PROFILE, "");
        VpnConfig config = new VpnConfig(vpnObject, vpnObject.getName() + ":" + vpnObject.getID(), profile.trim());

        config.user = emptyToNull(sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.USER, ""));
        config.password = emptyToNull(sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.PASSWORD, ""));
        config.connectTimeout = positive(sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT), DEFAULT_CONNECT_TIMEOUT);
        config.maxRetries = (int) Math.max(0, sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.MAX_RETRIES, DEFAULT_MAX_RETRIES));
        config.retryDelay = Math.max(0, sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.RETRY_DELAY, DEFAULT_RETRY_DELAY));
        config.checkHost = emptyToNull(sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.CHECK_HOST, ""));
        config.checkPort = sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.CHECK_PORT, 0L).intValue();
        config.idleTimeout = Math.max(0, sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.IDLE_TIMEOUT, DEFAULT_IDLE_TIMEOUT));
        config.maxSessionTime = positive(sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.MAX_SESSION_TIME, DEFAULT_MAX_SESSION_TIME), DEFAULT_MAX_SESSION_TIME);
        config.notifyOnConnectError = sh.getLastSample(vpnObject, DataCollectorTypes.VpnConnection.NOTIFY_ON_CONNECT_ERROR, false);
        return config;
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static long positive(Long value, long defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    public JEVisObject getObject() {
        return object;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public String getCheckHost() {
        return checkHost;
    }

    public int getCheckPort() {
        return checkPort;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public long getMaxSessionTime() {
        return maxSessionTime;
    }

    public boolean isNotifyOnConnectError() {
        return notifyOnConnectError;
    }

    public VpnConfig setNotifyOnConnectError(boolean notifyOnConnectError) {
        this.notifyOnConnectError = notifyOnConnectError;
        return this;
    }

    public VpnConfig setCredentials(String user, String password) {
        this.user = user;
        this.password = password;
        return this;
    }

    public VpnConfig setTimeouts(long connectTimeout, int maxRetries, long retryDelay, long idleTimeout, long maxSessionTime) {
        this.connectTimeout = connectTimeout;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.idleTimeout = idleTimeout;
        this.maxSessionTime = maxSessionTime;
        return this;
    }

    public VpnConfig setCheck(String checkHost, int checkPort) {
        this.checkHost = checkHost;
        this.checkPort = checkPort;
        return this;
    }
}
