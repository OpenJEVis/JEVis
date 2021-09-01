package org.jevis.jeconfig.plugin.notes;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.alarms.AlarmRow;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class NotesPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(NotesPlugin.class);
    public static String PLUGIN_NAME = "Notes Plugin";
    public static String NOTES_CLASS = "Data Notes";
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
    private final DateHelper dateHelper = new DateHelper(DateHelper.TransformType.PREVIEW);
    private final List<Task<List<NotesRow>>> runningUpdateTaskList = new ArrayList<>();
    private final List<Future<?>> futures = new ArrayList<>();
    private final Image taskImage = JEConfig.getImage("rodentia-icons_text-x-playlist.png");

    private final Comparator<AlarmRow> alarmRowComparator = new Comparator<AlarmRow>() {
        @Override
        public int compare(AlarmRow o1, AlarmRow o2) {
            return Comparator.comparing(AlarmRow::getTimeStamp).reversed().compare(o1, o2);
        }
    };
    private final TableView<NotesRow> tableView = new TableView<>();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
    private final JFXDatePicker startDatePicker = new JFXDatePicker();
    private final JFXDatePicker endDatePicker = new JFXDatePicker();
    private boolean init = false;
    private DateTime start;
    private DateTime end;
    private TimeFrame timeFrame = TimeFrame.TODAY;

    public NotesPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;

        this.borderPane.setCenter(this.tableView);
        Label label = new Label(I18n.getInstance().getString("plugin.notes.nonotes"));
        label.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        this.tableView.setPlaceholder(label);

        this.tableView.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
//        this.tableView.setStyle("-fx-background-color: white;");

//        this.tableView.setItems(alarmRows);

        this.numberFormat.setMinimumFractionDigits(2);
        this.numberFormat.setMaximumFractionDigits(2);

        this.startDatePicker.setPrefWidth(120d);
        this.endDatePicker.setPrefWidth(120d);

        createColumns();
    }

    private final JFXComboBox<TimeFrame> timeFrameComboBox = getTimeFrameComboBox();

    public static void autoFitTable(TableView<NotesRow> tableView) {
        for (TableColumn<NotesRow, ?> column : tableView.getColumns()) {
            try {
                if (tableView.getSkin() != null) {
                    columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
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

    private void restartExecutor() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(I18n.getInstance().getString("plugin.alarms.info.wait"));
            alert.show();
            JEConfig.getStatusBar().startProgressJob("stoppingAlarms", runningUpdateTaskList.size(), I18n.getInstance().getString("plugin.alarms.message.stoppingthreads"));

            JEConfig.getStatusBar().stopTasks(NotesPlugin.class.getName());
            this.runningUpdateTaskList.clear();
            JEConfig.getStatusBar().finishProgressJob("stoppingAlarms", I18n.getInstance().getString("plugin.alarms.message.stoppedall"));

            alert.close();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }    //    private ObservableList<AlarmRow> alarmRows = FXCollections.observableArrayList();

    private final ChangeListener<LocalDate> endDateChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != oldValue) {
            end = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 23, 59, 59);
            timeFrame = TimeFrame.CUSTOM;

            updateList();
            Platform.runLater(this::initToolBar);
        }
    };

    private void createColumns() {
        TableColumn<NotesRow, DateTime> dateColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.date"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<NotesRow, DateTime>("date"));
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

        dateColumn.setCellFactory(new Callback<TableColumn<NotesRow, DateTime>, TableCell<NotesRow, DateTime>>() {
            @Override
            public TableCell<NotesRow, DateTime> call(TableColumn<NotesRow, DateTime> param) {
                return new TableCell<NotesRow, DateTime>() {
                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item.toString("yyyy-MM-dd HH:mm:ss"));
                        }
                    }
                };
            }
        });

        TableColumn<NotesRow, String> noteColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.configname"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<NotesRow, String>("configname"));
        noteColumn.setStyle("-fx-alignment: CENTER;");
        noteColumn.setSortable(true);
