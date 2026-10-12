package org.jevis.commons.ws.sql.auth;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Locale;

/**
 * Authentication settings of the JEWebService, read from the {@code webservice.auth} branch of the config file.
 *
 * <pre>
 * &lt;webservice&gt;
 *     &lt;auth&gt;
 *         &lt;basic&gt;true&lt;/basic&gt;                     &lt;!-- legacy HTTP Basic auth, will be switched off --&gt;
 *         &lt;accesstimeout&gt;480&lt;/accesstimeout&gt;     &lt;!-- minutes, sliding --&gt;
 *         &lt;refreshtimeout&gt;30&lt;/refreshtimeout&gt;    &lt;!-- days, "remember me" --&gt;
 *         &lt;lockout&gt;
 *             &lt;threshold&gt;5&lt;/threshold&gt;
 *             &lt;minutes&gt;15&lt;/minutes&gt;
 *         &lt;/lockout&gt;
 *         &lt;mfa&gt;
 *             &lt;required&gt;none&lt;/required&gt;          &lt;!-- none | sysadmin | all --&gt;
 *             &lt;keyfile&gt;/home/jevis/jevis/etc/mfa.key&lt;/keyfile&gt;
 *         &lt;/mfa&gt;
 *     &lt;/auth&gt;
 * &lt;/webservice&gt;
 * </pre>
 */
public final class AuthConfig {

    private static final Logger logger = LogManager.getLogger(AuthConfig.class);
    private static boolean basicEnabled = true;
    private static int accessTimeoutMinutes = 8 * 60;
    private static int refreshTimeoutDays = 30;
    private static int lockoutThreshold = 5;
    private static int lockoutMinutes = 15;
    private static MfaPolicy mfaPolicy = MfaPolicy.NONE;
    private static String mfaKeyFile = System.getProperty("user.home") + File.separator + "jevis" + File.separator + "etc" + File.separator + "mfa.key";

    private AuthConfig() {
    }

    public static void read(XMLConfiguration config) {
        basicEnabled = Boolean.parseBoolean(config.getString("webservice.auth.basic", "true"));
        accessTimeoutMinutes = config.getInt("webservice.auth.accesstimeout", accessTimeoutMinutes);
        refreshTimeoutDays = config.getInt("webservice.auth.refreshtimeout", refreshTimeoutDays);
        lockoutThreshold = config.getInt("webservice.auth.lockout.threshold", lockoutThreshold);
        lockoutMinutes = config.getInt("webservice.auth.lockout.minutes", lockoutMinutes);
        mfaKeyFile = config.getString("webservice.auth.mfa.keyfile", mfaKeyFile);
        try {
            mfaPolicy = MfaPolicy.valueOf(config.getString("webservice.auth.mfa.required", "none").trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid value for webservice.auth.mfa.required, using 'none'");
            mfaPolicy = MfaPolicy.NONE;
        }
        logger.info("Auth config: basic={} accessTimeout={}min refreshTimeout={}d lockout={}x/{}min mfa={}",
                basicEnabled, accessTimeoutMinutes, refreshTimeoutDays, lockoutThreshold, lockoutMinutes, mfaPolicy);
        if (basicEnabled) {
            logger.warn("HTTP Basic authentication is enabled (webservice.auth.basic). It is deprecated and will be removed.");
        }
    }

    public static boolean isBasicEnabled() {
        return basicEnabled;
    }

    public static void setBasicEnabled(boolean basicEnabled) {
        AuthConfig.basicEnabled = basicEnabled;
    }

    public static int getAccessTimeoutMinutes() {
        return accessTimeoutMinutes;
    }

    public static int getRefreshTimeoutDays() {
        return refreshTimeoutDays;
    }

    public static int getLockoutThreshold() {
        return lockoutThreshold;
    }

    public static int getLockoutMinutes() {
        return lockoutMinutes;
    }

    public static MfaPolicy getMfaPolicy() {
        return mfaPolicy;
    }

    public static void setMfaPolicy(MfaPolicy mfaPolicy) {
        AuthConfig.mfaPolicy = mfaPolicy;
    }

    public static String getMfaKeyFile() {
        return mfaKeyFile;
    }

    public static void setMfaKeyFile(String mfaKeyFile) {
        AuthConfig.mfaKeyFile = mfaKeyFile;
    }

    /**
     * Who has to use two-factor authentication.
     */
    public enum MfaPolicy {
        NONE, SYSADMIN, ALL
    }
}
