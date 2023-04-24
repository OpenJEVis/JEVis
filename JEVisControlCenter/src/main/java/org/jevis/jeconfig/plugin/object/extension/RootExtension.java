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
package org.jevis.jeconfig.plugin.object.extension;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.tool.ImageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RootExtension implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(RootExtension.class);

    private static final String TITLE = I18n.getInstance().getString("plugin.object.root.title");
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final JEVisObject _obj;
    private final BorderPane _view = new BorderPane();

    public RootExtension(JEVisObject obj) {
        this._obj = obj;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            return obj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return false;
    }

    @Override
    public void showHelp(boolean show) {

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
        Platform.runLater(() -> {
            try {
                build(_obj);
            } catch (Exception ex) {
                logger.fatal(ex);
            }

        });

    }

    @Override
    public String getTitle() {
        return TITLE;
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
//        List<JEVisObject> ownerObj = new ArrayList<>();

        try {
            rootRel.addAll(obj.getRelationships(JEVisConstants.ObjectRelationship.ROOT, JEVisConstants.Direction.FORWARD));
        } catch (JEVisException ex) {
            logger.fatal(ex);
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
//        gridPane.add(new Separator(Orientation.HORIZONTAL_TOP_LEFT), 0, yAxis, 2, 1);
        for (final JEVisRelationship rel : rootRel) {
            try {
                HBox groupBox = new HBox(2);
//                Label nameLabel = new Label(rel.getOtherObject(obj).getName());
                Label nameLabel = new Label(getDisplayName(rel.getOtherObject(obj)));

//                ownerObj.add(rel.getOtherObject(obj));

                ImageView usericon = new ImageView();
                try {
                    usericon = ImageConverter.convertToImageView(rel.getOtherObject(obj).getJEVisClass().getIcon(), 17, 17);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }

                groupBox.getChildren().addAll(usericon, nameLabel);

                JFXButton remove = new JFXButton();
                remove.setGraphic(JEConfig.getImage("list-remove.png", 17, 17));
                remove.setOnAction(t -> {

                    try {
                        rel.getStartObject().deleteRelationship(rel);
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }

                    Platform.runLater(() -> {
                        try {
                            build(_obj);
                        } catch (Exception ex) {
                            logger.fatal(ex);
                        }

                    });
                });

                HBox controls = new HBox(5);
                controls.getChildren().addAll(remove);

                yAxis++;
                GridPane.setValignment(groupBox, VPos.BASELINE);
                GridPane.setValignment(controls, VPos.BASELINE);

                gridPane.add(groupBox, 0, yAxis);
                gridPane.add(controls, 1, yAxis);

            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }
        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 2, 1);

        yAxis++;
        gridPane.add(buildNewBox(obj), 0, yAxis, 2, 1);

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

    private HBox buildNewBox(final JEVisObject obj) {

        Label newOwnerlabel = new Label(I18n.getInstance().getString("plugin.object.root.ownerlabel"));
        newOwnerlabel.setPrefHeight(21);
        GridPane.setValignment(newOwnerlabel, VPos.CENTER);
        HBox addNewBox = new HBox(5);

        JFXButton newB = new JFXButton();
        newB.setGraphic(JEConfig.getImage("list-add.png", 17, 17));

        newB.setOnAction(t -> {
            try {
                List<JEVisTreeFilter> allFilter = new ArrayList<>();

                JEVisTreeFilter basicFilter = SelectTargetDialog.buildAllObjects();
                allFilter.add(basicFilter);

                SelectTargetDialog dia = new SelectTargetDialog(allFilter, basicFilter, null, SelectionMode.MULTIPLE, _obj.getDataSource(), new ArrayList<>());

                dia.setOnCloseRequest(event -> {
                    try {
                        if (dia.getResponse() == SelectTargetDialog.Response.OK) {
                            for (UserSelection selection : dia.getUserSelection()) {
                                obj.buildRelationship(selection.getSelectedObject(), JEVisConstants.ObjectRelationship.ROOT, JEVisConstants.Direction.FORWARD);

                                Platform.runLater(() -> {
                                    try {
                                        build(_obj);
                                    } catch (Exception ex) {
                                        logger.fatal(ex);
                                    }
                                });
                            }
                        }
                    } catch (Exception ex) {
                        logger.fatal(ex);
                    }
                });
                dia.show();
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        addNewBox.getChildren().setAll(newOwnerlabel, newB);

        return addNewBox;
    }

}
