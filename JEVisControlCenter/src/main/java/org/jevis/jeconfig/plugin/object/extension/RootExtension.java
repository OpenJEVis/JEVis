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
package org.jevis.jeconfig.plugin.object.extension;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jevis.api.*;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.classes.editor.ClassEditor;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.selectiontree.SelectObjectDialog;
import org.jevis.jeconfig.plugin.object.selectiontree.UserSelection;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ImageConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RootExtension implements ObjectEditorExtension {

    private static final String TITEL = I18n.getInstance().getString("plugin.object.root.title");
    private JEVisObject _obj;

    private BorderPane _view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    public RootExtension(JEVisObject obj) {
        this._obj = obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            return obj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP);
        } catch (JEVisException ex) {
            Logger.getLogger(RootExtension.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
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
                try {
                    build(_obj);
                } catch (Exception ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

    }

    @Override
    public String getTitle() {
        return TITEL;
    }

    @Override
    public boolean needSave() {
        return false;
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public void dismissChanges() {
        //TODO delete changes
    }

    private void build(final JEVisObject obj) {
        List<JEVisRelationship> rootRel = new ArrayList<>();
        List<JEVisObject> ownerObj = new ArrayList<>();

        try {
            for (JEVisRelationship rel : obj.getRelationships(JEVisConstants.ObjectRelationship.ROOT, JEVisConstants.Direction.FORWARD)) {
                rootRel.add(rel);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(RootExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(15);
        gridPane.setVgap(4);

        Label userHeader = new Label("");
        userHeader.setMinWidth(120d);
        Label removeHeader = new Label("");

        userHeader.setStyle("-fx-font-weight: bold;");
        removeHeader.setStyle("-fx-font-weight: bold;");

        int yAxis = 0;

        //Header
//        gridPane.add(userHeader, 0, yAxis);
//        gridPane.add(removeHeader, 1, yAxis);
//
//        yAxis++;
//
//        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 2, 1);
        for (final JEVisRelationship rel : rootRel) {
            try {
                HBox groupBox = new HBox(2);
//                Label nameLabel = new Label(rel.getOtherObject(obj).getName());
                Label nameLabel = new Label(getDisplayName(rel.getOtherObject(obj)));

                ownerObj.add(rel.getOtherObject(obj));

                ImageView usericon = new ImageView();
                try {
                    usericon = ImageConverter.convertToImageView(rel.getOtherObject(obj).getJEVisClass().getIcon(), 17, 17);
                } catch (JEVisException ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }

                groupBox.getChildren().addAll(usericon, nameLabel);

                Button remove = new Button();
                remove.setGraphic(JEConfig.getImage("list-remove.png", 17, 17));
                remove.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {

                        try {
                            rel.getStartObject().deleteRelationship(rel);
                        } catch (JEVisException ex) {
                            Logger.getLogger(RootExtension.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    build(_obj);
                                } catch (Exception ex) {
                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });
                    }
                });

                HBox contols = new HBox(5);
                contols.getChildren().addAll(remove);

                yAxis++;
                GridPane.setValignment(groupBox, VPos.BASELINE);
                GridPane.setValignment(contols, VPos.BASELINE);

                gridPane.add(groupBox, 0, yAxis);
                gridPane.add(contols, 1, yAxis);

            } catch (JEVisException ex) {
                Logger.getLogger(RootExtension.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 2, 1);

        yAxis++;
        try {
            gridPane.add(buildNewBox(obj, ownerObj), 0, yAxis, 2, 1);
        } catch (JEVisException ex) {
            Logger.getLogger(RootExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);
//        _view.getChildren().setAll(scroll);
        _view.setCenter(scroll);
    }

    private String getDisplayName(JEVisObject obj) {
        return obj.getName();

    }

    private HBox buildNewBox(final JEVisObject obj, final List<JEVisObject> allreadyOwner) throws JEVisException {
        JEVisClass groupClass = obj.getDataSource().getJEVisClass(Constants.JEVisClass.GROUP);
        List<JEVisObject> allGroups = obj.getDataSource().getObjects(groupClass, true);

        Label newOwnerlabel = new Label(I18n.getInstance().getString("plugin.object.root.ownerlabel"));
        newOwnerlabel.setPrefHeight(21);
        GridPane.setValignment(newOwnerlabel, VPos.CENTER);
        HBox addNewBox = new HBox(5);

        Button newB = new Button();
        //ToDo

//        newB.setGraphic(JEConfig.getImage("1404843819_node-tree.png", 17, 17));
        newB.setGraphic(JEConfig.getImage("list-add.png", 17, 17));

        newB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    SelectObjectDialog dia = new SelectObjectDialog();
                    SelectObjectDialog.Response re = dia.show(JEConfig.getStage(), _obj.getDataSource());

                    if (re == SelectObjectDialog.Response.OK) {
                        for (UserSelection selection : dia.getUserSelection()) {
                            obj.buildRelationship(selection.getSelectedObject(), JEVisConstants.ObjectRelationship.ROOT, JEVisConstants.Direction.FORWARD);
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    build(_obj);
                                } catch (Exception ex) {
                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });

                    }

                } catch (Exception ex) {
                    Logger.getLogger(ClassEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        addNewBox.getChildren().setAll(newOwnerlabel, newB);

        return addNewBox;
    }

}
