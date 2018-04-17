/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.Email;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.joda.time.DateTime;

/**
 *
 * @author gf
 */
public class EmailNotificationDriver implements NotificationDriver {

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
    public static final String PROTERTY_SMTP_HOST = "mail.smtp.host";
    public static final String PROTERTY_SMTP_AUTH = "mail.smtp.auth";
    public static final String PROTERTY_SMTP_PORT = "mail.smtp.port";
    public static final String PROTERTY_SMTP_STARTTLS = "mail.smtp.starttls.enable";
    public static final String PROTERTY_SMTP_SSL = "mail.smtp.ssl.enable";
    //
    public static final String PASSWORD = "Password";
    public static final String PORT = "Port";
    public static final String SENDER = "Server User Name";
    public static final String SMTP_SERVER = "SMTP Server";
    public static final String TRANSPORT_SECURITY = "Transport Security";
    public static final String AUTHENTICATOR = "Authenticator";
//    private Session session;
//    private MimeMessage _message;

    public enum TansportSecurity {

        NO, STARTTLS, SSLTLS
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
     * @param obj the JEVis Object
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
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, ex);
            }
            try {
                setPort(Long.valueOf(String.valueOf(getAttribute(notiObj, PORT))));
            } catch (Exception ex) {
                setPort(0);
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, ex);
            }
            try {
                setUser(String.valueOf(getAttribute(notiObj, SENDER)));
            } catch (Exception ex) {
                setUser(null);
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, ex);
            }
            try {
                setPassword(String.valueOf(getAttribute(notiObj, PASSWORD)));
            } catch (Exception ex) {
                setPassword(null);
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, ex);
            }
            try {
                setTransportSecurity(String.valueOf(getAttribute(notiObj, TRANSPORT_SECURITY)));
            } catch (Exception ex) {
                setTransportSecurity("SSL/TLS");
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, ex);
            }
            setDefaultAuthenticator();
