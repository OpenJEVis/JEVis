package org.jevis.jecc.plugin.alarms;

import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.joda.time.DateTime;

public class AlarmRow {
    private final Alarm alarm;
    private final DateTime timeStamp;
    private final AlarmConfiguration alarmConfiguration;
    private final Boolean isLinkedDisabled;


    public AlarmRow(DateTime timeStamp, AlarmConfiguration alarmConfiguration, Alarm alarm) {
        this.timeStamp = timeStamp;
        this.alarmConfiguration = alarmConfiguration;
        this.isLinkedDisabled = alarmConfiguration.isLinkDisabled();
        this.alarm = alarm;
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public AlarmConfiguration getAlarmConfiguration() {
        return alarmConfiguration;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public Boolean isLinkDisabled() {
        return isLinkedDisabled;
    }
}
