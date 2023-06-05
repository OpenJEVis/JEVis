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
package org.jevis.jeconfig.plugin.object.permission;

import com.jfoenix.controls.JFXCheckBox;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.resource.ResourceLoader;

/**
 *
 * @author fs
 */
public class AddSharePermissionsDialog {

    public Response show(Stage owner, String title, String titleLong, String message) {
        final Stage stage = new Stage();

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(250);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(titleLong);
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        //https://www.iconfinder.com/icons/68795/blue_question_icon#size=64
        String ICON_QUESTION = "1400874302_question_blue.png";
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

        MFXButton ok = new MFXButton(I18n.getInstance().getString("plugin.object.permission.add"));
        ok.setDefaultButton(true);

        final JFXCheckBox includeChildren = new JFXCheckBox(I18n.getInstance().getString("plugin.object.permission.includechildren"));
        includeChildren.setSelected(true);
//        MFXButton okAll = new MFXButton("Delte also for all Children");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(includeChildren, Priority.NEVER);
        HBox.setHgrow(ok, Priority.NEVER);

        MFXButton cancel = new MFXButton(I18n.getInstance().getString("plugin.object.permission.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(includeChildren, spacer, ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        HBox messagePanel = new HBox();
        messagePanel.setPadding(new Insets(30, 30, 30, 30));

        Label mewssage = new Label(message);
        messagePanel.getChildren().add(mewssage);
        mewssage.setWrapText(true);
        mewssage.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), messagePanel, buttonPanel);
        VBox.setVgrow(messagePanel, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        ok.setOnAction(t -> {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
            stage.close();
//                isOK.setValue(true);
            if (includeChildren.isSelected()) {
                response = Response.YES_ALL;
            } else {
                response = Response.YES;
            }

        });

//        okAll.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent t) {
////                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
//                stage.close();
////                isOK.setValue(true);
//                response = Response.YES_ALL;
//
//            }
//        });
        cancel.setOnAction(t -> {
            stage.close();
            response = Response.CANCEL;
        });

        stage.showAndWait();

        return response;
    }

    private Response response = Response.CANCEL;

    public enum Response {

        YES, CANCEL, YES_ALL
    }

}
