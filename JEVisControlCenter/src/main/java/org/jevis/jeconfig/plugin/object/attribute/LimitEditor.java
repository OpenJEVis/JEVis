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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.constants.JEDataProcessorConstants.*;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class LimitEditor implements AttributeEditor {
    private final Logger logger = LogManager.getLogger(LimitEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);
    private final ObservableList<String> optionsReferencePeriods = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
            GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR);
    private final ObservableList<String> optionsBoundSpecifics = FXCollections.observableArrayList(GapFillingBoundToSpecific.NONE, GapFillingBoundToSpecific.WEEKDAY,
            GapFillingBoundToSpecific.WEEKOFYEAR, GapFillingBoundToSpecific.MONTHOFYEAR);
    private final ObservableList<String> optionsType = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
            GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MINIMUM, GapFillingType.MAXIMUM, GapFillingType.MEDIAN);
    public JEVisAttribute _attribute;
    private HBox box = new HBox(12);
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private List<JsonLimitsConfig> _listConfig;
    private boolean delete = false;

    public LimitEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();

    }

    /**
     * Build main UI
     */
    private void init() {
        Button openConfig = new Button(I18n.getInstance().getString("plugin.object.attribute.limitseditor.openconfig"));
        openConfig.setOnAction(action -> {
            try {
                show();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });


        ToggleSwitchPlus enableButton = new ToggleSwitchPlus();

        enableButton.selectedProperty().addListener((observable, oldValue, newValue) -> {


            if (!newValue) {
                _changed.setValue((_lastSample != null));
                delete = (_lastSample != null);
            }

        });

        openConfig.visibleProperty().bind(enableButton.selectedProperty());

        try {
            if (_lastSample != null && !_lastSample.getValueAsString().isEmpty()) {
                enableButton.setSelected(true);
//                enableButton.setText(I18n.getInstance().getString("button.toggle.activate"));
            } else {
                enableButton.setSelected(false);
//                enableButton.setText(I18n.getInstance().getString("button.toggle.deactivate"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        box.getChildren().addAll(enableButton, openConfig);
    }

    @Override
    public boolean hasChanged() {
//        _changed.setValue(true);

        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.debug("StringValueEditor.commit(): '{}' {} {}", _attribute.getName(), hasChanged(), _newSample);

        if (hasChanged() && delete) {
            _attribute.deleteAllSample();
        } else if (hasChanged() && _newSample != null) {
            //TODO: check if tpye is ok, maybe better at imput time
            _newSample.commit();
            _lastSample = _newSample;
            _newSample = null;
            _changed.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        try {
            init();
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

    /**
     * Creates an default configuration with two gap filling configs
     *
     * @return
     */
    private List<JsonLimitsConfig> createDefaultConfig() {
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
     * Parse an JsonString with an JsonGapFillingConfig configuration.
     *
     * @param jsonstring
     * @return
     */
    private List<JsonLimitsConfig> parseJson(String jsonstring) {
        List<JsonLimitsConfig> list = new ArrayList<>();


        if (jsonstring.endsWith("]")) {
            list = new Gson().fromJson(jsonstring, new TypeToken<List<JsonLimitsConfig>>() {
            }.getType());
        } else {
            list.add(new Gson().fromJson(jsonstring, JsonLimitsConfig.class));
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
            _listConfig = parseJson(_lastSample.getValueAsString());
        } else {
            _listConfig = createDefaultConfig();
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setHeight(300);
        dialog.setWidth(620);


        dialog.setTitle(I18n.getInstance().getString("plugin.object.attribute.limitseditor.dialog.title"));
        dialog.setHeaderText(I18n.getInstance().getString("plugin.object.attribute.limitseditor.dialog.header"));
        dialog.setGraphic(JEConfig.getImage("fill_gap.png", 48, 48));
        dialog.getDialogPane().getButtonTypes().setAll();

        for (JsonLimitsConfig config : _listConfig) {
            Tab newTab = new Tab(config.getName());
            tabPane.getTabs().add(newTab);
            fillTab(newTab, config);

        }

        dialog.getDialogPane().contentProperty().setValue(tabPane);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);


        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                        try {
                            _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                            System.out.println("Commit: " + _newSample.getValueAsString());
                            _changed.setValue(true);
//                            commit();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    }
                });

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


        Label nameLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.name"));
        Label minLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.min"));
        Label maxLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.max"));
        Label typeOfSubstituteLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.typeOfSubstituteValue"));
        Label durationOverUnderRunLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.durationOverUnderRun"));
        Label defaultMinLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.defaultminvalue"));
        Label defaultMaxLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.defaultmaxvalue"));
        Label referencePeriodLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.referenceperiod"));
        Label referencePeriodCountLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.referenceperiodcount"));
        Label boundTosSecificLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.boundto"));


        JFXTextField nameField = new JFXTextField();
        JFXTextField minField = new JFXTextField();
        JFXTextField maxField = new JFXTextField();
        JFXTextField durationOverUnderRunField = new JFXTextField();
        JFXTextField defaultMinField = new JFXTextField();
        JFXTextField defaultMaxField = new JFXTextField();
        JFXTextField referencePeriodCountField = new JFXTextField();


        JFXComboBox typeBox = new JFXComboBox(optionsType);
        JFXComboBox referencePeriodBox = new JFXComboBox(optionsReferencePeriods);
        JFXComboBox boundSpecificBox = new JFXComboBox(optionsBoundSpecifics);

        double prefFieldWidth = 150;

//        referencePeriodBox.setMinWidth(100);
//        referencePeriodBox.setMaxWidth(100);
//        referencePeriodCountField.setMinWidth(prefFieldWidth-referencePeriodBox.getMinWidth()-gridPane.getHgap());
//        referencePeriodCountField.setMinWidth(prefFieldWidth-referencePeriodBox.getMaxWidth()-gridPane.getHgap());

        /**
         * Text layout
         */
        FXCollections.observableArrayList(typeBox, boundSpecificBox, referencePeriodBox, minField, referencePeriodCountField, maxField, durationOverUnderRunField, defaultMinField, defaultMinField, defaultMaxField)
                .forEach(field -> {
                    GridPane.setHgrow(field, Priority.ALWAYS);
//                    field.setPrefWidth(prefFieldWidth);
                    field.setMinWidth(prefFieldWidth);
                    field.setMaxWidth(prefFieldWidth);
                });
        FXCollections.observableArrayList(nameField, minField, maxField, durationOverUnderRunField, defaultMinField, defaultMaxField, referencePeriodCountField)
                .forEach(field -> field.setAlignment(Pos.CENTER_RIGHT));
//        FXCollections.observableArrayList(typeBox,boundSpecificBox, _field_Min, _field_Max, _field_Duration_Over_Underrun, _field_Default_Min_Value, _field_Default_Min_Value,_field_Default_Max_Value,_field_Reference_Period_Count)
//                .forEach(field -> field.setPrefWidth(150));


        /**
         * Fill configuration values into gui elements
         */
        try {
            durationOverUnderRunField.setText((Long.parseLong(config.getDurationOverUnderRun()) / 1000) + ""); //msec -> sec
        } catch (Exception ex) {
        }
        minField.setText(config.getMin());
        maxField.setText(config.getMax());
        defaultMinField.setText(config.getDefaultMinValue());
        defaultMaxField.setText(config.getDefaultMaxValue());
        referencePeriodCountField.setText(config.getReferenceperiodcount());


        typeBox.getSelectionModel().select(
                optionsType.contains(config.getTypeOfSubstituteValue()) ? config.getTypeOfSubstituteValue()
                        : GapFillingType.NONE);
        referencePeriodBox.getSelectionModel().select(
                optionsReferencePeriods.contains(config.getReferenceperiod()) ? config.getReferenceperiod()
                        : GapFillingReferencePeriod.NONE);
        boundSpecificBox.getSelectionModel().select(
                optionsBoundSpecifics.contains(config.getBindtospecific()) ? config.getBindtospecific()
                        : GapFillingBoundToSpecific.NONE);


        /**
         * Change Listeners
         */
        typeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Type Event: " + newValue);
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
            config.setMin(newValue);
        });
        maxField.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setMax(newValue);
        });
        durationOverUnderRunField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                config.setDurationOverUnderRun((Long.parseLong(newValue) * 1000l) + "");//sec -> msec
            } catch (Exception ex) {
            }
        });
        defaultMinField.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setDefaultMinValue(newValue);
        });
        defaultMaxField.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setDefaultMaxValue(newValue);
        });
        referencePeriodCountField.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setReferenceperiodcount(newValue);
        });


        /**
         * Create layout based on JsonGapFillingConfig type
         */
        int row = 0;
