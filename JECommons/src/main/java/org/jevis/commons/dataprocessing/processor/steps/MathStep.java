package org.jevis.commons.dataprocessing.processor.steps;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.PeriodRule;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.mariuszgromada.math.mxparser.Expression;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MathStep implements ProcessStep {

    private WorkDays workDays;

    @Override
    public void run(ResourceManager resourceManager) throws Exception {

        MathDataObject mathDataObject = resourceManager.getMathDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        workDays = new WorkDays(mathDataObject.getMathDataObject());

        if (mathDataObject.getManipulationAttribute().hasSample()) {
            DateTime start = null, end = null;
            DateTime beginning = mathDataObject.getBeginning();
            DateTime ending = mathDataObject.getEnding();
            int dayOfMonthBeginning = beginning.getDayOfMonth();
            int maxDayOfMonthStartDate = mathDataObject.getStartDate().dayOfMonth().getMaximumValue();
            if (beginning.dayOfMonth().getMaximumValue() == dayOfMonthBeginning) {
                dayOfMonthBeginning = maxDayOfMonthStartDate;
            }
            int dayOfMonthEnding = ending.getDayOfMonth();
            if (ending.dayOfMonth().getMaximumValue() == dayOfMonthEnding) {
                dayOfMonthEnding = maxDayOfMonthStartDate;
            }
            switch (mathDataObject.getReferencePeriod()) {
                default:
                case NONE:
                    start = mathDataObject.getStartDate();
                    end = mathDataObject.getEndDate().minusMillis(1);
                    break;
                case MINUTELY:
                    start = mathDataObject.getStartDate()
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate()
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
                case QUARTER_HOURLY:
                    // TODO: add start & end conditions for 15-minutely
                    break;
                case HOURLY:
                    start = mathDataObject.getStartDate()
                            .withMinuteOfHour(beginning.getMinuteOfHour())
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate()
                            .withMinuteOfHour(ending.getMinuteOfHour())
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
                case DAILY:
                    start = mathDataObject.getStartDate()
                            .withHourOfDay(beginning.getHourOfDay())
                            .withMinuteOfHour(beginning.getMinuteOfHour())
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate()
                            .withHourOfDay(ending.getHourOfDay())
                            .withMinuteOfHour(ending.getMinuteOfHour())
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
                case WEEKLY:
                    start = mathDataObject.getStartDate().withDayOfWeek(beginning.getDayOfWeek())
                            .withHourOfDay(beginning.getHourOfDay())
                            .withMinuteOfHour(beginning.getMinuteOfHour())
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate().withDayOfWeek(ending.getDayOfWeek())
                            .withHourOfDay(ending.getHourOfDay())
                            .withMinuteOfHour(ending.getMinuteOfHour())
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
                case MONTHLY:
                    start = mathDataObject.getStartDate().withDayOfMonth(dayOfMonthBeginning)
                            .withHourOfDay(beginning.getHourOfDay())
                            .withMinuteOfHour(beginning.getMinuteOfHour())
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate().withDayOfMonth(dayOfMonthEnding)
                            .withHourOfDay(ending.getHourOfDay())
                            .withMinuteOfHour(ending.getMinuteOfHour())
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
                case QUARTERLY:
                    // TODO: add start & end conditions for quarters
                    break;
                case YEARLY:
                    start = mathDataObject.getStartDate().withMonthOfYear(beginning.getMonthOfYear())
                            .withDayOfMonth(dayOfMonthBeginning)
                            .withHourOfDay(beginning.getHourOfDay())
                            .withMinuteOfHour(beginning.getMinuteOfHour())
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate().withMonthOfYear(ending.getMonthOfYear())
                            .withDayOfMonth(dayOfMonthEnding)
                            .withHourOfDay(ending.getHourOfDay())
                            .withMinuteOfHour(ending.getMinuteOfHour())
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
                case CUSTOM:
                case CUSTOM2:
                    start = mathDataObject.getStartDate().minusYears(ending.getYear() - beginning.getYear());
                    int maxDay = start.dayOfMonth().getMaximumValue();
                    if (dayOfMonthBeginning > maxDay) {
                        dayOfMonthBeginning = maxDay;
                    }
                    start = mathDataObject.getStartDate().minusYears(ending.getYear() - beginning.getYear())
                            .withDayOfMonth(dayOfMonthBeginning)
                            .withHourOfDay(beginning.getHourOfDay())
                            .withMinuteOfHour(beginning.getMinuteOfHour())
                            .withSecondOfMinute(beginning.getSecondOfMinute())
                            .withMillisOfSecond(beginning.getMillisOfSecond());
                    end = mathDataObject.getStartDate()
                            .withDayOfMonth(dayOfMonthEnding)
                            .withHourOfDay(ending.getHourOfDay())
                            .withMinuteOfHour(ending.getMinuteOfHour())
                            .withSecondOfMinute(ending.getSecondOfMinute())
                            .withMillisOfSecond(ending.getMillisOfSecond());
                    break;
            }

            if (start != null && end != null) {
                ManipulationMode manipulationMode = mathDataObject.getManipulationMode();
                List<JEVisSample> samples = mathDataObject.getInputAttribute().getSamples(start, end);

                if (mathDataObject.getReferencePeriod() == AggregationPeriod.CUSTOM) {
                    filterSamples(samples, intervals, mathDataObject.getPeriodAlignment());
                }

                switch (manipulationMode) {
                    case AVERAGE:
                        for (CleanInterval cleanInterval : intervals) {
                            calcAvg(cleanInterval, samples);
                        }
                        break;
                    case SUM:
                        for (CleanInterval cleanInterval : intervals) {
                            calcSum(cleanInterval, samples);
                        }
                        break;
                    case MIN:
                        for (CleanInterval cleanInterval : intervals) {
                            calcMin(cleanInterval, samples);
                        }
                        break;
                    case MAX:
                        for (CleanInterval cleanInterval : intervals) {
                            calcMax(cleanInterval, samples);
                        }
                        break;
                    case MEDIAN:
                        for (CleanInterval cleanInterval : intervals) {
                            calcMedian(cleanInterval, samples);
                        }
                        break;
                    case RUNNING_MEAN:
                        calcRunningMean(intervals, samples);
                        break;
                    case CENTRIC_RUNNING_MEAN:
                        calcCentricRunningMean(intervals, samples);
                        break;
                    case SORTED_MIN:
                        sortMin(intervals, samples);
                        break;
                    case SORTED_MAX:
                        sortMax(intervals, samples);
                        break;
                    case CUMULATE:
                        for (CleanInterval cleanInterval : intervals) {
                            cumulate(cleanInterval, samples);
                        }
                        break;
                    case NONE:
                        break;
                    case GEOMETRIC_MEAN:
                        for (CleanInterval cleanInterval : intervals) {
                            calcGeoAvg(cleanInterval, samples);
                        }
                        break;
                    case FORMULA:
                        calcFormula(intervals, samples, mathDataObject.getFormula());
                        break;
                }
            }
        }
    }

    private void filterSamples(List<JEVisSample> samples, List<CleanInterval> intervals, List<PeriodRule> periodAlignment) throws JEVisException {
        List<JEVisSample> toRemove = new ArrayList<>();
        PeriodRule periodRule = periodAlignment.get(0);
        DateTime date = intervals.get(0).getDate();

        for (JEVisSample sample : samples) {
            if (Period.months(1).equals(periodRule.getPeriod())) {
                if (sample.getTimestamp().getMonthOfYear() != date.getMonthOfYear()) {
                    toRemove.add(sample);
                }
            }
        }

        samples.removeAll(toRemove);
    }

    private void calcGeoAvg(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        double result = 0d;

        for (JEVisSample sample : samples) {
            if (samples.indexOf(sample) == 0) result = sample.getValueAsDouble();
            else result *= sample.getValueAsDouble();
        }

        result = Math.pow(result, 1.0 / samples.size());

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        cleanInterval.getResult().setNote("math(geo)");
    }

    private void calcAvg(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        double result = 0d;

        for (JEVisSample sample : samples) {
            if (samples.indexOf(sample) == 0) result = sample.getValueAsDouble();
            else result += sample.getValueAsDouble();
        }

        if (samples.size() > 0)
            result = result / samples.size();
        else result = 0d;

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        if (samples.size() > 0)
            cleanInterval.getResult().setNote("math(avg,"
                    + samples.size() + ","
                    + samples.get(0).getTimestamp().toString("YYYYMMdd") + "-" + samples.get(samples.size() - 1).getTimestamp().toString("YYYYMMdd")
                    + ")");
        else cleanInterval.getResult().setNote("math(avg,0)");
    }

    private void calcSum(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        double result = 0d;

        for (JEVisSample sample : samples) {
            if (samples.indexOf(sample) == 0) result = sample.getValueAsDouble();
            else result += sample.getValueAsDouble();
        }

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        if (samples.size() > 0)
            cleanInterval.getResult().setNote("math(sum,"
                    + samples.size() + ","
                    + samples.get(0).getTimestamp().toString("YYYYMMdd") + "-" + samples.get(samples.size() - 1).getTimestamp().toString("YYYYMMdd")
                    + ")");
        else cleanInterval.getResult().setNote("math(sum,0)");
    }

    private void calcMin(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        double result = Double.MAX_VALUE;

        for (JEVisSample sample : samples) {
            result = Math.min(result, sample.getValueAsDouble());
        }

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        cleanInterval.getResult().setNote("math(min)");
    }

    private void calcMax(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        double result = -Double.MAX_VALUE;

        for (JEVisSample sample : samples) {
            result = Math.max(result, sample.getValueAsDouble());
        }

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        cleanInterval.getResult().setNote("math(max)");
    }

    private void calcMedian(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        Double result = 0d;
        List<Double> sortedArray = new ArrayList<>();
        for (JEVisSample sample : samples) {
            if (sample.getValueAsDouble() != null) {
                sortedArray.add(sample.getValueAsDouble());
            }
        }
        Collections.sort(sortedArray);
        if (!sortedArray.isEmpty()) {
            if (sortedArray.size() > 2) result = sortedArray.get(sortedArray.size() / 2);
            else if (sortedArray.size() == 2) result = (sortedArray.get(0) + sortedArray.get(1)) / 2;
        }

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        cleanInterval.getResult().setNote("math(median)");
    }

    private void calcRunningMean(List<CleanInterval> intervals, List<JEVisSample> samples) throws JEVisException {
        Map<DateTime, CleanInterval> map = intervals.stream().collect(Collectors.toMap(CleanInterval::getDate, interval -> interval, (a, b) -> b));

        for (int i = 1; i < samples.size() - 1; i++) {
            Double value0 = samples.get(i - 1).getValueAsDouble();
            Double value1 = samples.get(i).getValueAsDouble();

            Double currentValue = 1d / 2d * (value0 + value1);
            DateTime newTS = samples.get(i).getTimestamp();

            map.get(newTS).getResult().setTimeStamp(newTS);
            map.get(newTS).getResult().setValue(currentValue);
            map.get(newTS).getResult().setNote("math(rm)");
        }
    }

    private void calcCentricRunningMean(List<CleanInterval> intervals, List<JEVisSample> samples) throws JEVisException {
        Map<DateTime, CleanInterval> map = intervals.stream().collect(Collectors.toMap(CleanInterval::getDate, interval -> interval, (a, b) -> b));

        for (int i = 1; i < samples.size() - 1; i++) {
            Double value0 = samples.get(i - 1).getValueAsDouble();
            Double value1 = samples.get(i).getValueAsDouble();
            Double value2 = samples.get(i + 1).getValueAsDouble();

            Double currentValue = 1d / 3d * (value0 + value1 + value2);
            DateTime newTS = samples.get(i).getTimestamp();

            map.get(newTS).getResult().setTimeStamp(newTS);
            map.get(newTS).getResult().setValue(currentValue);
            map.get(newTS).getResult().setNote("math(crm)");
        }
    }

    private void sortMin(List<CleanInterval> intervals, List<JEVisSample> samples) throws JEVisException {
        samples.sort((o1, o2) -> {
            try {
                return o1.getValueAsDouble().compareTo(o2.getValueAsDouble());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            return 0;
        });

        for (CleanInterval cleanInterval : intervals) {
            Double value = samples.get(intervals.indexOf(cleanInterval)).getValueAsDouble();

            cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
            cleanInterval.getResult().setValue(value);
            cleanInterval.getResult().setNote("math(sortMin)");
        }
    }

    private void sortMax(List<CleanInterval> intervals, List<JEVisSample> samples) throws JEVisException {
        samples.sort((o1, o2) -> {
            try {
                return o2.getValueAsDouble().compareTo(o1.getValueAsDouble());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            return 0;
        });

        for (CleanInterval cleanInterval : intervals) {
            Double value = samples.get(intervals.indexOf(cleanInterval)).getValueAsDouble();

            cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
            cleanInterval.getResult().setValue(value);
            cleanInterval.getResult().setNote("math(sortMax)");
        }
    }

    private void cumulate(CleanInterval cleanInterval, List<JEVisSample> samples) throws JEVisException {

        double result = 0d;

        for (JEVisSample sample : samples) {
            if (samples.indexOf(sample) == 0) result = sample.getValueAsDouble();
            else result += sample.getValueAsDouble();
        }

        cleanInterval.getResult().setTimeStamp(cleanInterval.getDate());
        cleanInterval.getResult().setValue(result);
        cleanInterval.getResult().setNote("math(avg)");
    }

    private void calcFormula(List<CleanInterval> intervals, List<JEVisSample> samples, String formula) throws JEVisException {
        Map<DateTime, CleanInterval> map = intervals.stream().collect(Collectors.toMap(CleanInterval::getDate, interval -> interval, (a, b) -> b));
        NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getDefaultBundle().getLocale());

        for (JEVisSample sample : samples) {
            Expression expression = new Expression(formula.replace("x", nf.format(sample.getValueAsDouble())));
            double result = expression.calculate();
            map.get(sample.getTimestamp()).getResult().setTimeStamp(sample.getTimestamp());
            map.get(sample.getTimestamp()).getResult().setValue(result);
            map.get(sample.getTimestamp()).getResult().setNote("math(formula)");
        }
    }
}
