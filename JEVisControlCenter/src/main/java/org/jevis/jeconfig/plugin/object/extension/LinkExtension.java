/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object.extension;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.CommonClasses;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.relationship.RelationshipTable;
import org.jevis.jeconfig.tool.I18n;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class LinkExtension implements ObjectEditorExtension {

    private static final String TITEL = I18n.getInstance().getString("plugin.object.links");
    private final BorderPane _view = new BorderPane();
    private JEVisObject _obj;
    private boolean _needSave = false;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    public LinkExtension(JEVisObject _obj) {
        this._obj = _obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            if (obj.getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
                return true;
            }
        } catch (JEVisException ex) {
            Logger.getLogger(LinkExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public void setVisible() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                buildGui(_obj);
            }
        });
    }

    private void buildGui(JEVisObject obj) {

        _needSave = false;

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        AnchorPane.setTopAnchor(gridPane, 10.0);
        AnchorPane.setRightAnchor(gridPane, 5.0);
        AnchorPane.setLeftAnchor(gridPane, 10.0);
        AnchorPane.setBottomAnchor(gridPane, 5.0);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);

        AnchorPane ap = new AnchorPane();

        try {
//            List<JEVisRelationship> rels = obj.getRelationships(JEVisConstants.ObjectRelationship.LINK, JEVisConstants.Direction.FORWARD);
            List<JEVisRelationship> rels = obj.getRelationships();

            if (!rels.isEmpty()) {

            }
            RelationshipTable table = new RelationshipTable(obj, rels);

//            StackPane sp = new StackPane();
//            sp.getChildren().setAll(table);
//
//            gridPane.add(sp, 0, 0);
            ap.getChildren().add(table);
            AnchorPane.setTopAnchor(table, 0.0);
            AnchorPane.setRightAnchor(table, 0.0);
            AnchorPane.setLeftAnchor(table, 0.0);
            AnchorPane.setBottomAnchor(table, 0.0);

        } catch (Exception ex) {

        }

        _view.setCenter(ap);

    }

    @Override
    public String getTitel() {

        return TITEL;
    }

    @Override
    public boolean needSave() {
        return _changed.getValue();
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
        //TODO delete changes
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

}
