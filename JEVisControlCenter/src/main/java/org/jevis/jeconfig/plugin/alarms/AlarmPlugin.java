package org.jevis.jeconfig.plugin.alarms;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.jevis.commons.alarm.AlarmType;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonAlarm;
import org.jevis.commons.json.JsonTools;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.application.resource.ResourceLoader;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.object.attribute.AlarmEditor;
import org.jevis.jeconfig.plugin.object.attribute.GapFillingEditor;
import org.jevis.jeconfig.plugin.object.attribute.LimitEditor;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jevis.commons.dataprocessing.CleanDataObject.AttributeName.*;

public class AlarmPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(AlarmPlugin.class);
    public static String PLUGIN_NAME = "Alarm Plugin";
    public static String ALARM_CONFIG_CLASS = "Alarm Configuration";
    private static int ROWS_PER_PAGE = 25;
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
    private final int iconSize = 20;
    private final Image checkAllImage = ResourceLoader.getImage("jetxee-check-sign-and-cross-sign-3.png");
    private final DateHelper dateHelper = new DateHelper(DateHelper.TransformType.PREVIEW);
    private final SimpleBooleanProperty hasAlarms = new SimpleBooleanProperty(false);
    private final ObservableMap<DateTime, Boolean> activeAlarms = FXCollections.observableHashMap();
    private final List<Task<List<AlarmRow>>> runningUpdateTaskList = new ArrayList<>();
    private final Pagination pagination = new Pagination();
    private final SimpleIntegerProperty showCheckedAlarms = new SimpleIntegerProperty(this, "showCheckedAlarms", 0);
    private final List<Future<?>> futures = new ArrayList<>();
    private final Image taskImage = JEConfig.getImage("alarm_icon.png");

    private final Comparator<AlarmRow> alarmRowComparator = new Comparator<AlarmRow>() {
        @Override
        public int compare(AlarmRow o1, AlarmRow o2) {
            return Comparator.comparing(AlarmRow::getTimeStamp).reversed().compare(o1, o2);
        }
    };
    private final List<AlarmRow> data = new ArrayList<>();
    private final TableView<AlarmRow> tableView = new TableView<>();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
    private final JFXDatePicker startDatePicker = new JFXDatePicker();
    private final JFXDatePicker endDatePicker = new JFXDatePicker();
    private boolean init = false;
    private DateTime start;
    private DateTime end;
    private TimeFrame timeFrame = TimeFrame.TODAY;

    public AlarmPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;


        Label label = new Label(I18n.getInstance().getString("plugin.alarms.noalarms"));
        label.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        //tableView.setPadding(new Insets(20, 20, 20, 20));
        tableView.setBorder(new Border(new BorderStroke(Paint.valueOf("#b5bbb7"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN, new Insets(20, 20, 20, 20))));

        this.numberFormat.setMinimumFractionDigits(2);
        this.numberFormat.setMaximumFractionDigits(2);

        this.startDatePicker.setPrefWidth(120d);
        this.startDatePicker.getStyleClass().add("ToolBarDatePicker");
        this.endDatePicker.setPrefWidth(120d);
        this.endDatePicker.getStyleClass().add("ToolBarDatePicker");

        createColumns();

        this.activeAlarms.addListener((MapChangeListener<? super DateTime, ? super Boolean>) change -> {
            this.hasAlarms.set(!this.activeAlarms.isEmpty());
        });

        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                if (pageIndex > data.size() / ROWS_PER_PAGE + 1) {
                    return null;
                } else {
                    return createPage(pageIndex);
                }
            }
        });
        this.borderPane.setCenter(pagination);
    }

    public static void autoFitTable(TableView<AlarmRow> tableView) {
        for (TableColumn<AlarmRow, ?> column : tableView.getColumns()) {
            try {
                if (tableView.getSkin() != null) {
                    columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
        }
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> {

            final String loading = I18n.getInstance().getString("plugin.alarms.reload.progress.message");
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

        });

        Separator sep1 = new Separator(Orientation.VERTICAL);
        Separator sep2 = new Separator(Orientation.VERTICAL);
        Separator sep3 = new Separator(Orientation.VERTICAL);

        if (start != null) {
            startDatePicker.valueProperty().removeListener(startDateChangeListener);
            startDatePicker.setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        }


        if (end != null) {
            endDatePicker.valueProperty().removeListener(endDateChangeListener);
            endDatePicker.setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
        }


        startDatePicker.valueProperty().addListener(startDateChangeListener);
        endDatePicker.valueProperty().addListener(endDateChangeListener);

        JFXComboBox<String> filterBox = new JFXComboBox<>();
        String showOnlyUncheckedAlarms = I18n.getInstance().getString("plugin.alarm.label.showunchecked");
        String showOnlyCheckedAlarms = I18n.getInstance().getString("plugin.alarm.label.showchecked");
        String showAllAlarms = I18n.getInstance().getString("plugin.alarm.label.showall");
        filterBox.getItems().addAll(showOnlyUncheckedAlarms, showOnlyCheckedAlarms, showAllAlarms);
        filterBox.getSelectionModel().select(showCheckedAlarms.get());

        filterBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                showCheckedAlarms.set(newValue.intValue());
                updateList();
            }
        });

        Separator sep4 = new Separator(Orientation.VERTICAL);

        ToggleButton checkAll = new ToggleButton(I18n.getInstance().getString("plugin.alarm.checkall"), JEConfig.getSVGImage(Icon.CHECK, iconSize, iconSize));
        checkAll.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(checkAll);
        checkAll.setOnMouseClicked(event -> {
            getAllAlarmConfigs().forEach(alarmConfiguration -> alarmConfiguration.setChecked(true));
            reload.fire();
        });

        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);

        timeFrameComboBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.timebox.tooltip")));
        startDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.startdate.tooltip")));
        endDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.enddate.tooltip")));
        filterBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.filter.tooltip")));
        checkAll.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.checkall.tooltip")));

        Separator sep5 = new Separator(Orientation.VERTICAL);

        Label labelNORPP = new Label(I18n.getInstance().getString("plugin.alarm.label.alarmsperpage"));
        VBox norppVBox = new VBox(labelNORPP);
        norppVBox.setAlignment(Pos.CENTER);

        NumberSpinner spinnerNORPP = new NumberSpinner(new BigDecimal(35), new BigDecimal(1));
        VBox vBoxSpinner = new VBox(spinnerNORPP);
        vBoxSpinner.setAlignment(Pos.CENTER);

        spinnerNORPP.numberProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                ROWS_PER_PAGE = newValue.intValue();
                createPage(pagination.getCurrentPageIndex());
            }
        });

        toolBar.getItems().setAll(timeFrameComboBox, sep1, startDatePicker, endDatePicker, sep2, reload, sep3, filterBox, sep4, checkAll, sep5, norppVBox, vBoxSpinner);
        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

        JEVisHelp.getInstance().addHelpItems(AlarmPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());


    }

    private final JFXComboBox<TimeFrame> timeFrameComboBox = getTimeFrameComboBox();

    private Node createPage(int pageIndex) {
        int numOfPages = 1;
        if (data.size() % ROWS_PER_PAGE == 0) {
            numOfPages = data.size() / ROWS_PER_PAGE;
        } else if (data.size() > ROWS_PER_PAGE) {
            numOfPages = data.size() / ROWS_PER_PAGE + 1;
        }
        pagination.setPageCount(numOfPages);
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, data.size());
        tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));

        return tableView;
    }    //    private ObservableList<AlarmRow> alarmRows = FXCollections.observableArrayList();    private final JFXComboBox<TimeFrame> timeFrameComboBox = getTimeFrameComboBox();    //    private ObservableList<AlarmRow> alarmRows = FXCollections.observableArrayList();

    private void checkForRunningTasks() throws InterruptedException {
        AtomicBoolean hasActiveChartTasks = new AtomicBoolean(false);
        ConcurrentHashMap<Task, String> taskList = JEConfig.getStatusBar().getTaskList();
        for (Map.Entry<Task, String> entry : taskList.entrySet()) {
            String s = entry.getValue();
            if (s.equals("AlarmConfigs")) {
                hasActiveChartTasks.set(true);
                break;
            }
        }
        if (!hasActiveChartTasks.get()) {
            JEConfig.getStatusBar().finishProgressJob("AlarmConfigs", "");
            data.sort(Comparator.comparing(AlarmRow::getTimeStamp).reversed());

            Platform.runLater(() -> {
                createPage(0);
                autoFitTable(tableView);
            });
        } else {
            Thread.sleep(500);
            checkForRunningTasks();
        }
    }

    private final ChangeListener<LocalDate> startDateChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != oldValue) {
            start = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0, 0);
            timeFrame = TimeFrame.CUSTOM;

            updateList();
            Platform.runLater(this::initToolBar);
        }
    };

    private void updateList() {

        Platform.runLater(this::initToolBar);

        if (init) {
            restartExecutor();
        } else {
            init = true;
        }

        data.clear();

        List<AlarmConfiguration> alarms = getAllAlarmConfigs();
        JEConfig.getStatusBar().startProgressJob("AlarmConfigs", alarms.size(), I18n.getInstance().getString("plugin.alarms.message.loadingconfigs"));

        alarms.forEach(alarmConfiguration -> {
            Task<List<AlarmRow>> task = new Task<List<AlarmRow>>() {

                @Override
                protected List<AlarmRow> call() {
                    List<AlarmRow> list = new ArrayList<>();
                    try {
                        Platform.runLater(() -> this.updateTitle("Loading Alarm '" + alarmConfiguration.getName() + "'"));
                        JEVisAttribute fileLog = alarmConfiguration.getFileLogAttribute();
                        list.addAll(getAlarmRow(fileLog, alarmConfiguration));
                        this.succeeded();
                    } catch (Exception e) {
                        logger.error(e);
                        this.failed();
                    } finally {
                        this.done();
                        data.addAll(list);

                        JEConfig.getStatusBar().progressProgressJob(
                                "AlarmConfigs",
                                1,
                                I18n.getInstance().getString("plugin.alarms.message.finishedalarmconfig") + " " + alarmConfiguration.getName());
                    }

                    return list;
                }
            };

            JEConfig.getStatusBar().addTask("AlarmConfigs", task, taskImage, true);//,


            this.runningUpdateTaskList.add(task);
        });

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    checkForRunningTasks();
                } catch (Exception e) {
                    failed();
                } finally {
                    succeeded();
                }

                return null;
            }
        };


        JEConfig.getStatusBar().addTask(AlarmPlugin.class.getName(), task, taskImage, true);
    }


    private final ChangeListener<LocalDate> endDateChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != oldValue) {
            end = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 23, 59, 59);
            timeFrame = TimeFrame.CUSTOM;

            updateList();
            Platform.runLater(this::initToolBar);
        }
    };

    private void restartExecutor() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(I18n.getInstance().getString("plugin.alarms.info.wait"));
            alert.show();
            JEConfig.getStatusBar().startProgressJob("stoppingAlarms", runningUpdateTaskList.size(), I18n.getInstance().getString("plugin.alarms.message.stoppingthreads"));

            JEConfig.getStatusBar().stopTasks(AlarmPlugin.class.getName());
            this.runningUpdateTaskList.clear();
            JEConfig.getStatusBar().finishProgressJob("stoppingAlarms", I18n.getInstance().getString("plugin.alarms.message.stoppedall"));

            alert.close();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private String getAlarm(Integer item) {
        switch (item) {
            case (4):
                return I18n.getInstance().getString("plugin.alarm.table.alarm.silent");
            case (2):
                return I18n.getInstance().getString("plugin.alarm.table.alarm.standby");
            case (1):
            default:
                return I18n.getInstance().getString("plugin.alarm.table.alarm.normal");
        }
    }

    private void createColumns() {
        TableColumn<AlarmRow, DateTime> dateColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.date"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, DateTime>("date"));
        dateColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setSortable(true);
//        dateColumn.setPrefWidth(160);
        dateColumn.setMinWidth(100);
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);

        dateColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getTimeStamp() != null)
                return new SimpleObjectProperty<>(param.getValue().getTimeStamp());
            else return new SimpleObjectProperty<>();
        });

        dateColumn.setCellFactory(new Callback<TableColumn<AlarmRow, DateTime>, TableCell<AlarmRow, DateTime>>() {
            @Override
            public TableCell<AlarmRow, DateTime> call(TableColumn<AlarmRow, DateTime> param) {
                return new TableCell<AlarmRow, DateTime>() {
                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item.toString("yyyy-MM-dd HH:mm:ss"));

                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                AlarmRow alarmRow = (AlarmRow) getTableRow().getItem();
                                if (!alarmRow.getAlarmConfiguration().isChecked()) {
                                    activeAlarms.put(alarmRow.getTimeStamp(), true);
                                } else {
                                    activeAlarms.remove(alarmRow.getTimeStamp());
                                }
                            }
                        }
                    }
                };
            }
        });


        TableColumn<AlarmRow, String> configNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.configname"));
        configNameColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, String>("configname"));
        configNameColumn.setStyle("-fx-alignment: CENTER;");
        configNameColumn.setSortable(true);
