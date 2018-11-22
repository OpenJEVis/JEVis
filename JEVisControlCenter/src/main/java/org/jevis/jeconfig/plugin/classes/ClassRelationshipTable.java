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
package org.jevis.jeconfig.plugin.classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.tool.I18n;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ClassRelationshipTable {
    private static final Logger logger = LogManager.getLogger(ClassRelationshipTable.class);

    public ClassRelationshipTable() {
    }

    public Node buildTree(JEVisClass jclass) {
        AnchorPane root = new AnchorPane();
//        root.setStyle("-fx-background-color: blue;");

        TableColumn otherClassCol = new TableColumn(I18n.getInstance().getString("plugin.classes.relationship.table.name"));
        otherClassCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("otherClass"));

        TableColumn typeCol = new TableColumn(I18n.getInstance().getString("plugin.classes.relationship.table.type"));
        typeCol.setCellValueFactory(new PropertyValueFactory<RelationshipColum, String>("type"));

        TableColumn directionCol = new TableColumn(I18n.getInstance().getString("plugin.classes.relationship.table.direction"));
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
            logger.fatal(ex);
        }

        return root;
    }
}
