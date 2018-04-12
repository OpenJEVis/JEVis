/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.datatype.scheduler.handler;

import java.util.List;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * Scheduler handler - gets true if the datetime satisfies at least one rule of scheduler, otherwise false.
 * @author Artur Iablokov
 */
public class SchedulerContainsHandler extends SchedulerServiceHandler {

    /**
     * gets true if the datetime satisfies at least one rule of scheduler, otherwise false.
     * @param dt
     * @param scheduler
     * @return 
     */
    public boolean contains(DateTime dt, CronScheduler scheduler) {
        DateTimeZone dtz = scheduler.getDatetTimeZone();
        List<SchedulerRule> rules = scheduler.getAllRules();

        for (SchedulerRule rule : rules) {
            if (checkDate(dt.withZone(dtz), rule)) {               
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
