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
import org.jevis.jedataprocessor.data.CleanDataObject;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
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
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listConversionToDifferential = cleanDataObject.getConversionDifferential();
        List<JEVisSample> listCounterOverflow = cleanDataObject.getCounterOverflow();

        if (listConversionToDifferential != null) {

            //Double lastDiffVal = cleanDataObject.getLastCounterValue();
            if (intervals.size() > 0) {
                DateTime firstTS = intervals.get(0).getInterval().getStart().minus(cleanDataObject.getRawDataPeriodAlignment());
                Double lastDiffVal = null;
                List<JEVisSample> rawSamples = cleanDataObject.getRawSamples();
                for (int i = 0; i < rawSamples.size(); i++) {
                    JEVisSample prevSmp = null;
                    if (i > 1) prevSmp = rawSamples.get(i - 1);
                    JEVisSample smp = rawSamples.get(i);

                    if (smp.getTimestamp().equals(firstTS)) {
                        lastDiffVal = smp.getValueAsDouble();
                        break;
                    } else if (prevSmp != null) {
                        if (prevSmp.getTimestamp().isBefore(firstTS) && smp.getTimestamp().isAfter(firstTS)) {
                            long diffToPrev = Math.abs(firstTS.getMillis() - prevSmp.getTimestamp().getMillis());
                            long diffToNxt = Math.abs(firstTS.getMillis() - smp.getTimestamp().getMillis());

                            if (diffToPrev < diffToNxt) lastDiffVal = prevSmp.getValueAsDouble();
                            else lastDiffVal = smp.getValueAsDouble();
                        }
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
                            if (currentInt.getDate().isAfter(timeStampOfConversion) && ((nextTimeStampOfConversion == null) || currentInt.getDate().isBefore(nextTimeStampOfConversion))) {
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
}
