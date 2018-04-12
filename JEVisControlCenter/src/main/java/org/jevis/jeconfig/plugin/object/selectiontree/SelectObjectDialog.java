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
package org.jevis.jeconfig.plugin.object.selectiontree;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.dialog.DialogHeader;
import org.jevis.application.resource.ResourceLoader;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SelectObjectDialog {

//    private VBox root = new VBox();
    private Button ok = new Button("OK");
    private String ICON = "1404313956_evolution-tasks.png";
    private JEVisDataSource _ds;
    private Stage stage;
    private Response _response = Response.CANCEL;
    private ObjectSelectionTree tree;

    public static enum Response {

        OK, CANCEL
    };

    public Response show(Stage owner, JEVisDataSource ds) {

        stage = new Stage();
        _ds = ds;

        stage.setTitle("Selection");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);

        VBox root = build();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(1024);
        stage.setHeight(768);
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.getIcons().setAll(ResourceLoader.getImage(ICON, 64, 64).getImage());

        stage.showAndWait();

        return _response;
    }

    private VBox build() {
        VBox root = new VBox(10);
//        root.setPadding(new Insets(10));
        Node header = DialogHeader.getDialogHeader(ICON, "Select Target Attribute");

        HBox buttonPanel = new HBox(8);

        VBox content = new VBox(10);

        tree = new ObjectSelectionTree(_ds);
//        tree.setAllowMultySelect(false);
        content.getChildren().setAll(tree);

        tree.getSelectionProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                ok.setDisable(!t1);
            }
        });

        ok.setDefaultButton(true);
        ok.setDisable(true);

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                stage.hide();
            }
        });

        ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                _response = Response.OK;
                stage.hide();
            }
        });

        buttonPanel.getChildren().setAll(ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(5));

        root.getChildren().addAll(header, content, buttonPanel);
        VBox.setVgrow(content, Priority.ALWAYS);
        return root;
    }

    public List<UserSelection> getUserSelection() {
        return tree.getUserSelection();
    }

}
