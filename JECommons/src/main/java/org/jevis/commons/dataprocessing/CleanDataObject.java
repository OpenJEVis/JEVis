/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.task.LogTaskManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.util.*;

import static org.jevis.commons.dataprocessing.CleanDataObject.AttributeName.*;

/**
 * @author broder
 */
public class CleanDataObject {

    public static final String CLASS_NAME = "Clean Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(CleanDataObject.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JEVisObject cleanObject;
    private JEVisObject rawDataObject;
    //attributes
    private Period periodCleanData;
    private Period periodRawData;
    private Boolean isPeriodAligned;
    private List<JEVisSample> conversionDifferential;
    private Integer periodOffset;
    private Boolean valueIsQuantity;
    private List<JEVisSample> multiplier;
    private Double offset;
    private GapStrategy gapStrategy;
    private Boolean enabled;
    //additional attributes
    private DateTime firstDate;
    private DateTime lastDate;
    private List<JEVisSample> rawSamplesDown;
    private List<JEVisSample> rawSamplesUp;
    private SampleHandler sampleHandler;

    private List<JsonGapFillingConfig> jsonGapFillingConfig;

    private Boolean limitsEnabled;
    private Boolean gapFillingEnabled;
    private List<JsonLimitsConfig> jsonLimitsConfig;
    private List<JEVisSample> counterOverflow;
    private Double lastDiffValue;
    private Double lastCleanValue;
    private boolean isFirstRunPeriod = true;
    private JEVisAttribute rawAttribute;

    private JEVisAttribute valueAttribute;
    private JEVisAttribute conversionToDifferentialAttribute;
    private JEVisAttribute enabledAttribute;
    private JEVisAttribute limitsEnabledAttribute;
    private JEVisAttribute limitsConfigurationAttribute;
    private JEVisAttribute gapFillingEnabledAttribute;
    private JEVisAttribute gapFillingConfigAttribute;
    private JEVisAttribute alarmEnabledAttribute;
    private JEVisAttribute alarmConfigAttribute;
    private JEVisAttribute alarmLogAttribute;
    private JEVisAttribute periodAlignmentAttribute;
    private JEVisAttribute periodOffsetAttribute;
    private JEVisAttribute valueIsAQuantityAttribute;
    private JEVisAttribute valueMultiplierAttribute;
    private JEVisAttribute valueOffsetAttribute;
    private JEVisAttribute counterOverflowAttribute;
    private DateTime lastRawDate;

    public CleanDataObject(JEVisObject calcObject, ObjectHandler objectHandler) {
        cleanObject = calcObject;
        rawDataObject = objectHandler.getFirstParent(calcObject);
        sampleHandler = new SampleHandler();
    }

    /**
     * Check if the configuration is valid. Returns false if configuration is not valid.
     *
     */
    public boolean checkConfig() {
        List<String> errors = new ArrayList<>();
        if (getLimitsEnabled() && getLimitsConfig().isEmpty()) {
            errors.add("Missing Limit configuration");
        }

        if (getGapFillingEnabled() && getGapFillingConfig().isEmpty()) {
            errors.add("Missing Gap configuration,");
        }

        if (getMultiplier().isEmpty()) {
            errors.add(("Multiplier is empty"));
        }
        if (getCleanObject() == null) {
            errors.add(("Offset is empty"));
        }
        try {
            JEVisAttribute cleanAtt = getValueAttribute();
            JEVisSample latestCleanSample = null;
            if (cleanAtt.hasSample())
                latestCleanSample = cleanAtt.getLatestSample();
            DateTime timestampLatestCleanSample = null;
            if (latestCleanSample != null)
                timestampLatestCleanSample = latestCleanSample.getTimestamp();

            JEVisAttribute rawAtt = getRawAttribute();
            JEVisSample latestRawSample = null;
            if (rawAtt.hasSample())
                latestRawSample = rawAtt.getLatestSample();
            DateTime timestampLatestRawSample = null;
            if (latestRawSample != null)
                timestampLatestRawSample = latestRawSample.getTimestamp();

            if (timestampLatestCleanSample != null && timestampLatestRawSample != null) {
                if (!timestampLatestRawSample.isAfter(timestampLatestCleanSample)) {
                    errors.add(("No new Samples for cleaning"));
                }
            } else if (timestampLatestCleanSample != null) {
                errors.add(("No new Samples for cleaning"));
            }
        } catch (Exception e) {
            logger.error("Error while loading latest samples. " + e);
        }

        if (!errors.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            errors.forEach(s -> {
                stringBuilder.append(" -");
                stringBuilder.append(s);
                stringBuilder.append("\n");
            });
            String exception = String.format("[%s] Error in configuration, stopping: %s", getCleanObject().getID(), stringBuilder.toString().replace("\n", ""));
            LogTaskManager.getInstance().getTask(getCleanObject().getID()).setException(new Exception(exception));

            return false;
        }

        return true;
    }

