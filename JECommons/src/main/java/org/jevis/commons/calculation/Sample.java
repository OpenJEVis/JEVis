/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author broder
 */
public class Sample {
    private static final Logger logger = LogManager.getLogger(Sample.class);

    private DateTime date;
    private BigDecimal value;
    private final String variable;

    Sample(JEVisSample currentSample, String variable) {
        try {
            this.date = currentSample.getTimestamp();
            this.value = new BigDecimal(currentSample.getValueAsDouble());
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        this.variable = variable;
    }

    public DateTime getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getVariable() {
        return variable;
    }


}
