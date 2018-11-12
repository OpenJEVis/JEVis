package org.jevis.jecalc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.JEDataProcessorConstants;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.gap.Gap;
import org.jevis.jecalc.limits.LimitBreak;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GapsAndLimits {
    private static final Logger logger = LogManager.getLogger(GapsAndLimits.class);
    private List<CleanInterval> intervals;
    private CleanDataAttribute calcAttribute;
    private GapsAndLimitsType gapsAndLimitsType;
    private List<Gap> gapList;
    private List<LimitBreak> limitBreaksList;
    private JsonGapFillingConfig c;
    private List<JEVisSample> sampleCache;

    public GapsAndLimits(List<CleanInterval> intervals, CleanDataAttribute calcAttribute, GapsAndLimitsType type,
                         JsonGapFillingConfig c, List<Gap> gapList, List<LimitBreak> limitBreaksList, List<JEVisSample> sampleCache) {
        this.intervals = intervals;
        this.calcAttribute = calcAttribute;
        this.gapsAndLimitsType = type;
        this.gapList = gapList;
        this.limitBreaksList = limitBreaksList;
        this.c = c;
        this.sampleCache = sampleCache;
    }

    public static String getNote(CleanInterval currentInterval) {
        String note = "";
        try {
            note += currentInterval.getTmpSamples().get(0).getNote();
        } catch (Exception e1) {
            try {
                note += currentInterval.getRawSamples().get(0).getNote();
            } catch (Exception e2) {
            }
        }
        if (note.equals("null")) {
            note = "No Note";
        }
        return note;
    }

    private Double getSpecificValue(DateTime lastDate) throws JEVisException {

        String bindToSpecificValue = c.getBindtospecific();
        if (Objects.isNull(bindToSpecificValue)) bindToSpecificValue = "";
        SampleHandler sh = new SampleHandler();
        List<JEVisSample> boundListSamples = new ArrayList<>();
        DateTime firstDate;

        boundListSamples.clear();
        firstDate = getFirstDate(lastDate);
        List<JEVisSample> listSamplesNew = new ArrayList<>();
        switch (bindToSpecificValue) {
            case (JEDataProcessorConstants.GapFillingBoundToSpecific.WEEKDAY):
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getDayOfWeek() == lastDate.getDayOfWeek()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                }
                for (CleanInterval ci : intervals) {
                    if (ci.getDate().equals(firstDate) || (ci.getDate().isAfter(firstDate) && ci.getDate().isBefore(lastDate))
                            || ci.getDate().equals(lastDate))
                        for (JEVisSample js : ci.getTmpSamples()) {
                            if (js.getTimestamp().getDayOfWeek() == lastDate.getDayOfWeek()) {
                                if ((js.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (js.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                    boundListSamples.add(js);
                                }
                            }
                        }
                }
                return calcValueWithType(boundListSamples);
            case (JEDataProcessorConstants.GapFillingBoundToSpecific.WEEKOFYEAR):
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getWeekyear() == lastDate.getWeekyear()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                }
                for (CleanInterval ci : intervals) {
                    if (ci.getDate().equals(firstDate) || (ci.getDate().isAfter(firstDate) && ci.getDate().isBefore(lastDate))
                            || ci.getDate().equals(lastDate))
                        for (JEVisSample js : ci.getTmpSamples()) {
                            if (js.getTimestamp().getWeekyear() == lastDate.getWeekyear()) {
                                if ((js.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (js.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                    boundListSamples.add(js);
                                }
                            }
                        }
                }
                return calcValueWithType(boundListSamples);
            case (JEDataProcessorConstants.GapFillingBoundToSpecific.MONTHOFYEAR):
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getMonthOfYear() == lastDate.getMonthOfYear()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                }
                for (CleanInterval ci : intervals) {
                    if (ci.getDate().equals(firstDate) || (ci.getDate().isAfter(firstDate) && ci.getDate().isBefore(lastDate))
                            || ci.getDate().equals(lastDate))
                        for (JEVisSample js : ci.getTmpSamples()) {
                            if (js.getTimestamp().getMonthOfYear() == lastDate.getMonthOfYear()) {
                                if ((js.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (js.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                    boundListSamples.add(js);
                                }
                            }
                        }
                }
                return calcValueWithType(boundListSamples);
            default:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                            listSamplesNew.add(sample);
                        }
                    }
                }
                for (CleanInterval ci : intervals) {
                    if (ci.getDate().equals(firstDate) || (ci.getDate().isAfter(firstDate) && ci.getDate().isBefore(lastDate))
                            || ci.getDate().equals(lastDate))
                        for (JEVisSample js : ci.getTmpSamples()) {
                            if ((js.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (js.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                listSamplesNew.add(js);
                            }
                        }
                }
                return calcValueWithType(listSamplesNew);
        }
    }

    private Double calcValueWithType(List<JEVisSample> listSamples) throws
            JEVisException {
        final String gapFillingType = c.getType();

        if (Objects.nonNull(listSamples) && !listSamples.isEmpty()) {
            switch (gapFillingType) {
                case JEDataProcessorConstants.GapFillingType.MINIMUM:
                    Double minValue = listSamples.get(0).getValueAsDouble();
                    for (JEVisSample sample : listSamples) {
                        minValue = Math.min(minValue, sample.getValueAsDouble());
                    }
                    return minValue;
                case JEDataProcessorConstants.GapFillingType.MAXIMUM:
                    Double maxValue = listSamples.get(0).getValueAsDouble();
                    for (JEVisSample sample : listSamples) {
                        maxValue = Math.max(maxValue, sample.getValueAsDouble());
                    }
                    return maxValue;
                case JEDataProcessorConstants.GapFillingType.MEDIAN:
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
                case JEDataProcessorConstants.GapFillingType.AVERAGE:
                    Double averageValue = 0d;
                    for (JEVisSample sample : listSamples) {
                        averageValue += sample.getValueAsDouble();
                    }
                    //logger.info("sum: " + averageValue + " listSize: " + listSamples.size());
                    averageValue = averageValue / listSamples.size();
                    return averageValue;
                default:
                    break;
            }
        }
        return 0d;
    }

    public void fillMaximum() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    for (CleanInterval currentInterval : currentGap.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",gap(Maximum)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
                break;
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Maximum)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
                break;
        }
    }

    public void fillMedian() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    for (CleanInterval currentInterval : currentGap.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",gap(Median)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);

                    }
                }
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Median)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
                break;
        }
        logger.info("Done");
    }

    public void fillAverage() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    for (CleanInterval currentInterval : currentGap.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",gap(Average)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Average)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
                break;
        }
    }

    public void fillInterpolation() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    Double firstValue = currentGap.getFirstValue();
                    Double lastValue = currentGap.getLastValue();
                    int size = currentGap.getIntervals().size() + 1; //if there is a gap of 2, then you have 3 steps
                    if (firstValue != null && lastValue != null) {
                        Double stepSize = (lastValue - firstValue) / size;
                        Double currentValue = firstValue + stepSize;
                        for (CleanInterval currentInterval : currentGap.getIntervals()) {
                            JEVisSample sample = new VirtualSample(currentInterval.getDate(), currentValue);
                            String note = "";
                            note += getNote(currentInterval);
                            note += ",gap(Interpolation)";
                            sample.setNote(note);
                            currentValue += stepSize;
                            currentInterval.addTmpSample(sample);
                        }
                    }
                }
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    Double firstValue = currentLimitBreak.getFirstValue();
                    Double lastValue = currentLimitBreak.getLastValue();
                    int size = currentLimitBreak.getIntervals().size() + 1; //if there is a gap of 2, then you have 3 steps
                    if (firstValue != null && lastValue != null) {
                        Double stepSize = (lastValue - firstValue) / size;
                        Double currentValue = firstValue + stepSize;
                        for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                            JEVisSample sample = new VirtualSample(currentInterval.getDate(), currentValue);
                            String note = "";
                            note += getNote(currentInterval);
                            note += ",limit(Interpolation)";
                            sample.setNote(note);
                            currentValue += stepSize;
                            currentInterval.addTmpSample(sample);
                        }
                    }
                }
                break;
        }
    }

    public void fillDefault() throws Exception {
        Double defaultValue = Double.valueOf(c.getDefaultvalue());
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    for (CleanInterval currentInterval : currentGap.getIntervals()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), defaultValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",gap(Default)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), defaultValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Default)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
                break;
        }
    }

    public void fillMinimum() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    for (CleanInterval currentInterval : currentGap.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",gap(Minimum)";
                        sample.setNote(note);
                    }
                }
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Minimum)";
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillStatic() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    Double firstValue = currentGap.getFirstValue();
                    for (CleanInterval currentInterval : currentGap.getIntervals()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",gap(Static)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    Double firstValue = currentLimitBreak.getFirstValue();
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        JEVisSample sample = new VirtualSample(currentInterval.getDate(), firstValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += ",limit(Static)";
                        sample.setNote(note);
                        currentInterval.addTmpSample(sample);
                    }
                }
                break;
        }
    }

    private DateTime getFirstDate(DateTime lastDate) {
        final String referencePeriod = c.getReferenceperiod();
        Integer referencePeriodCount = Integer.parseInt(c.getReferenceperiodcount());
        switch (referencePeriod) {
            case (JEDataProcessorConstants.GapFillingReferencePeriod.DAY):
                return lastDate.minusDays(referencePeriodCount);
            case (JEDataProcessorConstants.GapFillingReferencePeriod.WEEK):
                return lastDate.minusWeeks(referencePeriodCount);
            case (JEDataProcessorConstants.GapFillingReferencePeriod.MONTH):
                return lastDate.minusMonths(referencePeriodCount);
            case (JEDataProcessorConstants.GapFillingReferencePeriod.YEAR):
                return lastDate.minusYears(referencePeriodCount);
            default:
                return lastDate.minusMonths(referencePeriodCount);
        }
    }

    public enum GapsAndLimitsType {
        LIMITS_TYPE, GAPS_TYPE
    }
}
