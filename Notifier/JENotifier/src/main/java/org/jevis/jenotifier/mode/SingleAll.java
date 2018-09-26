/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.jenotifier.JENotifierHelper;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.jevis.jenotifier.loader.NotifierLoader;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author gf
 */
public class SingleAll {
    private static final Logger logger = LogManager.getLogger(SingleAll.class);

    //    private JENotifierConfig _config;
    private int _maxThread;
    //    private List<JEVisObject> _notiObj;
    private List<Long> _notiDriID;
    private JEVisDataSource _ds;
    private NotifierLoader _loader;
    private PrintWriter _writer;

    /**
     * This constructor is used in mode singleAll of mode Service
     *
     * @param config
     * @param writer
     */
    public SingleAll(JENotifierConfig config, PrintWriter writer) {
        this(config);
        _writer = writer;
    }

    /**
     * This constructor is used in mode singleAll
     *
     * @param config
     */
    public SingleAll(JENotifierConfig config) {
//        _config = config;
//        _notiID = ;
        _maxThread = config.getNumberOfThread();
        _notiDriID = config.getNotificationDriverIDs();
        _ds = JENotifierHelper.getConnectedDatabase(config);
        _loader = new NotifierLoader(config);
    }

    /**
     * Get all enabled notification from database. Start send the notification.
     * Handle with the multi threads.
     */
    public void start() {
        try {
            _loader.loadingClass();
//            logger.info(_maxThread);
            ExecutorService executor = Executors.newFixedThreadPool(_maxThread);
            //
            JEVisClass notification = _ds.getJEVisClass("Notification");
            List<JEVisObject> notiObj = _ds.getObjects(notification, true);
            List<JEVisObject> enabledNotiObj = EnabledNotificationFilter(notiObj);

            NotificationQueue nq = new NotificationQueue(enabledNotiObj, _loader.getNotiClasses());
            nq.prepareNotification();
            List<Notification> notis = nq.getNotifications();

            //
            List<JEVisObject> driverObjs = new ArrayList<>();
            for (long id : _notiDriID) {
                JEVisObject notiDriObj = _ds.getObject(id);
                driverObjs.add(notiDriObj);
            }
            DriverQueue dq = new DriverQueue(driverObjs, _loader.getDriverClasses());
            dq.prepareDriver();
            List<NotificationDriver> drivers = dq.getDrivers();

            for (Notification noti : notis) {
//                _loader.loadingClass();
                for (NotificationDriver notiDri : drivers) {
                    Runnable thr;
                    if (_writer != null) {
                        thr = new SendNotification(noti, notiDri, _writer);
                    } else {
                        thr = new SendNotification(noti, notiDri);
                    }
                    executor.execute(thr);
                }
            }
            // This will make the executor accept no new threads
            // and finish all existing threads in the queue
            executor.shutdown();
            // Wait until all threads are finish
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (JEVisException ex) {
            logger.error(ex);
        } catch (InterruptedException ex) {
            logger.error(ex);
        }

    }

    /**
     * Get the enabled notifications from all notifications
     *
     * @param allnoti
     * @return
     * @throws JEVisException
     */
    private List<JEVisObject> EnabledNotificationFilter(List<JEVisObject> allnoti) throws JEVisException {
        List<JEVisObject> enabledNoti = new ArrayList<>();
        for (JEVisObject noti : allnoti) {
            JEVisAttribute att = noti.getAttribute("Enabled");
            if (att != null) { //check, if the attribute exists.
                if (att.hasSample()) { //check, if this attribute has values.
                    JEVisSample enabled = att.getLatestSample();
                    if (enabled.getValue() != null && Boolean.valueOf(String.valueOf(enabled.getValue())) == true) { //check, if the value of this attribute is null.
                        enabledNoti.add(noti);
                    }
                }
            }
        }
        return enabledNoti;
    }
}
