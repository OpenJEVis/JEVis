package org.jevis.jecc.plugin.dashboard.timeframe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.datetime.CustomPeriodObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO: implement support for non one implementation for example 2 weeks
 */
public class TimeFrameFactory {

    public static final TimeFrame NONE = new TimeFrame() {
        @Override
        public String getListName() {
            return I18n.getInstance().getString("dialog.regression.type.none");
        }

        @Override
        public Interval nextPeriod(Interval interval, int addAmount) {
            return null;
        }

        @Override
        public Interval previousPeriod(Interval interval, int addAmount) {
            return null;
        }

        @Override
        public String format(Interval interval) {
            return null;
        }

        @Override
        public Interval getInterval(DateTime dateTime) {
            return null;
        }

        @Override
        public String getID() {
            return "NONE";
        }

        @Override
        public boolean hasNextPeriod(Interval interval) {
            return false;
        }

        @Override
        public boolean hasPreviousPeriod(Interval interval) {
            return false;
        }
    };
    private static final Logger logger = LogManager.getLogger(TimeFrameFactory.class);
    private final ObservableList<TimeFrame> list = FXCollections.observableArrayList();
    private JEVisDataSource ds;
    private WorkDays wd;

    public TimeFrameFactory(JEVisDataSource ds) {
        this.ds = ds;
    }

    public TimeFrame getTimeframe(String periode, String name) {
        LastPeriod lastPeriod = new LastPeriod(new Period(periode), name);
        return lastPeriod;
    }

    public List<TimeFrame> getReduced() {
        List<TimeFrame> reducedList = new ArrayList<>();
        reducedList.add(day());
        reducedList.add(week());
        reducedList.add(month());
        reducedList.add(year());
        reducedList.add(threeYears());
        reducedList.add(fiveYears());
        reducedList.add(tenYears());
        reducedList.add(custom());

        return reducedList;
    }

    public ObservableList<TimeFrame> getAll() {
        if (!list.isEmpty()) {
            return list;
        }

        //list.add(emptyTimeFrame());
        list.add(day());
        list.add(week());
        list.add(month());
        list.add(year());
        list.add(threeYears());
        list.add(fiveYears());
        list.add(tenYears());

        list.add(new LastPeriod(new Period("PT24H"), I18n.getInstance().getString("plugin.dashboard.timefactory.pt24h")));
        list.add(new LastPeriod(new Period("P7D"), I18n.getInstance().getString("plugin.dashboard.timefactory.p7d")));
        list.add(new LastPeriod(new Period("P30D"), I18n.getInstance().getString("plugin.dashboard.timefactory.p30d")));
        list.add(new LastPeriod(new Period("P365D"), I18n.getInstance().getString("plugin.dashboard.timefactory.p365d")));
        list.add(new LastPeriod(Period.ZERO, I18n.getInstance().getString("plugin.dashboard.timefactory.lastValue")));

        if (this.ds != null) {
            try {
                List<JEVisObject> listCustomPeriods = null;
                try {
                    listCustomPeriods = this.ds.getObjects(this.ds.getJEVisClass("Custom Period"), false);
                } catch (Exception e) {
                    logger.error("Error: could not get custom period", e);
                }

                List<CustomPeriodObject> listCustomPeriodObjects = null;
                if (listCustomPeriods != null) {
                    for (JEVisObject obj : listCustomPeriods) {
                        if (obj != null) {
                            if (listCustomPeriodObjects == null) listCustomPeriodObjects = new ArrayList<>();
                            CustomPeriodObject cpo = new CustomPeriodObject(obj, new ObjectHandler(this.ds));
                            if (cpo.isVisible()) {
                                listCustomPeriodObjects.add(cpo);
                            }
                        }
                    }
                }

                if (listCustomPeriodObjects != null) {
                    for (CustomPeriodObject cpo : listCustomPeriodObjects) {
                        list.add(customPeriodObject(cpo));
                    }
                }
            } catch (Exception ex) {
                logger.error("error while loading Custom TimeFactories: ", ex);
            }
        }


        return list;
    }


