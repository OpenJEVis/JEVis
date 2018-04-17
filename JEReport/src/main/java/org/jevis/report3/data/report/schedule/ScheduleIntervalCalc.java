package org.jevis.report3.data.report.schedule;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.IntervalCalculator;
import org.joda.time.Interval;

/**
 *
 * @author broder
 */
public class ScheduleIntervalCalc implements IntervalCalculator {

    private static boolean isInit = false;
    private Interval interval;


    @Override
    public Interval getInterval(PeriodModus modus) {
        return interval;
    }

    public synchronized boolean getIsInit() {
        return isInit;
    }

    public synchronized void setIsInitTrue() {
        isInit = true;
    }

    @Override
    public void buildIntervals(JEVisObject reportObject) {
        interval = JEVisIntervalParser.getInterval();
    }

}
