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
import org.jevis.commons.dataprocessing.processor.workflow.DifferentialRule;
import org.jevis.commons.dataprocessing.processor.workflow.PeriodRule;
import org.jevis.commons.datetime.PeriodArithmetic;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.json.JsonDeltaConfig;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.jevis.commons.dataprocessing.CleanDataObject.AttributeName.*;

/**
 * @author broder
 */
public class CleanDataObject {

    public static final String CLASS_NAME = "Clean Data";
    public static final String DATA_CLASS_NAME = "Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(CleanDataObject.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JEVisObject cleanObject;
    private JEVisObject rawDataObject;
    //attributes
    private List<PeriodRule> periodCleanData;
    private List<PeriodRule> periodRawData;
    private Boolean isPeriodAligned;
    private List<DifferentialRule> differentialRules;
    private Integer periodOffset;
    private Boolean valueIsQuantity;
    private List<JEVisSample> multiplier;
    private Double offset;
    private GapStrategy gapStrategy;
    private Boolean enabled;
    private Boolean alarmEnabled;
    //additional attributes
    private DateTime firstDate;
    private DateTime lastDate;
    private List<JEVisSample> rawSamplesDown;
    private final SampleHandler sampleHandler;

    private List<JsonGapFillingConfig> jsonGapFillingConfig;
    private JsonDeltaConfig jsonDeltaConfig;

    private Boolean limitsEnabled;
    private Boolean deltaEnabled;
    private Boolean gapFillingEnabled;
    private List<JsonLimitsConfig> jsonLimitsConfig;
    private List<JEVisSample> counterOverflow;
    private Double lastDiffValue;
    private Double lastCleanValue;
    private final boolean isFirstRunPeriod = true;
    private JEVisAttribute rawAttribute;

    private JEVisAttribute valueAttribute;
    private JEVisAttribute conversionToDifferentialAttribute;
    private JEVisAttribute enabledAttribute;
    private JEVisAttribute limitsEnabledAttribute;
    private JEVisAttribute limitsConfigurationAttribute;
    private JEVisAttribute gapFillingEnabledAttribute;
    private JEVisAttribute gapFillingConfigAttribute;
    private JEVisAttribute deltaEnabledAttribute;
    private JEVisAttribute deltaConfigurationAttribute;
    private JEVisAttribute alarmEnabledAttribute;
    private JEVisAttribute alarmConfigAttribute;
    private JEVisAttribute alarmLogAttribute;
    private JEVisAttribute periodAlignmentAttribute;
    private JEVisAttribute periodOffsetAttribute;
    private JEVisAttribute valueIsAQuantityAttribute;
    private JEVisAttribute valueMultiplierAttribute;
    private JEVisAttribute valueOffsetAttribute;
    private JEVisAttribute counterOverflowAttribute;
    private JEVisAttribute periodAttribute;
    private DateTime lastRawDate;
    private int processingSize = 10000;

    public CleanDataObject(JEVisObject cleanObject) {
        this.cleanObject = cleanObject;
        try {
            ObjectHandler objectHandler = new ObjectHandler(cleanObject.getDataSource());
            rawDataObject = objectHandler.getFirstParent(cleanObject);
        } catch (Exception e) {
            logger.error("Could not initialize Object Handler", e);
        }

        sampleHandler = new SampleHandler();
    }

    public CleanDataObject(JEVisObject cleanObject, ObjectHandler objectHandler) {
        this.cleanObject = cleanObject;
        rawDataObject = objectHandler.getFirstParent(cleanObject);
        sampleHandler = new SampleHandler();

        try {
            cleanObject.getDataSource().reloadAttribute(rawDataObject);
        } catch (Exception e) {
            logger.error("Could not reload input data object for object {}:{}", cleanObject.getName(), cleanObject.getID(), e);
        }
    }

