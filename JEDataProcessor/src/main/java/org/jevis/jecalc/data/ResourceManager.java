/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.data;

import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisSample;

/**
 *
 * @author broder
 */
public class ResourceManager {

    public List<CleanInterval> intervals = new ArrayList<>();
    private CleanDataAttribute calcAttribute;
    private List<JEVisSample> rawSamples;

    public List<CleanInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<CleanInterval> intervals) {
        this.intervals = intervals;
    }

    public void setCalcAttribute(CleanDataAttribute calcAttribute) {
        this.calcAttribute = calcAttribute;
    }

    public CleanDataAttribute getCalcAttribute() {
        return calcAttribute;
    }

    public void setRawSamples(List<JEVisSample> rawSamples) {
        this.rawSamples = rawSamples;
    }

    public List<JEVisSample> getRawSamples() {
        return rawSamples;
    }

}
