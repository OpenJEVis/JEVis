/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanIntervalN;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStepN;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManagerN;
import org.jevis.commons.task.LogTaskManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
public class ImportStepN implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(ImportStepN.class);

    @Override
    public void run(ResourceManagerN resourceManager) throws Exception {

        importIntoJEVis(resourceManager);

    }

    private void importIntoJEVis(ResourceManagerN resourceManager) throws Exception {
        JEVisObject cleanObject = null;
        Integer periodOffset = 0;
        if (resourceManager.getCleanDataObject() != null) {
            CleanDataObject cleanAttr = resourceManager.getCleanDataObject();
            cleanObject = cleanAttr.getCleanObject();
            periodOffset = cleanAttr.getPeriodOffset();

            if (!resourceManager.getIntervals().isEmpty()) {
                DateTime lastDateTimeOfResults = resourceManager.getIntervals().get(resourceManager.getIntervals().size() - 1).getInterval().getEnd();
                removeOldForecastSamples(cleanObject, lastDateTimeOfResults);
            }
        } else if (resourceManager.getForecastDataObject() != null) {
            ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();
            cleanObject = forecastDataObject.getForecastDataObject();
            JEVisAttribute attribute = cleanObject.getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME);
            attribute.deleteAllSample();
        } else if (resourceManager.getMathDataObject() != null) {
            MathDataObject mathDataObject = resourceManager.getMathDataObject();
            cleanObject = mathDataObject.getMathDataObject();
        }

        JEVisAttribute attribute = null;

        attribute = cleanObject.getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME);

        if (attribute == null) {
            return;
        }

        boolean hasSamples = attribute.hasSample();
        Map<DateTime, JEVisSample> listOldSamples = new HashMap<>();
        DateTime firstDateTimeOfResults = null;
        DateTime lastDateTimeOfResults = null;

        boolean monthPeriods = false;
        for (CleanIntervalN interval : resourceManager.getIntervals()) {
            if (interval.getOutputPeriod().equals(Period.months(1))) {
                monthPeriods = true;
                break;
            }
        }

        if (resourceManager.isClean() && !resourceManager.getIntervals().isEmpty() && !monthPeriods) {
            resourceManager.getIntervals().remove(0);
            firstDateTimeOfResults = resourceManager.getIntervals().get(0).getInterval().getStart();
            lastDateTimeOfResults = resourceManager.getIntervals().get(resourceManager.getIntervals().size() - 1).getInterval().getEnd();

            for (JEVisSample jeVisSample : attribute.getSamples(firstDateTimeOfResults, lastDateTimeOfResults)) {
                listOldSamples.put(jeVisSample.getTimestamp(), jeVisSample);
            }
        }

        List<JEVisSample> cleanSamples = new ArrayList<>();
        for (CleanIntervalN curInterval : resourceManager.getIntervals()) {
            JEVisSample sample = curInterval.getResult();

            Double value = sample.getValueAsDouble();
            if (value == null || value.isNaN() || value.isInfinite()) {
                continue;
            }
            DateTime date = sample.getTimestamp();
            if (date != null) {
                DateTime timestamp = sample.getTimestamp().plusSeconds(periodOffset);

                if (hasSamples) {
                    JEVisSample smp = listOldSamples.get(timestamp);
                    if (smp != null) {
                        attribute.deleteSamplesBetween(timestamp, timestamp);
                    }
                }
                JEVisSample sampleSql = attribute.buildSample(timestamp, value, sample.getNote());
                cleanSamples.add(sampleSql);
            }
        }
        if (cleanSamples.size() > 0) {
            logger.info("[{}] Start import of new Samples: {}", resourceManager.getID(), cleanSamples.size());
            insertSamples(attribute, cleanSamples);
            logger.info("[{}] Import finished for samples: {}", resourceManager.getID(), cleanSamples.size());
        } else {
            logger.info("[{}] No new Samples.", resourceManager.getID());
        }
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("S. Import", cleanSamples.size() + "");
    }

    private void removeOldForecastSamples(JEVisObject cleanObject, DateTime lastDateTimeOfResults) {
        try {
            JEVisClass foreCastClass = cleanObject.getDataSource().getJEVisClass(ForecastDataObject.CLASS_NAME);
            List<JEVisObject> children = cleanObject.getChildren(foreCastClass, false);
            if (!children.isEmpty()) {
                for (JEVisObject object : children) {
                    JEVisAttribute attribute = object.getAttribute(ForecastDataObject.VALUE_ATTRIBUTE_NAME);
                    if (attribute != null) {
                        attribute.deleteSamplesBetween(new DateTime(2001, 1, 1, 0, 0, 0), lastDateTimeOfResults);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void insertSamples(JEVisAttribute attribute, List<JEVisSample> samples) throws JEVisException {
        int perChunk = 30000;
        for (int i = 0; i < samples.size(); i += perChunk) {
            if ((i + perChunk) < samples.size()) {
                List<JEVisSample> chunk = samples.subList(i, i + perChunk);
                if (chunk.size() > 0) {
                    attribute.addSamples(chunk);
                }
            } else {
                List<JEVisSample> chunk = samples.subList(i, samples.size());
                if (chunk.size() > 0) {
                    attribute.addSamples(chunk);
                }
                break;
            }
        }
    }

}
