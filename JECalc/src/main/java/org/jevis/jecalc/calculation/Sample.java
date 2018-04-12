/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

/**
 *
 * @author broder
 */
public class Sample {

    private DateTime date;
    private Double value;
    private final String variable;

    Sample(JEVisSample currentSample, String variable) {
        try {
            this.date = currentSample.getTimestamp();
            this.value = currentSample.getValueAsDouble();
        } catch (JEVisException ex) {
            Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.variable = variable;
    }

    public DateTime getDate() {
        return date;
    }

    public Double getValue() {
        return value;
    }

    public String getVariable() {
        return variable;
    }
    
    

}
