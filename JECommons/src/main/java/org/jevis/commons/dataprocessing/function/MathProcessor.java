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
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class MathProcessor {
    private static final Logger logger = LogManager.getLogger(MathProcessor.class);
    private final SQLDataSource ds;
    private final AggregationTools aggregationTools;
    private final WorkDays workDays;
    private final AggregationPeriod aggregationPeriod;
    private final ManipulationMode mode;

    public MathProcessor(SQLDataSource ds, AggregationTools aggregationTools, WorkDays workDays, ManipulationMode mode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.aggregationTools = aggregationTools;
        this.workDays = workDays;
        this.mode = mode;
        this.aggregationPeriod = aggregationPeriod;
    }

    public List<JsonSample> getJsonResult(List<JsonSample> inputSamples, JsonAttribute jsonAttribute, DateTime from, DateTime to) {

        List<JsonSample> result = new ArrayList<>();

        try {
            Boolean hasSamples = false;

            List<Interval> intervals = new ArrayList<>();
            AggregationProcessor aggregation = null;
            if (aggregationPeriod != AggregationPeriod.NONE && aggregationTools != null) {
                aggregation = new AggregationProcessor(ds, aggregationTools, workDays, aggregationPeriod);
                intervals.addAll(aggregationTools.getIntervals(from, to, aggregationPeriod));
            }

            Double value = 0d;
            Double min = Double.MAX_VALUE;
            Double max = Double.MIN_VALUE;
            List<Double> listMedian = new ArrayList<>();

            DateTime dateTime = null;

            List<JsonSample> listManipulation = new ArrayList<>();

            JEVisUnit unit = new JEVisUnitImp(jsonAttribute.getDisplayUnit());

            if (mode.equals(ManipulationMode.AVERAGE) || mode.equals(ManipulationMode.MIN)
                    || mode.equals(ManipulationMode.MAX) || mode.equals(ManipulationMode.MEDIAN)) {
                if (aggregationPeriod == AggregationPeriod.NONE) {
                    for (JsonSample smp : inputSamples) {
                        Double currentValue = Double.parseDouble(smp.getValue());
                        value += currentValue;
                        min = java.lang.Math.min(min, currentValue);
                        max = java.lang.Math.max(max, currentValue);
                        listMedian.add(currentValue);

                        if (!hasSamples) hasSamples = true;
                        if (dateTime == null) dateTime = new DateTime(smp.getTs());
                    }

                    if (hasSamples) {
                        if (mode.equals(ManipulationMode.AVERAGE)) {
                            value = value / (double) inputSamples.size();
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
                    value = 0d;
                    listMedian = new ArrayList<>();
                    int lastPos = 0;

                    for (Interval interval : intervals) {
                        List<JsonSample> samplesInPeriod = new ArrayList<>();

                        DateTime intervalStart = interval.getStart();
                        DateTime intervalEnd = interval.getEnd();
                        dateTime = intervalStart;

                        lastPos = aggregation.aggregateSamplesToPeriod(lastPos, samplesInPeriod, intervalStart, intervalEnd, inputSamples);

                        for (JsonSample sample : samplesInPeriod) {
                            Double currentValue = Double.parseDouble(sample.getValue());
                            value += currentValue;
                            min = java.lang.Math.min(min, currentValue);
                            max = java.lang.Math.max(max, currentValue);
                            listMedian.add(currentValue);
                            if (!hasSamples) hasSamples = true;
                        }

                        QuantityUnits qu = new QuantityUnits();
                        boolean isQuantity = qu.isQuantityUnit(unit);
                        isQuantity = qu.isQuantityIfCleanData(ds, jsonAttribute, isQuantity);
                        if (hasSamples && !isQuantity) {
                            value = value / samplesInPeriod.size();
                        }
                    }

                    if (hasSamples) {
                        if (mode.equals(ManipulationMode.AVERAGE)) {
                            value = value / (double) inputSamples.size();
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
                }
            } else if (mode.equals(ManipulationMode.RUNNING_MEAN)) {
                if (inputSamples.size() > 1) {
                    if (aggregationPeriod == AggregationPeriod.NONE) {
                        for (int i = 1; i < inputSamples.size() - 1; i++) {

                            Double value0 = Double.parseDouble(inputSamples.get(i - 1).getValue());
                            Double value1 = Double.parseDouble(inputSamples.get(i).getValue());

                            Double currentValue = 1d / 2d * (value0 + value1);
                            DateTime newTS = new DateTime(inputSamples.get(i).getTs());

                            JsonSample smp = new JsonSample();
                            smp.setTs(newTS.toString());
                            smp.setValue(currentValue.toString());

                            String note = inputSamples.get(i).getNote();
                            if (note == null || note.equals(""))
                                note = "math(" + ManipulationMode.RUNNING_MEAN + ")";
                            else note += ",math(" + ManipulationMode.RUNNING_MEAN + ")";
                            smp.setNote(note);

                            listManipulation.add(smp);

                            if (!hasSamples) hasSamples = true;
                        }
                    } else {
                        List<JsonSample> aggregatedSamples = aggregation.getJsonResult(inputSamples, jsonAttribute, from, to);

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
                    }

                    result.addAll(listManipulation);
                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            } else if (mode.equals(ManipulationMode.CENTRIC_RUNNING_MEAN)) {
                if (inputSamples.size() > 2) {
                    if (aggregationPeriod == AggregationPeriod.NONE) {
                        for (int i = 1; i < inputSamples.size() - 1; i++) {

                            Double value0 = Double.parseDouble(inputSamples.get(i - 1).getValue());
                            Double value1 = Double.parseDouble(inputSamples.get(i).getValue());
                            Double value2 = Double.parseDouble(inputSamples.get(i + 1).getValue());

                            Double currentValue = 1d / 3d * (value0 + value1 + value2);
                            DateTime newTS = new DateTime(inputSamples.get(i).getTs());

                            JsonSample smp = new JsonSample();
                            smp.setTs(newTS.toString());
                            smp.setValue(currentValue.toString());

                            String note = inputSamples.get(i).getNote();
                            if (note == null || note.equals(""))
                                note = "math(" + ManipulationMode.CENTRIC_RUNNING_MEAN + ")";
                            else note += ",math(" + ManipulationMode.CENTRIC_RUNNING_MEAN + ")";
                            smp.setNote(note);

                            listManipulation.add(smp);

                            if (!hasSamples) hasSamples = true;
                        }
                    } else {
                        List<JsonSample> aggregatedSamples = aggregation.getJsonResult(inputSamples, jsonAttribute, from, to);

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
                    }

                    result.addAll(listManipulation);
                    logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                }
            } else if (mode.equals(ManipulationMode.SORTED_MIN) || mode.equals(ManipulationMode.SORTED_MAX)) {
                if (inputSamples.size() > 1) {
                    if (aggregationPeriod == AggregationPeriod.NONE) {
                        DateTime firstDate = new DateTime(inputSamples.get(0).getTs());
                        Period period = new Period(new DateTime(inputSamples.get(0).getTs()), new DateTime(inputSamples.get(1).getTs()));

                        if (mode.equals(ManipulationMode.SORTED_MIN)) {
                            inputSamples.sort(Comparator.comparingDouble(o -> Double.parseDouble(o.getValue())));
                        } else if (mode.equals(ManipulationMode.SORTED_MAX)) {
                            inputSamples.sort((o1, o2) -> Double.compare(Double.parseDouble(o2.getValue()), Double.parseDouble(o1.getValue())));
                        }

                        for (JsonSample sample : inputSamples) {
                            JsonSample smp = new JsonSample();
                            smp.setTs(firstDate.plus(inputSamples.indexOf(sample) * period.toStandardDuration().getMillis()).toString());
                            smp.setValue(sample.getValue());
                            smp.setNote("math(" + mode.toString() + ")");
                            listManipulation.add(smp);
                            if (!hasSamples) hasSamples = true;
                        }

                        result.addAll(listManipulation);

                        logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                    } else {
                        List<JsonSample> aggregatedSamples = aggregation.getJsonResult(inputSamples, jsonAttribute, from, to);

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
                if (inputSamples.size() > 1) {
                    if (aggregationPeriod == AggregationPeriod.NONE) {
                        DateTime firstDate = new DateTime(inputSamples.get(0).getTs());
                        Period period = new Period(new DateTime(inputSamples.get(0).getTs()), new DateTime(inputSamples.get(1).getTs()));

                        Double lastValue = 0d;
                        for (JsonSample sample : inputSamples) {
                            Double cumulatedValue = Double.parseDouble(sample.getValue()) + lastValue;
                            JsonSample smp = new JsonSample();
                            smp.setTs(firstDate.plus(inputSamples.indexOf(sample) * period.toStandardDuration().getMillis()).toString());
                            smp.setValue(cumulatedValue.toString());
                            lastValue = cumulatedValue;
                            smp.setNote("math(" + mode.toString() + ")");
                            listManipulation.add(smp);
                            if (!hasSamples) hasSamples = true;
                        }

                        result.addAll(listManipulation);

                        logger.info("Result is: {} : {} {}", dateTime, value, UnitManager.getInstance().format(unit));
                    } else {
                        List<JsonSample> aggregatedSamples = aggregation.getJsonResult(inputSamples, jsonAttribute, from, to);

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
        } catch (Exception e) {
            logger.error("Could not generate math samples", e);
        }

        return result;
    }
}