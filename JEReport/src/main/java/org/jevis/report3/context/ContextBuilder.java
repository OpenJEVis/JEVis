/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jevis.report3.data.attribute.AttributeConfiguration;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportData;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.jevis.report3.process.SampleGenerator;

/**
 *
 * @author broder
 */
public class ContextBuilder {

    private final SampleFactory sampleFactory;
    private IntervalCalculator intervalCalc;

    @Inject
    public ContextBuilder(SampleFactory sampleFactory) {
        this.sampleFactory = sampleFactory;
    }

    public void setIntervalCalculator(IntervalCalculator intervalCalc) {
        this.intervalCalc = intervalCalc;
    }

    void addAttributeMapToLinkMap(Map<String, Object> linkMap, Map<String, Object> attributeMap) {
        linkMap.putAll(attributeMap);
    }

    Map<String, Object> getMapFromReportLink(ReportLinkProperty linkProperty, ReportProperty property) {
        Map<String, Object> linkMap = new HashMap<>();
        List<ReportAttributeProperty> attributeProperties = linkProperty.getAttributeProperties();
        attributeProperties.addAll(linkProperty.getDefaultAttributeProperties());
        for (ReportAttributeProperty attributeProperty : attributeProperties) {
            List<AttributeConfiguration> attributeConfigs = attributeProperty.getAttributeConfigurations();
            SampleGenerator sampleGenerator = sampleFactory.getSampleGenerator(attributeConfigs, intervalCalc);
            Map<String, Object> attributeMap = sampleGenerator.work(linkProperty, attributeProperty, property);
            addAttributeMapToLinkMap(linkMap, attributeMap);
        }
        String objectName = linkProperty.getDataObject().getName();
        Map<String, Object> tmpMap = new HashMap<>();
        tmpMap.put("name", objectName);
        addAttributeMapToLinkMap(linkMap, tmpMap);
        return linkMap;
    }

    public Map<String, Object> buildContext(List<ReportData> reportLinkProperty, ReportProperty property, IntervalCalculator intervalCalc) {
        Map<String, Object> templateMap = new HashMap<>();
        for (ReportData linkProperty : reportLinkProperty) {
            templateMap.putAll(linkProperty.getReportMap(property,intervalCalc));
//            Map<String, Object> reportLinkMap = getMapFromReportLink(linkProperty, property);
//            templateMap.put(linkProperty.getTemplateVariableName(), reportLinkMap);
        }
        return templateMap;
    }

}
