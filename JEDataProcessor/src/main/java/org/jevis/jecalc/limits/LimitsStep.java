/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.limits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanDataAttributeJEVis;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.util.GapsAndLimits;
import org.jevis.jecalc.workflow.ProcessStep;
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
    CleanDataAttribute calcAttribute;
    private JEVisObject parentObject;
    private List<JEVisSample> sampleCache;

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        calcAttribute = resourceManager.getCalcAttribute();
        for (JEVisObject obj : calcAttribute.getObject().getParents()) {
            parentObject = obj;
        }

        if (!calcAttribute.getIsPeriodAligned() || !calcAttribute.getLimitsEnabled() || calcAttribute.getLimitsConfig().isEmpty()) {
            //no limits check when there is no alignment or disabled or no config
            return;
        }
        List<CleanInterval> intervals = resourceManager.getIntervals();

        List<JsonLimitsConfig> confLimitsStep1 = new ArrayList<>();
        List<JsonLimitsConfig> confLimitsStep2 = new ArrayList<>();
        if (calcAttribute.getLimitsConfig().size() > 0) {
            confLimitsStep1.add(calcAttribute.getLimitsConfig().get(0));
        }
        if (calcAttribute.getLimitsConfig().size() > 1) {
            confLimitsStep2.add(calcAttribute.getLimitsConfig().get(1));
        }

        List<JsonGapFillingConfig> confGaps = calcAttribute.getGapFillingConfig();

        if (Objects.nonNull(confLimitsStep1)) {
            //identify limit breaking intervals
            List<LimitBreak> limitBreaksStep1 = identifyLimitBreaks(intervals, confLimitsStep1);
            List<LimitBreak> limitBreaksStep2 = identifyLimitBreaks(intervals, confLimitsStep2);

            if (limitBreaksStep1.isEmpty() && limitBreaksStep2.isEmpty()) { //no limit checks when there is no alignment
                logger.info("No limit breaks identified.");
                return;
            }
            logger.info("{} limit breaks for step 1 identified", limitBreaksStep1.size());
            logger.info("{} limit breaks for step 2 identified", limitBreaksStep2.size());

            try {
                DateTime minDateForCache = calcAttribute.getFirstDate().minusMonths(6);
                DateTime lastDateForCache = calcAttribute.getFirstDate();

                sampleCache = calcAttribute.getObject().getAttribute(CleanDataAttributeJEVis.CLASS_NAME).getSamples(minDateForCache, lastDateForCache);
            } catch (Exception e) {
                logger.error("No caching possible: " + e);
            }
            for (JsonLimitsConfig limitsConfig : calcAttribute.getLimitsConfig()) {
                if (calcAttribute.getLimitsConfig().indexOf(limitsConfig) == 0) {
                    for (LimitBreak limitBreak : limitBreaksStep1) {
                        Double firstValue = limitBreak.getFirstValue();
                        for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                            logger.info("start marking peculiar samples");
                            for (JEVisSample smp : currentInterval.getTmpSamples()) {
                                String note = "";
                                note += smp.getNote();
                                note += ",limit(Step1)";
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
                                    logger.info("start filling with Mode for " + c.getType());
                                    DateTime firstDate = lb.getIntervals().get(0).getDate();
                                    DateTime lastDate = lb.getIntervals().get(lb.getIntervals().size() - 1).getDate();
                                    if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                                        if (!filledLimitBreaks.contains(lb)) {
                                            newLimitBreaks.add(lb);
                                            filledLimitBreaks.add(lb);
                                        }
                                    }
                                }

                                GapsAndLimits gal = new GapsAndLimits(intervals, calcAttribute, GapsAndLimits.GapsAndLimitsType.LIMITS_TYPE,
                                        c, new ArrayList<>(), limitBreaksStep2, sampleCache);

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
                            }
                        }
                    }
                }
            }
        }
    }

    private Long defaultValue(String s) {
        Long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<LimitBreak> identifyLimitBreaks(List<CleanInterval> intervals, List<JsonLimitsConfig> conf) throws Exception {
/**
 * Debug help
 */
//        System.out.println("identifyLimitBreaks");
//        for (CleanInterval interval : intervals) {
//            System.out.println("Interval: " + interval.getDate() + " " + interval.getInterval());
//            for (JEVisSample s : interval.getTmpSamples()) {
//                System.out.println("raw: " + s);
//            }
//            for (JEVisSample s : interval.getTmpSamples()) {
//                System.out.println("tmp: " + s);
//            }
//        }

        List<LimitBreak> limitBreaks = new ArrayList<>();
        LimitBreak currentLimitBreak = null;
        CleanInterval lastInterval = null;
        for (CleanInterval currentInterval : intervals) {
            for (JsonLimitsConfig lc : conf) {
                for (JEVisSample sample : currentInterval.getTmpSamples()) {
                    Double min = Double.parseDouble(lc.getMin());
                    Double max = Double.parseDouble(lc.getMax());

                    if (sample == null || sample.getValueAsDouble() == null) {
                        logger.error("- Limits Sample: {} min: {} max: {}" + sample, min, max);
                        throw new Exception("Error in identifyLimitBreaks, empty value in interval: " + currentInterval.getInterval());
                    }

                    Double sampleValue = sample.getValueAsDouble();

                    if (sample.getValueAsDouble() < min || sample.getValueAsDouble() > max) {
                        if (currentLimitBreak == null) {
                            currentLimitBreak = new LimitBreakJEVis();
                            if (lastInterval != null && !lastInterval.getTmpSamples().isEmpty() && Objects.nonNull(lastInterval.getTmpSamples().get(0)))
                                currentLimitBreak.setFirstValue(lastInterval.getTmpSamples().get(0).getValueAsDouble());
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
            lastInterval = currentInterval;
        }

        return limitBreaks;
    }
}
