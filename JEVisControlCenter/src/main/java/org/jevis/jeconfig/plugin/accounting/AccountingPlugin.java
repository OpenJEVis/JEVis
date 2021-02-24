package org.jevis.jeconfig.plugin.accounting;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.DayBox;
import org.jevis.jeconfig.application.control.MonthBox;
import org.jevis.jeconfig.application.control.YearBox;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.EquipmentDialog;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.TablePlugin;
import org.jevis.jeconfig.plugin.meters.JEVisClassTab;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.jevis.jeconfig.plugin.object.attribute.AttributeEditor;
import org.jevis.jeconfig.plugin.object.attribute.PeriodEditor;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

public class AccountingPlugin extends TablePlugin implements Plugin {
    public static final String ACCOUNTING_CLASS = "Energy Contracting Directory";
    public static final String ENERGY_SUPPLY_CONTRACTOR = "Energy Supply Contractor";
    public static final String ENERGY_METERING_POINT_OPERATION_CONTRACTOR = "Energy Metering Point Operation Contractor";
    public static final String ENERGY_GRID_OPERATION_CONTRACTOR = "Energy Grid Operation Contractor";
    public static final Insets INSETS = new Insets(12);
    public static final String ENERGY_SUPPLIER = "Energy Supplier";
    public static final String ENERGY_METERING_POINT_OPERATOR = "Energy Metering Point Operator";
    public static final String ENERGY_GRID_OPERATOR = "Energy Grid Operator";
    public static final String ENERGY_CONTRACTOR = "Energy Contractor";
    public static final String GOVERNMENTAL_DUES = "Governmental Dues";
    public static final String ENERGY_SUPPLY_DIRECTORY = "Energy Supply Directory";
    public static final String ENERGY_METERING_POINT_OPERATION_DIRECTORY = "Energy Metering Point Operation Directory";
    public static final String ENERGY_GRID_OPERATION_DIRECTORY = "Energy Grid Operation Directory";
    public static final String ENERGY_CONTRACTOR_DIRECTORY = "Energy Contractor Directory";
    private static final String PLUGIN_CLASS_NAME = "Accounting Plugin";
    private static final Logger logger = LogManager.getLogger(AccountingPlugin.class);
    private static final double EDITOR_MAX_HEIGHT = 50;
    public static String PLUGIN_NAME = "Accounting Plugin";
    final PseudoClass header = PseudoClass.getPseudoClass("section-header");
    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.AccountingPlugin");
    private final Image taskImage = JEConfig.getImage("accounting.png");
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private final JFXTabPane motherTabPane = new JFXTabPane();
    private final JFXTabPane billingTabPane = new JFXTabPane();
    private final JFXTabPane enterDataTabPane = new JFXTabPane();
    private final JFXTabPane configTabPane = new JFXTabPane();
    private final ToggleButton replaceButton = new ToggleButton("", JEConfig.getImage("text_replace.png", toolBarIconSize, toolBarIconSize));
    private final Callback<ListView<JEVisObject>, ListCell<JEVisObject>> objectNameCellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
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

    private final Callback<ListView<ComboBoxItem>, ListCell<ComboBoxItem>> comboBoxItemCellFactory = new Callback<ListView<ComboBoxItem>, ListCell<ComboBoxItem>>() {
        @Override
        public ListCell<ComboBoxItem> call(ListView<ComboBoxItem> param) {
            return new JFXListCell<ComboBoxItem>() {
                @Override
                protected void updateItem(ComboBoxItem obj, boolean empty) {
                    super.updateItem(obj, empty);
                    if (empty) {
                        setText(null);
                        setDisable(false);
                        pseudoClassStateChanged(header, false);
                    } else {
                        setText(obj.toString());
                        setDisable(!obj.isSelectable());
                        pseudoClassStateChanged(header, !obj.isSelectable());
                    }
                }
            };
        }
    };
    private boolean initialized = false;

    public AccountingPlugin(JEVisDataSource ds, String title) {
        super(ds, title);

        Tab viewTab = new Tab(I18n.getInstance().getString("plugin.accounting.tab.view"));
        viewTab.setContent(billingTabPane);

        Tab enterDataTab = new Tab(I18n.getInstance().getString("plugin.accounting.tab.enterdata"));
        enterDataTab.setContent(enterDataTabPane);

        Tab configTab = new Tab(I18n.getInstance().getString("plugin.accounting.tab.config"));
        configTab.setContent(configTabPane);

        motherTabPane.getTabs().addAll(viewTab, enterDataTab, configTab);

        this.borderPane.setCenter(motherTabPane);

        initToolBar();
    }


