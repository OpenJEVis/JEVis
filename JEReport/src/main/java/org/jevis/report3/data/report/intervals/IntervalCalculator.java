/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.intervals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.report.PeriodMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author broder
 */
public class IntervalCalculator {

    private static final Logger logger = LogManager.getLogger(IntervalCalculator.class);
    private final Map<String, ReportInterval> intervalMap = new ConcurrentHashMap<>();
    private final SampleHandler samplesHandler;
    private JEVisObject reportObject = null;
    private DateTime start;
    private DateTimeZone dateTimeZone;
    private String schedule;
    private WorkDays workDays;

    public IntervalCalculator(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }


    public ReportInterval getInterval(String period) {
        return intervalMap.get(period);
    }


    public JEVisObject getReportObject() {
        return reportObject;
    }


    public String getSchedule() {
        return schedule;
    }

    private void initializeIntervalMap(JEVisObject reportObject) {
        this.reportObject = reportObject;
        this.workDays = new WorkDays(reportObject);

        schedule = samplesHandler.getLastSample(reportObject, JC.Report.a_Schedule, Period.DAILY.toString());
        String startRecordString = samplesHandler.getLastSample(reportObject, JC.Report.a_StartRecord, "");
        dateTimeZone = samplesHandler.getLastSample(reportObject, JC.Report.a_TimeZone, DateTimeZone.UTC);

        start = DateTime.parse(startRecordString);
        DateTimeZone startZone = start.getZone();

        if (!startZone.equals(dateTimeZone)) {
            int dateTimeZoneOffset = dateTimeZone.getOffset(start);
            start = start.withZone(DateTimeZone.forOffsetMillis(dateTimeZoneOffset));
        }

        buildIntervals(schedule, start, false, Period.NONE);
    }

    public void buildIntervals(String scheduleString, DateTime start, boolean isOverride, Period originalPeriod) {
        intervalMap.clear();

        Period schedule = Period.valueOf(scheduleString.toUpperCase());

        org.jevis.commons.datetime.DateHelper dateHelper = null;

        dateHelper = PeriodHelper.getDateHelper(reportObject, schedule, dateHelper, start);
        if (dateHelper != null) {
            dateHelper.setWorkDays(workDays);
        }

        for (PeriodMode mode : PeriodMode.values()) {
            DateTime startRecord = calcStartRecord(start, schedule, mode, FixedPeriod.NONE, dateHelper);
            DateTime endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateTimeZone, dateHelper);

            ReportInterval interval = new ReportInterval(startRecord, endRecord);
            intervalMap.put(mode.toString().toUpperCase(), interval);
        }

        for (FixedPeriod fixedPeriod : FixedPeriod.values()) {
            DateTime startRecordFixed = calcStartRecord(start, schedule, PeriodMode.FIXED, fixedPeriod, dateHelper);
            DateTime startRecordFixedToReportEnd = calcStartRecord(start, schedule, PeriodMode.FIXED_TO_REPORT_END, fixedPeriod, dateHelper);
            DateTime endRecord = PeriodHelper.calcEndRecord(start, schedule, dateTimeZone, dateHelper);
            DateTime endRecordFixedOverride = PeriodHelper.calcEndRecord(startRecordFixedToReportEnd, originalPeriod, dateTimeZone, dateHelper);
            DateTime endRecordRelative = PeriodHelper.calcEndRecord(startRecordFixedToReportEnd, schedule, dateTimeZone, dateHelper);

            ReportInterval intervalFixed = new ReportInterval(startRecordFixed, endRecord);
            ReportInterval intervalFixedToReportEnd;
            if (!isOverride) {
                intervalFixedToReportEnd = new ReportInterval(startRecordFixedToReportEnd, endRecord);
            } else {
                intervalFixedToReportEnd = new ReportInterval(startRecordFixedToReportEnd, endRecordFixedOverride);
            }
            ReportInterval intervalRelative = new ReportInterval(startRecordFixedToReportEnd, endRecordRelative);

            String nameFixed = PeriodMode.FIXED + "_" + fixedPeriod.toString().toUpperCase();
            String nameFixedToReportEnd = PeriodMode.FIXED_TO_REPORT_END + "_" + fixedPeriod.toString().toUpperCase();
            String nameRelative = PeriodMode.RELATIVE + "_" + fixedPeriod.toString().toUpperCase();

            intervalMap.put(nameFixed, intervalFixed);
            intervalMap.put(nameFixedToReportEnd, intervalFixedToReportEnd);
            intervalMap.put(nameRelative, intervalRelative);
        }

