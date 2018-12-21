/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.jedataprocessor.data.CleanDataAttribute;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates empty interval classes from start date to end date
 *
 * @author broder
 */
public class PrepareStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(PrepareStep.class);

    @Override

    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();

        //get the raw samples for the cleaning
        logger.info("[{}] Request raw samples", resourceManager.getID());
        List<JEVisSample> rawSamples = calcAttribute.getRawSamples();
        logger.info("[{}] raw samples found for cleaning: {}", resourceManager.getID(), rawSamples.size());
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("Raw S.", rawSamples.size() + "");

        if (rawSamples.isEmpty()) {
            logger.info("[{}] No new raw date stopping this job", resourceManager.getID());
            return;
        }

        resourceManager.setRawSamples(rawSamples);

        Period periodAlignment = calcAttribute.getPeriodAlignment();
        logger.info("[{}] Period is {}", resourceManager.getID(), PeriodFormat.getDefault().print(periodAlignment));
        logger.info("[{}] Samples should be aligned {}", resourceManager.getID(), calcAttribute.getIsPeriodAligned());
        if (periodAlignment.equals(Period.ZERO) && calcAttribute.getIsPeriodAligned()) {
            throw new RuntimeException("No Input Sample Rate given for Object Clean Data and Attribute Value");
        } else if (calcAttribute.getIsPeriodAligned()) {
            List<CleanInterval> cleanIntervals = getIntervals(calcAttribute, periodAlignment);
            resourceManager.setIntervals(cleanIntervals);
        } else {
            List<CleanInterval> cleanIntervals = getIntervalsFromRawSamples(calcAttribute, rawSamples);
            resourceManager.setIntervals(cleanIntervals);
        }

    }

    private List<CleanInterval> getIntervals(CleanDataAttribute calcAttribute, Period periodAlignment) {
        List<CleanInterval> cleanIntervals = new ArrayList<>();

        if (calcAttribute.getMaxEndDate() == null) {
            logger.info("[{}] No Raw data, nothing to to", calcAttribute.getObject().getID());
            return cleanIntervals;
        }

        DateTime currentDate = calcAttribute.getFirstDate();
        DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime maxEndDate = calcAttribute.getMaxEndDate();

        logger.error("[{}] getIntervals: currentDate: {}  MaxEndDate: {} ", calcAttribute.getObject().getID(), currentDate, maxEndDate);

        if (currentDate == null || maxEndDate == null || !currentDate.isBefore(maxEndDate)) {
            logger.warn("Nothing to do with only one interval");
            return cleanIntervals;
//            throw new Exception(String.format("Cant calculate the intervals with start date %s  and end date %s", datePattern.print(currentDate), datePattern.print(maxEndDate)));
//            logger.error("Cant calculate the intervals with start date " + datePattern.print(currentDate) + " and end date " + datePattern.print(maxEndDate));
        } else {
            logger.info("[{}] Calc interval between start date {} and end date {}", calcAttribute.getObject().getID(), datePattern.print(currentDate), datePattern.print(maxEndDate));


            if (periodAlignment.equals(Period.months(1))) {
                currentDate = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), 1, 0, 0, 0);

                while (currentDate.isBefore(maxEndDate) && periodAlignment.getMillis() > 0) {
                    DateTime startInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), 1, 0, 0, 0);
                    DateTime endInterval = new DateTime(currentDate.getYear(), currentDate.getMonthOfYear(), 1, 0, 0, 0).plusMonths(1);
                    Interval interval = new Interval(startInterval, endInterval);

                    CleanInterval currentInterval = new CleanInterval(interval, currentDate);
                    cleanIntervals.add(currentInterval);

                    currentDate = currentDate.plus(periodAlignment);
                }
            } else if (periodAlignment.equals(Period.years(1))) {
                currentDate = new DateTime(currentDate.getYear(), 1, 1, 0, 0, 0);

                while (currentDate.isBefore(maxEndDate) && periodAlignment.getMillis() > 0) {
                    DateTime startInterval = new DateTime(currentDate.getYear(), 1, 1, 0, 0, 0);
                    DateTime endInterval = new DateTime(currentDate.getYear(), 1, 1, 0, 0, 0).plusYears(1);
                    Interval interval = new Interval(startInterval, endInterval);

                    CleanInterval currentInterval = new CleanInterval(interval, currentDate);
                    cleanIntervals.add(currentInterval);

                    currentDate = currentDate.plus(periodAlignment);
                }
            } else {
                Duration duration = periodAlignment.toStandardDuration();
                Long halfDuration = duration.getMillis() / 2;

                while (currentDate.isBefore(maxEndDate) && periodAlignment.getMillis() > 0) {
                    DateTime startInterval = currentDate.minus(halfDuration);
                    DateTime endInterval = currentDate.plus(halfDuration);
                    Interval interval = new Interval(startInterval, endInterval);

                    CleanInterval currentInterval = new CleanInterval(interval, currentDate);
                    cleanIntervals.add(currentInterval);

                    currentDate = currentDate.plus(periodAlignment);
                }
            }


            logger.info("[{}] {} intervals calculated", calcAttribute.getObject().getID(), cleanIntervals.size());
        }

        if (cleanIntervals.isEmpty()) {
            LogTaskManager.getInstance().getTask(calcAttribute.getObject().getID()).setStatus(Task.Status.IDLE);
        }

        return cleanIntervals;
    }

    private List<CleanInterval> getIntervalsFromRawSamples(CleanDataAttribute calcAttribute, List<JEVisSample> rawSamples) throws Exception {
        List<CleanInterval> cleanIntervals = new ArrayList<>();
        for (JEVisSample curSample : rawSamples) {
            DateTime startInterval = curSample.getTimestamp().plusSeconds(calcAttribute.getPeriodOffset());
            DateTime endInterval = startInterval.plusMillis(1);
            Interval interval = new Interval(startInterval, endInterval);

            CleanInterval currentInterval = new CleanInterval(interval, startInterval);
            cleanIntervals.add(currentInterval);

        }
        logger.info("[{}] {} intervals calculated", calcAttribute.getObject().getID(), cleanIntervals.size());
        return cleanIntervals;
    }
}
