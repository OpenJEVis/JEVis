package org.jevis.commons.calculation;

import org.jevis.api.*;
import org.jevis.commons.database.SampleHandler;
import org.joda.time.DateTime;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for Fix 1 (bounded reprocessing window) in {@link CalcJobFactory}: {@code getCurrentCalcJob}
 * must step back {@code N} output periods instead of exactly 1 before resuming, where N comes from
 * the "Reprocessing Periods" config attribute (default 1, preserving today's behavior).
 */
public class CalcJobFactoryTest {

    @Test
    public void getReprocessingPeriodsDefaultsToOne() throws Exception {
        assertEquals(1, invokeGetReprocessingPeriods(1L));
    }

    @Test
    public void getReprocessingPeriodsUsesConfiguredValue() throws Exception {
        assertEquals(5, invokeGetReprocessingPeriods(5L));
    }

    @Test
    public void getReprocessingPeriodsClampsBelowOneToOne() throws Exception {
        assertEquals(1, invokeGetReprocessingPeriods(0L));
    }

    private int invokeGetReprocessingPeriods(Long configuredValue) throws Exception {
        SampleHandler sampleHandler = mock(SampleHandler.class);
        JEVisObject jevisObject = mock(JEVisObject.class);
        when(sampleHandler.getLastSample(jevisObject, CalcJobFactory.Calculation.REPROCESSING_PERIODS.getName(), 1L))
                .thenReturn(configuredValue);

        Method method = CalcJobFactory.class.getDeclaredMethod("getReprocessingPeriods", SampleHandler.class, JEVisObject.class);
        method.setAccessible(true);
        return (int) method.invoke(new CalcJobFactory(), sampleHandler, jevisObject);
    }

    /**
     * getStartTimeFromOutputs() first advances one period past the last output sample (to
     * "the next period to compute"), then getCurrentCalcJob() steps back N periods from there.
     * With the default N=1 those cancel out exactly, reproducing today's behavior: resume
     * recomputing from the start of the last-written output period, unchanged.
     */
    @Test
    public void currentCalcJobStepsBackExactlyOnePeriodByDefault() throws Exception {
        DateTime capturedStartTime = runGetCurrentCalcJobAndCaptureInputWindowStart(1L);
        DateTime lastOutputPeriodStart = new DateTime(2024, 3, 1, 0, 0);
        assertEquals(lastOutputPeriodStart, capturedStartTime);
    }

    /**
     * With Reprocessing Periods = 3, the recompute window must reach back 3 output periods from
     * "the next period to compute" (lastOutputPeriodStart + 1 period), i.e. 2 periods before the
     * last-written output period - so a retroactive correction landing up to 2 periods before the
     * last-written sample is picked up automatically.
     */
    @Test
    public void currentCalcJobStepsBackConfiguredNumberOfPeriods() throws Exception {
        DateTime capturedStartTime = runGetCurrentCalcJobAndCaptureInputWindowStart(3L);
        DateTime lastOutputPeriodStart = new DateTime(2024, 3, 1, 0, 0);
        assertEquals(lastOutputPeriodStart.minusMonths(2), capturedStartTime);
    }

    /**
     * Builds a minimal but complete mock graph for one Calculation with one monthly output and one
     * periodic input, runs {@code getCurrentCalcJob}, and returns the start time that was actually
     * used to fetch input samples - i.e. the result of the reprocessing-window lookback.
     */
    private DateTime runGetCurrentCalcJobAndCaptureInputWindowStart(Long reprocessingPeriods) throws Exception {
        DateTime lastOutputPeriodStart = new DateTime(2024, 3, 1, 0, 0);

        JEVisDataSource ds = mock(JEVisDataSource.class);

        JEVisObject calcObject = mock(JEVisObject.class);
        when(calcObject.getID()).thenReturn(1L);
        when(calcObject.getDataSource()).thenReturn(ds);

        SampleHandler sampleHandler = mock(SampleHandler.class);
        when(sampleHandler.getLastSample(calcObject, CalcJobFactory.Calculation.EXPRESSION.getName(), "")).thenReturn("in1");
        when(sampleHandler.getLastSample(calcObject, CalcJobFactory.Calculation.REPROCESSING_PERIODS.getName(), 1L)).thenReturn(reprocessingPeriods);
        when(sampleHandler.getLastSample(calcObject, CalcJobFactory.Calculation.DIV0_HANDLING.getName(), "")).thenReturn("");
        when(sampleHandler.getLastSample(calcObject, CalcJobFactory.Calculation.STATIC_VALUE.getName(), 0.0)).thenReturn(0.0);

        JEVisAttribute allZeroValueAtt = mock(JEVisAttribute.class);
        when(allZeroValueAtt.hasSample()).thenReturn(false);
        when(calcObject.getAttribute(CalcJobFactory.Calculation.ALL_ZERO_VALUE.getName())).thenReturn(allZeroValueAtt);

        // --- Output side: one monthly output attribute ---
        JEVisClass outputClass = mock(JEVisClass.class);
        when(ds.getJEVisClass(CalcJobFactory.Calculation.OUTPUT_DATA.getName())).thenReturn(outputClass);

        JEVisObject outputChild = mock(JEVisObject.class);
        when(calcObject.getChildren(outputClass, false)).thenReturn(Collections.singletonList(outputChild));
        when(outputChild.getDataSource()).thenReturn(ds);

        JEVisAttribute outputTargetAttr = mock(JEVisAttribute.class);
        when(outputChild.getAttribute(CalcJobFactory.Calculation.OUTPUT_DATA.getName())).thenReturn(outputTargetAttr);
        JEVisSample outputTargetSample = mock(JEVisSample.class);
        when(outputTargetSample.getValueAsString()).thenReturn("50:Value");
        when(outputTargetAttr.getLatestSample()).thenReturn(outputTargetSample);

        JEVisObject outputValueObject = mock(JEVisObject.class);
        when(ds.getObject(50L)).thenReturn(outputValueObject);
        JEVisAttribute outputValueAttribute = mock(JEVisAttribute.class);
        when(outputValueObject.getAttribute("Value")).thenReturn(outputValueAttribute);
        when(outputValueAttribute.getObject()).thenReturn(outputValueObject);

        when(outputValueAttribute.hasSample()).thenReturn(true);
        JEVisSample lastOutputSample = mock(JEVisSample.class);
        when(lastOutputSample.getTimestamp()).thenReturn(lastOutputPeriodStart);
        when(outputValueAttribute.getLatestSample()).thenReturn(lastOutputSample);

        // Output object is on a monthly period grid starting 1990-01-01.
        JEVisAttribute outputPeriodAttr = mock(JEVisAttribute.class);
        when(outputValueObject.getAttribute("Period")).thenReturn(outputPeriodAttr);
        JEVisSample outputPeriodSample = mock(JEVisSample.class);
        when(outputPeriodSample.getTimestamp()).thenReturn(new DateTime(1990, 1, 1, 0, 0));
        when(outputPeriodSample.getValueAsString()).thenReturn("P1M");
        when(outputPeriodAttr.getAllSamples()).thenReturn(Collections.singletonList(outputPeriodSample));

        // --- Input side: one periodic input feeding variable "in1" ---
        JEVisClass inputClass = mock(JEVisClass.class);
        when(ds.getJEVisClass(CalcJobFactory.Calculation.INPUT.getName())).thenReturn(inputClass);

        JEVisObject inputChild = mock(JEVisObject.class);
        when(calcObject.getChildren(inputClass, false)).thenReturn(Collections.singletonList(inputChild));
        when(inputChild.getName()).thenReturn("in1-child");
        when(inputChild.getID()).thenReturn(61L);

        JEVisAttribute inputTypeAttr = mock(JEVisAttribute.class);
        when(inputChild.getAttribute(CalcJobFactory.Calculation.INPUT_TYPE.getName())).thenReturn(inputTypeAttr);
        JEVisSample inputTypeSample = mock(JEVisSample.class);
        when(inputTypeSample.getValueAsString()).thenReturn(CalcInputType.PERIODIC.toString());
        when(inputTypeAttr.getLatestSample()).thenReturn(inputTypeSample);

        JEVisAttribute inputDataTargetAttr = mock(JEVisAttribute.class);
        when(inputChild.getAttribute(CalcJobFactory.Calculation.INPUT_DATA.getName())).thenReturn(inputDataTargetAttr);
        JEVisSample inputDataTargetSample = mock(JEVisSample.class);
        when(inputDataTargetSample.getValueAsString()).thenReturn("60:Value");
        when(inputDataTargetAttr.getLatestSample()).thenReturn(inputDataTargetSample);

        JEVisObject inputValueObject = mock(JEVisObject.class);
        when(ds.getObject(60L)).thenReturn(inputValueObject);
        when(inputValueObject.getName()).thenReturn("input1");
        when(inputValueObject.getID()).thenReturn(60L);

        JEVisAttribute inputValueAttribute = mock(JEVisAttribute.class);
        when(inputValueObject.getAttribute("Value")).thenReturn(inputValueAttribute);
        when(inputValueAttribute.getObject()).thenReturn(inputValueObject);

        JEVisSample lastInputSample = mock(JEVisSample.class);
        when(lastInputSample.getTimestamp()).thenReturn(new DateTime(2024, 2, 1, 0, 0));
        when(inputValueAttribute.getLatestSample()).thenReturn(lastInputSample);
        when(inputValueAttribute.getTimestampOfFirstSample()).thenReturn(new DateTime(2020, 1, 1, 0, 0));

        List<JEVisSample> emptySamples = new ArrayList<>();
        when(inputValueAttribute.getSamples(any(DateTime.class), any(DateTime.class))).thenReturn(emptySamples);

        JEVisAttribute identifierAttr = mock(JEVisAttribute.class);
        when(inputChild.getAttribute(CalcJobFactory.Calculation.IDENTIFIER.getName())).thenReturn(identifierAttr);
        JEVisSample identifierSample = mock(JEVisSample.class);
        when(identifierSample.getValueAsString()).thenReturn("in1");
        when(identifierAttr.getLatestSample()).thenReturn(identifierSample);

        CalcJobFactory factory = new CalcJobFactory();
        factory.getCurrentCalcJob(sampleHandler, ds, calcObject);

        org.mockito.ArgumentCaptor<DateTime> startCaptor = org.mockito.ArgumentCaptor.forClass(DateTime.class);
        org.mockito.Mockito.verify(inputValueAttribute).getSamples(startCaptor.capture(), any(DateTime.class));
        return startCaptor.getValue();
    }
}
