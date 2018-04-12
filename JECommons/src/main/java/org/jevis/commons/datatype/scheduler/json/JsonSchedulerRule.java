/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.datatype.scheduler.json;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * json scheduler rule
 * 
 * @author Artur Iablokov
 */
public class JsonSchedulerRule {
    /**
     * months
     */
    private int[] months;
    /**
     * days of month
     */
    private String dayOfMonth;
    /**
     * days of week
     */
    private int[] dayOfWeek;
    /**
     * start time - hour
     */
    private String startTimeHours;
    /**
     * start time - minutes
     */
    private String startTimeMinutes;
    /**
     * end time - hour
     */
    private String endTimeHours;
    /**
     * end time - minutes
     */
    private String endTimeMinutes;

    @JsonGetter("months")
    public int[] getMonths() {
        return months;
    }    
    @JsonSetter("months")
    public void setMonths(int[] months) {
        this.months = months;
    }
    @JsonGetter("dayOfMonth")
    public String getDayOfMonth() {
        return dayOfMonth;
    }
    @JsonSetter("dayOfMonth")
    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
    @JsonGetter("dayOfWeek")
    public int[] getDayOfWeek() {
        return dayOfWeek;
    }
    @JsonSetter("dayOfWeek")
    public void setDayOfWeek(int[] dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    @JsonGetter("startTimeHours")
    public String getStartTimeHours() {
        return startTimeHours;
    }
    @JsonSetter("startTimeHours")
    public void setStartTimeHours(String startTimeHours) {
        this.startTimeHours = startTimeHours;
    }
    @JsonGetter("startTimeMinutes")
    public String getStartTimeMinutes() {
        return startTimeMinutes;
    }
    @JsonSetter("startTimeMinutes")
    public void setStartTimeMinutes(String startTimeMinutes) {
        this.startTimeMinutes = startTimeMinutes;
    }
    @JsonGetter("endTimeHours")
    public String getEndTimeHours() {
        return endTimeHours;
    }
    @JsonSetter("endTimeHours")
    public void setEndTimeHours(String endTimeHours) {
        this.endTimeHours = endTimeHours;
    }
    @JsonGetter("endTimeMinutes")
    public String getEndTimeMinutes() {
        return endTimeMinutes;
    }
    @JsonSetter("endTimeMinutes")
    public void setEndTimeMinutes(String endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

}
