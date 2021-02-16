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
import org.jevis.commons.dataprocessing.BasicProcess;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.ProcessFunction;
import org.jevis.commons.dataprocessing.ProcessOption;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CounterFunction implements ProcessFunction {

    private static final Logger logger = LogManager.getLogger(CounterFunction.class);
    public static final String NAME = "Counter Processor";

    private List<JsonSample> _jsonResult;
    private JsonSampleGenerator jsonSampleGenerator;

    public enum TS_MODE {

        BEGINNING, END
    }

    @Override
    public void resetResult() {
        _jsonResult = null;
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

    @Override
    public List<JsonSample> getJsonResult(BasicProcess task) {
        if (_jsonResult != null) {
            return _jsonResult;
        } else {
            _jsonResult = new ArrayList<>();

            if (task.getSubProcesses().size() != 1) {
                logger.warn("Waring Counter processor can only handel one input. using first only!");
            }

            JsonSample lastSample = null;

            TS_MODE mode = TS_MODE.BEGINNING;//TODO get from options

            for (JsonSample sample : task.getSubProcesses().get(0).getJsonResult()) {

                if (lastSample == null) {
                    lastSample = sample;
                } else {
                    double diff = 0;
                    if (Double.parseDouble(sample.getValue()) >= Double.parseDouble(lastSample.getValue())) {

                        diff = Double.parseDouble(sample.getValue()) - Double.parseDouble(lastSample.getValue());
//                            logger.info("pV: " + lastSample.getValueAsDouble() + "  nV:" + sample.getValueAsDouble() + "  diff:" + diff);
                        JsonSample jsonSample = new JsonSample();
                        if (mode == TS_MODE.BEGINNING) {
                            jsonSample.setTs(lastSample.getTs());
                            jsonSample.setValue(Double.toString(diff));
                        } else {
                            jsonSample.setTs(sample.getTs());
                            jsonSample.setValue(Double.toString(diff));
                        }
                        _jsonResult.add(jsonSample);

                    } else {
                        logger.error("Error counter is smaler the the previsus. maybe an counter overflow?");
                    }
                    lastSample = sample;
                }

            }
        }

        return _jsonResult;
    }

    @Override
    public void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }

}
