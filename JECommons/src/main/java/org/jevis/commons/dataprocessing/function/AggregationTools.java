package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.datetime.WorkDays;
import org.joda.time.*;

import java.util.ArrayList;
import java.util.List;

public class AggregationTools {
    private static final Logger logger = LogManager.getLogger(AggregationTools.class);
    private final DateTimeZone timeZone;
    private final WorkDays workDays;
    private Period period;

    public AggregationTools(DateTimeZone timeZone, WorkDays workDays) {
        this.timeZone = timeZone;
        this.workDays = workDays;
    }

    public List<Interval> getIntervals(DateTime from, DateTime to, AggregationPeriod aggregationPeriod) {
        Period period = Period.days(1);
        switch (aggregationPeriod) {
            default:
                break;
            case QUARTER_HOURLY:
                period = Period.minutes(15);
                from = from.withSecondOfMinute(0).withMillisOfSecond(0);
                to = to.plus(period);
                break;
            case HOURLY:
                period = Period.hours(1);
                from = from.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                to = to.plus(period);
                break;
            case DAILY:
                period = Period.days(1);
                from = from.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                to = to.plus(period);
                break;
            case WEEKLY:
                period = Period.weeks(1);
                from = from.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                to = to.plus(period);
                break;
            case MONTHLY:
                period = Period.months(1);
                from = from.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                to = to.plus(period);
                break;
            case QUARTERLY:
                period = Period.months(3);
                if (from.getMonthOfYear() < 4) {
                    from = from.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (from.getMonthOfYear() < 7) {
                    from = from.withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (from.getMonthOfYear() < 10) {
                    from = from.withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    from = from.withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
                to = to.plus(period);
                break;
            case YEARLY:
                period = Period.years(1);
                from = from.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                to = to.plus(period);
                break;
        }

        DateTime offset = new DateTime(1990, 1, 1, 0, 0, 0);

        return buildIntervals(period, offset, from, to);
    }

    private List<Interval> buildIntervals(Period period, DateTime offset, DateTime firstSample, DateTime lastSample) {
        DateTime benchMarkStart = new DateTime();
        List<Interval> result = new ArrayList<>();

        DateTime startDate = findFirstDuration(firstSample, period, offset);

        if (workDays != null && workDays.isEnabled()) {
            LocalTime start = new LocalTime(workDays.getWorkdayStart().getHour(), workDays.getWorkdayStart().getMinute(), workDays.getWorkdayStart().getSecond(), 0);
            startDate = startDate.withTime(start);
        }

        result.add(new Interval(startDate, period));

        boolean run = true;
        while (run) {
            startDate = startDate.plus(period);
            if (startDate.isAfter(lastSample)) {
                run = false;
            } else {
                result.add(new Interval(startDate, period));
            }
        }

        DateTime benchMarkEnd = new DateTime();
        logger.info("Time to create Intervals[{}] in {} ms", result.size(), (benchMarkEnd.getMillis() - benchMarkStart.getMillis()));
        return result;
    }

    private DateTime findFirstDuration(DateTime date, Period period, DateTime offset) {
        DateTime startD = new DateTime();
        DateTime firstPeriod = offset.withZone(timeZone);

        while (firstPeriod.isBefore(date) || firstPeriod.isEqual(date)) {
            firstPeriod = firstPeriod.plus(period);
        }
        firstPeriod = firstPeriod.minus(period);

        logger.info("finding date in: {} ms", ((new DateTime()).getMillis() - startD.getMillis()));
        logger.info("first offset date: offset: {} for period: {} input date: {} fistPeriod: {}", offset, period, date, firstPeriod);
        return firstPeriod;
    }
}
