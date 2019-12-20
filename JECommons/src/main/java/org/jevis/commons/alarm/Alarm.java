package org.jevis.commons.alarm;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */


public class Alarm {
    private final JEVisAttribute attribute;
    private final JEVisSample alarmSample;
    private final DateTime timeStamp;
    private final Double isValue;
    private final String operator;
    private final Double setValue;
    private final JEVisObject object;
    private final AlarmType alarmType;
    private final Integer logValue;
    private Double tolerance;

    public Alarm(JEVisObject object, JEVisAttribute attribute, JEVisSample alarmSample, DateTime timeStamp, Double isValue, String operator, Double setValue, AlarmType alarmType, Integer logValue) {
        this.object = object;
        this.attribute = attribute;
        this.alarmSample = alarmSample;
        this.timeStamp = timeStamp;
        this.isValue = isValue;
        this.operator = operator;
        this.setValue = setValue;
        this.alarmType = alarmType;
        this.logValue = logValue;
    }

    public JEVisObject getObject() {
        return object;
    }

    public JEVisAttribute getAttribute() {
        return attribute;
    }

    public JEVisSample getAlarmSample() {
        return alarmSample;
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public Integer getLogValue() {
        return logValue;
    }

    public Double getIsValue() {
        return isValue;
    }

    public String getOperator() {
        return operator;
    }

    public Double getSetValue() {
        return setValue;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }
}
