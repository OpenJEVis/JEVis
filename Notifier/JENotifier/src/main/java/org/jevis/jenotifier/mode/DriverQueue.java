/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jenotifier.mode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jenotifier.notifier.NotificationDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gf
 */
public class DriverQueue {
    private static final Logger logger = LogManager.getLogger(DriverQueue.class);

    private List<Class> _driverClass;
    private List<JEVisObject> _driverObjs;
    //    public static List<NotificationDriver> _emailDrivers = new ArrayList<>();
//    public static List<NotificationDriver> _pushDrivers = new ArrayList<>();
    private List<NotificationDriver> _drivers;

    public DriverQueue(List<JEVisObject> driverObjs, List<Class> driverClass) {
        _drivers = new ArrayList<>();
        _driverObjs = driverObjs;
        _driverClass = driverClass;
    }

//    public List<NotificationDriver> getEmailDrivers() {
//        return _emailDrivers;
//    }
//
//    public List<NotificationDriver> getPushDrivers() {
//        return _pushDrivers;
//    }

    /**
     * To get the drivers, which are already initialized by jevis objects.
     *
     * @return
     */
    public List<NotificationDriver> getDrivers() {
        return _drivers;
    }

    /**
     * Create different object of classes, which implements NotificationDriver,
     * and initialize the driver with the jevis objects.
     */
    public void prepareDriver() {
        try {
            for (Class cl : _driverClass) {
                NotificationDriver driver = (NotificationDriver) cl.newInstance();
                for (JEVisObject driverObj : _driverObjs) {
                    if (driver.isConfigurationObject(driverObj)) {
                        driver.setNotificationDriverObject(driverObj);
//                        if (driver.getDriverType().equalsIgnoreCase("email plugin")) {
//                            _emailDrivers.add(driver);
//                        }
//                        if (driver.getDriverType().equalsIgnoreCase("email plugin")) {
//                            _pushDrivers.add(driver);
//                        }
                        _drivers.add(driver);
                        driver = (NotificationDriver) cl.newInstance();//must build a new instance. If not, the driver will be all set by the last driver object from JEConfig, when there are more than one drivers of the same type!!!
                    }
                }
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        } catch (InstantiationException ex) {
            logger.fatal(ex);
        } catch (IllegalAccessException ex) {
            logger.fatal(ex);
        }
    }
}
