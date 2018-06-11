package org.jevis.jeconfig.plugin.object.attribute;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class GapFillingEditor implements AttributeEditor {
    private final Logger logger = LogManager.getLogger(GapFillingEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    HBox box = new HBox();
    private String logPrefix = "";
    private boolean _readOnly = true;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private TextField _field;
    Dialog<ButtonType> _dialog;
    private TextField _field_Name;
    private TextField _field_Type;
    private TextField _field_Boundary;
    private TextField _field_Default_Value;
    private TextField _field_Reference_Period;
    private TextField _field_Bound_Specific;
    private TextField _field_Reference_Period_Count;
    private GridPane _gp;
    private List<JsonGapFillingConfig> _listConfig;
    private JsonGapFillingConfig _firstConfig;
    private Response response = GapFillingEditor.Response.CANCEL;

    private void init() throws JEVisException {
        Button openConfig = new Button(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.openconfig"));

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

        if (_field == null) {
            _field = new javafx.scene.control.TextField();
            _field.setPrefWidth(500);//TODO: remove this workaround
            _field.setEditable(!_readOnly);

            JEVisSample lsample = _attribute.getLatestSample();

            if (lsample != null) {
                _field.setText(lsample.getValueAsString());
                _lastSample = lsample;
                logger.trace("Value: {}", _lastSample.toString());
            } else {
                logger.trace("empty value");
                _field.setText("");
            }

            _field.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

                try {
                    if (_lastSample == null || !_lastSample.getValueAsString().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _newSample = _attribute.buildSample(new DateTime(), newValue);
                        _changed.setValue(true);
                    }
                } catch (JEVisException ex) {
                    logger.catching(ex);
                }

            });

            _field.setPrefWidth(500);
            _field.setId("attributelabel");
        }
        box.getChildren().add(_field);
    }

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
        _dialog = new Dialog();
        _dialog.setTitle(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.dialog.title"));
        _dialog.setHeaderText(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.dialog.header"));
        _dialog.getDialogPane().getButtonTypes().setAll();

        VBox root = new VBox();

        _gp = new GridPane();
        _gp.setPadding(new Insets(10));
        _gp.setHgap(10);
        _gp.setVgap(5);

        int x = 0;
        if (_field_Name == null) {
            int width = 300;
            _field_Name = new javafx.scene.control.TextField();
            _field_Name.setPrefWidth(width);//TODO: remove this workaround
            _field_Name.setEditable(!_readOnly);
            _field_Bound_Specific = new javafx.scene.control.TextField();
            _field_Bound_Specific.setPrefWidth(width);//TODO: remove this workaround
            _field_Bound_Specific.setEditable(!_readOnly);
            _field_Boundary = new javafx.scene.control.TextField();
            _field_Boundary.setPrefWidth(width);//TODO: remove this workaround
            _field_Boundary.setEditable(!_readOnly);
            _field_Default_Value = new javafx.scene.control.TextField();
            _field_Default_Value.setPrefWidth(width);//TODO: remove this workaround
            _field_Default_Value.setEditable(!_readOnly);
            _field_Reference_Period = new javafx.scene.control.TextField();
            _field_Reference_Period.setPrefWidth(width);//TODO: remove this workaround
            _field_Reference_Period.setEditable(!_readOnly);
            _field_Reference_Period_Count = new javafx.scene.control.TextField();
            _field_Reference_Period_Count.setPrefWidth(width);//TODO: remove this workaround
            _field_Reference_Period_Count.setEditable(!_readOnly);
            _field_Type = new javafx.scene.control.TextField();
            _field_Type.setPrefWidth(width);//TODO: remove this workaround
            _field_Type.setEditable(!_readOnly);

        }
        String gapFillingConfig = _attribute.getLatestSample().getValueAsString();
        if (gapFillingConfig.endsWith("]")) {
            _listConfig = new Gson().fromJson(gapFillingConfig, new TypeToken<List<JsonGapFillingConfig>>() {
            }.getType());
        } else {
            _listConfig = new ArrayList<>();
            _listConfig.add(new Gson().fromJson(gapFillingConfig, JsonGapFillingConfig.class));
        }
        if (Objects.nonNull(_listConfig.get(0))) {
            _firstConfig = _listConfig.get(0);
            _field_Name.setText(_firstConfig.getName());
            _field_Type.setText(_firstConfig.getType());
            _field_Boundary.setText(_firstConfig.getBoundary());
            _field_Default_Value.setText(_firstConfig.getDefaultvalue());
            _field_Reference_Period.setText(_firstConfig.getReferenceperiod());
            _field_Reference_Period_Count.setText(_firstConfig.getReferenceperiodcount());
            _field_Bound_Specific.setText(_firstConfig.getBindtospecific());
        }

        _field_Name.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getName().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setName(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Bound_Specific.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getBindtospecific().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setBindtospecific(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Boundary.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getBoundary().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setBoundary(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Default_Value.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getDefaultvalue().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setDefaultvalue(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Reference_Period.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getReferenceperiod().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setReferenceperiod(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Reference_Period_Count.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getReferenceperiodcount().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setReferenceperiodcount(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });

        _field_Type.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                if (_firstConfig == null || !_firstConfig.getType().equals(newValue)) {
                    logger.info("Value Changed: {}", newValue);
                    _firstConfig.setType(newValue);

                    _newSample = _attribute.buildSample(new DateTime(), _firstConfig.toString());
                    _changed.setValue(true);
                }
            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        });


        if (_field == null) {
            _field = new javafx.scene.control.TextField();
            _field.setPrefWidth(500);//TODO: remove this workaround
            _field.setEditable(!_readOnly);

            JEVisSample lsample = _attribute.getLatestSample();

            if (lsample != null) {
                _field.setText(lsample.getValueAsString());
                _lastSample = lsample;
                logger.trace("Value: {}", _lastSample.toString());
            } else {
                logger.trace("empty value");
                _field.setText("");
            }

            _field.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

                try {
                    if (_lastSample == null || !_lastSample.getValueAsString().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _newSample = _attribute.buildSample(new DateTime(), newValue);
                        _changed.setValue(true);
                    }
                } catch (JEVisException ex) {
                    logger.catching(ex);
                }

            });

            _field.setPrefWidth(500);
            _field.setId("attributelabel");
        }

        Label name = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.name"));
        Label type = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.type"));
        Label boundary = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.boundary"));
        Label default_value = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.defaultvalue"));
        Label reference_period = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.referenceperiod"));
        Label reference_period_count = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.referenceperiodcount"));
        Label boundtospecific = new Label(I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.label.boundto"));
        Label all = new Label("String");
        _gp.add(name, 0, 0);
        _gp.add(_field_Name, 1, 0);
        _gp.add(type, 0, 1);
        _gp.add(_field_Type, 1, 1);
        _gp.add(boundary, 0, 2);
        _gp.add(_field_Boundary, 1, 2);
        _gp.add(default_value, 0, 3);
        _gp.add(_field_Default_Value, 1, 3);
        _gp.add(reference_period, 0, 4);
        _gp.add(_field_Reference_Period, 1, 4);
        _gp.add(reference_period_count, 0, 5);
        _gp.add(_field_Reference_Period_Count, 1, 5);
        _gp.add(boundtospecific, 0, 6);
        _gp.add(_field_Bound_Specific, 1, 6);
        _gp.add(all, 0, 7);
        _gp.add(_field, 1, 7);

        root.getChildren().add(_gp);
        HBox.setHgrow(_gp, Priority.ALWAYS);
        _dialog.getDialogPane().contentProperty().setValue(_gp);

        try {
            if (_attribute.getType().getValidity() == JEVisConstants.Validity.AT_DATE) {
                javafx.scene.control.Button chartView = new Button();
                chartView.setGraphic(JEConfig.getImage("1394566386_Graph.png", 20, 20));
                chartView.setStyle("-fx-padding: 0 2 0 2;-fx-background-insets: 0;-fx-background-radius: 0;-fx-background-color: transparent;");

                chartView.setMaxHeight(_field.getHeight());
                chartView.setMaxWidth(20);

                box.getChildren().add(chartView);
                HBox.setHgrow(chartView, Priority.NEVER);

            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(StringValueEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        _dialog.getDialogPane().getButtonTypes().addAll(ok, cancel);

        Platform.runLater(() -> _field_Name.requestFocus());
        _dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {

                        GapFillingEditor.this.response = GapFillingEditor.Response.YES;
                    } else {
                        GapFillingEditor.this.response = GapFillingEditor.Response.CANCEL;
                    }
                });


        logger.trace("Done");
        return response;
    }

    public enum Response {

        NO, YES, CANCEL
    }
}
