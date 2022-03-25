/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author gschutz
 */
public class ResourceManager {

    private static final Logger logger = LogManager.getLogger(ResourceManager.class);
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    public List<CleanInterval> intervals = new ArrayList<>();
    private CleanDataObject cleanDataObject;
    private ForecastDataObject forecastDataObject;
    private MathDataObject mathDataObject;
    private List<JEVisSample> rawSamplesDown;
    private Map<DateTime, JEVisSample> notesMap;
    private Map<DateTime, JEVisSample> userDataMap;
    private List<JEVisSample> sampleCache = new ArrayList<>();
    private List<CleanInterval> rawIntervals = new ArrayList<>();
    private Boolean isClean = true;

    public ResourceManager() {
        numberFormat.setMinimumFractionDigits(4);
        numberFormat.setMaximumFractionDigits(4);
    }

    public List<CleanInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<CleanInterval> intervals) {
        this.intervals = intervals;
    }

    public CleanDataObject getCleanDataObject() {
        return cleanDataObject;
    }

    public void setCleanDataObject(CleanDataObject cleanDataObject) {
        this.cleanDataObject = cleanDataObject;
    }

    public ForecastDataObject getForecastDataObject() {
        return forecastDataObject;
    }

    public void setForecastDataObject(ForecastDataObject forecastDataObject) {
        this.forecastDataObject = forecastDataObject;
    }

    public MathDataObject getMathDataObject() {
        return mathDataObject;
    }

    public void setMathDataObject(MathDataObject mathDataObject) {
        this.mathDataObject = mathDataObject;
    }

    public List<JEVisSample> getRawSamplesDown() {
        return rawSamplesDown;
    }

    public void setRawSamplesDown(List<JEVisSample> rawSamplesDown) {
        this.rawSamplesDown = rawSamplesDown;
    }

    public Map<DateTime, JEVisSample> getNotesMap() {
        return notesMap;
    }

    public void setNotesMap(Map<DateTime, JEVisSample> notesMap) {
        this.notesMap = notesMap;
    }

    public Map<DateTime, JEVisSample> getUserDataMap() {
        return userDataMap;
    }

    public void setUserDataMap(Map<DateTime, JEVisSample> userDataMap) {
        this.userDataMap = userDataMap;
    }

    public List<JEVisSample> getSampleCache() {
        if (sampleCache == null || sampleCache.isEmpty()) {
            try {
                DateTime date = getCleanDataObject().getFirstDate();
                DateTime minDateForCache = date;
                DateTime lastDateForCache = date;

                for (JsonGapFillingConfig jsonGapFillingConfig : getCleanDataObject().getGapFillingConfig()) {
                    try {
                        String referenceperiod = jsonGapFillingConfig.getReferenceperiod();
                        String referenceperiodcount = jsonGapFillingConfig.getReferenceperiodcount();

                        if (referenceperiod != null && referenceperiodcount != null) {
                            Integer c = Integer.parseInt(referenceperiodcount);
                            GapFillingReferencePeriod referencePeriod = GapFillingReferencePeriod.parse(referenceperiod);

                            switch (referencePeriod) {
                                case DAY:
                                    date = date.minusDays(c);
                                    break;
                                case WEEK:
                                    date = date.minusWeeks(c);
                                    break;
                                case MONTH:
                                    date = date.minusMonths(c);
                                    break;
                                case YEAR:
                                    date = date.minusYears(c);
                                    break;
                                case ALL:
                                    date = getCleanDataObject().getValueAttribute().getTimestampFromFirstSample();
                                    break;
                                case NONE:
                                    break;
                            }
                        }

                        if (date.isBefore(minDateForCache)) {
                            minDateForCache = date;
                        }
                    } catch (Exception e) {
                        logger.error("Error in gap filling config from object {}:{}", getCleanDataObject().getCleanObject().getName(), getCleanDataObject().getCleanObject().getID(), e);
                    }
                }

                sampleCache = getCleanDataObject().getCleanObject().getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME).getSamples(minDateForCache, lastDateForCache);

                if (sampleCache.isEmpty()) {
                    sampleCache = null;
                }
            } catch (Exception e) {
                logger.error("No caching possible: ", e);
            }
        }
        return sampleCache;
    }

    public void setSampleCache(List<JEVisSample> sampleCache) {
        this.sampleCache = sampleCache;
    }

    public Long getID() {
        if (cleanDataObject == null || cleanDataObject.getCleanObject() == null) {
            return -1L;
        }
        return cleanDataObject.getCleanObject().getID();
    }

    public List<CleanInterval> getRawIntervals() {
        if (rawIntervals.isEmpty()) {
            List<PeriodRule> rawPeriodAlignment = getCleanDataObject().getRawDataPeriodAlignment();
            List<PeriodRule> cleanDataPeriodAlignment = getCleanDataObject().getCleanDataPeriodAlignment();

            for (PeriodRule rawPeriodRule : rawPeriodAlignment) {
                Period currentRawDataPeriod = rawPeriodRule.getPeriod();
                DateTime nextRawDataPeriodTS = null;
                if (rawPeriodAlignment.size() > rawPeriodAlignment.indexOf(rawPeriodRule) + 1) {
                    nextRawDataPeriodTS = rawPeriodAlignment.get(rawPeriodAlignment.indexOf(rawPeriodRule) + 1).getStartOfPeriod();
                }

                Period currentCleanDataPeriod = CleanDataObject.getPeriodForDate(cleanDataPeriodAlignment, rawPeriodRule.getStartOfPeriod());

                if (!currentRawDataPeriod.equals(Period.ZERO) && currentRawDataPeriod.getMonths() == 0 && currentRawDataPeriod.getYears() == 0
                        && currentCleanDataPeriod.getMonths() == 0 && currentCleanDataPeriod.getYears() == 0) {
                    Duration duration = currentRawDataPeriod.toStandardDuration();
                    if (currentRawDataPeriod.toStandardMinutes().getMinutes() < 1) {
                        throw new IllegalStateException("Cant calculate the intervals with rawDataPeriodAlignment " + rawPeriodAlignment);
                    }
                    //the interval with date x begins at x - (duration/2) and ends at x + (duration/2)
                    //Todo Month has no well defined duration -> cant handle months atm
                    long halfDuration = duration.getMillis() / 2;


                    if (currentCleanDataPeriod.toStandardMinutes().getMinutes() < 1) {
                        throw new IllegalStateException("Cant calculate the intervals with cleanDataPeriodAlignment " + cleanDataPeriodAlignment);
                    }
                    DateTime currentDate = getCleanDataObject().getFirstDate().minus(currentCleanDataPeriod).minus(currentCleanDataPeriod);
                    DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                    DateTime maxEndDate = getCleanDataObject().getMaxEndDate();
                    if (currentDate == null || maxEndDate == null || !currentDate.isBefore(maxEndDate)) {
                        throw new IllegalStateException("Cant calculate the intervals with start date " + datePattern.print(currentDate) + " and end date " + datePattern.print(maxEndDate));
                    }
                    logger.info("Calc interval between start date {} and end date {}", datePattern.print(currentDate), datePattern.print(maxEndDate));

                    while (currentDate.isBefore(maxEndDate) && (nextRawDataPeriodTS == null || currentDate.isBefore(nextRawDataPeriodTS))) {
                        DateTime startInterval = currentDate.minus(halfDuration);
                        DateTime endInterval = currentDate.plus(halfDuration);
                        Interval interval = new Interval(startInterval, endInterval);

                        CleanInterval currentInterval = new CleanInterval(interval, currentDate);
                        rawIntervals.add(currentInterval);

                        //calculate the next date
                        currentDate = currentDate.plus(currentRawDataPeriod);
                    }
                } else if (currentRawDataPeriod.equals(Period.ZERO) && currentCleanDataPeriod.equals(Period.minutes(15))) {
                    try {
                        DateTime firstTs = cleanDataObject.getRawSamplesDown().get(0).getTimestamp();
                        if (firstTs.getMinuteOfHour() < 15) {
                            firstTs = firstTs.withMinuteOfHour(0);
                        } else if (firstTs.getMinuteOfHour() < 30) {
                            firstTs = firstTs.withMinuteOfHour(15);
                        } else if (firstTs.getMinuteOfHour() < 45) {
                            firstTs = firstTs.withMinuteOfHour(30);
                        } else {
                            firstTs = firstTs.withMinuteOfHour(45);
                        }

                        DateTime currentTs = firstTs;
                        DateTime lastTs = cleanDataObject.getRawSamplesDown().get(rawSamplesDown.size() - 1).getTimestamp();

                        while (currentTs.isBefore(lastTs) && (nextRawDataPeriodTS == null || currentTs.isBefore(nextRawDataPeriodTS))) {
                            Interval interval = new Interval(currentTs, currentTs.plusMinutes(14).plusSeconds(59).plusMillis(999));
                            CleanInterval CleanInterval = new CleanInterval(interval, currentTs);
                            rawIntervals.add(CleanInterval);
                            currentTs = currentTs.plusMinutes(15);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    /**
                     * TODO: months and years
                     */
                    for (JEVisSample sample : cleanDataObject.getRawSamplesDown()) {
                        try {
                            DateTime timestamp = sample.getTimestamp();

                            if (nextRawDataPeriodTS == null || timestamp.isBefore(nextRawDataPeriodTS)) {

                                DateTime start = timestamp.minusMillis(1);
                                DateTime end = timestamp;

                                if (currentRawDataPeriod.equals(Period.months(1))) {
                                    timestamp = timestamp.minusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                                    start = timestamp.plusMillis(1);
                                    end = timestamp.plusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                                }

                                Interval interval = new Interval(start, end);
                                CleanInterval CleanInterval = new CleanInterval(interval, timestamp);
                                rawIntervals.add(CleanInterval);
                            }
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    }
                }
                logger.info("{} intervals calculated", rawIntervals.size());
            }
        }

        return rawIntervals;
    }

    public void setRawIntervals(List<CleanInterval> rawIntervals) {
        this.rawIntervals = rawIntervals;
    }

    public Boolean isClean() {
        return isClean;
    }

    public void setClean(Boolean clean) {
        isClean = clean;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }
}
