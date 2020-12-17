/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.preparation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates empty interval classes from start date to end date
 *
 * @author gschutz
 */
public class PrepareStepN implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(PrepareStepN.class);

    @Override

    public void run(ResourceManagerN resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        //get the raw samples for the cleaning
        logger.info("[{}] Request raw samples", resourceManager.getID());
        List<JEVisSample> rawSamplesDown = cleanDataObject.getRawSamplesDown();
//        List<JEVisSample> rawSamplesUp = cleanDataObject.getRawSamplesUp();
        logger.info("[{}] raw samples found for cleaning: {}", resourceManager.getID(), rawSamplesDown.size());
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("Raw S.", rawSamplesDown.size() + "");

//        if (rawSamplesDown.isEmpty() || rawSamplesUp.isEmpty()) {
        if (rawSamplesDown.isEmpty()) {
            logger.info("[{}] No new raw date. Stopping this job", resourceManager.getID());
            return;
        }

        resourceManager.setRawSamplesDown(rawSamplesDown);

        Map<DateTime, JEVisSample> notesMap = cleanDataObject.getNotesMap();
        resourceManager.setNotesMap(notesMap);

        Map<DateTime, JEVisSample> userDataMap = cleanDataObject.getUserDataMap();
        resourceManager.setUserDataMap(userDataMap);

        List<PeriodRule> periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();

        if (periodCleanData.isEmpty() && cleanDataObject.getIsPeriodAligned()) {
            throw new RuntimeException("No Input Sample Rate given for Object Clean Data and Attribute Value");
        } else if (cleanDataObject.getIsPeriodAligned()) {
            List<CleanIntervalN> CleanIntervalNs = getIntervals(cleanDataObject, periodCleanData);
            resourceManager.setIntervals(CleanIntervalNs);
        } else {
            List<CleanIntervalN> CleanIntervalNs = getIntervalsFromRawSamples(cleanDataObject, rawSamplesDown);
            resourceManager.setIntervals(CleanIntervalNs);
        }

    }

    private List<CleanIntervalN> getIntervals(CleanDataObject cleanDataObject, List<PeriodRule> periodCleanData) throws JEVisException {
        List<CleanIntervalN> cleanIntervals = new ArrayList<>();

        if (cleanDataObject.getMaxEndDate() == null) {
            logger.info("[{}] No Raw data, nothing to to", cleanDataObject.getCleanObject().getID());
            return cleanIntervals;
        }

        List<PeriodRule> periodRawData = cleanDataObject.getRawDataPeriodAlignment();
        List<DifferentialRule> differentialRules = cleanDataObject.getDifferentialRules();
        DateTime currentDate = cleanDataObject.getFirstDate();
        DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime maxEndDate = cleanDataObject.getMaxEndDate();

        logger.info("[{}] getIntervals: currentDate: {}  MaxEndDate: {} ", cleanDataObject.getCleanObject().getID(), currentDate, maxEndDate);

        if (currentDate == null || maxEndDate == null || !currentDate.isBefore(maxEndDate)) {
            logger.warn("Nothing to do with only one interval");
            return cleanIntervals;
        } else {
            logger.info("[{}] Calc interval between start date {} and end date {}", cleanDataObject.getCleanObject().getID(), datePattern.print(currentDate), datePattern.print(maxEndDate));

            WorkDays wd = new WorkDays(cleanDataObject.getCleanObject());
            LocalTime dtStart = wd.getWorkdayStart();
            LocalTime dtEnd = wd.getWorkdayEnd();
            DateTime lastDate = null;

            Boolean firstIsDifferential = CleanDataObject.isDifferentialForDate(differentialRules, currentDate);
            Period firstRawPeriod = CleanDataObject.getPeriodForDate(periodRawData, currentDate);
            Period firstCleanPeriod = CleanDataObject.getPeriodForDate(periodCleanData, currentDate);

            //add half a period to maxEndDate
            if (firstCleanPeriod.getYears() > 0) {
                currentDate = currentDate.minusYears(1);
                maxEndDate = maxEndDate.plusMonths(6);
            }
            if (firstCleanPeriod.getMonths() > 0) {
                currentDate = currentDate.minusMonths(1);
                maxEndDate = maxEndDate.plusWeeks(2);
            }
            if (firstCleanPeriod.getWeeks() > 0) {
                currentDate = currentDate.minusWeeks(1);
                maxEndDate = maxEndDate.plusDays(3).plusHours(12);
            }
            if (firstCleanPeriod.getDays() > 0) {
                currentDate = currentDate.minusDays(1);
                maxEndDate = maxEndDate.plusHours(12);
            }
            if (firstCleanPeriod.getHours() > 0) {
                currentDate = currentDate.minusHours(1);
                maxEndDate = maxEndDate.plusMinutes(30);
            }

            while (currentDate.isBefore(maxEndDate) && !periodCleanData.isEmpty() && !currentDate.equals(lastDate)) {
                DateTime startInterval = null;
                DateTime endInterval = null;
                Period rawPeriod = CleanDataObject.getPeriodForDate(periodRawData, currentDate);
                Period cleanPeriod = CleanDataObject.getPeriodForDate(periodCleanData, currentDate);
                Boolean isDifferential = CleanDataObject.isDifferentialForDate(differentialRules, currentDate);
                boolean greaterThenDays = false;

                startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                        currentDate.getHourOfDay(), currentDate.getMinuteOfHour(), currentDate.getSecondOfMinute());
                endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                        currentDate.getHourOfDay(), currentDate.getMinuteOfHour(), currentDate.getSecondOfMinute());

                if (cleanPeriod.getYears() > 0) {
                    startInterval = startInterval.minusYears(cleanPeriod.getYears()).withMonthOfYear(1).withDayOfMonth(1);
                    endInterval = startInterval.plusYears(cleanPeriod.getYears()).withMonthOfYear(1).withDayOfMonth(1).minusDays(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getMonths() > 0) {
                    startInterval = startInterval.minusMonths(cleanPeriod.getMonths()).withDayOfMonth(1);
                    endInterval = startInterval.plusMonths(cleanPeriod.getMonths()).withDayOfMonth(1).minusDays(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getWeeks() > 0) {
                    startInterval = startInterval.minusWeeks(cleanPeriod.getWeeks()).withDayOfWeek(1);
                    endInterval = startInterval.plusWeeks(cleanPeriod.getWeeks()).withDayOfWeek(1).minusDays(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getDays() > 0) {
                    startInterval = startInterval.minusDays(cleanPeriod.getDays());
                    greaterThenDays = true;
                }
                if (cleanPeriod.getHours() > 0) {
                    startInterval = startInterval.minusHours(cleanPeriod.getHours());
                }
                if (cleanPeriod.getMinutes() > 0) {
                    startInterval = startInterval.minusMinutes(cleanPeriod.getMinutes());
                }
                if (cleanPeriod.getSeconds() > 0) {
                    startInterval = startInterval.minusSeconds(cleanPeriod.getSeconds());
                }

                if (greaterThenDays) {
                    startInterval = new DateTime(startInterval.getYear(), startInterval.getMonthOfYear(), startInterval.getDayOfMonth(),
                            dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond());
                    endInterval = new DateTime(endInterval.getYear(), endInterval.getMonthOfYear(), endInterval.getDayOfMonth(),
                            dtEnd.getHour(), dtEnd.getMinute(), dtEnd.getSecond());

                    if (dtEnd.isBefore(dtStart)) {
                        startInterval = startInterval.minusDays(1);
                        endInterval = endInterval.minusDays(1);
                    }
                }

                Interval interval = new Interval(startInterval.plusSeconds(1), endInterval);

                CleanIntervalN currentInterval = new CleanIntervalN(interval, endInterval);
                currentInterval.getResult().setTimeStamp(endInterval);
                currentInterval.setInputPeriod(rawPeriod);
                currentInterval.setOutputPeriod(cleanPeriod);
                currentInterval.setDifferential(isDifferential);
                cleanIntervals.add(currentInterval);

                lastDate = currentDate;
                currentDate = PeriodHelper.addPeriodToDate(currentDate, cleanPeriod);
            }

            logger.info("[{}] {} intervals calculated", cleanDataObject.getCleanObject().getID(), cleanIntervals.size());
        }

        if (cleanIntervals.isEmpty()) {
            LogTaskManager.getInstance().getTask(cleanDataObject.getCleanObject().getID()).setStatus(Task.Status.IDLE);
        }

        return cleanIntervals;
    }


    private List<CleanIntervalN> getIntervalsFromRawSamples(CleanDataObject cleanDataObject, List<JEVisSample> rawSamples) throws Exception {
        List<CleanIntervalN> CleanIntervalNs = new ArrayList<>();
        for (JEVisSample curSample : rawSamples) {

            DateTime timestamp = curSample.getTimestamp().plusSeconds(cleanDataObject.getPeriodOffset());
            Period rawPeriod = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), timestamp);
            Period cleanPeriod = CleanDataObject.getPeriodForDate(cleanDataObject.getCleanDataPeriodAlignment(), timestamp);

            DateTime start = timestamp.minusMillis(1);
            DateTime end = timestamp;

            Period periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), timestamp);

            if (periodForDate.equals(Period.months(1))) {
                timestamp = timestamp.minusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                start = timestamp.plusMillis(1);
                end = timestamp.plusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }

            Interval interval = new Interval(start, end);
            CleanIntervalN cleanIntervalN = new CleanIntervalN(interval, timestamp);
            cleanIntervalN.getResult().setTimeStamp(timestamp);
            cleanIntervalN.getRawSamples().add(curSample);
            cleanIntervalN.setInputPeriod(rawPeriod);
            cleanIntervalN.setOutputPeriod(cleanPeriod);
            CleanIntervalNs.add(cleanIntervalN);
        }
        logger.info("[{}] {} intervals calculated", cleanDataObject.getCleanObject().getID(), CleanIntervalNs.size());
        return CleanIntervalNs;
    }
}
