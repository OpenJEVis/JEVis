/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.gap;

import org.jevis.jecalc.data.CleanInterval;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author broder
 */
public class GapJEVis implements Gap {

    private List<CleanInterval> intervals = new ArrayList<>();
    private Double lastValue;
    private Double firstValue;

    @Override
    public void addInterval(CleanInterval currentInterval) {
        intervals.add(currentInterval);
    }

    @Override
    public void setLastValue(Double lastValue) {
        this.lastValue = lastValue;
    }

    @Override
    public void setFirstValue(Double firstValue) {
        this.firstValue = firstValue;
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
    public Double getLastValue() {
        return lastValue;
    }

}
