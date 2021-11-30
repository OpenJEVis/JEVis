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
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.dataprocessing.ProcessOptions.getAllJsonTimestamps;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class AggregatorFunction implements ProcessFunction {
    public static final String NAME = "Aggregator";
    private static final Logger logger = LogManager.getLogger(AggregatorFunction.class);
    private JsonSampleGenerator jsonSampleGenerator;

    public AggregatorFunction() {
    }

    public AggregatorFunction(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }

    private static int getLastPos(Process mainTask, List<JsonSample> result, JsonAttribute jsonAttribute, int lastPos, List<Interval> emptyIntervals, JEVisUnit unit, Interval interval, List<JsonSample> samplesInPeriod, DateTime intervalStart, DateTime intervalEnd, Period newPeriod, List<JsonSample> samples) {
        Period oldPeriod = null;
        if (samples.size() > 1) {
            oldPeriod = new Period(new DateTime(samples.get(0).getTs()), new DateTime(samples.get(1).getTs()));
        }

        lastPos = aggregateSamplesToPeriod(lastPos, samplesInPeriod, intervalStart, intervalEnd, samples, mainTask);

        boolean hasSamples = false;
        Double sum = 0d;

        for (JsonSample sample : samplesInPeriod) {
            sum += Double.parseDouble(sample.getValue());
            hasSamples = true;
        }

        /**
         * if its not a quantity the aggregated total value results from the mean value of all samples in a period, not their sum
         */

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(unit);
        isQuantity = qu.isQuantityIfCleanData(mainTask.getSqlDataSource(), jsonAttribute, isQuantity);

        if (hasSamples && !isQuantity) {
            sum = sum / samplesInPeriod.size();
        }

        if (hasSamples) {
            JsonSample resultSum = new JsonSample();
            resultSum.setTs(intervalStart.toString());
            resultSum.setValue(sum.toString());
            if (oldPeriod != null && newPeriod != null) {
                resultSum.setNote("Aggregation(" + oldPeriod + "/" + newPeriod + "," + samplesInPeriod.size() + ")");
            }
            result.add(resultSum);
            logger.debug("created aggregation sample: {}", resultSum.toString());
        } else {
            emptyIntervals.add(interval);
        }
        return lastPos;
    }

    public static int aggregateSamplesToPeriod(int lastPos, List<JsonSample> samplesInPeriod, DateTime intervalStart, DateTime intervalEnd, List<JsonSample> samples, Process mainTask) {
        for (int i = lastPos; i < samples.size(); i++) {
            DateTime sampleTS = new DateTime(samples.get(i).getTs());
            if ((sampleTS.equals(intervalStart) || (sampleTS.isAfter(intervalStart) && sampleTS.isBefore(intervalEnd)))) {
                //logger.info("add sample: " + samples.get(i));
                samplesInPeriod.add(samples.get(i));
                logger.debug("aggregate {} to interval {}-{} ", samples.get(i), intervalStart, intervalEnd);
            } else if (sampleTS.isAfter(intervalEnd) && i > 0) {
                lastPos = i - 1;
                break;
            }
        }
        return lastPos;
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

    @Override
    public List<JsonSample> getJsonResult(BasicProcess mainTask) {
        List<JsonSample> result = new ArrayList<>();

        List<List<JsonSample>> allSamples = new ArrayList<>();
        for (Process task : mainTask.getSubProcesses()) {
            allSamples.add(task.getJsonResult());
        }

        List<DateTime> allTimestamps = getAllJsonTimestamps(allSamples);
        if (allTimestamps.isEmpty()) {
            return result;
        }

        StartAndEndDates startAndEndDates = new StartAndEndDates(mainTask).invoke();

//        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, allTimestamps.get(0), allTimestamps.get(allTimestamps.size() - 1));
        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, startAndEndDates.getStart(), startAndEndDates.getEnd());

        JsonAttribute jsonAttribute = mainTask.getJsonAttribute();

        boolean isCustomWorkDay = jsonSampleGenerator.getCustomWorkday();
        WorkDays wd = jsonSampleGenerator.getWorkDays();
        isCustomWorkDay = wd.isCustomWorkDay();

        int lastPos = 0;
        List<Interval> emptyIntervals = new ArrayList<>();
        JEVisUnit unit = new JEVisUnitImp(jsonAttribute.getDisplayUnit());
        for (Interval interval : intervals) {
            List<JsonSample> samplesInPeriod = new ArrayList<>();
            DateTime intervalStart = interval.getStart();
            DateTime intervalEnd = interval.getEnd();

            Period newPeriod = null;
            try {
                newPeriod = new Period(intervalStart, intervalEnd);
            } catch (Exception e) {
                logger.error("Could not get new Period: ", e);
            }

            if (isCustomWorkDay && newPeriod != null && PeriodHelper.isGreaterThenDays(newPeriod)) {
                intervalStart = intervalStart.withHourOfDay(wd.getWorkdayStart().getHour())
                        .withMinuteOfHour(wd.getWorkdayStart().getMinute())
                        .withSecondOfMinute(wd.getWorkdayStart().getSecond());

                intervalEnd = intervalEnd.withHourOfDay(wd.getWorkdayEnd().getHour())
                        .withMinuteOfHour(wd.getWorkdayEnd().getMinute())
                        .withSecondOfMinute(wd.getWorkdayEnd().getSecond());

                if (wd.getWorkdayEnd().isBefore(wd.getWorkdayStart())) {
                    intervalStart = intervalStart.minusDays(1);
                    intervalEnd = intervalEnd.minusDays(1).plusSeconds(1);
                }
            }

            for (List<JsonSample> samples : allSamples) {
                lastPos = getLastPos(mainTask, result, jsonAttribute, lastPos, emptyIntervals, unit, interval, samplesInPeriod, intervalStart, intervalEnd, newPeriod, samples);
            }
        }

//        StartAndEndDates startAndEndDates = new StartAndEndDates(mainTask).invoke();
        DateTime start = startAndEndDates.getStart();
        DateTime end = startAndEndDates.getEnd();

        if (start != null && end != null) {
            for (Interval emptyInterval : emptyIntervals) {
                if (emptyInterval.getStart().equals(start)
                        || (emptyInterval.getStart().isAfter(start) && emptyInterval.getStart().isBefore(end))
                        || emptyInterval.getStart().equals(end)) {
                    JsonSample resultSum = new JsonSample();
                    resultSum.setTs(emptyInterval.getStart().toString());
                    resultSum.setValue(Double.toString(0d));

                    resultSum.setNote("Aggregation(" + emptyInterval.getStart().toString() + "/" + emptyInterval.getEnd().toString() + ")");
                    result.add(resultSum);
                }
            }
        }

        result.sort((o1, o2) -> DateTimeComparator.getInstance().compare(new DateTime(o1.getTs()), new DateTime(o2.getTs())));

        return result;

    }

    @Override
    public void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }


    static class Aggregate {
        private final Process mainTask;
        private final List<JEVisSample> allSamples;
        private final List<JsonSample> allJsonSamples;
        private final List<Interval> intervals;
        private final JsonSampleGenerator jsonSampleGenerator;
        private Boolean hasSamples;
        private JEVisUnit unit;
        private List<JEVisSample> aggregatedSamples;
        private List<JsonSample> aggregatedJsonSamples;

        public Aggregate(JsonSampleGenerator jsonSampleGenerator, Process mainTask, List<JEVisSample> allSamples, List<JsonSample> allJsonSamples, List<Interval> intervals, Boolean hasSamples, JEVisUnit unit) {
            this.jsonSampleGenerator = jsonSampleGenerator;
            this.mainTask = mainTask;
            this.allSamples = allSamples;
            this.allJsonSamples = allJsonSamples;
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

        public List<JsonSample> getAggregatedJsonSamples() {
            return aggregatedJsonSamples;
        }

        public Aggregate invoke() {
            JEVisAttribute attribute = null;
            aggregatedSamples = new ArrayList<>();
            if (allSamples.size() > 0) {
                try {
                    attribute = allSamples.get(0).getAttribute();
                } catch (Exception e) {
                    logger.error("Could not get Attribute: ", e);
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
                    } catch (Exception e) {
                        logger.error("Could not get old Period: ", e);
                    }
                }

                Period newPeriod = null;
                try {
                    newPeriod = new Period(interval.getStart(), interval.getEnd());
                } catch (Exception e) {
                    logger.error("Could not get new Period: ", e);
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
                    } catch (Exception ex) {
                        logger.fatal("Exception while going through sample: {}", ex.getMessage(), ex);
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
                    } catch (Exception ex) {
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
                            resultSum.setNote("Aggregation(" + oldPeriod + "/" + newPeriod + ")");
                        } catch (Exception e) {
                            logger.error("Could not set new Note to sample: ", e);
                        }
                    }
                    aggregatedSamples.add(resultSum);
                }

            }
            return this;
        }

        public Aggregate invokeJson() {
            aggregatedJsonSamples = new ArrayList<>();

            boolean isCustomWorkDay = jsonSampleGenerator.getCustomWorkday();
            WorkDays wd = jsonSampleGenerator.getWorkDays();

            List<Interval> emptyIntervals = new ArrayList<>();
            int lastPos = 0;
            for (Interval interval : intervals) {
                List<JsonSample> samplesInPeriod = new ArrayList<>();
                DateTime intervalStart = interval.getStart();
                DateTime intervalEnd = interval.getEnd();

                Period newPeriod = null;
                try {
                    newPeriod = new Period(interval.getStart(), interval.getEnd());
                } catch (Exception e) {
                    logger.error("Could not get new Period: ", e);
                }

                if (isCustomWorkDay && newPeriod != null && PeriodHelper.isGreaterThenDays(newPeriod)) {
                    intervalStart = intervalStart.withHourOfDay(wd.getWorkdayStart().getHour())
                            .withMinuteOfHour(wd.getWorkdayStart().getMinute())
                            .withSecondOfMinute(wd.getWorkdayStart().getSecond());

                    intervalEnd = intervalEnd.withHourOfDay(wd.getWorkdayEnd().getHour())
                            .withMinuteOfHour(wd.getWorkdayEnd().getMinute())
                            .withSecondOfMinute(wd.getWorkdayEnd().getSecond());

                    if (wd.getWorkdayEnd().isBefore(wd.getWorkdayStart())) intervalStart = intervalStart.minusDays(1);
                }

                JsonAttribute jsonAttribute = mainTask.getJsonAttribute();

                lastPos = getLastPos(mainTask, aggregatedJsonSamples, jsonAttribute, lastPos, emptyIntervals, unit, interval, samplesInPeriod, intervalStart, intervalEnd, newPeriod, allJsonSamples);

            }
            StartAndEndDates startAndEndDates = new StartAndEndDates(mainTask).invoke();
            DateTime start = startAndEndDates.getStart();
            DateTime end = startAndEndDates.getEnd();

            if (start != null && end != null) {
                for (Interval emptyInterval : emptyIntervals) {
                    if (emptyInterval.getStart().equals(start)
                            || (emptyInterval.getStart().isAfter(start) && emptyInterval.getStart().isBefore(end))
                            || emptyInterval.getStart().equals(end)) {
                        JsonSample resultSum = new JsonSample();
                        resultSum.setTs(emptyInterval.getStart().toString());
                        resultSum.setValue(Double.toString(0d));

                        resultSum.setNote("Aggregation(" + emptyInterval.getStart().toString() + "/" + emptyInterval.getEnd().toString() + ")");
                        aggregatedJsonSamples.add(resultSum);
                    }
                }
            }

            aggregatedJsonSamples.sort((o1, o2) -> DateTimeComparator.getInstance().compare(new DateTime(o1.getTs()), new DateTime(o2.getTs())));

            return this;
        }
    }
}
