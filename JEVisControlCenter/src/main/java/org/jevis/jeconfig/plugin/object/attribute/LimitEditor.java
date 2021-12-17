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
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
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
    private final ObservableList<GapFillingReferencePeriod> optionsReferencePeriods = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
            GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR, GapFillingReferencePeriod.ALL);
    private final ObservableList<GapFillingBoundToSpecific> optionsBoundSpecifics = FXCollections.observableArrayList(GapFillingBoundToSpecific.NONE, GapFillingBoundToSpecific.WEEKDAY,
            GapFillingBoundToSpecific.WEEKOFYEAR, GapFillingBoundToSpecific.MONTHOFYEAR);
    private final ObservableList<GapFillingType> optionsType = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
            GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MINIMUM, GapFillingType.MAXIMUM, GapFillingType.MEDIAN, GapFillingType.DELETE);
    private final StackPane dialogContainer;
    private String unitString = "";
    public JEVisAttribute _attribute;
    private final HBox box = new HBox(12);
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private List<JsonLimitsConfig> _listConfig;
    private final boolean delete = false;
    private final DoubleValidator dv = DoubleValidator.getInstance();

    public LimitEditor(StackPane dialogContainer, JEVisAttribute att) {
        this.dialogContainer = dialogContainer;
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();
        try {
            JEVisAttribute valueAttribute = _attribute.getObject().getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME);
            if (valueAttribute != null) {
                unitString = UnitManager.getInstance().format(valueAttribute.getDisplayUnit());
            }
        } catch (JEVisException e) {
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

        box.getChildren().addAll(openConfig);
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

    @Override
    public void commit() throws JEVisException {

        if (hasChanged() && _newSample != null) {
            //TODO: check if tpye is ok, maybe better at imput time
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

        JFXDialog dialog = new JFXDialog();
        dialog.setDialogContainer(dialogContainer);
        dialog.setTransitionType(JFXDialog.DialogTransition.NONE);

        for (JsonLimitsConfig config : _listConfig) {
            Tab newTab = new Tab(config.getName());
            tabPane.getTabs().add(newTab);
            fillTab(newTab, config);

        }

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
        cancel.setCancelButton(true);

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, tabPane, separator, buttonBar);
        dialog.setContent(vBox);

        ok.setOnAction(event -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                _changed.setValue(true);
                commit();
            } catch (JEVisException e) {
                logger.error("Could not write limit config to JEVis System: " + e);
            }
            dialog.close();
        });

        cancel.setOnAction(event -> dialog.close());

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

        JFXComboBox<GapFillingType> typeBox = new JFXComboBox<>(optionsType);
        Callback<ListView<GapFillingType>, ListCell<GapFillingType>> cellFactoryTypeBox = new Callback<javafx.scene.control.ListView<GapFillingType>, ListCell<GapFillingType>>() {
            @Override
            public ListCell<GapFillingType> call(javafx.scene.control.ListView<GapFillingType> param) {
                return new ListCell<GapFillingType>() {
                    @Override
                    protected void updateItem(GapFillingType type, boolean empty) {
                        super.updateItem(type, empty);
                        if (empty || type == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (type) {
                                case NONE:
                                    text = I18n.getInstance().getString("plugin.alarm.table.translation.none");
                                    break;
                                case STATIC:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.static");
                                    break;
                                case INTERPOLATION:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.interpolation");
                                    break;
                                case DEFAULT_VALUE:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.default");
                                    break;
                                case MINIMUM:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.min");
                                    break;
                                case MAXIMUM:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.max");
                                    break;
                                case MEDIAN:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.median");
                                    break;
                                case AVERAGE:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.average");
                                    break;
                                case DELETE:
                                    text = I18n.getInstance().getString("graph.dialog.note.text.limit2.delete");
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        typeBox.setCellFactory(cellFactoryTypeBox);
        typeBox.setButtonCell(cellFactoryTypeBox.call(null));

        JFXComboBox<GapFillingReferencePeriod> referencePeriodBox = new JFXComboBox<>(optionsReferencePeriods);
        Callback<ListView<GapFillingReferencePeriod>, ListCell<GapFillingReferencePeriod>> cellFactoryReferencePeriodBox = new Callback<javafx.scene.control.ListView<GapFillingReferencePeriod>, ListCell<GapFillingReferencePeriod>>() {
            @Override
            public ListCell<GapFillingReferencePeriod> call(javafx.scene.control.ListView<GapFillingReferencePeriod> param) {
                return new ListCell<GapFillingReferencePeriod>() {
                    @Override
                    protected void updateItem(GapFillingReferencePeriod referencePeriod, boolean empty) {
                        super.updateItem(referencePeriod, empty);
                        if (empty || referencePeriod == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (referencePeriod) {
                                case DAY:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.day");
                                    break;
                                case WEEK:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.week");
                                    break;
                                case MONTH:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.month");
                                    break;
                                case YEAR:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.year");
                                    break;
                                case ALL:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.all");
                                    break;
                                case NONE:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.referenceperiod.none");
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        referencePeriodBox.setCellFactory(cellFactoryReferencePeriodBox);
        referencePeriodBox.setButtonCell(cellFactoryReferencePeriodBox.call(null));

        JFXComboBox<GapFillingBoundToSpecific> boundSpecificBox = new JFXComboBox<>(optionsBoundSpecifics);
        Callback<ListView<GapFillingBoundToSpecific>, ListCell<GapFillingBoundToSpecific>> cellFactoryBoundToSpecificBox = new Callback<javafx.scene.control.ListView<GapFillingBoundToSpecific>, ListCell<GapFillingBoundToSpecific>>() {
            @Override
            public ListCell<GapFillingBoundToSpecific> call(javafx.scene.control.ListView<GapFillingBoundToSpecific> param) {
                return new ListCell<GapFillingBoundToSpecific>() {
                    @Override
                    protected void updateItem(GapFillingBoundToSpecific boundToSpecific, boolean empty) {
                        super.updateItem(boundToSpecific, empty);
                        if (empty || boundToSpecific == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (boundToSpecific) {
                                case NONE:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.none");
                                    break;
                                case WEEKDAY:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.weekday");
                                    break;
                                case WEEKOFYEAR:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.weekofyear");
                                    break;
                                case MONTHOFYEAR:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.monthofyear");
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        boundSpecificBox.setCellFactory(cellFactoryBoundToSpecificBox);
        boundSpecificBox.setButtonCell(cellFactoryBoundToSpecificBox.call(null));

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
        if (!unitString.equals("")) {
            gridPane.add(unitFieldMin, 3, row);
        }
        row++;

        gridPane.add(maxLabel, 0, row);
        gridPane.add(maxField, 1, row, 2, 1);
        if (!unitString.equals("")) {
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
