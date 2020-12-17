/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.GapN;
import org.jevis.commons.dataprocessing.processor.GapsAndLimitsN;
import org.jevis.commons.dataprocessing.processor.workflow.DifferentialRule;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStepN;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManagerN;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author gschutz
 */
public class FillGapStepN implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(FillGapStepN.class);

    @Override
    public void run(ResourceManagerN resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        if (!cleanDataObject.getIsPeriodAligned() || !cleanDataObject.getGapFillingEnabled() || cleanDataObject.getGapFillingConfig().isEmpty()) {
            //no gap filling when there is no alignment or disabled or no config
            return;
        }
        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        List<DifferentialRule> differentialRules = cleanDataObject.getDifferentialRules();

        //identify gaps, gaps holds intervals
        List<GapN> gaps = identifyGaps(rawSamples, cleanDataObject, differentialRules);
        logger.info("{} gaps identified", gaps.size());
        if (gaps.isEmpty()) { //no gap filling when there are no gaps
            return;
        }

        List<JsonGapFillingConfig> conf = cleanDataObject.getGapFillingConfig();

        if (Objects.nonNull(conf)) {
            if (!conf.isEmpty()) {
                List<JEVisSample> sampleCache = resourceManager.getSampleCache();

                List<GapN> doneGaps = new ArrayList<>();
                for (JsonGapFillingConfig c : conf) {
                    List<GapN> newGaps = new ArrayList<>();
                    for (GapN g : gaps) {
                        if (!doneGaps.contains(g)) {
                            logger.info("[{}] start filling with Mode for {}", cleanDataObject.getCleanObject().getID(), c.getType());
                            DateTime firstDate = g.getMissingDateTimes().get(0);
                            DateTime lastDate = g.getMissingDateTimes().get(g.getMissingDateTimes().size() - 1);
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

                    GapsAndLimitsN gal = new GapsAndLimitsN(null, rawSamples, GapsAndLimitsN.GapsAndLimitsTypeN.GAPS_TYPE,
                            c, newGaps, new ArrayList<>(), sampleCache);

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

    private List<GapN> identifyGaps(List<JEVisSample> rawSamples, CleanDataObject cleanDataObject, List<DifferentialRule> differentialRules) throws Exception {
        List<GapN> gaps = new ArrayList<>();

        Double lastValue = cleanDataObject.getLastCleanValue();
        DateTime expectedDateTime = null;
        DateTime lastDateTime = null;
        String lastNote = null;

        for (JEVisSample rawSample : rawSamples) {
            DateTime rawSampleTS = rawSample.getTimestamp();
            Period periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), rawSampleTS);

            if (rawSamples.indexOf(rawSample) == 0) {
                expectedDateTime = rawSampleTS;
                lastValue = rawSample.getValueAsDouble();
                lastNote = rawSample.getNote();
                continue;
            } else if (rawSampleTS.equals(expectedDateTime)) {
                lastValue = rawSample.getValueAsDouble();
                lastNote = rawSample.getNote();
            } else {
                GapN currentGap = new GapN();
                currentGap.setFirstValue(lastValue);
                currentGap.setLastValue(rawSample.getValueAsDouble());
                currentGap.setStartNote(lastNote);

                while (expectedDateTime != null && expectedDateTime.isBefore(rawSampleTS)) {
                    currentGap.addDateTime(expectedDateTime);
                    periodForDate = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), expectedDateTime);
                    expectedDateTime = expectedDateTime.plus(periodForDate);
                }

                gaps.add(currentGap);

                lastValue = rawSample.getValueAsDouble();
                lastNote = rawSample.getNote();
            }

            if (expectedDateTime != null) {
                expectedDateTime = expectedDateTime.plus(periodForDate);
            }
        }

        List<GapN> filteredGaps = new ArrayList<>();
        for (GapN gap : gaps) {
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
