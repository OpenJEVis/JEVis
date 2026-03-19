package org.jevis.commons.dataprocessing.processor.workflow;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one output time slot in the processing pipeline.
 *
 * <p>A {@code CleanInterval} bundles everything the pipeline needs to compute
 * a single output sample:</p>
 * <ul>
 *   <li>{@link #interval} — the half-open time window [{@code start}, {@code end})
 *       from which raw source samples are collected.</li>
 *   <li>{@link #date} — the exact output timestamp that will be written to the
 *       clean/forecast/math attribute (may differ from {@code interval.start}).</li>
 *   <li>{@link #rawSamples} — the raw source samples that fall inside the
 *       interval, populated by the alignment and preparation steps.</li>
 *   <li>{@link #result} — the {@link VirtualSample} whose value is computed
 *       and eventually persisted by {@link org.jevis.commons.dataprocessing.processor.steps.ImportStep}.</li>
 * </ul>
 *
 * <p>Two {@code CleanInterval} objects are considered equal if their
 * {@link #date} timestamps are equal.</p>
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

    /**
     * @param interval      the time window used to gather raw samples
     * @param exactDateTime the timestamp to assign to the computed output sample
     */
    public CleanInterval(Interval interval, DateTime exactDateTime) {
        this.interval = interval;
        this.date = exactDateTime;
    }

    /**
     * Returns the exact output timestamp for the computed result sample.
     */
    public DateTime getDate() {
        return date;
    }

    /** Returns the time window used to select raw input samples. */
    public Interval getInterval() {
        return interval;
    }

    /** Returns the mutable list of raw samples that fall within {@link #getInterval()}. */
    public List<JEVisSample> getRawSamples() {
        return rawSamples;
    }

    public void addRawSample(JEVisSample rawSample) {
        rawSamples.add(rawSample);
    }

    /** Returns the in-memory result sample whose value will be persisted by {@code ImportStep}. */
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CleanInterval) {
            CleanInterval cleanInterval = (CleanInterval) obj;
            return this.getDate().equals(cleanInterval.getDate());
        }

        return false;
    }
}
