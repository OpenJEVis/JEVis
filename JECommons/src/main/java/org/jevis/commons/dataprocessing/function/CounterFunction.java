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
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.dataprocessing.Process;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CounterFunction implements ProcessFunction {

    private static final Logger logger = LogManager.getLogger(CounterFunction.class);
    public static final String NAME = "Counter Processor";

    private List<JEVisSample> _result;

    public enum TS_MODE {

        BEGINNING, END
    }

    @Override
    public void resetResult() {
        _result = null;
    }

    @Override
    public List<JEVisSample> getResult(Process task) {
        if (_result != null) {
            return _result;
        } else {
            _result = new ArrayList<>();

            if (task.getSubProcesses().size() != 1) {
                logger.warn("Waring Counter processor can only handel one input. using first only!");
            }

            JEVisSample lastSample = null;

            TS_MODE mode = TS_MODE.BEGINNING;//TODO get from options

            for (JEVisSample sample : task.getSubProcesses().get(0).getResult()) {

                if (lastSample == null) {
                    lastSample = sample;
                } else {
                    double diff = 0;
                    try {
                        if (sample.getValueAsDouble() >= lastSample.getValueAsDouble()) {

                            diff = sample.getValueAsDouble() - lastSample.getValueAsDouble();
//                            logger.info("pV: " + lastSample.getValueAsDouble() + "  nV:" + sample.getValueAsDouble() + "  diff:" + diff);
                            if (mode == TS_MODE.BEGINNING) {
                                _result.add(new VirtualSample(lastSample.getTimestamp(), diff, task.getJEVisDataSource(), new VirtualAttribute(null)));
                            } else {
                                _result.add(new VirtualSample(sample.getTimestamp(), diff, task.getJEVisDataSource(), new VirtualAttribute(null)));
                            }

                        } else {
                            logger.error("Error counter is smaler the the previsus. maybe an counter overflow?");
                        }
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }
                    lastSample = sample;
                }

            }
        }

        return _result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<ProcessOption> getAvailableOptions() {
        List<ProcessOption> options = new ArrayList<>();

        options.add(new BasicProcessOption("Counter Overflow"));
        options.add(new BasicProcessOption("Counter Start"));

        return options;
    }

}
