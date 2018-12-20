package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.SimpleStringProperty;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.tool.I18n;

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

        if (note.contains("alignment(yes")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.alignedtrue"));
            formattedNote.append(System.getProperty("line.separator"));
        } else if (note.contains("alignment(no")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.alignedfalse"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("diff")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.diff"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("scale")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.scale"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("limit(Step1)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit1"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("gap(Default")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.default"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("gap(Static")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.static"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("gap(Average")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.average"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("gap(Median")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.median"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("gap(Interpolation")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.interpolation"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("gap(Min")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.min"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("gap(Max")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.gap.max"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("limit(Default)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.default"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("limit(Static)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.static"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("limit(Average)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.average"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("limit(Median)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.median"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("limit(Interpolation)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.interpolation"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("limit(Min)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.min"));
            formattedNote.append(System.getProperty("line.separator"));
        }
        if (note.contains("limit(Max)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.limit2.max"));
            formattedNote.append(System.getProperty("line.separator"));
        }

        if (note.contains("calc(infinite)")) {
            formattedNote.append(I18n.getInstance().getString("graph.dialog.note.text.calc.infinity"));
            formattedNote.append(System.getProperty("line.separator"));
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