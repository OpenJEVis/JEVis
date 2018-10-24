/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.differential;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listConversionToDifferential = calcAttribute.getConversionDifferential();
        List<JEVisSample> listCounterOverflow = calcAttribute.getCounterOverflow();

        if (listConversionToDifferential != null) {

            Double lastDiffVal = calcAttribute.getLastCounterValue();
            logger.info("[{}] use differential mode with starting value {}", calcAttribute.getObject().getID(), lastDiffVal);
            //get last Value which is smaller than the first interval val
            Boolean wasEmpty = false;
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
                        if (currentInt.getDate().isAfter(timeStampOfConversion) && ((nextTimeStampOfConversion == null) || currentInt.getDate().isBefore(nextTimeStampOfConversion))) {
                            if (!currentInt.getTmpSamples().isEmpty()) {
                                for (JEVisSample curSample : currentInt.getTmpSamples()) {

                                    Double rawValue = curSample.getValueAsDouble();
                                    Double cleanedVal = rawValue - lastDiffVal;
                                    String note = curSample.getNote();

                                    if (cleanedVal < 0) {
                                        logger.warn("[{}] Waring counter overflow", calcAttribute.getObject().getID());
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
