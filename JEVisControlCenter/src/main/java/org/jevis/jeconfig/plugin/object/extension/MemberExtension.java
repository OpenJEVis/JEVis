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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisConstants;
import static org.jevis.api.JEVisConstants.ObjectRelationship.*;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.relationship.RelationsManagment;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.classes.editor.ClassEditor;
import org.jevis.jeconfig.plugin.object.ObjectEditorExtension;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 * This extension handels the mombership of users to groups.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MemberExtension implements ObjectEditorExtension {

//    AnchorPane _view = new AnchorPane();
    BorderPane _view = new BorderPane();
    private JEVisObject _obj;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    private static final String TITEL = "Member";

    public MemberExtension(JEVisObject obj) {
        _obj = obj;

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
//        System.out.println("Build Member GUI for object: " + obj.getName());
        //First load all users the that the API has the allready cached befor loading the relationhsips
        //TODO: this could be a bad is the system has a lot of users and the current user is the system user
        List<JEVisObject> allUsers = obj.getDataSource().getObjects(obj.getDataSource().getJEVisClass("User"), true);
//        System.out.println("Total User count: " + allUsers.size());

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 20, 20));
        gridPane.setHgap(15);
        gridPane.setVgap(4);

        Label userHeader = new Label("User");
        userHeader.setMinWidth(120d);
        Label readHeader = new Label("Read");
        Label writeHeader = new Label("Write");
        Label exceHeader = new Label("Execute");
        Label deleteHeader = new Label("Delete");
        Label createHeader = new Label("Create");
        Label contolsHeader = new Label("Remove");

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

        Map<JEVisObject, List<JEVisRelationship>> members = new TreeMap<>(new Comparator<JEVisObject>() {

            @Override
            public int compare(JEVisObject o1, JEVisObject o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        try {
            //content
            for (JEVisRelationship rel : obj.getRelationships()) {
                if (rel.isType(MEMBER_CREATE)
                        || rel.isType(MEMBER_DELETE)
                        || rel.isType(MEMBER_READ)
                        || rel.isType(MEMBER_WRITE)
                        || rel.isType(MEMBER_CREATE)
                        || rel.isType(MEMBER_EXECUTE)) {
//                    System.out.println("is userright");
                    if (members.get(rel.getOtherObject(obj)) == null) {
                        members.put(rel.getOtherObject(obj), new ArrayList<JEVisRelationship>());
                    }

                    members.get(rel.getOtherObject(obj)).add(rel);
                }

            }
        } catch (JEVisException ex) {
            Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        JEVisClass userClass = null;
        try {
            userClass = obj.getDataSource().getJEVisClass("User");
        } catch (JEVisException ex) {
            Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (final Map.Entry<JEVisObject, List<JEVisRelationship>> member : members.entrySet()) {
            yAxis++;

            Label userLabel = new Label(member.getKey().getName());
            ImageView usericon = new ImageView();
            if (userClass != null) {
                try {
                    usericon = ImageConverter.convertToImageView(userClass.getIcon(), 17, 17);
                } catch (JEVisException ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            HBox userBox = new HBox(1);
            userBox.getChildren().setAll(usericon, userLabel);

            CheckBox readBox = new CheckBox();
            CheckBox writeBox = new CheckBox();
            CheckBox exceBox = new CheckBox();
            CheckBox deleteBox = new CheckBox();
            CheckBox createBox = new CheckBox();

            JEVisRelationship readRel = null;

            for (JEVisRelationship rel : member.getValue()) {
                try {
//                    System.out.println("###### Check re: " + rel);
                    switch (rel.getType()) {

                        case MEMBER_READ:
                            readBox.setSelected(true);
                            //TODo: does not make nuch sens to have and user how cannot read but do everything else?!
//                            System.out.println("Read membership: " + rel);
                            addRemoverelationshipAction(readBox, MEMBER_READ, member.getKey(), obj, rel);
                            readRel = rel;
                            break;
                        case MEMBER_WRITE:
                            writeBox.setSelected(true);
                            addRemoverelationshipAction(writeBox, MEMBER_WRITE, member.getKey(), obj, rel);
                            break;
                        case MEMBER_EXECUTE:
                            exceBox.setSelected(true);
                            addRemoverelationshipAction(exceBox, MEMBER_EXECUTE, member.getKey(), obj, rel);
                            break;
                        case MEMBER_DELETE:
                            deleteBox.setSelected(true);
                            addRemoverelationshipAction(deleteBox, MEMBER_DELETE, member.getKey(), obj, rel);
                            break;
                        case MEMBER_CREATE:
                            createBox.setSelected(true);
                            addRemoverelationshipAction(createBox, MEMBER_CREATE, member.getKey(), obj, rel);
                            break;
                    }
                } catch (JEVisException ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (!writeBox.isSelected()) {
                addRemoverelationshipAction(writeBox, MEMBER_WRITE, member.getKey(), obj, null);
            }
            if (!exceBox.isSelected()) {
                addRemoverelationshipAction(exceBox, MEMBER_EXECUTE, member.getKey(), obj, null);
            }
            if (!deleteBox.isSelected()) {
                addRemoverelationshipAction(deleteBox, MEMBER_DELETE, member.getKey(), obj, null);
            }
            if (!createBox.isSelected()) {
                addRemoverelationshipAction(createBox, MEMBER_CREATE, member.getKey(), obj, null);
            }

            HBox control = new HBox(0.5);

            Button remove = new Button();
            remove.setGraphic(JEConfig.getImage("list-remove.png", 17, 17));

            if (RelationsManagment.canDeleteMembership(readRel)) {
//                System.out.println("can delete Relationship");
                remove.setDisable(false);

                remove.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {

                        removeRelationhsips(member.getValue());

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    buildView(_obj);
                                } catch (JEVisException ex) {
                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });
                    }
                });

            } else {
                System.out.println("is not owner");
                remove.setDisable(true);
            }

//            if (!member.getKey().equals(member.getKey().getDataSource().getCurrentUser())) {
//                remove.setOnAction(new EventHandler<ActionEvent>() {
//                    @Override
//                    public void handle(ActionEvent t) {
//
//                        removeRelationhsips(member.getValue());
//
//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    buildView(_obj);
//                                } catch (JEVisException ex) {
//                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
//                                }
//
//                            }
//                        });
//                    }
//                });
//            } else {
//                remove.setDisable(true);
//            }
            remove.setTooltip(new Tooltip("Remove this user from the memberlist."));
            control.getChildren().setAll(remove);

            gridPane.add(userBox, 0, yAxis);
            gridPane.add(readBox, 1, yAxis);
            gridPane.add(writeBox, 2, yAxis);
            gridPane.add(exceBox, 3, yAxis);
            gridPane.add(deleteBox, 4, yAxis);
            gridPane.add(createBox, 5, yAxis);
            gridPane.add(control, 6, yAxis);

        }

        yAxis++;
        gridPane.add(new Separator(Orientation.HORIZONTAL), 0, yAxis, 7, 1);

        yAxis++;
        Label newUserLabel = new Label("Add new Member: ");
        newUserLabel.setPrefHeight(21);
        GridPane.setValignment(newUserLabel, VPos.CENTER);
        HBox addNewBox = new HBox(2);
        gridPane.add(addNewBox, 0, yAxis, 5, 1);

        Button newB = new Button();
        //ToDo
        final ComboBox<JEVisObject> users = new ComboBox<>();
        users.setMinWidth(150);
        users.setButtonCell(new ListCell<JEVisObject>() {

            @Override
            protected void updateItem(JEVisObject t, boolean bln) {
                super.updateItem(t, bln); //To change body of generated methods, choose Tools | Templates.
                if (!bln && t != null) {

                    setText(t.getName());
                }
            }

        });
        users.setCellFactory(new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {

            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> p) {
                final ListCell<JEVisObject> cell = new ListCell<JEVisObject>() {
                    {
                        super.setPrefWidth(100);
                    }

                    @Override
                    public void updateItem(JEVisObject item,
                            boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getName());
                        } else {
                            setText("No user available");
                        }
                    }
                };
                return cell;
            }
        });

        try {
//            List<JEVisObject> usersObjs = obj.getDataSource().getObjects(obj.getDataSource().getJEVisClass("User"), true);
            Collections.sort(allUsers, new Comparator<JEVisObject>() {

                @Override
                public int compare(JEVisObject o1, JEVisObject o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (JEVisObject user : allUsers) {
//                System.out.print("User in box: " + user.getName());
                if (!members.containsKey(user)) {
//                    System.out.println(" is not member yet");
                    users.getItems().add(user);
                } else {
//                    System.out.println(" is allready member");
                }

            }

            users.getSelectionModel().selectFirst();

        } catch (Exception ex) {
            Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        newB.setGraphic(JEConfig.getImage("list-add.png", 17, 17));
        newB.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    JEVisObject userObj = users.getSelectionModel().getSelectedItem();
                    System.out.println("create relationship for user: " + userObj.getName());

                    userObj.buildRelationship(obj, JEVisConstants.ObjectRelationship.MEMBER_READ, JEVisConstants.Direction.FORWARD);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                buildView(_obj);
                            } catch (JEVisException ex) {
                                Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    });

                } catch (Exception ex) {
                    Logger.getLogger(ClassEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        addNewBox.getChildren().setAll(newUserLabel, users, newB);

        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: transparent");
        scroll.setMaxSize(10000, 10000);
        scroll.setContent(gridPane);
//        _view.getChildren().setAll(scroll);
        _view.setCenter(scroll);
    }

    /**
     *
     * @param button
     * @param type
     * @param userObj
     * @param group
     * @param rel
     */
    private void addRemoverelationshipAction(final CheckBox button, final int type, final JEVisObject userObj, final JEVisObject group, final JEVisRelationship rel) {

        try {

            //Disable the posibility to configure the mombership of the user himself
//            if (userObj.equals(userObj.getDataSource().getCurrentUser())) {
            boolean isOK = false;
            try {
                RelationsManagment.canDeleteRelationship(userObj, rel);
                isOK = true;
            } catch (Exception ex) {

            }

            if (false) {
                button.setDisable(true);
            } else {

                button.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {

                        if (button.isSelected()) {
                            System.out.println("selected " + type);
                            try {
                                JEVisRelationship newRel = userObj.buildRelationship(group, type, JEVisConstants.Direction.FORWARD);
                                userObj.commit();//?
                            } catch (JEVisException ex) {
                                Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } else {
                            System.out.println("is NOT selected " + type);
                            try {
                                System.out.println("remove relationship: " + rel);
                                rel.getStartObject().deleteRelationship(rel);
                            } catch (JEVisException ex) {
                                Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    buildView(_obj);
                                } catch (JEVisException ex) {
                                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            }
                        });

                    }
                }
                );
            }

        } catch (Exception ex) {
            Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void removeRelationhsips(List<JEVisRelationship> memberships) {
        System.out.println("Delete membership for: " + memberships);
        for (JEVisRelationship rel : memberships) {
            try {
                if (rel.getType() == MEMBER_READ
                        || rel.getType() == MEMBER_WRITE
                        || rel.getType() == MEMBER_EXECUTE
                        || rel.getType() == MEMBER_DELETE
                        || rel.getType() == MEMBER_CREATE) {
                    rel.getStartObject().deleteRelationship(rel);
                    System.out.println("delete rel: " + rel);
                }

            } catch (JEVisException ex) {
                Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public void setVisible() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    buildView(_obj);
                } catch (JEVisException ex) {
                    Logger.getLogger(MemberExtension.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

    }

    @Override
    public String getTitel() {
        return TITEL;
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
