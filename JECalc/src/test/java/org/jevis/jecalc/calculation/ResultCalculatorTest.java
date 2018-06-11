/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author broder
 */
public class ResultCalculatorTest {

    @Test
    public void shouldCalcTheResultForOneDatarow() throws JEVisException {
        List<JEVisSample> samples = JEVisSampleCreator.getSamples(15, new DateTime(2000, 1, 1, 0, 0), new DateTime(2000, 1, 1, 1, 0));
        String samplesVariable = "server1";
        SampleMerger merger = new SampleMerger();
        merger.addSamples(samples, samplesVariable, SampleMerger.InputType.PERIODIC);
        Map<DateTime, List<Sample>> mergedSamples = merger.merge();

        String templateString = "#{server1}+5";
        CalcTemplate template = new CalcTemplate(templateString);
        ResultCalculator resultCalc = new ResultCalculator(mergedSamples, template);
        List<JEVisSample> resultSamples = resultCalc.calculateResult();

        List<JEVisSample> expectedSamples = new ArrayList<>();
        for (JEVisSample currentSample : samples) {
            expectedSamples.add(new VirtualSample(currentSample.getTimestamp(), currentSample.getValueAsDouble() + 5));
        }
        Assert.assertArrayEquals(expectedSamples.toArray(), resultSamples.toArray());
    }

//    @Test
//    public void shouldCalcTheResultForOfTwoDatarow() throws JEVisException {
//        CalcTemplate template = mock(CalcTemplate.class);
//        ResultCalculator resultCalc = new ResultCalculator(new HashMap<DateTime, List<Sample>>(), template);
//        List<JEVisSample> resultSamples = resultCalc.calculateResult();
//
//        verify(template).evaluate();
//    }
}
