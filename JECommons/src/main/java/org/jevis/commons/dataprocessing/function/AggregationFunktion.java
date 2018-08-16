/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing.function;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jevis.commons.dataprocessing.ProcessOptions.getAllTimestamps;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AggregationFunktion implements ProcessFunction {

    public static final String NAME = "Aggrigator";
    private final String mode;
    private static final String AVERAGE = "average";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String MEDIAN = "median";

    public AggregationFunktion(String mode) { //average or sum
        this.mode = mode;
    }

    @Override
    public List<JEVisSample> getResult(Process mainTask) {
        List<JEVisSample> result = new ArrayList<>();

        List<List<JEVisSample>> allSamples = new ArrayList<>();
        for (Process task : mainTask.getSubProcesses()) {
            allSamples.add(task.getResult());
        }

        List<DateTime> allTimestamps = getAllTimestamps(allSamples);
        if (allTimestamps.isEmpty()) {
            return result;
        }
        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));

        int lastPos = 0;
        for (Interval interval : intervals) {
            List<JEVisSample> samplesInPeriod = new ArrayList<>();
            for (List<JEVisSample> samples : allSamples) {
                for (int i = lastPos; i < samples.size(); i++) {
                    try {
                        if (interval.contains(samples.get(i).getTimestamp().minusMillis(1))) {
//                        System.out.println("add sample: " + samples.get(i));
                            samplesInPeriod.add(samples.get(i));
                        } else if (samples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                            lastPos = i;
                            break;
                        }
                    } catch (JEVisException ex) {
                        System.out.println("JEVisExeption while going trou sample: " + ex.getMessage());
                    }
                }
                boolean hasSamples = false;
                double sum = 0;
                Double min = Double.MAX_VALUE;
                Double max = Double.MIN_VALUE;
                List<Double> listMedian = new ArrayList<>();

                JEVisUnit unit = null;
                for (JEVisSample sample : samplesInPeriod) {
                    try {
                        Double d = sample.getValueAsDouble();
                        sum += d;
                        min = Math.min(min, d);
                        max = Math.max(max, d);
                        listMedian.add(d);
                        hasSamples = true;
                        if (unit == null) unit = sample.getUnit();
                    } catch (JEVisException ex) {
                        Logger.getLogger(AggregationFunktion.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (mode.equals(AVERAGE)) {
                    sum = sum / (double) samplesInPeriod.size();
                } else if (mode.equals(MIN)) {
                    sum = min;
                } else if (mode.equals(MAX)) {
                    sum = max;
                } else if (mode.equals(MEDIAN)) {
                    if (listMedian.size() > 1)
                        sum = listMedian.get((listMedian.size() - 1) / 2);
                }


                if (hasSamples) {
                    JEVisSample resultSum = new VirtualSample(interval.getEnd(), sum, unit, mainTask.getJEVisDataSource(), new VirtualAttribute(null));
                    result.add(resultSum);
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void resetResult() {
    }

    @Override
    public List<ProcessOption> getAvailableOptions() {
        List<ProcessOption> options = new ArrayList<>();

        return options;
    }
}
