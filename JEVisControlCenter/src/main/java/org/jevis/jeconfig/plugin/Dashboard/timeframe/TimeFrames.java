package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisObject;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

public class TimeFrames {

    private WorkDays workDays = new WorkDays(null);
    final String keyPreset = I18n.getInstance().getString("plugin.graph.interval.preset");
    final String keyHourly = I18n.getInstance().getString("plugin.graph.interval.hourly");
    final String keyQuarterly = I18n.getInstance().getString("plugin.graph.interval.quarterly");
    final String keyYearly = I18n.getInstance().getString("plugin.graph.interval.yearly");

    public ObservableList<TimeFrameFactory> getAll() {
        ObservableList<TimeFrameFactory> list = FXCollections.observableArrayList();

        list.add(day());
        list.add(week());
        list.add(month());
        list.add(year());

        return list;
    }

    /**
     * Enable workdays support by given an object (Clean/Raw) form which we get the corresponding
     * building and its workday settings.
     */
    public void setWorkdays(JEVisObject object) {
        workDays = new WorkDays(object);
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
                return I18n.getInstance().getString("plugin.graph.interval.daily");
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
//                return new Interval(start, end);
                return getWorkdayInterval(start, end);
            }
        };
    }

    private Interval getWorkdayInterval(DateTime start, DateTime end) {

        DateTime workStart = start;
        if (workDays.getWorkdayStart().isAfter(workDays.getWorkdayEnd())) {
            workStart = start.minusDays(1);
        }
        workStart = workStart.withHourOfDay(workDays.getWorkdayStart().getHour()).withMinuteOfHour(workDays.getWorkdayStart().getMinute());
        DateTime workEnd = end.withHourOfDay(workDays.getWorkdayEnd().getHour()).withMinuteOfHour(workDays.getWorkdayEnd().getMinute());
        return new Interval(workStart, workEnd);

    }

    public TimeFrameFactory week() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.WEEK.toString();
            }

            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.graph.interval.weekly");
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
                //                return new Interval(start, end);
                return getWorkdayInterval(start, end);
            }
        };
    }

    public TimeFrameFactory month() {
        return new TimeFrameFactory() {
            @Override
            public String getID() {
                return TimeFrameType.MONTH.toString();
            }

            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.graph.interval.monthly");
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
                int lastDayInMonth = dateTime.dayOfMonth().getMaximumValue();
                DateTime start = dateTime.withDayOfMonth(1)
//                        .withDayOfWeek(1)
                        .withHourOfDay(0)
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0)
                        .withMillisOfSecond(0);
                DateTime end = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), lastDayInMonth, 23, 59, 59, 999);

                //                return new Interval(start, end);
                return getWorkdayInterval(start, end);
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
                return I18n.getInstance().getString("plugin.graph.interval.yearly");
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
//                DateTime end = dateTime.withMonthOfYear(12).withDayOfMonth(dateTime.dayOfMonth().getMaximumValue()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                DateTime end = new DateTime(dateTime.getYear(), 12, 31, 23, 59, 59, 999);

                //                return new Interval(start, end);
                return getWorkdayInterval(start, end);
            }
        };
    }

    public enum TimeFrameType {
        DAY, WEEK, MONTH, YEAR, CUSTOM
    }


}
