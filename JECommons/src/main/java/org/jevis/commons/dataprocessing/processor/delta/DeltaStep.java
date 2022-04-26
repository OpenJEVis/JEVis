/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.delta;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.GapsAndLimits;
import org.jevis.commons.dataprocessing.processor.limits.LimitBreak;
import org.jevis.commons.dataprocessing.processor.limits.MinOrMax;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.json.JsonDeltaConfig;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jevis.commons.constants.GapFillingType.parse;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class DeltaStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(DeltaStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        if (!cleanDataObject.getDeltaEnabled() || cleanDataObject.getDeltaConfig() == null) {
            //no delta check when disabled or no config
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

        JsonDeltaConfig deltaConfig = cleanDataObject.getDeltaConfig();

        //identify limit breaking intervals
        List<LimitBreak> deltaBreaksStep1 = identifyLimitBreaks(resourceManager, intervals, Double.parseDouble(deltaConfig.getMin()), firstValue, MinOrMax.MIN);
        List<LimitBreak> deltaBreaksStep2 = identifyLimitBreaks(resourceManager, intervals, Double.parseDouble(deltaConfig.getMax()), firstValue, MinOrMax.MAX);

        if (deltaBreaksStep1.isEmpty() && deltaBreaksStep2.isEmpty()) { //no limit checks when there is no alignment
            logger.info("No delta breaks identified.");
            return;
        }
        logger.info("{} delta breaks for step 1 identified", deltaBreaksStep1.size());
        logger.info("{} delta breaks for step 2 identified", deltaBreaksStep2.size());

        NumberFormat nf = resourceManager.getNumberFormat();

        for (LimitBreak limitBreak : deltaBreaksStep1) {
            for (CleanInterval currentInterval : limitBreak.getIntervals()) {
                VirtualSample smp = currentInterval.getResult();
                String note = "";
                note += smp.getNote();
                note += "," + NoteConstants.Deltas.DELTA_STEP1;
                note += ",(" + nf.format(limitBreak.getMin()) + "," + nf.format(limitBreak.getMax()) + ")";
                smp.setNote(note);
            }
        }

        if (!deltaBreaksStep2.isEmpty()) {
            List<JEVisSample> sampleCache = resourceManager.getSampleCache();

            if (sampleCache == null) {
                sampleCache = intervals.stream().map(CleanInterval::getResult).collect(Collectors.toList());
                sampleCache.sort(Comparator.comparing(jeVisSample -> {
                    try {
                        return jeVisSample.getTimestamp();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return null;
                }));
            }


            List<LimitBreak> filledLimitBreaks = new ArrayList<>();
            List<LimitBreak> newLimitBreaks = new ArrayList<>();
            JsonGapFillingConfig c = deltaConfig.getMaxConfig();

            for (LimitBreak lb : deltaBreaksStep2) {
                if (!filledLimitBreaks.contains(lb)) {
                    logger.debug("[{}] start filling with Mode for {}", cleanDataObject.getCleanObject().getID(), c.getType());
                    DateTime firstDate = lb.getIntervals().get(0).getDate();
                    DateTime lastDate = lb.getIntervals().get(lb.getIntervals().size() - 1).getDate();
                    if ((lastDate.getMillis() - firstDate.getMillis()) <= defaultValue(c.getBoundary())) {
                        newLimitBreaks.add(lb);
                        filledLimitBreaks.add(lb);
                    }
                }
            }

            GapsAndLimits gal = new GapsAndLimits(intervals, null, GapsAndLimits.GapsAndLimitsType.DELTA_TYPE,
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

            logger.debug("[{}] Done", resourceManager.getID());

            if (deltaBreaksStep2.size() != filledLimitBreaks.size())
                logger.error("Could not complete all delta breaks. Delta break may have been too long for reasonable gap filling.");
        }

        logger.debug("[{}] finished substituting values", cleanDataObject.getCleanObject().getID());
    }

    private Long defaultValue(String s) {
        long l = 0L;
        if (Objects.nonNull(s)) {
            l = Long.parseLong(s);
        }
        return l;
    }

    private List<LimitBreak> identifyLimitBreaks(ResourceManager resourceManager, List<CleanInterval> intervals, double deltaPercent, Double firstValue, MinOrMax minOrMax) {

        List<LimitBreak> limitBreaks = new ArrayList<>();
        LimitBreak currentLimitBreak = null;
        CleanInterval lastInterval = null;
        double lastValue = Math.abs(firstValue);
        double delta = deltaPercent / 100;

        if (delta != 0d) {
            for (CleanInterval currentInterval : intervals) {
                VirtualSample sample = currentInterval.getResult();

                if (sample != null && sample.getValueAsDouble() != null) {
                    double sampleValue = Math.abs(sample.getValueAsDouble());
                    double diff = Math.abs(sampleValue - lastValue);
                    double d = 0d;
                    if (diff > 0d && sampleValue != 0d) {
                        d = diff / sampleValue;
                    } else if (diff > 0d && lastValue != 0d) {
                        d = diff / lastValue;
                    }

                    lastValue = sampleValue;
                    if (d >= delta) {
                        if (currentLimitBreak == null) {
                            currentLimitBreak = new LimitBreak(d, delta);
                            if (lastInterval != null)
                                currentLimitBreak.setFirstValue(lastInterval.getResult().getValueAsDouble());
                            else currentLimitBreak.setFirstValue(firstValue);
                            currentLimitBreak.addInterval(currentInterval);
                            currentLimitBreak.setMinOrMax(minOrMax);
                        } else {
                            currentLimitBreak.addInterval(currentInterval);
                        }
                    } else {
                        if (currentLimitBreak != null) {
                            logger.info("Delta Break on: {} to: {}", currentLimitBreak.getIntervals().get(0).getDate(),
                                    currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1).getDate());
                            currentLimitBreak.setLastValue(sampleValue);
                            limitBreaks.add(currentLimitBreak);
                            currentLimitBreak = null;
                        }
                    }
                } else {
                    logger.warn("Delta Error. No value for Delta Sample {} of object {}:{}", sample, resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getCleanDataObject().getCleanObject().getID());
                }
                lastInterval = currentInterval;
            }
        } else {
            for (CleanInterval currentInterval : intervals) {
                VirtualSample sample = currentInterval.getResult();

                if (sample != null && sample.getValueAsDouble() != null) {
                    double sampleValue = Math.abs(sample.getValueAsDouble());
                    double diff = Math.abs(sampleValue - lastValue);

                    lastValue = sampleValue;
                    if (diff == 0d) {
                        if (currentLimitBreak == null) {
                            currentLimitBreak = new LimitBreak(0d, delta);
                            if (lastInterval != null)
                                currentLimitBreak.setFirstValue(lastInterval.getResult().getValueAsDouble());
                            else currentLimitBreak.setFirstValue(firstValue);
                            currentLimitBreak.addInterval(currentInterval);
                            currentLimitBreak.setMinOrMax(minOrMax);
                        } else {
                            currentLimitBreak.addInterval(currentInterval);
                        }
                    } else {
                        if (currentLimitBreak != null) {
                            logger.info("Delta Break on: {} to: {}", currentLimitBreak.getIntervals().get(0).getDate(),
                                    currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1).getDate());
                            currentLimitBreak.setLastValue(sampleValue);
                            limitBreaks.add(currentLimitBreak);
                            currentLimitBreak = null;
                        }
                    }
                } else {
                    logger.warn("Delta Error. No value for Delta Sample {} of object {}:{}", sample, resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getCleanDataObject().getCleanObject().getID());
                }
                lastInterval = currentInterval;
            }
        }

        if (!limitBreaks.contains(currentLimitBreak)) {
            if (currentLimitBreak != null) {
                if (currentLimitBreak.getIntervals().size() > 0) {
                    CleanInterval last = currentLimitBreak.getIntervals().get(currentLimitBreak.getIntervals().size() - 1);

                    VirtualSample lastSample = last.getResult();

                    currentLimitBreak.setLastValue(lastSample.getValueAsDouble());
                    limitBreaks.add(currentLimitBreak);
                }
            }
        }

        return limitBreaks;
    }
}
