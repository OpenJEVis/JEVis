/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

/**
 *
 * @author Benjamin Reich
 */
public class IPAdressEditor implements AttributeEditor {

    private final TextField ipAdressField = new TextField();
    private final HBox editor = new HBox();

    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisDataSource ds;
    private JEVisAttribute att;
    private JEVisSample newSample;
    private static final Logger logger = LogManager.getLogger(IPAdressEditor.class);

    public IPAdressEditor(JEVisAttribute att) {
        this.att = att;
        builedGUI();
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.trace("commit: {}", att.getName());
        if (newSample != null) {
            newSample.commit();
            logger.trace("commit.done: {}", att.getName());
        }

    }

    @Override
    public Node getEditor() {
        return editor;
    }

    private void builedGUI() {
        JEVisSample sample = att.getLatestSample();
        ipAdressField.setPrefWidth(500);
        try {
            ipAdressField.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    try {
                        logger.trace("Envint in IP huh");
                        newSample = att.buildSample(new DateTime(), ipAdressField.getText());
                        logger.trace("-------My new value: {}", newSample.getValueAsString());
                        _changed.setValue(Boolean.TRUE);
//                        ipAdressField.setText(newSample.getValueAsString());

                    } catch (JEVisException ex) {
                        logger.catching(ex);
                    }
                }
            });

        } catch (Exception ex) {
            logger.catching(ex);
        }

        if (sample != null) {
            try {
                ipAdressField.setText(sample.getValueAsString());

            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        }

        editor.getChildren().add(ipAdressField);
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
    }

    @Override
    public JEVisAttribute getAttribute() {
        return att;
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }
}
