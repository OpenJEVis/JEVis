/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.gap;

import org.jevis.jedataprocessor.data.CleanInterval;

import java.util.ArrayList;
import java.util.List;


/**
 * @author broder
 */
public class Gap {

    private List<CleanInterval> intervals = new ArrayList<>();
    private Double lastValue;
    private Double firstValue;

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

}
