/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author br
 */
public class EnumEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(EnumEditor.class);
    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisSample _newSample;

    public EnumEditor(JEVisAttribute att) {
        _editor.getStylesheets().add("/styles/TimeZoneEditor.css");
        _attribute = att;
        buildGUI();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            _editor.getChildren().clear();
            buildGUI();
        });
    }

    private List<String> getEnumList(JEVisAttribute att) {
        List<String> enumList = new ArrayList<>();
        try {
            JEVisClass enumClass = att.getDataSource().getJEVisClass("Enum");
            JEVisClass constantsClass = att.getDataSource().getJEVisClass("Constants");
            List<JEVisObject> enumObjects = att.getDataSource().getObjects(enumClass, true);

            for (JEVisObject enumObj : enumObjects) {
                String jeVisClass = enumObj.getAttribute("JEVisClass").getLatestSample().getValueAsString();
                try {
                    if (jeVisClass.equals(att.getObject().getJEVisClassName())) {
                        List<JEVisObject> constants = enumObj.getChildren(constantsClass, true);
                        for (JEVisObject con : constants) {
                            if (con.getAttribute("Attribute").getLatestSample().getValueAsString().equals(att.getName())) {
                                JEVisAttribute jeVisAttribute = con.getAttribute("Entries");
                                if (jeVisAttribute.hasSample()) {
                                    String[] entries = jeVisAttribute.getLatestSample().getValueAsString().split(";");
                                    Collections.addAll(enumList, entries);
                                }
                            }
                        }

                    }
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }

        } catch (Exception ex) {
            logger.catching(ex);
        }
        return enumList;
    }

    private void buildGUI() {
        JEVisSample sample = _attribute.getLatestSample();
        ObservableList<String> enumList = FXCollections.observableArrayList();
        enumList.addAll(getEnumList(_attribute));
        AlphanumComparator ac = new AlphanumComparator();
        enumList.sort(ac);

        MFXComboBox<String> picker = new MFXComboBox<>(enumList);
        picker.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());

        if (sample != null) {
            try {
                String value = sample.getValueAsString();
                if (enumList.contains(value)) {
                    picker.selectItem(sample.getValueAsString());
                }

            } catch (JEVisException ex) {
                logger.catching(ex);
            }
        }

        picker.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), newValue);
                _changed.setValue(Boolean.TRUE);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        });

        _editor.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());
        _editor.getChildren().setAll(picker);

    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        if (_newSample != null) {
            _newSample.commit();
            _changed.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        return _editor;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        logger.error("setReadOnly on Enum: {}", canRead);
        _editor.setDisable(canRead);
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
