/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.datetime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.stream.IntStream;

/**
 * @author broder
 */
public class PeriodHelper {
    private static final Logger logger = LogManager.getLogger(PeriodHelper.class);
    private static final String CUSTOM_SCHEDULE_OBJECT_ATTRIBUTE = "Custom Schedule Object";
    public static String STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static double transformTimestampsToExcelTime(DateTime cal) {
        if (cal != null) {
            DateTime excelTime = new DateTime(1899, 12, 30, 0, 0, cal.getZone());
            double days = Days.daysBetween(excelTime, cal).getDays();
            double hourtmp = cal.getHourOfDay() * 60;
            double mintmp = cal.getMinuteOfHour();

            double d = (hourtmp + mintmp) / 1440;

            return days + d;
        } else return 0;
    }

    public static DateTime addPeriodToDate(DateTime currentDate, org.joda.time.Period cleanPeriod) {
        DateTime resultingDate = currentDate;

        resultingDate = resultingDate.plusYears(cleanPeriod.getYears());
        resultingDate = resultingDate.plusMonths(cleanPeriod.getMonths());
        resultingDate = resultingDate.plusWeeks(cleanPeriod.getWeeks());
        resultingDate = resultingDate.plusDays(cleanPeriod.getDays());
        resultingDate = resultingDate.plusHours(cleanPeriod.getHours());
        resultingDate = resultingDate.plusMinutes(cleanPeriod.getMinutes());
        resultingDate = resultingDate.plusSeconds(cleanPeriod.getSeconds());
        resultingDate = resultingDate.plusMillis(cleanPeriod.getMillis());

        return resultingDate;
    }

    public static DateTime minusPeriodToDate(DateTime currentDate, org.joda.time.Period cleanPeriod) {
        DateTime resultingDate = currentDate;

        resultingDate = resultingDate.minusYears(cleanPeriod.getYears());
        resultingDate = resultingDate.minusMonths(cleanPeriod.getMonths());
        resultingDate = resultingDate.minusWeeks(cleanPeriod.getWeeks());
        resultingDate = resultingDate.minusDays(cleanPeriod.getDays());
        resultingDate = resultingDate.minusHours(cleanPeriod.getHours());
        resultingDate = resultingDate.minusMinutes(cleanPeriod.getMinutes());
        resultingDate = resultingDate.minusSeconds(cleanPeriod.getSeconds());
        resultingDate = resultingDate.minusMillis(cleanPeriod.getMillis());

        return resultingDate;
    }

    public static DateTime getNextPeriod(DateTime start, Period schedule, int i, org.joda.time.Period customPeriod) {
        DateTime resultDate = start;
        boolean wasLastDay = start.getDayOfMonth() == start.dayOfMonth().getMaximumValue();
        switch (schedule) {
            case MINUTELY:
                resultDate = resultDate.plusMinutes(i);
                break;
            case QUARTER_HOURLY:
                resultDate = resultDate.plusMinutes(15 * i);
                break;
            case HOURLY:
                resultDate = resultDate.plusHours(i);
                break;
            case DAILY:
                resultDate = resultDate.plusDays(i);
                break;
            case WEEKLY:
                resultDate = resultDate.plusWeeks(i);
                break;
            case MONTHLY:
                resultDate = resultDate.plusMonths(i);
                if (wasLastDay) {
                    resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
                }
                break;
            case QUARTERLY:
                resultDate = resultDate.plusMonths(3 * i);
                if (wasLastDay) {
                    resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
                }
                break;
            case YEARLY:
                resultDate = resultDate.plusYears(i);
                if (wasLastDay) {
                    resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
                }
                break;
            case CUSTOM:
            case CUSTOM2:
                resultDate = resultDate.plus(customPeriod);
                if (org.joda.time.Period.months(1).equals(customPeriod) && wasLastDay) {
                    resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
                }
                break;
        }
        return resultDate;
    }

