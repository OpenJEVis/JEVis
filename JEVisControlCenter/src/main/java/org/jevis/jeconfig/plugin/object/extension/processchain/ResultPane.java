/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.extension.processchain;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.ProcessChains;
import org.jevis.commons.dataprocessing.ProcessFunction;
import org.jevis.jeconfig.JEConfig;

/**
 *
 * @author Florian Simon
 */
public class ResultPane extends Region {

    Button newB = new Button("", JEConfig.getImage("list-add.png", 12, 12));
    final ChoiceBox functionBox = new ChoiceBox();

    public ResultPane(Process task) {
//        System.out.println("ResultPane: " + task.getFunction().getName());
        GridPane layout = new GridPane();
        layout.setHgap(7);
        layout.setVgap(7);

        layout.setStyle("-fx-border-color: #B3ADAB;"
                + "-fx-border-insets: 5;"
                + "-fx-border-width: 1.5;"
                + "-fx-border-style: solid;"
        );

        //---------------------- Header ------------------------
        HBox header = new HBox();
        Label fName = new Label("Result");

        HBox bar = new HBox();
        bar.setStyle("-fx-background-color: #eef0bb");
        bar.getChildren().add(fName);
        bar.setAlignment(Pos.CENTER);

//        Button close = new Button("X");
        HBox.setHgrow(bar, Priority.ALWAYS);
//        HBox.setHgrow(close, Priority.NEVER);
        header.getChildren().addAll(bar);

        //---------------------- body ------------------------
        AnchorPane bodySpcer = new AnchorPane();

        GridPane body = new GridPane();
        body.setHgap(7);
        body.setVgap(7);

        int rowNr = 0;

        //---------------------- footer ------------------------
        Label newFunction = new Label("Add new Subfunction:");

        ChoiceBox selection = buildSelection();
        HBox sBox = new HBox(5);
        sBox.getChildren().addAll(selection, newB);

        body.add(newFunction, 0, rowNr);
        body.add(sBox, 1, rowNr);

        bodySpcer.getChildren().add(body);

        AnchorPane.setTopAnchor(body, 12.0);
        AnchorPane.setLeftAnchor(body, 12.0);
        AnchorPane.setRightAnchor(body, 12.0);
        AnchorPane.setBottomAnchor(body, 12.0);

        layout.add(header, 0, 0);
        layout.add(bodySpcer, 0, 1);

//        FunctionFooterPane ffp = new FunctionFooterPane();
//
//        VBox vbox = new VBox(10);
//        vbox.getChildren().addAll(layout, ffp);
        getChildren().add(layout);

        if (task == null) {
            functionBox.setDisable(false);
            newB.setDisable(false);
        } else {
            functionBox.setDisable(true);
            newB.setDisable(true);
        }

    }

    public void setOnAddAction(EventHandler<ActionEvent> event) {
        newB.setOnAction(event);
    }

    public void addNewSubFunctionListener(ChangeListener<String> listener) {
        functionBox.valueProperty().addListener(listener);
    }

    public String getSelectedNewSubFunction() {
        return functionBox.getSelectionModel().getSelectedItem().toString();
    }

    private ChoiceBox buildSelection() {

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
        } catch (Exception ex) {

        }
        functionBox.getSelectionModel().selectFirst();
//        guiType.getSelectionModel().select(type.getGUIDisplayType());
        functionBox.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("Select GUI Tpye: " + newValue);
                try {

                } catch (Exception ex) {
                    Logger.getLogger(FunctionFooterPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        return functionBox;
    }

}
