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
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.controlsfx.control.ToggleSwitch;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BooleanValueEditor implements AttributeEditor {

    public JEVisAttribute _attribute;
    private final HBox editorNode = new HBox();
    private ToggleSwitch _field;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private boolean _canRead = true;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(BooleanValueEditor.class);

    public BooleanValueEditor(JEVisAttribute att) {
        _attribute = att;
        try {
            buildEditor();
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public void setReadOnly(boolean canRead) {
        editorNode.setDisable(canRead);
        _canRead = canRead;
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
    public void commit() throws JEVisException {
        if (_newSample != null) {
            //TODO: check if tpye is ok, maybe better at imput time
            _newSample.commit();
        }
    }

    @Override
    public Node getEditor() {
        return editorNode;

    }

    private void buildEditor() throws JEVisException {
        logger.trace("start");
        _field = new ToggleSwitch("On");

        JEVisSample lsample = _attribute.getLatestSample();

        if (lsample != null) {
            System.out.println("set Old Value: " + lsample.getValueAsBoolean());
            boolean selected = lsample.getValueAsBoolean();
            _field.setSelected(selected);
            _field.selectedProperty().setValue(selected);
//            _field.setSelected(selected);//TODO: get default Value
            if (selected) {
                _field.setText("On");
            } else {
                _field.setText("Off");
            }

            _lastSample = lsample;
        } else {
            _field.setSelected(false);//TODO: get default Value
            _field.setText("Off");
        }

        _field.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                try {
                    _newSample = _attribute.buildSample(new DateTime(), _field.isSelected());
                    if (t1) {
                        _field.setText("On");
                    } else {
                        _field.setText("Off");
                    }
                    _changed.setValue(true);
                } catch (Exception ex) {
                    Logger.getLogger(BooleanValueEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        _field.setPrefWidth(65);
        editorNode.setPrefWidth(GenericAttributeExtension.editorWhith.getValue());
        _field.setId("attributelabel");

        logger.trace("end");
//        Region spacer = new  Region();
//        HBox.setHgrow(_field, Priority.NEVER);
//        HBox.setHgrow(spacer, Priority.ALWAYS);
        editorNode.getChildren().addAll(_field);

    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

}
