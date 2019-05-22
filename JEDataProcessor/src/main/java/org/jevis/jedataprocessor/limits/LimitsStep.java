/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.limits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
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
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LimitsStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(LimitsStep.class);
    private List<JEVisSample> sampleCache;

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        if (!cleanDataObject.getIsPeriodAligned() || !cleanDataObject.getLimitsEnabled() || cleanDataObject.getLimitsConfig().isEmpty()) {
            //no limits check when there is no alignment or disabled or no config
            return;
        }
        List<CleanInterval> intervals = resourceManager.getIntervals();
        JEVisAttribute cleanAttribute = cleanDataObject.getValueAttribute();
        Double firstValue = 0.0;
        if (cleanAttribute != null) {
            if (cleanAttribute.hasSample()) {
                JEVisSample firstSample = cleanAttribute.getLatestSample();
                if (firstSample != null) {
                    firstValue = firstSample.getValueAsDouble();
                }
            }
        }

        List<JsonLimitsConfig> confLimitsStep1 = new ArrayList<>();
        List<JsonLimitsConfig> confLimitsStep2 = new ArrayList<>();
        if (cleanDataObject.getLimitsConfig().size() > 0) {
            confLimitsStep1.add(cleanDataObject.getLimitsConfig().get(0));
        }
        if (cleanDataObject.getLimitsConfig().size() > 1) {
            confLimitsStep2.add(cleanDataObject.getLimitsConfig().get(1));
        }

        List<JsonGapFillingConfig> confGaps = cleanDataObject.getGapFillingConfig();

        //identify limit breaking intervals
        List<LimitBreak> limitBreaksStep1 = identifyLimitBreaks(intervals, confLimitsStep1, firstValue);
        List<LimitBreak> limitBreaksStep2 = identifyLimitBreaks(intervals, confLimitsStep2, firstValue);

        if (limitBreaksStep1.isEmpty() && limitBreaksStep2.isEmpty()) { //no limit checks when there is no alignment
            logger.info("No limit breaks identified.");
            return;
        }
        logger.info("{} limit breaks for step 1 identified", limitBreaksStep1.size());
        logger.info("{} limit breaks for step 2 identified", limitBreaksStep2.size());

        try {
            DateTime minDateForCache = cleanDataObject.getFirstDate().minusMonths(6);
            DateTime lastDateForCache = cleanDataObject.getFirstDate();

            sampleCache = cleanDataObject.getCleanObject().getAttribute(CleanDataObject.CLASS_NAME).getSamples(minDateForCache, lastDateForCache);
        } catch (Exception e) {
            logger.info("No caching possible: " + e);
        }
        for (JsonLimitsConfig limitsConfig : cleanDataObject.getLimitsConfig()) {
            if (cleanDataObject.getLimitsConfig().indexOf(limitsConfig) == 0) {
                for (LimitBreak limitBreak : limitBreaksStep1) {
                    for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                        logger.info("start marking peculiar samples");
                        for (JEVisSample smp : currentInterval.getTmpSamples()) {
                            String note = "";
                            note += smp.getNote();
                            note += "," + NoteConstants.Limits.LIMIT_STEP1;
                            smp.setNote(note);
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
                                if (!filledLimitBreaks.contains(lb)) {
                                    logger.info("[{}] start filling with Mode for {}", cleanDataObject.getCleanObject().getID(), c.getType());
                                    DateTime firstDate = lb.getIntervals().get(0).getDate();
                                    DateTime lastDate = lb.getIntervals().get(lb.getIntervals().size() - 1).getDate();
                                    if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                                        newLimitBreaks.add(lb);
                                        filledLimitBreaks.add(lb);
                                    }
                                }
                            }

                            GapsAndLimits gal = new GapsAndLimits(intervals, GapsAndLimits.GapsAndLimitsType.LIMITS_TYPE,
                                    c, new ArrayList<>(), newLimitBreaks, sampleCache);

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
                            logger.info("[{}] Done", resourceManager.getID());
                        }
                        if (limitBreaksStep2.size() != filledLimitBreaks.size())
                            logger.error("Could not complete all limit breaks. Limit break may have been too long for reasonable gap filling.");
                    } else {
                        logger.error("[{}] Found limit break but missing GapFillingConfig", resourceManager.getID());
                    }
                }
            }
        }
        sampleCache = null;
        logger.info("[{}] finished filling gaps", cleanDataObject.getCleanObject().getID());

    }

    private Long defaultValue(String s) {
        long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<LimitBreak> identifyLimitBreaks(List<CleanInterval> intervals, List<JsonLimitsConfig> conf, Double firstValue) throws Exception {

        List<LimitBreak> limitBreaks = new ArrayList<>();
        LimitBreak currentLimitBreak = null;
        CleanInterval lastInterval = null;
        for (CleanInterval currentInterval : intervals) {
            for (JsonLimitsConfig lc : conf) {
                if (lc.getMin() != null && !lc.getMin().equals("") && lc.getMax() != null && !lc.getMax().equals("")) {
                    Double min = Double.parseDouble(lc.getMin());
                    Double max = Double.parseDouble(lc.getMax());
                    for (JEVisSample sample : currentInterval.getTmpSamples()) {

                        if (sample == null || sample.getValueAsDouble() == null) {
                            logger.error("- Limits Sample: {} min: {} max: {}" + sample, min, max);
                            throw new Exception("Error in identifyLimitBreaks, empty value in interval: " + currentInterval.getInterval());
                        }

                        Double sampleValue = sample.getValueAsDouble();

                        if (sample.getValueAsDouble() < min || sample.getValueAsDouble() > max) {
                            if (currentLimitBreak == null) {
                                currentLimitBreak = new LimitBreak();
                                if (lastInterval != null && !lastInterval.getTmpSamples().isEmpty() && Objects.nonNull(lastInterval.getTmpSamples().get(0)))
                                    currentLimitBreak.setFirstValue(lastInterval.getTmpSamples().get(0).getValueAsDouble());
                                else currentLimitBreak.setFirstValue(firstValue);
                                currentLimitBreak.addInterval(currentInterval);
                                MinOrMax limit = null;
                                if (sampleValue < min) limit = MinOrMax.MIN;
                                if (sampleValue > max) limit = MinOrMax.MAX;
                                currentLimitBreak.setMinOrMax(limit);
                            } else {
                                currentLimitBreak.addInterval(currentInterval);
                            }
                        } else {
                            if (currentLimitBreak != null) {
                                logger.info("Limit Break on: " + currentLimitBreak.getIntervals().get(0).getDate() + " to: " +
                                        currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1).getDate());
                                currentLimitBreak.setLastValue(sampleValue);
                                limitBreaks.add(currentLimitBreak);
                                currentLimitBreak = null;
                            }
                        }
                    }
                }
            }
            lastInterval = currentInterval;
        }

        if (!limitBreaks.contains(currentLimitBreak)) {
            if (currentLimitBreak != null) {
                if (currentLimitBreak.getIntervals().size() > 0) {
                    CleanInterval last = currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1);
                    if (last.getTmpSamples().size() > 0) {
                        JEVisSample lastSample = last.getTmpSamples().get(last.getTmpSamples().size() - 1);

                        currentLimitBreak.setLastValue(lastSample.getValueAsDouble());
                        limitBreaks.add(currentLimitBreak);
                    }
                }
            }
        }

        return limitBreaks;
    }
}
