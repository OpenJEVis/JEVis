/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author broder
 */
public class ProcessHelper {
    private static final Logger logger = LogManager.getLogger(ProcessHelper.class);

    /**
     * Needed for list representations (e.g. period)
     *
     * @param samples
     * @param attribute
     * @return
     */
    public static ConcurrentHashMap<String, Object> getAttributeSamples(List<JEVisSample> samples, JEVisAttribute attribute, DateTimeZone timeZone) {
        List<ConcurrentHashMap<String, Object>> sampleList = new ArrayList<>();
//        for (JEVisSample sample : samples) {
//            Map<String, Object> valueMap = getSampleMap(sample, attribute);
//            sampleList.add(valueMap);

        if (samples.isEmpty()) {
            ConcurrentHashMap<String, Object> defMap = defSampleMap();
            sampleList.add(defMap);
        } else {
            for (JEVisSample sample : samples) {
                ConcurrentHashMap<String, Object> valueMap = getSampleMap(sample, attribute, timeZone);
                sampleList.add(valueMap);
            }
        }
        //parse the attributes
        ConcurrentHashMap<String, Object> attributeMap = getAttributeMap(sampleList, attribute.getName());
        return attributeMap;
    }

    static ConcurrentHashMap<String, Object> defSampleMap() {
        ConcurrentHashMap<String, Object> valueMap = new ConcurrentHashMap<>();
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
    public static ConcurrentHashMap<String, Object> getAttributeSample(JEVisSample sample, JEVisAttribute attribute, DateTimeZone timeZone) {
        ConcurrentHashMap<String, Object> sampleMap = getSampleMap(sample, attribute, timeZone);

        //parse the attributes
        String attributeName = attribute.getName().trim().replaceAll("\\s+", "");
        ConcurrentHashMap<String, Object> attributeMap = getAttributeMap(sampleMap, attributeName);
        return attributeMap;
    }

    private static ConcurrentHashMap<String, Object> getAttributeMap(Object sampleMap, String name) {
        ConcurrentHashMap<String, Object> attributeMap = new ConcurrentHashMap<>();
        String attributeName = name.trim().replaceAll("\\s+", "");
        attributeMap.put(attributeName, sampleMap);
        attributeMap.put(attributeName.toLowerCase(), sampleMap);
        return attributeMap;
    }

    static ConcurrentHashMap<String, Object> getSampleMap(JEVisSample sample, JEVisAttribute attribute, DateTimeZone timeZone) {
        ConcurrentHashMap<String, Object> valueMap = new ConcurrentHashMap<>();
        try {
            Object value = getValue(attribute, sample);
            DateTime utcTimestamp = sample.getTimestamp();
            DateTime convertedTimestamp = utcTimestamp.toDateTime(timeZone);
            Double timestamp = PeriodHelper.transformTimestampsToExcelTime(convertedTimestamp);
            JEVisUnit displayUnit = attribute.getDisplayUnit();
            if (displayUnit != null) {
                valueMap.put("unit", displayUnit.toString());
            } else {
                logger.warn("No display unit for attribute name: " + attribute.getName());
            }
            valueMap.put("value", value);
            valueMap.put("timestamp", timestamp);
        } catch (JEVisException ex) {
            logger.warn("Cant collect samples for attribute: " + attribute.getName(), ex);
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
            logger.error("", ex);
        }
        return value;
    }
}
