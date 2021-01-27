/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.commons.application.ApplicationInfo;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.ResourceLoader;

/**
 *
 * @author fs
 */
public class AboutDialog {

    public static String ICON_TASKBAR = "1400874302_question_blue.png";

    /**
     * Show an simple About Dialog.
     *
     * TODO: Add contact information (Tel. Email etc.), GPL info and other legal
     * informations
     *     *
     * @param title
     * @param message
     * @param info
     * @param image
     */
    public void show(String title, String message, ApplicationInfo info, Image image) {
        final Stage stage = new Stage();

        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(JEConfig.getStage());

        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        //TODo better be dynamic

        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);

        BorderPane header = new BorderPane();
        header.getStyleClass().add("dialog-header");
//        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
//        header.setPadding(new Insets(10, 10, 10, 10));

        ImageView imageView = ResourceLoader.getImage(ICON_TASKBAR, 65, 65);

        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        vboxLeft.getChildren().add(new ImageView(image));
        vboxLeft.setAlignment(Pos.CENTER);

        header.setCenter(vboxLeft);

        HBox buttonPanel = new HBox();

        Button cancel = new Button("Close");
        cancel.setDefaultButton(true);

        buttonPanel.getChildren().addAll(cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 5, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        Label pInfo = new Label(info.getName() + ":");
        pInfo.setTextFill(Color.web("#0076a3"));
        pInfo.setFont(Font.font("Cambria", 25));

        Label pVersion = new Label(info.getVersion());
        pVersion.setTextFill(Color.web("#0076a3"));
        pVersion.setFont(Font.font("Cambria", 25));

        Label apiInfo = new Label("JEAPI Version:");
        Label apiVersion = new Label(info.getAPIVersion().getName() + " " + info.getAPIVersion().getVersion());

        Label javaInfo = new Label("JAVA Vendor:");
        Label javaVersion = new Label(System.getProperty("java.vendor") + " " + System.getProperty("java.version"));


        Label systemInfo = new Label("OS System:");
        Label systemVersion = new Label(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));


        Label vmNameLabel = new Label("JAVA Name:");
        Label vmName = new Label(System.getProperty("java.vm.name") );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        //spalte , zeile bei 0 starten

        grid.addColumn(0,pInfo,vmNameLabel,javaInfo,systemInfo,apiInfo);
        grid.addColumn(1,pVersion,vmName,javaVersion,systemVersion,apiVersion);
//        grid.add(pInfo, 0, 0);
//        grid.add(pVersion, 1, 0);
//
//
//
//        grid.add(javaInfo, 0, 1);
//        grid.add(javaVersion, 1, 1);
//
//        grid.add(systemInfo, 0, 2);
//        grid.add(systemVersion, 1, 2);
//        grid.add(apiInfo, 0, 3);
//        grid.add(apiVersion, 1, 3);

//        int gy = 3;
//        for (LibraryInfo lib : info.getLibrarys()) {
//            Label libName = new Label(lib.getName() + ":");
//            Label libVersion = new Label(lib.getVersion());
//            ++gy;
//            grid.add(libName, 0, gy);
//            grid.add(libVersion, 1, gy);
//        }

        root.getChildren().addAll(
                header, new Separator(Orientation.HORIZONTAL),
                grid,
                buttonPanel
        );
//        root.setSpacing(5);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
//                logger.info("h: " + stage.getHeight() + " w:" + stage.getWidth());
            }
        });

        stage.setWidth(365);
        stage.setHeight(500);
        //Workaround to set a dynamic size
//        stage.setHeight(460 + ((gy - 3) * 17));


        stage.sizeToScene();
        stage.showAndWait();
    }

}
