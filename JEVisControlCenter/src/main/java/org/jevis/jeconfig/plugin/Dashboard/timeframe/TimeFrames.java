package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

public class TimeFrames {

    private static final Logger logger = LogManager.getLogger(TimeFrames.class);
    private JEVisDataSource ds;
    private WorkDays workDays = new WorkDays(null);

    public TimeFrames(JEVisDataSource ds) {
        this.ds = ds;
    }

    public TimeFrames() {
    }

    public ObservableList<TimeFrameFactory> getAll() {
        ObservableList<TimeFrameFactory> list = FXCollections.observableArrayList();

        list.add(day());
        list.add(week());
        list.add(month());
        list.add(year());

        List<JEVisObject> listCustomPeriods = null;
        try {
            listCustomPeriods = ds.getObjects(ds.getJEVisClass("Custom Period"), false);
        } catch (JEVisException e) {
            logger.error("Error: could not get custom period", e);
        }

        List<CustomPeriodObject> listCustomPeriodObjects = null;
        if (listCustomPeriods != null) {
            for (JEVisObject obj : listCustomPeriods) {
                if (obj != null) {
                    if (listCustomPeriodObjects == null) listCustomPeriodObjects = new ArrayList<>();
                    CustomPeriodObject cpo = new CustomPeriodObject(obj, new ObjectHandler(ds));
                    if (cpo.isVisible()) {
                        listCustomPeriodObjects.add(cpo);
                    }
                }
            }
        }

        if (listCustomPeriodObjects != null) {
            for (CustomPeriodObject cpo : listCustomPeriodObjects) {
                list.add(cpos(cpo));
            }
        }

        return list;
    }

    private TimeFrameFactory cpos(CustomPeriodObject cpo) {
        return new TimeFrameFactory() {
            @Override
            public String getListName() {
                return cpo.getObject().getName();
            }

            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Long endMillis = null;
                if (interval.getEnd().getSecondOfMinute() == 59) {
                    endMillis = (interval.getEnd().plusSeconds(1)).getMillis();
                } else {
                    endMillis = interval.getEnd().getMillis();
                }
                long l = endMillis - interval.getStart().getMillis();
                return new Interval(interval.getStart().plus(l), interval.getEnd().plus(l));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Long endMillis = null;
                if (interval.getEnd().getSecondOfMinute() == 59) {
                    endMillis = (interval.getEnd().plusSeconds(1)).getMillis();
                } else {
                    endMillis = interval.getEnd().getMillis();
                }
                long l = endMillis - interval.getStart().getMillis();
                return new Interval(interval.getStart().minus(l), interval.getEnd().minus(l));
            }

            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getStart()) + " / " + DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {

                DateHelper dateHelper = new DateHelper();
                dateHelper.setCustomPeriodObject(cpo);
                dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);

                dateHelper.setStartTime(workDays.getWorkdayStart());
                dateHelper.setEndTime(workDays.getWorkdayEnd());

                AnalysisTimeFrame newTimeFrame = new AnalysisTimeFrame();
                newTimeFrame.setTimeFrame(TimeFrame.CUSTOM_START_END);
                newTimeFrame.setId(cpo.getObject().getID());
                newTimeFrame.setStart(dateHelper.getStartDate());
                newTimeFrame.setEnd(dateHelper.getEndDate());


                return new Interval(newTimeFrame.getStart(), newTimeFrame.getEnd());
            }

            @Override
            public String getID() {
                return cpo.getObject().getID().toString();
            }
        };
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
                return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getStart()) + " / " + DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(interval.getEnd());
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
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.days(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.days(addAmount)));
            }

            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("E, yyyy-MM-dd").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return getWorkdayInterval(start, end);
            }
        };
    }

    private Interval removeWorkdayInterval(Interval interval) {
        DateTime workStart = interval.getStart();
        if (workDays.getWorkdayStart().isAfter(workDays.getWorkdayEnd())) {
            workStart = workStart.plusDays(1);
        }

        workStart = workStart.withHourOfDay(0).withMinuteOfHour(0);
        DateTime workEnd = interval.getEnd().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
        return new Interval(workStart, workEnd);
    }

    private Interval getWorkdayInterval(DateTime start, DateTime end) {

        DateTime workStart = start;
        if (workDays.getWorkdayStart().isAfter(workDays.getWorkdayEnd())) {
            workStart = start.minusDays(1);
        }
        workStart = workStart.withHourOfDay(workDays.getWorkdayStart().getHour()).withMinuteOfHour(workDays.getWorkdayStart().getMinute());
        DateTime workEnd = end.withHourOfDay(workDays.getWorkdayEnd().getHour()).withMinuteOfHour(workDays.getWorkdayEnd().getMinute());

//        DateTime workEnd = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), workDays.getWorkdayEnd().getHour(), workDays.getWorkdayEnd().getMinute(), 59, 999);

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
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.weeks(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.weeks(addAmount)));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("'KW' w").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                DateTime start = dateTime.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withDayOfWeek(7).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
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
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.months(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.months(addAmount)));
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
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.years(addAmount)));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.years(addAmount)));
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

    public void setDs(JEVisDataSource ds) {
        this.ds = ds;
    }
}
