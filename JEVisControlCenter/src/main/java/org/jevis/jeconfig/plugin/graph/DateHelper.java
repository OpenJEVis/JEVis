package org.jevis.jeconfig.plugin.graph;

import org.jevis.commons.database.ObjectHandler;
import org.jevis.jeconfig.plugin.graph.data.CustomPeriodObject;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

        switch (type) {
            case CUSTOM:
                break;
            case TODAY:
                if (startTime.isAfter(endTime)) now = now.minusDays(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LAST7DAYS:
                if (startTime.isAfter(endTime)) now = now.minusDays(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(7);
                break;
            case LAST30DAYS:
                if (startTime.isAfter(endTime)) now = now.minusDays(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(30);
                break;
            case LASTDAY:
                if (startTime.isAfter(endTime)) now = now.minusDays(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusDays(1);
                break;
            case LASTWEEK:
                if (startTime.isAfter(endTime)) now = now.minusDays(1);
                now = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).minusWeeks(1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
                break;
            case LASTMONTH:
                now = now.minusDays(LocalDate.now().getDayOfMonth() - 1);
                startDate = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth()).minusMonths(1);
                break;
            case CUSTOM_PERIOD:
                if (customPeriodObject.getStartReferencePoint() != null) {
                    if (startTime.isAfter(endTime)) now = now.minusDays(1);
                    Long startYears = 0L;
                    Long startMonths = 0L;
                    Long startWeeks = 0L;
                    Long startDays = 0L;
                    switch (customPeriodObject.getStartReferencePoint()) {
                        case "NOW":
                            break;
                        case "STARTTIMEDAY":
                            break;
                        case "CUSTOM_PERIOD":
                            try {
                                CustomPeriodObject cpo = new CustomPeriodObject(customPeriodObject.getStartReferenceObject(),
                                        new ObjectHandler(customPeriodObject.getObject().getDataSource()));
                                DateHelper dh = new DateHelper(cpo, TransformType.CUSTOM_PERIOD);
                                dh.setStartTime(startTime);
                                dh.setEndTime(endTime);
                                dh.setStartDate(startDate);
                                dh.setEndDate(endDate);
                                if (cpo.getStartReferencePoint().contains("DAY")) {

                                    Long startInterval = customPeriodObject.getStartInterval();
                                    DateTime newDT = getDateTimeForDayPeriod(dh, startInterval);

                                    now = LocalDate.of(newDT.getYear(), newDT.getMonthOfYear(), newDT.getDayOfMonth());
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "CURRENT_WEEK":
                            now = now.minusDays(now.getDayOfWeek().getValue());
                            break;
                        case "CURRENT_MONTH":
                            now = now.minusDays(now.getDayOfMonth());
                            break;
                        case "CURRENT_DAY":
                            break;
                        default:
                            break;
                    }

                    startYears = customPeriodObject.getStartYears();
                    startMonths = customPeriodObject.getStartMonths();
                    startWeeks = customPeriodObject.getStartWeeks();
                    startDays = customPeriodObject.getStartDays();

                    if (startYears < 0)
                        startDate = now.minusYears(Math.abs(startYears));
                    else if (startYears > 0)
                        startDate = now.plusYears(Math.abs(startYears));
                    else startDate = now;

                    if (startMonths < 0)
                        startDate = startDate.minusMonths(Math.abs(startMonths));
                    else if (startMonths > 0)
                        startDate = startDate.plusMonths(Math.abs(startMonths));

                    if (startWeeks < 0)
                        startDate = startDate.minusWeeks(Math.abs(startWeeks));
                    else if (startWeeks > 0)
                        startDate = startDate.plusWeeks(Math.abs(startWeeks));

                    if (startDays < 0)
                        startDate = startDate.minusDays(Math.abs(startDays));
                    else if (startDays > 0)
                        startDate = startDate.plusDays(Math.abs(startDays));
                }
                break;
            default:
                break;
        }
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public DateTime getDateTimeStartDate() {
        return new DateTime(getStartDate().getYear(), getStartDate().getMonth().getValue(), getStartDate().getDayOfMonth(),
                getStartTime().getHour(), getStartTime().getMinute(), getStartTime().getSecond(), 0);
    }

    private DateTime getDateTimeForDayPeriod(DateHelper dh, Long interval) {
        DateTime returnTimeStamp = null;

        DateTime start = new DateTime(startDate.getYear(), startDate.getMonth().getValue(), startDate.getDayOfMonth(),
                startTime.getHour(), startTime.getMinute(), startTime.getSecond(), 999);
        DateTime end = new DateTime(now.getYear(), now.getMonth().getValue(), now.getDayOfMonth(),
                endTime.getHour(), endTime.getMinute(), endTime.getSecond(), 999);

        Long d = dh.getDateTimeEndDate().getMillis() - dh.getDateTimeStartDate().getMillis();

        if (d > 0) {
            List<DateTime> listTimeStamps = new ArrayList<>();
            DateTime currentDateTime = start.plus(d);
            while (currentDateTime.isBefore(end)) {
                listTimeStamps.add(currentDateTime);
                currentDateTime = currentDateTime.plus(d);
            }

            if (interval < 0) {
                Integer index = listTimeStamps.size() - (int) Math.abs(interval);
                returnTimeStamp = listTimeStamps.get(index);
            } else if (interval > 0) {
                Integer index = (int) Math.abs(interval);
                returnTimeStamp = listTimeStamps.get(index);
            } else returnTimeStamp = listTimeStamps.get(listTimeStamps.size() - 1);
        }

        return returnTimeStamp;
    }

    public DateTime getDateTimeEndDate() {
        return new DateTime(getEndDate().getYear(), getEndDate().getMonth().getValue(), getEndDate().getDayOfMonth(),
                getEndTime().getHour(), getEndTime().getMinute(), getEndTime().getSecond(), 999);
    }

    public void setType(TransformType type) {
        this.type = type;
    }

    public LocalDate getEndDate() {
        now = LocalDate.now();
        //if (startTime.isAfter(endTime)) now = now.minusDays(1);

        switch (type) {
            case CUSTOM:
                break;
            case TODAY:
                endDate = now;
                break;
            case LAST7DAYS:
                endDate = now;
                break;
            case LAST30DAYS:
                endDate = now;
                break;
            case LASTDAY:
                endDate = now.minusDays(1);
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
                    Long endYears = 0L;
                    Long endMonths = 0L;
                    Long endWeeks = 0L;
                    Long endDays = 0L;
                    switch (customPeriodObject.getEndReferencePoint()) {
                        case "NOW":
                            break;
                        case "STARTTIMEDAY":
                            break;
                        case "CUSTOM_PERIOD":
                            try {
                                CustomPeriodObject cpo = new CustomPeriodObject(customPeriodObject.getEndReferenceObject(),
                                        new ObjectHandler(customPeriodObject.getObject().getDataSource()));
                                DateHelper dh = new DateHelper(cpo, TransformType.CUSTOM_PERIOD);
                                dh.setStartTime(startTime);
                                dh.setEndTime(endTime);
                                if (cpo.getEndReferencePoint().contains("DAY")) {

                                    Long endInterval = customPeriodObject.getEndInterval();
                                    DateTime newDT = getDateTimeForDayPeriod(dh, endInterval);

                                    now = LocalDate.of(newDT.getYear(), newDT.getMonthOfYear(), newDT.getDayOfMonth());
                                }
                            } catch (Exception e) {
                            }
                            break;
                        case "CURRENT_WEEK":
                            now = now.minusDays(now.getDayOfWeek().getValue());
                            break;
                        case "CURRENT_MONTH":
                            now = now.minusDays(now.getDayOfMonth());
                            break;
                        case "CURRENT_DAY":
                            break;
                        default:
                            break;
                    }
                    endYears = customPeriodObject.getEndYears();
                    endMonths = customPeriodObject.getEndMonths();
                    endWeeks = customPeriodObject.getEndWeeks();
                    endDays = customPeriodObject.getEndDays();

                    if (endYears < 0)
                        endDate = now.minusYears(Math.abs(endYears));
                    else if (endYears > 0)
                        endDate = now.plusYears(Math.abs(endYears));
                    else endDate = now;

                    if (endMonths < 0)
                        endDate = endDate.minusMonths(Math.abs(endMonths));
                    else if (endMonths > 0)
                        endDate = endDate.plusMonths(Math.abs(endMonths));

                    if (endWeeks < 0)
                        endDate = endDate.minusWeeks(Math.abs(endWeeks));
                    else if (endWeeks > 0)
                        endDate = endDate.plusWeeks(Math.abs(endWeeks));

                    if (endDays < 0)
                        endDate = endDate.minusDays(Math.abs(endDays));
                    else if (endDays > 0)
                        endDate = endDate.plusDays(Math.abs(endDays));
                }
                break;
            default:
                break;
        }
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        if (!type.equals(TransformType.CUSTOM_PERIOD)) {
            return startTime;
        } else {
            if (customPeriodObject.getStartReferencePoint() != null) {
                Long startHours = 0L;
                Long startMinutes = 0L;
                switch (customPeriodObject.getStartReferencePoint()) {
                    case "NOW":
                        startTime = LocalTime.now();
                        break;
                    case "STARTTIMEDAY":
                        break;
                    case "WEEK":
                        break;
                    case "CUSTOM_PERIOD":
                        try {
                            CustomPeriodObject cpo = new CustomPeriodObject(customPeriodObject.getStartReferenceObject(),
                                    customPeriodObject.getObjectHandler());
                            DateHelper dh = new DateHelper(cpo, TransformType.CUSTOM_PERIOD);
                            dh.setStartTime(startTime);
                            dh.setEndTime(endTime);
                            if (cpo.getStartReferencePoint().contains("DAY")) {

                                Long startInterval = customPeriodObject.getStartInterval();
                                DateTime newDT = getDateTimeForDayPeriod(dh, startInterval);

                                startTime = LocalTime.of(newDT.getHourOfDay(), newDT.getMinuteOfHour(), newDT.getSecondOfMinute());
                            }
                        } catch (Exception e) {
                        }
                        break;
                    case "CURRENT_DAY":
                        break;
                    default:
                        break;
                }

                startHours = customPeriodObject.getStartHours();
                if (startHours < 0)
                    startTime = startTime.minusHours(Math.abs(startHours));
                else if (startHours > 0)
                    startTime = startTime.plusHours(Math.abs(startHours));

                startMinutes = customPeriodObject.getStartMinutes();
                if (startMinutes < 0)
                    startTime = startTime.minusMinutes(Math.abs(startMinutes));
                else if (startMinutes > 0)
                    startTime = startTime.plusMinutes(Math.abs(startMinutes));
            }
            return startTime;
        }
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

    public LocalTime getEndTime() {
        if (!type.equals(TransformType.CUSTOM_PERIOD)) {
            return endTime;
        } else {
            if (customPeriodObject.getEndReferencePoint() != null) {
                Long endHours = 0L;
                Long endMinutes = 0L;
                switch (customPeriodObject.getEndReferencePoint()) {
                    case "NOW":
                        endTime = LocalTime.now();
                        break;
                    case "STARTTIMEDAY":
                        break;
                    case "CUSTOM_PERIOD":
                        try {
                            CustomPeriodObject cpo = new CustomPeriodObject(customPeriodObject.getEndReferenceObject(),
                                    new ObjectHandler(customPeriodObject.getObject().getDataSource()));
                            DateHelper dh = new DateHelper(cpo, TransformType.CUSTOM_PERIOD);
                            dh.setStartTime(startTime);
                            dh.setEndTime(endTime);
                            if (cpo.getEndReferencePoint().contains("DAY")) {

                                Long endInterval = customPeriodObject.getEndInterval();
                                DateTime newDT = getDateTimeForDayPeriod(dh, endInterval);

                                endTime = LocalTime.of(newDT.getHourOfDay(), newDT.getMinuteOfHour(), newDT.getSecondOfMinute());
                            }
                        } catch (Exception e) {
                        }
                        break;
                    case "WEEK":
                        break;
                    case "CURRENT_DAY":
                        break;
                    default:
                        break;
                }
                endHours = customPeriodObject.getEndHours();
                if (endHours < 0)
                    endTime = endTime.minusHours(Math.abs(endHours));
                else if (endHours > 0)
                    endTime = endTime.plusHours(Math.abs(endHours));

                endMinutes = customPeriodObject.getEndMinutes();
                if (endMinutes < 0)
                    endTime = endTime.minusMinutes(Math.abs(endMinutes));
                else if (endMinutes > 0)
                    endTime = endTime.plusMinutes(Math.abs(endMinutes));
            }
            return endTime;
        }
    }

    public Boolean isCustom() {
        switch (inputType) {
            case STARTDATE:
                for (TransformType tt : TransformType.values()) {
                    DateHelper dh = new DateHelper(tt);
                    dh.setStartTime(startTime);
                    dh.setEndTime(endTime);
                    dh.setStartDate(getStartDate());
                    dh.setEndDate(getEndDate());
                    dh.setCheckDate(checkDate);
                    if (dh.getCheckDate().equals(dh.getStartDate())) {
                        userSet = false;
                        break;
                    }
                }
                break;
            case ENDDATE:
                for (TransformType tt : TransformType.values()) {
                    DateHelper dh = new DateHelper(tt);
                    dh.setStartTime(startTime);
                    dh.setEndTime(endTime);
                    dh.setStartDate(getStartDate());
                    dh.setEndDate(getEndDate());
                    dh.setCheckDate(checkDate);
                    if (dh.getCheckDate().equals(dh.getEndDate())) {
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

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public void setCustomPeriodObject(CustomPeriodObject customPeriodObject) {
        this.customPeriodObject = customPeriodObject;
    }

    public void setCheckTime(LocalTime checkTime) {
        this.checkTime = checkTime;
    }

    public LocalDate getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(LocalDate checkDate) {
        this.checkDate = checkDate;
    }
}
