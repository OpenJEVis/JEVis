package org.jevis.jeconfig.plugin.object.attribute;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;

import java.util.logging.Level;

public class GapFillingEditor implements AttributeEditor {
    private final Logger logger = LogManager.getLogger(GapFillingEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    HBox box = new HBox();
    private String logPrefix = "";
    private boolean _readOnly = true;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private TextField _field;

    public GapFillingEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _changed.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                logger.debug("------------------> StringValueChanged: {}", newValue);
            }
        });
    }

    @Override
    public boolean hasChanged() {
        return _changed.getValue();
    }

    @Override
    public void commit() throws JEVisException {
        logger.debug("StringValueEditor.commit(): '{}' {} {}", _attribute.getName(), hasChanged(), _newSample);
        if (hasChanged() && _newSample != null) {
            //TODO: check if tpye is ok, maybe better at imput time
            _newSample.commit();
            _lastSample = _newSample;
            _newSample = null;
            _changed.setValue(false);
        }
    }

    @Override
    public Node getEditor() {
        try {
            init();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return box;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly = canRead;
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _attribute;
    }

    private void init() throws JEVisException {
        if (_field == null) {
            _field = new javafx.scene.control.TextField();
            _field.setPrefWidth(500);//TODO: remove this workaround
            _field.setEditable(!_readOnly);

            JEVisSample lsample = _attribute.getLatestSample();

            if (lsample != null) {
                _field.setText(lsample.getValueAsString());
                _lastSample = lsample;
                logger.trace("Value: {}", _lastSample.toString());
            } else {
                logger.trace("emty value");
                _field.setText("");
            }

            _field.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

                try {
                    if (_lastSample == null || !_lastSample.getValueAsString().equals(newValue)) {
                        logger.info("Value Changed: {}", newValue);
                        _newSample = _attribute.buildSample(new DateTime(), newValue);
                        _changed.setValue(true);
                    }
                } catch (JEVisException ex) {
                    logger.catching(ex);
                }

            });

            _field.setPrefWidth(500);
            _field.setId("attributelabel");

            if (_attribute.getType().getDescription() != null && !_attribute.getType().getDescription().isEmpty()) {
                Tooltip tooltip = new Tooltip();
                try {
                    tooltip.setText(_attribute.getType().getDescription());
                    tooltip.setGraphic(JEConfig.getImage("1393862576_info_blue.png", 30, 30));
                    _field.setTooltip(tooltip);
                } catch (JEVisException ex) {
                    java.util.logging.Logger.getLogger(StringValueEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            box.getChildren().add(_field);
            HBox.setHgrow(_field, Priority.ALWAYS);

            try {
                if (_attribute.getType().getValidity() == JEVisConstants.Validity.AT_DATE) {
                    javafx.scene.control.Button chartView = new Button();
                    chartView.setGraphic(JEConfig.getImage("1394566386_Graph.png", 20, 20));
                    chartView.setStyle("-fx-padding: 0 2 0 2;-fx-background-insets: 0;-fx-background-radius: 0;-fx-background-color: transparent;");

                    chartView.setMaxHeight(_field.getHeight());
                    chartView.setMaxWidth(20);

                    box.getChildren().add(chartView);
                    HBox.setHgrow(chartView, Priority.NEVER);

                }
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(StringValueEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        logger.trace("Done");
    }
}
