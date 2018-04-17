package org.jevis.jenotifier;

import org.jevis.api.JEVisException;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws JEVisException, InstantiationException, IllegalAccessException {
//        long notiID = 1482l;
//        long notiDriID = 1424l;
//
//        NotifierLoader loader1 = new NotifierLoader("/home/gf/NetBeansProjects/Notifier-EP/target/", "Notifier-EP-1.0-SNAPSHOT.jar");//, "org.jevis.jenotifier.notifier.Email.EmailNotificationDriver"
//
//        loader1.setClassName(NotifierLoader.EMAIL_NOTI_CLASS, NotifierLoader.EMAIL_NOTI_DRI_CLASS);
//        loader1.loadingClass();
//        NotificationDriver notiDri = loader1.getNotiDriInstance();
//
////        System.out.println(notiDri.getDriverType());
//
//        Notification noti = loader1.getNotiInstance();
//
//        JEVisDataSource ds = new JEVisDataSourceSQL("192.168.2.55", "3306", "jevis", "jevis", "jevistest");
//        ds.connect("Sys Admin", "JEV34Env");
//        JEVisObject notiObj = ds.getObject(notiID);
//        noti.setNotificationObject(notiObj);
//        JEVisObject notiDriObj = ds.getObject(notiDriID);
//        notiDri.setNotificationDriverObject(notiDriObj);
//
//        if (noti.isNotiConfigured() && notiDri.isDriverConfigured() && notiDri.isSupported(noti)) {
//
//            notiDri.sendNotification(noti);
//            System.out.println(notiDri.sendTimeRecorder(notiObj, noti));
//        }
//        if (noti.isSendSussesfully()) {
//            System.out.println("successfuly sent");
//// System.out.println((noti).getSendTime());
//        } else {
//            System.out.println("unsuccessfuly sent");
//        }
//        NotifierLoader loader1 = new NotifierLoader("/home/gf/NetBeansProjects/Notifier-EP/target/", "Notifier-EP-1.0-SNAPSHOT.jar");//, "org.jevis.jenotifier.notifier.Email.EmailNotificationDriver"
//
//        NotifierLoader loader = new NotifierLoader("/home/gf/NetBeansProjects/Notifier-EP/target/", "Notifier-EP-1.0-SNAPSHOT.jar");
//        
//        loader1.loading();
//        NotificationDriver notiDri = loader1.getNotificationDriver();
//
//        loader.loading();
//        NotificationDriver notiDri2 = loader.getNotificationDriver();
//
//        System.out.println(notiDri.getClass().equals(notiDri2.getClass()));
//
//        NotifierLoader_old loader1 = new NotifierLoader_old("/home/gf/NetBeansProjects/Notifier-EP/target/", "Notifier-EP-1.0-SNAPSHOT.jar");//, "org.jevis.jenotifier.notifier.Email.EmailNotificationDriver"
//
//        loader1.setClassDriName("org.jevis.jenotifier.notifier.Email.EmailNotificationDriver");
//        loader1.loading();
//        NotificationDriver notiDri = loader1.getNotificationDriver();
//
//        loader1.setClassDriName("org.jevis.jenotifier.notifier.AppPush.PushNotificationDriver");
//        loader1.loading();
//        NotificationDriver notiDri2 = loader1.getNotificationDriver();
//
//        System.out.println(notiDri.getClass().equals(notiDri2.getClass()));
//        ConfigXMLParse xml = new ConfigXMLParse();
//        xml.XMLToMap("/home/gf/NetBeansProjects/JENotifier/JENotifierConfig.xml");
//
//
//        System.out.println(xml.getter1().get("jevisUsername"));
    }
}
