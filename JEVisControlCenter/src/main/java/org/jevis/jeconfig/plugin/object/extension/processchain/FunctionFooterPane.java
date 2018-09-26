/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.extension.processchain;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.ProcessChains;
import org.jevis.commons.dataprocessing.ProcessFunction;
import org.jevis.jeconfig.JEConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon
 */
public class FunctionFooterPane extends Region {
    private static final Logger logger = LogManager.getLogger(FunctionFooterPane.class);

    private Node parent;
    private List<Node> children = new ArrayList<>();

    public FunctionFooterPane() {
//        this.parent = parent;
        buildView();
    }

    private void buildView() {
        GridPane layout = new GridPane();
        layout.setHgap(7);
        layout.setVgap(7);

        final Canvas canvas = new Canvas(20, 20);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1.0);

        gc.moveTo(10, 2);
        gc.lineTo(10, 28);
        gc.stroke();

        Button newB = new Button("", JEConfig.getImage("list-add.png", 12, 12));

        final ChoiceBox functionBox = new ChoiceBox();
        functionBox.setMaxWidth(500);
        functionBox.setPrefWidth(160);

        List<String> fcuntionList = new ArrayList<>();
        for (ProcessFunction function : ProcessChains.getAvailableFunctions(null)) {
            fcuntionList.add(function.getName());
        }

        ObservableList<String> items = FXCollections.observableList(fcuntionList);
        functionBox.setItems(items);
        try {
            functionBox.setDisable(!JEConfig.getDataSource().getCurrentUser().isSysAdmin());
        } catch (JEVisException es) {

        }
        functionBox.getSelectionModel().selectFirst();
//        guiType.getSelectionModel().select(type.getGUIDisplayType());
        functionBox.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                logger.info("Select GUI Tpye: " + newValue);
                try {

                } catch (Exception ex) {
                    logger.fatal(ex);
                }
            }
        });

        Region spacerLeft = new Region();
        Region spacerRight = new Region();

        layout.add(spacerLeft, 0, 0);
        layout.add(canvas, 1, 0);
        layout.add(newB, 2, 0);
        layout.add(functionBox, 3, 0);
        layout.add(spacerRight, 4, 0);

        GridPane.setHgrow(spacerLeft, Priority.ALWAYS);
        GridPane.setHgrow(spacerRight, Priority.ALWAYS);
        GridPane.setHgrow(canvas, Priority.NEVER);
        GridPane.setHgrow(newB, Priority.NEVER);
        GridPane.setHgrow(functionBox, Priority.NEVER);

//        for (Node ch : children) {
//            gc.moveTo(10, 5);
//            gc.lineTo(ch.getLayoutX(), 20);
//            gc.stroke();
//        }
        getChildren().add(layout);
    }

    public void addChild(Node node) {

    }

}
