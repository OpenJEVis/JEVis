package org.jevis.jeconfig.plugin.reports;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.report.JEVisFileWithSample;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.resource.PDFModel;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.joda.time.DateTime;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(ReportPlugin.class);
    public static String PLUGIN_NAME = "Report Plugin";
    public static String REPORT_CLASS = "Report";
    public static String REPORT_DIR_CLASS = "Report Directory";
    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ObjectRelations objectRelations;
    private final ToolBar toolBar = new ToolBar();
    private boolean initialized = false;
    private final ListView<JEVisObject> listView = new ListView<>();
    private final Pagination pagination = new Pagination();
    private final ComboBox<JEVisFileWithSample> fileComboBox = new ComboBox<>(FXCollections.observableArrayList());
    private final Map<JEVisFile, JEVisSample> sampleMap = new HashMap<>();
    private List<JEVisObject> disabledItemList;
    private final TextField filterInput = new TextField();
    private final int iconSize = 20;
    private boolean multipleDirectories;
    private final PDFModel model = new PDFModel();
    private final SimpleDoubleProperty zoomFactor = new SimpleDoubleProperty(0.3);
    //private final ImageView rightImage = JEConfig.getImage("right.png", 20, 20);
    //private final ImageView leftImage = JEConfig.getImage("left.png", 20, 20);
    private final ToggleButton prevButton = new ToggleButton("", JEConfig.getImage("arrow_left.png", iconSize, iconSize));
    private final ToggleButton nextButton = new ToggleButton("", JEConfig.getImage("arrow_right.png", iconSize, iconSize));
    private boolean newReport = false;
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();

    public ReportPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;

        this.filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

        VBox view = new VBox(pagination);
        view.setFillWidth(true);
        VBox.setVgrow(pagination, Priority.ALWAYS);
        view.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() != oldValue.doubleValue()) {
                pagination.setPrefWidth(newValue.doubleValue());
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
            if (!newReport && !newValue.equals(oldValue)) {
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

        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.HORIZONTAL);
        sp.setId("mainsplitpane");
        sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        VBox vBox = new VBox(filterInput, listView);
        sp.getItems().setAll(vBox, view);
        this.borderPane.setCenter(sp);

        VBox.setVgrow(listView, Priority.ALWAYS);

        sp.setDividerPositions(0.3);

        initToolBar();

        this.objectRelations = new ObjectRelations(ds);

    }

    public HBox createPage(int pageIndex) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        ImageView image = model.getImage(pageIndex, zoomFactor.get());
        Group group = new Group(image);
        BorderPane bp = new BorderPane(group);
        bp.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(bp);
        vBox.getChildren().add(scrollPane);
        hBox.getChildren().add(vBox);

        return hBox;
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> {
            initialized = false;

            final String loading = I18n.getInstance().getString("plugin.reports.reload.progress.message");
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            updateMessage(loading);
                            try {
                                ds.clearCache();
                                ds.preload();

                                getContentNode();
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                }
            };
            ProgressDialog pd = new ProgressDialog(service);
            pd.setHeaderText(I18n.getInstance().getString("plugin.reports.reload.progress.header"));
            pd.setTitle(I18n.getInstance().getString("plugin.reports.reload.progress.title"));
            pd.getDialogPane().setContent(null);

            service.start();

        });
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

            JEVisFile pdfFile = fileComboBox.getSelectionModel().getSelectedItem().getPdfFile();
            fileChooser.setInitialFileName(pdfFile.getFilename());
            File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
            if (selectedFile != null) {
                JEConfig.setLastPath(selectedFile);
                try {
                    pdfFile.saveToFile(selectedFile);
                } catch (IOException e) {
                    logger.error("Could not save pdf file", e);
                }
            }
        });

        ToggleButton xlsxButton = new ToggleButton("", JEConfig.getImage("xlsx_315594.png", iconSize, iconSize));
        Tooltip xlsxTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.xlsx"));
        xlsxButton.setTooltip(xlsxTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(xlsxButton);

        xlsxButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("XLSX File Destination");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
            fileChooser.getExtensionFilters().addAll(pdfFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter);

            JEVisFile xlsxFile = fileComboBox.getSelectionModel().getSelectedItem().getXlsxFile();
            fileChooser.setInitialFileName(xlsxFile.getFilename());
            File selectedFile = fileChooser.showSaveDialog(JEConfig.getStage());
            if (selectedFile != null) {
                JEConfig.setLastPath(selectedFile);
                try {
                    xlsxFile.saveToFile(selectedFile);
                } catch (IOException e) {
                    logger.error("Could not save xlsx file", e);
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
                PDDocument document = PDDocument.load(fileComboBox.getSelectionModel().getSelectedItem().getPdfFile().getBytes());
                printerJob.setPageable(new PDFPageable(document));
                if (printerJob.printDialog()) {
                    printerJob.print();
                }
            } catch (IOException | PrinterException e) {
                e.printStackTrace();
            }
        });

        ToggleButton zoomIn = new ToggleButton("", JEConfig.getImage("zoomIn_32.png", this.iconSize, this.iconSize));
        ToggleButton zoomOut = new ToggleButton("", JEConfig.getImage("zoomOut_32.png", this.iconSize, this.iconSize));

        zoomIn.setOnAction(event -> zoomFactor.set(zoomFactor.get() + 0.05));
        zoomOut.setOnAction(event -> zoomFactor.set(zoomFactor.get() - 0.05));

        zoomIn.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.zoomin")));
        zoomOut.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.zoomout")));

        Label labelDateTimeComboBox = new Label(I18n.getInstance().getString("plugin.reports.selectionbox.label"));
        labelDateTimeComboBox.setAlignment(Pos.CENTER_LEFT);

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
        fileComboBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.datelist")));

        prevButton.setOnMouseClicked(event -> {
            int i = fileComboBox.getSelectionModel().getSelectedIndex();
            if (i > 0) {
                fileComboBox.getSelectionModel().select(i - 1);
            }
        });

        nextButton.setOnMouseClicked(event -> {
            int i = fileComboBox.getSelectionModel().getSelectedIndex();
            if (i < sampleMap.size()) {
                fileComboBox.getSelectionModel().select(i + 1);
            }
        });

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(prevButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(nextButton);
        prevButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.prev")));
        nextButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.next")));

        fileComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) {
                try {
                    byte[] bytesFromSampleMap = newValue.getPdfFile().getBytes();
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


        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);

        toolBar.getItems().setAll(reload, new Separator(),
                pdfButton, xlsxButton, new Separator(),
                printButton, new Separator(),
                zoomIn, zoomOut, new Separator(),
                labelDateTimeComboBox, prevButton, fileComboBox, nextButton);

        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        JEVisHelp.getInstance().addHelpItems(this.getClass().getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());

    }

    @Override
    public String getClassName() {
        return "Report Plugin";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public StringProperty nameProperty() {
        return null;
    }

    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public String getToolTip() {
        return I18n.getInstance().getString("plugin.reports.tooltip");
    }

    @Override
    public StringProperty uuidProperty() {
        return null;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        return false;
    }

    @Override
    public Node getToolbar() {
        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {

    }

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        if (!initialized) {
            init();
        }
        return borderPane;
    }

    private void init() {

        ObservableList<JEVisObject> reports = FXCollections.observableArrayList(getAllReports());
        AlphanumComparator ac = new AlphanumComparator();
        reports.sort((o1, o2) -> {
            String name1 = objectRelations.getObjectPath(o1) + o1.getName();
            String name2 = objectRelations.getObjectPath(o2) + o2.getName();
            return ac.compare(name1, name2);
        });

        FilteredList<JEVisObject> filteredData = new FilteredList<>(reports, s -> true);

        filterInput.setPadding(new Insets(10));

        filterInput.textProperty().addListener(obs -> {
            String filter = filterInput.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredData.setPredicate(s -> {
                        boolean match = false;
                        String string = (objectRelations.getObjectPath(s) + s.getName()).toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredData.setPredicate(s -> (objectRelations.getObjectPath(s) + s.getName()).toLowerCase().contains(filter.toLowerCase()));
                }
            }
        });

        listView.setItems(filteredData);
        listView.setPrefWidth(250);
        disabledItemList = getDisabledItems(reports);
        setupCellFactory(listView);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                loadReport(newValue);
            }
        });

        initialized = true;
    }

    private List<JEVisObject> getDisabledItems(ObservableList<JEVisObject> reports) {
        List<JEVisObject> list = new ArrayList<>();
        for (JEVisObject reportObject : reports) {
            JEVisAttribute lastReportPDFAttribute = null;
            try {
                lastReportPDFAttribute = reportObject.getAttribute("Last Report PDF");
            } catch (JEVisException e) {
                logger.error("Could not get 'Last Report' Attribute from object {}:{}", reportObject.getName(), reportObject.getID(), e);
            }

            if (lastReportPDFAttribute != null) {
                if (!lastReportPDFAttribute.hasSample()) {
                    list.add(reportObject);
                }
            }
        }

        return list;
    }

    private void loadReport(JEVisObject reportObject) {

        Task loadOtherFilesInBackground = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    JEVisAttribute lastReportPDFAttribute = null;
                    JEVisAttribute lastReportXLSXAttribute = null;
                    try {
                        lastReportPDFAttribute = reportObject.getAttribute("Last Report PDF");
                        lastReportXLSXAttribute = reportObject.getAttribute("Last Report");
                    } catch (JEVisException e) {
                        logger.error("Could not get 'Last Report' Attribute from object {}:{}", reportObject.getName(), reportObject.getID(), e);
                    }

                    if (lastReportPDFAttribute != null && lastReportXLSXAttribute != null) {
                        sampleMap.clear();
                        Platform.runLater(() -> fileComboBox.getItems().clear());
                        List<JEVisSample> allPDFSamples = lastReportPDFAttribute.getAllSamples();
                        List<JEVisSample> allXLSXSamples = lastReportXLSXAttribute.getAllSamples();
                        if (allPDFSamples.size() > 0) {
                            JEVisFile lastSampleValueAsFile = allPDFSamples.get(allPDFSamples.size() - 1).getValueAsFile();
                            Task loadLastReport = new Task() {
                                @Override
                                protected Object call() throws Exception {
                                    try {
                                        newReport = true;
                                        byte[] bytes = lastSampleValueAsFile.getBytes();
                                        model.setBytes(bytes);

                                        Platform.runLater(() -> {
                                            pagination.setPageCount(model.numPages());
                                            zoomFactor.set(0.3);
                                            pagination.setPageFactory((Integer pageIndex) -> {
                                                if (pageIndex >= model.numPages()) {
                                                    return null;
                                                } else {
                                                    return createPage(pageIndex);
                                                }
                                            });
                                        });
                                        newReport = false;
                                    } catch (Exception e) {
                                        logger.error("Could not load latest report for {}:{}", reportObject.getName(), reportObject.getID(), e);
                                    }
                                    return null;
                                }
                            };
                            JEConfig.getStatusBar().addTask(ReportPlugin.class.getName(), loadLastReport, ReportPlugin.this.getIcon().getImage(), true);

                            for (JEVisSample pdfSample : allPDFSamples) {
                                try {
                                    JEVisFile pdfFile = pdfSample.getValueAsFile();

                                    JEVisFile xlsxFile = null;
                                    DateTime pdfTSLower = pdfSample.getTimestamp().minusMinutes(1);
                                    DateTime pdfTSUpper = pdfSample.getTimestamp().plusMinutes(1);
                                    for (JEVisSample sample : allXLSXSamples) {
                                        if (sample.getTimestamp().isAfter(pdfTSLower) && sample.getTimestamp().isBefore(pdfTSUpper)) {
                                            xlsxFile = sample.getValueAsFile();
                                            break;
                                        }
                                    }

                                    JEVisFileWithSample jeVisFileWithSample = new JEVisFileWithSample(pdfSample, pdfFile, xlsxFile);

                                    Platform.runLater(() -> {
                                        fileComboBox.getItems().add(jeVisFileWithSample);
                                        fileComboBox.getItems().sort((o1, o2) -> alphanumComparator.compare(o2.getPdfFile().getFilename(), o1.getPdfFile().getFilename()));
                                    });
                                    sampleMap.put(pdfFile, pdfSample);

                                } catch (JEVisException e) {
                                    logger.error("Could not add date to date list.");
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

        loadOtherFilesInBackground.setOnSucceeded(event -> Platform.runLater(() -> fileComboBox.getSelectionModel().selectFirst()));

        JEConfig.getStatusBar().addTask(PDFViewerDialog.class.getName(), loadOtherFilesInBackground, this.getIcon().getImage(), true);
    }


    private void setupCellFactory(ListView<JEVisObject> listView) {
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            if (disabledItemList.contains(obj)) {
                                setTextFill(Color.LIGHTGRAY);
                                setDisable(true);
                            } else {
                                setTextFill(Color.BLACK);
                                setDisable(false);
                            }

                            if (!getMultipleDirectories())
                                setText(obj.getName());
                            else {
                                String prefix = objectRelations.getObjectPath(obj);

                                setText(prefix + obj.getName());
                            }
                        }

                    }
                };
            }
        };

        listView.setCellFactory(cellFactory);
    }

    private List<JEVisObject> getAllReports() {
        List<JEVisObject> list = new ArrayList<>();
        JEVisClass reportClass = null;
        JEVisClass reportDirClass = null;
        try {
            reportClass = ds.getJEVisClass(REPORT_CLASS);
            list = ds.getObjects(reportClass, true);

            reportDirClass = ds.getJEVisClass(REPORT_DIR_CLASS);
            if (ds.getObjects(reportDirClass, true).size() > 1) {
                setMultipleDirectories(true);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("Report.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 3;
    }

    public boolean getMultipleDirectories() {
        return multipleDirectories;
    }

    public void setMultipleDirectories(boolean multipleDirectories) {
        this.multipleDirectories = multipleDirectories;
    }
}
