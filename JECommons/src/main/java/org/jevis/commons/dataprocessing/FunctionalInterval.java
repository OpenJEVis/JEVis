package org.jevis.commons.dataprocessing;

import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class FunctionalInterval {
    final Interval interval;
    List<JEVisSample> samples = new ArrayList<>();

    public FunctionalInterval(DateTime startDate, DateTime endDate) {
        this.interval = new Interval(startDate, endDate);
    }

    public List<JEVisSample> getSamples() {
        return samples;
    }

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }

    public Interval getInterval() {
        return interval;
    }
}
