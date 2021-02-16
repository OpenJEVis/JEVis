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
package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ConverterFunction implements ProcessFunction {

    private static final Logger logger = LogManager.getLogger(ConverterFunction.class);
    public final static String NAME = "Converter";
    public static final String MULTIPLAYER = "multiplier";
    public static final String OFFSET = "offset";
    private List<JsonSample> _jsonResult;
    private JsonSampleGenerator jsonSampleGenerator;

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

        options.add(new BasicProcessOption(MULTIPLAYER));
        options.add(new BasicProcessOption(OFFSET));

        return options;
    }

    @Override
    public List<JsonSample> getJsonResult(BasicProcess mainTask) {
        if (_jsonResult != null) {
            return _jsonResult;
        } else {
            _jsonResult = new ArrayList<>();

            double m = 1;
            double b = 0;

            m = Double.parseDouble(ProcessOptions.GetLatestOption(mainTask, MULTIPLAYER, new BasicProcessOption(MULTIPLAYER, "1")).getValue());//TYPO MULTIPLAYER
            b = Double.parseDouble(ProcessOptions.GetLatestOption(mainTask, OFFSET, new BasicProcessOption(OFFSET, "0")).getValue());

            if (mainTask.getSubProcesses().size() != 1) {
                logger.info("Waring Counter processor can only handle one input. using first only!");
            }

            logger.info("Using M:" + m + "  B:" + b);
            for (JsonSample sample : mainTask.getSubProcesses().get(0).getJsonResult()) {

                double sum = (Double.parseDouble(sample.getValue()) * m) + b;
                JsonSample jsonSample = new JsonSample();
                jsonSample.setTs(sample.getTs());
                jsonSample.setValue(Double.toString(sum));
                _jsonResult.add(jsonSample);
            }

        }
        return _jsonResult;
    }

    @Override
    public void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }

}
