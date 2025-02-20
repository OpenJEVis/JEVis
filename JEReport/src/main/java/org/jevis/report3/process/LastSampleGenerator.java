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
 * @author broder
 */
public class LastSampleGenerator {
    private static final Logger logger = LogManager.getLogger(LastSampleGenerator.class);

    public Map<String, Object> work(ReportLinkProperty linkData, ReportAttributeProperty attributeData, ReportProperty property) throws JEVisException {
        JEVisObject dataObject = linkData.getDataObject();
        Map<String, Object> resultMap = new HashMap<>();

        JEVisAttribute attr = dataObject.getAttribute(attributeData.getAttributeName());
        JEVisSample latestSample = attr.getLatestSample();
        if (latestSample != null) {
            resultMap = ProcessHelper.getAttributeSample(latestSample, attr, property.getTimeZone());
        }
        return resultMap;
    }
}
