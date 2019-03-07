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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.constants.JEDataProcessorConstants.*;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class GapFillingEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(GapFillingEditor.class);
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
    private List<JsonGapFillingConfig> _listConfig;

    public GapFillingEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();
    }

    /**
     * Build main UI
     */
    private void init() {
        Button openConfig = new Button(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.openconfig"));
        openConfig.setOnAction(action -> {
            try {
                show();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });


//        ToggleSwitchPlus enableButton = new ToggleSwitchPlus();
//        enableButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
////                enableButton.setText(I18n.getInstance().getString("button.toggle.activate"));
//                _changed.setValue((_lastSample != null));
//                delete = (_lastSample != null);
//            } else {
////                enableButton.setText(I18n.getInstance().getString("button.toggle.deactivate"));
//            }
//        });
//
//        openConfig.visibleProperty().bind(enableButton.selectedProperty());
//
//        try {
//            if (_lastSample != null && !_lastSample.getValueAsString().isEmpty()) {
//                enableButton.setSelected(true);
////                enableButton.setText(I18n.getInstance().getString("button.toggle.activate"));
//            } else {
//                enableButton.setSelected(false);
////                enableButton.setText(I18n.getInstance().getString("button.toggle.deactivate"));
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }


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
    public void commit() throws JEVisException {
        logger.debug("StringValueEditor.commit(): '{}' {} {}", _attribute.getName(), hasChanged(), _newSample);

        hasChanged();
        if (hasChanged() && _newSample != null) {
            //TODO: check if type is ok, maybe better at input time
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
    public static List<JsonGapFillingConfig> createDefaultConfig() {
        List<JsonGapFillingConfig> list = new ArrayList<>();
        JsonGapFillingConfig newConfig1 = new JsonGapFillingConfig();
        newConfig1.setType(GapFillingType.INTERPOLATION);
        newConfig1.setBoundary("3600000");


        newConfig1.setName(I18n.getInstance().getString("newobject.title1"));
        list.add(newConfig1);

        JsonGapFillingConfig newConfig2 = new JsonGapFillingConfig();
        newConfig2.setType(GapFillingType.AVERAGE);
        newConfig2.setBoundary("2592000000");
        newConfig2.setBindtospecific(GapFillingBoundToSpecific.WEEKDAY);
        newConfig2.setReferenceperiodcount("1");
        newConfig2.setReferenceperiod(GapFillingReferencePeriod.MONTH);

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
    private List<JsonGapFillingConfig> parseJson(String jsonstring) {
        List<JsonGapFillingConfig> list = new ArrayList<>();


        if (jsonstring.endsWith("]")) {
            list = new Gson().fromJson(jsonstring, new TypeToken<List<JsonGapFillingConfig>>() {
            }.getType());
        } else {
            list.add(new Gson().fromJson(jsonstring, JsonGapFillingConfig.class));
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
        dialog.setWidth(350);
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

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);


        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                        try {
                            _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());

                            commit();
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
    private void fillTab(Tab tab, JsonGapFillingConfig config) {
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

        JFXComboBox typeBox = new JFXComboBox(optionsType);
        JFXComboBox referencePeriodBox = new JFXComboBox(optionsReferencePeriods);
        JFXComboBox boundSpecificBox = new JFXComboBox(optionsBoundSpecifics);
        NumberSpinner referencePeriodCountText = new NumberSpinner(new BigDecimal(1), new BigDecimal(1));
        referencePeriodCountText.setMin(new BigDecimal(1));
        referencePeriodCountText.setMax(new BigDecimal(5));
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
        typeBox.getSelectionModel().select(config.getType());
        boundaryText.setText((Long.parseLong(config.getBoundary()) / 1000) + ""); //msec -> sec
        defaultValueText.setText(config.getDefaultvalue());

        typeBox.getSelectionModel().select(
                optionsType.contains(config.getType()) ? config.getType()
                        : GapFillingType.NONE);
        referencePeriodBox.getSelectionModel().select(
                optionsReferencePeriods.contains(config.getReferenceperiod()) ? config.getReferenceperiod()
                        : GapFillingReferencePeriod.NONE);
        BigDecimal parsedValue = new BigDecimal(145);
        try {
            if (config.getReferenceperiodcount() != null)
                parsedValue = new BigDecimal(Long.parseLong(config.getReferenceperiodcount()));
        } catch (Exception e) {
            logger.error("Parsing Exception: ", e);
        }

        referencePeriodCountText.setNumber(parsedValue);

        boundSpecificBox.getSelectionModel().select(
                optionsBoundSpecifics.contains(config.getBindtospecific()) ? config.getBindtospecific()
                        : GapFillingBoundToSpecific.NONE);

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

        typeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setType(newValue.toString());

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

        if (config.getType().equals(GapFillingType.DEFAULT_VALUE)) {
            row++;
            gridPane.add(defaultValueLabel, 0, row);
            gridPane.add(defaultValueText, 1, row);
        } else if (config.getType().equals(GapFillingType.NONE) || config.getType().equals(GapFillingType.STATIC)
                || config.getType().equals(GapFillingType.INTERPOLATION)) {
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

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }


    public enum Response {NO, YES, CANCEL}
}
