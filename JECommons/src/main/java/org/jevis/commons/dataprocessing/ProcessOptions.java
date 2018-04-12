/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.commons.config.Options;
import org.jevis.commons.dataprocessing.function.AggrigatorFunction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This class contains various methods for manipulating ProcessOptions.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ProcessOptions {

    public static String ROOT_OPTION_NAME = "Data Processing";
    public static String DEFAULT_OPTION_NAME = "Default";
    public static String PROSESS_CHAIN_OPTION_NAME = "Process Chains";//Rename into ProcessChain

    public static final String PERIOD = "period";
    public static final String OFFSET = "offset";
    public static final String TS_START = "date-start";
    public static final String TS_END = "date-end";
    public static final String TS_PATTERN = "yyyy-MM-dd hh:mm:ssZ";

    public static List<ProcessOption> ToProcessOption(Process process, JEVisOption option) {
        List<ProcessOption> pOptions = new ArrayList<>();

        for (JEVisOption jOpt : option.getOptions()) {
            ProcessOption po = new BasicProcessOption(jOpt.getKey(), jOpt.getValue());

            //Problem: JEVisOptions are herahic and the Processoption are flat but the Process him self has an herachie
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
     * @param option
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
            task.getOptions().add(new BasicProcessOption(TS_START, DateTimeFormat.forPattern(TS_PATTERN).print(from)));
            task.getOptions().add(new BasicProcessOption(TS_END, DateTimeFormat.forPattern(TS_PATTERN).print(until)));
        }

        if (childrenToo) {
            for (Process t : task.getSubProcesses()) {
                setStartEnd(t, from, until, overwrite, childrenToo);
            }

        }

    }

    /**
     * Returns the first and last timestamp of an task options
     *
     * @param task
     * @return
     */
    public static DateTime[] getStartAndEnd(Process task) {

        DateTime[] result = new DateTime[2];
        result[0] = null;
        result[1] = null;

        if (ContainsOption(task, TS_START)) {
            try {
                result[0] = DateTime.parse(GetLatestOption(task, TS_START, new BasicProcessOption(TS_START, "")).getValue(), DateTimeFormat.forPattern(TS_PATTERN));
//                result[0] = DateTime.parse(task.getOptions().get(TS_START), DateTimeFormat.forPattern(TS_PATTERN));
            } catch (Exception e) {
                System.out.println("error while parsing " + TS_START + " option");
            }
        } else {
            System.out.println("No " + TS_START + " option is missing");
        }

        if (ContainsOption(task, TS_END)) {
            try {
                result[1] = DateTime.parse(GetLatestOption(task, TS_END, new BasicProcessOption(TS_END, "")).getValue(), DateTimeFormat.forPattern(TS_PATTERN));
//                result[1] = DateTime.parse(task.getOptions().get(TS_END), DateTimeFormat.forPattern(TS_PATTERN));
            } catch (Exception ex) {
                System.out.println("error while parsing " + TS_END + " option");
            }
        } else {
            System.out.println("No " + TS_END + " option is missing");
        }

        return result;

    }

    /**
     * Return sthe first period matching the period, offset and target date
     *
     * @param date target date to find the matching period
     * @param period period
     * @param offset start offset
     * @return
     */
    public static DateTime findFirstDuration(DateTime date, Period period, DateTime offset) {
//        System.out.println("findFirstDuration: " + date + "   p: " + period);
        DateTime startD = new DateTime();
        DateTime fistPeriod = offset;
//        System.out.println("week: " + date.getWeekOfWeekyear());
//
        while (fistPeriod.isBefore(date) || fistPeriod.isEqual(date)) {
            fistPeriod = fistPeriod.plus(period);
        }
        fistPeriod = fistPeriod.minus(period);

        System.out.println("finding date in: " + ((new DateTime()).getMillis() - startD.getMillis()) + "ms");
        System.out.println("first offset date: offset:" + offset + "   for period: " + period + "   input date: " + date + "  fistPeriod: " + fistPeriod);
        return fistPeriod;
    }

    /**
     * @return
     */
    public static DateTime getOffset(Process task) {
        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
//        Chronology coptic = CopticChronology.getInstance(zone);
        DateTime _offset = new DateTime(2007, 1, 1, 0, 0, 0, zone);
        return _offset;
    }

    /**
     *
     * @param period
     * @param offset
     * @param firstSample
     * @param lastSample
     * @return
     */
    public static List<Interval> buildIntervals(Period period, DateTime offset, DateTime firstSample, DateTime lastSample) {
        DateTime benchMarkStart = new DateTime();
        List<Interval> result = new ArrayList<>();

        DateTime startDate = findFirstDuration(firstSample, period, offset);
        result.add(new Interval(startDate, period));

        boolean run = true;
//        DateTime time = startDate;
        while (run) {
            startDate = startDate.plus(period);
            if (startDate.isAfter(lastSample)) {
//                System.out.println("wtf: " + startDate.getMillis() + "     " + lastSample.getMillis());
//                System.out.println("faild Interval: " + startDate + "   is after   " + lastSample);
                run = false;
            } else {
                result.add(new Interval(startDate, period));
            }
        }

        DateTime benchMarkEnd = new DateTime();
        System.out.println("Time to create Intervals[" + result.size() + "]:  in " + (benchMarkEnd.getMillis() - benchMarkStart.getMillis()) + "ms");
        return result;
    }

    /**
     * Returns an list with every timestamp from an list of jevissample lists.
     * ervry timestamp is uniqe and sorted by datetime
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
                    Logger.getLogger(AggrigatorFunction.class.getName()).log(Level.SEVERE, null, ex);
                }
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
        DateTime offset = new DateTime(2001, 01, 01, 00, 00, 00);

        if (ContainsOption(task, PERIOD)) {
            System.out.println("Error missing period option");
        }
        if (ContainsOption(task, OFFSET)) {
            System.out.println("Error missing offset option");
            task.getOptions().add(new BasicProcessOption(OFFSET, "2001-01-01 00:00:00"));
        }

        for (ProcessOption option : task.getOptions()) {
            String key = option.getKey();
            String value = option.getValue();
            System.out.println("key: " + key);
            switch (key) {
                case PERIOD:
                    System.out.println("period string: " + value);
                    period = Period.parse(value);
                    System.out.println("pared period: " + period);
                    break;
                case OFFSET:
                    //TODO check value formate
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                    offset = dtf.parseDateTime(value);
                    break;
            }
        }

        return buildIntervals(period, offset, from, until);

    }

    /**
     * Cronverts an JEVIsSamle list to an Map
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
        if (opt == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns the root Option node of this Attribute
     *
     * @param attribute
     * @return returns the dataProcessing root or null
     */
    public static JEVisOption GetProcessOptionRoot(JEVisAttribute attribute) {

        if (attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            System.out.println("Optiones exists");
            for (JEVisOption option : attribute.getOptions()) {
                System.out.println("opt: " + option.getKey());
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
            System.out.println("root option: " + root.getKey());

            JEVisOption dpChainRoot = Options.getFirstOption(PROSESS_CHAIN_OPTION_NAME, root);

            for (JEVisOption process : dpChainRoot.getOptions()) {
                confDPs.add(process);
            }
        } catch (Exception ex) {
            //TODO implement an error handling
            ex.printStackTrace();
        }

        return confDPs;
    }

}
