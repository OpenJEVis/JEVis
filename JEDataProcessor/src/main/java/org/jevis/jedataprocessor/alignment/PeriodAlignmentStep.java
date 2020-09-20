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
import org.joda.time.Period;

import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

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
        List<CleanInterval> rawIntervals = resourceManager.getRawIntervals();
        List<CleanInterval> cleanIntervals = resourceManager.getIntervals();
        Map<DateTime, JEVisSample> userDataMap = resourceManager.getUserDataMap();
        Integer periodOffset = cleanDataObject.getPeriodOffset();

        PeriodComparator periodComparator = new PeriodComparator();
        Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
        Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();
        int compare = periodComparator.compare(periodCleanData, periodRawData);

        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        int currentSamplePointer = 0;
        for (CleanInterval rawInterval : rawIntervals) {
            boolean samplesInInterval = true;
            DateTime snapToGridStart = null;
            DateTime snapToGridEnd = null;
            DateTime date = rawInterval.getDate();
            long start = rawInterval.getInterval().getStartMillis();
            long end = rawInterval.getInterval().getEndMillis();
            long halfDiff = (end - start) / 2;

            if (cleanDataObject.getIsPeriodAligned()) {
                snapToGridStart = date.minus(halfDiff);
                snapToGridEnd = date.plus(halfDiff);
            } else {
                snapToGridStart = rawInterval.getInterval().getStart();
                snapToGridEnd = rawInterval.getInterval().getEnd();
            }

            while (samplesInInterval && currentSamplePointer < rawSamples.size()) {
                JEVisSample rawSample = rawSamples.get(currentSamplePointer);
                try {
                    DateTime timestamp = rawSample.getTimestamp();
                    if (userDataMap.containsKey(timestamp)) {
                        rawSample = userDataMap.get(timestamp);
                    }

                    int offset = Math.abs(periodOffset);
                    if (periodOffset >= 0) {
                        timestamp = timestamp.plusSeconds(offset);
                    } else {
                        timestamp = timestamp.minusSeconds(offset);
                    }

                    if (compare == 0 && timestamp.equals(snapToGridStart)
                            || (timestamp.isAfter(snapToGridStart) && timestamp.isBefore(snapToGridEnd))
                            || timestamp.equals(snapToGridEnd)) { //sample is in interval
                        rawInterval.addRawSample(rawSample);
                        currentSamplePointer++;
                    } else if (compare != 0 && timestamp.equals(rawInterval.getInterval().getStart())
                            || (timestamp.isAfter(rawInterval.getInterval().getStart()) && timestamp.isBefore(rawInterval.getInterval().getEnd()))
                            || timestamp.equals(rawInterval.getInterval().getEnd())) { //sample is in interval
                        rawInterval.addRawSample(rawSample);
                        currentSamplePointer++;
                    } else if (compare == 0 && timestamp.isBefore(snapToGridStart)) { //sample is before interval start --just find the start
                        currentSamplePointer++;
                    } else if (compare != 0 && timestamp.isBefore(rawInterval.getInterval().getStart())) { //sample is before interval start --just find the start
                        currentSamplePointer++;
                    } else {
                        samplesInInterval = false;
                    }
                } catch (Exception ex) {
                    throw new Exception("error while align the raw samples to the interval, no timestamp found", ex);
                }
            }
        }

        List<JEVisSample> listConversionToDifferential = cleanDataObject.getConversionDifferential();
        Boolean valueIsQuantity = cleanDataObject.getValueIsQuantity();

        //calc modes

        for (CleanInterval currentInterval : rawIntervals) {
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
                boolean avg = !valueIsQuantity && !conversionDifferential;

                if (currentInterval.getDate().equals(timeStampOfConversion) || (currentInterval.getDate().isAfter(timeStampOfConversion) &&
                        ((nextTimeStampOfConversion == null) || currentInterval.getDate().isBefore(nextTimeStampOfConversion)))) {
                    List<JEVisSample> currentRawSamples = currentInterval.getRawSamples();
                    if (currentRawSamples.isEmpty()) {
                        continue;
                    }

                    try {
                        if (!cleanDataObject.getIsPeriodAligned()) { //no alignment
                            for (JEVisSample sample : currentRawSamples) {
                                if (sample.getNote() != null && !sample.getNote().equals("")) {
                                    sample.setNote(sample.getNote() + "," + "alignment(no)");
                                } else {
                                    sample.setNote("alignment(no)");
                                }
                                if (userDataMap.containsKey(sample.getTimestamp())) {
                                    sample.setNote(sample.getNote() + "," + USER_VALUE);
                                }
                                currentInterval.addTmpSample(sample);
                            }
                        } else if (last) { //last sample
                            DateTime date = currentInterval.getDate();
                            JEVisSample rawSample = currentRawSamples.get(currentRawSamples.size() - 1);
                            Double valueAsDouble = rawSample.getValueAsDouble();
                            JEVisSample sample = new VirtualSample(date, valueAsDouble);
                            double diff = (date.getMillis() - rawSample.getTimestamp().getMillis()) / 1000;
                            String note = "";
                            if (diff > 0) {
                                note = "alignment(yes,+" + diff + "s,last)";
                            } else if (diff < 0) {
                                note = "alignment(yes,-" + Math.abs(diff) + "s,last)";
                            } else {
                                note = "alignment(no)";
                            }
                            if (userDataMap.containsKey(sample.getTimestamp())) {
                                note += "," + USER_VALUE;
                            }
                            sample.setNote(note);
                            rawSample.setNote(note);
                            currentInterval.addTmpSample(sample);

                        } else if (avg) {
                            Double currentValue = calcAvgSample(currentRawSamples);
                            DateTime date = currentInterval.getDate();
                            JEVisSample sample = new VirtualSample(date, currentValue);
                            String note = "";
                            if (currentRawSamples.size() == 1) {
                                JEVisSample rawSample = currentRawSamples.get(currentRawSamples.size() - 1);
                                double diff = (date.getMillis() - rawSample.getTimestamp().getMillis()) / 1000;
                                if (diff > 0) {
                                    note = "alignment(yes,+" + diff + "s,avg)";
                                } else if (diff < 0) {
                                    note = "alignment(yes,-" + Math.abs(diff) + "s,avg)";
                                }
                            }
                            if (note.equals("")) {
                                note = ("alignment(no)");
                            }
                            if (userDataMap.containsKey(sample.getTimestamp())) {
                                note += "," + USER_VALUE;
                            }
                            for (JEVisSample rawSample : rawSamples) {
                                rawSample.setNote(note);
                            }
                            sample.setNote(note);
                            currentInterval.addTmpSample(sample);
                        } else if (sum) {
                            Double currentValue = null;

                            int currentIntervalIndex = rawIntervals.indexOf(currentInterval);
                            if (periodRawData.equals(Period.ZERO) && periodCleanData.equals(Period.minutes(15))
                                    && currentIntervalIndex > 0) {
                                int lastIntervalWithSamples = 0;
                                int noOfIntermittentIntervals = 0;
                                for (int j = currentIntervalIndex - 1; j > -1; j--) {
                                    if (rawIntervals.get(j).getRawSamples().isEmpty()) {
                                        noOfIntermittentIntervals++;
                                    } else {
                                        lastIntervalWithSamples = j;
                                        break;
                                    }
                                }

                                currentValue = calcSumSample(currentRawSamples);
                                noOfIntermittentIntervals++;
                                if (currentValue != 0 && noOfIntermittentIntervals != 0) {
                                    currentValue = currentValue / noOfIntermittentIntervals;
                                }

                                for (int j = currentIntervalIndex - 1; j > lastIntervalWithSamples; j--) {
                                    CleanInterval cleanIntervalBack = rawIntervals.get(j);
                                    DateTime date = cleanIntervalBack.getDate();
                                    JEVisSample sample = new VirtualSample(date, currentValue);
                                    String note = "alignment(yes,sum)";
                                    if (userDataMap.containsKey(sample.getTimestamp())) {
                                        note += "," + USER_VALUE;
                                    }
                                    sample.setNote(note);
                                    cleanIntervalBack.addTmpSample(sample);
                                }
                            } else {
                                currentValue = calcSumSample(currentRawSamples);
                            }

                            DateTime date = currentInterval.getDate();
                            JEVisSample sample = new VirtualSample(date, currentValue);
                            String note = "";
                            if (currentRawSamples.size() == 1) {
                                JEVisSample rawSample = currentRawSamples.get(currentRawSamples.size() - 1);
                                double diff = (date.getMillis() - rawSample.getTimestamp().getMillis()) / 1000;
                                if (diff > 0) {
                                    note = "alignment(yes,+" + diff + "s,sum)";
                                } else if (diff < 0) {
                                    note = "alignment(yes,-" + Math.abs(diff) + "s,sum)";
                                }
                            }
                            if (note.equals("")) {
                                note = ("alignment(no)");
                            }
                            if (userDataMap.containsKey(sample.getTimestamp())) {
                                note += "," + USER_VALUE;
                            }
                            sample.setNote(note);
                            for (JEVisSample rawSample : rawSamples) {
                                rawSample.setNote(note);
                            }
                            currentInterval.addTmpSample(sample);
                        }
                    } catch (JEVisException ex) {
                        logger.error(ex);
                    }
                }
            }
        }

        if (cleanDataObject.getCleanDataPeriodAlignment().equals(cleanDataObject.getRawDataPeriodAlignment())) {
            resourceManager.setIntervals(rawIntervals);
        } else {
            int rawPointer = 0;

            for (CleanInterval cleanInterval : cleanIntervals) {
                DateTime start = cleanInterval.getInterval().getStart();
                DateTime end = cleanInterval.getInterval().getEnd();

                boolean samplesInInterval = true;
                while (samplesInInterval && rawPointer < rawIntervals.size()) {
                    CleanInterval rawInterval = rawIntervals.get(rawPointer);
                    DateTime timeStamp = rawInterval.getDate();
                    if (compare < 0 && start.isAfter(timeStamp)) {
                        cleanInterval.getRawSamples().addAll(rawInterval.getRawSamples());
                        cleanInterval.getTmpSamples().addAll(rawInterval.getTmpSamples());
                        rawPointer++;
                        samplesInInterval = false;
                    } else if (compare > 0 && timeStamp.equals(start)
                            || (timeStamp.isAfter(start) && timeStamp.isBefore(end))) {
                        cleanInterval.getRawSamples().addAll(rawInterval.getRawSamples());
                        cleanInterval.getTmpSamples().addAll(rawInterval.getTmpSamples());
                        rawPointer++;
                    } else if (timeStamp.isBefore(start)) {
                        rawPointer++;
                    } else samplesInInterval = false;
                }
            }
        }
    }

    private Double calcAvgSample(List<JEVisSample> currentRawSamples) {
        if (currentRawSamples.size() > 0) {
            Double value = 0.0;
            for (JEVisSample sample : currentRawSamples) {
                try {
                    Double valueAsDouble = sample.getValueAsDouble();
                    value += valueAsDouble;
                } catch (JEVisException ex) {
                    logger.error(ex);
                }
            }
            return value / currentRawSamples.size();
        } else return 0d;
    }

    private Double calcSumSample(List<JEVisSample> currentRawSamples) {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            try {
                Double valueAsDouble = sample.getValueAsDouble();
                value += valueAsDouble;
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return value;
    }

}
