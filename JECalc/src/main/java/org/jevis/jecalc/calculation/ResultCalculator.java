/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;

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

        for (Map.Entry<DateTime, List<Sample>> entry : mergedSamples.entrySet()) {
            int numberOfInputs = entry.getValue().size();
            Boolean[] arrayAllZero = new Boolean[numberOfInputs];

            for (Sample sample : entry.getValue()) {
                arrayAllZero[entry.getValue().indexOf(sample)] = sample.getValue().equals(0d);
            }

            Boolean allZero = true;
            for (Boolean b : arrayAllZero) {
                if (!b) allZero = b;
            }

            if (!allZero || allZeroReplacementValue == null) {
                for (Sample sample : entry.getValue()) {
                    template.put(sample.getVariable(), sample.getValue());
                }
            } else {
                for (Sample sample : entry.getValue()) {
                    template.put(sample.getVariable(), allZeroReplacementValue);
                }
            }

            Double evaluate = template.evaluate();
            if (Double.isInfinite(evaluate) || Double.isNaN(evaluate)) {
                //TODO implement different handling switch...

                VirtualSample smp = new VirtualSample(entry.getKey(), replacementValue);
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
                resultList.add(new VirtualSample(entry.getKey(), evaluate));
            }
        }
        return resultList;
    }

}
