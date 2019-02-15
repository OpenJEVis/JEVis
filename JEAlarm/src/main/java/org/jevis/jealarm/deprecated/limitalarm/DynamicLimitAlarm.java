/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm.deprecated.limitalarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author fs
 */
public class DynamicLimitAlarm extends LimitAlarm {
    private static final Logger logger = LogManager.getLogger(DynamicLimitAlarm.class);
    public final static String TOLERANCE = "Tolerance";
    public static String ALARM_CLASS = "Dynamic Limit Alarm";
    public static String COMPERATOR_DR = "Limit Data";
    private JEVisAttribute _offset;

    public DynamicLimitAlarm(JEVisObject alarm) {
        super(alarm);
    }

    @Override
    public void init() {
        super.init();
        try {
            _offset = alarmObj.getAttribute(TOLERANCE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the most valid offset value for the given timestamp.
     * <p>
     * This version simply returns latest value but maybe later we want to have
     * want to use the history function
     *
     * @param key
     * @return
     */
    public JEVisSample getValidOffst(DateTime key, List<JEVisSample> samples) {
        if (!samples.isEmpty()) {
            return samples.get(samples.size() - 1);//most simple but not save, list may not be sorted.
        } else {
            return null;
        }

    }

    private List<JEVisSample> getOffsets(JEVisAttribute att) {
        return att.getAllSamples();
    }

    @Override
    public void checkAlarm() {

    }

    /**
     * New workaround function for the JEReport -> alarm report
     *
     * @param from
     * @param until
     * @return
     * @throws JEVisException
     */
    public List<AlarmPeriodOld> makeAlarmReport(DateTime from, DateTime until) throws JEVisException {
        if (!_enabled) {
            logger.info("Alarm " + alarmObj.getName() + " is disabled");
            return new ArrayList<>();
        }

        DateTime lastUpdate = getLastUpdate();
        logger.info("Last Update: " + lastUpdate);
        Map<DateTime, JEVisSample> leftSamples = getLeftSamples(alarmObj, "Value", from, until);
        Map<DateTime, JEVisSample> rightSamples = getRightSamples(alarmObj, "Value", leftSamples);
        List<JEVisSample> offsets = getOffsets(_offset);

        OPERATOR opeator = getOperator(alarmObj);

        List<AlarmData> alarms = new ArrayList<>();
        AlarmPeriodOld lastPeriod = new AlarmPeriodOld();
        DateTime lastTS = null;
        //Workaround, normly belongs in the loop and has to be checkt for every timestamp
        JEVisSample offset = getValidOffst(null, offsets);

        boolean lastAlarmRaised = false;

        List<AlarmPeriodOld> alarmPeriods = new ArrayList<>();

        for (Map.Entry<DateTime, JEVisSample> entry : leftSamples.entrySet()) {
            logger.info("Date: " + entry.getKey());
        }

        for (Map.Entry<DateTime, JEVisSample> entry : leftSamples.entrySet()) {
            try {
                DateTime key = entry.getKey();
                if (lastTS == null || lastTS.isBefore(key)) {
                    lastTS = key;
                }

                JEVisSample valueSoll = entry.getValue();

                JEVisSample rightSample = rightSamples.get(key);
                if (rightSample == null || rightSample.getValueAsDouble() == null) {

                    continue;
                }

                double valueWithOffset = 0;
                if (offset != null && !offset.getValueAsDouble().isNaN() && offset.getValueAsDouble() != 0) {

                    valueWithOffset = rightSample.getValueAsDouble() * offset.getValueAsDouble();
                } else {
                    valueWithOffset = rightSample.getValueAsDouble();
                }

                if (offset != null) {
                    lastPeriod.setTolerance(offset.getValueAsDouble());
                }

                JEVisUnit unit = valueSoll.getUnit();
                boolean alarmRaised = false;
//                logger.info("Date: " + key + "  Ist: [" + valueSoll.getValueAsDouble() + "]    Soll: [" + valueWithOffset + "]");

                switch (opeator) {
                    case SMALER:
                        if (valueSoll.getValueAsDouble() < valueWithOffset) {
                            alarmRaised = true;
                        }
                        break;
                    case BIGGER:
                        if (valueSoll.getValueAsDouble() > valueWithOffset) {
                            alarmRaised = true;
                            logger.info("----Alarm at: " + key);
                        }
                        break;
                    case EQUALS:
                        if (valueSoll.getValueAsDouble().equals(valueWithOffset)) {
                            alarmRaised = true;
                        }
                        break;
                    case UNEQUALS:

                        if (!valueSoll.getValueAsDouble().equals(valueWithOffset)) {
                            alarmRaised = true;
                        }
                        break;
                }

                if (alarmRaised && lastAlarmRaised) {
                    lastPeriod.addAlarmPoint(key, valueSoll.getValueAsDouble(), valueWithOffset, 1);
                } else if (alarmRaised) {
                    lastPeriod = new AlarmPeriodOld();
                    lastPeriod.addAlarmPoint(key, valueSoll.getValueAsDouble(), valueWithOffset, 1);

                    alarmPeriods.add(lastPeriod);
                    lastAlarmRaised = true;
                } else {
                    lastAlarmRaised = false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
//                logger.info("cannot compare samples: " + ex);
            }

        }
        return alarmPeriods;

    }

    private Map<DateTime, JEVisSample> getRightSamples(JEVisObject alarm, String attribute, Map<DateTime, JEVisSample> leftSamples) throws JEVisException {
        logger.info("getRightSamples");
        DateTime firstTS = null;
        DateTime lastTs = null;
        for (Map.Entry<DateTime, JEVisSample> entry : leftSamples.entrySet()) {
            DateTime key = entry.getKey();
            if (firstTS == null || firstTS.isAfter(key)) {
                firstTS = key;
            }
            if (lastTs == null || lastTs.isBefore(key)) {
                lastTs = key;
            }

        }

        if (firstTS != null) {
            JEVisAttribute att = alarm.getAttribute(COMPERATOR_DR);
            logger.info("Refferrenz Data Point: " + att);
            Long id = att.getLatestSample().getValueAsLong();
            logger.info("ID: " + id);
            JEVisObject rightObj = alarm.getDataSource().getObject(id);
            logger.info("rightObj: " + rightObj);

            JEVisAttribute valueAtt = rightObj.getAttribute(attribute);
            logger.info("valueAtt: " + valueAtt);
            List<JEVisSample> samples = valueAtt.getSamples(firstTS, lastTs);
            return listToMap(samples);
        } else {
            return new HashMap<>();
        }

    }

    private Map<DateTime, JEVisSample> getLeftSamples(JEVisObject alarm, String attribute, DateTime from, DateTime until) throws JEVisException {
        logger.info("getLeftSamples");
        JEVisObject dataPoint = alarm.getParents().get(0);//not save
//        logger.info("Data Object: " + dataPoint);

        JEVisAttribute att = dataPoint.getAttribute(attribute);
//        logger.info("Attribute: " + att);
        List<JEVisSample> samples = att.getSamples(from, until);
        logger.info("Samples to check: " + samples.size());
        return listToMap(samples);
    }

    private Map<DateTime, JEVisSample> listToMap(List<JEVisSample> samples) {
        Map<DateTime, JEVisSample> map = new TreeMap<>();
        for (JEVisSample sample : samples) {
            try {
                map.put(sample.getTimestamp(), sample);
            } catch (Exception ex) {
                logger.info("Waring, Cannot read sample");
            }
        }
        return map;
    }
}
