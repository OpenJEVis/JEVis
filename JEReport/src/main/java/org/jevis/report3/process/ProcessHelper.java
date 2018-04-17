/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.report3.DateHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author broder
 */
public class ProcessHelper {

    /**
     * Needed for list representations (e.g. period)
     *
     * @param samples
     * @param attribute
     * @return
     */
    public static Map<String, Object> getAttributeSamples(List<JEVisSample> samples, JEVisAttribute attribute, DateTimeZone timeZone) {
        List<Map<String, Object>> sampleList = new ArrayList<>();
//        for (JEVisSample sample : samples) {
//            Map<String, Object> valueMap = getSampleMap(sample, attribute);
//            sampleList.add(valueMap);

        if (samples.isEmpty()) {
            Map<String, Object> defMap = defSampleMap();
            sampleList.add(defMap);
        } else {
            for (JEVisSample sample : samples) {
                Map<String, Object> valueMap = getSampleMap(sample, attribute, timeZone);
                sampleList.add(valueMap);
            }
        }
        //parse the attributes
        Map<String, Object> attributeMap = getAttributeMap(sampleList, attribute.getName());
        return attributeMap;
    }

    static Map<String, Object> defSampleMap() {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("unit", "NO UNIT");
        valueMap.put("value", "NO DATA");
        valueMap.put("timestamp", "NO DATE");
        return valueMap;
    }

    /**
     * Needed for String representation (eg configname: last)
     *
     * @param sample
     * @param attribute
     * @return
     */
    public static Map<String, Object> getAttributeSample(JEVisSample sample, JEVisAttribute attribute, DateTimeZone timeZone) {
        Map<String, Object> sampleMap = getSampleMap(sample, attribute, timeZone);

        //parse the attributes
        String attributeName = attribute.getName().trim().replaceAll("\\s+", "");
        Map<String, Object> attributeMap = getAttributeMap(sampleMap, attributeName);
        return attributeMap;
    }

    private static Map<String, Object> getAttributeMap(Object sampleMap, String name) {
        Map<String, Object> attributeMap = new HashMap<>();
        String attributeName = name.trim().replaceAll("\\s+", "");
        attributeMap.put(attributeName, sampleMap);
        attributeMap.put(attributeName.toLowerCase(), sampleMap);
        return attributeMap;
    }

    static Map<String, Object> getSampleMap(JEVisSample sample, JEVisAttribute attribute, DateTimeZone timeZone) {
        Map<String, Object> valueMap = new HashMap<>();
        try {
            Object value = getValue(attribute, sample);
            DateTime utcTimestamp = sample.getTimestamp();
            DateTime convertedTimestamp = utcTimestamp.toDateTime(timeZone);
            Double timestamp = DateHelper.transformTimestampsToExcelTime(convertedTimestamp);
            JEVisUnit displayUnit = attribute.getDisplayUnit();
            if (displayUnit != null) {
                valueMap.put("unit", displayUnit.toString());
            } else {
                Logger.getLogger(PeriodSampleGenerator.class.getName()).log(Level.WARN, "No display unit for attribute name: " + attribute.getName());
            }
            valueMap.put("value", value);
            valueMap.put("timestamp", timestamp);
        } catch (JEVisException ex) {
            Logger.getLogger(ProcessHelper.class.getClass()).log(Level.WARN, "Cant collect samples for attribute: " + attribute.getName(), ex);
        }
        return valueMap;

    }

    public static Object getValue(JEVisAttribute attribute, JEVisSample sample) {
        Object value = null;
        try {
            switch (attribute.getPrimitiveType()) {
                case JEVisConstants.PrimitiveType.STRING:
                    value = sample.getValueAsString();
                    break;
                case JEVisConstants.PrimitiveType.DOUBLE:
                    value = sample.getValueAsDouble();
                    break;
                case JEVisConstants.PrimitiveType.LONG:
                    value = sample.getValueAsLong();
                    break;
                case JEVisConstants.PrimitiveType.BOOLEAN:
                    value = sample.getValueAsBoolean();
                    break;
                default:
                    value = sample.getValue();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(PeriodSampleGenerator.class.getName()).log(Level.ERROR, null, ex);
        }
        return value;
    }
}
