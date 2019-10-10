/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.gap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.util.GapsAndLimits;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.JEDataProcessorConstants.GapFillingType;

/**
 * @author broder
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
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> listConversionToDifferential = cleanDataObject.getConversionDifferential();

        //identify gaps, gaps holds intervals
        List<Gap> gaps = identifyGaps(intervals, cleanDataObject, listConversionToDifferential);
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
                            logger.info("[{}] start filling with Mode for {}", cleanDataObject.getCleanObject().getID(), c.getType());
                            DateTime firstDate = g.getIntervals().get(0).getDate();
                            DateTime lastDate = g.getIntervals().get(g.getIntervals().size() - 1).getDate();
                            if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                                newGaps.add(g);
                                doneGaps.add(g);
                            }
                        }
                    }

                    if (newGaps.size() == 0) {
                        logger.error("No gaps in this interval.");
                        continue;
                    } else
                        logger.info("[{}] Start Gap filling, mode: '{}' gap size: {}", resourceManager.getID(), c.getType(), newGaps.size());

                    GapsAndLimits gal = new GapsAndLimits(intervals, GapsAndLimits.GapsAndLimitsType.GAPS_TYPE,
                            c, newGaps, new ArrayList<>(), sampleCache);

                    switch (c.getType()) {
                        case GapFillingType.NONE:
                            break;
                        case GapFillingType.STATIC:
                            gal.fillStatic();
                            break;
                        case GapFillingType.INTERPOLATION:
                            gal.fillInterpolation();
                            break;
                        case GapFillingType.DEFAULT_VALUE:
                            gal.fillDefault();
                            break;
                        case GapFillingType.MINIMUM:
                            gal.fillMinimum();
                            break;
                        case GapFillingType.MAXIMUM:
                            gal.fillMaximum();
                            break;
                        case GapFillingType.MEDIAN:
                            gal.fillMedian();
                            break;
                        case GapFillingType.AVERAGE:
                            gal.fillAverage();
                            break;
                        default:
                            break;
                    }
                    gal.clearLists();
                    logger.info("[{}:{}] Gap Filling Done", cleanDataObject.getCleanObject().getName(), resourceManager.getID());
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

    private List<Gap> identifyGaps(List<CleanInterval> intervals, CleanDataObject calcAttribute, List<JEVisSample> listConversionToDifferential) throws Exception {
        List<Gap> gaps = new ArrayList<>();
        Gap currentGap = null;
        Double lastValue = calcAttribute.getLastCleanValue();
        for (CleanInterval currentInterval : intervals) {
            for (int i = 0; i < listConversionToDifferential.size(); i++) {
                JEVisSample cd = listConversionToDifferential.get(i);

                DateTime timeStampOfConversion = cd.getTimestamp();

                DateTime nextTimeStampOfConversion = null;
                Boolean conversionToDifferential = cd.getValueAsBoolean();
                if (listConversionToDifferential.size() > (i + 1)) {
                    nextTimeStampOfConversion = (listConversionToDifferential.get(i + 1)).getTimestamp();
                }
                if (!conversionToDifferential) {
                    if (currentInterval.getDate().equals(timeStampOfConversion)
                            || currentInterval.getDate().isAfter(timeStampOfConversion)
                            && ((nextTimeStampOfConversion == null) || currentInterval.getDate().isBefore(nextTimeStampOfConversion))) {
                        if (currentInterval.getTmpSamples().isEmpty()) { //could be the start of the gap or in a gap
                            if (currentGap != null) {//current in a gap
                                currentGap.addInterval(currentInterval);
                            } else { //start of a gap
                                currentGap = new Gap();
                                currentGap.addInterval(currentInterval);
                                currentGap.setFirstValue(lastValue);
                            }
                        } else { //could be the end of the gap or no gap
                            for (JEVisSample sample : currentInterval.getTmpSamples()) {

                                Double rawValue = sample.getValueAsDouble();
                                if (currentGap != null) { //end of the gap
                                    currentGap.addInterval(currentInterval);
                                    currentGap.setLastValue(rawValue);
                                    gaps.add(currentGap);
                                    currentGap = null;
                                    lastValue = sample.getValueAsDouble();
                                } else { //not in a gap
                                    lastValue = sample.getValueAsDouble();
                                }

                            }
                        }
                    }
                } else {
                    if (currentInterval.getDate().equals(timeStampOfConversion)
                            || currentInterval.getDate().isAfter(timeStampOfConversion)
                            && ((nextTimeStampOfConversion == null) || currentInterval.getDate().isBefore(nextTimeStampOfConversion))) {
                        if (currentInterval.getTmpSamples().isEmpty()) { //could be the start of the gap or in a gap
                            if (currentGap != null) {//current in a gap
                                currentGap.addInterval(currentInterval);
                            } else { //start of a gap
                                currentGap = new Gap();
                                currentGap.addInterval(currentInterval);
                                currentGap.setFirstValue(lastValue);
                            }
                        } else { //could be the end of the gap or no gap
                            for (JEVisSample sample : currentInterval.getTmpSamples()) {

                                int currentIntervalIndex = intervals.indexOf(currentInterval);
                                if (currentIntervalIndex < intervals.size() - 2) {
                                    CleanInterval nextInterval = intervals.get(currentIntervalIndex + 1);
                                    if (currentGap != null && nextInterval != null && !nextInterval.getTmpSamples().isEmpty()) { //end of the gap
                                        currentGap.addInterval(currentInterval);
                                        Double raw2Value = nextInterval.getTmpSamples().get(nextInterval.getTmpSamples().size() - 1).getValueAsDouble();
                                        currentGap.setLastValue(raw2Value);
                                        gaps.add(currentGap);
                                        currentGap = null;
                                        lastValue = sample.getValueAsDouble();
                                    } else if (nextInterval != null && !nextInterval.getTmpSamples().isEmpty()) { //not in a gap
                                        lastValue = sample.getValueAsDouble();
                                    }
                                }
                            }
                        }
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

}
