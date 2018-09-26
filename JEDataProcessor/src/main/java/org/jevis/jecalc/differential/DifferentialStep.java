/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.differential;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class DifferentialStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(DifferentialStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listConversionToDifferential = calcAttribute.getConversionDifferential();
        List<JEVisSample> listCounterOverflow = calcAttribute.getCounterOverflow();

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
                                        String note = curSample.getNote();

                                        if (cleanedVal < 0) {
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

                                        if (wasEmtpy) {
                                            emptyIntervals.add(currentInt);
                                            wasEmtpy = false;
                                        }

                                    } catch (JEVisException ex) {
                                        logger.error(ex);
                                    }
                                }
                            } else {
                                if (lastDiffVal != null) {
                                    wasEmtpy = true;
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
