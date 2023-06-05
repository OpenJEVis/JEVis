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
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.processor.workflow.PeriodRule;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.GapFillingReferencePeriod.ALL;
import static org.jevis.commons.constants.GapFillingReferencePeriod.MONTH;
import static org.jevis.commons.dataprocessing.CleanDataObject.AttributeName.PERIOD;
import static org.jevis.commons.dataprocessing.ForecastDataObject.AttributeName.*;

/**
 * @author broder
 */
public class ForecastDataObject {

    public static final String CLASS_NAME = "Forecast Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(ForecastDataObject.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JEVisObject forecastDataObject;
    private final JEVisObject parentDataObject;
    private Boolean enabled;
    //additional attributes
    private final SampleHandler sampleHandler;
    private JEVisAttribute inputAttribute;
    private List<PeriodRule> periodInputData;

    private JEVisAttribute valueAttribute;
    private JEVisAttribute enabledAttribute;
    private JEVisAttribute typeAttribute;
    private JEVisAttribute referencePeriodAttribute;
    private JEVisAttribute referencePeriodCountAttribute;
    private JEVisAttribute bindToSpecificAttribute;
    private JEVisAttribute forecastDurationAttribute;
    private JEVisAttribute forecastDurationCountAttribute;
    private JEVisAttribute periodAttribute;
    private DateTime lastRawDate;
    private int processingSize = 10000;
    private List<JEVisSample> sampleCache;

    private Period inputDataPeriod;
    private DateTimeZone zone;

    public ForecastDataObject(JEVisObject forecastObject, ObjectHandler objectHandler) {
        forecastDataObject = forecastObject;
        parentDataObject = objectHandler.getFirstParent(forecastObject);
        sampleHandler = new SampleHandler();
    }

    public void getAttributes() throws JEVisException {
        if (enabledAttribute == null) {
            enabledAttribute = getForecastDataObject().getAttribute(ENABLED.getAttributeName());
        }

        if (valueAttribute == null) {
            valueAttribute = getForecastDataObject().getAttribute(VALUE.getAttributeName());
        }

        if (typeAttribute == null) {
            typeAttribute = getForecastDataObject().getAttribute(TYPE.getAttributeName());
        }

        if (referencePeriodAttribute == null) {
            referencePeriodAttribute = getForecastDataObject().getAttribute(REFERENCE_PERIOD.getAttributeName());
        }

        if (referencePeriodCountAttribute == null) {
            referencePeriodCountAttribute = getForecastDataObject().getAttribute(REFERENCE_PERIOD_COUNT.getAttributeName());
        }

        if (bindToSpecificAttribute == null) {
            bindToSpecificAttribute = getForecastDataObject().getAttribute(BIND_TO_SPECIFIC.getAttributeName());
        }

        if (forecastDurationAttribute == null) {
            forecastDurationAttribute = getForecastDataObject().getAttribute(FORECAST_DURATION.getAttributeName());
        }

        if (forecastDurationCountAttribute == null) {
            forecastDurationCountAttribute = getForecastDataObject().getAttribute(FORECAST_DURATION_COUNT.getAttributeName());
        }

        if (periodAttribute == null) {
            periodAttribute = getParentDataObject().getAttribute(PERIOD.getAttributeName());
        }
    }

