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

import org.jevis.commons.datatype.scheduler.cron.CronTime;
import org.jevis.commons.datatype.scheduler.cron.CronDayOfMonth;
import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.joda.time.DateTime;

/**
 * Default scheduler rule implementation
 * @author Artur Iablokov
 */
public class CronRule implements SchedulerRule {

    private final CronTime start = new CronTime();
    private final CronTime end = new CronTime();

    private final List<DayOfWeek> weekDays = new ArrayList<>();
    private CronDayOfMonth monthDays = null;
    private final List<Month> months = new ArrayList<>();

    @Override
    public void setDayOfWeek(DayOfWeek day, boolean enable) {
        if (enable) {
            weekDays.add(day);
        } else {
            weekDays.remove(day);
        }
    }

    @Override
    public boolean isDayOfWeekEnabled(DayOfWeek day) {
        return weekDays.contains(day);
    }

    @Override
    public void setMonth(Month month, boolean enable) {
        if (enable) {
            months.add(month);
        } else {
            months.remove(month);
        }
    }

    @Override
    public void setAllMonths(boolean enable) {
        for (Month m : Month.values()) {
            setMonth(m, enable);
        }
    }

    @Override
    public boolean isMonthEnabled(Month day) {
        return months.contains(day);
    }

    @Override
    public void setDayOfMonths(String dayOfMonth) throws InvalidParameterException {
        monthDays = new CronDayOfMonth();
        monthDays.init(dayOfMonth);
    }

    @Override
    public boolean isDayOfMonthEnabled(DateTime dt) {
        if (dt != null) {
            if (monthDays.isAllDays()) {
                return true;
            } else if (monthDays.isLastDay()) {
                int lastDayOfMonth = dt.dayOfMonth().withMaximumValue().getDayOfMonth();
                if (dt.getDayOfMonth() == lastDayOfMonth) {
                    return true;
                }
            }
            return monthDays.isDayEnabled(dt.getDayOfMonth());      
        }
        throw new InvalidParameterException("datetime value is null or corrupt");
    }

    @Override
    public String getDayOfMonth() {
        return monthDays.getDayOfMonthValue();
    }

    @Override
    public void setStartHour(String h) {
        start.setHour(h);
    }

    @Override
    public void setEndHour(String h) {
        end.setHour(h);
    }

    @Override
    public void setStartMinute(String m) {
        start.setMin(m);
    }

    @Override
    public void setEndMinute(String m) {
        end.setMin(m);
    }

    @Override
    public String getStartHour() {
        return start.getHour().getValue();
    }

    @Override
    public String getEndHour() {
        return end.getHour().getValue();
    }

    @Override
    public String getStartMinute() {
        return start.getMin().getValue();
    }

    @Override
    public String getEndMinute() {
        return end.getMin().getValue();
    }

    @Override
    public int[] getDayOfWeekArray() {
        List<Integer> ar = new ArrayList<>();
        weekDays.stream().forEach((d) -> {
            ar.add(d.getValue());
        });
        return ar.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public int[] getMonthArray() {
        List<Integer> ar = new ArrayList<>();
        months.stream().forEach((m) -> {
            ar.add(m.getValue());
        });
        return ar.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public CronTime getStartTime() {
        return start;
    }

    @Override
    public CronTime getEndTime() {
        return end;
    }
}