    public void getAttributes() throws JEVisException {
        if (conversionToDifferentialAttribute == null) {
            conversionToDifferentialAttribute = getCleanObject().getAttribute(CONVERSION_DIFFERENTIAL.getAttributeName());
        }
        if (enabledAttribute == null) {
            enabledAttribute = getCleanObject().getAttribute(ENABLED.getAttributeName());
        }
        if (limitsEnabledAttribute == null) {
            limitsEnabledAttribute = getCleanObject().getAttribute(LIMITS_ENABLED.getAttributeName());
        }
        if (limitsConfigurationAttribute == null) {
            limitsConfigurationAttribute = getCleanObject().getAttribute(LIMITS_CONFIGURATION.getAttributeName());
        }
        if (gapFillingEnabledAttribute == null) {
            gapFillingEnabledAttribute = getCleanObject().getAttribute(GAPFILLING_ENABLED.getAttributeName());
        }
        if (gapFillingConfigAttribute == null) {
            gapFillingConfigAttribute = getCleanObject().getAttribute(GAP_FILLING_CONFIG.getAttributeName());
        }
        if (alarmEnabledAttribute == null) {
            alarmEnabledAttribute = getCleanObject().getAttribute(ALARM_ENABLED.getAttributeName());
        }
        if (alarmConfigAttribute == null) {
            alarmConfigAttribute = getCleanObject().getAttribute(ALARM_CONFIG.getAttributeName());
        }
        if (alarmLogAttribute == null) {
            alarmLogAttribute = getCleanObject().getAttribute(ALARM_LOG.getAttributeName());
        }
        if (periodAlignmentAttribute == null) {
            periodAlignmentAttribute = getCleanObject().getAttribute(PERIOD_ALIGNMENT.getAttributeName());
        }
        if (periodOffsetAttribute == null) {
            periodOffsetAttribute = getCleanObject().getAttribute(PERIOD_OFFSET.getAttributeName());
        }
        if (valueIsAQuantityAttribute == null) {
            valueIsAQuantityAttribute = getCleanObject().getAttribute(VALUE_QUANTITY.getAttributeName());
        }
        if (valueMultiplierAttribute == null) {
            valueMultiplierAttribute = getCleanObject().getAttribute(MULTIPLIER.getAttributeName());
        }
        if (valueOffsetAttribute == null) {
            valueOffsetAttribute = getCleanObject().getAttribute(OFFSET.getAttributeName());
        }
        if (counterOverflowAttribute == null) {
            counterOverflowAttribute = getCleanObject().getAttribute(COUNTEROVERFLOW.getAttributeName());
        }

        if (valueAttribute == null) {
            valueAttribute = getCleanObject().getAttribute(VALUE.getAttributeName());
        }
    }

    public void reloadAttributes() throws JEVisException {

        getCleanObject().getDataSource().reloadAttribute(conversionToDifferentialAttribute);
        getCleanObject().getDataSource().reloadAttribute(enabledAttribute);
        getCleanObject().getDataSource().reloadAttribute(limitsEnabledAttribute);
        getCleanObject().getDataSource().reloadAttribute(limitsConfigurationAttribute);
        getCleanObject().getDataSource().reloadAttribute(gapFillingEnabledAttribute);
        getCleanObject().getDataSource().reloadAttribute(gapFillingConfigAttribute);
        getCleanObject().getDataSource().reloadAttribute(alarmEnabledAttribute);
        getCleanObject().getDataSource().reloadAttribute(alarmConfigAttribute);
        getCleanObject().getDataSource().reloadAttribute(alarmLogAttribute);
        getCleanObject().getDataSource().reloadAttribute(periodAlignmentAttribute);
        getCleanObject().getDataSource().reloadAttribute(periodOffsetAttribute);
        getCleanObject().getDataSource().reloadAttribute(valueIsAQuantityAttribute);
        getCleanObject().getDataSource().reloadAttribute(valueMultiplierAttribute);
        getCleanObject().getDataSource().reloadAttribute(valueOffsetAttribute);
        getCleanObject().getDataSource().reloadAttribute(counterOverflowAttribute);
        getCleanObject().getDataSource().reloadAttribute(valueAttribute);
    }

    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getCleanObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    public Boolean getIsPeriodAligned() {
        if (isPeriodAligned == null)
            isPeriodAligned = sampleHandler.getLastSample(getCleanObject(), PERIOD_ALIGNMENT.getAttributeName(), false);
        return isPeriodAligned;
    }

