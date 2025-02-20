package org.jevis.jecc.plugin.loadprofile.data;

import javafx.beans.property.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

public class TableSample {
    private final static Logger logger = LogManager.getLogger(TableSample.class);
    private final DoubleProperty value = new SimpleDoubleProperty(this, "value");
    private final StringProperty note = new SimpleStringProperty(this, "note", "");
    private final ObjectProperty<DateTime> dateTime = new SimpleObjectProperty<>(this, "dateTime");
    private final DoubleProperty diff = new SimpleDoubleProperty(this, "diff");

    private JEVisSample jevisSample = null;
    private boolean holiday = false;


    private TableSample() {
    }

    public TableSample(JEVisSample sample) {
        this.jevisSample = sample;
        loadSampleData();
    }

    public void loadSampleData() {
        try {
            setDateTime(jevisSample.getTimestamp());
            setValue(jevisSample.getValueAsDouble());
            setNote(jevisSample.getNote());
        } catch (Exception ex) {
            logger.error("Error while loading sample", ex);
        }
    }

    public Double getValue() {
        return value.get();
    }

    public void setValue(Double value) {
        this.value.setValue(value);
    }

    public DoubleProperty valueProperty() {
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

    public DateTime getDateTime() {
        return dateTime.get();
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime.set(dateTime);
    }

    public ObjectProperty<DateTime> dateTimeProperty() {
        return dateTime;
    }

    public JEVisSample getJEVisSample() {
        return jevisSample;
    }

    public void setJEVisSample(JEVisSample jevisSample) {
        this.jevisSample = jevisSample;
    }

    public double getDiff() {
        return this.diff.get();
    }

    public void setDiff(Double diff) {
        this.diff.set(diff);
    }

    public DoubleProperty diffProperty() {
        return this.diff;
    }

    public boolean isHoliday() {
        return holiday;
    }

    public void setHoliday(boolean isHoliday) {
        this.holiday = isHoliday;
    }
}