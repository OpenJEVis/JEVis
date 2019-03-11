//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.cron;

import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

public class CronScheduler {
    private final List<SchedulerRule> rules = new ArrayList();
    private DateTimeZone datetTimeZone;

    public CronScheduler() {
    }

    public void addRule(SchedulerRule scheduler) {
        this.rules.add(scheduler);
    }

    public void removeRule(SchedulerRule scheduler) {
        this.rules.remove(scheduler);
    }

    public List<SchedulerRule> getAllRules() {
        return this.rules;
    }

    public DateTimeZone getDatetTimeZone() {
        return this.datetTimeZone;
    }

    public void setDatetTimeZone(DateTimeZone datetTimeZone) {
        this.datetTimeZone = datetTimeZone;
    }
}
