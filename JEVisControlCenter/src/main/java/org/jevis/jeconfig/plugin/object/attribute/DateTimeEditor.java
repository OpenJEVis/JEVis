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

import java.time.LocalDateTime;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import jfxtras.scene.control.LocalDateTimeTextField;
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
public class DateTimeEditor implements AttributeEditor {

    private final LocalDateTimeTextField picker = new LocalDateTimeTextField();
    private final HBox editor = new HBox();
    private final JEVisAttribute att;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    private JEVisDataSource ds;
    private JEVisSample newSample;
    private final Logger logger = LogManager.getLogger(DateTimeEditor.class);

    public DateTimeEditor(JEVisAttribute att) {
        this.att = att;
        buildGUI();
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

    private void buildGUI() {
        JEVisSample sample = att.getLatestSample();
        picker.setPrefWidth(500);
        picker.textProperty().addListener((v, oldValue, newValue) -> {
            logger.trace("Envint in IP huh");

            try {
                if (sample == null || sample.getValueAsString().equals(newValue)) {
                    newSample = att.buildSample(new DateTime(), newValue);
                    _changed.setValue(Boolean.TRUE);
                }
            } catch (JEVisException ex) {
                java.util.logging.Logger.getLogger(DateTimeEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        if (sample != null) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(sample.getValueAsString(), picker.getDateTimeFormatter());
                picker.setLocalDateTime(dateTime);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
        editor.getChildren().add(picker);
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

}
