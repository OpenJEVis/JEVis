/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
public class ResourceManager {

    private static final Logger logger = LogManager.getLogger(ResourceManager.class);
    public List<CleanInterval> intervals = new ArrayList<>();
    private CleanDataObject cleanDataObject;
    private ForecastDataObject forecastDataObject;
    private List<JEVisSample> rawSamplesDown;
    private Map<DateTime, JEVisSample> notesMap;
    private Map<DateTime, JEVisSample> userDataMap;
    private List<JEVisSample> sampleCache = new ArrayList<>();
    private List<CleanInterval> rawIntervals = new ArrayList<>();

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
        if (sampleCache.isEmpty()) {
            try {
                DateTime minDateForCache = getCleanDataObject().getFirstDate().minusMonths(6);
                DateTime lastDateForCache = getCleanDataObject().getFirstDate();

                sampleCache = getCleanDataObject().getValueAttribute().getSamples(minDateForCache, lastDateForCache);
            } catch (Exception e) {
                logger.error("No caching possible: " + e);
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
            Period rawPeriodAlignment = getCleanDataObject().getRawDataPeriodAlignment();
            Period cleanDataPeriodAlignment = getCleanDataObject().getCleanDataPeriodAlignment();
            if (!rawPeriodAlignment.equals(Period.ZERO) && rawPeriodAlignment.getMonths() == 0 && rawPeriodAlignment.getYears() == 0
                    && cleanDataPeriodAlignment.getMonths() == 0 && cleanDataPeriodAlignment.getYears() == 0) {
                Duration duration = rawPeriodAlignment.toStandardDuration();
                if (rawPeriodAlignment.toStandardMinutes().getMinutes() < 1) {
                    throw new IllegalStateException("Cant calculate the intervals with rawDataPeriodAlignment " + rawPeriodAlignment);
                }
                //the interval with date x begins at x - (duration/2) and ends at x + (duration/2)
                //Todo Month has no well defined duration -> cant handle months atm
                long halfDuration = duration.getMillis() / 2;


                if (cleanDataPeriodAlignment.toStandardMinutes().getMinutes() < 1) {
                    throw new IllegalStateException("Cant calculate the intervals with cleanDataPeriodAlignment " + cleanDataPeriodAlignment);
                }
                DateTime currentDate = getCleanDataObject().getFirstDate().minus(cleanDataPeriodAlignment).minus(cleanDataPeriodAlignment);
                DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                DateTime maxEndDate = getCleanDataObject().getMaxEndDate();
                if (currentDate == null || maxEndDate == null || !currentDate.isBefore(maxEndDate)) {
                    throw new IllegalStateException("Cant calculate the intervals with startdate " + datePattern.print(currentDate) + " and enddate " + datePattern.print(maxEndDate));
                }
                logger.info("Calc interval between startdate {} and enddate {}", datePattern.print(currentDate), datePattern.print(maxEndDate));

                while (currentDate.isBefore(maxEndDate)) {
                    DateTime startInterval = currentDate.minus(halfDuration);
                    DateTime endInterval = currentDate.plus(halfDuration);
                    Interval interval = new Interval(startInterval, endInterval);

                    CleanInterval currentInterval = new CleanInterval(interval, currentDate);
                    rawIntervals.add(currentInterval);

                    //calculate the next date
                    currentDate = currentDate.plus(rawPeriodAlignment);
                }
            } else if (rawPeriodAlignment.equals(Period.ZERO) && cleanDataPeriodAlignment.equals(Period.minutes(15))) {
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

                    while (currentTs.isBefore(lastTs)) {
                        Interval interval = new Interval(currentTs, currentTs.plusMinutes(14).plusSeconds(59).plusMillis(999));
                        CleanInterval cleanInterval = new CleanInterval(interval, currentTs);
                        rawIntervals.add(cleanInterval);
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
                        Interval interval = new Interval(timestamp.minusMillis(1), timestamp);
                        CleanInterval cleanInterval = new CleanInterval(interval, timestamp);
                        rawIntervals.add(cleanInterval);
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }
            }
            logger.info("{} intervals calculated", rawIntervals.size());
        }
        return rawIntervals;
    }

    public void setRawIntervals(List<CleanInterval> rawIntervals) {
        this.rawIntervals = rawIntervals;
    }
}
