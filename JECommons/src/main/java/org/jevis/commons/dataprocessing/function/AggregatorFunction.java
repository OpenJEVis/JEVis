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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.dataprocessing.ProcessOptions.CUSTOM;
import static org.jevis.commons.dataprocessing.ProcessOptions.getAllTimestamps;

/**
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
        }

        List<DateTime> allTimestamps = getAllTimestamps(allSamples);
        if (allTimestamps.isEmpty()) {
            return result;
        }
        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));

        boolean isCustomWorkDay = true;
        for (ProcessOption option : mainTask.getOptions()) {
            if (option.getKey().equals(CUSTOM)) {
                isCustomWorkDay = Boolean.parseBoolean(option.getValue());
                break;
            }
        }

        WorkDays workDays = new WorkDays(mainTask.getObject());
        workDays.setEnabled(isCustomWorkDay);

        if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
            Period period = intervals.get(0).toPeriod();
            if (period.getDays() > 0 || period.getWeeks() > 0 || period.getMonths() > 0 || period.getYears() > 0) {
                List<Interval> newIntervals = new ArrayList<>();
                for (Interval interval : intervals) {
                    newIntervals.add(new Interval(interval.getStart().minusDays(1), interval.getEnd().minusDays(1)));
                }
                if (newIntervals.size() > 0) {
                    Interval lastInterval = newIntervals.get(newIntervals.size() - 1);
                    newIntervals.add(new Interval(lastInterval.getEnd(), lastInterval.getEnd().plus(period)));
                    intervals = newIntervals;
                }
            }
        }

        JEVisAttribute attribute = null;
        if (allSamples.size() > 0) {
            List<JEVisSample> samples = allSamples.get(0);
            if (samples.size() > 0) {
                try {
                    attribute = samples.get(0).getAttribute();
                } catch (JEVisException e) {
                    logger.error("Could not get Attribute: " + e);
                }
            }
        }

        int lastPos = 0;
        for (Interval interval : intervals) {
            List<JEVisSample> samplesInPeriod = new ArrayList<>();
            //logger.info("interval: " + interval);

            for (List<JEVisSample> samples : allSamples) {
                Period oldPeriod = null;
                if (samples.size() > 1) {
                    try {
                        oldPeriod = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                    } catch (JEVisException e) {
                        logger.error("Could not get old Period: " + e);
                    }
                }

                Period newPeriod = null;
                try {
                    newPeriod = new Period(interval.getStart(), interval.getEnd());
                } catch (Exception e) {
                    logger.error("Could not get new Period: " + e);
                }

                for (int i = lastPos; i < samples.size(); i++) {
                    try {
                        if (interval.contains(samples.get(i).getTimestamp().plusMillis(1))) {
                            //logger.info("add sample: " + samples.get(i));
                            samplesInPeriod.add(samples.get(i));
                        } else if (samples.get(i).getTimestamp().equals(interval.getEnd())
                                || samples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                            lastPos = i;
                            break;
                        }
                    } catch (JEVisException ex) {
                        logger.fatal("JEVisException while going through sample: " + ex.getMessage(), ex);
                    }
                }

                boolean hasSamples = false;
                Double sum = 0d;
                JEVisUnit unit = null;
                for (JEVisSample sample : samplesInPeriod) {
                    try {
                        sum += sample.getValueAsDouble();
                        hasSamples = true;
                        if (unit == null) unit = sample.getUnit();
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                }

                /**
                 * if its not a quantity the aggregated total value results from the mean value of all samples in a period, not their sum
                 */

                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(unit);

                if (hasSamples && !isQuantity) {
                    sum = sum / samplesInPeriod.size();
                }

                if (hasSamples) {
                    JEVisSample resultSum = new VirtualSample(interval.getStart(), sum, unit, mainTask.getJEVisDataSource(), attribute);
                    if (oldPeriod != null && newPeriod != null) {
                        try {
                            resultSum.setNote("Aggregation(" + oldPeriod.toString() + "/" + newPeriod.toString() + ")");
                        } catch (JEVisException e) {
                            logger.error("Could not set new Note to sample: " + e);
                        }
                    }
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
