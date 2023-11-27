package org.jevis.jecc.application.table;



import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.dialog.ImageViewerDialog;
import org.jevis.jecc.dialog.PDFViewerDialog;

import java.util.Optional;


public class FileCell<T> implements Callback<TableColumn<T, Optional<JEVisSample>>, TableCell<T, Optional<JEVisSample>>> {

    @Override
    public TableCell<T, Optional<JEVisSample>> call(TableColumn<T, Optional<JEVisSample>> param) {
        return new TableCell<T, Optional<JEVisSample>>() {
            final Button previewButton = new Button("", ControlCenter.getSVGImage(Icon.PREVIEW, 20, 20));

            @Override
            protected void updateItem(Optional<JEVisSample> item, boolean empty) {
                super.updateItem(item, empty);

                previewButton.setDisable(true);
                if (item != null) {
                    if (item.isPresent()) {
                        previewButton.setOnAction(actionEvent -> {
                            try {


                                JEVisFile jeVisFile;
                                JEVisAttribute jeVisAttribute;
                                jeVisFile = item.get().getValueAsFile();
                                jeVisAttribute = item.get().getAttribute();
                                String fileName = jeVisFile.getFilename();
                                String fileExtension = FilenameUtils.getExtension(fileName);

                                if (fileExtension.equals("pdf")) {
                                    Platform.runLater(() -> {
                                        PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                                        pdfViewerDialog.show(jeVisAttribute, jeVisFile, ControlCenter.getStage());
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                                        imageViewerDialog.show(jeVisAttribute, jeVisFile, ControlCenter.getStage());
                                    });
                                }
                            } catch (Exception e) {
                                System.out.println("jdjsdi");
                                e.printStackTrace();

                            }

                        });
                    }
                }
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else if (!item.isPresent()) {
                    setGraphic(previewButton);
                } else {
                    previewButton.setDisable(false);
                    setGraphic(previewButton);
                }
            }
        };
    }

}





