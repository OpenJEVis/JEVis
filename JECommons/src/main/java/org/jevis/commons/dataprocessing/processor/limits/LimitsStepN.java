/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.limits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.GapsAndLimitsN;
import org.jevis.commons.dataprocessing.processor.workflow.CleanIntervalN;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStepN;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManagerN;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.GapFillingType.parse;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class LimitsStepN implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(LimitsStepN.class);

    @Override
    public void run(ResourceManagerN resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        if (!cleanDataObject.getLimitsEnabled() || cleanDataObject.getLimitsConfig().isEmpty()) {
            //no limits check when disabled or no config
            return;
        }
        List<CleanIntervalN> intervals = resourceManager.getIntervals();
        List<JEVisSample> sampleCache = resourceManager.getSampleCache();
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
        List<LimitBreakN> limitBreaksStep1 = identifyLimitBreaks(resourceManager, intervals, confLimitsStep1, firstValue);
        List<LimitBreakN> limitBreaksStep2 = identifyLimitBreaks(resourceManager, intervals, confLimitsStep2, firstValue);

        if (limitBreaksStep1.isEmpty() && limitBreaksStep2.isEmpty()) { //no limit checks when there is no alignment
            logger.info("No limit breaks identified.");
            return;
        }
        logger.info("{} limit breaks for step 1 identified", limitBreaksStep1.size());
        logger.info("{} limit breaks for step 2 identified", limitBreaksStep2.size());

        for (JsonLimitsConfig limitsConfig : cleanDataObject.getLimitsConfig()) {
            if (cleanDataObject.getLimitsConfig().indexOf(limitsConfig) == 0) {
                for (LimitBreakN limitBreak : limitBreaksStep1) {
                    for (CleanIntervalN currentInterval : limitBreak.getIntervals()) {
                        logger.info("start marking peculiar samples");
                        VirtualSample smp = currentInterval.getResult();
                        String note = "";
                        note += smp.getNote();
                        note += "," + NoteConstants.Limits.LIMIT_STEP1;
                        smp.setNote(note);
                    }
                }
            } else {
                if (Objects.nonNull(confGaps)) {
                    if (!confGaps.isEmpty()) {
                        List<LimitBreakN> filledLimitBreaks = new ArrayList<>();
                        for (JsonGapFillingConfig c : confGaps) {
                            List<LimitBreakN> newLimitBreaks = new ArrayList<>();
                            for (LimitBreakN lb : limitBreaksStep2) {
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

                            GapsAndLimitsN gal = new GapsAndLimitsN(intervals, null, GapsAndLimitsN.GapsAndLimitsTypeN.LIMITS_TYPE,
                                    c, new ArrayList<>(), newLimitBreaks, sampleCache, cleanDataObject);

                            switch (parse(c.getType())) {
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
        logger.info("[{}] finished filling gaps", cleanDataObject.getCleanObject().getID());
    }

    private Long defaultValue(String s) {
        long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<LimitBreakN> identifyLimitBreaks(ResourceManagerN resourceManager, List<CleanIntervalN> intervals, List<JsonLimitsConfig> conf, Double firstValue) throws Exception {

        List<LimitBreakN> limitBreaks = new ArrayList<>();
        LimitBreakN currentLimitBreak = null;
        CleanIntervalN lastInterval = null;
        for (CleanIntervalN currentInterval : intervals) {
            for (JsonLimitsConfig lc : conf) {
                if (lc.getMin() != null && !lc.getMin().equals("") && lc.getMax() != null && !lc.getMax().equals("")) {
                    Double min = Double.parseDouble(lc.getMin());
                    Double max = Double.parseDouble(lc.getMax());
                    VirtualSample sample = currentInterval.getResult();

                    if (sample != null && sample.getValueAsDouble() != null) {
                        Double sampleValue = sample.getValueAsDouble();

                        if (sample.getValueAsDouble() < min || sample.getValueAsDouble() > max) {
                            if (currentLimitBreak == null) {
                                currentLimitBreak = new LimitBreakN(min, max);
                                if (lastInterval != null)
                                    currentLimitBreak.setFirstValue(lastInterval.getResult().getValueAsDouble());
                                else currentLimitBreak.setFirstValue(firstValue);
                                currentLimitBreak.addInterval(currentInterval);
                                MinOrMax limit = null;
                                if (sampleValue < min) {
                                    limit = MinOrMax.MIN;
                                }
                                if (sampleValue > max) {
                                    limit = MinOrMax.MAX;
                                }
                                currentLimitBreak.setMinOrMax(limit);
                            } else {
                                currentLimitBreak.addInterval(currentInterval);
                            }
                        } else {
                            if (currentLimitBreak != null) {
                                logger.info("Limit Break on: {} to: {}", currentLimitBreak.getIntervals().get(0).getDate(),
                                        currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1).getDate());
                                currentLimitBreak.setLastValue(sampleValue);
                                limitBreaks.add(currentLimitBreak);
                                currentLimitBreak = null;
                            }
                        }
                    } else {
                        logger.warn("Limits Error. No value for Limits Sample {} of object {}:{}", sample, resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getCleanDataObject().getCleanObject().getID());
                    }
                }
            }
            lastInterval = currentInterval;
        }

        if (!limitBreaks.contains(currentLimitBreak)) {
            if (currentLimitBreak != null) {
                if (currentLimitBreak.getIntervals().size() > 0) {
                    CleanIntervalN last = currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1);

                    VirtualSample lastSample = last.getResult();

                    currentLimitBreak.setLastValue(lastSample.getValueAsDouble());
                    limitBreaks.add(currentLimitBreak);
                }
            }
        }

        return limitBreaks;
    }
}
