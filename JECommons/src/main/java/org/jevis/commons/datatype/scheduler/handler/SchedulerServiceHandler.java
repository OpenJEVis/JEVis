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

import java.time.DayOfWeek;
import java.time.Month;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTime;

/**
 * Helper class for handlers
 * @author Artur Iablokov
 */
public abstract class SchedulerServiceHandler {
    
    protected boolean checkDate(DateTime dt, SchedulerRule rule) {
        Month m = Month.of(dt.getMonthOfYear());
        DayOfWeek dw = DayOfWeek.of(dt.getDayOfWeek());
        return rule.isMonthEnabled(m) && rule.isDayOfWeekEnabled(dw) && rule.isDayOfMonthEnabled(dt);
    }
}
