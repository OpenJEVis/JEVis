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
package org.jevis.jeconfig.plugin.classes;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassRelationshipTable {

    public ClassRelationshipTable() {
    }

    public Node buildTree(JEVisClass jclass) {
        AnchorPane root = new AnchorPane();
//        root.setStyle("-fx-background-color: blue;");

        TableColumn otherClassCol = new TableColumn("JEVisClass");
        otherClassCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("otherClass"));

        TableColumn typeCol = new TableColumn("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("type"));

        TableColumn directionCol = new TableColumn("Direction");
        directionCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("direction"));

        TableView table = new TableView();
        table.setPrefSize(200, 200);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //TODO: this in only posible in JAVAFX2.2+

        root.getChildren().add(table);

        AnchorPane.setTopAnchor(table, 10.0);
        AnchorPane.setRightAnchor(table, 10.0);
        AnchorPane.setLeftAnchor(table, 10.0);
        AnchorPane.setBottomAnchor(table, 10.0);

        table.getColumns().addAll(otherClassCol, typeCol, directionCol);

        otherClassCol.prefWidthProperty().bind(table.widthProperty().divide(3));
        typeCol.prefWidthProperty().bind(table.widthProperty().divide(3));
        directionCol.prefWidthProperty().bind(table.widthProperty().divide(3));
        try {
            List<RelationshipColum> tjc = new LinkedList<>();
            for (JEVisClassRelationship rel : jclass.getRelationships()) {
                tjc.add(new RelationshipColum(rel, jclass));
            }

            final ObservableList<RelationshipColum> data = FXCollections.observableArrayList(tjc);
            table.setItems(data);

        } catch (JEVisException ex) {
            Logger.getLogger(ClassRelationshipTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        return root;
    }
}
