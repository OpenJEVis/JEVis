/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.periodic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.event.EventPrecondition;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author broder
 */
public class PeriodPrecondition implements Precondition {

    private final SampleHandler samplesHandler;
    private static final Logger logger = LogManager.getLogger(PeriodPrecondition.class);

    @Inject
    public PeriodPrecondition(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public boolean isPreconditionReached(JEVisObject reportObject) {

        String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", Period.DAILY.toString());
        Period schedule = Period.valueOf(scheduleString.toUpperCase());
        String startRecordString = samplesHandler.getLastSample(reportObject, "Start Record", "");
        DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);

        org.jevis.commons.datetime.DateHelper dateHelper = null;
        dateHelper = PeriodHelper.getDateHelper(reportObject, schedule, dateHelper, startRecord);
        DateTime endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateHelper);

        String operator = samplesHandler.getLastSample(reportObject, "Operator", "");
        String limit = samplesHandler.getLastSample(reportObject, "Limit", "");
        Long jevisId = samplesHandler.getLastSample(reportObject, "JEVis ID", -1L);
        String attributeName = samplesHandler.getLastSample(reportObject, "Attribute Name", "");

        boolean isFulfilled = true;

        if (operator != null && !operator.equals("") && limit != null && jevisId != null && attributeName != null) {
            isFulfilled = false;

            EventPrecondition.EventOperator eventOperator = EventPrecondition.EventOperator.getEventOperator(operator);

            List<JEVisSample> samplesInPeriod = new ArrayList<>();
            try {
                JEVisDataSource ds = reportObject.getDataSource();
                samplesInPeriod = samplesHandler.getSamplesInPeriod(ds.getObject(jevisId), attributeName, startRecord, endRecord);
            } catch (JEVisException e) {
                logger.error("Could not get samples in interval");
            }

            if (samplesInPeriod.isEmpty()) isFulfilled = true;

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

        return ((endRecord != null && endRecord.isBefore(new DateTime())) && (isFulfilled));
    }
}
