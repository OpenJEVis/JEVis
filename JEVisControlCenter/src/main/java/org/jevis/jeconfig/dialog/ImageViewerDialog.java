package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisFile;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

public class ImageViewerDialog {
    private static final Logger logger = LogManager.getLogger(ImageViewerDialog.class);
    private final int iconSize = 32;
    private Stage stage;
    private ImageView imageView = new ImageView();

    public ImageViewerDialog() {

    }

    public void show(JEVisFile file, Window owner) {

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("dialog.pdfviewer.title"));

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.initOwner(owner);

        double maxScreenWidth = Screen.getPrimary().getBounds().getMaxX();
        double maxScreenHeight = Screen.getPrimary().getBounds().getMaxY();
        stage.setWidth(maxScreenWidth * 0.85);
        stage.setHeight(maxScreenHeight * 0.85);

        stage.setResizable(true);

        ScrollPane sp = new ScrollPane(imageView);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        imageView.fitWidthProperty().bind(sp.widthProperty());

        BorderPane bp = new BorderPane();

        HBox headerBox = new HBox();
        headerBox.setSpacing(4);

        ToggleButton saveButton = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        Tooltip pdfTooltip = new Tooltip(I18n.getInstance().getString("sampleeditor.confirmationdialog.save"));
        saveButton.setTooltip(pdfTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(saveButton);

        saveButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Image File Destination");
            FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files (*." + file.getFileExtension() + ")", "." + file.getFileExtension());
            fileChooser.getExtensionFilters().addAll(imageFilter);
            fileChooser.setSelectedExtensionFilter(imageFilter);

            fileChooser.setInitialFileName(file.getFilename());
            File fileDestination = fileChooser.showSaveDialog(stage);
            if (fileDestination != null) {
                File destinationFile = new File(fileDestination + fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
                try {
                    file.saveToFile(destinationFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        ToggleButton printButton = new ToggleButton("", JEConfig.getImage("Print_1493286.png", iconSize, iconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print"));
        printButton.setTooltip(printTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        printButton.setOnAction(event -> {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            try {
                printerJob.setPrintable(new Printable() {
                    @Override
                    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                        // Get the upper left corner that it printable
                        int x = (int) Math.ceil(pageFormat.getImageableX());
                        int y = (int) Math.ceil(pageFormat.getImageableY());
                        if (pageIndex != 0) {
                            return NO_SUCH_PAGE;
                        }

                        try {
                            File tempFile = File.createTempFile(file.getFilename().substring(0, file.getFilename().length() - 3), "." + file.getFileExtension());

                            tempFile.deleteOnExit();
                            file.saveToFile(tempFile);
                            Image fxImage = new Image(tempFile.toURI().toString());
                            BufferedImage image = SwingFXUtils.fromFXImage(fxImage, null);

                            graphics.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return PAGE_EXISTS;
                    }
                });
                if (printerJob.printDialog()) {
                    printerJob.print();
                }
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        });

        Region spacer = new Region();
        Label fileName = new Label(file.getFilename());
        fileName.setPadding(new Insets(0, 4, 0, 0));
        fileName.setTextFill(Color.web("#0076a3"));
        fileName.setFont(new Font("Cambria", iconSize));

        headerBox.getChildren().addAll(saveButton, printButton, spacer, fileName);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bp.setTop(headerBox);
        bp.setCenter(sp);

        Scene scene = new Scene(bp);
        stage.setScene(scene);

        try {
            File tempFile = File.createTempFile(file.getFilename().substring(0, file.getFilename().length() - 3), "." + file.getFileExtension());
            tempFile.deleteOnExit();
            file.saveToFile(tempFile);
            Image image = new Image(tempFile.toURI().toString());
            Platform.runLater(() -> imageView.setImage(image));
        } catch (IOException e) {
            e.printStackTrace();
        }


        stage.showAndWait();
    }
}
