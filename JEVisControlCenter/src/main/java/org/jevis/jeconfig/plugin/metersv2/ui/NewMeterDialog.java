package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
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
    private Optional<JEVisObject> parent = Optional.empty();

    private final MeterPlan meterPlan;


    public SingleSelectionModel<JEVisClassWrapper> getJeVisClassSingleSelectionModel() {
        return jeVisClassSingleSelectionModel;
    }

    private SingleSelectionModel<JEVisClassWrapper> jeVisClassSingleSelectionModel;

    private JEVisClassWrapper air;
    private JEVisClassWrapper compressedAir;
    private JEVisClassWrapper electricity;
    private JEVisClassWrapper gas;
    private JEVisClassWrapper heat;
    private JEVisClassWrapper nitrogen;
    private JEVisClassWrapper water;

    ComboBox<JEVisClassWrapper> comboBox = new ComboBox<>();
    JFXTextField jfxTextFieldName = new JFXTextField();

    JFXButton jfxButtonTarget = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 20, 20));


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


        try {
            air = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Air Measurement Instrument"));
            compressedAir = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Compressed-Air Measurement Instrument"));
            electricity = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Electricity Measurement Instrument"));
            gas = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Gas Measurement Instrument"));
            heat = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Heat Measurement Instrument"));
            nitrogen = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Nitrogen Measurement Instrument"));
            water = new JEVisClassWrapper(jeVisDataSource.getJEVisClass("Water Measurement Instrument"));

            comboBox.getItems().addAll(air, compressedAir, electricity, gas, heat, nitrogen, water);

            jeVisClassSingleSelectionModel = comboBox.getSelectionModel();
        } catch (JEVisException jeVisException) {
            logger.error(jeVisException);
        }


        jfxButtonTarget.setOnAction(actionEvent -> {
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

        gridPane.addRow(0, new Label("Name"), jfxTextFieldName);
        gridPane.addRow(1, new Label("Typ"), comboBox);
        gridPane.addRow(2, new Label("Target"), jfxButtonTarget);

        getDialogPane().setContent(gridPane);
    }

    public Optional<JEVisObject> getParent() {
        return parent;
    }

    public JEVisObject commit() {

        try {
            JEVisObject parent = getParent().orElse(getDirectory(meterPlan.getJeVisObject(), comboBox.getValue().getJeVisClass()));
            JEVisObject jeVisObject = parent.buildObject(jfxTextFieldName.getText(), comboBox.getValue().getJeVisClass());
            if (jeVisObject.isAllowedUnder(parent)) {
                jeVisObject.commit();
                return jeVisObject;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;


    }


    public MeterPlan getMeterPlan() {
        return meterPlan;
    }

    private JEVisObject getDirectory(JEVisObject rootDirectory, JEVisClass jeVisClass) throws JEVisException {

        List<JEVisObject> jeVisObjects = rootDirectory.getChildren(jeVisClass, false);

        if (jeVisObjects.size() == 0) {
            return createDirectory(rootDirectory, jeVisClass);

        } else {
            return jeVisObjects.get(0);
        }

    }

    private JEVisObject createDirectory(JEVisObject rootDirectory, JEVisClass jeVisClass) throws JEVisException {
        JEVisClass air = this.air.getJeVisClass();
        JEVisClass compressedAir = this.air.getJeVisClass();
        JEVisClass electricity = this.electricity.getJeVisClass();
        JEVisClass gas = this.gas.getJeVisClass();
        JEVisClass heat = this.heat.getJeVisClass();
        JEVisClass nitrogen = this.nitrogen.getJeVisClass();
        JEVisClass water = this.water.getJeVisClass();








        JEVisObject newDirectory = null;
        if (jeVisClass.equals(air)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.AirMeasurementDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else if (jeVisClass.equals(compressedAir)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.CompressedAirMeasurementDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else if (jeVisClass.equals(electricity)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.ElectricityMeasurementDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else if (jeVisClass.equals(gas)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.GasMeasurementDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else if (jeVisClass.equals(heat)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.HeatingEquipmentDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else if (jeVisClass.equals(nitrogen)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.NitrogenMeasurementDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else if (jeVisClass.equals(water)) {
            JEVisClass directory = jeVisDataSource.getJEVisClass(JC.Directory.WaterMeasurementDirectory.name);
            newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        } else {
            return null;
        }
        if (newDirectory != null) {
            newDirectory.commit();
        }
        return newDirectory;


    }


}

