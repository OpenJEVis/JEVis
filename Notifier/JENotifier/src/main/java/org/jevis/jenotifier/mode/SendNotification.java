/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import java.io.PrintWriter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;

/**
 *
 * @author gf
 */
public class SendNotification implements Runnable {

    private Notification _noti;
    private NotificationDriver _driver;
    private PrintWriter _writer;

    public SendNotification(Notification noti, NotificationDriver notiDri) {
        _noti = noti;
        _driver = notiDri;
    }

    public SendNotification(Notification notiObj, NotificationDriver notiDriObj, PrintWriter writer) {
        this(notiObj, notiDriObj);
//        _socket = socket;
        _writer = writer;
    }

    @Override
    public void run() {
        Logger.getLogger(Single.class.getName()).log(Level.DEBUG, "------- " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + " is configured: " + _driver.isDriverConfigured() + " -------"
                + "\n" + "------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " is configured: " + _noti.isNotiConfigured() + " -------"
                + "\n" + "------- " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + " supports " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + ": " + _driver.isSupported(_noti) + " -------");
//        System.out.println(_noti.isNotiConfigured());
//        System.out.println(_driver.isDriverConfigured());
//        System.out.println(_driver.isSupported(_noti));
        if (_noti.isNotiConfigured() && _driver.isDriverConfigured()) {
            if (_driver.isSupported(_noti)) {
//                synchronized (SendNoti.class) {
                _driver.sendNotification(_noti);
//                }
                if (_noti.isSendSuccessfully()) {
                    _driver.sendTimeRecorder(_noti);
                    Logger.getLogger(Single.class.getName()).log(Level.INFO, "------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Sent sucessfully by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                    if (_writer != null) {
                        _writer.println("------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Sent sucessfully by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                        _writer.flush();
                    }
                } else {
                    Logger.getLogger(Single.class.getName()).log(Level.INFO, "------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Send failed by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                    if (_writer != null) {
                        _writer.println("------- " + _noti.getJEVisObjectNoti().getName() + " " + _noti.getJEVisObjectNoti().getID() + " Send failed by " + _driver.getJEVisObjectDriver().getName() + " " + _driver.getJEVisObjectDriver().getID() + "-------");
                        _writer.flush();
                    }
                }
            }
        }
    }
}
