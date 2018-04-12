/**
 * Copyright (C) 2009 - 2016 Envidatec GmbH <info@envidatec.com>
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
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;

/**
 * Basic editor for attributes of the type string.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class StringValueEditor implements AttributeEditor {

    private HBox box = new HBox();
    public JEVisAttribute _attribute;
    private TextField _field;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private boolean _readOnly = true;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(StringValueEditor.class);

    public StringValueEditor(JEVisAttribute att) {
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

        }

        return box;
//        return _field;
    }

    /**
     * Build the GUI for this editor
     *
     * @throws JEVisException
     */
    private void init() throws JEVisException {
        if (_field == null) {
            _field = new TextField();
            _field.setPrefWidth(500);//TODO: remove this workaround
            _field.setEditable(!_readOnly);

            JEVisSample lsample = _attribute.getLatestSample();

            if (lsample != null) {
                _field.setText(lsample.getValueAsString());
                _lastSample = lsample;
                logger.trace("Value: {}", _lastSample.toString());
            } else {
                logger.trace("emty value");
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

            if (_attribute.getType().getDescription() != null && !_attribute.getType().getDescription().isEmpty()) {
                Tooltip tooltip = new Tooltip();
                try {
                    tooltip.setText(_attribute.getType().getDescription());
                    tooltip.setGraphic(JEConfig.getImage("1393862576_info_blue.png", 30, 30));
                    _field.setTooltip(tooltip);
                } catch (JEVisException ex) {
                    Logger.getLogger(StringValueEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            box.getChildren().add(_field);
            HBox.setHgrow(_field, Priority.ALWAYS);

            try {
                if (_attribute.getType().getValidity() == JEVisConstants.Validity.AT_DATE) {
                    Button chartView = new Button();
                    chartView.setGraphic(JEConfig.getImage("1394566386_Graph.png", 20, 20));
                    chartView.setStyle("-fx-padding: 0 2 0 2;-fx-background-insets: 0;-fx-background-radius: 0;-fx-background-color: transparent;");

                    chartView.setMaxHeight(_field.getHeight());
                    chartView.setMaxWidth(20);

                    box.getChildren().add(chartView);
                    HBox.setHgrow(chartView, Priority.NEVER);

                }
            } catch (Exception ex) {
                Logger.getLogger(StringValueEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        logger.trace("Done");
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }
}
