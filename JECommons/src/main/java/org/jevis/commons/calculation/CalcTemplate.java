/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.apache.logging.log4j.LogManager;

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author broder
 */
public class CalcTemplate {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CalcTemplate.class);
    private final Evaluator evaluator;
    private final String expression;
    private final long calcObjectId;

    public CalcTemplate(long calcObjID, String expression) {
        this.evaluator = new Evaluator();
        this.expression = expression;
        this.calcObjectId = calcObjID;
    }

    BigDecimal evaluate() {
        String value = null;
        try {
            value = evaluator.evaluate(expression);
        } catch (EvaluationException ex) {
            logger.error("Cant evaluate expression {}", expression, ex);
        }

        BigDecimal returnVal = null;
        try {
            returnVal = new BigDecimal(Objects.requireNonNull(value));
        } catch (NullPointerException | NumberFormatException ex) {
            logger.info("Cant create return value {} for formula {} of calc object {}", value, expression, calcObjectId);
        }
        return returnVal;

    }

    void put(String key, BigDecimal value) {
        evaluator.putVariable(key, value.toString());
    }

}
