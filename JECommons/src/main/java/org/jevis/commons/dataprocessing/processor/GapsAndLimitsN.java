package org.jevis.commons.dataprocessing.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.limits.LimitBreakN;
import org.jevis.commons.dataprocessing.processor.workflow.CleanIntervalN;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GapsAndLimitsN {
    private static final Logger logger = LogManager.getLogger(GapsAndLimitsN.class);
    private final List<JEVisSample> rawSamples;
    private final List<CleanIntervalN> intervals;
    private final GapsAndLimitsTypeN gapsAndLimitsType;
    private final List<GapN> gapList;
    private final List<LimitBreakN> limitBreaksList;
    private final JsonGapFillingConfig c;
    private final List<JEVisSample> sampleCache;

    public GapsAndLimitsN(List<CleanIntervalN> intervals, List<JEVisSample> rawSamples, GapsAndLimitsTypeN type,
                          JsonGapFillingConfig c, List<GapN> gapList, List<LimitBreakN> limitBreaksList, List<JEVisSample> sampleCache) {
        this.rawSamples = rawSamples;
        this.intervals = intervals;
        this.gapsAndLimitsType = type;
        this.gapList = gapList;
        this.limitBreaksList = limitBreaksList;
        this.c = c;
        this.sampleCache = sampleCache;
    }

    public static String getNote(CleanIntervalN currentInterval) {
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
        List<JEVisSample> listSamplesNew = new ArrayList<>();
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
                }
                for (JEVisSample jeVisSample : rawSamples) {
                    if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                            || jeVisSample.getTimestamp().equals(lastDate)) {
                        if (jeVisSample.getTimestamp().getDayOfWeek() == lastDate.getDayOfWeek()) {
                            if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(jeVisSample);
                            }
                        }
                    }
                }
                return calcValueWithType(boundListSamples);
            case WEEKOFYEAR:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if (sample.getTimestamp().getWeekyear() == lastDate.getWeekyear()) {
                            if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(sample);
                            }
                        }
                    }
                }
                for (JEVisSample jeVisSample : rawSamples) {
                    if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                            || jeVisSample.getTimestamp().equals(lastDate)) {
                        if (jeVisSample.getTimestamp().getWeekyear() == lastDate.getWeekyear()) {
                            if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(jeVisSample);
                            }
                        }
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
                }
                for (JEVisSample jeVisSample : rawSamples) {
                    if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                            || jeVisSample.getTimestamp().equals(lastDate)) {
                        if (jeVisSample.getTimestamp().getMonthOfYear() == lastDate.getMonthOfYear()) {
                            if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                                boundListSamples.add(jeVisSample);
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
                for (JEVisSample jeVisSample : rawSamples) {
                    if (jeVisSample.getTimestamp().equals(firstDate) || (jeVisSample.getTimestamp().isAfter(firstDate) && jeVisSample.getTimestamp().isBefore(lastDate))
                            || jeVisSample.getTimestamp().equals(lastDate)) {
                        if ((jeVisSample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (jeVisSample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                            listSamplesNew.add(jeVisSample);
                        }
                    }
                }
                return calcValueWithType(listSamplesNew);
        }
    }

    private Double calcValueWithType(List<JEVisSample> listSamples) throws
            JEVisException {
        final GapFillingType gapFillingType = GapFillingType.parse(c.getType());

        if (Objects.nonNull(listSamples) && !listSamples.isEmpty()) {
            switch (gapFillingType) {
                case MINIMUM:
                    Double minValue = listSamples.get(0).getValueAsDouble();
                    for (JEVisSample sample : listSamples) {
                        if (sample.getValueAsDouble() != null) {
                            minValue = Math.min(minValue, sample.getValueAsDouble());
                        }
                    }
                    return minValue;
                case MAXIMUM:
                    Double maxValue = listSamples.get(0).getValueAsDouble();
                    for (JEVisSample sample : listSamples) {
                        if (sample.getValueAsDouble() != null) {
                            if (sample.getValueAsDouble() != null) {
                                maxValue = Math.max(maxValue, sample.getValueAsDouble());
                            }
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
                for (GapN currentGap : gapList) {
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        Double value = getSpecificValue(dateTime);
                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_MAX;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
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
                        note += "," + NoteConstants.Limits.LIMIT_MAX;
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillMedian() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (GapN currentGap : gapList) {
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        Double value = getSpecificValue(dateTime);
                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_MEDIAN;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
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
                        note += "," + NoteConstants.Limits.LIMIT_MEDIAN;
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
                for (GapN currentGap : gapList) {
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        Double value = getSpecificValue(dateTime);
                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_AVERAGE;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
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
                        note += "," + NoteConstants.Limits.LIMIT_AVERAGE;
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
                    for (GapN currentGap : gapList) {
                        for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                            if (jeVisSample.getTimestamp().equals(dateTime)) {
                                tobeRemovedGaps.add(jeVisSample);
                            }
                        }
                    }
                }
                rawSamples.removeAll(tobeRemovedGaps);
                break;
            case LIMITS_TYPE:
                List<JEVisSample> tobeRemovedLimits = new ArrayList<>();
                for (JEVisSample jeVisSample : rawSamples) {
                    for (LimitBreakN limitBreak : limitBreaksList) {
                        for (CleanIntervalN cleanInterval1 : limitBreak.getIntervals()) {
                            if (jeVisSample.getTimestamp().equals(cleanInterval1.getDate())) {
                                tobeRemovedLimits.add(jeVisSample);
                            }
                        }
                    }
                }
                rawSamples.removeAll(tobeRemovedLimits);
                break;
        }
    }

    public void fillInterpolation() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (GapN currentGap : gapList) {
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
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    Double firstValue = currentLimitBreak.getFirstValue();
                    Double lastValue = currentLimitBreak.getLastValue();
                    int size = currentLimitBreak.getIntervals().size(); //if there is a gap of 2, then you have 3 steps
                    if (firstValue != null && lastValue != null) {
                        Double stepSize = (lastValue - firstValue) / size;
                        Double currentValue = firstValue;
                        for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
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
                            note += "," + NoteConstants.Limits.LIMIT_INTERPOLATION;
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
                for (GapN currentGap : gapList) {
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
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(defaultValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += "," + NoteConstants.Limits.LIMIT_DEFAULT;
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillMinimum() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (GapN currentGap : gapList) {
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        Double value = getSpecificValue(dateTime);
                        VirtualSample sample = new VirtualSample(dateTime, value);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_MIN;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
                        Double value = getSpecificValue(currentInterval.getDate());
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);
                        note += "," + NoteConstants.Limits.LIMIT_MIN;
                        sample.setNote(note);
                    }
                }
                break;
        }
    }

    public void fillStatic() throws Exception {
        switch (gapsAndLimitsType) {
            case GAPS_TYPE:
                for (GapN currentGap : gapList) {
                    Double firstValue = currentGap.getFirstValue();
                    for (DateTime dateTime : currentGap.getMissingDateTimes()) {
                        VirtualSample sample = new VirtualSample(dateTime, firstValue);
                        String note = "";
                        note += currentGap.getStartNote();
                        note += "," + NoteConstants.Gap.GAP_STATIC;
                        sample.setNote(note);
                        rawSamples.add(sample);
                    }
                }
                break;
            case LIMITS_TYPE:
                for (LimitBreakN currentLimitBreak : limitBreaksList) {
                    Double firstValue = currentLimitBreak.getFirstValue();
                    for (CleanIntervalN currentInterval : currentLimitBreak.getIntervals()) {
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(firstValue);
                        String note = "";
                        note += getNote(currentInterval);
                        note += "," + NoteConstants.Limits.LIMIT_STATIC;
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

    public enum GapsAndLimitsTypeN {
        LIMITS_TYPE, GAPS_TYPE, FORECAST_TYPE
    }
}
