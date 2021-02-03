package org.jevis.jeconfig.plugin.basedata;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.TablePlugin;
import org.jevis.jeconfig.plugin.charts.TableViewContextMenuHelper;
import org.jevis.jeconfig.plugin.meters.JEVisClassTab;
import org.jevis.jeconfig.plugin.meters.MeterPlugin;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

public class BaseDataPlugin extends TablePlugin implements Plugin {
    public static final String BASE_DATA_CLASS = "Base Data";
    private static final Logger logger = LogManager.getLogger(BaseDataPlugin.class);
    private static final double EDITOR_MAX_HEIGHT = 50;
    public static String PLUGIN_NAME = "Base Data Plugin";
    private final Image taskImage = JEConfig.getImage("building_equipment.png");
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private final TabPane tabPane = new TabPane();
    private boolean initialized = false;
    private JEVisClass baseDataClass;
    private final Preferences pref = Preferences.userRoot().node("JEVis.JEConfig.BaseDataPlugin");
    private int selectedIndex = 0;

    public BaseDataPlugin(JEVisDataSource ds, String title) {
        super(ds, title);
        this.borderPane.setCenter(tabPane);

        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                Tab selectedItem = this.tabPane.getSelectionModel().getSelectedItem();
                Platform.runLater(() -> {
                    if (selectedItem != null && selectedItem.getContent() instanceof TableView) {

                        TableView<RegisterTableRow> tableView = (TableView<RegisterTableRow>) selectedItem.getContent();

                        autoFitTable(tableView);
                    }
                });
            }
        });

        initToolBar();

    }

    private void createColumns(TableView<RegisterTableRow> tableView) {

        try {
            TableColumn<RegisterTableRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.basedata.table.basedata.columnname"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getName()));
            nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
            nameColumn.setSortable(true);
            nameColumn.setSortType(TableColumn.SortType.ASCENDING);

            tableView.getColumns().add(nameColumn);
            tableView.getSortOrder().addAll(nameColumn);

            JEVisClass baseDataClass = ds.getJEVisClass(BASE_DATA_CLASS);

            for (JEVisType type : baseDataClass.getTypes()) {

                if (type.getName().equals("Value")) {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
                    numberFormat.setMinimumFractionDigits(2);
                    numberFormat.setMaximumFractionDigits(2);
                    TableColumn<RegisterTableRow, JEVisObject> lastValueColumn = new TableColumn<>(I18nWS.getInstance().getTypeName(baseDataClass.getName(), type.getName()));
                    lastValueColumn.setStyle("-fx-alignment: CENTER;");
                    lastValueColumn.setMinWidth(250);
                    lastValueColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject()));
                    lastValueColumn.setCellFactory(new Callback<TableColumn<RegisterTableRow, JEVisObject>, TableCell<RegisterTableRow, JEVisObject>>() {
                        @Override
                        public TableCell<RegisterTableRow, JEVisObject> call(TableColumn<RegisterTableRow, JEVisObject> param) {
                            return new TableCell<RegisterTableRow, JEVisObject>() {
                                @Override
                                protected void updateItem(JEVisObject item, boolean empty) {
                                    super.updateItem(item, empty);

                                    if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                                        setText(null);
                                        setGraphic(null);
                                    } else {
                                        RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();
                                        JEVisAttribute att = registerTableRow.getAttributeMap().get(type);
                                        Button manSampleButton = new Button("", JEConfig.getImage("if_textfield_add_64870.png", tableIconSize, tableIconSize));
                                        manSampleButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.mansample")));

                                        if (att != null) {
                                            try {
                                                JEVisAttribute periodAtt = item.getAttribute("Period");

                                                if (att.hasSample() && periodAtt != null) {
                                                    JEVisSample latestSample = att.getLatestSample();
                                                    JEVisSample periodSample = periodAtt.getLatestSample();
                                                    Period period = new Period(periodSample.getValueAsString());
                                                    boolean isCounter = isCounter(att.getObject(), latestSample);
                                                    JEVisUnit displayUnit = att.getDisplayUnit();
                                                    String unitString = UnitManager.getInstance().format(displayUnit);
                                                    String normalPattern = DateTimeFormat.patternForStyle("SS", I18n.getInstance().getLocale());

                                                    addEventManSampleAction(new VirtualSample(new DateTime(), att.getObject().getID() + ":" + att.getName()), manSampleButton, registerTableRow.getName());

                                                    try {
                                                        if (period.equals(Period.days(1))) {
                                                            normalPattern = "dd. MMMM yyyy";
                                                        } else if (period.equals(Period.weeks(1))) {
                                                            normalPattern = "dd. MMMM yyyy";
                                                        } else if (period.equals(Period.months(1)) && !isCounter) {
                                                            normalPattern = "MMMM yyyy";
                                                        } else if (period.equals(Period.months(1)) && isCounter) {
                                                            normalPattern = "dd. MMMM yyyy";
                                                        } else if (period.equals(Period.years(1)) && !isCounter) {
                                                            normalPattern = "yyyy";
                                                        } else if (period.equals(Period.years(1)) && isCounter) {
                                                            normalPattern = "dd. MMMM yyyy";
                                                        } else {
                                                            normalPattern = "yyyy-MM-dd HH:mm:ss";
                                                        }
                                                    } catch (Exception e) {
                                                        logger.error("Could not determine sample rate, fall back to standard", e);
                                                    }

                                                    String timeString = latestSample.getTimestamp().toString(normalPattern);

                                                    setText(numberFormat.format(latestSample.getValueAsDouble()) + " " + unitString + " @ " + timeString);
                                                    setGraphic(manSampleButton);
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
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private boolean isMultiSite() {

        try {
            JEVisClass baseDataClass = ds.getJEVisClass(BASE_DATA_CLASS);
            List<JEVisObject> objects = ds.getObjects(baseDataClass, true);

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
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.basedata.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(20, 20);
        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(20, 20);

        toolBar.getItems().setAll(filterInput, reload);
        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        JEVisHelp.getInstance().addHelpItems(BaseDataPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());
    }

    @Override
    public String getClassName() {
        return "Base Data Plugin";
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
        return I18n.getInstance().getString("plugin.basedata.tooltip");
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
                selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
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
                JEConfig.getStatusBar().addTask(PLUGIN_NAME, clearCacheTask, JEConfig.getImage("base_data.png"), true);

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

    private void loadTabs(Map<String, List<JEVisObject>> allBaseData, List<String> typesOfBaseData) throws InterruptedException {
        AtomicBoolean hasActiveLoadTask = new AtomicBoolean(false);
        for (Map.Entry<Task, String> entry : JEConfig.getStatusBar().getTaskList().entrySet()) {
            Task task = entry.getKey();
            String s = entry.getValue();
            if (s.equals(BaseDataPlugin.class.getName() + "Load")) {
                hasActiveLoadTask.set(true);
                break;
            }
        }
        if (!hasActiveLoadTask.get()) {
            typesOfBaseData.forEach(typeOfBaseData -> {
                Task<JEVisClassTab> task = new Task<JEVisClassTab>() {
                    @Override
                    protected JEVisClassTab call() {
                        TableView<RegisterTableRow> tableView = new TableView<>();

                        JEVisClassTab tab = new JEVisClassTab();
                        List<JEVisObject> listObjects = allBaseData.get(typeOfBaseData);

                        TableViewContextMenuHelper contextMenuHelper = new TableViewContextMenuHelper(tableView);

                        if (contextMenuHelper.getTableHeaderRow() != null) {
                            contextMenuHelper.getTableHeaderRow().setOnMouseClicked(event -> {
                                if (event.getButton() == MouseButton.SECONDARY) {
                                    contextMenuHelper.showContextMenu();
                                }
                            });
                        }

                        try {
                            tab.setClassName(typeOfBaseData);
                            tab.setTableView(tableView);

                            tableView.setFixedCellSize(EDITOR_MAX_HEIGHT);
                            tableView.setTableMenuButtonVisible(true);

                            tab.setClosable(false);
                            createColumns(tableView);

                            ObservableList<RegisterTableRow> registerTableRows = FXCollections.observableArrayList();

                            for (JEVisObject baseDataObject : listObjects) {
                                Map<JEVisType, JEVisAttribute> map = new HashMap<>();

                                for (JEVisAttribute baseDataAttribute : baseDataObject.getAttributes()) {
                                    JEVisType type = null;

                                    try {
                                        type = baseDataAttribute.getType();
                                        map.put(type, baseDataAttribute);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                RegisterTableRow tableData = new RegisterTableRow(map, baseDataObject, isMultiSite());
                                registerTableRows.add(tableData);
                            }

                            FilteredList<RegisterTableRow> filteredList = new FilteredList<>(registerTableRows, s -> true);
                            addListener(filteredList);
                            tableView.setItems(filteredList);
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
            loadTabs(allBaseData, typesOfBaseData);
        }
    }

    private void updateList() {

        Platform.runLater(() -> tabPane.getTabs().clear());
        changeMap.clear();

        List<String> typesOfBaseData = new ArrayList<>();
        Map<String, List<JEVisObject>> allBaseData = new HashMap<>();
        Task load = new Task() {
            @Override
            protected Object call() throws Exception {
                allBaseData.putAll(getAllBaseData());

                allBaseData.forEach((key, list) -> typesOfBaseData.add(key));
                AlphanumComparator ac = new AlphanumComparator();
                typesOfBaseData.sort(ac);
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(BaseDataPlugin.class.getName() + "Load", load, taskImage, true);

        Task loadTabs = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    loadTabs(allBaseData, typesOfBaseData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(BaseDataPlugin.class.getName(), loadTabs, taskImage, true);
    }

    private Map<String, List<JEVisObject>> getAllBaseData() {
        Map<String, List<JEVisObject>> map = new HashMap<>();
        try {
            JEVisClass baseDataClass = ds.getJEVisClass(BASE_DATA_CLASS);
            List<JEVisObject> allObjects = ds.getObjects(baseDataClass, false);
            for (JEVisObject object : allObjects) {
                JEVisObject parent = object.getParents().get(0);
                if (!map.containsKey(parent.getName())) {
                    List<JEVisObject> objectArrayList = new ArrayList<>();
                    objectArrayList.add(object);
                    map.put(parent.getName(), objectArrayList);
                } else {
                    map.get(parent.getName()).add(object);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("base_data.png", 20, 20);
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
        JEConfig.getStatusBar().addTask(PLUGIN_NAME, loadTask, JEConfig.getImage("base_data.png"), true);

    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 7;
    }

}
