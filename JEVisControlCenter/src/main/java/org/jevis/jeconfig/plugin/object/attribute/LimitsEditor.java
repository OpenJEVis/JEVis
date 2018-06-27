package org.jevis.jeconfig.plugin.object.attribute;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.JEDataProcessorConstants.*;

public class LimitsEditor implements AttributeEditor {
    private final Logger logger = LogManager.getLogger(LimitsEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    HBox box = new HBox();
    TabPane _tp = new TabPane();
    private Dialog<ButtonType> _dialog = new Dialog<>();

    private String logPrefix = "";
    private boolean _readOnly = true;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;


    private List<JsonLimitsConfig> _listConfig;
    private Response response = LimitsEditor.Response.CANCEL;

    public LimitsEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _changed.addListener((observable, oldValue, newValue) -> logger.debug("------------------> StringValueChanged: {}", newValue));
    }

    private void init() {
        Button openConfig = new Button(I18n.getInstance().getString("plugin.object.attribute.limitseditor.openconfig"));
        _dialog.setResizable(true);
        _dialog.setHeight(300);
        _dialog.setWidth(350);

        openConfig.setOnAction(event -> {
            try {
                show();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });

        box.getChildren().add(openConfig);

//        For Bugfixing
//        JFXTextField _field;
//        _field = new JFXTextField();
//        _field.setPrefWidth(500);//TODO: remove this workaround
//        _field.setEditable(!_readOnly);
//
//        JEVisSample lsample = _attribute.getLatestSample();
//
//        if (lsample != null) {
//            _field.setText(lsample.getValueAsString());
//            _lastSample = lsample;
//            logger.trace("Value: {}", _lastSample.toString());
//        } else {
//            logger.trace("empty value");
//            _field.setText("");
//        }
//
//        _field.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
//
//            try {
//                if (_lastSample == null || !_lastSample.getValueAsString().equals(newValue)) {
//                    logger.info("Value Changed: {}", newValue);
//                    _newSample = _attribute.buildSample(new DateTime(), newValue);
//                    _changed.setValue(true);
//                }
//            } catch (JEVisException ex) {
//                logger.catching(ex);
//            }
//
//        });
//
//        _field.setPrefWidth(500);
//        _field.setId("attributelabel");
//
//        box.getChildren().add(_field);
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.debug("StringValueEditor.commit(): '{}' {} {}", _attribute.getName(), hasChanged(), _newSample);
        if (hasChanged() && _newSample != null) {
            //TODO: check if tpye is ok, maybe better at input time
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
        _readOnly = canRead;
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    private Response show() throws JEVisException {
        String limitsConfig;
        if (!_attribute.hasSample()) {
            JsonLimitsConfig newConfig = new JsonLimitsConfig();
            newConfig.setName(I18n.getInstance().getString("newobject.new.title"));
            _listConfig = new ArrayList<>();
            _listConfig.add(newConfig);
        } else {
            limitsConfig = _attribute.getLatestSample().getValueAsString();
            if (limitsConfig.endsWith("]")) {
                _listConfig = new Gson().fromJson(limitsConfig, new TypeToken<List<JsonLimitsConfig>>() {
                }.getType());
            } else {
                _listConfig = new ArrayList<>();
                _listConfig.add(new Gson().fromJson(limitsConfig, JsonLimitsConfig.class));
            }
        }

        _tp.getTabs().clear();
        _tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        _dialog.setTitle(I18n.getInstance().getString("plugin.object.attribute.limitseditor.dialog.title"));
        _dialog.setHeaderText(I18n.getInstance().getString("plugin.object.attribute.limitseditor.dialog.header"));
        _dialog.setGraphic(JEConfig.getImage("fill_gap.png", 48, 48));
        _dialog.getDialogPane().getButtonTypes().setAll();

        for (int i = 0; i < _listConfig.size(); i++) {
            _tp.getTabs().add(generateTab(i));
        }

        Tab newTab = new Tab();
        newTab.setText(I18n.getInstance().getString("plugin.classes.contextmenu.new"));
        _tp.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newNewTab) -> {
            if (newNewTab == newTab) {
                Platform.runLater(() -> {
                    JsonLimitsConfig newConfig = new JsonLimitsConfig();
                    newConfig.setName(I18n.getInstance().getString("newobject.new.title"));
                    _listConfig.add(newConfig);

                    try {
                        _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                        _changed.setValue(true);
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }

                    _tp.getTabs().remove(newTab);
                    _tp.getTabs().add(generateTab(_listConfig.size() - 1));
                    if (!_tp.getTabs().contains(newTab)) {
                        _tp.getTabs().add(newTab);
                    }
                });
            }
        });
        if (!_tp.getTabs().contains(newTab)) {
            _tp.getTabs().add(newTab);
        }


        _dialog.getDialogPane().contentProperty().setValue(_tp);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        _dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);


        _dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {

                        LimitsEditor.this.response = LimitsEditor.Response.YES;
                        try {
                            _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                            commit();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LimitsEditor.this.response = LimitsEditor.Response.CANCEL;
                    }
                });

