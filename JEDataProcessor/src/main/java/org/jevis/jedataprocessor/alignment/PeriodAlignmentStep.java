/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.alignment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.*;

/**
 * align the raw samples and calculate the value per interval if possible
 *
 * @author broder
 */
public class PeriodAlignmentStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(PeriodAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        Map<DateTime, JEVisSample> notesMap = resourceManager.getNotesMap();
        List<JEVisSample> rawSamples = new ArrayList<>();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        Integer periodOffset = resourceManager.getCleanDataObject().getPeriodOffset();

        //align the raw samples to the intervals
        Map<DateTime, Long> counter = new HashMap<>();
        Map<DateTime, JEVisSample> rawSamplesMap = new HashMap<>();
        Map<DateTime, Double> rawSamplesSteps = new HashMap<>();
        Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
        Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();

        long millisClean = periodCleanData.toStandardDuration().getMillis();
        long millisRaw = periodRawData.toStandardDuration().getMillis();
        long stepsInPeriod = millisRaw / millisClean;
        boolean downSampling = true;

        PeriodComparator periodComparator = new PeriodComparator();
        int compare = periodComparator.compare(periodCleanData, periodRawData);
        // if clean data period is longer (e.g. 1 day) or equal than raw data period (e.g. 15 minutes)
        // the down sampling method will be used, else the other

        if (compare < 0) {
            downSampling = false;
        }

        if (downSampling) {
            rawSamples = resourceManager.getRawSamplesDown();
            int currentSamplePointer = 0;
            for (CleanInterval cleanInterval : intervals) {
                boolean samplesInInterval = true;
                DateTime snapToGridStart = null;
                DateTime snapToGridEnd = null;
                DateTime date = cleanInterval.getDate();
                long start = cleanInterval.getInterval().getStartMillis();
                long end = cleanInterval.getInterval().getEndMillis();
                long halfDiff = (end - start) / 2;
                snapToGridStart = date.minus(halfDiff).minusMillis(1);
                snapToGridEnd = date.plus(halfDiff);

                while (samplesInInterval && currentSamplePointer < rawSamples.size()) {
                    JEVisSample rawSample = rawSamples.get(currentSamplePointer);
                    try {
                        DateTime timestamp = rawSample.getTimestamp().plusSeconds(periodOffset);
                        if (timestamp.equals(snapToGridStart)
                                || (timestamp.isAfter(snapToGridStart) && timestamp.isBefore(snapToGridEnd))
                                || timestamp.equals(snapToGridEnd)) { //sample is in interval
                            cleanInterval.addRawSample(rawSample);
                            currentSamplePointer++;
                        } else if (timestamp.isBefore(snapToGridStart)) { //sample is before interval start --just find the start
                            currentSamplePointer++;
                        } else {
                            samplesInInterval = false;
                        }
                    } catch (Exception ex) {
                        throw new Exception("error while align the raw samples to the interval, no timestamp found", ex);
                    }
                }
            }
        } else {
            rawSamples = resourceManager.getRawSamplesUp();

            for (JEVisSample rawSample : rawSamples) {
                try {
                    DateTime currentTS = rawSample.getTimestamp();
                    double currentValue = rawSample.getValueAsDouble();

                    JEVisSample nextSample = null;
                    if (rawSamples.indexOf(rawSample) + 1 < rawSamples.size()) {
                        nextSample = rawSamples.get(rawSamples.indexOf(rawSample) + 1);
                    }

                    double nextSampleValue = 0.0;
                    if (nextSample != null) {
                        nextSampleValue = nextSample.getValueAsDouble();

                    }
                    Interval rawInterval = new Interval(rawSample.getTimestamp(), Objects.requireNonNull(nextSample).getTimestamp());

                    long c = 0;
                    List<DateTime> lastAddedDT = new ArrayList<>();
                    for (CleanInterval ci : intervals) {
                        if (ci.getDate().isBefore(rawInterval.getStart())) {
                        } else if (ci.getDate().isBefore(rawInterval.getEnd())) {
                            ci.addRawSample(rawSample);
                            rawSamplesMap.put(ci.getDate(), rawSample);
                            lastAddedDT.add(ci.getDate());
                            c++;
                        } else {
                            if (ci.getDate().isAfter(rawInterval.getEnd()))
                                break;
                        }
                    }

                    if (c > 0) {
                        int startInt = (int) stepsInPeriod - (int) c;
                        double step = (nextSampleValue - currentValue) / stepsInPeriod;
                        for (int i = 0; i < lastAddedDT.size(); i++) {
                            DateTime curDate = lastAddedDT.get(i);
                            rawSamplesSteps.put(curDate, currentValue + (step * (startInt + i)));
                        }

                    }

                    if (!counter.containsKey(currentTS)) {
                        counter.put(currentTS, c);
                    } else {
                        Long val = counter.get(currentTS);
                        counter.remove(currentTS);
                        counter.put(currentTS, c + val);
                    }
                } catch (Exception e) {
                    logger.error("Error while processing raw samples.");
                }

            }
        }

        List<JEVisSample> listConversionToDifferential = cleanDataObject.getConversionDifferential();
        Boolean valueIsQuantity = cleanDataObject.getValueIsQuantity();

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

                if (currentInterval.getDate().equals(timeStampOfConversion) || (currentInterval.getDate().isAfter(timeStampOfConversion) &&
                        ((nextTimeStampOfConversion == null) || currentInterval.getDate().isBefore(nextTimeStampOfConversion)))) {

                    //logger.info("align {},last {}, sum {}, avg {}", cleanDataObject.getIsPeriodAligned(), last, sum, avg);
                    List<JEVisSample> currentRawSamples = currentInterval.getRawSamples();
                    if (currentRawSamples.isEmpty() && rawSamplesMap.isEmpty()) {
                        continue;
                    }

                    if (!cleanDataObject.getIsPeriodAligned()) { //no alignment
                        Double valueAsDouble = currentRawSamples.get(currentRawSamples.size() - 1).getValueAsDouble();
                        DateTime date = currentRawSamples.get(currentRawSamples.size() - 1).getTimestamp();
                        String note = "";
                        try {
                            note = currentRawSamples.get(currentRawSamples.size() - 1).getNote();
                        } catch (Exception e) {
                        }
                        if (note.equals("")) note += "alignment(no)";
                        else note += ",alignment(no)";
                        if (!notesMap.isEmpty()) {
                            JEVisSample noteSample = notesMap.get(date);
                            if (noteSample != null) note += ",userNotes";
                        }
                        JEVisSample sample = new VirtualSample(date, valueAsDouble);
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    } else if (conversionDifferential && !valueIsQuantity) { //last sample
                        DateTime date = currentInterval.getDate();
                        Double valueAsDouble;
                        if (downSampling) {
                            valueAsDouble = currentRawSamples.get(currentRawSamples.size() - 1).getValueAsDouble();
                        } else {
                            valueAsDouble = calcLastSampleUpscale(date, rawSamplesSteps);
                        }
                        JEVisSample sample = new VirtualSample(date, valueAsDouble);
                        String note = "";
                        try {
                            note = currentRawSamples.get(currentRawSamples.size() - 1).getNote();
                        } catch (Exception e) {
                        }
                        if (note.equals("")) note += "alignment(yes," + currentRawSamples.size() + ",last)";
                        else note += ",alignment(yes," + currentRawSamples.size() + ",last)";
                        if (!notesMap.isEmpty()) {
                            JEVisSample noteSample = notesMap.get(date);
                            if (noteSample != null) note += ",userNotes";
                        }
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);

                    } else if (!valueIsQuantity) {
                        Double currentValue;
                        if (downSampling) currentValue = calcAvgSample(currentRawSamples);
                        else {
                            DateTime date = currentInterval.getDate();
                            currentValue = calcAvgSampleUpscale(date, rawSamplesMap);
                        }
                        if (currentValue != null) {
                            DateTime date = currentInterval.getDate();
                            JEVisSample sample = new VirtualSample(date, currentValue);
                            String note = "";
                            try {
                                note = currentRawSamples.get(currentRawSamples.size() - 1).getNote();
                            } catch (Exception e) {
                            }
                            if (note.equals("")) note += "alignment(yes," + currentRawSamples.size() + ",avg)";
                            else note += ",alignment(yes," + currentRawSamples.size() + ",avg)";
                            if (!notesMap.isEmpty()) {
                                JEVisSample noteSample = notesMap.get(date);
                                if (noteSample != null) note += ",userNotes";
                            }
                            sample.setNote(note);
                            currentInterval.addTmpSample(sample);
                        }
                    } else {
                        Double currentValue;
                        if (downSampling && !conversionDifferential)
                            currentValue = calcSumSampleDownscaleNotDifferential(currentRawSamples);
                        else if (downSampling) {
                            currentValue = calcSumSampleDownscaleDifferential(currentRawSamples);
                        } else if (!conversionDifferential) {
                            DateTime date = currentInterval.getDate();
                            currentValue = calcSumUpscaleNotDifferential(date, rawSamplesMap, stepsInPeriod);
                        } else {
                            DateTime date = currentInterval.getDate();
                            currentValue = calcSumUpscaleDifferential(date, rawSamplesSteps);
                        }
                        DateTime date = currentInterval.getDate();
                        JEVisSample sample = new VirtualSample(date, currentValue);
                        String note = "";
                        try {
                            note = currentRawSamples.get(currentRawSamples.size() - 1).getNote();
                        } catch (Exception e) {
                        }
                        if (note.equals("")) note += "alignment(yes," + currentRawSamples.size() + ",sum)";
                        else note += ",alignment(yes," + currentRawSamples.size() + ",sum)";
                        if (!notesMap.isEmpty()) {
                            JEVisSample noteSample = notesMap.get(date);
                            if (noteSample != null) note += ",userNotes";
                        }
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }


            }
        }

    }

    private Double calcSumUpscaleDifferential(DateTime date, Map<DateTime, Double> rawSampleSteps) throws JEVisException {
        double value = 0.0;
        Double tempValue = rawSampleSteps.get(date);
        if (tempValue != null) {
            value = tempValue;
        }
        return value;
    }

    private Double calcSumUpscaleNotDifferential(DateTime date, Map<DateTime, JEVisSample> rawSamplesMap, long stepsInPeriod) throws JEVisException {
        double value = 0.0;
        JEVisSample sample = rawSamplesMap.get(date);
        if (sample != null) {
            value = sample.getValueAsDouble() / stepsInPeriod;
        }
        return value;
    }

    private Double calcAvgSample(List<JEVisSample> currentRawSamples) throws Exception {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            Double valueAsDouble = sample.getValueAsDouble();
            value += valueAsDouble;
        }
        return (value / currentRawSamples.size());
    }

    private Double calcAvgSampleUpscale(DateTime date, Map<DateTime, JEVisSample> rawSamplesMap) throws Exception {
        Double value = null;
        JEVisSample sample = rawSamplesMap.get(date);
        if (sample != null) {
            value = sample.getValueAsDouble();
        }
        return (value);
    }

    private Double calcSumSampleDownscaleNotDifferential(List<JEVisSample> currentRawSamples) throws Exception {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            Double valueAsDouble = sample.getValueAsDouble();
            value += valueAsDouble;
        }
        return value;
    }

    private Double calcSumSampleDownscaleDifferential(List<JEVisSample> currentRawSamples) throws Exception {
        double value = 0.0;
        if (currentRawSamples.size() > 0) {
            value = currentRawSamples.get(currentRawSamples.size() - 1).getValueAsDouble();
        }
        return value;
    }

    private Double calcLastSampleUpscale(DateTime date, Map<DateTime, Double> rawSamplesSteps) {
        /**
         * TODO testing
         */
        return rawSamplesSteps.get(date);
    }
}
