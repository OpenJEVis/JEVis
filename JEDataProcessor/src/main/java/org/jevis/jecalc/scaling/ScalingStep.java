/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.scaling;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author broder
 */
public class ScalingStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(ScalingStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        List<CleanInterval> intervals = resourceManager.getIntervals();

        StopWatch stopWatch = new Slf4JStopWatch("scaling");
        BigDecimal multiplier = new BigDecimal(calcAttribute.getMultiplier().toString());
        BigDecimal offset = new BigDecimal(calcAttribute.getOffset().toString());
        logger.info("scale with multiplier {} and offset {}", multiplier, offset);
        for (CleanInterval currentInt : intervals) {
            for (JEVisSample sample : currentInt.getTmpSamples()) {
                try {
                    Double rawValue = sample.getValueAsDouble();
                    if (rawValue != null) {
                        BigDecimal rawValueDec = new BigDecimal(rawValue.toString());
                        BigDecimal productDec = new BigDecimal(0);
                        productDec = productDec.add(rawValueDec);
                        productDec = productDec.multiply(multiplier);
                        productDec = productDec.add(offset);
                        sample.setValue(productDec.doubleValue());
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
