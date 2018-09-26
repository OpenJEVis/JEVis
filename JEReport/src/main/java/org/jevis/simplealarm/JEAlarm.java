/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.simplealarm.limitalarm.DynamicLimitAlarm;
import org.jevis.simplealarm.limitalarm.ILimitAlarm;
import org.jevis.simplealarm.limitalarm.StaticLimitAlarm;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * Quik and dirty JEVis alarming solution.
 *
 * Notes: - Static and Dynamic alarm have a lot of duplicated code - Connection
 * parameters are hard coded , need to be start parameters - Mail sender is also
 * hard coded, needs to be configured via JEVis db
 *
 * @author fs
 */
public class JEAlarm {
    private static final Logger logger = LogManager.getLogger(JEAlarm.class);
    public static final DecimalFormat deci = new DecimalFormat("#.00");
    public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZZ");
    JEVisDataSource ds = null;

    public JEAlarm() {
        logger.info("JEAlarm version 2017-02-07");
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            logger.info("Missing parameter for config file");
        }
        File file = new File(args[0]);

        if (!file.canRead()) {
            logger.info("Cannot read config file");
        }

    }

    //TODO: Overwrite
    public void executeAlarm() {

        JEVisClass dynamicAlarmClass = null;
        JEVisClass staticAlarmClass = null;
        try {
            dynamicAlarmClass = ds.getJEVisClass(DynamicLimitAlarm.ALARM_CLASS);
            staticAlarmClass = ds.getJEVisClass(StaticLimitAlarm.ALARM_CLASS);
        } catch (Exception ex) {
            logger.error("Dynamic and/or Static Limit Alarm Class not found or wrong", ex);
        }

        List<JEVisObject> dynamicAlarmObjects = null;
        try {
            dynamicAlarmObjects = ds.getObjects(dynamicAlarmClass, true);
        } catch (Exception ex) {
            logger.error("Cant get Dynamic Alarm JEVis Object", ex);
        }
        List<JEVisObject> staticAlarmObjects = null;
        try {
            staticAlarmObjects = ds.getObjects(staticAlarmClass, true);
        } catch (Exception ex) {
            logger.error("Cant get Static Alarm JEVis Object", ex);
        }

        showAlarmOverview(staticAlarmObjects);
        showAlarmOverview(dynamicAlarmObjects);

        //Dynamic Alarms------TODO -> write logger -----------------------------------------
        for (JEVisObject alarm : dynamicAlarmObjects) {
            try {
                logger.info("\n\nCheck Alarm: [" + alarm.getID() + "]" + alarm.getName());
                ILimitAlarm sAlarm = new DynamicLimitAlarm(alarm);
                sAlarm.init();
                sAlarm.checkAlarm();
            } catch (Exception ex) {
                logger.error("Error while creating Dynamic Alarm");
                ex.printStackTrace();
            }

        }

        //Static Alarms-----------------------------------------------
        for (JEVisObject alarm : staticAlarmObjects) {
            try {
                logger.info("\n\nCheck Alarm: [" + alarm.getID() + "]" + alarm.getName());
                ILimitAlarm sAlarm = new StaticLimitAlarm(alarm);
                sAlarm.init();
                sAlarm.checkAlarm();
            } catch (Exception ex) {
                logger.error("Error while processing Static Alarm");
                ex.printStackTrace();
            }
        }

    }

    //TODO sys.o.pri -> logger
    private void showAlarmOverview(List<JEVisObject> alarmObjects) {

        logger.info("Found " + alarmObjects.size() + " Dynamic Alarm Objects.");
        for (JEVisObject alarm : alarmObjects) {
            logger.info("-> " + alarm.getID() + " " + alarm.getName());
            try {
                JEVisObject parent = alarm.getParents().get(0);
                logger.info(" | " + parent.getID() + " " + parent.getName());

                JEVisObject parentParent = parent.getParents().get(0);
                logger.info(" | " + parentParent.getID() + " " + parentParent.getName());

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }
}
