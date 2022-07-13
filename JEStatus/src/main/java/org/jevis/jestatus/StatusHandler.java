/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of Launcher.
 * <p>
 * Launcher is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * Launcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Launcher. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Launcher is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jestatus;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.jenotifier.mode.SendNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;
import org.jevis.jenotifier.notifier.Email.EmailServiceProperty;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This Class handles the logic and the sending of the status.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class StatusHandler {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(StatusHandler.class);
    private final DateTime now;
    private Long latestReported;
    private JEVisDataSource _ds;
    private Config _conf;
    private JEVisObject notificationObject;

    /**
     * Create an StatusHandler for the given Configuration
     *
     * @param latestReported
     */
    public StatusHandler(JEVisDataSource ds, Long latestReported) {
        _ds = ds;
        this.latestReported = latestReported;
        this.now = DateTime.now();
    }

    public StatusHandler() {
        this.now = DateTime.now();
    }

    /**
     * Checks the Data Objects for alarms and send an email.
     *
     * @param
     * @throws JEVisException
     */
    public void checkStatus() throws JEVisException {

        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
//        sb.append(alarm.getGreeting());
//        sb.append(",");
//        sb.append("<br>");
//        sb.append("<br>");
//        sb.append(alarm.getMessage());

        AutoMySQLBackupTable autoMySQLBackupTable = new AutoMySQLBackupTable(_ds, getLatestReported());
        sb.append(autoMySQLBackupTable.getTableString());


        ServiceStatus serviceStatus = new ServiceStatus(_ds);
        sb.append(serviceStatus.getTableString());

        try {
            WirelessLogicStatus wirelessLogicStatus = new WirelessLogicStatus(_ds, getTariff(), getUsername(), getPassword());
            sb.append(wirelessLogicStatus.getTableString());
        } catch (Exception e) {
            logger.error("Could not generate wireless logic status", e);
        }

        DataServerTable dataServerTable = null;
        try {
            dataServerTable = new DataServerTable(_ds, getLatestReported());
            sb.append(dataServerTable.getTableString());
        } catch (Exception e) {
            logger.error("Could not generate data server table", e);
        }

        ReportTable reportTable = null;
        try {
            reportTable = new ReportTable(_ds, getLatestReported());
            sb.append(reportTable.getTableString());
        } catch (Exception e) {
            logger.error("Could not generate report table", e);
        }

        CalculationTable calculationTable = null;
        if (dataServerTable != null) {
            try {
                calculationTable = new CalculationTable(_ds, getLatestReported(), dataServerTable.getListCheckedData());
                sb.append(calculationTable.getTableString());
            } catch (Exception e) {
                logger.error("Could not generate calculation table", e);
            }
        }

        if (calculationTable != null) {
            try {
                CleanDataTable cleanDataTable = new CleanDataTable(_ds, getLatestReported(), calculationTable.getListCheckedData(), dataServerTable.getListCheckedData());
                sb.append(cleanDataTable.getTableString());
            } catch (Exception e) {
                logger.error("Could not generate clean data table", e);
            }
        }

        sb.append("</html>");

        logToServiceObject(sb.toString());
        logFileToServiceObject(sb.toString());

        if (isEMailEnabled()) {
            logger.info("E-Mail is enabled. Initializing...");

            initializeNotification();

            logger.info("Initialized Notification. Sending...");

            sendNotification(notificationObject, sb.toString());

            logger.info("Sent notification.");

//                sendAlarm(_conf, alarm, sb.toString());

        }

    }

    private void sendNotification(JEVisObject notificationObject, String customMessage) {
        try {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            EmailServiceProperty service = getStatusService();

            EmailNotification nofi = new EmailNotification();
            nofi.setNotificationObject(notificationObject);
            nofi.setIsHTML(true);

            JEVisObject notiDriObj = notificationObject.getDataSource().getObject(service.getMailID());
            EmailNotificationDriver emailNofi = new EmailNotificationDriver();
            emailNofi.setNotificationDriverObject(notiDriObj);

            SendNotification sn = new SendNotification(nofi, emailNofi, customMessage);
            executorService.submit(sn);

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private EmailServiceProperty getStatusService() {

        EmailServiceProperty service = new EmailServiceProperty();
        try {
            JEVisClass jeVisClass = _ds.getJEVisClass("JEStatus");
            List<JEVisObject> statusServices = _ds.getObjects(jeVisClass, true);
            if (statusServices.size() == 1) {
                service.initialize(statusServices.get(0));
            }
        } catch (JEVisException ex) {
            logger.error("error while getting status service", ex);
        }
        return service;
    }

    private void initializeNotification() {
        try {
            JEVisClass jEStatusClass = _ds.getJEVisClass("JEStatus");
            List<JEVisObject> jEStatusObjects = _ds.getObjects(jEStatusClass, true);
            if (!jEStatusObjects.isEmpty()) {
                JEVisClass notificationType = _ds.getJEVisClass("E-Mail Notification");
                List<JEVisObject> notificationObjects = jEStatusObjects.get(0).getChildren(notificationType, true);
                if (notificationObjects.size() == 1) {
                    notificationObject = notificationObjects.get(0);
                } else {
                    throw new IllegalStateException("Too many or no Notification Object for report Object: id: " + jEStatusObjects.get(0).getID() + " and name: " + jEStatusObjects.get(0).getName());
                }
            } else {
                throw new IllegalStateException("No JEStatus Objects found.");
            }

        } catch (JEVisException ex) {
            throw new RuntimeException("Error while parsing Notification Object for JEStatus", ex);
        }
    }

    private void logToServiceObject(String log) throws JEVisException {
        JEVisClass statusClass = _ds.getJEVisClass("JEStatus");
        List<JEVisObject> statusObjects = _ds.getObjects(statusClass, true);
        for (JEVisObject object : statusObjects) {

            JEVisAttribute emailEnabledAttribute = object.getAttribute("Status Log");
            JEVisSample newLog = emailEnabledAttribute.buildSample(DateTime.now(), log);
            newLog.commit();
        }
    }

    private void logFileToServiceObject(String log) throws JEVisException {
        JEVisClass statusClass = _ds.getJEVisClass("JEStatus");
        List<JEVisObject> statusObjects = _ds.getObjects(statusClass, true);
        for (JEVisObject object : statusObjects) {

            JEVisAttribute statusFileLogAttribute = object.getAttribute("Status File Log");
            try {
                JEVisFile file = new JEVisFileImp(DateTime.now().toString("yyyyMMdd_HHmmss") + ".html", log.getBytes(StandardCharsets.UTF_8));
                JEVisSample newLog = statusFileLogAttribute.buildSample(DateTime.now(), file);
                newLog.commit();
            } catch (Exception e) {
                logger.error("Could not create file", e);
            }
        }
    }

    private boolean isEMailEnabled() throws JEVisException {
        JEVisClass statusClass = _ds.getJEVisClass("JEStatus");
        List<JEVisObject> statusObjects = _ds.getObjects(statusClass, true);
        for (JEVisObject object : statusObjects) {

            JEVisAttribute emailEnabledAttribute = object.getAttribute("Status E-Mail");
            if (emailEnabledAttribute != null) {
                JEVisSample lastSample = emailEnabledAttribute.getLatestSample();
                if (lastSample != null) {
                    return lastSample.getValueAsBoolean();
                }
            }
        }
        return false;
    }

    /**
     * Returns if the given Object is from the given JEVisClass or an heir
     *
     * @param obj
     * @param jclass2
     * @return
     */
    private boolean isJEVisClassOrInherit(JEVisObject obj, JEVisClass jclass2) {
        try {
            return isJEVisClassOrInherit(obj.getJEVisClass(), jclass2);
        } catch (JEVisException ex) {
            logger.error(ex);
            return false;
        }
    }

    /**
     * return if the JEVisClass is from the given JEVisClass or its heir
     *
     * @param jclass  Class to check
     * @param jclass2 Class which the other class should hier from
     * @return
     */
    private boolean isJEVisClassOrInherit(JEVisClass jclass, JEVisClass jclass2) {
        try {
            if (jclass.equals(jclass2)) {
                return true;
            }

            return jclass2.getHeirs().contains(jclass);

        } catch (JEVisException ex) {
            logger.error(ex);
            return false;
        }

    }

    /**
     * Send the Status mail
     *
     * @param conf
     * @param body
     */
    public void sendAlarm(Config conf, String body) {
        try {

            Properties props = System.getProperties();

            props.put("mail.smtp.host", conf.getSmtpServer());

            Session session;
            props.put("mail.smtp.port", conf.getSmtpPort()); //TLS Port
            props.put("mail.smtp.auth", "true"); //enable authentication
            props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

            //create Authenticator object to pass in Session.getInstance argument
            Authenticator auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(conf.getSmtpUser(), conf.getSmtpPW());
                }
            };
            session = Session.getInstance(props, auth);

            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.addHeader("X-Priority", "2");

            msg.setFrom(new InternetAddress(conf.smtpFrom, "NoReply"));

            msg.setReplyTo(InternetAddress.parse(conf.getSmtpFrom(), false));

            msg.setSubject(conf.getSubject(), "UTF-8");

            msg.setContent(conf.getGreeting() + "\n" + conf.getMessage(), "text/html; charset=UTF-8");

            msg.setSentDate(new Date());

            for (String recipient : conf.getRecipient()) {
                InternetAddress address = new InternetAddress();
                address.setAddress(recipient);
                msg.addRecipient(Message.RecipientType.TO, address);
            }

            Transport.send(msg);
        } catch (Exception ex) {
            logger.info("could not send Email: ", ex);
        }

    }

    private DateTime getLatestReported() {
        return now.minus(Period.hours(latestReported.intValue()));
    }

    private List<String> getTariff() {
        List<String> tariffs = new ArrayList<>();
        try {
            JEVisClass jEStatusClass = _ds.getJEVisClass("JEStatus");
            List<JEVisObject> jEStatusObjects = _ds.getObjects(jEStatusClass, true);
            if (jEStatusObjects.size() > 0) {
                JEVisAttribute tariffsAttribute = jEStatusObjects.get(0).getAttribute("Tariffs");
                if (tariffsAttribute.hasSample()) {
                    String tariff = tariffsAttribute.getLatestSample().getValueAsString();

                    tariffs.addAll(Arrays.asList(tariff.split(";")));
                }
            }
        } catch (Exception e) {
            logger.error("Could not get tariffs from JEStatus", e);
        }
        return tariffs;
    }

    private String getUsername() {
        try {
            JEVisClass jEStatusClass = _ds.getJEVisClass("JEStatus");
            List<JEVisObject> jEStatusObjects = _ds.getObjects(jEStatusClass, true);
            if (jEStatusObjects.size() > 0) {
                JEVisAttribute userAttribute = jEStatusObjects.get(0).getAttribute("User");
                if (userAttribute.hasSample()) {
                    return userAttribute.getLatestSample().getValueAsString();
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            logger.error("Could not get username for tariff check from JEStatus", e);
        }
        return "";
    }

    private String getPassword() {
        try {
            JEVisClass jEStatusClass = _ds.getJEVisClass("JEStatus");
            List<JEVisObject> jEStatusObjects = _ds.getObjects(jEStatusClass, true);
            if (jEStatusObjects.size() > 0) {
                JEVisAttribute passwordAttribute = jEStatusObjects.get(0).getAttribute("Password");
                if (passwordAttribute.hasSample()) {
                    return passwordAttribute.getLatestSample().getValueAsString();
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            logger.error("Could not get password for tariff check from JEStatus", e);
        }
        return "";
    }


}