//            try {
//                _message = configureMessage(this);
//            } catch (UnsupportedEncodingException ex) {
//                java.util.logging.Logger.getLogger(EmailNotificationDriver.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            } catch (MessagingException ex) {
//                java.util.logging.Logger.getLogger(EmailNotificationDriver.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            }
        } else {
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, notiObj + "is not suitable for the Driver(Email)");
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
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUser(), getPassword()); //
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
     *
     */
    public void setDefaultTransportSecurity() {
        _transportSecurity = TansportSecurity.SSLTLS;
    }

    /**
     *
     * @param ts
     */
    public void setTransportSecurity(String ts) {
        if (ts.equalsIgnoreCase("No")) {
            _transportSecurity = TansportSecurity.NO;
        } else if (ts.equalsIgnoreCase("STARTTLS")) {
            _transportSecurity = TansportSecurity.STARTTLS;
        } else {
            _transportSecurity = TansportSecurity.SSLTLS;
        }
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
            try {
                successful = sendEmailNotification(emnoti);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, null, ex);
            }
            return successful;
        } else {
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "This Notification is not the EmailNotification.");
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "This Notification is" + jenoti.getType() + ".");
//            System.out.println("This Notification is not the EmailNotification.");
//            System.out.println("This Notification is" + jenoti.getType() + ".");
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
     * @throws java.io.UnsupportedEncodingException
     */
    public boolean sendEmailNotification(EmailNotification emnoti) throws UnsupportedEncodingException {
        try {
            Session session;
            Transport tr;
            MimeMessage message;
            synchronized (EmailNotificationDriver.class) {
                session = setServer(this); //set the properties for sending
                tr = session.getTransport();
                tr.connect();
            }
            message = configureMessage(session, emnoti);

            
//            synchronized (EmailNotificationDriver.class) {
//                message = configureMessage(this);
//                System.out.println("finish first config " + this.getJEVisObjectDriver().getID());
//                message.saveChanges();
//            }
//            message = configureMessage(message, emnoti); //configure the Email
//            System.out.println("finish second config " + this.getJEVisObjectDriver().getID());


//            Transport.send(message);// send the notification
        
            tr.sendMessage(message, message.getAllRecipients());
            tr.close();

            emnoti.setSuccessfulSend(true, new DateTime(new Date()));//set the send time
            return true;
        } catch (MessagingException mex) {
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, mex);
            return false;
        }

    }

    /**
     * Set the necessary properties to prepare for sending the Email. And
     * returns the variable of type Session
     *
     * @param driver
     * @return
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    public Session setServer(EmailNotificationDriver driver) throws UnsupportedEncodingException, MessagingException {

        synchronized (EmailNotificationDriver.class) {//EmailNotificationDriver.class
            Properties properties = System.getProperties();
            properties.setProperty(PROTERTY_SMTP_HOST, driver.getSMTPServer());
            properties.setProperty(PROTERTY_SMTP_AUTH, "true");// must set with true
            properties.setProperty("mail.transport.protocol", "smtp");
//            properties.setProperty("mail.debug", "true"); //to show the Information of sending. It can be false.
            properties.setProperty(PROTERTY_SMTP_PORT, String.valueOf(driver.getPort()));//set the smtp port
            if (driver.getTransportSecurity().equals(TansportSecurity.NO)) {
            } else if (driver.getTransportSecurity().equals(TansportSecurity.STARTTLS)) { //set the transport security as STARTTLS
                    properties.put(PROTERTY_SMTP_STARTTLS, "true");
            } else {
                properties.setProperty(PROTERTY_SMTP_SSL, "true"); //set the transport security as SSL
            }
            // properties.setProperty("mail.smtp.auth.login.disable", "true");
            // properties.setProperty("mail.smtp.auth.plain.disable", "true");
            // properties.setProperty("mail.smtp.auth.digest-md5.disable", "true");
            // properties.setProperty("mail.smtp.auth.ntlm.disable", "true");
            // properties.put("mail.smtp.auth.mechanisms", "LOGIN PLAIN DIGEST-MD5 NTLM");
            // properties.put("mail.smtp.auth.ntlm.domain", "mydomain");
            // properties.setProperty("mail.smtp.auth.ntlm.domain", "mydomain");
            // Session session = Session.getInstance(properties);
            Session session = Session.getInstance(properties, driver.getAuthenticator()); //get the Instance of Session
            return session;
        }
    }

    /**
     * To configure the Email information with the attribute of
     * EmailNotification and EmailNotificationDriver.
     *
     * @param session the Session created by setServer()
     * @param emnoti the EmailNotification to be sent
     * @return the configured Email: the variable of type MimeMessage
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private MimeMessage configureMessage(Session session, EmailNotification emnoti) throws MessagingException {
        MimeMessage message;
        message = new MimeMessage(session);//MimeMessage 
        if (getUser() != null) { //set the sender
            message.setFrom(new InternetAddress(getUser()));
        } else {
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "The address of sender is empty.");
//            System.out.println("The address of sender is empty.");
        }

        if (emnoti.getReceivers() != null) {
            Address[] addresses = emnoti.convertEmailAddress(emnoti.getReceivers()); //convert the String address to the type of Address[]

            message.addRecipients(Message.RecipientType.TO, addresses);
        } else {
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "The address of receiver is empty.");
//            System.out.println("The address of receiver is empty.");
        }

        if (emnoti.getCarbonCopys() != null) { //set the CCs
            Address[] addresses = emnoti.convertEmailAddress(emnoti.getCarbonCopys());
            message.addRecipients(Message.RecipientType.CC, addresses);
        }

        if (emnoti.getBlindCarbonCopys() != null) { //set the >BCCs
            Address[] addresses = emnoti.convertEmailAddress(emnoti.getBlindCarbonCopys());
            message.addRecipients(Message.RecipientType.BCC, addresses);
        }

        if (emnoti.getSubject() != null) { //set the subject
            message.setSubject(emnoti.getSubject());
        } else {
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "Send without Subject!");
//            System.out.println("Send without Subject!");
        }
        //set the Email Body (contains message and attachments)
        BodyPart messageBodyPart = new MimeBodyPart();
        Multipart multipart = new MimeMultipart();

        if (emnoti.getMessage() != null || emnoti.getAttachments() != null) { //at least one of this two informations is not null, then the email body will be setted
            if (emnoti.getMessage() != null) { //If message is not null, it will be setted
                if (emnoti.getIsHTML()) { //judge, Whether the message is HTML-Form or Text-Form
                    messageBodyPart.setContent(emnoti.getMessage(), "text/html"); //HTML-Form
                } else {
                    messageBodyPart.setText(emnoti.getMessage()); //Text-Form
                }
                multipart.addBodyPart(messageBodyPart);
            }
            if (emnoti.getAttachments() != null && !emnoti.getAttachments().isEmpty()) { //If attachment is not null and empty, it will be setted
                messageBodyPart = new MimeBodyPart(); //must instantiate a new Instance
                for (File file : emnoti.getAttachmentsAsFile()) {
//                    System.out.println("*********************" + file.exists());
                    DataSource source = new FileDataSource(file);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(file.getName());
                    multipart.addBodyPart(messageBodyPart);
                }
            }
        } else {
            messageBodyPart.setText("");
            multipart.addBodyPart(messageBodyPart);
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "There is no message and no attachment!");
//            System.out.println("There is no message and no attachment!");
        }

        message.setContent(multipart);
        return message;
    }
//
// public void sendHTMLEmail(Session session) {
// try {
// MimeMessage message = new MimeMessage(session);
//
// message.setContent("<h1>This is actual message</h1>","text/html");
//
// } catch (MessagingException mex) {
// mex.printStackTrace();
// }
// }

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
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "User name is illegal.");
//            System.out.println(emailAddress + "User name is illegal.");
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
//        jenoti.getJevisObject().getClass.equla("Email Notification")
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
            Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, null, ex);
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
//                System.out.println(recorder);
                if (recorder != null) {
                    for (DateTime time : noti.getSendTime()) {
                        JEVisSample t = recorder.buildSample(time, noti.getJEVisObjectNoti().getID(), "Sent by Driver" + getJEVisObjectDriver().getID()); //
                        ts.add(t);
                    }
                    recorder.addSamples(ts);
                    re = true;
                } else {
                    Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.INFO, "The attribute of the Notification " + noti.getJEVisObjectNoti().getID() + " does not exist.");
                }
            } catch (JEVisException ex) {
                Logger.getLogger(EmailNotificationDriver.class.getName()).log(Level.ERROR, null, ex);
            }
        }
        return re;
    }

    public void setNotificationDriver(List<String> str) {
    }
}
