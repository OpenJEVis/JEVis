package org.jevis.commons.dataprocessing.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.limits.LimitBreak;
import org.jevis.commons.dataprocessing.processor.steps.ScalingStep;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.DifferentialRule;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GapsAndLimits {
    private static final Logger logger = LogManager.getLogger(GapsAndLimits.class);
    private final CleanDataObject cleanDataObject;
    private final List<JEVisSample> rawSamples;
    private final List<CleanInterval> intervals;
    private final GapsAndLimitsType gapsAndLimitsType;
    private final List<Gap> gapList;
    private final List<LimitBreak> limitBreaksList;
    private final JsonGapFillingConfig c;
    private final List<JEVisSample> sampleCache;
    private final List<DifferentialRule> differentialRules;

    public GapsAndLimits(List<CleanInterval> intervals, List<JEVisSample> rawSamples, GapsAndLimitsType type,
                         JsonGapFillingConfig c, List<Gap> gapList, List<LimitBreak> limitBreaksList, List<JEVisSample> sampleCache, CleanDataObject cleanDataObject) {
        this.rawSamples = rawSamples;
        this.intervals = intervals;
        this.gapsAndLimitsType = type;
        this.gapList = gapList;
        this.limitBreaksList = limitBreaksList;
        this.c = c;
        this.sampleCache = sampleCache;
        this.cleanDataObject = cleanDataObject;
        this.differentialRules = cleanDataObject.getDifferentialRules();
    }

    public static String getNote(CleanInterval currentInterval) {
        try {
            return currentInterval.getResult().getNote();
        } catch (Exception e1) {
            try {
                return currentInterval.getRawSamples().get(0).getNote();
            } catch (Exception e2) {
                return "";
            }
        }
    }

    private Double getSpecificValue(DateTime lastDate) throws JEVisException {

        GapFillingBoundToSpecific bindToSpecificValue = GapFillingBoundToSpecific.parse(c.getBindtospecific());
        if (Objects.isNull(bindToSpecificValue)) bindToSpecificValue = GapFillingBoundToSpecific.NONE;
        List<JEVisSample> boundListSamples = new ArrayList<>();
        DateTime firstDate;

        firstDate = getFirstDate(lastDate);
        switch (bindToSpecificValue) {
            case WEEKDAY:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getDayOfWeek() == lastDate.getDayOfWeek()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                } else if (rawSamples != null) {
                    Double lastValue = rawSamples.get(0).getValueAsDouble();
                    for (JEVisSample jeVisSample : rawSamples) {
                        if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                                || jeVisSample.getTimestamp().equals(lastDate)) {
                            if (jeVisSample.getTimestamp().getDayOfWeek() == lastDate.getDayOfWeek()) {
                                if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                    if (!isDifferentialForDate(jeVisSample.getTimestamp())) {
                                        boundListSamples.add(jeVisSample);
                                    } else if (rawSamples.indexOf(jeVisSample) > 0 && lastValue != null && jeVisSample.getValueAsDouble() != null) {
                                        Double currentValue = jeVisSample.getValueAsDouble() - lastValue;
                                        currentValue = scaleValue(currentValue, jeVisSample.getTimestamp());

                                        VirtualSample virtualSample = new VirtualSample(jeVisSample.getTimestamp(), currentValue);
                                        virtualSample.setNote(jeVisSample.getNote());

                                        boundListSamples.add(virtualSample);
                                    }
                                }
                            }
                        }
                        lastValue = jeVisSample.getValueAsDouble();
                    }
                }
                return calcValueWithType(boundListSamples);
            case WEEKOFYEAR:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getWeekOfWeekyear() == lastDate.getWeekOfWeekyear()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                } else if (rawSamples != null) {
                    Double lastValue = rawSamples.get(0).getValueAsDouble();
                    for (JEVisSample jeVisSample : rawSamples) {
                        if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                                || jeVisSample.getTimestamp().equals(lastDate)) {
                            if (jeVisSample.getTimestamp().getWeekOfWeekyear() == lastDate.getWeekOfWeekyear()) {
                                if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                    if (!isDifferentialForDate(jeVisSample.getTimestamp())) {
                                        boundListSamples.add(jeVisSample);
                                    } else if (rawSamples.indexOf(jeVisSample) > 0 && lastValue != null && jeVisSample.getValueAsDouble() != null) {
                                        Double currentValue = jeVisSample.getValueAsDouble() - lastValue;
                                        currentValue = scaleValue(currentValue, jeVisSample.getTimestamp());

                                        VirtualSample virtualSample = new VirtualSample(jeVisSample.getTimestamp(), currentValue);
                                        virtualSample.setNote(jeVisSample.getNote());

                                        boundListSamples.add(virtualSample);
                                    }
                                }
                            }
                        }
                        lastValue = jeVisSample.getValueAsDouble();
                    }
                }
                return calcValueWithType(boundListSamples);
            case MONTHOFYEAR:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getMonthOfYear() == lastDate.getMonthOfYear()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                } else if (rawSamples != null) {
                    Double lastValue = rawSamples.get(0).getValueAsDouble();
                    for (JEVisSample jeVisSample : rawSamples) {
                        if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                                || jeVisSample.getTimestamp().equals(lastDate)) {
                            if (jeVisSample.getTimestamp().getMonthOfYear() == lastDate.getMonthOfYear()) {
                                if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                    if (!isDifferentialForDate(jeVisSample.getTimestamp())) {
                                        boundListSamples.add(jeVisSample);
                                    } else if (rawSamples.indexOf(jeVisSample) > 0 && lastValue != null && jeVisSample.getValueAsDouble() != null) {
                                        Double currentValue = jeVisSample.getValueAsDouble() - lastValue;
                                        currentValue = scaleValue(currentValue, jeVisSample.getTimestamp());

                                        VirtualSample virtualSample = new VirtualSample(jeVisSample.getTimestamp(), currentValue);
                                        virtualSample.setNote(jeVisSample.getNote());

                                        boundListSamples.add(virtualSample);
                                    }
                                }
                            }
                        }
                        lastValue = jeVisSample.getValueAsDouble();
                    }
                }
                return calcValueWithType(boundListSamples);
            default:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                        }
                    }
                } else if (rawSamples != null) {
                    Double lastValue = rawSamples.get(0).getValueAsDouble();
                    for (JEVisSample jeVisSample : rawSamples) {
                        if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                                || jeVisSample.getTimestamp().equals(lastDate)) {
                            if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                if (!isDifferentialForDate(jeVisSample.getTimestamp())) {
                                    boundListSamples.add(jeVisSample);
                                } else if (rawSamples.indexOf(jeVisSample) > 0 && lastValue != null && jeVisSample.getValueAsDouble() != null) {
                                    Double currentValue = jeVisSample.getValueAsDouble() - lastValue;
                                    currentValue = scaleValue(currentValue, jeVisSample.getTimestamp());

                                    VirtualSample virtualSample = new VirtualSample(jeVisSample.getTimestamp(), currentValue);
                                    virtualSample.setNote(jeVisSample.getNote());

                                    boundListSamples.add(virtualSample);
                                }
                            }
                        }
                        lastValue = jeVisSample.getValueAsDouble();
                    }
                }
                return calcValueWithType(boundListSamples);
        }
    }

    private boolean isDifferentialForDate(DateTime timestamp) {
        return CleanDataObject.isDifferentialForDate(differentialRules, timestamp);
    }

    private Double scaleValue(Double inputValue, DateTime dateTime) {
        BigDecimal offset = new BigDecimal(cleanDataObject.getOffset().toString());
        BigDecimal currentMulti = ScalingStep.getCurrentMultiplier(cleanDataObject.getMultiplier(), dateTime);
        BigDecimal rawValueDec = new BigDecimal(inputValue.toString());
        BigDecimal productDec = new BigDecimal(0);
        productDec = productDec.add(rawValueDec);
        productDec = productDec.multiply(currentMulti);
        productDec = productDec.add(offset);
        return productDec.doubleValue();
    }

    private Double scaleValueBack(Double inputValue, DateTime dateTime) {
        BigDecimal offset = new BigDecimal(cleanDataObject.getOffset().toString());
        BigDecimal currentMulti = ScalingStep.getCurrentMultiplier(cleanDataObject.getMultiplier(), dateTime);
        BigDecimal rawValueDec = new BigDecimal(inputValue.toString());
        BigDecimal divDec = new BigDecimal(0);
        divDec = divDec.add(rawValueDec);
        divDec = divDec.divide(currentMulti, RoundingMode.HALF_EVEN);
        divDec = divDec.add(offset);
        return divDec.doubleValue();
    }

    private Double calcValueWithType(List<JEVisSample> listSamples) throws
            JEVisException {
        final GapFillingType gapFillingType = GapFillingType.parse(c.getType());

        if (Objects.nonNull(listSamples) && !listSamples.isEmpty()) {
            switch (gapFillingType) {
                case MINIMUM:
                    Double minValue = null;
                    for (JEVisSample sample : listSamples) {
                        if (sample.getValueAsDouble() != null && minValue == null) {
                            minValue = sample.getValueAsDouble();
                        } else if (sample.getValueAsDouble() != null) {
                            minValue = Math.min(minValue, sample.getValueAsDouble());
                        }
                    }
                    return minValue;
                case MAXIMUM:
                    Double maxValue = null;
                    for (JEVisSample sample : listSamples) {
                        if (sample.getValueAsDouble() != null && maxValue == null) {
                            maxValue = sample.getValueAsDouble();
                        } else if (sample.getValueAsDouble() != null) {
                            maxValue = Math.max(maxValue, sample.getValueAsDouble());
                        }
                    }
                    return maxValue;
                case MEDIAN:
                    Double medianValue = 0d;
                    List<Double> sortedArray = new ArrayList<>();
                    for (JEVisSample sample : listSamples) {
                        if (sample.getValueAsDouble() != null) {
                            sortedArray.add(sample.getValueAsDouble());
                        }
                    }
                    Collections.sort(sortedArray);
                    if (!sortedArray.isEmpty()) {
                        if (sortedArray.size() > 2) medianValue = sortedArray.get(sortedArray.size() / 2);
                        else if (sortedArray.size() == 2) medianValue = (sortedArray.get(0) + sortedArray.get(1)) / 2;
                        else medianValue = sortedArray.get(0);
                    }

                    return medianValue;
                case AVERAGE:
                    double averageValue = 0d;
                    for (JEVisSample sample : listSamples) {
                        if (sample.getValueAsDouble() != null) {
                            averageValue += sample.getValueAsDouble();
                        }
                    }
                    //logger.info("sum: " + averageValue + " listSize: " + listSamples.size());
                    averageValue = averageValue / listSamples.size();
                    return averageValue;
                case DELETE:
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
                    Double value = currentGap.getFirstValue();
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        if (!isDifferentialForDate(dateTime)) {
                            value = scaleValueBack(getSpecificValue(dateTime), dateTime);
                        } else {
                            value += scaleValueBack(getSpecificValue(dateTime), dateTime);
                        }

                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_MAX;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());

                        if (currentLimitBreak.getMin() != null && value < currentLimitBreak.getMin()) {
                            value = currentLimitBreak.getMin();
                        }
                        if (currentLimitBreak.getMax() != null && value > currentLimitBreak.getMax()) {
                            value = currentLimitBreak.getMax();
                        }

                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);
                        if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                            note += "," + NoteConstants.Limits.LIMIT_MAX;
                        } else {
                            note += "," + NoteConstants.Deltas.DELTA_MAX;
                        }
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillMedian() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    Double value = currentGap.getFirstValue();
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        if (!isDifferentialForDate(dateTime)) {
                            value = scaleValueBack(getSpecificValue(dateTime), dateTime);
                        } else {
                            value += scaleValueBack(getSpecificValue(dateTime), dateTime);
                        }

                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_MEDIAN;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());

                        if (currentLimitBreak.getMin() != null && value < currentLimitBreak.getMin()) {
                            value = currentLimitBreak.getMin();
                        }
                        if (currentLimitBreak.getMax() != null && value > currentLimitBreak.getMax()) {
                            value = currentLimitBreak.getMax();
                        }

                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);
                        if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                            note += "," + NoteConstants.Limits.LIMIT_MEDIAN;
                        } else {
                            note += "," + NoteConstants.Deltas.DELTA_MEDIAN;
                        }
                        sample.setNote(note);
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
                    Double value = currentGap.getFirstValue();
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        Double specificValue = getSpecificValue(dateTime);
                        Double scaledValue = scaleValueBack(specificValue, dateTime);
                        if (!isDifferentialForDate(dateTime)) {
                            value = scaledValue;
                        } else {
                            value += scaledValue;
                        }

                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_AVERAGE;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());

                        if (currentLimitBreak.getMin() != null && value < currentLimitBreak.getMin()) {
                            value = currentLimitBreak.getMin();
                        }
                        if (currentLimitBreak.getMax() != null && value > currentLimitBreak.getMax()) {
                            value = currentLimitBreak.getMax();
                        }

                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);
                        if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                            note += "," + NoteConstants.Limits.LIMIT_AVERAGE;
                        } else {
                            note += "," + NoteConstants.Deltas.DELTA_AVERAGE;
                        }
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillDelete() throws JEVisException {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                List<JEVisSample> tobeRemovedGaps = new ArrayList<>();
                for (JEVisSample jeVisSample : rawSamples) {
                    for (Gap currentGap : gapList) {
                        for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                            if (jeVisSample.getTimestamp().equals(dateTime)) {
                                tobeRemovedGaps.add(jeVisSample);
                            }
                        }
                    }
                }
                rawSamples.removeAll(tobeRemovedGaps);
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                if (rawSamples != null) {
                    List<JEVisSample> tobeRemovedLimits = new ArrayList<>();
                    for (JEVisSample jeVisSample : rawSamples) {
                        for (LimitBreak limitBreak : limitBreaksList) {
                            for (CleanInterval cleanInterval1 : limitBreak.getIntervals()) {
                                if (jeVisSample.getTimestamp().equals(cleanInterval1.getDate())) {
                                    tobeRemovedLimits.add(jeVisSample);
                                }
                            }
                        }
                    }
                    rawSamples.removeAll(tobeRemovedLimits);
                }
                if (intervals != null) {
                    List<CleanInterval> tobeRemovedLimits = new ArrayList<>();
                    for (CleanInterval cleanInterval : intervals) {
                        for (LimitBreak limitBreak : limitBreaksList) {
                            for (CleanInterval cleanInterval1 : limitBreak.getIntervals()) {
                                if (cleanInterval.getResult().getTimestamp().equals(cleanInterval1.getDate())) {
                                    tobeRemovedLimits.add(cleanInterval);
                                }
                            }
                        }
                    }
                    intervals.removeAll(tobeRemovedLimits);
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
                    int size = currentGap.getMissingDateTimes().size() + 1; //if there is a gap of 2, then you have 3 steps
                    if (firstValue != null && lastValue != null) {
                        Double stepSize = (lastValue - firstValue) / size;
                        Double currentValue = firstValue + stepSize;
                        for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                            VirtualSample sample = new VirtualSample(dateTime, currentValue);
                            String note = "";
                            note += currentGap.getStartNote();
                            note += "," + NoteConstants.Gap.GAP_INTERPOLATION;
                            sample.setNote(note);
                            currentValue += stepSize;
                            rawSamples.add(sample);
                        }
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    Double firstValue = currentLimitBreak.getFirstValue();
                    Double lastValue = currentLimitBreak.getLastValue();
                    int size = currentLimitBreak.getIntervals().size() + 1; //if there is a gap of 2, then you have 3 steps
                    if (firstValue != null && lastValue != null) {
                        Double stepSize = (lastValue - firstValue) / size;
                        Double currentValue = firstValue + stepSize;
                        for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                            if (currentLimitBreak.getMin() != null && currentValue < currentLimitBreak.getMin()) {
                                currentValue = currentLimitBreak.getMin();
                            }
                            if (currentLimitBreak.getMax() != null && currentValue > currentLimitBreak.getMax()) {
                                currentValue = currentLimitBreak.getMax();
                            }

                            VirtualSample sample = currentInterval.getResult();
                            sample.setTimeStamp(currentInterval.getDate());
                            sample.setValue(currentValue);
                            String note = "";
                            note += getNote(currentInterval);
                            if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                                note += "," + NoteConstants.Limits.LIMIT_INTERPOLATION;
                            } else {
                                note += "," + NoteConstants.Deltas.DELTA_INTERPOLATION;
                            }
                            sample.setNote(note);
                            currentValue += stepSize;
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
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        VirtualSample sample = new VirtualSample(dateTime, defaultValue);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_DEFAULT;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(defaultValue);
                        String note = "";
                        note += getNote(currentInterval);
                        if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                            note += "," + NoteConstants.Limits.LIMIT_DEFAULT;
                        } else {
                            note += "," + NoteConstants.Deltas.DELTA_DEFAULT;
                        }
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillMinimum() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (Gap currentGap : gapList) {
                    Double value = currentGap.getFirstValue();
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        if (!isDifferentialForDate(dateTime)) {
                            value = scaleValueBack(getSpecificValue(dateTime), dateTime);
                        } else {
                            value += scaleValueBack(getSpecificValue(dateTime), dateTime);
                        }
                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_MIN;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);
                        if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                            note += "," + NoteConstants.Limits.LIMIT_MIN;
                        } else {
                            note += "," + NoteConstants.Deltas.DELTA_MIN;
                        }
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

                    Double addedValue = 0d;
                    if (isDifferentialForDate(currentGap.getMissingDateTimes().get(0))) {
                        Double lastValue = null;
                        for (JEVisSample jeVisSample : rawSamples) {
                            if (jeVisSample.getTimestamp().equals(currentGap.getMissingDateTimes().get(0))) {
                                lastValue = rawSamples.get(rawSamples.indexOf(jeVisSample) - 1).getValueAsDouble();
                                break;
                            }
                        }

                        if (lastValue != null) {
                            addedValue = firstValue - lastValue;
                        }
                    }

                    Double value = firstValue;
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {

                        if (!isDifferentialForDate(dateTime)) {
                            value = firstValue;
                        } else {
                            value += addedValue;
                        }

                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_STATIC;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case DELTA_TYPE:
            case LIMITS_TYPE:
                for (LimitBreak currentLimitBreak : limitBreaksList) {
                    Double firstValue = currentLimitBreak.getFirstValue();
                    for (CleanInterval currentInterval : currentLimitBreak.getIntervals()) {
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(firstValue);
                        String note = "";
                        note += getNote(currentInterval);
                        if (gapsAndLimitsType == GapsAndLimitsType.LIMITS_TYPE) {
                            note += "," + NoteConstants.Limits.LIMIT_STATIC;
                        } else {
                            note += "," + NoteConstants.Deltas.DELTA_STATIC;
                        }
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    private DateTime getFirstDate(DateTime lastDate) {
        final GapFillingReferencePeriod referencePeriod = GapFillingReferencePeriod.parse(c.getReferenceperiod());
        int referencePeriodCount = Integer.parseInt(c.getReferenceperiodcount());
        switch (referencePeriod) {
            case DAY:
                return lastDate.minusDays(referencePeriodCount);
            case WEEK:
                return lastDate.minusWeeks(referencePeriodCount);
            case MONTH:
                return lastDate.minusMonths(referencePeriodCount);
            case YEAR:
                return lastDate.minusYears(referencePeriodCount);
            case ALL:
                try {
                    return sampleCache.get(0).getTimestamp();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            default:
                return lastDate.minusMonths(referencePeriodCount);
        }
    }

    public enum GapsAndLimitsType {
        LIMITS_TYPE, GAPS_TYPE, FORECAST_TYPE, DELTA_TYPE
    }
}
