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
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.dataprocessing.ProcessOptions.CUSTOM;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class NullFunction implements ProcessFunction {
    private static final Logger logger = LogManager.getLogger(NullFunction.class);

    public static final String NAME = "Null Function";
    private final AggregationPeriod aggregationPeriod;
    private final ManipulationMode mode;
    private JsonSampleGenerator jsonSampleGenerator;

    public NullFunction(ManipulationMode mode, AggregationPeriod aggregationPeriod) {
        this.mode = mode;
        this.aggregationPeriod = aggregationPeriod;
    }

    public NullFunction(JsonSampleGenerator jsonSampleGenerator, ManipulationMode mode, AggregationPeriod aggregationPeriod) {
        this.jsonSampleGenerator = jsonSampleGenerator;
        this.mode = mode;
        this.aggregationPeriod = aggregationPeriod;
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

        return options;
    }

    @Override
    public List<JsonSample> getJsonResult(BasicProcess mainTask) {
        logger.info("Warning no Processor is set");

        List<JsonSample> allSamples = new ArrayList<>();
        for (Process task : mainTask.getSubProcesses()) {
            allSamples.addAll(task.getJsonResult());
            //logger.info("Add input result: " + allSamples.size());
        }

        boolean isCustomWorkDay = true;
        for (ProcessOption option : mainTask.getOptions()) {
            if (option.getKey().equals(CUSTOM)) {
                isCustomWorkDay = Boolean.parseBoolean(option.getValue());
                break;
            }
        }

        if (aggregationPeriod != AggregationPeriod.NONE) {
            BasicProcess aggregationProcess = new BasicProcess(jsonSampleGenerator);
            aggregationProcess.setSQLDataSource(mainTask.getSqlDataSource());
            aggregationProcess.setJsonObject(mainTask.getJsonObject());
            aggregationProcess.setJsonAttribute(mainTask.getJsonAttribute());
            aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.CUSTOM, Boolean.toString(isCustomWorkDay)));

            switch (aggregationPeriod) {
                case QUARTER_HOURLY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.minutes(15).toString()));
                    break;
                case DAILY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                    break;
                case HOURLY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
                    break;
                case WEEKLY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                    break;
                case MONTHLY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                    break;
                case QUARTERLY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(3).toString()));
                    break;
                case YEARLY:
                    aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                    break;
                default:
            }
            aggregationProcess.setFunction(new AggregatorFunction(jsonSampleGenerator));
            aggregationProcess.setID("Aggregation");

            aggregationProcess.setSubProcesses(mainTask.getSubProcesses());

            allSamples = aggregationProcess.getJsonResult();
        }

        return allSamples;
    }

    @Override
    public void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
    }

}
