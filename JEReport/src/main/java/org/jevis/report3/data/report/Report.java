/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jevis.api.JEVisFile;
import org.jevis.report3.TemplateTransformator;
import org.jxls.common.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author broder
 */
public class Report {
    
    private static final Logger logger = LoggerFactory.getLogger(Report.class);
    private final ReportProperty reportProperty;
    private final Map<String, Object> contextMap;

    public Report(ReportProperty property, Map<String, Object> contextMap) {
        this.reportProperty = property;
        this.contextMap = contextMap;
    }

    public byte[] getReportFile() {
        //get the template
        JEVisFile template = reportProperty.getTemplate();
        TemplateTransformator transformator = new TemplateTransformator();
        Context context = new Context(contextMap);
        try {
            transformator.transfrom(template.getBytes(), context);
        } catch (IOException ex) {
            System.out.println(ex);
            logger.error("error while transformatin of the template", ex);
        }
        return transformator.getOutputBytes();
    }
}
