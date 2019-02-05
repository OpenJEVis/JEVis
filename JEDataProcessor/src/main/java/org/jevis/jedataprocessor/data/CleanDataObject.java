/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.jedataprocessor.gap.GapMode;
import org.jevis.jedataprocessor.gap.GapStrategy;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.jedataprocessor.data.CleanDataObject.AttributeName.*;

/**
 * @author broder
 */
public class CleanDataObject {

    public static final String CLASS_NAME = "Clean Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(CleanDataObject.class);
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
    private List<JEVisSample> rawSamples;
    private SampleHandler sampleHandler;

    private List<JsonGapFillingConfig> jsonGapFillingConfig;

    private Boolean limitsEnabled;
    private Boolean gapFillingEnabled;
    private List<JsonLimitsConfig> jsonLimitsConfig;
    private List<JEVisSample> counterOverflow;
    private Double lastDiffValue;
    private Double lastCleanValue;
    private boolean isFirstRunPeriod = true;
    private JEVisAttribute cleanAttribute;
    private JEVisAttribute rawAttribute;

    public CleanDataObject(JEVisObject calcObject, ObjectHandler objectHandler) {
        cleanObject = calcObject;
        rawDataObject = objectHandler.getFirstParent(calcObject);
        sampleHandler = new SampleHandler();
    }

    /**
     * Check if the configuration is valid. Throws exception if configuration is not valid.
     *
     * @throws Exception
     */
    public void checkConfig() throws Exception {
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
            JEVisAttribute cleanAtt = getCleanAttribute();
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
            throw new Exception(exception);
        }


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
            DateTime timestampFromLastCleanSample = sampleHandler.getTimeStampFromLastSample(getCleanObject(), VALUE_ATTRIBUTE_NAME);
            if (timestampFromLastCleanSample != null) {
                firstDate = timestampFromLastCleanSample.plus(getCleanDataPeriodAlignment());
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
            lastDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(getCleanDataPeriodAlignment());
        }
        return lastDate;
    }

    public List<JsonGapFillingConfig> getGapFillingConfig() {
        if (jsonGapFillingConfig == null) {
            String gapFillingConfig = sampleHandler.getLastSample(getCleanObject(), GAP_FILLING_CONFIG.getAttributeName(), "");
            if (gapFillingConfig != null && !gapFillingConfig.equals("")) {
                jsonGapFillingConfig = new Gson().fromJson(gapFillingConfig, new TypeToken<List<JsonGapFillingConfig>>() {
                }.getType());
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
                jsonLimitsConfig = new Gson().fromJson(limitsConfiguration, new TypeToken<List<JsonLimitsConfig>>() {
                }.getType());
            } else {
                return new ArrayList<>();
            }

        }
        return jsonLimitsConfig;
    }

    public List<JEVisSample> getRawSamples() {
        if (rawSamples == null) {
            rawSamples = sampleHandler.getSamplesInPeriod(rawDataObject,
                    VALUE_ATTRIBUTE_NAME,
                    getFirstDate().minus(getCleanDataPeriodAlignment()).minus(getCleanDataPeriodAlignment()),
//                    getFirstDate(),
                    getMaxEndDate());
        }
        /**
         * - Start is the first sample of the clean data
         * - End is the last sample of the raw data
         */
        LogTaskManager.getInstance().getTask(getCleanObject().getID()).addStep("Last Clean Data", getFirstDate().minus(getCleanDataPeriodAlignment()));
//        LogTaskManager.getInstance().getTask(getCleanObject().getID()).addStep("Last Clean Data", getFirstDate());
        LogTaskManager.getInstance().getTask(getCleanObject().getID()).addStep("Last Raw Data", getMaxEndDate());
        return rawSamples;
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

    public JEVisAttribute getCleanAttribute() throws JEVisException {
        if (cleanAttribute == null)
            cleanAttribute = getCleanObject().getAttribute(VALUE.getAttributeName());
        return cleanAttribute;
    }

    public void setCleanAttribute(JEVisAttribute cleanAttribute) {
        this.cleanAttribute = cleanAttribute;
    }

    public JEVisAttribute getRawAttribute() throws JEVisException {
        if (rawAttribute == null)
            rawAttribute = getRawDataObject().getAttribute(VALUE.getAttributeName());
        return rawAttribute;
    }

    public void setRawAttribute(JEVisAttribute rawAttribute) {
        this.cleanAttribute = rawAttribute;
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
        LIMITS_CONFIGURATION("Limits Configuration");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
