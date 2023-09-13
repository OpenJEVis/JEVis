package org.jevis.jeconfig.application.table;


import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;

import java.awt.*;
import java.net.URI;
import java.util.Optional;


public class FileCell<T> implements Callback<TableColumn<T, Optional<JEVisSample>>, TableCell<T, Optional<JEVisSample>>> {

    @Override
            public TableCell<T, Optional<JEVisSample>> call(TableColumn<T, Optional<JEVisSample>> param) {
                return new TableCell<T, Optional<JEVisSample>>() {
                    @Override
                    protected void updateItem(Optional<JEVisSample> item, boolean empty) {
                        super.updateItem(item, empty);
                        JFXButton previewButton = new JFXButton("", JEConfig.getSVGImage(Icon.PREVIEW,20,20));

                        try {
                            JEVisFile jeVisFile;
                            JEVisAttribute jeVisAttribute;
                            if (item != null) {
                                if (item.isPresent()) {
                                    jeVisFile = item.get().getValueAsFile();
                                    jeVisAttribute = item.get().getAttribute();
                                    previewButton.setOnAction(actionEvent -> {
                                        String fileName = jeVisFile.getFilename();
                                        String fileExtension = FilenameUtils.getExtension(fileName);

                                        if (fileExtension.equals("pdf")) {
                                            Platform.runLater(() -> {
                                                PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                                                pdfViewerDialog.show(jeVisAttribute, jeVisFile, JEConfig.getStage());
                                            });
                                        }else {
                                            Platform.runLater(() -> {
                                                ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                                                imageViewerDialog.show(jeVisAttribute, jeVisFile, JEConfig.getStage());
                                            });
                                        }

                                    });
                                }else {
                                    previewButton.setDisable(true);
                                }

                            }else {
                                previewButton.setDisable(true);
                            }


                        } catch (Exception e) {
                            System.out.println("jdjsdi");
                            e.printStackTrace();

                        }

                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(previewButton);
                        }
                    }
                };
            }

        };





