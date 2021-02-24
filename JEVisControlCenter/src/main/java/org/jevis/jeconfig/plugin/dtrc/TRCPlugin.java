package org.jevis.jeconfig.plugin.dtrc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ibm.icu.text.NumberFormat;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.SaveUnderDialog;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.dialog.TemplateCalculationFormulaDialog;
import org.jevis.jeconfig.dialog.TemplateCalculationInputDialog;
import org.jevis.jeconfig.dialog.TemplateCalculationOutputDialog;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.joda.time.DateTime;
import org.mariuszgromada.math.mxparser.Expression;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

public class TRCPlugin implements Plugin {
    public static final String TEMPLATE_CLASS = "Result Calculation Template";
    private static final String PLUGIN_CLASS_NAME = "Template Result Calculation Plugin";
    private static final Logger logger = LogManager.getLogger(TRCPlugin.class);
    private static final String DATA_MODEL_ATTRIBUTE = "Template File";
    private static final String NO_RESULT = I18n.getInstance().getString("plugin.dtrc.noresult");
    private static final double EDITOR_MAX_HEIGHT = 50;
    public static String PLUGIN_NAME = "Template Result Calculation Plugin";
    protected final JEVisDataSource ds;
    protected final int toolBarIconSize = 20;
    protected final ObjectRelations objectRelations;
    protected final String title;
    protected final AlphanumComparator alphanumComparator = new AlphanumComparator();
    protected final JFXTextField filterInput = new JFXTextField();
    private final Image taskImage = JEConfig.getImage("measurement_instrument.png");
    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.TRCPlugin");
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private final FlowPane configFormulas = new FlowPane(4, 4);
    private final FlowPane configInputs = new FlowPane(4, 4);
    private final GridPane configOutputs = new GridPane();
    private final Tab configurationTab = new Tab(I18n.getInstance().getString("graph.tabs.configuration"));
    private final JFXTabPane tabPane = new JFXTabPane();
    private final Tab viewTab = new Tab(I18n.getInstance().getString("menu.view"));
    private final ObjectMapper mapper = new ObjectMapper();
    private final TemplateHandler templateHandler = new TemplateHandler();
    private final FlowPane viewInputs = new FlowPane(4, 4);
    private final GridPane viewOutputs = new GridPane();
    private final JFXDatePicker startDatePicker = new JFXDatePicker(LocalDate.now());
    private final JFXDatePicker endDatePicker = new JFXDatePicker(LocalDate.now());
    private final JFXTimePicker startTimePicker = new JFXTimePicker(LocalTime.of(0, 0, 0));
    private final JFXTimePicker endTimePicker = new JFXTimePicker(LocalTime.of(23, 59, 59));
    private final NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private boolean initialized = false;
    private JFXComboBox<JEVisObject> trcs;

    public TRCPlugin(JEVisDataSource ds) {
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.title = getTitleFromPlugin();

        this.filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.nf.setMinimumFractionDigits(2);
        this.nf.setMaximumFractionDigits(2);

        this.viewOutputs.setPadding(new Insets(4));
        this.viewOutputs.setVgap(6);
        this.viewOutputs.setHgap(6);

        this.configOutputs.setPadding(new Insets(4));
        this.configOutputs.setVgap(6);
        this.configOutputs.setHgap(6);

        initToolBar();
    }

