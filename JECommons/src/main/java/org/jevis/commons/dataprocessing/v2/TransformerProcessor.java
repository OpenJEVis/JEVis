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

/**
 *
 * @author Florian Simon
 */
public class TransformerProcessor implements Function {
    private static final Logger logger = LogManager.getLogger(TransformerProcessor.class);

    public static final String METER_CONSTANT = "Meter Constant";
    public static final String OFFSET = "OFFSET";

    private JEVisObject dataProcessorObject;
    private List<Result> inputs;
    private JEVisOption option;
    private DataWorkflow workflow;

    @Override
    public String getID() {
        return DataProcessing.GetDataProcessorID(dataProcessorObject);
    }

    @Override
    public void setWorkflow(DataWorkflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public void setObject(JEVisObject object) {
        logger.info("SetObject: " + object.getName());
        this.dataProcessorObject = object;
    }

    @Override
    public Result getResult() {
        logger.trace("TransformerProcessor.getResults()");
        Result result = new BasicResult();
        List<JEVisSample> samples = new ArrayList<>();

        double m = 1;
        double b = 0;

        m = DataProcessing.<Double>GetOptionValue(option, METER_CONSTANT, 1d);
        b = DataProcessing.<Double>GetOptionValue(option, OFFSET, 0d);

//        if (Options.hasOption(METER_CONSTANT, option)) {
//
//            String mString = Options.getFirstOption(METER_CONSTANT, option).getValue();
//            logger.info("found Meter Constant: " + mString);
//            try {
//                m = Double.parseDouble(mString);
//            } catch (Exception ex) {
//                Logger.getLogger(ConverterProcessor.class.getName()).log(Level.SEVERE, "Option " + METER_CONSTANT + " is missing not parsable", ex);
//            }
//
//        } else {
//            logger.info("No meter constat is set use default");
//
//        }
//
//        if (Options.hasOption(OFFSET, option)) {
//            String oString = Options.getFirstOption(OFFSET, option).getValue();
//            try {
//                b = Double.parseDouble(oString);
//            } catch (Exception ex) {
//                Logger.getLogger(ConverterProcessor.class.getName()).log(Level.SEVERE, "Option " + OFFSET + " is missing not parsable", ex);
//            }
//        } else {
//
//        }
        logger.info("Using M:" + m + "  B:" + b);
        for (JEVisSample sample : inputs.get(0).getSamples()) {

            try {
                double sum = (sample.getValueAsDouble() * m) + b;
                samples.add(new VirtualSample(sample.getTimestamp(), sum, dataProcessorObject.getDataSource(), new VirtualAttribute(null)));
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }

        logger.info("sample.size: " + samples.size());
        result.setSamples(samples);

        return result;
    }

    @Override
    public void setOptions(JEVisOption option) {
        logger.info("TransformerProcessor.setOptions: " + option);
        logger.info("  key: " + option.getKey());
        this.option = option;
    }

    @Override
    public void setInput(List<Result> results) {
        this.inputs = results;
    }

}
