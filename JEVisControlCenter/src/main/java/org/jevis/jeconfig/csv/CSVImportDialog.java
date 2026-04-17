/**
 * Copyright (C) 2014-2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.csv;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.NotificationPane;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.tool.Layouts;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.jevis.jeconfig.tool.ScreenSize;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVImportDialog {
    private static final Logger logger = LogManager.getLogger(CSVImportDialog.class);

    public static String ICON = "1403727005_gnome-mime-application-vnd.lotus-1-2-3.png";
    final JFXButton ok = new JFXButton(I18n.getInstance().getString("csv.ok"));
    final JFXButton automatic = new JFXButton(I18n.getInstance().getString("csv.automatic"));
    final JFXButton fileButton = new JFXButton(I18n.getInstance().getString("csv.file_select"));
    final JFXButton saveFormat = new JFXButton(I18n.getInstance().getString("csv.save_formate"));
    final NumberSpinner headerRowCount = new NumberSpinner(BigDecimal.valueOf(0), BigDecimal.valueOf(1));
    final ToggleGroup textDiGroup = new ToggleGroup();
    final AnchorPane tableRootPane = new AnchorPane();
    private final double LEFT_PADDING = 30;
    private final JFXComboBox<Separator> separatorComboBox = new JFXComboBox<>(FXCollections.observableArrayList(Separator.values()));
    private final JFXTextField otherSeparatorField = new JFXTextField();
    private final JFXComboBox<Enclosed> enclosedComboBox = new JFXComboBox<>(FXCollections.observableArrayList(Enclosed.values()));
    private final NotificationPane notificationPane = new NotificationPane();
    private final JFXTextField otherEnclosedField = new JFXTextField();
    private final JFXTextField customNoteField = new JFXTextField();
    ObservableList<String> formatOptions;
    private String _encloser = "";
    private String separator = "";
    private File _importFile;
    private FileType currentFileType = FileType.CSV;
    private JEVisDataSource _ds;
    private CSVTable table;
    private Stage stage;
    private Charset charset = Charset.defaultCharset();
    private String customNoteString = "";
    private Response response = Response.CANCEL;
    private JEVisAttribute preselectTarget = null;

    // Dynamic config section — swapped depending on file type
    private VBox dynamicConfigSection;
    private VBox csvSectionBox;
    private VBox xlsxSectionBox;
    private JFXComboBox<String> sheetComboBox;
    private int selectedSheetIndex = 0;

    public CSVImportDialog() {
        preselectTarget = null;
    }

    public CSVImportDialog(JEVisAttribute target) {
        preselectTarget = target;
    }

    // -------------------------------------------------------------------------
    // File-type detection
    // -------------------------------------------------------------------------

    private enum FileType { CSV, XLSX }

    private FileType detectFileType(File f) {
        String name = f.getName().toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) return FileType.XLSX;
        return FileType.CSV;
    }

    // -------------------------------------------------------------------------
    // CSV separator section
    // -------------------------------------------------------------------------

    private Node buildSeparatorPane() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(0, 10, 0, LEFT_PADDING));

        gp.setHgap(4);
        gp.setVgap(5);

        Label sepL = new Label(I18n.getInstance().getString("csv.separator.column"));
        Label sepTextL = new Label(I18n.getInstance().getString("csv.separator.text"));

        Callback<ListView<Separator>, ListCell<Separator>> seperatorCellFactory = new Callback<ListView<Separator>, ListCell<Separator>>() {
            @Override
            public ListCell<Separator> call(ListView<Separator> param) {
                return new ListCell<Separator>() {
                    @Override
                    protected void updateItem(Separator item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            String localText = "";
                            switch (item) {
                                case Tab:
                                    localText = I18n.getInstance().getString("csv.separators.tab");
                                    break;
                                case Comma:
                                    localText = I18n.getInstance().getString("csv.separators.comma");
                                    break;
                                case OTHER:
                                    localText = I18n.getInstance().getString("csv.separators.other");
                                    break;
                                case Semicolon:
                                    localText = I18n.getInstance().getString("csv.separators.semi");
                                    break;
                                case Space:
                                    localText = I18n.getInstance().getString("csv.separators.space");
                                    break;
                                default:
                                    break;
                            }
                            setText(localText);
                        }
                    }
                };
            }
        };
        separatorComboBox.setCellFactory(seperatorCellFactory);
        separatorComboBox.setButtonCell(seperatorCellFactory.call(null));
        otherEnclosedField.setDisable(true);
        otherSeparatorField.setDisable(true);
        separatorComboBox.getSelectionModel().select(Separator.Semicolon);
        separatorComboBox.setOnAction(event -> {
            if (separatorComboBox.getValue() == Separator.OTHER) {
                otherSeparatorField.setDisable(false);
                otherSeparatorField.requestFocus();
            } else {
                otherSeparatorField.setDisable(true);
                updateSeparator();
            }
        });

        Callback<ListView<Enclosed>, ListCell<Enclosed>> enclosedCellfactory = new Callback<ListView<Enclosed>, ListCell<Enclosed>>() {
            @Override
            public ListCell<Enclosed> call(ListView<Enclosed> param) {
                return new ListCell<Enclosed>() {
                    @Override
                    protected void updateItem(Enclosed item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null) {
                            String localText = "";
                            switch (item) {
                                case Apostrophe:
                                    localText = I18n.getInstance().getString("csv.enclosed.apostrophe");
                                    break;
                                case Ditto:
                                    localText = I18n.getInstance().getString("csv.enclosed.ditto");
                                    break;
                                case OTHER:
                                    localText = I18n.getInstance().getString("csv.enclosed.other");
                                    break;
                                case NONE:
                                    localText = I18n.getInstance().getString("csv.enclosed.none");
                                    break;
                                case Gravis:
                                    localText = I18n.getInstance().getString("csv.enclosed.gravis");
                                    break;
                                default:
                            }
                            setText(localText);
                        }
                    }
                };
            }
        };
        enclosedComboBox.setCellFactory(enclosedCellfactory);
        enclosedComboBox.setButtonCell(enclosedCellfactory.call(null));
        enclosedComboBox.getSelectionModel().select(Enclosed.NONE);
        enclosedComboBox.setOnAction(event -> {
            Platform.runLater(() -> {
                if (enclosedComboBox.getValue() == Enclosed.OTHER) {
                    otherEnclosedField.setDisable(false);
                    otherEnclosedField.requestFocus();
                } else {
                    otherEnclosedField.setDisable(true);
                    updateEnclosed();
                }
            });
        });

        otherSeparatorField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSeparator();
        });
        otherEnclosedField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateEnclosed();
        });

        enclosedComboBox.setMinWidth(200);
        separatorComboBox.setMinWidth(200);
        otherSeparatorField.setMinWidth(200);
        otherEnclosedField.setMinWidth(200);

        HBox otherBox = new HBox(5);
        otherBox.setAlignment(Pos.CENTER_LEFT);
        otherBox.getChildren().setAll(otherSeparatorField);

        HBox otherTextBox = new HBox(5);
        otherTextBox.setAlignment(Pos.CENTER_LEFT);
        otherTextBox.getChildren().setAll(otherEnclosedField);

        HBox root = new HBox();

        VBox columnB = new VBox(5);
        VBox textB = new VBox(5);

        root.setPadding(new Insets(5, 10, 5, LEFT_PADDING));
        VBox cSep = new VBox(5);
        VBox tSep = new VBox(5);
        cSep.setPadding(new Insets(0, 0, 0, 20));
        tSep.setPadding(new Insets(0, 0, 0, 20));

        cSep.getChildren().setAll(separatorComboBox, otherBox);
        tSep.getChildren().setAll(enclosedComboBox, otherTextBox);

        columnB.getChildren().setAll(sepL, cSep);
        textB.getChildren().setAll(sepTextL, tSep);

        Region spacer = new Region();

        root.getChildren().setAll(columnB, textB, spacer);
        root.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(columnB, Priority.NEVER);
        HBox.setHgrow(textB, Priority.NEVER);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        return root;
    }

    // -------------------------------------------------------------------------
    // XLSX sheet-selector section
    // -------------------------------------------------------------------------

    private Node buildSheetSelectorPane() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(0, 10, 0, LEFT_PADDING));
        gp.setHgap(4);
        gp.setVgap(5);

        Label sheetLabel = new Label(I18n.getInstance().getString("csv.xlsx.sheet"));
        sheetComboBox = new JFXComboBox<>();
        sheetComboBox.setMinWidth(200);
        sheetComboBox.setOnAction(event -> {
            int idx = sheetComboBox.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx != selectedSheetIndex) {
                selectedSheetIndex = idx;
                updateTree(true);
            }
        });

        gp.add(sheetLabel, 0, 0);
        gp.add(sheetComboBox, 1, 0);

        return gp;
    }

    // -------------------------------------------------------------------------
    // Layout helpers
    // -------------------------------------------------------------------------

    private Node buildTitle(String name) {
        HBox titelBox = new HBox(2);
        titelBox.setPadding(new Insets(8));
        javafx.scene.control.Separator titelSep = new javafx.scene.control.Separator(Orientation.HORIZONTAL);
        titelSep.setMaxWidth(Double.MAX_VALUE);
        Label title = new Label(name);
        titelBox.getChildren().setAll(title, titelSep);
        HBox.setHgrow(titelSep, Priority.NEVER);
        HBox.setHgrow(titelSep, Priority.ALWAYS);
        titelBox.setAlignment(Pos.CENTER_LEFT);
        titelBox.setPrefWidth(1024);
        return titelBox;
    }

    // -------------------------------------------------------------------------
    // show()
    // -------------------------------------------------------------------------

    public Response show(Stage owner, JEVisDataSource ds) {
        stage = new Stage();
        _ds = ds;
        BorderPane root = new BorderPane();
        notificationPane.setContent(root);
        Layouts.setAnchor(notificationPane, 0);
        Scene scene = new Scene(notificationPane);
        TopMenu.applyActiveTheme(scene);

        stage.setTitle(I18n.getInstance().getString("csv.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);
        stage.setScene(scene);
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(true);

        BorderPane header = new BorderPane();
        header.getStyleClass().add("dialog-header");

        Label topTitle = new Label(I18n.getInstance().getString("csv.top_title"));
        topTitle.setTextFill(Color.web("#0076a3"));
        topTitle.setFont(Font.font("Cambria", 18));

        ImageView imageView = ResourceLoader.getImage(ICON, 32, 32);
        stage.getIcons().add(imageView.getImage());

        VBox vboxLeft = new VBox();
        VBox vboxRight = new VBox();
        vboxLeft.setPadding(new Insets(10));
        vboxRight.setPadding(new Insets(10));
        vboxLeft.getChildren().add(topTitle);
        vboxLeft.setAlignment(Pos.CENTER_LEFT);
        vboxRight.setAlignment(Pos.CENTER_LEFT);
        vboxRight.getChildren().add(imageView);

        header.setLeft(vboxLeft);
        header.setRight(vboxRight);
        header.setBottom(new javafx.scene.control.Separator(Orientation.HORIZONTAL));

        HBox buttonPanel = new HBox(8);

        ok.setDefaultButton(true);
        saveFormat.setDisable(true);

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("csv.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().setAll(ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(5));

        // Build CSV separator section
        Node csvSeparatorTitle = buildTitle(I18n.getInstance().getString("csv.tab.title.seperator_options"));
        Node csvSeparatorPane = buildSeparatorPane();
        csvSectionBox = new VBox(csvSeparatorTitle, csvSeparatorPane);

        // Build XLSX sheet selector section
        Node xlsxSheetTitle = buildTitle(I18n.getInstance().getString("csv.tab.title.sheet_options"));
        Node xlsxSheetPane = buildSheetSelectorPane();
        xlsxSectionBox = new VBox(xlsxSheetTitle, xlsxSheetPane);

        // Dynamic section starts showing CSV controls
        dynamicConfigSection = new VBox();
        dynamicConfigSection.getChildren().setAll(csvSectionBox);

        Node filePane = buildFileOptions();
        Node tablePane = buildTablePane();

        GridPane content = new GridPane();
        content.addRow(0, buildTitle(I18n.getInstance().getString("csv.tab.title.file_options")));
        content.addRow(1, filePane);
        content.addRow(2, dynamicConfigSection);
        content.addRow(3, buildTitle(I18n.getInstance().getString("csv.tab.title.field_options")));
        content.addRow(4, tablePane);

        GridPane.setVgrow(tablePane, Priority.ALWAYS);
        content.getColumnConstraints().add(0, new ColumnConstraints(100, 1000, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));
        GridPane.setFillHeight(tablePane, true);

        root.setTop(header);
        root.setCenter(content);
        root.setBottom(buttonPanel);

        cancel.setOnAction(t -> {
            stage.close();
            response = Response.CANCEL;
        });

        ok.setOnAction(t -> {
            table.setCustomNote(customNoteString);
            final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("csv.progress.title"));
            Task<Integer> importTask = table.doImport();
            pForm.activateProgressBar(importTask);
            importTask.setOnSucceeded(event -> {
                pForm.getDialogStage().close();
                showNotification(importTask.getValue() + " " + I18n.getInstance().getString("csv.import.dialog.success.message"), JEConfig.getImage("1404237035_Valid.png", 24, 24));
            });
            importTask.setOnFailed(event -> {
                showNotification(importTask.getValue() + " " + I18n.getInstance().getString("csv.import.dialog.failed.message") + " " + importTask.getMessage()
                        , JEConfig.getImage("1401136217_exclamation-diamond_red.png", 24, 24));
            });
            importTask.setOnRunning(event -> {
                root.setDisable(true);
                pForm.getDialogStage().show();
                root.setDisable(false);
            });
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(importTask);
        });

        stage.setHeight(ScreenSize.fitScreenHeight(900));
        stage.setWidth(ScreenSize.fitScreenWidth(900));
        stage.showAndWait();

        return response;
    }

    // -------------------------------------------------------------------------
    // Switch dynamic config section based on detected file type
    // -------------------------------------------------------------------------

    private void applyFileType(FileType type) {
        currentFileType = type;
        if (type == FileType.XLSX) {
            dynamicConfigSection.getChildren().setAll(xlsxSectionBox);
            automatic.setDisable(true);
        } else {
            dynamicConfigSection.getChildren().setAll(csvSectionBox);
            automatic.setDisable(false);
        }
    }

    // -------------------------------------------------------------------------
    // Table pane
    // -------------------------------------------------------------------------

    private Node buildTablePane() {
        TableView placeholderTree = new TableView();
        TableColumn firstCol = new TableColumn(I18n.getInstance().getString("csv.table.first_col"));
        TableColumn lastCol = new TableColumn(I18n.getInstance().getString("csv.table.second_col"));
        firstCol.prefWidthProperty().bind(placeholderTree.widthProperty().multiply(0.5));
        lastCol.prefWidthProperty().bind(placeholderTree.widthProperty().multiply(0.5));
        placeholderTree.getColumns().addAll(firstCol, lastCol);

        Layouts.setAnchor(placeholderTree, 0);
        tableRootPane.getChildren().setAll(placeholderTree);

        return tableRootPane;
    }

    public void showNotification(String text, Node graphic) {
        notificationPane.show(text, graphic);
    }

    public void showNotification(String text) {
        notificationPane.show(text);
    }

    // -------------------------------------------------------------------------
    // updateTree — rebuild or refresh the preview table
    // -------------------------------------------------------------------------

    private void updateTree(boolean rebuildColumns) {
        if (_importFile != null) {
            Platform.runLater(() -> {
                if (table == null || rebuildColumns) {
                    final ImportParser parser = parseFile();
                    if (parser != null) {
                        table = new CSVTable(_ds, parser, preselectTarget);
                        tableRootPane.getChildren().setAll(table);
                        Layouts.setAnchor(table, 0);
                    }
                } else {
                    Platform.runLater(() -> table.refreshTable());
                }
            });
        }
    }

    // -------------------------------------------------------------------------
    // Parse the current file into an ImportParser
    // -------------------------------------------------------------------------

    private ImportParser parseFile() {
        if (_importFile == null) return null;
        if (currentFileType == FileType.XLSX) {
            XLSXParser xlsxParser = new XLSXParser(_importFile, selectedSheetIndex, getStartLine());
            // Populate sheet combo with names from this parse
            List<String> names = xlsxParser.getSheetNames();
            if (sheetComboBox != null) {
                Platform.runLater(() -> {
                    sheetComboBox.getItems().setAll(names);
                    if (selectedSheetIndex < names.size()) {
                        sheetComboBox.getSelectionModel().select(selectedSheetIndex);
                    }
                });
            }
            return xlsxParser;
        } else {
            return new CSVParser(_importFile, getEncloser(), getSeperator(), getStartLine(), charset);
        }
    }

    // -------------------------------------------------------------------------
    // File options panel
    // -------------------------------------------------------------------------

    private Node buildFileOptions() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(0, 10, 0, LEFT_PADDING));
        gp.setHgap(4);
        gp.setVgap(4);

        Label fileL = new Label(I18n.getInstance().getString("csv.file"));
        Label formatL = new Label(I18n.getInstance().getString("csv.format"));
        Label charSetL = new Label(I18n.getInstance().getString("csv.charset"));
        Label fromRow = new Label(I18n.getInstance().getString("csv.from_rowFrom"));
        Label customNoteLabel = new Label(I18n.getInstance().getString("csv.customnote"));
        final Label fileNameL = new Label();

        customNoteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                customNoteString = newValue;
            }
        });

        ObservableList<Charset> options = FXCollections.observableArrayList(Charset.availableCharsets().values());

        Callback<ListView<Charset>, ListCell<Charset>> cellFactory = new Callback<ListView<Charset>, ListCell<Charset>>() {
            @Override
            public ListCell<Charset> call(ListView<Charset> param) {
                return new ListCell<Charset>() {
                    {
                        super.setPrefWidth(260);
                    }

                    @Override
                    public void updateItem(Charset item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.displayName(I18n.getInstance().getLocale()));
                            setGraphic(null);
                        }
                    }
                };
            }
        };

        final JFXComboBox<Charset> charsetBox = new JFXComboBox<>(options);
        charsetBox.setCellFactory(cellFactory);
        charsetBox.setButtonCell(cellFactory.call(null));
        charsetBox.valueProperty().addListener(new ChangeListener<Charset>() {
            @Override
            public void changed(ObservableValue<? extends Charset> observable, Charset oldValue, Charset newValue) {
                charset = newValue;
                if (currentFileType == FileType.CSV) {
                    updateTree(true);
                }
            }
        });
        charsetBox.getSelectionModel().select(Charset.defaultCharset());

        formatOptions = FXCollections.observableArrayList();
        for (Format format : Format.values()) {
            formatOptions.add(format.name());
        }
        final JFXComboBox<String> formats = new JFXComboBox<>(formatOptions);
        formats.getSelectionModel().selectFirst();

        fileButton.setPrefWidth(200);
        customNoteField.setPrefWidth(200);
        charsetBox.setPrefWidth(200);
        formats.setPrefWidth(200);
        charsetBox.setMaxWidth(1000);
        formats.setMaxWidth(1000);

        headerRowCount.setMinHeight(22);
        headerRowCount.numberProperty().addListener((observable, oldValue, newValue) -> {
            if (table != null && newValue.intValue() != table.getParser().getHeaderLines()) {
                table.getParser().setHeaderLines(newValue.intValue());
                updateTree(false);
            }
        });

        automatic.setDisable(true);
        automatic.setOnAction(t -> {
            if (_importFile != null && currentFileType == FileType.CSV) {
                try {
                    CSVAnalyser csvAnalyser = new CSVAnalyser(_importFile);
                    setEncloser(csvAnalyser.getEnclosed());
                    setSeparator(csvAnalyser.getSeparator());
                    formats.getSelectionModel().select(Format.Custom.name());
                    if (table != null && table.getParser() instanceof CSVParser) {
                        CSVParser csvParser = (CSVParser) table.getParser();
                        csvParser.setEnclosed(csvAnalyser.getEnclosed());
                        csvParser.setSeparator(csvAnalyser.getSeparator());
                    }
                    updateTree(true);
                } catch (Exception ex) {
                    logger.info("Error while analysing csv: " + ex);
                }
            }
        });

        fileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();
                if (JEConfig.getLastPath() != null) {
                    File file = JEConfig.getLastPath();
                    if (file.exists() && file.canRead()) {
                        fileChooser.setInitialDirectory(file);
                    }
                }

                FileChooser.ExtensionFilter combinedFilter = new FileChooser.ExtensionFilter(
                        "CSV and XLSX files (*.csv, *.xlsx, *.xls)", "*.csv", "*.xlsx", "*.xls");
                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                FileChooser.ExtensionFilter xlsxFilter = new FileChooser.ExtensionFilter("XLSX files (*.xlsx, *.xls)", "*.xlsx", "*.xls");
                FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files", "*");
                fileChooser.getExtensionFilters().addAll(combinedFilter, csvFilter, xlsxFilter, allFilter);

                final File file = fileChooser.showOpenDialog(JEConfig.getStage());
                if (file != null && file.canRead()) {
                    Platform.runLater(() -> {
                        try {
                            JEConfig.setLastPath(file);
                            logger.info("file: " + file);
                            fileNameL.setText(file.getName());
                            _importFile = file;
                            selectedSheetIndex = 0;

                            FileType detected = detectFileType(file);
                            applyFileType(detected);

                            if (detected == FileType.CSV) {
                                CSVAnalyser analyse = new CSVAnalyser(_importFile);
                                setEncloser(analyse.getEnclosed());
                                setSeparator(analyse.getSeparator());
                                formats.getSelectionModel().select(Format.Custom.name());
                            }

                            updateTree(true);
                        } catch (Exception ex) {
                            logger.fatal(ex);
                        }
                    });
                }
            }
        });

        int x = 0;

        gp.add(fileL, 0, ++x);
        gp.add(fileButton, 1, x);
        gp.add(fileNameL, 2, x);

        gp.add(customNoteLabel, 0, ++x);
        gp.add(customNoteField, 1, x);

        gp.add(charSetL, 0, ++x);
        gp.add(charsetBox, 1, x);

        gp.add(fromRow, 0, ++x);
        gp.add(headerRowCount, 1, x);

        gp.add(automatic, 0, ++x, 2, 1);

        GridPane.setHgrow(buildTitle(I18n.getInstance().getString("csv.tab.title.field_options")), Priority.ALWAYS);

        updateTree(true);

        return gp;
    }

    // -------------------------------------------------------------------------
    // Separator helpers
    // -------------------------------------------------------------------------

    private void setSeparator(String sep) {
        separator = sep;
        switch (sep) {
            case ";":
                separatorComboBox.getSelectionModel().select(Separator.Semicolon);
                break;
            case ",":
                separatorComboBox.getSelectionModel().select(Separator.Comma);
                break;
            case " ":
                separatorComboBox.getSelectionModel().select(Separator.Space);
                break;
            case "\t":
                separatorComboBox.getSelectionModel().select(Separator.Tab);
                break;
            default:
                separatorComboBox.getSelectionModel().select(Separator.OTHER);
                otherSeparatorField.setText(sep);
                break;
        }
    }

    private int getStartLine() {
        return headerRowCount.getNumber().intValue();
    }

    private String getEncloser() {
        return _encloser;
    }

    private void setEncloser(String endclosed) {
        _encloser = endclosed;
        switch (endclosed) {
            case "\"":
                enclosedComboBox.getSelectionModel().select(Enclosed.Ditto);
                break;
            case "'":
                enclosedComboBox.getSelectionModel().select(Enclosed.Apostrophe);
                break;
            case "`":
                enclosedComboBox.getSelectionModel().select(Enclosed.Gravis);
                break;
            case "":
                enclosedComboBox.getSelectionModel().select(Enclosed.NONE);
                break;
            default:
                enclosedComboBox.getSelectionModel().select(Enclosed.OTHER);
                otherEnclosedField.setText(_encloser);
                break;
        }
    }

    private void updateEnclosed() {
        switch (enclosedComboBox.getValue()) {
            case OTHER:
                _encloser = otherEnclosedField.getText();
                break;
            case NONE:
                _encloser = "";
                break;
            case Ditto:
                _encloser = "\"";
                break;
            case Gravis:
                _encloser = "`";
                break;
        }
        updateTree(true);
    }

    private String getSeperator() {
        return separator;
    }

    private void updateSeparator() {
        switch (separatorComboBox.getValue()) {
            case OTHER:
                separator = otherSeparatorField.getText();
                break;
            case Space:
                separator = " ";
                break;
            case Semicolon:
                separator = ";";
                break;
            case Comma:
                separator = ",";
                break;
            case Tab:
                separator = "\t";
                break;
        }
        updateTree(true);
    }

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum Format {
        Default, ARA01, Custom
    }

    enum Enclosed {
        NONE, Apostrophe, Ditto, Gravis, OTHER
    }

    enum Separator {
        Semicolon, Comma, Space, Tab, OTHER
    }

    public enum Response {
        OK, CANCEL
    }
}
