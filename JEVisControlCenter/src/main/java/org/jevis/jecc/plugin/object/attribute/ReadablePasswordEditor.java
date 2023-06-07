/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
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

import com.jfoenix.controls.JFXPasswordField;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecc.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;


/**
 * @author br
 */
public class ReadablePasswordEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(ReadablePasswordEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JFXPasswordField passField = new JFXPasswordField();
    private HBox editor = new HBox();
    private JEVisDataSource ds;
    private JEVisAttribute att;

    public ReadablePasswordEditor(JEVisAttribute att) {
        this.att = att;
        builedGUI();
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            editor.getChildren().clear();
            builedGUI();
        });
    }

    @Override
    public void commit() throws JEVisException {
        if (_changed.getValue()) {
            JEVisSample newSample = att.buildSample(new DateTime(), passField.getText());
            newSample.commit();
            _changed.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        return editor;
    }

    private void builedGUI() {
        JEVisSample sample = att.getLatestSample();

        passField.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());
        passField.setAlignment(Pos.CENTER_RIGHT);
        if (sample != null) {
            try {
                passField.setText(sample.getValueAsString());

            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        }

        passField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (oldValue != null && !newValue.equals(oldValue)) {
                _changed.setValue(Boolean.TRUE);
            }
        });

        /**
         * Not so secure function to get a readable password for service purpose. This password are by design not so save because
         * the applications like the JEDataCollector will need the in clean form to send use them anyways. If there is a user with
         * read-right he can get the via API anyway.
         */
        final KeyCombination keyComb1 = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN);
        passField.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (keyComb1.match(event)) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(passField.getText());
                    clipboard.setContent(content);
                }
            }
        });

        editor.getChildren().add(passField);
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        passField.setDisable(canRead);
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
