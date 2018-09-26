/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.AppPush;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.jenotifier.notifier.Notification;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gf
 */
public class PushNotification implements Notification {
    //    public static final String SENT_TIME = "Sent Time";
//    public static final String ENABLED = "Enabled";
    public static final String BUNDLE_IDS = "Bundle IDs";

    private JEVisObject _jenoti;
    private List<String> _receivers;
    private List<String> _bundleIDs;
    private String _subject;
    private String _message;
    private boolean _broadcast;
    private String _broadcastPlatform;
    private final String _type = "Push Notification";
    private boolean _sendSuccessful;
    private List<DateTime> _sendTime;
    private boolean _enabled;
    //
    public static final String BROADCAST = "Broadcast";
    public static final String BROADCAST_PLATFORM = "Broadcast Platform";
    public static final String MESSAGE = "Message";
    public static final String RECIPIENTS = "Recipients";
    public static final String SUBJECT = "Subject";
    private static final Logger logger = LogManager.getLogger(PushNotification.class);

    public PushNotification() {
    }

    /**
     * This constructor is used to creat a new variable of type PushNotification
     * by copying a existed variable of type PushNotification. But
     * _sendSuccessful and _sendTime can't be kopied.
     *
     * @param pnoti
     */
    public PushNotification(PushNotification pnoti) {
        if (pnoti.isNotiConfigured()) {
            _jenoti = pnoti.getJEVisObjectNoti();
            _receivers = pnoti.getReceivers();
            _bundleIDs = pnoti.getBundleIDs();
            _subject = pnoti.getSubject();
            _message = pnoti.getMessage();
            _broadcast = pnoti.isBroadcast();
            _broadcastPlatform = pnoti.getBroadcastPlatform();
            _enabled = pnoti.isSendEnabled();
//        _sendSuccessful=pnoti.isSendSuccessfully();
//        _sendTime=pnoti.getSendTime();
        }
    }

