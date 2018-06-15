/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jecalc.gap.Gap.GapMode;
import org.jevis.jecalc.gap.Gap.GapStrategy;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jevis.jecalc.data.CleanDataAttributeJEVis.AttributeName.*;

/**
 * @author broder
 */
public class CleanDataAttributeJEVis implements CleanDataAttribute {

    public static final String CLASS_NAME = "Clean Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CleanDataAttributeJEVis.class);
    private final JEVisObject object;
    private JEVisObject rawDataObject;
    //attributes
    private Period period;
    private Boolean isPeriodAligned;
    private Boolean conversionDifferential;
    private Integer periodOffset;
    private Boolean valueIsQuantity;
    private Double multiplier;
    private Double offset;
    private GapStrategy gapStrategy;
    private Boolean enabled;
    //additional attributes
    private DateTime firstDate;
    private DateTime lastDate;
    private List<JEVisSample> rawSamples;
    private SampleHandler sampleHandler;
    private List<JsonGapFillingConfig> jsonConfig;
    private String gapFillingConfig;

    public CleanDataAttributeJEVis(JEVisObject calcObject) {
        object = calcObject;
    }

    public CleanDataAttributeJEVis(JEVisObject calcObject, ObjectHandler objectHandler) {
        object = calcObject;
        rawDataObject = objectHandler.getFirstParent(calcObject);
        offset = 0.0;

        sampleHandler = new SampleHandler();
        //get the period from the value attribute slider
        period = sampleHandler.getInputSampleRate(calcObject, VALUE_ATTRIBUTE_NAME);
        logger.info("Period is {}", PeriodFormat.getDefault().print(period));
        isPeriodAligned = sampleHandler.getLastSample(calcObject, PERIOD_ALIGNMENT.getAttributeName(), false);
        enabled = sampleHandler.getLastSample(calcObject, ENABLED.getAttributeName(), false);
        conversionDifferential = sampleHandler.getLastSample(calcObject, CONVERSION_DIFFERENTIAL.getAttributeName(), false);
        Long periodOffsetLong = sampleHandler.getLastSample(calcObject, PERIOD_OFFSET.getAttributeName(), 0l);
        periodOffset = (int) (long) periodOffsetLong;
        valueIsQuantity = sampleHandler.getLastSample(calcObject, VALUE_QUANTITY.getAttributeName(), false);
        multiplier = sampleHandler.getLastSample(calcObject, MULTIPLIER.getAttributeName(), 1.0);


        gapFillingConfig = sampleHandler.getLastSample(calcObject, GAP_FILLING_CONFIG.getAttributeName(), "");
        jsonConfig = new Gson().fromJson(gapFillingConfig, new TypeToken<List<JsonGapFillingConfig>>() {
        }.getType());
        for (JsonGapFillingConfig jgfc : jsonConfig) {
            System.out.println("ConfigString" + jgfc.toString());
        }

        //first date is the lastdate of clean datarow + period or the year of the first sample of the raw data
        DateTime timestampFromLastCleanSample = sampleHandler.getTimeStampFromLastSample(calcObject, VALUE_ATTRIBUTE_NAME);
        if (timestampFromLastCleanSample != null) {
            firstDate = timestampFromLastCleanSample.plus(period);
        } else {
            DateTime firstTimestampRaw = sampleHandler.getTimestampFromFirstSample(rawDataObject, VALUE_ATTRIBUTE_NAME);
            if (firstTimestampRaw != null) {
                firstDate = new DateTime(firstTimestampRaw.getYear(), firstTimestampRaw.getMonthOfYear(), firstTimestampRaw.getDayOfMonth(), 0, 0);
            } else {
                throw new RuntimeException("No raw values in clean data row");
            }
        }

        lastDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(period);

        String gapModeString = sampleHandler.getLastSample(calcObject, GAP_FILLING.getAttributeName(), GapMode.NONE.toString());
        gapStrategy = new GapStrategy(gapModeString);
    }

    @Override
    public Boolean getEnabled() {
        return enabled;
    }

    @Override
    public Boolean getIsPeriodAligned() {
        return isPeriodAligned;
    }

    @Override
    public Period getPeriodAlignment() {
        return period;
    }

    @Override
    public Boolean getConversionDifferential() {
        return conversionDifferential;
    }

    @Override
    public Integer getPeriodOffset() {
        return periodOffset;
    }

    @Override
    public Boolean getValueIsQuantity() {
        return valueIsQuantity;
    }

    public JEVisObject getObject() {
        return object;
    }

    @Override
    public DateTime getFirstDate() {
        return firstDate;
    }

    @Override
    public DateTime getMaxEndDate() {
        return lastDate;
    }

    @Override
    public List<JsonGapFillingConfig> getGapFillingConfig() {
        return jsonConfig;
    }

    @Override
    public List<JEVisSample> getRawSamples() {
        if (rawSamples != null) {
            return rawSamples;
        } else {
            rawSamples = sampleHandler.getSamplesInPeriod(rawDataObject, VALUE_ATTRIBUTE_NAME, firstDate.minus(period), lastDate);
//            rawSamples = rawDataObject.getAttribute("Value").getSamples(timestampFromLastSample, new DateTime());
            return rawSamples;
        }
    }

    @Override
    public Double getLastDiffValue() {
        Double lastValue = null;
        try {
            //if there are values in the clean data, then there should be a last value in the raw data
            JEVisAttribute attribute = object.getAttribute(VALUE_ATTRIBUTE_NAME);
            if (attribute.hasSample()) {
                DateTime timestampFromLastSample = attribute.getTimestampFromLastSample();
                DateTime lastPossibleDateTime = timestampFromLastSample.plus(period);
                DateTime firstDateTime = timestampFromLastSample.minus(period.multipliedBy(100));
                List<JEVisSample> samples = rawDataObject.getAttribute(VALUE_ATTRIBUTE_NAME).getSamples(firstDateTime, lastPossibleDateTime);
                Double firstRawValue = getRawSamples().get(0).getValueAsDouble();
                for (int i = samples.size() - 1; i >= 0; i--) {
                    Double valueAsDouble = samples.get(i).getValueAsDouble();
                    if (valueAsDouble < firstRawValue) {
                        lastValue = valueAsDouble;
                        break;
                    }
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(CleanDataAttributeJEVis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastValue;
    }

    @Override
    public Double getMultiplier() {
        return multiplier;
    }

    @Override
    public Double getOffset() {
        return offset;
    }

    @Override
    public Double getLastCleanValue() {
        Double lastValue = null;
        try {
            JEVisSample latestSample = object.getAttribute("Value").getLatestSample();
            if (latestSample != null) {
                lastValue = latestSample.getValueAsDouble();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(CleanDataAttributeJEVis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lastValue;
    }

    @Override
    public GapStrategy getGapFillingMode() {
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
        OFFSET("Value Offset"),
        VALUE("Value"),
        GAP_FILLING("Gap Filling"),
        ENABLED("Enabled"),
        GAP_FILLING_CONFIG("Gap Filling Config");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
