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
import org.jevis.jeconfig.application.control.CalendarBox;
import org.jevis.jeconfig.application.control.CalendarRow;
import org.joda.time.DateTime;

public class CalendarEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(TimeZoneEditor.class);

    private final JEVisAttribute _attribute;
    private final HBox _editor = new HBox(5);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(true);
    private JEVisSample _newSample;

    public CalendarEditor(JEVisAttribute att) {
//        _editor.getStylesheets().add("/styles/Calendar.css");
        _attribute = att;
        buildGUI();
    }

    private void buildGUI() {
        JEVisSample sample = _attribute.getLatestSample();

        CalendarBox calendarBox = new CalendarBox();

        if (sample != null) {
            try {
                String value = sample.getValueAsString();
                CalendarRow calendarRow = new CalendarRow(value);

                try {
                    calendarBox.selectItem(calendarRow);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception ex) {
                logger.fatal(ex);
            }
        }

        calendarBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                _newSample = _attribute.buildSample(new DateTime(), newValue.getCountryCode() + "," + newValue.getStateCode());
                _changed.setValue(Boolean.TRUE);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });

        _editor.getChildren().setAll(calendarBox);
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
