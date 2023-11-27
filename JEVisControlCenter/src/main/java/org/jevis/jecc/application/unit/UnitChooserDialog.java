/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc.application.unit;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.resource.ResourceLoader;

/**
 * @author fs
 */
public class UnitChooserDialog {
    private static final Logger logger = LogManager.getLogger(UnitChooserDialog.class);
    private Response response = Response.CANCEL;
    private JEVisUnit returnUnit;
    private String altSysbol;
    public UnitChooserDialog() {
    }

    public JEVisUnit getUnit() {
        logger.info("return  Unit: " + returnUnit);
        return returnUnit;
    }

    public Response show(Stage owner, JEVisAttribute att) throws JEVisException {
        return show(owner, att.getType());
    }

    public Response show(Stage owner, JEVisType type) throws JEVisException {
        return showSelector(owner, type.getUnit(), type.getAlternativSymbol());
    }

    public Response showSelector(Stage owner, final JEVisUnit unit, String altSysmbol) {
        final Stage stage = new Stage();
        returnUnit = unit;

        final BooleanProperty isOK = new SimpleBooleanProperty(false);
//        ImageView icon = new ImageView(UnitChooserDialog.class.getResource(ICON_QUESTION).toExternalForm());
//        stage.getIcons().add(icon.getImage());
        stage.setTitle("Unit");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        stage.setWidth(420);
        stage.setHeight(320);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        Node header = buildHeader("Unit selection");

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
        messagePanel.setPadding(new Insets(30, 30, 30, 30));

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setMinHeight(10);

        final UnitChooserPanel chooser = new UnitChooserPanel(unit, altSysmbol);
        chooser.setPadding(new Insets(10, 10, 10, 10));

        root.getChildren().addAll(header, new Separator(Orientation.HORIZONTAL), chooser.getView(), buttonPanel);
        VBox.setVgrow(messagePanel, Priority.ALWAYS);
        VBox.setVgrow(buttonPanel, Priority.NEVER);
        VBox.setVgrow(header, Priority.NEVER);
        root.setFillWidth(true);

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                stage.close();
//                isOK.setValue(true);
                response = UnitChooserDialog.Response.OK;
//                returnUnit = chooser.getFinalUnit();//TODO
                altSysbol = chooser.getAlternativSysbol();
            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.close();
                response = UnitChooserDialog.Response.CANCEL;

            }
        });

        stage.showAndWait();
        logger.info("return " + response);

        return response;

    }

    public String getAlternativSysmbol() {
        return altSysbol;
    }

    private Node buildHeader(String titleLong) {

        BorderPane header = new BorderPane();
//        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
        header.getStyleClass().add("dialog-header");
        header.setPadding(new Insets(10, 10, 10, 10));

        Label topTitle = new Label(titleLong);
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 25));

        ImageView imageView = ResourceLoader.getImage("1405368933_kruler.png", 65, 65);

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

    public enum Response {

        OK, CANCEL
    }

}
