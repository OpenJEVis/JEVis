/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.config;

import org.apache.logging.log4j.Level;
import org.jevis.commons.cli.JEVisCommandLine;
import org.jevis.jenotifier.JENotifierHelper;

import java.util.List;

/**
 * @author gf
 */
public class JENotifierConfig {

    //    private String _url;
    private String _jevisUserPW;
    private String _jevisUsername;
    private String _dbSchema;
    private String _dbPassword;
    private String _dbUser;
    private String _dbPort;
    private String _dbHost;
    private int _numberThread;
    private List<String> _nameOfNotiClass;
    private List<String> _nameOfDriverClass;
    private String _nameOfNotifierJar;
    private String _pathOfNotifierJar;
    private String _mode;
    private List<Long> _notiIDs;
    private List<Long> _notiDriIDs;
    //    private long _interval;
    private int _servicePort;
    private Level _debugLevel;

//    /**
//     *
//     * @return the URL of
//     */
//    public String getURL() {
//        return _url;
//    }

    /**
     * @return the host of the database
     */
    public String getDBHost() {
//        logger.info("code host in JENotifierConfig  " );
        return _dbHost;
    }

    /**
     * @return the port of the database
     */
    public String getDBPort() {
//        logger.info("code port in JENotifierConfig  " );
        return _dbPort;
    }

    /**
     * @return the user of the database
     */
    public String getDBUser() {
//        logger.info("code dbuser in JENotifierConfig  " );
        return _dbUser;
    }

    /**
     * @return the password of the database
     */
    public String getDBPassword() {
//        logger.info("code dbpassword in JENotifierConfig  ");
        return _dbPassword;
    }

    /**
     * @return the schema of the database
     */
    public String getDBSchema() {
//        logger.info("code schema in JENotifierConfig  ");
        return _dbSchema;
    }

    /**
     * @return the name of jevis name
     */
    public String getJEVisUserName() {
//        logger.info("code jevis user name in JENotifierConfig  " );
        return _jevisUsername;
    }

    /**
     * @return the password of jevis user
     */
    public String getJEVisUserPassword() {
//        logger.info("code jevis user pw in JENotifierConfig  ");
        return _jevisUserPW;
    }

    /**
     * @return the mode of JENotifier
     */
    public String getMode() {
//        logger.info("code mode in JENotifierConfig  " );
        return _mode;
    }

//    /**
//     *
//     * @return the interval time
//     */
//    public long getInterval() {
////        logger.info("code interval in JENotifierConfig  " );
//        return _interval;
//    }

    /**
     * @return the IDs of the notifications to be sent
     */
    public List<Long> getNotificationIDs() {
        return _notiIDs;
    }

    /**
     * @return the IDs of the drivers to be used
     */
    public List<Long> getNotificationDriverIDs() {
//        logger.info("code noti dri id in JENotifierConfig  " );
        return _notiDriIDs;
    }

    /**
     * @return the path of the Jar File (Notifier)
     */
    public String getPathNotifiier() {
//        logger.info("code path noti in JENotifierConfig  " );
        return _pathOfNotifierJar;
    }

    /**
     * @return the name of Jar File (Notifier)
     */
    public String getNameNotifier() {
//        logger.info("code name noti in JENotifierConfig  ");
        return _nameOfNotifierJar;
    }

    /**
     * @return the names of the notification classes to be loaded
     */
    public List<String> getClassNoti() {
        return _nameOfNotiClass;
    }

    /**
     * @return the names of the driver classes to be loaded
     */
    public List<String> getClassNotiDriver() {
        return _nameOfDriverClass;
    }

    /**
     * @return the max number of the threads
     */
    public int getNumberOfThread() {
//        logger.info(config.get(JENotifierHelper.NUMBER_THREAD));
        return _numberThread;
    }

    /**
     * @return the port of the service
     */
    public int getServicePort() {
        return _servicePort;
    }

    /**
     * It is used to change the default notification Ids in Configuration File.
     * Until now, it is only used in the mode Service.
     *
     * @return
     */
    public Level getDebugLevel() {
        return _debugLevel;
    }

