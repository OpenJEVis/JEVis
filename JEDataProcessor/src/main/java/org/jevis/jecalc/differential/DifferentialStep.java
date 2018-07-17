/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.differential;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
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
        if (!calcAttribute.getConversionDifferential()) {
            return;
        }
        StopWatch stopWatch = new Slf4JStopWatch("differential");
        List<CleanInterval> intervals = resourceManager.getIntervals();
        Double lastDiffVal = calcAttribute.getLastDiffValue();
        logger.info("use differential mode with starting value {}", lastDiffVal);
        //get last Value which is smaller than the first interval val
        Boolean wasEmtpy = false;
        List<CleanInterval> emptyIntervals = new ArrayList<>();
        for (CleanInterval currentInt : intervals) {
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
                            curSample.setValue(cleanedVal / emptyIntervals.size());
                            note += ",interpolated-break";
                            curSample.setNote(note);
                            lastDiffVal = rawValue;
                            for (CleanInterval ci : emptyIntervals) {
                                for (CleanInterval i : intervals) {
                                    if (i.getDate().equals(ci.getDate()) && i.getTmpSamples().isEmpty()) {
                                        JEVisSample newSample = new VirtualSample(ci.getDate(), cleanedVal / emptyIntervals.size());
                                        String n = curSample.getNote();
                                        n += ",diff,interpolated-break";
                                        newSample.setNote(n);
                                        i.addTmpSample(newSample);
                                    }
                                }
                            }
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
        stopWatch.stop();
    }
}
