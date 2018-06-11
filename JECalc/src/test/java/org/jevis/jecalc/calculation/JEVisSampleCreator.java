/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author broder
 */
public class JEVisSampleCreator {

    public static List<JEVisSample> getSamples(Integer minutes, DateTime start, DateTime end) {
        DateTime currentDate = start;
        List<JEVisSample> samples = new ArrayList<>();
        Random random = new Random();
        while (!currentDate.isAfter(end)) {
            JEVisSample currentSample = new VirtualSample(currentDate, random.nextDouble());
            samples.add(currentSample);
            currentDate = currentDate.plusMinutes(minutes);
        }
        return samples;
    }
}
