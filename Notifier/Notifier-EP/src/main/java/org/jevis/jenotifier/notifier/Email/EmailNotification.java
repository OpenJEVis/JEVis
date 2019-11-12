/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.Email;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.jenotifier.notifier.Notification;
import org.joda.time.DateTime;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gf
 */
public class EmailNotification implements Notification {
    private static final Logger logger = LogManager.getLogger(EmailNotification.class);

    private JEVisObject _jenoti;
    private List<String> _receivers;
    private List<String> _carbonCopys;
    private List<String> _blindCarbonCopys;
    private String _subject;
    private String _message;
    private List<String> _attachments;
    private List<File> _attachmentsAsFile;
    private boolean _isHTML;
    public static final String _type = "E-Mail Notification";
    private boolean _sendSuccessful = false;
    private List<DateTime> _sendTime;
    private boolean _enabled;
    //
    public static final String ATTACHMENTS = "Attachments";
    public static final String BLIND_CARBON_COPYS = "Blind Carbon Copys";
    public static final String CARBON_COPYS = "Carbon Copys";
    public static final String HTML_EMAIL = "HTML E-Mail";
    public static final String MESSAGE = "Message";
    public static final String RECIPIENTS = "Recipients";
    public static final String SUBJECT = "Subject";
    //    public static final String SENT_TIME = "Sent Time";
//    public static final String ENABLED = "Enabled";
    public static final String EMAIL_ADRESSES = "E-Mail Adresses";

    public EmailNotification() {
    }

    /**
     * This constructor is used to creat a new variable of type
     * EmailNotification by copying a existed variable of type
     * EmailNotification.
     *
     * @param emnoti
     */
    public EmailNotification(EmailNotification emnoti) {
        if (emnoti.isNotiConfigured()) {
            _jenoti = emnoti.getJEVisObjectNoti();
            _receivers = emnoti.getReceivers();
            _carbonCopys = emnoti.getCarbonCopys();
            _blindCarbonCopys = emnoti.getBlindCarbonCopys();
            _subject = emnoti.getSubject();
            _message = emnoti.getMessage();
            _attachments = emnoti.getAttachments();
            _attachmentsAsFile = emnoti.getAttachmentsAsFile();
            _isHTML = emnoti.getIsHTML();
            _enabled = emnoti.isSendEnabled();
//            _sendSuccessful=emnoti.isSendSuccessfully();
//            _sendTime=emnoti.getSendTime();
        }
    }

    /**
     * return the global variable _jenoti.
     *
     * @return
     */
    @Override
    public JEVisObject getJEVisObjectNoti() {
        return _jenoti;
    }

    /**
     * return the global variable _receivers.
     *
     * @return
     */
    public List<String> getReceivers() {
        return _receivers;
    }

    /**
     * return the global variable _carbonCopys.
     *
     * @return
     */
    public List<String> getCarbonCopys() {
        return _carbonCopys;
    }

    /**
     * return the global variable _blindCarbonCopys.
     *
     * @return
     */
    public List<String> getBlindCarbonCopys() {
        return _blindCarbonCopys;
    }

    /**
     * return the global variable _subject.
     *
     * @return
     */
    public String getSubject() {
        return _subject;
    }

    /**
     * return the global variable _message.
     *
     * @return
     */
    public String getMessage() {
        return _message;
    }

    /**
     * return the global variable _attachments.
     *
     * @return
     */
    public List<String> getAttachments() {
        return _attachments;
    }

    /**
     * return the global variable _attachmentsAsFile.
     *
     * @return
     */
    public List<File> getAttachmentsAsFile() {
        return _attachmentsAsFile;
    }

