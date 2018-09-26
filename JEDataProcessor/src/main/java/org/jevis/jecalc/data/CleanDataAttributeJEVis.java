/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jecalc.gap.Gap.GapMode;
import org.jevis.jecalc.gap.Gap.GapStrategy;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

import static org.jevis.jecalc.data.CleanDataAttributeJEVis.AttributeName.*;

/**
 * @author broder
 */
public class CleanDataAttributeJEVis implements CleanDataAttribute {

    public static final String CLASS_NAME = "Clean Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(CleanDataAttributeJEVis.class);
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

    public CleanDataAttributeJEVis(JEVisObject calcObject, ObjectHandler objectHandler) {
        object = calcObject;
        rawDataObject = objectHandler.getFirstParent(calcObject);
        sampleHandler = new SampleHandler();
    }

    @Override
    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    @Override
    public Boolean getIsPeriodAligned() {
        if (isPeriodAligned == null)
            isPeriodAligned = sampleHandler.getLastSample(getObject(), PERIOD_ALIGNMENT.getAttributeName(), false);
        return isPeriodAligned;
    }

    @Override
    public Period getPeriodAlignment() {
        if (period == null)
            period = sampleHandler.getInputSampleRate(getObject(), VALUE_ATTRIBUTE_NAME);
        return period;
    }

    @Override
    public List<JEVisSample> getConversionDifferential() {
        if (conversionDifferential == null)
            conversionDifferential = sampleHandler.getAllSamples(getObject(), CONVERSION_DIFFERENTIAL.getAttributeName()); //false;
        return conversionDifferential;
    }

    @Override
    public Integer getPeriodOffset() {
        if (periodOffset == null) {
            Long periodOffsetLong = sampleHandler.getLastSample(getObject(), PERIOD_OFFSET.getAttributeName(), 0L);
            periodOffset = (int) (long) periodOffsetLong;
        }
        return periodOffset;
    }

    @Override
    public Boolean getValueIsQuantity() {
        if (valueIsQuantity == null)
            valueIsQuantity = sampleHandler.getLastSample(getObject(), VALUE_QUANTITY.getAttributeName(), false);
        return valueIsQuantity;
    }

    @Override
    public Boolean getLimitsEnabled() {
        if (limitsEnabled == null)
            limitsEnabled = sampleHandler.getLastSample(getObject(), LIMITS_ENABLED.getAttributeName(), false);
        return limitsEnabled;
    }

    @Override
    public Boolean getGapFillingEnabled() {
        if (gapFillingEnabled == null)
            gapFillingEnabled = sampleHandler.getLastSample(getObject(), GAPFILLING_ENABLED.getAttributeName(), false);
        return gapFillingEnabled;
    }

    public JEVisObject getObject() {
        return object;
    }

    @Override
    public List<JEVisSample> getCounterOverflow() {
        if (counterOverflow == null)
            counterOverflow = sampleHandler.getAllSamples(getObject(), COUNTEROVERFLOW.getAttributeName());
        return counterOverflow;
    }

    @Override
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
                    throw new RuntimeException("No raw values in clean data row");
                }
            }
        }
        return firstDate;
    }

    @Override
    public DateTime getMaxEndDate() {
        if (lastDate == null)
            lastDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(period);
        return lastDate;
    }

    @Override
    public List<JsonGapFillingConfig> getGapFillingConfig() {
        if (jsonGapFillingConfig == null) {
            String gapFillingConfig = sampleHandler.getLastSample(getObject(), GAP_FILLING_CONFIG.getAttributeName(), "");
            if (gapFillingConfig != null && !gapFillingConfig.equals("")) {
                jsonGapFillingConfig = new Gson().fromJson(gapFillingConfig, new TypeToken<List<JsonGapFillingConfig>>() {
                }.getType());
            }
        }
        return jsonGapFillingConfig;
    }

    @Override
    public List<JsonLimitsConfig> getLimitsConfig() {
        if (jsonLimitsConfig == null) {
            String limitsConfiguration = sampleHandler.getLastSample(getObject(), LIMITS_CONFIGURATION.getAttributeName(), "");
            if (limitsConfiguration != null && !limitsConfiguration.equals("")) {
                jsonLimitsConfig = new Gson().fromJson(limitsConfiguration, new TypeToken<List<JsonLimitsConfig>>() {
                }.getType());
            }
        }
        return jsonLimitsConfig;
    }

    @Override
    public List<JEVisSample> getRawSamples() {
        if (rawSamples == null) {
            rawSamples = sampleHandler.getSamplesInPeriod(rawDataObject,
                    VALUE_ATTRIBUTE_NAME,
                    getFirstDate().minus(getPeriodAlignment()),
                    getMaxEndDate());
        }
        return rawSamples;
    }

    @Override
    public Double getLastDiffValue() {
        if (lastDiffValue == null) {
            try {
                //if there are values in the clean data, then there should be a last value in the raw data
                JEVisAttribute attribute = getObject().getAttribute(VALUE_ATTRIBUTE_NAME);
                if (attribute.hasSample()) {
                    DateTime timestampFromLastSample = attribute.getTimestampFromLastSample();
                    //DateTime lastPossibleDateTime = timestampFromLastSample.plus(period);
                    //DateTime firstDateTime = timestampFromLastSample.minus(period.multipliedBy(100));
                    //List<JEVisSample> samples = rawDataObject.getAttribute(VALUE_ATTRIBUTE_NAME).getSamples(firstDateTime, lastPossibleDateTime);
                    List<JEVisSample> samples = rawDataObject.getAttribute(VALUE_ATTRIBUTE_NAME).getSamples(timestampFromLastSample, timestampFromLastSample);

                    if (!samples.isEmpty()) {
                        lastDiffValue = samples.get(0).getValueAsDouble();
                        //TODO this is working for period aligned stuff, other needs testing, old version was producing unexpected spikes in the values
                    }

//                Double firstRawValue = samples.get(0).getValueAsDouble();
//                for (int i = samples.size() - 1; i >= 0; i--) {
//                    Double valueAsDouble = samples.get(i).getValueAsDouble();
//                    if (valueAsDouble > firstRawValue) {
//                        lastValue = valueAsDouble;
//                        break;
//                    }
//                }
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return lastDiffValue;
    }

    @Override
    public List<JEVisSample> getMultiplier() {
        if (multiplier == null)
            multiplier = sampleHandler.getAllSamples(getObject(), MULTIPLIER.getAttributeName());
        return multiplier;
    }

    @Override
    public Double getOffset() {
        if (offset == null) {
            offset = sampleHandler.getLastSample(getObject(), OFFSET.getAttributeName(), 0.0);
        }
        return offset;
    }

    @Override
    public Double getLastCleanValue() {
        if (lastCleanValue == null) {
            try {
                JEVisSample latestSample = getObject().getAttribute("Value").getLatestSample();
                if (latestSample != null) {
                    lastCleanValue = latestSample.getValueAsDouble();
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return lastCleanValue;
    }

    @Override
    public GapStrategy getGapFillingMode() {
        if (gapStrategy == null) {
            String gapModeString = sampleHandler.getLastSample(getObject(), GAP_FILLING.getAttributeName(), GapMode.NONE.toString());
            gapStrategy = new GapStrategy(gapModeString);
        }
        return gapStrategy;
    }

    @Override
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
