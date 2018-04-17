/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import org.jevis.api.JEVisObject;
import org.joda.time.Interval;

/**
 *
 * @author broder
 */
public interface IntervalCalculator {

    public void buildIntervals(JEVisObject reportObject);

    public Interval getInterval(PeriodModus periodModus);

    public enum PeriodModus {

        CURRENT, LAST;
    }
}