    public static String getRealName(JEVisObject jeVisObject) {
        String objectName = jeVisObject.getName();
        try {
            if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                JEVisObject firstParentalDataObject = CommonMethods.getFirstParentalDataObject(jeVisObject);
                objectName = firstParentalDataObject.getName();
            }
        } catch (JEVisException e) {
            logger.error("Could not get raw data name for object {}:{}", jeVisObject.getName(), jeVisObject.getID(), e);
        }
        return objectName;
    }

    private String getTitleFromPlugin() {
        JEVisClass pluginClass = null;
        try {
            pluginClass = ds.getJEVisClass(PLUGIN_NAME);
            List<JEVisObject> objects = ds.getObjects(pluginClass, true);
            if (!objects.isEmpty()) {
                return objects.get(0).getName();
            }
        } catch (JEVisException e) {
            logger.error("Could not get name from plugin class", e);
        }
        return I18n.getInstance().getString("plugin.dtrc.title");
    }

    private boolean isMultiSite() {

        try {
            JEVisClass measurementInstrumentDirectoryClass = ds.getJEVisClass("Dynamic Result Calculation Directory");
            List<JEVisObject> objects = ds.getObjects(measurementInstrumentDirectoryClass, true);

            List<JEVisObject> buildingParents = new ArrayList<>();
            for (JEVisObject jeVisObject : objects) {
                JEVisObject buildingParent = objectRelations.getBuildingParent(jeVisObject);
                if (!buildingParents.contains(buildingParent)) {
                    buildingParents.add(buildingParent);

                    if (buildingParents.size() > 1) {
                        return true;
                    }
                }
            }

        } catch (Exception e) {

        }

        return false;
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        Separator sep1 = new Separator(Orientation.VERTICAL);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        save.setOnAction(event -> handleRequest(Constants.Plugin.Command.SAVE));

        Separator sep2 = new Separator(Orientation.VERTICAL);

        ToggleButton newButton = new ToggleButton("", JEConfig.getImage("list-add.png", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newButton);
        newButton.setOnAction(event -> handleRequest(Constants.Plugin.Command.NEW));

        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        delete.setOnAction(event -> handleRequest(Constants.Plugin.Command.DELETE));

        Separator sep3 = new Separator(Orientation.VERTICAL);

        ToggleButton printButton = new ToggleButton("", JEConfig.getImage("Print_1493286.png", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        printButton.setOnAction(event -> {
            Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
            TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();

            Printer printer = null;
            ObservableSet<Printer> printers = Printer.getAllPrinters();
            printer = printers.stream().findFirst().orElse(printer);

            if (printer != null) {
                PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);
                PrinterJob job = PrinterJob.createPrinterJob(printer);

                if (job.showPrintDialog(JEConfig.getStage().getOwner())) {
                    double pagePrintableWidth = job.getJobSettings().getPageLayout().getPrintableWidth();
                    double pagePrintableHeight = job.getJobSettings().getPageLayout().getPrintableHeight();

                    double prefHeight = tableView.getPrefHeight();
                    double minHeight = tableView.getMinHeight();
                    double maxHeight = tableView.getMaxHeight();

                    tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(EDITOR_MAX_HEIGHT));
                    tableView.minHeightProperty().bind(tableView.prefHeightProperty());
                    tableView.maxHeightProperty().bind(tableView.prefHeightProperty());

                    double scaleX = pagePrintableWidth / tableView.getBoundsInParent().getWidth();
                    double scaleY = scaleX;
                    double localScale = scaleX;

                    double numberOfPages = Math.ceil((tableView.getPrefHeight() * localScale) / pagePrintableHeight);

                    tableView.getTransforms().add(new Scale(scaleX, (scaleY)));
                    tableView.getTransforms().add(new Translate(0, 0));

                    Translate gridTransform = new Translate();
                    tableView.getTransforms().add(gridTransform);

                    for (int i = 0; i < numberOfPages; i++) {
                        gridTransform.setY(-i * (pagePrintableHeight / localScale));
                        job.printPage(pageLayout, tableView);
                    }

                    job.endJob();

                    tableView.prefHeightProperty().unbind();
                    tableView.minHeightProperty().unbind();
                    tableView.maxHeightProperty().unbind();
                    tableView.getTransforms().clear();

                    tableView.setMinHeight(minHeight);
                    tableView.setMaxHeight(maxHeight);
                    tableView.setPrefHeight(prefHeight);
                }
            }
        });

        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(toolBarIconSize, toolBarIconSize);
        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(toolBarIconSize, toolBarIconSize);

        reload.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip")));
        save.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.save.tooltip")));
        newButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.new.tooltip")));

        printButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print")));

        trcs = new JFXComboBox<>();
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> attributeCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new JFXListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getName());
                        }
                    }
                };
            }
        };

        trcs.setCellFactory(attributeCellFactory);
        trcs.setButtonCell(attributeCellFactory.call(null));

        trcs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                templateHandler.setTemplateObject(newValue);

                updateFormulas();
                updateInputs();
                updateOutputs();
                updateViewInputFlowPane();
                updateViewOutputGridPane();
            }
        });

