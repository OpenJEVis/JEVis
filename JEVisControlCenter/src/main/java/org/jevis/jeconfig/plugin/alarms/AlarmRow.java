package org.jevis.jeconfig.plugin.alarms;

import org.jevis.commons.alarm.AlarmConfiguration;
import org.joda.time.DateTime;

public class AlarmRow {
    private DateTime timeStamp;
    private AlarmConfiguration alarmConfiguration;

    public AlarmRow(DateTime timeStamp, AlarmConfiguration alarmConfiguration) {
        this.timeStamp = timeStamp;
        this.alarmConfiguration = alarmConfiguration;
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public AlarmConfiguration getAlarmConfiguration() {
        return alarmConfiguration;
    }
}
