/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.intervals;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.report3.data.report.ReportConfiguration;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author broder
 */
public class Precondition {

    private static final Logger logger = LogManager.getLogger(Precondition.class);
    private final SampleHandler samplesHandler;

    public Precondition(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

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

        if (operator != null && !operator.isEmpty() && limit != null && jevisId != null && attributeName != null) {
            isFulfilled = false;

            EventOperator eventOperator = EventOperator.getEventOperator(operator);

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

        return ((endRecord != null) && (isFulfilled));
    }

    public enum EventOperator {

        EQUAL("=") {
            @Override
            public boolean isFulfilled(String value, String limit) {
                return compareTo(value, limit) == 0;
            }
        },
        GREATER(">") {
            @Override
            public boolean isFulfilled(String value, String limit) {
                return compareTo(value, limit) > 0;
            }
        },
        GREATER_THAN(">=") {
            @Override
            public boolean isFulfilled(String value, String limit) {
                return GREATER.isFulfilled(value, limit) || EQUAL.isFulfilled(value, limit);
            }
        },
        LOWER("<") {
            @Override
            public boolean isFulfilled(String value, String limit) {
                return compareTo(value, limit) < 0;
            }
        },
        LOWER_THAN("<=") {
            @Override
            public boolean isFulfilled(String value, String limit) {
                return LOWER.isFulfilled(value, limit) || EQUAL.isFulfilled(value, limit);
            }
        };
        private final String operator;

        EventOperator(String operator) {
            this.operator = operator;
        }

        public static EventOperator getEventOperator(String operator) {
            for (EventOperator currentOperator : EventOperator.values()) {
                if (currentOperator.getOperator().equals(operator)) {
                    return currentOperator;
                }
            }
            logger.info("not a supported operator found");
            return null;
        }

        public String getOperator() {
            return operator;
        }

        public abstract boolean isFulfilled(String value, String limit);

        public int compareTo(String obj1, String obj2) {
            if (NumberUtils.isCreatable(obj1) && NumberUtils.isCreatable(obj2)) {
                return new BigDecimal(obj1).compareTo(new BigDecimal(obj2));
            } else if (Boolean.parseBoolean(obj2)) {
                return 0;
            } else {
                return obj1.compareTo(obj2);
            }
        }
    }
}
