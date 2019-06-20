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
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        Boolean hasSamples = false;
        Double value = 0d;
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        List<Double> listMedian = new ArrayList<>();
        JEVisUnit unit = null;
        DateTime dateTime = null;

        List<JEVisSample> listManipulation = new ArrayList<>();

        List<DateTime> allTimestamps = getAllTimestampsSingleList(allSamples);
        if (allTimestamps.isEmpty()) {
            return result;
        }
        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));
        List<FunctionalInterval> functionalIntervals = intervals.stream().map(i -> new FunctionalInterval(i.getStart(), i.getEnd())).collect(Collectors.toList());

        int lastPos = 0;
        for (FunctionalInterval interval : functionalIntervals) {
            List<JEVisSample> samplesInPeriod = new ArrayList<>();

            for (int i = lastPos; i < allSamples.size(); i++) {
                try {
                    if (interval.getInterval().contains(allSamples.get(i).getTimestamp().plusMillis(1))) {
                        //logger.info("add sample: " + samples.get(i));
                        samplesInPeriod.add(allSamples.get(i));
                    } else if (allSamples.get(i).getTimestamp().equals(interval.getInterval().getEnd())
                            || allSamples.get(i).getTimestamp().isAfter(interval.getInterval().getEnd())) {
                        lastPos = i;
                        break;
                    }
                } catch (JEVisException ex) {
                    logger.fatal("JEVisException while going through sample: " + ex.getMessage(), ex);
                }
            }
            interval.setSamples(samplesInPeriod);
        }

        for (FunctionalInterval functionalInterval : functionalIntervals) {
            if (mode.equals(ManipulationMode.AVERAGE) || mode.equals(ManipulationMode.MIN)
                    || mode.equals(ManipulationMode.MAX) || mode.equals(ManipulationMode.MEDIAN)) {
                for (JEVisSample smp : functionalInterval.getSamples()) {
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
            } else if (mode.equals(ManipulationMode.RUNNING_MEAN)) {
                if (functionalInterval.getSamples().size() > 1) {
                    for (int i = 1; i < functionalInterval.getSamples().size() - 1; i++) {
                        try {

                            Double value0 = functionalInterval.getSamples().get(i - 1).getValueAsDouble();
                            Double value1 = functionalInterval.getSamples().get(i).getValueAsDouble();

                            Double currentValue = 1d / 2d * (value0 + value1);
                            DateTime newTS = functionalInterval.getSamples().get(i).getTimestamp();

                            if (unit == null) unit = functionalInterval.getSamples().get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);

                            String note = functionalInterval.getSamples().get(i).getNote();
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
                }
            } else if (mode.equals(ManipulationMode.CENTRIC_RUNNING_MEAN)) {
                if (functionalInterval.getSamples().size() > 2) {
                    for (int i = 1; i < functionalInterval.getSamples().size() - 1; i++) {
                        try {

                            Double value0 = functionalInterval.getSamples().get(i - 1).getValueAsDouble();
                            Double value1 = functionalInterval.getSamples().get(i).getValueAsDouble();
                            Double value2 = functionalInterval.getSamples().get(i + 1).getValueAsDouble();

                            Double currentValue = 1d / 3d * (value0 + value1 + value2);
                            DateTime newTS = functionalInterval.getSamples().get(i).getTimestamp();


                            if (unit == null) unit = functionalInterval.getSamples().get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);

                            String note = functionalInterval.getSamples().get(i).getNote();
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
                }
            } else if (mode.equals(ManipulationMode.SORTED_MIN) || mode.equals(ManipulationMode.SORTED_MAX)) {
                if (functionalInterval.getSamples().size() > 1) {
                    DateTime firstDate = null;
                    Period period = null;
                    try {
                        firstDate = functionalInterval.getSamples().get(0).getTimestamp();
                        period = new Period(functionalInterval.getSamples().get(0).getTimestamp(), functionalInterval.getSamples().get(1).getTimestamp());
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    if (mode.equals(ManipulationMode.SORTED_MIN)) {
                        functionalInterval.getSamples().sort((o1, o2) -> {
                            try {
                                return o1.getValueAsDouble().compareTo(o2.getValueAsDouble());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });
                    } else if (mode.equals(ManipulationMode.SORTED_MAX)) {
                        functionalInterval.getSamples().sort((o1, o2) -> {
                            try {
                                return o2.getValueAsDouble().compareTo(o1.getValueAsDouble());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });
                    }

                    for (JEVisSample sample : functionalInterval.getSamples()) {
                        JEVisSample smp = null;
                        try {
                            smp = new VirtualSample(firstDate.plus(functionalInterval.getSamples().indexOf(sample) * period.toStandardDuration().getMillis()), sample.getValueAsDouble());
                            smp.setNote("math(" + mode.toString() + ")");
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }
                }
            }

            if (hasSamples) {
                if (mode.equals(ManipulationMode.AVERAGE)) {
                    value = value / (double) functionalInterval.getSamples().size();
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

            if (!mode.equals(ManipulationMode.CENTRIC_RUNNING_MEAN) && !mode.equals(ManipulationMode.RUNNING_MEAN)
                    && !mode.equals(ManipulationMode.SORTED_MIN) && !mode.equals(ManipulationMode.SORTED_MAX)) {
                result.add(new VirtualSample(dateTime, value, unit, mainTask.getJEVisDataSource(), new VirtualAttribute(null)));
            } else {
                result.addAll(listManipulation);
            }
            logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().format(unit));
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

}
