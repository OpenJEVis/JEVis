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

    public static BigDecimal getCurrentMultiplier(List<JEVisSample> listMultipliers, DateTime date) {
        BigDecimal multi = new BigDecimal(1);
        try {
            for (JEVisSample multiplier : listMultipliers) {
                int index = listMultipliers.indexOf(multiplier);
                DateTime timeStampOfMultiplier = null;
                DateTime nextTimeStampOfMultiplier = null;
                Double multiplierDouble = null;

                timeStampOfMultiplier = multiplier.getTimestamp();
                multiplierDouble = multiplier.getValueAsDouble();

                if (index + 1 < listMultipliers.size()) {
                    nextTimeStampOfMultiplier = listMultipliers.get(index + 1).getTimestamp();
                }
                if (date.equals(timeStampOfMultiplier) || date.isAfter(timeStampOfMultiplier) && ((nextTimeStampOfMultiplier == null) || date.isBefore(nextTimeStampOfMultiplier))) {
                    multi = new BigDecimal(multiplierDouble.toString());
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not get multiplier for {}", date, e);
        }
        return multi;
    }

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject calcAttribute = resourceManager.getCleanDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listMultipliers = calcAttribute.getMultiplier();
        BigDecimal offset = new BigDecimal(calcAttribute.getOffset().toString());

        for (CleanInterval currentInt : intervals) {
            BigDecimal currentMulti = getCurrentMultiplier(listMultipliers, currentInt.getDate());

            VirtualSample sample = currentInt.getResult();
            Double rawValue = sample.getValueAsDouble();
            if (rawValue != null) {
                BigDecimal rawValueDec = new BigDecimal(rawValue.toString());
                BigDecimal productDec = new BigDecimal(0);
                productDec = productDec.add(rawValueDec);
                productDec = productDec.multiply(currentMulti);
                productDec = productDec.add(offset);
                sample.setValue(productDec.doubleValue());
                String note = sample.getNote();
                note += ",scale(" + currentMulti + ")";
                sample.setNote(note);
            }
        }
    }

}