    public Period getRawDataPeriodAlignment() {
        if (periodRawData == null)
            periodRawData = sampleHandler.getInputSampleRate(getRawDataObject(), VALUE_ATTRIBUTE_NAME);
        return periodRawData;
    }

    public Period getCleanDataPeriodAlignment() {
        if (periodCleanData == null)
            periodCleanData = sampleHandler.getInputSampleRate(getCleanObject(), VALUE_ATTRIBUTE_NAME);
        return periodCleanData;
    }

    public List<JEVisSample> getConversionDifferential() {
        if (conversionDifferential == null) {
            conversionDifferential = sampleHandler.getAllSamples(getCleanObject(), CONVERSION_DIFFERENTIAL.getAttributeName());
            if (conversionDifferential.isEmpty())
                conversionDifferential.add(new VirtualSample(new DateTime(2001, 1, 1, 0, 0, 0, 0), false));
        }
        return conversionDifferential;
    }

    public Integer getPeriodOffset() {
        if (periodOffset == null) {
            Long periodOffsetLong = sampleHandler.getLastSample(getCleanObject(), PERIOD_OFFSET.getAttributeName(), 0L);
            periodOffset = periodOffsetLong.intValue();
        }
        return periodOffset;
    }

    public Boolean getValueIsQuantity() {
        if (valueIsQuantity == null) {
            valueIsQuantity = sampleHandler.getLastSample(getCleanObject(), VALUE_QUANTITY.getAttributeName(), false);
        }
        return valueIsQuantity;
    }

    public Boolean getLimitsEnabled() {
        if (limitsEnabled == null)
            limitsEnabled = sampleHandler.getLastSample(getCleanObject(), LIMITS_ENABLED.getAttributeName(), false);
        return limitsEnabled;
    }

    public Boolean getGapFillingEnabled() {
        if (gapFillingEnabled == null)
            gapFillingEnabled = sampleHandler.getLastSample(getCleanObject(), GAPFILLING_ENABLED.getAttributeName(), false);
        return gapFillingEnabled;
    }

    public JEVisObject getCleanObject() {
        return cleanObject;
    }

    public JEVisObject getRawDataObject() {
        return rawDataObject;
    }

    public List<JEVisSample> getCounterOverflow() {
        if (counterOverflow == null)
            counterOverflow = sampleHandler.getAllSamples(getCleanObject(), COUNTEROVERFLOW.getAttributeName());
        return counterOverflow;
    }

