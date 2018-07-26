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
 *
 * @author broder
 */
public class ResultCalculator {

    private final CalcTemplate template;
    private final Map<DateTime, List<Sample>> mergedSamples;

    public ResultCalculator(Map<DateTime, List<Sample>> mergedSamples, CalcTemplate template) {
        this.mergedSamples = mergedSamples;
        this.template = template;
    }

    public List<JEVisSample> calculateResult() {
        List<JEVisSample> resultList = new ArrayList<>();
        for (Map.Entry<DateTime, List<Sample>> entry : mergedSamples.entrySet()) {
            for (Sample sample : entry.getValue()) {
                template.put(sample.getVariable(), sample.getValue());
                System.out.println("template.value" + sample.getValue());
            }
            Double evaluate = template.evaluate();
            System.out.println("evaluate: " + evaluate);
            resultList.add(new VirtualSample(entry.getKey(), evaluate));
        }
        return resultList;
    }

}
