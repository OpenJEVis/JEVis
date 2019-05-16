/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.Email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.joda.time.DateTime;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gf
 */
public class EmailNotificationDriver implements NotificationDriver {
    private static final Logger logger = LogManager.getLogger(EmailNotificationDriver.class);

    private JEVisObject _jeDri;
    private String _SMTPServer;
    private long _port;
    private String _userName;
    private String _password;
    private final String _type = "EMail Plugin";
    private TansportSecurity _transportSecurity;
    private Authenticator _authentication;
    //
    public static final String APPLICATIVE_NOTI_TYPE = "E-Mail Notification";
    //
    public static final String PROPERTY_SMTP_HOST = "mail.smtp.host";
    public static final String PROPERTY_SMTP_AUTH = "mail.smtp.auth";
    public static final String PROPERTY_SMTP_PORT = "mail.smtp.port";
    public static final String PROPERTY_SMTP_STARTTLS = "mail.smtp.starttls.enable";
    public static final String PROPERTY_SMTP_SSL = "mail.smtp.ssl.enable";
    //
    public static final String PASSWORD = "Password";
    public static final String PORT = "Port";
    public static final String SENDER = "Server User Name";
    public static final String SMTP_SERVER = "SMTP Server";
    public static final String TRANSPORT_SECURITY = "Transport Security";
    public static final String AUTHENTICATOR = "Authenticator";
//    private Session session;
//    private MimeMessage _message;

    /**
     *
     */
    public void setDefaultTransportSecurity() {
        _transportSecurity = TansportSecurity.STARTTLS;
    }
// public enum Authentication {
// Password, CodedPassword, KerberosGSSAPI, NTLM, TLSCertificate
// }

    public EmailNotificationDriver() {
    }

    /**
     * This constructor is used to creat a new variable of type
     * EmailNotificationDriver by copying a existed variable of type
     * EmailNotificationDriver.
     *
     * @param emnotidrv
     */
    public EmailNotificationDriver(EmailNotificationDriver emnotidrv) {
        if (emnotidrv.isDriverConfigured()) {
            _jeDri = emnotidrv.getJEVisObjectDriver();
            _SMTPServer = emnotidrv.getSMTPServer();
            _port = emnotidrv.getPort();
            _userName = emnotidrv.getUser();
            _password = emnotidrv.getPassword();
            _transportSecurity = emnotidrv.getTransportSecurity();
            _authentication = emnotidrv.getAuthenticator();
        }
    }

    /**
     * To get the value of the attribute of a JevisObject
     *
     * @param obj     the JEVis Object
     * @param attName the name of the attribute
     * @return the value of the attribute
     * @throws JEVisException
     */
    private Object getAttribute(JEVisObject obj, String attName) throws JEVisException {
        JEVisAttribute att = obj.getAttribute(attName);
        if (att != null) { //check, if the attribute exists.
            if (att.hasSample()) { //check, if this attribute has values.
                JEVisSample sample = att.getLatestSample();
                if (sample.getValue() != null) { //check, if the value of this attribute is null.
                    return sample.getValue();
                } else {
                    throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute'value is not filled: " + attName);
                }
            } else {
                throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute'value is not filled: " + attName);
            }
        } else {
            throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute is missing: " + attName);
        }
    }

