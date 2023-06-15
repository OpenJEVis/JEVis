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
package org.jevis.jecc.plugin.object.attribute;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonTools;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.control.BoundSpecificBox;
import org.jevis.jecc.application.control.GapFillingTypeBox;
import org.jevis.jecc.application.control.ReferencePeriodsBox;
import org.jevis.jecc.tool.NumberSpinner;
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class GapFillingEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(GapFillingEditor.class);
    private static final DoubleValidator dv = DoubleValidator.getInstance();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);
    private final HBox box = new HBox(12);
    public JEVisAttribute _attribute;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private List<JsonGapFillingConfig> _listConfig;
    private boolean initialized = false;

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

        GapFillingTypeBox typeBox = new GapFillingTypeBox();
        typeBox.setMaxWidth(800);

        ReferencePeriodsBox referencePeriodBox = new ReferencePeriodsBox();
        referencePeriodBox.setMaxWidth(800);

        BoundSpecificBox boundSpecificBox = new BoundSpecificBox();
        boundSpecificBox.setMaxWidth(800);

        NumberSpinner referencePeriodCountText = new NumberSpinner(new BigDecimal(1), new BigDecimal(1));
        referencePeriodCountText.setMin(new BigDecimal(1));
        referencePeriodCountText.setMax(new BigDecimal(99));
        MFXTextField boundaryText = new MFXTextField();
        boundaryText.setFloatMode(FloatMode.DISABLED);
        MFXTextField defaultValueText = new MFXTextField();
        defaultValueText.setFloatMode(FloatMode.DISABLED);

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
        typeBox.selectItem(GapFillingType.parse(config.getType()));
        boundaryText.setText((Long.parseLong(config.getBoundary()) / 1000) + ""); //msec -> sec
        try {
            NumberFormat numberFormat = NumberFormat.getInstance(I18n.getInstance().getLocale());
            if (config.getDefaultvalue() != null) {
                Double defaultValue = Double.parseDouble(config.getDefaultvalue());
                defaultValueText.setText(numberFormat.format(defaultValue));
            }
        } catch (Exception e) {
            logger.error("Could not parse default value", e);
        }


        typeBox.selectItem(GapFillingType.parse(config.getType()));
        referencePeriodBox.selectItem(GapFillingReferencePeriod.parse(config.getReferenceperiod()));
        boundSpecificBox.selectItem(GapFillingBoundToSpecific.parse(config.getBindtospecific()));

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
            try {
                String parsedDefaultValue = dv.validate(newValue, I18n.getInstance().getLocale()).toString();
                config.setDefaultvalue(parsedDefaultValue);
            } catch (Exception e) {
                defaultValueText.setText(oldValue);
            }
        });
        boundaryText.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setBoundary((Long.parseLong(newValue) * 1000L) + "");//sec -> msec
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


        ColumnConstraints column1 = new ColumnConstraints(100, 150, 800, Priority.SOMETIMES, HPos.LEFT, false);
        ColumnConstraints column2 = new ColumnConstraints(200, 300, 800, Priority.ALWAYS, HPos.LEFT, true);
        gridPane.getColumnConstraints().addAll(column1, column2);


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

    /**
     * Build main UI
     */
    private void init() {
        MFXButton openConfig = new MFXButton(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.openconfig"));
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

    /**
     * Parse an JsonString with an JsonGapFillingConfig configuration.
     *
     * @param jsonstring
     * @return
     */
    public List<JsonGapFillingConfig> parseJson(String jsonstring) throws IOException {
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

        Dialog dialog = new Dialog();
        dialog.setTitle(I18n.getInstance().getString("plugin.configuration.gapfilling.title"));
        dialog.setHeaderText(I18n.getInstance().getString("plugin.configuration.gapfilling.header"));
        dialog.setResizable(true);
        dialog.initOwner(ControlCenter.getStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);
        dialog.setHeight(450);
        dialog.setWidth(620);

        for (JsonGapFillingConfig config : _listConfig) {
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
                logger.error("Could not write gap config to JEVis System: ", e);
            }
            dialog.close();
        });

        cancelButton.setOnAction(event -> dialog.close());

        dialog.show();
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }


    public enum Response {NO, YES, CANCEL}
}
