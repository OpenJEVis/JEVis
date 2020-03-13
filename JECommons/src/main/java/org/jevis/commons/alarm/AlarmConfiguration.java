package org.jevis.commons.alarm;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.i18n.I18n;
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
    private final String DISABLE_LINK = "Disable Link";
    private final String ALARM_SCOPE = "Alarm Scope";
    private final String ALARM_PERIOD = "Alarm Period";
    private final String ALARM_OBJECTS = "Alarm Objects";
    private final String LOG_FILE = "Log File";
    public final static String ALARM_CHECKED = "Alarm Checked";
    private final JEVisObject object;
    private final JEVisDataSource ds;
    private AlarmScope alarmScope = AlarmScope.NONE;
    private DateTime timeStamp;
    private Period alarmPeriod = Period.NONE;
    private JEVisAttribute timeStampAttribute;
    private JEVisAttribute logAttribute;
    private List<JEVisObject> alarmObjects;

    public AlarmConfiguration(JEVisDataSource ds, JEVisObject jeVisObject) {
        this.ds = ds;
        this.object = jeVisObject;
    }

    public Boolean isEnabled() {
        try {
            JEVisAttribute enabledAtt = object.getAttribute(ENABLED_NAME);
            if (enabledAtt != null) {
                JEVisSample latestSample = enabledAtt.getLatestSample();
                if (latestSample != null) {
                    return latestSample.getValueAsBoolean();
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean isLinkDisabled() {
        try {
            JEVisAttribute disabledAtt = object.getAttribute(DISABLE_LINK);
            if (disabledAtt != null) {
                JEVisSample latestSample = disabledAtt.getLatestSample();
                if (latestSample != null) {
                    return latestSample.getValueAsBoolean();
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return false;
    }

    public JEVisAttribute getCheckedAttribute() {
        try {
            return object.getAttribute(ALARM_CHECKED);
        } catch (JEVisException e) {
            logger.error("Could not get checked attribute for object {}:{}", object.getName(), object.getID(), e);
        }
        return null;
    }

    public JEVisAttribute getFileLogAttribute() {
        try {
            return object.getAttribute(LOG_FILE);
        } catch (JEVisException e) {
            logger.error("Could not get file log attribute for object {}:{}", object.getName(), object.getID(), e);
        }
        return null;
    }

    public Boolean isChecked() {
        try {
            JEVisSample latestSample = getCheckedAttribute().getLatestSample();
            if (latestSample != null) {
                return latestSample.getValueAsBoolean();
            } else return false;
        } catch (JEVisException e) {
            logger.error("Could not get checked status for object {}:{}", object.getName(), object.getID(), e);
        }
        return false;
    }

    public void setChecked(Boolean checked) {
        try {
            if (ds.getCurrentUser().canWrite(object.getID())) {
                JEVisAttribute checkedAttribute = object.getAttribute(ALARM_CHECKED);
                if (checkedAttribute != null) {
                    JEVisSample sample = checkedAttribute.buildSample(new DateTime(), checked);
                    if (checked) {
                        sample.setNote("Checked by " + ds.getCurrentUser().getAccountName());
                    } else {
                        sample.setNote("Unchecked by " + ds.getCurrentUser().getAccountName());
                    }
                    sample.commit();
                }
            } else {
                Platform.runLater(() -> {
                    Alert alert1 = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.title"));
                    alert1.setContentText(I18n.getInstance().getString("dialog.warning.notallowed"));
                    alert1.showAndWait();
                });
            }
        } catch (Exception e) {
            logger.error("Could not set checked attribute for object {}:{}", object.getName(), object.getID(), e);
        }
    }

    public JEVisObject getObject() {
        return object;
    }

    public Long getId() {
        return object.getID();
    }

    public String getName() {
        return object.getName();
    }

    public AlarmScope getAlarmScope() {

        try {
            JEVisAttribute scopeAtt = object.getAttribute(ALARM_SCOPE);
            if (scopeAtt != null) {
                JEVisSample latestSample = scopeAtt.getLatestSample();
                if (latestSample != null) {
                    String scope = latestSample.getValueAsString();
                    if (scope.equals(AlarmScope.COMPLETE.toString())) alarmScope = AlarmScope.COMPLETE;
                    else if (scope.equals(AlarmScope.SELECTED.toString())) alarmScope = AlarmScope.SELECTED;
                    else if (scope.equals(AlarmScope.WITHOUT_SELECTED.toString()))
                        alarmScope = AlarmScope.WITHOUT_SELECTED;
                    else alarmScope = AlarmScope.NONE;
                }

            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return alarmScope;
    }

    public DateTime getTimeStamp() {
        try {
            JEVisAttribute timeStampAtt = object.getAttribute(TIME_STAMP);
            if (timeStampAtt != null) {
                JEVisSample latestSample = timeStampAtt.getLatestSample();
                if (latestSample != null) {
                    String dateTimeString = latestSample.getValueAsString();
                    if (!dateTimeString.equals("")) timeStamp = new DateTime(dateTimeString);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }

    public Period getAlarmPeriod() {
        try {
            JEVisAttribute periodAtt = object.getAttribute(ALARM_PERIOD);
            if (periodAtt != null) {
                JEVisSample latestSample = periodAtt.getLatestSample();
                if (latestSample != null) {
                    String alarmPeriodString = latestSample.getValueAsString();
                    if (alarmPeriodString.equals(Period.MINUTELY.toString())) alarmPeriod = Period.MINUTELY;
                    else if (alarmPeriodString.equals(Period.QUARTER_HOURLY.toString()))
                        alarmPeriod = Period.QUARTER_HOURLY;
                    else if (alarmPeriodString.equals(Period.HOURLY.toString())) alarmPeriod = Period.HOURLY;
                    else if (alarmPeriodString.equals(Period.DAILY.toString())) alarmPeriod = Period.DAILY;
                    else if (alarmPeriodString.equals(Period.WEEKLY.toString())) alarmPeriod = Period.WEEKLY;
                    else if (alarmPeriodString.equals(Period.MONTHLY.toString())) alarmPeriod = Period.MONTHLY;
                    else if (alarmPeriodString.equals(Period.QUARTERLY.toString())) alarmPeriod = Period.QUARTERLY;
                    else if (alarmPeriodString.equals(Period.YEARLY.toString())) alarmPeriod = Period.YEARLY;
                    else if (alarmPeriodString.equals(Period.CUSTOM.toString())) alarmPeriod = Period.CUSTOM;
                    else alarmPeriod = Period.NONE;
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return alarmPeriod;
    }

    public JEVisDataSource getDs() {
        return ds;
    }

    public JEVisAttribute getTimeStampAttribute() {
        if (timeStampAttribute == null) {
            try {
                timeStampAttribute = object.getAttribute(TIME_STAMP);
            } catch (JEVisException e) {
                logger.error("Could not get time stamp attribute: ", e);
            }
        }
        return timeStampAttribute;
    }

    public JEVisAttribute getLogAttribute() {
        if (logAttribute == null) {
            try {
                logAttribute = object.getAttribute(LOG);
            } catch (JEVisException e) {
                logger.error("Could not get log attribute: ", e);
            }
        }
        return logAttribute;
    }

    public List<JEVisObject> getAlarmObjects() {
        alarmObjects = new ArrayList<>();

        try {
            JEVisAttribute objectsAtt = object.getAttribute(ALARM_OBJECTS);
            if (objectsAtt != null) {
                JEVisSample latestSample = objectsAtt.getLatestSample();
                if (latestSample != null) {
                    String alarmObjectsString = latestSample.getValueAsString();
                    TargetHelper th = new TargetHelper(ds, alarmObjectsString);
                    if (th.getObject() != null && !th.getObject().isEmpty()) {
                        alarmObjects.addAll(th.getObject());
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return alarmObjects;
    }
}
