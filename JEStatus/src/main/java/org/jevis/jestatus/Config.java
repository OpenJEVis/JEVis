/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of Launcher.
 * <p>
 * Launcher is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * Launcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Launcher. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Launcher is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jestatus;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class handels the configuration of the Launcher.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Config {

    public static final String CONFIG = "config";
    public static final String ALARM = "alarm";
    private static final Logger logger = LogManager.getLogger(Config.class);
    private String message;
    private String greeting;
    private String subject;
    private String[] recipient;
    String smtpFrom;
    private String jevisUser;
    private String jevisPW;
    private String jevisURL;
    private int jevisPort;
    private String jevisSchema;
    private String jevisDBUser;
    private String jevisDBPassword;
    private String smtpUser;
    private String smtpPW;
    private String smtpServer;
    private boolean smtpSSL;
    private int smtpPort;
    private String smtpSignatur;
    private boolean smtpStartTLS;
    private final List<Alarm> alarms = new ArrayList<>();

    /**
     * Create an new Configuration out of an xml file
     *
     * @param file
     * @throws ConfigurationException
     */
    public Config(String file) throws ConfigurationException {
        File cFile = new File(file);
        if (cFile.exists()) {
            XMLConfiguration configFile = new XMLConfiguration(cFile);

//            jevisUser = configFile.getString("jevis.user");
//            jevisPW = configFile.getString("jevis.password");
//            jevisURL = configFile.getString("jevis.server");
//            jevisPort = configFile.getInt("jevis.port");
//            jevisSchema = configFile.getString("jevis.schema");
//            jevisDBUser = configFile.getString("jevis.dbuser");
//            jevisDBPassword = configFile.getString("jevis.dbpassword");

            smtpUser = configFile.getString("sender.user");
            smtpPW = configFile.getString("sender.password");
            smtpServer = configFile.getString("sender.smtp");
            smtpSSL = configFile.getBoolean("sender.ssl");
            smtpPort = configFile.getInt("sender.port");
            smtpFrom = configFile.getString("sender.from");
            smtpSignatur = configFile.getString("sender.signatur");
            smtpStartTLS = configFile.getBoolean("sender.starttls");

            recipient = configFile.getStringArray("alarm.recipient");
            subject = configFile.getString("alarm.subject");
            greeting = configFile.getString("alarm.greeting");
            message = configFile.getString("alarm.message");

//            logger.info("jevisUser: " + jevisUser);
//            logger.info("jevisPW: " + jevisPW);
//            logger.info("jevisURL: " + jevisURL);
//            logger.info("jevisPort: " + jevisPort);
//            logger.info("smtpUser: " + smtpUser);
//            logger.info("smtpPW: " + smtpPW);
//            logger.info("smtpServer: " + smtpServer);
            int i = 0;
            while (configFile.getString("alarm(" + i + ").subject") != null) {
//                logger.info("--------------");
//                logger.info("Alarm[" + i + "]" + configFile.getString("alarm(" + i + ").subject"));
                try {
                    Alarm alarm = new Alarm(
                            configFile.getString("alarm(" + i + ").subject"),
                            configFile.getStringArray("alarm(" + i + ").datapoint"),
                            configFile.getStringArray("alarm(" + i + ").recipient"),
                            configFile.getStringArray("alarm(" + i + ").bcc"),
                            configFile.getInt("alarm(" + i + ").timelimit"),
                            configFile.getInt("alarm(" + i + ").ignoreold"),
                            configFile.getString("alarm(" + i + ").greeting"),
                            configFile.getString("alarm(" + i + ").message"),
                            configFile.getBoolean("alarm(" + i + ").ignorefalse", true)
                    );
//                logger.info("recipient: " + configFile.getStringArray("alarm(" + i + ").recipient").length);
                    alarms.add(alarm);
                } catch (Exception ex) {
                    logger.info("Configuration error: ", ex);
                }
                i++;
            }
            logger.info("--------------");

        } else {
            logger.info("Abbort, configfile dows not exists");
        }

    }

    /**
     * Get the mail signartur for the sender.
     *
     * @return
     */
    public String getSmtpSignatur() {
        return smtpSignatur;
    }

    /**
     * Returns the user for the MySQL DB
     *
     * @return
     */
    public String getJevisDBUser() {
        return jevisDBUser;
    }

    /**
     * Returns the password for the MySQL DB user
     *
     * @return
     */
    public String getJevisDBPassword() {
        return jevisDBPassword;
    }

    /**
     * Returns returns the MySQl DB schema
     *
     * @return
     */
    public String getJevisSchema() {
        return jevisSchema;
    }

    /**
     * Returns the JEVis user
     *
     * @return
     */
    public String getJevisUser() {
        return jevisUser;
    }

    public String getJevisPW() {
        return jevisPW;
    }

    /**
     * Returns the JEVis password
     *
     * @return
     */
    public String getJevisURL() {
        return jevisURL;
    }

    /**
     * returns the port (mysql)
     *
     * @return
     */
    public int getJevisPort() {
        return jevisPort;
    }

    /**
     * returns the username for the smtp server
     *
     * @return
     */
    public String getSmtpUser() {
        return smtpUser;
    }

    /**
     * Returns the password for the smtp account
     *
     * @return
     */
    public String getSmtpPW() {
        return smtpPW;
    }

    /**
     * Returns the password for the smtp server
     *
     * @return
     */
    public String getSmtpServer() {
        return smtpServer;
    }

    /**
     * Return an list of configured alarms
     *
     * @return
     */
    public List<Alarm> getAlarms() {
        return alarms;
    }

    /**
     * Returns if the smtp sevrer supports ssl
     *
     * @return
     */
    public boolean isSmtpSSL() {
        return smtpSSL;
    }

    /**
     * returns the port of the smtp server
     *
     * @return
     */
    public int getSmtpPort() {
        return smtpPort;
    }

    /**
     * reports the from for the mail
     *
     * @return
     */
    public String getSmtpFrom() {
        return smtpFrom;
    }

    public boolean isSmtpStartTLS() {
        return smtpStartTLS;
    }

    public String getMessage() {
        return message;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getSubject() {
        return subject;
    }

    public String[] getRecipient() {
        return recipient;
    }
}
