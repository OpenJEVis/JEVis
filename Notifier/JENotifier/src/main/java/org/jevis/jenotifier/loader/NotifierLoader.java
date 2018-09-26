/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.loader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jenotifier.config.JENotifierConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gf
 */
public class NotifierLoader {
    private static final Logger logger = LogManager.getLogger(NotifierLoader.class);

    //    private Class _noti;
//    private Class _notidri;
    private List<Class> _classNoti;
    private List<Class> _classDriver;
    private ClassLoader _loader;
    private String _filePath;
    private String _fileName;
    //    public static String EMAIL_NOTI_CLASS = "org.jevis.jenotifier.notifier.Email.EmailNotification";
//    public static String EMAIL_NOTI_DRI_CLASS = "org.jevis.jenotifier.notifier.Email.EmailNotificationDriver";
//    public static String PUSH_NOTI_CLASS = "org.jevis.jenotifier.notifier.AppPush.PushNotification";
//    public static String PUSH_NOTI_DRI_CLASS = "org.jevis.jenotifier.notifier.AppPush.PushNotificationDriver";
//    private String _notiClassName;
//    private String _notiDriClassName;
    private List<String> _classNameNoti;
    private List<String> _classNameDriver;

    /**
     * Initialise the variable NotifierLoader and build a Loader(URLClassLoader)
     *
     * @param con
     */
    public NotifierLoader(JENotifierConfig con) {
        _classNoti = new ArrayList<>();
        _classDriver = new ArrayList<>();
        _filePath = con.getPathNotifiier();
        _fileName = con.getNameNotifier();
        _classNameNoti = con.getClassNoti();
        _classNameDriver = con.getClassNotiDriver();
        _loader = loaderClass(_filePath, _fileName);
        logger.info("-------The Loader is ready-------");
    }

    public NotifierLoader(String filePath, String fileName, String classNameNoti, String classNameDriver) {
        _classNoti = new ArrayList<>();
        _classDriver = new ArrayList<>();
        _filePath = filePath;
        _fileName = fileName;
        _classNameNoti = new ArrayList<>();
        _classNameNoti.add(classNameNoti);
        _classNameDriver = new ArrayList<>();
        _classNameDriver.add(classNameDriver);
        _loader = loaderClass(_filePath, _fileName);
        logger.info("-------The Loader is ready-------");
    }

    public NotifierLoader(File notifierFile, String classNameNoti, String classNameDriver) {
        _classNoti = new ArrayList<>();
        _classDriver = new ArrayList<>();
        _classNameNoti = new ArrayList<>();
        _classNameNoti.add(classNameNoti);
        _classNameDriver = new ArrayList<>();
        _classNameDriver.add(classNameDriver);
        _loader = loaderClass(notifierFile);
        logger.info("-------The Loader is ready-------");
    }

//    /**
//     * Set the Name of the Notification and NotifitionDriver, which are to
//     * loaded. The Notification and the NotificationDriver should be matched.
//     *
//     * @param noti the full name of the Notification Class
//     * @param notidri the full name of the NotificationDriver Class
//     */
//    public void setClassName(String noti, String notidri) {
//        _notiClassName = noti;
//        _notiDriClassName = notidri;
//    }
//    /**
//     * Set the Name of the Notification, which is to loaded.
//     *
//     * @param noti the full name of the Notification Class
//     */
//    public void setNotiClassName(String noti) {
//        _notiClassName = noti;
//    }
//    /**
//     * Set the Name of the NotificationDriver, which is to loaded.
//     *
//     * @param notidri the full name of the NotificationDriver Class
//     */
//    public void setNotiDriClassName(String notidri) {
//        _notiDriClassName = notidri;
//    }

    /**
     * Load the Class: Notification and NotifitionDriver.
     */
    public void loadingClass() {
        for (String className : _classNameNoti) {
            try {
//                logger.info(_loader.loadClass(className));
                _classNoti.add(_loader.loadClass(className));
            } catch (ClassNotFoundException ex) {
                logger.fatal(ex);
            }
        }
        for (String className : _classNameDriver) {
            try {
                _classDriver.add(_loader.loadClass(className));
            } catch (ClassNotFoundException ex) {
                logger.fatal(ex);
            }
        }
    }

    /**
     * To get the all classes of notification, which implements the interface
     * Notification.
     *
     * @return
     */
    public List<Class> getNotiClasses() {
        return _classNoti;
    }

    /**
     * To get the all classes of driver, which implements the interface
     * NotificationDriver.
     *
     * @return
     */
    public List<Class> getDriverClasses() {
        return _classDriver;
    }

//    
//    /**
//     * Get the Instance of Notification. Before using this function, the Class
//     * should be loaded. (the function loadingClass or loadingNotiClass should
//     * be used first.)
//     *
//     * @return
//     */
//    public Notification getNotiInstance() {
//        Notification instance = null;
//        try {
//            instance = (Notification) _noti.newInstance();
//        } catch (InstantiationException ex) {
//            logger.fatal(ex);
//            Logger.getLogger("EXCEPTION").log(Level.ERROR, null, ex);
//        } catch (IllegalAccessException ex) {
//            logger.fatal(ex);
//            Logger.getLogger("EXCEPTION").log(Level.ERROR, null, ex);
//        }
//        return instance;//_noti;
//    }
//
//    /**
//     * Get the Instance of NotificationDriver. Before using this function, the
//     * Class should be loaded. (the function loadingClass or loadingNotiDriClass
//     * should be used first.)
//     *
//     * @return
//     */
//    public NotificationDriver getNotiDriInstance() {
//        NotificationDriver instance = null;
//        try {
//            instance = (NotificationDriver) _notidri.newInstance();
//        } catch (InstantiationException ex) {
//            logger.fatal(ex);
//            Logger.getLogger("EXCEPTION").log(Level.ERROR, null, ex);
//        } catch (IllegalAccessException ex) {
//            logger.fatal(ex);
//            Logger.getLogger("EXCEPTION").log(Level.ERROR, null, ex);
//        }
//        return instance;//_notidri;
//    }

    /**
     * Build one Instance of ClassLoader.
     *
     * @param filePath the path until the .jar file
     * @param fileName the name of the .jar file
     * @return
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private ClassLoader loaderClass(String filePath, final String fileName) {
        File dir = new File(filePath);
        File[] fileList = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(fileName);
            }
        });
        URL[] urls = new URL[1];
        try {
            urls[0] = fileList[0].toURI().toURL();
        } catch (MalformedURLException ex) {
            logger.fatal(ex);
        }
        ClassLoader cl = new URLClassLoader(urls);
        return cl;
    }

    private ClassLoader loaderClass(File notifierFile) {
        URL[] urls = new URL[1];
        try {
            urls[0] = notifierFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            logger.fatal(ex);
        }
        ClassLoader cl = new URLClassLoader(urls);
        return cl;
    }
}
