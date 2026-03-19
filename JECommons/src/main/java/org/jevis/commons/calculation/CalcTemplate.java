package org.jevis.commons.calculation;

import com.udojava.evalex.Expression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Evaluates a mathematical expression with named variable bindings using EvalEx.
 * <p>
 * Usage pattern:
 * <ol>
 *   <li>Construct with a formula string (JEval {@code #{varname}} syntax is auto-normalized).</li>
 *   <li>Call {@link #put(String, BigDecimal)} for each variable binding before the first
 *       {@link #evaluate()} call.</li>
 *   <li>Call {@link #evaluate()} to compute the result. The {@link Expression} is built
 *       lazily on the first call and reused for all subsequent calls — variables are updated
 *       before each eval, so no re-parse occurs per timestamp.</li>
 * </ol>
 * Returns {@code null} when the result is NaN or infinite (e.g. division by zero).
 */
public class CalcTemplate {

    private static final Logger logger = LogManager.getLogger(CalcTemplate.class);
    private final String normalizedExpression;
    private final long calcObjectId;
    private final Map<String, BigDecimal> variables = new HashMap<>();
    private Expression expr;

    /**
     * @param calcObjID     JEVis object ID of the owning Calculation — used in log messages.
     * @param rawExpression formula string; JEval {@code #{varname}} placeholders are normalized
     *                      to plain {@code varname} automatically.
     */
    public CalcTemplate(long calcObjID, String rawExpression) {
        this.normalizedExpression = rawExpression.replaceAll("#\\{(\\w+)}", "$1");
        this.calcObjectId = calcObjID;
    }

    /**
     * Evaluates the expression using the variable bindings set via {@link #put}.
     * The underlying {@link Expression} is constructed once on the first call and reused
     * on subsequent calls (no re-parse per timestamp).
     *
     * @return the result as {@link BigDecimal}, or {@code null} if the result is NaN/infinite.
     */
    BigDecimal evaluate() {
        if (expr == null) {
            expr = new Expression(normalizedExpression);
        }
        variables.forEach(expr::setVariable);
        try {
            BigDecimal result = expr.eval();
            double d = result.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                logger.info("Expression {} for calc object {} produced NaN/Infinite", normalizedExpression, calcObjectId);
                return null;
            }
            return result;
        } catch (Expression.ExpressionException | ArithmeticException e) {
            logger.error("Expression evaluation failed for calc object {}: {}", calcObjectId, e.getMessage());
            return null;
        }
    }

    /**
     * Binds or updates the value of a named variable.
     *
     * @param key   variable name (must match a name used in the formula)
     * @param value the numeric value to assign
     */
    void put(String key, BigDecimal value) {
        variables.put(key, value);
    }
}