//        toolBar.getItems().setAll(filterInput, trcs, reload, sep1, save, sep2, newButton, sep3, printButton);
        toolBar.getItems().setAll(trcs, reload, sep1, save, sep2, newButton, sep3, printButton);
        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

        JEVisHelp.getInstance().addHelpItems(TRCPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());
    }

    @Override
    public String getClassName() {
        return PLUGIN_CLASS_NAME;
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
        return I18n.getInstance().getString("plugin.dtrc.tooltip");
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
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                return true;
            case Constants.Plugin.Command.DELETE:
                return true;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return true;
            case Constants.Plugin.Command.RELOAD:
                return true;
            case Constants.Plugin.Command.ADD_TABLE:
                return false;
            case Constants.Plugin.Command.EDIT_TABLE:
                return false;
            case Constants.Plugin.Command.CREATE_WIZARD:
                return false;
            case Constants.Plugin.Command.FIND_OBJECT:
                return false;
            case Constants.Plugin.Command.PASTE:
                return false;
            case Constants.Plugin.Command.COPY:
                return false;
            case Constants.Plugin.Command.CUT:
                return false;
            case Constants.Plugin.Command.FIND_AGAIN:
                return false;
            default:
                return false;
        }
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
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                try {
                    JEVisClass templateClass = ds.getJEVisClass(TEMPLATE_CLASS);

                    SaveUnderDialog.saveUnderAnalysis(ds, templateHandler.getTemplateObject(), templateClass, templateHandler.getTitle(), (target, sameObject) -> {

                        JEVisAttribute dataModel = null;
                        try {
                            templateHandler.setTitle(target.getName());

                            dataModel = templateHandler.getTemplateObject().getAttribute(DATA_MODEL_ATTRIBUTE);

                            JEVisFileImp jsonFile = new JEVisFileImp(
                                    templateHandler.getTitle() + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                    , this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(templateHandler.toJsonNode()).getBytes(StandardCharsets.UTF_8));
                            JEVisSample newSample = dataModel.buildSample(new DateTime(), jsonFile);
                            newSample.commit();
                        } catch (Exception e) {
                            logger.error("Could not save template", e);
                        }
                        return true;
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
            case Constants.Plugin.Command.DELETE:
                //TODO: Delete Function
//                Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
//                TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();
//
//                JEVisObject object = tableView.getSelectionModel().getSelectedItem().getObject();
//
//                Dialog<ButtonType> reallyDelete = new Dialog<>();
//                reallyDelete.setTitle(I18n.getInstance().getString("plugin.graph.dialog.delete.title"));
//                final JFXButtonType ok = new JFXButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"), JFXButtonBar.ButtonData.YES);
//                final JFXButtonType cancel = new JFXButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.cancel"), JFXButtonBar.ButtonData.CANCEL_CLOSE);
//
//                reallyDelete.setContentText(I18n.getInstance().getString("plugin.meters.dialog.delete.message"));
//                reallyDelete.getDialogPane().getButtonTypes().addAll(ok, cancel);
//                reallyDelete.showAndWait().ifPresent(response -> {
//                    if (response.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
//                        try {
//                            if (getDataSource().getCurrentUser().canDelete(object.getID())) {
//                                getDataSource().deleteObject(object.getID());
//                                handleRequest(Constants.Plugin.Command.RELOAD);
//                            } else {
//                                Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.meters.dialog.delete.error"), cancel);
//                                alert.showAndWait();
//                            }
//                        } catch (JEVisException e) {
//                            logger.error("Error: could not delete current meter", e);
//                        }
//                    }
//                });
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                //TODO: New Function
//                MeterDialog meterDialog = new MeterDialog(getDataSource(), ((JEVisClassTab) tabPane.getSelectionModel().getSelectedItem()).getJeVisClass());
//                if (meterDialog.showNewWindow() == Response.OK) {
//                    handleRequest(Constants.Plugin.Command.RELOAD);
//                }
                break;
            case Constants.Plugin.Command.RELOAD:
                //TODO: Reload Function
                List<JEVisObject> allTemplateCalculations = getAllTemplateCalculations();
                trcs.setItems(FXCollections.observableArrayList(allTemplateCalculations));
                if (allTemplateCalculations.isEmpty()) {
                    templateHandler.setRcTemplate(new RCTemplate());
                } else {
                    Platform.runLater(() -> trcs.getSelectionModel().selectFirst());
                }
//                Platform.runLater(() -> replaceButton.setDisable(true));
//                selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
//
//                Task clearCacheTask = new Task() {
//                    @Override
//                    protected Object call() throws Exception {
//                        try {
//                            this.updateTitle(I18n.getInstance().getString("Clear Cache"));
//                            if (initialized) {
//                                getDataSource().clearCache();
//                                getDataSource().preload();
//                            } else {
//                                initialized = true;
//                            }
//                            updateList();
//                            succeeded();
//                        } catch (Exception ex) {
//                            failed();
//                        } finally {
//                            done();
//                        }
//                        return null;
//                    }
//                };
//                JEConfig.getStatusBar().addTask(PLUGIN_NAME, clearCacheTask, JEConfig.getImage("measurement_instrument.png"), true);

                break;
            case Constants.Plugin.Command.ADD_TABLE:
                break;
            case Constants.Plugin.Command.EDIT_TABLE:
                break;
            case Constants.Plugin.Command.CREATE_WIZARD:
                break;
            case Constants.Plugin.Command.FIND_OBJECT:
                break;
            case Constants.Plugin.Command.PASTE:
                break;
            case Constants.Plugin.Command.COPY:
                break;
            case Constants.Plugin.Command.CUT:
                break;
            case Constants.Plugin.Command.FIND_AGAIN:
                break;
        }
    }

    @Override
    public Node getContentNode() {
        return borderPane;
    }

    private List<JEVisObject> getAllTemplateCalculations() {
        List<JEVisObject> list = new ArrayList<>();
        try {
            JEVisClass templateClass = getDataSource().getJEVisClass(TEMPLATE_CLASS);
            list = getDataSource().getObjects(templateClass, true);
            list.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("measurement_instrument.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

        initGui();

        Task loadTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    this.updateTitle(I18n.getInstance().getString("plugin.meters.load"));
                    if (!initialized) {
                        initialized = true;
                        handleRequest(Constants.Plugin.Command.RELOAD);
                    }
                    succeeded();
                } catch (Exception ex) {
                    failed();
                } finally {
                    done();
                }
                return null;
            }
        };
        JEConfig.getStatusBar().addTask(PLUGIN_NAME, loadTask, JEConfig.getImage("measurement_instrument.png"), true);

    }

    private void initGui() {
        viewTab.setClosable(false);
        configurationTab.setClosable(false);

        Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate") + "  ");
        Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));

        startDatePicker.setPrefWidth(120d);
        endDatePicker.setPrefWidth(120d);

        startTimePicker.setPrefWidth(100d);
        startTimePicker.setMaxWidth(100d);
        startTimePicker.set24HourView(true);
        startTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        endTimePicker.setPrefWidth(100d);
        endTimePicker.setMaxWidth(100d);
        endTimePicker.set24HourView(true);
        endTimePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        startTimePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateViewOutputGridPane());
        endTimePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateViewOutputGridPane());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateViewOutputGridPane());
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateViewOutputGridPane());

        IntervalSelector intervalSelector = new IntervalSelector(ds, startDatePicker, startTimePicker, endDatePicker, endTimePicker);


        GridPane datePane = new GridPane();
        datePane.setPadding(new Insets(4));
        datePane.setHgap(6);
        datePane.setVgap(6);

        datePane.add(startText, 0, 0);
        datePane.add(startDatePicker, 1, 0);
        datePane.add(startTimePicker, 2, 0);

        datePane.add(endText, 0, 1);
        datePane.add(endDatePicker, 1, 1);
        datePane.add(endTimePicker, 2, 1);

        HBox dateBox = new HBox(4, datePane, intervalSelector);

        if (templateHandler.getRcTemplate() != null) {
            updateViewInputFlowPane();
            updateViewOutputGridPane();
        }

        Label inputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.view.input"));
        inputsLabel.setPadding(new Insets(8, 0, 8, 0));
        inputsLabel.setFont(new Font(18));
        Label outputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.view.output"));
        outputsLabel.setPadding(new Insets(8, 0, 8, 0));
        outputsLabel.setFont(new Font(18));

        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setPadding(new Insets(8, 0, 8, 0));

        Separator separator2 = new Separator(Orientation.HORIZONTAL);
        separator2.setPadding(new Insets(8, 0, 8, 0));

        VBox viewVBox = new VBox(4,
                dateBox, separator1,
                inputsLabel, viewInputs, separator2,
                outputsLabel, viewOutputs);

        viewVBox.setPadding(new Insets(12));
        viewTab.setContent(viewVBox);

        configFormulas.getChildren().add(buildAddFormulaButton());
        configInputs.getChildren().add(buildAddInputButton());

        Label formulaLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel"));
        Label inputsLabel2 = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.inputslabel"));
        Label outputsLabel2 = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.outputslabel"));

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setPadding(new Insets(8, 0, 8, 0));

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        separator4.setPadding(new Insets(8, 0, 8, 0));

        VBox configVBox = new VBox(4, formulaLabel, configFormulas, separator3,
                inputsLabel2, configInputs, separator4,
                new HBox(outputsLabel2, buildAddOutputButton()), configOutputs);

        configVBox.setPadding(new Insets(12));
        configurationTab.setContent(configVBox);

        tabPane.getTabs().setAll(viewTab, configurationTab);

        borderPane.setCenter(tabPane);
    }

    private void updateViewInputFlowPane() {
        viewInputs.getChildren().clear();

        Map<JEVisClass, List<TemplateInput>> groupedInputsMap = new HashMap<>();
        List<TemplateInput> ungroupedInputs = new ArrayList<>();
        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            if (templateInput.getGroup() == null || templateInput.getGroup()) {
                JEVisClass jeVisClass = null;
                try {
                    jeVisClass = ds.getJEVisClass(templateInput.getObjectClass());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if (groupedInputsMap.get(jeVisClass) == null) {
                    List<TemplateInput> list = new ArrayList<>();
                    list.add(templateInput);
                    groupedInputsMap.put(jeVisClass, list);
                } else {
                    groupedInputsMap.get(jeVisClass).add(templateInput);
                }
            } else ungroupedInputs.add(templateInput);
        }


        for (Map.Entry<JEVisClass, List<TemplateInput>> templateInput : groupedInputsMap.entrySet()) {
            JEVisClass inputClass = templateInput.getKey();
            List<TemplateInput> groupedInputs = templateInput.getValue();
            String className = null;
            try {
                className = I18nWS.getInstance().getClassName(inputClass);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            Label label = new Label(className);
            label.setAlignment(Pos.CENTER);
            List<JEVisObject> objects = null;
            try {
                objects = ds.getObjects(ds.getJEVisClass(inputClass.getName()), false);
                List<JEVisObject> filteredObjects = new ArrayList<>();
                String filter = groupedInputs.get(0).getFilter();
                for (JEVisObject jeVisObject : objects) {
                    String objectName = getRealName(jeVisObject);

                    if (filter != null && filter.contains(" ")) {
                        String[] result = filter.split(" ");
                        String string = objectName.toLowerCase();
                        boolean[] results = new boolean[result.length];
                        for (int i = 0, resultLength = result.length; i < resultLength; i++) {
                            String value = result[i];
                            String subString = value.toLowerCase();
                            results[i] = string.contains(subString);
                        }

                        boolean allFound = true;
                        for (boolean b : results) {
                            if (!b) {
                                allFound = false;
                                break;
                            }
                        }
                        if (allFound) {
                            filteredObjects.add(jeVisObject);
                        }

                    } else if (filter != null) {
                        String string = objectName.toLowerCase();
                        if (string.contains(filter.toLowerCase())) {
                            filteredObjects.add(jeVisObject);
                        }
                    } else filteredObjects.add(jeVisObject);
                }

                objects = filteredObjects;
            } catch (JEVisException e) {
                logger.error("Could not get JEVisClass {}", className, e);
            }
            JFXComboBox<JEVisObject> objectSelector = new JFXComboBox<>(FXCollections.observableArrayList(objects));
            Callback<ListView<JEVisObject>, ListCell<JEVisObject>> attributeCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
                @Override
                public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                    return new JFXListCell<JEVisObject>() {
                        @Override
                        protected void updateItem(JEVisObject obj, boolean empty) {
                            super.updateItem(obj, empty);
                            if (obj == null || empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                try {
                                    if (!obj.getJEVisClassName().equals("Clean Data")) {
                                        setText(obj.getName());
                                    } else {
                                        setText(getRealName(obj));
                                    }
                                } catch (JEVisException e) {
                                    logger.error("Could not get JEVisClass of object {}:{}", obj.getName(), obj.getID(), e);
                                }
                            }
                        }
                    };
                }
            };

            objectSelector.setCellFactory(attributeCellFactory);
            objectSelector.setButtonCell(attributeCellFactory.call(null));

            objectSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    groupedInputs.forEach(templateInput1 -> templateInput1.setObjectID(newValue.getID()));
                    updateViewOutputGridPane();
                }
            });

            VBox vBox = new VBox(label);
            vBox.setAlignment(Pos.CENTER);
            HBox templateBox = new HBox(4, vBox, objectSelector);
            viewInputs.getChildren().add(templateBox);

            Platform.runLater(() -> objectSelector.getSelectionModel().selectFirst());
        }
    }

    private void updateViewOutputGridPane() {
        Platform.runLater(() -> viewOutputs.getChildren().clear());

        DateTime start = new DateTime(startDatePicker.getValue().getYear(), startDatePicker.getValue().getMonthValue(), startDatePicker.getValue().getDayOfMonth(),
                startTimePicker.getValue().getHour(), startTimePicker.getValue().getMinute(), startTimePicker.getValue().getSecond());
        DateTime end = new DateTime(endDatePicker.getValue().getYear(), endDatePicker.getValue().getMonthValue(), endDatePicker.getValue().getDayOfMonth(),
                endTimePicker.getValue().getHour(), endTimePicker.getValue().getMinute(), endTimePicker.getValue().getSecond());

        for (TemplateOutput templateOutput : templateHandler.getRcTemplate().getTemplateOutputs()) {

            Label label = new Label(templateOutput.getName());
            if (templateOutput.getNameBold()) {
                label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, label.getFont().getSize()));
            }
            Label result = new Label();
            result.setTextAlignment(TextAlignment.RIGHT);
            if (templateOutput.getResultBold()) {
                result.setFont(Font.font(result.getFont().getFamily(), FontWeight.BOLD, result.getFont().getSize()));
            }
            HBox hBox = new HBox(label, result);

            Task<String> task = new Task<String>() {
                @Override
                protected String call() {
                    String result = NO_RESULT;

                    TemplateFormula formula = templateHandler.getRcTemplate().getTemplateFormulas().stream().filter(templateFormula -> templateFormula.getOutput().equals(templateOutput.getVariableName())).findFirst().orElse(null);

                    if (formula != null) {
                        linkInputs(formula, templateHandler.getRcTemplate().getTemplateInputs());
                        String formulaString = formula.getFormula();
                        boolean isText = false;
                        for (TemplateInput templateInput : formula.getInputs()) {
                            try {
                                if (templateInput.getVariableType().equals(InputVariableType.STRING.toString())) {
                                    isText = true;
                                }

                                formulaString = formulaString.replace(templateInput.getVariableName(), templateInput.getValue(ds, start, end));

                            } catch (JEVisException e) {
                                logger.error("Could not get template input value for {}", templateInput.getVariableName(), e);
                            }
                        }

                        if (!isText) {
                            Expression expression = new Expression(formulaString);
                            result = nf.format(expression.calculate()) + " " + templateOutput.getUnit();
                        } else result = formulaString;
                    } else result = "";

                    return result;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                try {
                    result.setText(task.get());
                } catch (InterruptedException e) {
                    logger.error("InterruptedException", e);
                } catch (ExecutionException e) {
                    logger.error("ExecutionException", e);
                }
            }));

            JEConfig.getStatusBar().addTask(TRCPlugin.class.getSimpleName(), task, null, true);

            Platform.runLater(() -> this.viewOutputs.add(hBox, templateOutput.getColumn(), templateOutput.getRow(), templateOutput.getColSpan(), templateOutput.getRowSpan()));
        }
    }

    private void linkInputs(TemplateFormula formula, List<TemplateInput> templateInputs) {
        for (TemplateInput templateInput : formula.getInputs()) {
            templateInputs.stream().filter(input1 -> templateInput.getVariableName().equals(input1.getVariableName())).findFirst().ifPresent(templateInput::clone);
        }
    }

    private JFXButton buildAddFormulaButton() {
        JFXButton addFormulaButton = new JFXButton("", ResourceLoader.getImage("list-add.png", 15, 15));

        addFormulaButton.setOnAction(event -> {
            TemplateFormula templateFormula = new TemplateFormula();

            TemplateCalculationFormulaDialog templateCalculationFormulaDialog = new TemplateCalculationFormulaDialog();
            if (templateCalculationFormulaDialog.show(ds, templateHandler.getRcTemplate().getTemplateInputs(), templateHandler.getRcTemplate().getTemplateOutputs(), templateFormula) == Response.OK) {
                templateHandler.getRcTemplate().getTemplateFormulas().add(templateFormula);
                updateFormulas();
            }
        });

        return addFormulaButton;
    }

    private JFXButton buildAddInputButton() {
        JFXButton addInputButton = new JFXButton("", ResourceLoader.getImage("list-add.png", 15, 15));

        addInputButton.setOnAction(event -> {
            TemplateInput templateInput = new TemplateInput();

            TemplateCalculationInputDialog templateCalculationInputDialog = new TemplateCalculationInputDialog();
            if (templateCalculationInputDialog.show(ds, templateInput) == Response.OK) {
                templateHandler.getRcTemplate().getTemplateInputs().add(templateInput);
                updateInputs();
            }
        });

        return addInputButton;
    }

    private JFXButton buildAddOutputButton() {
        JFXButton addOutputButton = new JFXButton("", ResourceLoader.getImage("list-add.png", 15, 15));

        addOutputButton.setOnAction(event -> {
            TemplateOutput templateOutput = new TemplateOutput();
            int index = templateHandler.getRcTemplate().getTemplateOutputs().size();

            templateOutput.setColumn(0);
            templateOutput.setRow(index);

            TemplateCalculationOutputDialog templateCalculationOutputDialog = new TemplateCalculationOutputDialog();
            if (templateCalculationOutputDialog.show(ds, templateOutput) == Response.OK) {
                templateHandler.getRcTemplate().getTemplateOutputs().add(templateOutput);
                updateOutputs();
            }
        });

        return addOutputButton;
    }

    private void updateFormulas() {
        int size = configFormulas.getChildren().size();
        if (size > 1) {
            configFormulas.getChildren().remove(1, size);
        }

        for (TemplateFormula templateFormula : templateHandler.getRcTemplate().getTemplateFormulas()) {
            int index = templateHandler.getRcTemplate().getTemplateFormulas().indexOf(templateFormula);
            configFormulas.getChildren().add(createFormulaButton(templateFormula, index));
        }
    }

    private void updateInputs() {
        int size = configInputs.getChildren().size();
        if (size > 1) {
            configInputs.getChildren().remove(1, size);
        }

        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            configInputs.getChildren().add(createInputButton(templateInput));
        }

        updateViewInputFlowPane();
    }

    private void updateOutputs() {

        Platform.runLater(() -> configOutputs.getChildren().clear());

        int maxRow = 0;
        int maxColumn = 0;
        for (TemplateOutput templateOutput : templateHandler.getRcTemplate().getTemplateOutputs()) {
            maxRow = Math.max(maxRow, templateOutput.getRow());
            maxColumn = Math.max(maxColumn, templateOutput.getColumn());
        }

        for (int i = 1; i <= maxRow + 1; i++) {
            int finalI = i;
            Platform.runLater(() -> configOutputs.add(new Label(String.valueOf(finalI - 1)), 0, finalI));
        }

        for (int i = 1; i <= maxColumn + 1; i++) {
            int finalI = i;
            Platform.runLater(() -> configOutputs.add(new Label(String.valueOf(finalI - 1)), finalI, 0));
        }

        for (TemplateOutput templateOutput : templateHandler.getRcTemplate().getTemplateOutputs()) {
            int index = templateHandler.getRcTemplate().getTemplateOutputs().indexOf(templateOutput);

            Integer col = templateOutput.getColumn();
            Integer row = templateOutput.getRow();
            int colSpan = templateOutput.getColSpan();
            int rowSpan = templateOutput.getRowSpan();

            if (col == null) {
                col = 0;
                templateOutput.setColumn(0);
            }
            if (row == null) {
                row = index;
                templateOutput.setRow(index);
            }

            Integer finalCol = col;
            Integer finalRow = row;
            Platform.runLater(() -> configOutputs.add(createOutputButton(templateOutput), finalCol + 1, finalRow + 1, colSpan, rowSpan));
        }
    }

    private JFXButton createFormulaButton(TemplateFormula templateFormula, int index) {
        JFXButton formulaButton = new JFXButton(templateFormula.getName());
        formulaButton.setMnemonicParsing(false);

        if (templateFormula.getName() == null || templateFormula.getName().equals(""))
            formulaButton.setText(String.valueOf(index));

        formulaButton.setOnAction(event -> {
            TemplateCalculationFormulaDialog templateCalculationFormulaDialog = new TemplateCalculationFormulaDialog();
            if (templateCalculationFormulaDialog.show(ds, templateHandler.getRcTemplate().getTemplateInputs(), templateHandler.getRcTemplate().getTemplateOutputs(), templateFormula) == Response.DELETE) {
                templateHandler.getRcTemplate().getTemplateFormulas().remove(templateFormula);
            }
            updateFormulas();
        });

        return formulaButton;
    }

    private JFXButton createInputButton(TemplateInput templateInput) {
        JFXButton inputButton = new JFXButton(templateInput.getVariableName());
        inputButton.setMnemonicParsing(false);

        inputButton.setOnAction(event -> {
            TemplateCalculationInputDialog templateCalculationInputDialog = new TemplateCalculationInputDialog();
            if (templateCalculationInputDialog.show(ds, templateInput) == Response.DELETE) {
                templateHandler.getRcTemplate().getTemplateInputs().remove(templateInput);
            }
            updateInputs();
        });

        return inputButton;
    }

    private JFXButton createOutputButton(TemplateOutput templateOutput) {
        JFXButton outputButton = new JFXButton(templateOutput.getName());
        outputButton.setMnemonicParsing(false);

        outputButton.setOnAction(event -> {
            TemplateCalculationOutputDialog templateCalculationOutputDialog = new TemplateCalculationOutputDialog();
            if (templateCalculationOutputDialog.show(ds, templateOutput) == Response.DELETE) {
                templateHandler.getRcTemplate().getTemplateOutputs().remove(templateOutput);
            }
            updateOutputs();
        });

        return outputButton;
    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 5;
    }

}
