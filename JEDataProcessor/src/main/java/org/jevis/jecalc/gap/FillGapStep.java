/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.gap;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.JEDataProcessorConstants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.JEDataProcessorConstants.GapFillingReferencePeriod;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.json.JsonGapFillingConfig;
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
public class FillGapStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(FillGapStep.class);
    CleanDataAttribute calcAttribute;
    private JEVisObject parentObject;

    @Override
    public void run(ResourceManager resourceManager) throws JEVisException {
        calcAttribute = resourceManager.getCalcAttribute();
        for (JEVisObject obj : calcAttribute.getObject().getParents()) {
            parentObject = obj;
        }
        if (!calcAttribute.getIsPeriodAligned()) { //no gap filling when there is no alignment
            return;
        }
        StopWatch stopWatch = new Slf4JStopWatch("gap_filling");
        List<CleanInterval> intervals = resourceManager.getIntervals();

        //identify gaps, gaps holds intervals
        List<Gap> gaps = identifyGaps(intervals, calcAttribute);
        logger.info("{} gaps identified", gaps.size());
        if (gaps.isEmpty()) { //no gap filling when there are no gaps
            return;
        }

        List<JsonGapFillingConfig> conf = calcAttribute.getGapFillingConfig();

        if (Objects.nonNull(conf)) {
            if (!conf.isEmpty()) {
                for (JsonGapFillingConfig c : conf) {
                    logger.info("start filling with new Mode for " + c.getType());
                    List<Gap> newGaps = new ArrayList<>();
                    for (Gap g : gaps) {
                        DateTime firstDate = g.getIntervals().get(0).getDate();
                        DateTime lastDate = g.getIntervals().get(g.getIntervals().size() - 1).getDate();
                        if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                            newGaps.add(g);
                        }

                        switch (c.getType()) {
                            case GapFillingType.NONE:
                                break;
                            case GapFillingType.STATIC:
                                fillStatic(newGaps);
                                break;
                            case GapFillingType.INTERPOLATION:
                                fillInterpolation(newGaps);
                                break;
                            case GapFillingType.DEFAULT_VALUE:
                                Double defaultValue = Double.valueOf(c.getDefaultvalue());
                                fillDefault(newGaps, defaultValue);
                                break;
                            case GapFillingType.MINIMUM:
                                fillMinimum(newGaps, c);
                                break;
                            case GapFillingType.MAXIMUM:
                                fillMaximum(newGaps, c);
                                break;
                            case GapFillingType.MEDIAN:
                                fillMedian(newGaps, c);
                                break;
                            case GapFillingType.AVERAGE:
                                fillAverage(newGaps, c);
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else {
                logger.error("Found gap but missing GapFillingConfig in Object: " + calcAttribute.getObject().getName() + " Id: " + calcAttribute.getObject().getID());
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

    private List<Gap> identifyGaps(List<CleanInterval> intervals, CleanDataAttribute calcAttribute) {
        List<Gap> gaps = new ArrayList<>();
        Gap currentGap = null;
        Double lastValue = calcAttribute.getLastCleanValue();
        for (CleanInterval currentInterval : intervals) {
            if (currentInterval.getTmpSamples().isEmpty()) { //could be the start of the gap or in a gap
                if (currentGap != null) {//current in a gap
                    currentGap.addInterval(currentInterval);
                } else { //start of a gap
                    currentGap = new GapJEVis();
                    currentGap.addInterval(currentInterval);
                    currentGap.setFirstValue(lastValue);
                }
            } else { //could be the end of the gap or no gap
                for (JEVisSample sample : currentInterval.getTmpSamples()) {
                    try {
                        Double rawValue = sample.getValueAsDouble();
                        if (currentGap != null) { //end of the gap
                            currentGap.setLastValue(rawValue);
                            gaps.add(currentGap);
                            currentGap = null;
                            lastValue = sample.getValueAsDouble();
                        } else { //not in a gap
                            lastValue = sample.getValueAsDouble();
                        }
                    } catch (JEVisException ex) {
                        logger.error(null, ex);
                    }
                }
            }
        }

        List<Gap> filteredGaps = new ArrayList<>();
        for (Gap gap : gaps) {
            List<CleanInterval> currentIntervals = gap.getIntervals();
            Double firstGapValue = gap.getFirstValue();
            Double lastGapValue = gap.getLastValue();
            if (!currentIntervals.isEmpty() && firstGapValue != null && lastGapValue != null) {
                filteredGaps.add(gap);
            }
        }
        return filteredGaps;
    }

    private void fillStatic(List<Gap> gaps) {
        for (Gap currentGap : gaps) {
            Double firstValue = currentGap.getFirstValue();
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                    String note = "";
                    try {
                        note += currentInterval.getRawSamples().get(0).getNote();
                    } catch (Exception e) {
                    }
                    note += "gap(static)";
                    sample.setNote(note);
                    currentInterval.addTmpSample(sample);
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillInterpolation(List<Gap> gaps) {
        for (Gap currentGap : gaps) {
            Double firstValue = currentGap.getFirstValue();
            Double lastValue = currentGap.getLastValue();
            int size = currentGap.getIntervals().size() + 1; //if there is a gap of 2, then you have 3 steps
            Double stepSize = (lastValue - firstValue) / size;
            Double currenValue = firstValue + stepSize;
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    for (JEVisSample smp : currentInterval.getRawSamples()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), currenValue);
                        String note = currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getNote() + ",gap(Interpolation)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillDefault(List<Gap> gaps, Double value) {
        for (Gap currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    for (JEVisSample smp : currentInterval.getRawSamples()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getNote() + ",gap(Default)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }


    private void fillMinimum(List<Gap> gaps, JsonGapFillingConfig c) {

        for (Gap currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    Double value = getGapValue(currentInterval.getDate(), c);
                    for (JEVisSample smp : currentInterval.getRawSamples()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getNote() + ",gap(Minimum)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private Double getGapValue(DateTime lastDate, JsonGapFillingConfig c) throws JEVisException {

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
        final String gapFillingType = c.getType();
        switch (gapFillingType) {
            case GapFillingType.MINIMUM:
                if (Objects.nonNull(listSamples)) {
                    Double minValue = listSamples.get(0).getValueAsDouble();
                    for (JEVisSample sample : listSamples) {
                        minValue = Math.min(minValue, sample.getValueAsDouble());
                    }
                    return minValue;
                }
                break;
            case GapFillingType.MAXIMUM:
                if (Objects.nonNull(listSamples)) {
                    Double maxValue = listSamples.get(0).getValueAsDouble();
                    for (JEVisSample sample : listSamples) {
                        maxValue = Math.max(maxValue, sample.getValueAsDouble());
                    }
                    return maxValue;
                }
                break;
            case GapFillingType.MEDIAN:
                if (Objects.nonNull(listSamples)) {
                    Double medianValue = 0d;
                    List<Double> sortedArray = new ArrayList<>();
                    for (JEVisSample sample : listSamples) {
                        sortedArray.add(sample.getValueAsDouble());
                    }
                    Collections.sort(sortedArray);
                    if (!sortedArray.isEmpty()) {
                        if (sortedArray.size() > 2) medianValue = sortedArray.get(sortedArray.size() / 2);
                        else if (sortedArray.size() == 2) medianValue = (sortedArray.get(0) + sortedArray.get(1)) / 2;
                    }

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

    private void fillMaximum(List<Gap> gaps, JsonGapFillingConfig c) {

        for (Gap currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    Double value = getGapValue(currentInterval.getDate(), c);
                    for (JEVisSample smp : currentInterval.getRawSamples()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getNote() + ",gap(Maximum)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillMedian(List<Gap> gaps, JsonGapFillingConfig c) {
        for (Gap currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    Double value = getGapValue(currentInterval.getDate(), c);
                    for (JEVisSample smp : currentInterval.getRawSamples()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getNote() + ",gap(Median)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private void fillAverage(List<Gap> gaps, JsonGapFillingConfig c) {
        for (Gap currentGap : gaps) {
            for (CleanInterval currentInterval : currentGap.getIntervals()) {
                try {
                    Double value = getGapValue(currentInterval.getDate(), c);
                    for (JEVisSample smp : currentInterval.getRawSamples()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = currentInterval.getTmpSamples().get(currentInterval.getRawSamples().indexOf(smp)).getNote() + ",gap(Average)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                } catch (JEVisException | ClassCastException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

}
