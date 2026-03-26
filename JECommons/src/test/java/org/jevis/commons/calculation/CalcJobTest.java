package org.jevis.commons.calculation;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalcJobTest {

    /**
     * Fix 1a: CalcJob.execute() must return true when exactly 1 result sample is produced.
     */
    @Test
    public void testNewSamplesWrittenTrueForOneSample() throws Exception {
        DateTime ts = new DateTime(2020, 1, 1, 0, 0);
        VirtualSample vs = new VirtualSample(ts, 5.0);

        CalcInputObject inputObj = mock(CalcInputObject.class);
        List<JEVisSample> inputSamples = Collections.singletonList(vs);
        when(inputObj.getSamples()).thenReturn(inputSamples);
        when(inputObj.getIdentifier()).thenReturn("x");
        when(inputObj.getInputType()).thenReturn(CalcInputType.PERIODIC);

        JEVisAttribute outputAttr = mock(JEVisAttribute.class);
        when(outputAttr.hasSample()).thenReturn(false);
        when(outputAttr.getSamples(any(DateTime.class), any(DateTime.class))).thenReturn(new ArrayList<>());
        when(outputAttr.addSamples(any())).thenReturn(0);

        CalcJob job = new CalcJob(
                Collections.singletonList(inputObj),
                "x",
                Collections.singletonList(outputAttr),
                1L
        );
        job.setStaticValue(0.0);

        boolean result = job.execute();
        assertTrue("execute() must return true when 1 sample is produced", result);
    }

    @Test
    public void testNewSamplesWrittenFalseForZeroSamples() throws Exception {
        CalcInputObject inputObj = mock(CalcInputObject.class);
        when(inputObj.getSamples()).thenReturn(new ArrayList<>());
        when(inputObj.getIdentifier()).thenReturn("x");
        when(inputObj.getInputType()).thenReturn(CalcInputType.PERIODIC);

        JEVisAttribute outputAttr = mock(JEVisAttribute.class);
        when(outputAttr.hasSample()).thenReturn(false);

        CalcJob job = new CalcJob(
                Collections.singletonList(inputObj),
                "x",
                Collections.singletonList(outputAttr),
                1L
        );
        job.setStaticValue(0.0);

        boolean result = job.execute();
        assertFalse("execute() must return false when no samples are produced", result);
    }
}
