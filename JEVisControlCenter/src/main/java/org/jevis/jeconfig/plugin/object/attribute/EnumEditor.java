/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author br
 */
public class EnumEditor implements AttributeEditor {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(EnumEditor.class);
    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private JEVisSample _newSample;
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);

    public EnumEditor(JEVisAttribute att) {
        _editor.getStylesheets().add("/styles/TimeZoneEditor.css");
        _attribute = att;
        buildGUI();
    }

    public ObservableList<String> getEnumList(JEVisAttribute att) {
        ObservableList<String> enumList = FXCollections.observableArrayList();
        try {
            JEVisClass enumClass = att.getDataSource().getJEVisClass("Enum");
            JEVisClass constansClass = att.getDataSource().getJEVisClass("Constants");
            List<JEVisObject> enumObjs = att.getDataSource().getObjects(enumClass, true);

            for (JEVisObject enumObj : enumObjs) {
                logger.debug("Enum Obj: {}", enumObj);
                String jclass = enumObj.getAttribute("JEVisClass").getLatestSample().getValueAsString();
                logger.debug("Class to compare to: {}", jclass);
                try {
                    if (jclass.equals(att.getObject().getJEVisClassName())) {
                        logger.debug("true");
                        List<JEVisObject> constats = enumObj.getChildren(constansClass, true);
                        for (JEVisObject con : constats) {
                            logger.debug("Constants obj: {}", con.getID());
                            
                            
                            if (con.getAttribute("Attribute").getLatestSample().getValueAsString().equals(att.getName())) {
                                logger.debug("Attribute matched");
                                JEVisAttribute entrieA = con.getAttribute("Entries");
                                if (entrieA.hasSample()) {
                                    String[] entries = entrieA.getLatestSample().getValueAsString().split(";");
                                    for (String value : entries) {
                                        logger.debug("add Value: {}", value);
                                        enumList.add(value);
                                    }
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

//        ComboBox picker = new ComboBox(enumList);
        JFXComboBox picker = new JFXComboBox(enumList);
        picker.setPrefWidth(GenericAttributeExtension.editorWhith.getValue());

        if (sample != null) {
            try {
                String value = sample.getValueAsString();
                if (enumList.contains(value)) {
                    picker.setValue(sample.getValueAsString());
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
                Logger.getLogger(EnumEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        _editor.setPrefWidth(GenericAttributeExtension.editorWhith.getValue());
        Region spacer = new  Region();
//        HBox box = new HBox();
//        HBox.setHgrow(picker, Priority.NEVER);
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//        box.getChildren().addAll(spacer,picker);
        _editor.getChildren().addAll(picker);

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
        logger.error("setReadOnly on Enum: {}",canRead);
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
