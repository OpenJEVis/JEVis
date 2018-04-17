/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.periodic;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.Finisher;
import org.jevis.report3.data.report.Report;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class PeriodFinisher implements Finisher {

    private final SampleHandler sampleHandler;
    private ReportProperty.ReportSchedule schedule;
    private DateTime startRecord;
    private DateTime endRecord;

    @Inject
    public PeriodFinisher(SampleHandler sampleHandler) {
        this.sampleHandler = sampleHandler;
    }

    @Override
    public void finishReport(Report report, ReportProperty property) {
        try {
            JEVisObject reportObject = property.getReportObject();
            parseDates(reportObject);
           
            DateTime newStartRecordTime = DateHelper.getNextPeriod(startRecord, schedule, 1);
            String newStartTimeString = newStartRecordTime.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException ex) {
            Logger.getLogger(PeriodFinisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void parseDates(JEVisObject reportObject) {
        String scheduleString = sampleHandler.getLastSample(reportObject, "Schedule", ReportProperty.ReportSchedule.DAILY.toString());
        schedule = ReportProperty.ReportSchedule.valueOf(scheduleString.toUpperCase());
        String startRecordString = sampleHandler.getLastSampleAsString(reportObject, "Start Record");
        startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
        endRecord = DateHelper.calcEndRecord(startRecord, schedule);
    }

}
