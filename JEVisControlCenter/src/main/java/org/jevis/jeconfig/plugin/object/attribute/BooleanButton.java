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

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BooleanButton implements AttributeEditor {

    public JEVisAttribute _attribute;
//    private CheckBox _field;
    private ToggleButton _field;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private boolean _hasChanged = false;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private boolean _canRead = true;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(BooleanButton.class);

    public BooleanButton(JEVisAttribute att) {
        _attribute = att;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _canRead = canRead;
    }

    @Override
    public boolean hasChanged() {
        //System.out.println(_attribute.get - Name() + " changed: " + _hasChanged);
        return _changed.getValue();
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void commit() throws JEVisException {
//        System.out.println("Commit boolean ");
//        if (_newSample != null) {
//            System.out.println("Value: " + _newSample.getValue());
//            //TODO: check if tpye is ok, maybe better at imput time
//            _newSample.commit();
//        }
    }

    @Override
    public Node getEditor() {
        try {
            buildEditor();
        } catch (Exception ex) {

        }
        return _field;

    }

    private void buildEditor() throws JEVisException {
        if (_field == null) {
            logger.trace("start");
            _field = new ToggleButton();
            _field.getStylesheets().add("/styles/BooleanButton.css");
            _field.setMaxWidth(150);
            _field.setMaxHeight(18);

            JEVisSample lsample = _attribute.getLatestSample();

            if (lsample != null) {
                _field.setSelected(lsample.getValueAsBoolean());//TODO: get default Value
                _lastSample = lsample;
            } else {
                _field.setSelected(false);//TODO: get default Value
            }

            _field.setOnAction((ActionEvent t) -> {
                try {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle(I18n.getInstance().getString("plugin.object.attribute.boolean.alert.title"));
                    alert.setHeaderText(I18n.getInstance().getString("plugin.object.attribute.boolean.alert.message"));

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        _newSample = _attribute.buildSample(new DateTime(), _field.isSelected());
                        //_changed.setValue(true);    It alaways be commited by press any button
                        try {
                            _newSample.commit();    //Commit sample by press any button
                        } catch (JEVisException ex) {

                        }

                        toggleText();
                    } else {
                        _field.setSelected(!_field.isSelected());
                    }

                } catch (Exception ex) {
                    Logger.getLogger(BooleanButton.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            //_field.setPrefWidth(500);
            _field.setId("attributelabel");
            toggleText();

            _field.setDisable(_canRead);
            logger.trace("end");
        }
    }

    private void toggleText() {
        if (_field.isSelected()) {
            _field.setText(I18n.getInstance().getString("plugin.object.attribute.boolean.inprogress"));
        } else {
            _field.setText(I18n.getInstance().getString("plugin.object.attribute.boolean.idle"));
        }

        System.out.println("_field Status: " + _field.isSelected());
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

}
