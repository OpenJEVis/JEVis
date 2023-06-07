/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecc.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;


/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BooleanValueEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(BooleanValueEditor.class);
    private final HBox editorNode = new HBox();
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    private ToggleSwitchPlus _field;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private boolean _canRead = true;

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
    public void update() {
        Platform.runLater(() -> {
            try {
                editorNode.getChildren().clear();
                buildEditor();
            } catch (Exception ex) {

            }
        });
    }

    @Override
    public boolean hasChanged() {
//        logger.info(attribute.getName() + " changed: " + _hasChanged);
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
        _field = new ToggleSwitchPlus();

        JEVisSample lsample = _attribute.getLatestSample();


        if (lsample != null) {
            boolean selected = lsample.getValueAsBoolean();
            _field.setSelected(selected);
            _lastSample = lsample;
        } else {
            _field.setSelected(false);//TODO: get default Value
        }

        _field.selectedProperty().addListener((ov, t, t1) -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), _field.isSelected());
                _changed.setValue(true);
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        });
        logger.trace("end");
        editorNode.getChildren().setAll(_field);

    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }


    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
