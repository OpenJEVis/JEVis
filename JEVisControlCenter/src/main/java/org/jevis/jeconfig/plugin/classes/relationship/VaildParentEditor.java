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
package org.jevis.jeconfig.plugin.classes.relationship;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.ImageConverter;

/**
 *
 * @author Florian Simon<florian.imon@envidatec.com>
 */
public class VaildParentEditor {

    private final String INHERIT = "Inherit";
    private final String NESTED = "Nested";
    private final String OK_PARENT = "Vaild Parent";

    private final VBox _view;

    public VaildParentEditor() {
        _view = new VBox();
//        _view.setStyle("-fx-background-color: #E2E2E2");
        _view.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
    }

    public void setJEVisClass(final JEVisClass jclass) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                GridPane gb = new GridPane();
                gb.setPadding(new Insets(5, 0, 20, 20));
                gb.setHgap(7);
                gb.setVgap(7);

                Label headerClass = new Label("JEVisClass");
//                Label headerType = new Label("Type");
//                Label headerDirection = new Label("Direction");

                int x = 0;
                gb.add(headerClass, 0, x);
//                gb.add(headerDirection, 1, x);
//                gb.add(headerType, 2, x);

                gb.add(new Separator(Orientation.HORIZONTAL), 0, ++x, 2, 1);

                try {
                    for (final JEVisClassRelationship rel : jclass.getRelationships()) {
                        if (rel.getType() == JEVisConstants.ClassRelationship.OK_PARENT && rel.getOtherClass(jclass).equals(rel.getEnd())) {
                            HBox classBox = new HBox(5);
                            classBox.setAlignment(Pos.CENTER_LEFT);

                            Label otherClass = new Label(rel.getOtherClass(jclass).getName());
                            ImageView icon = ImageConverter.convertToImageView(rel.getOtherClass(jclass).getIcon(), 30, 30);

                            classBox.getChildren().setAll(icon, otherClass);

                            gb.add(classBox, 0, ++x);
                            Button remove = new Button();
                            remove.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                            remove.setGraphic(JEConfig.getImage("list-remove.png", 20, 20));
                            gb.add(remove, 1, x);

                            remove.setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent t) {
                                    try {
                                        jclass.deleteRelationship(rel);

                                        setJEVisClass(jclass);
                                    } catch (Exception ex) {
                                    }
                                }
                            ;

                        }


                 );
                    }

                }
            }
            catch (JEVisException ex) {
                    Logger.getLogger(VaildParentEditor.class.getName()).log(Level.SEVERE, null, ex);
                }

                final Button newB = new Button();

                newB.setGraphic(JEConfig.getImage("list-add.png", 20, 20));
                newB.setDisable(true);

                //TODO: replace with Tree selection dialog
                final TextField newTF = new TextField();

                newTF.setPromptText("Class Name");
                gb.add(new Separator(Orientation.HORIZONTAL), 0, ++x, 2, 1);
                gb.add(newTF, 0, ++x);
                gb.add(newB, 1, x);

                newTF.setOnKeyReleased(
                        new EventHandler<KeyEvent>() {

                    @Override
                    public void handle(KeyEvent t
                    ) {
                        try {
                            if (!newTF.getText().isEmpty()) {
                                JEVisClass isClass = jclass.getDataSource().getJEVisClass(newTF.getText());
                                if (isClass != null) {
                                    Platform.runLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                newB.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
                                            } catch (JEVisException ex) {

                                            }
//                                                    newB.setDisable(false);
                                        }
                                    });

                                } else {
                                    Platform.runLater(new Runnable() {

                                        @Override
                                        public void run() {
                                            newB.setDisable(true);
                                        }
                                    });
                                }
                            }

                        } catch (JEVisException ex) {
                            Logger.getLogger(VaildParentEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
                );

                newB.setOnAction(
                        new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t
                    ) {

                        try {
                            JEVisClass target = jclass.getDataSource().getJEVisClass(newTF.getText());
                            jclass.buildRelationship(
                                    target, JEVisConstants.ClassRelationship.OK_PARENT, JEVisConstants.Direction.FORWARD);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    setJEVisClass(jclass);
                                }
                            });

                        } catch (JEVisException ex) {
                            Logger.getLogger(VaildParentEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                );

                _view.getChildren()
                        .setAll(gb);
            }

        }
        );

    }

    public ComboBox<String> buildTypeBox(JEVisClassRelationship rel) throws JEVisException {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().add(INHERIT);
        box.getItems().add(NESTED);
        box.getItems().add(OK_PARENT);

        switch (rel.getType()) {
            case JEVisConstants.ClassRelationship.INHERIT:
                box.getSelectionModel().select(INHERIT);
                break;
            case JEVisConstants.ClassRelationship.NESTED:
                box.getSelectionModel().select(NESTED);
                break;
            case JEVisConstants.ClassRelationship.OK_PARENT:
                box.getSelectionModel().select(OK_PARENT);
                break;
            default:
                box.getItems().add(rel.getType() + "");
                box.getSelectionModel().select(rel.getType() + "");
                break;
        }

        try {
            box.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (Exception ex) {

        }

        return box;
    }

    public ComboBox<String> buildDirectionBox(JEVisClassRelationship rel, JEVisClass jclass) throws JEVisException {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().add("Forward");
        box.getItems().add("Backward");

        if (rel.getOtherClass(jclass).equals(rel.getStart())) {
            box.getSelectionModel().select("Forward");
        } else {
            box.getSelectionModel().select("Backward");
        }

        try {
            box.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (JEVisException ex) {

        }

        return box;
    }

    public Node getView() {
        return _view;
    }

    public void commitAll() {

    }

}
