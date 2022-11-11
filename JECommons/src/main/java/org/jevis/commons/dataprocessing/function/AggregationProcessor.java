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
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class AggregationProcessor {
    private static final Logger logger = LogManager.getLogger(AggregationProcessor.class);
    private final SQLDataSource ds;
    private final WorkDays workDays;
    private final AggregationPeriod aggregation;
    private final AggregationTools aggregationTools;

    public AggregationProcessor(SQLDataSource ds, AggregationTools aggregationTools, WorkDays workDays, AggregationPeriod aggregation) {
        this.ds = ds;
        this.workDays = workDays;
        this.aggregation = aggregation;
        this.aggregationTools = aggregationTools;
    }

    public static List<DateTime> getAllJsonTimestamps(List<JsonSample> samples) {
        List<DateTime> result = new ArrayList<>();

        for (JsonSample sample : samples) {
            if (!result.contains(new DateTime(sample.getTs()))) {
                result.add(new DateTime(sample.getTs()));
            }
        }

        Collections.sort(result);

        return result;
    }

    private int getLastPos(List<JsonSample> result, JsonAttribute jsonAttribute, int lastPos, List<Interval> emptyIntervals, JEVisUnit unit, Interval interval, List<JsonSample> samplesInPeriod, DateTime intervalStart, DateTime intervalEnd, Period newPeriod, List<JsonSample> samples) {
        Period oldPeriod = null;
        if (samples.size() > 1) {
            oldPeriod = new Period(new DateTime(samples.get(0).getTs()), new DateTime(samples.get(1).getTs()));
        }

        lastPos = aggregateSamplesToPeriod(lastPos, samplesInPeriod, intervalStart, intervalEnd, samples);

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
        isQuantity = qu.isQuantityIfCleanData(ds, jsonAttribute, isQuantity);

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

    public int aggregateSamplesToPeriod(int lastPos, List<JsonSample> samplesInPeriod, DateTime intervalStart, DateTime intervalEnd, List<JsonSample> samples) {
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

    public List<JsonSample> getJsonResult(List<JsonSample> inputSamples, JsonAttribute jsonAttribute, DateTime from, DateTime to) {
        List<JsonSample> result = new ArrayList<>();

        try {
            List<DateTime> allTimestamps = getAllJsonTimestamps(inputSamples);
            if (allTimestamps.isEmpty()) {
                return result;
            }

            List<Interval> intervals = aggregationTools.getIntervals(from, to, aggregation);

            boolean isCustomWorkDay = workDays.isCustomWorkDay();

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
                    LocalTime workdayStart = workDays.getWorkdayStart(intervalStart);
                    intervalStart = intervalStart.withHourOfDay(workdayStart.getHour())
                            .withMinuteOfHour(workdayStart.getMinute())
                            .withSecondOfMinute(workdayStart.getSecond());

                    LocalTime workdayEnd = workDays.getWorkdayEnd(intervalStart);
                    intervalEnd = intervalEnd.withHourOfDay(workdayEnd.getHour())
                            .withMinuteOfHour(workdayEnd.getMinute())
                            .withSecondOfMinute(workdayEnd.getSecond());

                    if (workdayEnd.isBefore(workdayStart)) {
                        intervalStart = intervalStart.minusDays(1);
                        intervalEnd = intervalEnd.minusDays(1).plusSeconds(1);
                    }
                }

                lastPos = getLastPos(result, jsonAttribute, lastPos, emptyIntervals, unit, interval, samplesInPeriod, intervalStart, intervalEnd, newPeriod, inputSamples);
            }

            if (from != null && to != null) {
                for (Interval emptyInterval : emptyIntervals) {
                    if (emptyInterval.getStart().equals(from)
                            || (emptyInterval.getStart().isAfter(from) && emptyInterval.getStart().isBefore(to))
                            || emptyInterval.getStart().equals(to)) {
                        JsonSample resultSum = new JsonSample();
                        resultSum.setTs(emptyInterval.getStart().toString());
                        resultSum.setValue(Double.toString(0d));

                        resultSum.setNote("Aggregation(" + emptyInterval.getStart().toString() + "/" + emptyInterval.getEnd().toString() + ")");
                        result.add(resultSum);
                    }
                }
            }

            result.sort((o1, o2) -> DateTimeComparator.getInstance().compare(new DateTime(o1.getTs()), new DateTime(o2.getTs())));
        } catch (Exception e) {
            logger.error("Could not generate aggregated samples", e);
        }

        return result;
    }
}
