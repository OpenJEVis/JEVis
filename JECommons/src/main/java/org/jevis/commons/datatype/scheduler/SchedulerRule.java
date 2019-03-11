//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler;

import org.jevis.commons.datatype.scheduler.cron.CronTime;
import org.joda.time.DateTime;

import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.Month;

public interface SchedulerRule {
    void setDayOfWeek(DayOfWeek var1, boolean var2);

    boolean isDayOfWeekEnabled(DayOfWeek var1);

    String getDayOfWeekArray();

    void setMonth(Month var1, boolean var2);

    void setAllMonths(boolean var1);

    boolean isMonthEnabled(Month var1);

    String getMonthArray();

    void setDayOfMonths(String var1) throws InvalidParameterException;

    boolean isDayOfMonthEnabled(DateTime var1);

    String getDayOfMonth() throws InvalidParameterException;

    CronTime getStartTime();

    CronTime getEndTime();

    String getStartHour();

    void setStartHour(String var1);

    String getEndHour();

    void setEndHour(String var1);

    String getStartMinute();

    void setStartMinute(String var1);

    String getEndMinute();

    void setEndMinute(String var1);
}
