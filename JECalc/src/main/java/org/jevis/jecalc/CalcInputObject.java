/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import java.util.List;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.calculation.SampleMerger.InputType;

/**
 *
 * @author broder
 */
public class CalcInputObject {

    private final String identifier;
    private final List<JEVisSample> samples;
    private final InputType inputType;

    CalcInputObject(List<JEVisSample> samples, String identifier, InputType inputType) {
        this.samples = samples;
        this.identifier = identifier;
        this.inputType = inputType;
    }

    public InputType getInputType() {
        return inputType;
    }
    
    public String getIdentifier() {
        return identifier;
    }

    public List<JEVisSample> getSamples() {
        return samples;
    }

}
