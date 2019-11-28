/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.save;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
public class ImportStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(ImportStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {

        importIntoJEVis(resourceManager);

    }

    private void importIntoJEVis(ResourceManager resourceManager) throws Exception {
        JEVisObject cleanObject = null;
        Integer periodOffset = 0;
        if (resourceManager.getCleanDataObject() != null) {
            CleanDataObject cleanAttr = resourceManager.getCleanDataObject();
            cleanObject = cleanAttr.getCleanObject();
            periodOffset = cleanAttr.getPeriodOffset();
        } else {
            ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();
            cleanObject = forecastDataObject.getForecastDataObject();
            JEVisAttribute attribute = cleanObject.getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME);
            attribute.deleteAllSample();
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
        if (!resourceManager.getIntervals().isEmpty()) {
            firstDateTimeOfResults = resourceManager.getIntervals().get(0).getInterval().getStart();
            lastDateTimeOfResults = resourceManager.getIntervals().get(resourceManager.getIntervals().size() - 1).getInterval().getEnd();

            for (JEVisSample jeVisSample : attribute.getSamples(firstDateTimeOfResults, lastDateTimeOfResults)) {
                listOldSamples.put(jeVisSample.getTimestamp(), jeVisSample);
            }
        }

        List<JEVisSample> cleanSamples = new ArrayList<>();
        for (CleanInterval curInterval : resourceManager.getIntervals()) {
            if (curInterval.getTmpSamples().size() > 0) {
                int lastTmpSampleIndex = curInterval.getTmpSamples().size() - 1;
                JEVisSample sample = curInterval.getTmpSamples().get(lastTmpSampleIndex);

                Double rawValue = sample.getValueAsDouble();
                if (rawValue == null || rawValue.isNaN() || rawValue.isInfinite()) {
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
                    JEVisSample sampleSql = attribute.buildSample(timestamp, rawValue, sample.getNote());
                    cleanSamples.add(sampleSql);
                }
            }
        }
        if (cleanSamples.size() > 0) {
            logger.info("[{}] Start import of new Samples: {}", resourceManager.getID(), cleanSamples.size());
            insertSamples(attribute, cleanSamples);
            logger.info("[{}] Import finished for samples: {}", resourceManager.getID(), cleanSamples.size());
            LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("S. Import", cleanSamples.size() + "");
        } else {
            logger.info("[{}] No new Samples.", resourceManager.getID());
            LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("S. Import", cleanSamples.size() + "");
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
