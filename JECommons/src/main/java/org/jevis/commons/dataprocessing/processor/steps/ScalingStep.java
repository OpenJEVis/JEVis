/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author broder
 */
public class ScalingStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(ScalingStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject calcAttribute = resourceManager.getCleanDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listMultipliers = calcAttribute.getMultiplier();

        for (JEVisSample multiplier : listMultipliers) {
            int index = listMultipliers.indexOf(multiplier);
            DateTime timeStampOfMultiplier = null;
            DateTime nextTimeStampOfMultiplier = null;
            Double multiplierDouble = null;
            BigDecimal offset = new BigDecimal(calcAttribute.getOffset().toString());

            timeStampOfMultiplier = multiplier.getTimestamp();
            multiplierDouble = multiplier.getValueAsDouble();

            if (index + 1 < listMultipliers.size()) {
                nextTimeStampOfMultiplier = listMultipliers.get(index + 1).getTimestamp();
            }

            logger.info("[{}] scale with multiplier {} and offset {} starting at: {}", calcAttribute.getCleanObject().getID(), multiplierDouble, offset, timeStampOfMultiplier);
            for (CleanInterval currentInt : intervals) {
                if (currentInt.getDate().equals(timeStampOfMultiplier) || currentInt.getDate().isAfter(timeStampOfMultiplier) && ((nextTimeStampOfMultiplier == null) || currentInt.getDate().isBefore(nextTimeStampOfMultiplier))) {
                    BigDecimal multi = new BigDecimal(multiplierDouble.toString());
                    VirtualSample sample = currentInt.getResult();
                    Double rawValue = sample.getValueAsDouble();
                    if (rawValue != null) {
                        BigDecimal rawValueDec = new BigDecimal(rawValue.toString());
                        BigDecimal productDec = new BigDecimal(0);
                        productDec = productDec.add(rawValueDec);
                        productDec = productDec.multiply(multi);
                        productDec = productDec.add(offset);
                        sample.setValue(productDec.doubleValue());
                        String note = sample.getNote();
                        note += ",scale(" + multi + ")";
                        sample.setNote(note);
                    }
                }
            }
        }
    }

}
