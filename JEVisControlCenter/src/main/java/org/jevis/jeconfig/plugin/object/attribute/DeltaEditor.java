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
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import org.jevis.commons.dataprocessing.VirtualAttribute;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonDeltaConfig;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonTools;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.joda.time.DateTime;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class DeltaEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(DeltaEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);
    private final HBox box = new HBox(12);
    private final boolean delete = false;
    private final DoubleValidator dv = DoubleValidator.getInstance();
    public JEVisAttribute _attribute;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private JsonDeltaConfig deltaConfig;
    private boolean initialized = false;

    public DeltaEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();


    }

    /**
     * Creates a default configuration standard gap filling configs
     *
     * @return
     */
    public static JsonDeltaConfig createDefaultConfig() {
        JsonDeltaConfig deltaConfig = new JsonDeltaConfig();
        deltaConfig.setName("Delta Configuration");

        deltaConfig.setMin(String.valueOf(0));
        deltaConfig.setMax(String.valueOf(0));

        JsonGapFillingConfig newConfig2 = new JsonGapFillingConfig();
        newConfig2.setType(GapFillingType.AVERAGE.toString());
        newConfig2.setBoundary("2592000000");
        newConfig2.setBindtospecific(GapFillingBoundToSpecific.WEEKDAY.toString());
        newConfig2.setReferenceperiodcount("1");
        newConfig2.setReferenceperiod(GapFillingReferencePeriod.MONTH.toString());
        newConfig2.setName(I18n.getInstance().getString("newobject.title2"));
        deltaConfig.setMaxConfig(newConfig2);

        return deltaConfig;
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
            //TODO: check if tpye is ok, maybe better at imput time
            logger.debug("Commit: {}", _newSample.getValueAsString());
            _newSample.commit();
            _lastSample = _newSample;
            _newSample = null;
            _changed.setValue(false);
        }
    }

    /**
     * Parse an JsonString with an JsonGapFillingConfig configuration.
     *
     * @param jsonString
     * @return
     */
    private JsonDeltaConfig parseJson(String jsonString) throws IOException {
        return JsonTools.objectMapper().readValue(jsonString, JsonDeltaConfig.class);
    }

    /**
     * Show the configuration dialog
     *
     * @throws JEVisException
     */
    private void show() throws JEVisException {
        if (_lastSample != null && !_lastSample.getValueAsString().isEmpty()) {
            try {
                deltaConfig = parseJson(_lastSample.getValueAsString());
            } catch (IOException e) {
                logger.error("Could not parse Json: {}", _lastSample.getValueAsString(), e);
            }
        } else {
            deltaConfig = createDefaultConfig();
        }

        Dialog dialog = new Dialog();
        dialog.setTitle(I18n.getInstance().getString("plugin.configuration.deltaeditor.title"));
        dialog.setHeaderText(I18n.getInstance().getString("plugin.configuration.deltaeditor.header"));
        dialog.setResizable(true);
        dialog.initOwner(JEConfig.getStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        GridPane gp = createContent(deltaConfig);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, gp, separator);
        dialog.getDialogPane().setContent(vBox);

        okButton.setOnAction(event -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), deltaConfig.toString());
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
     * Fills the given tab content with a gui for the given gap configuration
     *
     * @param config
     */
    private GridPane createContent(JsonDeltaConfig config) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setMinHeight(220);//we have hidden element and need space
        gridPane.setMinWidth(435);

        Label minLabel = new Label(I18n.getInstance().getString("newobject.title1"));
        Label maxLabel = new Label(I18n.getInstance().getString("newobject.title2"));

        JFXTextField minField = new JFXTextField();
        JFXTextField maxField = new JFXTextField();

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

        Label unitFieldMin = new Label("%");
        Label unitFieldMax = new Label("%");

        JEVisAttribute maxConfigAtt = new VirtualAttribute(this._attribute.getObject(), "maxConfig");
        VirtualSample maxSample = new VirtualSample(new DateTime(), config.getMaxConfig().toString());
        List<JEVisSample> maxConfigList = new ArrayList<>();
        maxConfigList.add(maxSample);
        try {
            maxConfigAtt.addSamples(maxConfigList);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        GapFillingEditor maxConfig = new GapFillingEditor(maxConfigAtt);
        maxConfig.getValueChangedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                List<JsonGapFillingConfig> jsonGapFillingConfigs = maxConfig.parseJson(maxConfigAtt.getLatestSample().getValueAsString());
                config.setMaxConfig(jsonGapFillingConfigs.get(0));
            } catch (IOException | JEVisException e) {
                e.printStackTrace();
            }
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

        VBox minLabelBox = new VBox(minLabel);
        minLabelBox.setAlignment(Pos.CENTER);
        VBox unitFieldMinBox = new VBox(unitFieldMin);
        unitFieldMinBox.setAlignment(Pos.CENTER);

        gridPane.add(minLabelBox, 0, row);
        gridPane.add(minField, 1, row, 2, 1);
        gridPane.add(unitFieldMinBox, 3, row);
        row++;

        VBox maxLabelBox = new VBox(maxLabel);
        maxLabelBox.setAlignment(Pos.CENTER);
        VBox unitFieldMaxBox = new VBox(unitFieldMax);
        unitFieldMaxBox.setAlignment(Pos.CENTER);

        gridPane.add(maxLabelBox, 0, row);
        gridPane.add(maxField, 1, row, 2, 1);
        gridPane.add(unitFieldMaxBox, 3, row);
        gridPane.add(maxConfig.getEditor(), 4, row);

        return gridPane;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }


    public enum Response {NO, YES, CANCEL}
}
