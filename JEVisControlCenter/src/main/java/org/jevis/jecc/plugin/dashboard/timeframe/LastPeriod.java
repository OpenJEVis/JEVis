package org.jevis.jecc.plugin.dashboard.timeframe;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

/**
 * Calculates an dynamic period
 */
public class LastPeriod implements TimeFrame {

    private final Period period;
    private final String listName;

    public LastPeriod(Period period) {
        this.period = period;
        this.listName = "";
    }

    public LastPeriod(Period period, String listName) {
        this.period = period;
        this.listName = listName;
    }


    @Override
    public String getID() {
        return this.period.toString();
    }

    @Override
    public String getListName() {
        return (this.listName.isEmpty() == true)
                ? this.period.toString()
                : this.listName;
    }

    @Override
    public Interval nextPeriod(Interval interval, int addAmount) {
//        Interval normalized = removeWorkdayInterval(interval);
//        return getInterval(normalized.getEnd().plus(Period.days(addAmount)));
        return null;
    }

    @Override
    public Interval previousPeriod(Interval interval, int addAmount) {
//        Interval normalized = removeWorkdayInterval(interval);
//        return getInterval(normalized.getStart().minus(Period.days(addAmount)));
        return null;
    }

    @Override
    public String format(Interval interval) {
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getStart()) + " / " + DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getEnd());

    }

    @Override
    public Interval getInterval(DateTime dateTime) {
        if (dateTime.isAfterNow()) {
            dateTime = DateTime.now();
        }
        DateTime end = dateTime;
//        DateTime start = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime start = dateTime.minus(this.period);

//        DateTime end = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
        return new Interval(start, end);
    }

    @Override
    public boolean hasNextPeriod(Interval interval) {
        return interval.getEnd().isAfterNow();
    }

    @Override
    public boolean hasPreviousPeriod(Interval interval) {
        return true;
    }

}
