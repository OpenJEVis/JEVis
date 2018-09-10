package org.jevis.jeconfig.plugin.graph;

import org.jevis.jeconfig.plugin.graph.data.CustomPeriodObject;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateHelper {
    private LocalTime startTime = LocalTime.of(0, 0, 0, 0);
    private LocalTime endTime = LocalTime.of(23, 59, 59, 999);
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate checkDate;
    private LocalTime checkTime;
    private TransformType type;
    private LocalDate now;
    private InputType inputType;
    private Boolean userSet = true;
    private CustomPeriodObject customPeriodObject;

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

    public DateHelper(CustomPeriodObject cpo, TransformType type) {
        this.customPeriodObject = cpo;
        this.type = type;
    }

    public LocalDate getStartDate() {
        now = LocalDate.now();
        if (startTime.isAfter(endTime)) now = now.minusDays(1);

        switch (type) {
            case CUSTOM:
                break;
            case TODAY:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST7DAYS:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(7);
                break;
            case LAST30DAYS:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(30);
                break;
            case LASTDAY:
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(1);
                break;
            case LASTWEEK:
                now = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).minusWeeks(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LASTMONTH:
                now = now.minusDays(LocalDate.now().getDayOfMonth() - 1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(1);
                break;
            case CUSTOM_PERIOD:
                if (customPeriodObject.getStartReferencePoint() != null) {
                    switch (customPeriodObject.getStartReferencePoint()) {
                        case "NOW":
                            startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusYears(customPeriodObject.getStartMinusYears());
                            startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(customPeriodObject.getStartMinusMonths());
                            startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(customPeriodObject.getEndMinusDays());
                            break;
                        case "STARTTIMEDAY":
                            startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusYears(customPeriodObject.getStartMinusYears());
                            startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(customPeriodObject.getStartMinusMonths());
                            startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(customPeriodObject.getEndMinusDays());
                            break;
                        case "CUSTOM_PERIOD":
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        return startDate;
    }

    public DateTime getDateTimeStartDate() {
        return new DateTime(getStartDate().getYear(), getStartDate().getMonth().getValue(), getStartDate().getDayOfMonth(),
                getStartTime().getHour(), getStartTime().getMinute(), getStartTime().getSecond(), 0);
    }

    public LocalDate getEndDate() {
        now = LocalDate.now();
        //if (startTime.isAfter(endTime)) now = now.minusDays(1);

        switch (type) {
            case CUSTOM:
                break;
            case TODAY:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST7DAYS:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST30DAYS:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LASTDAY:
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(1);
                break;
            case LASTWEEK:
                now = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).minusWeeks(1);
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).plusDays(6);
                break;
            case LASTMONTH:
                now = now.minusDays(LocalDate.now().getDayOfMonth() - 1);
                endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(1);
                break;
            case CUSTOM_PERIOD:
                if (customPeriodObject.getEndReferencePoint() != null) {
                    switch (customPeriodObject.getEndReferencePoint()) {
                        case "NOW":
                            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusYears(customPeriodObject.getEndMinusYears());
                            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(customPeriodObject.getEndMinusMonths());
                            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(customPeriodObject.getEndMinusDays());
                            break;
                        case "STARTTIMEDAY":
                            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusYears(customPeriodObject.getEndMinusYears());
                            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(customPeriodObject.getEndMinusMonths());
                            endDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(customPeriodObject.getEndMinusDays());
                            break;
                        case "CUSTOM_PERIOD":
                            break;
                        default:
                            break;
                    }
                }
                break;
            default:
                break;
        }
        return endDate;
    }

    public DateTime getDateTimeEndDate() {
        return new DateTime(getEndDate().getYear(), getEndDate().getMonth().getValue(), getEndDate().getDayOfMonth(),
                getEndTime().getHour(), getEndTime().getMinute(), getEndTime().getSecond(), 999);
    }

    public void setType(TransformType type) {
        this.type = type;
    }

    public LocalTime getStartTime() {
        if (!type.equals(TransformType.CUSTOM_PERIOD)) {
            return startTime;
        } else {
            LocalTime nowTime = LocalTime.now();
            if (customPeriodObject.getStartReferencePoint() != null) {
                switch (customPeriodObject.getStartReferencePoint()) {
                    case "NOW":
                        startTime = LocalTime.of(nowTime.getHour(), nowTime.getMinute(), nowTime.getSecond()).minusHours(customPeriodObject.getStartMinusHours());
                        startTime = LocalTime.of(nowTime.getHour(), nowTime.getMinute(), nowTime.getSecond()).minusMinutes(customPeriodObject.getStartMinusMinutes());
                        break;
                    case "STARTTIMEDAY":
                        startTime = LocalTime.of(startTime.getHour(), startTime.getMinute(), startTime.getSecond()).minusHours(customPeriodObject.getStartMinusHours());
                        startTime = LocalTime.of(startTime.getHour(), startTime.getMinute(), startTime.getSecond()).minusMinutes(customPeriodObject.getStartMinusMinutes());
                        break;
                    case "CUSTOM_PERIOD":
                        break;
                    default:
                        break;
                }
            }
            return startTime;
        }
    }


    public LocalTime getEndTime() {
        if (!type.equals(TransformType.CUSTOM_PERIOD)) {
            return endTime;
        } else {
            LocalTime nowTime = LocalTime.now();
            if (customPeriodObject.getEndReferencePoint() != null) {
                switch (customPeriodObject.getEndReferencePoint()) {
                    case "NOW":
                        endTime = LocalTime.of(nowTime.getHour(), nowTime.getMinute(), nowTime.getSecond()).minusHours(customPeriodObject.getStartMinusHours());
                        endTime = LocalTime.of(nowTime.getHour(), nowTime.getMinute(), nowTime.getSecond()).minusMinutes(customPeriodObject.getStartMinusMinutes());
                        break;
                    case "STARTTIMEDAY":
                        endTime = LocalTime.of(endTime.getHour(), endTime.getMinute(), endTime.getSecond()).minusHours(customPeriodObject.getStartMinusHours());
                        endTime = LocalTime.of(endTime.getHour(), endTime.getMinute(), endTime.getSecond()).minusMinutes(customPeriodObject.getStartMinusMinutes());
                        break;
                    case "CUSTOM_PERIOD":
                        break;
                    default:
                        break;
                }
            }
            return endTime;
        }
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

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public enum TransformType {CUSTOM, TODAY, LAST7DAYS, LAST30DAYS, LASTDAY, LASTWEEK, LASTMONTH, CUSTOM_PERIOD}

    public enum InputType {STARTDATE, ENDDATE, STARTTIME, ENDTIME}

    public enum CustomReferencePoint {

        NOW(I18n.getInstance().getString("graph.datehelper.referencepoint.now")), CUSTOM_PERIOD(I18n.getInstance().getString("graph.datehelper.referencepoint.customperiod")),
        WEEKDAY(I18n.getInstance().getString("graph.datehelper.referencepoint.weekday")), MONTH(I18n.getInstance().getString("graph.datehelper.referencepoint.month")),
        STARTTIMEDAY(I18n.getInstance().getString("graph.datehelper.referencepoint.starttimeday")), EDNTIMEDAY(I18n.getInstance().getString("graph.datehelper.referencepoint.endtimeday"));

        private final String referencePointName;

        CustomReferencePoint(String referencePointName) {
            this.referencePointName = referencePointName;
        }

        public String getReferencePointName() {
            return referencePointName;
        }
    }

    public enum Weekday {

        MONDAY(I18n.getInstance().getString("graph.datehelper.weekday.monday")), TUESDAY(I18n.getInstance().getString("graph.datehelper.weekday.tuesday")),
        WEDNESDAY(I18n.getInstance().getString("graph.datehelper.weekday.wednesday")), THURSDAY(I18n.getInstance().getString("graph.datehelper.weekday.thursday")),
        FRIDAY(I18n.getInstance().getString("graph.datehelper.weekday.friday")), SATURDAY(I18n.getInstance().getString("graph.datehelper.weekday.saturday")),
        SUNDAY(I18n.getInstance().getString("graph.datehelper.weekday.sunday"));

        private final String weekdayName;

        Weekday(String weekdayName) {
            this.weekdayName = weekdayName;
        }

        public String getWeekdayName() {
            return weekdayName;
        }
    }

    public enum Month {

        JANUARY(I18n.getInstance().getString("graph.datehelper.months.january")), FEBRUARY(I18n.getInstance().getString("graph.datehelper.months.february")),
        MARCH(I18n.getInstance().getString("graph.datehelper.months.march")), APRIL(I18n.getInstance().getString("graph.datehelper.months.april")),
        MAY(I18n.getInstance().getString("graph.datehelper.months.may")), JUNE(I18n.getInstance().getString("graph.datehelper.months.june")),
        JULY(I18n.getInstance().getString("graph.datehelper.months.july")), AUGUST(I18n.getInstance().getString("graph.datehelper.months.august")),
        SEPTEMBER(I18n.getInstance().getString("graph.datehelper.months.september")), OCTOBER(I18n.getInstance().getString("graph.datehelper.months.october")),
        NOVEMBER(I18n.getInstance().getString("graph.datehelper.months.november")), DECEMBER(I18n.getInstance().getString("graph.datehelper.months.december"));

        private final String monthName;

        Month(String monthName) {
            this.monthName = monthName;
        }

        public String getMonthName() {
            return monthName;
        }
    }
}
