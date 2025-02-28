package org.jevis.jeconfig.application.table;


import com.jfoenix.controls.JFXButton;
import io.jenetics.jpx.GPX;
import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.plugin.object.attribute.GPSEditor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


public class FileCell<T> implements Callback<TableColumn<T, Optional<JEVisSample>>, TableCell<T, Optional<JEVisSample>>> {
    private static final Logger logger = LogManager.getLogger(FileCell.class);

    @Override
    public TableCell<T, Optional<JEVisSample>> call(TableColumn<T, Optional<JEVisSample>> param) {
        return new TableCell<T, Optional<JEVisSample>>() {
            final JFXButton previewButton = new JFXButton("", JEConfig.getSVGImage(Icon.PREVIEW, 20, 20));
            final JFXButton mapButton = new JFXButton("", JEConfig.getSVGImage(Icon.MAP, 20, 20));

            @Override
            protected void updateItem(Optional<JEVisSample> item, boolean empty) {
                super.updateItem(item, empty);

                previewButton.setDisable(true);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {

                    try {
                        JEVisFile jeVisFile = item.get().getValueAsFile();

                        if (item.get().getAttribute().getType().getName().equals(JC.MeasurementInstrument.a_GPSPosition)) {

                            File tmpFile = File.createTempFile(jeVisFile.getFilename(), "tmp");
                            jeVisFile.saveToFile(tmpFile);

                            AtomicReference<Double> lat = new AtomicReference<>();
                            AtomicReference<Double> lon = new AtomicReference<>();
                            InputStream inputStream = Files.newInputStream(tmpFile.toPath());
                            GPX.read(inputStream).tracks()
                                    .forEach(trackSegments -> {
                                        trackSegments.getSegments().forEach(wayPoints -> wayPoints.forEach(wayPoint -> {
                                            lat.set(wayPoint.getLatitude().doubleValue());
                                            lon.set(wayPoint.getLongitude().doubleValue());
                                        }));
                                    });

                            inputStream.close();

                            mapButton.setOnAction(actionEvent -> GPSEditor.openMap(actionEvent, lat.get().toString(), lon.get().toString()));

                            setGraphic(mapButton);
                        } else {
                            previewButton.setOnAction(actionEvent -> {
                                try {

                                    JEVisAttribute jeVisAttribute = item.get().getAttribute();
                                    String fileName = jeVisFile.getFilename();
                                    String fileExtension = FilenameUtils.getExtension(fileName);

                                    if (fileExtension.equals("pdf")) {
                                        Platform.runLater(() -> {
                                            PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                                            pdfViewerDialog.show(jeVisAttribute, jeVisFile, JEConfig.getStage());
                                        });
                                    } else {
                                        Platform.runLater(() -> {
                                            ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                                            imageViewerDialog.show(jeVisAttribute, jeVisFile, JEConfig.getStage());
                                        });
                                    }
                                } catch (Exception e) {
                                    logger.error(e);

                                }

                            });


                            previewButton.setDisable(false);
                            setGraphic(previewButton);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }

                }
            }
        };
    }

}





