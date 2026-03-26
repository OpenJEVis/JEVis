package org.jevis.jecalc;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Smoke tests for CalcLauncher.
 */
public class CalcLauncherTest {

    @Test
    public void testClassLoads() {
        assertNotNull(CalcLauncher.class);
    }
}
