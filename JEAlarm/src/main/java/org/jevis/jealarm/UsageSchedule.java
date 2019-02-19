/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm;


import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.datatype.scheduler.handler.SchedulerContainsHandler;
import org.joda.time.DateTime;

public class UsageSchedule {

    private final CronScheduler scheduler;
    private final UsageScheduleType type;
    private final SchedulerContainsHandler handler = new SchedulerContainsHandler();

    public UsageSchedule(CronScheduler scheduler, UsageScheduleType type) {
        this.scheduler = scheduler;
        this.type = type;
    }

    public boolean contains(DateTime dt) {
        return handler.contains(dt, scheduler);
    }

    public UsageScheduleType getType() {
        return type;
    }
}
