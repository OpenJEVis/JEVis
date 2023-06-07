/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.sample.csvexporttable;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Old TableView for JEVisSamples, used by CSV Export Plugin.
 *
 * @deprecated replaced by CSVExportTableSampleTable
 */
public class CSVExportTableSampleTable extends TableView {
    static final DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
    private static final Logger logger = LogManager.getLogger(CSVExportTableSampleTable.class);
    private JEVisAttribute attribute;

    public CSVExportTableSampleTable(JEVisAttribute attribute, List<JEVisSample> samples) {
        super();
        this.attribute = attribute;
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setPlaceholder(new Label("No Data"));

        build();

        setMinWidth(555d);//TODo: replace Dirty workaround
        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        List<CSVExportTableSample> tjc = new LinkedList<>();
        for (JEVisSample sample : samples) {
            tjc.add(new CSVExportTableSample(sample));
        }

        final ObservableList<CSVExportTableSample> data = FXCollections.observableArrayList(tjc);
        setItems(data);

    }

    private void build() {

        Callback<TableColumn, TableCell> cellFactory = new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell();
            }
        };

        TableColumn dateCol = new TableColumn("Date");

        dateCol.setMinWidth(100);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<CSVExportTableSample, String>("date"));
        dateCol.setCellFactory(cellFactory);

        dateCol.setEditable(true);

        TableColumn valueCol = new TableColumn("Value");

        valueCol.setMinWidth(100);
        valueCol.setCellValueFactory(new PropertyValueFactory<CSVExportTableSample, String>("value"));
        valueCol.setCellFactory(cellFactory);


        valueCol.setEditable(true);

        TableColumn noteCol = new TableColumn("Note");

        noteCol.setMinWidth(200);
        noteCol.setCellValueFactory(new PropertyValueFactory<CSVExportTableSample, String>("note"));
        noteCol.setCellFactory(cellFactory);

        noteCol.setEditable(true);

        getColumns().addAll(dateCol, valueCol, noteCol);

        setEditable(true);

        dateCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CSVExportTableSample, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CSVExportTableSample, String> t
                    ) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setDate(t.getNewValue());
                    }
                }
        );

        try {
            if (attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                valueCol.setOnEditStart(new EventHandler<TableColumn.CellEditEvent>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent event) {
                        try {
                            event.consume();
                            JEVisSample sample = ((CSVExportTableSample) event.getTableView().getItems().get(event.getTablePosition().getRow())).getSample();
                            logger.info("Sample: " + sample.getTimestamp());

                            try {
//                                loadWithAnimation();
                                JEVisFile file = sample.getValueAsFile();

                                FileChooser fileChooser = new FileChooser();
                                fileChooser.setInitialFileName(file.getFilename());
                                fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
                                fileChooser.getExtensionFilters().addAll(
                                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                                File selectedFile = fileChooser.showSaveDialog(null);
                                if (selectedFile != null) {
                                    ControlCenter.setLastPath(selectedFile);
                                    file.saveToFile(selectedFile);
                                }
                            } catch (Exception ex) {
                                logger.fatal(ex);
                            }


                        } catch (Exception ex) {
                            logger.fatal(ex);
                        }
                    }
                });
                valueCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent event) {
                        event.consume();
                    }
                });

            } else {
                valueCol.setOnEditCommit(
                        new EventHandler<TableColumn.CellEditEvent<CSVExportTableSample, String>>() {
                            @Override
                            public void handle(TableColumn.CellEditEvent<CSVExportTableSample, String> t) {
                                t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
                            }
                        }
                );
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }


        noteCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<CSVExportTableSample, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<CSVExportTableSample, String> t
                    ) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setNote(t.getNewValue());
                    }
                }
        );
    }

    public class TableSample {

        private SimpleStringProperty date = new SimpleStringProperty("Error");
        private SimpleDoubleProperty value = new SimpleDoubleProperty(0d);
        private SimpleStringProperty note = new SimpleStringProperty("Error");

        private JEVisSample _sample = null;


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
                logger.fatal(ex);
                return value.get();
            }
        }

        public void setValue(Double fName) {
            value.set(fName);
        }
    }
}
