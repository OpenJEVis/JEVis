package org.jevis.jeconfig.plugin.alarms;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class AlarmRow {

    private static final Logger logger = LogManager.getLogger(AlarmRow.class);
    private final AlarmConfiguration alarmConfiguration;
    private final ObjectProperty<DateTime> dateTime;
    private final ObjectProperty<Alarm> alarm;
    private final StringProperty location;
    private final StringProperty configName;
    private final ObjectProperty<JEVisObject> objectName;
    private final DoubleProperty isValue;
    private final DoubleProperty setValue;
    private final StringProperty unit;
    private final StringProperty operator;
    private final IntegerProperty logValue;
    private final DoubleProperty tolerance;
    private final StringProperty alarmType;
    private final BooleanProperty checked;
    private final Boolean isLinkDisabled;
    private final String formatString;

    public AlarmRow(ObjectRelations objectRelations, AlarmConfiguration alarmConfiguration, Alarm alarm) {
        this.alarmConfiguration = alarmConfiguration;
        this.dateTime = new SimpleObjectProperty<>(this, "dateTime", alarm.getTimeStamp());
        this.configName = new SimpleStringProperty(this, "configName", alarmConfiguration.getName());
        this.objectName = new SimpleObjectProperty<>(this, "objectName", alarm.getObject());
        this.isLinkDisabled = alarmConfiguration.isLinkDisabled();

        this.isValue = new SimpleDoubleProperty(this, "isValue");
        try {
            this.isValue.set(alarm.getIsValue());
        } catch (Exception e) {
            logger.error(e);
        }

        this.setValue = new SimpleDoubleProperty(this, "setValue");
        try {
            this.setValue.set(alarm.getSetValue());
        } catch (Exception e) {
            logger.error(e);
        }

        this.unit = new SimpleStringProperty(this, "unit");
        try {
            unit.set(UnitManager.getInstance().format(alarm.getAttribute().getDisplayUnit()));
        } catch (Exception e) {
            logger.error(e);
        }

        this.operator = new SimpleStringProperty(this, "operator");
        try {
            this.operator.set(alarm.getOperator());
        } catch (Exception e) {
            logger.error(e);
        }

        this.logValue = new SimpleIntegerProperty(this, "logValue");
        try {
            this.logValue.set(alarm.getLogValue());
        } catch (Exception e) {
            logger.error(e);
        }

        this.tolerance = new SimpleDoubleProperty(this, "tolerance");
        try {
            this.tolerance.set(alarm.getTolerance());
        } catch (Exception e) {
            logger.error(e);
        }

        this.alarmType = new SimpleStringProperty(this, "alarmType", alarm.getTranslatedTypeName());
        this.checked = new SimpleBooleanProperty(this, "confirmation", alarmConfiguration.isChecked());
        this.alarm = new SimpleObjectProperty<>(this, "alarm", alarm);

        JEVisDataSource ds = alarmConfiguration.getDs();

        Period periodForDate = CleanDataObject.getPeriodForDate(alarm.getObject(), alarm.getTimeStamp());
        this.formatString = PeriodHelper.getFormatString(periodForDate, false);

        String location = "";

        if (ChartTools.isMultiSite(ds)) {
            location += objectRelations.getObjectPath(alarmConfiguration.getObject());
        }
        if (ChartTools.isMultiDir(ds, alarmConfiguration.getObject())) {
            location += objectRelations.getRelativePath(alarmConfiguration.getObject());
        }

        this.location = new SimpleStringProperty(this, "location", location);
    }

    public Boolean isLinkDisabled() {
        return isLinkDisabled;
    }

    public DateTime getDateTime() {
        return dateTime.get();
    }

    public ObjectProperty<DateTime> dateTimeProperty() {
        return dateTime;
    }

    public String getConfigName() {
        return configName.get();
    }

    public StringProperty configNameProperty() {
        return configName;
    }

    public JEVisObject getObjectName() {
        return objectName.get();
    }

    public ObjectProperty<JEVisObject> objectNameProperty() {
        return objectName;
    }

    public String getLocation() {
        return location.get();
    }

    public StringProperty locationProperty() {
        return location;
    }

    public double getIsValue() {
        return isValue.get();
    }

    public DoubleProperty isValueProperty() {
        return isValue;
    }

    public String getUnit() {
        return unit.get();
    }

    public StringProperty unitProperty() {
        return unit;
    }

    public Alarm getAlarm() {
        return alarm.get();
    }

    public ObjectProperty<Alarm> alarmProperty() {
        return alarm;
    }

    public double getSetValue() {
        return setValue.get();
    }

    public DoubleProperty setValueProperty() {
        return setValue;
    }

    public String getOperator() {
        return operator.get();
    }

    public StringProperty operatorProperty() {
        return operator;
    }

    public int getLogValue() {
        return logValue.get();
    }

    public IntegerProperty logValueProperty() {
        return logValue;
    }

    public double getTolerance() {
        return tolerance.get();
    }

    public DoubleProperty toleranceProperty() {
        return tolerance;
    }

    public String getAlarmType() {
        return alarmType.get();
    }

    public StringProperty alarmTypeProperty() {
        return alarmType;
    }

    public boolean isChecked() {
        return checked.get();
    }

    public void setChecked(Boolean checked) {
        this.alarmConfiguration.setChecked(checked);
    }

    public BooleanProperty checkedProperty() {
        return checked;
    }

    public String getFormatString() {
        return formatString;
    }

    public AlarmConfiguration getAlarmConfiguration() {
        return alarmConfiguration;
    }
}
