package org.jevis.jealarm;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */


public class Alarm {
    private final JEVisAttribute attribute;
    private final JEVisSample sample;
    private final JEVisObject object;
    private final AlarmType alarmType;

    public Alarm(JEVisObject object, JEVisAttribute attribute, JEVisSample sample, AlarmType alarmType) {
        this.object = object;
        this.attribute = attribute;
        this.sample = sample;
        this.alarmType = alarmType;
    }

    public JEVisObject getObject() {
        return object;
    }

    public JEVisAttribute getAttribute() {
        return attribute;
    }

    public JEVisSample getSample() {
        return sample;
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }
}
