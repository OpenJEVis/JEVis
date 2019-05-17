/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.List;

/**
 *
 * @author gf
 */
public interface NotificationDriver {

    /**
     * Returns the type of driver.
     *
     * @return
     */
    String getDriverType();

    /**
     * Send the Notification. If the notification is successully sent, returns
     * true. If not, returns false.
     *
     * @param jenoti
     * @return
     */
    boolean sendNotification(Notification jenoti);

    /**
     * Send the Notification. If the notification is successully sent, returns
     * true. If not, returns false.
     *
     * @param jenoti
     * @return
     */
    boolean sendNotification(Notification jenoti, String customMessage);

    /**
     * Check, whether the notification driver is configured to send the notification.
     *
     * @return
     */
    boolean isDriverConfigured();

    /**
     * Check, whether Notification and Notification Driver are corresponding.
     *
     * @param jenoti
     * @return
     */
    boolean isSupported(Notification jenoti);

    /**
     * Set all global variables with the import parameters in database read by
     * JEVisObject.
     *
     * @param notiObj
     * @throws JEVisException
     */
    void setNotificationDriverObject(JEVisObject notiObj) throws JEVisException;

    /**
     *To store the sent Time in JEConfig(database)
     * @param noti
     * @return
     */
    boolean sendTimeRecorder(Notification noti);
//
//    boolean sendTimeRecorder(JEVisObject notiObj, Notification noti);

    /**
     * To check, whether the jevis object is of one of the type, which implements interface NotificationDriver, so that the object can used to initialize the driver.
     * @param driverObj
     * @return 
     */
    boolean isConfigurationObject(JEVisObject driverObj);

    /**
     * To get the jevis object, which is used to initialize the driver.
     * @return 
     */
    JEVisObject getJEVisObjectDriver();
    
    /**
     *
     * @param str
     */
    void setNotificationDriver(List<String> str);

}
