package org.jevis.jeconfig.plugin.alarms;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmConfiguration;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(AlarmPlugin.class);
    public static String PLUGIN_NAME = "Alarm Plugin";
    public static String ALARM_CONFIG_CLASS = "Alarm Configuration";
    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private final int iconSize = 24;
    private boolean initialized = false;
    private ListView<AlarmRow> listView = new ListView<>();
    private WebView web = new WebView();
    private WebEngine engine = web.getEngine();
    private GridPane gp = new GridPane();
    private DateHelper dateHelper = new DateHelper(DateHelper.TransformType.TODAY);
    private ComboBox<TimeFrame> timeFrameComboBox;
    private SimpleBooleanProperty hasAlarms = new SimpleBooleanProperty(false);
    private ObservableMap<DateTime, Boolean> activeAlarms = FXCollections.observableHashMap();
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public AlarmPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;

        this.gp.setPadding(new Insets(4, 4, 4, 4));
        this.gp.setHgap(4);
        this.gp.setVgap(4);

        BorderPane view = new BorderPane();
        view.setTop(gp);
        view.setCenter(web);

        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.HORIZONTAL);
        sp.setId("mainsplitpane");
        sp.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        sp.getItems().setAll(listView, view);
        this.borderPane.setCenter(sp);

        sp.setDividerPositions(0.3);

        initToolBar();

        this.engine.setUserStyleSheetLocation(JEConfig.class.getResource("/web/web.css").toExternalForm());

        this.engine.setJavaScriptEnabled(true);

        this.activeAlarms.addListener((MapChangeListener<? super DateTime, ? super Boolean>) change -> {
            if (activeAlarms.isEmpty()) {
                hasAlarms.set(false);
            } else {
                hasAlarms.set(true);
            }
        });

    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> {
            initialized = false;

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

        timeFrameComboBox = getTimeFrameComboBox();

        toolBar.getItems().setAll(reload, sep1, timeFrameComboBox);
    }

    private ComboBox<TimeFrame> getTimeFrameComboBox() {
        ComboBox<TimeFrame> box = new ComboBox<>();

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

        ObservableList<TimeFrame> timeFrames = FXCollections.observableArrayList(TimeFrame.values());
        timeFrames.remove(TimeFrame.values().length - 2, TimeFrame.values().length);
        timeFrames.remove(0, 1);
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
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        box.setCellFactory(cellFactory);
        box.setButtonCell(cellFactory.call(null));

        box.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                switch (newValue) {
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
                updateList();
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
        if (!initialized) {
            init();
        }
        return borderPane;
    }

    private void init() {

        listView.setPrefWidth(100);
        setupCellFactory(listView);

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                loadAlarm(newValue.getTimeStamp(), newValue.getAlarmConfiguration());
            }
        });

        Platform.runLater(() -> timeFrameComboBox.getSelectionModel().select(TimeFrame.TODAY));

        initialized = true;
    }

    private void updateList() {

        ObservableList<AlarmRow> alarmRows = FXCollections.observableArrayList();
        listView.setItems(alarmRows);

        executor.execute(() -> {
            List<AlarmConfiguration> alarms = getAllAlarmConfigs();
            for (AlarmConfiguration alarmConfiguration : alarms) {
                JEVisAttribute logAttribute = alarmConfiguration.getLogAttribute();
                if (logAttribute.hasSample())
                    for (JEVisSample jeVisSample : logAttribute.getSamples(dateHelper.getStartDate(), dateHelper.getEndDate())) {
                        Platform.runLater(() -> {
                            try {
                                alarmRows.add(new AlarmRow(jeVisSample.getTimestamp(), alarmConfiguration));
                                if (!alarmConfiguration.isChecked()) {
                                    this.hasAlarms.set(true);
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                Platform.runLater(() -> alarmRows.sort(Comparator.comparing(AlarmRow::getTimeStamp).reversed()));
            }
        });
    }

    private void loadAlarm(DateTime timeStamp, AlarmConfiguration alarmObject) {
        JEVisAttribute logAttribute = alarmObject.getLogAttribute();

        if (logAttribute != null) {
            List<JEVisSample> samples = logAttribute.getSamples(timeStamp, timeStamp);

            if (!samples.isEmpty()) {
                try {
                    this.gp.getChildren().clear();

                    this.engine.loadContent(samples.get(0).getValueAsString());

                    Label checkedLabel = new Label(I18n.getInstance().getString("plugin.alarms.checked"));
                    checkedLabel.setAlignment(Pos.CENTER_LEFT);

                    ImageView checked = JEConfig.getImage("1404237035_Valid.png", iconSize, iconSize);
                    ImageView notChecked = JEConfig.getImage("1404237042_Error.png", iconSize, iconSize);

                    ToggleButton checkedButton = new ToggleButton("", notChecked);
                    Tooltip checkedTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.tooltip.checked"));
                    checkedButton.setTooltip(checkedTooltip);
                    GlobalToolBar.changeBackgroundOnHoverUsingBinding(checkedButton);
                    checkedButton.setSelected(alarmObject.isChecked());
                    checkedButton.styleProperty().bind(
                            Bindings
                                    .when(checkedButton.hoverProperty())
                                    .then(
                                            new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                                    .otherwise(Bindings
                                            .when(checkedButton.selectedProperty())
                                            .then("-fx-background-insets: 1 1 1;")
                                            .otherwise(
                                                    new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

                    checkedButton.setOnAction(action -> {
                        if (checkedButton.isSelected()) {
                            checkedButton.setGraphic(checked);
                            alarmObject.setChecked(true);
                        } else {
                            checkedButton.setGraphic(notChecked);
                            alarmObject.setChecked(false);
                        }
                        Platform.runLater(() -> listView.refresh());
                    });

                    this.gp.add(checkedLabel, 0, 0);
                    this.gp.add(checkedButton, 1, 0);
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupCellFactory(ListView<AlarmRow> listView) {
        Callback<ListView<AlarmRow>, ListCell<AlarmRow>> cellFactory = new Callback<ListView<AlarmRow>, ListCell<AlarmRow>>() {
            @Override
            public ListCell<AlarmRow> call(ListView<AlarmRow> param) {
                return new ListCell<AlarmRow>() {
                    @Override
                    protected void updateItem(AlarmRow obj, boolean empty) {
                        super.updateItem(obj, empty);

                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(obj.getTimeStamp().toString("YYYY-MM-dd HH:mm:ss") + " - " + obj.getAlarmConfiguration().getName());
                            if (!obj.getAlarmConfiguration().isChecked()) {
                                setTextFill(Color.RED);
                                activeAlarms.put(obj.getTimeStamp(), true);
                            } else {
                                setTextFill(Color.BLACK);
                                activeAlarms.remove(obj.getTimeStamp());
                            }
                        }
                    }
                };
            }
        };

        listView.setCellFactory(cellFactory);
    }

    private List<AlarmConfiguration> getAllAlarmConfigs() {
        List<AlarmConfiguration> list = new ArrayList<>();
        JEVisClass alarmConfigClass = null;
        try {
            alarmConfigClass = ds.getJEVisClass(ALARM_CONFIG_CLASS);
            List<JEVisObject> allObjects = ds.getObjects(alarmConfigClass, true);
            for (JEVisObject object : allObjects) {
                AlarmConfiguration alarmConfiguration = new AlarmConfiguration(ds, object);
                list.add(alarmConfiguration);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("Warning-icon.png", 20, 20);
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
        return 4;
    }

    public SimpleBooleanProperty hasAlarmsProperty() {
        return hasAlarms;
    }
}
