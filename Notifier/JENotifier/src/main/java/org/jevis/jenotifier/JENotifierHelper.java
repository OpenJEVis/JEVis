/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.BasicOption;
import org.jevis.commons.config.CommonOptions;
import org.jevis.jenotifier.config.JENotifierConfig;

/**
 *
 * @author gf
 */
public class JENotifierHelper {
    //Command line Parameter

    public static final String DB_HOST = "dbHost";
    public static final String DB_PORT = "dbPort";
    public static final String DB_USER = "dbUser";
    public static final String DB_PASSWORD = "dbPassword";
    public static final String DB_SCHEMA = "dbSchema";
    public static final String JEVIS_USER_NAME = "jevisUsername";
    public static final String JEVIS_USER_PW = "jevisUserPW";
//    public static final String URL = "url";
    public static final String MODE = "mode";
//    public static final String INTERVAL = "interval";
    public static final String NUMBER_THREAD = "numberThread";
    public static final String SERVICE_PORT = "ServicePort";
    public static final String NOTI_IDS = "notiIDs";
    public static final String NOTI_DRI_IDS = "notiDriverIDs";
    public static final String PATH_NOTIFIER_JAR = "pathOfJar";
    public static final String NAME_NOTIFIER_JAR = "nameOfJar";
    public static final String NAME_NOTI_CLASS = "nameOfNotiClass";
    public static final String NAME_Driver_CLASS = "nameOfDriverClass";
    public static final String DEBUG_LEVEL = "debugLevel";
//the explanation of the command line
    public static final String DB_HOST_EXP = "The Host of Database";
    public static final String DB_PORT_EXP = "The Port of Database";
    public static final String DB_USER_EXP = "The User of Database";
    public static final String DB_PASSWORD_EXP = "The Password of Database";
    public static final String DB_SCHEMA_EXP = "The Schema of Database";
    public static final String JEVIS_USER_NAME_EXP = "Jevis User Name";
    public static final String JEVIS_USER_PW_EXP = "Jevis User Password";
//    public static final String URL_EXP = "url";
    public static final String MODE_EXP = "The running mode of JENotifier";
//    public static final String INTERVAL_EXP = "The Interval to send the Notifications";
    public static final String NUMBER_THREAD_EXP = "The maxmum number of the Thread";
    public static final String SERVICE_PORT_EXP = "The port of the Service: 5120";
    public static final String NOTI_IDS_EXP = "The IDs of Notifications in Database";
    public static final String NOTI_DRI_IDS_EXP = "The IDs of Notification Drivers in Database";
    public static final String PATH_NOTIFIER_JAR_EXP = "The Path of the Notifier Program";
    public static final String NAME_NOTIFIER_JAR_EXP = "The Name of the Notifier Program";
    public static final String NAME_NOTI_CLASS_EXP = "The full Name of Notification classes, split by \";\"";
    public static final String NAME_Driver_CLASS_EXP = "The full Name of NotificationDriver classes, split by \";\"";
    public static final String DEBUG_LEVEL_EXE = "Debug Level";

    private List<JEVisOption> configuration;

    /**
     * connect the database
     */
    public static JEVisDataSource getConnectedDatabase(JENotifierConfig con) {
        JEVisDataSource ds = null;
        try {
            JEVisOption host = new BasicOption(CommonOptions.DataSource.DataSource.getKey(),con.getDBHost(),"");


//            ds = new JEVisDataSourceSQL(con.getDBHost(), con.getDBPort(), con.getDBSchema(), con.getDBUser(), con.getDBPassword());
            //TODO: use the new jevis cli
            ds = new org.jevis.jeapi.ws.JEVisDataSourceWS();

            ds.connect(con.getJEVisUserName(), con.getJEVisUserPassword());
        } catch (JEVisException ex) {
            Logger.getLogger(JENotifierHelper.class.getName()).log(Level.ERROR, null, ex);
        }
        return ds;
    }

    /**
     * To initialize the level of the root Logger
     *
     * @param debugLevel
     */
    public static void initializeLogger(Level debugLevel) {
        Logger.getRootLogger().setLevel(debugLevel);
    }

//    private void initNewAppender(String NameForAppender, String Name4LogFile) {
////        logger = Logger.getLogger(NameForAppender); //NOT DEFAULT BY "logger = Logger.getLogger(TestJob.class);"
//
//        FileAppender appender = new FileAppender();
//        appender.setLayout(new PatternLayout("[%d{dd MMM yyyy HH:mm:ss}][%c{2}]: %-10m%n"));
//        appender.setFile(Name4LogFile);
//        appender.setAppend(true);
//        appender.setImmediateFlush(true);
//        appender.activateOptions();
//        appender.setName(NameForAppender);
//        appender.addFilter(new ThreadFilter(NameForAppender));
//        logger.setAdditivity(false);    //<--do not use default root logger
//        logger.addAppender(appender);
//}
    /**
     * To delete the Em space and En space in front and after the string.
     *
     * @param source
     * @return
     */
    public static String trimSpace(String source) {
        return source == null ? source : source.replaceAll("^[\\s　]*|[\\s　]*$", "");
    }

    /**
     * To split all IDs ,convert them to the type Long and put them into the
     * list.
     *
     * @param id split by ;
     * @return
     */
    public static List<Long> splitIDs(String id) {
        List<Long> notiIDs = new ArrayList<>();
        if (id != null && !id.equals("")) {
            if (id.contains(";")) {
                String[] strs = id.split(";");
                for (int i = 0; i < strs.length; i++) {
                    strs[i] = trimSpace(strs[i]);
                    if (!notiIDs.contains(Long.parseLong(strs[i]))) { // repetitive id is not allowed
                        notiIDs.add(Long.parseLong(strs[i]));
                    }
                }
            } else {
                notiIDs.add(Long.parseLong(trimSpace(id)));
            }
        }
        return notiIDs;
    }

    /**
     * To split all imports and put it into the list.
     *
     * @param str split by ;
     * @return
     */
    public static List<String> split(String str) {
        List<String> classes = new ArrayList<>();
        if (str != null && !str.equals("")) {
            if (str.contains(";")) {
                String[] strs = str.split(";");
                for (int i = 0; i < strs.length; i++) {
                    strs[i] = trimSpace(strs[i]);
                    if (!classes.contains(strs[i])) { // repetitive id is not allowed
                        classes.add(strs[i]);
                    }
                }
            } else {
                classes.add(str);
            }
        }
        return classes;
    }
}
