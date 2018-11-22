/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.notifier.VIDA350;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author gf
 */
public class VIDA350NotificationDriver implements NotificationDriver {
    private static final Logger logger = LogManager.getLogger(VIDA350NotificationDriver.class);

    private JEVisObject _jeDri;
    //    private URL _;
//    private String _;
//    private int _;
//    private String _;
//    private String _;
    private final String _type = "VIDA350 Plugin";
    public static final String APPLICATIVE_NOTI_TYPE = "VIDA350 Notification";

    public VIDA350NotificationDriver() {
    }

//    public VIDA350NotificationDriver(VIDA350NotificationDriver vdnotiDri) {
//        if (vdnotiDri.isDriverConfigured()) {
//            
//        }
//    }

    /**
     * Send the VIDA350 Notification with socket the returned responce-status is
     * not known, so set the successful with "if(true)", later can be revised
     *
     * @param noti
     * @return
     */
    public boolean sendVIDA350Notification(VIDA350Notification noti) {
        boolean success = false;
        try {
            Socket client = new Socket(noti.getIP(), noti.getPort());
            Writer writer = new OutputStreamWriter(client.getOutputStream());
            writer.write(noti.getInstruction() + " " + noti.getSetTime());
            writer.flush();
//            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
//            client.setSoTimeout(30 * 1000);
//            StringBuffer sb = new StringBuffer();
//            String temp;
//            try {
//                while ((temp = br.readLine()) != null) {
//                    temp = new String(temp.getBytes(), "utf-8");
//                    sb.append(temp);
//                }
//            } catch (SocketTimeoutException e) {
//                logger.info("Data reading time out!");
////                logger.info("Data reading time out!");
//            }
////            logger.info("Server: " + sb);
            if (true) {
                noti.setSuccessfulSend(true, new DateTime(new Date()));
                success = true;
            }
            writer.close();
            client.close();
        } catch (UnknownHostException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        return success;
    }

    /**
     * return the global variable _type. Once this class is instantiated, _type
     * is fixed as "VIDA350 Plugin".
     *
     * @return
     */
    public String getDriverType() {
        return _type;
    }

    /**
     * return the global variable _jeDri.
     *
     * @return
     */
    public JEVisObject getJEVisObjectDriver() {
        return _jeDri;
    }

    /**
     * To send the Notification, the Notification must have the type: VIDA350
     * Notification. If the notification is sucessfully sent, returns true.
     * Else, returns false.
     *
     * @param jenoti
     * @return
     */
    public boolean sendNotification(Notification jenoti) {
        boolean successful = false;
        if (jenoti.getType().equals(APPLICATIVE_NOTI_TYPE)) {
            VIDA350Notification vidanoti = (VIDA350Notification) jenoti;
            successful = sendVIDA350Notification(vidanoti);
            return successful;
        } else {
            logger.info("This Notification is not the VIDA350Notification.");
            logger.info("This Notification is" + jenoti.getType() + ".");
//            logger.info("This Notification is not the VIDA350Notification.");
//            logger.info("This Notification is" + jenoti.getType() + ".");
            return successful;
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
     * If the notification has the type: VIDA350 Notification, then the driver
     * can support the notification. If supported, it only means, this driver
     * can send the notification. But if the driver is not configured or rightly
     * configured, the driver can not send the notification, even if it is
     * supported.
     *
     * @param jenoti
     * @return
     */
    public boolean isSupported(Notification jenoti) {
        boolean support;
        support = jenoti.getType().equals(APPLICATIVE_NOTI_TYPE);
        return support;
    }

    /**
     * If there are later some attributes in JEConfig to be build. This function
     * must be changed to import more attributes.
     *
     * @param notiObj
     * @throws JEVisException
     */
    public void setNotificationDriverObject(JEVisObject notiObj) throws JEVisException {
        if (notiObj.getJEVisClass().getName().equals(_type)) {
            _jeDri = notiObj;
//            try {
//            set(String.valueOf(getAttribute(notiObj, Attribute_Name)));
//            } catch (Exception ex) {
//                Logger.getLogger(VIDA350NotificationDriver.class.getName()).log(Level.ERROR, ex);
//            }
        } else {
            logger.info(notiObj + " is not suitable for Push Notification Driver");
        }
    }

    /**
     * store the send time into JEConfig
     *
     * @param noti
     * @return
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
                    logger.info("The attribute of the Notification " + noti.getJEVisObjectNoti().getID() + " does not exist.");
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return re;
    }

    /**
     * check, whether the jevis object of type "VIDA350 Plugin" and can be used
     * to set
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

    //    public boolean sendTimeRecorder(JEVisObject notiObj, Notification noti) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    public void setNotificationDriver(List<String> str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
