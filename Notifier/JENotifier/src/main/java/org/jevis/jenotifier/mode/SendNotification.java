/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;

import java.io.PrintWriter;

/**
 * @author gf
 */
public class SendNotification implements Runnable {
    private static final Logger logger = LogManager.getLogger(SendNotification.class);

    private final Notification _noti;
    private final NotificationDriver _driver;
    int retry = 0;
    private PrintWriter _writer;
    private String _customMessage;

    public SendNotification(Notification noti, NotificationDriver notiDri) {
        _noti = noti;
        _driver = notiDri;
    }

    public SendNotification(Notification noti, NotificationDriver notiDri, String customMessage) {
        _noti = noti;
        _driver = notiDri;
        _customMessage = customMessage;
    }

    public SendNotification(Notification notiObj, NotificationDriver notiDriObj, PrintWriter writer) {
        this(notiObj, notiDriObj);
//        _socket = socket;
        _writer = writer;
    }

    @Override
    public void run() {
        logger.debug("------- " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + " is configured: " + _driver.isDriverConfigured() + " -------"
                + "\n" + "------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " is configured: " + _noti.isNotiConfigured() + " -------"
                + "\n" + "------- " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + " supports " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + ": " + _driver.isSupported(_noti) + " -------");

        if (_noti.isNotiConfigured() && _driver.isDriverConfigured()) {
            if (_driver.isSupported(_noti)) {
                if (_customMessage == null) {
                    _driver.sendNotification(_noti);
                } else {
                    _driver.sendNotification(_noti, _customMessage);
                }
                if (_noti.isSendSuccessfully()) {
                    _driver.sendTimeRecorder(_noti);
                    logger.info("------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Sent successfully by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                    if (_writer != null) {
                        _writer.println("------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Sent successfully by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                        _writer.flush();
                    }
                } else {
                    logger.info("------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Send failed by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                    if (_writer != null) {
                        _writer.println("------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Send failed by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                        _writer.flush();
                    }

                    if (retry < 5) {
                        try {
                            retry++;
                            Thread.sleep(30000);
                            run();
                        } catch (Exception e) {
                            logger.error("Error in retry queue for report notification");
                        }
                    }
                }
            }
        } else {
            _writer.println("------- " + _noti.getJEVisObjectNoti().getName() + "is not configured correctly");
            _writer.flush();
        }
    }
}