//        configNameColumn.setPrefWidth(500);
        configNameColumn.setMinWidth(100);

        configNameColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarmConfiguration() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarmConfiguration().getName());
            else return new SimpleObjectProperty<>();
        });

        configNameColumn.setCellFactory(new Callback<TableColumn<AlarmRow, String>, TableCell<AlarmRow, String>>() {
            @Override
            public TableCell<AlarmRow, String> call(TableColumn<AlarmRow, String> param) {
                return new TableCell<AlarmRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, JEVisObject> objectNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.objectname"));
        objectNameColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, JEVisObject>("objectname"));
        objectNameColumn.setStyle("-fx-alignment: CENTER;");
        objectNameColumn.setSortable(true);
//        objectNameColumn.setPrefWidth(500);
        objectNameColumn.setMinWidth(100);

        objectNameColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getObject() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getObject());
            else return new SimpleObjectProperty<>();
        });

        objectNameColumn.setCellFactory(new Callback<TableColumn<AlarmRow, JEVisObject>, TableCell<AlarmRow, JEVisObject>>() {
            @Override
            public TableCell<AlarmRow, JEVisObject> call(TableColumn<AlarmRow, JEVisObject> param) {
                return new TableCell<AlarmRow, JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String text = "";
                            try {

                                text += getFullName(item);

                                if (getTableRow() != null && getTableRow().getItem() != null) {
                                    AlarmRow alarmRow = (AlarmRow) getTableRow().getItem();

                                    if (alarmRow.isLinkDisabled()) {
                                        setTextFill(Color.BLACK);
                                        setUnderline(false);
                                    } else {

                                        this.setOnMouseClicked(event -> JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, getAnalysisRequest(alarmRow, item)));
                                        this.hoverProperty().addListener((observable, oldValue, newValue) -> {
                                            if (newValue) {
                                                this.getScene().setCursor(Cursor.HAND);
                                            } else {
                                                this.getScene().setCursor(Cursor.DEFAULT);
                                            }
                                        });

                                        setTextFill(Color.BLUE);
                                        setUnderline(true);
                                    }

                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            setText(text);
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, Double> isValueColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.isValue"));
        isValueColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, Double>("isValue"));
        isValueColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        isValueColumn.setSortable(false);
//        isValueColumn.setPrefWidth(500);
        isValueColumn.setMinWidth(100);

        isValueColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getIsValue() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getIsValue());
            else return new SimpleObjectProperty<>();
        });

        isValueColumn.setCellFactory(new Callback<TableColumn<AlarmRow, Double>, TableCell<AlarmRow, Double>>() {
            @Override
            public TableCell<AlarmRow, Double> call(TableColumn<AlarmRow, Double> param) {
                return new TableCell<AlarmRow, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String text = "";
                            text += numberFormat.format(item);
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                AlarmRow alarmRow = (AlarmRow) getTableRow().getItem();
                                try {
                                    text += " " + UnitManager.getInstance().format(alarmRow.getAlarm().getAttribute().getDisplayUnit());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            setText(text);
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, String> operatorColumn = new TableColumn<>("");
        operatorColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, String>("operator"));
        operatorColumn.setStyle("-fx-alignment: CENTER;");
        operatorColumn.setSortable(false);
//        operatorColumn.setPrefWidth(500);
//        operatorColumn.setMinWidth(100);

        operatorColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getOperator() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getOperator());
            else return new SimpleObjectProperty<>();
        });

        operatorColumn.setCellFactory(new Callback<TableColumn<AlarmRow, String>, TableCell<AlarmRow, String>>() {
            @Override
            public TableCell<AlarmRow, String> call(TableColumn<AlarmRow, String> param) {
                return new TableCell<AlarmRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            switch (item) {
                                case ">":
                                    setText(">");
                                    break;
                                case ">=":
                                case "":
                                case "\u2265":
                                    setText("\u2265");
                                    break;
                                case "=":
                                    setText("=");
                                    break;
                                case "!=":
                                case "\u2260":
                                    setText("\u2260");
                                    break;
                                case "<":
                                    setText("<");
                                    break;
                                case "\u2264":
                                case "<=":
                                    setText("\u2264");
                                    break;
                                default:
                                    setText(item);
                            }
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, Double> shouldBeValueColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.shouldBeValue"));
        shouldBeValueColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, Double>("shouldBeValue"));
        shouldBeValueColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        shouldBeValueColumn.setSortable(false);
//        shouldBeValueColumn.setPrefWidth(500);
        shouldBeValueColumn.setMinWidth(100);

        shouldBeValueColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getSetValue() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getSetValue());
            else return new SimpleObjectProperty<>();
        });

        shouldBeValueColumn.setCellFactory(new Callback<TableColumn<AlarmRow, Double>, TableCell<AlarmRow, Double>>() {
            @Override
            public TableCell<AlarmRow, Double> call(TableColumn<AlarmRow, Double> param) {
                return new TableCell<AlarmRow, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String text = "";
                            text += numberFormat.format(item);
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                AlarmRow alarmRow = (AlarmRow) getTableRow().getItem();

                                try {
                                    text += " ";
                                    if (alarmRow.getAlarm().getAlarmType() != AlarmType.D1 && alarmRow.getAlarm().getAlarmType() != AlarmType.D2) {
                                        text += UnitManager.getInstance().format(alarmRow.getAlarm().getAttribute().getDisplayUnit());
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            setText(text);
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, Integer> logValueColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.alarm"));
        logValueColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, Integer>("alarm"));
        logValueColumn.setStyle("-fx-alignment: CENTER;");
        logValueColumn.setSortable(true);
//        logValueColumn.setPrefWidth(500);
        logValueColumn.setMinWidth(100);

        logValueColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getLogValue() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getLogValue());
            else return new SimpleObjectProperty<>();
        });

        logValueColumn.setCellFactory(new Callback<TableColumn<AlarmRow, Integer>, TableCell<AlarmRow, Integer>>() {
            @Override
            public TableCell<AlarmRow, Integer> call(TableColumn<AlarmRow, Integer> param) {
                return new TableCell<AlarmRow, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(getAlarm(item));
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, Double> toleranceColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.tolerance"));
        toleranceColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, Double>("tolerance"));
        toleranceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        toleranceColumn.setSortable(false);
//        toleranceColumn.setPrefWidth(500);
        toleranceColumn.setMinWidth(100);

        toleranceColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getTolerance() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getTolerance());
            else return new SimpleObjectProperty<>();
        });

        toleranceColumn.setCellFactory(new Callback<TableColumn<AlarmRow, Double>, TableCell<AlarmRow, Double>>() {
            @Override
            public TableCell<AlarmRow, Double> call(TableColumn<AlarmRow, Double> param) {
                return new TableCell<AlarmRow, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(null);
                        setText(null);
                        if (item == null && !empty) {
                            setText("± 0%");
                        } else if (item != null && !empty) {
                            setText("± " + numberFormat.format(item) + "%");
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, String> alarmTypeColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.alarmType"));
        alarmTypeColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, String>("alarmType"));
        alarmTypeColumn.setStyle("-fx-alignment: CENTER;");
        alarmTypeColumn.setSortable(false);
//        alarmTypeColumn.setPrefWidth(500);
        alarmTypeColumn.setMinWidth(100);

        alarmTypeColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getTranslatedTypeName() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm().getTranslatedTypeName());
            else return new SimpleObjectProperty<>();
        });

        alarmTypeColumn.setCellFactory(new Callback<TableColumn<AlarmRow, String>, TableCell<AlarmRow, String>>() {
            @Override
            public TableCell<AlarmRow, String> call(TableColumn<AlarmRow, String> param) {
                return new TableCell<AlarmRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, Boolean> confirmationColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.confirmation"));
        confirmationColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, Boolean>("confirmation"));
        confirmationColumn.setStyle("-fx-alignment: CENTER;");
        confirmationColumn.setSortable(false);
//        alarmTypeColumn.setPrefWidth(500);
        confirmationColumn.setMinWidth(100);

        confirmationColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getAlarmType() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarmConfiguration().isChecked());
            else return new SimpleObjectProperty<>();
        });

        confirmationColumn.setCellFactory(new Callback<TableColumn<AlarmRow, Boolean>, TableCell<AlarmRow, Boolean>>() {
            @Override
            public TableCell<AlarmRow, Boolean> call(TableColumn<AlarmRow, Boolean> param) {
                return new TableCell<AlarmRow, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            ImageView checked = JEConfig.getImage("1404237035_Valid.png", iconSize, iconSize);
                            ImageView notChecked = JEConfig.getImage("1404237042_Error.png", iconSize, iconSize);

                            ToggleButton checkedButton = new ToggleButton("");
                            if (item) {
                                checkedButton.setGraphic(checked);
                            } else {
                                checkedButton.setGraphic(notChecked);
                            }
                            Tooltip checkedTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.tooltip.checked"));
                            checkedButton.setTooltip(checkedTooltip);
                            GlobalToolBar.changeBackgroundOnHoverUsingBinding(checkedButton);
                            checkedButton.setSelected(item);
                            checkedButton.styleProperty().bind(
                                    Bindings
                                            .when(checkedButton.hoverProperty())
                                            .then(
                                                    new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                                            .otherwise(Bindings
                                                    .when(checkedButton.selectedProperty())
                                                    .then("-fx-background-color: transparent;-fx-background-insets: 1 1 1;")
                                                    .otherwise(
                                                            new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

                            checkedButton.setOnAction(action -> {
                                if (checkedButton.isSelected()) {
                                    checkedButton.setGraphic(checked);
                                    AlarmRow alarmRow = (AlarmRow) getTableRow().getItem();
                                    alarmRow.getAlarmConfiguration().setChecked(true);
                                } else {
                                    checkedButton.setGraphic(notChecked);
                                    AlarmRow alarmRow = (AlarmRow) getTableRow().getItem();
                                    alarmRow.getAlarmConfiguration().setChecked(false);
                                }
                                Platform.runLater(() -> createPage(0));
                            });

                            setGraphic(checkedButton);
                        }
                    }
                };
            }
        });

        TableColumn<AlarmRow, Alarm> configurationColumn = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.configuration"));
        configurationColumn.setCellValueFactory(new PropertyValueFactory<AlarmRow, Alarm>("configuration"));
        configurationColumn.setStyle("-fx-alignment: CENTER;");
        configurationColumn.setSortable(false);
//        alarmTypeColumn.setPrefWidth(500);
        configurationColumn.setMinWidth(100);

        configurationColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getAlarm() != null && param.getValue().getAlarm().getAlarmType() != null)
                return new SimpleObjectProperty<>(param.getValue().getAlarm());
            else return new SimpleObjectProperty<>();
        });

        configurationColumn.setCellFactory(new Callback<TableColumn<AlarmRow, Alarm>, TableCell<AlarmRow, Alarm>>() {
            @Override
            public TableCell<AlarmRow, Alarm> call(TableColumn<AlarmRow, Alarm> param) {
                return new TableCell<AlarmRow, Alarm>() {
                    @Override
                    protected void updateItem(Alarm item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            try {
                                Node node = null;
                                if (item.getAlarmType().equals(AlarmType.L1) || item.getAlarmType().equals(AlarmType.L2)) {
                                    JEVisAttribute limitConfigAttribute = item.getObject().getAttribute(LIMITS_CONFIGURATION.getAttributeName());

                                    LimitEditor limitEditor = new LimitEditor(limitConfigAttribute);
                                    HBox hbox = (HBox) limitEditor.getEditor();
                                    JFXButton jfxButton = (JFXButton) hbox.getChildren().get(0);
                                    jfxButton.setText(I18nWS.getInstance().getTypeName(limitConfigAttribute.getType()));
                                    limitEditor.getEditor().setDisable(!ds.getCurrentUser().canWrite(limitConfigAttribute.getObjectID()));

                                    setGraphic(limitEditor.getEditor());
                                } else if (item.getAlarmType().equals(AlarmType.GAP)) {
                                    JEVisAttribute gapFillingConfiguration = item.getObject().getAttribute(GAP_FILLING_CONFIG.getAttributeName());

                                    GapFillingEditor gapFillingEditor = new GapFillingEditor(gapFillingConfiguration);
                                    HBox hbox = (HBox) gapFillingEditor.getEditor();
                                    JFXButton jfxButton = (JFXButton) hbox.getChildren().get(0);
                                    jfxButton.setText(I18nWS.getInstance().getTypeName(gapFillingConfiguration.getType()));
                                    gapFillingEditor.getEditor().setDisable(!ds.getCurrentUser().canWrite(gapFillingConfiguration.getObjectID()));

                                    setGraphic(gapFillingEditor.getEditor());
                                } else {

                                    JEVisAttribute alarmConfigAttribute = item.getObject().getAttribute(ALARM_CONFIG.getAttributeName());

                                    AlarmEditor alarmConfiguration = new AlarmEditor(alarmConfigAttribute);
                                    HBox hbox = (HBox) alarmConfiguration.getEditor();
                                    JFXButton jfxButton = (JFXButton) hbox.getChildren().get(0);
                                    jfxButton.setText(I18nWS.getInstance().getTypeName(alarmConfigAttribute.getType()));
                                    alarmConfiguration.getEditor().setDisable(!ds.getCurrentUser().canWrite(alarmConfigAttribute.getObjectID()));

                                    setGraphic(alarmConfiguration.getEditor());
                                }

                            } catch (Exception e) {
                                logger.error("Could not get alarm attribute for alarm {}", item, e);
                            }
                        }
                    }
                };
            }
        });

        tableView.getColumns().setAll(dateColumn, configNameColumn, objectNameColumn, isValueColumn, operatorColumn, shouldBeValueColumn, logValueColumn, toleranceColumn, alarmTypeColumn, confirmationColumn, configurationColumn);

        Platform.runLater(() -> {
            tableView.getSortOrder().clear();
            tableView.getSortOrder().setAll(dateColumn);
        });


    }

    private String getFullName(JEVisObject item) throws JEVisException {
        String name = "";
        JEVisObject firstParentalDataObject = DataMethods.getFirstParentalDataObject(item);

        if (firstParentalDataObject != null) {
            name += firstParentalDataObject.getName();
            name += getFollowUpName(firstParentalDataObject, item);
        }
        return name;
    }

    private String getFollowUpName(JEVisObject firstParentalDataObject, JEVisObject item) throws JEVisException {
        String name = "";
        for (JEVisObject parent : item.getParents()) {
            if (!parent.equals(firstParentalDataObject)) {
                name += " - ";
                name += parent.getName();
                name += getFollowUpName(firstParentalDataObject, parent);
            }
        }
        return name;
    }

    private Object getAnalysisRequest(AlarmRow alarmRow, JEVisObject item) {

        DateTime ts = alarmRow.getAlarm().getTimeStamp();
        Period period = Period.hours(12);

        Period newPeriod = CleanDataObject.getPeriodForDate(item, ts);

        if (!newPeriod.equals(Period.ZERO)) {
            period = newPeriod;
        }

        DateTime start = ts.minus(period);
        DateTime end = ts.plus(period);

        for (int i = 0; i < 10; i++) {
            start = start.minus(period);
            end = end.plus(period);
        }

        return new AnalysisRequest(item, AggregationPeriod.NONE, ManipulationMode.NONE, start, end);
    }

    private List<AlarmConfiguration> getAllAlarmConfigs() {
        List<AlarmConfiguration> list = new ArrayList<>();
        try {
            JEVisClass alarmConfigClass = ds.getJEVisClass(ALARM_CONFIG_CLASS);
            List<JEVisObject> allObjects = ds.getObjects(alarmConfigClass, true);
            for (JEVisObject object : allObjects) {
                AlarmConfiguration alarmConfiguration = new AlarmConfiguration(ds, object);

                if (alarmConfiguration.isEnabled()) {
                    if (showCheckedAlarms.get() == 0 && !alarmConfiguration.isChecked()) {
                        list.add(alarmConfiguration);
                    } else if (showCheckedAlarms.get() == 1 && alarmConfiguration.isChecked()) {
                        list.add(alarmConfiguration);
                    } else if (showCheckedAlarms.get() == 2) {
                        list.add(alarmConfiguration);
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    private JFXComboBox<TimeFrame> getTimeFrameComboBox() {
        JFXComboBox<TimeFrame> box = new JFXComboBox<>();

        final String today = I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
        final String yesterday = I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String thisWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonthisweek");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String thisMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonthismonth");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");
        final String thisYear = I18n.getInstance().getString("plugin.graph.changedate.buttonthisyear");
        final String lastYear = I18n.getInstance().getString("plugin.graph.changedate.buttonlastyear");
        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
        final String preview = I18n.getInstance().getString("plugin.graph.changedate.preview");

        ObservableList<TimeFrame> timeFrames = FXCollections.observableArrayList(TimeFrame.values());
        timeFrames.remove(TimeFrame.values().length - 2, TimeFrame.values().length - 1);
        box.setItems(timeFrames);

        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> cellFactory = new Callback<javafx.scene.control.ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(javafx.scene.control.ListView<TimeFrame> param) {
                return new ListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame timeFrame, boolean empty) {
                        super.updateItem(timeFrame, empty);
                        setText(null);
                        setGraphic(null);

                        if (timeFrame != null && !empty) {
                            String text = "";
                            switch (timeFrame) {
                                case TODAY:
                                    text = today;
                                    break;
                                case YESTERDAY:
                                    text = yesterday;
                                    break;
                                case LAST_7_DAYS:
                                    text = last7Days;
                                    break;
                                case THIS_WEEK:
                                    text = thisWeek;
                                    break;
                                case LAST_WEEK:
                                    text = lastWeek;
                                    break;
                                case LAST_30_DAYS:
                                    text = last30Days;
                                    break;
                                case THIS_MONTH:
                                    text = thisMonth;
                                    break;
                                case LAST_MONTH:
                                    text = lastMonth;
                                    break;
                                case THIS_YEAR:
                                    text = thisYear;
                                    break;
                                case LAST_YEAR:
                                    text = lastYear;
                                    break;
                                case CUSTOM: {
                                    text = custom;
                                    break;
                                }
                                case PREVIEW:
                                    text = preview;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        box.setCellFactory(cellFactory);
        box.setButtonCell(cellFactory.call(null));
        box.getSelectionModel().select(timeFrame);

        box.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                switch (newValue) {
                    case CUSTOM:
                        break;
                    case TODAY:
                        dateHelper.setType(DateHelper.TransformType.TODAY);
                        break;
                    case YESTERDAY:
                        dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                        break;
                    case LAST_7_DAYS:
                        dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                        break;
                    case THIS_WEEK:
                        dateHelper.setType(DateHelper.TransformType.THISWEEK);
                        break;
                    case LAST_WEEK:
                        dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                        break;
                    case LAST_30_DAYS:
                        dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                        break;
                    case THIS_MONTH:
                        dateHelper.setType(DateHelper.TransformType.THISMONTH);
                        break;
                    case LAST_MONTH:
                        dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                        break;
                    case THIS_YEAR:
                        dateHelper.setType(DateHelper.TransformType.THISYEAR);
                        break;
                    case LAST_YEAR:
                        dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                        break;
                }

                if (newValue != TimeFrame.CUSTOM) {
                    timeFrame = newValue;
                    start = dateHelper.getStartDate();
                    end = dateHelper.getEndDate();

                    updateList();
                }
            }
        });

        return box;
    }

    @Override
    public String getClassName() {
        return "Alarm Plugin";
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
        return I18n.getInstance().getString("plugin.alarms.tooltip");
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
        return borderPane;
    }


    private synchronized boolean allJobsDone(List<Future<?>> futures) {
        boolean allDone = true;
        Iterator<Future<?>> itr = futures.iterator();
        while (itr.hasNext()) {
            if (!itr.next().isDone()) {
                allDone = false;
            }
        }

        return allDone;
    }

    private List<AlarmRow> getAlarmRow(JEVisAttribute fileLog, AlarmConfiguration alarmConfiguration) throws JEVisException, IOException {
        List<AlarmRow> list = new ArrayList<>();
        if (fileLog.hasSample()) {
            for (JEVisSample jeVisSample : fileLog.getSamples(start, end)) {
                JsonAlarm[] jsonAlarmList = JsonTools.objectMapper().readValue(jeVisSample.getValueAsFile().getBytes(), JsonAlarm[].class);
                for (JsonAlarm jsonAlarm : jsonAlarmList) {
                    JEVisObject object = ds.getObject(jsonAlarm.getObject());
                    JEVisAttribute attribute = object.getAttribute(jsonAlarm.getAttribute());
                    DateTime dateTime = new DateTime(jsonAlarm.getTimeStamp());
                    List<JEVisSample> samples = attribute.getSamples(dateTime, dateTime);
                    if (!samples.isEmpty()) {
                        JEVisSample sample = samples.get(0);

                        Alarm alarm = new Alarm(object, attribute, sample, dateTime, jsonAlarm.getIsValue(), jsonAlarm.getOperator(), jsonAlarm.getShouldBeValue(), jsonAlarm.getAlarmType(), jsonAlarm.getLogValue());

                        AlarmRow alarmRow = new AlarmRow(alarm.getTimeStamp(), alarmConfiguration, alarm);

                        list.add(alarmRow);
                    }
                }
            }
        }
        return list;
    }


    @Override
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.ERROR, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {


        this.timeFrameComboBox.getSelectionModel().select(TimeFrame.PREVIEW);


        Platform.runLater(() -> autoFitTable(tableView));
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

    public SimpleBooleanProperty hasAlarmsProperty() {
        return hasAlarms;
    }


}
