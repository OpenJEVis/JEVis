package org.jevis.jeconfig.plugin.basedata;

import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.plugin.equipment.EquipmentPlugin;
import org.jevis.jeconfig.plugin.meters.AttributeValueChange;
import org.jevis.jeconfig.plugin.meters.RegisterTableRow;
import org.jevis.jeconfig.plugin.object.ObjectPlugin;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseDataPlugin implements Plugin {
    public static final String BASE_DATA_CLASS = "Base Data";
    private static final Logger logger = LogManager.getLogger(EquipmentPlugin.class);
    private static final double EDITOR_MAX_HEIGHT = 50;
    public static String PLUGIN_NAME = "Base Data Plugin";
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final int toolBarIconSize = 20;
    private final int tableIconSize = 18;
    private final Image taskImage = JEConfig.getImage("building_equipment.png");
    private final ObjectRelations objectRelations;
    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    Map<JEVisAttribute, AttributeValueChange> changeMap = new HashMap<>();
    private boolean initialized = false;
    private JEVisClass baseDataClass;

    public BaseDataPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.objectRelations = new ObjectRelations(ds);

        initToolBar();

    }

    public static void autoFitTable(TableView<RegisterTableRow> tableView) {
        for (TableColumn<RegisterTableRow, ?> column : tableView.getColumns()) {
//            if (column.isVisible()) {
            try {
                if (tableView.getSkin() != null) {
                    columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
//            }
        }
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


            for (JEVisType type : baseDataClass.getTypes()) {
                TableColumn<RegisterTableRow, JEVisAttribute> column = new TableColumn<>(I18nWS.getInstance().getTypeName(baseDataClass.getName(), type.getName()));
                column.setStyle("-fx-alignment: CENTER;");
                column.setSortable(false);

                column.setId(type.getName());
                column.setCellValueFactory(param -> {
                    try {
                        return new ReadOnlyObjectWrapper<>(param.getValue().getObject().getAttribute(type));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new ReadOnlyObjectWrapper<>();
                });

                column.setCellFactory(valueCell());
                column.setMinWidth(120);

                tableView.getColumns().add(column);

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


    private Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>> valueCell() {
        return new Callback<TableColumn<RegisterTableRow, JEVisAttribute>, TableCell<RegisterTableRow, JEVisAttribute>>() {
            @Override
            public TableCell<RegisterTableRow, JEVisAttribute> call(TableColumn<RegisterTableRow, JEVisAttribute> param) {
                return new TableCell<RegisterTableRow, JEVisAttribute>() {
                    @Override
                    protected void updateItem(JEVisAttribute item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RegisterTableRow registerTableRow = (RegisterTableRow) getTableRow().getItem();

                            JFXTextField valueField = new JFXTextField();
                            valueField.setPrefWidth(150);
                            JFXTextField unitField = new JFXTextField();
                            unitField.setPrefWidth(45);
                            try {
                                JEVisAttribute attribute = registerTableRow.getAttributeMap().get(item.getType());
                                if (attribute != null && attribute.hasSample()) {
                                    valueField.setText(attribute.getLatestSample().getValueAsDouble().toString());

                                    if (attribute.getDisplayUnit() != null && !attribute.getInputUnit().getLabel().isEmpty()) {
                                        unitField.setText(UnitManager.getInstance().format(attribute.getDisplayUnit().getLabel()));
                                    } else {
                                        unitField.setText(UnitManager.getInstance().format(attribute.getInputUnit().getLabel()));
                                    }
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }

                            Button manSampleButton = new Button("", JEConfig.getImage("if_textfield_add_64870.png", tableIconSize, tableIconSize));
                            manSampleButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.meters.table.mansample")));

                            Button gotoButton = new Button("",
                                    JEConfig.getImage("1476393792_Gnome-Go-Jump-32.png", tableIconSize, tableIconSize));//icon
                            gotoButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.object.attribute.target.goto.tooltip")));

                            addEventManSampleAction(item, manSampleButton, registerTableRow.getName());

                            gotoButton.setOnAction(event -> {
                                try {
                                    JEVisObject findObj = ds.getObject(item.getObjectID());
                                    JEConfig.openObjectInPlugin(ObjectPlugin.PLUGIN_NAME, findObj);
                                } catch (Exception ex) {
                                    logger.catching(ex);
                                }
                            });


                            HBox hBox = new HBox(valueField, unitField, manSampleButton, gotoButton);
                            hBox.setAlignment(Pos.CENTER);
                            hBox.setSpacing(4);

                            VBox vBox = new VBox(hBox);
                            vBox.setAlignment(Pos.CENTER);
                            setGraphic(vBox);
                        }
                    }
                };
            }
        };
    }

    private void addEventManSampleAction(JEVisAttribute attribute, Button buttonToAddEvent, String headerText) {
        EnterDataDialog enterDataDialog = new EnterDataDialog(getDataSource());
        if (attribute != null) {
            try {
                enterDataDialog.setTarget(false, attribute);

                if (attribute.hasSample() && attribute.getLatestSample() != null) {
                    enterDataDialog.setSample(attribute.getLatestSample());
                }

                enterDataDialog.setShowValuePrompt(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        buttonToAddEvent.setOnAction(event -> {
            enterDataDialog.showPopup(buttonToAddEvent, headerText);
        });
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", toolBarIconSize, toolBarIconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));


        toolBar.getItems().setAll(reload);
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

    private void loadData(List<JEVisObject> allBaseData) throws InterruptedException {
        AtomicBoolean hasActiveLoadTask = new AtomicBoolean(false);
        for (Map.Entry<Task, String> entry : JEConfig.getStatusBar().getTaskList().entrySet()) {
            Task task = entry.getKey();
            String s = entry.getValue();
            if (s.equals(EquipmentPlugin.class.getName() + "Load")) {
                hasActiveLoadTask.set(true);
                break;
            }
        }
        if (!hasActiveLoadTask.get()) {
            Task<TableView> task = new Task<TableView>() {
                @Override
                protected TableView call() {
                    TableView<RegisterTableRow> tableView = new TableView<>();

                    try {
                        AlphanumComparator ac = new AlphanumComparator();

                        tableView.setFixedCellSize(EDITOR_MAX_HEIGHT);
                        tableView.setSortPolicy(param -> {
                            Comparator<RegisterTableRow> comparator = (t1, t2) -> ac.compare(t1.getObject().getName(), t2.getObject().getName());
                            FXCollections.sort(tableView.getItems(), comparator);
                            return true;
                        });
                        tableView.setTableMenuButtonVisible(true);


                        createColumns(tableView);

                        List<RegisterTableRow> registerTableRows = new ArrayList<>();

                        JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
                        JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");

                        for (JEVisObject meterObject : allBaseData) {
                            Map<JEVisType, JEVisAttribute> map = new HashMap<>();

                            for (JEVisAttribute meterObjectAttribute : meterObject.getAttributes()) {
                                JEVisType type = null;

                                try {
                                    type = meterObjectAttribute.getType();

                                    map.put(type, meterObjectAttribute);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            RegisterTableRow tableData = new RegisterTableRow(map, meterObject, isMultiSite());
                            registerTableRows.add(tableData);
                        }

                        tableView.getItems().setAll(registerTableRows);
                        this.succeeded();
                    } catch (Exception e) {
                        logger.error(e);
                        this.failed();
                    } finally {
                        Platform.runLater(() -> borderPane.setCenter(tableView));
                        Platform.runLater(() -> autoFitTable(tableView));
                        Platform.runLater(tableView::sort);
                        this.done();
                    }
                    return tableView;
                }
            };
            JEConfig.getStatusBar().addTask(EquipmentPlugin.class.getName(), task, taskImage, true);

        } else {
            Thread.sleep(500);
            loadData(allBaseData);
        }
    }

    private void updateList() {

        changeMap.clear();

        List<JEVisObject> allBaseData = getAllBaseData();
        AlphanumComparator ac = new AlphanumComparator();
        allBaseData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

        Task loadData = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    loadData(allBaseData);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask(EquipmentPlugin.class.getName(), loadData, taskImage, true);
    }

    private List<JEVisObject> getAllBaseData() {
        List<JEVisObject> list = new ArrayList<>();
        try {
            baseDataClass = ds.getJEVisClass(BASE_DATA_CLASS);
            list.addAll(ds.getObjects(baseDataClass, false));
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
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
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 7;
    }

}
