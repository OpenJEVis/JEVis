/**
 * Copyright (C) 2018 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object.attribute;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.json.JsonTools;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.control.BoundSpecificBox;
import org.jevis.jeconfig.application.control.GapFillingTypeBox;
import org.jevis.jeconfig.application.control.ReferencePeriodsBox;
import org.joda.time.DateTime;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class LimitEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(LimitEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);
    private String unitString = "";
    public JEVisAttribute _attribute;
    private final HBox box = new HBox(12);
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private List<JsonLimitsConfig> _listConfig;
    private final boolean delete = false;
    private final DoubleValidator dv = DoubleValidator.getInstance();
    private boolean initialized = false;

    public LimitEditor(JEVisAttribute att) {

        _attribute = att;
        _lastSample = _attribute.getLatestSample();
        try {
            JEVisAttribute valueAttribute = _attribute.getObject().getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME);
            if (valueAttribute != null) {
                unitString = UnitManager.getInstance().format(valueAttribute.getDisplayUnit());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an default configuration with two gap filling configs
     *
     * @return
     */
    public static List<JsonLimitsConfig> createDefaultConfig() {
        List<JsonLimitsConfig> list = new ArrayList<>();

        JsonLimitsConfig newConfig1 = new JsonLimitsConfig();

        newConfig1.setName(I18n.getInstance().getString("newobject.title1"));
        list.add(newConfig1);

        JsonLimitsConfig newConfig2 = new JsonLimitsConfig();

        newConfig2.setName(I18n.getInstance().getString("newobject.title2"));
        list.add(newConfig2);
        return list;
    }

    /**
     * Build main UI
     */
    private void init() {
        JFXButton openConfig = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.limitseditor.openconfig"));
        openConfig.setOnAction(action -> {
            try {
                show();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });

        box.getChildren().setAll(openConfig);

        initialized = true;
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            init();
        });
    }

    @Override
    public Node getEditor() {
        try {
            if (!initialized) {
                init();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly.setValue(canRead);
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    @Override
    public void commit() throws JEVisException {

        if (hasChanged() && _newSample != null) {
            //TODO: check if type is ok, maybe better at input time
            logger.debug("Commit: " + _newSample.getValueAsString());
            _newSample.commit();
            _lastSample = _newSample;
            _newSample = null;
            _changed.setValue(false);
        }
    }

    /**
     * Parse an JsonString with an JsonGapFillingConfig configuration.
     *
     * @param jsonstring
     * @return
     */
    private List<JsonLimitsConfig> parseJson(String jsonstring) throws IOException {
        List<JsonLimitsConfig> list = new ArrayList<>();


        if (jsonstring.endsWith("]")) {
            list = Arrays.asList(JsonTools.objectMapper().readValue(jsonstring, JsonLimitsConfig[].class));
        } else {
            list.add(JsonTools.objectMapper().readValue(jsonstring, JsonLimitsConfig.class));
        }

        return list;
    }

    /**
     * Show the configuration dialog
     *
     * @throws JEVisException
     */
    private void show() throws JEVisException {
        if (_lastSample != null && !_lastSample.getValueAsString().isEmpty()) {
            try {
                _listConfig = parseJson(_lastSample.getValueAsString());
            } catch (IOException e) {
                logger.error("Could not parse Json: {}", _lastSample.getValueAsString(), e);
            }
        } else {
            _listConfig = createDefaultConfig();
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Dialog dialog = new Dialog();
        dialog.setTitle(I18n.getInstance().getString("plugin.configuration.limiteditor.title"));
        dialog.setHeaderText(I18n.getInstance().getString("plugin.configuration.limiteditor.header"));
        dialog.setResizable(true);
        dialog.initOwner(JEConfig.getStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        for (JsonLimitsConfig config : _listConfig) {
            Tab newTab = new Tab(config.getName());
            tabPane.getTabs().add(newTab);
            fillTab(newTab, config);
        }

        ButtonType okType = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, tabPane, separator);
        dialog.getDialogPane().setContent(vBox);

        okButton.setOnAction(event -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                _changed.setValue(true);
                commit();
            } catch (JEVisException e) {
                logger.error("Could not write limit config to JEVis System: " + e);
            }
            dialog.close();
        });

        cancelButton.setOnAction(event -> dialog.close());

        dialog.show();
    }

    /**
     * Fills the given tab content with an gui for the given gap configuration
     *
     * @param tab
     * @param config
     */
    private void fillTab(Tab tab, JsonLimitsConfig config) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setMinHeight(220);//we have hidden element and need space
        gridPane.setMinWidth(435);

        Label minLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.min"));
        Label maxLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.max"));

        JFXTextField nameField = new JFXTextField();
        JFXTextField minField = new JFXTextField();
        JFXTextField maxField = new JFXTextField();
        Label unitFieldMin = new Label(unitString);
        Label unitFieldMax = new Label(unitString);

        GapFillingTypeBox typeBox = new GapFillingTypeBox();
        typeBox.setMaxWidth(800);

        ReferencePeriodsBox referencePeriodBox = new ReferencePeriodsBox();
        referencePeriodBox.setMaxWidth(800);

        BoundSpecificBox boundSpecificBox = new BoundSpecificBox();
        boundSpecificBox.setMaxWidth(800);

        double prefFieldWidth = 150;

        /**
         * Text layout
         */
        FXCollections.observableArrayList(typeBox, boundSpecificBox, referencePeriodBox, minField, maxField)
                .forEach(field -> {
                    GridPane.setHgrow(field, Priority.ALWAYS);
                    field.setMinWidth(prefFieldWidth);
                    field.setMaxWidth(prefFieldWidth);
                });
        FXCollections.observableArrayList(nameField, minField, maxField)
                .forEach(field -> field.setAlignment(Pos.CENTER_RIGHT));

        /**
         * Fill configuration values into gui elements
         */
        try {
            NumberFormat numberFormat = NumberFormat.getInstance(I18n.getInstance().getLocale());
            if (config.getMin() != null) {
                Double min = Double.parseDouble(config.getMin());
                minField.setText(numberFormat.format(min));
            }

            if (config.getMax() != null) {
                Double max = Double.parseDouble(config.getMax());
                maxField.setText(numberFormat.format(max));
            }
        } catch (Exception e) {
            logger.error("Could not parse limit values", e);
        }

        typeBox.getSelectionModel().select(GapFillingType.parse(config.getTypeOfSubstituteValue()));
        referencePeriodBox.getSelectionModel().select(GapFillingReferencePeriod.parse(config.getReferenceperiod()));
        boundSpecificBox.getSelectionModel().select(GapFillingBoundToSpecific.parse(config.getBindtospecific()));

        /**
         * Change Listeners
         */
        typeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.info("Type Event: " + newValue);
            config.setTypeOfSubstituteValue(newValue.toString());
            fillTab(tab, config);
        });
        referencePeriodBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setReferenceperiod(newValue.toString());
        });

        boundSpecificBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setBindtospecific(newValue.toString());
        });

        minField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                config.setMin(parsedValue);
            } catch (Exception e) {
                minField.setText(oldValue);
            }
        });
        maxField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String parsedValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                config.setMax(parsedValue);
            } catch (Exception e) {
                maxField.setText(oldValue);
            }
        });

        /**
         * Create layout based on JsonLimitsConfig type
         */
        int row = 0;

        gridPane.add(minLabel, 0, row);
        gridPane.add(minField, 1, row, 2, 1);
        if (!unitString.isEmpty()) {
            gridPane.add(unitFieldMin, 3, row);
        }
        row++;

        gridPane.add(maxLabel, 0, row);
        gridPane.add(maxField, 1, row, 2, 1);
        if (!unitString.isEmpty()) {
            gridPane.add(unitFieldMax, 3, row);
        }

        tab.setContent(gridPane);
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }


    public enum Response {NO, YES, CANCEL}
}