        logger.info("Initialized Interval Map. Created {} entries", intervalMap.size());
    }

    private DateTime getEndForFixed(Period schedule, DateTime start) {
        switch (schedule) {
            case MINUTELY:
                return start.plusMinutes(1);
            case QUARTER_HOURLY:
                return start.plusMinutes(15);
            case HOURLY:
                return start.plusHours(1);
            case DAILY:
                return start.plusDays(1);
            case WEEKLY:
                return start.plusWeeks(1);
            case MONTHLY:
                return start.plusMonths(1);
            case QUARTERLY:
                return start.plusMonths(3);
            case YEARLY:
                return start.plusYears(1);
            case NONE:
            case CUSTOM:
            case CUSTOM2:
            default:
                return start;
        }
    }

    private DateTime calcStartRecord(DateTime startRecord, Period schedule, PeriodMode periodMode, FixedPeriod fixedPeriod, org.jevis.commons.datetime.DateHelper dateHelper) {
        DateTime resultStartRecord = startRecord;
        switch (periodMode) {
            case LAST:
                resultStartRecord = PeriodHelper.getPriorStartRecord(startRecord, schedule, dateHelper);
                break;
            case ALL:
                JEVisSample startRecordSample = samplesHandler.getFirstSample(reportObject, "Start Record");
                if (startRecordSample != null) {
                    try {
                        resultStartRecord = DateTime.parse(startRecordSample.getValueAsString());
                    } catch (JEVisException e) {
                        logger.error("Could not parse first sample of start record of object {}", reportObject, e);
                    }
                }

                break;
            case FIXED:
                switch (fixedPeriod) {
                    case QUARTER_HOUR:
                        resultStartRecord = startRecord.withSecondOfMinute(0).withMillisOfSecond(0);
                        break;
                    case HOUR:
                        resultStartRecord = startRecord.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        break;
                    case DAY:
                        resultStartRecord = startRecord.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        break;
                    case WEEK:
                        resultStartRecord = startRecord.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        break;
                    case MONTH:
                        resultStartRecord = startRecord.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        break;
                    case QUARTER:
                        if (startRecord.getMonthOfYear() <= 3) {
                            resultStartRecord = startRecord.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        } else if (startRecord.getMonthOfYear() <= 6) {
                            resultStartRecord = startRecord.withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        } else if (startRecord.getMonthOfYear() <= 9) {
                            resultStartRecord = startRecord.withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        } else if (startRecord.getMonthOfYear() <= 12) {
                            resultStartRecord = startRecord.withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        }
                        break;
                    case YEAR:
                        resultStartRecord = startRecord.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        break;
                    case THREEYEARS:
                        resultStartRecord = startRecord.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusYears(2);
                        break;
                    case FIVEYEARS:
                        resultStartRecord = startRecord.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusYears(4);
                        break;
                    case TENYEARS:
                        resultStartRecord = startRecord.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusYears(9);
                        break;
                    case NONE:
                    default:
                        break;
                }
                break;
            case RELATIVE:
            case FIXED_TO_REPORT_END:
                int weekOfWeekyear = startRecord.getWeekOfWeekyear();
                int dayOfWeek = startRecord.getDayOfWeek();
                int hourOfDay = startRecord.getHourOfDay();
                int minuteOfHour = startRecord.getMinuteOfHour();
                int secondOfMinute = startRecord.getSecondOfMinute();
                int millisOfSecond = startRecord.getMillisOfSecond();
                switch (fixedPeriod) {
                    case QUARTER_HOUR:
                        resultStartRecord = startRecord.minusMinutes(15);
                        break;
                    case HOUR:
                        resultStartRecord = startRecord.minusHours(1);
                        break;
                    case DAY:
                        resultStartRecord = startRecord.minusDays(1);
                        break;
                    case WEEK:
                        resultStartRecord = startRecord.minusWeeks(1);
                        break;
                    case MONTH:
                        resultStartRecord = startRecord.minusMonths(1);
                        break;
                    case QUARTER:
                        resultStartRecord = startRecord.minusMonths(3);
                        break;
                    case YEAR:
                        resultStartRecord = startRecord.minusYears(1);
                        if (schedule == Period.WEEKLY) {
                            resultStartRecord = resultStartRecord.withWeekOfWeekyear(weekOfWeekyear)
                                    .withDayOfWeek(dayOfWeek)
                                    .withHourOfDay(hourOfDay)
                                    .withMinuteOfHour(minuteOfHour)
                                    .withSecondOfMinute(secondOfMinute)
                                    .withMillisOfSecond(millisOfSecond);
                        }
                        break;
                    case THREEYEARS:
                        resultStartRecord = startRecord.minusYears(2);
                        if (schedule == Period.WEEKLY) {
                            resultStartRecord = resultStartRecord.withWeekOfWeekyear(weekOfWeekyear)
                                    .withDayOfWeek(dayOfWeek)
                                    .withHourOfDay(hourOfDay)
                                    .withMinuteOfHour(minuteOfHour)
                                    .withSecondOfMinute(secondOfMinute)
                                    .withMillisOfSecond(millisOfSecond);
                        }
                        break;
                    case FIVEYEARS:
                        resultStartRecord = startRecord.minusYears(4);
                        if (schedule == Period.WEEKLY) {
                            resultStartRecord = resultStartRecord.withWeekOfWeekyear(weekOfWeekyear)
                                    .withDayOfWeek(dayOfWeek)
                                    .withHourOfDay(hourOfDay)
                                    .withMinuteOfHour(minuteOfHour)
                                    .withSecondOfMinute(secondOfMinute)
                                    .withMillisOfSecond(millisOfSecond);
                        }
                        break;
                    case TENYEARS:
                        resultStartRecord = startRecord.minusYears(9);
                        if (schedule == Period.WEEKLY) {
                            resultStartRecord = resultStartRecord.withWeekOfWeekyear(weekOfWeekyear)
                                    .withDayOfWeek(dayOfWeek)
                                    .withHourOfDay(hourOfDay)
                                    .withMinuteOfHour(minuteOfHour)
                                    .withSecondOfMinute(secondOfMinute)
                                    .withMillisOfSecond(millisOfSecond);
                        }
                        break;
                    case NONE:
                    default:
                        break;
                }
                break;
            case CURRENT:
            default:
                break;
        }

        int dateTimeZoneOffset = dateTimeZone.getOffset(resultStartRecord);
        resultStartRecord = resultStartRecord.withZoneRetainFields(DateTimeZone.forOffsetMillis(dateTimeZoneOffset));

        return resultStartRecord;
    }

    public DateTime alignDateToSchedule(String scheduleString, DateTime dateTime) {
        DateTime resultDateTime = dateTime;
        switch (scheduleString) {
            case "MINUTELY":
                resultDateTime = dateTime.withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case "HOURLY":
                resultDateTime = dateTime.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case "DAILY":
                resultDateTime = dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case "WEEKLY":
                resultDateTime = dateTime.withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case "MONTHLY":
                resultDateTime = dateTime.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case "QUARTERLY":
                if (dateTime.getMonthOfYear() <= 3) {
                    resultDateTime = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (dateTime.getMonthOfYear() <= 6) {
                    resultDateTime = dateTime.withMonthOfYear(4).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (dateTime.getMonthOfYear() <= 9) {
                    resultDateTime = dateTime.withMonthOfYear(7).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (dateTime.getMonthOfYear() <= 12) {
                    resultDateTime = dateTime.withMonthOfYear(10).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
                break;
            case "YEARLY":
                resultDateTime = dateTime.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case "NONE":
            default:
                break;
        }

        return resultDateTime;
    }

    public void buildIntervals(JEVisObject reportObject) {
        initializeIntervalMap(reportObject);
    }

    public DateTime getStart() {
        return start;
    }
}
