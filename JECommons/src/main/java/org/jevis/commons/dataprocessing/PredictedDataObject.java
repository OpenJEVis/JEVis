/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.util.HashMap;
import java.util.Map;

import static org.jevis.commons.dataprocessing.PredictedDataObject.AttributeName.*;

/**
 * @author broder
 */
public class PredictedDataObject {

    public static final String CLASS_NAME = "Predicted Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(PredictedDataObject.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JEVisObject predictedDataObject;
    private JEVisObject parentDataObject;
    private Boolean enabled;
    //additional attributes
    private SampleHandler sampleHandler;
    private JEVisAttribute inputAttribute;

    private JEVisAttribute valueAttribute;
    private JEVisAttribute enabledAttribute;
    private JEVisAttribute typeAttribute;
    private JEVisAttribute referencePeriodAttribute;
    private JEVisAttribute referencePeriodCountAttribute;
    private JEVisAttribute bindToSpecificAttribute;
    private JEVisAttribute predictionDurationAttribute;
    private JEVisAttribute predictionDurationCountAttribute;
    private DateTime lastRawDate;
    private int processingSize = 10000;

    private Period inputDataPeriod;

    public PredictedDataObject(JEVisObject calcObject, ObjectHandler objectHandler) {
        predictedDataObject = calcObject;
        parentDataObject = objectHandler.getFirstParent(calcObject);
        sampleHandler = new SampleHandler();
    }

    public void getAttributes() throws JEVisException {
        if (enabledAttribute == null) {
            enabledAttribute = getPredictedDataObject().getAttribute(ENABLED.getAttributeName());
        }

        if (valueAttribute == null) {
            valueAttribute = getPredictedDataObject().getAttribute(VALUE.getAttributeName());
        }

        if (typeAttribute == null) {
            typeAttribute = getPredictedDataObject().getAttribute(TYPE.getAttributeName());
        }

        if (referencePeriodAttribute == null) {
            referencePeriodAttribute = getPredictedDataObject().getAttribute(REFERENCE_PERIOD.getAttributeName());
        }

        if (referencePeriodCountAttribute == null) {
            referencePeriodCountAttribute = getPredictedDataObject().getAttribute(REFERENCE_PERIOD_COUNT.getAttributeName());
        }

        if (bindToSpecificAttribute == null) {
            bindToSpecificAttribute = getPredictedDataObject().getAttribute(BIND_TO_SPECIFIC.getAttributeName());
        }

        if (predictionDurationAttribute == null) {
            predictionDurationAttribute = getPredictedDataObject().getAttribute(PREDICTION_DURATION.getAttributeName());
        }

        if (predictionDurationCountAttribute == null) {
            predictionDurationCountAttribute = getPredictedDataObject().getAttribute(PREDICTION_DURATION_COUNT.getAttributeName());
        }
    }

