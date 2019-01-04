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
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
        List<JEVisObject> outOfBounds = new ArrayList<>();
        List<JEVisObject> dps = getDataPoints(alarm);

        DateTime now = new DateTime();
        DateTime ignoreTS = now.minus(Period.hours(alarm.getIgnoreOld()));
        DateTime limit = now.minus(Period.hours(alarm.getTimeLimit()));

        for (JEVisObject obj : dps) {
            JEVisSample lastSample = obj.getAttribute("Value").getLatestSample();
            if (lastSample != null) {
                if (lastSample.getTimestamp().isBefore(limit) && lastSample.getTimestamp().isAfter(ignoreTS)) {
                    outOfBounds.add(obj);
                }
            }
        }
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append(alarm.getGreeting());
        sb.append(",");
        sb.append("<br>");
        sb.append("<br>");
        sb.append(alarm.getMessage());
        sb.append("<br>");
        sb.append("<br>");

        String tabelCSS = "background-color:#FFF;"
                + "text-color: #024457;"
                + "outer-border: 1px solid #167F92;"
                + "empty-cells:show;"
                + "border-collapse:collapse;"
                //                + "border: 2px solid #D9E4E6;"
                + "cell-border: 1px solid #D9E4E6";

        String headerCSS = "background-color: #1a719c;"
                + "color: #FFF;";

        String rowCss = "text-color: #024457;padding: 5px;";//"border: 1px solid #D9E4E6;"

        String highlight = "background-color: #EAF3F3";

        sb.append("<table style=\"");
        sb.append(tabelCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>Organisation</th>");
        sb.append("    <th>Building</th>");
        sb.append("    <th>Datapoint</th>");
        sb.append("    <th>Datapoint Class</th>");
        sb.append("    <th>Last Value</th>");
        sb.append("  </tr>");//border=\"0\"

        JEVisClass orga = _ds.getJEVisClass("Organization");
        JEVisClass building = _ds.getJEVisClass("Monitored Object");
        JEVisClass dir = _ds.getJEVisClass("Data Directory");
        JEVisClass cleanData = _ds.getJEVisClass("Clean Data");

        boolean odd = false;
        for (JEVisObject currentDataPoint : outOfBounds) {
            String name = currentDataPoint.getName();

            boolean hasCleanDataObject = false;
            JEVisObject currentCleanDataObject = null;
            for (JEVisObject child : currentDataPoint.getChildren()) {
                try {
                    JEVisClass childClass = child.getJEVisClass();
                    if (childClass != null && childClass.equals(cleanData)) {
                        hasCleanDataObject = true;
                        currentCleanDataObject = child;
                        break;
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }

            String css = rowCss;
            if (odd) {
                css += highlight;
            }
            odd = !odd;

            sb.append("<tr>");
            /**
             * Organisation Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentDataPoint, orga));
            sb.append("</td>");
            /**
             * Building Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentDataPoint, building));
            sb.append("</td>");
            /**
             * Datapoint Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(name);
            if (hasCleanDataObject) {
                sb.append(" (");
                sb.append(currentCleanDataObject.getName());
                sb.append(")");
            }
            sb.append("</td>");
            /**
             * Datapoint Class Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            if (!hasCleanDataObject) {
                sb.append(currentDataPoint.getJEVisClass().getName());
            } else {
                sb.append(currentCleanDataObject.getJEVisClass().getName());
            }
            sb.append("</td>");
            /**
             * Last Value Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            if (!hasCleanDataObject) {
                sb.append(dtf.print(currentDataPoint.getAttribute("Value").getLatestSample().getTimestamp()));
            } else {
                JEVisSample smp = currentCleanDataObject.getAttribute("Value").getLatestSample();
                if (smp != null) {
                    sb.append(dtf.print(smp.getTimestamp()));
                } else {
                    sb.append(dtf.print(currentDataPoint.getAttribute("Value").getLatestSample().getTimestamp()));
                    sb.append(", but no Clean Data values");
                }
            }
            sb.append("</td>");
            sb.append("</tr>");// style=\"border: 1px solid #D9E4E6;\">");

        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append(_conf.getSmtpSignatur());
        sb.append("</html>");

        if (outOfBounds.isEmpty() && alarm.isIgnoreFalse()) {
            //Do nothing then
        } else {
            sendAlarm(_conf, alarm, sb.toString());
        }

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
//            HtmlEmail email = new HtmlEmail();
//
//            //SimpleEmail email = new SimpleEmail();
//            email.setHostName(conf.getSmtpServer());
//            email.setSmtpPort(conf.getSmtpPort());
//
//            email.setAuthentication(conf.getSmtpUser(), conf.getSmtpPW());
//
//            email.setSSLOnConnect(conf.isSmtpSSL());
//            email.setFrom(conf.smtpFrom);
//            email.setSubject(alarm.getSubject());
//            email.setStartTLSEnabled(conf.isSmtpStartTLS());
//            email.setCharset("UTF-8");
//
//            for (String recipient : alarm.getRecipient()) {
//                email.addTo(recipient);
//            }
//
//            for (String bcc : alarm.getBcc()) {
//                email.addBcc(bcc);
//            }
//            //email.setMsg(body);
//            email.setHtmlMsg(body);
//
//            email.setTextMsg("Your email client does not support HTML messages");
//
//            email.setDebug(true);
//            email.send();
//            logger.info("Alarm send: " + alarm.getSubject());


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
