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
import org.jevis.simplealarm.AlarmData;
import org.jevis.simplealarm.AlarmHelper;
import org.jevis.simplealarm.JEAlarm;
import org.joda.time.DateTime;

/**
 * Copy of the DynamicLimitAlarm(SimpleAlarm)
 *
 * @author fs
 */
public class StaticLimitAlarm extends LimitAlarm {

    public static String ALARM_CLASS = "Static Limit Alarm";
    public static String LIMIT_VALUE = "Limit";

    public StaticLimitAlarm(JEVisObject alarm) {
        super(alarm);

    }

    /**
     * Find the last valid alarm limit sample. To be valid it the timestamp has
     * to be before or at the same time as the value to check and be he mot near
     * to the check value ts.
     *
     * @param key timestamp of the sample we want to check
     * @param samples List of all alarm limits
     * @return null if no sample was found or the most valid sample
     */
    private JEVisSample getValidSample(DateTime key, Map<DateTime, JEVisSample> samples) {

// Tode for an working timestamp history
        JEVisSample lastValid = null;

        for (Map.Entry<DateTime, JEVisSample> entry : samples.entrySet()) {
            DateTime key1 = entry.getKey();
            JEVisSample value = entry.getValue();

            try {

                if (key.isBefore(key1) || key.equals(key1)) {
                    if (lastValid == null) {
                        lastValid = value;
                    } else if (lastValid.getTimestamp().isBefore(key1)) {
                        lastValid = value;
                    }
                }

            } catch (Exception ex) {

            }

        }

        return lastValid;
    }

    public static int moduloPowerOfTwo(int x, int powerOfTwoY) {
        return x & (powerOfTwoY - 1);
    }

    @Override
    public void checkAlarm() throws JEVisException {
        if (!_enabled) {
            System.out.println("Alarm " + alarmObj.getName() + " is disabled");
            return;
        }

        DateTime lastUpdate = getLastUpdate();
        System.out.println("Last Update: " + lastUpdate);
        Map<DateTime, JEVisSample> leftSamples = getLeftSamples(alarmObj, "Value", lastUpdate);
        Map<DateTime, JEVisSample> limitSamples = getRightSamples(alarmObj, leftSamples);

        OPERATOR opeator = getOperator(alarmObj);

        List<AlarmData> alarms = new ArrayList<>();
        DateTime lastTS = null;

        int count = 0;

        JEVisSample rightSample = alarmObj.getAttribute(LIMIT_VALUE).getLatestSample();//Not nullpointer save workaround

        for (Map.Entry<DateTime, JEVisSample> entry : leftSamples.entrySet()) {
            try {
                DateTime key = entry.getKey();
                if (lastTS == null || lastTS.isBefore(key)) {
                    lastTS = key;
                }

                JEVisSample value = entry.getValue();
                switch (opeator) {
                    case SMALER:
                        alarms.add(new AlarmData(
                                key,
                                (value.getValueAsDouble() < rightSample.getValueAsDouble()),
                                "[" + JEAlarm.formatter.print(key) + "] " + AlarmHelper.formateValue(value) + " < " + AlarmHelper.formateValue(rightSample)));

                        break;
                    case BIGGER:
                        alarms.add(new AlarmData(
                                key,
                                (value.getValueAsDouble() > rightSample.getValueAsDouble()),
                                "[" + JEAlarm.formatter.print(key) + "] " + AlarmHelper.formateValue(value) + " > " + AlarmHelper.formateValue(rightSample)));

                        break;
                    case EQUALS:
                        alarms.add(new AlarmData(
                                key,
                                (value.getValueAsDouble().equals(rightSample.getValueAsDouble())),
                                "[" + JEAlarm.formatter.print(key) + "] " + AlarmHelper.formateValue(value) + " = " + AlarmHelper.formateValue(rightSample)));

                        break;
                    case UNEQUALS:
                        alarms.add(new AlarmData(
                                key,
                                (!value.getValueAsDouble().equals(rightSample.getValueAsDouble())),
                                "[" + JEAlarm.formatter.print(key) + "] " + AlarmHelper.formateValue(value) + " != " + AlarmHelper.formateValue(rightSample)));

                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
//                System.out.println("cannot compare samples: " + ex);
            }
        }

        if (!alarms.isEmpty()) {
            System.out.println("Found " + alarms.size() + " alarms");
            logAlarms(_log, alarms);
            setStatus(lastTS, "Updated");

        }
    }

    private Map<DateTime, JEVisSample> getRightSamples(JEVisObject alarm, Map<DateTime, JEVisSample> leftSamples) throws JEVisException {
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
            JEVisAttribute att = alarm.getAttribute(LIMIT_VALUE);
            //Quick solution to get all Samples, there will be only an handfull off samples in the
            //most cases and this saves us the time to find the last valid
            List<JEVisSample> samples = att.getAllSamples();
            return listToMap(samples);
        } else {
            return new HashMap<>();
        }

    }

    private Map<DateTime, JEVisSample> getLeftSamples(JEVisObject alarm, String attribute, DateTime lastUpdate) throws JEVisException {
        System.out.println("getLeftSamples");
        JEVisObject dataPoint = alarm.getParents().get(0);//not save
//        System.out.println("Data Object: " + dataPoint);

        JEVisAttribute att = dataPoint.getAttribute(attribute);
//        System.out.println("Attribute: " + att);
        List<JEVisSample> samples = att.getSamples(lastUpdate, null);
        System.out.println("Samples to check: " + samples.size());
        return listToMap(samples);
    }

    Map<DateTime, JEVisSample> listToMap(List<JEVisSample> samples) {
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
