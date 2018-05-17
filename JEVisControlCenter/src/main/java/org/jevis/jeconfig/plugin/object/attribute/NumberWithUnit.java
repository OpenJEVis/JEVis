/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.application.jevistree.plugin.AttributeSettingsDialog;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.jevis.jeconfig.sample.SampleEditor;
import org.joda.time.DateTime;

/**
 * This editor can edit and render values from the type number.
 *
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class NumberWithUnit implements AttributeEditor {

    HBox box = new HBox();
    public JEVisAttribute _attribute;
    private TextField _field;
    private Node cell;
//    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private String preOKValue;
    private boolean _readOnly = true;
    private boolean _withUnit = false;
    private final Logger logger = LogManager.getLogger(NumberWithUnit.class);
    private String logPrefix = "";

    public NumberWithUnit(JEVisAttribute att, boolean withUnit) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _withUnit = withUnit;

        try {
            logPrefix += String.format("[%s-%s] %s -", _attribute.getObject().getID(), _attribute.getObject().getName(), _attribute.getName());
        } catch (Exception ex) {

        }
    }

    @Override
    public boolean hasChanged() {
//        System.out.println(_attribute.getName() + " changed: " + _hasChanged);
        return _changed.getValue();
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
    public void commit() throws JEVisException {
        if (hasChanged()) {

            if (!_field.getText().isEmpty()) {
                JEVisSample _newSample = buildNewSample();
                //TODO: check if type is ok, maybe better at imput time
                _newSample.commit();
                _lastSample = _newSample;
            }

            getValueChangedProperty().setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        try {
            buildTextFild();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
//        return _field;
    }

    private EventHandler<KeyEvent> buildEventHandler() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                TextField txt_TextField = (TextField) event.getSource();

                //only numbers an comma and dot
                if (!event.getCharacter().matches("[.,0-9]")) {
                    event.consume();
                    ;
                }

                try {
                    if (_attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                        if (event.getCharacter().matches("[.,]")) {
                            event.consume();
                            ;
                        }
                    } else if (_attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                        //only one comma or dot
                        if (event.getCharacter().matches("[.,]") && (txt_TextField.getText().contains(".") || txt_TextField.getText().contains(","))) {
                            event.consume();
                            ;
                        }
                    }
                } catch (Exception ex) {
                    logger.debug("{} Keyevent Error: ", logPrefix);
                    logger.catching(ex);
                }

            }
        };
    }

    private StringConverter buildStringConverter() {
        return new StringConverter() {
            @Override
            public String toString(Object object) {
                String returnValue = "";
                try {
                    logger.debug("{}StringConverter init: '{}'", logPrefix, object);
                    if (object != null) {
                        if (!object.toString().isEmpty()
                                && object.toString().trim().length() > 0) {

//                            NumberFormat formate = NumberFormat.getIntegerInstance(Locale.US);
//                            formate.setGroupingUsed(false);
                            String sValue = object.toString().replaceAll(",", ".");
                            sValue = sValue.replaceAll(" ", "");
                            sValue = sValue.replaceAll("[^0-9,.]", "");

                            if (_attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
//                                formate.setParseIntegerOnly(true);
                                Long lValue = (long) Double.parseDouble(sValue);
//                                returnValue = formate.format(lValue);
                                returnValue = lValue.toString();
                            } else {
//                                formate.setMinimumFractionDigits(2);
                                Double dValue = Double.parseDouble(sValue);
//                                returnValue = formate.format(dValue);
                                returnValue = dValue.toString();
                            }
                        } else {
                            logger.debug("{}Emty Number return 0 as default", logPrefix);
                            return "";
                        }

                    }

                } catch (Exception ex) {
                    logger.error("{} Error while Convertig value", logPrefix);
                    logger.catching(ex);
                }
                logger.debug("{}StringConverter return value: '{}'", logPrefix, returnValue);
                return returnValue;
            }

            @Override
            public Object fromString(String string) {
                return string;
            }
        };
    }

    private void buildTextFild() throws JEVisException {
        if (_field == null) {
            _field = new TextField();
            _field.setPrefWidth(GenericAttributeExtension.editorWhith.getValue());

            _lastSample = _attribute.getLatestSample();
//            _field.setPromptText("0.0");

            /**
             * Check Key imput
             */
            _field.addEventFilter(KeyEvent.KEY_TYPED, buildEventHandler());

            StringConverter sc = buildStringConverter();
            _field.setTextFormatter(new TextFormatter<>(sc));

            _field.setId("attributelabel");
            _field.setAlignment(Pos.CENTER_RIGHT);
