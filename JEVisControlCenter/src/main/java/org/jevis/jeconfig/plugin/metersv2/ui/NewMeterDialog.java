package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.metersv2.JEVisClassWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class NewMeterDialog extends Dialog {

    private static final Logger logger = LogManager.getLogger(NewMeterDialog.class);

    private JEVisDataSource jeVisDataSource;
    private Optional<JEVisObject> parent= Optional.empty();

    private final MeterPlan meterPlan;

    public String getNameProperty() {
        return nameProperty.get();
    }

    public StringProperty namePropertyProperty() {
        return nameProperty;
    }

    private StringProperty nameProperty;

    public SingleSelectionModel<JEVisClassWrapper> getJeVisClassSingleSelectionModel() {
        return jeVisClassSingleSelectionModel;
    }

    private SingleSelectionModel<JEVisClassWrapper> jeVisClassSingleSelectionModel;

    public NewMeterDialog(JEVisDataSource jeVisDataSource, MeterPlan meterPlan) {
        this.jeVisDataSource = jeVisDataSource;
        this.meterPlan = meterPlan;


        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);

        GridPane gridPane = new GridPane();
        gridPane.setPrefWidth(400);
        gridPane.setPrefHeight(400);
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        JFXButton jfxButton = new JFXButton("", JEConfig.getSVGImage(Icon.TREE,20,20));

        JFXTextField jfxTextField = new JFXTextField();

        nameProperty = jfxTextField.textProperty();




        ComboBox<JEVisClassWrapper> comboBox = new ComboBox<>();

        try{
            JEVisClassWrapper air = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Air Measurement Instrument"));
            JEVisClassWrapper compressedAir = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Compressed-Air Measurement Instrument"));
            JEVisClassWrapper electricity = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Electricity Measurement Instrument"));
            JEVisClassWrapper gas = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Gas Measurement Instrument"));
            JEVisClassWrapper heat = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Heat Measurement Instrument"));
            JEVisClassWrapper nitrogen = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Nitrogen Measurement Instrument"));
            JEVisClassWrapper water = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Water Measurement Instrument"));

            comboBox.getItems().addAll(air,compressedAir,electricity,gas,heat,nitrogen,water);

            jeVisClassSingleSelectionModel = comboBox.getSelectionModel();
        }catch (JEVisException jeVisException){
            logger.error(jeVisException);
        }




        jfxButton.setOnAction(actionEvent -> {
            TargetHelper th = null;

            th = new TargetHelper(jeVisDataSource, "");


            if (th.isValid() && th.targetObjectAccessible()) {
                logger.info("Target Is valid");
            }
            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllMeasurement(jeVisDataSource);
            allFilter.add(allDataFilter);
            List<UserSelection> openList = new ArrayList<>();
            if (th != null && !th.getObject().isEmpty()) {
                for (JEVisObject obj : th.getObject()) {
                    openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                }


            }
            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.SINGLE, jeVisDataSource, openList);
            selectTargetDialog.setOnCloseRequest(event1 -> {
                try {
                    if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        logger.trace("Selection Done");


                        List<UserSelection> selections = selectTargetDialog.getUserSelection();
                        for (UserSelection us : selections) {
                           parent = Optional.of(us.getSelectedObject());
                        }

                    }

                } catch (Exception e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis Exception", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            selectTargetDialog.showAndWait();
        });

        gridPane.addRow(0,new Label("Name"),jfxTextField);
        gridPane.addRow(1,new Label("Typ"),comboBox);
        gridPane.addRow(2,new Label("Target"),jfxButton);

        getDialogPane().setContent(gridPane);
    }

    public Optional<JEVisObject> getParent() {
        return parent;
    }

    public MeterPlan getMeterPlan() {
        return meterPlan;
    }
}
