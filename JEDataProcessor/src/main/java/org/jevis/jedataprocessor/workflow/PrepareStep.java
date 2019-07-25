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
        List<JEVisSample> rawSamplesDown = cleanDataObject.getRawSamplesDown();
        List<JEVisSample> rawSamplesUp = cleanDataObject.getRawSamplesUp();
        logger.info("[{}] raw samples found for cleaning: {}", resourceManager.getID(), rawSamplesDown.size());
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("Raw S.", rawSamplesDown.size() + "");

        if (rawSamplesDown.isEmpty() || rawSamplesUp.isEmpty()) {
            logger.info("[{}] No new raw date stopping this job", resourceManager.getID());
            return;
        }

        resourceManager.setRawSamplesDown(rawSamplesDown);
        resourceManager.setRawSamplesUp(rawSamplesUp);

        Map<DateTime, JEVisSample> notesMap = cleanDataObject.getNotesMap();
        resourceManager.setNotesMap(notesMap);


        Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
        Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();
        if (rawSamplesDown.size() > 1)
            logger.info("[{}] Input Period is {}", resourceManager.getID(), PeriodFormat.getDefault().print(new Period(rawSamplesDown.get(0).getTimestamp(), rawSamplesDown.get(1).getTimestamp())));
        logger.info("[{}] Period is {}", resourceManager.getID(), PeriodFormat.getDefault().print(periodCleanData));
        logger.info("[{}] Samples should be aligned {}", resourceManager.getID(), cleanDataObject.getIsPeriodAligned());
        if (periodCleanData.equals(Period.ZERO) && cleanDataObject.getIsPeriodAligned()) {
            throw new RuntimeException("No Input Sample Rate given for Object Clean Data and Attribute Value");
        } else if (cleanDataObject.getIsPeriodAligned()) {
            List<CleanInterval> cleanIntervals = getIntervals(cleanDataObject, periodCleanData, periodRawData);
            resourceManager.setIntervals(cleanIntervals);
        } else {
            List<CleanInterval> cleanIntervals = getIntervalsFromRawSamples(cleanDataObject, rawSamplesDown);
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

        logger.info("[{}] getIntervals: currentDate: {}  MaxEndDate: {} ", calcAttribute.getCleanObject().getID(), currentDate, maxEndDate);

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

            while (currentDate.isBefore(maxEndDate) && !periodCleanData.equals(Period.ZERO) && !currentDate.equals(lastDate)) {
                DateTime startInterval = null;
                DateTime endInterval = null;
                lastDate = currentDate;

                boolean periodRawHasMonths = periodRawData.getMonths() > 0;
                boolean periodRawHasYear = periodRawData.getYears() > 0;
                boolean periodRawHasDays = periodRawData.getDays() > 0;
                boolean periodRawHasHours = periodRawData.getHours() > 0;
                boolean periodRawHasMinutes = periodRawData.getMinutes() > 0;
                boolean periodRawHasSeconds = periodRawData.getSeconds() > 0;

                boolean periodCleanHasMonths = periodCleanData.getMonths() > 0;
                boolean periodCleanHasYear = periodCleanData.getYears() > 0;
                boolean periodCleanHasDays = periodCleanData.getDays() > 0;
                boolean periodCleanHasHours = periodCleanData.getHours() > 0;
                boolean periodCleanHasMinutes = periodCleanData.getMinutes() > 0;
                boolean periodCleanHasSeconds = periodCleanData.getSeconds() > 0;


                startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                        currentDate.getHourOfDay(), currentDate.getMinuteOfHour(), currentDate.getSecondOfMinute());
                endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth(),
                        currentDate.getHourOfDay(), currentDate.getMinuteOfHour(), currentDate.getSecondOfMinute());

                if (periodCleanHasYear) {
                    startInterval = startInterval.minusYears(periodCleanData.getYears()).withMonthOfYear(1).withDayOfMonth(1);
                    endInterval = startInterval.plusYears(periodCleanData.getYears()).withMonthOfYear(1).withDayOfMonth(1);
                }
                if (periodCleanHasMonths) {
                    startInterval = startInterval.minusMonths(periodCleanData.getMonths()).withDayOfMonth(1);
                    endInterval = startInterval.plusMonths(periodCleanData.getMonths()).withDayOfMonth(1);
                }
                if (periodCleanHasDays) {
                    startInterval = startInterval.minusDays(periodCleanData.getDays());
                }
                if (periodCleanHasHours) {
                    startInterval = startInterval.minusHours(periodCleanData.getHours());
                }
                if (periodCleanHasMinutes) {
                    startInterval = startInterval.minusMinutes(periodCleanData.getMinutes());
                }
                if (periodCleanHasSeconds) {
                    startInterval = startInterval.minusSeconds(periodCleanData.getSeconds());
                }


                if (periodCleanHasDays || periodCleanHasMonths || periodCleanHasYear) {
                    startInterval = new DateTime(startInterval.getYear(), startInterval.getMonthOfYear(), startInterval.getDayOfMonth(),
                            dtStart.getHour(), dtStart.getMinute(), dtStart.getSecond());
                    endInterval = new DateTime(endInterval.getYear(), endInterval.getMonthOfYear(), endInterval.getDayOfMonth(),
                            dtEnd.getHour(), dtEnd.getMinute(), dtEnd.getSecond());

                    if (dtEnd.isBefore(dtStart)) {
                        startInterval = startInterval.minusDays(1);
                        endInterval = endInterval.minusDays(1);
                    }
                }

                if (startInterval != null && endInterval != null) {
                    Interval interval = new Interval(startInterval, endInterval);
                    CleanInterval currentInterval = new CleanInterval(interval, startInterval);
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

    private List<CleanInterval> getIntervalsFromRawSamples(CleanDataObject
                                                                   calcAttribute, List<JEVisSample> rawSamples) throws Exception {
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
