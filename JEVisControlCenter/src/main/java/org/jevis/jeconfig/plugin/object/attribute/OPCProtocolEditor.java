/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

/**
 * @author br
 */
public class OPCProtocolEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(OPCProtocolEditor.class);

    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private JEVisSample _newSample;
    private String initinitialProtocol = "opc.tcp";

    public OPCProtocolEditor(JEVisAttribute att) {
        _attribute = att;

        try {
            if (_attribute.getLatestSample() != null || !_attribute.getLatestSample().getValueAsString().isEmpty()) {
                initinitialProtocol = _attribute.getLatestSample().getValueAsString();
            }
        } catch (NullPointerException np) {
            try {
                _newSample = _attribute.buildSample(new DateTime(), initinitialProtocol);
                _changed.setValue(Boolean.TRUE);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.catching(ex);
        }

        buildGUI();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            _editor.getChildren().clear();
            buildGUI();
        });
    }


    private void buildGUI() {
        ObservableList<String> enumList = FXCollections.observableArrayList();
        enumList.add("opc.tcp");
        enumList.add("http");

        JFXComboBox picker = new JFXComboBox(enumList);

        picker.getSelectionModel().select(initinitialProtocol);

        picker.setPrefWidth(GenericAttributeExtension.editorWidth.getValue());

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
