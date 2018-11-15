/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.data;

import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class CleanInterval {

    private final DateTime date;
    private final Interval interval;
    private final List<JEVisSample> rawSamples = new ArrayList<>();
    private final List<JEVisSample> tmpSamples = new ArrayList<>();

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

    public List<JEVisSample> getTmpSamples() {
        return tmpSamples;
    }

    public void addTmpSample(JEVisSample sample) {
        tmpSamples.add(sample);
    }
}
