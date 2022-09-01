package org.jevis.jeconfig.plugin.dtrc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.*;
import org.jevis.jeconfig.application.control.SaveUnderDialog;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.dialog.TemplateCalculationFormulaDialog;
import org.jevis.jeconfig.dialog.TemplateCalculationInputDialog;
import org.jevis.jeconfig.dialog.TemplateCalculationOutputDialog;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class TRCPlugin implements Plugin {
    public static final String TEMPLATE_CLASS = "Result Calculation Template";
    public static final String TEMPLATE_DIRECTORY_CLASS = "Template Calculation Directory";
    private static final String PLUGIN_CLASS_NAME = "Template Result Calculation Plugin";
    private static final Logger logger = LogManager.getLogger(TRCPlugin.class);
    private static final String DATA_MODEL_ATTRIBUTE = "Template File";
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
    private final FlowPane configFormulaInputs = new FlowPane(4, 4);
    private final GridPane configOutputs = new GridPane();
    private final Tab configurationTab = new Tab(I18n.getInstance().getString("graph.tabs.configuration"));
    private final TabPane tabPane = new TabPane();
    private OutputView viewTab;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TemplateHandler templateHandler = new TemplateHandler();

    private boolean initialized = false;
    private JFXComboBox<JEVisObject> trcs;
    private StackPane dialogStackPane;

    public TRCPlugin(JEVisDataSource ds) {
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.title = getTitleFromPlugin();

        this.filterInput.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.configOutputs.setPadding(new Insets(4));
        this.configOutputs.setVgap(6);
        this.configOutputs.setHgap(6);
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
        ToggleButton reload = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        Separator sep1 = new Separator(Orientation.VERTICAL);

        ToggleButton save = new ToggleButton("", JEConfig.getSVGImage(Icon.SAVE, toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        save.setOnAction(event -> handleRequest(Constants.Plugin.Command.SAVE));

        Separator sep2 = new Separator(Orientation.VERTICAL);

        ToggleButton newButton = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newButton);
        newButton.setOnAction(event -> handleRequest(Constants.Plugin.Command.NEW));

        ToggleButton delete = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        delete.setOnAction(event -> handleRequest(Constants.Plugin.Command.DELETE));

        Separator sep3 = new Separator(Orientation.VERTICAL);

        ToggleButton printButton = new ToggleButton("", JEConfig.getSVGImage(Icon.PRINT, toolBarIconSize, toolBarIconSize));
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
                viewTab.requestUpdate();
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

                    SaveUnderDialog saveUnderDialog = new SaveUnderDialog(dialogStackPane, ds, TEMPLATE_DIRECTORY_CLASS, templateHandler.getTemplateObject(), templateClass, templateHandler.getTitle(), (target, sameObject) -> {

                        JEVisAttribute dataModel = null;
                        try {
                            templateHandler.setTitle(target.getName());

                            dataModel = target.getAttribute(DATA_MODEL_ATTRIBUTE);

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
                    saveUnderDialog.setOnDialogClosed(event -> {
                        if (saveUnderDialog.getResponse() == Response.OK) {
                            handleRequest(Constants.Plugin.Command.RELOAD);
                        }
                    });

                    saveUnderDialog.show();
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
                JEVisObject selectedItem = trcs.getSelectionModel().getSelectedItem();
                if (allTemplateCalculations.isEmpty()) {
                    templateHandler.setRcTemplate(new RCTemplate());
                } else {
                    Platform.runLater(() -> {
                        trcs.getItems().clear();
                        trcs.getItems().addAll(allTemplateCalculations);

                        if (trcs.getItems().contains(selectedItem)) {
                            trcs.getSelectionModel().select(selectedItem);
                        } else {
                            trcs.getSelectionModel().selectFirst();
                        }
                    });
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
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.GAUGE, Plugin.IconSize, Plugin.IconSize,Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

        if (!initialized) {
            initialized = true;

            initToolBar();

            List<JEVisObject> allTemplateCalculations = getAllTemplateCalculations();
            if (allTemplateCalculations.isEmpty()) {
                templateHandler.setRcTemplate(new RCTemplate());
            } else {
                trcs.getItems().clear();
                trcs.getItems().addAll(allTemplateCalculations);
                trcs.getSelectionModel().selectFirst();
            }

            initGui();
        }

    }

    private void initGui() {
        viewTab = new OutputView(I18n.getInstance().getString("menu.view"), ds, templateHandler);

        viewTab.setClosable(false);
        viewTab.showInputs(true);

        configurationTab.setClosable(false);

        if (templateHandler.getRcTemplate() != null) {
            viewTab.updateViewInputFlowPane();
            viewTab.requestUpdate();
        }

        configFormulas.getChildren().add(buildAddFormulaButton());
        configInputs.getChildren().add(buildAddInputButton());

        Label formulaLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel"));
//        Label formulaInputsLabel = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.formulalabel") + " " + I18n.getInstance().getString("plugin.dtrc.dialog.inputslabel"));
        Label inputsLabel2 = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.inputslabel"));
        Label outputsLabel2 = new Label(I18n.getInstance().getString("plugin.dtrc.dialog.outputslabel"));

        Separator separator3 = new Separator(Orientation.HORIZONTAL);
        separator3.setPadding(new Insets(8, 0, 8, 0));

        Separator separator4 = new Separator(Orientation.HORIZONTAL);
        separator4.setPadding(new Insets(8, 0, 8, 0));

        Separator separator5 = new Separator(Orientation.HORIZONTAL);
        separator5.setPadding(new Insets(8, 0, 8, 0));

        VBox configVBox = new VBox(4, formulaLabel, configFormulas, separator3,
//        VBox configVBox = new VBox(4, formulaLabel, configFormulas, separator3, formulaInputsLabel, configFormulaInputs, separator4,
                inputsLabel2, configInputs, separator5, new HBox(outputsLabel2, buildAddOutputButton()), configOutputs);

        configVBox.setPadding(new Insets(12));

        ScrollPane configScrollPane = new ScrollPane(configVBox);
        configScrollPane.setFitToHeight(true);
        configScrollPane.setFitToWidth(true);

        dialogStackPane = new StackPane(configScrollPane);

        configurationTab.setContent(dialogStackPane);

        tabPane.getTabs().setAll(viewTab, configurationTab);

        borderPane.setCenter(tabPane);

    }

    private JFXButton buildAddFormulaButton() {
        JFXButton addFormulaButton = new JFXButton("", ResourceLoader.getImage("list-add.png", 15, 15));

        addFormulaButton.setOnAction(event -> {
            TemplateFormula templateFormula = new TemplateFormula();

            TemplateCalculationFormulaDialog templateCalculationFormulaDialog = new TemplateCalculationFormulaDialog(dialogStackPane, ds, templateHandler.getRcTemplate(), templateFormula);
            templateCalculationFormulaDialog.show();
            templateCalculationFormulaDialog.setOnDialogClosed(event1 -> {
                if (templateCalculationFormulaDialog.getResponse() == Response.OK) {
                    templateHandler.getRcTemplate().getTemplateFormulas().add(templateFormula);

                    createInputFromFormula(templateFormula);

                    updateFormulas();
                }
            });
        });

        return addFormulaButton;
    }

    private void createInputFromFormula(TemplateFormula templateFormula) {
        TemplateInput formulaInput = new TemplateInput();
        formulaInput.setVariableName(templateFormula.getName());
        formulaInput.setTemplateFormula(templateFormula.getName());
        formulaInput.setVariableType(InputVariableType.FORMULA.toString());
        templateHandler.getRcTemplate().getTemplateInputs().add(formulaInput);
    }

    private void deleteInputFromFormula(TemplateFormula templateFormula) {
        List<TemplateInput> templateInputsToRemove = new ArrayList<>();
        templateHandler.getRcTemplate().getTemplateInputs().forEach(templateInput -> {
            if (templateInput.getVariableName().equals(templateFormula.getName()) && templateInput.getVariableType().equals(InputVariableType.FORMULA.toString())) {
                templateInputsToRemove.add(templateInput);
            }
        });
        templateHandler.getRcTemplate().getTemplateInputs().removeAll(templateInputsToRemove);
    }

    private JFXButton buildAddInputButton() {
        JFXButton addInputButton = new JFXButton("", ResourceLoader.getImage("list-add.png", 15, 15));

        addInputButton.setOnAction(event -> {
            TemplateInput templateInput = new TemplateInput();

            TemplateCalculationInputDialog templateCalculationInputDialog = new TemplateCalculationInputDialog(dialogStackPane, ds, templateHandler.getRcTemplate(), templateInput);
            templateCalculationInputDialog.show();

            templateCalculationInputDialog.setOnDialogClosed(event1 -> {
                if (templateCalculationInputDialog.getResponse() == Response.OK) {
                    templateHandler.getRcTemplate().getTemplateInputs().add(templateInput);
                    updateInputs();
                }
            });
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

            TemplateCalculationOutputDialog templateCalculationOutputDialog = new TemplateCalculationOutputDialog(dialogStackPane, ds, templateOutput);
            templateCalculationOutputDialog.show();

            templateCalculationOutputDialog.setOnDialogClosed(event1 -> {
                if (templateCalculationOutputDialog.getResponse() == Response.OK) {
                    templateHandler.getRcTemplate().getTemplateOutputs().add(templateOutput);
                    updateOutputs();
                }
            });
        });

        return addOutputButton;
    }

    private void updateFormulas() {

        configFormulas.getChildren().clear();
        configFormulaInputs.getChildren().clear();
        configFormulas.getChildren().add(buildAddFormulaButton());
        templateHandler.getRcTemplate().getTemplateFormulas().sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));

        for (TemplateFormula templateFormula : templateHandler.getRcTemplate().getTemplateFormulas()) {
            int index = templateHandler.getRcTemplate().getTemplateFormulas().indexOf(templateFormula);
            configFormulas.getChildren().add(createFormulaButton(templateFormula, index));
            configFormulaInputs.getChildren().add(createFormulaInputButton(templateFormula, index));
        }
    }

    private void updateInputs() {

        configInputs.getChildren().clear();
        configInputs.getChildren().add(buildAddInputButton());
        templateHandler.getRcTemplate().getTemplateInputs().sort((o1, o2) -> alphanumComparator.compare(o1.getVariableName(), o2.getVariableName()));

        for (TemplateInput templateInput : templateHandler.getRcTemplate().getTemplateInputs()) {
            configInputs.getChildren().add(createInputButton(templateInput));
        }

        viewTab.updateViewInputFlowPane();
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

        if (templateFormula.getName() == null || templateFormula.getName().equals("")) {
            formulaButton.setText(String.valueOf(index));
        }

        formulaButton.setOnAction(event -> {
            TemplateCalculationFormulaDialog templateCalculationFormulaDialog = new TemplateCalculationFormulaDialog(dialogStackPane, ds, templateHandler.getRcTemplate(), templateFormula);
            templateCalculationFormulaDialog.show();

            templateCalculationFormulaDialog.setOnDialogClosed(event1 -> {
                if (templateCalculationFormulaDialog.getResponse() == Response.DELETE) {
                    templateHandler.getRcTemplate().getTemplateFormulas().remove(templateFormula);

                    deleteInputFromFormula(templateFormula);
                }
                updateFormulas();
            });
        });

        return formulaButton;
    }

    private JFXButton createFormulaInputButton(TemplateFormula templateFormula, int index) {

        JFXButton formulaInputButton = new JFXButton(templateFormula.getName());
        formulaInputButton.setMnemonicParsing(false);
        formulaInputButton.setStyle("-fx-background-color: derive(-fx-base, 120%);");

        if (templateFormula.getName() == null || templateFormula.getName().equals("")) {
            formulaInputButton.setText(String.valueOf(index));
        }

        return formulaInputButton;
    }

    private JFXButton createInputButton(TemplateInput templateInput) {
        JFXButton inputButton = new JFXButton(templateInput.getVariableName());
        inputButton.setMnemonicParsing(false);

        inputButton.setOnAction(event -> {
            TemplateCalculationInputDialog templateCalculationInputDialog = new TemplateCalculationInputDialog(dialogStackPane, ds, templateHandler.getRcTemplate(), templateInput);
            templateCalculationInputDialog.show();

            templateCalculationInputDialog.setOnDialogClosed(event1 -> {
                if (templateCalculationInputDialog.getResponse() == Response.DELETE) {
                    templateHandler.getRcTemplate().getTemplateInputs().remove(templateInput);
                }
                updateInputs();
            });
        });

        return inputButton;
    }

    private JFXButton createOutputButton(TemplateOutput templateOutput) {
        JFXButton outputButton = new JFXButton(templateOutput.getName());
        outputButton.setMnemonicParsing(false);

        outputButton.setOnAction(event -> {
            TemplateCalculationOutputDialog templateCalculationOutputDialog = new TemplateCalculationOutputDialog(dialogStackPane, ds, templateOutput);
            templateCalculationOutputDialog.show();

            templateCalculationOutputDialog.setOnDialogClosed(event1 -> {
                if (templateCalculationOutputDialog.getResponse() == Response.DELETE) {
                    templateHandler.getRcTemplate().getTemplateOutputs().remove(templateOutput);
                }
                updateOutputs();
            });
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
        return 9;
    }

}
