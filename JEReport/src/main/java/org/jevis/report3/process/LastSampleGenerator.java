/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportLinkProperty;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author broder
 */
public class LastSampleGenerator implements SampleGenerator {
    private static final Logger logger = LogManager.getLogger(LastSampleGenerator.class);

    @Override
    public Map<String, Object> work(ReportLinkProperty linkData, ReportAttributeProperty attributeData, ReportProperty property) {
        JEVisObject dataObject = linkData.getDataObject();
        Map<String, Object> resultMap = new HashMap<>();

        try {
            JEVisAttribute attr = dataObject.getAttribute(attributeData.getAttributeName());
            JEVisSample latestSample = attr.getLatestSample();
            if (latestSample != null) {
                resultMap = ProcessHelper.getAttributeSample(latestSample, attr, property.getTimeZone());
            }
        } catch (JEVisException ex) {
            logger.error("Cant collect samples for attribute: " + attributeData.getAttributeName(), ex);
        }
        return resultMap;
    }
}
