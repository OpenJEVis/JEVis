/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.tool;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.application.resource.ResourceLoader;

/**
 *
 * @author fs
 */
public class NewClassDialog {
    private static final Logger logger = LogManager.getLogger(NewClassDialog.class);
    //https://www.iconfinder.com/icons/68795/blue_question_icon#size=64
    public static String ICON_QUESTION = "1400874302_question_blue.png";

    /**
     *
     * @param owner
     * @param superClass
     * @param ds
     * @return
     */
    public Response show(Stage owner, final JEVisClass superClass, final JEVisDataSource ds) {
        final Stage stage = new Stage();

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        stage.setTitle("New Class");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(350);
        stage.setHeight(230);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        HBox buttonPanel = new HBox();

        final Button ok = new Button("OK");
        ok.setDefaultButton(true);

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        Label nameL = new Label("Name:");
        final TextField nameF = new TextField();
        nameF.setPromptText("Enter new name here");

        final Label warning = new Label("Exists!");
        warning.setTextFill(Color.web("#CB5959"));

        final TextField heritB = new TextField();
        heritB.setPromptText("Iherit class name");
        heritB.setDisable(true);

        warning.setMaxWidth(50);
        warning.setVisible(false);

        if (superClass != null) {
            try {
                heritB.setText(superClass.getName());
                heritB.setDisable(false);
                iherit.setSelected(true);
                inherit = superClass;
//                nameF.setText("new " + heir.getName());
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        } else {
            heritB.setText("");
            heritB.setDisable(true);
            iherit.setSelected(false);
            inherit = null;
        }

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(10));
        gp.setHgap(10);
        gp.setVgap(5);
        int x = 0;
        gp.add(nameL, 0, x);
        gp.add(nameF, 1, x);
        gp.add(warning, 2, x);
        gp.add(iherit, 0, ++x, 1, 1);
        gp.add(heritB, 1, x, 1, 1);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);
        Node header = buildHeader("New Class");

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), gp, buttonPanel);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        ok.setDisable(true);
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                stage.close();
//                isOK.setValue(true);
                response = Response.YES;

            }
        });

        heritB.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                try {
                    inherit = ds.getJEVisClass(heritB.getText());
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }
        });

        nameF.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                name = nameF.getText();
                try {
                    if (!name.equals("") && ds.getJEVisClass(name) == null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ok.setDisable(false);
                                warning.setVisible(false);
                            }
                        });

                    } else {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ok.setDisable(true);
                                warning.setVisible(true);
                            }
                        });

                    }
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }

            }
        });

        iherit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                heritB.setDisable(!iherit.isSelected());
            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;

            }
        });

        nameF.requestFocus();
        stage.sizeToScene();
        stage.showAndWait();
//        if (isOK.getValue() == true) {
//            response = Response.YES;
//        }

        logger.info("return {}", response);

        return response;
    }

    private JEVisClass inherit = null;
    private String name;
    final CheckBox iherit = new CheckBox("Inherit:");

    private Response response = Response.CANCEL;

    public enum Response {

        NO, YES, CANCEL
    }

    public String getClassName() {
        return name;
    }

    public JEVisClass getInheritance() {
        if (iherit.isSelected()) {
            return inherit;
        } else {
            return null;
        }

    }

    private Node buildHeader(String titleLong) {

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(titleLong);
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView imageView = ResourceLoader.getImage("1402083924_Project.png", 65, 65);

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(imageView);

        header.setLeft(vboxLeft);

        header.setRight(vboxRight);
        return header;
    }

}
