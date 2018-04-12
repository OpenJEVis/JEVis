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
package org.jevis.commons.datatype.scheduler.cron;

import java.util.ArrayList;
import java.util.List;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTimeZone;

/**
 * Is default Scheduler implementation.
 * has timezone and list of rules
 * @author Artur Iablokov
 */
public class CronScheduler {
    
    private final List<SchedulerRule> rules = new ArrayList<>();
    private DateTimeZone datetTimeZone;
    
    /**
     * adds rule to scheduler
     * @param scheduler 
     */
    public void addRule(SchedulerRule scheduler) {
        rules.add(scheduler);
    }

    /**
     * remove rule from scheduler
     * @param scheduler 
     */
    public void removeRule(SchedulerRule scheduler) {
        rules.remove(scheduler);
    }

    /**
     * gets list of rules
     * @return 
     */
    public List<SchedulerRule> getAllRules() {
        return rules;
    }

    /**
     * gets timezone of this scheduler
     * @return 
     */
    public DateTimeZone getDatetTimeZone() {
        return datetTimeZone;
    }

    /**
     *  sets timezone for this scheduler
     * @param datetTimeZone 
     */
    public void setDatetTimeZone(DateTimeZone datetTimeZone) {
        this.datetTimeZone = datetTimeZone;
    }
}
