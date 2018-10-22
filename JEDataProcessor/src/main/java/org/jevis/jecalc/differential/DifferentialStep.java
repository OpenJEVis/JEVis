/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.differential;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.jecalc.data.CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME;

/**
 * @author broder
 */
public class DifferentialStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(DifferentialStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
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
                for (int i = 0; i < listConversionToDifferential.size(); i++) {
                    JEVisSample cd = listConversionToDifferential.get(i);
//                for (JEVisSample cd : listConversionToDifferential) {

                    DateTime timeStampOfConversion = cd.getTimestamp();

                    DateTime nextTimeStampOfConversion = null;
                    Boolean conversionToDifferential = (cd.getValueAsString().equals("1") || cd.getValueAsBoolean()) ? true : false;
                    if (listConversionToDifferential.size() > (i + 1)) {
                        nextTimeStampOfConversion = (listConversionToDifferential.get(i + 1)).getTimestamp();
                    }

                    if (conversionToDifferential) {
                        if (currentInt.getDate().isAfter(timeStampOfConversion) && ((nextTimeStampOfConversion == null) || currentInt.getDate().isBefore(nextTimeStampOfConversion))) {
                            if (!currentInt.getTmpSamples().isEmpty()) {
                                for (JEVisSample curSample : currentInt.getTmpSamples()) {

                                    Double rawValue = curSample.getValueAsDouble();

//                                    //set the last diff value if its null (mostly if it is a fresh raw data row)
//                                    if (lastDiffVal == null || rawValue == null) {
//                                        if (lastDiffVal == null) {
//                                            lastDiffVal = rawValue;
//                                            curSample.setValue(null);
//                                        }
//                                        continue;
//                                    }

                                    /**
                                     * TODO: solve this design problem
                                     * What to do with the first sample in an diff calc because we don't have thepreviouss value
                                     * to calculate the difference in an counter value.
                                     */
                                    if (lastDiffVal == null || rawValue == null) {
                                        if (resourceManager.getCalcAttribute().getFirstDate() == null) {
                                            logger.warn("Special case Diff1: first sample in Diff: {}", curSample.getTimestamp());
                                            lastDiffVal = 0d;
                                        } else {
                                            logger.warn("Special case Diff2: first sample in Diff: {}", curSample.getTimestamp());
                                            List<JEVisSample> prePeriodVavlues = (new SampleHandler()).getSamplesInPeriod(
                                                    resourceManager.getCalcAttribute().getObject().getParents().get(0), VALUE_ATTRIBUTE_NAME,
                                                    resourceManager.getCalcAttribute().getFirstDate().minus(resourceManager.getCalcAttribute().getPeriodAlignment()),
                                                    curSample.getTimestamp().minus(Period.minutes(1))
                                            );

                                            if (!prePeriodVavlues.isEmpty()) {
                                                JEVisSample lastRaw = prePeriodVavlues.get(prePeriodVavlues.size() - 1);
                                                lastDiffVal = lastRaw.getValueAsDouble();
                                            } else {
                                                throw new Exception("Error in DifferentialStep missing previous value before: " + curSample.getTimestamp());
                                            }
                                        }
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
