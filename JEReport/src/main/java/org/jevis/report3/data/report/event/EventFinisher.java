package org.jevis.report3.data.report.event;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.data.report.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author broder
 */
public class EventFinisher implements Finisher {

    private final SampleHandler samplesHandler;

    @Inject
    public EventFinisher(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public void finishReport(Report report, ReportProperty property) {
        try {
            JEVisObject reportObject = property.getReportObject();
            
            Long jevisId = samplesHandler.getLastSampleAsLong(reportObject, "JEVis ID");
            String attributeName = samplesHandler.getLastSampleAsString(reportObject, "Attribute Name");

            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);
            String newStartTimeString = lastDate.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException ex) {
            Logger.getLogger(EventFinisher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
