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
package org.jevis.jecc.plugin.object.extension;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.relationship.RelationsManagment;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.Constants;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.dialog.ConfirmDialog;
import org.jevis.jecc.dialog.InfoDialog;
import org.jevis.jecc.plugin.object.ObjectEditorExtension;
import org.jevis.jecc.plugin.object.permission.AddSharePermissionsDialog;
import org.jevis.jecc.plugin.object.permission.RemoveSharePermissonsDialog;
import org.jevis.jecc.tool.ImageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PermissionExtension implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(PermissionExtension.class);
    private static final String TITLE = I18n.getInstance().getString("plugin.object.permissions.title");
    private final JEVisObject _obj;
    private final BorderPane _view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private ObjectRelations objectRelations;
    private String lastFilterInput = "";

    public PermissionExtension(JEVisObject obj) {
        this._obj = obj;
        try {
            this.objectRelations = new ObjectRelations(obj.getDataSource());
        } catch (JEVisException e) {
            logger.error(e);
        }
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        return true;
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
    public void showHelp(boolean show) {

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
        _changed.setValue(false);
        //TODO delete changes
    }

    private void build(final JEVisObject obj) {
        List<JEVisRelationship> ownerRel = new ArrayList<>();
        List<JEVisObject> ownerObj = new ArrayList<>();

        try {
            //                logger.info("owner: " + rel.getOtherObject(obj));
            ownerRel.addAll(obj.getRelationships(JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD));
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        GridPane gridPane = new GridPane();
//        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(15);
        gridPane.setVgap(4);

        Label userHeader = new Label(I18n.getInstance().getString("plugin.object.permissions.sharewith"));
        userHeader.setMinWidth(120d);
        Label removeHeader = new Label("");

        userHeader.setStyle("-fx-font-weight: bold;");
        removeHeader.setStyle("-fx-font-weight: bold;");

        int yAxis = 0;

        for (final JEVisRelationship rel : ownerRel) {
            try {
                HBox groupBox = new HBox(2);

                Label nameLabel = new Label(getDisplayName(rel.getOtherObject(obj)));

                ownerObj.add(rel.getOtherObject(obj));

                ImageView usericon = new ImageView();
                try {
                    usericon = ImageConverter.convertToImageView(rel.getOtherObject(obj).getJEVisClass().getIcon(), 17, 17);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }

                groupBox.getChildren().addAll(usericon, nameLabel);

                MFXButton remove = new MFXButton();
                remove.setGraphic(ControlCenter.getImage("list-remove.png", 17, 17));
                remove.setOnAction(t -> {

                    try {
                        RemoveSharePermissonsDialog dia = new RemoveSharePermissonsDialog();
                        RemoveSharePermissonsDialog.Response re = dia.show(ControlCenter.getStage(),
                                I18n.getInstance().getString("plugin.object.permissions.delete.title"),
                                I18n.getInstance().getString("plugin.object.permissions.delete.title_long"),
                                I18n.getInstance().getString("plugin.object.permissions.delete.message"));
                        if (re == RemoveSharePermissonsDialog.Response.YES) {
                            rel.getStartObject().deleteRelationship(rel);
                        } else if (re == RemoveSharePermissonsDialog.Response.YES_ALL) {
                            removeFromAllChildren(obj, rel.getOtherObject(obj));
                            rel.getStartObject().deleteRelationship(rel);
                        }

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

                MFXButton forAllChildren = new MFXButton();
                forAllChildren.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.permissions.include_children")));
                forAllChildren.setGraphic(ControlCenter.getImage("1417642712_sitemap.png", 17, 17));
                forAllChildren.setOnAction(t -> {

                    try {
                        ConfirmDialog dia = new ConfirmDialog();
                        ConfirmDialog.Response re = dia.show(
                                I18n.getInstance().getString("plugin.object.permissions.share.title"),
                                I18n.getInstance().getString("plugin.object.permissions.share.title_long"),
                                I18n.getInstance().getString("plugin.object.permissions.share.message"));

                        if (re == ConfirmDialog.Response.YES) {
                            addToAllChildren(obj, rel.getOtherObject(obj));
                        }

                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }

                });

                remove.setDisable(true);
                forAllChildren.setDisable(true);
                try {
//                    RelationsManagment.canDeleteRelationship(obj.getDataSource().getCurrentUser(), rel);

                    if (RelationsManagment.canDeleteOwnership(rel)) {
                        remove.setDisable(false);
                        forAllChildren.setDisable(false);
                    }

                } catch (Exception ex) {
                    logger.info("permissions error: " + ex);
                }

                HBox controls = new HBox(5);
                controls.getChildren().addAll(forAllChildren, remove);

                yAxis++;
                GridPane.setValignment(groupBox, VPos.BASELINE);
                GridPane.setValignment(controls, VPos.BASELINE);

                gridPane.add(groupBox, 0, yAxis);
                gridPane.add(controls, 1, yAxis);

            } catch (Exception ex) {
                logger.fatal(ex);
                InfoDialog infoDia = new InfoDialog();
                infoDia.show(
                        I18n.getInstance().getString("dialog.warning.title"),
                        I18n.getInstance().getString("plugin.object.permissions.error.read"),
                        ex.getMessage());
            }
        }
        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 2, 1);

        yAxis++;
        try {
            gridPane.add(buildNewBox(obj, ownerObj), 0, yAxis, 2, 1);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        ToggleSwitch isPublicButton = new ToggleSwitch(I18n.getInstance().getString("plugin.object.permissions.share_public"));
//        isPublicButton.setPadding(new Insets(5, 0, 20, 20));
        try {
            isPublicButton.setDisable(!obj.getDataSource().getCurrentUser().isSysAdmin());
            isPublicButton.setSelected(obj.isPublic());
            isPublicButton.selectedProperty().setValue(obj.isPublic());
        } catch (JEVisException ex) {
            isPublicButton.setDisable(true);
            isPublicButton.setSelected(false);
        }


        isPublicButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                obj.setIsPublic(newValue);
                obj.commit();

                ConfirmDialog dia = new ConfirmDialog();
                ConfirmDialog.Response re = dia.show(
                        I18n.getInstance().getString("plugin.object.permissions.share.title"),
                        I18n.getInstance().getString("plugin.object.permissions.share.title_long"),
                        I18n.getInstance().getString("plugin.object.permissions.share.message"));

                if (re == ConfirmDialog.Response.YES) {
                    setPublicForAllChildren(obj, newValue);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        VBox vbox = new VBox(10d, isPublicButton, gridPane);
        vbox.setPadding(new Insets(5, 0, 20, 20));

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(vbox);
//        _view.getChildren().setAll(scroll);
        _view.setCenter(scroll);
    }

    private void setPublicForAllChildren(JEVisObject obj, Boolean newValue) {
        try {
            for (JEVisObject child : obj.getChildren()) {
                try {
                    child.setIsPublic(newValue);
                    child.commit();
                    setPublicForAllChildren(child, newValue);
                } catch (JEVisException ex) {
                    logger.warn("Error while creating user right", ex);
                }
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    /**
     * Applies the same owner to all Children
     *
     * @param obj
     * @param group
     */
    private void addToAllChildren(JEVisObject obj, JEVisObject group) {
        try {
            for (JEVisObject children : obj.getChildren()) {
                try {
                    JEVisRelationship newRel = children.buildRelationship(group, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
                    addToAllChildren(children, group);
                } catch (JEVisException ex) {
                    logger.warn("Error while creating user right", ex);
                }
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }
    }

    private void removeFromAllChildren(JEVisObject obj, JEVisObject group) {
        try {
            for (JEVisObject children : obj.getChildren()) {
                for (JEVisRelationship rel : children.getRelationships(JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD)) {
                    if (rel.getEndObject().equals(group)) {
                        try {
                            rel.getStartObject().deleteRelationship(rel);
                        } catch (JEVisException ex) {
                            logger.warn("Error while deleting userright", ex);
                        }
                    }
                }
                removeFromAllChildren(children, group);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    private String getDisplayName(JEVisObject obj) {
        if (obj != null) {
            String dName = "";

            String prefix = objectRelations.getObjectPath(obj);

            dName = prefix + obj.getName();

            return dName;
        } else return null;

    }

    private GridPane buildNewBox(final JEVisObject obj, final List<JEVisObject> allreadyOwner) throws JEVisException {
        JEVisClass groupClass = obj.getDataSource().getJEVisClass(Constants.JEVisClass.GROUP);
        List<JEVisObject> allGroups = obj.getDataSource().getObjects(groupClass, true);

        Label newOwnerlabel = new Label(I18n.getInstance().getString("plugin.object.permissions.share_with_new"));
        newOwnerlabel.setPrefHeight(21);
        newOwnerlabel.setAlignment(Pos.CENTER);
        GridPane.setValignment(newOwnerlabel, VPos.CENTER);
        GridPane addNewBox = new GridPane();
        addNewBox.setHgap(10);
        addNewBox.setVgap(4);

        MFXButton newB = new MFXButton();
        //ToDo
        final MFXComboBox<JEVisObject> groupsCBox = new MFXComboBox<>();
        groupsCBox.setFloatMode(FloatMode.DISABLED);
        groupsCBox.setMinWidth(300);

        //TODO JFX17

        groupsCBox.setConverter(new StringConverter<JEVisObject>() {
            @Override
            public String toString(JEVisObject object) {
                String text = "";
                if (object != null && object.getName() != null) {
                    String prefix = objectRelations.getObjectPath(object);

                    text = prefix + object.getName();
                }

                return text;
            }

            @Override
            public JEVisObject fromString(String string) {
                return groupsCBox.getItems().get(groupsCBox.getSelectedIndex());
            }
        });

        ObservableList<JEVisObject> possibleOwners = FXCollections.observableArrayList();

        try {
            AlphanumComparator ac = new AlphanumComparator();
            allGroups.sort((o1, o2) -> ac.compare(getDisplayName(o1), getDisplayName(o2)));

            for (JEVisObject group : allGroups) {
                if (!allreadyOwner.contains(group)) {
                    possibleOwners.add(group);
                }
            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }

        FilteredList<JEVisObject> filteredData = new FilteredList<>(possibleOwners, s -> true);
        MFXTextField filterInput = new MFXTextField();
        filterInput.setFloatMode(FloatMode.DISABLED);
        filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

        filterInput.textProperty().addListener(obs -> {
            String filter = filterInput.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = (objectRelations.getObjectPath(s) + s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (objectRelations.getObjectPath(s) + s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
            Platform.runLater(() -> groupsCBox.getSelectionModel().selectFirst());
        });

        groupsCBox.setItems(filteredData);

        Platform.runLater(() -> {
            filterInput.setText(lastFilterInput);
            groupsCBox.getSelectionModel().selectFirst();
        });

        newB.setGraphic(ControlCenter.getImage("list-add.png", 17, 17));
        newB.setOnAction(t -> {
            try {
                JEVisObject groupObj = groupsCBox.getSelectionModel().getSelectedItem();
                lastFilterInput = filterInput.getText();
//                    logger.info("create relationship for user: " + groupObj.getName());

                if (groupObj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
                    //TODo: here
                    AddSharePermissionsDialog dia = new AddSharePermissionsDialog();
                    AddSharePermissionsDialog.Response re = dia.show(ControlCenter.getStage(),
                            I18n.getInstance().getString("plugin.object.permissions.new.title"),
                            I18n.getInstance().getString("plugin.object.permissions.new.title_long"),
                            I18n.getInstance().getString("plugin.object.permissions.new.message", groupObj.getName()));
                    if (re == AddSharePermissionsDialog.Response.YES || re == AddSharePermissionsDialog.Response.YES_ALL) {
                        JEVisRelationship newRel = obj.buildRelationship(groupObj, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
//                            logger.info("new Owner: " + newRel);
                        if (re == AddSharePermissionsDialog.Response.YES_ALL) {
//                                logger.info("add also to all children");
                            addToAllChildren(obj, newRel.getOtherObject(obj));
                        }
                    }

                    Platform.runLater(() -> {
                        try {
                            build(_obj);
                        } catch (Exception ex) {
                            logger.fatal(ex);
                        }

                    });
                }

            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        addNewBox.add(newOwnerlabel, 0, 0, 1, 2);
        addNewBox.add(filterInput, 1, 0, 1, 1);
        addNewBox.add(groupsCBox, 1, 1, 1, 1);
        addNewBox.add(newB, 2, 1, 1, 1);

        return addNewBox;
    }

}
