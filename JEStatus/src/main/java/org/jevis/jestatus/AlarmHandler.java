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

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.joda.time.DateTime;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This Class handels the logic and the sending of the alarms.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AlarmHandler {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmHandler.class);

    private JEVisDataSource _ds;
    private Config _conf;

    /**
     * Create an AlarmHandler for the given Configuration
     *
     * @param conf
     */
    public AlarmHandler(Config conf, JEVisDataSource ds) {
        _conf = conf;
        _ds = ds;
    }

    /**
     * Get an List of all datapoints configured in this alrms. If the parameter
     * "<datapoint>" is emty or missing it will return all "Data" Objects
     *
     * @param alarm
     * @return
     * @throws JEVisException
     */
    private List<JEVisObject> getDataPoints(Alarm alarm) throws JEVisException {
        List<JEVisObject> dps = new ArrayList<>();

        if (alarm.getDatapoint() != null && alarm.getDatapoint().length > 0) {
            for (String dp : alarm.getDatapoint()) {
                try {
                    JEVisObject obj = _ds.getObject(Long.parseLong(dp));
                    if (obj != null && obj.getJEVisClass().getName().equals("Data")) {
                        logger.info("Add dP: " + obj.getName());
                        dps.add(obj);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            dps.addAll(_ds.getObjects(_ds.getJEVisClass("Data"), false));

        }

        return dps;
    }

    /**
     * Checks the Data Objects for alarms and send an email.
     *
     * @param alarm
     * @throws JEVisException
     */
    public void checkAlarm(Alarm alarm) throws JEVisException {

        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append(alarm.getGreeting());
        sb.append(",");
        sb.append("<br>");
        sb.append("<br>");
        sb.append(alarm.getMessage());
        sb.append("<br>");
        sb.append("<br>");

        DataServerTable dataServerTable = new DataServerTable(_ds, alarm);
        sb.append(dataServerTable.getTableString());

        CalculationTable calculationTable = new CalculationTable(_ds, alarm, dataServerTable.getListCheckedData());
        sb.append(calculationTable.getTableString());

        CleanDataTable cleanDataTable = new CleanDataTable(_ds, alarm, calculationTable.getListCheckedData(), dataServerTable.getListCheckedData());
        sb.append(cleanDataTable.getTableString());

        sb.append(_conf.getSmtpSignatur());
        sb.append("</html>");

        if (alarm.isIgnoreFalse()) {
            //Do nothing then
        } else {
            logToServiceObject(sb.toString());
            if (isEMailEnabled()) {
                sendAlarm(_conf, alarm, sb.toString());
            }

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
     * Returns the name of parent Object from the given JEVisClass. If the
     * Parent does not exist the name will be emty
     * <p>
     * TODO: make an check for an endless loop
     *
     * @param obj
     * @param jclass
     * @return
     */
    private String getParentName(JEVisObject obj, JEVisClass jclass) {
        StringBuilder name = new StringBuilder();
        try {

            for (JEVisObject parent : obj.getParents()) {
//                if (parent.getJEVisClass().equals(jclass)) {
                if (isJEVisClassOrInherit(parent, jclass)) {
                    name.append(parent.getName());
                } else {
                    name.append(getParentName(parent, jclass));
                }
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        return name.toString();
    }

    /**
     * Send the Alarm mail
     *
     * @param conf
     * @param alarm
     * @param body
     */
    private void sendAlarm(Config conf, Alarm alarm, String body) {
        try {

            Properties props = System.getProperties();

            props.put("mail.smtp.host", conf.getSmtpServer());

            Session session;
            if (conf.isSmtpStartTLS()) {
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
            } else if (conf.isSmtpSSL()) {
                props.put("mail.smtp.socketFactory.port", conf.getSmtpPort()); //SSL Port
                props.put("mail.smtp.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
                props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
                props.put("mail.smtp.port", conf.getSmtpPort()); //SMTP Port

                Authenticator auth = new Authenticator() {
                    //override the getPasswordAuthentication method
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(conf.getSmtpUser(), conf.getSmtpPW());
                    }
                };

                session = Session.getDefaultInstance(props, auth);

            } else session = Session.getInstance(props, null);

            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(conf.smtpFrom, "NoReply"));

            msg.setReplyTo(InternetAddress.parse(conf.getSmtpFrom(), false));

            msg.setSubject(alarm.getSubject(), "UTF-8");

            msg.setContent(body, "text/html; charset=UTF-8");

            msg.setSentDate(new Date());

            for (String recipient : alarm.getRecipient()) {
                InternetAddress address = new InternetAddress();
                address.setAddress(recipient);
                msg.addRecipient(Message.RecipientType.TO, address);
            }

            Transport.send(msg);
        } catch (Exception ex) {
            logger.info("could not send Email: " + ex);
        }

    }

}
