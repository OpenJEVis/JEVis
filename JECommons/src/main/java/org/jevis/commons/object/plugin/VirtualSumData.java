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
package org.jevis.commons.object.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.management.Query.value;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.VirtuelSample;
import org.jevis.commons.dataprocessing.v2.DataProcessing;
import org.jevis.commons.dataprocessing.v2.DataWorkflow;
import org.joda.time.DateTime;

/**
 * This virtual data can summarize multible inputes.
 *
 * This is an prototype implementation of an virtual data object.
 *
 * @TODO: this will be an attribute driver if they exist
 *
 * @author Florian Simon
 */
public class VirtualSumData {

    public enum Operator {

        TIMES, DIVIDED, PLUS, MINUS, NONE
    }
    private Operator _operator;
    private String _version;
    private List<Input> _inputs;

    public VirtualSumData() {
        _operator = Operator.PLUS;
        _version = "1";
        _inputs = new ArrayList<>();
    }

    public VirtualSumData(JEVisObject obj) throws JEVisException {
        String json = obj.getAttribute("Formula").getLatestSample().getValueAsString();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

    }

    public void initData(JEVisDataSource ds, JsonVirtualCalc json) {
        System.out.println("VirtualSumData");
        System.out.println("json.getOperator(): " + json.getOperator());

        _operator = json.getOperatorAsEnum();
        System.out.println("Operator parsed: " + _operator.name());

        _version = json.getVersion();

        _inputs = new ArrayList<>();
        for (JsonInput in : json.getInputs()) {
            try {
                _inputs.add(new FomulaInput(ds, in));
            } catch (JEVisException ex) {
                Logger.getLogger(VirtualSumData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public VirtualSumData(JEVisDataSource ds, JsonVirtualCalc json) {
        initData(ds, json);
    }

    public void setVersion(String version) {
        _version = version;
    }

    public void setOperator(Operator operator) {
        _operator = operator;
    }

    public Operator getOperator() {
        return _operator;
    }

    public String getVersion() {
        return _version;
    }

    public void setInputs(List<Input> inputs) {
        _inputs = inputs;
    }

    public List<Input> getInputs() {
        return _inputs;
    }

    private Map<DateTime, Object> getMergedTimeRange(Map<Input, Map<DateTime, JEVisSample>> inputs) throws JEVisException {
        Map<DateTime, Object> timerange = new HashMap<>();

        for (Map.Entry<Input, Map<DateTime, JEVisSample>> entrySet : inputs.entrySet()) {
            Input key = entrySet.getKey();
            Map<DateTime, JEVisSample> value = entrySet.getValue();

            for (Map.Entry<DateTime, JEVisSample> sampleSet : value.entrySet()) {
                DateTime key1 = sampleSet.getKey();
                JEVisSample value1 = sampleSet.getValue();

                timerange.put(value1.getTimestamp(), null);

            }

        }
        return timerange;
    }

    public List<JEVisSample> getResult(DateTime from, DateTime until) throws JEVisException {
        List<JEVisSample> response = new ArrayList<>();

        Map<Input, Map<DateTime, JEVisSample>> imputSamples = new HashMap<>();
        for (Input in : _inputs) {
            HashMap<DateTime, JEVisSample> sMap = new HashMap<>();

            for (JEVisSample sam : in.getAttribute().getSamples(from, until)) {
                sMap.put(sam.getTimestamp(), sam);
            }

            for (String name : DataProcessing.GetConfiguredWorkflowNames(in.getAttribute())) {
                System.out.println("has Workflow: " + name);
            }

//            DataWorkflow dw = DataProcessing.GetConfiguredWorkflow(in.getAttribute(), in.getWorkflowID());
            imputSamples.put(in, sMap);
        }

        Map<DateTime, Object> timestamps = getMergedTimeRange(imputSamples);
//        System.out.println("Inputs: " + imputSamples.size());

        boolean firstLoop = true;
        for (Map.Entry<DateTime, Object> entrySet : timestamps.entrySet()) {

            DateTime ts = entrySet.getKey();

            JEVisSample newSample = new VirtuelSample(ts, 0);
//            System.out.println("--- " + ts + " ---");

            double newValue = 0;

            for (Map.Entry<Input, Map<DateTime, JEVisSample>> insamples : imputSamples.entrySet()) {
                double value = 0;
                if (insamples.getValue().containsKey(ts)) {
                    value = insamples.getValue().get(ts).getValueAsDouble();
                } else {
                    value = 0;
                }

                switch (_operator) {
                    case DIVIDED:
                        if (firstLoop) {

                        } else {
                            //Will thow errors divide by 0 and so on
                            newValue = newValue / value;
                        }
                        break;
                    case TIMES:
                        if (firstLoop) {
                            newValue = 1 * value;
                        } else {
                            //Will thow errors
                            newValue = newValue * value;
                        }
                        break;
                    case PLUS:
//                        System.out.println("Input: " + insamples.getKey().getID() + " value: " + value);
                        //TODO unit handling
                        newValue = newValue + value;

                        break;
                    case MINUS:
                        newValue = newValue - value;
                        break;
                    default:
                        break;

                }

            }
//            System.out.println(newValue);

            newSample.setValue(newValue);
            response.add(newSample);
            firstLoop = false;

        }

        return response;
    }

}
