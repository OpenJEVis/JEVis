package org.jevis.jecc.sample.tableview;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

/**
 * Inner Class to capsule JEVisSample to be manipulated in a table
 * <p>
 * TODO:
 * - Revert changed function
 */
public class TableSample {
    private final static Logger logger = LogManager.getLogger(TableSample.class);
    private final ObjectProperty<Object> value = new SimpleObjectProperty<>(this, "value");
    private final StringProperty note = new SimpleStringProperty(this, "note", "");
    private final ObjectProperty<DateTime> timeStamp = new SimpleObjectProperty<>(this, "timeStamp");
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(this, "selected", false);

    private JEVisSample jevisSample = null;
    private boolean isNew = false;


    private TableSample() {
    }

    public TableSample(JEVisSample sample) {
        this.jevisSample = sample;
        loadSampleData();
    }

    public void loadSampleData() {
        try {
            setTimeStamp(jevisSample.getTimestamp());
            setValue(jevisSample.getValue());
            setNote(jevisSample.getNote());
        } catch (Exception ex) {
            logger.error("Error while loading sample", ex);
        }
    }


    public boolean hasChanged() {
        try {
            if (!jevisSample.getTimestamp().equals(timeStamp.getValue())) {
                return true;
            }

            if (!jevisSample.getNote().equals(note.getValue())) {
                return true;
            }
            if (!jevisSample.getValue().equals(value.getValue())) {
                return true;
            }

        } catch (Exception ex) {
            logger.error("Error while checking for changes", ex);
        }

        return false;


    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public Object getValue() {
        return value.get();
    }

    public void setValue(Object value) {
        this.value.setValue(value);
    }

    public ObjectProperty<Object> valueProperty() {
        return value;
    }

    public String getNote() {
        return note.getValue();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public StringProperty noteProperty() {
        return note;
    }

    public DateTime getTimeStamp() {
        return timeStamp.get();
    }

    public void setTimeStamp(DateTime timeStamp) {
        this.timeStamp.set(timeStamp);
    }

    public ObjectProperty<DateTime> timeStampProperty() {
        return timeStamp;
    }

    public JEVisSample getJEVisSample() {
        return jevisSample;
    }

    public void setJEVisSample(JEVisSample jevisSample) {
        this.jevisSample = jevisSample;
    }

    @Override
    public String toString() {
        return "CSVExportTableSample{" +
                "value=" + value +
                ", note=" + note +
                ", timeStamp=" + timeStamp +
                ", jevisSample=" + jevisSample +
                '}';
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}