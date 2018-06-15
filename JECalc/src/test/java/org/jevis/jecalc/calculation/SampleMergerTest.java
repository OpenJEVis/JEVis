/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.jecalc.calculation.SampleMerger.InputType;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 *
 * @author broder
 */
public class SampleMergerTest {

    SampleMerger drMerger;

    @Before
    public void setUp() {
        drMerger = new SampleMerger();

    }

    @Test
    public void shouldContainAllDates() {
        List<JEVisSample> samples1 = JEVisSampleCreator.getSamples(15, new DateTime(2000, 1, 1, 0, 0), new DateTime(2000, 1, 1, 2, 0));
        List<JEVisSample> samples2 = JEVisSampleCreator.getSamples(30, new DateTime(2000, 1, 1, 1, 0), new DateTime(2000, 1, 1, 3, 0));
        drMerger.addSamples(samples1, "server1", InputType.PERIODIC);
        drMerger.addSamples(samples2, "server2", InputType.PERIODIC);
        Map<DateTime, List<Sample>> sampleMap = drMerger.merge();
        Assert.assertEquals(3, sampleMap.keySet().size());
        List<DateTime> actualDates = new ArrayList<>(sampleMap.keySet());
        Collections.sort(actualDates);
        List<DateTime> expectedDates = new ArrayList<>();
        expectedDates.add(new DateTime(2000, 1, 1, 1, 0));
        expectedDates.add(new DateTime(2000, 1, 1, 1, 30));
        expectedDates.add(new DateTime(2000, 1, 1, 2, 0));
        Assert.assertArrayEquals(expectedDates.toArray(), actualDates.toArray());
    }

    @Test
    public void shouldAddConstant() {
        List<JEVisSample> samples1 = JEVisSampleCreator.getSamples(15, new DateTime(2000, 1, 1, 0, 0), new DateTime(2000, 1, 1, 2, 0));
        List<JEVisSample> samples2 = JEVisSampleCreator.getSamples(30, new DateTime(2000, 1, 1, 1, 0), new DateTime(2000, 1, 1, 3, 0));
        drMerger.addSamples(samples1, "server1", InputType.PERIODIC);
        drMerger.addSamples(samples2, "server2", InputType.PERIODIC);
        JEVisSample currentSample = new VirtualSample(new DateTime(2000, 1, 1, 1, 0), 5.0);
        drMerger.addSamples(Arrays.asList(currentSample), "constant", InputType.STATIC);
        mergeAndCompareResults(3, 3);
    }

    @Test
    public void shouldAddPeriodConstant() {
        List<JEVisSample> samples1 = JEVisSampleCreator.getSamples(15, new DateTime(2000, 1, 1, 0, 0), new DateTime(2000, 1, 1, 3, 0));
        List<JEVisSample> samples2 = JEVisSampleCreator.getSamples(15, new DateTime(2000, 1, 1, 1, 0), new DateTime(2000, 1, 1, 3, 0));
        drMerger.addSamples(samples1, "server1", InputType.PERIODIC);
        drMerger.addSamples(samples2, "server2", InputType.PERIODIC);
        JEVisSample currentSample1 = new VirtualSample(new DateTime(2000, 1, 1, 1, 0), 5.0);
        JEVisSample currentSample2 = new VirtualSample(new DateTime(2000, 1, 1, 2, 0), 10.0);
        drMerger.addSamples(Arrays.asList(currentSample1, currentSample2), "constant", InputType.NON_PERIODIC);
        mergeAndCompareResults(9, 3);
    }

    @Test
    public void shouldMergeTwoNotScheduleEqualDatarows() {
        List<JEVisSample> samples1 = JEVisSampleCreator.getSamples(15, new DateTime(2000, 1, 1, 0, 0), new DateTime(2000, 1, 1, 2, 0));
        List<JEVisSample> samples2 = JEVisSampleCreator.getSamples(30, new DateTime(2000, 1, 1, 1, 0), new DateTime(2000, 1, 1, 3, 0));
        drMerger.addSamples(samples1, "server1", InputType.PERIODIC);
        drMerger.addSamples(samples2, "server2", InputType.PERIODIC);
        mergeAndCompareResults(3, 2);
    }

    private void mergeAndCompareResults(int expectedMapSize, int expectedSamplesPerMap) {
        Map<DateTime, List<Sample>> sampleMap = drMerger.merge();
        Assert.assertEquals(expectedMapSize, sampleMap.size());
        for (Map.Entry<DateTime, List<Sample>> mapEntry : sampleMap.entrySet()) {
            Assert.assertEquals(expectedSamplesPerMap, mapEntry.getValue().size());
        }
    }
}
