/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.Gap;
import org.jevis.commons.dataprocessing.processor.GapsAndLimits;
import org.jevis.commons.dataprocessing.processor.workflow.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author gschutz
 */
public class FillGapStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(FillGapStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        if (!cleanDataObject.getIsPeriodAligned() || !cleanDataObject.getGapFillingEnabled() || cleanDataObject.getGapFillingConfig().isEmpty()) {
            //no gap filling when there is no alignment or disabled or no config
            return;
        }
        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<DifferentialRule> differentialRules = cleanDataObject.getDifferentialRules();

        WorkDays workDays = new WorkDays(cleanDataObject.getCleanObject());
        DateTimeZone timeZone = workDays.getDateTimeZone();

        //check if intervals start before raw samples in case of current gap
        if (!intervals.isEmpty() && !rawSamples.isEmpty()
                &&
                (intervals.get(0).getDate().equals(rawSamples.get(0).getTimestamp()) ||
                        intervals.get(0).getDate().isBefore(rawSamples.get(0).getTimestamp()))) {
            logger.debug("detected possible current gap, increasing raws ample cache");
            List<PeriodRule> rawDataPeriodAlignment = cleanDataObject.getRawDataPeriodAlignment();
            JEVisAttribute rawAttribute = cleanDataObject.getRawAttribute();
            DateTime firstIntervalDate = intervals.get(0).getDate();
            DateTime firstRawSampleDate = rawSamples.get(0).getTimestamp();
            DateTime currentDate = firstRawSampleDate;
            int maxGapCount = 10000;
            int i = 0;

            while (i < maxGapCount &&
                    (firstIntervalDate.equals(firstRawSampleDate) || firstIntervalDate.isBefore(firstRawSampleDate))) {
                i++;
                Period periodForDate = CleanDataObject.getPeriodForDate(rawDataPeriodAlignment, firstRawSampleDate);
                DateTime newStart = currentDate.minus(periodForDate);
                List<JEVisSample> samples = rawAttribute.getSamples(newStart, currentDate);
                if (!samples.isEmpty()) {
                    currentDate = newStart;
                    List<JEVisSample> filteredList = new ArrayList<>();
                    for (JEVisSample sample : samples) {
                        boolean tsExists = false;
                        for (JEVisSample rawSample : rawSamples) {
                            if (rawSample.getTimestamp().equals(sample.getTimestamp())) {
                                tsExists = true;
                                break;
                            }
                        }
                        if (!tsExists) {
                            filteredList.add(sample);
                            firstRawSampleDate = sample.getTimestamp();
                        }
                    }
                    rawSamples.addAll(filteredList);
                } else {
                    currentDate = newStart;
                }
            }

            rawSamples.sort(Comparator.comparing(jeVisSample -> {
                try {
                    return jeVisSample.getTimestamp();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
                return null;
            }));
        }

        //identify gaps, gaps holds intervals
        List<Gap> gaps = identifyGaps(rawSamples, cleanDataObject, differentialRules, timeZone);
        logger.info("{} gaps identified", gaps.size());
        if (gaps.isEmpty()) { //no gap filling when there are no gaps
            return;
        }

        List<JsonGapFillingConfig> conf = cleanDataObject.getGapFillingConfig();

        if (Objects.nonNull(conf)) {
            if (!conf.isEmpty()) {
                List<JEVisSample> sampleCache = resourceManager.getSampleCache();

                List<Gap> doneGaps = new ArrayList<>();
                for (JsonGapFillingConfig c : conf) {
                    List<Gap> newGaps = new ArrayList<>();
                    for (Gap g : gaps) {
                        if (!doneGaps.contains(g)) {
                            logger.debug("[{}] start filling with Mode for {}", cleanDataObject.getCleanObject().getID(), c.getType());
                            DateTime firstDate = g.getMissingDateTimes().get(0);
                            DateTime lastDate = g.getMissingDateTimes().get(g.getMissingDateTimes().size() - 1);
                            if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                                newGaps.add(g);
                                doneGaps.add(g);
                            }
                        }
                    }

                    if (newGaps.isEmpty()) {
                        logger.debug("No gaps in this interval.");
                        continue;
                    } else {
                        logger.debug("[{}] Start Gap filling, mode: '{}' gap size: {}", resourceManager.getID(), c.getType(), newGaps.size());
                    }

                    GapsAndLimits gal = new GapsAndLimits(null, rawSamples, GapsAndLimits.GapsAndLimitsType.GAPS_TYPE,
                            c, newGaps, new ArrayList<>(), sampleCache, cleanDataObject);

                    switch (GapFillingType.parse(c.getType())) {
                        case STATIC:
                            gal.fillStatic();
                            break;
                        case INTERPOLATION:
                            gal.fillInterpolation();
                            break;
                        case DEFAULT_VALUE:
                            gal.fillDefault();
                            break;
                        case MINIMUM:
                            gal.fillMinimum();
                            break;
                        case MAXIMUM:
                            gal.fillMaximum();
                            break;
                        case MEDIAN:
                            gal.fillMedian();
                            break;
                        case AVERAGE:
                            gal.fillAverage();
                            break;
                        case DELETE:
                            gal.fillDelete();
                            break;
                        case NONE:
                        default:
                            break;
                    }
                    logger.debug("[{}:{}] Gap Filling Done", cleanDataObject.getCleanObject().getName(), resourceManager.getID());
                    rawSamples.sort(Comparator.comparing(jeVisSample -> {
                        try {
                            return jeVisSample.getTimestamp();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }));
                }
                if (gaps.size() != doneGaps.size()) {
                    logger.error("Could not complete all gaps. Gap may have been too long for reasonable gap filling on object {}:{}", cleanDataObject.getCleanObject().getName(), cleanDataObject.getCleanObject().getID());
                }
            } else {
                logger.error("[{}] Found gap but missing GapFillingConfig", resourceManager.getID());
            }

        }
    }

    private Long defaultValue(String s) {
        long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<Gap> identifyGaps(List<JEVisSample> rawSamples, CleanDataObject cleanDataObject, List<DifferentialRule> differentialRules, DateTimeZone timeZone) throws Exception {
        List<Gap> gaps = new ArrayList<>();

        Double lastValue = cleanDataObject.getLastCleanValue();
        DateTime expectedDateTime = null;
        DateTime lastDateTime = null;
        String lastNote = null;


        for (JEVisSample rawSample : rawSamples) {
            int i = rawSamples.indexOf(rawSample);
            DateTime rawSampleTSUTC = rawSample.getTimestamp();
            DateTime rawSampleTS = rawSampleTSUTC.withZone(timeZone);

            Period periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), rawSampleTSUTC);
            if (i < rawSamples.size() - 1) {
                periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), rawSamples.get(i + 1).getTimestamp());
            }

            // For Async Data
            if (periodForDate.equals(Period.ZERO)) {
                logger.error("No raw and no clean data period, gap filling not possible");
                break;
            }

            if (i == 0) {
                expectedDateTime = PeriodHelper.getNextPeriod(rawSampleTS, periodForDate, 1, true, timeZone).withZone(timeZone);
                lastValue = rawSample.getValueAsDouble();
                lastNote = rawSample.getNote();
                continue;
            } else if (rawSampleTS.equals(expectedDateTime)) {
                lastValue = rawSample.getValueAsDouble();
                lastNote = rawSample.getNote();
            } else {
                Gap currentGap = new Gap();
                currentGap.setFirstValue(lastValue);
                currentGap.setLastValue(rawSample.getValueAsDouble());
                currentGap.setStartNote(lastNote);

                while (expectedDateTime != null && expectedDateTime.isBefore(rawSampleTS)) {
                    currentGap.addDateTime(expectedDateTime.withZone(DateTimeZone.UTC));
                    periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), expectedDateTime);
                    if (i < rawSamples.size() - 1) {
                        periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), rawSamples.get(i + 1).getTimestamp());
                    }
                    expectedDateTime = PeriodHelper.getNextPeriod(expectedDateTime, periodForDate, 1, true, timeZone).withZone(timeZone);

                    if (periodForDate.equals(Period.ZERO)) {
                        logger.error("No raw and no clean data period, gap filling not possible");
                        break;
                    }
                }

                gaps.add(currentGap);

                lastValue = rawSample.getValueAsDouble();
                lastNote = rawSample.getNote();
            }

            if (expectedDateTime != null) {
                expectedDateTime = PeriodHelper.getNextPeriod(expectedDateTime, periodForDate, 1, true, timeZone).withZone(timeZone);
            }
        }

        List<Gap> filteredGaps = new ArrayList<>();
        for (Gap gap : gaps) {
            List<DateTime> missingDateTimes = gap.getMissingDateTimes();
            Double firstGapValue = gap.getFirstValue();
            Double lastGapValue = gap.getLastValue();
            if (!missingDateTimes.isEmpty() && firstGapValue != null && lastGapValue != null) {
                filteredGaps.add(gap);
            }
        }
        return filteredGaps;
    }

}
