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

import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.datatype.scheduler.cron.CronTime;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.jevis.commons.datatype.scheduler.handler.SchedulerServiceHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Attention! This class/method not tested!
 * @author Artur Iablokov
 */
public class SchedulerCronHandler extends SchedulerServiceHandler {

    public boolean isAfter(DateTime last, CronScheduler scheduler, int schedulerType) {

        DateTimeZone dtz = scheduler.getDatetTimeZone();
        for (SchedulerRule r : scheduler.getAllRules()) {
            CronTime t = r.getStartTime();
            DateTime minNextRun = null;
            boolean run = true;

            if (schedulerType == 0) {
                minNextRun = last.withTime(0, 0, 0, 0);
            } else if (schedulerType == 1) {
                minNextRun = last;
            }

            if (!(t.getHour().isAlias() || t.getMin().isAlias())) {
                minNextRun = last;
                while (run) {
                    if (checkDate(minNextRun, r) && minNextRun.isAfter(last)) {
                        run = false;
                    } else {
                        minNextRun.plusDays(1);
                    }
                }
            } else {
                while (run) {
                    if (checkDate(minNextRun, r) && minNextRun.isAfter(last)) {
                        run = false;
                    } else {
                        if (t.getHour().isAlias()) {
                            minNextRun.plusHours(t.getHour().getDenominator());
                        }
                        if (t.getMin().isAlias()) {
                            minNextRun.plusMinutes(t.getMin().getDenominator());
                        }
                    }
                }
            }
            DateTime now = DateTime.now(dtz);
            if (now.isAfter(minNextRun)) {
                return true;
            }
        }
        return false;
    }

}
