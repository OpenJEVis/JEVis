/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jenotifier.notifier.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gf
 */
public class NotificationQueue {
    private static final Logger logger = LogManager.getLogger(NotificationQueue.class);

    private List<Class> _notiClass;
    private List<JEVisObject> _notiObjs;
    //    private static List<Notification> _emailNotis = new ArrayList<>();
//    private static List<Notification> _pushNotis = new ArrayList<>();
    private List<Notification> _notis;

    //////    private synchronized JEVisDataSource ds = new JEVisDataSourceSQL(null, null, null, null, null);
    public NotificationQueue(List<JEVisObject> notiObjs, List<Class> notiClass) {
        _notis = new ArrayList<>();
        _notiObjs = notiObjs;
        _notiClass = notiClass;
    }

//////    private void test(){
//////        run{
//////        Notification n1 = new Notification(ds);
//////        n1.ds.writesomething
//////    }
//////        run{
//////        Notification n2 = new Notification(ds);
//////        n1.ds.writesomething
//////    }
//////    }
//    public List<Notification> getEmailNotification() {
//        return _emailNotis;
//    }
//
//    public List<Notification> getPushNotification() {
//        return _pushNotis;
//    }

    /**
     * To get the notifications, which are already initialized by jevis objects.
     *
     * @return
     */
    public List<Notification> getNotifications() {
        return _notis;
    }

    /**
     * Create different object of classes, which implements Notification, and
     * initialize the notification with the jevis objects.
     */
    public void prepareNotification() {
        try {
            for (Class cl : _notiClass) {
                Notification noti = (Notification) cl.newInstance();
                for (JEVisObject notiObj : _notiObjs) {
                    if (noti.isConfigurationObject(notiObj)) {
                        noti.setNotificationObject(notiObj);
//                        if (noti.getType().equalsIgnoreCase("e-mail notification")) {
//                            _emailNotis.add(noti);
//                        }
//                        if (noti.getType().equalsIgnoreCase("push notification")) {
//                            _pushNotis.add(noti);
//                        }
                        _notis.add(noti);
                        noti = (Notification) cl.newInstance();//must build a new instance. If not, the driver will be all set by the last notification object from JEConfig, when there are more than one notifications of the same type!!!
                    }
                }
            }
        } catch (InstantiationException ex) {
            logger.fatal(ex);
        } catch (IllegalAccessException ex) {
            logger.fatal(ex);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }
}
