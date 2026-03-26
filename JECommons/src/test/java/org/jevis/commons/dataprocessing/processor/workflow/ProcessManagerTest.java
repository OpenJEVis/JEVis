package org.jevis.commons.dataprocessing.processor.workflow;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ProcessingType} logic (replaces the former three-boolean
 * state in {@link ProcessManager}).
 *
 * <p>Full {@link ProcessManager} construction requires a live JEVis data source;
 * these tests exercise the {@link ProcessingType} enum and the
 * {@link ResourceManager} type-flag methods that depend on it.</p>
 */
public class ProcessManagerTest {

    @Test
    public void testProcessingType_cleanIsDefault() {
        ResourceManager rm = new ResourceManager();
        assertTrue(rm.isClean());
        assertFalse(rm.isForecast());
        assertFalse(rm.isMath());
    }

    @Test
    public void testProcessingType_forecast() {
        ResourceManager rm = new ResourceManager();
        rm.setProcessingType(ProcessingType.FORECAST);

        assertFalse(rm.isClean());
        assertTrue(rm.isForecast());
        assertFalse(rm.isMath());
    }

    @Test
    public void testProcessingType_math() {
        ResourceManager rm = new ResourceManager();
        rm.setProcessingType(ProcessingType.MATH);

        assertFalse(rm.isClean());
        assertFalse(rm.isForecast());
        assertTrue(rm.isMath());
    }

    @Test
    public void testProcessingType_switchBack() {
        ResourceManager rm = new ResourceManager();
        rm.setProcessingType(ProcessingType.MATH);
        rm.setProcessingType(ProcessingType.CLEAN);

        assertTrue(rm.isClean());
        assertFalse(rm.isMath());
    }

    @Test
    public void testAllProcessingTypesDistinct() {
        assertNotEquals(ProcessingType.CLEAN, ProcessingType.FORECAST);
        assertNotEquals(ProcessingType.CLEAN, ProcessingType.MATH);
        assertNotEquals(ProcessingType.FORECAST, ProcessingType.MATH);
    }

    @Test
    public void testIntervalsNullAfterReset() {
        ResourceManager rm = new ResourceManager();
        rm.setIntervals(null);
        assertNull(rm.getIntervals());
    }
}
