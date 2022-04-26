package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.stage.*;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.report.JEVisFileWithSample;
import org.jevis.commons.utils.FileNames;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.tool.ScreenSize;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageViewerDialog {
    private static final Logger logger = LogManager.getLogger(ImageViewerDialog.class);
    private final int iconSize = 32;
    private Stage stage;
    private final ImageView imageView = new ImageView();
    private final JFXComboBox<JEVisFileWithSample> fileComboBox = new JFXComboBox<>(FXCollections.observableArrayList());
    private final Map<JEVisFile, JEVisSample> sampleMap = new HashMap<>();
    private final ImageView imageIcon = JEConfig.getImage("1390344346_3d_objects.png", iconSize, iconSize);
    private final ImageView rightImage = JEConfig.getImage("right.png", 20, 20);
    private final ImageView leftImage = JEConfig.getImage("left.png", 20, 20);
    private Label fileName;

    public ImageViewerDialog() {

    }

    public void show(JEVisAttribute attribute, JEVisFile file, Window owner) {

        if (stage != null) {
            stage.close();
            stage = null;
        }

        Task loadOtherFilesInBackground = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    if (attribute != null) {
                        sampleMap.clear();
                        Platform.runLater(() -> fileComboBox.getItems().clear());

                        java.util.List<JEVisSample> allSamples = attribute.getAllSamples();
                        if (allSamples.size() > 0) {
                            for (JEVisSample jeVisSample : allSamples) {
                                try {
                                    JEVisFile valueAsFile = jeVisSample.getValueAsFile();
                                    fileComboBox.getItems().add(new JEVisFileWithSample(jeVisSample, valueAsFile));
                                    sampleMap.put(valueAsFile, jeVisSample);
                                } catch (JEVisException e) {
                                    logger.error("Could not add date to dat list.");
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    failed();
                } finally {
                    succeeded();
                }
                return null;
            }
        };

        loadOtherFilesInBackground.setOnSucceeded(event -> Platform.runLater(() -> fileComboBox.getSelectionModel().selectLast()));

        JEConfig.getStatusBar().addTask(PDFViewerDialog.class.getName(), loadOtherFilesInBackground, imageIcon.getImage(), true);

        stage = new Stage();

        stage.setTitle(I18n.getInstance().getString("dialog.picture.title"));

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.initOwner(owner);

        double maxScreenWidth = Screen.getPrimary().getBounds().getMaxX();
        double maxScreenHeight = Screen.getPrimary().getBounds().getMaxY();
        //stage.setWidth(maxScreenWidth * 0.85);
        //stage.setHeight(maxScreenHeight * 0.85);

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

            fileChooser.setInitialFileName(FileNames.fixName(file.getFilename()));
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

        ToggleButton rotateButton = new ToggleButton("", JEConfig.getImage("Rotate.png", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(rotateButton);
        rotateButton.setOnAction(event -> {
            imageView.setRotate(imageView.getRotate() + 90);
        });

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
                            setText(obj.getPdfFile().getFilename());
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

        headerBox.getChildren().addAll(saveButton, printButton, rotateButton, separator2, sampleSelection, spacer, fileName);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bp.setTop(headerBox);
        bp.setCenter(sp);

        Scene scene = new Scene(bp);
        TopMenu.applyActiveTheme(scene);
        stage.setScene(scene);

        try {
            createImage(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) {
                try {
                    createImage(file);
                } catch (Exception e) {
                    logger.error("Could not load report for ts {}", newValue.toString(), e);
                }
            }
        });

        stage.showAndWait();
    }

    private void createImage(JEVisFile file) throws IOException {
        File tempFile = File.createTempFile(FilenameUtils.getExtension(file.getFilename()), "." + file.getFileExtension());
        tempFile.deleteOnExit();
        file.saveToFile(tempFile);
        Image image = new Image(tempFile.toURI().toString());
        Platform.runLater(() -> imageView.setImage(image));
        Platform.runLater(() -> fileName.setText(file.getFilename()));

        Platform.runLater(() -> {
            imageView.setImage(image);
            fileName.setText(file.getFilename());

            stage.setWidth(ScreenSize.fitScreenWidth(image.getWidth()));
            stage.setHeight(ScreenSize.fitScreenHeight(image.getHeight()));
            stage.centerOnScreen();

        });


    }
}
