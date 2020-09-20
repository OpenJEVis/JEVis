/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JENotifier-EP.
 * <p>
 * JENotifier-EP is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JENotifier-EP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JENotifier-EP. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JENotifier-EP is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jenotifier.notifier.SQL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jb
 */
public class SQLNotificationDriver implements NotificationDriver {
    private static final Logger logger = LogManager.getLogger(SQLNotificationDriver.class);

    private JEVisObject _jeDri;
    private final String _type = "SQL Plugin";
    public static final String APPLICATIVE_NOTI_TYPE = "SQL Notification";

    public SQLNotificationDriver() {
    }

    /**
     * To send the Notification, the Notification must have the type SQL
     * Notification. If it is sent seccessfully, return true. Else return false.
     *
     * @return the send status
     */
    public boolean sendNotification(Notification jenoti) {
        boolean successful = false;
        if (jenoti.getType().equals(APPLICATIVE_NOTI_TYPE)) {
            SQLNotification sqlnoti = (SQLNotification) jenoti;
            try {
                successful = sendSQLNotification(sqlnoti);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        } else {
            logger.info("This Notification is not the SQLNotification.");
            logger.info("This Notification is {}.", jenoti.getType());
        }
        return successful;
    }

    @Override
    public boolean sendNotification(Notification jenoti, String customMessage) {
        return false;
    }

    /**
     * All necessary parameters will be configured to send the SQLNotification.
     * If the SQLNotification is sucessfully sent, returns true. Else, returns
     * false.
     *
     * @param sqlnoti
     * @return the send status
     * @throws JEVisException
     */
    public boolean sendSQLNotification(SQLNotification sqlnoti) throws JEVisException {
        try {
            Connection con = sqlnoti.getConnectedInputDataBase(sqlnoti.getSqltype());
            for (SQLQuery query : sqlnoti.getQuerys()) {
                PreparedStatement preparedstatement;
                String updateString = query.handleSQLQuery();
                preparedstatement = con.prepareStatement(updateString);
                List<JEVisSample> samples = query.fetchSamples(sqlnoti.getDs());
                for (JEVisSample sample : samples) {
                    preparedstatement = query.setValueInprepareStatement(preparedstatement, sample);
                    preparedstatement = query.setTimestampInprepareStatement(preparedstatement, sample);
//                    Logger.getLogger(SQLNotificationDriver.class.getName()).log(Level.ERROR, "This Notification is" + preparedstatement + ".");
                    preparedstatement.executeUpdate();
                }
                preparedstatement.close();
            }
            con.close();
            sqlnoti.setSuccessfulSend(true, new DateTime(new Date()));
            return true;
        } catch (SQLException ex) {
            logger.fatal(ex);
            return false;
        }
    }

    /**
     * Because there is nothing to configure for this Driver,so directly return
     * true. If later there is something to configure, this function must be
     * changed.
     *
     * @return
     */
    public boolean isDriverConfigured() {
        return true;
    }

    /**
     * If the notification has the type: SQL Notification, then the driver can
     * support the notification. If supported, it only means, this driver can
     * send the SQL Notification. But if the driver is not configured or rightly
     * configured, the driver can not send the SQL, even if it is supported.
     *
     * @param jenoti the JEVis notification
     * @return
     */
    public boolean isSupported(Notification jenoti) {
        boolean support;
        support = jenoti.getType().equals(APPLICATIVE_NOTI_TYPE);
        return support;
    }

    /**
     * Call the function getAttribute(,) to get parameters of the notification
     * in Database and use the setter to assign the global variables. If there
     * is an IllegalArgumentException, the complex variable will be assigned
     * with null and the simple variables will not be dealed. The information of
     * the exception will also be printed.
     *
     * @param notiObj the JEVis Object
     * @throws JEVisException
     */
    public void setNotificationDriverObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jeDri = notiObj;
        } else {
            logger.info("{} is not suitable for SQL Notification Driver", notiObj);
        }
    }

    /**
     * Store the send time into JEConfig.
     *
     * @param noti The JEVis Notification
     * @return time recorders
     */
    public boolean sendTimeRecorder(Notification noti) {
        boolean re = false;
        if (noti.isSendSuccessfully()) {
            try {
                List<JEVisSample> ts = new ArrayList<JEVisSample>();
                JEVisAttribute recorder = noti.getJEVisObjectNoti().getAttribute(Notification.SENT_TIME);
                if (recorder != null) {
                    for (DateTime time : noti.getSendTime()) {
                        JEVisSample t = recorder.buildSample(time, noti.getJEVisObjectNoti().getID(), "Sent by Driver " + getJEVisObjectDriver().getID());
                        ts.add(t);
                    }
                    recorder.addSamples(ts);
                    re = true;
                } else {
                    logger.info("The attribute of the Notification {} does not exist.", noti.getJEVisObjectNoti().getID());
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return re;
    }

    /**
     * Check, whether the jevis object of type "SQL Plugin" and can be used to
     * set.
     *
     * @param driverObj
     * @return
     */
    public boolean isConfigurationObject(JEVisObject driverObj) {
        try {
            return driverObj.getJEVisClass().getName().equals(_type);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

    public JEVisObject getJEVisObjectDriver() {
        return _jeDri;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "SQL Plugin".
     *
     * @return
     */
    public String getDriverType() {
        return _type;
    }

    public void setNotificationDriver(List<String> str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
