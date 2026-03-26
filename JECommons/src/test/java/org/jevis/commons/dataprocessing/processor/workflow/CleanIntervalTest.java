package org.jevis.commons.dataprocessing.processor.workflow;

import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CleanInterval}.
 */
public class CleanIntervalTest {

    @Test
    public void testGetDate_returnsExactDateTime() {
        DateTime ts = new DateTime(2023, 1, 15, 10, 0, 0, 0);
        Interval interval = new Interval(ts.minusMinutes(15), ts);
        CleanInterval ci = new CleanInterval(interval, ts);

        assertEquals(ts, ci.getDate());
    }

    @Test
    public void testGetInterval_returnsConstructedInterval() {
        DateTime start = new DateTime(2023, 1, 15, 9, 45, 0, 0);
        DateTime end = new DateTime(2023, 1, 15, 10, 0, 0, 0);
        Interval interval = new Interval(start, end);
        CleanInterval ci = new CleanInterval(interval, end);

        assertEquals(start, ci.getInterval().getStart());
        assertEquals(end, ci.getInterval().getEnd());
    }

    @Test
    public void testResult_initiallyHasNullValue() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        CleanInterval ci = new CleanInterval(new Interval(ts.minusMinutes(1), ts), ts);

        assertNull(ci.getResult().getValueAsDouble());
    }

    @Test
    public void testResult_setTimestampRoundtrip() {
        DateTime ts = new DateTime(2023, 6, 1, 12, 0, 0, 0);
        CleanInterval ci = new CleanInterval(new Interval(ts.minusMinutes(15), ts), ts);
        ci.getResult().setTimeStamp(ts);

        assertEquals(ts, ci.getResult().getTimestamp());
    }

    @Test
    public void testAddRawSample_incrementsListSize() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        CleanInterval ci = new CleanInterval(new Interval(ts.minusMinutes(1), ts), ts);

        VirtualSample vs = new VirtualSample(ts, 42.0);
        ci.addRawSample(vs);

        assertEquals(1, ci.getRawSamples().size());
    }

    @Test
    public void testEquals_sameDate() {
        DateTime ts = new DateTime(2023, 3, 1, 8, 0, 0, 0);
        CleanInterval a = new CleanInterval(new Interval(ts.minusMinutes(15), ts), ts);
        CleanInterval b = new CleanInterval(new Interval(ts.minusMinutes(30), ts), ts);

        assertEquals(a, b);
    }

    @Test
    public void testEquals_differentDate() {
        DateTime ts1 = new DateTime(2023, 3, 1, 8, 0, 0, 0);
        DateTime ts2 = new DateTime(2023, 3, 1, 9, 0, 0, 0);
        CleanInterval a = new CleanInterval(new Interval(ts1.minusMinutes(15), ts1), ts1);
        CleanInterval b = new CleanInterval(new Interval(ts2.minusMinutes(15), ts2), ts2);

        assertNotEquals(a, b);
    }

    @Test
    public void testPeriodAndMultiplier() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        CleanInterval ci = new CleanInterval(new Interval(ts.minusMinutes(15), ts), ts);
        ci.setInputPeriod(Period.minutes(15));
        ci.setOutputPeriod(Period.hours(1));
        ci.setMultiplier(1.5);

        assertEquals(Period.minutes(15), ci.getInputPeriod());
        assertEquals(Period.hours(1), ci.getOutputPeriod());
        assertEquals(1.5, ci.getMultiplier(), 1e-10);
    }
}
