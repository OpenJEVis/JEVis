package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.SimpleStringProperty;
import org.jevis.api.*;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmType;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.DifferentialRule;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;

public class RowNote {
    private final JEVisObject dataObject;
    private final Double scaleFactor;
    private final SimpleStringProperty name;
    private final SimpleStringProperty note;
    private final SimpleStringProperty userNote;
    private final SimpleStringProperty userValue;
    private final SimpleStringProperty userValueUnit;
    private final Alarm alarm;
    private Boolean changed = false;
    private JEVisSample sample;

    public RowNote(JEVisObject dataObject, JEVisSample sample, JEVisSample userNoteSample, String name, String userNote, String userValue, String userValueUnit, Double scaleFactor, Alarm alarm) {
        this.name = new SimpleStringProperty(name);
        this.userNote = new SimpleStringProperty(userNote);
        this.userValue = new SimpleStringProperty(userValue);
        this.userValueUnit = new SimpleStringProperty(userValueUnit);
        this.dataObject = dataObject;
        this.scaleFactor = scaleFactor;
        this.sample = sample;
        this.alarm = alarm;

        StringBuilder formattedNote = new StringBuilder();
        NumberFormat nf = NumberFormat.getInstance(JEConfig.getConfig().getLocale());
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String note = "";

        try {
            note = sample.getNote();
            if (userNoteSample != null && !note.contains(NoteConstants.User.USER_NOTES)) {
                if (!userNoteSample.getValueAsString().isEmpty()) {
                    note += "," + NoteConstants.User.USER_NOTES;
                }
            }

            if (alarm != null) {
                note += "," + alarm.getAlarmType();
            }

        } catch (JEVisException e) {

        }

        if (note.contains(NoteConstants.Limits.LIMIT_STEP1)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit1"));
            formattedNote.append(System.getProperty("line.separator"));

            try {
                JEVisClass cleanDataClass = sample.getDataSource().getJEVisClass("Clean Data");
                JEVisObject object = sample.getAttribute().getObject();
                if (object.getJEVisClass().equals(cleanDataClass)) {
                    CleanDataObject cleanDataObject = new CleanDataObject(object, new ObjectHandler(object.getDataSource()));
                    List<JsonLimitsConfig> limitsConfig = cleanDataObject.getLimitsConfig();
                    for (JsonLimitsConfig limitsConfig1 : limitsConfig) {
                        int index = limitsConfig.indexOf(limitsConfig1);
                        if (index == 0) {
                            formattedNote.append("L1 Min: ");
                            formattedNote.append(limitsConfig1.getMin());
                            formattedNote.append(" ");
                            formattedNote.append(cleanDataObject.getValueAttribute().getDisplayUnit().getLabel());
                            formattedNote.append(System.getProperty("line.separator"));
                            formattedNote.append(" L1 Max: ");
                            formattedNote.append(limitsConfig1.getMax());
                            formattedNote.append(" ");
                            formattedNote.append(cleanDataObject.getValueAttribute().getDisplayUnit().getLabel());
                            formattedNote.append(System.getProperty("line.separator"));
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        if (note.contains(NoteConstants.Gap.GAP_DEFAULT)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.default"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Gap.GAP_STATIC)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.static"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Gap.GAP_AVERAGE)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.average"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Gap.GAP_MEDIAN)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.median"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Gap.GAP_INTERPOLATION)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.interpolation"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Gap.GAP_MIN)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.min"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Gap.GAP_MAX)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.max"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Limits.LIMIT_DEFAULT) || note.contains(NoteConstants.Limits.LIMIT_INTERPOLATION)
                || note.contains(NoteConstants.Limits.LIMIT_MEDIAN) || note.contains(NoteConstants.Limits.LIMIT_AVERAGE)
                || note.contains(NoteConstants.Limits.LIMIT_MAX) || note.contains(NoteConstants.Limits.LIMIT_MIN)
                || note.contains(NoteConstants.Limits.LIMIT_STATIC)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.limit"));
            formattedNote.append(System.getProperty("line.separator"));
            try {
                JEVisClass cleanDataClass = sample.getDataSource().getJEVisClass("Clean Data");
                JEVisObject object = sample.getAttribute().getObject();
                if (object.getJEVisClass().equals(cleanDataClass)) {
                    CleanDataObject cleanDataObject = new CleanDataObject(object, new ObjectHandler(object.getDataSource()));
                    List<JsonLimitsConfig> limitsConfig = cleanDataObject.getLimitsConfig();
                    for (JsonLimitsConfig limitsConfig1 : limitsConfig) {
                        int index = limitsConfig.indexOf(limitsConfig1);
                        if (index == 1) {
                            formattedNote.append("L2 Min: ");
                            formattedNote.append(limitsConfig1.getMin());
                            formattedNote.append(" ");
                            formattedNote.append(cleanDataObject.getValueAttribute().getDisplayUnit().getLabel());
                            formattedNote.append(System.getProperty("line.separator"));
                            formattedNote.append(" L2 Max: ");
                            formattedNote.append(limitsConfig1.getMax());
                            formattedNote.append(System.getProperty("line.separator"));
                            formattedNote.append(" ");
                            formattedNote.append(cleanDataObject.getValueAttribute().getDisplayUnit().getLabel());
                            formattedNote.append(System.getProperty("line.separator"));
                        }
                    }

                    DateTime timestamp = sample.getTimestamp();
                    JEVisAttribute rawDataValueAttribute = object.getParents().get(0).getAttribute("Value");
                    if (!cleanDataObject.getDifferentialRules().isEmpty()) {
                        for (DifferentialRule jeVisSample : cleanDataObject.getDifferentialRules()) {
                            if (jeVisSample.isDifferential()) {
                                if (timestamp.isAfter(jeVisSample.getStartOfPeriod())) {
                                    List<JEVisSample> samples = rawDataValueAttribute.getSamples(timestamp.minus(rawDataValueAttribute.getInputSampleRate()), timestamp);
                                    if (samples.size() == 2) {
                                        formattedNote.append("Original Value: ");
                                        formattedNote.append(nf.format(samples.get(1).getValueAsDouble() - samples.get(0).getValueAsDouble()));
                                        formattedNote.append(" ").append(cleanDataObject.getValueAttribute().getDisplayUnit().getLabel());
                                        formattedNote.append(System.getProperty("line.separator"));
                                    }
                                }
                            } else {
                                if (timestamp.isAfter(jeVisSample.getStartOfPeriod())) {
                                    List<JEVisSample> samples = rawDataValueAttribute.getSamples(timestamp, timestamp);
                                    if (!samples.isEmpty()) {
                                        formattedNote.append("Original: ");
                                        formattedNote.append(nf.format(samples.get(0).getValueAsDouble()));
                                        formattedNote.append(" ").append(cleanDataObject.getValueAttribute().getDisplayUnit().getLabel());
                                        formattedNote.append(System.getProperty("line.separator"));
                                    }
                                }
                            }
                        }
                    } else {
                        List<JEVisSample> samples = rawDataValueAttribute.getSamples(timestamp, timestamp);
                        if (!samples.isEmpty()) {
                            formattedNote.append("Original: ");
                            formattedNote.append(samples.get(0).getValueAsDouble());
                            formattedNote.append(System.getProperty("line.separator"));
                        }
                    }
                }

            } catch (Exception e) {
            }
        }


        if (note.contains(NoteConstants.Limits.LIMIT_DEFAULT)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.default"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Limits.LIMIT_STATIC)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.static"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Limits.LIMIT_AVERAGE)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.average"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Limits.LIMIT_MEDIAN)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.median"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Limits.LIMIT_INTERPOLATION)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.interpolation"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Limits.LIMIT_MIN)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.min"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Limits.LIMIT_MAX)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.max"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Differential.COUNTER_OVERFLOW)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.counteroverflow"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Calc.CALC_INFINITE)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.calc.infinity"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Forecast.FORECAST) && note.contains(NoteConstants.Forecast.FORECAST_AVERAGE)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.forecast.average"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Forecast.FORECAST) && note.contains(NoteConstants.Forecast.FORECAST_MEDIAN)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.forecast.median"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains(NoteConstants.Forecast.FORECAST) && note.contains(NoteConstants.Forecast.FORECAST_MIN)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.forecast.min"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains(NoteConstants.Forecast.FORECAST) && note.contains(NoteConstants.Forecast.FORECAST_MAX)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.forecast.max"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        try {
            if (alarm != null && (note.contains(AlarmType.DYNAMIC.toString()) || note.contains(AlarmType.STATIC.toString()))) {
                formattedNote.append(alarm.getTranslatedTypeName());
                formattedNote.append(System.getProperty("line.separator"));
                formattedNote.append(I18n.getInstance().getString("alarms.table.captions.currentvalue")).append(": ").append(alarm.getIsValue());
                formattedNote.append(System.getProperty("line.separator"));
                formattedNote.append(alarm.getOperator());
                formattedNote.append(System.getProperty("line.separator"));
                formattedNote.append(I18n.getInstance().getString("alarms.table.captions.setvalue")).append(": ").append(alarm.getSetValue());
            }
        } catch (Exception e) {
        }

        this.note = new SimpleStringProperty(formattedNote.toString());
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public String getUserNote() {
        return userNote.get();
    }

    public void setUserNote(String userNote) {
        this.userNote.set(userNote);
    }

    public SimpleStringProperty userNoteProperty() {
        return userNote;
    }

    public JEVisObject getDataObject() {
        return dataObject;
    }

    public Boolean getChanged() {
        return changed;
    }

    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    public JEVisSample getSample() {
        return sample;
    }

    public void setSample(JEVisSample sample) {
        this.sample = sample;
    }

    public String getUserValue() {
        return userValue.get();
    }

    public void setUserValue(String userValue) {
        this.userValue.set(userValue);
    }

    public String getUserValueUnit() {
        return userValueUnit.get();
    }

    public void setUserValueUnit(String userValueUnit) {
        this.userValueUnit.set(userValueUnit);
    }

    public SimpleStringProperty userValueUnitProperty() {
        return userValueUnit;
    }

    public SimpleStringProperty userValueProperty() {
        return userValue;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }
}