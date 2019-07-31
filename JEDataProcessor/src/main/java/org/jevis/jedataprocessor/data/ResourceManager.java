/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
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
    private List<JEVisSample> rawSamplesDown;
    private Map<DateTime, JEVisSample> notesMap;
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
            Period periodAlignment = getCleanDataObject().getRawDataPeriodAlignment();
            if (periodAlignment.getMonths() == 0 && periodAlignment.getYears() == 0) {
                Duration duration = periodAlignment.toStandardDuration();
                if (periodAlignment.toStandardMinutes().getMinutes() < 1) {
                    throw new IllegalStateException("Cant calculate the intervals with rawDataPeriodAlignment " + periodAlignment);
                }
                //the interval with date x begins at x - (duration/2) and ends at x + (duration/2)
                //Todo Month has no well defined duration -> cant handle months atm
                long halfDuration = duration.getMillis() / 2;

                Period cleanDataPeriodAlignment = getCleanDataObject().getCleanDataPeriodAlignment();
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
                    currentDate = currentDate.plus(periodAlignment);
                }
            } else {
                /**
                 * TODO: months and years
                 */
            }
            logger.info("{} intervals calculated", rawIntervals.size());
        }
        return rawIntervals;
    }

    public void setRawIntervals(List<CleanInterval> rawIntervals) {
        this.rawIntervals = rawIntervals;
    }
}
