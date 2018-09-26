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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.dataprocessing.ProcessOptions.getAllTimestamps;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AggregatorFunction implements ProcessFunction {
    public static final String NAME = "Aggregator";
    private static final Logger logger = LogManager.getLogger(AggregatorFunction.class);

    @Override
    public List<JEVisSample> getResult(Process mainTask) {
        List<JEVisSample> result = new ArrayList<>();

        List<List<JEVisSample>> allSamples = new ArrayList<>();
        for (Process task : mainTask.getSubProcesses()) {
            allSamples.add(task.getResult());
            //logger.info("Add input result: " + allSamples.size());
        }

        List<DateTime> allTimestamps = getAllTimestamps(allSamples);
        if (allTimestamps.isEmpty()) {
            return result;
        }
        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));

        //logger.info("intervals: " + intervals.size());

        int lastPos = 0;
        for (Interval interval : intervals) {
            List<JEVisSample> samplesInPeriod = new ArrayList<>();
            //logger.info("interval: " + interval);

            for (List<JEVisSample> samples : allSamples) {
                for (int i = lastPos; i < samples.size(); i++) {
                    try {
                        if (interval.contains(samples.get(i).getTimestamp())) {
                            //logger.info("add sample: " + samples.get(i));
                            samplesInPeriod.add(samples.get(i));
                        } else if (samples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                            lastPos = i;
                            break;
                        }
                    } catch (JEVisException ex) {
                        logger.fatal("JEVisException while going through sample: " + ex.getMessage(), ex);
                    }
                }

                BigDecimal sum = BigDecimal.ZERO;
                JEVisUnit unit = null;
                for (JEVisSample sample : samplesInPeriod) {
                    try {
                        sum.add(BigDecimal.valueOf(sample.getValueAsDouble()));
                        if (unit == null) unit = sample.getUnit();
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                }

                JEVisSample resultSum = new VirtualSample(interval.getStart(), sum.doubleValue(), unit, mainTask.getJEVisDataSource(), new VirtualAttribute(null));
                result.add(resultSum);
                try {
                    logger.info("resultSum: " + resultSum.getTimestamp() + "  " + resultSum.getValueAsDouble());
                } catch (JEVisException ex) {
                    logger.fatal(ex);
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
