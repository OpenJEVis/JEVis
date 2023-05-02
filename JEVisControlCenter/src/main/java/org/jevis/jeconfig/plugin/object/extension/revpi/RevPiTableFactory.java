package org.jevis.jeconfig.plugin.object.extension.revpi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

public class RevPiTableFactory {



    public static TableView getTable(ObservableList observableList) {
        TableView<RevPiTrend> trendTableView = new TableView<>(observableList);
        trendTableView.setMinWidth(1000);
        trendTableView.setEditable(true);

        TableColumn< RevPiTrend, Boolean > selectedColumn = new TableColumn<>( "selected" );
        selectedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectedColumn));
        selectedColumn.setCellValueFactory(data -> data.getValue().selectedProperty());

        selectedColumn.setEditable(true);


        TableColumn< RevPiTrend, String > nameColumn = new TableColumn<>( "name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn< RevPiTrend, String > trendIdColumn = new TableColumn<>( "trendId");
        trendIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrendId()));

        TableColumn< RevPiTrend, String > configColumn = new TableColumn<>( "config");
        configColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getConfig()));

        trendTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        trendTableView.getColumns().addAll(selectedColumn, nameColumn, trendIdColumn,configColumn);

        return trendTableView;


    }



}
