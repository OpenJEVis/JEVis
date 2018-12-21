/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.scaling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jedataprocessor.data.CleanDataAttribute;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
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
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listMultipliers = calcAttribute.getMultiplier();

        for (JEVisSample multiplier : listMultipliers) {

            DateTime timeStampOfMultiplier = null;
            DateTime nextTimeStampOfMultiplier = null;
            Double multiplierDouble = null;
            BigDecimal offset = new BigDecimal(calcAttribute.getOffset().toString());
            try {
                timeStampOfMultiplier = multiplier.getTimestamp();
                multiplierDouble = multiplier.getValueAsDouble();
                try {
                    nextTimeStampOfMultiplier = (listMultipliers.get(listMultipliers.indexOf(multiplier) + 1)).getTimestamp();
                } catch (Exception ignored) {

                }
            } catch (JEVisException e) {
                throw new Exception("no timestamp for multiplier", e);
            }
            logger.info("[{}] scale with multiplier {} and offset {} starting at: {}", calcAttribute.getObject().getID(), multiplierDouble, offset, timeStampOfMultiplier);
            for (CleanInterval currentInt : intervals) {
                if (currentInt.getDate().isAfter(timeStampOfMultiplier) && ((nextTimeStampOfMultiplier == null) || currentInt.getDate().isBefore(nextTimeStampOfMultiplier))) {
                    BigDecimal multi = new BigDecimal(multiplierDouble.toString());
                    for (JEVisSample sample : currentInt.getTmpSamples()) {
                        Double rawValue = sample.getValueAsDouble();
                        if (rawValue != null) {
                            BigDecimal rawValueDec = new BigDecimal(rawValue.toString());
                            BigDecimal productDec = new BigDecimal(0);
                            productDec = productDec.add(rawValueDec);
                            productDec = productDec.multiply(multi);
                            productDec = productDec.add(offset);
                            sample.setValue(productDec.doubleValue());
                            String note = sample.getNote();
                            note += ",scale";
                            sample.setNote(note);
                        }
                    }
                }

            }
        }
    }

}