    /**
     * Call the function getAttribute(,) to get parameters of the notification
     * in Database and use the setter to assign the global variables. If there
     * is an IllegalArgumentException, the complex variable will be assigned
     * with null and the simple variables will not be dealed. The information of
     * the exception will also be printed.
     *
     * @param notiObj
     * @throws JEVisException
     */
    public void setNotificationDriverObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jeDri = notiObj;
            try {
                setSMTPServer(String.valueOf(getAttribute(notiObj, SMTP_SERVER))); //the second parameter should one to one correspondance with the name in JEConfig
            } catch (Exception ex) {
                setSMTPServer(null);
                logger.error(ex);
            }
            try {
                setPort(Long.valueOf(String.valueOf(getAttribute(notiObj, PORT))));
            } catch (Exception ex) {
                setPort(0);
                logger.error(ex);
            }
            try {
                setUser(String.valueOf(getAttribute(notiObj, SENDER)));
            } catch (Exception ex) {
                setUser(null);
                logger.error(ex);
            }
            try {
                setPassword(String.valueOf(getAttribute(notiObj, PASSWORD)));
            } catch (Exception ex) {
                setPassword(null);
                logger.error(ex);
            }
            try {
                setTransportSecurity(String.valueOf(getAttribute(notiObj, TRANSPORT_SECURITY)));
            } catch (Exception ex) {
                setTransportSecurity("STARTTLS");
                logger.info(ex);
            }
            setDefaultAuthenticator();

        } else {
            logger.info(notiObj + "is not suitable for the Driver(Email)");
        }

    }

    /**
     * To set the global variable _SMTPServer. If the param is null or "",
     * _SMTPServer remains null.
     *
     * @param smtp
     */
    public void setSMTPServer(String smtp) {
        if (smtp != null && !smtp.isEmpty()) {
            _SMTPServer = smtp;
        }
    }

    /**
     * To set the global variable _port. If it is not setted, _port will be 0
     *
     * @param port
     */
    public void setPort(long port) {
        _port = port;
    }

    /**
     * To set the global variable _userName, it is an email address. If the
     * email address is illegal, keeps _userName as null.
     *
     * @param user
     */
    public void setUser(String user) {
        if (isEmailAddressLegal(user)) {
            _userName = user;
        }
    }

    /**
     * To set the global variable _password. If the param is null or "",
     * _password remains null.
     *
     * @param password
     */
    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            _password = password;
        }
    }

    /**
     * To set the global variable _authentication. Default: password
     * authentication
     */
    public void setDefaultAuthenticator() {
        _authentication = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUser(), getPassword());
            }
        };
    }

    /**
     * not finished
     *
     * @param auth
     */
    public void setAuthenticator(long auth) {
        if (auth == 1) {
//not finished
        } else {
            _authentication = new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getUser(), getPassword()); //
                }
            };
        }
    }

    /**
     * @param ts
     */
    public void setTransportSecurity(String ts) {
        if (ts.equalsIgnoreCase("No")) {
            _transportSecurity = TansportSecurity.NO;
        } else if (ts.equalsIgnoreCase("STARTTLS")) {
            _transportSecurity = TansportSecurity.STARTTLS;
        } else if (ts.equalsIgnoreCase("SSL")) {
            _transportSecurity = TansportSecurity.SSL;
        }
    }

    /**
     * Set the necessary properties to prepare for sending the Email. And
     * returns the variable of type Session
     *
     * @param driver
     * @return
     */
    public Session setServer(EmailNotificationDriver driver) {//EmailNotificationDriver.class
        Properties properties = System.getProperties();
        Session session = null;
        properties.setProperty(PROPERTY_SMTP_HOST, driver.getSMTPServer());

        properties.setProperty("mail.transport.protocol", "smtp");

        if (driver.getTransportSecurity().equals(TansportSecurity.NO)) {
            Session.getInstance(properties, null);
        } else if (driver.getTransportSecurity().equals(TansportSecurity.STARTTLS)) { //set the transport security as STARTTLS
            properties.setProperty(PROPERTY_SMTP_PORT, String.valueOf(driver.getPort()));//set the smtp port
            properties.setProperty(PROPERTY_SMTP_AUTH, "true");// must set with true
            properties.put(PROPERTY_SMTP_STARTTLS, "true");
            session = Session.getInstance(properties, this.getAuthenticator()); //get the Instance of Session
        } else if (driver.getTransportSecurity().equals(TansportSecurity.SSL)) {
            properties.put("mail.smtp.socketFactory.port", String.valueOf(driver.getPort())); //SSL Port
            properties.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class

            properties.setProperty(PROPERTY_SMTP_PORT, String.valueOf(driver.getPort()));//set the smtp port
            properties.setProperty(PROPERTY_SMTP_AUTH, "true");// must set with true
            //properties.setProperty(PROPERTY_SMTP_SSL, "true"); //set the transport security as SSL
            session = Session.getDefaultInstance(properties, this.getAuthenticator()); //get the Instance of Session
        }

        return session;
    }

    /**
     * return the global variable _transportSecurity.
     *
     * @return
     */
    public TansportSecurity getTransportSecurity() {
        return _transportSecurity;
    }

    /**
     * return the global variable _authentication.
     *
     * @return
     */
    public Authenticator getAuthenticator() {
        return _authentication;
    }

    /**
     * return the global variable _jeDri.
     *
     * @return
     */
    public JEVisObject getJEVisObjectDriver() {
        return _jeDri;
    }

    /**
     * return the global variable _SMTPServe.
     *
     * @return
     */
    public String getSMTPServer() {
        return _SMTPServer;
    }

    /**
     * return the global variable _port.
     *
     * @return
     */
    public long getPort() {
        return _port;
    }

    /**
     * return the global variable _userName.
     *
     * @return
     */
    public String getUser() {
        return _userName;
    }

    /**
     * return the global variable _password.
     *
     * @return
     */
    public String getPassword() {
        return _password;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "EMail Plugin".
     *
     * @return
     */
    public String getDriverType() {
        return _type;
    }

    /**
     * To send the Notification, the Notification must have the type: Email
     * Notification. If the notification is sucessfully sent, returns true.
     * Else, returns false.
     *
     * @param jenoti type: Notification
     * @return
     */
    public boolean sendNotification(Notification jenoti) {
        boolean successful = false;
        if (jenoti.getType().equals(APPLICATIVE_NOTI_TYPE)) {
            EmailNotification emnoti = (EmailNotification) jenoti;//(EmailNotification) jenoti;
            successful = sendEmailNotification(emnoti);
            return successful;
        } else {
            logger.info("This Notification is not the EmailNotification.");
            logger.info("This Notification is" + jenoti.getType() + ".");
            return successful;
        }
    }

    /**
     * All necessary parameters will be configured to send the
     * EmailNotification. If the EmailNotification is sucessfully sent, returns
     * true. Else, returns false.
     *
     * @param emnoti type: EmailNotification
     * @return
     */
    public boolean sendEmailNotification(EmailNotification emnoti) {
        try {
            Session session;
            MimeMessage message;

            session = setServer(this); //set the properties for sen
            logger.debug("Set Server finished.");

            message = configureMessage(session, emnoti);
            logger.debug("Finished configuring message.");

            Transport.send(message);
            logger.debug("Sent message.");

            emnoti.setSuccessfulSend(true, new DateTime(new Date()));//set the send time
            return true;
        } catch (MessagingException mex) {
            logger.error(mex);
            return false;
        }

    }

    /**
     * To configure the Email information with the attribute of
     * EmailNotification and EmailNotificationDriver.
     *
     * @param session the Session created by setServer()
     * @param emnoti  the EmailNotification to be sent
     * @return the configured Email: the variable of type MimeMessage
     * @throws UnsupportedEncodingException
     */
    private MimeMessage configureMessage(Session session, EmailNotification emnoti) throws MessagingException {
        MimeMessage message;
        message = new MimeMessage(session);//MimeMessage

        message.addHeader("Content-type", "text/HTML; charset=UTF-8");
        message.addHeader("format", "flowed");
        message.addHeader("Content-Transfer-Encoding", "8bit");

        if (getUser() != null) { //set the sender
            message.setFrom(new InternetAddress(getUser()));
        } else {
            logger.info("The address of sender is empty.");
        }

        if (emnoti.getReceivers() != null) {
            for (String recipient : emnoti.getReceivers()) {
                InternetAddress address = new InternetAddress();
                address.setAddress(recipient);
                message.addRecipient(Message.RecipientType.TO, address);
            }
        } else {
            logger.info("The address of receiver is empty.");
        }

        if (emnoti.getCarbonCopys() != null) { //set the CCs
            for (String recipient : emnoti.getCarbonCopys()) {
                InternetAddress address = new InternetAddress();
                address.setAddress(recipient);
                message.addRecipient(Message.RecipientType.CC, address);
            }
        }

        if (emnoti.getBlindCarbonCopys() != null) { //set the >BCCs
            for (String recipient : emnoti.getBlindCarbonCopys()) {
                InternetAddress address = new InternetAddress();
                address.setAddress(recipient);
                message.addRecipient(Message.RecipientType.BCC, address);
            }
        }

        if (emnoti.getSubject() != null) { //set the subject
            message.setSubject(emnoti.getSubject());
        } else {
            logger.info("Send without Subject!");
        }
        //set the Email Body (contains message and attachments)
        BodyPart messageBodyPart = new MimeBodyPart();
        Multipart multipart = new MimeMultipart();

        if (emnoti.getMessage() != null || emnoti.getAttachments() != null) { //at least one of this two informations is not null, then the email body will be setted
            if (emnoti.getMessage() != null) { //If message is not null, it will be setted
                if (emnoti.getIsHTML()) { //judge, Whether the message is HTML-Form or Text-Form
                    messageBodyPart.setContent(emnoti.getMessage(), "text/html; charset=UTF-8"); //HTML-Form
                } else {
                    messageBodyPart.setText(emnoti.getMessage()); //Text-Form
                }
                multipart.addBodyPart(messageBodyPart);
            }
            if (emnoti.getAttachments() != null && !emnoti.getAttachments().isEmpty()) { //If attachment is not null and empty, it will be setted
                messageBodyPart = new MimeBodyPart(); //must instantiate a new Instance
                for (File file : emnoti.getAttachmentsAsFile()) {
                    DataSource source = new FileDataSource(file);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(file.getName());
                    multipart.addBodyPart(messageBodyPart);
                }
            }
        } else {
            messageBodyPart.setText("");
            multipart.addBodyPart(messageBodyPart);
            logger.info("There is no message and no attachment!");
        }

        message.setContent(multipart, "text/html; charset=UTF-8");
        return message;
    }

    public enum TansportSecurity {

        NO, STARTTLS, SSL
    }

    /**
     * To check, whether the email address is legal. If the email address matchs
     * the Canonical law,it is legal and true will be returned. Else, false will
     * be returned.
     *
     * @param emailAddress
     * @return
     */
    private boolean isEmailAddressLegal(String emailAddress) {
        boolean isEmail;
        Pattern pattern = Pattern.compile("^([a-z0-9A-Z]+[-|\\.|_]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"); //Canonical law for Email
        if (emailAddress != null) {
            Matcher matcher = pattern.matcher(emailAddress);
            isEmail = matcher.matches();
        } else {
            isEmail = false;
        }
        if (!isEmail) {
            logger.info("User name is illegal.");
        }
        return isEmail;
    }

    /**
     * If the notification has the type: E-Mail Notification, then the driver
     * can support the notification. If supported, it only means, this driver
     * can send the email. But if the driver is not configured or rightly
     * configured, the driver can not send the Email, even if it is supported.
     *
     * @param jenoti
     * @return
     */
    public boolean isSupported(Notification jenoti) {
        boolean support;
        support = jenoti.getType().equals(APPLICATIVE_NOTI_TYPE);
        return support;
    }

    /**
     * Only if the smtp server, the user and the password is setted, the driver
     * is considered as configured. But if, for example, the server is not
     * rightly setted, then the driver can not send Email, even if it is
     * configured.
     *
     * @return
     */
    public boolean isDriverConfigured() {
        boolean isConfigured = false;
        if (_SMTPServer != null && _userName != null && _password != null) {
            isConfigured = true;
        }
        return isConfigured;
    }

    @Override
    public String toString() {
        return "EmailNotificationDriver{" + "_jeDri=" + _jeDri + ", _SMTPServer=" + _SMTPServer + ", _port=" + _port + ", _userName=" + _userName + ", _password=" + _password + ", _type=" + _type + ", _transportSecurity=" + _transportSecurity + ", _authentication=" + _authentication + '}';
    }

    /**
     * check, whether the jevis object of type "EMail Plugin" and can be used to
     * set
     *
     * @param driverObj
     * @return
     */
    public boolean isConfigurationObject(JEVisObject driverObj) {
        try {
            return driverObj.getJEVisClass().getName().equals(_type);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

    /**
     * store the send time into JEConfig
     *
     * @param noti
     * @return
     */
    public boolean sendTimeRecorder(Notification noti) {
        boolean re = false;
        if (noti.isSendSuccessfully()) {
            try {
                List<JEVisSample> ts = new ArrayList<JEVisSample>();
                JEVisAttribute recorder = noti.getJEVisObjectNoti().getAttribute(Notification.SENT_TIME);
                if (recorder != null) {
                    for (DateTime time : noti.getSendTime()) {
                        JEVisSample t = recorder.buildSample(time, noti.getJEVisObjectNoti().getID(), "Sent by Driver" + getJEVisObjectDriver().getID()); //
                        ts.add(t);
                    }
                    recorder.addSamples(ts);
                    re = true;
                } else {
                    logger.info("The attribute of the Notification " + noti.getJEVisObjectNoti().getID() + " does not exist.");
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return re;
    }

    public void setNotificationDriver(List<String> str) {
    }
}
