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
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.workflow.*;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

/**
 * @author broder
 */
public class DifferentialStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(DifferentialStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        List<DifferentialRule> listConversionToDifferential = cleanDataObject.getDifferentialRules();
        List<JEVisSample> listCounterOverflow = cleanDataObject.getCounterOverflow();
        Map<DateTime, JEVisSample> userDataMap = resourceManager.getUserDataMap();

        int rawPointer = 0;
        for (CleanInterval interval : intervals) {
            int compare = interval.getCompare();
            Boolean isDifferential = interval.isDifferential();

            DateTime start = interval.getInterval().getStart();
            DateTime end = interval.getInterval().getEnd();

            boolean samplesInInterval = true;
            while (samplesInInterval && rawPointer < rawSamples.size()) {
                JEVisSample jeVisSample = rawSamples.get(rawPointer);
                DateTime timeStamp = jeVisSample.getTimestamp();

                if (userDataMap.containsKey(timeStamp)) {
                    jeVisSample = userDataMap.get(timeStamp);
                }

                if (compare > 0
                        && (interval.getOutputPeriod().getDays() == 0 && interval.getOutputPeriod().getWeeks() == 0 && interval.getOutputPeriod().getMonths() == 0 && interval.getOutputPeriod().getYears() == 0)
                        && (timeStamp.equals(end) || (timeStamp.isAfter(start) && timeStamp.isBefore(end)))) {
                    //raw data  period is smaller than clean data period, e.g. 15-minute values -> day values
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (compare > 0
                        && (interval.getOutputPeriod().getDays() == 1 || interval.getOutputPeriod().getWeeks() == 1 || interval.getOutputPeriod().getMonths() == 1 || interval.getOutputPeriod().getYears() == 1)
                        && (timeStamp.equals(start.minusSeconds(1)) || (timeStamp.isAfter(start) && timeStamp.isBefore(end)))) {
                    //raw data  period is smaller than clean data period, e.g. 15-minute values -> month/year values
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (compare < 0
                        && ((timeStamp.equals(end) && !interval.getOutputPeriod().equals(Period.minutes(5)))
                        || (interval.getOutputPeriod().equals(Period.minutes(5)) && (timeStamp.equals(interval.getInterval().getEnd()) || (timeStamp.isAfter(interval.getInterval().getStart()) && timeStamp.isBefore(interval.getInterval().getEnd())))))) {
                    //raw data  period is bigger than clean data period, e.g. day values -> 15-minute values

                    if (interval.getOutputPeriod().equals(Period.minutes(5))) {
                        if (isDifferential) {
                            if (rawPointer == 1) {
                                Double newValue = jeVisSample.getValueAsDouble() - ((jeVisSample.getValueAsDouble() - rawSamples.get(0).getValueAsDouble()) / 2);
                                VirtualSample virtualSample = new VirtualSample(timeStamp, newValue);
                                virtualSample.setNote(jeVisSample.getNote());
                                interval.getRawSamples().add(virtualSample);
                                rawPointer--;
                                break;
                            } else if (rawPointer > 1 && (interval.getDate().getMinuteOfHour() % 5 == 0 || interval.getDate().getMinuteOfHour() % 10 == 0)) {
                                Double newValue = jeVisSample.getValueAsDouble() - ((jeVisSample.getValueAsDouble() - rawSamples.get(rawPointer - 1).getValueAsDouble()) / 2);
                                VirtualSample virtualSample = new VirtualSample(timeStamp, newValue);
                                virtualSample.setNote(jeVisSample.getNote());
                                interval.getRawSamples().add(virtualSample);
                                rawPointer--;
                                break;
                            } else if (rawPointer > 1 && rawSamples.size() > rawPointer + 1 && (interval.getDate().getMinuteOfHour() == 0 || interval.getDate().getMinuteOfHour() % 15 == 0)) {
                                Double newValue = jeVisSample.getValueAsDouble() - ((rawSamples.get(rawPointer + 1).getValueAsDouble() - rawSamples.get(rawPointer - 1).getValueAsDouble()) / 3);
                                VirtualSample virtualSample = new VirtualSample(timeStamp, newValue);
                                virtualSample.setNote(jeVisSample.getNote());
                                interval.getRawSamples().add(virtualSample);
                                rawPointer--;
                                break;
                            } else interval.getRawSamples().add(jeVisSample);
                        } else {
                            interval.getRawSamples().add(jeVisSample);
                            if (rawPointer > 0) {
                                rawPointer--;
                            }
                            break;
                        }
                    } else {
                        interval.getRawSamples().add(jeVisSample);
                    }

                    rawPointer++;
                } else if (compare == 0 && !isDifferential && (timeStamp.equals(interval.getDate()))) {
                    //raw data period equal clean data period
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (compare == 0 && (timeStamp.equals(end) || (timeStamp.isAfter(start) && timeStamp.isBefore(end)))) {
                    //raw data period equal clean data period
                    interval.getRawSamples().add(jeVisSample);
                    rawPointer++;
                } else if (timeStamp.isBefore(start)) {
                    rawPointer++;
                } else samplesInInterval = false;
            }
        }

        if (listConversionToDifferential != null) {

            DateTime firstTS = null;
            for (CleanInterval interval : intervals) {
                if (!interval.getRawSamples().isEmpty()) {
                    firstTS = interval.getRawSamples().get(0).getTimestamp();
                    break;
                }
            }

            DateTime lastDiffTS = firstTS;
            Double lastDiffVal = rawSamples.get(0).getValueAsDouble();
            CleanInterval lastInterval = null;
            boolean found = false;

            //Find with loaded raw samples
            for (int i = rawSamples.size() - 1; i > -1; i--) {
                JEVisSample sample = rawSamples.get(i);
                if (sample.getTimestamp().isBefore(firstTS)) {
                    lastDiffVal = sample.getValueAsDouble();
                    lastDiffTS = sample.getTimestamp();
                    found = true;
                    break;
                }
            }

            //Find with new raw samples
            if (!found) {
                for (JEVisSample smp : rawSamples) {
                    if (smp.getTimestamp().equals(firstTS) || smp.getTimestamp().isBefore(firstTS)) {
                        if (smp.getTimestamp().equals(firstTS)) {
                            logger.debug("Searching for new first diff value");
                            List<PeriodRule> periods = new ArrayList<>();
                            periods.addAll(cleanDataObject.getCleanDataPeriodAlignment());
                            periods.addAll(cleanDataObject.getRawDataPeriodAlignment());
                            Period maxPeriod = cleanDataObject.getMaxPeriod(periods);
                            DateTime newFirstTs = firstTS;
                            DateTime timestampOfFirstSample = cleanDataObject.getRawAttribute().getTimestampOfFirstSample();
                            List<JEVisSample> samples = new ArrayList<>();

                            while (timestampOfFirstSample.isBefore(newFirstTs) && samples.size() < 2 && !maxPeriod.equals(Period.ZERO)) {
                                newFirstTs = newFirstTs.minus(maxPeriod);
                                samples = cleanDataObject.getRawAttribute().getSamples(newFirstTs, firstTS);
                            }

                            for (int i = samples.size() - 1; i > -1; i--) {
                                JEVisSample sample = samples.get(i);
                                if (sample.getTimestamp().isBefore(firstTS)) {
                                    lastDiffVal = sample.getValueAsDouble();
                                    lastDiffTS = sample.getTimestamp();
                                    found = true;
                                    break;
                                }
                            }

                        } else if (smp.getTimestamp().isBefore(firstTS)) {
                            lastDiffVal = smp.getValueAsDouble();
                            lastDiffTS = smp.getTimestamp();
                        }
                    } else break;
                }
            }

            //Last fallback
            if (!found) {
                Period period = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), lastDiffTS);
                DateTime approximateLastDate = lastDiffTS.minus(period).minus(period);
                List<JEVisSample> samples = cleanDataObject.getRawAttribute().getSamples(approximateLastDate, lastDiffTS);
                for (JEVisSample smp : samples) {
                    if (smp.getTimestamp().isBefore(firstTS)) {
                        lastDiffVal = smp.getValueAsDouble();
                        lastDiffTS = smp.getTimestamp();
                    } else break;
                }
            }

            logger.debug("[{}] use differential mode with starting value {}", cleanDataObject.getCleanObject().getID(), lastDiffVal);

            for (CleanInterval interval : intervals) {
                if (interval.isDifferential()) {
                    for (JEVisSample curSample : interval.getRawSamples()) {

                        int index = interval.getRawSamples().indexOf(curSample);
                        DateTime tmpTimeStamp = curSample.getTimestamp();

                        Double rawValue = curSample.getValueAsDouble();

                        if (rawValue == null) {
                            continue;
                        }

                        if (curSample.getNote().contains(NoteConstants.Differential.COUNTER_CHANGE)) {
                            lastDiffVal = rawValue;
                            continue;
                        }

                        double cleanedVal = rawValue - lastDiffVal;

                        if (interval.getInputPeriod().equals(Period.months(1))) {

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
                    String note = null;
                    if (interval.getResult().getNote() == null) {
                        int i = intervals.indexOf(interval);
                        if (currentRawSamples.isEmpty() && i > 0) {
                            while (note == null && i > 0) {
                                List<JEVisSample> lastRawSamples = intervals.get(i - 1).getRawSamples();
                                if (!lastRawSamples.isEmpty()) {
                                    note = lastRawSamples.get(lastRawSamples.size() - 1).getNote();
                                }
                                i--;
                            }

                            if (note == null) {
                                note = "";
                            }
                        } else if (!currentRawSamples.isEmpty()) {
                            note = currentRawSamples.get(currentRawSamples.size() - 1).getNote();
                        } else {
                            note = "";
                        }
                    } else {
                        note = interval.getResult().getNote();
                    }

                    if (!note.isEmpty()) note += ",";

                    Double currentValue;
                    if (currentRawSamples != null && !valueIsQuantity) {
                        currentValue = calcAvgSample(currentRawSamples);
                        interval.getResult().setValue(currentValue);

                        int size = currentRawSamples.size();
                        if (size > 1) {
                            note += "avg" + size + ")";
                        } else {
                            note += "avg";
                        }
                    } else if (currentRawSamples != null) {
                        if (interval.getCompare() < 0 && interval.getOutputPeriod().equals(Period.minutes(5))) {
                            double df = 3d;
                            try {
                                df = (double) interval.getInputPeriod().toStandardDuration().getMillis() / (double) interval.getOutputPeriod().toStandardDuration().getMillis();
                            } catch (Exception e) {
                                logger.error("Could not determine periods for div factor", e);
                            }
                            currentValue = calcDiffSample(currentRawSamples, df);
                        } else {
                            currentValue = calcSumSample(currentRawSamples);
                        }
                        interval.getResult().setValue(currentValue);

                        int size = currentRawSamples.size();
                        if (size > 1) {
                            note += "sum" + size + ")";
                        } else {
                            note += "sum)";
                        }
                    } else {
                        //TODO: what to do
                        logger.info("Missing feature");
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

    private Double calcDiffSample(List<JEVisSample> currentRawSamples, double df) {
        Double value = 0.0;
        for (JEVisSample sample : currentRawSamples) {
            try {
                Double valueAsDouble = sample.getValueAsDouble();
                value = valueAsDouble / df;
            } catch (JEVisException ex) {
                logger.error(ex);
            }
        }
        return value;
    }
}
