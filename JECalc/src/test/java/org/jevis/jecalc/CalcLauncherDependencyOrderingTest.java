package org.jevis.jecalc;

import org.jevis.api.*;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.cli.AbstractCliApp;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fix 2 (dependency-aware ordering in JECalc): when Calc A's output feeds Calc B's input and both
 * are due in the same cycle, {@code CalcLauncher.buildDependencyLevels} must place A in an earlier
 * level than B, so {@code executeCalcJobs} runs A to completion before B starts and B sees A's
 * freshly written output within the same pass.
 */
public class CalcLauncherDependencyOrderingTest {

    @Test
    public void producerIsOrderedBeforeConsumer() throws Exception {
        JEVisDataSource ds = mock(JEVisDataSource.class);

        JEVisClass outputClass = mock(JEVisClass.class);
        when(ds.getJEVisClass(CalcJobFactory.Calculation.OUTPUT_DATA.getName())).thenReturn(outputClass);
        JEVisClass inputClass = mock(JEVisClass.class);
        when(ds.getJEVisClass(CalcJobFactory.Calculation.INPUT.getName())).thenReturn(inputClass);

        // Shared object A's output writes to: object 100, attribute "Value".
        JEVisObject sharedObject = mock(JEVisObject.class);
        when(ds.getObject(100L)).thenReturn(sharedObject);
        JEVisAttribute sharedValueAttribute = mock(JEVisAttribute.class);
        when(sharedObject.getAttribute("Value")).thenReturn(sharedValueAttribute);
        when(sharedValueAttribute.getObject()).thenReturn(sharedObject);
        when(sharedObject.getID()).thenReturn(100L);

        // --- Calc A: produces the shared object's "Value" attribute, no inputs ---
        JEVisObject calcA = mock(JEVisObject.class);
        when(calcA.getID()).thenReturn(1L);
        when(calcA.getDataSource()).thenReturn(ds);

        JEVisObject outputChildA = mock(JEVisObject.class);
        when(calcA.getChildren(outputClass, false)).thenReturn(Collections.singletonList(outputChildA));
        when(outputChildA.getDataSource()).thenReturn(ds);
        JEVisAttribute outputTargetAttrA = mock(JEVisAttribute.class);
        when(outputChildA.getAttribute(CalcJobFactory.Calculation.OUTPUT_DATA.getName())).thenReturn(outputTargetAttrA);
        JEVisSample outputTargetSampleA = mock(JEVisSample.class);
        when(outputTargetSampleA.getValueAsString()).thenReturn("100:Value");
        when(outputTargetAttrA.getLatestSample()).thenReturn(outputTargetSampleA);

        // --- Calc B: consumes the shared object's "Value" attribute as input, no outputs ---
        JEVisObject calcB = mock(JEVisObject.class);
        when(calcB.getID()).thenReturn(2L);
        when(calcB.getDataSource()).thenReturn(ds);

        JEVisObject inputChildB = mock(JEVisObject.class);
        when(calcB.getChildren(inputClass, false)).thenReturn(Collections.singletonList(inputChildB));
        JEVisAttribute inputDataTargetAttrB = mock(JEVisAttribute.class);
        when(inputChildB.getAttribute(CalcJobFactory.Calculation.INPUT_DATA.getName())).thenReturn(inputDataTargetAttrB);
        JEVisSample inputDataTargetSampleB = mock(JEVisSample.class);
        when(inputDataTargetSampleB.getValueAsString()).thenReturn("100:Value");
        when(inputDataTargetAttrB.getLatestSample()).thenReturn(inputDataTargetSampleB);

        CalcLauncher launcher = new CalcLauncher(new String[]{}, "test");
        Field dsField = AbstractCliApp.class.getDeclaredField("ds");
        dsField.setAccessible(true);
        dsField.set(launcher, ds);

        Method buildDependencyLevels = CalcLauncher.class.getDeclaredMethod("buildDependencyLevels", List.class);
        buildDependencyLevels.setAccessible(true);

        // Pass B before A in the input list - ordering must come from the dependency graph,
        // not from list order.
        @SuppressWarnings("unchecked")
        List<List<JEVisObject>> levels = (List<List<JEVisObject>>) buildDependencyLevels.invoke(launcher, Arrays.asList(calcB, calcA));

        assertEquals("Producer and consumer must be in separate levels", 2, levels.size());
        assertEquals(1, levels.get(0).size());
        assertSame("Calc A (the producer) must be in the first level", calcA, levels.get(0).get(0));
        assertEquals(1, levels.get(1).size());
        assertSame("Calc B (the consumer) must be in the second level", calcB, levels.get(1).get(0));
    }
}