    /**
     * @param notiIDs
     */
    public void changeNotificationIDs(List<Long> notiIDs) {
        _notiIDs = notiIDs;
    }

    /**
     * To get the configuration parameters form Config File and overwrite this
     * parameters with the parameter from command line, if there are some
     * parameters from command line
     *
     * @param command
     */
    public void parser(String[] command) {
        JEVisCommandLine cmd = SetOption.JENotifierCommandLine();
        cmd.parse(command);

        if (cmd.getConfigPath() != null && !cmd.getConfigPath().isEmpty()) {

            ConfigXMLParse cp = new ConfigXMLParse();
            cp.XMLToMap(cmd.getConfigPath());
//            _url = cp.getJEVisConfigParam().get(JENotifierHelper.URL);
            _jevisUserPW = cp.getJENotifierParam().get(JENotifierHelper.JEVIS_USER_PW);
            _jevisUsername = cp.getJENotifierParam().get(JENotifierHelper.JEVIS_USER_NAME);
            _dbSchema = cp.getJENotifierParam().get(JENotifierHelper.DB_SCHEMA);
            _dbPassword = cp.getJENotifierParam().get(JENotifierHelper.DB_PASSWORD);
            _dbUser = cp.getJENotifierParam().get(JENotifierHelper.DB_USER);
            _dbPort = cp.getJENotifierParam().get(JENotifierHelper.DB_PORT);
            _dbHost = cp.getJENotifierParam().get(JENotifierHelper.DB_HOST);
            _numberThread = Integer.parseInt(cp.getJENotifierParam().get(JENotifierHelper.NUMBER_THREAD));
            _nameOfNotifierJar = cp.getJENotifierParam().get(JENotifierHelper.NAME_NOTIFIER_JAR);
            _nameOfNotiClass = JENotifierHelper.split(cp.getJENotifierParam().get(JENotifierHelper.NAME_NOTI_CLASS));
            _nameOfDriverClass = JENotifierHelper.split(cp.getJENotifierParam().get(JENotifierHelper.NAME_Driver_CLASS));
            _pathOfNotifierJar = cp.getJENotifierParam().get(JENotifierHelper.PATH_NOTIFIER_JAR);
            _mode = cp.getJENotifierParam().get(JENotifierHelper.MODE);
            _notiIDs = JENotifierHelper.splitIDs(cp.getJENotifierParam().get(JENotifierHelper.NOTI_IDS));
            _notiDriIDs = JENotifierHelper.splitIDs(cp.getJENotifierParam().get(JENotifierHelper.NOTI_DRI_IDS));
//            _interval = Long.parseLong(cp.getJENotifierParam().get(JENotifierHelper.INTERVAL));
            _servicePort = Integer.parseInt(cp.getJENotifierParam().get(JENotifierHelper.SERVICE_PORT));
            _debugLevel = Level.toLevel(JENotifierHelper.trimSpace(cp.getJENotifierParam().get(JENotifierHelper.DEBUG_LEVEL)));

//            if (cmd.getValue(JENotifierHelper.URL) != null && !cmd.getValue(JENotifierHelper.URL).isEmpty()) {
//                _url = cmd.getValue(JENotifierHelper.URL);
//            }
            if (cmd.getServer() != null && !cmd.getServer().isEmpty()) {
                _dbHost = cmd.getServer();
            }

            for (int i = 0; i < command.length; i++) {
                if (command[i].contains("-jp")) {
                    _dbPort = String.valueOf(cmd.getPort());
                }
            } //the default value of port in cmd is 80, can only check command to overwrite the port

            if (cmd.getUser() != null && !cmd.getUser().isEmpty()) {
                _dbUser = cmd.getUser();
            }
            if (cmd.getPassword() != null && !cmd.getPassword().isEmpty()) {
                _dbPassword = cmd.getPassword();
            }
            for (int i = 0; i < command.length; i++) {
                if (command[i].contains("-d")) {
                    _debugLevel = cmd.getDebugLevel();
                }
            }
            if (cmd.getValue(JENotifierHelper.DB_SCHEMA) != null && !cmd.getValue(JENotifierHelper.DB_SCHEMA).isEmpty()) {
                _dbSchema = cmd.getValue(JENotifierHelper.DB_SCHEMA);
            }
            if (cmd.getValue(JENotifierHelper.JEVIS_USER_NAME) != null && !cmd.getValue(JENotifierHelper.JEVIS_USER_NAME).isEmpty()) {
                _jevisUsername = cmd.getValue(JENotifierHelper.JEVIS_USER_NAME);
            }
            if (cmd.getValue(JENotifierHelper.JEVIS_USER_PW) != null && !cmd.getValue(JENotifierHelper.JEVIS_USER_PW).isEmpty()) {
                _jevisUserPW = cmd.getValue(JENotifierHelper.JEVIS_USER_PW);
            }
            if (cmd.getValue(JENotifierHelper.MODE) != null && !cmd.getValue(JENotifierHelper.MODE).isEmpty()) {
                _mode = cmd.getValue(JENotifierHelper.MODE);
            }
//            if (cmd.getValue(JENotifierHelper.INTERVAL) != null && !cmd.getValue(JENotifierHelper.INTERVAL).isEmpty()) {
//                _interval = Long.parseLong(cmd.getValue(JENotifierHelper.INTERVAL));
//            }
            if (cmd.getValue(JENotifierHelper.NOTI_IDS) != null && !cmd.getValue(JENotifierHelper.NOTI_IDS).isEmpty()) {
                _notiIDs = JENotifierHelper.splitIDs(cmd.getValue(JENotifierHelper.NOTI_IDS));
            }
            if (cmd.getValue(JENotifierHelper.NOTI_DRI_IDS) != null && !cmd.getValue(JENotifierHelper.NOTI_DRI_IDS).isEmpty()) {
                _notiDriIDs = JENotifierHelper.splitIDs(cmd.getValue(JENotifierHelper.NOTI_DRI_IDS));
            }
            if (cmd.getValue(JENotifierHelper.PATH_NOTIFIER_JAR) != null && !cmd.getValue(JENotifierHelper.PATH_NOTIFIER_JAR).isEmpty()) {
                _pathOfNotifierJar = cmd.getValue(JENotifierHelper.PATH_NOTIFIER_JAR);
            }
            if (cmd.getValue(JENotifierHelper.NAME_NOTIFIER_JAR) != null && !cmd.getValue(JENotifierHelper.NAME_NOTIFIER_JAR).isEmpty()) {
                _nameOfNotifierJar = cmd.getValue(JENotifierHelper.NAME_NOTIFIER_JAR);
            }
            if (cmd.getValue(JENotifierHelper.NAME_NOTI_CLASS) != null && !cmd.getValue(JENotifierHelper.NAME_NOTI_CLASS).isEmpty()) {
                _nameOfNotiClass = JENotifierHelper.split(cmd.getValue(JENotifierHelper.NAME_NOTI_CLASS));
            }
            if (cmd.getValue(JENotifierHelper.NAME_Driver_CLASS) != null && !cmd.getValue(JENotifierHelper.NAME_Driver_CLASS).isEmpty()) {
                _nameOfDriverClass = JENotifierHelper.split(cmd.getValue(JENotifierHelper.NAME_Driver_CLASS));
            }
            if (cmd.getValue(JENotifierHelper.NUMBER_THREAD) != null && !cmd.getValue(JENotifierHelper.NUMBER_THREAD).isEmpty()) {
                _numberThread = Integer.parseInt(cmd.getValue(JENotifierHelper.NUMBER_THREAD));
            }
            if (cmd.getValue(JENotifierHelper.SERVICE_PORT) != null && !cmd.getValue(JENotifierHelper.SERVICE_PORT).isEmpty()) {
                _servicePort = Integer.parseInt(cmd.getValue(JENotifierHelper.SERVICE_PORT));
            }
        }
    }
}
