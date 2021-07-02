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
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.jevis.commons.dataprocessing.ProcessOptions.getAllJsonTimestampsSingleList;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MathFunction implements ProcessFunction {
    private static final Logger logger = LogManager.getLogger(MathFunction.class);

    public static final String NAME = "Math Processor";
    private AggregationPeriod aggregationPeriod;
    private ManipulationMode mode;
    private JsonSampleGenerator jsonSampleGenerator;

    public MathFunction(ManipulationMode mode, AggregationPeriod aggregationPeriod) {
        this.mode = mode;
        this.aggregationPeriod = aggregationPeriod;
    }

    public MathFunction(JsonSampleGenerator jsonSampleGenerator, ManipulationMode mode, AggregationPeriod aggregationPeriod) {
        this.jsonSampleGenerator = jsonSampleGenerator;
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

    @Override
    public List<JsonSample> getJsonResult(BasicProcess mainTask) {
        logger.info("get Result for {} with function {}", mainTask.getID(), mode.toString());
        List<JsonSample> result = new ArrayList<>();

        List<JsonSample> allSamples = new ArrayList<>();
        Boolean hasSamples = false;

        for (Process task : mainTask.getSubProcesses()) {
            allSamples.addAll(task.getJsonResult());
            if (task.getJsonResult().size() > 0) hasSamples = true;
        }

        List<DateTime> allTimestamps = null;
        List<Interval> intervals = null;
        if (aggregationPeriod != AggregationPeriod.NONE) {
            allTimestamps = getAllJsonTimestampsSingleList(allSamples);
//            intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));
            StartAndEndDates startAndEndDates = new StartAndEndDates(mainTask).invoke();
            intervals = ProcessOptions.getIntervals(mainTask, startAndEndDates.getStart(), startAndEndDates.getEnd());
        }

        Double value = 0d;
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        List<Double> listMedian = new ArrayList<>();

        DateTime dateTime = null;

        List<JsonSample> listManipulation = new ArrayList<>();

        JsonAttribute jsonAttribute = mainTask.getJsonAttribute();
        JEVisUnit unit = new JEVisUnitImp(jsonAttribute.getDisplayUnit());

        if (mode.equals(ManipulationMode.AVERAGE) || mode.equals(ManipulationMode.MIN)
                || mode.equals(ManipulationMode.MAX) || mode.equals(ManipulationMode.MEDIAN)) {
            if (aggregationPeriod == AggregationPeriod.NONE) {
                for (JsonSample smp : allSamples) {
                    Double currentValue = Double.parseDouble(smp.getValue());
                    value += currentValue;
                    min = Math.min(min, currentValue);
                    max = Math.max(max, currentValue);
                    listMedian.add(currentValue);

                    if (!hasSamples) hasSamples = true;
                    if (dateTime == null) dateTime = new DateTime(smp.getTs());
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

                JsonSample jsonSample = new JsonSample();
                jsonSample.setTs(dateTime.toString());
                jsonSample.setValue(value.toString());
                result.add(jsonSample);

                logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));

            } else {
                int lastPos = 0;
                for (Interval interval : intervals) {
                    List<JsonSample> samplesInPeriod = new ArrayList<>();

                    DateTime intervalStart = interval.getStart();
                    DateTime intervalEnd = interval.getEnd();

                    lastPos = AggregatorFunction.aggregateSamplesToPeriod(lastPos, samplesInPeriod, intervalStart, intervalEnd, allSamples, mainTask);

                    value = 0d;
                    listMedian = new ArrayList<>();
                    for (JsonSample sample : samplesInPeriod) {
                        Double currentValue = Double.parseDouble(sample.getValue());
                        value += currentValue;
                        min = Math.min(min, currentValue);
                        max = Math.max(max, currentValue);
                        listMedian.add(currentValue);

                        if (!hasSamples) hasSamples = true;
                        if (dateTime == null) dateTime = new DateTime(sample.getTs());
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

                    JsonSample jsonSample = new JsonSample();
                    jsonSample.setTs(dateTime.toString());
                    jsonSample.setValue(value.toString());

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            }

        } else if (mode.equals(ManipulationMode.RUNNING_MEAN)) {
            if (allSamples.size() > 1) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    for (int i = 1; i < allSamples.size() - 1; i++) {

                        Double value0 = Double.parseDouble(allSamples.get(i - 1).getValue());
                        Double value1 = Double.parseDouble(allSamples.get(i).getValue());

                        Double currentValue = 1d / 2d * (value0 + value1);
                        DateTime newTS = new DateTime(allSamples.get(i).getTs());

                        JsonSample smp = new JsonSample();
                        smp.setTs(newTS.toString());
                        smp.setValue(currentValue.toString());

                        String note = allSamples.get(i).getNote();
                        if (note == null || note.equals(""))
                            note = "math(" + ManipulationMode.RUNNING_MEAN + ")";
                        else note += ",math(" + ManipulationMode.RUNNING_MEAN + ")";
                        smp.setNote(note);

                        listManipulation.add(smp);

                        if (!hasSamples) hasSamples = true;

                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                } else {
                    AggregatorFunction.Aggregate aggregate = new AggregatorFunction.Aggregate(jsonSampleGenerator, mainTask, null, allSamples, intervals, hasSamples, unit).invokeJson();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JsonSample> aggregatedSamples = aggregate.getAggregatedJsonSamples();

                    for (int i = 1; i < aggregatedSamples.size() - 1; i++) {
                        Double value0 = Double.parseDouble(aggregatedSamples.get(i - 1).getValue());
                        Double value1 = Double.parseDouble(aggregatedSamples.get(i).getValue());

                        Double currentValue = 1d / 2d * (value0 + value1);
                        DateTime newTS = new DateTime(aggregatedSamples.get(i).getTs());


                        JsonSample smp = new JsonSample();
                        smp.setTs(newTS.toString());
                        smp.setValue(currentValue.toString());

                        String note = aggregatedSamples.get(i).getNote();
                        if (note == null || note.equals(""))
                            note = "math(" + ManipulationMode.RUNNING_MEAN + ")";
                        else note += ",math(" + ManipulationMode.RUNNING_MEAN + ")";
                        smp.setNote(note);

                        listManipulation.add(smp);

                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            }
        } else if (mode.equals(ManipulationMode.CENTRIC_RUNNING_MEAN)) {
            if (allSamples.size() > 2) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    for (int i = 1; i < allSamples.size() - 1; i++) {

                        Double value0 = Double.parseDouble(allSamples.get(i - 1).getValue());
                        Double value1 = Double.parseDouble(allSamples.get(i).getValue());
                        Double value2 = Double.parseDouble(allSamples.get(i + 1).getValue());

                        Double currentValue = 1d / 3d * (value0 + value1 + value2);
                        DateTime newTS = new DateTime(allSamples.get(i).getTs());

                        JsonSample smp = new JsonSample();
                        smp.setTs(newTS.toString());
                        smp.setValue(currentValue.toString());

                        String note = allSamples.get(i).getNote();
                        if (note == null || note.equals(""))
                            note = "math(" + ManipulationMode.CENTRIC_RUNNING_MEAN + ")";
                        else note += ",math(" + ManipulationMode.CENTRIC_RUNNING_MEAN + ")";
                        smp.setNote(note);

                        listManipulation.add(smp);

                        if (!hasSamples) hasSamples = true;

                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                } else {
                    AggregatorFunction.Aggregate aggregate = new AggregatorFunction.Aggregate(jsonSampleGenerator, mainTask, null, allSamples, intervals, hasSamples, unit).invokeJson();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JsonSample> aggregatedSamples = aggregate.getAggregatedJsonSamples();

                    for (int i = 1; i < aggregatedSamples.size() - 1; i++) {

                        Double value0 = Double.parseDouble(aggregatedSamples.get(i - 1).getValue());
                        Double value1 = Double.parseDouble(aggregatedSamples.get(i).getValue());
                        Double value2 = Double.parseDouble(aggregatedSamples.get(i + 1).getValue());

                        Double currentValue = 1d / 3d * (value0 + value1 + value2);
                        DateTime newTS = new DateTime(aggregatedSamples.get(i).getTs());

                        JsonSample smp = new JsonSample();
                        smp.setTs(newTS.toString());
                        smp.setValue(currentValue.toString());

                        String note = aggregatedSamples.get(i).getNote();
                        if (note == null || note.equals(""))
                            note = "math(" + ManipulationMode.CENTRIC_RUNNING_MEAN + ")";
                        else note += ",math(" + ManipulationMode.CENTRIC_RUNNING_MEAN + ")";
                        smp.setNote(note);

                        listManipulation.add(smp);

                        if (!hasSamples) hasSamples = true;

                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            }
        } else if (mode.equals(ManipulationMode.SORTED_MIN) || mode.equals(ManipulationMode.SORTED_MAX)) {
            if (allSamples.size() > 1) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    DateTime firstDate = new DateTime(allSamples.get(0).getTs());
                    Period period = new Period(new DateTime(allSamples.get(0).getTs()), new DateTime(allSamples.get(1).getTs()));

                    if (mode.equals(ManipulationMode.SORTED_MIN)) {
                        allSamples.sort(Comparator.comparingDouble(o -> Double.parseDouble(o.getValue())));
                    } else if (mode.equals(ManipulationMode.SORTED_MAX)) {
                        allSamples.sort((o1, o2) -> Double.compare(Double.parseDouble(o2.getValue()), Double.parseDouble(o1.getValue())));
                    }

                    for (JsonSample sample : allSamples) {
                        JsonSample smp = new JsonSample();
                        smp.setTs(firstDate.plus(allSamples.indexOf(sample) * period.toStandardDuration().getMillis()).toString());
                        smp.setValue(sample.getValue());
                        smp.setNote("math(" + mode.toString() + ")");
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                } else {
                    AggregatorFunction.Aggregate aggregate = new AggregatorFunction.Aggregate(jsonSampleGenerator, mainTask, null, allSamples, intervals, hasSamples, unit).invokeJson();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JsonSample> aggregatedSamples = aggregate.getAggregatedJsonSamples();

                    DateTime firstDate = new DateTime(aggregatedSamples.get(0).getTs());
                    Period period = new Period(new DateTime(aggregatedSamples.get(0).getTs()), new DateTime(aggregatedSamples.get(1).getTs()));

                    if (mode.equals(ManipulationMode.SORTED_MIN)) {
                        aggregatedSamples.sort(Comparator.comparingDouble(o -> Double.parseDouble(o.getValue())));
                    } else if (mode.equals(ManipulationMode.SORTED_MAX)) {
                        aggregatedSamples.sort((o1, o2) -> Double.compare(Double.parseDouble(o2.getValue()), Double.parseDouble(o1.getValue())));
                    }

                    for (JsonSample sample : aggregatedSamples) {
                        JsonSample smp = new JsonSample();
                        smp.setTs(firstDate.plus(aggregatedSamples.indexOf(sample) * period.toStandardDuration().getMillis()).toString());
                        smp.setValue(sample.getValue());
                        smp.setNote("math(" + mode.toString() + ")");
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);
                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            }
        } else if (mode.equals(ManipulationMode.CUMULATE)) {
            if (allSamples.size() > 1) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    DateTime firstDate = new DateTime(allSamples.get(0).getTs());
                    Period period = new Period(new DateTime(allSamples.get(0).getTs()), new DateTime(allSamples.get(1).getTs()));

                    Double lastValue = 0d;
                    for (JsonSample sample : allSamples) {
                        Double cumulatedValue = Double.parseDouble(sample.getValue()) + lastValue;
                        JsonSample smp = new JsonSample();
                        smp.setTs(firstDate.plus(allSamples.indexOf(sample) * period.toStandardDuration().getMillis()).toString());
                        smp.setValue(cumulatedValue.toString());
                        lastValue = cumulatedValue;
                        smp.setNote("math(" + mode.toString() + ")");
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);

                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                } else {
                    AggregatorFunction.Aggregate aggregate = new AggregatorFunction.Aggregate(jsonSampleGenerator, mainTask, null, allSamples, intervals, hasSamples, unit).invokeJson();
                    hasSamples = aggregate.getHasSamples();
                    unit = aggregate.getUnit();
                    List<JsonSample> aggregatedSamples = aggregate.getAggregatedJsonSamples();

                    DateTime firstDate = new DateTime(aggregatedSamples.get(0).getTs());
                    Period period = new Period(new DateTime(aggregatedSamples.get(0).getTs()), new DateTime(aggregatedSamples.get(1).getTs()));

                    Double lastValue = 0d;
                    for (JsonSample sample : aggregatedSamples) {
                        Double cumulatedValue = Double.parseDouble(sample.getValue()) + lastValue;
                        JsonSample smp = new JsonSample();
                        smp.setTs(firstDate.plus(aggregatedSamples.indexOf(sample) * period.toStandardDuration().getMillis()).toString());
                        smp.setValue(cumulatedValue.toString());
                        lastValue = cumulatedValue;
                        smp.setNote("math(" + mode.toString() + ")");
                        listManipulation.add(smp);
                        if (!hasSamples) hasSamples = true;
                    }

                    result.addAll(listManipulation);
                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            }
        }

        return result;
    }

    @Override
    public void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }

}