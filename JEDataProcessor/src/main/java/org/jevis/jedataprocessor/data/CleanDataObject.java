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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
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
import java.util.List;

import static org.jevis.jedataprocessor.data.CleanDataObject.AttributeName.*;

/**
 * @author broder
 */
public class CleanDataObject {

    public static final String CLASS_NAME = "Clean Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(CleanDataObject.class);
    private final JEVisObject object;
    private JEVisObject rawDataObject;
    //attributes
    private Period period;
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

    public CleanDataObject(JEVisObject calcObject, ObjectHandler objectHandler) {
        object = calcObject;
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
        if (getObject() == null) {
            errors.add(("Offset is empty"));
        }
        try {
            JEVisSample latestCleanSample = getObject().getAttribute("Value").getLatestSample();
            DateTime timestampLatestCleanSample = latestCleanSample.getTimestamp();

            JEVisSample latestRawSample = getRawDataObject().getAttribute("Value").getLatestSample();
            DateTime timestampLatestRawSample = latestRawSample.getTimestamp();

            if (timestampLatestCleanSample != null && timestampLatestRawSample != null) {
                if (!timestampLatestRawSample.isAfter(timestampLatestCleanSample)) {
                    errors.add(("No new Samples for cleaning"));
                }
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
            String exception = String.format("[%s] Error in configuration, stopping: %s", getObject().getID(), stringBuilder.toString().replace("\n", ""));
            LogTaskManager.getInstance().getTask(getObject().getID()).setException(new Exception(exception));
            throw new Exception(exception);
        }


    }

    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    public Boolean getIsPeriodAligned() {
        if (isPeriodAligned == null)
            isPeriodAligned = sampleHandler.getLastSample(getObject(), PERIOD_ALIGNMENT.getAttributeName(), false);
        return isPeriodAligned;
    }

    public Period getPeriodAlignment() {
        if (period == null)
            period = sampleHandler.getInputSampleRate(getObject(), VALUE_ATTRIBUTE_NAME);
        return period;
    }

    public List<JEVisSample> getConversionDifferential() {
        if (conversionDifferential == null) {
            conversionDifferential = sampleHandler.getAllSamples(getObject(), CONVERSION_DIFFERENTIAL.getAttributeName());
            if (conversionDifferential.isEmpty())
                conversionDifferential.add(new VirtualSample(new DateTime(2001, 1, 1, 0, 0, 0, 0), false));
        }
        return conversionDifferential;
    }

    public Integer getPeriodOffset() {
        if (periodOffset == null) {
            Long periodOffsetLong = sampleHandler.getLastSample(getObject(), PERIOD_OFFSET.getAttributeName(), 0L);
            periodOffset = periodOffsetLong.intValue();
        }
        return periodOffset;
    }

    public Boolean getValueIsQuantity() {
        if (valueIsQuantity == null) {
            valueIsQuantity = sampleHandler.getLastSample(getObject(), VALUE_QUANTITY.getAttributeName(), false);
        }
        return valueIsQuantity;
    }

    public Boolean getLimitsEnabled() {
        if (limitsEnabled == null)
            limitsEnabled = sampleHandler.getLastSample(getObject(), LIMITS_ENABLED.getAttributeName(), false);
        return limitsEnabled;
    }

    public Boolean getGapFillingEnabled() {
        if (gapFillingEnabled == null)
            gapFillingEnabled = sampleHandler.getLastSample(getObject(), GAPFILLING_ENABLED.getAttributeName(), false);
        return gapFillingEnabled;
    }

    public JEVisObject getObject() {
        return object;
    }

    public JEVisObject getRawDataObject() {
        return rawDataObject;
    }

    public List<JEVisSample> getCounterOverflow() {
        if (counterOverflow == null)
            counterOverflow = sampleHandler.getAllSamples(getObject(), COUNTEROVERFLOW.getAttributeName());
        return counterOverflow;
    }

    public DateTime getFirstDate() {
        if (firstDate == null) {
            //first date is the lastdate of clean datarow + period or the year of the first sample of the raw data
            DateTime timestampFromLastCleanSample = sampleHandler.getTimeStampFromLastSample(getObject(), VALUE_ATTRIBUTE_NAME);
            if (timestampFromLastCleanSample != null) {
                firstDate = timestampFromLastCleanSample.plus(getPeriodAlignment());
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

    public DateTime getMaxEndDate() {
        if (lastDate == null)
            lastDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(period);
        return lastDate;
    }

    public List<JsonGapFillingConfig> getGapFillingConfig() {
        if (jsonGapFillingConfig == null) {
            String gapFillingConfig = sampleHandler.getLastSample(getObject(), GAP_FILLING_CONFIG.getAttributeName(), "");
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
            String limitsConfiguration = sampleHandler.getLastSample(getObject(), LIMITS_CONFIGURATION.getAttributeName(), "");
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
                    getFirstDate().minus(getPeriodAlignment()),
//                    getFirstDate(),
                    getMaxEndDate());
        }
        /**
         * - Start is the first sample of the clean data
         * - End is the last sample of the raw data
         */
        LogTaskManager.getInstance().getTask(getObject().getID()).addStep("Last Clean Data", getFirstDate().minus(getPeriodAlignment()));
//        LogTaskManager.getInstance().getTask(getObject().getID()).addStep("Last Clean Data", getFirstDate());
        LogTaskManager.getInstance().getTask(getObject().getID()).addStep("Last Raw Data", getMaxEndDate());
        return rawSamples;
    }

    /**
     * Return true if this is the first run of the precalc for this Clean Data
     *
     * @return
     */
    public boolean isFirstRun() throws Exception {
        /**
         * default is true, if true check if there clean data and if return false
         */
        if (isFirstRunPeriod) {
            JEVisAttribute attribute = getObject().getAttribute(VALUE_ATTRIBUTE_NAME);
            isFirstRunPeriod = attribute.getLatestSample() == null;
        }
        logger.info("[{}] is first run: {}", getObject().getID(), isFirstRunPeriod);
        return isFirstRunPeriod;
    }

    /**
     * Returns the last counter value
     *
     * @return
     * @throws Exception
     */
    public Double getLastCounterValue() throws Exception {
        logger.info("[{}] getLastCounterValue ", getObject().getID());

        JEVisAttribute attribute = getObject().getAttribute(VALUE_ATTRIBUTE_NAME);


        /**
         * If this is the first ever run use the first raw counter value also as previous value.
         * The first diff value will allays be 0 for now. No counter in the real world starts a 0 if the start there
         * data collection.
         */
        if (isFirstRun()) {
            return getRawSamples().get(0).getValueAsDouble();
        }

        /**
         * If this is not the first ever run return the last counter value before the clean process period
         */
        DateTime timestampFromLastSample = attribute.getTimestampFromLastSample();

        logger.error("[{}] get last raw counter: " + timestampFromLastSample);
        JEVisAttribute rawValuesAtt = rawDataObject.getAttribute(VALUE_ATTRIBUTE_NAME);
        rawDataObject.getDataSource().reloadAttribute(rawValuesAtt);
        /**
         * getFirstDate() gives us the next new clean data timestamp and we want the the last used
         * raw sample before this. because of the chaotic nature of raw values we cannot be sure which it is
         * so we load more an take the second last for now.
         * TODO: we may have to store the last used clean sample or make this function more intelligent
         */
//        List<JEVisSample> rawSamples = rawValuesAtt.getSamples(getFirstDate().minus(getPeriodAlignment().multipliedBy(3)), getFirstDate());
//        return rawSamples.get(rawSamples.size() - 2).getValueAsDouble();
        DateTime firstDate = getFirstDate();
        Long period = getPeriodAlignment().toStandardDuration().getMillis();
        long halfPeriod = period / 2;
        DateTime start = firstDate.minus(period + halfPeriod);
        firstDate = firstDate.plus(halfPeriod);
        List<JEVisSample> samples = rawValuesAtt.getSamples(start, firstDate);

        return samples.get(0).getValueAsDouble();

    }

    public List<JEVisSample> getMultiplier() {
        if (multiplier == null) {
            multiplier = sampleHandler.getAllSamples(getObject(), MULTIPLIER.getAttributeName());
            if (multiplier.isEmpty())
                multiplier.add(new VirtualSample(new DateTime(2001, 1, 1, 0, 0, 0, 0), 1.0));
        }
        return multiplier;
    }

    public Double getOffset() {
        if (offset == null) {
            offset = sampleHandler.getLastSample(getObject(), OFFSET.getAttributeName(), 0.0);
        }
        return offset;
    }

    public Double getLastCleanValue() throws Exception {
        if (lastCleanValue == null) {
            JEVisSample latestSample = getObject().getAttribute("Value").getLatestSample();
            if (latestSample != null) {
                lastCleanValue = latestSample.getValueAsDouble();
            }
        }
        return lastCleanValue;
    }

    public GapStrategy getGapFillingMode() {
        if (gapStrategy == null) {
            String gapModeString = sampleHandler.getLastSample(getObject(), GAP_FILLING.getAttributeName(), GapMode.NONE.toString());
            gapStrategy = new GapStrategy(gapModeString);
        }
        return gapStrategy;
    }

    public String getName() {
        return object.getName() + "," + object.getID();
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