    public void reloadAttributes() throws JEVisException {
        getPredictedDataObject().getDataSource().reloadAttribute(enabledAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(valueAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(typeAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(referencePeriodAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(referencePeriodCountAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(bindToSpecificAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(predictionDurationAttribute);
        getPredictedDataObject().getDataSource().reloadAttribute(predictionDurationCountAttribute);
    }

    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getPredictedDataObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    public JEVisObject getPredictedDataObject() {
        return predictedDataObject;
    }

    public JEVisObject getParentDataObject() {
        return parentDataObject;
    }

    public Map<DateTime, JEVisSample> getNotesMap() {
        Map<DateTime, JEVisSample> notesMap = new HashMap<>();
        try {
            final JEVisClass dataNoteClass = parentDataObject.getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject obj : predictedDataObject.getParents().get(0).getChildren(dataNoteClass, true)) {
                if (obj.getName().contains(predictedDataObject.getName())) {
                    JEVisAttribute userNoteAttribute = obj.getAttribute("User Notes");
                    if (userNoteAttribute.hasSample()) {
                        for (JEVisSample smp : userNoteAttribute.getAllSamples()) {
                            notesMap.put(smp.getTimestamp(), smp);
                        }
                    }
                }
            }
        } catch (JEVisException e) {
        }
        return notesMap;
    }

    public String getName() {
        return predictedDataObject.getName() + ":" + predictedDataObject.getID();
    }

    public JEVisAttribute getValueAttribute() throws JEVisException {
        if (valueAttribute == null)
            valueAttribute = getPredictedDataObject().getAttribute(VALUE.getAttributeName());
        return valueAttribute;
    }

    public JEVisAttribute getInputAttribute() throws JEVisException {
        if (inputAttribute == null)
            inputAttribute = getParentDataObject().getAttribute(VALUE.getAttributeName());
        return inputAttribute;
    }

    public void setInputAttribute(JEVisAttribute inputAttribute) {
        this.valueAttribute = inputAttribute;
    }

    public JEVisAttribute getTypeAttribute() throws JEVisException {
        if (typeAttribute == null)
            typeAttribute = getPredictedDataObject().getAttribute(TYPE.getAttributeName());
        return typeAttribute;
    }

    public JEVisAttribute getReferencePeriodAttribute() throws JEVisException {
        if (referencePeriodAttribute == null)
            referencePeriodAttribute = getPredictedDataObject().getAttribute(REFERENCE_PERIOD.getAttributeName());
        return referencePeriodAttribute;
    }

    public JEVisAttribute getReferencePeriodCountAttribute() throws JEVisException {
        if (referencePeriodCountAttribute == null)
            referencePeriodCountAttribute = getPredictedDataObject().getAttribute(REFERENCE_PERIOD_COUNT.getAttributeName());
        return referencePeriodCountAttribute;
    }

    public JEVisAttribute getBindToSpecificAttribute() throws JEVisException {
        if (bindToSpecificAttribute == null)
            bindToSpecificAttribute = getPredictedDataObject().getAttribute(BIND_TO_SPECIFIC.getAttributeName());
        return bindToSpecificAttribute;
    }

    public JEVisAttribute getPredictionDurationAttribute() throws JEVisException {
        if (predictionDurationAttribute == null)
            predictionDurationAttribute = getPredictedDataObject().getAttribute(PREDICTION_DURATION.getAttributeName());
        return predictionDurationAttribute;
    }

    public JEVisAttribute getPredictionDurationCountAttribute() throws JEVisException {
        if (predictionDurationCountAttribute == null)
            predictionDurationCountAttribute = getPredictedDataObject().getAttribute(PREDICTION_DURATION_COUNT.getAttributeName());
        return predictionDurationCountAttribute;
    }

    public Period getInputDataPeriod() {
        if (inputDataPeriod == null) {
            try {
                inputDataPeriod = getInputAttribute().getInputSampleRate();
            } catch (Exception e) {
                logger.error("Could not get input data period for object {}:{}", getParentDataObject().getName(), getParentDataObject().getID(), e);
            }
        }
        return inputDataPeriod;
    }

    public JEVisAttribute getEnabledAttribute() {
        return enabledAttribute;
    }

    public void setProcessingSize(int processingSize) {
        this.processingSize = processingSize;
    }

    public DateTime getStartDate() {
        return getLastRun(this.getPredictedDataObject());
    }

    public DateTime getEndDate() throws JEVisException {
        if (getPredictionDurationAttribute().hasSample()) {
            String predictionDuration = getPredictionDurationAttribute().getLatestSample().getValueAsString();
            long predictionDurationCount = 1L;
            if (getPredictionDurationCountAttribute().hasSample()) {
                predictionDurationCount = getPredictionDurationCountAttribute().getLatestSample().getValueAsLong();
            }
            long duration = 0L;

            switch (predictionDuration) {
                case "MINUTES":
                    duration = 60L * 1000L;
                    break;
                case "HOURS":
                    duration = 60L * 60L * 1000L;
                    break;
                case "DAYS":
                    duration = 24L * 60L * 60L * 1000L;
                    break;
                case "WEEKS":
                    duration = 7L * 24L * 60L * 60L * 1000L;
                    break;
                case "MONTHS":
                    duration = 4L * 7L * 24L * 60L * 60L * 1000L;
                    break;
            }

            duration *= predictionDurationCount;
            return getStartDate().plus(duration);
        }
        return null;
    }

    public boolean isReady(JEVisObject object) {
        DateTime lastRun = getLastRun(object);
        Long cycleTime = getCycleTime(object);
        DateTime nextRun = lastRun.plusMillis(cycleTime.intValue());
        return DateTime.now().withZone(getTimeZone(object)).equals(nextRun) || DateTime.now().isAfter(nextRun);
    }

    private DateTimeZone getTimeZone(JEVisObject object) {
        DateTimeZone zone = DateTimeZone.UTC;

        JEVisAttribute timeZoneAttribute = null;
        try {
            timeZoneAttribute = object.getAttribute("Timezone");
            if (timeZoneAttribute != null) {
                JEVisSample lastTimeZoneSample = timeZoneAttribute.getLatestSample();
                if (lastTimeZoneSample != null) {
                    zone = DateTimeZone.forID(lastTimeZoneSample.getValueAsString());
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        return zone;
    }

    private DateTime getLastRun(JEVisObject object) {
        DateTime dateTime = new DateTime(2001, 1, 1, 0, 0, 0).withZone(getTimeZone(object));

        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                JEVisSample lastSample = lastRunAttribute.getLatestSample();
                if (lastSample != null) {
                    dateTime = new DateTime(lastSample.getValueAsString());
                }
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: " + e);
        }

        return dateTime;
    }

    private Long getCycleTime(JEVisObject object) {
        Long aLong = null;

        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Cycle Time");
            if (lastRunAttribute != null) {
                JEVisSample lastSample = lastRunAttribute.getLatestSample();
                if (lastSample != null) {
                    aLong = lastSample.getValueAsLong();
                }
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source cycle time: " + e);
        }

        return aLong;
    }

    public void finishCurrentRun(JEVisObject object) {
        Long cycleTime = getCycleTime(object);
        DateTime lastRun = getLastRun(object);
        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                DateTime dateTime = lastRun.plusMillis(cycleTime.intValue());
                JEVisSample newSample = lastRunAttribute.buildSample(DateTime.now(), dateTime);
                newSample.commit();
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: " + e);
        }
    }

    public JsonGapFillingConfig getJsonGapFillingConfig() throws JEVisException {
        JsonGapFillingConfig jsonGapFillingConfig = new JsonGapFillingConfig();

        String type = null;
        String referencePeriod = null;
        String referencePeriodCount = null;
        String bindToSpecific = null;
        if (getTypeAttribute().hasSample()) {
            type = getTypeAttribute().getLatestSample().getValueAsString();
        }

        if (getReferencePeriodAttribute().hasSample()) {
            referencePeriod = getReferencePeriodAttribute().getLatestSample().getValueAsString();
        }

        if (getReferencePeriodCountAttribute().hasSample()) {
            referencePeriodCount = getReferencePeriodCountAttribute().getLatestSample().getValueAsString();
        }

        if (getBindToSpecificAttribute().hasSample()) {
            bindToSpecific = getBindToSpecificAttribute().getLatestSample().getValueAsString();
        }

        jsonGapFillingConfig.setType(type);
        jsonGapFillingConfig.setReferenceperiod(referencePeriod);
        jsonGapFillingConfig.setReferenceperiodcount(referencePeriodCount);
        jsonGapFillingConfig.setBindtospecific(bindToSpecific);

        return jsonGapFillingConfig;
    }

    public enum AttributeName {
        VALUE("Value"),
        ENABLED("Enabled"),
        TYPE("Type"),
        REFERENCE_PERIOD("Reference Period"),
        REFERENCE_PERIOD_COUNT("Reference Period Count"),
        BIND_TO_SPECIFIC("Bind To Specific"),
        PREDICTION_DURATION("Prediction Duration"),
        PREDICTION_DURATION_COUNT("Prediction Duration Count");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
