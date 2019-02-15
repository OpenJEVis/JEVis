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
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;

/**
 * @author broder
 */
public class PeriodHelper {
    private static final Logger logger = LogManager.getLogger(PeriodHelper.class);
    private static String CUSTOM_SCHEDULE_OBJECT_ATTRIBUTE = "Custom Schedule Object";

    public static double transformTimestampsToExcelTime(DateTime cal) {
        DateTime excelTime = new DateTime(1899, 12, 30, 0, 0, cal.getZone());
        double days = Days.daysBetween(excelTime, cal).getDays();
        double hourtmp = cal.getHourOfDay() * 60;
        double mintmp = cal.getMinuteOfHour();

        double d = (hourtmp + mintmp) / 1440;

        return days + d;
    }

    public static DateTime getNextPeriod(DateTime start, Period schedule, int i) {
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
                Interval temp = new Interval(dateHelper.getStartDate(), dateHelper.getEndDate());
                resultDate = resultDate.minus(temp.toDurationMillis());
                break;
        }
        return resultDate;
    }

    public static DateHelper getDateHelper(JEVisObject objectWithCustomScheduleAttribute, Period schedule, DateHelper dateHelper, DateTime start) {
        if (schedule.equals(Period.CUSTOM)) {
            dateHelper = new DateHelper();
            dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
            dateHelper.setStartDate(start);
            dateHelper.setEndDate(start);
            CustomPeriodObject cpo = null;
            try {
                String targetString = objectWithCustomScheduleAttribute.getAttribute(CUSTOM_SCHEDULE_OBJECT_ATTRIBUTE).getLatestSample().getValueAsString();
                TargetHelper th = new TargetHelper(objectWithCustomScheduleAttribute.getDataSource(), targetString);

                if (th.targetAccessible())
                    cpo = new CustomPeriodObject(th.getObject().get(0), new ObjectHandler(objectWithCustomScheduleAttribute.getDataSource()));
            } catch (JEVisException e) {
                logger.error("Could not get Target Object.");
            }
            dateHelper.setCustomPeriodObject(cpo);
        }
        return dateHelper;
    }
}
