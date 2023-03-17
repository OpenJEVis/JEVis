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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.AlarmConstants;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonAlarmConfig;
import org.jevis.commons.json.JsonScheduler;
import org.jevis.commons.json.JsonTools;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class AlarmEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(AlarmEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);

    private final ObservableList<AlarmConstants.Operator> operator = FXCollections.observableArrayList(AlarmConstants.Operator.SMALLER, AlarmConstants.Operator.BIGGER, AlarmConstants.Operator.EQUALS,
            AlarmConstants.Operator.SMALLER_EQUALS, AlarmConstants.Operator.BIGGER_EQUALS, AlarmConstants.Operator.NOT_EQUALS);
    public JEVisAttribute _attribute;
    private final HBox box = new HBox(12);
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private List<JsonAlarmConfig> _listConfig;
    private final boolean delete = false;
    private boolean initialized = false;

    public AlarmEditor(JEVisAttribute att) {

        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();

    }

    /**
     * Build main UI
     */
    private void init() {
        JFXButton openConfig = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.openconfig"));
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
//        _changed.setValue(true);

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
    public void commit() throws JEVisException {
        logger.debug("Alarm Editor.commit(): '{}' {} {}", _attribute.getName(), hasChanged(), _newSample);

        if (hasChanged() && delete) {
            _attribute.deleteAllSample();
        } else if (hasChanged() && _newSample != null) {
            _newSample.commit();
            _lastSample = _newSample;
            _newSample = null;
            _changed.setValue(false);
        }
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
     * Creates an default configuration with one Alarm configuration
     *
     * @return
     */
    private List<JsonAlarmConfig> createDefaultConfig() {
        List<JsonAlarmConfig> list = new ArrayList<>();

        JsonAlarmConfig newConfig1 = new JsonAlarmConfig();

        newConfig1.setName(I18n.getInstance().getString("newobject.alarmconfig1"));
        newConfig1.setId("0");
        newConfig1.setOperator(AlarmConstants.Operator.SMALLER.toString());

        JsonAlarmConfig newConfig2 = new JsonAlarmConfig();
        newConfig2.setId("1");

        newConfig2.setName(I18n.getInstance().getString("newobject.alarmconfig2"));
        newConfig2.setOperator(AlarmConstants.Operator.SMALLER.toString());

        list.add(newConfig1);
        list.add(newConfig2);

        return list;
    }

    /**
     * Parse an JsonString with an JsonAlarmConfig configuration.
     *
     * @param jsonstring
     * @return
     */
    private List<JsonAlarmConfig> parseJson(String jsonstring) throws IOException {
        List<JsonAlarmConfig> list = new ArrayList<>();


        if (jsonstring.startsWith("[")) {
            list = Arrays.asList(JsonTools.objectMapper().readValue(jsonstring, JsonAlarmConfig[].class));
        } else {
            list.add(JsonTools.objectMapper().readValue(jsonstring, JsonAlarmConfig.class));
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
        tabPane.setMinWidth(800);
        tabPane.setPrefWidth(480);
        tabPane.setPrefHeight(350);

        Dialog dialog = new Dialog();
        dialog.setTitle(I18n.getInstance().getString("plugin.configuration.alarmeditor.title"));
        dialog.setHeaderText(I18n.getInstance().getString("plugin.configuration.alarmeditor.header"));
        dialog.setResizable(true);
        dialog.initOwner(JEConfig.getStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        for (JsonAlarmConfig config : _listConfig) {
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
                logger.debug("Commit: {}", _newSample.getValueAsString());
                commit();
            } catch (JEVisException e) {
                logger.error("Could not commit.");
            }
            dialog.close();
        });

        cancelButton.setOnAction(event -> dialog.close());

        dialog.show();
    }

    /**
     * Fills the given tab content with an gui for the given alarm configuration
     *
     * @param tab
     * @param config
     */
    private void fillTab(Tab tab, JsonAlarmConfig config) {
        Integer id = null;
        if (config.getId() != null) {
            id = Integer.parseInt(config.getId());
        }

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setMinHeight(220);//we have hidden element and need space
        gridPane.setMinWidth(435);


        Label limitDataLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.label.limitData"));
        Label limitLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.label.limit"));
        Label operatorLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.label.operator"));
        Label silentTimeLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.label.silentTime"));
        Label standbyLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.label.standbyTime"));
        Label toleranceLabel = new Label(I18n.getInstance().getString("plugin.object.attribute.alarmeditor.label.tolerance"));

        JFXTextField nameField = new JFXTextField();

        HBox limitDataBox = new HBox();
        JFXButton treeButton = new JFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        JFXButton gotoButton = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.target.goto"),
                JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", 18, 18));//icon
        gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        limitDataBox.setSpacing(10);
        limitDataBox.getChildren().setAll(treeButton, gotoButton, rightSpacer);

        if (config.getLimitData() != null) {
            try {

                TargetHelper th = new TargetHelper(_attribute.getDataSource(), config.getLimitData());

                if (th.isValid() && th.targetObjectAccessible()) {

                    StringBuilder bText = new StringBuilder();

                    for (JEVisObject obj : th.getObject()) {
                        int index = th.getObject().indexOf(obj);
                        if (index > 0) bText.append("; ");

                        bText.append("[");
                        bText.append(obj.getID());
                        bText.append("] ");
                        bText.append(obj.getName());

                        if (th.isAttribute()) {

                            bText.append(" - ");
                            bText.append(th.getAttribute().get(index).getName());

                        }
                    }

                    treeButton.setText(bText.toString());
                }

            } catch (Exception ex) {
                logger.catching(ex);
            }
        }

        treeButton.setOnAction(event -> {
            TargetHelper th = null;
            if (config.getLimitData() != null) {
                try {
                    th = new TargetHelper(_attribute.getDataSource(), config.getLimitData());
                } catch (JEVisException e) {
                    logger.error("Could not get target.");
                }
            }

            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataAndCleanDataFilter();
            JEVisTreeFilter allAttributesFilter = SelectTargetDialog.buildAllAttributesFilter();
            allFilter.add(allDataFilter);
            allFilter.add(allAttributesFilter);

            List<UserSelection> openList = new ArrayList<>();
            if (th != null && !th.getAttribute().isEmpty()) {
                for (JEVisAttribute att : th.getAttribute())
                    openList.add(new UserSelection(UserSelection.SelectionType.Attribute, att, null, null));
            } else if (th != null && !th.getObject().isEmpty()) {
                for (JEVisObject obj : th.getObject())
                    openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
            }

            try {
                SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.SINGLE, _attribute.getDataSource(), openList);

                selectTargetDialog.setOnCloseRequest(event1 -> {
                    if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                        logger.trace("Selection Done");

                        StringBuilder newTarget = new StringBuilder();
                        List<UserSelection> selections = selectTargetDialog.getUserSelection();
                        for (UserSelection us : selections) {
                            int index = selections.indexOf(us);
                            if (index > 0) newTarget.append(";");

                            newTarget.append(us.getSelectedObject().getID());
                            if (us.getSelectedAttribute() != null) {
                                newTarget.append(":");
                                newTarget.append(us.getSelectedAttribute().getName());
                            } else {
                                newTarget.append(":Value");
                            }
                        }

                        treeButton.setText(newTarget.toString());
                        config.setLimitData(newTarget.toString());
                    }

                });
                selectTargetDialog.show();
            } catch (Exception e) {
                logger.error(e);
            }
        });

        JFXTextField limitField = new JFXTextField();

        JFXComboBox<AlarmConstants.Operator> operator = new JFXComboBox<>(this.operator);
        Callback<ListView<AlarmConstants.Operator>, ListCell<AlarmConstants.Operator>> cellFactory = new Callback<ListView<AlarmConstants.Operator>, ListCell<AlarmConstants.Operator>>() {
            @Override
            public ListCell<AlarmConstants.Operator> call(ListView<AlarmConstants.Operator> param) {
                return new ListCell<AlarmConstants.Operator>() {
                    @Override
                    protected void updateItem(AlarmConstants.Operator operator1, boolean empty) {
                        super.updateItem(operator1, empty);
                        if (empty || operator1 == null) {
                            setText("");
                        } else {
                            setText(AlarmConstants.Operator.getValue(operator1));
                        }
                    }
                };
            }
        };

        operator.setCellFactory(cellFactory);
        operator.setButtonCell(cellFactory.call(null));

        JFXTextField toleranceField = new JFXTextField();

        ScheduleEditor silentTime;

        JsonScheduler silentTimeValue;
        if (config.getSilentTime() != null) {
            silentTimeValue = config.getSilentTime();
            silentTime = new ScheduleEditor(silentTimeValue);
        } else {
            silentTime = new ScheduleEditor();
        }

        silentTime.getValueChangedProperty().addListener((observable, oldValue, newValue) -> {
            config.setSilentTime(silentTime.getInputValue());
        });

        ScheduleEditor standbyTime;

        JsonScheduler standbyTimeValue;
        if (config.getStandbyTime() != null) {
            standbyTimeValue = config.getStandbyTime();
            standbyTime = new ScheduleEditor(standbyTimeValue);
        } else {
            standbyTime = new ScheduleEditor();
        }

        standbyTime.getValueChangedProperty().addListener((observable, oldValue, newValue) -> {
            config.setStandbyTime(standbyTime.getInputValue());
        });

        double prefFieldWidth = 150;


        /**
         * Text layout
         */


        /**
         * Fill configuration values into gui elements
         */
        if (config.getLimit() != null) {
            limitField.setText(config.getLimit());
        }

        if (config.getTolerance() != null) {
            toleranceField.setText(config.getTolerance());
        }

        if (config.getOperator() != null) {
            operator.getSelectionModel().select(AlarmConstants.Operator.parse(config.getOperator()));
        } else operator.getSelectionModel().selectFirst();


        /**
         * Change Listeners
         */
        limitField.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setLimit(newValue);
        });
        toleranceField.textProperty().addListener((observable, oldValue, newValue) -> {
            config.setTolerance(newValue);
        });
        operator.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setOperator(newValue.toString());
        });


        /**
         * Create layout
         */
        int row = 0;
        if (id == 1) {
            gridPane.add(limitDataLabel, 0, row);
            gridPane.add(limitDataBox, 1, row);
        } else if (id == 0) {
            gridPane.add(limitLabel, 0, row);
            gridPane.add(limitField, 1, row);
        }
        row++;
        gridPane.add(operatorLabel, 0, row);
        gridPane.add(operator, 1, row);
        row++;

        Separator separator1 = new Separator();
        separator1.setPadding(new Insets(4, 4, 4, 4));
        separator1.setOrientation(Orientation.HORIZONTAL);
        gridPane.add(separator1, 0, row, 2, 1);
        row++;

        gridPane.add(silentTimeLabel, 0, row);
        gridPane.add(silentTime.getEditor(), 1, row);
        row++;

        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(4, 4, 4, 4));
        separator2.setOrientation(Orientation.HORIZONTAL);
        gridPane.add(separator2, 0, row, 2, 1);
        row++;

        gridPane.add(standbyLabel, 0, row);
        gridPane.add(standbyTime.getEditor(), 1, row);
        row++;

        Separator separator3 = new Separator();
        separator3.setPadding(new Insets(4, 4, 4, 4));
        separator3.setOrientation(Orientation.HORIZONTAL);
        gridPane.add(separator3, 0, row, 2, 1);
        row++;

        gridPane.add(toleranceLabel, 0, row);
        gridPane.add(toleranceField, 1, row);

        tab.setContent(new ScrollPane(gridPane));
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }


    public enum Response {NO, YES, CANCEL}
}