    private boolean isMultiSite() {

        try {
            JEVisClass equipmentRegisterClass = ds.getJEVisClass(ACCOUNTING_CLASS);
            List<JEVisObject> objects = ds.getObjects(equipmentRegisterClass, false);

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

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(replaceButton);
        replaceButton.setOnAction(event -> {
            JEVisClassTab selectedItem = (JEVisClassTab) motherTabPane.getSelectionModel().getSelectedItem();
            TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();

            EquipmentDialog meterDialog = new EquipmentDialog(ds, selectedItem.getJeVisClass());
            if (meterDialog.showReplaceWindow(tableView.getSelectionModel().getSelectedItem().getObject()) == Response.OK) {
                handleRequest(Constants.Plugin.Command.RELOAD);
            }
        });
        replaceButton.setDisable(true);

        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);
        delete.setOnAction(event -> handleRequest(Constants.Plugin.Command.DELETE));

        Separator sep3 = new Separator(Orientation.VERTICAL);

        ToggleButton printButton = new ToggleButton("", JEConfig.getImage("Print_1493286.png", toolBarIconSize, toolBarIconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        printButton.setOnAction(event -> {
            Tab selectedItem = motherTabPane.getSelectionModel().getSelectedItem();
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

        reload.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.accounting.toolbar.reload.tooltip")));
        save.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.accounting.toolbar.save.tooltip")));
        newButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.accounting.new.tooltip")));
        replaceButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.accounting.toolbar.replace.tooltip")));
        printButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.accounting.toolbar.tooltip.print")));


        toolBar.getItems().setAll(filterInput, reload, sep1, save, sep2, newButton, sep3, printButton);
        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        JEVisHelp.getInstance().addHelpItems(AccountingPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());
    }

    @Override
    public String getClassName() {
        return AccountingPlugin.PLUGIN_CLASS_NAME;
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
        return I18n.getInstance().getString("plugin.accounting.tooltip");
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

                break;
            case Constants.Plugin.Command.DELETE:

                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:

                break;
            case Constants.Plugin.Command.RELOAD:
                Platform.runLater(() -> replaceButton.setDisable(true));

                Task clearCacheTask = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            this.updateTitle(I18n.getInstance().getString("Clear Cache"));
                            if (initialized) {
                                ds.clearCache();
                                ds.preload();
                            } else {
                                initialized = true;

                                updateGUI();
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
                JEConfig.getStatusBar().addTask(PLUGIN_NAME, clearCacheTask, JEConfig.getImage("accounting.png"), true);

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


    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("accounting.png", 20, 20);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {

        Task loadTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    this.updateTitle(I18n.getInstance().getString("plugin.accounting.load"));
                    if (!initialized) {
                        initialized = true;

                        updateGUI();
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
        JEConfig.getStatusBar().addTask(PLUGIN_NAME, loadTask, JEConfig.getImage("accounting.png"), true);

    }

    private void updateGUI() throws JEVisException {
        enterDataTabPane.getTabs().clear();

        JEVisClass energySupplyDirectoryClass = ds.getJEVisClass(ENERGY_SUPPLY_DIRECTORY);
        JEVisClass energySupplierClass = ds.getJEVisClass(ENERGY_SUPPLIER);
        JEVisClass energyMeteringPointOperationDirectoryClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATION_DIRECTORY);
        JEVisClass energyMeteringOperatorClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATOR);
        JEVisClass energyGridOperationDirectoryClass = ds.getJEVisClass(ENERGY_GRID_OPERATION_DIRECTORY);
        JEVisClass energyGridOperatorClass = ds.getJEVisClass(ENERGY_GRID_OPERATOR);
        JEVisClass energyContractorDirectoryClass = ds.getJEVisClass(ENERGY_CONTRACTOR_DIRECTORY);
        JEVisClass energyContractorClass = ds.getJEVisClass(ENERGY_CONTRACTOR);
        JEVisClass energySupplyContractorClass = ds.getJEVisClass(ENERGY_SUPPLY_CONTRACTOR);
        JEVisClass energyMeteringPointOperationContractorClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATION_CONTRACTOR);
        JEVisClass energyGridOperationContractorClass = ds.getJEVisClass(ENERGY_GRID_OPERATION_CONTRACTOR);
        JEVisClass governmentalDuesClass = ds.getJEVisClass(GOVERNMENTAL_DUES);

        Tab energySupplierTab = new Tab(I18nWS.getInstance().getClassName(energySupplierClass));
        JFXComboBox<JEVisObject> energySupplierBox = new JFXComboBox<>();
        energySupplierBox.setCellFactory(objectNameCellFactory);
        energySupplierBox.setButtonCell(objectNameCellFactory.call(null));

        List<JEVisObject> allEnergySupplier = ds.getObjects(energySupplierClass, true);
        allEnergySupplier.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        energySupplierBox.setItems(FXCollections.observableArrayList(allEnergySupplier));

        DateTime nextTS = new DateTime();
        YearBox yearBox = new YearBox(nextTS);
        MonthBox monthBox = new MonthBox();
        DayBox dayBox = new DayBox();

        yearBox.setRelations(monthBox, dayBox);
        monthBox.setRelations(yearBox, dayBox, nextTS);

        GridPane esGP = new GridPane();
        esGP.setPadding(INSETS);
        esGP.setHgap(6);
        esGP.setVgap(6);
        VBox esVBox = new VBox(6, energySupplierBox, esGP);
        energySupplierTab.setContent(esVBox);

        energySupplierBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateGrid(esGP, newValue);
        });

        energySupplierBox.getSelectionModel().selectFirst();

        Tab energyMeteringOperatorsTab = new Tab(I18nWS.getInstance().getClassName(energyMeteringOperatorClass));
        JFXComboBox<JEVisObject> energyMeteringOperatorBox = new JFXComboBox<>();
        energyMeteringOperatorBox.setCellFactory(objectNameCellFactory);
        energyMeteringOperatorBox.setButtonCell(objectNameCellFactory.call(null));

        List<JEVisObject> allEnergyMeteringOperators = ds.getObjects(energyMeteringOperatorClass, true);
        allEnergyMeteringOperators.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        energyMeteringOperatorBox.setItems(FXCollections.observableArrayList(allEnergyMeteringOperators));

        GridPane emoGP = new GridPane();
        emoGP.setPadding(INSETS);
        emoGP.setHgap(6);
        emoGP.setVgap(6);
        VBox emoVBox = new VBox(6, energyMeteringOperatorBox, emoGP);
        energyMeteringOperatorsTab.setContent(emoVBox);

        energyMeteringOperatorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateGrid(emoGP, newValue);
        });

        energyMeteringOperatorBox.getSelectionModel().selectFirst();

        Tab energyGridOperatorsTab = new Tab(I18nWS.getInstance().getClassName(energyGridOperatorClass));
        JFXComboBox<JEVisObject> energyGridOperatorBox = new JFXComboBox<>();
        energyGridOperatorBox.setCellFactory(objectNameCellFactory);
        energyGridOperatorBox.setButtonCell(objectNameCellFactory.call(null));

        List<JEVisObject> allEnergyGridOperators = ds.getObjects(energyGridOperatorClass, true);
        allEnergyGridOperators.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        energyGridOperatorBox.setItems(FXCollections.observableArrayList(allEnergyGridOperators));

        GridPane egoGP = new GridPane();
        egoGP.setPadding(INSETS);
        egoGP.setHgap(6);
        egoGP.setVgap(6);
        VBox egoVBox = new VBox(6, energyGridOperatorBox, egoGP);
        energyGridOperatorsTab.setContent(egoVBox);

        energyGridOperatorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateGrid(egoGP, newValue);
        });

        energyGridOperatorBox.getSelectionModel().selectFirst();

        Tab energyContractorTab = new Tab(I18nWS.getInstance().getClassName(energyContractorClass));
        JFXComboBox<ComboBoxItem> energyContractorBox = new JFXComboBox<>();
        energyContractorBox.setCellFactory(comboBoxItemCellFactory);
        energyContractorBox.setButtonCell(comboBoxItemCellFactory.call(null));

        List<JEVisObject> energySupplyContractors = ds.getObjects(energySupplyContractorClass, false);
        energySupplyContractors.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        List<JEVisObject> energyMeteringPointContractors = ds.getObjects(energyMeteringPointOperationContractorClass, false);
        energyMeteringPointContractors.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        List<JEVisObject> energyGridOperationContractors = ds.getObjects(energyGridOperationContractorClass, false);
        energyGridOperationContractors.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));

        ObservableList<ComboBoxItem> allContractors = FXCollections.observableArrayList();
        allContractors.add(new ComboBoxItem(I18nWS.getInstance().getClassName(energySupplyContractorClass.getName()), false));
        energySupplyContractors.forEach(jeVisObject -> allContractors.add(new ComboBoxItem(jeVisObject, true)));
        allContractors.add(new ComboBoxItem("", false));
        allContractors.add(new ComboBoxItem(I18nWS.getInstance().getClassName(energyMeteringPointOperationContractorClass.getName()), false));
        energyMeteringPointContractors.forEach(jeVisObject -> allContractors.add(new ComboBoxItem(jeVisObject, true)));
        allContractors.add(new ComboBoxItem("", false));
        allContractors.add(new ComboBoxItem(I18nWS.getInstance().getClassName(energyGridOperationContractorClass.getName()), false));
        energyGridOperationContractors.forEach(jeVisObject -> allContractors.add(new ComboBoxItem(jeVisObject, true)));

        energyContractorBox.setItems(allContractors);

        GridPane cGP = new GridPane();
        cGP.setPadding(INSETS);
        cGP.setHgap(6);
        cGP.setVgap(6);
        VBox cVBox = new VBox(6, energyContractorBox, cGP);
        energyContractorTab.setContent(cVBox);

        energyContractorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateGrid(cGP, newValue.getObject());
        });

        if (!energySupplyContractors.isEmpty()) {
            energyContractorBox.getSelectionModel().select(new ComboBoxItem(energySupplyContractors.get(0), true));
        } else if (!energyMeteringPointContractors.isEmpty()) {
            energyContractorBox.getSelectionModel().select(new ComboBoxItem(energyMeteringPointContractors.get(0), true));
        } else if (!energyGridOperationContractors.isEmpty()) {
            energyContractorBox.getSelectionModel().select(new ComboBoxItem(energyGridOperationContractors.get(0), true));
        }

        Tab governmentalDuesTab = new Tab(I18nWS.getInstance().getClassName(governmentalDuesClass));
        JFXComboBox<JEVisObject> governmentalDuesBox = new JFXComboBox<>();
        governmentalDuesBox.setCellFactory(objectNameCellFactory);
        governmentalDuesBox.setButtonCell(objectNameCellFactory.call(null));

        List<JEVisObject> allGovernmentalDues = ds.getObjects(governmentalDuesClass, true);
        allGovernmentalDues.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
        governmentalDuesBox.setItems(FXCollections.observableArrayList(allGovernmentalDues));

        GridPane gdGP = new GridPane();
        gdGP.setPadding(INSETS);
        gdGP.setHgap(6);
        gdGP.setVgap(6);
        VBox gdVBox = new VBox(6, governmentalDuesBox, gdGP);
        governmentalDuesTab.setContent(gdVBox);

        governmentalDuesBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateGrid(gdGP, newValue);
        });

        governmentalDuesBox.getSelectionModel().selectFirst();

        Platform.runLater(() -> enterDataTabPane.getTabs().addAll(energySupplierTab, energyMeteringOperatorsTab, energyGridOperatorsTab, energyContractorTab, governmentalDuesTab));
    }

    private void updateGrid(GridPane gp, JEVisObject selectedObject) {
        if (selectedObject != null) {
            gp.getChildren().clear();
//            attributeEditors.clear();
            try {
                JEVisClass energySupplyContractorClass = ds.getJEVisClass(ENERGY_SUPPLY_CONTRACTOR);
                JEVisClass energyMeteringPointOperationContractorClass = ds.getJEVisClass(ENERGY_METERING_POINT_OPERATION_CONTRACTOR);
                JEVisClass energyGridOperationContractorClass = ds.getJEVisClass(ENERGY_GRID_OPERATION_CONTRACTOR);
                int column = 0;
                int row = 0;

                List<JEVisAttribute> attributes = selectedObject.getAttributes();
                attributes.sort(Comparator.comparingInt(o -> {
                    try {
                        return o.getType().getGUIPosition();
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }));

                for (JEVisAttribute attribute : attributes) {
                    int index = attributes.indexOf(attribute);
                    JEVisObject contractor = null;
                    boolean isContractorAttribute = false;
                    if (attribute.getName().equals("Contractor")) {
                        isContractorAttribute = true;
                        if (attribute.hasSample()) {
                            JEVisSample latestSample = attribute.getLatestSample();
                            if (latestSample != null) {
                                TargetHelper th = new TargetHelper(ds, latestSample.getValueAsString());
                                if (th.isValid() && th.targetAccessible()) {
                                    contractor = th.getObject().get(0);
                                }
                            }
                        }
                    }

                    if (index == 2 || (index > 2 && index % 2 == 0)) {
                        column = 0;
                        row++;
                    }

                    Label typeName = new Label(I18nWS.getInstance().getTypeName(attribute.getType()));
                    VBox typeBox = new VBox(typeName);
                    typeBox.setAlignment(Pos.CENTER);
                    VBox editorBox = new VBox();

                    if (!isContractorAttribute) {
                        AttributeEditor attributeEditor = GenericAttributeExtension.getEditor(attribute.getType(), attribute);
                        attributeEditor.setReadOnly(false);
                        if (attribute.getType().getGUIDisplayType().equals("Period")) {
                            PeriodEditor periodEditor = (PeriodEditor) attributeEditor;
                            periodEditor.showTs(false);
                        }
//                    attributeEditors.add(attributeEditor);
                        editorBox.getChildren().setAll(attributeEditor.getEditor());
                        editorBox.setAlignment(Pos.CENTER);
                    } else {
                        JFXComboBox<JEVisObject> contractorBox = new JFXComboBox<>();
                        contractorBox.setCellFactory(objectNameCellFactory);
                        contractorBox.setButtonCell(objectNameCellFactory.call(null));

                        List<JEVisObject> allContractors = new ArrayList<>();
                        String jeVisClassName = selectedObject.getJEVisClass().getInheritance().getName();
                        if (ENERGY_SUPPLIER.equals(jeVisClassName)) {
                            allContractors.addAll(ds.getObjects(energySupplyContractorClass, false));
                        } else if (ENERGY_METERING_POINT_OPERATOR.equals(jeVisClassName)) {
                            allContractors.addAll(ds.getObjects(energyMeteringPointOperationContractorClass, false));
                        } else if (ENERGY_GRID_OPERATOR.equals(jeVisClassName)) {
                            allContractors.addAll(ds.getObjects(energyGridOperationContractorClass, false));
                        }

                        contractorBox.setItems(FXCollections.observableArrayList(allContractors));

                        if (contractor != null) {
                            contractorBox.getSelectionModel().select(contractor);
                        }

                        contractorBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                            try {
                                JEVisSample newSample = attribute.buildSample(new DateTime(), newValue.getID());
                                newSample.commit();
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        });

                        editorBox.getChildren().setAll(contractorBox);
                        editorBox.setAlignment(Pos.CENTER_LEFT);
                    }


                    if (column < 2) {
                        gp.add(typeBox, column, row);
                    } else {
                        gp.add(typeBox, column + 1, row);
                    }
                    column++;

                    if (column < 2) {
                        gp.add(editorBox, column, row);
                    } else {
                        gp.add(editorBox, column + 1, row);
                    }
                    column++;
                }

                Separator separator = new Separator(Orientation.VERTICAL);
                gp.add(separator, 2, 0, 1, row + 1);

                row++;

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 6;
    }

    public static class ComboBoxItem {
        private final String name;
        private final JEVisObject object;
        private final boolean selectable;

        public ComboBoxItem(JEVisObject object, boolean selectable) {
            this.object = object;
            this.name = object.getName();
            this.selectable = selectable;
        }

        public ComboBoxItem(String name, boolean selectable) {
            this.object = null;
            this.name = name;
            this.selectable = selectable;
        }


        public String getName() {
            return name;
        }

        public JEVisObject getObject() {
            return object;
        }

        public boolean isSelectable() {
            return selectable;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
