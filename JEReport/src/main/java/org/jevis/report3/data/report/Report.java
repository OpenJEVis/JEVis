/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisFile;
import org.jevis.report3.TemplateTransformator;
import org.jxls.common.Context;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author broder
 */
public class Report {

    private static final Logger logger = LogManager.getLogger(Report.class);
    private final ReportProperty reportProperty;
    private final Map<String, Object> contextMap;

    public Report(ReportProperty property, Map<String, Object> contextMap) {
        this.reportProperty = property;
        this.contextMap = contextMap;
    }

    public byte[] getReportFile() {
        //get the template
        JEVisFile template = reportProperty.getTemplate();
        TemplateTransformator templateTransformator = new TemplateTransformator();

        Context context = new Context(contextMap);
        context.getConfig().setIsFormulaProcessingRequired(true);

        try {
            templateTransformator.transform(template.getBytes(), context);
        } catch (IOException ex) {
            logger.fatal("error while transformation of the template", ex);
        }
        return templateTransformator.getOutputBytes();
    }
}
