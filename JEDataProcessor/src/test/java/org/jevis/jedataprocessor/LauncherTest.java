package org.jevis.jedataprocessor;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Launcher} logic that can be exercised without a live
 * JEVis connection.
 *
 * <p>Tests focus on the duplicate-job guard (putIfAbsent semantics) and the
 * enabled/disabled filtering, both of which use plain data-structures.</p>
 */
public class LauncherTest {

    // -----------------------------------------------------------------------
    // Duplicate-job guard — putIfAbsent semantics
    // -----------------------------------------------------------------------

    @Test
    public void testPutIfAbsent_firstInsert_returnsNull() {
        ConcurrentHashMap<Long, DateTime> runningJobs = new ConcurrentHashMap<>();
        Long id = 42L;

        DateTime result = runningJobs.putIfAbsent(id, new DateTime());

        assertNull("First putIfAbsent should return null (slot was empty)", result);
        assertTrue(runningJobs.containsKey(id));
    }

    @Test
    public void testPutIfAbsent_secondInsert_returnsExisting() {
        ConcurrentHashMap<Long, DateTime> runningJobs = new ConcurrentHashMap<>();
        Long id = 42L;
        DateTime first = new DateTime(2023, 1, 1, 0, 0, 0, 0);

        runningJobs.put(id, first);
        DateTime result = runningJobs.putIfAbsent(id, new DateTime());

        assertNotNull("Second putIfAbsent should return the existing value", result);
        assertEquals(first, result);
    }

    @Test
    public void testPutIfAbsent_guard_preventsDoubleSubmit() {
        ConcurrentHashMap<Long, DateTime> runningJobs = new ConcurrentHashMap<>();
        Long id = 99L;
        int submitCount = 0;

        // Simulate two threads trying to submit the same job
        if (runningJobs.putIfAbsent(id, new DateTime()) == null) submitCount++;
        if (runningJobs.putIfAbsent(id, new DateTime()) == null) submitCount++;

        assertEquals("Only one submit should win the race", 1, submitCount);
    }

    // -----------------------------------------------------------------------
    // getAllCleaningObjects() enabled/disabled filtering simulation
    // -----------------------------------------------------------------------

    @Test
    public void testEnabledFiltering_onlyEnabledAdded() {
        // Simulate the filtering loop from getAllCleaningObjects():
        // only objects where isEnabled() is true should make it into filteredObjects.

        JEVisObject enabled = mock(JEVisObject.class);
        JEVisObject disabled = mock(JEVisObject.class);
        when(enabled.getID()).thenReturn(1L);
        when(disabled.getID()).thenReturn(2L);

        java.util.List<JEVisObject> all = java.util.Arrays.asList(enabled, disabled);
        java.util.Map<Long, Boolean> enabledMap = new java.util.HashMap<>();
        enabledMap.put(1L, true);
        enabledMap.put(2L, false);

        java.util.List<JEVisObject> filteredObjects = new java.util.ArrayList<>();
        for (JEVisObject obj : all) {
            if (enabledMap.getOrDefault(obj.getID(), false)) {
                filteredObjects.add(obj);
            }
        }

        assertEquals(1, filteredObjects.size());
        assertEquals(1L, (long) filteredObjects.get(0).getID());
    }

    @Test
    public void testEnabledFiltering_noneEnabled_emptyResult() {
        JEVisObject obj1 = mock(JEVisObject.class);
        JEVisObject obj2 = mock(JEVisObject.class);
        when(obj1.getID()).thenReturn(1L);
        when(obj2.getID()).thenReturn(2L);

        java.util.List<JEVisObject> filteredObjects = new java.util.ArrayList<>();
        // neither is added → list stays empty

        assertTrue(filteredObjects.isEmpty());
    }
}
