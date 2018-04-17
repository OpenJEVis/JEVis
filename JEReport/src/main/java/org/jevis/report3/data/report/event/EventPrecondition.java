package org.jevis.report3.data.report.event;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.commons.lang3.math.NumberUtils;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportConfiguration;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class EventPrecondition implements Precondition {

    private final SampleHandler samplesHandler;

    @Inject
    public EventPrecondition(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public boolean isPreconditionReached(JEVisObject reportObject) {

        try {
            String startRecordString = samplesHandler.getLastSampleAsString(reportObject, "Start Record");
            DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);

            String operator = samplesHandler.getLastSampleAsString(reportObject, "Operator");
            EventOperator eventOperator = EventOperator.getEventOperator(operator);
            String limit = samplesHandler.getLastSampleAsString(reportObject, "Limit");
            Long jevisId = samplesHandler.getLastSampleAsLong(reportObject, "JEVis ID");
            String attributeName = samplesHandler.getLastSampleAsString(reportObject, "Attribute Name");

            List<JEVisSample> samplesInPeriod = samplesHandler.getSamplesInPeriod(reportObject.getDataSource().getObject(jevisId), attributeName, startRecord, new DateTime());

            for (JEVisSample sample : samplesInPeriod) {
                String value = sample.getValueAsString();
                boolean isFullfilled = eventOperator.isFullfilled(value, limit);
                if (isFullfilled) {
                    return true;
                }
            }

            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);
            String newStartTimeString = lastDate.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();

        } catch (JEVisException ex) {
            Logger.getLogger(EventPrecondition.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public enum EventOperator {

        EQUAL("=") {
                    @Override
                    public boolean isFullfilled(String value, String limit) {
                        return compareTo(value, limit) == 0;
                    }
                },
        GREATER(">") {
                    @Override
                    public boolean isFullfilled(String value, String limit) {
                        return compareTo(value, limit) > 0;
                    }
                },
        GREATER_THAN(">=") {
                    @Override
                    public boolean isFullfilled(String value, String limit) {
                        return GREATER.isFullfilled(value, limit) || EQUAL.isFullfilled(value, limit);
                    }
                },
        LOWER("<") {
                    @Override
                    public boolean isFullfilled(String value, String limit) {
                        return compareTo(value, limit) < 0;
                    }
                },
        LOWER_THAN("<=") {
                    @Override
                    public boolean isFullfilled(String value, String limit) {
                        return LOWER.isFullfilled(value, limit) || EQUAL.isFullfilled(value, limit);
                    }
                };
        private final String operator;

        private EventOperator(String operator) {
            this.operator = operator;
        }

        public static EventOperator getEventOperator(String operator) {
            for (EventOperator currentOperator : EventOperator.values()) {
                if (currentOperator.getOperator().equals(operator)) {
                    return currentOperator;
                }
            }
            System.out.println("not a supported operator found");
            return null;
        }

        public String getOperator() {
            return operator;
        }

        public abstract boolean isFullfilled(String value, String limit);

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
