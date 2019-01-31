/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import org.jevis.report3.data.report.ReportProperty.ReportSchedule;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;

/**
 * @author broder
 */
public class DateHelper {

    public static double transformTimestampsToExcelTime(DateTime cal) {
        DateTime excelTime = new DateTime(1899, 12, 30, 0, 0, cal.getZone());
        double days = Days.daysBetween(excelTime, cal).getDays();
        double hourtmp = cal.getHourOfDay() * 60;
        double mintmp = cal.getMinuteOfHour();

        double d = (hourtmp + mintmp) / 1440;

        return days + d;
    }

    public static DateTime getNextPeriod(DateTime start, ReportSchedule schedule, int i) {
        DateTime resultDate = start;
        switch (schedule) {
            case DAILY:
                resultDate = resultDate.plusDays(i);
                break;
            case WEEKLY:
                resultDate = resultDate.plusWeeks(i);
                break;
            case MONTHLY:
                resultDate = resultDate.plusMonths(i);
                break;
            case YEARLY:
                resultDate = resultDate.plusYears(i);
                break;
        }
        return resultDate;
    }

    public static DateTime calcEndRecord(DateTime start, ReportSchedule schedule, org.jevis.commons.datetime.DateHelper dateHelper) {
        DateTime resultDate = start;
        switch (schedule) {
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
                Interval temp = new Interval(dateHelper.getStartDate(), dateHelper.getEndDate());
                resultDate = resultDate.plus(temp.toDurationMillis());
                break;
        }
        return resultDate;
    }

    public static DateTime getPriorStartRecord(DateTime startRecord, ReportSchedule schedule, org.jevis.commons.datetime.DateHelper dateHelper) {
        DateTime resultDate = startRecord;
        switch (schedule) {
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
                Interval temp = new Interval(dateHelper.getStartDate(), dateHelper.getEndDate());
                resultDate = resultDate.minus(temp.toDurationMillis());
                break;
        }
        return resultDate;
    }
}
