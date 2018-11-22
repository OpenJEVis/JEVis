/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
//import com.springsource.javax.mail;

/**
 *
 * @author broder
 */
public class Mail {

    String _host;
    int _port;
    String _user;
    String _pass;
    String _subject;

//    public Mail()
//    {
////        _host = ReportHandler.HOST;
////        _port = ReportHandler.PORT;
////        _user = ReportHandler.USER;
////        _pass = ReportHandler.PASS;
////        _subject = ReportHandler.SUBJECT;
//    }
    public Mail() {
        _host = "smtp.1und1.de";
        _port = 587;
        _user = "JEReport@openjevis.org";
        _pass = "ahMai8Ee";
        _subject = "A new Report in the Attachment";
    }

    public Mail(String host, int port, String user, String password, String subject) {
        _host = host;
        _port = port;
        _user = user;
        _pass = password;
        _subject = subject;
    }

    public void sendMail(String emails, String filename) throws AddressException, MessagingException, GeneralSecurityException {
        logger.info("FILENAME " + filename);
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
//        String host = "smtp.1und1.de";
//        int _port = 587;
//        final String _user = "JEReport@openjevis.org";
//        final String _pass = "ahMai8Ee";

        // SMTP Host setzen
        Properties props = new Properties();
        //Authentifizierung aktivieren
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.setProperty("mail.smtp.ssl.trust", "smtpserver");

        // Default Session holen, obige Properties und Authentifizierungsdaten setzen
//
//        MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
//        socketFactory.setTrustAllHosts(true);
//        props.put("mail.imaps.ssl.socketFactory", socketFactory);

//        props.put("mail.smtp.ssl.trust", true);
        props.setProperty("mail.smtp.auth", "true");  //dies war wichtig fürs AIT mit gmail
        props.setProperty("mail.smtp.starttls.enable", "true");  //dies war wichtig fürs AIT mit gmail
//        props.setProperty("mail.smtp.ssl.trust", "smtpserver");  //nur dies war ausreichend ohne gmail (nicht verschlüsselt?)


        Session session = Session.getInstance(props);
        // debug mode setzen
        session.setDebug(false);

        Transport transport = session.getTransport("smtp");
        transport.connect(_host, _port, _user, _pass);

        Address[] addresses = InternetAddress.parse(emails);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(_user));
        message.setRecipients(Message.RecipientType.TO, addresses);

        MimeMultipart mimeMultipart = new MimeMultipart();

        MimeBodyPart attachement = new MimeBodyPart();
        File file = new File(filename);
        attachement.setDataHandler(new DataHandler(new FileDataSource(file)));
        attachement.setFileName(file.getName());
        attachement.setDisposition(MimeBodyPart.ATTACHMENT);

//        message.setSubject("new Report " + file.getName());
        message.setSubject(_subject);

        MimeBodyPart text = new MimeBodyPart();
        text.setText("A new report in the attachement!");
        text.setDisposition(MimeBodyPart.INLINE);

        mimeMultipart.addBodyPart(text);
        mimeMultipart.addBodyPart(attachement);

        message.setContent(mimeMultipart);

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        transport.sendMessage(message, addresses);
        logger.info("E-Mail gesendet an " + emails);
        transport.close();
    }
}
