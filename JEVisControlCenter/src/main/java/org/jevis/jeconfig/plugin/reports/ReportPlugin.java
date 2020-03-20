package org.jevis.jeconfig.plugin.reports;

import com.google.common.util.concurrent.AtomicDouble;
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
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.resource.PDFModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
    private ListView<JEVisObject> listView = new ListView<>();
    Pagination pagination = new Pagination();
    private ComboBox<DateTime> dateTimeComboBox;
    private List<JEVisObject> disabledItemList;
    private HBox hBox = new HBox();
    private TextField filterInput = new TextField();
    private final int iconSize = 20;
    private boolean multipleDirectories;
    private PDFModel model = new PDFModel();
    private AtomicDouble zoomFactor = new AtomicDouble(1);

    public ReportPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;

        this.hBox.setPadding(new Insets(4, 4, 4, 4));
        this.hBox.setSpacing(4);
        this.filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));

        ScrollPane scrollPane = new ScrollPane(pagination);
        VBox view = new VBox(hBox, scrollPane);
        view.setFillWidth(true);
        VBox.setVgrow(hBox, Priority.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
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

                    if (deltaY < 0) {
                        zoomFactor.set(zoomFactor.get() - 0.05);
                    } else {
                        zoomFactor.set(zoomFactor.get() + 0.05);
                    }
                    pagination.setPageFactory(index -> new Group(model.getImage(index, zoomFactor.get())));
                    event.consume();
                }
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

        Separator sep1 = new Separator(Orientation.VERTICAL);

        ToggleButton pdfButton = new ToggleButton("", JEConfig.getImage("pdf_24_2133056.png", iconSize, iconSize));
        Tooltip pdfTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.pdf"));
        pdfButton.setTooltip(pdfTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(pdfButton);

        pdfButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("PDF File Destination");
            DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files (*.pdf)", ".pdf");
            fileChooser.getExtensionFilters().addAll(pdfFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter);

            JEVisObject selectedItem = listView.getSelectionModel().getSelectedItem();
            fileChooser.setInitialFileName(selectedItem.getName() + fmtDate.print(new DateTime()));
            File file = fileChooser.showSaveDialog(JEConfig.getStage());
            if (file != null) {
                File destinationFile = new File(file + fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
                try {
                    JEVisAttribute last_report_pdf = selectedItem.getAttribute("Last Report PDF");
                    DateTime dateTime = dateTimeComboBox.getSelectionModel().getSelectedItem();
                    List<JEVisSample> samples = last_report_pdf.getSamples(dateTime, dateTime);
                    samples.get(0).getValueAsFile().saveToFile(destinationFile);
                } catch (JEVisException | IOException e) {
                    e.printStackTrace();
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
            DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyyMMdd");
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", ".xlsx");
            fileChooser.getExtensionFilters().addAll(pdfFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter);

            JEVisObject selectedItem = listView.getSelectionModel().getSelectedItem();
            fileChooser.setInitialFileName(selectedItem.getName() + fmtDate.print(new DateTime()));
            File file = fileChooser.showSaveDialog(JEConfig.getStage());
            if (file != null) {
                File destinationFile = new File(file + fileChooser.getSelectedExtensionFilter().getExtensions().get(0));
                try {
                    JEVisAttribute last_report_pdf = selectedItem.getAttribute("Last Report");
                    DateTime dateTime = dateTimeComboBox.getSelectionModel().getSelectedItem();
                    List<JEVisSample> samples = last_report_pdf.getSamples(dateTime.minusMinutes(1), dateTime.plusMinutes(1));
                    samples.get(0).getValueAsFile().saveToFile(destinationFile);
                } catch (JEVisException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Separator sep2 = new Separator(Orientation.VERTICAL);

        ToggleButton printButton = new ToggleButton("", JEConfig.getImage("Print_1493286.png", iconSize, iconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print"));
        printButton.setTooltip(printTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        printButton.setOnAction(event -> {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            try {
                JEVisObject selectedItem = listView.getSelectionModel().getSelectedItem();
                JEVisAttribute last_report_pdf = selectedItem.getAttribute("Last Report PDF");
                DateTime dateTime = dateTimeComboBox.getSelectionModel().getSelectedItem();
                List<JEVisSample> samples = last_report_pdf.getSamples(dateTime, dateTime);
                PDDocument document = PDDocument.load(samples.get(0).getValueAsFile().getBytes());
                printerJob.setPageable(new PDFPageable(document));
                if (printerJob.printDialog()) {
                    printerJob.print();
                }
            } catch (IOException | JEVisException | PrinterException e) {
                e.printStackTrace();
            }
        });

        toolBar.getItems().setAll(reload, sep1, pdfButton, xlsxButton, sep2, printButton);
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
        JEVisAttribute lastReportPDFAttribute = null;
        try {
            lastReportPDFAttribute = reportObject.getAttribute("Last Report PDF");
        } catch (JEVisException e) {
            logger.error("Could not get 'Last Report' Attribute from object {}:{}", reportObject.getName(), reportObject.getID(), e);
        }

        if (lastReportPDFAttribute != null) {
            List<JEVisSample> allSamples = lastReportPDFAttribute.getAllSamples();
            if (allSamples.size() > 0) {
                JEVisSample lastSample = allSamples.get(allSamples.size() - 1);
                Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
                List<DateTime> dateTimeList = new ArrayList<>();
                for (JEVisSample jeVisSample : allSamples) {
                    try {
                        dateTimeList.add(jeVisSample.getTimestamp());
                        sampleMap.put(jeVisSample.getTimestamp(), jeVisSample);
                    } catch (JEVisException e) {
                        logger.error("Could not add date to dat list.");
                    }
                }
                dateTimeComboBox = new ComboBox<>(FXCollections.observableList(dateTimeList));
                Callback<ListView<DateTime>, ListCell<DateTime>> cellFactory = new Callback<ListView<DateTime>, ListCell<DateTime>>() {
                    @Override
                    public ListCell<DateTime> call(ListView<DateTime> param) {
                        return new ListCell<DateTime>() {
                            @Override
                            protected void updateItem(DateTime obj, boolean empty) {
                                super.updateItem(obj, empty);
                                if (obj == null || empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    setText(obj.toString("yyyy-MM-dd HH:mm"));
                                }
                            }
                        };
                    }
                };

                dateTimeComboBox.setCellFactory(cellFactory);
                dateTimeComboBox.setButtonCell(cellFactory.call(null));

                try {
                    dateTimeComboBox.getSelectionModel().select(lastSample.getTimestamp());
                } catch (JEVisException e) {
                    logger.error("Could not get Time Stamp of last sample.");
                    dateTimeComboBox.getSelectionModel().select(dateTimeList.size() - 1);
                }

                ImageView leftImage = JEConfig.getImage("left.png", 20, 20);
                ImageView rightImage = JEConfig.getImage("right.png", 20, 20);

                leftImage.setOnMouseClicked(event -> {
                    int i = dateTimeComboBox.getSelectionModel().getSelectedIndex();
                    if (i > 0) {
                        dateTimeComboBox.getSelectionModel().select(i - 1);
                    }
                });

                rightImage.setOnMouseClicked(event -> {
                    int i = dateTimeComboBox.getSelectionModel().getSelectedIndex();
                    if (i < sampleMap.size()) {
                        dateTimeComboBox.getSelectionModel().select(i + 1);
                    }
                });

                Separator sep1 = new Separator(Orientation.VERTICAL);
                Separator sep2 = new Separator(Orientation.VERTICAL);

                Label labelDateTimeComboBox = new Label(I18n.getInstance().getString("plugin.reports.selectionbox.label"));
                labelDateTimeComboBox.setAlignment(Pos.CENTER_LEFT);

                hBox.getChildren().setAll(labelDateTimeComboBox, leftImage, sep1, dateTimeComboBox, sep2, rightImage);
                hBox.setAlignment(Pos.CENTER);

                dateTimeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        try {
                            byte[] bytes = sampleMap.get(newValue).getValueAsFile().getBytes();
                            model.setBytes(bytes);
                            pagination.setPageCount(model.numPages());
                            zoomFactor.set(0.3);
                            pagination.setPageFactory(index -> new Group(model.getImage(index, zoomFactor.get())));
                        } catch (Exception e) {
                            logger.error("Could not load report for {}:{} for ts {}", reportObject.getName(), reportObject.getID(), newValue.toString(), e);
                        }
                    }
                });

                try {
                    byte[] bytes = sampleMap.get(lastSample.getTimestamp()).getValueAsFile().getBytes();
                    model.setBytes(bytes);
                    pagination.setPageCount(model.numPages());
                    zoomFactor.set(0.3);
                    pagination.setPageFactory(index -> new Group(model.getImage(index, zoomFactor.get())));
                } catch (Exception e) {
                    logger.error("Could not load latest report for {}:{}", reportObject.getName(), reportObject.getID(), e);
                }
            }
        }
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
