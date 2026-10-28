package org.jevis.jedatacollector.vpn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jenotifier.notifier.Email.EmailNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Sends VPN alerts by e-mail, configured like JEStatus/JEAlarm: the JEDataCollector service object
 * holds the "Notification ID" of the mail driver object, the recipients come from an "E-Mail Notification"
 * child of the VPN Connection object or, as fallback, of the service object.
 */
public class EmailVpnAlertSender implements ThrottlingAlertNotifier.Sender {

    public static final String NOTIFICATION_ID = "Notification ID";
    private static final Logger logger = LogManager.getLogger(EmailVpnAlertSender.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 30000;

    private final JEVisDataSource ds;
    private final JEVisObject serviceObject;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "vpn-alert-mail");
        thread.setDaemon(true);
        return thread;
    });

    public EmailVpnAlertSender(JEVisDataSource ds, JEVisObject serviceObject) {
        this.ds = ds;
        this.serviceObject = serviceObject;
    }

    @Override
    public void send(String subject, String body, boolean priority, VpnConfig config) {
        executor.submit(() -> {
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                try {
                    if (deliver(subject, body, priority, config)) return;
                } catch (Exception e) {
                    logger.error("Could not send VPN alert '{}' (attempt {})", subject, attempt, e);
                }
                if (attempt < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            logger.error("VPN alert '{}' could not be delivered", subject);
        });
    }

    private boolean deliver(String subject, String body, boolean priority, VpnConfig config) throws JEVisException {
        if (serviceObject == null) {
            logger.warn("No JEDataCollector service object, VPN alert not mailed: {}", subject);
            return true;
        }
        Long mailId = new SampleHandler().getLastSample(serviceObject, NOTIFICATION_ID, (Long) null);
        JEVisObject driverObject = mailId != null ? ds.getObject(mailId) : null;
        JEVisObject notificationObject = findNotificationObject(config);
        if (driverObject == null || notificationObject == null) {
            logger.warn("VPN alert e-mail not configured (Notification ID on {} / E-Mail Notification child missing), not mailed: {}",
                    serviceObject.getName(), subject);
            return true;
        }

        EmailNotification notification = new EmailNotification();
        notification.setNotificationObject(notificationObject);
        notification.setIsHTML(true);
        notification.setPriority(priority);
        notification.setSubject(subject);

        EmailNotificationDriver driver = new EmailNotificationDriver();
        driver.setNotificationDriverObject(driverObject);
        if (!driver.isDriverConfigured() || !notification.isNotiConfigured()) {
            logger.warn("Mail driver {} or notification {} is not configured completely, VPN alert not mailed: {}",
                    driverObject.getID(), notificationObject.getID(), subject);
            return true;
        }
        boolean sent = driver.sendNotification(notification, body);
        if (sent) logger.info("VPN alert mailed: {}", subject);
        return sent;
    }

    private JEVisObject findNotificationObject(VpnConfig config) throws JEVisException {
        JEVisClass notificationClass = ds.getJEVisClass(EmailNotification._type);
        if (config != null && config.getObject() != null) {
            List<JEVisObject> children = config.getObject().getChildren(notificationClass, true);
            if (!children.isEmpty()) return children.get(0);
        }
        List<JEVisObject> children = serviceObject.getChildren(notificationClass, true);
        return children.isEmpty() ? null : children.get(0);
    }

    @Override
    public void flush(long timeoutMillis) {
        try {
            executor.submit(() -> {
            }).get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("Pending VPN alert mails not delivered within {} ms", timeoutMillis);
        }
    }
}
