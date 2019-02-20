/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm.deprecated.limitalarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.alarm.UsageSchedule;
import org.jevis.jealarm.ScheduleService;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ai
 */
public abstract class LimitAlarm implements ILimitAlarm {
    private static final Logger logger = LogManager.getLogger(LimitAlarm.class);
    private final int SILENT = 1;
    private final int STANDBY = 2;
    public final static String SILENT_TIME = "Silent Time";
    public final static String STANDBY_TIME = "Standby Time";
    public final static String STATUS = "Status";
    public final static String ENABLED = "Enable";
    public final static String ALARM_LOG = "Alarm Log";
    public final static String OPERATOR_ATTRIBUTE = "Operator";
    final JEVisObject alarmObj;
    JEVisAttribute _status;
    boolean _enabled = false;
    JEVisAttribute _log;
    private List<UsageSchedule> up = new ArrayList<>();

    @Override
    abstract public void checkAlarm() throws JEVisException;

    public enum OPERATOR {
        SMALER, BIGGER, EQUALS, UNEQUALS
    }

    public LimitAlarm(JEVisObject alarm) {
        alarmObj = alarm;
    }

    @Override
    public void init() {
        try {
            _log = alarmObj.getAttribute(ALARM_LOG);
            _status = alarmObj.getAttribute(STATUS);
            _enabled = isEnabled(alarmObj);
            for (JEVisAttribute att : alarmObj.getAttributes()) {
                logger.info("   Attribute: " + att);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initUsageTime();
    }

    //public abstract void sendAlarm() throws JEVisException;
    void logAlarms(JEVisAttribute logAtt, List<AlarmData> alarms) {
        int lastLog = 0;
        try {
            JEVisSample lstLog = logAtt.getLatestSample();
            if (lstLog != null) {
                lastLog = lstLog.getValueAsLong().intValue();
            }
        } catch (JEVisException jex) {
            jex.printStackTrace();
        }

        List<JEVisSample> alarmLogs = new ArrayList<>();
        for (AlarmData alarm : alarms) {
            Integer logVal = 0;
            if (alarm.isAlarm()) {
                logVal = ScheduleService.getValueForLog(alarm.getTime(), up);
            }

            try {
                alarmLogs.add(logAtt.buildSample(alarm.getTime(), logVal));
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        try {
            logAtt.addSamples(alarmLogs);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    DateTime getLastUpdate() {

        try {
            if (_status.hasSample()) {
                JEVisSample dateSample = _status.getLatestSample();

                return dateSample.getTimestamp();
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    OPERATOR getOperator(JEVisObject alarm) throws JEVisException {
        JEVisAttribute operatorAtt = alarm.getAttribute(OPERATOR_ATTRIBUTE);

        JEVisSample operatorSample = operatorAtt.getLatestSample();

        try {

            switch (operatorSample.getValueAsString()) {
                case "<":
                    return OPERATOR.SMALER;
                case ">":
                    return OPERATOR.BIGGER;
                case "=":
                    return OPERATOR.EQUALS;
                case "!=":
                    return OPERATOR.UNEQUALS;
            }
        } catch (Exception ex) {
            logger.error("Error could not parse Operator");
        }
        return OPERATOR.SMALER;
    }

    boolean isEnabled(JEVisObject alarmObj) {
        try {
            JEVisAttribute enabeldatt = alarmObj.getAttribute(ENABLED);
            if (enabeldatt.hasSample()) {
                boolean value = enabeldatt.getLatestSample().getValueAsBoolean();
//                logger.info("ebalbed: " + value);
                return value;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    void setStatus(DateTime ts, String message) {
        try {
            JEVisSample sample = _status.buildSample(DateTime.now(), message);
            sample.commit();
        } catch (Exception ex) {

        }

    }

    @SuppressWarnings("unused")
    private void printAlarms(List<AlarmData> alarms) {
        for (AlarmData alarm : alarms) {
            logger.info("Alarm: " + alarm.getMessage());
        }
    }

    private void initUsageTime() {

//        UsageSchedule silentTime = new UsageSchedule(UsageScheduleType.SILENT);
//        silentTime.setPeriod(alarmObj, SILENT_TIME);
//        up.add(silentTime);
//        UsageSchedule standbyTime = new UsageSchedule(UsageScheduleType.STANDBY);
//        standbyTime.setPeriod(alarmObj, STANDBY_TIME);
//        up.add(standbyTime);
    }

}
