/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Creates empty interval classes from start date to end date
 *
 * @author broder
 */
public class PrepareStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(PrepareStep.class);

    @Override

    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        //get the raw samples for the cleaning
        logger.info("[{}] Request raw samples", resourceManager.getID());
        List<JEVisSample> rawSamples = cleanDataObject.getRawSamples();
        logger.info("[{}] raw samples found for cleaning: {}", resourceManager.getID(), rawSamples.size());
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("Raw S.", rawSamples.size() + "");

        if (rawSamples.isEmpty()) {
            logger.info("[{}] No new raw date stopping this job", resourceManager.getID());
            return;
        }

        resourceManager.setRawSamples(rawSamples);

        Map<DateTime, JEVisSample> notesMap = cleanDataObject.getNotesMap();
        resourceManager.setNotesMap(notesMap);


        Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
        Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();
        if (rawSamples.size() > 1)
            logger.info("[{}] Input Period is {}", resourceManager.getID(), PeriodFormat.getDefault().print(new Period(rawSamples.get(0).getTimestamp(), rawSamples.get(1).getTimestamp())));
        logger.info("[{}] Period is {}", resourceManager.getID(), PeriodFormat.getDefault().print(periodCleanData));
        logger.info("[{}] Samples should be aligned {}", resourceManager.getID(), cleanDataObject.getIsPeriodAligned());
        if (periodCleanData.equals(Period.ZERO) && cleanDataObject.getIsPeriodAligned()) {
            throw new RuntimeException("No Input Sample Rate given for Object Clean Data and Attribute Value");
        } else if (cleanDataObject.getIsPeriodAligned()) {
            List<CleanInterval> cleanIntervals = getIntervals(cleanDataObject, periodCleanData, periodRawData);
            resourceManager.setIntervals(cleanIntervals);
        } else {
            List<CleanInterval> cleanIntervals = getIntervalsFromRawSamples(cleanDataObject, rawSamples);
            resourceManager.setIntervals(cleanIntervals);
        }

    }

    private List<CleanInterval> getIntervals(CleanDataObject calcAttribute, Period periodCleanData, Period periodRawData) throws JEVisException {
        List<CleanInterval> cleanIntervals = new ArrayList<>();

        if (calcAttribute.getMaxEndDate() == null) {
            logger.info("[{}] No Raw data, nothing to to", calcAttribute.getCleanObject().getID());
            return cleanIntervals;
        }

        DateTime currentDate = calcAttribute.getFirstDate();
        DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime maxEndDate = calcAttribute.getMaxEndDate();

        logger.error("[{}] getIntervals: currentDate: {}  MaxEndDate: {} ", calcAttribute.getCleanObject().getID(), currentDate, maxEndDate);

        if (currentDate == null || maxEndDate == null || !currentDate.isBefore(maxEndDate)) {
            logger.warn("Nothing to do with only one interval");
            return cleanIntervals;
//            throw new Exception(String.format("Cant calculate the intervals with start date %s  and end date %s", datePattern.print(currentDate), datePattern.print(maxEndDate)));
//            logger.error("Cant calculate the intervals with start date " + datePattern.print(currentDate) + " and end date " + datePattern.print(maxEndDate));
        } else {
            logger.info("[{}] Calc interval between start date {} and end date {}", calcAttribute.getCleanObject().getID(), datePattern.print(currentDate), datePattern.print(maxEndDate));

            WorkDays wd = new WorkDays(calcAttribute.getCleanObject());
            LocalTime dtStart = wd.getWorkdayStart();
            LocalTime dtEnd = wd.getWorkdayEnd();
            DateTime lastDate = null;

            while (currentDate.isBefore(maxEndDate) && periodCleanData.toStandardDuration().getMillis() > 0 && !currentDate.equals(lastDate)) {
                DateTime startInterval = null;
                DateTime endInterval = null;
                lastDate = currentDate;

                if (periodCleanData.toStandardDuration().getMillis() > periodRawData.toStandardDuration().getMillis()) {
                    /**
                     * for aggregation purposes
                     */
                    if (periodCleanData.equals(Period.years(1))) {
                        /**
                         * smaller to Year
                         */
                        if (currentDate.getYear() == maxEndDate.getYear()) currentDate = currentDate.minusYears(1);

                        startInterval = new DateTime(currentDate.getYear(), 1, 1,
                                dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond());
                        endInterval = new DateTime(currentDate.getYear(), 1, 1,
                                dtEnd.getHour(), dtEnd.getMinute(), dtEnd.getSecond()).plusYears(1);
                        if (dtEnd.isBefore(dtStart)) Objects.requireNonNull(startInterval).minusDays(1);
                    } else if (periodCleanData.equals(Period.months(1))) {
                        /**
                         * smaller to Month
                         */
                        if (currentDate.getYear() == maxEndDate.getYear() &&
                                currentDate.getMonthOfYear() == maxEndDate.getMonthOfYear())
                            currentDate = currentDate.minusMonths(1);

                        startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), 1,
                                dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond());
                        endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), 1,
                                dtEnd.getHour(), dtEnd.getMinute(), dtEnd.getSecond()).plusMonths(1);
                        if (dtEnd.isBefore(dtStart)) Objects.requireNonNull(startInterval).minusDays(1);
                    } else if (periodCleanData.equals(Period.days(1))) {
                        /**
                         * smaller to Days
                         */
                        if (currentDate.getYear() == maxEndDate.getYear() &&
                                currentDate.getMonthOfYear() == maxEndDate.getMonthOfYear() &&
                                currentDate.getDayOfMonth() == maxEndDate.getDayOfMonth())
                            currentDate = currentDate.minusDays(1);

                        startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                                dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond());
                        endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                                dtEnd.getHour(), dtEnd.getMinute(), dtEnd.getSecond());
                    } else if (periodCleanData.equals(Period.hours(1))) {
                        /**
                         * smaller to Hour
                         */
                        if (currentDate.getYear() == maxEndDate.getYear() &&
                                currentDate.getMonthOfYear() == maxEndDate.getMonthOfYear() &&
                                currentDate.getDayOfMonth() == maxEndDate.getDayOfMonth() &&
                                currentDate.getHourOfDay() == maxEndDate.getHourOfDay())
                            currentDate = currentDate.minusHours(1);

                        startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                                currentDate.getHourOfDay(), 0, 0);
                        endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                                currentDate.getHourOfDay(), 59, 59, 999);
                    } else if (periodCleanData.equals(Period.minutes(1))) {
                        /**
                         * smaller to Minute
                         */
                        if (currentDate.getYear() == maxEndDate.getYear() &&
                                currentDate.getMonthOfYear() == maxEndDate.getMonthOfYear() &&
                                currentDate.getDayOfMonth() == maxEndDate.getDayOfMonth() &&
                                currentDate.getHourOfDay() == maxEndDate.getHourOfDay() &&
                                currentDate.getMinuteOfHour() == maxEndDate.getMinuteOfHour())
                            currentDate = currentDate.minusMinutes(1);

                        startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                                currentDate.getHourOfDay(), currentDate.getMinuteOfHour(), 0);
                        endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                                currentDate.getHourOfDay(), currentDate.getMinuteOfHour(), 59, 999).plusMinutes(1);
                    } else {
                        long duration = periodCleanData.toStandardDuration().getMillis();
                        long halfDuration = duration / 2;

                        startInterval = currentDate.minus(halfDuration);
                        endInterval = currentDate.plus(halfDuration);
                    }

                    if (dtEnd.isBefore(dtStart)) startInterval = Objects.requireNonNull(startInterval).minusDays(1);
                } else {
                    long duration = periodCleanData.toStandardDuration().getMillis();
                    long halfDuration = duration / 2;

                    startInterval = currentDate.minus(halfDuration);
                    endInterval = currentDate.plus(halfDuration);
                }

                if (startInterval != null && endInterval != null) {
                    Interval interval = new Interval(startInterval, endInterval);
                    CleanInterval currentInterval = new CleanInterval(interval, currentDate);
                    cleanIntervals.add(currentInterval);
                }

                currentDate = currentDate.plus(periodCleanData);
            }

            logger.info("[{}] {} intervals calculated", calcAttribute.getCleanObject().getID(), cleanIntervals.size());
        }

        if (cleanIntervals.isEmpty()) {
            LogTaskManager.getInstance().getTask(calcAttribute.getCleanObject().getID()).setStatus(Task.Status.IDLE);
        }

        return cleanIntervals;
    }

    private List<CleanInterval> getIntervalsFromRawSamples(CleanDataObject calcAttribute, List<JEVisSample> rawSamples) throws Exception {
        List<CleanInterval> cleanIntervals = new ArrayList<>();
        for (JEVisSample curSample : rawSamples) {
            DateTime startInterval = curSample.getTimestamp().plusSeconds(calcAttribute.getPeriodOffset());
            DateTime endInterval = startInterval.plusMillis(1);
            Interval interval = new Interval(startInterval, endInterval);

            CleanInterval currentInterval = new CleanInterval(interval, startInterval);
            cleanIntervals.add(currentInterval);

        }
        logger.info("[{}] {} intervals calculated", calcAttribute.getCleanObject().getID(), cleanIntervals.size());
        return cleanIntervals;
    }
}
