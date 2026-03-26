package org.jevis.commons.dataprocessing.processor.workflow;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ResourceManager}.
 */
public class ResourceManagerTest {

    private ResourceManager rm;

    @Before
    public void setUp() {
        rm = new ResourceManager();
    }

    @Test
    public void testDefault_isClean() {
        assertTrue("Default processing type should be CLEAN", rm.isClean());
        assertFalse(rm.isForecast());
        assertFalse(rm.isMath());
    }

    @Test
    public void testSetForecast() {
        rm.setProcessingType(ProcessingType.FORECAST);

        assertFalse(rm.isClean());
        assertTrue(rm.isForecast());
        assertFalse(rm.isMath());
    }

    @Test
    public void testSetMath() {
        rm.setProcessingType(ProcessingType.MATH);

        assertFalse(rm.isClean());
        assertFalse(rm.isForecast());
        assertTrue(rm.isMath());
    }

    @Test
    public void testSetCleanExplicitly() {
        rm.setProcessingType(ProcessingType.FORECAST);
        rm.setProcessingType(ProcessingType.CLEAN);

        assertTrue(rm.isClean());
    }

    @Test
    public void testIntervalsStartsAsEmptyList() {
        assertNotNull(rm.getIntervals());
        assertTrue(rm.getIntervals().isEmpty());
    }

    @Test
    public void testSetIntervals_null_thenGetReturnsNull() {
        rm.setIntervals(null);
        assertNull(rm.getIntervals());
    }

    @Test
    public void testSetIntervals_list() {
        rm.setIntervals(new ArrayList<>());
        assertNotNull(rm.getIntervals());
        assertTrue(rm.getIntervals().isEmpty());
    }

    @Test
    public void testSampleCacheReset() {
        rm.setSampleCache(new ArrayList<>());
        rm.setSampleCache(null);
        // getSampleCache() will attempt DB access if not null — just verify setter/getter work
        // without triggering the lazy-load (cleanDataObject is null → returns null)
        assertNull(rm.getSampleCache());
    }

    @Test
    public void testGetID_withoutCleanDataObject_returnsMinusOne() {
        assertEquals(-1L, (long) rm.getID());
    }
}
