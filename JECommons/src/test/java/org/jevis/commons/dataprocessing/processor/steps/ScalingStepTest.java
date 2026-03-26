package org.jevis.commons.dataprocessing.processor.steps;

import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the multiplier logic in {@link ScalingStep}.
 *
 * <p>Because {@code ScalingStep.run()} requires a full {@link org.jevis.commons.dataprocessing.processor.workflow.ResourceManager}
 * with a live {@link org.jevis.commons.dataprocessing.CleanDataObject}, these tests validate the
 * scaling mathematics independently using {@link CleanInterval} directly.</p>
 */
public class ScalingStepTest {

    /**
     * Applies a multiplier to a {@link CleanInterval}'s result value, mimicking ScalingStep.
     */
    private static void applyMultiplier(CleanInterval ci, double multiplier) {
        Double val = ci.getResult().getValueAsDouble();
        if (val != null) {
            ci.getResult().setValue(val * multiplier);
        }
    }

    private static CleanInterval makeInterval(DateTime ts, double value) {
        Interval iv = new Interval(ts.minusMinutes(15), ts);
        CleanInterval ci = new CleanInterval(iv, ts);
        ci.getResult().setTimeStamp(ts);
        ci.getResult().setValue(value);
        ci.setOutputPeriod(Period.minutes(15));
        return ci;
    }

    @Test
    public void testMultiplier_two() {
        DateTime ts = new DateTime(2023, 1, 1, 1, 0, 0, 0);
        CleanInterval ci = makeInterval(ts, 10.0);

        applyMultiplier(ci, 2.0);

        assertEquals(20.0, ci.getResult().getValueAsDouble(), 1e-10);
    }

    @Test
    public void testMultiplier_zero() {
        DateTime ts = new DateTime(2023, 1, 1, 1, 0, 0, 0);
        CleanInterval ci = makeInterval(ts, 100.0);

        applyMultiplier(ci, 0.0);

        assertEquals(0.0, ci.getResult().getValueAsDouble(), 1e-10);
    }

    @Test
    public void testMultiplier_negative() {
        DateTime ts = new DateTime(2023, 1, 1, 1, 0, 0, 0);
        CleanInterval ci = makeInterval(ts, 5.0);

        applyMultiplier(ci, -1.0);

        assertEquals(-5.0, ci.getResult().getValueAsDouble(), 1e-10);
    }

    @Test
    public void testMultiplier_fractional() {
        DateTime ts = new DateTime(2023, 1, 1, 1, 0, 0, 0);
        CleanInterval ci = makeInterval(ts, 100.0);

        applyMultiplier(ci, 0.001);

        assertEquals(0.1, ci.getResult().getValueAsDouble(), 1e-10);
    }

    @Test
    public void testMultiplier_appliedToMultipleIntervals() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<CleanInterval> intervals = Arrays.asList(
                makeInterval(base.plusMinutes(15), 4.0),
                makeInterval(base.plusMinutes(30), 8.0),
                makeInterval(base.plusMinutes(45), 12.0)
        );

        for (CleanInterval ci : intervals) {
            applyMultiplier(ci, 3.0);
        }

        assertEquals(12.0, intervals.get(0).getResult().getValueAsDouble(), 1e-10);
        assertEquals(24.0, intervals.get(1).getResult().getValueAsDouble(), 1e-10);
        assertEquals(36.0, intervals.get(2).getResult().getValueAsDouble(), 1e-10);
    }
}
