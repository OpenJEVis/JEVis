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
import org.jevis.commons.classes.JC;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.report3.data.report.ReportAttributes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author broder
 */
public class Finisher {
    private static final Logger logger = LogManager.getLogger(Finisher.class);

    private final JEVisObject reportObject;
    private final SampleHandler sampleHandler;
    private Period schedule;
    private final DateTimeZone dateTimeZone;
    private DateTime startRecord;
    private DateTime endRecord;

    public Finisher(JEVisObject reportObject, SampleHandler sampleHandler) {
        this.reportObject = reportObject;
        this.sampleHandler = sampleHandler;
        this.dateTimeZone = sampleHandler.getLastSample(reportObject, JC.Report.a_TimeZone, DateTimeZone.UTC);
        parseDates();
    }

    public void finishReport() {
        try {
            DateTime newStartRecordTime = PeriodHelper.getNextPeriod(startRecord, schedule, dateTimeZone, 1, null);
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartRecordTime).commit();
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    private void parseDates() {
        String scheduleString = sampleHandler.getLastSample(reportObject, "Schedule", Period.DAILY.toString());
        schedule = Period.valueOf(scheduleString.toUpperCase());
        startRecord = sampleHandler.getLastSample(reportObject, "Start Record", new DateTime());
        org.jevis.commons.datetime.DateHelper dateHelper = null;
        dateHelper = PeriodHelper.getDateHelper(reportObject, schedule, dateHelper, startRecord);

        endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateTimeZone, dateHelper);
    }

    public void continueWithNextReport() {
        try {
            DateTime newStartRecordTime = PeriodHelper.getNextPeriod(startRecord, schedule, dateTimeZone, 1, null);
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartRecordTime).commit();
        } catch (JEVisException e) {
            logger.error(e);
        }
    }
}
