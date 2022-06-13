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
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.PasswordDialog;
import org.joda.time.DateTime;


/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PasswordEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(PasswordEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    private final HBox box = new HBox();
    private boolean _hasChanged = false;
    private JFXButton _setPW;
    private boolean _readOnly = true;

    public PasswordEditor(JEVisAttribute att) {
        _attribute = att;
    }

    @Override
    public boolean hasChanged() {
//        logger.info(attribute.getName() + " changed: " + _hasChanged);
        return _hasChanged;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            buildTextField();
        });
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly = canRead;
    }

    //    @Override
//    public void setAttribute(JEVisAttribute att) {
//        attribute = att;
//    }
    @Override
    public void commit() {
//        if (_hasChanged && _newSample != null) {
//
//            //TODO: check if tpye is ok, maybe better at imput time
//            _newSample.commit();
//        }
    }

    @Override
    public Node getEditor() {
        try {
            buildTextField();
        } catch (Exception ex) {

        }

        return box;
//        return _field;
    }

    private void buildTextField() {
        if (_setPW == null) {
            _setPW = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.password.button"),
                    JEConfig.getImage("1415303685_lock-s1.png", 18, 18));
            _setPW.setDisable(_readOnly);
            _setPW.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    PasswordDialog dia = new PasswordDialog();
                    if (dia.show(JEConfig.getStage(), null) == PasswordDialog.Response.YES) {

                        try {
                            String note = String.format("Password set by %s", _attribute.getDataSource().getCurrentUser().getAccountName());

                            JEVisSample sample;
                            if (_attribute.hasSample()) {
                                sample = _attribute.getLatestSample();
                                sample.setValue(dia.getPassword());
                                sample.setNote(note);
                            } else {
                                sample = _attribute.buildSample(new DateTime(), dia.getPassword(), note);

                            }
                            sample.commit();
                            _hasChanged = true;

                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }

                    }
                }
            });
            box.getChildren().add(_setPW);
        }

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
