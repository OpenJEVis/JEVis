/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.periodic;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.report.event.EventPrecondition;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
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
        String startRecordString = samplesHandler.getLastSample(reportObject, "Start Record", "");
        DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
        DateTime endRecord = DateHelper.calcEndRecord(startRecord, schedule);

        String operator = samplesHandler.getLastSample(reportObject, "Operator", "");
        String limit = samplesHandler.getLastSample(reportObject, "Limit", "");
        Long jevisId = samplesHandler.getLastSample(reportObject, "JEVis ID", -1L);
        String attributeName = samplesHandler.getLastSample(reportObject, "Attribute Name", "");

        boolean isFulfilled = true;

        if (operator != null && !operator.equals("") && limit != null && jevisId != null && attributeName != null) {
            isFulfilled = false;

            EventPrecondition.EventOperator eventOperator = EventPrecondition.EventOperator.getEventOperator(operator);

            List<JEVisSample> samplesInPeriod = null;
            try {
                samplesInPeriod = samplesHandler.getSamplesInPeriod(reportObject.getDataSource().getObject(jevisId), attributeName, startRecord, new DateTime());
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            for (JEVisSample sample : Objects.requireNonNull(samplesInPeriod)) {

                String value = null;
                try {
                    value = sample.getValueAsString();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                isFulfilled = Objects.requireNonNull(eventOperator).isFulfilled(value, limit);

                if (isFulfilled) break;
            }
        }

        return ((endRecord != null) && (isFulfilled));
    }
}
