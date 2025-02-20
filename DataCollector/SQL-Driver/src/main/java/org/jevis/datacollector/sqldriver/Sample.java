package org.jevis.datacollector.sqldriver;

import org.jevis.api.JEVisSample;

public class Sample {

    private final JEVisSample jeVisSample;
    private final SampleStatus sampleStatus;

    public Sample(JEVisSample jeVisSample, SampleStatus sampleStatus) {
        this.jeVisSample = jeVisSample;
        this.sampleStatus = sampleStatus;
    }

    public JEVisSample getJeVisSample() {
        return jeVisSample;
    }

    public SampleStatus getSampleStatus() {
        return sampleStatus;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "jeVisSample=" + jeVisSample +
                ", sampleStatus=" + sampleStatus +
                '}';
    }
}
