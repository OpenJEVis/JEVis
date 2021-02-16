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
package org.jevis.jeconfig.plugin.unit;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.dialog.DialogHeader;

import javax.measure.unit.Unit;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class NewUnitDialog {

    private static final String ICON = "1405368933_kruler.png";

    public enum Response {

        NO, YES, CANCEL
    }

    private final Response response = Response.CANCEL;

    public Response show(Stage owner, final Unit parent) {
        final Stage stage = new Stage();

        final BooleanProperty isOK = new SimpleBooleanProperty(false);

        stage.setTitle("New Object");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

//        BorderPane root = new BorderPane();
        VBox root = new VBox();

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);
        stage.setWidth(380);
        stage.setHeight(260);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);

        Node header = DialogHeader.getDialogHeader(ICON, "Create Unit");

        VBox content = new VBox(8);
        content.setPadding(new Insets(20));

        HBox combineBox = new HBox();

        HBox buttonPanel = new HBox();

        final JFXButton ok = new JFXButton("OK");
        ok.setDefaultButton(true);
        ok.setDisable(true);

        JFXButton cancel = new JFXButton("Cancel");
        cancel.setCancelButton(true);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        root.getChildren().setAll(header, new Separator(Orientation.HORIZONTAL), content, buttonPanel);

        return response;
    }

}
