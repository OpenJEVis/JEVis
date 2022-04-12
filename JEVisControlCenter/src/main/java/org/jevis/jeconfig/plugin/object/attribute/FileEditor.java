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
package org.jevis.jeconfig.plugin.object.attribute;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.FileNames;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.JsonViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.joda.time.DateTime;

import java.io.File;

/**
 * @author fs
 */
public class FileEditor implements AttributeEditor {

    private static final Logger logger = LogManager.getLogger(FileEditor.class);
    private final BooleanProperty changed = new SimpleBooleanProperty(false);
    public JEVisAttribute attribute;
    private final HBox box = new HBox();
    private final boolean hasChanged = false;
    private JFXButton _downloadButton;
    private JFXButton uploadButton;
    private boolean readOnly = true;
    //Enable the automatic download of the smaple fo rthe filename
    //This function is suboptial and gives a abd user experience.
    private final boolean _autoDownload = true;

    public FileEditor(JEVisAttribute att) {
        attribute = att;
    }

    @Override
    public boolean hasChanged() {
//        logger.info(attribute.getName() + " changed: " + hasChanged);
        return hasChanged;
    }

    @Override
    public void setReadOnly(boolean canRead) {
        readOnly = canRead;
    }

    @Override
    public BooleanProperty getValueChangedProperty() {
        return changed;
    }

    @Override
    public void commit() {
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            box.getChildren().clear();
            init();
        });
    }

    @Override
    public Node getEditor() {
        try {
            init();
        } catch (Exception ex) {

        }

        return box;
    }

    private void init() {

        _downloadButton = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.file.download")
                , JEConfig.getImage("698925-icon-92-inbox-download-48.png", 18, 18));
        uploadButton = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.file.upload"),
                JEConfig.getImage("1429894158_698394-icon-130-cloud-upload-48.png", 18, 18));

        boolean isPDF = false;
        boolean isImage = false;
        boolean isJson = false;

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        try {
            JEVisSample lsample = attribute.getLatestSample();
            if (lsample != null) {
                String fileName = lsample.getValueAsString();
                if (_autoDownload) {
                    _downloadButton.setText(FileNames.fixName(fileName));
                } else {
                    _downloadButton.setText(I18n.getInstance().getString("plugin.object.attribute.file.button_emty"));
                }
                String extension = FilenameUtils.getExtension(fileName);
                switch (extension) {
                    case "pdf":
                        isPDF = true;
                        break;
                    case "png":
                    case "jpg":
                    case "jpeg":
                    case "gif":
                        isImage = true;
                        break;
                    case "json":
                        isJson = true;
                        break;
                }
            } else {
                _downloadButton.setDisable(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        box.setSpacing(10);

        box.getChildren().setAll(uploadButton, _downloadButton, rightSpacer);

        if (isPDF) {
            JFXButton pdfButton = new JFXButton("", JEConfig.getImage("pdf_24_2133056.png", 18, 18));
            box.getChildren().add(2, pdfButton);
            pdfButton.setOnAction(event -> {
                PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                try {
                    JEVisSample latestSample = attribute.getLatestSample();
                    if (latestSample != null) {
                        pdfViewerDialog.show(attribute, latestSample.getValueAsFile(), JEConfig.getStage());
                    }
                } catch (JEVisException e) {
                    logger.error("Could not open pdf viewer", e);
                }
            });
        }

        if (isImage) {
            JFXButton imageButton = new JFXButton("", JEConfig.getImage("export-image.png", 18, 18));
            box.getChildren().add(2, imageButton);
            imageButton.setOnAction(event -> {
                ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                try {
                    JEVisSample latestSample = attribute.getLatestSample();
                    if (latestSample != null) {
                        imageViewerDialog.show(attribute, latestSample.getValueAsFile(), JEConfig.getStage());
                    }
                } catch (JEVisException e) {
                    logger.error("Could not open pdf viewer", e);
                }
            });
        }

        if (isJson) {
            JFXButton jsonButton = new JFXButton("", JEConfig.getImage("json_icon.png", 18, 18));
            box.getChildren().add(2, jsonButton);
            jsonButton.setOnAction(event -> {
                JsonViewerDialog jsonViewerDialog = new JsonViewerDialog();
                try {
                    JEVisSample latestSample = attribute.getLatestSample();
                    if (latestSample != null) {
                        jsonViewerDialog.show(attribute, latestSample.getValueAsFile(), JEConfig.getStage());
                    }
                } catch (JEVisException e) {
                    logger.error("Could not open pdf viewer", e);
                }
            });
        }

        uploadButton.setOnAction(t -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(JEConfig.getLastPath());
            fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.upload"));
            fileChooser.getExtensionFilters().addAll(
                    //                            new ExtensionFilter("Text Files", "*.txt"),
                    //                            new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                    //                            new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                    new ExtensionFilter("All Files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
            if (selectedFile != null) {
                try {
                    JEConfig.setLastPath(selectedFile);
                    logger.debug("add new file: {}", selectedFile);
                    JEVisFile jfile = new JEVisFileImp(selectedFile.getName(), selectedFile);
                    JEVisSample sample = attribute.buildSample(new DateTime(), jfile);

                    //-----
                    final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.waitsave"));

                    Task<Void> upload = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {

                            sample.commit();//Workaround, normally the user need to press save
                            Thread.sleep(60);
                            return null;
                        }
                    };
                    upload.setOnSucceeded(event -> {
                        pForm.getDialogStage().close();
                        try {
//                                    attribute.getDataSource().getAttributes(attribute.getObjectID());//Reload workaround
                            attribute.getLatestSample();//Reload workaround
                            _downloadButton.setText(selectedFile.getName());
                            _downloadButton.setDisable(false);
                            setChanged(true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    upload.setOnCancelled(event -> pForm.getDialogStage().hide());

                    upload.setOnFailed(event -> {
                        pForm.getDialogStage().hide();
                        logger.info("Error while upload");
                    });

                    pForm.activateProgressBar(upload);
                    pForm.getDialogStage().show();

                    new Thread(upload).start();

                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }
        });

        _downloadButton.setOnAction(t -> {
            try {
                loadWithAnimation();
                JEVisFile file = attribute.getLatestSample().getValueAsFile();

                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(FileNames.fixName(file.getFilename()));
                fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
                fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
                if (selectedFile != null) {
                    JEConfig.setLastPath(selectedFile);
                    file.saveToFile(selectedFile);
                }
            } catch (Exception ex) {
                logger.catching(ex);
            }
        });

        uploadButton.setDisable(readOnly);

    }


    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }

    private void loadWithAnimation() {
        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.attribute.file.progress"));

        Task<Void> upload = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                Thread.sleep(60);
                return null;
            }
        };
        upload.setOnSucceeded(event -> pForm.getDialogStage().close());

        EventHandler<WorkerStateEvent> workerStateEventEventHandler = event -> pForm.getDialogStage().hide();
        upload.setOnCancelled(workerStateEventEventHandler);

        upload.setOnFailed(workerStateEventEventHandler);

        pForm.activateProgressBar(upload);
        pForm.getDialogStage().show();

        new Thread(upload).start();
    }

    @Override
    public boolean isValid() {
        //TODO: implement validation
        return true;
    }

    public void setChanged(boolean changed) {
        this.changed.set(changed);
    }
}
