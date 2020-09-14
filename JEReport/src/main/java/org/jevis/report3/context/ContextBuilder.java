/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportData;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author broder
 */
public class ContextBuilder {

    private static final Logger logger = LogManager.getLogger(ContextBuilder.class);
    private IntervalCalculator intervalCalc;

    public void setIntervalCalculator(IntervalCalculator intervalCalc) {
        this.intervalCalc = intervalCalc;
    }

    void addAttributeMapToLinkMap(ConcurrentHashMap<String, Object> linkMap, ConcurrentHashMap<String, Object> attributeMap) {
        linkMap.putAll(attributeMap);
    }

    public ConcurrentHashMap<String, Object> buildContext(List<ReportData> reportLinkProperty, ReportProperty property, IntervalCalculator intervalCalc) {
        ConcurrentHashMap<String, Object> templateMap = new ConcurrentHashMap<>();
        reportLinkProperty.parallelStream().forEach(linkProperty -> templateMap.putAll(linkProperty.getReportMap(property, intervalCalc)));
        logger.info("Built Context for {} report links", templateMap.size());

        return templateMap;
    }
}
