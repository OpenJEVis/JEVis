/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.intervals;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.report3.data.report.Report;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * @author broder
 */
public class Finisher {
    private static final Logger logger = LogManager.getLogger(Finisher.class);

    private final SampleHandler sampleHandler;
    private Period schedule;
    private DateTime startRecord;
    private DateTime endRecord;

    public Finisher(SampleHandler sampleHandler) {
        this.sampleHandler = sampleHandler;
    }

    public void finishReport(Report report, ReportProperty property) {
        try {
            JEVisObject reportObject = property.getReportObject();
            parseDates(reportObject);

            DateTime newStartRecordTime = PeriodHelper.getNextPeriod(startRecord, schedule, 1, null);
            String newStartTimeString = newStartRecordTime.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    private void parseDates(JEVisObject reportObject) {
        String scheduleString = sampleHandler.getLastSample(reportObject, "Schedule", Period.DAILY.toString());
        schedule = Period.valueOf(scheduleString.toUpperCase());
        String startRecordString = sampleHandler.getLastSample(reportObject, "Start Record", "");
        startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
        org.jevis.commons.datetime.DateHelper dateHelper = null;
        dateHelper = PeriodHelper.getDateHelper(reportObject, schedule, dateHelper, startRecord);

        endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateHelper);
    }

    public void continueWithNextReport(JEVisObject reportObject) {
        try {
            parseDates(reportObject);
            DateTime newStartRecordTime = PeriodHelper.getNextPeriod(startRecord, schedule, 1, null);
            String newStartTimeString = newStartRecordTime.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException e) {
            logger.error(e);
        }
    }
}
