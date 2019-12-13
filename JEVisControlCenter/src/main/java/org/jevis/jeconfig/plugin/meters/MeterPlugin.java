package org.jevis.jeconfig.plugin.meters;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.plugin.object.attribute.AttributeEditor;
import org.jevis.jeconfig.plugin.object.extension.GenericAttributeExtension;
import org.jevis.jeconfig.tool.I18n;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeterPlugin implements Plugin {
    public static final String MEASUREMENT_INSTRUMENT_CLASS = "Measurement Instrument";
    private static final Logger logger = LogManager.getLogger(MeterPlugin.class);
    private static final double EDITOR_MAX_HEIGHT = 60;
    public static String PLUGIN_NAME = "Meter Plugin";
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private final int iconSize = 24;
    Map<String, AttributeEditor> attributeEditorMap = new HashMap<>();
    private TabPane tabPane = new TabPane();
    private boolean initialized = false;

    public MeterPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.borderPane.setCenter(tabPane);
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                Platform.runLater(() -> {
                    Tab selectedItem = this.tabPane.getSelectionModel().getSelectedItem();
                    if (selectedItem.getContent() instanceof TableView) {
                        TableView<MeterRow> tableView = (TableView<MeterRow>) selectedItem.getContent();
                        autoFitTable(tableView);
                    }
                });
            }
        });

        initToolBar();

    }

    public static void autoFitTable(TableView<MeterRow> tableView) {
//        tableView.getItems().addListener(new ListChangeListener<Object>() {
//            @Override
//            public void onChanged(Change<?> c) {
        for (TableColumn<MeterRow, ?> column : tableView.getColumns()) {
            try {
                if (tableView.getSkin() != null) {
                    columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

//            }
//        });
    }

    private void createColumns(TableView<MeterRow> tableView, JEVisClass jeVisClass) {

        try {
            TableColumn<MeterRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.objectname"));
            nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getObject().getName()));
            nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

            tableView.getColumns().add(nameColumn);

            JEVisType onlineIdType = jeVisClass.getType("Online ID");
            JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
            JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");

            for (JEVisType type : jeVisClass.getTypes()) {
                TableColumn<MeterRow, Object> column = new TableColumn<>(I18nWS.getInstance().getTypeName(jeVisClass.getName(), type.getName()));
                column.setStyle("-fx-alignment: CENTER;");
                column.setId(type.getName());
                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().getObject()));
                column.setCellFactory(new Callback<TableColumn<MeterRow, Object>, TableCell<MeterRow, Object>>() {
                    @Override
                    public TableCell<MeterRow, Object> call(TableColumn<MeterRow, Object> param) {
                        return new TableCell<MeterRow, Object>() {
                            @Override
                            protected void updateItem(Object item, boolean empty) {
                                super.updateItem(item, empty);

                                if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    MeterRow meterRow = (MeterRow) getTableRow().getItem();
                                    try {
                                        JEVisAttribute jeVisAttribute = meterRow.getAttributeMap().get(type);
                                        if (jeVisAttribute != null) {
                                            AttributeEditor editor = GenericAttributeExtension.getEditor(type, jeVisAttribute);

                                            String hash = jeVisAttribute.getObject().getID() + ":" + jeVisAttribute.getName();

                                            if (!attributeEditorMap.containsKey(hash)) {
                                                attributeEditorMap.put(hash, editor);
                                            }

                                            editor.setReadOnly(false);
                                            setGraphic(editor.getEditor());
                                        }
                                    } catch (JEVisException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                    }
                });

                tableView.getColumns().add(column);

                if (type.equals(onlineIdType)) {
                    TableColumn<MeterRow, Object> multiplierColumn = new TableColumn<>(I18nWS.getInstance().getTypeName(cleanDataClass.getName(), multiplierType.getName()));
                    multiplierColumn.setStyle("-fx-alignment: CENTER;");
                    multiplierColumn.setId(multiplierType.getName());
                    multiplierColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().getObject()));
                    multiplierColumn.setCellFactory(new Callback<TableColumn<MeterRow, Object>, TableCell<MeterRow, Object>>() {
                        @Override
                        public TableCell<MeterRow, Object> call(TableColumn<MeterRow, Object> param) {
                            return new TableCell<MeterRow, Object>() {
                                @Override
                                protected void updateItem(Object item, boolean empty) {
                                    super.updateItem(item, empty);

                                    if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                                        setText(null);
                                        setGraphic(null);
                                    } else {
                                        MeterRow meterRow = (MeterRow) getTableRow().getItem();
                                        try {
                                            JEVisAttribute jeVisAttribute = meterRow.getAttributeMap().get(multiplierType);

                                            if (jeVisAttribute != null) {
                                                AttributeEditor editor = GenericAttributeExtension.getEditor(multiplierType, jeVisAttribute);

                                                String hash = jeVisAttribute.getObject().getName() + ":" + jeVisAttribute.getObject().getID() + ":" + jeVisAttribute.getName();

                                                if (!attributeEditorMap.containsKey(hash)) {
                                                    attributeEditorMap.put(hash, editor);
                                                }

                                                editor.setReadOnly(false);
                                                setGraphic(editor.getEditor());
                                            }
                                        } catch (JEVisException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            };
                        }
                    });

                    tableView.getColumns().add(multiplierColumn);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        Separator sep1 = new Separator(Orientation.VERTICAL);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        save.setOnAction(event -> handleRequest(Constants.Plugin.Command.SAVE));

        Separator sep2 = new Separator(Orientation.VERTICAL);
        Separator sep3 = new Separator(Orientation.VERTICAL);


        toolBar.getItems().setAll(reload, sep1, save);
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
                return false;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return false;
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
                for (Map.Entry<String, AttributeEditor> entry : attributeEditorMap.entrySet()) {
                    String s = entry.getKey();
                    AttributeEditor attributeEditor = entry.getValue();

                    if (attributeEditor.hasChanged()) {
                        System.out.println("hasChanged: " + attributeEditor);
                        try {
                            attributeEditor.commit();
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case Constants.Plugin.Command.DELETE:
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                break;
            case Constants.Plugin.Command.RELOAD:
                final String loading = I18n.getInstance().getString("plugin.alarms.reload.progress.message");
                Service<Void> service = new Service<Void>() {
                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {
                            @Override
                            protected Void call() {
                                updateMessage(loading);
                                try {
                                    if (initialized) {
                                        ds.clearCache();
                                        ds.preload();
                                    } else {
                                        initialized = true;
                                    }

                                    updateList();
                                } catch (Exception e) {
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

    private void updateList() {

        tabPane.getTabs().clear();

        Map<JEVisClass, List<JEVisObject>> allMeters = getAllMeters();

        for (Map.Entry<JEVisClass, List<JEVisObject>> entry : allMeters.entrySet()) {
            JEVisClass jeVisClass = entry.getKey();
            List<JEVisObject> listObjects = entry.getValue();

            try {
                TableView<MeterRow> tableView = new TableView<>();
                Tab tab = new Tab(I18nWS.getInstance().getClassName(jeVisClass), tableView);
                tab.setClosable(false);

                createColumns(tableView, jeVisClass);

                List<MeterRow> meterRows = new ArrayList<>();
                for (JEVisObject meterObject : listObjects) {
                    Map<JEVisType, JEVisAttribute> map = new HashMap<>();

                    for (JEVisAttribute meterObjectAttribute : meterObject.getAttributes()) {
                        JEVisType type = null;

                        try {
                            type = meterObjectAttribute.getType();

                            map.put(type, meterObjectAttribute);

                            JEVisType onlineIdType = jeVisClass.getType("Online ID");
                            JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
                            JEVisType multiplierType = cleanDataClass.getType("Value Multiplier");

                            if (type.equals(onlineIdType)) {
                                if (meterObjectAttribute.hasSample()) {
                                    TargetHelper th = new TargetHelper(ds, meterObjectAttribute);

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

                    MeterRow tableData = new MeterRow(map, meterObject);
                    meterRows.add(tableData);
                }

                tableView.getItems().setAll(meterRows);

                Platform.runLater(() -> {
                    tabPane.getTabs().add(tab);

                    Platform.runLater(() -> {
                        autoFitTable(tableView);
                        tableView.setFixedCellSize(60);
                    });
                });
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<JEVisClass, List<JEVisObject>> getAllMeters() {
        Map<JEVisClass, List<JEVisObject>> map = new HashMap<>();
        try {
            JEVisClass meterClass = ds.getJEVisClass(MEASUREMENT_INSTRUMENT_CLASS);
            List<JEVisObject> allObjects = ds.getObjects(meterClass, true);
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
        if (!initialized) {
            handleRequest(Constants.Plugin.Command.RELOAD);
        }
    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 5;
    }

}
