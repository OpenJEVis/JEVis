/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

import java.util.List;

/**
 *
 * @author gf
 */
public interface Notification {

    String ENABLED = "Enabled";
    String SENT_TIME = "Sent Time";

    /**
     * Use the JEVis Object to initialize the Notfication
     *
     * @param notiObj
     * @throws org.jevis.api.JEVisException
     */
    void setNotificationObject(JEVisObject notiObj) throws JEVisException;

    /**
     * Use the JEVis Object to initialize the Notfication
     *
     * @param notiObj
     * @param file
     * @throws org.jevis.api.JEVisException
     */
    void setNotificationObject(JEVisObject notiObj, JEVisFile file) throws JEVisException;

    /**
     * Returns the type of the Notification
     *
     * @return
     */
    String getType();

    /**
     * If the Notification is successfully sent, sets the true and the send time
     *
     * @param sendSuccessful
     * @param date
     */
    void setSuccessfulSend(boolean sendSuccessful, DateTime date);
    
    /**
     * If the notification is sent successfully, it returns true. Else, it
     * returns false.
     *
     * @return
     */
    boolean isSendSuccessfully();

    /**
     * Check, if the Notification has the essential element, so that it can be
     * sent.
     *
     * @return
     */
    boolean isNotiConfigured();

    /**
     * To get the sent time of the notification, which is set during the
     * operation of program.
     *
     * @return
     */
    List<DateTime> getSendTime();

    /**
     * To check, whether the jevis object is of one of the type, which
     * implements interface Notification, so that the object can used to
     * initialize the notification.
     *
     * @param notiObj
     * @return
     */
    boolean isConfigurationObject(JEVisObject notiObj);

    /**
     * To get the jevis object, which is used to initialize the notification.
     *
     * @return
     */
    JEVisObject getJEVisObjectNoti();

    /**
     * To check, whether the notification is enabled to be sent. Whether it is
     * enabled, is configured in JEConfig.
     *
     * @return
     */
    boolean isSendEnabled();

    /**
     * To get the sent Time, which is stored in JEConfig under the attribute
     * "Sent Time".
     *
     * @return
     */
    List<DateTime> getSendDate();

    /**
     *
     * @return
     */
    List<DateTime> getSendSchedule();

    /**
     *
     * @param str
     */
    void setNotification(List<String> str);
}