    /**
     * To package _bundleIDs, _subject and _message into a JSON Object. To form
     * the request of the HTTP. The broadcast has the highst priority. If
     * broadcast is true, the receivers will be ignored. If broadcast is false,
     * then configure the request according to the quantity of the receivers
     *
     * @return
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject httpBody = new JSONObject();
        if (isNotiConfigured()) {
            if (_broadcast == false) {
                if (_bundleIDs.size() == 1) {
                    httpBody.put("recipient", _bundleIDs.get(0));
                } else if (_bundleIDs.size() > 1) {
                    JSONArray receivers = new JSONArray();
                    for (String buID : _bundleIDs) {
                        receivers.add(buID);
                    }
                    httpBody.put("recipients", receivers);
                }
            }
            httpBody.put("title", _subject);
            httpBody.put("message", _message);
        }
        return httpBody;
    }

    /**
     * Return the JEVis Object, which is of type Push Notification and is used
     * to set the instance of PushNotification (_jenoti)
     *
     * @return
     */
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
     * return the global variable _bundleIDs.
     *
     * @return
     */
    public List<String> getBundleIDs() {
        return _bundleIDs;
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
     * return the global variable _broadcast.
     *
     * @return
     */
    public boolean isBroadcast() {
        return _broadcast;
    }

    /**
     * return the global variable _broadcastPlatform.
     *
     * @return
     */
    public String getBroadcastPlatform() {
        return _broadcastPlatform;
    }

    /**
     * return the global variable _sendTime. Only the Email is sent and
     * setSussesfulSend(boolean sendSucessful, DateTime date) is called,
     * _sendTime will have the value. Else, it will be null.
     *
     * @return
     */
    public List<DateTime> getSendTime() {
        return _sendTime;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "Push Notification".
     *
     * @return
     */
    public String getType() {
        return _type;
    }

    /**
     * To check, if the PushNotification has been sent.
     *
     * @return
     */
    public boolean isSendSuccessfully() {
        return _sendSuccessful;
    }

    /**
     * set _enabled
     *
     * @return
     */
    public boolean isSendEnabled() {
        return _enabled;
    }

    /**
     * according to the different push way to return the different mode. The
     * broadcast has the highst priority. If broadcast is true, the receivers
     * will be ignored. Then according to broadcast platform, if broadcast
     * platform is one of ios, android and windowsphone, return
     * platform/{platform}. Else, return broadcast. If broadcast is false, then
     * return the push way according to the quantity of the receivers (one
     * receiver: single; more receivers: multi).
     * <p>
     * This function is used to compare with the driver to get the right driver.
     *
     * @return
     */
    public String getPushWay() {
        String pw = null;
        if (isNotiConfigured()) {
            if (!isBroadcast() && getReceivers().size() == 1) {
                pw = "single";//the old one: /coffee/push/notification   /cmns/v1/info/
            } else if (!isBroadcast() && getReceivers().size() > 1) {
                pw = "multi";//the old one: /coffee/push/channel
            } else if (isBroadcast() && (!getBroadcastPlatform().toLowerCase().equals("ios") && !getBroadcastPlatform().toLowerCase().equals("android") && !getBroadcastPlatform().toLowerCase().equals("windowsphone"))) {
                pw = "broadcast";//the old one: /coffee/push/broadcast
            } else if (isBroadcast() && (getBroadcastPlatform().toLowerCase().equals("ios") || getBroadcastPlatform().toLowerCase().equals("android") || getBroadcastPlatform().toLowerCase().equals("windowsphone"))) {
                pw = "platform/" + getBroadcastPlatform().toLowerCase();//the old one: /coffee/push/platform/
            }
        } else {
            logger.info("The notification is not configured!!");
//            logger.info("The notification is not configured!!");
        }
        return pw;
    }

    /**
     * To set the global variable _allReceivers. It will call the function
     * splitImport(to, ";") to get a list, which stores the receivers (Device
     * ID).
     *
     * @param to the IDs. Default:Every ID is split with ";".
     */
    public void setReceivers(String to) {
        _receivers = splitImport(to, ";");
    }

    /**
     * If _receivers is not setted, it will call setReceivers(to). Else, it will
     * add the new IDs into the list _receivers.
     *
     * @param to the IDs. Default:Every ID is split with ";".
     */
    public void addReceivers(String to) {
        if (_receivers == null) {
            setReceivers(to);
        } else {
            List<String> receivers = splitImport(to, ";");
            if (!receivers.isEmpty()) {
                for (String rec : receivers) {
                    if (!_receivers.contains(rec)) { // repetitive receivers sre not allowed
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
     * To set the global variable _bundleIDs. It will call the function
     * splitImport(to, ";") to get a list, which stores the Bundle IDs
     *
     * @param bundleIDs
     */
    public void setBundleIDs(String bundleIDs) {
        _bundleIDs = splitImport(bundleIDs, ";");
    }

    /**
     * If _bundleIDs is not setted, it will call setBundleIDs(bundleIDs). Else,
     * it will add the new IDs into the list _bundleIDs.
     *
     * @param bundleIDs
     */
    public void addBundleIDs(String bundleIDs) {
        if (_bundleIDs == null) {
            setBundleIDs(bundleIDs);
        } else {
            List<String> bIDs = splitImport(bundleIDs, ";");
            if (!bIDs.isEmpty()) {
                for (String buID : bIDs) {
                    if (!_bundleIDs.contains(buID)) { // repetitive Bundle IDs sre not allowed
                        _bundleIDs.add(buID);
                    } else {
                        logger.info("Bundle IDs: " + buID + " is not allowed");
//                        logger.info("Repetitive Receivers: " + buID + " is not allowed");
                    }
                }
            }
        }
    }

    /**
     * To set the global variable _subject.
     *
     * @param subject
     */
    public void setSubject(String subject) {
        _subject = subject;
    }

    /**
     * To set the global variable _message.
     *
     * @param msg
     */
    public void setMessage(String msg) {
        _message = msg;
    }

    /**
     * To set the global variable _broadcast.
     *
     * @param broadcast
     */
    public void setBroadcast(boolean broadcast) {
        _broadcast = broadcast;
    }

    /**
     * To set the global variable _broadcastPlatform. If the platform is one of
     * the ios, android and windowsphone, it will be setted with the platform.
     *
     * @param platform
     */
    public void setBroadcastPlatform(String platform) {
        if (null != platform) {
            if (platform.toLowerCase().equals("ios") || platform.toLowerCase().equals("android") || platform.toLowerCase().equals("windowsphone")) {
                _broadcastPlatform = platform.toLowerCase();
            }
//        else {
//            _broadcastPlatform = "";
//        }
        }
    }

    /**
     * delete the Em space and En space in front and in back
     *
     * @param source
     * @return
     */
    private String trimSpace(String source) {
        return source == null ? source : source.replaceAll("^[\\s　]*|[\\s　]*$", "");
    }

    /**
     * To split the imported parameter, put the splited parameters into a list.
     * Repetitive ID is not allowed. If there is nothing to split, it will
     * return an empty list.
     *
     * @param str
     * @param split the split symbol
     * @return
     */
    public List<String> splitImport(String str, String split) {
        List<String> imports = new ArrayList();
        if (str != null && !str.isEmpty()) {
            if (str.contains(split)) {
                String[] strs = str.split(split);
                for (int i = 0; i < strs.length; i++) {
                    strs[i] = trimSpace(strs[i]);
                    if (!imports.contains(strs[i])) { // repetitive ID is not allowed
                        imports.add(strs[i]);
                    } else {
                        logger.info("Repetitive ID: " + strs[i] + " is not allowed");
//                        logger.info("Repetitive ID: " + strs[i] + " is not allowed");
                    }
                }
                imports.addAll(Arrays.asList(strs));
            } else {
                imports.add(str);
            }
        }
        return imports;
    }

    /**
     * If the Push is successfully sent, sets _sendSucessful with true and
     * _sendTime with the send time.
     *
     * @param sendSuccessful
     * @param date
     */
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
    public Object getAttribute(JEVisObject obj, String attName) throws JEVisException {
        JEVisAttribute att = obj.getAttribute(attName);
        if (att != null) { //check, whether the attribute exists.
            if (att.hasSample()) { //check, whether this attribute has values.
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
    public void setNotificationObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jenoti = notiObj;
            try {
                setReceivers(String.valueOf(getAttribute(notiObj, RECIPIENTS)));//the second parameter should one to one correspondance with the name in JEConfig
            } catch (IllegalArgumentException ex) {
                setReceivers(null);
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
                setBroadcast(Boolean.parseBoolean(String.valueOf(getAttribute(notiObj, BROADCAST))));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }
            try {
                setBroadcastPlatform(String.valueOf(getAttribute(notiObj, BROADCAST_PLATFORM)));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }
            try {
                _enabled = Boolean.valueOf(String.valueOf(getAttribute(notiObj, ENABLED)));
            } catch (IllegalArgumentException ex) {
                _enabled = false;
                logger.info(ex);
            }
            try {
                setBundleIDs(String.valueOf(getAttribute(notiObj, BUNDLE_IDS)));
            } catch (IllegalArgumentException ex) {
                setBundleIDs(null);
                logger.error(ex);
            }
        } else {
            logger.info(notiObj + " is not suitable for Push Notification");
        }
    }

    @Override
    public void setNotificationObject(JEVisObject notiObj, JEVisFile file) {

    }

    /**
     * Configured means: subject and message have content; broadcast is true or
     * at least one receiver exists.
     *
     * @return
     */
    public boolean isNotiConfigured() {
        boolean configured = false;
        if (_broadcast == true) {
            if (_subject != null && _message != null) {
                if (!_subject.isEmpty() && !_message.isEmpty()) {
                    configured = true;
                }
            }
        } else {
            if (_receivers != null && _subject != null && _message != null) {
                if (!_subject.isEmpty() && !_message.isEmpty() && !_receivers.isEmpty()) {
                    configured = true;
                }
            }
        }
        return configured;
    }

    @Override
    public String toString() {
        return "PushNotification{" + "_jenoti= " + _jenoti + ", _receivers=" + _receivers + ", _bundleIDs= " + _bundleIDs + ", _subject=" + _subject + ", _message=" + _message + ", _broadcast=" + _broadcast + ", _broadcastPlatform=" + _broadcastPlatform + ", _type=" + _type + ", _sendSucessful=" + _sendSuccessful + ", _sendTime=" + _sendTime + ", _enabled= " + _enabled + '}';
    }

    /**
     * check, whether the jevis object of type "Push Notification" and can be
     * used to set
     *
     * @param notiObj
     * @return
     */
    public boolean isConfigurationObject(JEVisObject notiObj) {
        try {
            return notiObj.getJEVisClass().getName().equals(_type);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

    public List<DateTime> getSendDate() {
        List<DateTime> sendDate = new ArrayList<DateTime>();
        try {
            JEVisAttribute att = this.getJEVisObjectNoti().getAttribute(PushNotification.SENT_TIME);
            if (att != null) {
                List<JEVisSample> times = att.getAllSamples();
                for (JEVisSample t : times) {
                    sendDate.add(t.getTimestamp());
                }
            } else {
                logger.info("The attribute " + SENT_TIME + " of " + getJEVisObjectNoti().getID() + " does not exist.");
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return sendDate;
    }

    public List<DateTime> getSendSchedule() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setNotification(List<String> str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
