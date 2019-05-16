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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.*;
import org.jevis.commons.relationship.RelationsManagment;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.dialog.ConfirmDialog;
import org.jevis.jeconfig.dialog.InfoDialog;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.plugin.object.permission.AddSharePermissionsDialog;
import org.jevis.jeconfig.plugin.object.permission.RemoveSharePermissonsDialog;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ImageConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PermissionExtension implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(PermissionExtension.class);

    private static final String TITLE = I18n.getInstance().getString("plugin.object.permissions.title");
    private JEVisObject _obj;

    private BorderPane _view = new BorderPane();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    public PermissionExtension(JEVisObject obj) {
        this._obj = obj;
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

                Button remove = new Button();
                remove.setGraphic(JEConfig.getImage("list-remove.png", 17, 17));
                remove.setOnAction(t -> {

                    try {
                        RemoveSharePermissonsDialog dia = new RemoveSharePermissonsDialog();
                        RemoveSharePermissonsDialog.Response re = dia.show(JEConfig.getStage(),
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

                Button forAllChildren = new Button();
                forAllChildren.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.permissions.include_children")));
                forAllChildren.setGraphic(JEConfig.getImage("1417642712_sitemap.png", 17, 17));
                forAllChildren.setOnAction(t -> {

                    try {
                        ConfirmDialog dia = new ConfirmDialog();
                        ConfirmDialog.Response re = dia.show(
                                I18n.getInstance().getString("plugin.object.permissions.share.title"),
                                I18n.getInstance().getString("plugin.object.permissions.share.title_long"),
                                I18n.getInstance().getString("plugin.object.permissions.share.message"));

                        if (re == ConfirmDialog.Response.YES) {
                            addTooAllChildren(obj, rel.getOtherObject(obj));

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

    /**
     * Alppys the same ower to all Children
     *
     * @param obj
     * @param group
     */
    private void addTooAllChildren(JEVisObject obj, JEVisObject group) {
        try {
            for (JEVisObject children : obj.getChildren()) {
                try {
                    JEVisRelationship newRel = children.buildRelationship(group, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
                    addTooAllChildren(children, group);
                } catch (JEVisException ex) {
                    logger.warn("Error while creating userright", ex);
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

    /**
     * Returns the Organization of an Group. TODO; this code is very static and
     * could be more dynamic
     *
     * @param obj
     * @return
     * @throws JEVisException
     */
    private JEVisObject getOrganization(JEVisObject obj) throws JEVisException {
        Objects.requireNonNull(obj, "getDisplayName: Missing Group Object");
        if (obj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
            for (JEVisObject p1 : obj.getParents()) {
                if (p1.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP_DIRECTORY)) {
                    for (JEVisObject p2 : p1.getParents()) {
                        if (p2.getJEVisClass().getName().equals(Constants.JEVisClass.ADMINISTRATION_DIRECTORY)) {
                            for (JEVisObject p3 : p2.getParents()) {
                                if (p3.getJEVisClass().getName().equals(Constants.JEVisClass.ORGANIZATION)
                                        || p3.getJEVisClass().getName().equals(Constants.JEVisClass.SYSTEM)) {
                                    return p3;
                                }
                            }
                        }
                    }
                }
            }

        }
        return null;
    }

    private JEVisObject getBuilding(JEVisObject obj) throws JEVisException {
        Objects.requireNonNull(obj, "getDisplayName: Missing Group Object");
        if (obj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
            for (JEVisObject p1 : obj.getParents()) {
                if (p1.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP_DIRECTORY)) {
                    for (JEVisObject p2 : p1.getParents()) {
                        if (p2.getJEVisClass().getName().equals(Constants.JEVisClass.ADMINISTRATION_DIRECTORY)) {
                            for (JEVisObject p3 : p2.getParents()) {
                                if (p3.getJEVisClass().getName().equals(Constants.JEVisClass.BUILDING)
                                        || p3.getJEVisClass().getName().equals(Constants.JEVisClass.SYSTEM)
                                        || p3.getJEVisClass().getName().equals(Constants.JEVisClass.MONITORED_OBJECT)) {
                                    return p3;
                                }
                            }
                        }
                    }
                }
            }

        }
        return null;
    }

    private String getDisplayName(JEVisObject obj) {
        if (obj != null) {
//            Objects.requireNonNull(obj, "getDisplayName: Missing Group Object");
////        logger.info("get Diaplayname for: " + obj.getName());
//            String dName = "";
//
//            try {
//                JEVisObject org = getOrganization(obj);
//                if (org != null) {
////            logger.info("Using org: " + org.getName());
//                    dName += "/ " + org.getName() + " / ";
//
//                    JEVisObject building = getBuilding(obj);
//                    if (building != null) {
//                        dName += " / " + building.getName() + " / ";
//                    }
//                } else {
////            logger.info("is null");
//                }
//            } catch (JEVisException ex) {
//                logger.fatal(ex);
//            }
//
//            dName += obj.getName();

            String dName = "";
            String prefix = "";
            try {

                JEVisObject thirdParent = obj.getParents().get(0).getParents().get(0).getParents().get(0);
                JEVisClass buildingClass = obj.getDataSource().getJEVisClass("Building");
                JEVisClass organisationClass = obj.getDataSource().getJEVisClass("Organization");

                if (thirdParent.getJEVisClass().equals(buildingClass)) {

                    try {
                        JEVisObject organisationParent = thirdParent.getParents().get(0).getParents().get(0);
                        if (organisationParent.getJEVisClass().equals(organisationClass)) {

                            prefix += organisationParent.getName() + " / " + thirdParent.getName() + " / ";
                        }
                    } catch (JEVisException e) {
                        logger.error("Could not get Organization parent of " + thirdParent.getName() + ":" + thirdParent.getID());

                        prefix += thirdParent.getName() + " / ";
                    }
                } else if (thirdParent.getJEVisClass().equals(organisationClass)) {

                    prefix += thirdParent.getName() + " / ";

                }

            } catch (Exception e) {
            }
            dName = prefix + obj.getName();

            return dName;
        } else return null;

    }

    private HBox buildNewBox(final JEVisObject obj, final List<JEVisObject> allreadyOwner) throws JEVisException {
        JEVisClass groupClass = obj.getDataSource().getJEVisClass(Constants.JEVisClass.GROUP);
        List<JEVisObject> allGroups = obj.getDataSource().getObjects(groupClass, true);

        Label newOwnerlabel = new Label(I18n.getInstance().getString("plugin.object.permissions.share_with_new"));
        newOwnerlabel.setPrefHeight(21);
        GridPane.setValignment(newOwnerlabel, VPos.CENTER);
        HBox addNewBox = new HBox(5);

        Button newB = new Button();
        //ToDo
        final ComboBox<JEVisObject> groupsCBox = new ComboBox<>();
        groupsCBox.setMinWidth(300);
//        groupsCBox.setButtonCell(new ListCell<JEVisObject>() {
//
//            @Override
//            protected void updateItem(JEVisObject t, boolean bln) {
//                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
//                if (!bln && t != null) {
//                    setMinWidth(300);
//                    setText(getDisplayName(t));
////                    setText(t.getName());
//                }
//            }
//
//        });
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {

                            String prefix = "";
                            try {

                                JEVisObject secondParent = obj.getParents().get(0).getParents().get(0).getParents().get(0);
                                JEVisClass buildingClass = obj.getDataSource().getJEVisClass("Building");
                                JEVisClass organisationClass = obj.getDataSource().getJEVisClass("Organization");

                                if (secondParent.getJEVisClass().equals(buildingClass)) {

                                    try {
                                        JEVisObject organisationParent = secondParent.getParents().get(0).getParents().get(0);
                                        if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                            prefix += organisationParent.getName() + " / " + secondParent.getName() + " / ";
                                        }
                                    } catch (JEVisException e) {
                                        logger.error("Could not get Organization parent of " + secondParent.getName() + ":" + secondParent.getID());

                                        prefix += secondParent.getName() + " / ";
                                    }
                                } else if (secondParent.getJEVisClass().equals(organisationClass)) {

                                    prefix += secondParent.getName() + " / ";

                                }

                            } catch (Exception e) {
                            }
                            setText(prefix + obj.getName());

                        }
                    }
                };
            }
        };

        groupsCBox.setCellFactory(cellFactory);
        groupsCBox.setButtonCell(cellFactory.call(null));

        try {
//            List<JEVisObject> usersObjs = obj.getDataSource().getObjects(obj.getDataSource().getJEVisClass("User"), true);
            AlphanumComparator ac = new AlphanumComparator();
            allGroups.sort((o1, o2) -> ac.compare(getDisplayName(o1), getDisplayName(o2)));
//            Collections.sort(allGroups, new Comparator<JEVisObject>() {
//
//                @Override
//                public int compare(JEVisObject o1, JEVisObject o2) {
//                    //                    return o1.getName().compareTo(o2.getName());
//                    return getDisplayName(o1).compareTo(getDisplayName(o2));
//                }
//            });

            for (JEVisObject group : allGroups) {
                if (!allreadyOwner.contains(group)) {
                    groupsCBox.getItems().add(group);
                }

            }

            groupsCBox.getSelectionModel().selectFirst();

        } catch (Exception ex) {
            logger.fatal(ex);
        }

        newB.setGraphic(JEConfig.getImage("list-add.png", 17, 17));
        newB.setOnAction(t -> {
            try {
                JEVisObject groupObj = groupsCBox.getSelectionModel().getSelectedItem();
//                    logger.info("create relationship for user: " + groupObj.getName());

                if (groupObj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
                    //TODo: here
                    AddSharePermissionsDialog dia = new AddSharePermissionsDialog();
                    AddSharePermissionsDialog.Response re = dia.show(JEConfig.getStage(),
                            I18n.getInstance().getString("plugin.object.permissions.new.title"),
                            I18n.getInstance().getString("plugin.object.permissions.new.title_long"),
                            I18n.getInstance().getString("plugin.object.permissions.new.message", groupObj.getName()));
                    if (re == AddSharePermissionsDialog.Response.YES || re == AddSharePermissionsDialog.Response.YES_ALL) {
                        JEVisRelationship newRel = obj.buildRelationship(groupObj, JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD);
//                            logger.info("new Owner: " + newRel);
                        if (re == AddSharePermissionsDialog.Response.YES_ALL) {
//                                logger.info("add also to all children");
                            addTooAllChildren(obj, newRel.getOtherObject(obj));
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

        addNewBox.getChildren().setAll(newOwnerlabel, groupsCBox, newB);

        return addNewBox;
    }

}