//            _field.setDisable(_readOnly);
            _field.setEditable(!_readOnly);

            if (_attribute.getType().getDescription() != null && !_attribute.getType().getDescription().isEmpty()) {
                Tooltip tooltip = new Tooltip();
                try {
                    tooltip.setText(_attribute.getType().getDescription());
                    tooltip.setGraphic(JEConfig.getImage("1393862576_info_blue.png", 30, 30));
                    _field.setTooltip(tooltip);
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }

            String unitName = "";
            if (_attribute.getDisplayUnit() != null) {
                unitName = _attribute.getDisplayUnit().toString();
            }
            final Button unitb = new Button(unitName);

            double height = 28;

            unitb.setPrefWidth(80);
            unitb.setPrefHeight(height);
            unitb.setStyle("-fx-background-radius: 0 10 10 0; -fx-base: rgba(75, 106, 139, 0.89);");
            unitb.setAlignment(Pos.BOTTOM_LEFT);
            unitb.setDisable(_readOnly);

            _field.setStyle("-fx-background-radius: 3 0 0 3;");

            unitb.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {

//                    final Point2D nodeCoord = unitb.localToScene(0.0, 0.0);
                    AttributeSettingsDialog asd = new AttributeSettingsDialog();

                    try {
                        if (asd.show(JEConfig.getStage(), _attribute) == AttributeSettingsDialog.Response.YES) {
                            logger.trace("JSOn.unit: {}", _attribute.getDisplayUnit().toJSON());
//                            unitb.setText(asd.getPrefix() + UnitManager.getInstance().formate(suc.getUnit()));
                            asd.saveInDataSource();
                            unitb.setText(_attribute.getDisplayUnit().getLabel());
                            if (_lastSample != null) {
                                Double value = _lastSample.getValueAsDouble(_attribute.getDisplayUnit());
                                _field.setText(value + "");
                            }

                        }
                    } catch (JEVisException ex) {
                        logger.catching(ex);
                    }
                }
            });

            HBox.setHgrow(_field, Priority.ALWAYS);

            Button chartView = new Button();

            try {
                chartView = new Button();
                chartView.setGraphic(JEConfig.getImage("1394566386_Graph.png", height, height));
                chartView.setStyle("-fx-padding: 0 2 0 2;-fx-background-insets: 0;-fx-background-radius: 0;-fx-background-color: transparent;");

                chartView.setMaxHeight(_field.getHeight());
                chartView.setMaxWidth(height);
                chartView.setPrefHeight(height);

                box.getChildren().add(chartView);
                HBox.setHgrow(chartView, Priority.NEVER);
                box.setAlignment(Pos.CENTER_LEFT);

                chartView.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        SampleEditor se = new SampleEditor();
                        se.show(JEConfig.getStage(), _attribute);

                    }
                });
            } catch (Exception ex) {
                logger.catching(ex);
            }
            box.getChildren().setAll(chartView, _field, unitb);

            intLastValue();

            _field.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

                try {
                    if (newValue != null) {
                        if (_lastSample == null) {
                            getValueChangedProperty().setValue(true);
                        } else {
                            try {

                                if (!newValue.isEmpty() && !newValue.endsWith(",") & !newValue.endsWith(".")) {
                                    if (_attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                                        getValueChangedProperty().setValue(!_lastSample.getValueAsLong().equals(Long.parseLong(newValue)));
                                    }

                                    if (_attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                                        double newDValue = Double.parseDouble(newValue);
                                        logger.debug("{} Parsed Double: '{}'", logPrefix, newDValue);
                                        if (!_lastSample.getValueAsDouble().equals(newDValue)) {
                                            logger.debug("{} Value has changed", logPrefix);
                                            getValueChangedProperty().setValue(true);
                                        } else {
                                            logger.debug("{} Value has _NOT_ changed", logPrefix);
                                            getValueChangedProperty().setValue(false);
                                        }

                                    }
                                }

                            } catch (Exception ex) {
                                logger.catching(ex);
                            }
                        }

                    }

                } catch (Exception ex) {
                    logger.catching(ex);
                }

            });

        }

    }

    private void intLastValue() {
        try {
            if (_lastSample != null) {
                Double value;
                if (_attribute.getDisplayUnit() != null && _attribute.getInputUnit() != null) {
                    try {
                        value = _lastSample.getValueAsDouble(_attribute.getDisplayUnit());
                    } catch (Exception ex) {
//                        value = Double.NaN;
//                        Alert alert = new Alert(Alert.AlertType.ERROR);
//                        alert.setTitle("Unit Error");
//                        alert.setHeaderText(null);
//                        alert.setContentText("Unit convert error, fallback to unit less. (" + ex.getMessage() + ")");
//
//                        alert.showAndWait();
                        value = _lastSample.getValueAsDouble();
                    }
                } else {
                    value = _lastSample.getValueAsDouble();
                }
                logger.debug("{} intLastValue: '{}'", logPrefix, value);

                _field.setText(value + "");
//                formate(_field);

                preOKValue = value + "";
            } else {
//                _field.setText("");
                preOKValue = "";
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    private JEVisSample buildNewSample() {
        try {

            JEVisSample newSample = null;
            if (_lastSample == null) {
                newSample = _attribute.buildSample(new DateTime(), _field.getText());
//                _changed.setValue(true);
//                _field.setText(_newSample.getValueAsString());

                //update if the value is diffrent than the old
            } else if (_attribute.getType().getPrimitiveType()==(JEVisConstants.PrimitiveType.LONG)) {
                newSample = buildNewLongSample();
            } else if (_attribute.getType().getPrimitiveType()==(JEVisConstants.PrimitiveType.DOUBLE)) {    
                newSample = buildNewDoubleSample();
                //if the sample is the same as the old do nothing
            } else if (_lastSample.getValueAsString().equals(_field.getText())) {
//                _changed.setValue(false);
//                _field.setText(_lastSample.getValueAsString());
            }
            return newSample;

        } catch (JEVisException ex) {
            logger.catching(ex);
        }

        return null;
    }
    
    private JEVisSample buildNewLongSample() {
        JEVisSample newSample = null;
        try {
            if (Long.parseLong(_field.getText()) != _lastSample.getValueAsLong()) {
                if (_attribute.getDisplayUnit() != null && _attribute.getInputUnit() != null) {
                    newSample = _attribute.buildSample(new DateTime(), getConvertedValue().longValue());
                } else {
                    newSample = _attribute.buildSample(new DateTime(), _field.getText());
                }
            }
            return newSample;
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(NumberWithUnit.class.getName()).log(Level.SEVERE, "Could not build a new double sample", ex);
        }
        return null;
    }

    private JEVisSample buildNewDoubleSample() {
        JEVisSample newSample = null;
        try {
            if (Double.parseDouble(_field.getText()) != _lastSample.getValueAsDouble()) {
                
                if (_attribute.getDisplayUnit() != null && _attribute.getInputUnit() != null) {
                    newSample = _attribute.buildSample(new DateTime(), getConvertedValue());
                } else {
                    newSample = _attribute.buildSample(new DateTime(), _field.getText());
                }
//                _changed.setValue(true);
//                _field.setText(_newSample.getValueAsString());
            }
            
            return newSample;
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(NumberWithUnit.class.getName()).log(Level.SEVERE, "Could not build a new double sample", ex);
        }
        return null;
    }
    
    private Double getConvertedValue(){
        JEVisUnit dispUnit;
        try {
            dispUnit = _attribute.getDisplayUnit();
            return dispUnit.converteTo(_attribute.getInputUnit(), Double.valueOf(_field.getText()));
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(NumberWithUnit.class.getName()).log(Level.SEVERE, "Could not convert value according to unit", ex);
        }
        return null;
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }
}
