package org.jevis.jeconfig.plugin.alarms;

import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.joda.time.DateTime;

public class AlarmRow {
    private final Alarm alarm;
    private final DateTime timeStamp;
    private final AlarmConfiguration alarmConfiguration;


    public AlarmRow(DateTime timeStamp, AlarmConfiguration alarmConfiguration, Alarm alarm) {
        this.timeStamp = timeStamp;
        this.alarmConfiguration = alarmConfiguration;
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
}
