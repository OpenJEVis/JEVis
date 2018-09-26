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
 * Tester. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.dataprocessing.Process;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ImpulseFunction implements ProcessFunction {
    public static final String NAME = "Impulse Cleaner";
    private static final Logger logger = LogManager.getLogger(ImpulseFunction.class);

    private Period _period;
    private List<Interval> _durations;

    private DateTime _offset = ProcessOptions.getOffset(null);

    public ImpulseFunction() {
    }

    public ImpulseFunction(Period period) {
        _period = period;

    }

    @Override
    public void resetResult() {
//        _result = null;
    }

    @Override
    public List<JEVisSample> getResult(Process mainTask) {
        List<JEVisSample> result = new ArrayList<>();

        if (mainTask.getSubProcesses().size() > 1) {
            logger.info("Impulse cleaner cannot work with more than one input, using first only.");
        } else if (mainTask.getSubProcesses().size() < 1) {
            logger.warn("Impulse cleaner, no input nothing to do");
        }

        List<JEVisSample> samples = mainTask.getSubProcesses().get(0).getResult();

        DateTime firstTS = DateTime.now();
        DateTime lastTS = DateTime.now();
        try {
            firstTS = samples.get(0).getTimestamp();
            lastTS = samples.get(samples.size()).getTimestamp();
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        List<Interval> intervals = ProcessOptions.getIntervals(mainTask, firstTS, lastTS);

        int lastPos = 0;
        for (Interval interval : intervals) {
            List<JEVisSample> samplesInPeriod = new ArrayList<>();

            for (int i = lastPos; i < samples.size(); i++) {
                try {
                    if (interval.contains(samples.get(i).getTimestamp())) {
//                        logger.info("add sample: " + samples.get(i));
                        samplesInPeriod.add(samples.get(i));
                    } else if (samples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                        lastPos = i;
                        break;
                    }
                } catch (JEVisException ex) {
                    logger.error("JEVisExeption while going trou sample: " + ex.getMessage());
                }
            }

            //TODO: thi sis an dummy for
            JEVisSample bestmatch = null;
            for (JEVisSample sample : samplesInPeriod) {

                long bestDiff = 99999999999999999l;
                try {
                    long middelMili = ((interval.getEndMillis() - interval.getStartMillis()) / 2) + interval.getStartMillis();
                    long diff = Math.abs(sample.getTimestamp().getMillis() - middelMili);
//                    logger.info("Diff for: " + sample.getTimestamp() + "      -> " + diff);

                    if (bestmatch != null) {
                        if (bestDiff < diff) {
                            bestDiff = diff;
                            bestmatch = sample;
                        }
                    } else {
                        bestmatch = sample;
                        bestDiff = diff;
                    }

                } catch (JEVisException ex) {
                    logger.error("JEVisExeption while going trou sample2: " + ex.getMessage());
                }
            }
            if (bestmatch != null) {
                logger.info("Best match: " + bestmatch);
                result.add(bestmatch);
            }
        }

        return result;
    }

    public List<JEVisSample> getResult(ProcessOptions options, List<List<JEVisSample>> allSamples) {
        List<JEVisSample> result = new ArrayList<>();
        for (List<JEVisSample> samples : allSamples) {

            try {
                _durations = ProcessOptions.buildIntervals(Period.minutes(15), _offset, samples.get(0).getTimestamp(), samples.get(samples.size() - 1).getTimestamp());
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }

            //Samples list is sorted by default
            int lastPos = 0;
            for (Interval interval : _durations) {
//            logger.info("Interval: " + interval);
                List<JEVisSample> samplesInPeriod = new ArrayList<>();

                for (int i = lastPos; i < samples.size(); i++) {
                    try {
                        if (interval.contains(samples.get(i).getTimestamp())) {
//                        logger.info("add sample: " + samples.get(i));
                            samplesInPeriod.add(samples.get(i));
                        } else if (samples.get(i).getTimestamp().isAfter(interval.getEnd())) {
                            lastPos = i;
                            break;
                        }
                    } catch (JEVisException ex) {
                        logger.error("JEVisExeption while going trou sample: " + ex.getMessage());
                    }
                }

                //TODO: thi sis an dummy for
                JEVisSample bestmatch = null;
                for (JEVisSample sample : samplesInPeriod) {

                    long bestDiff = 99999999999999999l;
                    try {
                        long middelMili = ((interval.getEndMillis() - interval.getStartMillis()) / 2) + interval.getStartMillis();
                        long diff = Math.abs(sample.getTimestamp().getMillis() - middelMili);
//                    logger.info("Diff for: " + sample.getTimestamp() + "      -> " + diff);

                        if (bestmatch != null) {
                            if (bestDiff < diff) {
                                bestDiff = diff;
                                bestmatch = sample;
                            }
                        } else {
                            bestmatch = sample;
                            bestDiff = diff;
                        }

                    } catch (JEVisException ex) {
                        logger.error("JEVisExeption while going trou sample2: " + ex.getMessage());
                    }
                }
                if (bestmatch != null) {
                    logger.info("Best match: " + bestmatch);
                    result.add(bestmatch);
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

}
