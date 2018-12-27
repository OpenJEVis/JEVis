package org.jevis.report3.data.report.event;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportConfiguration;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author broder
 */
public class EventPrecondition implements Precondition {
    private static final Logger logger = LogManager.getLogger(EventPrecondition.class);

    private final SampleHandler samplesHandler;

    @Inject
    public EventPrecondition(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public boolean isPreconditionReached(JEVisObject reportObject) {

        try {
            String startRecordString = samplesHandler.getLastSample(reportObject, "Start Record", "");
            DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);

            String operator = samplesHandler.getLastSample(reportObject, "Operator", "");
            EventOperator eventOperator = EventOperator.getEventOperator(operator);
            String limit = samplesHandler.getLastSample(reportObject, "Limit", "");
            Long jevisId = samplesHandler.getLastSample(reportObject, "JEVis ID", -1L);
            String attributeName = samplesHandler.getLastSample(reportObject, "Attribute Name", "");

            List<JEVisSample> samplesInPeriod = samplesHandler.getSamplesInPeriod(reportObject.getDataSource().getObject(jevisId), attributeName, startRecord, new DateTime());

            for (JEVisSample sample : samplesInPeriod) {
                String value = sample.getValueAsString();
                boolean isFullfilled = eventOperator.isFulfilled(value, limit);
                if (isFullfilled) {
                    return true;
                }
            }

            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);
            String newStartTimeString = lastDate.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();

        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
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
            if (NumberUtils.isNumber(obj1) && NumberUtils.isNumber(obj2)) {
                return new BigDecimal(obj1).compareTo(new BigDecimal(obj2));
            } else if (Boolean.parseBoolean(obj2)) {
                return 0;
            } else {
                return obj1.compareTo(obj2);
            }
        }
    }
}
