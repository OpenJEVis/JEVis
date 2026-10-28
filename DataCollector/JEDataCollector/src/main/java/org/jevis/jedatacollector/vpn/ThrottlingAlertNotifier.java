package org.jevis.jedatacollector.vpn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Prevents alert floods: per profile and alert type only the first occurrence is sent, followed by a
 * reminder per interval while the problem persists, and one all-clear message when a condition is resolved.
 */
public class ThrottlingAlertNotifier implements VpnAlertNotifier {

    private static final Logger logger = LogManager.getLogger(ThrottlingAlertNotifier.class);
    private final Sender sender;
    private final long reminderInterval;
    private final LongSupplier clock;
    private final Map<String, ActiveAlert> active = new HashMap<>();
    public ThrottlingAlertNotifier(Sender sender, long reminderIntervalMillis) {
        this(sender, reminderIntervalMillis, System::currentTimeMillis);
    }
    ThrottlingAlertNotifier(Sender sender, long reminderIntervalMillis, LongSupplier clock) {
        this.sender = sender;
        this.reminderInterval = reminderIntervalMillis;
        this.clock = clock;
    }

    private static String body(AlertType type, String profile, VpnConfig config, String details) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h3>").append(escape(type.getTitle())).append("</h3>");
        sb.append("<table>");
        row(sb, "Profile", profile);
        if (config != null) row(sb, "VPN Connection", config.getName());
        row(sb, "Type", type.name());
        row(sb, "Time", DateTime.now().toString());
        sb.append("</table>");
        sb.append("<pre>").append(escape(details)).append("</pre>");
        if (type.isCritical()) {
            sb.append("<p>Please check the server: <code>pgrep -a openvpn</code>, <code>sudo /usr/local/sbin/jevis-vpn stop ")
                    .append(escape(profile)).append("</code> (see DataCollector/JEDataCollector/docs/VPN.md).</p>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void row(StringBuilder sb, String key, String value) {
        sb.append("<tr><td><b>").append(escape(key)).append("</b></td><td>").append(escape(value)).append("</td></tr>");
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    public void raise(AlertType type, String profile, VpnConfig config, String details) {
        logger.error("VPN alert {} for profile {}: {}", type, profile, details);
        long now = clock.getAsLong();
        String key = type + "|" + profile;
        String subject;
        synchronized (active) {
            ActiveAlert alert = active.get(key);
            if (alert != null && now - alert.lastSent < reminderInterval) {
                alert.occurrences++;
                return;
            }
            if (alert == null || !type.isCondition()) {
                alert = new ActiveAlert(now);
                active.put(key, alert);
                subject = "[JEVis VPN] " + type.getTitle() + ": " + profile;
            } else {
                alert.occurrences++;
                alert.lastSent = now;
                subject = "[JEVis VPN] REMINDER - " + type.getTitle() + ": " + profile;
            }
            details = details + "\n\nFirst occurrence: " + new DateTime(alert.firstRaised)
                    + "\nOccurrences: " + alert.occurrences;
        }
        sender.send(subject, body(type, profile, config, details), type.isCritical(), config);
    }

    @Override
    public void resolve(AlertType type, String profile, VpnConfig config, String details) {
        ActiveAlert alert;
        synchronized (active) {
            alert = active.remove(type + "|" + profile);
        }
        if (alert == null || !type.isCondition()) return;
        logger.info("VPN alert {} for profile {} resolved: {}", type, profile, details);
        sender.send("[JEVis VPN] RESOLVED - " + type.getTitle() + ": " + profile,
                body(type, profile, config, details + "\n\nProblem started: " + new DateTime(alert.firstRaised)),
                false, config);
    }

    @Override
    public void flush(long timeoutMillis) {
        sender.flush(timeoutMillis);
    }

    public interface Sender {
        void send(String subject, String body, boolean priority, VpnConfig config);

        default void flush(long timeoutMillis) {
        }
    }

    private static class ActiveAlert {
        final long firstRaised;
        long lastSent;
        int occurrences = 1;

        ActiveAlert(long now) {
            this.firstRaised = now;
            this.lastSent = now;
        }
    }
}
