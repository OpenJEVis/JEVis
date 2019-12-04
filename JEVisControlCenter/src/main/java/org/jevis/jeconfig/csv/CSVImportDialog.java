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
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.Layouts;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.jevis.jeconfig.tool.ScreenSize;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVImportDialog {
    private static final Logger logger = LogManager.getLogger(CSVImportDialog.class);

    public static String ICON = "1403727005_gnome-mime-application-vnd.lotus-1-2-3.png";

    private String _encloser = "";
    private String separator = "";

    private double LEFT_PADDING = 30;

    final Button ok = new Button(I18n.getInstance().getString("csv.ok"));
    final Button automatic = new Button(I18n.getInstance().getString("csv.automatic"));//, JEConfig.getImage("1403018303_Refresh.png", 15, 15));
    final Button fileButton = new Button(I18n.getInstance().getString("csv.file_select"));
    final Button saveFormat = new Button(I18n.getInstance().getString("csv.save_formate"));
    final NumberSpinner headerRowCount = new NumberSpinner(BigDecimal.valueOf(0), BigDecimal.valueOf(1));

    enum Seperator {
        Semicolon, Comma, Space, Tab, OTHER
    }

    private Node buildSeparatorPane() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(0, 10, 0, LEFT_PADDING));

        gp.setHgap(4);
        gp.setVgap(5);

        Label sepL = new Label(I18n.getInstance().getString("csv.separator.column"));
        Label sepTextL = new Label(I18n.getInstance().getString("csv.separator.text"));


        Callback<ListView<Seperator>, ListCell<Seperator>> seperatorCellFactory = new Callback<ListView<Seperator>, ListCell<Seperator>>() {
            @Override
            public ListCell<Seperator> call(ListView<Seperator> param) {
                return new ListCell<Seperator>() {
                    @Override
                    protected void updateItem(Seperator item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            String localText = "";
                            switch (item) {
                                case Tab:
                                    localText = I18n.getInstance().getString("csv.seperators.tab");
                                    break;
                                case Comma:
                                    localText = I18n.getInstance().getString("csv.seperators.comma");
                                    break;
                                case OTHER:
                                    localText = I18n.getInstance().getString("csv.seperators.other");
                                    break;
                                case Semicolon:
                                    localText = I18n.getInstance().getString("csv.seperators.semi");
                                    break;
                                case Space:
                                    localText = I18n.getInstance().getString("csv.seperators.space");
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
        seperatorComboBox.setCellFactory(seperatorCellFactory);
        seperatorComboBox.setButtonCell(seperatorCellFactory.call(null));
        otherEnclosedField.setDisable(true);
        otherSeperatorField.setDisable(true);
        seperatorComboBox.getSelectionModel().select(Seperator.Semicolon);
        seperatorComboBox.setOnAction(event -> {
            if (seperatorComboBox.getValue() == Seperator.OTHER) {
                otherSeperatorField.setDisable(false);
                otherSeperatorField.requestFocus();
            } else {
                otherSeperatorField.setDisable(true);
                updateSeperator();
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

        otherSeperatorField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSeperator();
        });
        otherEnclosedField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateEnclosed();
        });

        enclosedComboBox.setMinWidth(200);
        seperatorComboBox.setMinWidth(200);
        otherSeperatorField.setMinWidth(200);
        otherEnclosedField.setMinWidth(200);

        HBox otherBox = new HBox(5);
        otherBox.setAlignment(Pos.CENTER_LEFT);
        otherBox.getChildren().setAll(otherSeperatorField);

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

        cSep.getChildren().setAll(seperatorComboBox, otherBox);
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

    private ComboBox<Seperator> seperatorComboBox = new ComboBox<>(FXCollections.observableArrayList(Seperator.values()));
    private ComboBox<Enclosed> enclosedComboBox = new ComboBox<>(FXCollections.observableArrayList(Enclosed.values()));

    final ToggleGroup textDiGroup = new ToggleGroup();
    ObservableList<String> formatOptions;

    private NotificationPane notificationPane = new NotificationPane();
    final AnchorPane tableRootPane = new AnchorPane();

    private TextField otherSeperatorField = new TextField();
    private TextField otherEnclosedField = new TextField();
    private TextField customNoteField = new TextField();
    private File _csvFile;
    private JEVisDataSource _ds;
    private CSVTable table;
    private Stage stage;
    private Charset charset = Charset.defaultCharset();
    private String customNoteString = "";


    public Response show(Stage owner, JEVisDataSource ds) {
        stage = new Stage();
        _ds = ds;
        BorderPane root = new BorderPane();
        notificationPane.setContent(root);
        Layouts.setAnchor(notificationPane, 0);
        Scene scene = new Scene(notificationPane);

        stage.setTitle(I18n.getInstance().getString("csv.title"));
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);
        stage.setScene(scene);
//        stage.setMaximized(true);
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(true);

        BorderPane header = new BorderPane();
        header.setStyle("-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);");
//        header.setPadding(new Insets(10, 10, 10, 10));

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
        header.setBottom(new Separator(Orientation.HORIZONTAL));

        HBox buttonPanel = new HBox(8);

        ok.setDefaultButton(true);
        saveFormat.setDisable(true);//Disabled as long its not working

        Button cancel = new Button(I18n.getInstance().getString("csv.cancel"));
        cancel.setCancelButton(true);

        buttonPanel.getChildren().setAll(ok, cancel);
        buttonPanel.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPanel.setPadding(new Insets(5));


        Node filePane = buildFileOptions();
        Node separatorPane = buildSeparatorPane();
        Node tablePane = buildTablePane();
        Region spacer = new Region();
        GridPane content = new GridPane();

        content.addRow(0, buildTitle(I18n.getInstance().getString("csv.tab.title.file_options")));
        content.addRow(1, filePane);
        content.addRow(2, buildTitle(I18n.getInstance().getString("csv.tab.title.seperator_options")));
        content.addRow(3, separatorPane);
        content.addRow(4, buildTitle(I18n.getInstance().getString("csv.tab.title.field_options")));
        content.addRow(5, tablePane);

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
//            root.setDisable(true);
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

//        stage.sizeToScene();
        stage.setHeight(ScreenSize.fitScreenHeight(900));
        stage.setWidth(ScreenSize.fitScreenWidth(900));
        stage.showAndWait();

        return response;
    }

    public void showNotification(String text, Node graphic) {
//        notificationPane.setShowFromTop(false);
        notificationPane.show(text, graphic);
    }

    public void showNotification(String text) {
//        notificationPane.setShowFromTop(false);
        notificationPane.show(text);
    }

    private void updateTree(boolean rebuildColumns) {
        if (_csvFile != null) {
            Platform.runLater(() -> {

                if (table == null || rebuildColumns) {
                    final CSVParser parser = parseCSV();
                    table = new CSVTable(_ds, parser);
                    tableRootPane.getChildren().setAll(table);
                    Layouts.setAnchor(table, 0);
                } else {
                    Platform.runLater(() -> {
                        table.refreshTable();
                    });
                }
            });
        }
    }

    private Response response = Response.CANCEL;

    private Node buildTablePane() {
        TableView placeholderTree = new TableView();
        TableColumn firstNameCol = new TableColumn(I18n.getInstance().getString("csv.table.first_col"));
        TableColumn lastNameCol = new TableColumn(I18n.getInstance().getString("csv.table.second_col"));
        firstNameCol.prefWidthProperty().bind(placeholderTree.widthProperty().multiply(0.5));
        lastNameCol.prefWidthProperty().bind(placeholderTree.widthProperty().multiply(0.5));
        placeholderTree.getColumns().addAll(firstNameCol, lastNameCol);

        Layouts.setAnchor(placeholderTree, 0);
        tableRootPane.getChildren().setAll(placeholderTree);

        return tableRootPane;
    }

    private CSVParser parseCSV() {
        return new CSVParser(_csvFile, getEncloser(), getSeperator(), getStartLine(), charset);
    }

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

        final ComboBox<Charset> charsetBox = new ComboBox<>(options);
        charsetBox.setCellFactory(cellFactory);
        charsetBox.setButtonCell(cellFactory.call(null));
        charsetBox.valueProperty().addListener(new ChangeListener<Charset>() {
            @Override
            public void changed(ObservableValue<? extends Charset> observable, Charset oldValue, Charset newValue) {
                charset = newValue;
                updateTree(true);
            }
        });
        charsetBox.getSelectionModel().select(Charset.defaultCharset());

        formatOptions = FXCollections.observableArrayList();
        for (Format format : Format.values()) {
            formatOptions.add(format.name());
        }

//        formatOptions = FXCollections.observableArrayList("MS Office, ARA01, Custom");
        final ComboBox<String> formats = new ComboBox<>(formatOptions);
        formats.getSelectionModel().selectFirst();

        Node title = buildTitle(I18n.getInstance().getString("csv.tab.title.field_options"));

        fileButton.setPrefWidth(200);
        customNoteField.setPrefWidth(200);
        charsetBox.setPrefWidth(200);
        formats.setPrefWidth(200);
        charsetBox.setMaxWidth(1000);
        formats.setMaxWidth(1000);

        headerRowCount.setMinHeight(22);
        headerRowCount.numberProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != table.getParser().getHeader()) {
                table.getParser().setHeader(newValue.intValue());
                updateTree(false);
            }
        });

        automatic.setDisable(true);
        automatic.setOnAction(t -> {
            if (_csvFile != null) {
                try {
                    CSVAnalyser csvAnalyser = new CSVAnalyser(_csvFile);
                    setEncloser(csvAnalyser.getEnclosed());
                    setSeperator(csvAnalyser.getSeparator());
                    formats.getSelectionModel().select(Format.Custom.name());
                    table.getParser().setEnclosed(csvAnalyser.getEnclosed());
                    table.getParser().setSeparator(csvAnalyser.getSeparator());
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
//                    logger.info("Last Path: " + JEConfig.getLastPath().getParentFile());
                    File file = JEConfig.getLastPath();
                    if (file.exists() && file.canRead()) {
                        fileChooser.setInitialDirectory(file);
                    }
                }

                FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
                FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files ", "*");
                fileChooser.getExtensionFilters().addAll(csvFilter, allFilter);

                final File file = fileChooser.showOpenDialog(JEConfig.getStage());
                if (file != null && file.canRead()) {

                    Platform.runLater(() -> {
                        try {
                            JEConfig.setLastPath(file);
                            logger.info("file: " + file);

                            fileNameL.setText(file.getName());// + System.getProperty("file.separator") + file.getName());

                            openFile(file);
                            automatic.setDisable(false);
                            CSVAnalyser analyse = new CSVAnalyser(_csvFile);

                            setEncloser(analyse.getEnclosed());
                            setSeperator(analyse.getSeparator());
                            formats.getSelectionModel().select(Format.Custom.name());

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

        GridPane.setHgrow(title, Priority.ALWAYS);

        updateTree(true);

        return gp;

    }

    public enum Format {

        Default, ARA01, Custom
    }

    enum Enclosed {
        NONE, Apostrophe, Ditto, Gravis, OTHER
    }


    private Node buildTitle(String name) {
        HBox titelBox = new HBox(2);
        titelBox.setPadding(new Insets(8));
        Separator titelSep = new Separator(Orientation.HORIZONTAL);
        titelSep.setMaxWidth(Double.MAX_VALUE);
        Label title = new Label(name);
//        titelBox.getChildren().setAll(titelSep);
        titelBox.getChildren().setAll(title, titelSep);
        HBox.setHgrow(titelSep, Priority.NEVER);
        HBox.setHgrow(titelSep, Priority.ALWAYS);
        titelBox.setAlignment(Pos.CENTER_LEFT);
        titelBox.setPrefWidth(1024);

        return titelBox;
    }

    private void parseFile() {
    }

    private int getStartLine() {
        return headerRowCount.getNumber().intValue();
    }

    private String getEncloser() {
        return _encloser;

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

    private String getSeperator() {
        return separator;
    }

    private void setSeperator(String sep) {
        separator = sep;

        switch (sep) {
            case ";":
                seperatorComboBox.getSelectionModel().select(Seperator.Semicolon);
                break;
            case ",":
                seperatorComboBox.getSelectionModel().select(Seperator.Comma);
                break;
            case " ":
                seperatorComboBox.getSelectionModel().select(Seperator.Space);
                break;
            case "\t":
                seperatorComboBox.getSelectionModel().select(Seperator.Tab);
                break;
            default:
                seperatorComboBox.getSelectionModel().select(Seperator.OTHER);
                otherSeperatorField.setText(sep);
                break;
        }
    }

    private void updateSeperator() {
        switch (seperatorComboBox.getValue()) {
            case OTHER:
                separator = otherSeperatorField.getText();
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

//        if (sepGroup.getSelectedToggle() != null) {
//            RadioButton selecedt = (RadioButton) sepGroup.getSelectedToggle();
//            if (selecedt.equals(semicolon)) {
//                separator = ";";
//            } else if (selecedt.equals(comma)) {
//                separator = ",";
//            } else if (selecedt.equals(space)) {
//                separator = " ";
//            } else if (selecedt.equals(tab)) {
//                separator = "\t";
//            } else if (selecedt.equals(otherLineSep)) {
//
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        otherColumnF.requestFocus();
//                    }
//                });
//                if (!otherColumnF.getText().isEmpty()) {
//                    separator = otherColumnF.getText();
//                } else {
//                    return;
//                }
//
//            }
//            updateTree(true);
//        }

    }

    public enum Response {

        OK, CANCEL
    }

    private void openFile(File file) {
        _csvFile = file;
    }


}
