package org.jevis.jecc.dialog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
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
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.TopMenu;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonViewerDialog {
    private static final Logger logger = LogManager.getLogger(JsonViewerDialog.class);
    private final int iconSize = 32;
    private final ImageView rightImage = ControlCenter.getImage("right.png", 20, 20);
    private final ImageView leftImage = ControlCenter.getImage("left.png", 20, 20);
    private final MFXComboBox<JEVisFileWithSample> fileComboBox = new MFXComboBox<>(FXCollections.observableArrayList());
    private final Map<JEVisFile, JEVisSample> sampleMap = new HashMap<>();
    private final ImageView jsonIcon = ControlCenter.getImage("json_icon.png", iconSize, iconSize);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Stage stage;
    private Label fileName;

    public JsonViewerDialog() {
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.fileComboBox.setFloatMode(FloatMode.DISABLED);
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
                                logger.error("Could not add date to date list.");
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

        ControlCenter.getStatusBar().addTask(JsonViewerDialog.class.getName(), loadOtherFilesInBackground, jsonIcon.getImage(), true);

        stage.setTitle(I18n.getInstance().getString("dialog.jsonviewer.title"));

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

        ToggleButton jsonButton = new ToggleButton("", jsonIcon);
        Tooltip jsonTooltip = new Tooltip(I18n.getInstance().getString("dialog.jsonviewer.tooltip.json"));
        jsonButton.setTooltip(jsonTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(jsonButton);

        jsonButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Json File Destination");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Json Files (*.json)", ".json");
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

        ToggleButton printButton = new ToggleButton("", ControlCenter.getImage("Print_1493286.png", iconSize, iconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("dialog.jsonviewer.tooltip.print"));
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

        //TODO JFX17
        fileComboBox.setFloatMode(FloatMode.DISABLED);
        fileComboBox.setConverter(new StringConverter<JEVisFileWithSample>() {
            @Override
            public String toString(JEVisFileWithSample object) {
                if (object != null) {
                    return object.getPdfFile().getFilename();
                } else return "";
            }

            @Override
            public JEVisFileWithSample fromString(String string) {
                return fileComboBox.getItems().stream().filter(jeVisFileWithSample -> jeVisFileWithSample.getPdfFile().getFilename().equals(string)).findFirst().orElse(null);
            }
        });

        leftImage.setOnMouseClicked(event -> {
            int i = fileComboBox.getSelectionModel().getSelectedIndex();
            if (i > 0) {
                fileComboBox.getSelectionModel().selectIndex(i - 1);
            }
        });

        rightImage.setOnMouseClicked(event -> {
            int i = fileComboBox.getSelectionModel().getSelectedIndex();
            if (i < sampleMap.size()) {
                fileComboBox.getSelectionModel().selectIndex(i + 1);
            }
        });

        Separator separator2 = new Separator(Orientation.VERTICAL);
        VBox left = new VBox(leftImage);
        left.setAlignment(Pos.CENTER);
        VBox right = new VBox(rightImage);
        right.setAlignment(Pos.CENTER);
        VBox sampleSelection = new VBox(new HBox(4, left, fileComboBox, right));
        sampleSelection.setAlignment(Pos.CENTER);

        headerBox.getChildren().addAll(jsonButton, printButton, separator, sampleSelection, spacer, fileName);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bp.setTop(headerBox);

        TextArea textArea = new TextArea();
        bp.setCenter(textArea);

        Scene scene = new Scene(bp);
        stage.setScene(scene);

        try {
            JsonNode jsonNode = objectMapper.readTree(file.getBytes());
            textArea.setText(jsonNode.toPrettyString());
        } catch (IOException e) {
            logger.error("Could not read json file", e);
        }

        fileComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) {
                try {
                    byte[] bytesFromSampleMap = newValue.getPdfFile().getBytes();
                    Platform.runLater(() -> fileName.setText(newValue.getPdfFile().getFilename()));
                    JsonNode jsonNode = objectMapper.readTree(bytesFromSampleMap);
                    textArea.setText(jsonNode.toPrettyString());
                } catch (Exception e) {
                    logger.error("Could not load json file for ts {}", newValue.toString(), e);
                }
            }
        });
        stage.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TopMenu.applyActiveTheme(scene);
            }
        });
        stage.showAndWait();
    }
}

