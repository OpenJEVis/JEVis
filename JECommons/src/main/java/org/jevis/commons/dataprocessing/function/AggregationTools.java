package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class AggregationTools {
    private static final Logger logger = LogManager.getLogger(AggregationTools.class);
    private final DateTimeZone timeZone;
    private final WorkDays workDays;
    private final Period period;

    public AggregationTools(DateTimeZone timeZone, WorkDays workDays, AggregationPeriod aggregationPeriod) {
        this.timeZone = timeZone;
        this.workDays = workDays;
        this.period = AggregationPeriod.getJodaPeriod(aggregationPeriod);
    }

    public List<Interval> buildIntervals(DateTime from, DateTime to) {
        DateTime benchMarkStart = new DateTime();
        List<Interval> result = new ArrayList<>();

        DateTime startDate = from.withZone(timeZone);

        DateTime oldDate;

        while (startDate.isBefore(to)) {
            oldDate = startDate;
            startDate = PeriodHelper.getNextPeriod(startDate, period, 1, false, timeZone).withZone(timeZone);
            result.add(new Interval(oldDate, startDate));
        }

        DateTime benchMarkEnd = new DateTime();
        logger.info("Time to create Intervals[{}] in {} ms", result.size(), (benchMarkEnd.getMillis() - benchMarkStart.getMillis()));
        return result;
    }

    public Interval getInterval(WorkDays workDays, DateTime start, DateTime end, AggregationPeriod aggregationPeriod) {
        Interval resultInterval;
        DateTime newStart = start;
        DateTime newEnd = end;

        switch (aggregationPeriod) {
            default:
            case NONE:
                resultInterval = new Interval(start, end);
                break;
            case MINUTELY:
                newStart = start.withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusMinutes(1).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusMinutes(1);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case QUARTER_HOURLY:
                if (end.getMinuteOfHour() < 15) {
                    newEnd = end.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (end.getMinuteOfHour() < 30) {
                    newEnd = end.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (end.getMinuteOfHour() < 45) {
                    newEnd = end.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    newEnd = end.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }

                if (start.getMinuteOfHour() < 15) {
                    newStart = start.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (start.getMinuteOfHour() < 30) {
                    newStart = start.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (start.getMinuteOfHour() < 45) {
                    newStart = start.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    newStart = start.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                }

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusMinutes(15);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case HOURLY:
                newStart = start.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusHours(1);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case DAILY:
                newStart = start.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusDays(1);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case WEEKLY:
                newStart = start.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusWeeks(1).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusWeeks(1);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case MONTHLY:
                newStart = start.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusMonths(1);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case QUARTERLY:
                if (end.getMonthOfYear() < 4) {
                    newEnd = end.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (end.getMonthOfYear() < 7) {
                    newEnd = end.withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (end.getMonthOfYear() < 10) {
                    newEnd = end.withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    newEnd = end.withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }

                if (start.getMonthOfYear() < 4) {
                    newStart = start.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (start.getMonthOfYear() < 7) {
                    newStart = start.withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (start.getMonthOfYear() < 10) {
                    newStart = start.withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    newStart = start.withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusMonths(3);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case YEARLY:
                newStart = start.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusYears(1).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusYears(1);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case THREEYEARS:
                newStart = start.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusYears(3).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusYears(3);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case FIVEYEARS:
                newStart = start.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusYears(5).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusYears(5);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
            case TENYEARS:
                newStart = start.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                newEnd = end.plusYears(10).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                if (newEnd.equals(newStart) || newEnd.isBefore(newStart)) {
                    newEnd = newStart.plusYears(10);
                }

                resultInterval = new Interval(newStart, newEnd);
                break;
        }

        if (workDays.isEnabled() && workDays.isCustomWorkDay() && PeriodHelper.isGreaterThenDays(aggregationPeriod)) {
            java.time.LocalTime workdayStart = workDays.getWorkdayStart();
            newStart = newStart.withHourOfDay(workdayStart.getHour())
                    .withMinuteOfHour(workdayStart.getMinute())
                    .withSecondOfMinute(workdayStart.getSecond());

            java.time.LocalTime workdayEnd = workDays.getWorkdayEnd();
            newEnd = newEnd.withHourOfDay(workdayEnd.getHour())
                    .withMinuteOfHour(workdayEnd.getMinute())
                    .withSecondOfMinute(workdayEnd.getSecond());

            if (workdayEnd.isBefore(workdayStart)) {
                newStart = newStart.minusDays(1);
                newEnd = newEnd.minusDays(1);
            }

            resultInterval = new Interval(newStart, newEnd);
        }

        return resultInterval;
    }
}