    public DateTime getFirstDate() {
        if (firstDate == null) {
            //first date is the lastdate of clean datarow + periodCleanData or the year of the first sample of the raw data
            DateTime timestampFromLastCleanSample = null;
            JEVisAttribute attribute = null;
            try {
                attribute = getCleanObject().getAttribute(VALUE_ATTRIBUTE_NAME);
            } catch (JEVisException e) {
                logger.error("Could not get attribute " + VALUE_ATTRIBUTE_NAME +
                        " of object " + getCleanObject().getName() + ":" + getCleanObject().getID());
            }
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    try {
                        timestampFromLastCleanSample = lastSample.getTimestamp();
                    } catch (JEVisException e) {
                        logger.error("Could not get last sample of attribute " + attribute.getName() +
                                " of object " + getCleanObject().getName() + ":" + getCleanObject().getID());
                    }
                }
            }
            if (timestampFromLastCleanSample != null) {
//                firstDate = timestampFromLastCleanSample.plus(getCleanDataPeriodAlignment());
                firstDate = timestampFromLastCleanSample;
            } else {
                DateTime firstTimestampRaw = sampleHandler.getTimestampFromFirstSample(rawDataObject, VALUE_ATTRIBUTE_NAME);
                if (firstTimestampRaw != null) {
                    firstDate = new DateTime(firstTimestampRaw.getYear(), firstTimestampRaw.getMonthOfYear(), firstTimestampRaw.getDayOfMonth(), 0, 0);
                } else {
                    firstDate = null;
//                    throw new RuntimeException("No raw values in clean data row");
                }
            }
        }
        return firstDate;
    }

    public void setFirstDate(DateTime firstDate) {
        this.firstDate = firstDate;
    }

    public DateTime getMaxEndDate() {
        if (lastDate == null) {
            try {
                int indexLastRawSample = getRawSamplesDown().size() - 1;
                lastDate = rawSamplesDown.get(indexLastRawSample).getTimestamp().plus(getCleanDataPeriodAlignment());
                //lastDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(getCleanDataPeriodAlignment());
            } catch (JEVisException e) {
                logger.error("Could not get timestamp of last Raw sample.");
            }
        }
        return lastDate;
    }

    public List<JsonGapFillingConfig> getGapFillingConfig() {
        if (jsonGapFillingConfig == null) {
            String gapFillingConfig = sampleHandler.getLastSample(getCleanObject(), GAP_FILLING_CONFIG.getAttributeName(), "");
            if (gapFillingConfig != null && !gapFillingConfig.equals("")) {
                try {
                    jsonGapFillingConfig = Arrays.asList(objectMapper.readValue(gapFillingConfig, JsonGapFillingConfig[].class));
                } catch (JsonParseException e) {
                    logger.error("Could not parse gapFillingConfig because of JsonParseException: {}", gapFillingConfig, e);
                    return new ArrayList<>();
                } catch (JsonMappingException e) {
                    logger.error("Could not parse gapFillingConfig because of JsonMappingException: {}", gapFillingConfig, e);
                    return new ArrayList<>();
                } catch (IOException e) {
                    logger.error("Could not parse gapFillingConfig because of IOException: {}", gapFillingConfig, e);
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }
        }
        return jsonGapFillingConfig;
    }

    public List<JsonLimitsConfig> getLimitsConfig() {
        if (jsonLimitsConfig == null) {
            String limitsConfiguration = sampleHandler.getLastSample(getCleanObject(), LIMITS_CONFIGURATION.getAttributeName(), "");
            if (limitsConfiguration != null && !limitsConfiguration.equals("")) {
                try {
                    jsonLimitsConfig = Arrays.asList(objectMapper.readValue(limitsConfiguration, JsonLimitsConfig[].class));
                } catch (JsonParseException e) {
                    logger.error("Could not parse gapFillingConfig because of JsonParseException: {}", limitsConfiguration, e);
                    return new ArrayList<>();
                } catch (JsonMappingException e) {
                    logger.error("Could not parse gapFillingConfig because of JsonMappingException: {}", limitsConfiguration, e);
                    return new ArrayList<>();
                } catch (IOException e) {
                    logger.error("Could not parse gapFillingConfig because of IOException: {}", limitsConfiguration, e);
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }

        }
        return jsonLimitsConfig;
    }

    public List<JEVisSample> getRawSamplesDown() {
        if (rawSamplesDown == null) {
            DateTime firstDate = getFirstDate()
                    .minus(getCleanDataPeriodAlignment())
                    .minus(getCleanDataPeriodAlignment())
                    .minus(getCleanDataPeriodAlignment())
                    .minus(getCleanDataPeriodAlignment());
            rawSamplesDown = sampleHandler.getSamplesInPeriod(
                    rawDataObject,
                    VALUE_ATTRIBUTE_NAME,
                    firstDate,
                    getLastRawDate());

            if (rawSamplesDown.size() > 10000) {
                rawSamplesDown = rawSamplesDown.subList(0, 10000);
            }
        }
        return rawSamplesDown;
    }

    public List<JEVisSample> getRawSamplesUp() {
        if (rawSamplesUp == null) {
            DateTime firstDate = getFirstDate()
                    .minus(getRawDataPeriodAlignment())
                    .minus(getRawDataPeriodAlignment())
                    .minus(getRawDataPeriodAlignment())
                    .minus(getRawDataPeriodAlignment());
            rawSamplesUp = sampleHandler.getSamplesInPeriod(
                    rawDataObject,
                    VALUE_ATTRIBUTE_NAME,
                    firstDate,
                    getLastRawDate());

            if (rawSamplesUp.size() > 100000) {
                rawSamplesUp.subList(0, 100000);
            }
        }
        return rawSamplesUp;
    }

    public Map<DateTime, JEVisSample> getNotesMap() {
        Map<DateTime, JEVisSample> notesMap = new HashMap<>();
        try {
            final JEVisClass dataNoteClass = rawDataObject.getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject obj : cleanObject.getParents().get(0).getChildren(dataNoteClass, true)) {
                if (obj.getName().contains(cleanObject.getName())) {
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

    public List<JEVisSample> getMultiplier() {
        if (multiplier == null) {
            multiplier = sampleHandler.getAllSamples(getCleanObject(), MULTIPLIER.getAttributeName());
            if (multiplier.isEmpty())
                multiplier.add(new VirtualSample(new DateTime(2001, 1, 1, 0, 0, 0, 0), 1.0));
        }
        return multiplier;
    }

    public Double getOffset() {
        if (offset == null) {
            offset = sampleHandler.getLastSample(getCleanObject(), OFFSET.getAttributeName(), 0.0);
        }
        return offset;
    }

    public Double getLastCleanValue() throws Exception {
        if (lastCleanValue == null) {
            JEVisSample latestSample = getCleanObject().getAttribute(VALUE.getAttributeName()).getLatestSample();
            if (latestSample != null) {
                lastCleanValue = latestSample.getValueAsDouble();
            }
        }
        return lastCleanValue;
    }

    public GapStrategy getGapFillingMode() {
        if (gapStrategy == null) {
            String gapModeString = sampleHandler.getLastSample(getCleanObject(), GAP_FILLING.getAttributeName(), GapMode.NONE.toString());
            gapStrategy = new GapStrategy(gapModeString);
        }
        return gapStrategy;
    }

    public String getName() {
        return cleanObject.getName() + "," + cleanObject.getID();
    }

    public JEVisAttribute getValueAttribute() throws JEVisException {
        if (valueAttribute == null)
            valueAttribute = getCleanObject().getAttribute(VALUE.getAttributeName());
        return valueAttribute;
    }

    public void setValueAttribute(JEVisAttribute valueAttribute) {
        this.valueAttribute = valueAttribute;
    }

    public JEVisAttribute getRawAttribute() throws JEVisException {
        if (rawAttribute == null)
            rawAttribute = getRawDataObject().getAttribute(VALUE.getAttributeName());
        return rawAttribute;
    }

    public void setRawAttribute(JEVisAttribute rawAttribute) {
        this.valueAttribute = rawAttribute;
    }

    public JEVisAttribute getConversionToDifferentialAttribute() {
        return conversionToDifferentialAttribute;
    }

    public JEVisAttribute getEnabledAttribute() {
        return enabledAttribute;
    }

    public JEVisAttribute getLimitsEnabledAttribute() {
        return limitsEnabledAttribute;
    }

    public JEVisAttribute getLimitsConfigurationAttribute() {
        return limitsConfigurationAttribute;
    }

    public JEVisAttribute getGapFillingEnabledAttribute() {
        return gapFillingEnabledAttribute;
    }

    public JEVisAttribute getGapFillingConfigAttribute() {
        return gapFillingConfigAttribute;
    }

    public JEVisAttribute getAlarmEnabledAttribute() {
        return alarmEnabledAttribute;
    }

    public JEVisAttribute getAlarmConfigAttribute() {
        return alarmConfigAttribute;
    }

    public JEVisAttribute getAlarmLogAttribute() {
        return alarmLogAttribute;
    }

    public JEVisAttribute getPeriodAlignmentAttribute() {
        return periodAlignmentAttribute;
    }

    public JEVisAttribute getPeriodOffsetAttribute() {
        return periodOffsetAttribute;
    }

    public JEVisAttribute getValueIsAQuantityAttribute() {
        return valueIsAQuantityAttribute;
    }

    public JEVisAttribute getValueMultiplierAttribute() {
        return valueMultiplierAttribute;
    }

    public JEVisAttribute getValueOffsetAttribute() {
        return valueOffsetAttribute;
    }

    public JEVisAttribute getCounterOverflowAttribute() {
        return counterOverflowAttribute;
    }

    public void clearLists() {
        rawSamplesDown = null;
        rawSamplesUp = null;
    }

    public enum AttributeName {

        PERIOD_OFFSET("Period Offset"),
        PERIOD_ALIGNMENT("Period Alignment"),
        VALUE_QUANTITY("Value is a Quantity"),
        CONVERSION_DIFFERENTIAL("Conversion to Differential"),
        MULTIPLIER("Value Multiplier"),
        COUNTEROVERFLOW("Counter Overflow"),
        OFFSET("Value Offset"),
        VALUE("Value"),
        GAP_FILLING("Gap Filling"),
        ENABLED("Enabled"),
        GAP_FILLING_CONFIG("Gap Filling Config"),
        LIMITS_ENABLED("Limits Enabled"),
        GAPFILLING_ENABLED("GapFilling Enabled"),
        LIMITS_CONFIGURATION("Limits Configuration"),
        ALARM_ENABLED("Alarm Enabled"),
        ALARM_CONFIG("Alarm Config"),
        ALARM_LOG("Alarm Log");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }

    public DateTime getLastRawDate() {
        if (lastRawDate == null) {
            lastRawDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(getCleanDataPeriodAlignment());
        }

        return lastRawDate;
    }
}
