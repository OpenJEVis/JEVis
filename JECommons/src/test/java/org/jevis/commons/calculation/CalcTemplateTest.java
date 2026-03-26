package org.jevis.commons.calculation;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class CalcTemplateTest {

    @Test
    public void testSimpleAddition() {
        CalcTemplate t = new CalcTemplate(1L, "a + b");
        t.put("a", BigDecimal.valueOf(3.0));
        t.put("b", BigDecimal.valueOf(4.0));
        BigDecimal result = t.evaluate();
        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(7.0)));
    }

    @Test
    public void testDivisionByZeroReturnsNull() {
        CalcTemplate t = new CalcTemplate(1L, "a / b");
        t.put("a", BigDecimal.valueOf(1.0));
        t.put("b", BigDecimal.ZERO);
        assertNull(t.evaluate());
    }

    @Test
    public void testMultipleEvaluateCallsConsistent() {
        CalcTemplate t = new CalcTemplate(1L, "x * 2");
        t.put("x", BigDecimal.valueOf(5.0));
        BigDecimal first = t.evaluate();
        BigDecimal second = t.evaluate();
        assertNotNull(first);
        assertEquals(0, first.compareTo(second));
        assertEquals(0, first.compareTo(BigDecimal.valueOf(10.0)));
    }

    @Test
    public void testUpdatedVariableIsPickedUp() {
        CalcTemplate t = new CalcTemplate(1L, "x * 2");
        t.put("x", BigDecimal.valueOf(5.0));
        BigDecimal first = t.evaluate();
        t.put("x", BigDecimal.valueOf(10.0));
        BigDecimal second = t.evaluate();
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(0, first.compareTo(BigDecimal.valueOf(10.0)));
        assertEquals(0, second.compareTo(BigDecimal.valueOf(20.0)));
    }

    @Test
    public void testJEvalSyntaxNormalization() {
        // JEval-style #{varname} formula must be normalized and evaluate correctly
        CalcTemplate t = new CalcTemplate(1L, "#{a} + #{b}");
        t.put("a", BigDecimal.valueOf(2.0));
        t.put("b", BigDecimal.valueOf(3.0));
        BigDecimal result = t.evaluate();
        assertNotNull("JEval-style formula should evaluate correctly after normalization", result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(5.0)));
    }

    @Test
    public void testMixedSyntaxNormalization() {
        // Mixed old/new syntax
        CalcTemplate t = new CalcTemplate(1L, "#{a} + b");
        t.put("a", BigDecimal.valueOf(1.0));
        t.put("b", BigDecimal.valueOf(1.0));
        BigDecimal result = t.evaluate();
        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(2.0)));
    }
}