//        gridPane.add(name, 0, row);
//        gridPane.add(typeBox, 1, row);
//        row++;
        gridPane.add(minLabel, 0, row);
        gridPane.add(minField, 1, row, 2, 1);
        row++;
        gridPane.add(maxLabel, 0, row);
        gridPane.add(maxField, 1, row, 2, 1);
        row++;
        gridPane.add(durationOverUnderRunLabel, 0, row);
        gridPane.add(durationOverUnderRunField, 1, row, 2, 1);
        row++;
        gridPane.add(typeOfSubstituteLabel, 0, row);
        gridPane.add(typeBox, 1, row, 2, 1);

//        System.out.println("Type: '" +config.getTypeOfSubstituteValue()+"' ?= '" +GapFillingType.MEDIAN +"' ="+ (config.getTypeOfSubstituteValue().equals(GapFillingType.MEDIAN)));
        if (config.getTypeOfSubstituteValue() == null || config.getTypeOfSubstituteValue().equals(GapFillingType.NONE)) {

        } else if (config.getTypeOfSubstituteValue().equals(GapFillingType.DEFAULT_VALUE)) {
            row++;
            gridPane.add(defaultMinLabel, 0, row);
            gridPane.add(defaultMinField, 1, row, 2, 1);
            row++;
            gridPane.add(defaultMaxLabel, 0, row);
            gridPane.add(defaultMaxField, 1, row, 2, 1);
        } else if (config.getTypeOfSubstituteValue().equals(GapFillingType.INTERPOLATION)
                || config.getTypeOfSubstituteValue().equals(GapFillingType.AVERAGE)
                || config.getTypeOfSubstituteValue().equals(GapFillingType.MEDIAN)) {
            row++;
            gridPane.add(referencePeriodLabel, 0, row);
            gridPane.add(referencePeriodBox, 1, row, 2, 1);
            row++;
            gridPane.add(referencePeriodCountLabel, 0, row);
            gridPane.add(referencePeriodCountField, 1, row, 2, 1);
            row++;
            gridPane.add(boundTosSecificLabel, 0, row);
            gridPane.add(boundSpecificBox, 1, row, 2, 1);
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
