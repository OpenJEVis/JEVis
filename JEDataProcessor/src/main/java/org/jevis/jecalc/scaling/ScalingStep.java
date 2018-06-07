/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.scaling;

import org.jevis.jecalc.workflow.ProcessStep;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.CleanDataAttribute;
import java.util.List;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author broder
 */
public class ScalingStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(ScalingStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        List<CleanInterval> intervals = resourceManager.getIntervals();

        StopWatch stopWatch = new Slf4JStopWatch("scaling");
        Double multiplier = calcAttribute.getMultiplier();
        Double offset = calcAttribute.getOffset();
        logger.info("scale with multiplier {} and offset {}", multiplier, offset);
        for (CleanInterval currentInt : intervals) {
            for (JEVisSample sample : currentInt.getTmpSamples()) {
                try {
                    Double rawValue = sample.getValueAsDouble();
                    if (rawValue != null) {
                        Double cleanedValue = rawValue * multiplier + offset;
                        sample.setValue(cleanedValue);
                        String note = sample.getNote();
                        note += ",scale";
                        sample.setNote(note);
                    }
                } catch (JEVisException ex) {
                    logger.error(null, ex);
                }
            }

        }
        stopWatch.stop();
    }

}
