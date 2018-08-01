/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.limits;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.JEDataProcessorConstants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.JEDataProcessorConstants.GapFillingReferencePeriod;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanDataAttributeJEVis;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.JEDataProcessorConstants.GapFillingType;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LimitsStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(LimitsStep.class);
    CleanDataAttribute calcAttribute;
    private JEVisObject parentObject;

    @Override
    public void run(ResourceManager resourceManager) throws JEVisException {
        calcAttribute = resourceManager.getCalcAttribute();
        for (JEVisObject obj : calcAttribute.getObject().getParents()) {
            parentObject = obj;
        }
        if (!calcAttribute.getIsPeriodAligned() && !calcAttribute.getLimitsEnabled()) { //no limits check when there is no alignment and disabled
            return;
        }
        StopWatch stopWatch = new Slf4JStopWatch("limits_check");
        List<CleanInterval> intervals = resourceManager.getIntervals();

        List<JsonLimitsConfig> conf = calcAttribute.getLimitsConfig();

        if (Objects.nonNull(conf)) {
            //identify limitbreaking intervals
            List<LimitBreak> limitBreaks = identifyLimitBreaks(intervals, conf);
            logger.info("{} limit breaks identified", limitBreaks.size());

            if (limitBreaks.isEmpty()) { //no limit checks when there is no alignment
                return;
            }

            for (JsonLimitsConfig c : conf) {
                logger.info("start filling with Mode for " + c.getTypeOfSubstituteValue());
                List<LimitBreak> newLimitBreaks = new ArrayList<>();
                for (LimitBreak lb : limitBreaks) {
                    DateTime firstDate = lb.getIntervals().get(0).getDate();
                    DateTime lastDate = lb.getIntervals().get(lb.getIntervals().size() - 1).getDate();
                    if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getDurationOverUnderRun())) {
                        newLimitBreaks.add(lb);
                    }

                    switch (c.getTypeOfSubstituteValue()) {
                        case GapFillingType.NONE:
                            break;
                        case GapFillingType.STATIC:
                            fillStatic(newLimitBreaks);
                            break;
                        case GapFillingType.INTERPOLATION:
                            fillInterpolation(newLimitBreaks);
                            break;
                        case GapFillingType.DEFAULT_VALUE:
                            Double defaultMinValue = Double.valueOf(c.getDefaultMinValue());
                            Double defaultMaxValue = Double.valueOf(c.getDefaultMaxValue());
                            fillDefault(newLimitBreaks, defaultMinValue, defaultMaxValue);
                            break;
                        case GapFillingType.MEDIAN:
                            fillMedian(newLimitBreaks, c);
                            break;
                        case GapFillingType.AVERAGE:
                            fillAverage(newLimitBreaks, c);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        stopWatch.stop();
    }

    private Long defaultValue(String s) {
        Long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<LimitBreak> identifyLimitBreaks(List<CleanInterval> intervals, List<JsonLimitsConfig> conf) throws JEVisException {
        List<LimitBreak> limitBreaks = new ArrayList<>();
        LimitBreak currentLimitBreak = null;
        CleanInterval lastInterval = null;
        for (CleanInterval currentInterval : intervals) {
            for (JsonLimitsConfig lc : conf) {
                for (JEVisSample sample : currentInterval.getTmpSamples()) {
                    Double min = Double.parseDouble(lc.getMin());
                    Double max = Double.parseDouble(lc.getMax());
                    if (sample.getValueAsDouble() < min || sample.getValueAsDouble() > max) {
                        if (currentLimitBreak == null) {
                            currentLimitBreak = new LimitBreakJEVis();
                            if (lastInterval != null && Objects.nonNull(lastInterval.getTmpSamples().get(0)))
                                currentLimitBreak.setFirstValue(lastInterval.getTmpSamples().get(0).getValueAsDouble());
                            currentLimitBreak.addInterval(currentInterval);
                            MinOrMax limit = null;
                            if (sample.getValueAsDouble() < min) limit = MinOrMax.MIN;
                            if (sample.getValueAsDouble() > max) limit = MinOrMax.MAX;
                            currentLimitBreak.setMinOrMax(limit);
                        } else {
                            currentLimitBreak.addInterval(currentInterval);
                        }
                    } else {
                        if (currentLimitBreak != null) {
                            logger.info("Limit Break on: " + currentLimitBreak.getIntervals().get(0).getDate() + " to: " +
                                    currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1).getDate());
                            currentLimitBreak.setLastValue(sample.getValueAsDouble());
                            limitBreaks.add(currentLimitBreak);
                            currentLimitBreak = null;
                        }
                    }
                }
            }
            lastInterval = currentInterval;
        }

        return limitBreaks;
    }

    private void fillStatic(List<LimitBreak> limitBreaks) {
        for (LimitBreak currentLimitBreak : limitBreaks) {
            Double firstValue = currentLimitBreak.getFirstValue();
            for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                    String note = "limit(Static)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillInterpolation(List<LimitBreak> limitBreaks) {
        for (LimitBreak currentLimitBreak : limitBreaks) {
            Double firstValue = currentLimitBreak.getFirstValue();
            Double lastValue = currentLimitBreak.getLastValue();
            int size = currentLimitBreak.getIntervals().size() + 1; //if there is a Limit Break of 2, then you have 3 steps
            Double stepSize = (lastValue - firstValue) / size;
            Double currenValue = firstValue + stepSize;
            for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                    sample.setValue(currenValue);
                    currenValue += stepSize;
                    String note = "limit(Interpolation)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillDefault(List<LimitBreak> limitBreaks, Double minValue, Double maxValue) {
        for (LimitBreak currentLimitBreak : limitBreaks) {
            for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                try {
                    JEVisSample sample = null;
                    MinOrMax limit = null;
                    if (currentLimitBreak.getMinOrMax() == MinOrMax.MIN) {
                        sample = new VirtualSample(currentInterval.getDate(), minValue);
                        limit = MinOrMax.MIN;
                    }
                    if (currentLimitBreak.getMinOrMax() == MinOrMax.MAX) {
                        sample = new VirtualSample(currentInterval.getDate(), maxValue);
                        limit = MinOrMax.MAX;
                    }
                    String note = "limit(DefaultValue," + limit + ")";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private Double getLimitBreakValue(DateTime lastDate, JsonLimitsConfig c) throws JEVisException {

        return getSpecificValue(lastDate, c);
    }

    private Double getSpecificValue(DateTime lastDate, JsonLimitsConfig c) throws JEVisException {

        String bindToSpecificValue = c.getBindtospecific();
        if (Objects.isNull(bindToSpecificValue)) bindToSpecificValue = "";
        SampleHandler sh = new SampleHandler();
        List<JEVisSample> listSamples = null;
        List<JEVisSample> boundListSamples = new ArrayList<>();
        DateTime firstDate;
        switch (bindToSpecificValue) {
            default:
                firstDate = getFirstDate(lastDate, c);
                List<JEVisSample> listSamplesNew = new ArrayList<>();
                listSamples = sh.getSamplesInPeriod(parentObject, CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME, firstDate, lastDate);
                for (JEVisSample sample : listSamples) {
                    if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                        listSamplesNew.add(sample);
                    }
                }
                return calcValueWithType(listSamplesNew, c);

            case (GapFillingBoundToSpecific.WEEKDAY):
                boundListSamples.clear();
                firstDate = getFirstDate(lastDate, c);
                listSamples = sh.getSamplesInPeriod(parentObject, CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME, firstDate, lastDate);
                for (JEVisSample sample : listSamples) {
                    if (sample.getTimestamp().getDayOfWeek() == lastDate.getDayOfWeek()) {
                        if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                            boundListSamples.add(sample);
                        }
                    }
                }
                return calcValueWithType(boundListSamples, c);
            case (GapFillingBoundToSpecific.WEEKOFYEAR):
                boundListSamples.clear();
                firstDate = getFirstDate(lastDate, c);
                listSamples = sh.getSamplesInPeriod(parentObject, CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME, firstDate, lastDate);
                for (JEVisSample sample : listSamples) {
                    if (sample.getTimestamp().getWeekyear() == lastDate.getWeekyear()) {
                        if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                            boundListSamples.add(sample);
                        }
                    }
                }
                return calcValueWithType(boundListSamples, c);
            case (GapFillingBoundToSpecific.MONTHOFYEAR):
                boundListSamples.clear();
                firstDate = getFirstDate(lastDate, c);
                listSamples = sh.getSamplesInPeriod(parentObject, CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME, firstDate, lastDate);
                for (JEVisSample sample : listSamples) {
                    if (sample.getTimestamp().getMonthOfYear() == lastDate.getMonthOfYear()) {
                        if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                            boundListSamples.add(sample);
                        }
                    }
                }
                return calcValueWithType(boundListSamples, c);
        }
    }

    private DateTime getFirstDate(DateTime lastDate, JsonLimitsConfig c) {
        final String referencePeriod = c.getReferenceperiod();
        Integer referencePeriodCount = Integer.parseInt(c.getReferenceperiodcount());
        switch (referencePeriod) {
            case (GapFillingReferencePeriod.DAY):
                return lastDate.minusDays(referencePeriodCount);
            case (GapFillingReferencePeriod.WEEK):
                return lastDate.minusWeeks(referencePeriodCount);
            case (GapFillingReferencePeriod.MONTH):
                return lastDate.minusMonths(referencePeriodCount);
            case (GapFillingReferencePeriod.YEAR):
                return lastDate.minusYears(referencePeriodCount);
            default:
                return lastDate.minusDays(referencePeriodCount);
        }
    }

    private Double calcValueWithType(List<JEVisSample> listSamples, JsonLimitsConfig c) throws
            JEVisException {
        final String typeOfSubstituteValue = c.getTypeOfSubstituteValue();
        switch (typeOfSubstituteValue) {
            case GapFillingType.MEDIAN:
                if (Objects.nonNull(listSamples)) {
                    Double medianValue = 0d;
                    List<Double> sortedArray = new ArrayList<>();
                    for (JEVisSample sample : listSamples) {
                        sortedArray.add(sample.getValueAsDouble());
                    }
                    Collections.sort(sortedArray);
                    medianValue = sortedArray.get(sortedArray.size() / 2);
                    return medianValue;
                }
                break;
            case GapFillingType.AVERAGE:
                if (Objects.nonNull(listSamples)) {
                    Double averageValue = 0d;
                    for (JEVisSample sample : listSamples) {
                        averageValue += sample.getValueAsDouble();
                    }
                    averageValue = averageValue / listSamples.size();
                    return averageValue;
                }
                break;
            default:
                break;
        }
        return Double.NaN;
    }


    private void fillMedian(List<LimitBreak> gaps, JsonLimitsConfig c) {
        for (LimitBreak currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    Double value = getLimitBreakValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "limit(Median)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillAverage(List<LimitBreak> limitBreaks, JsonLimitsConfig c) {
        for (LimitBreak currentLimitBreak : limitBreaks) {
            for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                try {
                    Double value = getLimitBreakValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "limit(Average)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

}
