package org.jevis.jeconfig.plugin.meters;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Callback;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.MeterDialog;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.TablePlugin;
import org.jevis.jeconfig.plugin.charts.TableViewContextMenuHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

public class MeterPlugin extends TablePlugin {
    public static final String MEASUREMENT_INSTRUMENT_CLASS = "Measurement Instrument";
    public static final String MEASUREMENT_INSTRUMENT_DIRECTORY_CLASS = "Measurement Directory";
    private static final double EDITOR_MAX_HEIGHT = 50;
    public static String PLUGIN_NAME = "Meter Plugin";
    private final Image taskImage = JEConfig.getImage("measurement_instrument.png");

    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.MeterPlugin");
    private final ToolBar toolBar = new ToolBar();
    private boolean initialized = false;
    private final ToggleButton replaceButton = new ToggleButton("", JEConfig.getImage("text_replace.png", toolBarIconSize, toolBarIconSize));
    private int selectedIndex = 0;
    private final JFXButton renameButton = new JFXButton(I18n.getInstance().getString("plugin.meters.button.rename"));

    public MeterPlugin(JEVisDataSource ds, String title) {
        super(ds, title);
        this.borderPane.setCenter(tabPane);

        borderPane.setOnKeyPressed(this::handleRename);

        initToolBar();
    }

