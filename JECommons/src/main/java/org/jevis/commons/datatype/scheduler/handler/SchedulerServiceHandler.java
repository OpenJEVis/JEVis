//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.handler;

import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTime;

import java.time.DayOfWeek;
import java.time.Month;

public abstract class SchedulerServiceHandler {
    public SchedulerServiceHandler() {
    }

    protected boolean checkDate(DateTime dt, SchedulerRule rule) {
        Month m = Month.of(dt.getMonthOfYear());
        DayOfWeek dw = DayOfWeek.of(dt.getDayOfWeek());
        return rule.isMonthEnabled(m) && rule.isDayOfWeekEnabled(dw) && rule.isDayOfMonthEnabled(dt);
    }
}
