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
 * @author broder
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

        //identify limitbreaking intervals
        List<LimitBreak> limitBreaks = identifyLimitBreaks(intervals, calcAttribute);
        logger.info("{} gaps identified", limitBreaks.size());
        if (limitBreaks.isEmpty()) { //no limit checks when there is no alignment
            return;
        }

        List<JsonLimitsConfig> conf = calcAttribute.getLimitsConfig();

        for (JsonLimitsConfig c : conf) {
            logger.info("start filling with new Mode for " + c.getTypeOfSubstituteValue());
            List<LimitBreak> newLimitBreaks = new ArrayList<>();
            for (LimitBreak lb : limitBreaks) {
                DateTime firstDate = lb.getIntervals().get(0).getDate();
                DateTime lastDate = lb.getIntervals().get(lb.getIntervals().size() - 1).getDate();
                System.out.println("Diff: " + (lastDate.getMillis() - firstDate.getMillis()));
                if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getDurationOverUnderRun())) {
                    System.out.println("Gap: " + lb.toString() + " FirstDate: " + firstDate + " LastDate: " + lastDate);
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
                        Double defaultValue = Double.valueOf(c.getDefaultvalue());
                        fillDefault(newLimitBreaks, defaultValue);
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

        stopWatch.stop();
    }

    private Long defaultValue(String s) {
        Long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<LimitBreak> identifyLimitBreaks(List<CleanInterval> intervals, CleanDataAttribute calcAttribute) {
        List<LimitBreak> limitBreaks = new ArrayList<>();
        LimitBreak currentLimitBreak = null;
        Double lastValue = calcAttribute.getLastCleanValue();
        for (CleanInterval currentInterval : intervals) {
            System.out.println(currentInterval.getDate() + " " + currentInterval.getTmpSamples().size());
        }


        return limitBreaks;
    }

    private void fillStatic(List<LimitBreak> gaps) {
        for (LimitBreak currentGap : gaps) {
            Double firstValue = currentGap.getFirstValue();
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                    String note = "gap(static)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillInterpolation(List<LimitBreak> gaps) {
        for (LimitBreak currentGap : gaps) {
            Double firstValue = currentGap.getFirstValue();
            Double lastValue = currentGap.getLastValue();
            int size = currentGap.getIntervals().size() + 1; //if there is a gap of 2, then you have 3 steps
            Double stepSize = (lastValue - firstValue) / (double) size;
            Double currenValue = firstValue + stepSize;
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                    sample.setValue(currenValue);
                    currenValue += stepSize;
                    String note = "gap(interpolation)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillDefault(List<LimitBreak> gaps, Double value) {
        for (LimitBreak currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "gap(default)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private double getGapValue(DateTime lastDate, JsonLimitsConfig c) throws JEVisException {

        return getSpecificValue(lastDate, c);
    }

    private double getSpecificValue(DateTime lastDate, JsonLimitsConfig c) throws JEVisException {

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

    private double calcValueWithType(List<JEVisSample> listSamples, JsonLimitsConfig c) throws
            JEVisException {
        final String gapFillingType = c.getTypeOfSubstituteValue();
        switch (gapFillingType) {
            case GapFillingType.MEDIAN:
                if (Objects.nonNull(listSamples)) {
                    double medianValue = 0;
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
                    double averageValue = 0;
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
        return 0;
    }


    private void fillMedian(List<LimitBreak> gaps, JsonLimitsConfig c) {
        for (LimitBreak currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    double value = getGapValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "gap(Median)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillAverage(List<LimitBreak> gaps, JsonLimitsConfig c) {
        for (LimitBreak currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    double value = getGapValue(currentInterval.getDate(), c);
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                    String note = "gap(Average)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

}
