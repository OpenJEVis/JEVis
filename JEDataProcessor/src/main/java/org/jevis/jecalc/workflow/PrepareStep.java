/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.workflow;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Creates empty interval classes from start date to end date
 *
 * @author broder
 */
public class PrepareStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(PrepareStep.class);

    @Override

    public void run(ResourceManager resourceManager) {
        StopWatch stopWatch = new Slf4JStopWatch("prepare");
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();

        //get the raw samples for the cleaning
        List<JEVisSample> rawSamples = calcAttribute.getRawSamples();
        logger.info("{} raw samples found for cleaning", rawSamples.size());
        resourceManager.setRawSamples(rawSamples);

        Period periodAlignment = calcAttribute.getPeriodAlignment();
        logger.info("Period is {}", PeriodFormat.getDefault().print(periodAlignment));
        logger.info("Samples should be aligned {}", calcAttribute.getIsPeriodAligned());
        if (periodAlignment.toStandardDuration().getMillis() == 0 && calcAttribute.getIsPeriodAligned()) {
            throw new RuntimeException("No Input Sample Rate given for Object Clean Data and Attribute Value");
        } else if (calcAttribute.getIsPeriodAligned()) {
            List<CleanInterval> cleanIntervals = getIntervals(calcAttribute, periodAlignment);
            resourceManager.setIntervals(cleanIntervals);
        } else {
            List<CleanInterval> cleanIntervals = getIntervalsFromRawSamples(calcAttribute, rawSamples);
            resourceManager.setIntervals(cleanIntervals);
        }

        stopWatch.stop();
    }

    private List<CleanInterval> getIntervals(CleanDataAttribute calcAttribute, Period periodAlignment) {
        Duration duration = periodAlignment.toStandardDuration();
        //the interval with date x begins at x - (duration/2) and ends at x + (duration/2)
        //Todo Month has no well defined duration -> cant handle months atm
        Long halfDuration = duration.getMillis() / 2;

        List<CleanInterval> cleanIntervals = new ArrayList<>();
        DateTime currentDate = calcAttribute.getFirstDate();
        DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime maxEndDate = calcAttribute.getMaxEndDate();
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

    private List<CleanInterval> getIntervalsFromRawSamples(CleanDataAttribute calcAttribute, List<JEVisSample> rawSamples) {
        List<CleanInterval> cleanIntervals = new ArrayList<>();
        for (JEVisSample curSample : rawSamples) {
            try {
                DateTime startInterval = curSample.getTimestamp().plusSeconds(calcAttribute.getPeriodOffset());
                DateTime endInterval = startInterval.plusMillis(1);
                Interval interval = new Interval(startInterval, endInterval);

                CleanInterval currentInterval = new CleanInterval(interval, startInterval);
                cleanIntervals.add(currentInterval);
            } catch (JEVisException ex) {
                java.util.logging.Logger.getLogger(PrepareStep.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        logger.info("{} intervals calculated", cleanIntervals.size());
        return cleanIntervals;
    }
}