    public static Double getMultiplierForDate(JEVisObject cleanObject, DateTime timestamp) {
        double multiplier = 1d;

        try {
            JEVisAttribute multiplierAttribute = cleanObject.getAttribute("Multiplier");
            if (multiplierAttribute != null) {
                List<JEVisSample> sampleList = multiplierAttribute.getAllSamples();
                for (JEVisSample sample : sampleList) {
                    int index = sampleList.indexOf(sample);
                    DateTime timeStampOfMultiplier = null;
                    DateTime nextTimeStampOfMultiplier = null;
                    Double multiplierDouble = null;

                    timeStampOfMultiplier = sample.getTimestamp();
                    multiplierDouble = sample.getValueAsDouble();

                    if (index + 1 < sampleList.size()) {
                        nextTimeStampOfMultiplier = sampleList.get(index + 1).getTimestamp();
                    }

                    if (timestamp.equals(timeStampOfMultiplier) || timestamp.isAfter(timeStampOfMultiplier) && ((nextTimeStampOfMultiplier == null) || timestamp.isBefore(nextTimeStampOfMultiplier))) {
                        BigDecimal multi = new BigDecimal(multiplierDouble.toString());
                        multiplier = multi.doubleValue();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not get multiplier for object {}:{} and date {}", cleanObject.getName(), cleanObject.getID(), timestamp, e);
        }

        return multiplier;
    }

    /**
     * Check if the configuration is valid. Returns false if configuration is not valid.
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
        if (getOffset() == null) {
            errors.add(("Offset is empty"));
        }
        try {
            JEVisAttribute cleanAtt = getValueAttribute();
            JEVisSample latestCleanSample = null;
            if (cleanAtt.hasSample()) {
                latestCleanSample = cleanAtt.getLatestSample();
            }

            DateTime timestampLatestCleanSample = null;

            if (latestCleanSample != null) {
                timestampLatestCleanSample = latestCleanSample.getTimestamp();
            }

            JEVisAttribute rawAtt = getRawAttribute();
            JEVisSample latestRawSample = null;

            if (rawAtt.hasSample()) {
                latestRawSample = rawAtt.getLatestSample();
            } else {
                errors.add(("No raw samples"));
            }

            DateTime timestampLatestRawSample = null;
            if (latestRawSample != null) {
                timestampLatestRawSample = latestRawSample.getTimestamp();
            }

            Period inputPeriod = getPeriodForDate(getRawDataPeriodAlignment(), timestampLatestRawSample);
            Period outputPeriod = getPeriodForDate(getCleanDataPeriodAlignment(), timestampLatestRawSample);
            PeriodComparator periodComparator = new PeriodComparator();
            int compare = periodComparator.compare(outputPeriod, inputPeriod);

            if (timestampLatestCleanSample != null && timestampLatestRawSample != null && compare < 1) {
                if (!timestampLatestRawSample.isAfter(timestampLatestCleanSample)) {
                    errors.add(("No new Samples for cleaning"));
                }
            } else if (timestampLatestCleanSample != null && compare < 1) {
                errors.add(("No new Samples for cleaning"));
            }
        } catch (Exception e) {
            errors.add("Error while loading latest samples");
        }

        if (!errors.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            errors.forEach(s -> {
                if (errors.indexOf(s) > 0) {
                    stringBuilder.append("-");
                }
                stringBuilder.append(s);
            });
            LogTaskManager.getInstance().getTask(getCleanObject().getID()).setException(new Exception(stringBuilder.toString()));
            logger.info("Job of {}:{} stopped. Error: {}", getCleanObject().getName(), getCleanObject().getID(), errors.toString());

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
        if (deltaEnabledAttribute == null) {
            deltaEnabledAttribute = getCleanObject().getAttribute(DELTA_ENABLED.getAttributeName());
        }
        if (deltaConfigurationAttribute == null) {
            deltaConfigurationAttribute = getCleanObject().getAttribute(DELTA_CONFIGURATION.getAttributeName());
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

        if (periodAttribute == null) {
            periodAttribute = getCleanObject().getAttribute(PERIOD.getAttributeName());
        }
    }

    public static Period getPeriodForDate(JEVisObject object, DateTime dateTime) {
        List<PeriodRule> periodRules = getPeriodAlignmentForObject(object);
        for (PeriodRule periodRule : periodRules) {
            PeriodRule nextRule = null;
            if (periodRules.size() > periodRules.indexOf(periodRule) + 1) {
                nextRule = periodRules.get(periodRules.indexOf(periodRule) + 1);
            }

            DateTime ts = periodRule.getStartOfPeriod();
            if (dateTime.equals(ts) || dateTime.isAfter(ts) && (nextRule == null || dateTime.isBefore(nextRule.getStartOfPeriod()))) {
                return periodRule.getPeriod();
            }
        }
        return Period.ZERO;
    }

    public static Period getPeriodForDate(List<PeriodRule> periodRules, DateTime dateTime) {
        for (PeriodRule periodRule : periodRules) {
            PeriodRule nextRule = null;
            if (periodRules.size() > periodRules.indexOf(periodRule) + 1) {
                nextRule = periodRules.get(periodRules.indexOf(periodRule) + 1);
            }

            DateTime ts = periodRule.getStartOfPeriod();
            if (dateTime.equals(ts) || dateTime.isAfter(ts) && (nextRule == null || dateTime.isBefore(nextRule.getStartOfPeriod()))) {
                return periodRule.getPeriod();
            }
        }
        return Period.ZERO;
    }

    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getCleanObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    public static List<PeriodRule> getPeriodAlignmentForObject(JEVisObject object) {
        List<PeriodRule> periodList = new ArrayList<>();
        List<JEVisSample> allSamples = new SampleHandler().getAllSamples(object, PERIOD.getAttributeName());

        for (JEVisSample jeVisSample : allSamples) {

            try {
                DateTime startOfPeriod = jeVisSample.getTimestamp();
                String periodString = jeVisSample.getValueAsString();
                Period p = new Period(periodString);
                periodList.add(new PeriodRule(startOfPeriod, p));
            } catch (Exception e) {
                logger.error("Could not create Period rule for sample {}", jeVisSample, e);
            }
        }

        if (allSamples.isEmpty()) {
            periodList.add(new PeriodRule(
                    new DateTime(1990, 1, 1, 0, 0, 0, 0),
                    Period.ZERO));
        }
        return periodList;
    }

    public Boolean getIsPeriodAligned() {
        if (isPeriodAligned == null)
            isPeriodAligned = sampleHandler.getLastSample(getCleanObject(), PERIOD_ALIGNMENT.getAttributeName(), false);
        return isPeriodAligned;
    }

    public static Boolean isDifferentialForDate(List<DifferentialRule> differentialRules, DateTime dateTime) {
        for (DifferentialRule differentialRule : differentialRules) {
            DifferentialRule nextRule = null;
            if (differentialRules.size() > differentialRules.indexOf(differentialRule) + 1) {
                nextRule = differentialRules.get(differentialRules.indexOf(differentialRule) + 1);
            }
            DateTime ts = differentialRule.getStartOfPeriod();
            if (dateTime.equals(ts) || dateTime.isAfter(ts) && (nextRule == null || dateTime.isBefore(nextRule.getStartOfPeriod()))) {
                return differentialRule.isDifferential();
            }
        }
        return false;
    }

    public static boolean isCounter(JEVisObject object, JEVisSample latestSample) {
        boolean isCounter = false;
        try {
            JEVisDataSource ds = null;
            if (object == null && latestSample.getDataSource() != null) {
                ds = latestSample.getDataSource();
                object = latestSample.getAttribute().getObject();
            } else if (object != null) {
                ds = object.getDataSource();
            } else return false;

            JEVisClass dataClass = ds.getJEVisClass("Data");
            JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
            if (object != null && object.getJEVisClass().equals(dataClass)) {
                JEVisObject cleanDataObject = CommonMethods.getFirstCleanObject(object);
                CleanDataObject cdo = new CleanDataObject(cleanDataObject);
                isCounter = CleanDataObject.isDifferentialForDate(cdo.getDifferentialRules(), latestSample.getTimestamp());
            } else if (object != null && object.getJEVisClass().equals(cleanDataClass)) {
                CleanDataObject cdo = new CleanDataObject(object);
                isCounter = CleanDataObject.isDifferentialForDate(cdo.getDifferentialRules(), latestSample.getTimestamp());
            }
        } catch (Exception e) {
            logger.error("Could not determine diff or not", e);
        }

        return isCounter;
    }

    public void reloadAttributes() throws JEVisException {

        if (conversionToDifferentialAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(conversionToDifferentialAttribute);
            differentialRules = null;
        }

        if (enabledAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(enabledAttribute);
            enabled = null;
        }

        if (limitsEnabledAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(limitsEnabledAttribute);
            limitsEnabled = null;
        }
        if (limitsConfigurationAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(limitsConfigurationAttribute);
            jsonLimitsConfig = null;
        }
        if (gapFillingEnabledAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(gapFillingEnabledAttribute);
            gapFillingEnabled = null;
        }
        if (gapFillingConfigAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(gapFillingConfigAttribute);
            jsonGapFillingConfig = null;
        }
        if (deltaEnabledAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(deltaEnabledAttribute);
            deltaEnabled = null;
        }
        if (deltaConfigurationAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(deltaConfigurationAttribute);
            jsonDeltaConfig = null;
        }
        if (alarmEnabledAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(alarmEnabledAttribute);
        }
        if (alarmConfigAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(alarmConfigAttribute);
        }
        if (alarmLogAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(alarmLogAttribute);
        }
        if (periodAlignmentAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(periodAlignmentAttribute);
            isPeriodAligned = null;
        }
        if (periodOffsetAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(periodOffsetAttribute);
            periodOffset = null;
        }
        if (valueIsAQuantityAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(valueIsAQuantityAttribute);
            valueIsQuantity = null;
        }
        if (valueMultiplierAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(valueMultiplierAttribute);
            multiplier = null;
        }
        if (valueOffsetAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(valueOffsetAttribute);
            offset = null;
        }
        if (counterOverflowAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(counterOverflowAttribute);
            counterOverflow = null;
        }
        if (valueAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(valueAttribute);
        }

        if (periodAttribute != null) {
            getCleanObject().getDataSource().reloadAttribute(periodAttribute);
        }

        periodRawData = null;
        periodCleanData = null;
        firstDate = null;
        lastDate = null;
        rawSamplesDown = null;
        lastCleanValue = null;
    }

    public Boolean isAlarmEnabled() {
        if (alarmEnabled == null)
            alarmEnabled = sampleHandler.getLastSample(getCleanObject(), ALARM_ENABLED.getAttributeName(), false);
        return alarmEnabled;
    }

    public List<PeriodRule> getRawDataPeriodAlignment() {
        if (periodRawData == null) {
            periodRawData = new ArrayList<>();
            List<JEVisSample> allSamples = sampleHandler.getAllSamples(getRawDataObject(), PERIOD.getAttributeName());

            for (JEVisSample jeVisSample : allSamples) {

                try {
                    DateTime startOfPeriod = jeVisSample.getTimestamp();
                    String periodString = jeVisSample.getValueAsString();
                    Period p = new Period(periodString);
                    periodRawData.add(new PeriodRule(startOfPeriod, p));
                } catch (Exception e) {
                    logger.error("Could not create Period rule for sample {} of object {}:{}", jeVisSample, getRawDataObject().getName(), getRawDataObject().getID(), e);
                }
            }

            if (allSamples.isEmpty()) {
                periodRawData.add(new PeriodRule(
                        new DateTime(1990, 1, 1, 0, 0, 0, 0),
                        Period.ZERO));
            }
        }
        return periodRawData;
    }

    public List<PeriodRule> getCleanDataPeriodAlignment() {
        if (periodCleanData == null) {
            periodCleanData = new ArrayList<>();
            List<JEVisSample> allSamples = sampleHandler.getAllSamples(getCleanObject(), PERIOD.getAttributeName());

            for (JEVisSample jeVisSample : allSamples) {

                try {
                    DateTime startOfPeriod = jeVisSample.getTimestamp();
                    String periodString = jeVisSample.getValueAsString();
                    Period p = new Period(periodString);
                    periodCleanData.add(new PeriodRule(startOfPeriod, p));
                } catch (Exception e) {
                    logger.error("Could not create Period rule for sample {}", jeVisSample, e);
                }
            }

            if (allSamples.isEmpty()) {
                periodCleanData.add(new PeriodRule(
                        new DateTime(1990, 1, 1, 0, 0, 0, 0),
                        Period.ZERO));
            }
        }
        return periodCleanData;
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

    public Boolean getDeltaEnabled() {
        if (deltaEnabled == null)
            deltaEnabled = sampleHandler.getLastSample(getCleanObject(), DELTA_ENABLED.getAttributeName(), false);
        return deltaEnabled;
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
            //first date is the last date of clean data row + periodCleanData or the year of the first sample of the raw data
            DateTime timestampFromLastCleanSample = null;
            JEVisAttribute attribute = null;
            try {
                attribute = getCleanObject().getAttribute(VALUE_ATTRIBUTE_NAME);
            } catch (Exception e) {
                logger.error("Could not get attribute {} of object {}:{}", VALUE_ATTRIBUTE_NAME, getCleanObject().getName(), getCleanObject().getID());
            }
            if (attribute != null) {
                JEVisSample lastSample = attribute.getLatestSample();
                if (lastSample != null) {
                    try {
                        timestampFromLastCleanSample = lastSample.getTimestamp();
                    } catch (Exception e) {
                        logger.error("Could not get last sample of attribute {} of object {}:{}", attribute.getName(), getCleanObject().getName(), getCleanObject().getID());
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
                    logger.error("Could not get first date for: {}:{}", getCleanObject().getName(), getCleanObject().getID());
//                    throw new RuntimeException("No raw values in clean data row");
                }
            }
        }
        return firstDate;
    }

    public void setFirstDate(DateTime firstDate) {
        this.firstDate = firstDate;
    }

    public Period getMaxPeriod(List<PeriodRule> list) {
        Period p1 = Period.ZERO;
        PeriodComparator periodComparator = new PeriodComparator();
        for (PeriodRule periodRule : list) {
            if (list.indexOf(periodRule) == 0) {
                p1 = periodRule.getPeriod();
            } else {
                Period p2 = periodRule.getPeriod();
                int compare = periodComparator.compare(p1, p2);
                if (compare < 0) {
                    p1 = p2;
                }
            }
        }
        return p1;
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
                } catch (Exception e) {
                    logger.error("Could not parse gapFillingConfig because of Exception: {}", limitsConfiguration, e);
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }

        }
        return jsonLimitsConfig;
    }

    public JsonDeltaConfig getDeltaConfig() {
        if (jsonDeltaConfig == null) {
            String deltaConfiguration = sampleHandler.getLastSample(getCleanObject(), DELTA_CONFIGURATION.getAttributeName(), "");
            if (deltaConfiguration != null && !deltaConfiguration.equals("")) {
                try {
                    jsonDeltaConfig = objectMapper.readValue(deltaConfiguration, JsonDeltaConfig.class);
                } catch (JsonParseException e) {
                    logger.error("Could not parse gapFillingConfig because of JsonParseException: {}", deltaConfiguration, e);
                    return null;
                } catch (JsonMappingException e) {
                    logger.error("Could not parse gapFillingConfig because of JsonMappingException: {}", deltaConfiguration, e);
                    return null;
                } catch (IOException e) {
                    logger.error("Could not parse gapFillingConfig because of IOException: {}", deltaConfiguration, e);
                    return null;
                } catch (Exception e) {
                    logger.error("Could not parse gapFillingConfig because of Exception: {}", deltaConfiguration, e);
                    return null;
                }
            } else {
                return null;
            }

        }
        return jsonDeltaConfig;
    }

    public List<DifferentialRule> getDifferentialRules() {
        if (differentialRules == null) {
            differentialRules = new ArrayList<>();
            List<JEVisSample> allSamples = sampleHandler.getAllSamples(getCleanObject(), CONVERSION_DIFFERENTIAL.getAttributeName());

            for (JEVisSample jeVisSample : allSamples) {
                try {
                    DateTime startOfPeriod = jeVisSample.getTimestamp();
                    Boolean isDifferential = jeVisSample.getValueAsBoolean();
                    differentialRules.add(new DifferentialRule(startOfPeriod, isDifferential));
                } catch (Exception e) {
                    logger.error("Could not create Differential rule for sample {}", jeVisSample, e);
                }
            }

            if (allSamples.isEmpty()) {
                differentialRules.add(new DifferentialRule(
                        new DateTime(1990, 1, 1, 0, 0, 0, 0), false));
            }
        }
        return differentialRules;
    }

    public DateTime getMaxEndDate() {
        if (lastDate == null) {
            try {
                List<JEVisSample> rawSamplesDown = getRawSamplesDown();
                if (!rawSamplesDown.isEmpty()) {
                    int indexLastRawSample = rawSamplesDown.size() - 1;

                    Period lastPeriod = getPeriodForDate(getCleanDataPeriodAlignment(), rawSamplesDown.get(indexLastRawSample).getTimestamp());

                    lastDate = this.rawSamplesDown.get(indexLastRawSample).getTimestamp().plus(lastPeriod);
                }
                //lastDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(getCleanDataPeriodAlignment());
            } catch (Exception e) {
                logger.error("Could not get timestamp of last Raw sample.");
            }
        }
        return lastDate;
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
        } catch (Exception e) {
            logger.error("Could not get notes map for {}:{}", cleanObject.getName(), cleanObject.getID(), e);
        }
        return notesMap;
    }

    public Map<DateTime, JEVisSample> getUserDataMap() {
        Map<DateTime, JEVisSample> userDataMap = new HashMap<>();
        try {
            final JEVisClass userDataClass = rawDataObject.getDataSource().getJEVisClass("User Data");
            for (JEVisObject parent : cleanObject.getParents()) {
                String parentName = parent.getName();
                for (JEVisObject parentParent : parent.getParents()) {
                    for (JEVisObject obj : parentParent.getChildren(userDataClass, true)) {
                        if (obj.getName().contains(parentName)) {
                            JEVisAttribute userDataValueAttribute = obj.getAttribute("Value");
                            if (userDataValueAttribute.hasSample()) {
                                for (JEVisSample smp : userDataValueAttribute.getAllSamples()) {
                                    userDataMap.put(smp.getTimestamp(), smp);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not get user data map for {}:{}", cleanObject.getName(), cleanObject.getID(), e);
        }
        return userDataMap;
    }

    public List<JEVisSample> getMultiplier() {
        if (multiplier == null) {
            multiplier = sampleHandler.getAllSamples(getCleanObject(), MULTIPLIER.getAttributeName());
            if (multiplier.isEmpty()) {
                multiplier.add(new VirtualSample(new DateTime(1990, 1, 1, 0, 0, 0, 0), 1.0));
            }
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

    public JEVisAttribute getDeltaEnabledAttribute() {
        return deltaEnabledAttribute;
    }

    public JEVisAttribute getDeltaConfigurationAttribute() {
        return deltaConfigurationAttribute;
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

    public JEVisAttribute getPeriodAttribute() {
        return periodAttribute;
    }

    public void setProcessingSize(int processingSize) {
        this.processingSize = processingSize;
    }

    public int getMaxProcessingSize() {
        return processingSize;
    }

    public List<JEVisSample> getRawSamplesDown() {
        if (rawSamplesDown == null) {
            List<PeriodRule> periods = new ArrayList<>();
            periods.addAll(getCleanDataPeriodAlignment());
            periods.addAll(getRawDataPeriodAlignment());
            Period maxPeriod = getMaxPeriod(periods);
            DateTime firstDate = getFirstDate()
                    .minus(maxPeriod)
                    .minus(maxPeriod)
                    .minus(maxPeriod)
                    .minus(maxPeriod);

            firstDate = PeriodHelper.alignDateToPeriod(firstDate, maxPeriod, getCleanObject());

            DateTime lastRawDate = getLastRawDate();
            DateTime lastDate1 = firstDate;
            DateTime lastDate2 = firstDate;
            DateTime lastDate = null;

            Period rawDataPeriod = getPeriodForDate(getRawDataPeriodAlignment(), firstDate);
            Period cleanDataPeriod = getPeriodForDate(getCleanDataPeriodAlignment(), firstDate);
            if (!rawDataPeriod.equals(Period.ZERO) || !cleanDataPeriod.equals(Period.ZERO)) {

                if (!rawDataPeriod.equals(Period.ZERO)) {
                    long l = PeriodArithmetic.periodsInAnInterval(new Interval(firstDate, lastDate1), rawDataPeriod);

                    while (l <= processingSize * 2d && (lastDate1.isBefore(lastRawDate) || lastDate1.equals(lastRawDate))) {
                        rawDataPeriod = getPeriodForDate(getRawDataPeriodAlignment(), lastDate1);
                        lastDate1 = lastDate1.plus(rawDataPeriod);
                        l = PeriodArithmetic.periodsInAnInterval(new Interval(firstDate, lastDate1), rawDataPeriod);
                    }
                }

                if (!cleanDataPeriod.equals(Period.ZERO)) {
                    long l = PeriodArithmetic.periodsInAnInterval(new Interval(firstDate, lastDate2), cleanDataPeriod);

                    while (l <= processingSize * 2d && (lastDate2.isBefore(lastRawDate) || lastDate2.equals(lastRawDate))) {
                        cleanDataPeriod = getPeriodForDate(getCleanDataPeriodAlignment(), lastDate2);
                        lastDate2 = lastDate2.plus(cleanDataPeriod);
                        l = PeriodArithmetic.periodsInAnInterval(new Interval(firstDate, lastDate2), cleanDataPeriod);
                    }
                }

                if (!lastDate1.equals(firstDate) && lastDate1.isBefore(lastDate2) || lastDate1.equals(lastDate2)) {
                    lastDate = lastDate1;
                } else if (!lastDate2.equals(firstDate) || lastDate2.isBefore(lastDate1)) {
                    lastDate = lastDate2;
                }
            }

            if (lastDate == null) {
                lastDate = lastRawDate;
            }

            rawSamplesDown = sampleHandler.getSamplesInPeriod(
                    rawDataObject,
                    VALUE_ATTRIBUTE_NAME,
                    firstDate,
                    lastDate);
        }
        return rawSamplesDown;
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
        PERIOD("Period"),
        GAP_FILLING("Gap Filling"),
        ENABLED("Enabled"),
        GAPFILLING_ENABLED("GapFilling Enabled"),
        GAP_FILLING_CONFIG("Gap Filling Config"),
        LIMITS_ENABLED("Limits Enabled"),
        LIMITS_CONFIGURATION("Limits Configuration"),
        DELTA_ENABLED("Delta Enabled"),
        DELTA_CONFIGURATION("Delta Config"),
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
            Period lastPeriod = getCleanDataPeriodAlignment().get(getCleanDataPeriodAlignment().size() - 1).getPeriod();
            lastRawDate = sampleHandler.getTimeStampFromLastSample(rawDataObject, VALUE_ATTRIBUTE_NAME).plus(lastPeriod);
        }

        return lastRawDate;
    }
}
