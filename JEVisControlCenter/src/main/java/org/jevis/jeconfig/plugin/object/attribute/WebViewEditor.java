/**
 * Copyright (C) 2018 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.plugin.object.attribute;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor to configure JsonGapFillingConfig elements
 */
public class WebViewEditor implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(WebViewEditor.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final BooleanProperty _readOnly = new SimpleBooleanProperty(false);
    public JEVisAttribute _attribute;
    private JEVisSample _lastSample;
    private VBox vBox = new VBox();
    private WebView webView = new WebView();

    public WebViewEditor(JEVisAttribute att) {
        logger.debug("==init== for: {}", att.getName());
        _attribute = att;
        _lastSample = _attribute.getLatestSample();
        vBox.setFillWidth(true);
        vBox.setSpacing(4);
    }

    /**
     * Build main UI
     */
    private void init() {
        List<JEVisSample> allSamples = _attribute.getAllSamples();
        Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
        List<DateTime> dateTimeList = new ArrayList<>();
        for (JEVisSample jeVisSample : allSamples) {
            try {
                dateTimeList.add(jeVisSample.getTimestamp());
                sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
            } catch (JEVisException e) {
                logger.error("Could not add date to dat list.");
            }
        }
        ComboBox<DateTime> dateTimeComboBox = new ComboBox<>(FXCollections.observableList(dateTimeList));
        try {
            if(_lastSample!=null){
                dateTimeComboBox.getSelectionModel().select(_lastSample.getTimestamp());
            }
        } catch (Exception e) {
            logger.error("Could not get Time Stamp of last sample.");
            dateTimeComboBox.getSelectionModel().select(dateTimeList.size() - 1);
        }

        String lastSampleString = "";
        try {
            if (_lastSample != null) {
                lastSampleString = _lastSample.getValueAsString();
                webView.getEngine().loadContent(lastSampleString);
            }
        } catch (Exception e) {
            logger.error("Could not get sample as String.");
        }

        dateTimeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                try {
                    webView.getEngine().loadContent(sampleMap.get(newValue).getValueAsString());
                } catch (Exception e) {
                    logger.error("Could not get sample string for datetime {}", newValue);
                }
            }
        });

        vBox.getChildren().setAll(dateTimeComboBox, webView);

    }

    @Override
    public boolean hasChanged() {

        return _changed.getValue();
    }

    @Override
    public void update() {
        Platform.runLater(this::init);
    }

    @Override
    public void commit() throws JEVisException {

    }

    @Override
    public Node getEditor() {
        try {
            init();
        } catch (Exception ex) {
            logger.catching(ex);
        }

        return vBox;
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

    public WebView getWebView() {
        return webView;
    }
}
