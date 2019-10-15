/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jenotifier.JENotifierHelper;
import org.jevis.jenotifier.config.JENotifierConfig;
import org.jevis.jenotifier.loader.NotifierLoader;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author gf
 */
public class Single {
    private static final Logger logger = LogManager.getLogger(Single.class);

    //    private JENotifierConfig _config;
    private int _maxThread;
    private List<Long> _notiID;
    private List<Long> _notiDriID;
    private JEVisDataSource _ds;
    private NotifierLoader _loader;
    private PrintWriter _writer;

    /**
     * This constructor is used in mode single.
     *
     * @param config
     */
    public Single(JENotifierConfig config) {
//        _config = config;
        _maxThread = config.getNumberOfThread();
        _notiID = config.getNotificationIDs();
        _notiDriID = config.getNotificationDriverIDs();
        _ds = JENotifierHelper.getConnectedDatabase(config);
        _loader = new NotifierLoader(config);
    }

    public Single(Long notiId, Long notiDriID, JEVisDataSource datasource, String filePath, String fileName, String classNameNoti, String classNameDriver) {
//        _config = config;
        _maxThread = 1;
        _notiID = new ArrayList<>();
        _notiID.add(notiId);
        _notiDriID = new ArrayList<>();
        _notiDriID.add(notiDriID);
        _ds = datasource;
        _loader = new NotifierLoader(filePath, fileName, classNameNoti, classNameDriver);
    }

    public Single(Long notiId, Long notiDriID, JEVisDataSource datasource, File notifierFile, String classNameNoti, String classNameDriver) {
//        _config = config;
        _maxThread = 1;
        _notiID = new ArrayList<>();
        _notiID.add(notiId);
        _notiDriID = new ArrayList<>();
        _notiDriID.add(notiDriID);
        _ds = datasource;
        _loader = new NotifierLoader(notifierFile, classNameNoti, classNameDriver);
    }

    /**
     * This constructor is used in mode single of mode Service.
     *
     * @param config
     * @param writer
     */
    public Single(JENotifierConfig config, PrintWriter writer) {
        this(config);
        _writer = writer;
    }

    /**
     * start send the notification. Handle with the multi threads.
     */
    public void start() {
        try {
            _loader.loadingClass();
//            logger.info(_maxThread);
            ExecutorService executor = Executors.newFixedThreadPool(_maxThread);

            //
            List<JEVisObject> driverObjs = new ArrayList<>();
            for (long id : _notiDriID) {
                JEVisObject notiDriObj = _ds.getObject(id);
                driverObjs.add(notiDriObj);
            }
            DriverQueue dq = new DriverQueue(driverObjs, _loader.getDriverClasses());
            dq.prepareDriver();
            List<NotificationDriver> drivers = dq.getDrivers();

            //
            List<JEVisObject> notiObjs = new ArrayList<>();
            for (long id : _notiID) {
                JEVisObject notiObj = _ds.getObject(id);
                notiObjs.add(notiObj);
            }
            NotificationQueue nq = new NotificationQueue(notiObjs, _loader.getNotiClasses());
            nq.prepareNotification();
            List<Notification> notis = nq.getNotifications();

//            logger.info("+++++++++++++++++++++++++++++++++++++++");
            for (Notification id : notis) {
                for (NotificationDriver notiDri : drivers) {
                    Runnable thr;
                    if (_writer != null) {
                        thr = new SendNotification(id, notiDri, _writer);
                    } else {
                        thr = new SendNotification(id, notiDri);//fication
                    }
                    executor.execute(thr);
                }
            }
//            for (Notification id : notis) {
//                for (NotificationDriver notiDri : drivers) {
//                    Runnable thr;
//                    if (_writer != null) {
//                        thr = new SendNoti(id, notiDri, _writer);
//                    } else {
//                        thr = new SendNoti(id, notiDri);//fication
//                    }
//                    executor.executeExport(thr);
//                }
//            }

            // This will make the executor accept no new threads
            // and finish all existing threads in the queue
            executor.shutdown();
            // Wait until all threads are finish
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            logger.fatal(ex);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }
}
