package org.jevis.jeconfig.plugin.graph;

import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateHelper {
    final LocalTime startTime = LocalTime.of(0, 0, 0, 0);
    final LocalTime endTime = LocalTime.of(23, 59, 59, 999);
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate checkDate;
    private LocalTime checkTime;
    private TransformType type;
    private LocalDate now;
    private InputType inputType;
    private Boolean userSet = true;

    public DateHelper(TransformType type) {
        this.type = type;
        now = LocalDate.now();
    }

    public DateHelper() {
        now = LocalDate.now();
    }

    public DateHelper(InputType inputType, LocalDate localDate) {
        this.inputType = inputType;
        checkDate = localDate;
    }

    public DateHelper(InputType inputType, LocalTime localTime) {
        this.inputType = inputType;
        checkTime = localTime;
    }

    public LocalDate getStartDate() {
        now = LocalDate.now();
        switch (type) {
            case CUSTOM:
                break;
            case LASTDAY:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST7DAYS:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(7);
                break;
            case LAST30DAYS:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(30);
                break;
            case LASTWEEK:
                now = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).minusWeeks(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LASTMONTH:
                now = now.minusDays(LocalDate.now().getDayOfMonth() - 1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(1);
                break;
            default:
                break;
        }
        return startDate;
    }

    public DateTime getDateTimeStartDate() {
        return new DateTime(getStartDate().getYear(), getStartDate().getMonth().getValue(), getStartDate().getDayOfMonth(), 0, 0, 0, 0);
    }

    public LocalDate getEndDate() {
        now = LocalDate.now();
        switch (type) {
            case CUSTOM:
                break;
            case LASTDAY:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST7DAYS:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST30DAYS:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LASTWEEK:
                now = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).minusWeeks(1);
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).plusDays(6);
                break;
            case LASTMONTH:
                now = now.minusDays(LocalDate.now().getDayOfMonth() - 1);
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(1);
                break;
            default:
                break;
        }
        return endDate;
    }

    public DateTime getDateTimeEndDate() {
        return new DateTime(getEndDate().getYear(), getEndDate().getMonth().getValue(), getEndDate().getDayOfMonth(), 23, 59, 59, 999);
    }

    public void setType(TransformType type) {
        this.type = type;
    }

    public LocalTime getStartTime() {
        return startTime;
    }


    public LocalTime getEndTime() {
        return endTime;
    }

    public Boolean isCustom() {
        switch (inputType) {
            case STARTDATE:
                for (TransformType tt : TransformType.values()) {
                    this.type = tt;
                    if (checkDate.equals(getStartDate())) {
                        userSet = false;
                        break;
                    }
                }
                break;
            case ENDDATE:
                for (TransformType tt : TransformType.values()) {
                    this.type = tt;
                    if (checkDate.equals(getEndDate())) {
                        userSet = false;
                        break;
                    }
                }
                break;
            case STARTTIME:
                if (checkTime.equals(getStartTime())) {
                    userSet = false;
                    break;
                }
                break;
            case ENDTIME:
                if (checkTime.equals(getEndTime())) {
                    userSet = false;
                    break;
                }
                break;
            default:
                break;
        }

        return userSet;
    }

    public enum TransformType {CUSTOM, LASTDAY, LAST7DAYS, LAST30DAYS, LASTWEEK, LASTMONTH}

    public enum InputType {STARTDATE, ENDDATE, STARTTIME, ENDTIME}
}
