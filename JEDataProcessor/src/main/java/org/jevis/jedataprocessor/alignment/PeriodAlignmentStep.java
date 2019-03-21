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
        List<JEVisSample> rawSamples = resourceManager.getRawSamples();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        Integer periodOffset = resourceManager.getCleanDataObject().getPeriodOffset();

        //align the raw samples to the intervals
        Map<DateTime, Long> counter = new HashMap<>();
        Map<DateTime, JEVisSample> rawSamplesMap = new HashMap<>();
        Map<DateTime, Double> rawSamplesSteps = new HashMap<>();
        Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
        Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();

        boolean downSampling = true;

        PeriodComparator periodComparator = new PeriodComparator();
        int compare = periodComparator.compare(periodCleanData, periodRawData);
        // if clean data period is longer (e.g. 1 day) or equal than raw data period (e.g. 15 minutes)
        // the down sampling method will be used, else the other

        if (compare < 0) {
            downSampling = false;
        }

        if (downSampling) {
            int currentSamplePointer = 0;
            for (CleanInterval cleanInterval : intervals) {
                boolean samplesInInterval = true;
                DateTime snapToGridStart = null;
                DateTime snapToGridEnd = null;
                DateTime date = cleanInterval.getDate();
                long start = cleanInterval.getInterval().getStartMillis();
                long end = cleanInterval.getInterval().getEndMillis();
                long halfDiff = (end - start) / 2;
                snapToGridStart = date.minus(halfDiff);
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

            for (JEVisSample rawSample : rawSamples) {
                DateTime currentTS = rawSample.getTimestamp();
                try {

                    double currentValue = rawSample.getValueAsDouble();

                    JEVisSample nextSample = rawSamples.get(rawSamples.indexOf(rawSample) + 1);
                    double nextSampleValue = 0.0;
                    if (nextSample != null) nextSampleValue = nextSample.getValueAsDouble();

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

                    double step = (nextSampleValue - currentValue) / c;
                    for (int i = 0; i < lastAddedDT.size(); i++) {
                        DateTime curDate = lastAddedDT.get(i);
                        //rawSamplesSteps.put(curDate, step * (lastAddedDT.size() - i));
                        rawSamplesSteps.put(curDate, currentValue + (step * i));
                    }

                    if (!counter.containsKey(currentTS)) {
                        counter.put(currentTS, c);
                    } else {
                        Long val = counter.get(currentTS);
                        counter.remove(currentTS);
                        counter.put(currentTS, c + val);
                    }
                } catch (Exception e) {
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


                boolean last = conversionDifferential;
                boolean sum = valueIsQuantity && !conversionDifferential;
                boolean avg = !valueIsQuantity && !conversionDifferential;

                if (currentInterval.getDate().equals(timeStampOfConversion) || (currentInterval.getDate().isAfter(timeStampOfConversion) &&
                        ((nextTimeStampOfConversion == null) || currentInterval.getDate().isBefore(nextTimeStampOfConversion)))) {

                    //logger.info("align {},last {}, sum {}, avg {}", cleanDataObject.getIsPeriodAligned(), last, sum, avg);
                    List<JEVisSample> currentRawSamples = currentInterval.getRawSamples();
                    if (currentRawSamples.isEmpty()) {
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
                    } else if (last) { //last sample
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

                    } else if (avg) {
                        Double currentValue;
                        if (downSampling) currentValue = calcAvgSample(currentRawSamples);
                        else {
                            DateTime date = currentInterval.getDate();
                            currentValue = calcAvgSampleUpscale(date, rawSamplesMap);
                        }
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
                    } else if (sum) {
                        Double currentValue;
                        if (downSampling) currentValue = calcSumSample(currentRawSamples);
                        else {
                            DateTime date = currentInterval.getDate();
                            currentValue = calcSumSampleUpscale(date, rawSamplesMap, counter);
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

    private Double calcSumSampleUpscale(DateTime date, Map<DateTime, JEVisSample> rawSamplesMap, Map<DateTime, Long> counter) throws JEVisException {
        double value = 0.0;
        JEVisSample sample = rawSamplesMap.get(date);
        if (sample != null) {
            DateTime ts = sample.getTimestamp();
            value = sample.getValueAsDouble();
            long c = counter.get(ts);
            if (c > 0) value = value / c;
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

    private Double calcAvgSampleUpscale(DateTime date, Map<DateTime, JEVisSample> allRawSamples) throws Exception {
        double value = 0.0;
        /**
         * TODO unfinished
         */
        JEVisSample sample = allRawSamples.get(date);
        if (sample != null) {
            value = sample.getValueAsDouble();
        }
        return (value);
    }

    private Double calcSumSample(List<JEVisSample> currentRawSamples) throws Exception {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            Double valueAsDouble = sample.getValueAsDouble();
            value += valueAsDouble;
        }
        return value;
    }

    private Double calcLastSampleUpscale(DateTime date, Map<DateTime, Double> rawSamplesSteps) {
        return rawSamplesSteps.get(date);
    }
}
