//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.handler;

import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import java.util.List;

public class SchedulerContainsHandler extends SchedulerServiceHandler {
    public SchedulerContainsHandler() {
    }

    public boolean contains(DateTime dt, CronScheduler scheduler) {
        DateTimeZone dtz = scheduler.getDatetTimeZone();
        List<SchedulerRule> rules = scheduler.getAllRules();

        for (SchedulerRule rule : rules) {
            if (this.checkDate(dt.withZone(dtz), rule)) {
                DateTime startDt = dt.withZone(dtz).withTime(Integer.parseInt(rule.getStartHour()), Integer.parseInt(rule.getStartMinute()), 0, 0);
                DateTime endDt = dt.withZone(dtz).withTime(Integer.parseInt(rule.getEndHour()), Integer.parseInt(rule.getEndMinute()), 59, 0);
                Interval in = new Interval(startDt, endDt);
                if (in.contains(dt)) {
                    return true;
                }
            }
        }

        return false;
    }
}