    public TimeFrame customPeriodObject(CustomPeriodObject cpo) {
        return new TimeFrame() {

            @Override
            public String getListName() {
                if (cpo != null && cpo.getObject() != null) {
                    return cpo.getObject().getName();
                }
                return "Unacceptable Period";

            }

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }

            @Override
            public String getID() {
                return cpo.getObject().getID().toString();
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
                if (wd != null) {
                    dateHelper.setWorkDays(wd);
                }

                return new Interval(dateHelper.getStartDate(), dateHelper.getEndDate());
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                /** Customer special case **/
                if (cpo.getEndReferencePoint().equals("NOW")) {
                    return false;
                }

                return interval.getEnd().isAfterNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                /** Customer special case **/
                return !cpo.getEndReferencePoint().equals("NOW");
            }

        };
    }

    public TimeFrame custom() {
        return new TimeFrame() {
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
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }
        };
    }

    public TimeFrame day() {
        return new TimeFrame() {

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }

            @Override
            public String toString() {
                return getID();
            }

            @Override
            public String getID() {
                return Period.days(1).toString();
//                return Period.hours(24).toString();
//                return TimeFrameType.DAY.toString();
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
//                System.out.println("Day: "
//                        + DateTimeFormat.forPattern("E, yyyy-MM-dd").print(interval.getEnd())
//                        + "  aus" + interval);
                return DateTimeFormat.forPattern("E, yyyy-MM-dd").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                return new Interval(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }


        };
    }

    private Interval removeWorkdayInterval(Interval interval) {

        if (wd != null && wd.getWorkdayEnd().isBefore(wd.getWorkdayStart())) {
            try {
                if (interval.toDurationMillis() > 86400000) {
                    interval = new Interval(interval.getStart().plusDays(1), interval.getEnd());
                }
            } catch (Exception e) {
            }
        }

        DateTime workStart = interval.getStart();

        workStart = workStart.withHourOfDay(0).withMinuteOfHour(0);
        DateTime workEnd = interval.getEnd().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
        return new Interval(workStart, workEnd);
    }

    public TimeFrame week() {
        return new TimeFrame() {
            @Override
            public String getID() {
                return Period.weeks(1).toString();
//                return Period.days(7).toString();
//                return TimeFrameType.WEEK.toString();
            }

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }


            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.graph.interval.weekly");
            }


            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.weeks(addAmount)).withDayOfWeek(1));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.weeks(addAmount)).withDayOfWeek(1));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("'KW' w").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = dateTime.withDayOfWeek(7).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);

                return applyWorkDay(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }

        };
    }

    private Interval applyWorkDay(DateTime start, DateTime end) {
        if (wd != null) {
            start = start.withHourOfDay(wd.getWorkdayStart().getHour()).withMinuteOfHour(wd.getWorkdayStart().getMinute()).withSecondOfMinute(wd.getWorkdayStart().getSecond());
            end = end.withHourOfDay(wd.getWorkdayEnd().getHour()).withMinuteOfHour(wd.getWorkdayEnd().getMinute()).withSecondOfMinute(wd.getWorkdayEnd().getSecond());
            if (wd.getWorkdayEnd().isBefore(wd.getWorkdayStart())) {
                start = start.minusDays(1);
            }
        }
        return new Interval(start, end);
    }

    public TimeFrame month() {
        return new TimeFrame() {
            @Override
            public String getID() {
                return Period.months(1).toString();
//                return Period.days(30).toString();
            }

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }


            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.graph.interval.monthly");
            }


            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.months(addAmount)).withDayOfMonth(1));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.months(addAmount)).withDayOfMonth(1));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("MMMM yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                int lastDayInMonth = dateTime.dayOfMonth().getMaximumValue();
                DateTime start = dateTime.withDayOfMonth(1)
                        .withHourOfDay(0)
                        .withMinuteOfHour(0)
                        .withSecondOfMinute(0)
                        .withMillisOfSecond(0);
                DateTime end = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), lastDayInMonth, 23, 59, 59, 999);

                return applyWorkDay(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }

        };
    }

    public TimeFrame year() {
        return new TimeFrame() {

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }

            @Override
            public String toString() {
                return getID();
            }

            @Override
            public String getID() {
                return Period.years(1).toString();
//                return Period.days(365).toString();
//                return TimeFrameType.YEAR.toString();
            }


            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.graph.interval.yearly");
            }


            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.years(addAmount)).withDayOfYear(1));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.years(addAmount)).withDayOfYear(1));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                DateTime end = new DateTime(dateTime.getYear(), 12, 31, 23, 59, 59, 999);

                return applyWorkDay(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }


        };
    }

    public TimeFrame threeYears() {
        return new TimeFrame() {

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }

            @Override
            public String toString() {
                return getID();
            }

            @Override
            public String getID() {
                return Period.years(3).toString();
            }


            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.object.report.dialog.period.last") + " "
                        + I18n.getInstance().getString("plugin.object.report.dialog.aggregation.threeyears");
            }


            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.years(addAmount)).withDayOfYear(1));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.years(addAmount)).withDayOfYear(1));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusYears(3);
                DateTime end = new DateTime(dateTime.getYear(), 12, 31, 23, 59, 59, 999).minusYears(1);

                return applyWorkDay(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }


        };
    }

    public TimeFrame fiveYears() {
        return new TimeFrame() {

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }

            @Override
            public String toString() {
                return getID();
            }

            @Override
            public String getID() {
                return Period.years(5).toString();
            }


            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.object.report.dialog.period.last") + " "
                        + I18n.getInstance().getString("plugin.object.report.dialog.aggregation.fiveyears");
            }


            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.years(addAmount)).withDayOfYear(1));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.years(addAmount)).withDayOfYear(1));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusYears(5);
                DateTime end = new DateTime(dateTime.getYear(), 12, 31, 23, 59, 59, 999).minusYears(1);

                return applyWorkDay(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }


        };
    }

    public TimeFrame tenYears() {
        return new TimeFrame() {

            @Override
            public boolean equals(Object obj) {
                return timeFrameEqual(obj);
            }

            @Override
            public String toString() {
                return getID();
            }

            @Override
            public String getID() {
                return Period.years(10).toString();
            }


            @Override
            public String getListName() {
                return I18n.getInstance().getString("plugin.object.report.dialog.period.last") + " "
                        + I18n.getInstance().getString("plugin.object.report.dialog.aggregation.tenyears");
            }


            @Override
            public Interval nextPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getEnd().plus(Period.years(addAmount)).withDayOfYear(1));
            }

            @Override
            public Interval previousPeriod(Interval interval, int addAmount) {
                Interval normalized = removeWorkdayInterval(interval);
                return getInterval(normalized.getStart().minus(Period.years(addAmount)).withDayOfYear(1));
            }


            @Override
            public String format(Interval interval) {
                return DateTimeFormat.forPattern("yyyy").print(interval.getEnd());
            }

            @Override
            public Interval getInterval(DateTime dateTime) {
                if (dateTime.isAfterNow()) {
                    dateTime = DateTime.now();
                }
                DateTime start = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusYears(5);
                DateTime end = new DateTime(dateTime.getYear(), 12, 31, 23, 59, 59, 999).minusYears(1);

                return applyWorkDay(start, end);
            }

            @Override
            public boolean hasNextPeriod(Interval interval) {
                return interval.getEnd().isBeforeNow();
            }

            @Override
            public boolean hasPreviousPeriod(Interval interval) {
                return true;
            }


        };
    }

    public ObservableList<TimeFrame> getAll(JEVisObject activeDashboard) {
        this.wd = new WorkDays(activeDashboard);
        return getAll();
    }

    public void setDs(JEVisDataSource ds) {
        this.ds = ds;
    }

    public void setActiveDashboard(JEVisObject activeDashboard) {
        this.wd = new WorkDays(activeDashboard);
    }

    public enum TimeFrameType {
        DAY, WEEK, MONTH, YEAR, CUSTOM
    }
}
