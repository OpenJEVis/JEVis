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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.relationship.RelationsManagment;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ImageConverter;

import java.util.*;

import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

/**
 * This extension handels the mombership of users to groups.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MemberExtension implements ObjectEditorExtension {
    private static final Logger logger = LogManager.getLogger(MemberExtension.class);

    private static final String TITLE = I18n.getInstance().getString("plugin.object.member");
    private final JEVisObject _obj;
    private ObjectRelations objectRelations;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    //    AnchorPane _view = new AnchorPane();
    private BorderPane _view = new BorderPane();

    public MemberExtension(JEVisObject obj) {
        this._obj = obj;
        try {
            this.objectRelations = new ObjectRelations(obj.getDataSource());
        } catch (JEVisException e) {
            logger.error(e);
        }
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    /**
     * Build the GUI element
     *
     * @param obj
     * @throws JEVisException
     */
    private void buildView(final JEVisObject obj) throws JEVisException {
//        logger.info("Build Member GUI for object: " + obj.getName());
        //First load all users the that the API has the allready cached befor loading the relationhsips
        //TODO: this could be a bad is the system has a lot of users and the current user is the system user
        List<JEVisObject> allUsers = obj.getDataSource().getObjects(obj.getDataSource().getJEVisClass("User"), true);
//        logger.info("Total User count: " + allUsers.size());

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(15);
        gridPane.setVgap(4);

        Label userHeader = new Label(I18n.getInstance().getString("plugin.object.member.user"));
        userHeader.setMinWidth(120d);
        Label readHeader = new Label(I18n.getInstance().getString("plugin.object.member.read"));
        Label writeHeader = new Label(I18n.getInstance().getString("plugin.object.member.write"));
        Label exceHeader = new Label(I18n.getInstance().getString("plugin.object.member.execute"));
        Label deleteHeader = new Label(I18n.getInstance().getString("plugin.object.member.delete"));
        Label createHeader = new Label(I18n.getInstance().getString("plugin.object.member.create"));
        Label contolsHeader = new Label(I18n.getInstance().getString("plugin.object.member.remove"));

        userHeader.setStyle("-fx-font-weight: bold;");
        readHeader.setStyle("-fx-font-weight: bold;");
        writeHeader.setStyle("-fx-font-weight: bold;");
        exceHeader.setStyle("-fx-font-weight: bold;");
        deleteHeader.setStyle("-fx-font-weight: bold;");
        createHeader.setStyle("-fx-font-weight: bold;");
        contolsHeader.setStyle("-fx-font-weight: bold;");

        int yAxis = 0;

        //Header
        gridPane.add(userHeader, 0, yAxis);
        gridPane.add(readHeader, 1, yAxis);
        gridPane.add(writeHeader, 2, yAxis);
        gridPane.add(exceHeader, 3, yAxis);
        gridPane.add(deleteHeader, 4, yAxis);
        gridPane.add(createHeader, 5, yAxis);
        gridPane.add(contolsHeader, 6, yAxis);

        GridPane.setHalignment(readHeader, HPos.CENTER);
        GridPane.setHalignment(writeHeader, HPos.CENTER);
        GridPane.setHalignment(exceHeader, HPos.CENTER);
        GridPane.setHalignment(deleteHeader, HPos.CENTER);
        GridPane.setHalignment(createHeader, HPos.CENTER);

        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 7, 1);

        Map<JEVisObject, List<JEVisRelationship>> members = new TreeMap<>(Comparator.comparing(this::getDisplayName));

        try {
            for (JEVisRelationship rel : obj.getRelationships()) {
                try {
                    if (rel == null || rel.getStartObject() == null || rel.getEndObject() == null) {
                        logger.info("Incorrect relationship: " + rel);
                        continue;
                    }
                    if (rel.isType(MEMBER_CREATE)
                            || rel.isType(MEMBER_DELETE)
                            || rel.isType(MEMBER_READ)
                            || rel.isType(MEMBER_WRITE)
                            || rel.isType(MEMBER_CREATE)
                            || rel.isType(MEMBER_EXECUTE)) {
                        members.computeIfAbsent(rel.getOtherObject(obj), k -> new ArrayList<>());
                        members.get(rel.getOtherObject(obj)).add(rel);
                    }
                } catch (Exception ex) {
                    logger.fatal(ex);
                }

            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        JEVisClass userClass = null;
        try {
            userClass = obj.getDataSource().getJEVisClass("User");
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        boolean userCanEdit = obj.getDataSource().getCurrentUser().canWrite(obj.getID());
        logger.info("User is allowed to edit the object: " + obj.getID());

        for (final Map.Entry<JEVisObject, List<JEVisRelationship>> member : members.entrySet()) {
            yAxis++;

            Label userLabel = new Label(getDisplayName(member.getKey()));
            ImageView usericon = new ImageView();
            if (userClass != null) {
                try {
                    usericon = ImageConverter.convertToImageView(userClass.getIcon(), 17, 17);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }

            HBox userBox = new HBox(1);
            userBox.getChildren().setAll(usericon, userLabel);

            CheckBox readBox = new CheckBox();
            CheckBox writeBox = new CheckBox();
            CheckBox execBox = new CheckBox();
            CheckBox deleteBox = new CheckBox();
            CheckBox createBox = new CheckBox();

            JEVisRelationship aRelationship = null;

            /**
             * Add existing Member relationships
             */
            for (JEVisRelationship rel : member.getValue()) {
                try {
                    logger.info("###### Check re: " + rel);
                    aRelationship = rel;//we need one of the relationship to test if we can delete all this relationships. Doesnt matter which one of this.
                    switch (rel.getType()) {

                        case MEMBER_READ:
                            readBox.setSelected(true);
                            addRelationshipAction(readBox, MEMBER_READ, member.getKey(), obj, rel, userCanEdit);
                            break;
                        case MEMBER_WRITE:
                            writeBox.setSelected(true);
                            addRelationshipAction(writeBox, MEMBER_WRITE, member.getKey(), obj, rel, userCanEdit);
                            break;
                        case MEMBER_EXECUTE:
                            execBox.setSelected(true);
                            addRelationshipAction(execBox, MEMBER_EXECUTE, member.getKey(), obj, rel, userCanEdit);
                            break;
                        case MEMBER_DELETE:
                            deleteBox.setSelected(true);
                            addRelationshipAction(deleteBox, MEMBER_DELETE, member.getKey(), obj, rel, userCanEdit);
                            break;
                        case MEMBER_CREATE:
                            createBox.setSelected(true);
                            addRelationshipAction(createBox, MEMBER_CREATE, member.getKey(), obj, rel, userCanEdit);
                            break;
                    }
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
            }

            /**
             * Add ActionEvents to create Relationships
             */
            if (!readBox.isSelected()) {
                addRelationshipAction(readBox, MEMBER_READ, member.getKey(), obj, null, userCanEdit);
            }
            if (!writeBox.isSelected()) {
                addRelationshipAction(writeBox, MEMBER_WRITE, member.getKey(), obj, null, userCanEdit);
            }
            if (!execBox.isSelected()) {
                addRelationshipAction(execBox, MEMBER_EXECUTE, member.getKey(), obj, null, userCanEdit);
            }
            if (!deleteBox.isSelected()) {
                addRelationshipAction(deleteBox, MEMBER_DELETE, member.getKey(), obj, null, userCanEdit);
            }
            if (!createBox.isSelected()) {
                addRelationshipAction(createBox, MEMBER_CREATE, member.getKey(), obj, null, userCanEdit);
            }

            HBox control = new HBox(0.5);

            Button remove = new Button();
            remove.setGraphic(JEConfig.getImage("list-remove.png", 17, 17));

            //if the currentUser has a group which has delete right on the userObj he can delete it
            if (RelationsManagment.canDeleteMembership(aRelationship)) {
                logger.info("can delete Relationship");
                remove.setDisable(false);

                remove.setOnAction(t -> {

                    removeRelationships(member.getValue());

                    Platform.runLater(() -> {
                        try {
                            buildView(_obj);
                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }

                    });
                });

            } else {
                logger.info("user has no remove ");
                remove.setDisable(true);
            }

            remove.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.member.remove_tooltip")));
            control.getChildren().setAll(remove);

            gridPane.add(userBox, 0, yAxis);
            gridPane.add(readBox, 1, yAxis);
            gridPane.add(writeBox, 2, yAxis);
            gridPane.add(execBox, 3, yAxis);
            gridPane.add(deleteBox, 4, yAxis);
            gridPane.add(createBox, 5, yAxis);
            gridPane.add(control, 6, yAxis);

        }

        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 7, 1);

        yAxis++;
        Label newUserLabel = new Label(I18n.getInstance().getString("plugin.object.member.addmember"));
        newUserLabel.setPrefHeight(21);
        GridPane.setValignment(newUserLabel, VPos.CENTER);
        HBox addNewBox = new HBox(2);
        gridPane.add(addNewBox, 0, yAxis, 7, 1);
        GridPane.setFillWidth(addNewBox, true);

        Button newB = new Button();
        //ToDo
        final ComboBox<JEVisObject> users = new ComboBox<>();
        users.setMinWidth(500);
        users.setButtonCell(new ListCell<JEVisObject>() {

            @Override
            protected void updateItem(JEVisObject t, boolean bln) {
                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
                if (!bln && t != null) {

                    setText(getDisplayName(t));
                }
            }

        });
        users.setCellFactory(new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {

            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> p) {
                return new ListCell<JEVisObject>() {
//                    {
//                        super.setPrefWidth(100);
//                    }

                    @Override
                    public void updateItem(JEVisObject item,
                                           boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(getDisplayName(item));
                        } else {
                            setText(I18n.getInstance().getString("plugin.object.member.user_not_available"));
                        }
                    }
                };
            }
        });

        try {
//            List<JEVisObject> usersObjs = obj.getDataSource().getObjects(obj.getDataSource().getJEVisClass("User"), true);
            allUsers.sort(Comparator.comparing(this::getDisplayName));

            for (JEVisObject user : allUsers) {
//                System.out.print("User in box: " + user.getName());
                if (!members.containsKey(user)) {
//                    logger.info(" is not member yet");
                    users.getItems().add(user);
                } else {
//                    logger.info(" is allready member");
                }

            }

            users.getSelectionModel().selectFirst();

        } catch (Exception ex) {
            logger.fatal(ex);
        }

        newB.setGraphic(JEConfig.getImage("list-add.png", 17, 17));
        newB.setOnAction(t -> {
            try {
                JEVisObject userObj = users.getSelectionModel().getSelectedItem();

                userObj.buildRelationship(obj, JEVisConstants.ObjectRelationship.MEMBER_READ, JEVisConstants.Direction.FORWARD);
                Platform.runLater(() -> {
                    try {
                        buildView(_obj);
                    } catch (JEVisException ex) {
                        logger.fatal(ex);
                    }

                });

            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });

        addNewBox.getChildren().setAll(newUserLabel, users, newB);
        HBox.setHgrow(users, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setContent(gridPane);
        _view.setCenter(scroll);
    }

    private String getDisplayName(JEVisObject obj) {
        if (obj != null) {
            String dName = "";

            String prefix = objectRelations.getObjectPath(obj);

            dName = prefix + obj.getName();

            return dName;
        } else return null;
    }

    /**
     * @param button
     * @param type
     * @param userObj
     * @param group
     * @param rel
     */
    private void addRelationshipAction(final CheckBox button, final int type, final JEVisObject userObj, final JEVisObject group, final JEVisRelationship rel, boolean userCanEdit) {

        try {
            if (!userCanEdit) {
                button.setDisable(true);
            } else {

                button.setOnAction(t -> {

                            if (button.isSelected()) {
                                try {
                                    JEVisRelationship newRel = userObj.buildRelationship(group, type, JEVisConstants.Direction.FORWARD);
                                    userObj.commit();//?
                                } catch (JEVisException ex) {
                                    logger.fatal(ex);
                                }

                            } else {
                                try {
                                    if (rel != null) {
                                        rel.getStartObject().deleteRelationship(rel);
                                    }
                                } catch (JEVisException ex) {
                                    logger.fatal(ex);
                                }
                            }

                            Platform.runLater(() -> {
                                try {
                                    logger.info("Rebuild GUI");
                                    buildView(_obj);
                                } catch (JEVisException ex) {
                                    logger.fatal(ex);
                                }

                            });

                        }
                );
            }

        } catch (Exception ex) {
            logger.fatal(ex);
        }

    }

    private void removeRelationships(List<JEVisRelationship> memberships) {
        for (JEVisRelationship rel : memberships) {
            try {
                if (rel.getType() == MEMBER_READ
                        || rel.getType() == MEMBER_WRITE
                        || rel.getType() == MEMBER_EXECUTE
                        || rel.getType() == MEMBER_DELETE
                        || rel.getType() == MEMBER_CREATE) {
                    rel.getStartObject().deleteRelationship(rel);
                }

            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }
    }

    @Override
    public Node getView() {
        return _view;
    }

    @Override
    public boolean isForObject(JEVisObject obj) {
        try {
            if (obj.getJEVisClass().getName().equals(Constants.JEVisClass.GROUP)) {
                return true;
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        return false;
    }

    @Override
    public void setVisible() {
        Platform.runLater(() -> {
            try {
                buildView(_obj);
            } catch (JEVisException ex) {
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
        //TODo: implement?!
        return false;
    }

    @Override
    public void dismissChanges() {
        _changed.setValue(false);
        //TODO delete changes
    }

    @Override
    public boolean save() {
        //TODo: implement?!, in the moment all changes will be done momentary
        return true;
    }

}
