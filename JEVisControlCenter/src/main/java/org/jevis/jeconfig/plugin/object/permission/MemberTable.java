/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MemberTable extends TableView {

    public MemberTable(JEVisObject obj) {
        super();

        TableColumn ownerColumn = new TableColumn("Member");
        ownerColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, String>("member"));

        TableColumn readColumn = new TableColumn("Read");
        readColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("read"));

        TableColumn writeColumn = new TableColumn("Write");
        writeColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("write"));

        TableColumn deleteColumn = new TableColumn("Delete");
        deleteColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("delete"));

        TableColumn execColumn = new TableColumn("Excecute");
        execColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("exce"));

        TableColumn createColumn = new TableColumn("Create");
        createColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("create"));

        setBooleanCellrenderer(readColumn);
        setBooleanCellrenderer(execColumn);
        setBooleanCellrenderer(createColumn);
        setBooleanCellrenderer(writeColumn);
        setBooleanCellrenderer(deleteColumn);

        setMinWidth(555d);//TODo: replace this dirty workaround
        setPrefHeight(200d);//TODo: replace this dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getColumns().addAll(ownerColumn, readColumn, deleteColumn, execColumn, createColumn);

//        List<JEVisRelationship> relPerOwner = new ArrayList<>();
        Map<JEVisObject, List<JEVisRelationship>> members = new HashMap<>();
        System.out.println("MemberTable.getRealtionships");

        try {
            for (JEVisRelationship rel : obj.getRelationships()) {
                if (rel.getType() == MEMBER_READ || rel.getType() == MEMBER_WRITE || rel.getType() == MEMBER_DELETE
                        || rel.getType() == MEMBER_EXECUTE || rel.getType() == MEMBER_CREATE) {
//                    System.out.println("is userright: " + rel);
                    JEVisObject otherObj = rel.getOtherObject(obj);

                    if (members.get(otherObj) == null) {
                        members.put(otherObj, new ArrayList<JEVisRelationship>());
                    }
                    List<JEVisRelationship> memberRel = members.get(otherObj);
                    memberRel.add(rel);

                }

            }
        } catch (JEVisException ex) {
            Logger.getLogger(MemberTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        final ObservableList<MemberRow> data = FXCollections.observableArrayList();

        for (Map.Entry<JEVisObject, List<JEVisRelationship>> entry : members.entrySet()) {
            MemberRow newRow = new MemberRow(entry.getKey(), entry.getValue());
            System.out.println("member size: " + entry.getValue().size());
            data.add(newRow);
        }

        setItems(data);

    }

    private void setBooleanCellrenderer(TableColumn column) {
        column.setCellFactory(new Callback() {

            @Override
            public TableCell<MemberRow, Boolean> call(Object p) {

                return new CheckBoxTableCell<MemberRow, Boolean>();
            }
        });

    }

    public static class CheckBoxTableCell<S, T> extends TableCell<S, T> {

        private final CheckBox checkBox;
        private ObservableValue<T> ov;

        public CheckBoxTableCell() {
            this.checkBox = new CheckBox();
            this.checkBox.setAlignment(Pos.CENTER);

            setAlignment(Pos.CENTER);
            setGraphic(checkBox);

            checkBox.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {

                    MemberRow mr = (MemberRow) getTableRow().getItem();
                    System.out.println("mr: " + mr.getCreate() + "," + mr.getDelete() + "," + mr.getWrite() + "," + mr.getExce());

                    System.out.println("someting changed in line: " + getIndex());
                }
            });

        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                System.out.println("update icon: " + item);
                if (item instanceof Boolean) {
                    checkBox.setSelected((Boolean) item);
                }

                setGraphic(checkBox);
//                if (ov instanceof BooleanProperty) {
//                    checkBox.selectedProperty().unbindBidirectional((BooleanProperty) ov);
//                }
//                ov = getTableColumn().getCellObservableValue(getIndex());
//                if (ov instanceof BooleanProperty) {
//                    checkBox.selectedProperty().bindBidirectional((BooleanProperty) ov);
//                }
            }
        }
    }

}