    public void reloadAttributes() throws JEVisException {
        getForecastDataObject().getDataSource().reloadAttribute(enabledAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(valueAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(typeAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(referencePeriodAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(referencePeriodCountAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(bindToSpecificAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(forecastDurationAttribute);
        getForecastDataObject().getDataSource().reloadAttribute(forecastDurationCountAttribute);
        getParentDataObject().getDataSource().reloadAttribute(periodAttribute);
    }

    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getForecastDataObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    public JEVisObject getForecastDataObject() {
        return forecastDataObject;
    }

    public JEVisObject getParentDataObject() {
        return parentDataObject;
    }

    public Map<DateTime, JEVisSample> getNotesMap() {
        Map<DateTime, JEVisSample> notesMap = new HashMap<>();
        try {
            final JEVisClass dataNoteClass = parentDataObject.getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject obj : forecastDataObject.getParents().get(0).getChildren(dataNoteClass, true)) {
                if (obj.getName().contains(forecastDataObject.getName())) {
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
        return forecastDataObject.getName() + ":" + forecastDataObject.getID();
    }

    public JEVisAttribute getValueAttribute() throws JEVisException {
        if (valueAttribute == null)
            valueAttribute = getForecastDataObject().getAttribute(VALUE.getAttributeName());
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
            typeAttribute = getForecastDataObject().getAttribute(TYPE.getAttributeName());
        return typeAttribute;
    }

    public JEVisAttribute getReferencePeriodAttribute() throws JEVisException {
        if (referencePeriodAttribute == null)
            referencePeriodAttribute = getForecastDataObject().getAttribute(REFERENCE_PERIOD.getAttributeName());
        return referencePeriodAttribute;
    }

    public JEVisAttribute getReferencePeriodCountAttribute() throws JEVisException {
        if (referencePeriodCountAttribute == null)
            referencePeriodCountAttribute = getForecastDataObject().getAttribute(REFERENCE_PERIOD_COUNT.getAttributeName());
        return referencePeriodCountAttribute;
    }

    public JEVisAttribute getBindToSpecificAttribute() throws JEVisException {
        if (bindToSpecificAttribute == null)
            bindToSpecificAttribute = getForecastDataObject().getAttribute(BIND_TO_SPECIFIC.getAttributeName());
        return bindToSpecificAttribute;
    }

    public JEVisAttribute getForecastDurationAttribute() throws JEVisException {
        if (forecastDurationAttribute == null)
            forecastDurationAttribute = getForecastDataObject().getAttribute(FORECAST_DURATION.getAttributeName());
        return forecastDurationAttribute;
    }

    public JEVisAttribute getForecastDurationCountAttribute() throws JEVisException {
        if (forecastDurationCountAttribute == null)
            forecastDurationCountAttribute = getForecastDataObject().getAttribute(FORECAST_DURATION_COUNT.getAttributeName());
        return forecastDurationCountAttribute;
    }

    public List<PeriodRule> getInputDataPeriodAlignment() {
        if (periodInputData == null) {
            periodInputData = new ArrayList<>();
            List<JEVisSample> allSamples = sampleHandler.getAllSamples(getParentDataObject(), PERIOD.getAttributeName());

            for (JEVisSample jeVisSample : allSamples) {

                try {
                    DateTime startOfPeriod = jeVisSample.getTimestamp();
                    String periodString = jeVisSample.getValueAsString();
                    Period p = new Period(periodString);
                    periodInputData.add(new PeriodRule(startOfPeriod, p));
                } catch (Exception e) {
                    logger.error("Could not create Period rule for sample {}", jeVisSample, e);
                }
            }

            if (allSamples.isEmpty()) {
                periodInputData.add(new PeriodRule(
                        new DateTime(1990, 1, 1, 0, 0, 0, 0),
                        Period.ZERO));
            }
        }
        return periodInputData;
    }

    public JEVisAttribute getEnabledAttribute() {
        return enabledAttribute;
    }

    public void setProcessingSize(int processingSize) {
        this.processingSize = processingSize;
    }

    public List<JEVisSample> getSampleCache() {
        if (this.sampleCache == null || this.sampleCache.isEmpty()) {
            GapFillingReferencePeriod referencePeriod = null;
            int referencePeriodCount = 6;
            try {
                if (getReferencePeriodAttribute().hasSample()) {
                    referencePeriod = GapFillingReferencePeriod.parse(getReferencePeriodAttribute().getLatestSample().getValueAsString());
                }
            } catch (JEVisException e) {
                logger.error("Could not get reference period from {}:{}, assuming default value of month", getForecastDataObject().getName(), getForecastDataObject().getID(), e);
                referencePeriod = MONTH;
            }

            try {
                if (getReferencePeriodCountAttribute().hasSample()) {
                    referencePeriodCount = getReferencePeriodCountAttribute().getLatestSample().getValueAsLong().intValue();
                }
            } catch (JEVisException e) {
                logger.error("Could not get reference period count from {}:{}, assuming default value of 6", getForecastDataObject().getName(), getForecastDataObject().getID(), e);
            }

            long duration = 0L;
            if (referencePeriod != null) {
                DateTime endDate = getStartDate();
                DateTime startDate = null;

                switch (referencePeriod) {
                    case DAY:
                        startDate = endDate.minusDays(referencePeriodCount);
                        break;
                    case MONTH:
                        startDate = endDate.minusMonths(referencePeriodCount);
                        break;
                    case WEEK:
                        startDate = endDate.minusWeeks(referencePeriodCount);
                        break;
                    case YEAR:
                        startDate = endDate.minusYears(referencePeriodCount);
                        break;
                    case ALL:
                        try {
                            sampleCache = getInputAttribute().getAllSamples();
                            return sampleCache;
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                try {
                    sampleCache = getInputAttribute().getSamples(startDate, endDate);
                    return sampleCache;
                } catch (JEVisException e) {
                    logger.error("Could not get samples from {}:{} in the interval {} to {}",
                            getForecastDataObject().getName(), getForecastDataObject().getID(), startDate, endDate, e);
                }
            }
            try {
                sampleCache = getInputAttribute().getAllSamples();
                return sampleCache;
            } catch (JEVisException ex) {
                ex.printStackTrace();
            }
            sampleCache = new ArrayList<>();
        }
        return sampleCache;
    }

    public DateTime getStartDate() {
        DateTime lastRun = getLastRun(this.getForecastDataObject()).withZone(getTimeZone());

        //align lastRun date to forecast duration

        DateTime alignedDate = lastRun;
        try {
            String forecastDuration = getForecastDurationAttribute().getLatestSample().getValueAsString();

            if (forecastDuration.equals("WEEKS")) {
                alignedDate = alignedDate.withDayOfWeek(1);
                forecastDuration = "DAYS";
            }

            switch (forecastDuration) {
                case "YEARS":
                    alignedDate = alignedDate.withMonthOfYear(1);
                case "MONTHS":
                    alignedDate = alignedDate.withDayOfMonth(1);
                case "DAYS":
                    alignedDate = alignedDate.withHourOfDay(0);
                case "HOURS":
                    alignedDate = alignedDate.withMinuteOfHour(0);
                case "MINUTES":
                    alignedDate = alignedDate.withSecondOfMinute(0);
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return alignedDate.withZone(DateTimeZone.UTC);
    }

    private static DateTime fixTimeZoneOffset(DateTimeZone tz, DateTime start, int offset) {
        int newOffset = tz.getOffset(start);

        if (newOffset > offset) {
            start = start.minus(newOffset - offset);
        } else if (newOffset < offset) {
            start = start.plus(offset - newOffset);
        }
        return start;
    }

    public boolean isReady(JEVisObject object) {
        DateTime lastRun = getLastRun(object);
        Long cycleTime = getCycleTime(object);
        DateTime nextRun = lastRun.plusMillis(cycleTime.intValue());
        try {
            DateTime lastTsOfInputData = getInputAttribute().getLatestSample().getTimestamp();
            return lastTsOfInputData.withZone(getTimeZone()).equals(nextRun) || lastTsOfInputData.withZone(getTimeZone()).isAfter(nextRun);
        } catch (Exception e) {
            logger.error("Could not get last ts of input data", e);
        }

        return false;
    }

    private DateTimeZone getTimeZone() {
        if (zone == null) {
            zone = DateTimeZone.UTC;

            JEVisAttribute timeZoneAttribute = null;
            try {
                timeZoneAttribute = getForecastDataObject().getAttribute("Timezone");
                if (timeZoneAttribute != null) {
                    JEVisSample lastTimeZoneSample = timeZoneAttribute.getLatestSample();
                    if (lastTimeZoneSample != null) {
                        zone = DateTimeZone.forID(lastTimeZoneSample.getValueAsString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return zone;
    }

    private DateTime getLastRun(JEVisObject object) {
        DateTime dateTime = new DateTime(1990, 1, 1, 0, 0, 0).withZone(getTimeZone());

        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                JEVisSample lastSample = lastRunAttribute.getLatestSample();
                if (lastSample != null) {
                    dateTime = new DateTime(lastSample.getValueAsString());
                }
            }

        } catch (Exception e) {
            logger.error("Could not get data source last run time: ", e);
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
            logger.error("Could not get data source cycle time: ", e);
        }

        return aLong;
    }

    public DateTime getEndDate() throws JEVisException {
        if (getForecastDurationAttribute().hasSample()) {
            String forecastDuration = getForecastDurationAttribute().getLatestSample().getValueAsString();
            int forecastDurationCount = 1;
            if (getForecastDurationCountAttribute().hasSample()) {
                forecastDurationCount = getForecastDurationCountAttribute().getLatestSample().getValueAsLong().intValue();
            }

            DateTime startDate = getStartDate().withZone(getTimeZone());
            switch (forecastDuration) {
                case "MINUTES":
                    return startDate.plusMinutes(forecastDurationCount).withZone(DateTimeZone.UTC);
                case "HOURS":
                    return startDate.plusHours(forecastDurationCount).withZone(DateTimeZone.UTC);
                case "DAYS":
                    return startDate.plusDays(forecastDurationCount).withZone(DateTimeZone.UTC);
                case "WEEKS":
                    return startDate.plusWeeks(forecastDurationCount).withZone(DateTimeZone.UTC);
                case "MONTHS":
                    return startDate.plusMonths(forecastDurationCount).withZone(DateTimeZone.UTC);
                case "YEARS":
                    return startDate.plusYears(forecastDurationCount).withZone(DateTimeZone.UTC);
            }
        }
        return null;
    }

    public void finishCurrentRun(JEVisObject object) {
        Long cycleTime = getCycleTime(object);
        DateTime lastRun = getLastRun(object);
        DateTimeZone timeZone = getTimeZone();
        int offset = timeZone.getOffset(lastRun);
        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                DateTime dateTime = lastRun.plusMillis(cycleTime.intValue());
                dateTime = fixTimeZoneOffset(timeZone, dateTime, offset);
                JEVisSample newSample = lastRunAttribute.buildSample(DateTime.now(), dateTime);
                newSample.commit();
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: ", e);
        }
    }

    public JsonGapFillingConfig getJsonGapFillingConfig() throws JEVisException {
        JsonGapFillingConfig jsonGapFillingConfig = new JsonGapFillingConfig();

        String referencePeriodCount = "6";
        GapFillingType type = GapFillingType.parse(getTypeAttribute().getLatestSample().getValueAsString());

        GapFillingReferencePeriod referencePeriod = GapFillingReferencePeriod.parse(getReferencePeriodAttribute().getLatestSample().getValueAsString());

        if (getReferencePeriodCountAttribute().hasSample()) {
            referencePeriodCount = getReferencePeriodCountAttribute().getLatestSample().getValueAsString();
        }

        if (referencePeriod == ALL) {
            referencePeriodCount = String.valueOf(Integer.MAX_VALUE);
        }

        GapFillingBoundToSpecific bindToSpecific = GapFillingBoundToSpecific.parse(getBindToSpecificAttribute().getLatestSample().getValueAsString());

        jsonGapFillingConfig.setType(type.toString());
        jsonGapFillingConfig.setReferenceperiod(referencePeriod.toString());
        jsonGapFillingConfig.setReferenceperiodcount(referencePeriodCount);
        jsonGapFillingConfig.setBindtospecific(bindToSpecific.toString());

        return jsonGapFillingConfig;
    }

    public enum AttributeName {
        VALUE("Value"),
        ENABLED("Enabled"),
        TYPE("Type"),
        REFERENCE_PERIOD("Reference Period"),
        REFERENCE_PERIOD_COUNT("Reference Period Count"),
        BIND_TO_SPECIFIC("Bind To Specific"),
        FORECAST_DURATION("Forecast Duration"),
        FORECAST_DURATION_COUNT("Forecast Duration Count");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
