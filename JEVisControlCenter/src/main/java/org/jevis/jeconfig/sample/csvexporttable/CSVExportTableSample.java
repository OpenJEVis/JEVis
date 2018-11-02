/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sample.csvexporttable;

import javafx.beans.property.SimpleStringProperty;
import org.jevis.api.JEVisSample;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Old TableView for JEVisSamples, used by CSV Export Plugin.
 *
 * @author Benjamin Reich
 * @deprecated replaced by CSVExportTableSampleTable
 */
public class CSVExportTableSample {


    static final DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");

    private final SimpleStringProperty _date = new SimpleStringProperty("ERROR");
    private final SimpleStringProperty _value = new SimpleStringProperty("ERROR");
    private final SimpleStringProperty _note = new SimpleStringProperty("ERROR");
    private JEVisSample _sample;

    public CSVExportTableSample(JEVisSample sample) {
        try {
            _date.set(fmtDate.print(sample.getTimestamp()));
        } catch (Exception ex) {

        }
        try {
            _value.set(sample.getValueAsString());
        } catch (Exception ex) {

        }
        try {
            _note.set(sample.getNote());
        } catch (Exception ex) {

        }
        _sample = sample;
    }

    public String getDate() {
        return _date.get();
    }

    public void setDate(String date) {
        _date.set(date);
    }

    public SimpleStringProperty dateProperty() {
        return _date;
    }

    public String getValue() {
        return _value.get();
    }

    public void setValue(String value) {
        _value.set(value);
    }

    public SimpleStringProperty valueProperty() {
        return _value;
    }

    public String getNote() {
        return _note.get();
    }

    public void setNote(String note) {
        _note.set(note);
    }

    public SimpleStringProperty getPrimaryEmailProperty() {
        return _note;
    }

    public JEVisSample getSample() {
        return _sample;
    }

    public void setSample(JEVisSample sample) {
        _sample = sample;
    }
}
