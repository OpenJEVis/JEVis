/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.jecc.ControlCenter;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.file.Files;


/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class FileHTML implements AttributeEditor {
    private static final Logger logger = LogManager.getLogger(FileHTML.class);
    private final BooleanProperty _changed = new SimpleBooleanProperty(false);
    private final HBox box = new HBox();
    public JEVisAttribute _attribute;
    private HTMLEditor editor;
    private JEVisSample _newSample;
    private JEVisSample _lastSample;
    private boolean _readOnly = true;
    private boolean initialized = false;

    public FileHTML(JEVisAttribute att) {
        _attribute = att;
    }

    @Override
    public boolean hasChanged() {
//        logger.info(attribute.getName() + " changed: " + _hasChanged);
        return _changed.getValue();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            try {
                box.getChildren().clear();
                buildEditor();
            } catch (Exception ex) {
            }
        });
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return _changed;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        _readOnly = canRead;
    }

    //    @Override
//    public void setAttribute(JEVisAttribute att) {
//        attribute = att;
//    }
    @Override
    public void commit() throws JEVisException {

        // Use the Codec to save the document in a binary format

        try {
            DateTime now = DateTime.now();
            String dateString = now.toString("yyyy-MM-dd") + "_" + now.toString("HHmmss");
            String fileName = _attribute.getObject().getID() + "_" + _attribute.getName() + dateString;
            File tmpFile = File.createTempFile(fileName, "tmp");

            FileWriter fileWriter = new FileWriter(tmpFile);
            fileWriter.write(editor.getHtmlText());

            fileWriter.close();

            JEVisFile file = new JEVisFileImp(fileName, tmpFile);
            _newSample = _attribute.buildSample(now, file);
            _newSample.commit();
        } catch (IOException fnfe) {
            logger.error("IOException while saving file", fnfe);
        } catch (JEVisException e) {
            logger.error("Could not save file to JEVis", e);
        }
    }

    @Override
    public Node getEditor() {
        try {
            if (!initialized) {
                buildEditor();
            }
        } catch (Exception ex) {

        }

        return box;
//        return _field;
    }

    private void buildEditor() throws JEVisException {
        if (editor == null) {

            box.getChildren().clear();
            editor = new HTMLEditor();

            if (_attribute.getLatestSample() != null) {
//                _field.setText(_attribute.getLatestSample().getValueAsString());
                _lastSample = _attribute.getLatestSample();


                try {
                    JEVisFile jeVisFile = _lastSample.getValueAsFile();
                    File tmpFile = File.createTempFile(jeVisFile.getFilename(), "tmp");
                    jeVisFile.saveToFile(tmpFile);

                    InputStream inputStream = Files.newInputStream(tmpFile.toPath());
                    FileReader fileReader = new FileReader(tmpFile);

                    editor.setHtmlText(IOUtils.toString(fileReader));
                    inputStream.close();
                    fileReader.close();

                } catch (IOException e) {
                    logger.error("IOException while loading file", e);
                }
            } else {
//                _field.setText("");
            }

//            _field.setOnKeyReleased(t -> {
//                try {
//                    if (_lastSample == null) {
//                        _newSample = _attribute.buildSample(new DateTime(), _field.getText());
//                        _changed.setValue(true);
//                    } else if (!_lastSample.getValueAsString().equals(_field.getText())) {
//                        _changed.setValue(true);
//                        _newSample = _attribute.buildSample(new DateTime(), _field.getText());
//                    } else if (_lastSample.getValueAsString().equals(_field.getText())) {
//                        _changed.setValue(false);
//                    }
//                } catch (JEVisException ex) {
//                    logger.fatal(ex);
//                }
//
//            });


            if (_attribute.getType().getDescription() != null && !_attribute.getType().getDescription().isEmpty()) {
                Tooltip tooltip = new Tooltip();
                try {
                    tooltip.setText(_attribute.getType().getDescription());
                    tooltip.setGraphic(ControlCenter.getImage("1393862576_info_blue.png", 30, 30));
//                    _field.getArea().setTooltip(tooltip);
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }

            box.getChildren().add(editor);
            HBox.setHgrow(editor, Priority.ALWAYS);

            initialized = true;
        }
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
