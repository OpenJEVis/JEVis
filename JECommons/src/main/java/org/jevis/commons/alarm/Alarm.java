package org.jevis.commons.alarm;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */


public class Alarm {
    private final JEVisAttribute attribute;
    private final JEVisSample alarmSample;
    private final Double isValue;
    private final Double shouldBeValue;
    private final JEVisObject object;
    private final AlarmType alarmType;
    private final int logValue;
    private Double tolerance;

    public Alarm(JEVisObject object, JEVisAttribute attribute, JEVisSample alarmSample, Double isValue, Double shouldBeValue, AlarmType alarmType, int logValue) {
        this.object = object;
        this.attribute = attribute;
        this.alarmSample = alarmSample;
        this.isValue = isValue;
        this.shouldBeValue = shouldBeValue;
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

    public int getLogValue() {
        return logValue;
    }

    public Double getIsValue() {
        return isValue;
    }

    public Double getShouldBeValue() {
        return shouldBeValue;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }
}
