/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.save;

import org.jevis.jecalc.workflow.ProcessStep;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanInterval;
import org.jevis.jecalc.data.CleanDataAttributeJEVis;
import org.jevis.jecalc.data.CleanDataAttributeOffline;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 *
 * @author broder
 */
public class ImportStep implements ProcessStep {

    private static final Logger logger = LoggerFactory.getLogger(ImportStep.class);

    @Override
    public void run(ResourceManager resourceManager) {
        StopWatch stopWatch = new Slf4JStopWatch("import");
        if (resourceManager.getCalcAttribute() instanceof CleanDataAttributeJEVis) {
            importintoJEVis(resourceManager);
        } else if (resourceManager.getCalcAttribute() instanceof CleanDataAttributeOffline) {
            writeIntoFile(resourceManager);
        }
        stopWatch.stop();
    }

    private void importintoJEVis(ResourceManager resourceManager) {
        CleanDataAttributeJEVis cleanAttr = (CleanDataAttributeJEVis) resourceManager.getCalcAttribute();
        JEVisObject cleanObject = cleanAttr.getObject();
        JEVisAttribute attribute = null;
        try {
            attribute = cleanObject.getAttribute(CleanDataAttributeJEVis.VALUE_ATTRIBUTE_NAME);
        } catch (JEVisException ex) {
            logger.error(null, ex);
        }
        if (attribute == null) {
            return;
        }

        List<JEVisSample> cleanSamples = new ArrayList<>();
        CleanDataAttribute calcAttribute = resourceManager.getCalcAttribute();
        for (CleanInterval curInterval : resourceManager.getIntervals()) {
            for (JEVisSample sample : curInterval.getTmpSamples()) {
                try {
                    Double rawValue = sample.getValueAsDouble();
                    if (rawValue == null) {
                        continue;
                    }
                    DateTime date = sample.getTimestamp();
                    if (date != null) {
                        try {
                            DateTime timestamp = sample.getTimestamp().plusSeconds(calcAttribute.getPeriodOffset());
                            JEVisSample sampleSql = attribute.buildSample(timestamp, rawValue, sample.getNote());
                            cleanSamples.add(sampleSql);
                        } catch (JEVisException ex) {
                            logger.error(null, ex);
                        }
                    }
                } catch (JEVisException ex) {
                    logger.error(null, ex);
                }
            }
        }
        try {
            attribute.addSamples(cleanSamples);
        } catch (JEVisException ex) {
        }
        logger.info("{} samples imported", cleanSamples.size());
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
            logger.error(null, ex);
        }
    }
}
