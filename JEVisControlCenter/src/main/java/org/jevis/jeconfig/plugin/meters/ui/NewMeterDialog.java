package org.jevis.jeconfig.plugin.meters.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.meters.JEVisClassWrapper;
import org.jevis.jeconfig.plugin.meters.data.MeterList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class NewMeterDialog extends Dialog {

    private static final Logger logger = LogManager.getLogger(NewMeterDialog.class);
    private final MeterList meterList;
    private final JEVisDataSource jeVisDataSource;
    ComboBox<JEVisClassWrapper> comboBox = new ComboBox<>();
    JFXTextField jfxTextFieldName = new JFXTextField();
    JFXButton jfxButtonTarget = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 20, 20));
    private Optional<JEVisObject> parent = Optional.empty();
    private SingleSelectionModel<JEVisClassWrapper> jeVisClassSingleSelectionModel;
    private JEVisClassWrapper air;
    private JEVisClassWrapper compressedAir;
    private JEVisClassWrapper electricity;
    private JEVisClassWrapper gas;
    private JEVisClassWrapper heat;
    private JEVisClassWrapper nitrogen;
    private JEVisClassWrapper water;

    public NewMeterDialog(JEVisDataSource jeVisDataSource, MeterList meterList) {
        this.jeVisDataSource = jeVisDataSource;
        this.meterList = meterList;


        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);

        GridPane gridPane = new GridPane();
        gridPane.setPrefWidth(600);
        gridPane.setPrefHeight(400);
        gridPane.setVgap(10);
        gridPane.setHgap(25);


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


        jfxTextFieldName.setPrefWidth(350);
        comboBox.setPrefWidth(350);
        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.meters.name")), jfxTextFieldName);
        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.meters.medium")), comboBox);
        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.meters.parent")), jfxButtonTarget);

        getDialogPane().setContent(gridPane);
    }

    public SingleSelectionModel<JEVisClassWrapper> getJeVisClassSingleSelectionModel() {
        return jeVisClassSingleSelectionModel;
    }

    public Optional<JEVisObject> getParent() {
        return parent;
    }

    public JEVisObject commit() {

        try {
            JEVisObject parent = getParent().orElse(getDirectory(meterList.getJeVisObject(), comboBox.getValue().getJeVisClass()));
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

    private JEVisObject getDirectory(JEVisObject rootDirectory, JEVisClass jeVisClass) throws JEVisException {

        List<JEVisObject> jeVisObjects = rootDirectory.getChildren(getDirectoryClass(jeVisClass), false);

        if (jeVisObjects.size() == 0) {
            return createDirectory(rootDirectory, jeVisClass);

        } else {
            return jeVisObjects.get(0);
        }

    }

    private JEVisObject createDirectory(JEVisObject rootDirectory, JEVisClass jeVisClass) throws JEVisException {

        JEVisClass directory = getDirectoryClass(jeVisClass);

        if (directory == null) return null;
        JEVisObject newDirectory = rootDirectory.buildObject(directory.getName(), directory);
        newDirectory.commit();


        return newDirectory;


    }

    private JEVisClass getDirectoryClass(JEVisClass jeVisClass) throws JEVisException {
        JEVisClass air = this.air.getJeVisClass();
        JEVisClass compressedAir = this.compressedAir.getJeVisClass();
        JEVisClass electricity = this.electricity.getJeVisClass();
        JEVisClass gas = this.gas.getJeVisClass();
        JEVisClass heat = this.heat.getJeVisClass();
        JEVisClass nitrogen = this.nitrogen.getJeVisClass();
        JEVisClass water = this.water.getJeVisClass();

        JEVisClass directory = null;


        logger.debug(jeVisClass.getName());
        logger.debug(compressedAir.getName());

        logger.debug(jeVisClass.equals(compressedAir));

        if (jeVisClass.equals(air)) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.AirMeasurementDirectory.name);
        } else if (jeVisClass.getName().equals(compressedAir.getName())) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.CompressedAirMeasurementDirectory.name);
        } else if (jeVisClass.equals(electricity)) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.ElectricityMeasurementDirectory.name);
        } else if (jeVisClass.equals(gas)) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.GasMeasurementDirectory.name);
        } else if (jeVisClass.equals(heat)) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.HeatingEquipmentDirectory.name);
        } else if (jeVisClass.equals(nitrogen)) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.NitrogenMeasurementDirectory.name);
        } else if (jeVisClass.equals(water)) {
            directory = jeVisDataSource.getJEVisClass(JC.Directory.WaterMeasurementDirectory.name);
        }

        return directory;


    }


}

