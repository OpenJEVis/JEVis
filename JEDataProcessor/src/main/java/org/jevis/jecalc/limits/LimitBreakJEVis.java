/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.limits;

import org.jevis.jecalc.data.CleanInterval;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LimitBreakJEVis implements LimitBreak {

    private List<CleanInterval> intervals = new ArrayList<>();
    private Double lastValue;
    private Double firstValue;
    private MinOrMax minOrMax;

    @Override
    public void addInterval(CleanInterval currentInterval) {
        intervals.add(currentInterval);
    }

    @Override
    public List<CleanInterval> getIntervals() {
        return intervals;
    }

    @Override
    public Double getFirstValue() {
        return firstValue;
    }

    @Override
    public void setFirstValue(Double firstValue) {
        this.firstValue = firstValue;
    }

    @Override
    public Double getLastValue() {
        return lastValue;
    }

    @Override
    public void setLastValue(Double lastValue) {
        this.lastValue = lastValue;
    }

    @Override
    public MinOrMax getMinOrMax() {
        return minOrMax;
    }

    @Override
    public void setMinOrMax(MinOrMax choice) {
        this.minOrMax = choice;
    }

}
