/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.limits;

import org.jevis.jedataprocessor.data.CleanInterval;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LimitBreak {

    private List<CleanInterval> intervals = new ArrayList<>();
    private Double lastValue;
    private Double firstValue;
    private MinOrMax minOrMax;
    private Double min;
    private Double max;

    public LimitBreak(Double min, Double max) {
        this.min = min;
        this.max = max;
    }

    public void addInterval(CleanInterval currentInterval) {
        intervals.add(currentInterval);
    }

    public List<CleanInterval> getIntervals() {
        return intervals;
    }

    public Double getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(Double firstValue) {
        this.firstValue = firstValue;
    }

    public Double getLastValue() {
        return lastValue;
    }

    public void setLastValue(Double lastValue) {
        this.lastValue = lastValue;
    }

    public MinOrMax getMinOrMax() {
        return minOrMax;
    }

    public void setMinOrMax(MinOrMax choice) {
        this.minOrMax = choice;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }
}
