/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.calculation.SampleMerger.InputType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class CalcInputObject {

    private final String identifier;
    private final JEVisAttribute valueAttribute;
    private final InputType inputType;
    private List<JEVisSample> samples;

    CalcInputObject(String identifier, InputType inputType, JEVisAttribute valueAttribute) {
        this.identifier = identifier;
        this.inputType = inputType;
        this.valueAttribute = valueAttribute;
    }

    public InputType getInputType() {
        return inputType;
    }

    public JEVisAttribute getValueAttribute() {
        return valueAttribute;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<JEVisSample> getSamples() {
        return samples;
    }

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }


    public List<JEVisSample> buildSamplesFromInputType(JEVisAttribute valueAttribute, InputType inputType, DateTime startTime, DateTime endTime) {

        List<JEVisSample> returnSamples = new ArrayList<>();
        switch (inputType) {
            case PERIODIC:
                //todo try to make it better for incomplete periods (aggregation)
                returnSamples = valueAttribute.getSamples(startTime, endTime);
                break;
            case STATIC:
                JEVisSample constant = valueAttribute.getLatestSample();
                if (constant != null) {
                    returnSamples.add(constant);
                } else {
                    throw new IllegalArgumentException("Constant with id " + valueAttribute.getObject().getID() + " has no value");
                }
                break;
            case NON_PERIODIC:
                returnSamples = valueAttribute.getAllSamples();
                break;
        }
        this.samples = returnSamples;

        return samples;
    }
}
