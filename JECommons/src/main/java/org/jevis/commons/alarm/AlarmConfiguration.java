package org.jevis.commons.alarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */


public class AlarmConfiguration {
    public static final String CLASS_NAME = "Alarm Configuration";
    private static final Logger logger = LogManager.getLogger(AlarmConfiguration.class);
    private static final String TIME_STAMP = "Time Stamp";
    private static final String LOG = "Log";
    private final String ENABLED_NAME = "Enabled";
    private final String ALARM_SCOPE = "Alarm Scope";
    private final String ALARM_PERIOD = "Alarm Period";
    private final String ALARM_OBJECTS = "Alarm Objects";
    public final static String ALARM_CHECKED = "Alarm Checked";
    private final JEVisObject object;
    private final JEVisDataSource ds;
    private Boolean enabled;
    private Boolean checked;
    private SampleHandler sampleHandler;
    private Long id;
    private String name;
    private AlarmScope alarmScope;
    private DateTime timeStamp;
    private Period alarmPeriod;
    private JEVisAttribute timeStampAttribute;
    private JEVisAttribute logAttribute;
    private List<JEVisObject> alarmObjects;

    public AlarmConfiguration(JEVisDataSource ds, JEVisObject jeVisObject) {
        this.ds = ds;
        this.object = jeVisObject;
        sampleHandler = new SampleHandler();
    }

    public Boolean isEnabled() {
        if (enabled == null) {
            enabled = sampleHandler.getLastSample(getObject(), ENABLED_NAME, false);
        }
        return enabled;
    }

    public Boolean isChecked() {
        if (checked == null) {
            checked = sampleHandler.getLastSample(getObject(), ALARM_CHECKED, false);
        }
        return checked;
    }

    public void setChecked(Boolean checked) {
        try {
            JEVisAttribute checkedAttribute = object.getAttribute(ALARM_CHECKED);
            if (checkedAttribute != null) {
                JEVisSample sample = checkedAttribute.buildSample(new DateTime(), checked);
                sample.commit();
            }
        } catch (Exception e) {
            logger.error("Could not set checked attribute for object {}:{}", object.getName(), object.getID(), e);
        }
    }

    public JEVisObject getObject() {
        return object;
    }

    public Long getId() {
        if (id == null)
            id = getObject().getID();
        return id;
    }

    public String getName() {
        if (name == null)
            name = getObject().getName();
        return name;
    }

    public AlarmScope getAlarmScope() {
        if (alarmScope == null) {
            String scope = sampleHandler.getLastSample(getObject(), ALARM_SCOPE, "");
            if (scope.equals(AlarmScope.COMPLETE.toString())) alarmScope = AlarmScope.COMPLETE;
            else if (scope.equals(AlarmScope.SELECTED.toString())) alarmScope = AlarmScope.SELECTED;
            else if (scope.equals(AlarmScope.WITHOUT_SELECTED.toString())) alarmScope = AlarmScope.WITHOUT_SELECTED;
            else alarmScope = AlarmScope.NONE;
        }
        return alarmScope;
    }

    public DateTime getTimeStamp() {
        if (timeStamp == null) {
            String dateTimeString = sampleHandler.getLastSample(getObject(), TIME_STAMP, "");
            if (!dateTimeString.equals("")) timeStamp = new DateTime(dateTimeString);

            try {
                timeStampAttribute = getObject().getAttribute(TIME_STAMP);
                logAttribute = getObject().getAttribute(LOG);
            } catch (JEVisException e) {
                logger.error("Could not get Time Stamp Attribute.");
            }
        }
        return timeStamp;
    }

    public Period getAlarmPeriod() {
        if (alarmPeriod == null) {
            String alarmPeriodString = sampleHandler.getLastSample(getObject(), ALARM_PERIOD, "");
            if (alarmPeriodString.equals(Period.HOURLY.toString())) alarmPeriod = Period.HOURLY;
            else if (alarmPeriodString.equals(Period.DAILY.toString())) alarmPeriod = Period.DAILY;
            else if (alarmPeriodString.equals(Period.WEEKLY.toString())) alarmPeriod = Period.WEEKLY;
            else if (alarmPeriodString.equals(Period.MONTHLY.toString())) alarmPeriod = Period.MONTHLY;
            else if (alarmPeriodString.equals(Period.QUARTERLY.toString())) alarmPeriod = Period.QUARTERLY;
            else if (alarmPeriodString.equals(Period.YEARLY.toString())) alarmPeriod = Period.YEARLY;
            else if (alarmPeriodString.equals(Period.CUSTOM.toString())) alarmPeriod = Period.CUSTOM;
            else alarmPeriod = Period.NONE;
        }
        return alarmPeriod;
    }

    public JEVisDataSource getDs() {
        return ds;
    }

    public JEVisAttribute getTimeStampAttribute() {
        if (timeStampAttribute == null) {
            try {
                timeStampAttribute = getObject().getAttribute(TIME_STAMP);
            } catch (JEVisException e) {
                logger.error("Could not get time stamp attribute: " + e);
            }
        }
        return timeStampAttribute;
    }

    public JEVisAttribute getLogAttribute() {
        return logAttribute;
    }

    public List<JEVisObject> getAlarmObjects() {
        if (alarmObjects == null) {
            alarmObjects = new ArrayList<>();

            String alarmObjectsString = sampleHandler.getLastSample(getObject(), ALARM_OBJECTS, "");
            TargetHelper th = new TargetHelper(ds, alarmObjectsString);
            if (th.getObject() != null && !th.getObject().isEmpty()) {
                alarmObjects.addAll(th.getObject());
            }
        }
        return alarmObjects;
    }
}
