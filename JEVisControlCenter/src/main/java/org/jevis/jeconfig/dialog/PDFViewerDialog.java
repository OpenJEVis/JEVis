package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.report.JEVisFileWithSample;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.PDFModel;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFViewerDialog {
    private static final Logger logger = LogManager.getLogger(PDFViewerDialog.class);
    private final int iconSize = 32;
    private Stage stage;
    private final SimpleDoubleProperty zoomFactor = new SimpleDoubleProperty(0.3);
    private PDFModel model;
    private final ImageView rightImage = JEConfig.getImage("right.png", 20, 20);
    private final ImageView leftImage = JEConfig.getImage("left.png", 20, 20);
    private final ComboBox<JEVisFileWithSample> fileComboBox = new ComboBox<>(FXCollections.observableArrayList());
    private final Map<JEVisFile, JEVisSample> sampleMap = new HashMap<>();
    private final ImageView pdfIcon = JEConfig.getImage("pdf_24_2133056.png", iconSize, iconSize);
    private Label fileName;

    public PDFViewerDialog() {

    }

    public void show(JEVisAttribute attribute, JEVisFile file, Window owner) {

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        Task loadOtherFilesInBackground = new Task() {
            @Override
            protected Object call() throws Exception {
                if (attribute != null) {
                    List<JEVisSample> allSamples = attribute.getAllSamples();
                    if (allSamples.size() > 0) {
                        JEVisSample lastSample = allSamples.get(allSamples.size() - 1);
                        sampleMap.clear();
                        List<JEVisFileWithSample> files = new ArrayList<>();
                        for (JEVisSample jeVisSample : allSamples) {
                            try {
                                JEVisFile valueAsFile = jeVisSample.getValueAsFile();
                                files.add(new JEVisFileWithSample(jeVisSample, valueAsFile));
                                sampleMap.put(valueAsFile, jeVisSample);
                            } catch (JEVisException e) {
                                logger.error("Could not add date to dat list.");
                            }
                        }

                        Platform.runLater(() -> {
                            fileComboBox.getItems().clear();
                            fileComboBox.getItems().addAll(files);

                            fileComboBox.getSelectionModel().selectLast();
                        });
                    }
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(PDFViewerDialog.class.getName(), loadOtherFilesInBackground, pdfIcon.getImage(), true);

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

        ToggleButton pdfButton = new ToggleButton("", pdfIcon);
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
        fileName = new Label(file.getFilename());
        fileName.setPadding(new Insets(0, 4, 0, 0));
        fileName.setTextFill(Color.web("#0076a3"));
        fileName.setFont(new Font("Cambria", iconSize));

        Callback<ListView<JEVisFileWithSample>, ListCell<JEVisFileWithSample>> cellFactory = new Callback<ListView<JEVisFileWithSample>, ListCell<JEVisFileWithSample>>() {
            @Override
            public ListCell<JEVisFileWithSample> call(ListView<JEVisFileWithSample> param) {
                return new ListCell<JEVisFileWithSample>() {
                    @Override
                    protected void updateItem(JEVisFileWithSample obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getJeVisFile().getFilename());
                        }
                    }
                };
            }
        };

        fileComboBox.setCellFactory(cellFactory);
        fileComboBox.setButtonCell(cellFactory.call(null));

        leftImage.setOnMouseClicked(event -> {
            int i = fileComboBox.getSelectionModel().getSelectedIndex();
            if (i > 0) {
                fileComboBox.getSelectionModel().select(i - 1);
            }
        });

        rightImage.setOnMouseClicked(event -> {
            int i = fileComboBox.getSelectionModel().getSelectedIndex();
            if (i < sampleMap.size()) {
                fileComboBox.getSelectionModel().select(i + 1);
            }
        });

        Separator separator2 = new Separator(Orientation.VERTICAL);
        VBox left = new VBox(leftImage);
        left.setAlignment(Pos.CENTER);
        VBox right = new VBox(rightImage);
        right.setAlignment(Pos.CENTER);
        VBox sampleSelection = new VBox(new HBox(4, left, fileComboBox, right));
        sampleSelection.setAlignment(Pos.CENTER);

        headerBox.getChildren().addAll(pdfButton, printButton, separator, zoomIn, zoomOut, separator2, sampleSelection, spacer, fileName);
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
        pagination.setPageFactory((
                Integer pageIndex) -> {
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
                int currentPageIndex = pagination.getCurrentPageIndex();
                pagination.setPageFactory((Integer pageIndex) -> {
                    if (pageIndex >= model.numPages()) {
                        return null;
                    } else {
                        return createPage(pageIndex);
                    }
                });
                Platform.runLater(() -> pagination.setCurrentPageIndex(currentPageIndex));
            }
        });

        fileComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) {
                try {
                    byte[] bytesFromSampleMap = newValue.getJeVisFile().getBytes();
                    Platform.runLater(() -> fileName.setText(newValue.getJeVisFile().getFilename()));
                    model.setBytes(bytesFromSampleMap);
                    pagination.setPageCount(model.numPages());
                    zoomFactor.set(0.3);
                    pagination.setPageFactory((Integer pageIndex) -> {
                        if (pageIndex >= model.numPages()) {
                            return null;
                        } else {
                            return createPage(pageIndex);
                        }
                    });
                } catch (Exception e) {
                    logger.error("Could not load report for ts {}", newValue.toString(), e);
                }
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

