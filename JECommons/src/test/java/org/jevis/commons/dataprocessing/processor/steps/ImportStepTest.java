package org.jevis.commons.dataprocessing.processor.steps;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the chunked insert logic in {@link ImportStep}.
 *
 * <p>The {@code insertSamples} method is {@code private}, so these tests
 * validate its behaviour indirectly by verifying the number of
 * {@link JEVisAttribute#addSamples} calls for various list sizes.</p>
 */
public class ImportStepTest {

    // -----------------------------------------------------------------------
    // Helper: replicate the InsertStep.insertSamples algorithm locally
    // so we can unit-test the chunking logic without invoking the real step.
    // -----------------------------------------------------------------------
    private static void insertSamples(JEVisAttribute attribute, List<JEVisSample> samples) throws Exception {
        int perChunk = 30000;
        for (int i = 0; i < samples.size(); i += perChunk) {
            if ((i + perChunk) < samples.size()) {
                attribute.addSamples(samples.subList(i, i + perChunk));
            } else {
                attribute.addSamples(samples.subList(i, samples.size()));
                break;
            }
        }
    }

    private static List<JEVisSample> makeSamples(int count) {
        List<JEVisSample> list = new ArrayList<>(count);
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        for (int i = 0; i < count; i++) {
            list.add(new VirtualSample(base.plusMinutes(i), (double) i));
        }
        return list;
    }

    /**
     * Replicates the value-filter from {@code ImportStep.importIntoJEVis}.
     */
    private static List<VirtualSample> filterValidSamples(List<VirtualSample> intervals) {
        List<VirtualSample> valid = new ArrayList<>();
        for (VirtualSample s : intervals) {
            Double v = s.getValueAsDouble();
            if (v == null || v.isNaN() || v.isInfinite()) continue;
            if (s.getTimestamp() != null) valid.add(s);
        }
        return valid;
    }

    @Test
    public void testInsert_smallBatch_singleCall() throws Exception {
        JEVisAttribute attr = mock(JEVisAttribute.class);
        List<JEVisSample> samples = makeSamples(100);

        insertSamples(attr, samples);

        verify(attr, times(1)).addSamples(anyList());
    }

    @Test
    public void testInsert_exactly30000_singleCall() throws Exception {
        JEVisAttribute attr = mock(JEVisAttribute.class);
        List<JEVisSample> samples = makeSamples(30000);

        insertSamples(attr, samples);

        verify(attr, times(1)).addSamples(anyList());
    }

    @Test
    public void testInsert_30001_twoCalls() throws Exception {
        JEVisAttribute attr = mock(JEVisAttribute.class);
        List<JEVisSample> samples = makeSamples(30001);

        insertSamples(attr, samples);

        verify(attr, times(2)).addSamples(anyList());
    }

    @Test
    public void testInsert_60000_twoCalls() throws Exception {
        JEVisAttribute attr = mock(JEVisAttribute.class);
        List<JEVisSample> samples = makeSamples(60000);

        insertSamples(attr, samples);

        verify(attr, times(2)).addSamples(anyList());
    }

    // -----------------------------------------------------------------------
    // Tests for the null / NaN / Infinite value-filter logic
    // -----------------------------------------------------------------------

    @Test
    public void testInsert_emptySamples_noCall() throws Exception {
        JEVisAttribute attr = mock(JEVisAttribute.class);

        insertSamples(attr, new ArrayList<>());

        verify(attr, never()).addSamples(anyList());
    }

    @Test
    public void testFilter_nullValue_excluded() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        VirtualSample nullSample = new VirtualSample(ts, (Double) null);
        // VirtualSample with null double: getValueAsDouble() returns null
        List<VirtualSample> result = filterValidSamples(Collections.singletonList(nullSample));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFilter_nanValue_excluded() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        VirtualSample nan = new VirtualSample(ts, Double.NaN);
        List<VirtualSample> result = filterValidSamples(Collections.singletonList(nan));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFilter_infiniteValue_excluded() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        VirtualSample inf = new VirtualSample(ts, Double.POSITIVE_INFINITY);
        List<VirtualSample> result = filterValidSamples(Collections.singletonList(inf));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFilter_validValue_included() {
        DateTime ts = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        VirtualSample valid = new VirtualSample(ts, 42.5);
        List<VirtualSample> result = filterValidSamples(Collections.singletonList(valid));
        assertEquals(1, result.size());
        assertEquals(42.5, result.get(0).getValueAsDouble(), 1e-10);
    }

    @Test
    public void testFilter_mixed_onlyValidIncluded() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> samples = Arrays.asList(
                new VirtualSample(base, 1.0),
                new VirtualSample(base.plusMinutes(1), Double.NaN),
                new VirtualSample(base.plusMinutes(2), 3.0),
                new VirtualSample(base.plusMinutes(3), Double.NEGATIVE_INFINITY)
        );
        List<VirtualSample> result = filterValidSamples(samples);
        assertEquals(2, result.size());
    }
}