//        noteColumn.setPrefWidth(500);
        noteColumn.setMinWidth(100);

        noteColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getNote() != null)
                return new SimpleObjectProperty<>(param.getValue().getNote());
            else return new SimpleObjectProperty<>();
        });

        noteColumn.setCellFactory(new Callback<TableColumn<NotesRow, String>, TableCell<NotesRow, String>>() {
            @Override
            public TableCell<NotesRow, String> call(TableColumn<NotesRow, String> param) {
                return new TableCell<NotesRow, String>() {
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

        TableColumn<NotesRow, JEVisObject> objectNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.objectname"));
        objectNameColumn.setCellValueFactory(new PropertyValueFactory<NotesRow, JEVisObject>("objectname"));
        objectNameColumn.setStyle("-fx-alignment: CENTER;");
        objectNameColumn.setSortable(true);
//        objectNameColumn.setPrefWidth(500);
        objectNameColumn.setMinWidth(100);

        objectNameColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getObject() != null)
                return new SimpleObjectProperty<>(param.getValue().getObject());
            else return new SimpleObjectProperty<>();
        });

        objectNameColumn.setCellFactory(new Callback<TableColumn<NotesRow, JEVisObject>, TableCell<NotesRow, JEVisObject>>() {
            @Override
            public TableCell<NotesRow, JEVisObject> call(TableColumn<NotesRow, JEVisObject> param) {
                return new TableCell<NotesRow, JEVisObject>() {
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
                                    NotesRow notesRow = (NotesRow) getTableRow().getItem();


                                    this.setOnMouseClicked(event -> JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, getAnalysisRequest(notesRow, item)));
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
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            setText(text);
                        }
                    }
                };
            }
        });

        tableView.getColumns().setAll(dateColumn, noteColumn, objectNameColumn);
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

    private Object getAnalysisRequest(NotesRow notesRow, JEVisObject noteItem) {
        DateTime start = notesRow.getTimeStamp().minusHours(12);
        DateTime end = notesRow.getTimeStamp().plusHours(12);

        JEVisObject parallelItem = null;
        try {
            JEVisObject parent = noteItem.getParents().get(0);
            for (JEVisObject child : parent.getChildren()) {
                if (!child.equals(noteItem) && child.getName().equals(noteItem.getName())) {
                    parallelItem = child;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not get parallel item for note object {}:{}", noteItem.getName(), noteItem.getID());
        }

        AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
        return new AnalysisRequest(parallelItem, AggregationPeriod.NONE, ManipulationMode.NONE, analysisTimeFrame, start, end);
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
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

        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);

        timeFrameComboBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.timebox.tooltip")));
        startDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.startdate.tooltip")));
        endDatePicker.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.enddate.tooltip")));

        toolBar.getItems().setAll(timeFrameComboBox, sep1, startDatePicker, endDatePicker, sep2, reload);
        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

        JEVisHelp.getInstance().addHelpItems(NotesPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());

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

        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> cellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
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
        return "Notes Plugin";
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
        return I18n.getInstance().getString("plugin.notes.tooltip");
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

    private void updateList() {

        Platform.runLater(this::initToolBar);

        if (init) {
            restartExecutor();
        } else {
            init = true;
        }

        tableView.getItems().clear();

        List<JEVisObject> noteObjects = getAllNoteObjects();
        JEConfig.getStatusBar().startProgressJob("AlarmConfigs", noteObjects.size(), I18n.getInstance().getString("plugin.alarms.message.loadingconfigs"));

        noteObjects.forEach(noteObject -> {
            Task<List<NotesRow>> task = new Task<List<NotesRow>>() {
                @Override
                protected List<NotesRow> call() {
                    List<NotesRow> list = new ArrayList<>();
                    try {
                        Platform.runLater(() -> this.updateTitle("Loading Notes '" + noteObject.getName() + "'"));
                        JEVisAttribute userNotes = noteObject.getAttribute("User Notes");
                        list.addAll(getNotesRow(userNotes));
                        this.succeeded();
                    } catch (Exception e) {
                        logger.error(e);
                        this.failed();
                    } finally {
                        this.done();
                        Platform.runLater(() -> tableView.getItems().addAll(list));
                        //Platform.runLater(() -> tableView.getItems().sort(Comparator.comparing(AlarmRow::getTimeStamp).reversed()));
                        Platform.runLater(() -> autoFitTable(tableView));

                        if (noteObjects.indexOf(noteObject) % 5 == 0
                                || noteObjects.indexOf(noteObject) == noteObjects.size() - 1) {
                            Platform.runLater(() -> tableView.sort());
                        }

                        JEConfig.getStatusBar().progressProgressJob(
                                "AlarmConfigs",
                                1,
                                I18n.getInstance().getString("plugin.alarms.message.finishedalarmconfig") + " " + noteObject.getName());
                    }

                    return list;
                }
            };
            //JEConfig.getStatusBar().addTask(task,JEConfig.getImage("alarm_icon.png"));//,
            JEConfig.getStatusBar().addTask(NotesPlugin.class.getName(), task, taskImage, true);//,


            /** check if all Jobs are done/failed to set statusbar **/
            EventHandler<WorkerStateEvent> doneEvent = event -> {
                if (allJobsDone(futures)) {
                    JEConfig.getStatusBar().finishProgressJob("AlarmConfigs", "");
                    Platform.runLater(() -> tableView.sort());
                    Platform.runLater(() -> autoFitTable(tableView));
                }
            };
            Platform.runLater(() -> {
                task.setOnSucceeded(doneEvent);
                task.setOnFailed(doneEvent);
            });


            this.runningUpdateTaskList.add(task);
            //this.executor.execute(task);
        });

        /**
         futures = runningUpdateTaskList.stream()
         .map(r -> executor.submit(r))
         .collect(Collectors.toList());
         **/
    }

    private List<NotesRow> getNotesRow(JEVisAttribute notesAttribute) throws JEVisException, IOException {
        List<NotesRow> list = new ArrayList<>();
        if (notesAttribute.hasSample()) {
            for (JEVisSample jeVisSample : notesAttribute.getSamples(start, end)) {

                NotesRow alarmRow = new NotesRow(jeVisSample.getTimestamp(), jeVisSample.getValueAsString(), notesAttribute.getObject());

                list.add(alarmRow);
            }
        }
        return list;
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

    private List<JEVisObject> getAllNoteObjects() {
        List<JEVisObject> list = new ArrayList<>();
        JEVisClass dataNotesClass = null;
        try {
            dataNotesClass = ds.getJEVisClass(NOTES_CLASS);
            list.addAll(ds.getObjects(dataNotesClass, true));
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("rodentia-icons_text-x-playlist.png", 20, 20);
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
        return 5;
    }


}
