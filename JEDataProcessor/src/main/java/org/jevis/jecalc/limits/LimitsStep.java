/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.limits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.JEDataProcessorConstants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.JEDataProcessorConstants.GapFillingReferencePeriod;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanDataAttributeJEVis;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.JEDataProcessorConstants.GapFillingType;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LimitsStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(LimitsStep.class);
    CleanDataAttribute calcAttribute;
    private JEVisObject parentObject;

    @Override
    public void run(ResourceManager resourceManager) throws JEVisException {
        calcAttribute = resourceManager.getCalcAttribute();
        for (JEVisObject obj : calcAttribute.getObject().getParents()) {
            parentObject = obj;
        }
        if (!calcAttribute.getIsPeriodAligned() || !calcAttribute.getLimitsEnabled() || !calcAttribute.getLimitsConfig().isEmpty()) {
            //no limits check when there is no alignment or disabled or no config
            return;
        }
        List<CleanInterval> intervals = resourceManager.getIntervals();

        List<JsonLimitsConfig> confLimitsStep1 = new ArrayList<>();
        List<JsonLimitsConfig> confLimitsStep2 = new ArrayList<>();
        if (calcAttribute.getLimitsConfig().size() > 0) {
            confLimitsStep1.add(calcAttribute.getLimitsConfig().get(0));
        }
        if (calcAttribute.getLimitsConfig().size() > 1) {
            confLimitsStep2.add(calcAttribute.getLimitsConfig().get(1));
        }

        List<JsonGapFillingConfig> confGaps = calcAttribute.getGapFillingConfig();

        if (Objects.nonNull(confLimitsStep1)) {
            //identify limitbreaking intervals
            List<LimitBreak> limitBreaksStep1 = identifyLimitBreaks(intervals, confLimitsStep1);
            List<LimitBreak> limitBreaksStep2 = identifyLimitBreaks(intervals, confLimitsStep2);
            logger.info("{} limit breaks identified", limitBreaksStep2.size() + limitBreaksStep1.size());

            if (limitBreaksStep1.isEmpty() && limitBreaksStep2.isEmpty()) { //no limit checks when there is no alignment
                return;
            }

            for (JsonLimitsConfig limitsConfig : calcAttribute.getLimitsConfig()) {
                if (calcAttribute.getLimitsConfig().indexOf(limitsConfig) == 0) {
                    for (LimitBreak limitBreak : limitBreaksStep1) {
                        Double firstValue = limitBreak.getFirstValue();
                        for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                            logger.info("start marking strange Nodes");
                            try {
                                for (JEVisSample smp : currentInterval.getRawSamples()) {
                                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getValueAsDouble());
                                    String note = "";
                                    note += getNote(currentInterval);
                                    note += ",limit(Step1)";
                                    sample.setNote(note);
                                    currentInterval.addTmpSample(sample);
                                }
                            } catch (JEVisException | ClassCastException ex) {
                                logger.error(ex);
                            }
                        }
                    }
                } else {
                    if (Objects.nonNull(confGaps)) {
                        if (!confGaps.isEmpty()) {
                            List<LimitBreak> filledLimitBreaks = new ArrayList<>();
                            for (JsonGapFillingConfig c : confGaps) {
                                List<LimitBreak> newLimitBreaks = new ArrayList<>();
                                for (LimitBreak lb : limitBreaksStep2) {
                                    logger.info("start filling with Mode for " + c.getType());
                                    DateTime firstDate = lb.getIntervals().get(0).getDate();
                                    DateTime lastDate = lb.getIntervals().get(lb.getIntervals().size() - 1).getDate();
                                    if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                                        if (!filledLimitBreaks.contains(lb)) {
                                            newLimitBreaks.add(lb);
                                            filledLimitBreaks.add(lb);
                                        }
                                    }
                                }
                                switch (c.getType()) {
                                    case GapFillingType.NONE:
                                        break;
                                    case GapFillingType.STATIC:
                                        fillStatic(newLimitBreaks);
                                        break;
                                    case GapFillingType.INTERPOLATION:
                                        fillInterpolation(newLimitBreaks);
                                        break;
                                    case GapFillingType.DEFAULT_VALUE:
                                        Double defaultValue = Double.valueOf(c.getDefaultvalue());
                                        fillDefault(newLimitBreaks, defaultValue);
                                        break;
                                    case GapFillingType.MINIMUM:
                                        fillMinimum(newLimitBreaks, c);
                                        break;
                                    case GapFillingType.MAXIMUM:
                                        fillMaximum(newLimitBreaks, c);
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
                }
            }
        }
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
                    Double min;
                    Double max;
                    try {
                        min = Double.parseDouble(lc.getMin());
                        max = Double.parseDouble(lc.getMax());
                    } catch (Exception e) {
                        logger.error("Invalid Limit Configuration", e);
                        return null;
                    }
                    if (sample.getValueAsDouble() < min || sample.getValueAsDouble() > max) {
                        if (currentLimitBreak == null) {
                            currentLimitBreak = new LimitBreakJEVis();
                            if (lastInterval != null && !lastInterval.getTmpSamples().isEmpty() && Objects.nonNull(lastInterval.getTmpSamples().get(0)))
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

    private void fillStatic(List<LimitBreak> breaks) {
        for (LimitBreak limitBreak : breaks) {
            Double firstValue = limitBreak.getFirstValue();
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                    String note = "";
                    note += getNote(currentInterval);
                    note += ",limit(Static)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    private void fillInterpolation(List<LimitBreak> breaks) {
        for (LimitBreak limitBreak : breaks) {
            Double firstValue = limitBreak.getFirstValue();
            Double lastValue = limitBreak.getLastValue();
            int size = limitBreak.getIntervals().size() + 1; //if there is a Limit Break of 2, then you have 3 steps
            if (firstValue != null && lastValue != null) {
                Double stepSize = (lastValue - firstValue) / size;
                Double currenValue = firstValue + stepSize;
                for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                    try {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), currenValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Interpolation)";
                        sample.setNote(note);
                        currenValue += stepSize;
                        currentInterval.addTmpSample(sample);
                    } catch (JEVisException | ClassCastException ex) {
                        logger.error(ex);
                    }
                }
            }
        }
    }

    private void fillDefault(List<LimitBreak> breaks, Double defaultValue) {
        for (LimitBreak limitBreak : breaks) {
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), defaultValue);
                    String note = "";
                    note += getNote(currentInterval);
                    note += ",limit(Default)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    private Double getLimitBreakValue(DateTime lastDate, JsonGapFillingConfig c) throws JEVisException {

        return getSpecificValue(lastDate, c);
    }

    private Double getSpecificValue(DateTime lastDate, JsonGapFillingConfig c) throws JEVisException {

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

    private DateTime getFirstDate(DateTime lastDate, JsonGapFillingConfig c) {
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

    private Double calcValueWithType(List<JEVisSample> listSamples, JsonGapFillingConfig c) throws
            JEVisException {
        final String typeOfSubstituteValue = c.getType();
        if (Objects.nonNull(listSamples) && !listSamples.isEmpty()) {
            switch (typeOfSubstituteValue) {
                case GapFillingType.MEDIAN:
                    Double medianValue = 0d;
                    List<Double> sortedArray = new ArrayList<>();
                    for (JEVisSample sample : listSamples) {
                        sortedArray.add(sample.getValueAsDouble());
                    }
                    Collections.sort(sortedArray);
                    medianValue = sortedArray.get(sortedArray.size() / 2);
                    return medianValue;
                case GapFillingType.AVERAGE:
                    Double averageValue = 0d;
                    for (JEVisSample sample : listSamples) {
                        averageValue += sample.getValueAsDouble();
                    }
                    averageValue = averageValue / listSamples.size();
                    return averageValue;
                default:
                    break;
            }
        }
        return 0d;
    }

    private void fillMinimum(List<LimitBreak> breaks, JsonGapFillingConfig c) {

        for (LimitBreak limitBreak : breaks) {
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                try {
                    Double value = getLimitBreakValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "";
                    note += getNote(currentInterval);
                    note += ",limit(Minimum)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    private void fillMaximum(List<LimitBreak> breaks, JsonGapFillingConfig c) {

        for (LimitBreak limitBreak : breaks) {
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                try {
                    Double value = getLimitBreakValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "";
                    note += getNote(currentInterval);
                    note += ",limit(Maximum)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    private void fillMedian(List<LimitBreak> breaks, JsonGapFillingConfig c) {
        for (LimitBreak limitBreak : breaks) {
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                try {
                    Double value = getLimitBreakValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "";
                    note += getNote(currentInterval);
                    note += ",limit(Median)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    private void fillAverage(List<LimitBreak> breaks, JsonGapFillingConfig c) {
        for (LimitBreak limitBreak : breaks) {
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                try {
                    Double value = getLimitBreakValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "";
                    note += getNote(currentInterval);
                    note += ",limit(Average)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(ex);
                }
            }
        }
    }

}
