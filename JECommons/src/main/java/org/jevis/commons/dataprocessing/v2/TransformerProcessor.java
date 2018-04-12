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
package org.jevis.commons.dataprocessing.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.commons.config.Options;
import org.jevis.commons.dataprocessing.VirtualAttribute;
import org.jevis.commons.dataprocessing.VirtuelSample;
import org.jevis.commons.dataprocessing.function.ConverterFunction;

/**
 *
 * @author Florian Simon
 */
public class TransformerProcessor implements Function {

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
        System.out.println("SetObject: " + object.getName());
        this.dataProcessorObject = object;
    }

    @Override
    public Result getResult() {
        Logger.getLogger(TransformerProcessor.class.getName()).log(Level.FINE, null, "TransformerProcessor.getResults()");
        Result result = new BasicResult();
        List<JEVisSample> samples = new ArrayList<>();

        double m = 1;
        double b = 0;

        System.out.println("");

        m = DataProcessing.<Double>GetOptionValue(option, METER_CONSTANT, 1d);
        b = DataProcessing.<Double>GetOptionValue(option, OFFSET, 0d);

//        if (Options.hasOption(METER_CONSTANT, option)) {
//
//            String mString = Options.getFirstOption(METER_CONSTANT, option).getValue();
//            System.out.println("found Meter Constant: " + mString);
//            try {
//                m = Double.parseDouble(mString);
//            } catch (Exception ex) {
//                Logger.getLogger(ConverterProcessor.class.getName()).log(Level.SEVERE, "Option " + METER_CONSTANT + " is missing not parsable", ex);
//            }
//
//        } else {
//            System.out.println("No meter constat is set use default");
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
        System.out.println("Using M:" + m + "  B:" + b);
        for (JEVisSample sample : inputs.get(0).getSamples()) {

            try {
                double sum = (sample.getValueAsDouble() * m) + b;
                samples.add(new VirtuelSample(sample.getTimestamp(), sum, dataProcessorObject.getDataSource(), new VirtualAttribute(null)));
            } catch (JEVisException ex) {
                Logger.getLogger(ConverterFunction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("sample.size: " + samples.size());
        result.setSamples(samples);

        return result;
    }

    @Override
    public void setOptions(JEVisOption option) {
        System.out.print("TransformerProcessor.setOptions: " + option);
        System.out.println("  key: " + option.getKey());
        this.option = option;;
    }

    @Override
    public void setInput(List<Result> results) {
        this.inputs = results;
    }

}
