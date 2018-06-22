package org.jevis.jeconfig.plugin.object.attribute;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jevis.commons.constants.JEDataProcessorConstants.*;

public class GapFillingEditor implements AttributeEditor {
    private final Logger logger = LogManager.getLogger(GapFillingEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    HBox box = new HBox();
    TabPane _tp = new TabPane();
    Dialog<ButtonType> _dialog = new Dialog<>();

    private String logPrefix = "";
    private boolean _readOnly = true;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;


    private List<JsonGapFillingConfig> _listConfig;
    private Response response = GapFillingEditor.Response.CANCEL;

    public GapFillingEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _changed.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                logger.debug("------------------> StringValueChanged: {}", newValue);
            }
        });
    }

    private void init() {
        Button openConfig = new Button(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.openconfig"));
        _dialog.setResizable(true);
        _dialog.setHeight(300);
        _dialog.setWidth(350);

        openConfig.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    show();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
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
        _readOnly = canRead;
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    private Response show() throws JEVisException {
        String gapFillingConfig;
        if (!_attribute.hasSample()) {
            JsonGapFillingConfig newConfig = new JsonGapFillingConfig();
            newConfig.setName(I18n.getInstance().getString("newobject.new.title"));
            _listConfig = new ArrayList<>();
            _listConfig.add(newConfig);
        } else {
            gapFillingConfig = _attribute.getLatestSample().getValueAsString();
            if (gapFillingConfig.endsWith("]")) {
                _listConfig = new Gson().fromJson(gapFillingConfig, new TypeToken<List<JsonGapFillingConfig>>() {
                }.getType());
            } else {
                _listConfig = new ArrayList<>();
                _listConfig.add(new Gson().fromJson(gapFillingConfig, JsonGapFillingConfig.class));
            }
        }

        _tp.getTabs().clear();
        _tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        _dialog.setTitle(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.dialog.title"));
        _dialog.setHeaderText(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.dialog.header"));
        _dialog.setGraphic(JEConfig.getImage("fill_gap.png", 48, 48));
        _dialog.getDialogPane().getButtonTypes().setAll();

        for (int i = 0; i < _listConfig.size(); i++) {
            _tp.getTabs().add(generateTab(i));
        }

        Tab newTab = new Tab();
        newTab.setText(I18n.getInstance().getString("plugin.classes.contextmenu.new"));
        _tp.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newNewTab) {
                if (newNewTab == newTab) {
                    Platform.runLater(() -> {
                        JsonGapFillingConfig newConfig = new JsonGapFillingConfig();
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

                        GapFillingEditor.this.response = GapFillingEditor.Response.YES;
                        try {
                            _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                            commit();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    } else {
                        GapFillingEditor.this.response = GapFillingEditor.Response.CANCEL;
                    }
                });

        logger.trace("Done");

        return response;
    }

    private Tab generateTab(int i) {
        JFXTextField _field_Name;
        JFXComboBox _field_Type;
        JFXTextField _field_Boundary;
        JFXTextField _field_Default_Value;
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
        //_field_Name.setPrefWidth(width);//TODO: remove this workaround
        _field_Name.setEditable(!_readOnly);
        ObservableList<String> optionsBoundSpecific = FXCollections.observableArrayList(GapFillingBoundToSpecific.NONE, GapFillingBoundToSpecific.WEEKDAY,
                GapFillingBoundToSpecific.WEEKOFYEAR, GapFillingBoundToSpecific.MONTHOFYEAR);
        _field_Bound_Specific = new JFXComboBox(optionsBoundSpecific);
        //_field_Bound_Specific.setPrefWidth(width);//TODO: remove this workaround
        _field_Bound_Specific.setEditable(_readOnly);
        _field_Boundary = new JFXTextField();
        //_field_Boundary.setPrefWidth(width);//TODO: remove this workaround
        _field_Boundary.setEditable(!_readOnly);
        _field_Default_Value = new JFXTextField();
        //_field_Default_Value.setPrefWidth(width);//TODO: remove this workaround
        _field_Default_Value.setEditable(!_readOnly);
        ObservableList<String> optionsReferencePeriod = FXCollections.observableArrayList(GapFillingReferencePeriod.NONE, GapFillingReferencePeriod.DAY,
                GapFillingReferencePeriod.WEEK, GapFillingReferencePeriod.MONTH, GapFillingReferencePeriod.YEAR);
        _field_Reference_Period = new JFXComboBox(optionsReferencePeriod);
        //_field_Reference_Period.setPrefWidth(width);//TODO: remove this workaround
        _field_Reference_Period.setEditable(_readOnly);
        _field_Reference_Period_Count = new JFXTextField();
        _field_Reference_Period_Count.setPrefWidth(width);//TODO: remove this workaround
        _field_Reference_Period_Count.setEditable(!_readOnly);
        ObservableList<String> optionsType = FXCollections.observableArrayList(GapFillingType.NONE, GapFillingType.INTERPOLATION, GapFillingType.AVERAGE,
                GapFillingType.DEFAULT_VALUE, GapFillingType.STATIC, GapFillingType.MINIMUM, GapFillingType.MAXIMUM, GapFillingType.MEDIAN);
        _field_Type = new JFXComboBox(optionsType);
        //_field_Type.setPrefWidth(width);//TODO: remove this workaround
        _field_Type.setEditable(_readOnly);

        Label name = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.name"));
        Label type = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.type"));
        Label boundary = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.boundary"));
        Label default_value = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.defaultvalue"));
        Label reference_period = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.referenceperiod"));
        Label reference_period_count = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.referenceperiodcount"));
        Label boundtospecific = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.boundto"));

        JFXButton deleteConfig = new JFXButton(I18n.getInstance().getString("menu.edit.delete"));

        if (Objects.nonNull(_listConfig.get(i))) {
            _field_Name.setText(_listConfig.get(i).getName());
            _field_Type.setValue(_listConfig.get(i).getType());
            _field_Boundary.setText(_listConfig.get(i).getBoundary());
            _field_Default_Value.setText(_listConfig.get(i).getDefaultvalue());
            _field_Reference_Period.setValue(_listConfig.get(i).getReferenceperiod());
            _field_Reference_Period_Count.setText(_listConfig.get(i).getReferenceperiodcount());
            _field_Bound_Specific.setValue(_listConfig.get(i).getBindtospecific());
        }

        int finalI = i;
        _field_Name.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_listConfig.get(finalI) == null || !_listConfig.get(finalI).getName().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _listConfig.get(finalI).setName(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Bound_Specific.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (_listConfig.get(finalI) == null || !_listConfig.get(finalI).getBindtospecific().equals(newValue)) {

                    String v = String.valueOf(newValue);
                    _listConfig.get(finalI).setBindtospecific(v);

                    _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Boundary.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (!newValue.matches("\\d*")) {
                    _field_Boundary.setText(newValue.replaceAll("[^\\d]", ""));

                    if (_listConfig.get(finalI) == null || !_listConfig.get(finalI).getBoundary().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _listConfig.get(finalI).setBoundary(newValue);

                        _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                        _changed.setValue(true);
                    }
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Default_Value.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_listConfig.get(finalI) == null || !_listConfig.get(finalI).getDefaultvalue().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _listConfig.get(finalI).setDefaultvalue(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Reference_Period.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (_listConfig.get(finalI) == null || !_listConfig.get(finalI).getReferenceperiod().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    String v = String.valueOf(newValue);
                    _listConfig.get(finalI).setReferenceperiod(v);

                    _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Reference_Period_Count.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (!newValue.matches("\\d*")) {
                    _field_Reference_Period_Count.setText(newValue.replaceAll("[^\\d]", ""));

                    if (_listConfig.get(finalI) == null || !_listConfig.get(finalI).getReferenceperiodcount().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _listConfig.get(finalI).setReferenceperiodcount(newValue);

                        _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                        _changed.setValue(true);
                    }
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Type.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (_listConfig.get(finalI).getType() == null || !_listConfig.get(finalI).getType().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    String v = String.valueOf(newValue);
                    _listConfig.get(finalI).setType(v);

                    _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                    _changed.setValue(true);

                    if (newValue.equals(GapFillingType.DEFAULT_VALUE) && !_gp.getChildren().contains(default_value)) {
                        _gp.add(default_value, 0, 3);
                        _gp.add(_field_Default_Value, 1, 3);

                        _gp.getChildren().removeAll(reference_period, reference_period_count, _field_Reference_Period, _field_Reference_Period_Count,
                                boundtospecific, _field_Bound_Specific);
                    }

                    if (!_listConfig.get(finalI).getType().equals(GapFillingType.DEFAULT_VALUE) && !_gp.getChildren().contains(reference_period)) {
                        _gp.add(reference_period_count, 0, 4);
                        _gp.add(_field_Reference_Period_Count, 1, 4);
                        _gp.add(_field_Reference_Period, 2, 4);
                        _gp.add(reference_period, 3, 4);

                        _gp.add(boundtospecific, 0, 6);
                        _gp.add(_field_Bound_Specific, 1, 6);

                        _gp.getChildren().removeAll(default_value, _field_Default_Value);
                    }

                    if ((newValue.equals(GapFillingType.NONE) || newValue.equals(GapFillingType.STATIC))
                            && (!_gp.getChildren().contains(default_value) || !_gp.getChildren().contains(reference_period))) {
                        _gp.getChildren().removeAll(reference_period, reference_period_count, _field_Reference_Period, _field_Reference_Period_Count,
                                boundtospecific, _field_Bound_Specific, default_value, _field_Default_Value);
                    }

                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        deleteConfig.setOnAction(event -> {
            try {
                logger.info("Detele Config: {}");
                _listConfig.remove(finalI);
                _tp.getTabs().remove(finalI);

                _newSample = _attribute.buildSample(new DateTime(), _listConfig.toString());
                _changed.setValue(true);

            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _gp.add(name, 0, 0);
        _gp.add(_field_Name, 1, 0);
        _gp.add(type, 0, 1);
        _gp.add(_field_Type, 1, 1);
        _gp.add(boundary, 0, 2);
        _gp.add(_field_Boundary, 1, 2);

        Tab tab = new Tab();

        if (Objects.nonNull(_listConfig.get(finalI).getType())) {
            if (_listConfig.get(finalI).getType().equals(GapFillingType.DEFAULT_VALUE) && !_listConfig.get(finalI).getType().equals(GapFillingType.STATIC)
                    && !_listConfig.get(finalI).getType().equals(GapFillingType.NONE)) {
                _gp.add(default_value, 0, 3);
                _gp.add(_field_Default_Value, 1, 3);
            }

            if (!_listConfig.get(finalI).getType().equals(GapFillingType.DEFAULT_VALUE) && !_listConfig.get(finalI).getType().equals(GapFillingType.STATIC)
                    && !_listConfig.get(finalI).getType().equals(GapFillingType.NONE)) {
                _gp.add(reference_period_count, 0, 4);
                _gp.add(_field_Reference_Period_Count, 1, 4);
                _gp.add(_field_Reference_Period, 2, 4);
                _gp.add(reference_period, 3, 4);

                _gp.add(boundtospecific, 0, 6);
                _gp.add(_field_Bound_Specific, 1, 6);
            }
        }

        _gp.add(deleteConfig, 3, 8);

        tab.setContent(_gp);
        tab.setText(_listConfig.get(i).getName());

        return tab;
    }

    public enum Response {

        NO, YES, CANCEL
    }
}