    /**
     * return the global variable _isHTML. If _isHTML is true, the email will be
     * sent as HTML-Email. Else, it will be sent as Text-Email.
     *
     * @return
     */
    public boolean getIsHTML() {
        return _isHTML;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "E-Mail Notification".
     *
     * @return
     */
    @Override
    public String getType() {
        return _type;
    }

    /**
     * return the global variable _sendTime. Only the Email is sent and
     * setSussesfulSend(boolean sendSucessful, DateTime date) is called,
     * _sendTime will have the value. Else, it will be null.
     *
     * @return
     */
    @Override
    public List<DateTime> getSendTime() {
        return _sendTime;
    }

    /**
     * To check, if the EmailNotification has been sent.
     *
     * @return
     */
    @Override
    public boolean isSendSuccessfully() {
        return _sendSuccessful;
    }

    /**
     * set _enabled
     *
     * @return
     */
    @Override
    public boolean isSendEnabled() {
        return _enabled;
    }

//    /**
//     *
//     * @return
//     */
//    public String getSendTimeAsString() {
//        return _sendTime.toString();
//    }

    /**
     * To set the global variable _receivers. It will call the function
     * splitImport(to, ";") to get a list, which stores the email address. Then
     * it will call isEmailAddressesLegal(_receivers) to delete the illegal
     * email address.
     *
     * @param to the email addresses. Default:Every address is split with ";".
     */
    public void setReceivers(String to) {
        _receivers = splitImport(to, ";");
        if (!_receivers.isEmpty()) {
            isEmailAddressesLegal(_receivers);
        }
    }

    /**
     * If _receivers is not setted, it will call setReceivers(to). Else, it will
     * add the new addresses into the list _receivers, delete the illegal email
     * address.
     *
     * @param to the email addresses. Default:Every address is split with ";".
     */
    public void addReceivers(String to) {
        if (_receivers == null) {
            setReceivers(to);
        } else {
            List<String> receivers = splitImport(to, ";");
            if (!receivers.isEmpty()) {
                isEmailAddressesLegal(receivers);
                for (String rec : receivers) {
                    if (!_receivers.contains(rec)) { // repetitive receivers is not allowed
                        _receivers.add(rec);
                    } else {
                        logger.info("Repetitive Receivers: " + rec + " is not allowed");
//                        logger.info("Repetitive Receivers: " + rec + " is not allowed");
                    }
                }
            }
        }
    }

    /**
     * To set the global variable _carbonCopys. It will call the function
     * splitImport(cc, ";") to get a list, which stores the email address. Then
     * it will call isEmailAddressesLegal(_carbonCopys) to delete the illegal
     * email address.
     *
     * @param cc the email addresses. Default:Every address is split with ";".
     */
    public void setCarbonCopys(String cc) {
        _carbonCopys = splitImport(cc, ";");
        if (!_carbonCopys.isEmpty()) {
            isEmailAddressesLegal(_carbonCopys);
        }
    }

    /**
     * If _carbonCopys is not setted, it will call setCarbonCopys(cc). Else, it
     * will add the new addresses into the list _carbonCopys, delete the illegal
     * email address.
     *
     * @param cc the email addresses. Default:Every address is split with ";".
     */
    public void addCarbonCopys(String cc) {
        if (_carbonCopys == null) {
            setCarbonCopys(cc);
        } else {
            List<String> ccs = splitImport(cc, ";");
            if (!ccs.isEmpty()) {
                isEmailAddressesLegal(ccs);
                for (String rec : ccs) {
                    if (!_carbonCopys.contains(rec)) { // repetitive cc is not allowed
                        _carbonCopys.add(rec);
                    } else {
                        logger.info("Repetitive CC: " + rec + " is not allowed");
//                        logger.info("Repetitive CC: " + rec + " is not allowed");
                    }
                }
            }
        }
    }

    /**
     * To set the global variable _blindCarbonCopys. It will call the function
     * splitImport(bcc, ";") to get a list, which stores the email address. Then
     * it will call isEmailAddressesLegal(_blindCarbonCopys) to delete the
     * illegal email address.
     *
     * @param bcc the email addresses. Default:Every address is split with ";".
     */
    public void setBlindCarbonCopys(String bcc) {
        _blindCarbonCopys = splitImport(bcc, ";");
        if (!_blindCarbonCopys.isEmpty()) {
            isEmailAddressesLegal(_blindCarbonCopys);
        }
    }

    /**
     * If _blindCarbonCopys is not setted, it will call
     * setBlindCarbonCopys(bcc). Else, it will add the new addresses into the
     * list _blindCarbonCopys, delete the illegal email address.
     *
     * @param bcc the email addresses. Default:Every address is split with ";".
     */
    public void addBlindCarbonCopys(String bcc) {
        if (_blindCarbonCopys == null) {
            setBlindCarbonCopys(bcc);
        } else {
            List<String> bccs = splitImport(bcc, ";");
            if (!bccs.isEmpty()) {
                isEmailAddressesLegal(bccs);
                for (String rec : bccs) { // repetitive bcc is not allowed
                    if (!_blindCarbonCopys.contains(rec)) {
                        _blindCarbonCopys.add(rec);
                    } else {
                        logger.info("Repetitive BCC: " + rec + " is not allowed");
//                        logger.info("Repetitive BCC: " + rec + " is not allowed");
                    }
                }
            }
        }
    }

    /**
     * To set the global variable _subject. If the param is null, _subject
     * remains null.
     *
     * @param subject
     */
    public void setSubject(String subject) {
        _subject = subject;
    }

    /**
     * To set the global variable _message. If the param is null, _message
     * remains null.
     *
     * @param msg
     */
    public void setMessage(String msg) {
        _message = msg;
    }

    /**
     * To set the global variable _attachments and _attachmentsAsFile will be
     * setted according to _attachments. It will call the function
     * splitImport(to, ";") to get a list, which stores the file address.
     *
     * @param attachment the file addresses. Default:Every address is split with
     *                   ";".
     */
    public void setAttachments(String attachment) {
        _attachments = splitImport(attachment, ";");
        _attachmentsAsFile = new ArrayList<File>();
        if (!_attachments.isEmpty()) {
            for (String atch : _attachments) {
                File newFile = new File(atch);
                if (newFile.exists()) {
                    _attachmentsAsFile.add(newFile);//if the address is wrong, can cause exception
                } else {
// newFile.createNewFile();
// _allAttachmentsAsFile.add(newFile);
                    logger.info("There is no file under this path: " + atch);
//                    logger.info("There is no file under this path: " + atch);
                }
            }
        }
    }

    /**
     * If _attachments is not setted, it will call setAttachments(attachment).
     * Else, it will add the new file addresses into the list _attachments.
     *
     * @param attachment the file addresses. Default:Every address is split with
     *                   ";".
     */
    public void addAttachments(String attachment) {
        if (_attachments == null) {
            setAttachments(attachment);
        } else {
            List<String> attachments = splitImport(attachment, ";");
            if (!attachments.isEmpty()) {
                for (String atch : attachments) {
                    if (!_attachments.contains(atch)) { // repetitive attachment is not allowed
                        _attachments.add(atch);
                        File newFile = new File(atch);
                        if (newFile.exists()) {
                            _attachmentsAsFile.add(newFile);//if the address is wrong, can cause exception
                        } else {
// newFile.createNewFile();
// _allAttachmentsAsFile.add(newFile);
                            logger.info("There is no file under this path: " + atch);
//                            logger.info("There is no file under this path: " + atch);
                        }
                    } else {
                        logger.info("Repetitive address: " + atch + " is not allowed");
//                        logger.info("Repetitive address: " + atch + " is not allowed");
                    }
                }
            }
        }
    }

    public void setAttachmentsAsFile(JEVisFile attachment) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream("/tmp/" + attachment.getFilename());
            output.write(attachment.getBytes());
            output.close();
            File tmpFile = new File("/tmp/" + attachment.getFilename());
            setAttachmentsAsFile(tmpFile);
            //                tmpFile.delete();
            _attachmentsAsFile = new ArrayList<File>();
            _attachmentsAsFile.add(tmpFile);
            _attachments = new ArrayList<String>();
            if (!_attachmentsAsFile.isEmpty()) {
                for (File atch : _attachmentsAsFile) {
                    _attachments.add(atch.getPath());
                }
            }
        } catch (FileNotFoundException ex) {
            logger.fatal(ex);
        } catch (IOException ex) {
            logger.fatal(ex);
        }
    }

    /**
     * To set the global variable _attachmentsAsFile and _attachments will be
     * setted according to _attachmentsAsFile.
     *
     * @param attachment
     */
    public void setAttachmentsAsFile(File attachment) {
        _attachmentsAsFile = new ArrayList<File>();
        _attachmentsAsFile.add(attachment);
        _attachments = new ArrayList<String>();
        if (!_attachmentsAsFile.isEmpty()) {
            for (File atch : _attachmentsAsFile) {
                _attachments.add(atch.getPath());
            }
        }
    }

    /**
     * If _attachmentsAsFile is not setted, it will call
     * setAttachmentsAsFile(attachment). Else, it will add the new file
     * addresses into the list _attachments.
     *
     * @param attachment
     */
    public void addAttachmentsAsFile(File attachment) {
        if (_attachmentsAsFile == null) {
            setAttachmentsAsFile(attachment);
        } else {
            _attachmentsAsFile.add(attachment);
            _attachments.add(attachment.getPath());
        }
    }

    /**
     * To set the global variable _isHTML.
     *
     * @param html
     */
    public void setIsHTML(boolean html) {
        _isHTML = html;
    }

    private JEVisFile getJEVisFile(JEVisObject obj, String attName) throws JEVisException {
        JEVisAttribute att = obj.getAttribute(attName);
        if (att != null) { //check, if the attribute exists.
            if (att.hasSample()) { //check, if this attribute has values.
                JEVisSample sample = att.getLatestSample();
                if (sample.getValueAsFile() != null) { //check, if the value of this attribute is null.
                    return sample.getValueAsFile();
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
     * To check, whether the email addresses in a list are legal. The illegal
     * email addresses will be deleted. If the list is empty at the beginning,
     * it will be considered as illegal.
     *
     * @param emailAddresses
     * @return
     */
    public boolean isEmailAddressesLegal(List<String> emailAddresses) {
        boolean isEmail = true;
        if (emailAddresses != null && !emailAddresses.isEmpty()) {
            List<String> toBeRemoved = new ArrayList<>();
            for (String email : emailAddresses) {
                if (!isEmailAddressLegal(email)) {
                    toBeRemoved.add(email);//
                    isEmail = false;
                }
            }
            emailAddresses.removeAll(toBeRemoved);
        } else {
            isEmail = false;
            logger.info(emailAddresses + " is illegal.");
//            logger.info(emailAddresses + " is illegal.");
        }
        return isEmail;
    }

    /**
     * To check, whether the email address is legal. If the email address matchs
     * the Canonical law,it is legal and true will be returned. Else, false will
     * be returned.
     *
     * @param emailAddress
     * @return
     */
    public boolean isEmailAddressLegal(String emailAddress) {
        boolean isEmail;
        Pattern pattern = Pattern.compile("^([a-z0-9A-Z]+[-|\\.|_]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
        if (emailAddress != null) {
            Matcher matcher = pattern.matcher(emailAddress);
            isEmail = matcher.matches();
        } else {
            isEmail = false;
        }
        return isEmail;
    }

    /**
     * To delete the Em space and En space in front and in back
     *
     * @param source
     * @return
     */
    public String trimSpace(String source) {
        return source == null ? source : source.replaceAll("^[\\s　]*|[\\s　]*$", "");
    }

    /**
     * To split the email addresses or file addresses, put the addresses into a
     * list. Repetitive address is not allowed. If there is nothing to split, it
     * will return an empty list.
     *
     * @param str   the addresses to be splited
     * @param split the split symbol
     * @return
     */
    public List<String> splitImport(String str, String split) {
        List<String> address = new ArrayList<String>();
        if (str != null && !str.isEmpty()) {
            if (str.contains(split)) {
                String[] strs = str.split(split);
                for (int i = 0; i < strs.length; i++) {
                    strs[i] = trimSpace(strs[i]);
                    if (!address.contains(strs[i])) { // repetitive address is not allowed
                        address.add(strs[i]);
                    } else {
                        logger.info("Repetitive address: " + strs[i] + " is not allowed");
//                        logger.info("Repetitive address: " + strs[i] + " is not allowed");
                    }
                }
            } else {
                address.add(str);
            }
        }
        return address;
    }

    /**
     * To convert the type of email addresses. If the param is null, returns a
     * Array, which has no element.
     *
     * @param emailAddress
     * @return
     * @throws AddressException
     */
    public Address[] convertEmailAddress(List<String> emailAddress) throws AddressException {
//if emailAddress is null, can cause exception
        Address[] addresses;
        if (null != emailAddress) {
            addresses = new Address[emailAddress.size()];
            for (int i = 0; i < emailAddress.size(); i++) {
                addresses[i] = new InternetAddress(emailAddress.get(i));
            }
        } else {
            addresses = new Address[0];
        }
        return addresses;
    }

    /**
     * If the Email is successfully sent, sets _sendSucessful with true and
     * _sendTime with the send time.
     *
     * @param sendSuccessful
     * @param date
     */
    @Override
    public void setSuccessfulSend(boolean sendSuccessful, DateTime date) {
        _sendSuccessful = sendSuccessful;
        if (_sendTime == null) { //If the notification is sent many times, all time will be recored.
            _sendTime = new ArrayList<DateTime>();
        }
        if (sendSuccessful == true && null != date) {
            _sendTime.add(date);
        }
    }

    /**
     * To get the value of the attribute of a JEVisObject
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

    @Override
    public void setNotificationObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jenoti = notiObj;
            try {
                setReceivers(String.valueOf(getAttribute(notiObj, RECIPIENTS)));//the second parameter should one to one correspondence with the name in JEConfig
            } catch (Exception ex) {
                setReceivers(null);
                logger.error(ex);
            }
            try {
                setCarbonCopys(String.valueOf(getAttribute(notiObj, CARBON_COPYS)));
            } catch (Exception ex) {
                setCarbonCopys("");
                logger.error(ex);
            }
            try {
                setBlindCarbonCopys(String.valueOf(getAttribute(notiObj, BLIND_CARBON_COPYS)));
            } catch (Exception ex) {
                setBlindCarbonCopys("");
                logger.error(ex);
            }
            try {
                setSubject(String.valueOf(getAttribute(notiObj, SUBJECT)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            try {
                setMessage(String.valueOf(getAttribute(notiObj, MESSAGE)));
            } catch (Exception ex) {
                logger.error(ex);
            }
            JEVisFile file = null;
            try {
//                logger.info(String.valueOf(getAttribute(notiObj, ATTACHMENTS)));
//                setAttachments(String.valueOf(getAttribute(notiObj, ATTACHMENTS)));

                if (getJEVisFile(notiObj, ATTACHMENTS) != null) {
                    do {
                        file = getJEVisFile(notiObj, ATTACHMENTS); //TODO this workaround needs to be fixed
                    } while (Objects.isNull(file.getBytes()));
                }

            } catch (Exception ex) {
                logger.error(ex);
            }

            try {
                if (file != null) {
                    setAttachmentsAsFile(file);
                }
            } catch (Exception ex) {
                setAttachments(null);
                logger.error(ex);
            }

            try {
                setIsHTML(Boolean.valueOf(String.valueOf(getAttribute(notiObj, HTML_EMAIL))));
            } catch (Exception ex) {
                setIsHTML(false);
                logger.error(ex);
            }
            try {
                _enabled = Boolean.valueOf(String.valueOf(getAttribute(notiObj, ENABLED)));
            } catch (Exception ex) {
                _enabled = false;
                logger.error(ex);
            }
        } else {
            logger.info(notiObj + " is not suitable for Email Notification");
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
    @Override
    public synchronized void setNotificationObject(JEVisObject notiObj, JEVisFile file) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jenoti = notiObj;
            try {
                setReceivers(String.valueOf(getAttribute(notiObj, RECIPIENTS)));//the second parameter should one to one correspondance with the name in JEConfig
            } catch (IllegalArgumentException ex) {
                setReceivers(null);
                logger.fatal(ex);
            }
            try {
                setCarbonCopys(String.valueOf(getAttribute(notiObj, CARBON_COPYS)));
            } catch (IllegalArgumentException ex) {
                setCarbonCopys(null);
                logger.info(ex);
            }
            try {
                setBlindCarbonCopys(String.valueOf(getAttribute(notiObj, BLIND_CARBON_COPYS)));
            } catch (IllegalArgumentException ex) {
                setBlindCarbonCopys(null);
                logger.info(ex);
            }
            try {
                setSubject(String.valueOf(getAttribute(notiObj, SUBJECT)));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }
            try {
                setMessage(String.valueOf(getAttribute(notiObj, MESSAGE)));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }

            try {
                setAttachmentsAsFile(file);
            } catch (IllegalArgumentException ex) {
                setAttachments(null);
                logger.info(ex);
            }

            try {
                setIsHTML(Boolean.valueOf(String.valueOf(getAttribute(notiObj, HTML_EMAIL))));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }
            try {
                _enabled = Boolean.valueOf(String.valueOf(getAttribute(notiObj, ENABLED)));
            } catch (IllegalArgumentException ex) {
                _enabled = false;
                logger.info(ex);
            }
        } else {
            logger.info(notiObj + " is not suitable for Email Notification");
        }
    }

    /**
     * If the _allReceivers has at least one legal email address, it will be
     * considered as configured. Because the function setAllReceivers() will
     * automatically delete the illegal email addresses, there only need to
     * check, whether _allReceivers is null and is empty.
     *
     * @return
     */
    @Override
    public boolean isNotiConfigured() {
        boolean configured = false;
        if (_receivers != null) {
            if (!_receivers.isEmpty()) {
                configured = true;
            }
        }
        return configured;
    }

    @Override
    public String toString() {
        return "EmailNotification{" + "_jenoti=" + _jenoti + ", _receivers=" + _receivers + ", _carbonCopys=" + _carbonCopys + ", _blindCarbonCopys=" + _blindCarbonCopys + ", _subject=" + _subject + ", _message=" + _message + ", _attachments=" + _attachments + ", _attachmentsAsFile=" + _attachmentsAsFile + ", _isHTML=" + _isHTML + ", _type=" + _type + ", _sendSucessful=" + _sendSuccessful + ", _sendTime=" + _sendTime + ", _enabled" + _enabled + '}';
    }

    @Override
    public void setNotification(List<String> str) {
        setReceivers(str.get(0));
        setCarbonCopys(str.get(1));
        setBlindCarbonCopys(str.get(2));
        setSubject(str.get(3));
        setMessage(str.get(4));
        setAttachments(str.get(5));
        setIsHTML(Boolean.parseBoolean(str.get(6)));
    }

    /**
     * check, whether the jevis object of type "E-Mail Notification" and can be
     * used to set
     *
     * @param notiObj
     * @return
     */
    @Override
    public boolean isConfigurationObject(JEVisObject notiObj) {
        try {
//            logger.info(notiObj.getJEVisClass().getName().equals(_type));
            return notiObj.getJEVisClass().getName().equals(_type);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return false;
    }

    @Override
    public List<DateTime> getSendDate() {
        List<DateTime> sendDate = new ArrayList<DateTime>();
        try {
            JEVisAttribute att = this.getJEVisObjectNoti().getAttribute(EmailNotification.SENT_TIME);
            if (att != null) {
                List<JEVisSample> times = att.getAllSamples();
                for (JEVisSample t : times) {
                    sendDate.add(t.getTimestamp());
                }
            } else {
                logger.info("The attribute " + SENT_TIME + " of " + getJEVisObjectNoti().getID() + " does not exist.");
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return sendDate;
    }

    @Override
    public List<DateTime> getSendSchedule() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
