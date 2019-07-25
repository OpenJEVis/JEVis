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
    private List<JEVisSample> rawSamplesUp;
    private Map<DateTime, JEVisSample> notesMap;

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

    public List<JEVisSample> getRawSamplesUp() {
        return rawSamplesUp;
    }

    public void setRawSamplesUp(List<JEVisSample> rawSamplesUp) {
        this.rawSamplesUp = rawSamplesUp;
    }

    public Long getID() {
        if (cleanDataObject == null || cleanDataObject.getCleanObject() == null) {
            return -1L;
        }
        return cleanDataObject.getCleanObject().getID();
    }

    public List<CleanInterval> getRawIntervals() {
        Period periodAlignment = getCleanDataObject().getRawDataPeriodAlignment();
        Duration duration = periodAlignment.toStandardDuration();
        //the interval with date x begins at x - (duration/2) and ends at x + (duration/2)
        //Todo Month has no well defined duration -> cant handle months atm
        long halfDuration = duration.getMillis() / 2;

        List<CleanInterval> cleanIntervals = new ArrayList<>();
        DateTime currentDate = getCleanDataObject().getFirstDate().minus(getCleanDataObject().getCleanDataPeriodAlignment()).minus(getCleanDataObject().getCleanDataPeriodAlignment());
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
            cleanIntervals.add(currentInterval);

            //calculate the next date
            currentDate = currentDate.plus(periodAlignment);
        }
        logger.info("{} intervals calculated", cleanIntervals.size());
        return cleanIntervals;
    }
}
