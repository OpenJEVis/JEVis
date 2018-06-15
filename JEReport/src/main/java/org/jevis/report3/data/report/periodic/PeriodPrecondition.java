/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.periodic;

import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;

/**
 *
 * @author broder
 */
public class PeriodPrecondition implements Precondition {

    private final SampleHandler samplesHandler;

    @Inject
    public PeriodPrecondition(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public boolean isPreconditionReached(JEVisObject reportObject) {

        String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", ReportProperty.ReportSchedule.DAILY.toString());
        ReportProperty.ReportSchedule schedule = ReportProperty.ReportSchedule.valueOf(scheduleString.toUpperCase());
        String startRecordString = samplesHandler.getLastSampleAsString(reportObject, "Start Record");
        DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
        DateTime endRecord = DateHelper.calcEndRecord(startRecord, schedule);

        return endRecord != null;
    }
}
