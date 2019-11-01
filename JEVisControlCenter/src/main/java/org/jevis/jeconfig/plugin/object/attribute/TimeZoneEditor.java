/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.object.attribute;

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
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


/**
 * @author br
 */
public class TimeZoneEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(TimeZoneEditor.class);

    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(true);
    private JEVisSample _newSample;

    public TimeZoneEditor(JEVisAttribute att) {
        _editor.getStylesheets().add("/styles/TimeZoneEditor.css");
        _attribute = att;
        buildGUI();
    }

    private void buildGUI() {
        JEVisSample sample = _attribute.getLatestSample();

        DateTimeZone utc = DateTimeZone.UTC;
        TimeZoneBox timeZoneBox = new TimeZoneBox();

        if (sample != null) {
            try {
                String value = sample.getValueAsString();
                DateTimeZone dateTimeZone = null;

                try {
                    dateTimeZone = DateTimeZone.forID(value);
                } catch (IllegalArgumentException e) {

                }

                if (dateTimeZone != null) {
                    timeZoneBox.getSelectionModel().select(dateTimeZone);
                } else {
                    //Fix missconfigured zones
                    timeZoneBox.getSelectionModel().select(utc);

                    _newSample = _attribute.buildSample(new DateTime(), utc.getID());
                    _changed.setValue(Boolean.TRUE);

                }

            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        } else {
            try {
                timeZoneBox.getSelectionModel().select(utc);
                _newSample = _attribute.buildSample(new DateTime(), utc.getID());
                _changed.setValue(Boolean.TRUE);
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        }

        _readOnly.addListener((observable, oldValue, newValue) -> timeZoneBox.setDisable(newValue));

        timeZoneBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), newValue);
                _changed.setValue(Boolean.TRUE);
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        });
        _editor.getChildren().addAll(timeZoneBox);

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
    public void update() {
        Platform.runLater(() -> {
            _editor.getChildren().clear();
            buildGUI();
        });
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
        _readOnly.setValue(canRead);
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
