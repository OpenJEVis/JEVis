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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.PasswordDialog;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class PasswordEditor implements AttributeEditor {

    HBox box = new HBox();
    public JEVisAttribute _attribute;
    private boolean _hasChanged = false;
    private Button _setPW;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private boolean _readOnly = true;

    public PasswordEditor(JEVisAttribute att) {
        _attribute = att;
    }

    @Override
    public boolean hasChanged() {
//        System.out.println(_attribute.getName() + " changed: " + _hasChanged);
        return _hasChanged;
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
//        _attribute = att;
//    }
    @Override
    public void commit() throws JEVisException {
//        if (_hasChanged && _newSample != null) {
//
//            //TODO: check if tpye is ok, maybe better at imput time
//            _newSample.commit();
//        }
    }

    @Override
    public Node getEditor() {
        try {
            buildTextFild();
        } catch (Exception ex) {

        }

        return box;
//        return _field;
    }

    private void buildTextFild() throws JEVisException {
        if (_setPW == null) {
            _setPW = new Button(I18n.getInstance().getString("plugin.object.attribute.password.button"),
                    JEConfig.getImage("1415303685_lock-s1.png", 18, 18));
            _setPW.setDisable(_readOnly);
            _setPW.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent t) {
                    PasswordDialog dia = new PasswordDialog();
                    if (dia.show(JEConfig.getStage()) == PasswordDialog.Response.YES) {

                        try {
                            String note = String.format("Password set by %s", _attribute.getDataSource().getCurrentUser().getAccountName());

                            JEVisSample sample;
                            if (_attribute.hasSample()) {
                                System.out.println("update existing sample");
                                sample = _attribute.getLatestSample();
                                sample.setValue(dia.getPassword());
                            } else {
                                System.out.println("create new sample");
                                sample = _attribute.buildSample(new DateTime(), dia.getPassword(), note);

                            }
                            sample.commit();

                        } catch (JEVisException ex) {
                            Logger.getLogger(PasswordEditor.class.getName()).log(Level.SEVERE, null, ex);
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
