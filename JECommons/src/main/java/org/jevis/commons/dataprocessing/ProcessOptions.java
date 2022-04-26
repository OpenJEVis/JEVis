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
package org.jevis.commons.dataprocessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.commons.config.Options;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

import java.util.*;

/**
 * This class contains various methods for manipulating ProcessOptions.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ProcessOptions {
    public static final String PERIOD = "period";
    public static final String OFFSET = "offset";
    public static final String TIMEZONE = "timezone";
    public static final String CUSTOM = "custom";
    public static final String TS_START = "date-start";
    public static final String TS_END = "date-end";
    private static final Logger logger = LogManager.getLogger(ProcessOptions.class);
    public static String ROOT_OPTION_NAME = "Data Processing";
    public static String DEFAULT_OPTION_NAME = "Default";
    public static String PROCESS_CHAIN_OPTION_NAME = "Process Chains";//Rename into ProcessChain

    public static List<ProcessOption> ToProcessOption(Process process, JEVisOption option) {
        List<ProcessOption> pOptions = new ArrayList<>();

        for (JEVisOption jOpt : option.getOptions()) {
            ProcessOption po = new BasicProcessOption(jOpt.getKey(), jOpt.getValue());

            //Problem: JEVisOptions are hierarchic and the Processoption are flat but the Process him self has an hierarchy
            //match option to the right process
            //? replace ProcessOption with JEVisOption
            pOptions.add(po);

        }

        return pOptions;
    }

    //@todo: add recursion?!
    public static boolean ContainsOption(Process task, String optionKey) {
        for (ProcessOption option : task.getOptions()) {
            if (option.getKey().equalsIgnoreCase(optionKey)) {
                return true;
            }
        }

        return false;
    }

    public static ProcessOption GetLatestOption(Process task, String key, ProcessOption defaultOption) {
        List<ProcessOption> options = new ArrayList<>();
        FindOptions(task, key, options);

        if (options.isEmpty()) {
            return defaultOption;
        } else {
            //TODO take latest based on ts
            return options.get(options.size() - 1);
        }

    }

    public static List<ProcessOption> GetOption(Process task, String key) {
        List<ProcessOption> options = new ArrayList<>();
        FindOptions(task, key, options);
        return options;
    }

    /**
     * Recursion helper to get all Options with the key
     *
     * @param task
     * @param key
     * @param options
     */
    private static void FindOptions(Process task, String key, List<ProcessOption> options) {
        for (ProcessOption option : task.getOptions()) {
            if (option.getKey().equalsIgnoreCase(key)) {
                options.add(option);
            }
        }
    }

    public static void setStartEnd(Process task, DateTime from, DateTime until, boolean overwrite, boolean childrenToo) {

        if (ContainsOption(task, TS_START) || overwrite) {
            task.getOptions().add(new BasicProcessOption(TS_START, JEVisDates.DEFAULT_DATE_FORMAT.print(from)));
            task.getOptions().add(new BasicProcessOption(TS_END, JEVisDates.DEFAULT_DATE_FORMAT.print(until)));
        }

        if (childrenToo) {
            for (Process t : task.getSubProcesses()) {
                setStartEnd(t, from, until, overwrite, childrenToo);
            }

        }

    }

    /**
     * Returns the first and last timestamp of task options
     *
     * @param task
     * @return
     */
    public static DateTime[] getStartAndEnd(BasicProcess task) {

        DateTime[] result = new DateTime[2];
        result[0] = null;
        result[1] = null;

        if (ContainsOption(task, TS_START)) {
            try {
                result[0] = new DateTime(GetLatestOption(task, TS_START, new BasicProcessOption(TS_START, "")).getValue());
//                result[0] = DateTime.parse(task.getOptions().get(TS_START), DateTimeFormat.forPattern(TS_PATTERN));
            } catch (Exception e) {
                logger.error("error while parsing {} option", TS_START);
            }
        } else {
            logger.info("No {} option is missing", TS_START);
        }

        if (ContainsOption(task, TS_END)) {
            try {
                result[1] = new DateTime(GetLatestOption(task, TS_END, new BasicProcessOption(TS_END, "")).getValue());
//                result[1] = DateTime.parse(task.getOptions().get(TS_END), DateTimeFormat.forPattern(TS_PATTERN));
            } catch (Exception ex) {
                logger.error("error while parsing {} option", TS_END);
            }
        } else {
            logger.info("No {} option is missing", TS_END);
        }

        WorkDays wd = task.getJsonSampleGenerator().getWorkDays();
        if (result[0] != null && wd.getWorkdayEnd(result[0]).isBefore(wd.getWorkdayStart(result[0])))
            result[0] = result[0].minusDays(1);

        Period period = Period.ZERO;
        for (ProcessOption option : task.getOptions()) {
            String key = option.getKey();
            String value = option.getValue();
            if (PERIOD.equals(key)) {
                period = Period.parse(value);
            }
        }

        AggregationPeriod aggregationPeriod = task.getJsonSampleGenerator().getAggregationPeriod();
        if (result[0] != null && result[1] != null) {
            switch (aggregationPeriod) {
                default:
                    break;
                case QUARTER_HOURLY:
                    result[0] = result[0].withSecondOfMinute(0).withMillisOfSecond(0);
                    result[1] = result[1].plus(period);
                    break;
                case HOURLY:
                    result[0] = result[0].withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    result[1] = result[1].plus(period);
                    break;
                case DAILY:
                    result[0] = result[0].withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    result[1] = result[1].plus(period);
                    break;
                case WEEKLY:
                    result[0] = result[0].withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    result[1] = result[1].plus(period);
                    break;
                case MONTHLY:
                    result[0] = result[0].withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    result[1] = result[1].plus(period);
                    break;
                case QUARTERLY:
                    if (result[0].getMonthOfYear() < 4) {
                        result[0] = result[0].withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    } else if (result[0].getMonthOfYear() < 7) {
                        result[0] = result[0].withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    } else if (result[0].getMonthOfYear() < 10) {
                        result[0] = result[0].withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    } else {
                        result[0] = result[0].withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    }
                    result[1] = result[1].plus(period);
                    break;
                case YEARLY:
                    result[0] = result[0].withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    result[1] = result[1].plus(period);
                    break;
            }
        }

        return result;

    }

    /**
     * Return the first period matching the period, offset and target date
     *
     * @param date   target date to find the matching period
     * @param period period
     * @param offset start offset
     * @return
     */
    public static DateTime findFirstDuration(DateTime date, Period period, DateTime offset, DateTimeZone timeZone) {
//        logger.info("findFirstDuration: " + date + "   p: " + period);
        DateTime startD = new DateTime();
        DateTime firstPeriod = offset.withZone(timeZone);
//        logger.info("week: " + date.getWeekOfWeekyear());
//
        while (firstPeriod.isBefore(date) || firstPeriod.isEqual(date)) {
            firstPeriod = firstPeriod.plus(period);
        }
        firstPeriod = firstPeriod.minus(period);

        logger.info("finding date in: {} ms", ((new DateTime()).getMillis() - startD.getMillis()));
        logger.info("first offset date: offset: {} for period: {} input date: {} fistPeriod: {}", offset, period, date, firstPeriod);
        return firstPeriod;
    }

    /**
     * @return
     */
    public static DateTime getOffset(Process task) {
        return new DateTime(1990, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    }

    /**
     * @param period
     * @param offset
     * @param firstSample
     * @param lastSample
     * @param wd
     * @return
     */
    public static List<Interval> buildIntervals(Period period, DateTime offset, DateTime firstSample, DateTime lastSample, DateTimeZone timeZone, WorkDays wd) {
        DateTime benchMarkStart = new DateTime();
        List<Interval> result = new ArrayList<>();

        DateTime startDate = findFirstDuration(firstSample, period, offset, timeZone);

        if (wd != null && wd.isEnabled()) {
            LocalTime start = new LocalTime(wd.getWorkdayStart().getHour(), wd.getWorkdayStart().getMinute(), wd.getWorkdayStart().getSecond(), 0);
            startDate = startDate.withTime(start);
        }

        result.add(new Interval(startDate, period));

        boolean run = true;
//        DateTime time = startDate;
        while (run) {
            startDate = startDate.plus(period);
            if (startDate.isAfter(lastSample)) {
//                logger.info("wtf: " + startDate.getMillis() + "     " + lastSample.getMillis());
//                logger.info("failed Interval: " + startDate + "   is after   " + lastSample);
                run = false;
            } else {
                result.add(new Interval(startDate, period));
            }
        }

        DateTime benchMarkEnd = new DateTime();
        logger.info("Time to create Intervals[{}] in {} ms", result.size(), (benchMarkEnd.getMillis() - benchMarkStart.getMillis()));
        return result;
    }

    /**
     * Returns a list with every timestamp from a list of jevissample lists.
     * every timestamp is unique and sorted by datetime
     *
     * @param allSamples list of all timestamps.
     * @return
     */
    public static List<DateTime> getAllTimestamps(List<List<JEVisSample>> allSamples) {
        List<DateTime> result = new ArrayList<>();
        for (List<JEVisSample> samples : allSamples) {
            for (JEVisSample sample : samples) {
                try {
                    if (!result.contains(sample.getTimestamp())) {
                        result.add(sample.getTimestamp());
                    }
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }

        }
        Collections.sort(result);

        return result;
    }

    public static List<DateTime> getAllJsonTimestamps(List<List<JsonSample>> allSamples) {
        List<DateTime> result = new ArrayList<>();
        for (List<JsonSample> samples : allSamples) {
            for (JsonSample sample : samples) {
                if (!result.contains(new DateTime(sample.getTs()))) {
                    result.add(new DateTime(sample.getTs()));
                }
            }
        }
        Collections.sort(result);

        return result;
    }

    public static List<DateTime> getAllTimestampsSingleList(List<JEVisSample> allSamples) {
        List<DateTime> result = new ArrayList<>();
        for (JEVisSample sample : allSamples) {
            try {
                if (!result.contains(sample.getTimestamp())) {
                    result.add(sample.getTimestamp());
                }
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }

        }
        Collections.sort(result);

        return result;
    }

    public static List<DateTime> getAllJsonTimestampsSingleList(List<JsonSample> allSamples) {
        List<DateTime> result = new ArrayList<>();
        for (JsonSample sample : allSamples) {
            if (!result.contains(new DateTime(sample.getTs()))) {
                result.add(new DateTime(sample.getTs()));
            }

        }
        Collections.sort(result);

        return result;
    }

    /**
     * Returns an list of intervals
     *
     * @param task
     * @param from
     * @param until
     * @return
     */
    public static List<Interval> getIntervals(Process task, DateTime from, DateTime until) {
        Period period = Period.days(1);
        DateTime offset = new DateTime(1990, 1, 1, 0, 0, 0);
        DateTimeZone timeZone = DateTimeZone.UTC;
        WorkDays wd = null;
        AggregationPeriod aggregationPeriod = null;
        if (task instanceof BasicProcess) {
            BasicProcess basicProcess = (BasicProcess) task;
            wd = basicProcess.getJsonSampleGenerator().getWorkDays();
            aggregationPeriod = basicProcess.getJsonSampleGenerator().getAggregationPeriod();
        }

        if (!ContainsOption(task, PERIOD)) {
            logger.warn("Error missing period option");
        }
        if (!ContainsOption(task, OFFSET)) {
            logger.warn("Error missing offset option");

            task.getOptions().add(new BasicProcessOption(OFFSET, offset.toString()));
        }

        for (ProcessOption option : task.getOptions()) {
            String key = option.getKey();
            String value = option.getValue();
            logger.info("key: {}", key);
            switch (key) {
                case PERIOD:
                    logger.info("period string: {}", value);
                    period = Period.parse(value);
                    logger.info("parsed period: {}", period.toString(PeriodFormat.wordBased()));
                    break;
                case OFFSET:
                    //TODO check value formats
                    try {
                        offset = new DateTime(value);
                    } catch (Exception e) {
                        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                        offset = dtf.parseDateTime(value);
                    }
                    break;
                case TIMEZONE:
                    try {
                        timeZone = DateTimeZone.forID(value);
                    } catch (Exception e) {
                    }
                case CUSTOM:
                    if (wd != null && aggregationPeriod != null) {
                        switch (aggregationPeriod) {
                            case DAILY:
                            case WEEKLY:
                            case MONTHLY:
                            case QUARTERLY:
                            case YEARLY:
                                wd.setEnabled(Boolean.parseBoolean(value));
                                break;
                        }
                    }
                    break;
            }
        }

        return buildIntervals(period, offset, from, until, timeZone, wd);

    }

    /**
     * Cronverts an JEVIsSample list to an Map
     *
     * @param samples
     * @return
     * @throws JEVisException
     */
    public static Map<DateTime, JEVisSample> sampleListToMap(List<JEVisSample> samples) throws JEVisException {
        Map<DateTime, JEVisSample> result = new HashMap<>();

        for (JEVisSample sample : samples) {
            result.put(sample.getTimestamp(), sample);
        }

        return result;
    }

    /**
     * Check if this attribute has an dataProcessing option
     *
     * @param attribute
     * @return
     */
    public static boolean HasDataProcess(JEVisAttribute attribute) {
        JEVisOption opt = GetProcessOptionRoot(attribute);
        return opt != null;
    }

    /**
     * Returns the root Option node of this Attribute
     *
     * @param attribute
     * @return returns the dataProcessing root or null
     */
    public static JEVisOption GetProcessOptionRoot(JEVisAttribute attribute) {

        if (attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            logger.info("Optiones exists");
            for (JEVisOption option : attribute.getOptions()) {
                logger.info("opt: {}", option.getKey());
                if (option.getKey().equalsIgnoreCase(ROOT_OPTION_NAME)) {
//                    JEVisOption dpOption = option.getOption(ROOT_OPTION_NAME);

                    return option;
                }
            }
        }
        return null;

    }

    /**
     * Returns the name of the default DataProcess.
     *
     * @param att
     * @return Name of the default process, emty String if no default is set.
     */
    public static String GetDefaultProcessName(JEVisAttribute att) {
        JEVisOption rootOpt = GetProcessOptionRoot(att);

        if (Options.hasOption(DEFAULT_OPTION_NAME, rootOpt)) {
            JEVisOption defaultOpt = Options.getFirstOption(DEFAULT_OPTION_NAME, rootOpt);

            return defaultOpt.getValue();
        }
        return "";
    }

    /**
     * Returns an list of all DataProcess configurations.
     *
     * @param att
     * @return
     */
    public static List<JEVisOption> GetConfiguredProcesses(JEVisAttribute att) {
        List<JEVisOption> confDPs = new ArrayList<>();
        try {
            JEVisOption root = GetProcessOptionRoot(att);
            logger.info("root option: " + root.getKey());

            JEVisOption dpChainRoot = Options.getFirstOption(PROCESS_CHAIN_OPTION_NAME, root);

            for (JEVisOption process : dpChainRoot.getOptions()) {
                confDPs.add(process);
            }
        } catch (Exception ex) {
            //TODO implement an error handling
            logger.fatal(ex);

        }

        return confDPs;
    }

    public static boolean isCustomWorkdayPrev(BasicProcess task) {
        boolean isCustomWorkDay = true;
        for (ProcessOption option : task.getOptions()) {
            if (option.getKey().equals(CUSTOM)) {
                isCustomWorkDay = Boolean.parseBoolean(option.getValue());
                break;
            }
        }

        WorkDays workDays = new WorkDays(task.getSqlDataSource(), task.getJsonObject());
        workDays.setEnabled(isCustomWorkDay);

        return workDays.getWorkdayEnd(new DateTime()).isBefore(workDays.getWorkdayStart(new DateTime()));
    }
}
