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
package org.jevis.application.unit;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.measure.unit.Unit;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisUnit;
import org.jevis.application.dialog.DialogHeader;
import org.jevis.application.resource.ResourceLoader;
import org.jevis.commons.unit.JEVisUnitImp;

/**
 * This simple Dialog allows the user to select an Unit in an treeview
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SimpleTreeUnitChooser {

    public static enum Response {

        YES, CANCEL
    };

    private Response response = Response.CANCEL;

    private UnitTree uTree;
    private JEVisUnit _unit = new JEVisUnitImp(Unit.ONE);

    public SimpleTreeUnitChooser() {
    }

    public Response show(Point2D position, final JEVisDataSource ds) throws JEVisException {
        final Stage stage = new Stage();

        stage.setTitle("Base Unit Selection");
        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.initOwner(owner);
        stage.setX(position.getX());
        stage.setY(position.getY());
        stage.setWidth(410);
        stage.setHeight(640);

        VBox root = new VBox();

        Scene scene = new Scene(root);
        stage.setScene(scene);

//        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);

        ImageView imageView = ResourceLoader.getImage("1405444584_measure.png", 65, 65);

        Node header = DialogHeader.getDialogHeader("1405444584_measure.png", "Unit Selection");

        stage.getIcons().add(imageView.getImage());

//        Node header = DialogHeader.getDialogHeader("1404313956_evolution-tasks.png", "Unit Selection");
        HBox buttonPanel = new HBox();

        Button ok = new Button("OK");
        ok.setDefaultButton(true);

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);

        uTree = new UnitTree(ds);
        uTree.setPrefSize(550, 600);

        VBox box = new VBox();
        box.getChildren().add(uTree);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        root.getChildren().setAll(header, new Separator(Orientation.HORIZONTAL), box, buttonPanel);

        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
//                System.out.println("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                response = Response.YES;
                System.out.println("UnitTree.OK: " + uTree.getSelectedObject().getUnit());
                _unit = uTree.getSelectedObject().getUnit();
                stage.close();

            }
        });

        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                response = Response.CANCEL;

                stage.close();

            }
        });

        stage.sizeToScene();
        stage.showAndWait();

        return response;
    }

    public JEVisUnit getUnit() {
        return _unit;
    }

}
