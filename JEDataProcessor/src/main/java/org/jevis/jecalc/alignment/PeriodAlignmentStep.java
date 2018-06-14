/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.alignment;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * align the raw samples and calculate the value per interval if possible
 *
 * @author broder
 */
public class PeriodAlignmentStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(PeriodAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        StopWatch stopWatch = new Slf4JStopWatch("align");
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();

        List<JEVisSample> rawSamples = resourceManager.getRawSamples();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        Integer periodOffset = resourceManager.getCalcAttribute().getPeriodOffset();

        //align the raw samples to the intervals
        int currentSamplePointer = 0;
        for (int i = 0; i < intervals.size(); i++) {
            boolean samplesInInterval = true;
            while (samplesInInterval && currentSamplePointer < rawSamples.size()) {
                CleanInterval currentInterval = intervals.get(i);
                JEVisSample rawSample = rawSamples.get(currentSamplePointer);
                try {
                    DateTime timestamp = rawSample.getTimestamp().plusSeconds(periodOffset);
                    Interval interval = currentInterval.getInterval();
                    if (interval.contains(timestamp)) { //sample is in interval
                        currentInterval.addRawSample(rawSample);
                        currentSamplePointer++;
                    } else if (timestamp.isBefore(interval.getStart())) { //sample is before interval start --just find the start
                        currentSamplePointer++;
                    } else {
                        samplesInInterval = false;
                    }
                } catch (JEVisException ex) {
                    logger.error("error while align the raw samples to the interval, no timestamp found", ex);
                }
            }
        }

        Boolean conversionDifferential = calcAttribute.getConversionDifferential();
        Boolean valueIsQuantity = calcAttribute.getValueIsQuantity();

        //calc modes
        Boolean last = valueIsQuantity && conversionDifferential;
        Boolean sum = valueIsQuantity && !conversionDifferential;
        Boolean avg = !valueIsQuantity;

        logger.info("align {},last {}, sum {}, avg {}", calcAttribute.getIsPeriodAligned(), last, sum, avg);
        //calc the sample per interval if possible depending on alignment and aggregation mode (avg oder only first value)
        for (CleanInterval currentInterval : intervals) {
            List<JEVisSample> currentRawSamples = currentInterval.getRawSamples();
            if (currentRawSamples.isEmpty()) {
                continue;
            }

            try {
                if (!calcAttribute.getIsPeriodAligned()) { //no alignment
                    for (JEVisSample sample : currentRawSamples) {
                        sample.setNote("alignment(no)");
                        currentInterval.addTmpSample(sample);
                    }
                } else if (last) { //last sample
                    DateTime date = currentInterval.getDate();
                    Double valueAsDouble = currentRawSamples.get(currentRawSamples.size() - 1).getValueAsDouble();
                    JEVisSample sample = new VirtualSample(date, valueAsDouble);
                    sample.setNote("alignment(yes," + currentRawSamples.size() + ",last)");
                    currentInterval.addTmpSample(sample);

                } else if (avg) {
                    Double currentValue = calcAvgSample(currentRawSamples);
                    DateTime date = currentInterval.getDate();
                    JEVisSample sample = new VirtualSample(date, currentValue);
                    sample.setNote("alignment(yes," + currentRawSamples.size() + ",avg)");
                    currentInterval.addTmpSample(sample);
                } else if (sum) {
                    Double currentValue = calcSumSample(currentRawSamples);
                    DateTime date = currentInterval.getDate();
                    JEVisSample sample = new VirtualSample(date, currentValue);
                    sample.setNote("alignment(yes," + currentRawSamples.size() + ",sum)");
                    currentInterval.addTmpSample(sample);
                }
            } catch (JEVisException ex) {
                logger.error(null, ex);
            }
        }
        stopWatch.stop();
    }

    private Double calcAvgSample(List<JEVisSample> currentRawSamples) {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            try {
                Double valueAsDouble = sample.getValueAsDouble();
                value += valueAsDouble;
            } catch (JEVisException ex) {
                logger.error(null, ex);
            }
        }
        Double avgValue = value / currentRawSamples.size();
        return avgValue;
    }

    private Double calcSumSample(List<JEVisSample> currentRawSamples) {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            try {
                Double valueAsDouble = sample.getValueAsDouble();
                value += valueAsDouble;
            } catch (JEVisException ex) {
                logger.error(null, ex);
            }
        }
        return value;
    }

}
