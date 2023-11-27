/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc.plugin.object.permission;


import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MemberTable extends TableView {
    private static final Logger logger = LogManager.getLogger(MemberTable.class);

    public MemberTable(JEVisObject obj) {
        super();

        TableColumn<MemberRow, String> ownerColumn = new TableColumn<>(I18n.getInstance().getString("plugin.object.permission.member"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, String>("member"));

        TableColumn readColumn = new TableColumn(I18n.getInstance().getString("plugin.object.permission.read"));
        readColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("read"));

        TableColumn writeColumn = new TableColumn(I18n.getInstance().getString("plugin.object.permission.write"));
        writeColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("write"));

        TableColumn deleteColumn = new TableColumn(I18n.getInstance().getString("plugin.object.permission.delete"));
        deleteColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("delete"));

        TableColumn execColumn = new TableColumn(I18n.getInstance().getString("plugin.object.permission.execute"));
        execColumn.setCellValueFactory(new PropertyValueFactory<MemberRow, Boolean>("exce"));

        TableColumn createColumn = new TableColumn(I18n.getInstance().getString("plugin.object.permission.create"));
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

        try {
            for (JEVisRelationship rel : obj.getRelationships()) {
                if (rel.getType() == MEMBER_READ || rel.getType() == MEMBER_WRITE || rel.getType() == MEMBER_DELETE
                        || rel.getType() == MEMBER_EXECUTE || rel.getType() == MEMBER_CREATE) {
//                    logger.info("is userright: " + rel);
                    JEVisObject otherObj = rel.getOtherObject(obj);

                    members.computeIfAbsent(otherObj, k -> new ArrayList<JEVisRelationship>());
                    List<JEVisRelationship> memberRel = members.get(otherObj);
                    memberRel.add(rel);

                }

            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        final ObservableList<MemberRow> data = FXCollections.observableArrayList();

        for (Map.Entry<JEVisObject, List<JEVisRelationship>> entry : members.entrySet()) {
            MemberRow newRow = new MemberRow(entry.getKey(), entry.getValue());
            data.add(newRow);
        }

        setItems(data);

    }

    private void setBooleanCellrenderer(TableColumn column) {
        column.setCellFactory(p -> new javafx.scene.control.cell.CheckBoxTableCell<>());

    }

    public static class CheckBoxTableCell<S, T> extends TableCell<S, T> {

        private final CheckBox checkBox;
        private ObservableValue<T> ov;

        public CheckBoxTableCell() {
            this.checkBox = new CheckBox();
            this.checkBox.setAlignment(Pos.CENTER);

            setAlignment(Pos.CENTER);
            setGraphic(checkBox);


        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
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
