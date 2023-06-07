package org.jevis.jecc.plugin.charts;

import org.jevis.jecc.sample.DaySchedule;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ValuesSetting {
    private DateTime resultStart;
    private DateTime resultEnd;

    private boolean inside;

    private Map<Integer, DaySchedule> dayScheduleMap = new HashMap<>();

    public ValuesSetting() {
    }

    public void setResultStart(LocalDate resultStartDate, LocalTime resultStartTime) {
        this.resultStart = new DateTime(resultStartDate.getYear(), resultStartDate.getMonthValue(), resultStartDate.getDayOfMonth(),
                resultStartTime.getHour(), resultStartTime.getMinute(), resultStartTime.getSecond(), 0);
    }

    public void setResultEnd(LocalDate resultEndDate, LocalTime resultEndTime) {
        this.resultEnd = new DateTime(resultEndDate.getYear(), resultEndDate.getMonthValue(), resultEndDate.getDayOfMonth(),
                resultEndTime.getHour(), resultEndTime.getMinute(), resultEndTime.getSecond(), 999);
    }

    public DateTime getResultStart() {
        return resultStart;
    }

    public DateTime getResultEnd() {
        return resultEnd;
    }

    public Map<Integer, DaySchedule> getDaySchedule() {
        return dayScheduleMap;
    }

    public void setDaySchedule(Map<Integer, DaySchedule> dayScheduleMap) {
        this.dayScheduleMap = dayScheduleMap;
    }

    public boolean isInside() {
        return inside;
    }

    public void setInside(boolean inside) {
        this.inside = inside;
    }
}
