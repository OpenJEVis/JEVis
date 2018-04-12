/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.sampletable.EditingCell;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTable extends TableView {

    static final DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");

    public SampleTable(List<JEVisSample> samples) {
        super();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setPlaceholder(new Label("No Data"));
        
        build();
//        TableColumn tsColum = new TableColumn("Timestamp");
//        tsColum.setCellValueFactory(new PropertyValueFactory<TableSample, String>("Date"));
//
//        TableColumn valueColum = new TableColumn("Value");
//        valueColum.setCellValueFactory(new PropertyValueFactory<TableSample, Double>("Value"));
//
//        TableColumn noteColum = new TableColumn("Note");
//        noteColum.setCellValueFactory(new PropertyValueFactory<TableSample, String>("Note"));

        setMinWidth(555d);//TODo: replace Dirty workaround
        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        getColumns().addAll(tsColum, valueColum, noteColum);


        List<org.jevis.jeconfig.sampletable.TableSample> tjc = new LinkedList<>();
        for (JEVisSample sample : samples) {
            tjc.add(new org.jevis.jeconfig.sampletable.TableSample(sample));
        }

        final ObservableList<org.jevis.jeconfig.sampletable.TableSample> data = FXCollections.observableArrayList(tjc);
        setItems(data);

    }

    public class TableSample {

        private SimpleStringProperty date = new SimpleStringProperty("Error");
        private SimpleDoubleProperty value = new SimpleDoubleProperty(0d);
        private SimpleStringProperty note = new SimpleStringProperty("Error");

        private JEVisSample _sample = null;

        /**
         *
         * @param relation
         * @param jclass
         */
        public TableSample(JEVisSample sample) {
            try {
                this.date = new SimpleStringProperty(fmtDate.print(sample.getTimestamp()));
                this.value = new SimpleDoubleProperty(sample.getValueAsDouble());
                this.note = new SimpleStringProperty(sample.getNote());
                _sample = sample;
            } catch (Exception ex) {
            }
        }

        public JEVisSample getSample() {
            return _sample;
        }

        public String getDate() {
            return date.get();
        }

        public void setDate(String fName) {
            date.set(fName);
        }

        public String getNote() {
            return note.get();
        }

        public void setNote(String noteString) {
            note.set(noteString);
        }

        public Double getValue() {
            try {
                return _sample.getValueAsDouble(_sample.getAttribute().getDisplayUnit());
//            return value.get();
            } catch (JEVisException ex) {
                Logger.getLogger(SampleTable.class.getName()).log(Level.SEVERE, null, ex);
                return value.get();
            }
        }

        public void setValue(Double fName) {
            value.set(fName);
        }
    }
        private void build() {

        Callback<TableColumn, TableCell> cellFactory = new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell();
            }
        };

        TableColumn dateCol = new TableColumn("Date");

        dateCol.setMinWidth(
                100);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<org.jevis.jeconfig.sampletable.TableSample, String>("date"));
        dateCol.setCellFactory(cellFactory);

        dateCol.setEditable(
                false);

        TableColumn valueCol = new TableColumn("Value");

        valueCol.setMinWidth(
                100);
        valueCol.setCellValueFactory(
                new PropertyValueFactory<org.jevis.jeconfig.sampletable.TableSample, String>("value"));
        valueCol.setCellFactory(cellFactory);

        valueCol.setEditable(
                true);

        TableColumn noteCol = new TableColumn("Note");

        noteCol.setMinWidth(
                200);
        noteCol.setCellValueFactory(
                new PropertyValueFactory<org.jevis.jeconfig.sampletable.TableSample, String>("note"));
        noteCol.setCellFactory(cellFactory);

        noteCol.setEditable(true);

        //Add the columns and data to the table.
//        _table.setItems(_data);
//        setItems(_data);

//        _table.getColumns()
//                .addAll(dateCol, valueCol, noteCol);
        getColumns().addAll(dateCol, valueCol, noteCol);

        //Make the table editable
//        _table.setEditable(true);
        setEditable(true);

        //Modifying the firstName property
        dateCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String> t
            ) {
                ((org.jevis.jeconfig.sampletable.TableSample) t.getTableView().getItems().get(t.getTablePosition().getRow())).setDate(t.getNewValue());
            }
        }
        );
        //Modifying the lastName property
        valueCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String> t
            ) {
                ((org.jevis.jeconfig.sampletable.TableSample) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
            }
        }
        );
        //Modifying the primary email property
        noteCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String> t
            ) {
                ((org.jevis.jeconfig.sampletable.TableSample) t.getTableView().getItems().get(t.getTablePosition().getRow())).setNote(t.getNewValue());
            }
        }
        );
    }
}
