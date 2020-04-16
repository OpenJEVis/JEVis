package org.jevis.jeconfig.dialog;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.jevis.api.JEVisFile;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.PDFModel;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

public class PDFViewerDialog {
    private static final Logger logger = LogManager.getLogger(PDFViewerDialog.class);
    private final int iconSize = 32;
    private Stage stage;
    private final SimpleDoubleProperty zoomFactor = new SimpleDoubleProperty(0.3);
    private PDFModel model;

    public PDFViewerDialog() {

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

        BorderPane bp = new BorderPane();

        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(2));
        headerBox.setSpacing(4);

        ToggleButton pdfButton = new ToggleButton("", JEConfig.getImage("pdf_24_2133056.png", iconSize, iconSize));
        Tooltip pdfTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.pdf"));
        pdfButton.setTooltip(pdfTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(pdfButton);

        pdfButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("PDF File Destination");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files (*.pdf)", ".pdf");
            fileChooser.getExtensionFilters().addAll(pdfFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter);

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
                PDDocument document = PDDocument.load(file.getBytes());
                printerJob.setPageable(new PDFPageable(document));
                if (printerJob.printDialog()) {
                    printerJob.print();
                }
            } catch (IOException | PrinterException e) {
                e.printStackTrace();
            }
        });

        Separator separator = new Separator(Orientation.VERTICAL);
        ToggleButton zoomIn = new ToggleButton("", JEConfig.getImage("zoomIn_32.png", this.iconSize, this.iconSize));
        ToggleButton zoomOut = new ToggleButton("", JEConfig.getImage("zoomOut_32.png", this.iconSize, this.iconSize));

        zoomIn.setOnAction(event -> zoomFactor.set(zoomFactor.get() + 0.05));
        zoomOut.setOnAction(event -> zoomFactor.set(zoomFactor.get() - 0.05));

        Region spacer = new Region();
        Label fileName = new Label(file.getFilename());
        fileName.setPadding(new Insets(0, 4, 0, 0));
        fileName.setTextFill(Color.web("#0076a3"));
        fileName.setFont(new Font("Cambria", iconSize));

        headerBox.getChildren().addAll(pdfButton, printButton, separator, zoomIn, zoomOut, spacer, fileName);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bp.setTop(headerBox);
        Pagination pagination = new Pagination();
        pagination.getStylesheets().add(PDFViewerDialog.class.getResource("/styles/pagination.css").toExternalForm());
        bp.setCenter(pagination);
        bp.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() != oldValue.doubleValue()) {
                pagination.setPrefWidth(newValue.doubleValue());
            }
        });

        Scene scene = new Scene(bp);
        stage.setScene(scene);

        byte[] bytes = file.getBytes();
        model = new PDFModel(bytes);
        pagination.setPageCount(model.numPages());
        pagination.setPageFactory((Integer pageIndex) -> {
            if (pageIndex >= model.numPages()) {
                return null;
            } else {
                return createPage(pageIndex);
            }
        });

        pagination.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.isControlDown()) {
                    double deltaY = event.getDeltaY();

                    if (deltaY < 0 && zoomFactor.get() - 0.05 > 0) {
                        zoomFactor.set(zoomFactor.get() - 0.05);
                    } else {
                        zoomFactor.set(zoomFactor.get() + 0.05);
                    }
                    event.consume();
                }
            }
        });

        zoomFactor.addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                pagination.setPageFactory((Integer pageIndex) -> {
                    if (pageIndex >= model.numPages()) {
                        return null;
                    } else {
                        return createPage(pageIndex);
                    }
                });
            }
        });

        stage.showAndWait();
    }

    public HBox createPage(int pageIndex) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        ImageView image = model.getImage(pageIndex, zoomFactor.get());
        Group group = new Group(image);
        ScrollPane scrollPane = new ScrollPane(group);
        vBox.getChildren().add(scrollPane);
        hBox.getChildren().add(vBox);

        return hBox;
    }

}

