/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sample.csvexporttable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisSample;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Benjamin Reich
 * @deprecated
 */
public class SampleTableView extends TableView {

    //    private TableView _table = new TableView();
    private final ObservableList<CSVExportTableSample> _data;
    private double _lastBarPositionV;
    private double _lastBarPositionH;
    private JEVisDataSource _ds;

    public SampleTableView(List<JEVisSample> samples) {
        super();
        List<CSVExportTableSample> tjc = new LinkedList<>();
        for (JEVisSample sample : samples) {
            tjc.add(new CSVExportTableSample(sample));
        }
        _data = FXCollections.observableArrayList(tjc);
        setMinWidth(555d);//TODo: replace Dirty workaround
        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        build();

        setItems(_data);

    }

    private void build() {

        Callback<TableColumn, TableCell> cellFactory = p -> new EditingCell();

        TableColumn dateCol = new TableColumn("Date");

        dateCol.setMinWidth(100);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<CSVExportTableSample, String>("date"));
        dateCol.setCellFactory(cellFactory);

        dateCol.setEditable(
                false);

        TableColumn valueCol = new TableColumn("Value");

        valueCol.setMinWidth(100);
        valueCol.setCellValueFactory(
                new PropertyValueFactory<CSVExportTableSample, String>("value"));

        valueCol.setCellFactory(cellFactory);

        valueCol.setEditable(true);

        TableColumn noteCol = new TableColumn("Note");

        noteCol.setMinWidth(200);

        noteCol.setCellValueFactory(
                new PropertyValueFactory<CSVExportTableSample, String>("note"));
        noteCol.setCellFactory(cellFactory);

        noteCol.setEditable(true);

        dateCol.setOnEditCommit(
                new EventHandler<CellEditEvent<CSVExportTableSample, String>>() {
                    @Override
                    public void handle(CellEditEvent<CSVExportTableSample, String> t
                    ) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setDate(t.getNewValue());
                    }
                }
        );
        //Modifying the Value property

        valueCol.setOnEditCommit(
                new EventHandler<CellEditEvent<CSVExportTableSample, String>>() {
                    @Override
                    public void handle(CellEditEvent<CSVExportTableSample, String> t
                    ) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());

                    }
                }
        );
        //Modifying the note property
        noteCol.setOnEditCommit(
                new EventHandler<CellEditEvent<CSVExportTableSample, String>>() {
                    @Override
                    public void handle(CellEditEvent<CSVExportTableSample, String> t
                    ) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setNote(t.getNewValue());
                    }
                }
        );

        setItems(_data);

        setEditable(true);

        getColumns().addAll(dateCol, valueCol, noteCol);
    }

}
