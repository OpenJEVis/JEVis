/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.application.unit;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.dialog.DialogHeader;

import javax.measure.unit.Unit;

/**
 * This simple Dialog allows the user to select an Unit in an treeview
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SimpleTreeUnitChooser {
    private static final Logger logger = LogManager.getLogger(SimpleTreeUnitChooser.class);

    public Response show(Point2D position, final JEVisDataSource ds) {
        final Stage stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("plugin.units.baseunit.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
//        stage.initOwner(owner);
        stage.setX(position.getX());
        stage.setY(position.getY());
        stage.setWidth(410);
        stage.setHeight(640);

        VBox root = new VBox();

        Scene scene = new Scene(root);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);

//        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);

        ImageView imageView = ResourceLoader.getImage("1405444584_measure.png", 65, 65);

        Node header = DialogHeader.getDialogHeader("1405444584_measure.png", I18n.getInstance().getString("plugin.units.baseunit.header"));

        stage.getIcons().add(imageView.getImage());

//        Node header = DialogHeader.getDialogHeader("1404313956_evolution-tasks.png", "Unit Selection");
        HBox buttonPanel = new HBox();

        MFXButton ok = new MFXButton(I18n.getInstance().getString("plugin.units.baseunit.ok"));
        ok.setDefaultButton(true);

        MFXButton cancel = new MFXButton(I18n.getInstance().getString("plugin.units.baseunit.cancel"));
        cancel.setCancelButton(true);

        UnitTree uTree = new UnitTree(ds);
        uTree.setPrefSize(550, 600);

        VBox box = new VBox();
        box.getChildren().add(uTree);

        buttonPanel.getChildren().addAll(ok, cancel);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));
        buttonPanel.setSpacing(10);
        buttonPanel.setMaxHeight(25);

        root.getChildren().setAll(header, new Separator(Orientation.HORIZONTAL), box, buttonPanel);

        ok.setOnAction(t -> {
            if (uTree.getSelectedObject() != null) {
//                logger.info("Size: h:" + stage.getHeight() + " w:" + stage.getWidth());
                response = Response.YES;
                logger.info("UnitTree.OK: " + uTree.getSelectedObject().getUnit());
                _unit = uTree.getSelectedObject().getUnit();
                stage.close();
            }

        });

        cancel.setOnAction(t -> {
            response = Response.CANCEL;

            stage.close();

        });

        uTree.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    if (uTree.getSelectedObject() != null) {
                        response = Response.YES;
                        logger.info("UnitTree.OK: " + uTree.getSelectedObject().getUnit());
                        _unit = uTree.getSelectedObject().getUnit();
                        stage.close();
                    }
                }
            }
        });

        stage.sizeToScene();
        stage.showAndWait();

        return response;
    }

    private Response response = Response.CANCEL;

    private JEVisUnit _unit = new JEVisUnitImp(Unit.ONE);

    public SimpleTreeUnitChooser() {
    }

    public enum Response {

        YES, CANCEL
    }

    public JEVisUnit getUnit() {
        return _unit;
    }

}
