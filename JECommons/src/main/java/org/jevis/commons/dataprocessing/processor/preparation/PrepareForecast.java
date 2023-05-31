package org.jevis.commons.dataprocessing.processor.preparation;

import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.PeriodRule;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.datetime.WorkDays;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class PrepareForecast implements ProcessStep {
    private static DateTime fixTimeZoneOffset(DateTimeZone tz, DateTime start, int offset) {
        int newOffset = tz.getOffset(start);

        if (newOffset > offset) {
            start = start.minus(newOffset - offset);
        } else if (newOffset < offset) {
            start = start.plus(offset - newOffset);
        }
        return start;
    }

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();
        WorkDays workDays = new WorkDays(forecastDataObject.getForecastDataObject());
        DateTimeZone tz = workDays.getDateTimeZone();

        List<PeriodRule> periodRules = forecastDataObject.getInputDataPeriodAlignment();
        List<CleanInterval> intervals = new ArrayList<>();

        DateTime start = forecastDataObject.getStartDate();
        DateTime end = forecastDataObject.getEndDate();
        Period inputPeriod = CleanDataObject.getPeriodForDate(periodRules, start);

        boolean periodHasYear = inputPeriod.getYears() > 0;
        boolean periodHasMonths = inputPeriod.getMonths() > 0;
        boolean periodHasWeeks = inputPeriod.getWeeks() > 0;
        boolean periodHasDays = inputPeriod.getDays() > 0;
        boolean periodHasHours = inputPeriod.getHours() > 0;
        boolean periodHasMinutes = inputPeriod.getMinutes() > 0;
        boolean periodHasSeconds = inputPeriod.getSeconds() > 0;
        boolean periodHasMillis = inputPeriod.getMillis() > 0;

        if (periodHasMonths || periodHasYear || periodHasWeeks || periodHasDays || periodHasHours || periodHasMinutes || periodHasSeconds || periodHasMillis) {

            if (start != null && end != null) {
                int offset = tz.getOffset(start);
                while (start.isBefore(end)) {

                    inputPeriod = CleanDataObject.getPeriodForDate(periodRules, start);
                    DateTime endOfInterval;
                    if (periodHasYear) {
                        endOfInterval = start.plusYears(inputPeriod.getYears());
                        endOfInterval = endOfInterval.plusMonths(inputPeriod.getMonths());
                        endOfInterval = addLesserPeriods(endOfInterval, inputPeriod);

                        if (inputPeriod.getMonths() == 0 && inputPeriod.getYears() == 1 && inputPeriod.getWeeks() == 0
                                && inputPeriod.getDays() == 0 && inputPeriod.getHours() == 0 && inputPeriod.getMinutes() == 0
                                && inputPeriod.getMillis() == 0) {
                            endOfInterval = endOfInterval.withMonthOfYear(endOfInterval.monthOfYear().getMaximumValue());
                            endOfInterval = endOfInterval.withDayOfMonth(endOfInterval.dayOfMonth().getMaximumValue());
                        }
                    } else if (periodHasMonths) {
                        endOfInterval = start.plusMonths(inputPeriod.getMonths());
                        endOfInterval = endOfInterval.plusYears(inputPeriod.getYears());
                        endOfInterval = addLesserPeriods(endOfInterval, inputPeriod);

                        if (inputPeriod.getMonths() == 1 && inputPeriod.getYears() == 0 && inputPeriod.getWeeks() == 0
                                && inputPeriod.getDays() == 0 && inputPeriod.getHours() == 0 && inputPeriod.getMinutes() == 0
                                && inputPeriod.getMillis() == 0) {
                            endOfInterval = endOfInterval.withDayOfMonth(endOfInterval.dayOfMonth().getMaximumValue());
                        }
                    } else {
                        endOfInterval = start.plus(inputPeriod);
                    }
                    endOfInterval = fixTimeZoneOffset(tz, endOfInterval, offset);
                    endOfInterval = endOfInterval.minusMillis(1);

                    if (!start.isBefore(endOfInterval)) {
                        break;
                    }
                    
                    Interval interval = new Interval(start, endOfInterval);
                    CleanInterval cleanInterval = new CleanInterval(interval, start);
                    cleanInterval.getResult().setTimeStamp(start);
                    cleanInterval.setInputPeriod(inputPeriod);
                    intervals.add(cleanInterval);

                    if (periodHasYear) {
                        start = start.plusYears(inputPeriod.getYears());
                        start = start.plusMonths(inputPeriod.getMonths());
                        start = addLesserPeriods(start, inputPeriod);

                        if (inputPeriod.getMonths() == 0 && inputPeriod.getYears() == 1 && inputPeriod.getWeeks() == 0
                                && inputPeriod.getDays() == 0 && inputPeriod.getHours() == 0 && inputPeriod.getMinutes() == 0
                                && inputPeriod.getMillis() == 0) {
                            start = start.withMonthOfYear(start.monthOfYear().getMaximumValue());
                            start = start.withDayOfMonth(start.dayOfMonth().getMaximumValue());
                        }
                    } else if (periodHasMonths) {
                        start = start.plusMonths(inputPeriod.getMonths());
                        start = start.plusYears(inputPeriod.getYears());
                        start = addLesserPeriods(start, inputPeriod);

                        if (inputPeriod.getMonths() == 1 && inputPeriod.getYears() == 0 && inputPeriod.getWeeks() == 0
                                && inputPeriod.getDays() == 0 && inputPeriod.getHours() == 0 && inputPeriod.getMinutes() == 0
                                && inputPeriod.getMillis() == 0) {
                            start = start.withDayOfMonth(start.dayOfMonth().getMaximumValue());
                        }
                    } else {
                        start = start.plus(inputPeriod);
                    }
                    start = fixTimeZoneOffset(tz, start, offset);
                    offset = tz.getOffset(start);
                }
                resourceManager.setIntervals(intervals);
            }
        }
    }

    private DateTime addLesserPeriods(DateTime start, Period inputPeriod) {
        DateTime date = start;
        date = date.plusWeeks(inputPeriod.getWeeks());
        date = date.plusDays(inputPeriod.getDays());
        date = date.plusHours(inputPeriod.getHours());
        date = date.plusMinutes(inputPeriod.getMinutes());
        date = date.plusSeconds(inputPeriod.getSeconds());
        date = date.plusMillis(inputPeriod.getMillis());
        return date;
    }
}
