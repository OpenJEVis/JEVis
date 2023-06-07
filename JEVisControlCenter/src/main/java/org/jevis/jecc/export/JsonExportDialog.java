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
package org.jevis.jecc.export;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonFactory;
import org.jevis.commons.json.JsonFileExporter;
import org.jevis.commons.json.JsonObject;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.resource.ResourceLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author fs
 */
public class JsonExportDialog {
    private static final Logger logger = LogManager.getLogger(JsonExportDialog.class);
    //https://www.iconfinder.com/icons/68795/blue_question_icon#size=64
//    public static String ICON_QUESTION = "1400874302_question_blue.png";
    public static String ICON_QUESTION = "1401894975_Export.png";
    public File tartetFile;

    public JsonExportDialog(Stage owner, String title, final JEVisObject obj) {
        final Stage stage = new Stage();

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        Label destinationL = new Label(I18n.getInstance().getString("export.file"));
        final MFXTextField destinationF = new MFXTextField();
        final MFXButton fileSelect = new MFXButton(I18n.getInstance().getString("export.select"));
        final MFXCheckbox allChildren = new MFXCheckbox(I18n.getInstance().getString("export.include_objects"));
        final MFXCheckbox allSamples = new MFXCheckbox(I18n.getInstance().getString("export.include_samples"));
        final MFXCheckbox attributes = new MFXCheckbox(I18n.getInstance().getString("export.include_attributes"));
        allSamples.setDisable(true);

        HBox fileBox = new HBox(5);
        fileBox.getChildren().setAll(destinationF, fileSelect);
        HBox.setHgrow(destinationF, Priority.ALWAYS);
        HBox.setHgrow(fileSelect, Priority.NEVER);
        HBox.setMargin(fileBox, Insets.EMPTY);
        fileBox.setFillHeight(false);

        GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(5);
        content.setPadding(new Insets(20, 10, 10, 10));

        int x = 0;

        content.add(destinationL, 0, x);
        content.add(fileBox, 1, x);
        content.add(allChildren, 0, ++x, 2, 1);
        content.add(attributes, 0, ++x, 2, 1);
        content.add(allSamples, 0, ++x, 2, 1);

//        GridPane.setHalignment(storeConfig, HPos.LEFT);
        GridPane.setHgrow(destinationL, Priority.NEVER);
        GridPane.setHgrow(fileBox, Priority.ALWAYS);
        GridPane.setHgrow(allChildren, Priority.ALWAYS);
        GridPane.setHgrow(attributes, Priority.ALWAYS);
        GridPane.setHgrow(allSamples, Priority.ALWAYS);

        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(500);
        stage.setHeight(270);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        BorderPane header = new BorderPane();
        header.getStyleClass().add("dialog-header");
//        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(I18n.getInstance().getString("export.name.tooltip", obj.getName()));
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView saveIcon = ResourceLoader.getImage("1401915009_stock_export.png", 65, 65);
        ImageView imageView = ResourceLoader.getImage(ICON_QUESTION, 65, 65);

        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(saveIcon);

        header.setLeft(vboxLeft);

        header.setRight(vboxRight);

        HBox buttonPanel = new HBox();

        final MFXButton ok = new MFXButton(I18n.getInstance().getString("export.button.export"));
        ok.setDefaultButton(true);
        ok.setDisable(true);

        MFXButton cancel = new MFXButton(I18n.getInstance().getString("export.button.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        HBox messagePanel = new HBox();
        messagePanel.setPadding(new Insets(30, 30, 30, 30));

        Label mewssage = new Label(I18n.getInstance().getString("export.jon_export"));
        messagePanel.getChildren().add(mewssage);
        mewssage.setWrapText(true);
        mewssage.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), content, buttonPanel);
        VBox.setVgrow(messagePanel, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        destinationF.onKeyReleasedProperty().addListener(new ChangeListener<EventHandler<? super KeyEvent>>() {

            @Override
            public void changed(ObservableValue<? extends EventHandler<? super KeyEvent>> ov, EventHandler<? super KeyEvent> t, EventHandler<? super KeyEvent> t1) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ok.setDisable(false);
                    }
                });

            }
        });

        fileSelect.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                logger.info("");
                FileChooser fileChooser = new FileChooser();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

                fileChooser.setInitialFileName(obj.getName() + "_" + sdf.format(new Date()) + ".json");
                //Show open file dialog
                tartetFile = fileChooser.showSaveDialog(ControlCenter.getStage());

                if (tartetFile != null) {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ok.setDisable(false);
                        }
                    });

                }
                destinationF.setText(tartetFile.getPath());
            }
        });

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());

                boolean isChildren = allChildren.isSelected();
                boolean isAattributes = attributes.isSelected();
                boolean isSamples = false;
                if (isAattributes) {
                    isSamples = allSamples.isSelected();
                }

                tartetFile = new File(destinationF.getText());

                try {
                    JsonObject jobj = JsonFactory.buildObject(obj, isAattributes, isChildren, isSamples);
                    JsonFileExporter.writeToFile(tartetFile, jobj);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }

                stage.close();

            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();

            }
        });

        attributes.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                allSamples.setDisable(!t1);
//                if (allSamples.isSelected() && !t1) {
//                    allSamples.setSelected(false);
//                }

            }
        });

        stage.showAndWait();
    }

}
