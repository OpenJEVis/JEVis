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
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
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
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingBoundToSpecific;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonTools;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class GapFillingEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(GapFillingEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);
    public static final ObservableList<GapFillingReferencePeriod> optionsReferencePeriods = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
            GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR, GapFillingReferencePeriod.ALL);
    public static final ObservableList<GapFillingBoundToSpecific> optionsBoundSpecifics = FXCollections.observableArrayList(GapFillingBoundToSpecific.NONE, GapFillingBoundToSpecific.WEEKDAY,
            GapFillingBoundToSpecific.WEEKOFYEAR, GapFillingBoundToSpecific.MONTHOFYEAR);
    public static final ObservableList<GapFillingType> optionsType = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
            GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MINIMUM, GapFillingType.MAXIMUM, GapFillingType.MEDIAN, GapFillingType.DELETE);
    public JEVisAttribute _attribute;
    private final HBox box = new HBox(12);
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private List<JsonGapFillingConfig> _listConfig;

    public GapFillingEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();
    }

    /**
     * Creates an default configuration with two gap filling configs
     *
     * @return
     */
    public static List<JsonGapFillingConfig> createDefaultConfig() {
        List<JsonGapFillingConfig> list = new ArrayList<>();
        JsonGapFillingConfig newConfig1 = new JsonGapFillingConfig();
        newConfig1.setType(GapFillingType.INTERPOLATION.toString());
        newConfig1.setBoundary("3600000");


        newConfig1.setName(I18n.getInstance().getString("newobject.title1"));
        list.add(newConfig1);

        JsonGapFillingConfig newConfig2 = new JsonGapFillingConfig();
        newConfig2.setType(GapFillingType.AVERAGE.toString());
        newConfig2.setBoundary("2592000000");
        newConfig2.setBindtospecific(GapFillingBoundToSpecific.WEEKDAY.toString());
        newConfig2.setReferenceperiodcount("1");
        newConfig2.setReferenceperiod(GapFillingReferencePeriod.MONTH.toString());

        newConfig2.setName(I18n.getInstance().getString("newobject.title2"));
        list.add(newConfig2);
        return list;
    }

    /**
     * Build main UI
     */
    private void init() {
        JFXButton openConfig = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.openconfig"));
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
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            getEditor();
        });
    }

    @Override
    public boolean hasChanged() {
        _changed.setValue(true);

        return _changed.getValue();
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
     * Fills the given tab content with an gui for the given gap configuration
     *
     * @param tab
     * @param config
     */
    public static void fillTab(Tab tab, JsonGapFillingConfig config) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);


        //Label nameLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.name"));
        Label typeLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.type"));
        Label boundaryLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.boundary"));
        Label defaultValueLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.defaultvalue"));
        Label referencePeriodLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.referenceperiod"));
        Label referencePeriodCountLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.referenceperiodcount"));
        Label boundToSpecificLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.boundto"));

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

        NumberSpinner referencePeriodCountText = new NumberSpinner(new BigDecimal(1), new BigDecimal(1));
        referencePeriodCountText.setMin(new BigDecimal(1));
        referencePeriodCountText.setMax(new BigDecimal(99));
        JFXTextField boundaryText = new JFXTextField();
        JFXTextField defaultValueText = new JFXTextField();

        /**
         * Text layout
         */
        FXCollections.observableArrayList(typeBox, boundaryText, defaultValueText, referencePeriodBox, boundSpecificBox, referencePeriodCountText)
                .forEach(field -> field.setPrefWidth(150));
        FXCollections.observableArrayList(boundaryText, defaultValueText)
                .forEach(field -> field.setAlignment(Pos.CENTER_RIGHT));

        /**
         * Fill configuration values into gui elements
         */
        typeBox.getSelectionModel().select(GapFillingType.parse(config.getType()));
        boundaryText.setText((Long.parseLong(config.getBoundary()) / 1000) + ""); //msec -> sec
        defaultValueText.setText(config.getDefaultvalue());

        typeBox.getSelectionModel().select(GapFillingType.parse(config.getType()));
        referencePeriodBox.getSelectionModel().select(GapFillingReferencePeriod.parse(config.getReferenceperiod()));
        boundSpecificBox.getSelectionModel().select(GapFillingBoundToSpecific.parse(config.getBindtospecific()));

        BigDecimal parsedValue = new BigDecimal(145);
        try {
            if (config.getReferenceperiodcount() != null)
                parsedValue = new BigDecimal(Long.parseLong(config.getReferenceperiodcount()));
        } catch (Exception e) {
            logger.error("Parsing Exception: ", e);
        }

        referencePeriodCountText.setNumber(parsedValue);

        /**
         * Change Listeners
         */
        typeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setType(newValue.toString());
            fillTab(tab, config);
        });
        defaultValueText.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setDefaultvalue(newValue);
        });
        boundaryText.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setBoundary((Long.parseLong(newValue) * 1000l) + "");//sec -> msec
        });

        referencePeriodCountText.numberProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() <= 6 && newValue.intValue() > 0)
                config.setReferenceperiodcount(newValue.toPlainString());
        });

        referencePeriodBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setReferenceperiod(newValue.toString());
        });

        boundSpecificBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setBindtospecific(newValue.toString());
        });

        /**
         * Create layout based on JsonGapFillingConfig type
         */
        int row = 0;
        gridPane.add(typeLabel, 0, row);
        gridPane.add(typeBox, 1, row);

        row++;
        gridPane.add(boundaryLabel, 0, row);
        gridPane.add(boundaryText, 1, row);

        if (GapFillingType.parse(config.getType()).equals(GapFillingType.DEFAULT_VALUE)) {
            row++;
            gridPane.add(defaultValueLabel, 0, row);
            gridPane.add(defaultValueText, 1, row);
        } else if (GapFillingType.parse(config.getType()).equals(GapFillingType.NONE) || GapFillingType.parse(config.getType()).equals(GapFillingType.STATIC)
                || GapFillingType.parse(config.getType()).equals(GapFillingType.INTERPOLATION)) {
            //Nothing to add
        } else {
            row++;
            gridPane.add(referencePeriodLabel, 0, row);
            gridPane.add(referencePeriodBox, 1, row);
            row++;
            gridPane.add(referencePeriodCountLabel, 0, row);
            gridPane.add(referencePeriodCountText, 1, row);
            row++;
            gridPane.add(boundToSpecificLabel, 0, row);
            gridPane.add(boundSpecificBox, 1, row);
        }


        tab.setContent(gridPane);
    }

    /**
     * Parse an JsonString with an JsonGapFillingConfig configuration.
     *
     * @param jsonstring
     * @return
     */
    private List<JsonGapFillingConfig> parseJson(String jsonstring) throws IOException {
        List<JsonGapFillingConfig> list = new ArrayList<>();


        if (jsonstring.endsWith("]")) {
            list = Arrays.asList(JsonTools.objectMapper().readValue(jsonstring, JsonGapFillingConfig[].class));

        } else {
            list.add(JsonTools.objectMapper().readValue(jsonstring, JsonGapFillingConfig.class));
        }

        return list;
    }

    @Override
    public void commit() throws JEVisException {

        if (hasChanged() && _newSample != null) {
            //TODO: check if type is ok, maybe better at input time
            logger.debug("Commit: {}", _newSample.getValueAsString());
            _newSample.commit();
            _lastSample = _newSample;
            _newSample = null;
            _changed.setValue(false);
        }
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

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setHeight(450);
        dialog.setWidth(620);
        dialog.setTitle(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.dialog.title"));
        dialog.setHeaderText(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.dialog.header"));
        dialog.setGraphic(JEConfig.getImage("fill_gap.png", 48, 48));
        dialog.getDialogPane().getButtonTypes().setAll();

        for (JsonGapFillingConfig config : _listConfig) {
            Tab newTab = new Tab(config.getName());
            tabPane.getTabs().add(newTab);
            fillTab(newTab, config);
        }

        dialog.getDialogPane().contentProperty().setValue(tabPane);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);


        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                        try {
                            _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                            _changed.setValue(true);
                            commit();
                        } catch (JEVisException e) {
                            logger.error("Could not write gap config to JEVis System: ", e);
                        }
                    }
                });

    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }


    public enum Response {NO, YES, CANCEL}
}
