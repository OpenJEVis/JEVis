/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
public class ResultCalculator {

    private final CalcTemplate template;
    private final Map<DateTime, List<Sample>> mergedSamples;

    public ResultCalculator(Map<DateTime, List<Sample>> mergedSamples, CalcTemplate template) {
        this.mergedSamples = mergedSamples;
        this.template = template;
    }

    public List<JEVisSample> calculateResult(String div0Handling, Double replacementValue, Double allZeroReplacementValue) {
        List<JEVisSample> resultList = new ArrayList<>();

        mergedSamples.forEach((key, value) -> {
            int numberOfInputs = value.size();
            Boolean[] arrayAllZero = new Boolean[numberOfInputs];

            value.forEach(sample -> arrayAllZero[value.indexOf(sample)] = sample.getValue().equals(new BigDecimal(0)));

            boolean allZero = true;
            for (Boolean b : arrayAllZero) {
                if (!b) {
                    allZero = false;
                    break;
                }
            }

            if (!allZero || allZeroReplacementValue == null) {
                value.forEach(sample -> template.put(sample.getVariable(), sample.getValue()));

                BigDecimal evaluate = template.evaluate();
//                if (Double.isInfinite(evaluate.doubleValue()) || Double.isNaN(evaluate.doubleValue())) {
                if (evaluate == null) {
                    //TODO implement different handling switch...

                    VirtualSample smp = new VirtualSample(key, replacementValue);
                    String note = smp.getNote();

                    if (note == null) {
                        note = "";
                        note += "calc(infinite)";
                    } else {
                        note += ",calc(infinite)";
                    }
                    smp.setNote(note);
                    resultList.add(smp);
                } else {
                    VirtualSample newSample = new VirtualSample(key, evaluate.doubleValue());
                    newSample.setNote("");
                    resultList.add(newSample);
                }

            } else {
                VirtualSample newSample = new VirtualSample(key, allZeroReplacementValue);
                newSample.setNote("");
                resultList.add(newSample);
            }
        });
        return resultList;
    }

}
