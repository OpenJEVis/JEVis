package org.jevis.jeconfig.plugin.charts;

import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;

public class BaseLoadSetting {
    private DateTime baseLoadStart;
    private DateTime baseLoadEnd;
    private int repeatType = 0;
    private DateTime resultStart;
    private DateTime resultEnd;

    public BaseLoadSetting() {

    }

    public void setBaseLoadStart(LocalDate baseLoadStartDate, LocalTime baseLoadStartTime) {
        this.baseLoadStart = new DateTime(baseLoadStartDate.getYear(), baseLoadStartDate.getMonthValue(), baseLoadStartDate.getDayOfMonth(),
                baseLoadStartTime.getHour(), baseLoadStartTime.getMinute(), baseLoadStartTime.getSecond(), 0);
    }

    public void setBaseLoadEnd(LocalDate baseLoadEndDate, LocalTime baseLoadEndTime) {
        this.baseLoadEnd = new DateTime(baseLoadEndDate.getYear(), baseLoadEndDate.getMonthValue(), baseLoadEndDate.getDayOfMonth(),
                baseLoadEndTime.getHour(), baseLoadEndTime.getMinute(), baseLoadEndTime.getSecond(), 999);
    }

    public void setResultStart(LocalDate resultStartDate, LocalTime resultStartTime) {
        this.resultStart = new DateTime(resultStartDate.getYear(), resultStartDate.getMonthValue(), resultStartDate.getDayOfMonth(),
                resultStartTime.getHour(), resultStartTime.getMinute(), resultStartTime.getSecond(), 0);
    }

    public void setResultEnd(LocalDate resultEndDate, LocalTime resultEndTime) {
        this.resultEnd = new DateTime(resultEndDate.getYear(), resultEndDate.getMonthValue(), resultEndDate.getDayOfMonth(),
                resultEndTime.getHour(), resultEndTime.getMinute(), resultEndTime.getSecond(), 999);
    }

    public DateTime getBaseLoadStart() {
        return baseLoadStart;
    }

    public DateTime getBaseLoadEnd() {
        return baseLoadEnd;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public DateTime getResultStart() {
        return resultStart;
    }

    public DateTime getResultEnd() {
        return resultEnd;
    }
}
