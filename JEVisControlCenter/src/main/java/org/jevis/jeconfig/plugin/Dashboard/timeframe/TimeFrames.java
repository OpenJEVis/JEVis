package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

public class TimeFrames {

    public ObservableList<TimeFrameFactory> getAll() {
        ObservableList<TimeFrameFactory> list = FXCollections.observableArrayList();

        list.add(day());
        list.add(week());
        list.add(month());
        list.add(year());

        return list;
    }

    public TimeFrameFactory custom() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.CUSTOM.toString();
            }

            @Override
            public String getListName() {
                return "Individuell";
            }

            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                return interval;
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                return interval;
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy-MM-dd mm:ss").print(interval.getStart()) + " / " + DateTimeFormat.forPattern("yyyy-MM-dd mm:ss").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }
        };
    }

    public TimeFrameFactory day() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.DAY.toString();
            }

            @Override
            public String getListName() {
                return "Tag";
            }

            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getEnd().plus(Period.days(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getStart().minus(Period.days(addAmount)));
            }

            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("E, yyyy-MM-dd").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }
        };
    }

    public TimeFrameFactory week() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.WEEK.toString();
            }

            @Override
            public String getListName() {
                return "Woche";
            }

            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getEnd().plus(Period.weeks(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getStart().minus(Period.weeks(addAmount)));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("'KW' w").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withDayOfWeek(7).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }
        };
    }

    public TimeFrameFactory month() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.YEAR.toString();
            }

            @Override
            public String getListName() {
                return "Monat";
            }

            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getEnd().plus(Period.months(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getStart().minus(Period.months(addAmount)));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("MMMM yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withDayOfMonth(1).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withDayOfMonth(dateTime.dayOfMonth().getMaximumValue()).withDayOfWeek(7).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }
        };
    }

    public TimeFrameFactory year() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.YEAR.toString();
            }

            @Override
            public String getListName() {
                return "Jahr";
            }

            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getEnd().plus(Period.years(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                return getInterval(interval.getStart().minus(Period.years(addAmount)));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withMonthOfYear(12).withDayOfMonth(dateTime.dayOfMonth().getMaximumValue()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }
        };
    }

    public enum TimeFrameType {
        DAY, WEEK, MONTH, YEAR, CUSTOM
    }


}
