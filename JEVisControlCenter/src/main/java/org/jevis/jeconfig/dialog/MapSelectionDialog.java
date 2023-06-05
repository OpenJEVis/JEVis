/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeFactory;
import org.jevis.jeconfig.application.jevistree.TreePlugin;
import org.jevis.jeconfig.application.jevistree.plugin.MapPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MapSelectionDialog {
    private static final Logger logger = LogManager.getLogger(MapSelectionDialog.class);

    public Response show() {
        _response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.setTitle("Selection");

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(JEConfig.getStage());

        stage.setWidth(1124);
        stage.setHeight(768);
        stage.setResizable(false);

        VBox root = new VBox();

        DialogHeader header = new DialogHeader();
        Node headerNode = DialogHeader.getDialogHeader(ICON, "Selection Dialog");

        Separator sep = new Separator(Orientation.HORIZONTAL);

        AnchorPane treePane = new AnchorPane();

        JEVisTree tree = getTree();
        treePane.getChildren().setAll(tree);
        AnchorPane.setTopAnchor(tree, 0d);
        AnchorPane.setRightAnchor(tree, 0d);
        AnchorPane.setBottomAnchor(tree, 0d);
        AnchorPane.setLeftAnchor(tree, 0d);

        HBox buttonBox = new HBox(10);
        Region spacer = new Region();
        MFXButton ok = new MFXButton("Load");
        ok.setDefaultButton(true);

        HBox.setHgrow(ok, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(ok, new Insets(10));
        buttonBox.getChildren().setAll(spacer, ok);
        root.getChildren().setAll(headerNode, treePane, sep, buttonBox);

        VBox.setVgrow(treePane, Priority.ALWAYS);
        VBox.setVgrow(sep, Priority.NEVER);
        VBox.setVgrow(buttonBox, Priority.NEVER);

//        stage.getIcons().setAll(ResourceLoader.getImage(ICON, 64, 64).getImage());
        ok.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                tree.setUserSelectionEnded();
                _response = Response.OK;

                logger.info("Results");
                for (TreePlugin plugin : tree.getPlugins()) {
                    if (plugin instanceof MapPlugin) {
                        logger.info("Found Mapchart plugin");
                        MapPlugin bp = (MapPlugin) plugin;

                        data = bp.getSelectedData();
                    }
                }
                stage.hide();
            }
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.showAndWait();

        return _response;
    }

    private Response _response = Response.CANCEL;

    private final JEVisDataSource _ds;
    private final String ICON = "1404313956_evolution-tasks.png";
    private Map<String, MapPlugin.DataModel> data = new HashMap<>();
    private Stage stage;
    private boolean init = true;
    private JEVisTree _tree;

    public MapSelectionDialog(JEVisDataSource ds) {
        _ds = ds;
    }

    public JEVisTree getTree() {
        if (!init) {
            return _tree;
        }

        _tree = JEVisTreeFactory.buildDefaultMapTree(_ds);
        init = false;

        return _tree;
    }

    public enum Response {
        OK, CANCEL
    }

    public Map<String, MapPlugin.DataModel> getSelectedData() {
        return data;
    }

}
