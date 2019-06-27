package org.jevis.commons.datetime;

import org.jevis.api.JEVisAttribute;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.ObjectHandler;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

public class DateHelper {
    private LocalTime startTime = LocalTime.of(0, 0, 0, 0);
    private LocalTime endTime = LocalTime.of(23, 59, 59, 999);
    private DateTime startDate;
    private DateTime endDate;
    private LocalDate checkDate;
    private LocalTime checkTime;
    private TransformType type;
    private DateTime now;
    private InputType inputType;
    private Boolean userSet = true;
    private CustomPeriodObject customPeriodObject;
    private DateTime minStartDateTime;
    private DateTime maxEndDateTime;

    public DateHelper(TransformType type) {
        this.type = type;
        now = DateTime.now();
    }

    public DateHelper() {
        now = DateTime.now();
    }

    private DateTime nowStartWithTime() {
        if (startTime.isAfter(endTime)) {
            DateTime now = DateTime.now();
            return new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                    startTime.getHour(), startTime.getMinute(), startTime.getSecond()).minusDays(1);
        } else {
            return new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                    startTime.getHour(), startTime.getMinute(), startTime.getSecond());
        }
    }

    private DateTime nowEndWithTime() {
        DateTime now = DateTime.now();
        return new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                endTime.getHour(), endTime.getMinute(), endTime.getSecond());
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
        this.now = DateTime.now();
    }

    public DateTime getStartDate() {

        switch (type) {
            case CUSTOM:
                break;
            case TODAY:

                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                        startTime.getHour(), startTime.getMinute(), startTime.getSecond());

                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case LAST7DAYS:

                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                        startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusDays(7);

                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case LAST30DAYS:

                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                        startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusDays(30);

                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case YESTERDAY:
                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), startTime.getHour(),
                        startTime.getMinute(), startTime.getSecond())
                        .minusDays(1);
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case THISWEEK:
                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusDays(now.getDayOfWeek() - 1);
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case LASTWEEK:
                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusDays(now.getDayOfWeek() - 1).minusWeeks(1);
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case THISMONTH:
                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusDays(now.getDayOfMonth() - 1);
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case LASTMONTH:
                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusMonths(1).minusDays(now.getDayOfMonth() - 1);
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case THISYEAR:
                startDate = new DateTime(now.getYear(), 1, 1, startTime.getHour(), startTime.getMinute(), startTime.getSecond());
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case LASTYEAR:
                startDate = new DateTime(now.getYear(), 1, 1, startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                        .minusYears(1);
                if (startTime.isAfter(endTime)) startDate = startDate.minusDays(1);
                break;
            case CUSTOM_PERIOD:
                if (Objects.nonNull(customPeriodObject)) {
                    if (customPeriodObject.getStartReferencePoint() != null) {
                        Long startYears = 0L;
                        Long startMonths = 0L;
                        Long startWeeks = 0L;
                        Long startDays = 0L;
                        Long startHours = 0L;
                        Long startMinutes = 0L;
                        switch (customPeriodObject.getStartReferencePoint()) {
                            case "NOW":
                                startDate = DateTime.now();
                                break;
                            case "STARTTIMEDAY":
                                startDate = startDate.minusMillis(startDate.getMillisOfDay());
                                break;
                            case "CUSTOM_PERIOD":
                                try {
                                    CustomPeriodObject cpo = new CustomPeriodObject(customPeriodObject.getStartReferenceObject(),
                                            new ObjectHandler(customPeriodObject.getObject().getDataSource()));
                                    DateHelper dh = new DateHelper(cpo, TransformType.CUSTOM_PERIOD);
                                    dh.setStartTime(startTime);
                                    dh.setEndTime(endTime);
                                    if (cpo.getStartReferencePoint().contains("DAY")) {

                                        Long startInterval = customPeriodObject.getStartInterval();
                                        DateTime newDT = getStartDateTimeForDayPeriod(dh, startInterval);

                                        startDate = new DateTime(newDT.getYear(), newDT.getMonthOfYear(), newDT.getDayOfMonth(),
                                                newDT.getHourOfDay(), newDT.getMinuteOfHour(), newDT.getSecondOfMinute());
                                    }
                                } catch (Exception e) {
                                }
                                break;
                            case "CURRENT_WEEK":
                                now = DateTime.now();
                                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                                        startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                                        .minusDays(now.getDayOfWeek() - 1);
                                break;
                            case "CURRENT_MONTH":
                                now = DateTime.now();
                                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                                        startTime.getHour(), startTime.getMinute(), startTime.getSecond())
                                        .minusDays(now.getDayOfMonth());
                                break;
                            case "CURRENT_DAY":
                                now = DateTime.now();
                                startDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                                        startTime.getHour(), startTime.getMinute(), startTime.getSecond());
                                break;
                            case "SPECIFIED_DATE":
                                try {
                                    startYears = customPeriodObject.getEndYears();
                                    startMonths = customPeriodObject.getEndMonths();
                                    startDays = customPeriodObject.getEndDays();
                                    startHours = customPeriodObject.getEndHours();
                                    startMinutes = customPeriodObject.getEndMinutes();
                                    startDate = new DateTime(startYears.intValue(), startMonths.intValue(), startDays.intValue(),
                                            startHours.intValue(), startMinutes.intValue(), 59);
                                    return startDate;
                                } catch (Exception e) {
                                }
                                break;
                            default:
                                break;
                        }

                        startYears = customPeriodObject.getStartYears();
                        startMonths = customPeriodObject.getStartMonths();
                        startWeeks = customPeriodObject.getStartWeeks();
                        startDays = customPeriodObject.getStartDays();
                        startHours = customPeriodObject.getStartHours();
                        startMinutes = customPeriodObject.getStartMinutes();

                        if (startYears < 0)
                            startDate = startDate.minusYears((int) Math.abs(startYears));
                        else if (startYears > 0)
                            startDate = startDate.plusYears((int) Math.abs(startYears));

                        if (startMonths < 0)
                            startDate = startDate.minusMonths((int) Math.abs(startMonths));
                        else if (startMonths > 0)
                            startDate = startDate.plusMonths((int) Math.abs(startMonths));

                        if (startWeeks < 0)
                            startDate = startDate.minusWeeks((int) Math.abs(startWeeks));
                        else if (startWeeks > 0)
                            startDate = startDate.plusWeeks((int) Math.abs(startWeeks));

                        if (startDays < 0)
                            startDate = startDate.minusDays((int) Math.abs(startDays));
                        else if (startDays > 0)
                            startDate = startDate.plusDays((int) Math.abs(startDays));

                        if (startHours < 0)
                            startDate = startDate.minusHours((int) Math.abs(startHours));
                        else if (startHours > 0)
                            startDate = startDate.plusHours((int) Math.abs(startHours));

                        if (startMinutes < 0)
                            startDate = startDate.minusMinutes((int) Math.abs(startMinutes));
                        else if (startMinutes > 0)
                            startDate = startDate.plusMinutes((int) Math.abs(startMinutes));
                    }
                }
                break;
            default:
                break;
        }
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    private DateTime getStartDateTimeForDayPeriod(DateHelper dh, Long interval) {
        DateTime returnTimeStamp = null;

        DateTime start = nowStartWithTime();
        DateTime end = nowEndWithTime();

        Long d = dh.getEndDate().getMillis() - dh.getStartDate().getMillis();

        if (d > 0) {
            List<DateTime> listTimeStamps = new ArrayList<>();
            listTimeStamps.add(start);
            DateTime currentDateTime = start.plus(d);
            while (currentDateTime.isBefore(end)) {
                if (currentDateTime.getSecondOfMinute() == 59) currentDateTime = currentDateTime.plusSeconds(1);
                listTimeStamps.add(currentDateTime);
                currentDateTime = currentDateTime.plus(d);
            }

            if (interval < 0) {
                Integer index = (int) Math.abs(interval);
                for (int i = listTimeStamps.size() - 1; i >= 0; i--) {
                    if (listTimeStamps.get(i).isBefore(DateTime.now())) return listTimeStamps.get(i - index);
                }
            } else if (interval > 0) {
                Integer index = (int) Math.abs(interval);
                for (int i = 0; i >= listTimeStamps.size() - 1; i++) {
                    if (listTimeStamps.get(i + index).isAfter(DateTime.now())) return listTimeStamps.get(i + index);
                }
            } else {
                for (int i = listTimeStamps.size() - 1; i >= 0; i--) {
                    if (listTimeStamps.get(i).isBefore(DateTime.now())) return listTimeStamps.get(i);
                }
            }
        }

        return returnTimeStamp;
    }

    private DateTime getEndDateTimeForDayPeriod(DateHelper dh, Long interval) {
        DateTime returnTimeStamp = null;

        DateTime start = nowStartWithTime();
        DateTime end = nowEndWithTime();

        Long d = dh.getEndDate().getMillis() - dh.getStartDate().getMillis();

        if (d > 0) {
            List<DateTime> listTimeStamps = new ArrayList<>();
            listTimeStamps.add(start);
            DateTime currentDateTime = start.plus(d);
            while (currentDateTime.isBefore(end)) {
                listTimeStamps.add(currentDateTime);
                currentDateTime = currentDateTime.plus(d);
            }

            if (interval < 0) {
                Integer index = (int) Math.abs(interval);
                for (int i = listTimeStamps.size() - 1; i >= 0; i--) {
                    if (listTimeStamps.get(i - index).isBefore(DateTime.now())) return listTimeStamps.get(i - index);
                }
            } else if (interval > 0) {
                Integer index = (int) Math.abs(interval);
                for (int i = 0; i >= listTimeStamps.size() - 1; i++) {
                    if (listTimeStamps.get(i + index).isAfter(DateTime.now())) return listTimeStamps.get(i + index);
                }
            } else {
                for (int i = listTimeStamps.size() - 1; i >= 0; i--) {
                    if (listTimeStamps.get(i).isBefore(DateTime.now())) return listTimeStamps.get(i);
                }
            }
        }

        return returnTimeStamp;
    }

    public void setType(TransformType type) {
        this.type = type;
    }

    public DateTime getEndDate() {
        //if (startTime.isAfter(endTime)) now = now.minusDays(1);

        switch (type) {
            case TODAY:
            case THISMONTH:
            case THISYEAR:
            case THISWEEK:
            case LAST30DAYS:
            case LAST7DAYS:
                now = DateTime.now();

                endDate = now;
                break;
            case YESTERDAY:
                now = DateTime.now();
                endDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                        endTime.getHour(), endTime.getMinute(), endTime.getSecond())
                        .minusDays(1);

                break;
            case LASTWEEK:
                now = DateTime.now();
                endDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                        endTime.getHour(), endTime.getMinute(), endTime.getSecond())
                        .minusDays(now.getDayOfWeek() - 1).minusWeeks(1)
                        .plusDays(6);
                break;
            case LASTMONTH:
                now = DateTime.now();
                endDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                        endTime.getHour(), endTime.getMinute(), endTime.getSecond())
                        .minusDays(now.getDayOfMonth() - 1)
                        .minusDays(1);
                break;
            case LASTYEAR:
                now = DateTime.now();
                endDate = new DateTime(now.getYear(), 1, 1,
                        endTime.getHour(), endTime.getMinute(), endTime.getSecond())
                        .minusDays(1);
                break;
            case CUSTOM_PERIOD:
                if (Objects.nonNull(customPeriodObject)) {
                    if (customPeriodObject.getEndReferencePoint() != null) {
                        Long endYears = 0L;
                        Long endMonths = 0L;
                        Long endWeeks = 0L;
                        Long endDays = 0L;
                        Long endHours = 0L;
                        Long endMinutes = 0L;
                        switch (customPeriodObject.getEndReferencePoint()) {
                            case "NOW":
                                endDate = DateTime.now();
                                break;
                            case "STARTTIMEDAY":
                                endDate = endDate.minusMillis(endDate.getMillisOfDay());
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
                                        DateTime newDT = getEndDateTimeForDayPeriod(dh, endInterval);

                                        endDate = new DateTime(newDT.getYear(), newDT.getMonthOfYear(), newDT.getDayOfMonth(),
                                                newDT.getHourOfDay(), newDT.getMinuteOfHour(), newDT.getSecondOfMinute());
                                    }
                                } catch (Exception e) {
                                }
                                break;
                            case "CURRENT_WEEK":
                                now = DateTime.now();
                                endDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                                        endTime.getHour(), endTime.getMinute(), endTime.getSecond());
                                endDate = endDate.minusDays(now.getDayOfWeek());
                                endDate = endDate.plusWeeks(1);
                                break;
                            case "CURRENT_MONTH":
                                now = DateTime.now();
                                endDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                                        endTime.getHour(), endTime.getMinute(), endTime.getSecond());
                                endDate = endDate.minusDays(now.getDayOfMonth() - 1);
                                endDate = endDate.plusMonths(1);
                                endDate = endDate.minusDays(1);
                                break;
                            case "CURRENT_DAY":
                                now = DateTime.now();
                                endDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(),
                                        endTime.getHour(), endTime.getMinute(), endTime.getSecond());
                                break;
                            case "SPECIFIED_DATE":
                                try {
                                    endYears = customPeriodObject.getEndYears();
                                    endMonths = customPeriodObject.getEndMonths();
                                    endDays = customPeriodObject.getEndDays();
                                    endHours = customPeriodObject.getEndHours();
                                    endMinutes = customPeriodObject.getEndMinutes();
                                    endDate = new DateTime(endYears.intValue(), endMonths.intValue(), endDays.intValue(),
                                            endHours.intValue(), endMinutes.intValue(), 59);
                                    return endDate;
                                } catch (Exception e) {
                                }
                                break;
                            default:
                                break;
                        }
                        endYears = customPeriodObject.getEndYears();
                        endMonths = customPeriodObject.getEndMonths();
                        endWeeks = customPeriodObject.getEndWeeks();
                        endDays = customPeriodObject.getEndDays();
                        endHours = customPeriodObject.getEndHours();
                        endMinutes = customPeriodObject.getEndMinutes();

                        if (endYears < 0)
                            endDate = endDate.minusYears((int) Math.abs(endYears));
                        else if (endYears > 0)
                            endDate = endDate.plusYears((int) Math.abs(endYears));

                        if (endMonths < 0)
                            endDate = endDate.minusMonths((int) Math.abs(endMonths));
                        else if (endMonths > 0)
                            endDate = endDate.plusMonths((int) Math.abs(endMonths));

                        if (endWeeks < 0)
                            endDate = endDate.minusWeeks((int) Math.abs(endWeeks));
                        else if (endWeeks > 0)
                            endDate = endDate.plusWeeks((int) Math.abs(endWeeks));

                        if (endDays < 0)
                            endDate = endDate.minusDays((int) Math.abs(endDays));
                        else if (endDays > 0)
                            endDate = endDate.plusDays((int) Math.abs(endDays));

                        if (endHours < 0)
                            endDate = endDate.minusHours((int) Math.abs(endHours));
                        else if (endHours > 0)
                            endDate = endDate.plusHours((int) Math.abs(endHours));

                        if (endMinutes < 0)
                            endDate = endDate.minusMinutes((int) Math.abs(endMinutes));
                        else if (endMinutes > 0)
                            endDate = endDate.plusMinutes((int) Math.abs(endMinutes));
                    }
                }
                break;
            default:
                break;
        }
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        DateTime start = getStartDate();
        return LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute());
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public enum TransformType {CUSTOM, TODAY, LAST7DAYS, LAST30DAYS, YESTERDAY, THISWEEK, LASTWEEK, THISMONTH, LASTMONTH, CUSTOM_PERIOD, THISYEAR, LASTYEAR}

    public enum InputType {STARTDATE, ENDDATE, STARTTIME, ENDTIME}

    public LocalTime getEndTime() {
        DateTime end = getEndDate();
        return LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute());
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
                    if (customPeriodObject != null) dh.setCustomPeriodObject(customPeriodObject);
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
                    if (customPeriodObject != null) dh.setCustomPeriodObject(customPeriodObject);
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

    public LocalDate getStartAsLocalDate() {
        DateTime start = getStartDate();
        return LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth());
    }

    public LocalDate getEndAsLocalDate() {
        DateTime end = getEndDate();
        return LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth());
    }

    public void setMinStartDateTime(DateTime minStartDateTime) {
        this.minStartDateTime = minStartDateTime;
    }

    public void setMaxEndDateTime(DateTime maxEndDateTime) {
        this.maxEndDateTime = maxEndDateTime;
    }

    public DateTime getMinStartDateTime() {
        return minStartDateTime;
    }

    public DateTime getMaxEndDateTime() {
        return maxEndDateTime;
    }

    public void setMinMaxForDateHelper(List<ChartDataModel> chartDataModels) {
        DateTime min = null;
        DateTime max = null;

        for (ChartDataModel chartDataModel : chartDataModels) {
            JEVisAttribute att = chartDataModel.getAttribute();
            if (att != null) {
                DateTime min_check = null;
                DateTime timestampFromFirstSample = att.getTimestampFromFirstSample();
                if (timestampFromFirstSample != null) {
                    min_check = new DateTime(
                            timestampFromFirstSample.getYear(),
                            timestampFromFirstSample.getMonthOfYear(),
                            timestampFromFirstSample.getDayOfMonth(),
                            timestampFromFirstSample.getHourOfDay(),
                            timestampFromFirstSample.getMinuteOfHour(),
                            timestampFromFirstSample.getSecondOfMinute());
                }

                DateTime timestampFromLastSample = att.getTimestampFromLastSample();
                DateTime max_check = null;
                if (timestampFromLastSample != null) {
                    max_check = new DateTime(
                            timestampFromLastSample.getYear(),
                            timestampFromLastSample.getMonthOfYear(),
                            timestampFromLastSample.getDayOfMonth(),
                            timestampFromLastSample.getHourOfDay(),
                            timestampFromLastSample.getMinuteOfHour(),
                            timestampFromLastSample.getSecondOfMinute());
                }

                if (min == null || (min_check != null && min_check.isBefore(min))) min = min_check;
                if (max == null || (max_check != null && max_check.isAfter(max))) max = max_check;
            }
        }

        if (min != null && max != null) {
            setMinStartDateTime(min);
            setMaxEndDateTime(max);
        }

    }
}
