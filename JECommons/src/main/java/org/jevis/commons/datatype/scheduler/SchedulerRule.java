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
package org.jevis.commons.datatype.scheduler;

import org.jevis.commons.datatype.scheduler.cron.CronTime;
import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.Month;
import org.joda.time.DateTime;

/**
 *
 * @author Artur Iablokov
 */
public interface SchedulerRule {

    /**
     * sets day of week, true - enabled / false disabled
     *
     * @param day
     * @param enable
     */
    public void setDayOfWeek(DayOfWeek day, boolean enable);

    /**
     * gets true if day of week is enabled, otherwise false
     *
     * @param day
     * @return
     */
    public boolean isDayOfWeekEnabled(DayOfWeek day);

    /**
     * gets days of week as integer array
     *
     * @return
     */
    public int[] getDayOfWeekArray();

    /**
     * sets month, true - enabled / false disabled
     *
     * @param month
     * @param enable
     */
    public void setMonth(Month month, boolean enable);

    /**
     * sets all months, true enable all months, otherwise disable
     *
     * @param enable
     */
    public void setAllMonths(boolean enable);

    /**
     * gets true if month is enabled, otherwise false
     *
     * @param day
     * @return
     */
    public boolean isMonthEnabled(Month day);

    /**
     * gets months as integer array
     *
     * @return
     */
    public int[] getMonthArray();

    /**
     * set day of months examples: "*", "1", "1,2,3,4,31", "1,2,3,5-10", "LAST",
     * "1,LAST"
     *
     * @param dayOfMonth
     * @throws InvalidParameterException
     */
    public void setDayOfMonths(String dayOfMonth) throws InvalidParameterException;

    /**
     * gets true if day of month is enabled, otherwise false
     *
     * @param dt
     * @return
     */
    public boolean isDayOfMonthEnabled(DateTime dt);

    /**
     * gets day of month
     *
     * @return @throws InvalidParameterException
     */
    public String getDayOfMonth() throws InvalidParameterException;

    /**
     * sets start hour
     *
     * @param h
     */
    public void setStartHour(String h);

    /**
     * sets end hour
     *
     * @param h
     */
    public void setEndHour(String h);

    /**
     * sets start minute
     *
     * @param m
     */
    public void setStartMinute(String m);

    /**
     * sets end minute
     *
     * @param m
     */
    public void setEndMinute(String m);

    /**
     * gets start time object
     *
     * @return
     */
    public CronTime getStartTime();

    /**
     * gets end time object
     *
     * @return
     */
    public CronTime getEndTime();

    /**
     * gets start hour
     *
     * @return
     */
    public String getStartHour();

    /**
     * gets end hour
     *
     * @return
     */
    public String getEndHour();

    /**
     * gets start minutes
     *
     * @return
     */
    public String getStartMinute();

    /**
     * gets end minutes
     *
     * @return
     */
    public String getEndMinute();
}
