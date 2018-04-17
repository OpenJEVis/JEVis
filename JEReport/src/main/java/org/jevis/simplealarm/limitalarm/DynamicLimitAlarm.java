/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm.limitalarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.simplealarm.AlarmData;
import org.jevis.simplealarm.AlarmPeriod;
import org.joda.time.DateTime;

/**
 *
 * @author fs
 */
public class DynamicLimitAlarm extends LimitAlarm {

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
     *
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
    public void checkAlarm() throws JEVisException {

    }

    /**
     * New workaround function for the JEReport -> alarm report
     *
     * @param from
     * @param until
     * @return
     * @throws JEVisException
     */
    public List<AlarmPeriod> makeAlarmReport(DateTime from, DateTime until) throws JEVisException {
        if (!_enabled) {
            System.out.println("Alarm " + alarmObj.getName() + " is disabled");
            return new ArrayList<>();
        }

        DateTime lastUpdate = getLastUpdate();
        System.out.println("Last Update: " + lastUpdate);
        Map<DateTime, JEVisSample> leftSamples = getLeftSamples(alarmObj, "Value", from, until);
        Map<DateTime, JEVisSample> rightSamples = getRightSamples(alarmObj, "Value", leftSamples);
        List<JEVisSample> offsets = getOffsets(_offset);

        OPERATOR opeator = getOperator(alarmObj);

        List<AlarmData> alarms = new ArrayList<>();
        AlarmPeriod lastPeriod = new AlarmPeriod();
        DateTime lastTS = null;
        //Workaround, normly belongs in the loop and has to be checkt for every timestamp
        JEVisSample offset = getValidOffst(null, offsets);

        boolean lastAlarmRaised = false;

        List<AlarmPeriod> alarmPeriods = new ArrayList<>();

        for (Map.Entry<DateTime, JEVisSample> entry : leftSamples.entrySet()) {
            System.out.println("Date: " + entry.getKey());
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
//                System.out.println("Date: " + key + "  Ist: [" + valueSoll.getValueAsDouble() + "]    Soll: [" + valueWithOffset + "]");

                switch (opeator) {
                    case SMALER:
                        if (valueSoll.getValueAsDouble() < valueWithOffset) {
                            alarmRaised = true;
                        }
                        break;
                    case BIGGER:
                        if (valueSoll.getValueAsDouble() > valueWithOffset) {
                            alarmRaised = true;
                            System.out.println("----Alarm at: " + key);
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
                    lastPeriod = new AlarmPeriod();
                    lastPeriod.addAlarmPoint(key, valueSoll.getValueAsDouble(), valueWithOffset, 1);

                    alarmPeriods.add(lastPeriod);
                    lastAlarmRaised = true;
                } else {
                    lastAlarmRaised = false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
//                System.out.println("cannot compare samples: " + ex);
            }

        }
        return alarmPeriods;

    }

    private Map<DateTime, JEVisSample> getRightSamples(JEVisObject alarm, String attribute, Map<DateTime, JEVisSample> leftSamples) throws JEVisException {
        System.out.println("getRightSamples");
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
            System.out.println("Refferrenz Data Point: " + att);
            Long id = att.getLatestSample().getValueAsLong();
            System.out.println("ID: " + id);
            JEVisObject rightObj = alarm.getDataSource().getObject(id);
            System.out.println("rightObj: " + rightObj);

            JEVisAttribute valueAtt = rightObj.getAttribute(attribute);
            System.out.println("valueAtt: " + valueAtt);
            List<JEVisSample> samples = valueAtt.getSamples(firstTS, lastTs);
            return listToMap(samples);
        } else {
            return new HashMap<>();
        }

    }

    private Map<DateTime, JEVisSample> getLeftSamples(JEVisObject alarm, String attribute, DateTime from, DateTime until) throws JEVisException {
        System.out.println("getLeftSamples");
        JEVisObject dataPoint = alarm.getParents().get(0);//not save
//        System.out.println("Data Object: " + dataPoint);

        JEVisAttribute att = dataPoint.getAttribute(attribute);
//        System.out.println("Attribute: " + att);
        List<JEVisSample> samples = att.getSamples(from, until);
        System.out.println("Samples to check: " + samples.size());
        return listToMap(samples);
    }

    private Map<DateTime, JEVisSample> listToMap(List<JEVisSample> samples) {
        Map<DateTime, JEVisSample> map = new TreeMap<>();
        for (JEVisSample sample : samples) {
            try {
                map.put(sample.getTimestamp(), sample);
            } catch (Exception ex) {
                System.out.println("Waring, Cannot read sample");
            }
        }
        return map;
    }
}
