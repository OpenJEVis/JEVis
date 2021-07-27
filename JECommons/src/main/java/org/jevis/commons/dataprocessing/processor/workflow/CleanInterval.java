/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.workflow;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gschutz
 */
public class CleanInterval {

    private final DateTime date;
    private final Interval interval;
    private final List<JEVisSample> rawSamples = new ArrayList<>();
    private final VirtualSample result = new VirtualSample();
    private Double multiplier;
    private Boolean differential;
    private Period inputPeriod;
    private Period outputPeriod;
    private Integer compare = 0;

    public CleanInterval(Interval interval, DateTime exactDateTime) {
        this.interval = interval;
        this.date = exactDateTime;
    }

    public DateTime getDate() {
        return date;
    }

    public Interval getInterval() {
        return interval;
    }

    public List<JEVisSample> getRawSamples() {
        return rawSamples;
    }

    public void addRawSample(JEVisSample rawSample) {
        rawSamples.add(rawSample);
    }

    public VirtualSample getResult() {
        return result;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public Boolean isDifferential() {
        return differential;
    }

    public void setDifferential(Boolean differential) {
        this.differential = differential;
    }

    public Period getInputPeriod() {
        return inputPeriod;
    }

    public void setInputPeriod(Period inputPeriod) {
        this.inputPeriod = inputPeriod;
    }

    public Period getOutputPeriod() {
        return outputPeriod;
    }

    public void setOutputPeriod(Period outputPeriod) {
        this.outputPeriod = outputPeriod;
    }

    public Integer getCompare() {
        return compare;
    }

    public void setCompare(Integer compare) {
        this.compare = compare;
    }
}
