/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.differential;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class DifferentialStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(DifferentialStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listConversionToDifferential = cleanDataObject.getConversionDifferential();
        List<JEVisSample> listCounterOverflow = cleanDataObject.getCounterOverflow();

        if (listConversionToDifferential != null) {

            List<Interval> ctdList = getIntervalsFromConversionToDifferentialList(listConversionToDifferential);

            boolean downSampling = true;
            boolean isCounterChange = false;
            Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
            Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();

            PeriodComparator periodComparator = new PeriodComparator();
            int compare = periodComparator.compare(periodCleanData, periodRawData);
            if (compare < 0) {
                downSampling = false;
            }

            if (intervals.size() > 0) {
                Double lastDiffVal = null;
                DateTime lastDiffTS = null;
                CleanInterval lastInterval = null;

                List<JEVisSample> rawSamples = new ArrayList<>();

                DateTime firstTS = intervals.get(0).getDate();
                boolean found = false;

                if (downSampling) {
                    DateTime startInt = intervals.get(0).getDate().minus(periodCleanData).minus(periodCleanData);
                    DateTime endInt = intervals.get(1).getDate();
                    rawSamples = cleanDataObject.getRawAttribute().getSamples(startInt, endInt);
                    JEVisSample lastSample = null;
                    for (JEVisSample smp : rawSamples) {
                        DateTime timestamp = smp.getTimestamp();
                        if (lastSample != null && (timestamp.equals(firstTS) || timestamp.isAfter(firstTS)) && timeStampInIntervals(timestamp, ctdList)) {
                            lastDiffVal = lastSample.getValueAsDouble();
                            lastDiffTS = lastSample.getTimestamp();
                            break;
                        } else lastSample = smp;
                    }
                } else {
//                    DateTime firstDate = cleanDataObject.getFirstDate();
                    rawSamples = cleanDataObject.getRawSamplesUp();
                    if (!intervals.isEmpty()) {
                        CleanInterval firstInterval = intervals.get(0);
                        if (firstInterval != null && !firstInterval.getTmpSamples().isEmpty()) {

                            JEVisSample firstTmpSample = firstInterval.getTmpSamples().get(0);
                            if (firstTmpSample != null) {

                                Double firstIntervalValue = firstTmpSample.getValueAsDouble();

                                long millisClean = periodCleanData.toStandardDuration().getMillis();
                                long millisRaw = periodRawData.toStandardDuration().getMillis();

                                double stepsInPeriod = (double) millisRaw / (double) millisClean;

                                double diffFirstTwoRawSamples = rawSamples.get(1).getValueAsDouble() - rawSamples.get(0).getValueAsDouble();
                                double stepSize = diffFirstTwoRawSamples / stepsInPeriod;

                                lastDiffVal = firstIntervalValue - stepSize;
                                lastDiffTS = firstTmpSample.getTimestamp();
                            }
                        }
                    }
                }

                if (lastDiffVal == null) {
                    if (rawSamples.size() > 0) {
                        for (JEVisSample smp : rawSamples) {
                            if (smp.getTimestamp().isBefore(firstTS)) {
                                lastDiffVal = smp.getValueAsDouble();
                                lastDiffTS = smp.getTimestamp();
                            }
                        }
                    }

                    if (lastDiffVal == null) {
                        logger.warn("No raw samples! Assuming starting value .");
                        lastDiffVal = 0d;
                        if (intervals.size() > 1) {
                            Double value1 = null;
                            if (!intervals.get(0).getRawSamples().isEmpty()) {
                                value1 = intervals.get(0).getRawSamples().get(0).getValueAsDouble();
                            } else if (!intervals.get(0).getTmpSamples().isEmpty()) {
                                value1 = intervals.get(0).getTmpSamples().get(0).getValueAsDouble();
                            }

                            Double value2 = null;
                            DateTime value2TS = null;

                            if (intervals.get(0).getRawSamples().size() > 1) {
                                if (!intervals.get(0).getRawSamples().isEmpty()) {
                                    value2 = intervals.get(0).getRawSamples().get(1).getValueAsDouble();
                                    value2TS = intervals.get(0).getRawSamples().get(1).getTimestamp();
                                } else if (!intervals.get(0).getTmpSamples().isEmpty()) {
                                    value2 = intervals.get(0).getTmpSamples().get(1).getValueAsDouble();
                                    value2TS = intervals.get(0).getTmpSamples().get(1).getTimestamp();
                                }
                            } else {
                                if (!intervals.get(1).getRawSamples().isEmpty()) {
                                    value2 = intervals.get(1).getRawSamples().get(0).getValueAsDouble();
                                    value2TS = intervals.get(1).getRawSamples().get(0).getTimestamp();
                                } else if (!intervals.get(1).getTmpSamples().isEmpty()) {
                                    value2 = intervals.get(1).getTmpSamples().get(0).getValueAsDouble();
                                    value2TS = intervals.get(1).getTmpSamples().get(0).getTimestamp();
                                }
                            }
                            if (value1 != null && value2 != null) {
                                lastDiffVal = value1 - (value2 - value1);
                                lastDiffTS = value2TS;
                            }
                        }
                    }
                }

                logger.info("[{}] use differential mode with starting value {}", cleanDataObject.getCleanObject().getID(), lastDiffVal);

                for (CleanInterval currentInt : intervals) {
                    for (int i = 0; i < listConversionToDifferential.size(); i++) {
                        JEVisSample cd = listConversionToDifferential.get(i);

                        DateTime timeStampOfConversion = cd.getTimestamp();

                        DateTime nextTimeStampOfConversion = null;
                        Boolean conversionToDifferential = cd.getValueAsBoolean();
                        if (listConversionToDifferential.size() > (i + 1)) {
                            nextTimeStampOfConversion = (listConversionToDifferential.get(i + 1)).getTimestamp();
                        }

                        if (conversionToDifferential) {
                            if (currentInt.getDate().equals(timeStampOfConversion)
                                    || currentInt.getDate().isAfter(timeStampOfConversion)
                                    && ((nextTimeStampOfConversion == null) || currentInt.getDate().isBefore(nextTimeStampOfConversion))) {
                                if (!currentInt.getRawSamples().isEmpty()) {
                                    currentInt.getTmpSamples().clear();
                                    for (JEVisSample curSample : currentInt.getRawSamples()) {

                                        int index = currentInt.getRawSamples().indexOf(curSample);
                                        DateTime tmpTimeStamp = curSample.getTimestamp();
                                        if (currentInt.getTmpSamples().size() > index) {
                                            tmpTimeStamp = currentInt.getTmpSamples().get(index).getTimestamp();
                                        }

                                        if (currentInt.getTmpSamples().size() == 1) {
                                            tmpTimeStamp = currentInt.getDate();
                                        }

                                        Double rawValue = curSample.getValueAsDouble();

                                        if (isCounterChange) {
                                            lastDiffVal = rawValue;
                                            isCounterChange = false;
                                            continue;
                                        }

                                        double cleanedVal = rawValue - lastDiffVal;
                                        isCounterChange = curSample.getNote().contains("cc");

                                        if (cleanDataObject.getRawDataPeriodAlignment().equals(Period.months(1)) && !isCounterChange) {

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

                                            if (dayOfMonth > 1 && index == 0 && lastInterval != null && lastInterval.getTmpSamples().isEmpty()) {

                                                double lastValue = dayValue * daysOfLastMonth;
                                                cleanedVal = dayValue * dayOfMonth;

                                                JEVisSample newTmpSample = new VirtualSample(tmpTimeStamp.minusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0), lastValue);
                                                newTmpSample.setNote("");
                                                lastInterval.getTmpSamples().add(newTmpSample);

                                            } else if (dayOfMonth > 1 && index == 0 && lastInterval != null && !lastInterval.getTmpSamples().isEmpty()) {

                                                cleanedVal = dayValue * dayOfMonth;

                                                JEVisSample lastSample = lastInterval.getTmpSamples().get(0);
                                                double lastValue = (dayValue * daysOfLastMonth) + lastSample.getValueAsDouble();

                                                lastSample.setValue(lastValue);

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

                                        if (currentInt.getTmpSamples().isEmpty()) {
                                            JEVisSample newTmpSample = new VirtualSample(currentInt.getDate(), cleanedVal);
                                            newTmpSample.setNote(note);
                                            currentInt.addTmpSample(newTmpSample);
                                        } else {
                                            JEVisSample tmpSample = currentInt.getTmpSamples().get(0);
                                            tmpSample.setValue(tmpSample.getValueAsDouble() + cleanedVal);
                                        }
                                        lastDiffVal = rawValue;
                                        lastDiffTS = curSample.getTimestamp();
                                    }
                                }
                            }
                        }
                    }
                    lastInterval = currentInt;
                }
            }
        }

    }

    private boolean timeStampInIntervals(DateTime timestamp, List<Interval> ctdList) {
        boolean contained = false;
        for (Interval interval : ctdList) {
            if (!contained) {
                contained = timestamp.equals(interval.getStart()) || (timestamp.isAfter(interval.getStart()) && timestamp.isBefore(interval.getEnd()));
            }
        }
        return contained;
    }

    private List<Interval> getIntervalsFromConversionToDifferentialList
            (List<JEVisSample> listConversionToDifferential) {
        List<Interval> tempList = new ArrayList<>();
        try {
            boolean starting = false;
            DateTime lastTs = new DateTime(2001, 1, 1, 0, 0, 0);
            int size = listConversionToDifferential.size();
            for (int i = 0; i < size; i++) {
                JEVisSample cd = listConversionToDifferential.get(i);

                Boolean conversionToDifferential = cd.getValueAsBoolean();
                DateTime timeStampOfConversion = cd.getTimestamp();

                //first interval
                if (i == 0 && !lastTs.equals(timeStampOfConversion)) {
                    Interval interval = new Interval(lastTs, timeStampOfConversion);
                    if (conversionToDifferential) tempList.add(interval);
                    lastTs = timeStampOfConversion;
                } else if (i == 0 && lastTs.equals(timeStampOfConversion)) {

                } else if (i == size - 1 && !starting) {
                    Interval interval = new Interval(timeStampOfConversion, new DateTime(2050, 1, 1, 0, 0, 0));
                    if (conversionToDifferential) tempList.add(interval);
                } else if (conversionToDifferential && !starting) {
                    starting = true;
                    lastTs = timeStampOfConversion;
                } else if (!conversionToDifferential && starting) {
                    starting = false;
                    Interval interval = new Interval(lastTs, timeStampOfConversion);
                    tempList.add(interval);
                    lastTs = timeStampOfConversion;
                }
            }
            Boolean lastConversionToDifferential = listConversionToDifferential.get(size - 1).getValueAsBoolean();
            DateTime lastTimeStampOfConversion = listConversionToDifferential.get(size - 1).getTimestamp();
            if (lastConversionToDifferential) {
                Interval newInterval = new Interval(lastTimeStampOfConversion, new DateTime(2050, 1, 1, 0, 0, 0));
                if (!tempList.contains(newInterval)) tempList.add(newInterval);
            }
        } catch (Exception e) {
            logger.error("Could not create Interval list from conversion to differential configuration: " + e);
        }

        return tempList;
    }
}
