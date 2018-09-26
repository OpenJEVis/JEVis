/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.VIDA350;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.jenotifier.notifier.Notification;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gf
 */
public class VIDA350Notification implements Notification {
    private static final Logger logger = LogManager.getLogger(VIDA350Notification.class);

    private JEVisObject _jenoti;
    //    private List<String> _ip;
    private String _ip;
    private String _setTime;
    private String _instruction;
    private int _port;
    private List<DateTime> _sendTime;
    private boolean _enabled;
    private final String _type = "VIDA350 Notification";
    private boolean _sendSuccessful;
    //
    public static final String INSTRUCTION = "Instruction";
    public static final String IP = "IP";
    public static final String SET_TIME = "Set Time";
    public static final String PORT = "Port";
//    public static final String SENT_TIME = "Sent Time";
//    public static final String ENABLED = "Enabled";

    public VIDA350Notification() {
    }

    public VIDA350Notification(VIDA350Notification vdnoti) {
        if (vdnoti.isNotiConfigured()) {
            _jenoti = vdnoti.getJEVisObjectNoti();
            _ip = vdnoti.getIP();
            _setTime = vdnoti.getSetTime();
            _instruction = vdnoti.getInstruction();
            _port = vdnoti.getPort();
            _enabled = vdnoti.isSendEnabled();
        }
    }

    /**
     * return the IP address of VIDA350 (_ip)
     *
     * @return
     */
    public String getIP() {
        return _ip;
    }

    /**
     * return the start time to be set (_setTime)
     *
     * @return
     */
    public String getSetTime() {
        return _setTime;
    }

    /**
     * return the instruction for VIDA350 (_instruction)
     *
     * @return
     */
    public String getInstruction() {
        return _instruction;
    }

    /**
     * return the port of VIDA350 (_port)
     *
     * @return
     */
    public int getPort() {
        return _port;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "VIDA350 Notification".
     *
     * @return
     */
    public String getType() {
        return _type;
    }

    /**
     * To check, if the EmailNotification has been sent.
     *
     * @return
     */
    public boolean isSendSuccessfully() {
        return _sendSuccessful;
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
     * return the global variable _jenoti.
     *
     * @return
     */
    public JEVisObject getJEVisObjectNoti() {
        return _jenoti;
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
     * to set the IP address of VIDA350
     *
     * @param ip
     */
    public void setIP(String ip) {
        _ip = ip;//
    }

    /**
     * set the start time for VIDA350
     *
     * @param subject
     */
    public void setSetTime(String time) {
        _setTime = time;
    }

    /**
     * set the instruction of the message
     *
     * @param msg
     */
    public void setInstruction(String instruction) {
        _instruction = instruction;
    }

    /**
     * set the port of VIDA350
     *
     * @param port
     */
    public void setPort(int port) {
        _port = port;
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
    public void setNotificationObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jenoti = notiObj;
            try {
                setIP(String.valueOf(getAttribute(notiObj, IP)));
            } catch (IllegalArgumentException ex) {

                logger.info(ex);
            }
            try {
                setInstruction(String.valueOf(getAttribute(notiObj, INSTRUCTION)));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }
            try {
                setSetTime(String.valueOf(getAttribute(notiObj, SET_TIME)));
            } catch (IllegalArgumentException ex) {
                logger.info(ex);
            }
            try {
                setPort(Integer.parseInt(String.valueOf(getAttribute(notiObj, PORT))));
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
            logger.info(notiObj + " is not suitable for Push Notification");
        }
    }

    @Override
    public void setNotificationObject(JEVisObject notiObj, JEVisFile file) {

    }

    /**
     * If the Email is successfully sent, sets _sendSucessful with true and
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
     * @return
     */
    public boolean isNotiConfigured() {
        return null != getSetTime() && !getSetTime().isEmpty() && null != getInstruction() && !getInstruction().isEmpty() && null != getIP() && !getIP().isEmpty();
    }

    /**
     * check, whether the jevis object of type "E-Mail Notification" and can be
     * used to set
     *
     * @param notiObj
     * @return
     */
    public boolean isConfigurationObject(JEVisObject notiObj) {
        try {
            return notiObj.getJEVisClass().getName().equals(_type);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return false;
    }

    public List<DateTime> getSendDate() {
        List<DateTime> sendDate = new ArrayList<DateTime>();
        try {
            JEVisAttribute att = this.getJEVisObjectNoti().getAttribute(VIDA350Notification.SENT_TIME);
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

    public List<DateTime> getSendSchedule() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setNotification(List<String> str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
