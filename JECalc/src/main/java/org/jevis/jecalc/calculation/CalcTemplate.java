/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author broder
 */
public class CalcTemplate {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CalcTemplate.class);
    private final Evaluator evaluator;
    private final String expression;

    public CalcTemplate(String expression) {
        evaluator = new Evaluator();
        this.expression = expression;
    }

    Double evaluate() {
        String value = null;
        try {
            value = evaluator.evaluate(expression);
        } catch (EvaluationException ex) {
            logger.error("Cant evaluate expression {}", expression, ex);
        }

        Double returnVal = null;
        try {
            returnVal = Double.parseDouble(value);

            returnVal = Precision.round(returnVal, 12);

        } catch (NullPointerException | NumberFormatException ex) {
            logger.error("Cant evaluate expression {}", expression, ex);
        }
        return returnVal;

    }

    void put(String key, Double value) {
        evaluator.putVariable(key, value.toString());
    }

}
