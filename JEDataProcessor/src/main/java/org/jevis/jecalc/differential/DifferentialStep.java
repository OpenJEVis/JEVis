/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.differential;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class DifferentialStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(DifferentialStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listConversionToDifferential = calcAttribute.getConversionDifferential();
        StopWatch stopWatch = new Slf4JStopWatch("differential");

        if (listConversionToDifferential != null) {

            Double lastDiffVal = calcAttribute.getLastDiffValue();
            logger.info("use differential mode with starting value {}", lastDiffVal);
            //get last Value which is smaller than the first interval val
            Boolean wasEmtpy = false;
            List<CleanInterval> emptyIntervals = new ArrayList<>();
            for (CleanInterval currentInt : intervals) {
                for (JEVisSample cd : listConversionToDifferential) {

                    DateTime timeStampOfConversion = null;
                    DateTime nextTimeStampOfConversion = null;
                    Boolean conversionToDifferential = false;
                    try {
                        timeStampOfConversion = cd.getTimestamp();
                        if (cd.getValueAsString().equals("1") || cd.getValueAsBoolean()) //TODO find out whats wrong with the .getValueAsBoolean() function
                            conversionToDifferential = true;
                        try {
                            nextTimeStampOfConversion = (listConversionToDifferential.get(listConversionToDifferential.indexOf(cd) + 1)).getTimestamp();
                        } catch (Exception e) {

                        }
                    } catch (JEVisException e) {
                        logger.error("no timestamp", e);
                    }
                    if (conversionToDifferential) {
                        if (currentInt.getDate().isAfter(timeStampOfConversion) && ((nextTimeStampOfConversion == null) || currentInt.getDate().isBefore(nextTimeStampOfConversion))) {
                            if (!currentInt.getTmpSamples().isEmpty()) {
                                for (JEVisSample curSample : currentInt.getTmpSamples()) {
                                    try {
                                        Double rawValue = curSample.getValueAsDouble();

                                        //set the last diff value if its null (mostly if it is a fresh raw data row)
                                        if (lastDiffVal == null || rawValue == null) {
                                            if (lastDiffVal == null) {
                                                lastDiffVal = rawValue;
                                                curSample.setValue(null);
                                            }
                                            continue;
                                        }

                                        Double cleanedVal = rawValue - lastDiffVal;
                                        curSample.setValue(cleanedVal);
                                        String note = curSample.getNote();
                                        note += ",diff";
                                        curSample.setNote(note);
                                        lastDiffVal = rawValue;

                                        if (wasEmtpy) {
                                            emptyIntervals.add(currentInt);
//                                            curSample.setValue(cleanedVal / (emptyIntervals.size() + 1));
//                                            note += ",break-in-raw-data";
//                                            curSample.setNote(note);
//                                            lastDiffVal = rawValue;
//                                            for (CleanInterval ci : emptyIntervals) {
//                                                for (CleanInterval i : intervals) {
//                                                    if (i.getDate().equals(ci.getDate()) && i.getTmpSamples().isEmpty()) {
//                                                        JEVisSample newSample = new VirtualSample(ci.getDate(), cleanedVal / (emptyIntervals.size() + 1));
//                                                        String n = curSample.getNote();
//                                                        n += ",diff,break-in-raw-data";
//                                                        newSample.setNote(n);
//                                                        i.addTmpSample(newSample);
//                                                    }
//                                                }
//                                            }
                                            wasEmtpy = false;
                                        }

                                    } catch (JEVisException ex) {
                                        logger.error(null, ex);
                                    }
                                }
                            } else {
                                if (lastDiffVal != null) {
                                    wasEmtpy = true;
                                    emptyIntervals.add(currentInt);
                                }
                            }
                        }
                    }
                }
            }

            intervals.removeAll(emptyIntervals);
        }
        stopWatch.stop();
    }
}