    public static DateTime getNextPeriod(DateTime start, org.joda.time.Period period, int count, boolean aligned, DateTimeZone timeZone) {
        DateTime resultDate = start.withZone(timeZone);
        boolean wasLastDay = resultDate.getDayOfMonth() == resultDate.dayOfMonth().getMaximumValue();
        if (org.joda.time.Period.minutes(1).equals(period)) {
            resultDate = resultDate.plusMinutes(count);
            if (aligned) {
                resultDate = resultDate.withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.minutes(15).equals(period)) {
            resultDate = resultDate.plusMinutes(15 * count);
            if (aligned) {
                resultDate = resultDate.withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.hours(1).equals(period)) {
            resultDate = resultDate.plusHours(count);
            if (aligned) {
                resultDate = resultDate.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.days(1).equals(period)) {
            resultDate = resultDate.plusDays(count);
            if (aligned) {
                resultDate = resultDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.weeks(1).equals(period)) {
            resultDate = resultDate.plusWeeks(count);
            if (aligned) {
                resultDate = resultDate.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.months(1).equals(period)) {
            resultDate = resultDate.plusMonths(count);
            if (wasLastDay) {
                resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
            } else if (aligned) {
                resultDate = resultDate.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.months(3).equals(period)) {
            resultDate = resultDate.plusMonths(3 * count);
            if (wasLastDay) {
                resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
            } else if (aligned) {
                resultDate = resultDate.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (org.joda.time.Period.years(1).equals(period)) {
            resultDate = resultDate.plusYears(count);
            if (wasLastDay) {
                resultDate = resultDate.withDayOfMonth(resultDate.dayOfMonth().getMaximumValue());
            } else if (aligned) {
                resultDate = resultDate.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        }
        return resultDate.withZone(DateTimeZone.UTC);
    }

    public static DateTime calcEndRecord(DateTime start, Period schedule, org.jevis.commons.datetime.DateHelper dateHelper) {
        DateTime resultDate = start;
        switch (schedule) {
            case MINUTELY:
                resultDate = resultDate.plusMinutes(1).minusMillis(1);
                break;
            case QUARTER_HOURLY:
                resultDate = resultDate.plusMinutes(15).minusMillis(1);
                break;
            case HOURLY:
                resultDate = resultDate.plusHours(1).minusMillis(1);
                break;
            case DAILY:
                resultDate = resultDate.plusDays(1).minusMillis(1);
                break;
            case WEEKLY:
                resultDate = resultDate.plusWeeks(1).minusMillis(1);
                break;
            case MONTHLY:
                resultDate = resultDate.plusMonths(1).minusMillis(1);
                break;
            case QUARTERLY:
                resultDate = resultDate.plusMonths(3).minusMillis(1);
                break;
            case YEARLY:
                resultDate = resultDate.plusYears(1).minusMillis(1);
                break;
            case CUSTOM:
            case CUSTOM2:
                Interval temp = new Interval(dateHelper.getStartDate(), dateHelper.getEndDate());
                resultDate = resultDate.plus(temp.toDurationMillis()).minusMillis(1);
                break;
        }
        return resultDate;
    }

    public static DateTime getPriorStartRecord(DateTime startRecord, Period schedule, org.jevis.commons.datetime.DateHelper dateHelper) {
        DateTime resultDate = startRecord;
        switch (schedule) {
            case MINUTELY:
                resultDate = resultDate.minusMinutes(1);
                break;
            case QUARTER_HOURLY:
                resultDate = resultDate.minusMinutes(15);
                break;
            case HOURLY:
                resultDate = resultDate.minusHours(1);
                break;
            case DAILY:
                resultDate = resultDate.minusDays(1);
                break;
            case WEEKLY:
                resultDate = resultDate.minusWeeks(1);
                break;
            case MONTHLY:
                resultDate = resultDate.minusMonths(1);
                break;
            case QUARTERLY:
                resultDate = resultDate.minusMonths(3);
                break;
            case YEARLY:
                resultDate = resultDate.minusYears(1);
                break;
            case CUSTOM:
            case CUSTOM2:
                Interval temp = new Interval(dateHelper.getStartDate(), dateHelper.getEndDate());
                resultDate = resultDate.minus(temp.toDurationMillis());
                break;
        }
        return resultDate;
    }

    public static DateHelper getDateHelper(JEVisObject objectWithCustomScheduleAttribute, Period schedule, DateHelper dateHelper, DateTime start) {
        if (schedule.equals(Period.CUSTOM) || schedule.equals(Period.CUSTOM2)) {
            dateHelper = new DateHelper();
            dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
            dateHelper.setStartDate(start);
            dateHelper.setEndDate(start);
            CustomPeriodObject cpo = null;
            try {
                String targetString = objectWithCustomScheduleAttribute.getAttribute(CUSTOM_SCHEDULE_OBJECT_ATTRIBUTE).getLatestSample().getValueAsString();
                TargetHelper th = new TargetHelper(objectWithCustomScheduleAttribute.getDataSource(), targetString);

                if (th.targetObjectAccessible())
                    cpo = new CustomPeriodObject(th.getObject().get(0), new ObjectHandler(objectWithCustomScheduleAttribute.getDataSource()));
            } catch (JEVisException e) {
                logger.error("Could not get Target Object.");
            }
            dateHelper.setCustomPeriodObject(cpo);
        }
        return dateHelper;
    }

    public static int getLastSunday(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.add(Calendar.DATE, -1);
        cal.add(Calendar.DAY_OF_MONTH, -(cal.get(Calendar.DAY_OF_WEEK) - 1));
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isGreaterThenDays(org.joda.time.Period period) {
        return IntStream.of(period.getYears(), period.getMonths(), period.getWeeks(), period.getDays()).anyMatch(i -> i > 0);
    }

    public static boolean isGreaterThenDays(AggregationPeriod aggregationPeriod) {
        switch (aggregationPeriod) {
            default:
            case NONE:
            case MINUTELY:
            case QUARTER_HOURLY:
            case HOURLY:
                return false;
            case DAILY:
            case WEEKLY:
            case MONTHLY:
            case QUARTERLY:
            case YEARLY:
            case THREEYEARS:
            case FIVEYEARS:
            case TENYEARS:
                return true;
        }
    }

    public static String getFormatString(org.joda.time.Period period, boolean isCounter) {
        String normalPattern = DateTimeFormat.patternForStyle("SS", I18n.getInstance().getLocale());

        if (period != null) {
            try {
                if (period.equals(org.joda.time.Period.days(1))) {
                    normalPattern = "dd. MMM (EEE)";
                } else if (period.equals(org.joda.time.Period.weeks(1))) {
                    normalPattern = "'KW'w yyyy";
                } else if (period.equals(org.joda.time.Period.months(1)) && !isCounter) {
                    normalPattern = "MMMM yyyy";
                } else if (period.equals(org.joda.time.Period.months(1)) && isCounter) {
                    normalPattern = "dd. MMMM yyyy";
                } else if (period.equals(org.joda.time.Period.years(1)) && !isCounter) {
                    normalPattern = "yyyy";
                } else if (period.equals(org.joda.time.Period.years(1)) && isCounter) {
                    normalPattern = "dd. MMMM yyyy";
                } else {
                    normalPattern = "yyyy-MM-dd HH:mm:ss";
                }
            } catch (Exception e) {
                logger.error("Could not determine sample rate, fall back to standard", e);
            }
        }
        return normalPattern;
    }

    public static DateTime alignDateToPeriod(DateTime firstDate, org.joda.time.Period maxPeriod, JEVisObject object) {
        DateTime alignedDate = null;
        DateTime lowerTS = null;
        DateTime higherTS = null;
        boolean isGreaterThenDays = false;
        WorkDays workDays = new WorkDays(object);

        if (maxPeriod.equals(org.joda.time.Period.minutes(1))) {
            lowerTS = firstDate.minusSeconds(30).withSecondOfMinute(0).withMillisOfSecond(0);
            higherTS = firstDate.plusSeconds(30).withSecondOfMinute(0).withMillisOfSecond(0);
        } else if (maxPeriod.getMinutes() == 5) {
            if (firstDate.getMinuteOfHour() == 0) {
                lowerTS = firstDate.minusMinutes(5).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 5) {
                lowerTS = firstDate.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(5).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 10) {
                lowerTS = firstDate.withMinuteOfHour(5).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(10).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 15) {
                lowerTS = firstDate.withMinuteOfHour(10).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 20) {
                lowerTS = firstDate.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(20).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 25) {
                lowerTS = firstDate.withMinuteOfHour(20).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(25).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 30) {
                lowerTS = firstDate.withMinuteOfHour(25).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 35) {
                lowerTS = firstDate.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(35).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 40) {
                lowerTS = firstDate.withMinuteOfHour(35).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(40).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 45) {
                lowerTS = firstDate.withMinuteOfHour(40).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 50) {
                lowerTS = firstDate.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(50).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 55) {
                lowerTS = firstDate.withMinuteOfHour(50).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(55).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 60) {
                lowerTS = firstDate.withMinuteOfHour(55).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (maxPeriod.getMinutes() == 15) {
            if (firstDate.getMinuteOfHour() == 0) {
                lowerTS = firstDate.minusMinutes(15).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 15) {
                lowerTS = firstDate.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 30) {
                lowerTS = firstDate.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 45) {
                lowerTS = firstDate.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
            } else {
                lowerTS = firstDate.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (maxPeriod.getMinutes() == 30) {
            if (firstDate.getMinuteOfHour() == 0) {
                lowerTS = firstDate.minusMinutes(30).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMinuteOfHour() < 30) {
                lowerTS = firstDate.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
            } else {
                lowerTS = firstDate.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (maxPeriod.getHours() == 1) {
            lowerTS = firstDate.minusMinutes(30).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            higherTS = firstDate.plusMinutes(30).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        } else if (maxPeriod.getDays() == 1) {
            lowerTS = firstDate.minusHours(12).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            higherTS = firstDate.plusHours(12).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            isGreaterThenDays = true;
        } else if (maxPeriod.getWeeks() == 1) {
            lowerTS = firstDate.minusHours(84).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            higherTS = firstDate.plusHours(84).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            isGreaterThenDays = true;
        } else if (maxPeriod.getMonths() == 1) {
            lowerTS = firstDate.minusHours(363).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            higherTS = firstDate.plusHours(363).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            isGreaterThenDays = true;
        } else if (maxPeriod.getMonths() == 3) {
            isGreaterThenDays = true;
            if (firstDate.getMonthOfYear() <= 3) {
                lowerTS = firstDate.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMonthOfYear(3).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMonthOfYear() <= 6) {
                lowerTS = firstDate.withMonthOfYear(3).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMonthOfYear(6).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMonthOfYear() <= 9) {
                lowerTS = firstDate.withMonthOfYear(6).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMonthOfYear(9).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (firstDate.getMonthOfYear() <= 12) {
                lowerTS = firstDate.withMonthOfYear(9).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = firstDate.withMonthOfYear(12).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else if (maxPeriod.getYears() == 1) {
            isGreaterThenDays = true;
            lowerTS = firstDate.minusDays(182).minusHours(15).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            higherTS = firstDate.plusDays(182).plusHours(15).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        }

        if (isGreaterThenDays && lowerTS != null) {
            LocalTime workdayStart = workDays.getWorkdayStart(firstDate);
            lowerTS = lowerTS.withHourOfDay(workdayStart.getHour())
                    .withMinuteOfHour(workdayStart.getMinute())
                    .withSecondOfMinute(workdayStart.getSecond());
            higherTS = higherTS.withHourOfDay(workdayStart.getHour())
                    .withMinuteOfHour(workdayStart.getMinute())
                    .withSecondOfMinute(workdayStart.getSecond());

            if (workDays.getWorkdayEnd(firstDate).isBefore(workdayStart)) {
                lowerTS = lowerTS.minusDays(1);
                higherTS = higherTS.minusDays(1);
            }
        }

        if (lowerTS != null && higherTS != null) {
            long lowerDiff = firstDate.getMillis() - lowerTS.getMillis();
            long higherDiff = higherTS.getMillis() - firstDate.getMillis();

            if (lowerDiff < higherDiff && !lowerTS.equals(firstDate)) {
                alignedDate = lowerTS;

            } else if (higherDiff < lowerDiff && !higherTS.equals(firstDate)) {
                alignedDate = higherTS;

            } else {
                alignedDate = firstDate;
            }
        } else {
            alignedDate = firstDate;
        }

        return alignedDate;
    }
}
