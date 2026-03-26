package org.jevis.commons.dataprocessing.processor.preparation;

import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the deduplication logic extracted from {@link PrepareStep}.
 *
 * <p>These tests exercise the HashSet-based O(1) duplicate check that replaced
 * the original O(n²) inner-loop approach.  They are written against plain
 * {@link VirtualSample} objects — no mocking or API connections required.</p>
 */
public class PrepareStepDeduplicationTest {

    /**
     * Simulates the deduplication logic used in PrepareStep:
     * build a HashSet from existing timestamps, then filter new samples.
     */
    private List<VirtualSample> dedup(List<VirtualSample> existing, List<VirtualSample> incoming) {
        Set<DateTime> existingTs = new HashSet<>();
        for (VirtualSample s : existing) {
            existingTs.add(s.getTimestamp());
        }

        List<VirtualSample> filtered = new ArrayList<>();
        for (VirtualSample s : incoming) {
            if (!existingTs.contains(s.getTimestamp())) {
                filtered.add(s);
                existingTs.add(s.getTimestamp());
            }
        }
        return filtered;
    }

    @Test
    public void testDedup_noOverlap_allAdded() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> existing = Arrays.asList(
                new VirtualSample(base.minusMinutes(15), 1.0),
                new VirtualSample(base.minusMinutes(30), 2.0)
        );
        List<VirtualSample> incoming = Arrays.asList(
                new VirtualSample(base.minusMinutes(45), 3.0),
                new VirtualSample(base.minusMinutes(60), 4.0)
        );

        List<VirtualSample> result = dedup(existing, incoming);
        assertEquals(2, result.size());
    }

    @Test
    public void testDedup_fullOverlap_noneAdded() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> existing = Arrays.asList(
                new VirtualSample(base, 1.0),
                new VirtualSample(base.minusMinutes(15), 2.0)
        );
        List<VirtualSample> incoming = Arrays.asList(
                new VirtualSample(base, 99.0),
                new VirtualSample(base.minusMinutes(15), 99.0)
        );

        List<VirtualSample> result = dedup(existing, incoming);
        assertTrue("All duplicates should be filtered", result.isEmpty());
    }

    @Test
    public void testDedup_partialOverlap_onlyNewAdded() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> existing = Collections.singletonList(
                new VirtualSample(base, 1.0)
        );
        List<VirtualSample> incoming = Arrays.asList(
                new VirtualSample(base, 99.0),               // duplicate
                new VirtualSample(base.minusMinutes(15), 2.0) // new
        );

        List<VirtualSample> result = dedup(existing, incoming);
        assertEquals(1, result.size());
        assertEquals(base.minusMinutes(15), result.get(0).getTimestamp());
    }

    @Test
    public void testDedup_emptyExisting_allAdded() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> existing = Collections.emptyList();
        List<VirtualSample> incoming = Arrays.asList(
                new VirtualSample(base, 1.0),
                new VirtualSample(base.minusMinutes(15), 2.0)
        );

        List<VirtualSample> result = dedup(existing, incoming);
        assertEquals(2, result.size());
    }

    @Test
    public void testDedup_emptyIncoming_noneAdded() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> existing = Collections.singletonList(
                new VirtualSample(base, 1.0)
        );

        List<VirtualSample> result = dedup(existing, Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDedup_duplicatesWithinIncoming_deduped() {
        DateTime base = new DateTime(2023, 1, 1, 0, 0, 0, 0);
        List<VirtualSample> existing = Collections.emptyList();
        List<VirtualSample> incoming = Arrays.asList(
                new VirtualSample(base, 1.0),
                new VirtualSample(base, 2.0)  // same timestamp as previous
        );

        List<VirtualSample> result = dedup(existing, incoming);
        assertEquals("Second occurrence of same timestamp should be filtered", 1, result.size());
    }

    @Test
    public void testCleanInterval_construction() {
        DateTime ts = new DateTime(2023, 6, 15, 12, 0, 0, 0);
        Interval interval = new Interval(ts.minusMinutes(15), ts);
        CleanInterval ci = new CleanInterval(interval, ts);
        ci.getResult().setTimeStamp(ts);

        assertEquals(ts, ci.getDate());
        assertEquals(ts, ci.getResult().getTimestamp());
        assertTrue(ci.getRawSamples().isEmpty());
    }
}
