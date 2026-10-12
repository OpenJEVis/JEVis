package org.jevis.commons.calculation;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression test for e6b4b775e: {@code CalcInputObject.getUserData} previously called
 * {@code map.get(ts)} twice, throwing an NPE-adjacent bug when no original sample existed at a
 * user-value timestamp. The fix caches the lookup into a local variable.
 */
public class CalcInputObjectTest {

    @Test
    public void userValueWithNoOriginalSampleDoesNotThrowAndFallsBackToValueAttribute() throws Exception {
        DateTime ts = new DateTime(2020, 1, 1, 0, 0);

        JEVisAttribute valueAttribute = mock(JEVisAttribute.class);
        JEVisObject valueObject = mock(JEVisObject.class);
        when(valueAttribute.getObject()).thenReturn(valueObject);
        when(valueObject.getName()).thenReturn("value");
        when(valueObject.getID()).thenReturn(1L);

        CalcInputObject calcInputObject = new CalcInputObject("x", CalcInputType.PERIODIC, valueAttribute);

        JEVisObject userDataObject = mock(JEVisObject.class);
        JEVisAttribute userDataValueAttribute = mock(JEVisAttribute.class);
        when(userDataObject.getAttribute("Value")).thenReturn(userDataValueAttribute);

        JEVisSample userValue = mock(JEVisSample.class);
        when(userValue.getTimestamp()).thenReturn(ts);
        when(userValue.getValueAsDouble()).thenReturn(42.0);
        when(userDataValueAttribute.getSamples(ts, ts)).thenReturn(Collections.singletonList(userValue));

        // No original sample at ts - this is the case that previously NPE'd.
        List<JEVisSample> originalSamples = new ArrayList<>();

        Method getUserData = CalcInputObject.class.getDeclaredMethod(
                "getUserData", DateTime.class, DateTime.class, JEVisObject.class, List.class);
        getUserData.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<JEVisSample> result = (List<JEVisSample>) getUserData.invoke(
                calcInputObject, ts, ts, userDataObject, originalSamples);

        assertEquals(1, result.size());
        VirtualSample merged = (VirtualSample) result.get(0);
        assertEquals(ts, merged.getTimestamp());
        assertEquals(42.0, merged.getValueAsDouble(), 0.0001);
        assertEquals(",userValue", merged.getNote());
        assertSame("sampleAttribute must fall back to valueAttribute when there is no original sample",
                valueAttribute, merged.getAttribute());
    }
}
