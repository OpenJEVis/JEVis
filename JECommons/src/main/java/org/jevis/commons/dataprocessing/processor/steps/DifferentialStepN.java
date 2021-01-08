/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanIntervalN;
import org.jevis.commons.dataprocessing.processor.workflow.DifferentialRule;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStepN;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManagerN;
import org.jevis.commons.datetime.PeriodComparator;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

/**
 * @author broder
 */
public class DifferentialStepN implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(DifferentialStepN.class);

    @Override
    public void run(ResourceManagerN resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();
        List<CleanIntervalN> intervals = resourceManager.getIntervals();
        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        List<DifferentialRule> listConversionToDifferential = cleanDataObject.getDifferentialRules();
        List<JEVisSample> listCounterOverflow = cleanDataObject.getCounterOverflow();
        Map<DateTime, JEVisSample> userDataMap = resourceManager.getUserDataMap();
        PeriodComparator periodComparator = new PeriodComparator();

        int rawPointer = 0;
        for (CleanIntervalN interval : intervals) {
            int compare = periodComparator.compare(interval.getInputPeriod(), interval.getOutputPeriod());

            DateTime start = interval.getInterval().getStart();
            DateTime end = interval.getInterval().getEnd();

            boolean samplesInInterval = true;
            while (samplesInInterval && rawPointer < rawSamples.size()) {
                JEVisSample jeVisSample = rawSamples.get(rawPointer);
                DateTime timeStamp = jeVisSample.getTimestamp();
                if (compare > 0 && timeStamp.equals(start) || (timeStamp.isAfter(start) && timeStamp.isBefore(end))) {
                    //raw data  period is smaller then clean data period, e.g. 15-minute values -> day values
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (compare < 0 && timeStamp.equals(start)) {
                    //raw data  period is bigger then clean data period, e.g. day values -> 15-minute values
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (compare == 0 && timeStamp.equals(end) || (timeStamp.isAfter(start) && timeStamp.isBefore(end))) {
                    //raw data period equal clean data period
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (timeStamp.isBefore(start)) {
                    rawPointer++;
                } else samplesInInterval = false;
            }
        }

        if (listConversionToDifferential != null) {

            boolean isCounterChange = false;
            DateTime firstTS = intervals.get(0).getDate();
            DateTime lastDiffTS = firstTS;
            Double lastDiffVal = rawSamples.get(0).getValueAsDouble();
            CleanIntervalN lastInterval = null;

            for (JEVisSample smp : rawSamples) {
                if (smp.getTimestamp().isBefore(firstTS)) {
                    lastDiffVal = smp.getValueAsDouble();
                    lastDiffTS = smp.getTimestamp();
                } else break;
            }

            logger.debug("[{}] use differential mode with starting value {}", cleanDataObject.getCleanObject().getID(), lastDiffVal);

            for (CleanIntervalN interval : intervals) {
                if (interval.getDifferential()) {
                    for (JEVisSample curSample : interval.getRawSamples()) {

                        int index = interval.getRawSamples().indexOf(curSample);
                        DateTime tmpTimeStamp = curSample.getTimestamp();

                        Double rawValue = curSample.getValueAsDouble();

                        if (isCounterChange) {
                            lastDiffVal = rawValue;
                            isCounterChange = false;
                            continue;
                        }

                        double cleanedVal = rawValue - lastDiffVal;
                        isCounterChange = curSample.getNote().contains("cc");

                        if (interval.getInputPeriod().equals(Period.months(1)) && !isCounterChange) {

                            int dayOfMonth = curSample.getTimestamp().getDayOfMonth();

                            int maxDaysLastMonth = curSample.getTimestamp().minusMonths(1).dayOfMonth().getMaximumValue();

                            int daysOfLastMonth = 0;
                            if (lastDiffTS != null && lastDiffTS.getDayOfMonth() == 1) {
                                daysOfLastMonth = maxDaysLastMonth - (lastDiffTS.getDayOfMonth() - 1);
                            } else if (lastDiffTS != null && lastDiffTS.getDayOfMonth() > 1) {
                                daysOfLastMonth = maxDaysLastMonth - lastDiffTS.getDayOfMonth();
                            } else {
                                daysOfLastMonth = maxDaysLastMonth;
                            }

                            double divisor = dayOfMonth + daysOfLastMonth;

                            if (dayOfMonth == 1 && daysOfLastMonth == maxDaysLastMonth && divisor > 1) {
                                divisor -= 1;
                            }

                            double dayValue = cleanedVal / divisor;
                            tmpTimeStamp = tmpTimeStamp.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                            if (dayOfMonth > 1 && index == 0 && lastInterval != null && lastInterval.getResult().getValueAsDouble() == null) {

                                double lastValue = dayValue * daysOfLastMonth;
                                cleanedVal = dayValue * dayOfMonth;

                                lastInterval.getResult().setTimeStamp(tmpTimeStamp.minusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0));
                                lastInterval.getResult().setValue(lastValue);
                                lastInterval.getResult().setNote("");

                            } else if (dayOfMonth > 1 && index == 0 && lastInterval != null && lastInterval.getResult().getValueAsDouble() != null) {

                                cleanedVal = dayValue * dayOfMonth;

                                double lastSampleValue = lastInterval.getResult().getValueAsDouble();
                                double lastValue = (dayValue * daysOfLastMonth) + lastSampleValue;

                                lastInterval.getResult().setValue(lastValue);
                            }
                        }

                        String note = curSample.getNote();

                        if (cleanedVal < 0) {
                            logger.warn("[{}] Warning possible counter overflow", cleanDataObject.getCleanObject().getID());
                            for (JEVisSample counterOverflow : listCounterOverflow) {
                                if (counterOverflow != null && curSample.getTimestamp().isAfter(counterOverflow.getTimestamp())
                                        && counterOverflow.getValueAsDouble() != 0.0) {
                                    cleanedVal = (counterOverflow.getValueAsDouble() - lastDiffVal) + rawValue;
                                    note += "," + NoteConstants.Differential.COUNTER_OVERFLOW;
                                    break;
                                }
                            }
                        }

                        note += "," + NoteConstants.Differential.DIFFERENTIAL_ON;

                        if (interval.getResult().getValueAsDouble() == null) {
                            interval.getResult().setValue(cleanedVal);
                            interval.getResult().setNote(note);
                        } else {
                            interval.getResult().setValue(interval.getResult().getValueAsDouble() + cleanedVal);
                        }
                        lastDiffVal = rawValue;
                        lastDiffTS = curSample.getTimestamp();
                    }
                } else {

                    boolean valueIsQuantity = cleanDataObject.getValueIsQuantity();

                    List<JEVisSample> currentRawSamples = interval.getRawSamples();
                    String note;
                    if (interval.getResult().getNote() == null) {
                        note = currentRawSamples.get(0).getNote();
                    } else {
                        note = interval.getResult().getNote();
                    }

                    Double currentValue;
                    if (!valueIsQuantity) {
                        currentValue = calcAvgSample(currentRawSamples);
                        interval.getResult().setValue(currentValue);

                        int size = currentRawSamples.size();
                        if (size > 1) {
                            note += ",avg" + size + ")";
                        } else {
                            note += ",avg";
                        }
                    } else {
                        currentValue = calcSumSample(currentRawSamples);
                        interval.getResult().setValue(currentValue);

                        int size = currentRawSamples.size();
                        if (size > 1) {
                            note += ",sum" + size + ")";
                        } else {
                            note += ",sum)";
                        }
                    }
                    if (userDataMap.containsKey(interval.getResult().getTimestamp())) {
                        note += "," + USER_VALUE;
                    }
                    interval.getResult().setNote(note);
                }
                lastInterval = interval;
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
