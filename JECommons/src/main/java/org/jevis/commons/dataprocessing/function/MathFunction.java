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
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.jevis.commons.dataprocessing.ProcessOptions.getAllTimestampsSingleList;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MathFunction implements ProcessFunction {
    private static final Logger logger = LogManager.getLogger(MathFunction.class);

    public static final String NAME = "Math Processor";
    private AggregationPeriod aggregationPeriod;
    private ManipulationMode mode;

    public MathFunction(ManipulationMode mode, AggregationPeriod aggregationPeriod) {
        this.mode = mode;
        this.aggregationPeriod = aggregationPeriod;
    }

    public MathFunction(String mode) {
        /**
         * TODO needs initializer? where is it used?
         */
    }

    @Override
    public void resetResult() {
    }

    @Override
    public List<JEVisSample> getResult(Process mainTask) {
        logger.info("get Result for " + mainTask.getID() + " with function " + mode.toString());
        List<JEVisSample> result = new ArrayList<>();

        List<JEVisSample> allSamples = new ArrayList<>();
        for (Process task : mainTask.getSubProcesses()) {
            allSamples.addAll(task.getResult());
        }

        List<DateTime> allTimestamps = null;
        List<Interval> intervals = null;
        if (aggregationPeriod != AggregationPeriod.NONE) {
            allTimestamps = getAllTimestampsSingleList(allSamples);
            intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));
        }

        Boolean hasSamples = false;
        Double value = 0d;
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        List<Double> listMedian = new ArrayList<>();
        JEVisUnit unit = null;
        DateTime dateTime = null;

        List<JEVisSample> listManipulation = new ArrayList<>();

        if (mode.equals(ManipulationMode.AVERAGE) || mode.equals(ManipulationMode.MIN)
                || mode.equals(ManipulationMode.MAX) || mode.equals(ManipulationMode.MEDIAN)) {
            if (aggregationPeriod == AggregationPeriod.NONE) {
                for (JEVisSample smp : allSamples) {
                    try {
                        Double currentValue = smp.getValueAsDouble();
                        value += currentValue;
                        min = Math.min(min, currentValue);
                        max = Math.max(max, currentValue);
                        listMedian.add(currentValue);

                        if (!hasSamples) hasSamples = true;
                        if (unit == null) unit = smp.getUnit();
                        if (dateTime == null) dateTime = smp.getTimestamp();
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                }

                if (hasSamples) {
                    if (mode.equals(ManipulationMode.AVERAGE)) {
                        value = value / (double) allSamples.size();
                    } else if (mode.equals(ManipulationMode.MIN)) {
                        value = min;
                    } else if (mode.equals(ManipulationMode.MAX)) {
                        value = max;
                    } else if (mode.equals(ManipulationMode.MEDIAN)) {
                        if (listMedian.size() > 1)
                            listMedian.sort(Comparator.naturalOrder());
                        value = listMedian.get((listMedian.size() - 1) / 2);
                    }
                }

                result.add(new VirtualSample(dateTime, value, unit, mainTask.getJEVisDataSource(), new VirtualAttribute(null)));

                logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));

            } else {
                int lastPos = 0;
                for (Interval interval : intervals) {
                    List<JEVisSample> samplesInPeriod = new ArrayList<>();

                    for (int i = lastPos; i < allSamples.size(); i++) {
                        try {
                            if (interval.contains(allSamples.get(i).getTimestamp().plusMillis(1))) {
                                //logger.info("add sample: " + samples.get(i));
                                samplesInPeriod.add(allSamples.get(i));
                            } else if (allSamples.get(i).getTimestamp().equals(interval.getEnd())
                                    || allSamples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                                lastPos = i;
                                break;
                            }
                        } catch (JEVisException ex) {
                            logger.fatal("JEVisException while going through sample: " + ex.getMessage(), ex);
                        }
                    }

                    value = 0d;
                    listMedian = new ArrayList<>();
                    for (JEVisSample sample : samplesInPeriod) {
                        try {
                            Double currentValue = sample.getValueAsDouble();
                            value += currentValue;
                            min = Math.min(min, currentValue);
                            max = Math.max(max, currentValue);
                            listMedian.add(currentValue);

                            if (!hasSamples) hasSamples = true;
                            if (unit == null) unit = sample.getUnit();
                            if (dateTime == null) dateTime = sample.getTimestamp();
                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }
                    }

                    QuantityUnits qu = new QuantityUnits();
                    boolean isQuantity = qu.isQuantityUnit(unit);

                    if (hasSamples && !isQuantity) {
                        value = value / samplesInPeriod.size();
                    }

                    if (hasSamples) {
                        if (mode.equals(ManipulationMode.AVERAGE)) {
                            value = value / (double) allSamples.size();
                        } else if (mode.equals(ManipulationMode.MIN)) {
                            value = min;
                        } else if (mode.equals(ManipulationMode.MAX)) {
                            value = max;
                        } else if (mode.equals(ManipulationMode.MEDIAN)) {
                            if (listMedian.size() > 1)
                                listMedian.sort(Comparator.naturalOrder());
                            value = listMedian.get((listMedian.size() - 1) / 2);
                        }
                    }

                    result.add(new VirtualSample(dateTime, value, unit, mainTask.getJEVisDataSource(), new VirtualAttribute(null)));

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));

                }
            }

        } else if (mode.equals(ManipulationMode.RUNNING_MEAN)) {
            if (allSamples.size() > 1) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    for (int i = 1; i < allSamples.size() - 1; i++) {
                        try {

                            Double value0 = allSamples.get(i - 1).getValueAsDouble();
                            Double value1 = allSamples.get(i).getValueAsDouble();

                            Double currentValue = 1d / 2d * (value0 + value1);
                            DateTime newTS = allSamples.get(i).getTimestamp();

                            if (unit == null) unit = allSamples.get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);

                            String note = allSamples.get(i).getNote();
                            if (note == null || note.equals(""))
                                note = "math(" + ManipulationMode.RUNNING_MEAN.toString() + ")";
                            else note += ",math(" + ManipulationMode.RUNNING_MEAN.toString() + ")";
                            smp.setNote(note);

                            listManipulation.add(smp);

                            if (!hasSamples) hasSamples = true;

                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                } else {
                    Aggregate aggregate = new Aggregate(mainTask, allSamples, intervals, hasSamples, unit).invoke();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JEVisSample> aggregatedSamples = aggregate.getAggregatedSamples();

                    for (int i = 1; i < aggregatedSamples.size() - 1; i++) {
                        try {
                            Double value0 = aggregatedSamples.get(i - 1).getValueAsDouble();
                            Double value1 = aggregatedSamples.get(i).getValueAsDouble();

                            Double currentValue = 1d / 2d * (value0 + value1);
                            DateTime newTS = aggregatedSamples.get(i).getTimestamp();

                            if (unit == null) unit = aggregatedSamples.get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);

                            String note = aggregatedSamples.get(i).getNote();
                            if (note == null || note.equals(""))
                                note = "math(" + ManipulationMode.RUNNING_MEAN.toString() + ")";
                            else note += ",math(" + ManipulationMode.RUNNING_MEAN.toString() + ")";
                            smp.setNote(note);

                            listManipulation.add(smp);

                            if (!hasSamples) hasSamples = true;
                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                }
            }
        } else if (mode.equals(ManipulationMode.CENTRIC_RUNNING_MEAN)) {
            if (allSamples.size() > 2) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    for (int i = 1; i < allSamples.size() - 1; i++) {
                        try {

                            Double value0 = allSamples.get(i - 1).getValueAsDouble();
                            Double value1 = allSamples.get(i).getValueAsDouble();
                            Double value2 = allSamples.get(i + 1).getValueAsDouble();

                            Double currentValue = 1d / 3d * (value0 + value1 + value2);
                            DateTime newTS = allSamples.get(i).getTimestamp();


                            if (unit == null) unit = allSamples.get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);

                            String note = allSamples.get(i).getNote();
                            if (note == null || note.equals(""))
                                note = "math(" + ManipulationMode.CENTRIC_RUNNING_MEAN.toString() + ")";
                            else note += ",math(" + ManipulationMode.CENTRIC_RUNNING_MEAN.toString() + ")";
                            smp.setNote(note);

                            listManipulation.add(smp);

                            if (!hasSamples) hasSamples = true;

                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                } else {
                    Aggregate aggregate = new Aggregate(mainTask, allSamples, intervals, hasSamples, unit).invoke();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JEVisSample> aggregatedSamples = aggregate.getAggregatedSamples();

                    for (int i = 1; i < aggregatedSamples.size() - 1; i++) {
                        try {

                            Double value0 = aggregatedSamples.get(i - 1).getValueAsDouble();
                            Double value1 = aggregatedSamples.get(i).getValueAsDouble();
                            Double value2 = aggregatedSamples.get(i + 1).getValueAsDouble();

                            Double currentValue = 1d / 3d * (value0 + value1 + value2);
                            DateTime newTS = aggregatedSamples.get(i).getTimestamp();


                            if (unit == null) unit = aggregatedSamples.get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);

                            String note = aggregatedSamples.get(i).getNote();
                            if (note == null || note.equals(""))
                                note = "math(" + ManipulationMode.CENTRIC_RUNNING_MEAN.toString() + ")";
                            else note += ",math(" + ManipulationMode.CENTRIC_RUNNING_MEAN.toString() + ")";
                            smp.setNote(note);

                            listManipulation.add(smp);

                            if (!hasSamples) hasSamples = true;

                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                }
            }
        } else if (mode.equals(ManipulationMode.SORTED_MIN) || mode.equals(ManipulationMode.SORTED_MAX)) {
            if (allSamples.size() > 1) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    DateTime firstDate = null;
                    Period period = null;
                    try {
                        firstDate = allSamples.get(0).getTimestamp();
                        period = new Period(allSamples.get(0).getTimestamp(), allSamples.get(1).getTimestamp());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    if (mode.equals(ManipulationMode.SORTED_MIN)) {
                        allSamples.sort((o1, o2) -> {
                            try {
                                return o1.getValueAsDouble().compareTo(o2.getValueAsDouble());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });
                    } else if (mode.equals(ManipulationMode.SORTED_MAX)) {
                        allSamples.sort((o1, o2) -> {
                            try {
                                return o2.getValueAsDouble().compareTo(o1.getValueAsDouble());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });
                    }

                    for (JEVisSample sample : allSamples) {
                        JEVisSample smp = null;
                        try {
                            smp = new VirtualSample(firstDate.plus(allSamples.indexOf(sample) * period.toStandardDuration().getMillis()), sample.getValueAsDouble());
                            smp.setNote("math(" + mode.toString() + ")");
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                } else {
                    Aggregate aggregate = new Aggregate(mainTask, allSamples, intervals, hasSamples, unit).invoke();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JEVisSample> aggregatedSamples = aggregate.getAggregatedSamples();

                    DateTime firstDate = null;
                    Period period = null;
                    try {
                        firstDate = aggregatedSamples.get(0).getTimestamp();
                        period = new Period(aggregatedSamples.get(0).getTimestamp(), aggregatedSamples.get(1).getTimestamp());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    if (mode.equals(ManipulationMode.SORTED_MIN)) {
                        aggregatedSamples.sort((o1, o2) -> {
                            try {
                                return o1.getValueAsDouble().compareTo(o2.getValueAsDouble());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });
                    } else if (mode.equals(ManipulationMode.SORTED_MAX)) {
                        aggregatedSamples.sort((o1, o2) -> {
                            try {
                                return o2.getValueAsDouble().compareTo(o1.getValueAsDouble());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });
                    }

                    for (JEVisSample sample : aggregatedSamples) {
                        JEVisSample smp = null;
                        try {
                            smp = new VirtualSample(firstDate.plus(aggregatedSamples.indexOf(sample) * period.toStandardDuration().getMillis()), sample.getValueAsDouble());
                            smp.setNote("math(" + mode.toString() + ")");
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);
                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                }
            }
        } else if (mode.equals(ManipulationMode.CUMULATE)) {
            if (allSamples.size() > 1) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    DateTime firstDate = null;
                    Period period = null;
                    try {
                        firstDate = allSamples.get(0).getTimestamp();
                        period = new Period(allSamples.get(0).getTimestamp(), allSamples.get(1).getTimestamp());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    Double lastValue = 0d;
                    for (JEVisSample sample : allSamples) {
                        JEVisSample smp = null;
                        try {
                            Double cumulatedValue = sample.getValueAsDouble() + lastValue;
                            smp = new VirtualSample(firstDate.plus(allSamples.indexOf(sample) * period.toStandardDuration().getMillis()), cumulatedValue);
                            lastValue = cumulatedValue;
                            smp.setNote("math(" + mode.toString() + ")");
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
                } else {
                    Aggregate aggregate = new Aggregate(mainTask, allSamples, intervals, hasSamples, unit).invoke();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JEVisSample> aggregatedSamples = aggregate.getAggregatedSamples();

                    DateTime firstDate = null;
                    Period period = null;
                    try {
                        firstDate = aggregatedSamples.get(0).getTimestamp();
                        period = new Period(aggregatedSamples.get(0).getTimestamp(), aggregatedSamples.get(1).getTimestamp());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    Double lastValue = 0d;
                    for (JEVisSample sample : aggregatedSamples) {
                        JEVisSample smp = null;
                        try {
                            Double cumulatedValue = sample.getValueAsDouble() + lastValue;
                            smp = new VirtualSample(firstDate.plus(aggregatedSamples.indexOf(sample) * period.toStandardDuration().getMillis()), cumulatedValue);
                            lastValue = cumulatedValue;
                            smp.setNote("math(" + mode.toString() + ")");
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);
                    logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
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
    public List<ProcessOption> getAvailableOptions() {
        List<ProcessOption> options = new ArrayList<>();

        options.add(new BasicProcessOption("Object"));
        options.add(new BasicProcessOption("Attribute"));
        options.add(new BasicProcessOption("Workflow"));

        return options;
    }

    private class Aggregate {
        private Process mainTask;
        private List<JEVisSample> allSamples;
        private List<Interval> intervals;
        private Boolean hasSamples;
        private JEVisUnit unit;
        private List<JEVisSample> aggregatedSamples;

        public Aggregate(Process mainTask, List<JEVisSample> allSamples, List<Interval> intervals, Boolean hasSamples, JEVisUnit unit) {
            this.mainTask = mainTask;
            this.allSamples = allSamples;
            this.intervals = intervals;
            this.hasSamples = hasSamples;
            this.unit = unit;
        }

        public Boolean getHasSamples() {
            return hasSamples;
        }

        public JEVisUnit getUnit() {
            return unit;
        }

        public List<JEVisSample> getAggregatedSamples() {
            return aggregatedSamples;
        }

        public Aggregate invoke() {
            JEVisAttribute attribute = null;
            aggregatedSamples = new ArrayList<>();
            if (allSamples.size() > 0) {
                try {
                    attribute = allSamples.get(0).getAttribute();
                } catch (JEVisException e) {
                    logger.error("Could not get Attribute: " + e);
                }
            }

            int lastPos = 0;
            for (Interval interval : intervals) {
                List<JEVisSample> samplesInPeriod = new ArrayList<>();
                //logger.info("interval: " + interval);

                Period oldPeriod = null;
                if (allSamples.size() > 1) {
                    try {
                        oldPeriod = new Period(allSamples.get(0).getTimestamp(), allSamples.get(1).getTimestamp());
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

                for (int i = lastPos; i < allSamples.size(); i++) {
                    try {
                        if (interval.contains(allSamples.get(i).getTimestamp().plusMillis(1))) {
                            //logger.info("add sample: " + samples.get(i));
                            samplesInPeriod.add(allSamples.get(i));
                        } else if (allSamples.get(i).getTimestamp().equals(interval.getEnd())
                                || allSamples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                            lastPos = i;
                            break;
                        }
                    } catch (JEVisException ex) {
                        logger.fatal("JEVisException while going through sample: " + ex.getMessage(), ex);
                    }
                }

                hasSamples = false;
                Double sum = 0d;
                unit = null;
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
                    aggregatedSamples.add(resultSum);
                }

            }
            return this;
        }
    }
}