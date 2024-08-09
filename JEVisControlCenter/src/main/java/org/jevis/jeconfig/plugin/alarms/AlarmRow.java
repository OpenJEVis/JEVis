package org.jevis.jeconfig.plugin.alarms;

import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class AlarmRow {
    private final Alarm alarm;
    private final DateTime timeStamp;
    private final AlarmConfiguration alarmConfiguration;
    private final Boolean isLinkedDisabled;
    private final String formatString;


    public AlarmRow(DateTime timeStamp, AlarmConfiguration alarmConfiguration, Alarm alarm) {
        this.timeStamp = timeStamp;
        this.alarmConfiguration = alarmConfiguration;
        this.isLinkedDisabled = alarmConfiguration.isLinkDisabled();
        this.alarm = alarm;

        Period periodForDate = CleanDataObject.getPeriodForDate(alarm.getObject(), timeStamp);
        this.formatString = PeriodHelper.getFormatString(periodForDate, false);
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

    public String getFormatString() {
        return formatString;
    }
}