        logger.trace("Done");

        return response;
    }

    private Tab generateTab(int i) {
        JFXTextField _field_Name;
        JFXTextField _field_Min;
        JFXTextField _field_Max;
        JFXComboBox _field_Type_Of_Substitute_Value;
        JFXTextField _field_Duration_Over_Underrun;
        JFXTextField _field_Default_Min_Value;
        JFXTextField _field_Default_Max_Value;
        JFXComboBox _field_Reference_Period;
        JFXComboBox _field_Bound_Specific;
        JFXTextField _field_Reference_Period_Count;
        GridPane _gp;
        _gp = new GridPane();

        VBox.setVgrow(_gp, Priority.ALWAYS);
        HBox.setHgrow(_gp, Priority.ALWAYS);

        _gp.setPadding(new Insets(10));
        _gp.setHgap(10);
        _gp.setVgap(5);


        int width = 150;
        _field_Name = new JFXTextField();
        _field_Name.setEditable(!_readOnly);

        _field_Min = new JFXTextField();
        _field_Min.setEditable(!_readOnly);

        _field_Max = new JFXTextField();
        _field_Max.setEditable(!_readOnly);

        ObservableList<String> optionsBoundSpecific = FXCollections.observableArrayList(GapFillingBoundToSpecific.NONE, GapFillingBoundToSpecific.WEEKDAY,
                GapFillingBoundToSpecific.WEEKOFYEAR, GapFillingBoundToSpecific.MONTHOFYEAR);
        _field_Bound_Specific = new JFXComboBox(optionsBoundSpecific);
        _field_Bound_Specific.setEditable(_readOnly);

        _field_Duration_Over_Underrun = new JFXTextField();
        _field_Duration_Over_Underrun.setEditable(!_readOnly);

        _field_Default_Min_Value = new JFXTextField();
        _field_Default_Min_Value.setEditable(!_readOnly);

        _field_Default_Max_Value = new JFXTextField();
        _field_Default_Max_Value.setEditable(!_readOnly);

        ObservableList<String> optionsReferencePeriod = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
                GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR);
        _field_Reference_Period = new JFXComboBox(optionsReferencePeriod);
        _field_Reference_Period.setEditable(_readOnly);

        _field_Reference_Period_Count = new JFXTextField();
        _field_Reference_Period_Count.setEditable(!_readOnly);

        ObservableList<String> optionsType = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
                GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MEDIAN);
        _field_Type_Of_Substitute_Value = new JFXComboBox(optionsType);
        _field_Type_Of_Substitute_Value.setEditable(_readOnly);

        Label name = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.name"));
        Label min = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.min"));
        Label max = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.max"));
        Label typeOfSubstituteValue = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.typeOfSubstituteValue"));
        Label durationOverUnderRun = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.durationOverUnderRun"));
        Label defaultMinValue = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.defaultminvalue"));
        Label defaultMaxValue = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.defaultmaxvalue"));
        Label reference_period = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.referenceperiod"));
        Label reference_period_count = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.referenceperiodcount"));
        Label boundtospecific = new Label(I18n.getInstance().getString("plugin.object.attribute.limitseditor.label.boundto"));

        JFXButton deleteConfig = new JFXButton(I18n.getInstance().getString("menu.edit.delete"));

        if (Objects.nonNull(_listConfig.get(i))) {
            _field_Name.setText(_listConfig.get(i).getName());
            _field_Min.setText(_listConfig.get(i).getMin());
            _field_Max.setText(_listConfig.get(i).getMax());
            _field_Type_Of_Substitute_Value.setValue(_listConfig.get(i).getTypeOfSubstituteValue());
            _field_Duration_Over_Underrun.setText(_listConfig.get(i).getDurationOverUnderRun());
            _field_Default_Min_Value.setText(_listConfig.get(i).getDefaultMinValue());
            _field_Default_Max_Value.setText(_listConfig.get(i).getDefaultMaxValue());
            _field_Reference_Period.setValue(_listConfig.get(i).getReferenceperiod());
            _field_Reference_Period_Count.setText(_listConfig.get(i).getReferenceperiodcount());
            _field_Bound_Specific.setValue(_listConfig.get(i).getBindtospecific());
        }

        int finalI = i;
        _field_Name.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (_listConfig.get(finalI).getName() == null || !_listConfig.get(finalI).getName().equals(newValue)) {
                logger.info("Value Changed: {}", newValue);
                _listConfig.get(finalI).setName(newValue);

                _changed.setValue(true);
            }
        });

        _field_Min.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (_listConfig.get(finalI).getMin() == null || !_listConfig.get(finalI).getMin().equals(newValue)) {
                logger.info("Value Changed: {}", newValue);
                _listConfig.get(finalI).setMin(newValue);

                _changed.setValue(true);
            }
        });

        _field_Max.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (_listConfig.get(finalI).getMax() == null || !_listConfig.get(finalI).getMax().equals(newValue)) {
                logger.info("Value Changed: {}", newValue);
                _listConfig.get(finalI).setMax(newValue);

                _changed.setValue(true);
            }
        });

        _field_Bound_Specific.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (_listConfig.get(finalI).getBindtospecific() == null || !_listConfig.get(finalI).getBindtospecific().equals(newValue)) {

                String v = String.valueOf(newValue);
                _listConfig.get(finalI).setBindtospecific(v);

                _changed.setValue(true);
            }
        });

        _field_Duration_Over_Underrun.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (_listConfig.get(finalI).getDurationOverUnderRun() == null || !_listConfig.get(finalI).getDurationOverUnderRun().equals(newValue)) {
                logger.info("Value Changed: {}", newValue);
                _listConfig.get(finalI).setDurationOverUnderRun(newValue);

                _changed.setValue(true);
            }
        });

        _field_Default_Min_Value.textProperty().

                addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->

                {
                    if (_listConfig.get(finalI).getDefaultMinValue() == null || !_listConfig.get(finalI).getDefaultMinValue().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _listConfig.get(finalI).setDefaultMinValue(newValue);

                        _changed.setValue(true);
                    }
                });

        _field_Default_Max_Value.textProperty().

                addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->

                {
                    if (_listConfig.get(finalI).getDefaultMaxValue() == null || !_listConfig.get(finalI).getDefaultMaxValue().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _listConfig.get(finalI).setDefaultMaxValue(newValue);

                        _changed.setValue(true);
                    }
                });

        _field_Reference_Period.valueProperty().addListener((observable, oldValue, newValue) ->

        {
            if (_listConfig.get(finalI).getReferenceperiod() == null || !_listConfig.get(finalI).getReferenceperiod().equals(newValue)) {
                logger.info("Value Changed: {}", newValue);
                String v = String.valueOf(newValue);
                _listConfig.get(finalI).setReferenceperiod(v);

                _changed.setValue(true);
            }
        });

        _field_Reference_Period_Count.textProperty().

                addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->

                {
                    if (_listConfig.get(finalI).getReferenceperiodcount() == null || !_listConfig.get(finalI).getReferenceperiodcount().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _listConfig.get(finalI).setReferenceperiodcount(newValue);

                        _changed.setValue(true);
                    }
                });

        _field_Type_Of_Substitute_Value.valueProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (_listConfig.get(finalI).getTypeOfSubstituteValue() == null || !_listConfig.get(finalI).getTypeOfSubstituteValue().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        String v = String.valueOf(newValue);
                        _listConfig.get(finalI).setTypeOfSubstituteValue(v);

                        _changed.setValue(true);

                        int row = 0;
                        if (newValue.equals(GapFillingType.DEFAULT_VALUE) && !_gp.getChildren().contains(defaultMinValue)) {
                            _gp.getChildren().removeAll(reference_period, reference_period_count, _field_Reference_Period, _field_Reference_Period_Count,
                                    boundtospecific, _field_Bound_Specific, deleteConfig);
                            row = _gp.impl_getRowCount();

                            row++;
                            _gp.add(defaultMinValue, 0, row);
                            _gp.add(_field_Default_Min_Value, 1, row);

                            row++;
                            _gp.add(defaultMaxValue, 0, row);
                            _gp.add(_field_Default_Max_Value, 1, row);

                            row++;
                            _gp.add(deleteConfig, 3, row);
                        }

                        if (!_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.DEFAULT_VALUE) && !_gp.getChildren().contains(reference_period)) {
                            _gp.getChildren().removeAll(defaultMinValue, defaultMaxValue, _field_Default_Min_Value, _field_Default_Max_Value, deleteConfig);
                            row = _gp.impl_getRowCount();

                            row++;
                            _gp.add(reference_period_count, 0, row);
                            _gp.add(_field_Reference_Period_Count, 1, row);
                            _gp.add(_field_Reference_Period, 2, row);
                            _gp.add(reference_period, 3, row);

                            row++;
                            _gp.add(boundtospecific, 0, row);
                            _gp.add(_field_Bound_Specific, 1, row);

                            row++;
                            _gp.add(deleteConfig, 3, row);
                        }

                        if ((newValue.equals(GapFillingType.NONE) || newValue.equals(GapFillingType.STATIC))
                                && (!_gp.getChildren().contains(defaultMinValue) || !_gp.getChildren().contains(reference_period))) {
                            _gp.getChildren().removeAll(reference_period, reference_period_count, _field_Reference_Period, _field_Reference_Period_Count,
                                    boundtospecific, _field_Bound_Specific, defaultMinValue, defaultMaxValue, _field_Default_Min_Value, _field_Default_Max_Value);
                        }

                    }
                });

        deleteConfig.setOnAction(event ->

        {
            logger.info("Detele Config: {}");
            _listConfig.remove(finalI);
            _tp.getTabs().remove(finalI);

            _changed.setValue(true);

        });

        int row = 0;
        _gp.add(name, 0, row);
        _gp.add(_field_Name, 1, row);

        row++;
        _gp.add(min, 0, row);
        _gp.add(_field_Min, 1, row);

        row++;
        _gp.add(max, 0, row);
        _gp.add(_field_Max, 1, row);

        row++;
        _gp.add(typeOfSubstituteValue, 0, row);
        _gp.add(_field_Type_Of_Substitute_Value, 1, row);

        row++;
        _gp.add(durationOverUnderRun, 0, row);
        _gp.add(_field_Duration_Over_Underrun, 1, row);

        Tab tab = new Tab();

        if (Objects.nonNull(_listConfig.get(finalI).

                getTypeOfSubstituteValue()))

        {
            if (_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.DEFAULT_VALUE) && !_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.STATIC)
                    && !_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.NONE)) {
                row++;
                _gp.add(defaultMinValue, 0, row);
                _gp.add(_field_Default_Min_Value, 1, row);
                row++;
                _gp.add(defaultMaxValue, 0, row);
                _gp.add(_field_Default_Max_Value, 1, row);
            }

            if (!_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.DEFAULT_VALUE) && !_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.STATIC)
                    && !_listConfig.get(finalI).getTypeOfSubstituteValue().equals(GapFillingType.NONE)) {
                row++;
                _gp.add(reference_period_count, 0, row);
                _gp.add(_field_Reference_Period_Count, 1, row);
                _gp.add(_field_Reference_Period, 2, row);
                _gp.add(reference_period, 3, row);

                row++;
                _gp.add(boundtospecific, 0, row);
                _gp.add(_field_Bound_Specific, 1, row);
            }
        }

        row++;
        _gp.add(deleteConfig, 3, row);

        tab.setContent(_gp);
        tab.setText(_listConfig.get(i).

                getName());

        return tab;
    }

    public enum Response {

        NO, YES, CANCEL
    }
}
