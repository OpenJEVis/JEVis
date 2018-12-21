/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.alignment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.jedataprocessor.data.CleanDataAttribute;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;

/**
 * align the raw samples and calculate the value per interval if possible
 *
 * @author broder
 */
public class PeriodAlignmentStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(PeriodAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();

        List<JEVisSample> rawSamples = resourceManager.getRawSamples();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        Integer periodOffset = resourceManager.getCalcAttribute().getPeriodOffset();

        //align the raw samples to the intervals
        int currentSamplePointer = 0;
        for (CleanInterval interval1 : intervals) {
            boolean samplesInInterval = true;
            while (samplesInInterval && currentSamplePointer < rawSamples.size()) {
                JEVisSample rawSample = rawSamples.get(currentSamplePointer);
                try {
                    DateTime timestamp = rawSample.getTimestamp().plusSeconds(periodOffset);
                    Interval interval = interval1.getInterval();
                    if (interval.contains(timestamp)) { //sample is in interval
                        interval1.addRawSample(rawSample);
                        currentSamplePointer++;
                    } else if (timestamp.isBefore(interval.getStart())) { //sample is before interval start --just find the start
                        currentSamplePointer++;
                    } else {
                        samplesInInterval = false;
                    }
                } catch (Exception ex) {
                    throw new Exception("error while align the raw samples to the interval, no timestamp found", ex);
                }
            }
        }

        List<JEVisSample> listConversionToDifferential = calcAttribute.getConversionDifferential();
        Boolean valueIsQuantity = calcAttribute.getValueIsQuantity();


        /**
         * calc the sample per interval if possible depending on alignment and aggregation mode (avg oder only first value)
         */
        for (CleanInterval currentInterval : intervals) {

            for (int i = 0; i < listConversionToDifferential.size(); i++) {
                JEVisSample ctd = listConversionToDifferential.get(i);
                DateTime nextTimeStampOfConversion = null;
                if (listConversionToDifferential.size() > (i + 1)) {
                    nextTimeStampOfConversion = (listConversionToDifferential.get(i + 1)).getTimestamp();
                }

                DateTime timeStampOfConversion = ctd.getTimestamp();
                Boolean conversionDifferential = ctd.getValueAsBoolean();


                boolean last = valueIsQuantity && conversionDifferential;
                boolean sum = valueIsQuantity && !conversionDifferential;
                boolean avg = !valueIsQuantity;

                if (currentInterval.getDate().isAfter(timeStampOfConversion) &&
                        ((nextTimeStampOfConversion == null) || currentInterval.getDate().isBefore(nextTimeStampOfConversion))) {

                    //logger.info("align {},last {}, sum {}, avg {}", calcAttribute.getIsPeriodAligned(), last, sum, avg);
                    List<JEVisSample> currentRawSamples = currentInterval.getRawSamples();
                    if (currentRawSamples.isEmpty()) {
                        continue;
                    }

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
                    } else {
                        Double currentValue = calcSumSample(currentRawSamples);
                        DateTime date = currentInterval.getDate();
                        JEVisSample sample = new VirtualSample(date, currentValue);
                        sample.setNote("alignment(yes," + currentRawSamples.size() + ",sum)");
                        currentInterval.addTmpSample(sample);
                    }
                }


            }
        }

/**
 * Debug helper
 */
//        System.out.println("===== after period aliment");
//        for (CleanInterval interval : intervals) {
//            System.out.println("Interval: " + interval.getDate() + " " + interval.getInterval());
//            for (JEVisSample s : interval.getTmpSamples()) {
//                System.out.println("raw: " + s);
//            }
//            for (JEVisSample s : interval.getTmpSamples()) {
//                System.out.println("tmp: " + s);
//            }
//        }

    }

    private Double calcAvgSample(List<JEVisSample> currentRawSamples) throws Exception {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            Double valueAsDouble = sample.getValueAsDouble();
            value += valueAsDouble;
        }
        return (value / currentRawSamples.size());
    }

    private Double calcSumSample(List<JEVisSample> currentRawSamples) throws Exception {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            Double valueAsDouble = sample.getValueAsDouble();
            value += valueAsDouble;
        }
        return value;
    }

}
