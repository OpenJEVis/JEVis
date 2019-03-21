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
package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.jevis.jeconfig.application.resource.ResourceLoader;

/**
 * @author fs
 */
public class ConfirmDialog {

    //https://www.iconfinder.com/icons/68795/blue_question_icon#size=64
    public static String ICON_QUESTION = "1400874302_question_blue.png";

    private Response response = Response.CANCEL;


    public Response show(String title, String titleLong, String message) {
        return show(null, title, titleLong, message);
    }

    public Response show(Window owner, String title, String titleLong, String message) {
        final Stage stage = new Stage();
        stage.initOwner(owner);

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(250);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setAlwaysOnTop(true);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(titleLong);
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView imageView = ResourceLoader.getImage(ICON_QUESTION, 65, 65);

        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(imageView);

        header.setLeft(vboxLeft);

        header.setRight(vboxRight);

        HBox buttonPanel = new HBox();

        Button ok = new Button("OK");
        ok.setDefaultButton(true);

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        HBox messagePanel = new HBox();
        messagePanel.setPadding(new Insets(30, 0, 30, 30));

        TextArea messageLabel = new TextArea(message);
        messageLabel.getStylesheets().add(ConfirmDialog.class.getResource("/styles/TransparentTextArea.css").toExternalForm());
//        messagePanel.getChildren().add(messageLabel);
        messageLabel.setPrefRowCount(5);
        messageLabel.setWrapText(true);
        messageLabel.setEditable(false);


//        messageLabel.setAlignment(Pos.CENTER_LEFT);
        messageLabel.setStyle(" .text-area, .text-area .viewport, .text-area .content {\n" +
                "    -fx-background-color: transparent ;\n" +
                "}");

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), messageLabel, buttonPanel);
        VBox.setVgrow(messageLabel, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);
        VBox.setVgrow(root, Priority.ALWAYS);

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                stage.close();
//                isOK.setValue(true);
                response = Response.YES;

            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = Response.CANCEL;

            }
        });

        stage.setAlwaysOnTop(true);
        Platform.runLater(stage::toFront);
        Platform.runLater(ok::requestFocus);
        stage.showAndWait();

        return response;
    }

    public enum Response {

        NO, YES, CANCEL
    }

}
