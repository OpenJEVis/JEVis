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


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.ws.json.Json18nEnum;
import org.jevis.jecc.application.application.I18nWS;
import org.jevis.jecc.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Florian Simon
 */
public class DynamicEnumEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(DynamicEnumEditor.class);
    private final HBox editor = new HBox();
    private final JEVisAttribute att;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final JEVisSample originalSample;
    private final ObservableList<String> observableList = FXCollections.observableArrayList();
    private Map<String, Json18nEnum> json18nEnum = new HashMap<>();
    private JEVisSample newSample = null;


    public DynamicEnumEditor(JEVisAttribute att) {
        this.att = att;
        originalSample = att.getLatestSample();
        parseEnum();
        buildGUI();
    }

    private void parseEnum() {
        if (att != null) {
            try {
                json18nEnum = I18nWS.getInstance().getEnum(att.getObject().getJEVisClassName(), att.getName());
                observableList.setAll(json18nEnum.keySet());
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    private String getLocalEnumName(String enumName) {
        String name = I18n.getInstance().getString("plugin.object.attribute.missingname");
        boolean found = false;
        for (Map.Entry<String, String> entry : json18nEnum.get(enumName).getNames().entrySet()) {
            String s = entry.getKey();
            String s2 = entry.getValue();
            if (s.equals(I18nWS.getInstance().getLanguage())) {
                name = s2;
                found = true;
                break;
            }
        }

        if (!found) {
            for (Map.Entry<String, String> entry : json18nEnum.get(enumName).getNames().entrySet()) {
                String s = entry.getKey();
                String s2 = entry.getValue();
                if (s.equals(Locale.ENGLISH.getLanguage())) {
                    name = s2;
                    found = true;
                    break;
                }
            }
        }

        return name;
    }

    @Override
    public void commit() throws JEVisException {
        logger.error("commit: {}", att.getName());
        if (hasChanged()) {
            _changed.setValue(false);
            newSample.commit();
            logger.trace("commit.done: {}", att.getName());
        }

    }

    @Override
    public Node getEditor() {
        return editor;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            editor.getChildren().clear();
            buildGUI();
        });
    }


    private void buildGUI() {
        try {
            try {
                ComboBox<String> picker = new ComboBox<>(observableList);
                picker.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());

                //TODO JFX17

                picker.setConverter(new StringConverter<String>() {
                    @Override
                    public String toString(String object) {
                        return getLocalEnumName(object);
                    }

                    @Override
                    public String fromString(String string) {
                        return picker.getItems().get(picker.getSelectionModel().getSelectedIndex());
                    }
                });

                editor.getChildren().setAll(picker);
                JEVisSample lastSample = att.getLatestSample();
                if (lastSample != null) {
                    String value = lastSample.getValueAsString();
                    picker.getSelectionModel().select(value);
                }

                picker.valueProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        newSample = att.buildSample(DateTime.now(), newValue);
                        _changed.setValue(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                logger.catching(ex);
            }


        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        editor.setDisable(canRead);
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
