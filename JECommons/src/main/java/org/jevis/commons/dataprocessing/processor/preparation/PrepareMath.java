package org.jevis.commons.dataprocessing.processor.preparation;

import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.PeriodRule;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.datetime.WorkDays;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class PrepareMath implements ProcessStep {
    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        MathDataObject mathDataObject = resourceManager.getMathDataObject();

        List<PeriodRule> periodRules = mathDataObject.getInputDataPeriodAlignment();
        List<CleanInterval> intervals = new ArrayList<>();

        DateTime start = mathDataObject.getStartDate();
        DateTime end = mathDataObject.getEndDate();
        Period inputPeriod = CleanDataObject.getPeriodForDate(periodRules, start);

        AggregationPeriod aggregationPeriod = mathDataObject.getReferencePeriod();
        WorkDays workDays = new WorkDays(mathDataObject.getMathDataObject());

        if (aggregationPeriod.isGreaterThenDays() && workDays.isCustomWorkDay()) {
            java.time.LocalTime wdStart = workDays.getWorkdayStart();
            java.time.LocalTime wdEnd = workDays.getWorkdayEnd();
            LocalTime dtStart = new LocalTime(wdStart.getHour(), wdStart.getMinute(), wdStart.getSecond(), 0);
            LocalTime dtEnd = new LocalTime(wdEnd.getHour(), wdEnd.getMinute(), wdEnd.getSecond(), 999);

            start = start.withTime(dtStart);
            end = end.withTime(dtEnd).plusMillis(1);

            if (dtEnd.isBefore(dtStart)) {
                start = start.minusDays(1);
                end = end.minusDays(1);
            }
        }

        Long referencePeriodCount = mathDataObject.getReferencePeriodCount();
        Long periodOffset = mathDataObject.getPeriodOffset();

        boolean periodHasYear = inputPeriod.getYears() > 0;
        boolean periodHasMonths = inputPeriod.getMonths() > 0;
        boolean periodHasWeeks = inputPeriod.getWeeks() > 0;
        boolean periodHasDays = inputPeriod.getDays() > 0;
        boolean periodHasHours = inputPeriod.getHours() > 0;
        boolean periodHasMinutes = inputPeriod.getMinutes() > 0;
        boolean periodHasSeconds = inputPeriod.getSeconds() > 0;
        boolean periodHasMillis = inputPeriod.getMillis() > 0;

        if (periodHasMonths || periodHasYear || periodHasWeeks || periodHasDays || periodHasHours || periodHasMinutes || periodHasSeconds || periodHasMillis) {
            if (mathDataObject.isFillPeriod()) {
                if (start != null && end != null) {
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
                        endOfInterval = endOfInterval.minusMillis(1);
                        Interval interval = new Interval(start, endOfInterval);
                        CleanInterval cleanInterval = new CleanInterval(interval, start);
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
                    }
                    resourceManager.setIntervals(intervals);
                }
            } else {
                Interval interval = new Interval(start, end.minusMillis(1));

                if (periodOffset != 0L) {
                    start = mathDataObject.getNextRunWithOffset(aggregationPeriod, periodOffset, referencePeriodCount, start);
                }

                CleanInterval cleanInterval = new CleanInterval(interval, start);
                intervals.add(cleanInterval);
                resourceManager.setIntervals(intervals);
            }
        } else {
            Interval interval = new Interval(start, end.minusMillis(1));

            if (periodOffset != 0L) {
                start = mathDataObject.getNextRunWithOffset(aggregationPeriod, periodOffset, referencePeriodCount, start);
            }

            CleanInterval cleanInterval = new CleanInterval(interval, start);
            intervals.add(cleanInterval);
            resourceManager.setIntervals(intervals);
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
