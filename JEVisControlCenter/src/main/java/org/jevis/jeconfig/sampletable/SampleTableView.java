/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.sampletable;

import java.util.LinkedList;
import java.util.List;
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

/**
 *
 * @author Benjamin Reich
 */
public class SampleTableView extends TableView {

    private double _lastBarPositionV;
    private double _lastBarPositionH;
    private JEVisDataSource _ds;
//    private TableView _table = new TableView();
    private final ObservableList<TableSample> _data;

    public SampleTableView(List<JEVisSample> samples) {
        super();
        List<TableSample> tjc = new LinkedList<>();
        for (JEVisSample sample : samples) {
            tjc.add(new TableSample(sample));
        }
        _data = FXCollections.observableArrayList(tjc);
        setMinWidth(555d);//TODo: replace Dirty workaround
        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        build();
        
        setItems(_data);
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
                new PropertyValueFactory<TableSample, String>("date"));
        dateCol.setCellFactory(cellFactory);

        dateCol.setEditable(
                false);

        TableColumn valueCol = new TableColumn("Value");

        valueCol.setMinWidth(100);
        valueCol.setCellValueFactory(
                new PropertyValueFactory<TableSample, String>("value"));
        valueCol.setCellFactory(cellFactory);

        valueCol.setEditable(
                true);

        TableColumn noteCol = new TableColumn("Note");

        noteCol.setMinWidth(200);
        
        noteCol.setCellValueFactory(
                new PropertyValueFactory<TableSample, String>("note"));
        noteCol.setCellFactory(cellFactory);

        noteCol.setEditable(true);

        setItems(_data);

        getColumns()
                .addAll(dateCol, valueCol, noteCol);

        setEditable(true);

        dateCol.setOnEditCommit(
                new EventHandler<CellEditEvent<TableSample, String>>() {
            @Override
            public void handle(CellEditEvent<TableSample, String> t
            ) {
                ((TableSample) t.getTableView().getItems().get(t.getTablePosition().getRow())).setDate(t.getNewValue());
            }
        }
        );
        //Modifying the lastName property
        valueCol.setOnEditCommit(
                new EventHandler<CellEditEvent<TableSample, String>>() {
            @Override
            public void handle(CellEditEvent<TableSample, String> t
            ) {
                ((TableSample) t.getTableView().getItems().get(t.getTablePosition().getRow())).setValue(t.getNewValue());
            }
        }
        );
        //Modifying the primary email property
        noteCol.setOnEditCommit(
                new EventHandler<CellEditEvent<TableSample, String>>() {
            @Override
            public void handle(CellEditEvent<TableSample, String> t
            ) {
                ((TableSample) t.getTableView().getItems().get(t.getTablePosition().getRow())).setNote(t.getNewValue());
            }
        }
        );
    }

}
