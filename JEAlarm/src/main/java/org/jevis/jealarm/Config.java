/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAlarm.
 *
 * JEAlarm is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAlarm is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAlarm. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAlarm is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jealarm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * This Class handels the configuration of the JEAlarm.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Config {

    public static final String CONFIG = "config";
    public static final String ALARM = "alarm";

    public String jevisUser;
    public String jevisPW;
    public String jevisURL;
    public int jevisPort;
    public String jevisSchema;
    public String jevisDBUser;
    public String jevisDBPassword;

    public String smtpUser;
    public String smtpPW;
    public String smtpServer;
    public boolean smtpSSL;
    public int smtpPort;
    public String smtpFrom;
    public String smtpSignatur;
    public List<Alarm> alarms = new ArrayList<>();

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

            jevisUser = configFile.getString("jevis.user");
            jevisPW = configFile.getString("jevis.password");
            jevisURL = configFile.getString("jevis.server");
            jevisPort = configFile.getInt("jevis.port");
            jevisSchema = configFile.getString("jevis.schema");
            jevisDBUser = configFile.getString("jevis.dbuser");
            jevisDBPassword = configFile.getString("jevis.dbpassword");

            smtpUser = configFile.getString("sender.user");
            smtpPW = configFile.getString("sender.password");
            smtpServer = configFile.getString("sender.smtp");
            smtpSSL = configFile.getBoolean("sender.ssl");
            smtpPort = configFile.getInt("sender.port");
            smtpFrom = configFile.getString("sender.from");
            smtpSignatur = configFile.getString("sender.signatur");

//            System.out.println("jevisUser: " + jevisUser);
//            System.out.println("jevisPW: " + jevisPW);
//            System.out.println("jevisURL: " + jevisURL);
//            System.out.println("jevisPort: " + jevisPort);
//            System.out.println("smtpUser: " + smtpUser);
//            System.out.println("smtpPW: " + smtpPW);
//            System.out.println("smtpServer: " + smtpServer);
            int i = 0;
            while (configFile.getString("alarm(" + i + ").subject") != null) {
//                System.out.println("--------------");
//                System.out.println("Alarm[" + i + "]" + configFile.getString("alarm(" + i + ").subject"));
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
//                System.out.println("recipient: " + configFile.getStringArray("alarm(" + i + ").recipient").length);
                    alarms.add(alarm);
                } catch (Exception ex) {
                    System.out.println("Configiration error: " + ex);
                }
                i++;
            }
            System.out.println("--------------");

        } else {
            System.out.println("Abbort, configfile dows not exists");
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

}
