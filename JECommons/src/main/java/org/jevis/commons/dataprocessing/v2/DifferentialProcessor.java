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
package org.jevis.commons.dataprocessing.v2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtualAttribute;
import org.jevis.commons.dataprocessing.VirtualSample;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.dataprocessing.v2.TransformerProcessor.METER_CONSTANT;

/**
 *
 * @author Florian Simon
 */
public class DifferentialProcessor implements Function {
    private static final Logger logger = LogManager.getLogger(DifferentialProcessor.class);

    private JEVisObject dpObject;
    private JEVisOption option;
    private List<Result> inputs;
    private DataWorkflow workflow;

    public enum TS_MODE {

        BEGINNING, END
    }

    @Override
    public String getID() {
        return DataProcessing.GetDataProcessorID(dpObject);
    }

    @Override
    public void setWorkflow(DataWorkflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public Result getResult() {
        logger.info("DifferentialProcessor.getResult()");
        Result result = new BasicResult();
        List<JEVisSample> samples = new ArrayList<>();
        if (inputs == null || inputs.isEmpty()) {
            logger.info("DifferentialProcessor.getResult() input is null/emty");
        }

        JEVisSample lastSample = null;

        TS_MODE mode = TS_MODE.BEGINNING;//TODO get from options

        double overFlow = DataProcessing.<Double>GetOptionValue(option, METER_CONSTANT, Double.MAX_VALUE);

        for (JEVisSample sample : this.inputs.get(0).getSamples()) {//TODO replace get(0)

            if (lastSample == null) {
                lastSample = sample;

            } else {
                double diff = 0;
                try {
                    if (sample.getValueAsDouble() >= lastSample.getValueAsDouble()) {

                        diff = sample.getValueAsDouble() - lastSample.getValueAsDouble();
//                            logger.info("pV: " + lastSample.getValueAsDouble() + "  nV:" + sample.getValueAsDouble() + "  diff:" + diff);
                        if (mode == TS_MODE.BEGINNING) {
                            samples.add(new VirtualSample(lastSample.getTimestamp(), diff, dpObject.getDataSource(), new VirtualAttribute(null, "Diff Attribute")));
                        } else {
                            samples.add(new VirtualSample(sample.getTimestamp(), diff, dpObject.getDataSource(), new VirtualAttribute(null, "Diff Attribute")));
                        }

                    } else {
//                        logger.info("Error counter is smaler the the previsus. maybe an counter overflow?");
                    }
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
                lastSample = sample;
            }

        }

        logger.info("sample.size: {}", samples.size());
        result.setSamples(samples);
        return result;
    }

    @Override
    public void setOptions(JEVisOption option) {
        this.option = option;
    }

    @Override
    public void setObject(JEVisObject object) {
        this.dpObject = object;
    }

    @Override
    public void setInput(List<Result> results) {
        this.inputs = results;
    }

}
