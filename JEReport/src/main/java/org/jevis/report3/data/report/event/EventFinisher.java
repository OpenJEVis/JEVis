package org.jevis.report3.data.report.event;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.data.report.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;


/**
 *
 * @author broder
 */
public class EventFinisher implements Finisher {
    private static final Logger logger = LogManager.getLogger(EventFinisher.class);

    private final SampleHandler samplesHandler;

    @Inject
    public EventFinisher(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public void finishReport(Report report, ReportProperty property) {
        try {
            JEVisObject reportObject = property.getReportObject();

            Long jevisId = samplesHandler.getLastSample(reportObject, "JEVis ID", -1L);
            String attributeName = samplesHandler.getLastSample(reportObject, "Attribute Name", "");

            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);
            String newStartTimeString = lastDate.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException ex) {
            logger.error(ex);
        }

    }

    @Override
    public void continueWithNextReport(JEVisObject reportObject) {
        try {
            Long jevisId = samplesHandler.getLastSample(reportObject, "JEVis ID", -1L);
            String attributeName = samplesHandler.getLastSample(reportObject, "Attribute Name", "");

            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);
            String newStartTimeString = lastDate.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException e) {
            logger.error(e);
        }
    }

}