    private void createColumns(TableView<RegisterTableRow> tableView, JEVisClass jeVisClass) {

        try {
            TableColumn<RegisterTableRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.meters.table.measurementpoint.columnname"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getName()));
            nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            nameColumn.setSortable(true);
            nameColumn.setSortType(TableColumn.SortType.ASCENDING);

            tableView.getColumns().add(nameColumn);
            tableView.getSortOrder().addAll(nameColumn);

            JEVisType onlineIdType = jeVisClass.getType("Online ID");
            JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
            JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");
            JEVisType locationType = jeVisClass.getType("Location");
            JEVisType pictureType = jeVisClass.getType("Picture");
            JEVisType measuringPointIdType = jeVisClass.getType("Measuring Point ID");
            JEVisType measuringPointName = jeVisClass.getType("Measuring Point Name");

            for (JEVisType type : jeVisClass.getTypes()) {
                TableColumn<RegisterTableRow, JEVisAttribute> column = new TableColumn<>(I18nWS.getInstance().getTypeName(jeVisClass.getName(), type.getName()));
                column.setStyle("-fx-alignment: CENTER;");
                column.setSortable(false);

                column.setVisible(pref.getBoolean(type.getName(), true));
                column.visibleProperty().addListener((observable, oldValue, newValue) -> {
                    try {
                        pref.putBoolean(type.getName(), newValue);
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                });

                column.setId(type.getName());
                column.setCellValueFactory(param -> {
                    try {
                        return new ReadOnlyObjectWrapper<>(param.getValue().getObject().getAttribute(type));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new ReadOnlyObjectWrapper<>();
                });

                switch (type.getPrimitiveType()) {
                    case JEVisConstants.PrimitiveType.LONG:
                        column.setCellFactory(valueCellInteger());
                        break;
                    case JEVisConstants.PrimitiveType.DOUBLE:
                        column.setCellFactory(valueCellDouble());
                        break;
                    case JEVisConstants.PrimitiveType.FILE:
                        column.setCellFactory(valueCellFile());
                        column.setMinWidth(125);
                        break;
                    case JEVisConstants.PrimitiveType.BOOLEAN:
                        column.setCellFactory(valueCellBoolean());
                        break;
                    default:
                        if (type.getName().equalsIgnoreCase("Password") || type.getPrimitiveType() == JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                            column.setCellFactory(valueCellStringPassword());
                        } else if (type.getGUIDisplayType().equals(GUIConstants.TARGET_OBJECT.getId()) || type.getGUIDisplayType().equals(GUIConstants.TARGET_ATTRIBUTE.getId())) {
                            column.setCellFactory(valueCellTargetSelection());
                            column.setMinWidth(120);
                        } else if (type.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || type.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                            column.setCellFactory(valueCellDateTime());
                            column.setMinWidth(110);
                        } else {
                            column.setCellFactory(valueCellString());
                        }

                        break;
                }

                tableView.getColumns().add(column);

                if (type.equals(onlineIdType)) {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
                    numberFormat.setMinimumFractionDigits(2);
                    numberFormat.setMaximumFractionDigits(2);
                    TableColumn<RegisterTableRow, Object> lastValueColumn = new TableColumn<>(I18n.getInstance().getString("status.table.captions.lastrawvalue"));
                    lastValueColumn.setStyle("-fx-alignment: CENTER;");
                    lastValueColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject()));
                    lastValueColumn.setCellFactory(new Callback<TableColumn<RegisterTableRow, Object>, TableCell<RegisterTableRow, Object>>() {
                        @Override
                        public TableCell<RegisterTableRow, Object> call(TableColumn<RegisterTableRow, Object> param) {
                            return new TableCell<RegisterTableRow, Object>() {
                                @Override
                                protected void updateItem(Object item, boolean empty) {
                                    super.updateItem(item, empty);

                                    if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                                        setText(null);
                                        setGraphic(null);
                                    } else {
                                        RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();
                                        JEVisAttribute jeVisAttribute = registerTableRow.getAttributeMap().get(onlineIdType);

                                        if (jeVisAttribute != null) {
                                            try {
                                                TargetHelper th = new TargetHelper(ds, jeVisAttribute);

                                                if (th.isValid() && th.targetAccessible()) {

                                                    JEVisAttribute att = th.getObject().get(0).getAttribute("Value");
                                                    JEVisAttribute periodAtt = th.getObject().get(0).getAttribute("Period");

                                                    if (att != null && att.hasSample() && periodAtt != null) {
                                                        JEVisSample latestSample = att.getLatestSample();
                                                        JEVisSample periodSample = periodAtt.getLatestSample();
                                                        Period period = new Period(periodSample.getValueAsString());
                                                        boolean isCounter = CleanDataObject.isCounter(att.getObject(), latestSample);
                                                        JEVisUnit displayUnit = att.getDisplayUnit();
                                                        String unitString = UnitManager.getInstance().format(displayUnit);
                                                        String normalPattern = PeriodHelper.getFormatString(period, isCounter);

                                                        String timeString = latestSample.getTimestamp().toString(normalPattern);

                                                        setText(numberFormat.format(latestSample.getValueAsDouble()) + " " + unitString + " @ " + timeString);
                                                    }


                                                }
                                            } catch (Exception e) {
                                                setText(null);
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            };
                        }
                    });

                    tableView.getColumns().add(lastValueColumn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            JEVisClassTab selectedItem = (JEVisClassTab) tabPane.getSelectionModel().getSelectedItem();
            TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();

            MeterDialog meterDialog = new MeterDialog(dialogContainer, getDataSource(), selectedItem.getJeVisClass());
            meterDialog.setOnDialogClosed(event1 -> {
                if (meterDialog.getResponse() == Response.OK) {
                    handleRequest(Constants.Plugin.Command.RELOAD);
                }
            });
            meterDialog.showReplaceWindow(tableView.getSelectionModel().getSelectedItem().getObject());
        });
        replaceButton.setDisable(true);

        renameButton.setTooltip(new Tooltip("F2"));
        renameButton.setOnAction(event -> openRenameDialog());
        renameButton.setDisable(true);

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
        replaceButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.replace.tooltip")));
        printButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print")));

        toolBar.getItems().setAll(filterInput, reload, sep1, save, sep2, newButton, replaceButton, renameButton, sep3, printButton);
        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

        JEVisHelp.getInstance().addHelpItems(MeterPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());
    }

    @Override
    public String getClassName() {
        return "Meter Plugin";
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
        return I18n.getInstance().getString("plugin.meters.tooltip");
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
    public void setDataSource(JEVisDataSource ds) {

    }

    @Override
    public void handleRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                DateTime saveTime = new DateTime();
                for (Map.Entry<JEVisAttribute, AttributeValueChange> entry : getChangeMap().entrySet()) {
                    JEVisAttribute a = entry.getKey();
                    AttributeValueChange attributeValueChange = entry.getValue();
                    try {
                        attributeValueChange.commit(saveTime);
                    } catch (JEVisException e) {
                        logger.error("Could not save {} for attribute {} of object {}:{}", attributeValueChange.toString(), a.getName(), a.getObject().getName(), a.getObject().getID(), e);
                    }

                }
                break;
            case Constants.Plugin.Command.DELETE:
                Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
                TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();

                JEVisObject object = tableView.getSelectionModel().getSelectedItem().getObject();

                Dialog<ButtonType> reallyDelete = new Dialog<>();
                reallyDelete.setTitle(I18n.getInstance().getString("plugin.graph.dialog.delete.title"));
                final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"), ButtonBar.ButtonData.YES);
                final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                reallyDelete.setContentText(I18n.getInstance().getString("plugin.meters.dialog.delete.message"));
                reallyDelete.getDialogPane().getButtonTypes().addAll(ok, cancel);
                reallyDelete.showAndWait().ifPresent(response -> {
                    if (response.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
                        try {
                            if (getDataSource().getCurrentUser().canDelete(object.getID())) {
                                getDataSource().deleteObject(object.getID());
                                handleRequest(Constants.Plugin.Command.RELOAD);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.meters.dialog.delete.error"), cancel);
                                alert.showAndWait();
                            }
                        } catch (JEVisException e) {
                            logger.error("Error: could not delete current meter", e);
                        }
                    }
                });
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                MeterDialog meterDialog = new MeterDialog(dialogContainer, getDataSource(), ((JEVisClassTab) tabPane.getSelectionModel().getSelectedItem()).getJeVisClass());
                meterDialog.setOnDialogClosed(event -> {
                    if (meterDialog.getResponse() == Response.OK) {
                        handleRequest(Constants.Plugin.Command.RELOAD);
                    }
                });

                meterDialog.show();
                break;
            case Constants.Plugin.Command.RELOAD:
                Platform.runLater(() -> {
                    replaceButton.setDisable(true);
                    renameButton.setDisable(true);
                });
                selectedIndex = tabPane.getSelectionModel().getSelectedIndex();

                Task clearCacheTask = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            this.updateTitle(I18n.getInstance().getString("Clear Cache"));
                            if (initialized) {
                                getDataSource().clearCache();
                                getDataSource().preload();
                            } else {
                                initialized = true;
                            }
                            updateList();
                            succeeded();
                        } catch (Exception ex) {
                            failed();
                        } finally {
                            done();
                        }
                        return null;
                    }
                };
                JEConfig.getStatusBar().addTask(PLUGIN_NAME, clearCacheTask, JEConfig.getImage("measurement_instrument.png"), true);

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

    private void loadTabs(Map<JEVisClass, List<JEVisObject>> allMeters, List<JEVisClass> classes) throws InterruptedException {
        AtomicBoolean hasActiveLoadTask = new AtomicBoolean(false);
        for (Map.Entry<Task, String> entry : JEConfig.getStatusBar().getTaskList().entrySet()) {
            Task task = entry.getKey();
            String s = entry.getValue();
            if (s.equals(MeterPlugin.class.getName() + "Load")) {
                hasActiveLoadTask.set(true);
                break;
            }
        }
        if (!hasActiveLoadTask.get()) {
            classes.forEach(jeVisClass -> {
                Task<JEVisClassTab> task = new Task<JEVisClassTab>() {
                    @Override
                    protected JEVisClassTab call() {
                        TableView<RegisterTableRow> tableView = new TableView<>();

                        JEVisClassTab tab = new JEVisClassTab();
                        List<JEVisObject> listObjects = allMeters.get(jeVisClass);

                        TableViewContextMenuHelper contextMenuHelper = new TableViewContextMenuHelper(tableView);

                        if (contextMenuHelper.getTableHeaderRow() != null) {
                            contextMenuHelper.getTableHeaderRow().setOnMouseClicked(event -> {
                                if (event.getButton() == MouseButton.SECONDARY) {
                                    contextMenuHelper.showContextMenu();
                                }
                            });
                        }

                        try {
                            tab.setClassName(I18nWS.getInstance().getClassName(jeVisClass));
                            tab.setTableView(tableView);
                            tab.setJEVisClass(jeVisClass);

                            tableView.setFixedCellSize(EDITOR_MAX_HEIGHT);
                            tableView.setTableMenuButtonVisible(true);

                            tab.setClosable(false);
                            createColumns(tableView, jeVisClass);

                            tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue != oldValue && newValue != null) {
                                    Platform.runLater(() -> {
                                        replaceButton.setDisable(false);
                                        renameButton.setDisable(false);
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        replaceButton.setDisable(true);
                                        renameButton.setDisable(true);
                                    });
                                }
                            });

                            tableView.setOnKeyPressed(event -> handleRename(event));

                            ObservableList<RegisterTableRow> registerTableRows = FXCollections.observableArrayList();
                            JEVisType onlineIdType = jeVisClass.getType("Online ID");
                            JEVisClass cleanDataClass = getDataSource().getJEVisClass("Clean Data");
                            JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");

                            for (JEVisObject meterObject : listObjects) {
                                Map<JEVisType, JEVisAttribute> map = new HashMap<>();

                                for (JEVisAttribute meterObjectAttribute : meterObject.getAttributes()) {
                                    JEVisType type = null;

                                    try {
                                        type = meterObjectAttribute.getType();

                                        map.put(type, meterObjectAttribute);

                                        if (type.equals(onlineIdType)) {
                                            if (meterObjectAttribute.hasSample()) {
                                                TargetHelper th = new TargetHelper(getDataSource(), meterObjectAttribute);

                                                if (!th.getObject().isEmpty()) {
                                                    List<JEVisObject> children = th.getObject().get(0).getChildren(cleanDataClass, true);

                                                    for (JEVisObject cleanObject : children) {
                                                        JEVisAttribute cleanObjectAttribute = cleanObject.getAttribute(multiplierType);

                                                        if (cleanObjectAttribute != null) {
                                                            map.put(multiplierType, cleanObjectAttribute);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                RegisterTableRow tableData = new RegisterTableRow(map, meterObject, isMultiSite());
                                registerTableRows.add(tableData);
                            }

                            tab.setFilteredList(new FilteredList<>(registerTableRows, s -> true));
                            Platform.runLater(() -> tableView.setItems(tab.getFilteredList()));
                            this.succeeded();
                        } catch (Exception e) {
                            logger.error(e);
                            this.failed();
                        } finally {
                            Platform.runLater(() -> {
                                tabPane.getTabs().add(tab);
                                tabPane.getTabs().sort((o1, o2) -> alphanumComparator.compare(o1.getText(), o2.getText()));

                                if (tabPane.getTabs().size() > selectedIndex) {
                                    tabPane.getSelectionModel().select(selectedIndex);
                                }
                            });
                            Platform.runLater(() -> autoFitTable(tableView));
                            Platform.runLater(tableView::sort);
                            this.done();
                        }
                        return tab;
                    }
                };
                JEConfig.getStatusBar().addTask(MeterPlugin.class.getName(), task, taskImage, true);
            });
        } else {
            Thread.sleep(500);
            loadTabs(allMeters, classes);
        }
    }

    private void updateList() {

        Platform.runLater(() -> tabPane.getTabs().clear());
        getChangeMap().clear();

        List<JEVisClass> classes = new ArrayList<>();
        Map<JEVisClass, List<JEVisObject>> allMeters = new HashMap<>();
        Task load = new Task() {
            @Override
            protected Object call() throws Exception {
                allMeters.putAll(getAllMeters());

                allMeters.forEach((key, list) -> classes.add(key));
                AlphanumComparator ac = new AlphanumComparator();
                classes.sort((o1, o2) -> {
                    try {
                        return ac.compare(I18nWS.getInstance().getClassName(o1.getName()), I18nWS.getInstance().getClassName(o2.getName()));
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                    return 1;
                });
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(MeterPlugin.class.getName() + "Load", load, taskImage, true);

        Task loadTabs = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    loadTabs(allMeters, classes);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(MeterPlugin.class.getName(), loadTabs, taskImage, true);
    }

    private Map<JEVisClass, List<JEVisObject>> getAllMeters() {
        Map<JEVisClass, List<JEVisObject>> map = new HashMap<>();
        try {
            JEVisClass meterClass = getDataSource().getJEVisClass(MEASUREMENT_INSTRUMENT_CLASS);
            List<JEVisObject> allObjects = getDataSource().getObjects(meterClass, true);
            for (JEVisObject object : allObjects) {
                if (!map.containsKey(object.getJEVisClass())) {
                    List<JEVisObject> objectArrayList = new ArrayList<>();
                    objectArrayList.add(object);
                    map.put(object.getJEVisClass(), objectArrayList);
                } else {
                    map.get(object.getJEVisClass()).add(object);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return map;
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

        Task loadTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    this.updateTitle(I18n.getInstance().getString("plugin.meters.load"));
                    if (!initialized) {
                        initialized = true;
                        boolean isMultiSite = isMultiSite(MeterPlugin.MEASUREMENT_INSTRUMENT_DIRECTORY_CLASS);
                        updateList();
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

    private void handleRename(KeyEvent event) {
        if (event.getCode() == KeyCode.F2) {
            openRenameDialog();
        }
    }

    private void openRenameDialog() {
        Tab selectedItem = tabPane.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getContent() instanceof TableView) {

            TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();

            if (tableView.getSelectionModel().getSelectedItem() != null) {
                try {
                    MeterRenameDialog meterRenameDialog = new MeterRenameDialog(dialogContainer, tableView.getSelectionModel().getSelectedItem());
                    meterRenameDialog.show();
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
