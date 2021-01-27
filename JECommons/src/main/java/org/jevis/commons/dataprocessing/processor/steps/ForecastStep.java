package org.jevis.commons.dataprocessing.processor.steps;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.GapFillingType.parse;
import static org.jevis.commons.dataprocessing.processor.GapsAndLimits.getNote;

public class ForecastStep implements ProcessStep {
    @Override
    public void run(ResourceManager resourceManager) throws Exception {

        ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> sampleCache = forecastDataObject.getSampleCache();

        if (forecastDataObject.getTypeAttribute().hasSample()) {
            String type = forecastDataObject.getTypeAttribute().getLatestSample().getValueAsString();
            JsonGapFillingConfig c = forecastDataObject.getJsonGapFillingConfig();
            int lastIndex = 0;
            switch (parse(type)) {
                case MINIMUM:
                    lastIndex = intervals.size() - 1;
                    for (CleanInterval currentInterval : intervals) {
                        int index = intervals.indexOf(currentInterval);
                        Double value = getSpecificValue(sampleCache, intervals, c, currentInterval.getDate());
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);

                        if (index == 0) {
                            note += "," + NoteConstants.Forecast.FORECAST_1 + NoteConstants.Forecast.FORECAST_MIN;
                        } else if (index == lastIndex) {
                            note += "," + NoteConstants.Forecast.FORECAST_2 + NoteConstants.Forecast.FORECAST_MIN;
                        } else {
                            note += "," + NoteConstants.Forecast.FORECAST + NoteConstants.Forecast.FORECAST_MIN;
                        }

                        sample.setNote(note);
                    }
                    break;
                case MAXIMUM:
                    lastIndex = intervals.size() - 1;
                    for (CleanInterval currentInterval : intervals) {
                        int index = intervals.indexOf(currentInterval);
                        Double value = getSpecificValue(sampleCache, intervals, c, currentInterval.getDate());
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);

                        String note = "";
                        note += getNote(currentInterval);

                        if (index == 0) {
                            note += "," + NoteConstants.Forecast.FORECAST_1 + NoteConstants.Forecast.FORECAST_MAX;
                        } else if (index == lastIndex) {
                            note += "," + NoteConstants.Forecast.FORECAST_2 + NoteConstants.Forecast.FORECAST_MAX;
                        } else {
                            note += "," + NoteConstants.Forecast.FORECAST + NoteConstants.Forecast.FORECAST_MAX;
                        }

                        sample.setNote(note);
                    }
                    break;
                case MEDIAN:
                    lastIndex = intervals.size() - 1;
                    for (CleanInterval currentInterval : intervals) {
                        int index = intervals.indexOf(currentInterval);
                        Double value = getSpecificValue(sampleCache, intervals, c, currentInterval.getDate());
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);

                        if (index == 0) {
                            note += "," + NoteConstants.Forecast.FORECAST_1 + NoteConstants.Forecast.FORECAST_MEDIAN;
                        } else if (index == lastIndex) {
                            note += "," + NoteConstants.Forecast.FORECAST_2 + NoteConstants.Forecast.FORECAST_MEDIAN;
                        } else {
                            note += "," + NoteConstants.Forecast.FORECAST + NoteConstants.Forecast.FORECAST_MEDIAN;
                        }

                        sample.setNote(note);
                    }
                    break;
                case AVERAGE:
                    lastIndex = intervals.size() - 1;
                    for (CleanInterval currentInterval : intervals) {
                        int index = intervals.indexOf(currentInterval);
                        Double value = getSpecificValue(sampleCache, intervals, c, currentInterval.getDate());
                        VirtualSample sample = currentInterval.getResult();
                        sample.setTimeStamp(currentInterval.getDate());
                        sample.setValue(value);
                        String note = "";
                        note += getNote(currentInterval);

                        if (index == 0) {
                            note += "," + NoteConstants.Forecast.FORECAST_1 + NoteConstants.Forecast.FORECAST_AVERAGE;
                        } else if (index == lastIndex) {
                            note += "," + NoteConstants.Forecast.FORECAST_2 + NoteConstants.Forecast.FORECAST_AVERAGE;
                        } else {
                            note += "," + NoteConstants.Forecast.FORECAST + NoteConstants.Forecast.FORECAST_AVERAGE;
                        }

                        sample.setNote(note);
                    }
                    break;
            }
        }

    }

    private DateTime getFirstDate(List<JEVisSample> sampleCache, JsonGapFillingConfig c, DateTime lastDate) {
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

    private Double getSpecificValue(List<JEVisSample> sampleCache, List<CleanInterval> intervals, JsonGapFillingConfig c, DateTime lastDate) throws JEVisException {

        GapFillingBoundToSpecific bindToSpecificValue = GapFillingBoundToSpecific.parse(c.getBindtospecific());
        if (Objects.isNull(bindToSpecificValue)) bindToSpecificValue = GapFillingBoundToSpecific.NONE;
        List<JEVisSample> boundListSamples = new ArrayList<>();
        DateTime firstDate;

        firstDate = getFirstDate(sampleCache, c, lastDate);
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
                return calcValueWithType(boundListSamples, c);
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
                return calcValueWithType(boundListSamples, c);
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
                return calcValueWithType(boundListSamples, c);
            default:
                if (sampleCache != null && !sampleCache.isEmpty()) {
                    for (JEVisSample sample : sampleCache) {
                        if ((sample.getTimestamp().getHourOfDay() == lastDate.getHourOfDay()) && (sample.getTimestamp().getMinuteOfHour() == lastDate.getMinuteOfHour())) {
                            listSamplesNew.add(sample);
                        }
                    }
                }
                return calcValueWithType(listSamplesNew, c);
        }
    }

    private Double calcValueWithType(List<JEVisSample> listSamples, JsonGapFillingConfig c) throws
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
}
