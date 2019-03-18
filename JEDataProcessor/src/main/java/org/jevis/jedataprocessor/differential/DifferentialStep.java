/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.differential;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Interval;

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

            if (intervals.size() > 0) {
                Double lastDiffVal = null;
                DateTime firstDate = cleanDataObject.getFirstDate()
                        .minus(cleanDataObject.getCleanDataPeriodAlignment())
                        .minus(cleanDataObject.getCleanDataPeriodAlignment());
                List<JEVisSample> rawSamples = cleanDataObject.getRawAttribute().getSamples(firstDate, cleanDataObject.getLastRawDate());
                DateTime firstTS = cleanDataObject.getFirstDate();
                boolean found = false;

                JEVisSample lastSample = null;
                for (JEVisSample smp : rawSamples) {
                    DateTime timestamp = smp.getTimestamp();
                    if (timestamp.equals(firstTS) && timeStampInIntervals(timestamp, ctdList)) {
                        lastDiffVal = smp.getValueAsDouble();
                        break;
                    } else if (lastSample != null && timestamp.isAfter(firstTS) && timeStampInIntervals(timestamp, ctdList)) {
//                        long diffToPrev = Math.abs(firstTS.getMillis() - lastSample.getTimestamp().getMillis());
//                        long diffToNxt = Math.abs(firstTS.getMillis() - timestamp.getMillis());
//
//                        if (diffToPrev < diffToNxt) lastDiffVal = lastSample.getValueAsDouble();
//                        else lastDiffVal = smp.getValueAsDouble();
                        lastDiffVal = lastSample.getValueAsDouble();
                        break;

                    } else lastSample = smp;
                }

                if (lastDiffVal == null) {
                    for (JEVisSample smp : rawSamples) {
                        DateTime ts = smp.getTimestamp();

                        for (Interval interval : ctdList) {
                            if (ts.equals(interval.getStart()) || (ts.isAfter(interval.getStart()) && ts.isBefore(interval.getEnd()))) {
                                lastDiffVal = smp.getValueAsDouble();
                                found = true;
                                break;
                            }
                        }

                        if (found) break;
                    }
                }

                if (lastDiffVal == null) {
                    if (rawSamples.size() > 0) {
                        lastDiffVal = rawSamples.get(0).getValueAsDouble();
                    } else {
                        throw new JEVisException("No raw samples!", 232134093);
                    }
                }

                logger.info("[{}] use differential mode with starting value {}", cleanDataObject.getCleanObject().getID(), lastDiffVal);

                //get last Value which is smaller than the first interval val
                boolean wasEmpty = false;
                List<CleanInterval> emptyIntervals = new ArrayList<>();

                for (CleanInterval currentInt : intervals) {
                    for (int i = 0; i < listConversionToDifferential.size(); i++) {
                        JEVisSample cd = listConversionToDifferential.get(i);

                        DateTime timeStampOfConversion = cd.getTimestamp();

                        DateTime nextTimeStampOfConversion = null;
                        Boolean conversionToDifferential = cd.getValueAsBoolean();
//                    Boolean conversionToDifferential = (cd.getValueAsString().equals("1") || cd.getValueAsBoolean()) ? true : false;
                        if (listConversionToDifferential.size() > (i + 1)) {
                            nextTimeStampOfConversion = (listConversionToDifferential.get(i + 1)).getTimestamp();
                        }

                        if (conversionToDifferential) {
                            if (currentInt.getDate().equals(timeStampOfConversion)
                                    || currentInt.getDate().isAfter(timeStampOfConversion)
                                    && ((nextTimeStampOfConversion == null) || currentInt.getDate().isBefore(nextTimeStampOfConversion))) {
                                if (!currentInt.getTmpSamples().isEmpty()) {
                                    for (JEVisSample curSample : currentInt.getTmpSamples()) {

                                        Double rawValue = curSample.getValueAsDouble();
                                        double cleanedVal = rawValue - lastDiffVal;
                                        String note = curSample.getNote();

                                        if (cleanedVal < 0) {
                                            logger.warn("[{}] Warning possible counter overflow", cleanDataObject.getCleanObject().getID());
                                            for (JEVisSample counterOverflow : listCounterOverflow) {
                                                if (counterOverflow != null && curSample.getTimestamp().isAfter(counterOverflow.getTimestamp())
                                                        && counterOverflow.getValueAsDouble() != 0.0) {
                                                    cleanedVal = (counterOverflow.getValueAsDouble() - lastDiffVal) + rawValue;
                                                    break;
                                                }
                                            }
                                        }

                                        curSample.setValue(cleanedVal);

                                        note += ",diff";
                                        curSample.setNote(note);
                                        lastDiffVal = rawValue;

                                        if (wasEmpty) {
                                            emptyIntervals.add(currentInt);
                                            wasEmpty = false;
                                        }

                                    }
                                } else {
                                    if (lastDiffVal != null) {
                                        wasEmpty = true;
                                    }
                                }
                            }
                        }
                    }
                }
                for (CleanInterval ci : intervals) {
                    for (CleanInterval ce : emptyIntervals) {
                        if (ci.getDate().equals(ce.getDate())) {
                            ci.getTmpSamples().clear();
                        }
                    }
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
