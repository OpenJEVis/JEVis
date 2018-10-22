/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.save;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.jecalc.data.*;
import org.jevis.jecalc.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class ImportStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(ImportStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        if (resourceManager.getCalcAttribute() instanceof CleanDataAttributeJEVis) {
            importintoJEVis(resourceManager);
        } else if (resourceManager.getCalcAttribute() instanceof CleanDataAttributeOffline) {
            writeIntoFile(resourceManager);
        }
    }

    private void importintoJEVis(ResourceManager resourceManager) throws Exception {
        CleanDataAttributeJEVis cleanAttr = (CleanDataAttributeJEVis) resourceManager.getCalcAttribute();
        JEVisObject cleanObject = cleanAttr.getObject();
        JEVisAttribute attribute = null;

        attribute = cleanObject.getAttribute(CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME);

        if (attribute == null) {
            return;
        }

        List<JEVisSample> cleanSamples = new ArrayList<>();
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        for (CleanInterval curInterval : resourceManager.getIntervals()) {
            for (JEVisSample sample : curInterval.getTmpSamples()) {
                Double rawValue = sample.getValueAsDouble();
                if (rawValue == null) {
                    continue;
                }
                DateTime date = sample.getTimestamp();
                if (date != null) {
                    DateTime timestamp = sample.getTimestamp().plusSeconds(calcAttribute.getPeriodOffset());
                    JEVisSample sampleSql = attribute.buildSample(timestamp, rawValue, sample.getNote());
                    cleanSamples.add(sampleSql);

                }
            }
        }
        logger.info("[{}] Start import of new Samples: {}", resourceManager.getID(), cleanSamples.size());
        attribute.addSamples(cleanSamples);
        logger.info("[{}] Imported finished for samples: {}", resourceManager.getID(), cleanSamples.size());
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("S. Import", cleanSamples.size() + "");
    }

    private void writeIntoFile(ResourceManager resourceManager) {
        CleanDataAttributeOffline cleanAttr = (CleanDataAttributeOffline) resourceManager.getCalcAttribute();
        String pathToOutput = cleanAttr.getPathToOutput();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(pathToOutput), StandardCharsets.UTF_8))) {
            for (CleanInterval curInterval : resourceManager.getIntervals()) {
                for (JEVisSample sample : curInterval.getTmpSamples()) {
                    DateTime timestamp = sample.getTimestamp().plusSeconds(resourceManager.getCalcAttribute().getPeriodOffset());
                    String dateAsString = timestamp.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                    Double value = sample.getValueAsDouble();
                    if (value == null) {
                        continue;
                    }
                    writer.write(dateAsString + ";" + value + "\n");
                }
            }
        } catch (IOException | JEVisException ex) {
            logger.error(ex);
        }
    }
}
