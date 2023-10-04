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
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.task.LogTaskManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Creates empty interval classes from start date to end date
 *
 * @author gschutz
 */
public class PrepareStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(PrepareStep.class);
    private final ProcessManager processManager;

    public PrepareStep(ProcessManager processManager) {
        this.processManager = processManager;
    }

    @Override

    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        //get the raw samples for the cleaning
        logger.info("[{}] Request raw samples", resourceManager.getID());
        List<JEVisSample> rawSamplesDown = cleanDataObject.getRawSamplesDown();
//        List<JEVisSample> rawSamplesUp = cleanDataObject.getRawSamplesUp();
        logger.info("[{}] raw samples found for cleaning: {}", resourceManager.getID(), rawSamplesDown.size());
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("Raw S.", String.valueOf(rawSamplesDown.size()));

//        if (rawSamplesDown.isEmpty() || rawSamplesUp.isEmpty()) {
        if (rawSamplesDown.isEmpty()) {
            throw new RuntimeException(String.format("[%s] No new raw data. Stopping this job", resourceManager.getID()));
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
            List<CleanInterval> cleanIntervals = getIntervals(cleanDataObject, periodCleanData);
            resourceManager.setIntervals(cleanIntervals);
        } else {
            List<CleanInterval> cleanIntervals = getIntervalsFromRawSamples(cleanDataObject, rawSamplesDown);
            resourceManager.setIntervals(cleanIntervals);
        }

        if (resourceManager.getIntervals().isEmpty()) {
            throw new RuntimeException(String.format("[%s] No new intervals. Stopping this job", resourceManager.getID()));
        }
    }

    private List<CleanInterval> getIntervals(CleanDataObject cleanDataObject, List<PeriodRule> periodCleanData) throws JEVisException {
        List<CleanInterval> cleanIntervals = new ArrayList<>();

        if (cleanDataObject.getMaxEndDate() == null) {
            logger.info("[{}] No Raw data, nothing to to", cleanDataObject.getCleanObject().getID());
            return cleanIntervals;
        }

        List<PeriodRule> periodRawData = cleanDataObject.getRawDataPeriodAlignment();
        List<DifferentialRule> differentialRules = cleanDataObject.getDifferentialRules();
        DateTime currentDate = cleanDataObject.getFirstDate();
        DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        WorkDays wd = new WorkDays(cleanDataObject.getCleanObject());
        DateTimeZone timeZone = wd.getDateTimeZone();
        LocalTime dtStart = wd.getWorkdayStart();
        LocalTime dtEnd = wd.getWorkdayEnd();
        DateTime maxEndDate = cleanDataObject.getMaxEndDate();
        Boolean firstIsDifferential = CleanDataObject.isDifferentialForDate(differentialRules, currentDate);

        int indexLastRawSample = cleanDataObject.getRawSamplesDown().size() - 1;
        Period lastPeriod = CleanDataObject.getPeriodForDate(cleanDataObject.getCleanDataPeriodAlignment(), cleanDataObject.getRawSamplesDown().get(indexLastRawSample).getTimestamp());
        if (dtEnd.isBefore(dtStart) && IntStream.of(lastPeriod.getYears(), lastPeriod.getMonths(), lastPeriod.getWeeks()).anyMatch(i -> i > 0)) {
            currentDate = currentDate.minus(lastPeriod);
            maxEndDate = maxEndDate.minus(lastPeriod);
        } else if (IntStream.of(lastPeriod.getYears(), lastPeriod.getMonths()).anyMatch(i -> i > 0) && firstIsDifferential) {
            maxEndDate = maxEndDate.minus(lastPeriod);
        }

        logger.info("[{}] getIntervals: currentDate: {}  MaxEndDate: {} ", cleanDataObject.getCleanObject().getID(), currentDate, maxEndDate);

        if (currentDate == null || maxEndDate == null || !currentDate.isBefore(maxEndDate)) {
            logger.warn("Nothing to do with only one interval");
            return cleanIntervals;
        } else {
            logger.info("[{}] Calc interval between start date {} and end date {}", cleanDataObject.getCleanObject().getID(), datePattern.print(currentDate), datePattern.print(maxEndDate));

            PeriodComparator periodComparator = new PeriodComparator();
            DateTime lastDate = null;

            Period firstRawPeriod = CleanDataObject.getPeriodForDate(periodRawData, currentDate);
            Period firstCleanPeriod = CleanDataObject.getPeriodForDate(periodCleanData, currentDate);
            int compare = periodComparator.compare(firstCleanPeriod, firstRawPeriod);
            int maxProcessingSize = cleanDataObject.getMaxProcessingSize();
            boolean isFinished = true;

            //add half a period to maxEndDate

            DateTime currentDateLocal = currentDate.withZone(timeZone);
            DateTime maxEndDateLocal = maxEndDate.withZone(timeZone);
            Long offsetMillis = null;

            if (firstCleanPeriod.getYears() > 0) {
                currentDateLocal = currentDateLocal.minusYears(1).withMonthOfYear(1).withDayOfMonth(1);

                offsetMillis = maxEndDateLocal.getMillis() - maxEndDateLocal.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
                maxEndDateLocal = maxEndDateLocal.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (dtEnd.isBefore(dtStart)) {
                    maxEndDateLocal = maxEndDateLocal.plusYears(1);
                }
            }
            if (compare < 0 && firstRawPeriod.getYears() > 0) {
                currentDateLocal = currentDateLocal.minusYears(1).withMonthOfYear(1).withDayOfMonth(1);
            }

            if (firstCleanPeriod.getMonths() > 0) {
                currentDateLocal = currentDateLocal.minusMonths(1).withDayOfMonth(1);

                offsetMillis = maxEndDateLocal.getMillis() - maxEndDateLocal.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
                maxEndDateLocal = maxEndDateLocal.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (dtEnd.isBefore(dtStart)) {
                    maxEndDateLocal = maxEndDateLocal.plusMonths(1);
                }
            }
            if (compare < 0 && firstRawPeriod.getMonths() > 0) {
                currentDateLocal = currentDateLocal.minusMonths(1).withDayOfMonth(1);
            }

            if (compare < 0 && CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataObject(), maxEndDateLocal).equals(Period.months(1))) {
                maxEndDateLocal = maxEndDateLocal.withDayOfMonth(maxEndDateLocal.dayOfMonth().getMaximumValue()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
            }

            if (firstCleanPeriod.getWeeks() > 0) {
                currentDateLocal = currentDateLocal.minusWeeks(1).withDayOfWeek(1);

                offsetMillis = maxEndDateLocal.getMillis() - maxEndDateLocal.plusWeeks(1).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
                maxEndDateLocal = maxEndDateLocal.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
            if (compare < 0 && firstRawPeriod.getWeeks() > 0) {
                currentDateLocal = currentDateLocal.minusWeeks(1).withDayOfWeek(1);
            }

            if (firstCleanPeriod.getDays() > 0) {
                currentDateLocal = currentDateLocal.minusDays(1);

                offsetMillis = maxEndDateLocal.getMillis() - maxEndDateLocal.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
                maxEndDateLocal = maxEndDateLocal.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
            if (compare < 0 && firstRawPeriod.getDays() > 0) {
                currentDateLocal = currentDateLocal.minusDays(1);
            }

            if (firstCleanPeriod.getHours() > 0) {
                currentDateLocal = currentDateLocal.minusHours(1).withMinuteOfHour(0);
                //like days?
                offsetMillis = maxEndDateLocal.getMillis() - maxEndDateLocal.plusMinutes(30).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).getMillis();
                maxEndDateLocal = maxEndDateLocal.plusMinutes(30).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
            if (compare < 0 && firstRawPeriod.getHours() > 0) {
                currentDateLocal = currentDateLocal.minusHours(1).withMinuteOfHour(0);
            }

            if (firstCleanPeriod.getMinutes() > 0 && firstRawPeriod.getHours() == 1 && firstRawPeriod.getMinutes() == 0) {
                currentDateLocal = currentDateLocal.plus(firstCleanPeriod);
            }

            if (firstCleanPeriod.getMinutes() > 0) {
                currentDateLocal = currentDateLocal.minusMinutes(firstCleanPeriod.getMinutes());
            }

            if (firstCleanPeriod.getSeconds() > 0) {
                currentDateLocal = currentDateLocal.minusSeconds(firstCleanPeriod.getSeconds());
            }

            currentDate = currentDateLocal.withZone(DateTimeZone.UTC);
            maxEndDate = maxEndDateLocal.withZone(DateTimeZone.UTC);
            Period lastCleanPeriod = firstCleanPeriod;

            while (currentDate.isBefore(maxEndDate) && !periodCleanData.isEmpty() && !currentDate.equals(lastDate)) {
                DateTime startDateTime = null;
                DateTime endDateTime = null;

                Period rawPeriod = CleanDataObject.getPeriodForDate(periodRawData, currentDate);
                Period cleanPeriod = CleanDataObject.getPeriodForDate(periodCleanData, currentDate);

                if (!cleanPeriod.equals(lastCleanPeriod)) {
                    currentDate = CleanDataObject.getDateForPeriodForDate(periodCleanData, currentDate);
                    currentDateLocal = currentDate.withZone(timeZone);
                    rawPeriod = CleanDataObject.getPeriodForDate(periodRawData, currentDate);
                }

                lastCleanPeriod = cleanPeriod;

                Boolean isDifferential = CleanDataObject.isDifferentialForDate(differentialRules, currentDate);
                boolean greaterThenDays = false;

                DateTime intervalDate = currentDate.withZone(timeZone);
                startDateTime = currentDate.withZone(timeZone);
                endDateTime = currentDate.withZone(timeZone);

                if (cleanPeriod.getYears() > 0) {
                    if (isDifferential) {
                        intervalDate = startDateTime.withMonthOfYear(1).withDayOfMonth(1);
                        startDateTime = intervalDate;
                    } else {
                        startDateTime = startDateTime.withMonthOfYear(1).withDayOfMonth(1);
                    }
                    endDateTime = startDateTime.plusYears(cleanPeriod.getYears()).withMonthOfYear(1).withDayOfMonth(1).minusDays(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getMonths() > 0) {
                    if (isDifferential) {
                        intervalDate = startDateTime.withDayOfMonth(1);
                        startDateTime = intervalDate;
                    } else {
                        startDateTime = startDateTime.withDayOfMonth(1);
                    }
                    endDateTime = startDateTime.plusMonths(cleanPeriod.getMonths()).withDayOfMonth(1).minusDays(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getWeeks() > 0) {
                    if (isDifferential) {
                        intervalDate = startDateTime.withDayOfWeek(1);
                        startDateTime = intervalDate;
                    } else {
                        startDateTime = startDateTime.withDayOfWeek(1);
                    }
                    endDateTime = startDateTime.plusWeeks(cleanPeriod.getWeeks()).withDayOfWeek(1).minusDays(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getDays() > 0) {
                    if (isDifferential) {
                        intervalDate = startDateTime.withTime(0, 0, 0, 0);
                        startDateTime = intervalDate;
                    } else {
                        startDateTime = startDateTime.withTime(0, 0, 0, 0);
                    }
                    endDateTime = startDateTime.plusDays(cleanPeriod.getDays()).minusSeconds(1);
                    greaterThenDays = true;
                }
                if (cleanPeriod.getHours() > 0) {
                    startDateTime = startDateTime.minusHours(cleanPeriod.getHours());
                }
                if (cleanPeriod.getMinutes() > 0) {
                    startDateTime = startDateTime.minusMinutes(cleanPeriod.getMinutes());
                }
                if (cleanPeriod.getSeconds() > 0) {
                    startDateTime = startDateTime.minusSeconds(cleanPeriod.getSeconds());
                }

                if (greaterThenDays) {
                    intervalDate = new DateTime(intervalDate.getYear(), intervalDate.getMonthOfYear(), intervalDate.getDayOfMonth(),
                            dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond(), timeZone);
                    startDateTime = new DateTime(startDateTime.getYear(), startDateTime.getMonthOfYear(), startDateTime.getDayOfMonth(),
                            dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond(), timeZone);
                    endDateTime = new DateTime(endDateTime.getYear(), endDateTime.getMonthOfYear(), endDateTime.getDayOfMonth(),
                            dtEnd.getHour(), dtEnd.getMinute(), dtEnd.getSecond(), timeZone);

                    if (dtEnd.isBefore(dtStart)) {
                        intervalDate = intervalDate.minusDays(1);
                        startDateTime = startDateTime.minusDays(1);
                    } else if (dtEnd.equals(dtStart)) {
                        endDateTime = endDateTime.plusDays(1);
                    }
                }

                intervalDate = intervalDate.withZone(DateTimeZone.UTC);
                startDateTime = startDateTime.withZone(DateTimeZone.UTC);
                endDateTime = endDateTime.withZone(DateTimeZone.UTC);
                CleanInterval currentInterval;

                if (!greaterThenDays) {
                    Interval interval = null;
                    if (rawPeriod.equals(Period.minutes(15)) && cleanPeriod.equals(Period.minutes(5))) {
                        interval = new Interval(startDateTime.minusMinutes(5).plusSeconds(1), endDateTime.plusMinutes(5));
                    } else {
                        interval = new Interval(startDateTime.plusSeconds(1), endDateTime);
                    }

                    currentInterval = new CleanInterval(interval, endDateTime);
                    currentInterval.getResult().setTimeStamp(endDateTime);
                } else if (!isDifferential &&
                        (rawPeriod.getMonths() == 1 && cleanPeriod.getMonths() == 1)
                        || (rawPeriod.getYears() == 1 && cleanPeriod.getYears() == 1)) {
                    Interval interval = new Interval(startDateTime, endDateTime);
                    currentInterval = new CleanInterval(interval, startDateTime);
                    currentInterval.getResult().setTimeStamp(startDateTime);
                } else if (!isDifferential && cleanPeriod.getDays() == 1) {
                    Interval interval = new Interval(startDateTime.plusSeconds(1), endDateTime.minusSeconds(1));
                    currentInterval = new CleanInterval(interval, startDateTime);
                    currentInterval.getResult().setTimeStamp(startDateTime);
                } else if (!isDifferential) {
                    Interval interval = new Interval(startDateTime.plusSeconds(1), endDateTime.plusSeconds(1));
                    currentInterval = new CleanInterval(interval, startDateTime);
                    currentInterval.getResult().setTimeStamp(startDateTime);
                } else {
                    Interval interval = new Interval(startDateTime.plusSeconds(1), endDateTime.plusSeconds(1));
                    currentInterval = new CleanInterval(interval, intervalDate);
                    currentInterval.getResult().setTimeStamp(intervalDate);
                }

                currentInterval.setInputPeriod(rawPeriod);
                currentInterval.setOutputPeriod(cleanPeriod);
                currentInterval.setDifferential(isDifferential);
                currentInterval.setCompare(periodComparator.compare(cleanPeriod, rawPeriod));
                cleanIntervals.add(currentInterval);

                lastDate = currentDate;
                currentDateLocal = PeriodHelper.addPeriodToDate(currentDateLocal, cleanPeriod);
                currentDate = currentDateLocal.withZone(DateTimeZone.UTC);

                Period nextCleanPeriod = CleanDataObject.getPeriodForDate(periodCleanData, currentDate);
                if (!nextCleanPeriod.equals(cleanPeriod) && offsetMillis != null) {
                    maxEndDate = maxEndDate.plusMillis(offsetMillis.intValue());
                }

                if (cleanIntervals.size() >= maxProcessingSize) {
                    isFinished = false;
                    break;
                }
            }

            DateTime startDate = cleanIntervals.get(0).getDate();
            DateTime endDate = cleanIntervals.get(cleanIntervals.size() - 1).getDate();

            logger.info("[{}] {} intervals calculated between {} and {}",
                    cleanDataObject.getCleanObject().getID(), cleanIntervals.size(),
                    startDate, endDate);

            processManager.setFinished(isFinished);
        }

        return cleanIntervals;
    }

    private List<CleanInterval> getIntervalsFromRawSamples(CleanDataObject cleanDataObject, List<JEVisSample> rawSamples) throws Exception {
        List<CleanInterval> cleanIntervals = new ArrayList<>();

        for (JEVisSample curSample : rawSamples) {

            DateTime timestamp = curSample.getTimestamp().plusSeconds(cleanDataObject.getPeriodOffset());
            Period rawPeriod = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), timestamp);
            Period cleanPeriod = CleanDataObject.getPeriodForDate(cleanDataObject.getCleanDataPeriodAlignment(), timestamp);

            DateTime start = timestamp.minusMillis(1);
            DateTime end = timestamp;

            Period periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), timestamp);

            if (cleanDataObject.getIsPeriodAligned() && periodForDate.equals(Period.months(1))) {
                timestamp = timestamp.minusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                start = timestamp.plusMillis(1);
                end = timestamp.plusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }

            Boolean isDifferential = CleanDataObject.isDifferentialForDate(cleanDataObject.getDifferentialRules(), timestamp);

            Interval interval = new Interval(start, end);
            CleanInterval cleanInterval = new CleanInterval(interval, timestamp);
            cleanInterval.getResult().setTimeStamp(timestamp);
            cleanInterval.setInputPeriod(rawPeriod);
            cleanInterval.setOutputPeriod(cleanPeriod);
            cleanInterval.setDifferential(isDifferential);
            cleanIntervals.add(cleanInterval);
        }

        logger.info("[{}] {} intervals calculated", cleanDataObject.getCleanObject().getID(), cleanIntervals.size());
        processManager.setFinished(true);

        return cleanIntervals;
    }
}
