package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.SimpleStringProperty;
import org.jevis.api.*;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.jeconfig.tool.I18n;

import java.util.List;

public class RowNote {
    private final JEVisObject dataObject;
    private SimpleStringProperty name;
    private SimpleStringProperty note;
    private SimpleStringProperty userNote;
    private Boolean changed = false;
    private JEVisSample sample;

    public RowNote(JEVisObject dataObject, JEVisSample sample, String name, String userNote) {
        this.name = new SimpleStringProperty(name);

        this.userNote = new SimpleStringProperty(userNote);
        this.dataObject = dataObject;
        this.sample = sample;

        StringBuilder formattedNote = new StringBuilder();
        String note = "";

        try {
            note = sample.getNote();
        } catch (JEVisException e) {

        }

//        if (note.contains(NoteConstants.Alignment.ALIGNMENT_YES)) {
//            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.alignedtrue"));
//            formattedNote.append(System.getProperty("line.separator"));
//        } else if (note.contains(NoteConstants.Alignment.ALIGNMENT_NO)) {
//            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.alignedfalse"));
//            formattedNote.append(System.getProperty("line.separator"));
//        }

//        if (note.contains(NoteConstants.Differential.DIFFERENTIAL_ON)) {
//            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.diff"));
//            formattedNote.append(System.getProperty("line.separator"));
//        }
//        if (note.contains(NoteConstants.Scaling.SCALING_ON)) {
//            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.scale"));
//            formattedNote.append(System.getProperty("line.separator"));
//        }

        if (note.contains(NoteConstants.Limits.LIMIT_STEP1)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit1"));
            formattedNote.append(System.getProperty("line.separator"));
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

        if (note.contains(NoteConstants.Calc.CALC_INFINITE)) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.calc.infinity"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        try {
            JEVisClass cleanDataClass = sample.getDataSource().getJEVisClass("Clean Data");
            JEVisObject object = sample.getAttribute().getObject();
            if (object.getJEVisClass().equals(cleanDataClass)) {
                JEVisAttribute log = object.getAttribute("Alarm Log");
                if (log != null) {
                    List<JEVisSample> logSamples = log.getSamples(sample.getTimestamp(), sample.getTimestamp());
                    if (logSamples != null && !logSamples.isEmpty()) {

                        Double valueAsDouble = logSamples.get(0).getValueAsDouble();
                        if (valueAsDouble.equals(1d)) {
                            formattedNote.append("Alarm normal");
                        } else if (valueAsDouble.equals(2d)) {
                            formattedNote.append("Alarm silent");
                        } else if (valueAsDouble.equals(4d)) {
                            formattedNote.append("Alarm standby");
                        }

                    }
                }
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
}