/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.calculation;

import org.jevis.jecalc.calculation.CalcTemplate;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author broder
 */
public class CalcTemplateTest {

    @Test
    public void shouldCalcASummationWithoutVariables() {
        CalcTemplate template = new CalcTemplate("5+5");
        Assert.assertEquals("calculate a sum with 2 numbers 5 and 5", 10d, template.evaluate());
    }

    @Test
    public void shouldCalcASummationWithVariables() {
        CalcTemplate template = new CalcTemplate("#{server1}+#{server2}");
        template.put("server1", 5.0);
        template.put("server2", 5.0);
        Assert.assertEquals("calculate a sum with 2 numbers 5 and 5", 10d, template.evaluate());
    }

}
