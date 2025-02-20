package org.jevis.jecc.plugin.loadprofile;

import de.focus_shift.jollyday.core.Holiday;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.JodaConverters;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonSchedulerRule;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.*;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.TimeZoneBox;
import org.jevis.jecc.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jecc.application.control.AnalysisLinkButton;
import org.jevis.jecc.application.control.CalendarBox;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.tools.Holidays;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.dialog.ProgressForm;
import org.jevis.jecc.plugin.loadprofile.data.MixedLoadProfile;
import org.jevis.jecc.plugin.loadprofile.data.TableSample;
import org.jevis.jecc.plugin.object.attribute.ScheduleEditor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LoadProfilePlugin implements Plugin {
    public static final String PLUGIN_NAME = "Load Profile Plugin";
    private static final Logger logger = LogManager.getLogger(LoadProfilePlugin.class);
    private final int iconSize = 18;
    private final JEVisDataSource ds;
    private final String title;
    private final ToolBar toolBar = new ToolBar();
    private final BorderPane borderPane = new BorderPane();
    private final NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private final AtomicReference<JEVisObject> outputObject = new AtomicReference<>();
    private final ObservableList<JEVisObject> inputList = FXCollections.observableArrayList();
    private final ListView<JEVisObject> inputListView = new ListView<>(inputList);
    private final CalendarBox calendarBox = new CalendarBox();
    private final ScheduleEditor workdayEditor = new ScheduleEditor();
    private final ObservableList<TableSample> allDaysLoadProfileList = FXCollections.observableArrayList();
    private final TableView<TableSample> allDaysLoadProfileView = new TableView<>(allDaysLoadProfileList);
    private final ObservableList<MixedLoadProfile> mixedLoadProfileList = FXCollections.observableArrayList();
    private final TableView<MixedLoadProfile> mixedLoadProfileView = new TableView<>(mixedLoadProfileList);
    private boolean initialized = false;

    public LoadProfilePlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.workdayEditor.setShowMonthsProperty(false);
        this.workdayEditor.setShowDaysOfMonthProperty(false);
        this.nf.setMaximumFractionDigits(2);

        initToolbar();
    }

    private boolean isHoliday(Set<Holiday> holidays, DateTime ts) {
        boolean holiday = false;

        for (Holiday holiday1 : holidays) {
            org.joda.time.LocalDate localDate = JodaConverters.javaToJodaLocalDate(holiday1.getDate());
            org.joda.time.LocalDate localDate1 = ts.toLocalDate();
            if (localDate.getYear() == localDate1.getYear() && localDate.getMonthOfYear() == localDate1.getMonthOfYear()
                    && localDate.getDayOfMonth() == localDate1.getDayOfMonth()) {
                holiday = true;
                break;
            }
        }

        return holiday;
    }

    @Override
    public String getClassName() {
        return "Load Profile Plugin";
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
        return "";
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public String getToolTip() {
        return I18n.getInstance().getString("plugin.loadprofile.tooltip");
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
                return false;
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
                break;
            case Constants.Plugin.Command.DELETE:
                break;
            case Constants.Plugin.Command.EXPAND:
                break;
            case Constants.Plugin.Command.NEW:
                break;
            case Constants.Plugin.Command.RELOAD:
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
    public Region getIcon() {
        return ControlCenter.getSVGImage(Icon.LOADPROFILE, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {
        if (!initialized) {
            initGui();
            initialized = true;
        }
    }

    private void initGui() {

        Button addInput = new Button("", ControlCenter.getSVGImage(Icon.PLUS, iconSize, iconSize));
        Button removeInput = new Button("", ControlCenter.getSVGImage(Icon.DELETE, iconSize, iconSize));
        HBox inputListMenu = new HBox(6, addInput, removeInput);

        BorderPane inputControl = new BorderPane();
        inputControl.setTop(inputListMenu);
        inputControl.setCenter(inputListView);

        addInput.setOnAction(actionEvent -> {
            List<JEVisClass> classList = new ArrayList<>();
            for (String className : TreeSelectionDialog.allData) {
                try {
                    classList.add(ds.getJEVisClass(className));
                } catch (JEVisException e) {
                    logger.error(e);
                }
            }
            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(ds, classList, SelectionMode.MULTIPLE);
            selectionDialog.showAndWait();

            selectionDialog.getUserSelection().forEach(userSelection -> inputList.add(userSelection.getSelectedObject()));
        });

        removeInput.setOnAction(actionEvent -> inputList.removeAll(inputListView.getSelectionModel().getSelectedItems()));

        Label outputButtonLabel = new Label(I18n.getInstance().getString("plugin.loadprofile.label.output"));
        Button outputButton = new Button(I18n.getInstance().getString("plugin.loadprofile.button.output"));
        AnalysisLinkButton analysisLinkButton = new AnalysisLinkButton();
        HBox outputControl = new HBox(6, outputButtonLabel, outputButton, analysisLinkButton);

        outputButton.setOnAction(actionEvent -> {
            List<JEVisClass> classList = new ArrayList<>();
            for (String className : TreeSelectionDialog.allData) {
                try {
                    classList.add(ds.getJEVisClass(className));
                } catch (JEVisException e) {
                    logger.error(e);
                }
            }
            TreeSelectionDialog selectionDialog = new TreeSelectionDialog(ds, classList, SelectionMode.SINGLE);
            selectionDialog.showAndWait();

            for (UserSelection userSelection : selectionDialog.getUserSelection()) {
                outputObject.set(userSelection.getSelectedObject());
                try {
                    analysisLinkButton.init(userSelection.getSelectedObject().getAttribute("Value"));
                } catch (Exception e) {
                    logger.error(e);
                }
                break;
            }
        });

        Label scheduleLabel = new Label(I18n.getInstance().getString("plugin.loadprofile.label.schedule"));
        Label calendarLabel = new Label(I18n.getInstance().getString("plugin.loadprofile.label.calendar"));
        VBox settingsBox = new VBox(6, scheduleLabel, workdayEditor.getEditor(), calendarLabel, calendarBox);

        Button calculateButton = new Button(I18n.getInstance().getString("plugin.loadprofile.button.calculate"));
        calculateButton.setOnAction(actionEvent -> {
            calculate();
        });

        HBox buttonBar = new HBox(6, calculateButton);

        VBox content = new VBox(6, inputControl, outputControl, settingsBox, buttonBar);

        TabPane tabPane = new TabPane();

        Tab mainPage = new Tab(I18n.getInstance().getString("plugin.loadprofile.tab.mainpage"), content);

        Tab allDaysTab = createTSTab(I18n.getInstance().getString("plugin.loadprofile.tab.alldays"), allDaysLoadProfileView);

        Tab standardLoadProfileTab = createTab(I18n.getInstance().getString("plugin.loadprofile.tab.mixed"), mixedLoadProfileView);

        tabPane.getTabs().addAll(mainPage, allDaysTab, standardLoadProfileTab);

        borderPane.setCenter(tabPane);
    }

    private void calculate() {
        ProgressForm progressForm = new ProgressForm(I18n.getInstance().getString("plugin.loadprofile.progress.calculating") + "...");

        Task<Void> calculate = new Task<Void>() {
            @Override
            protected Void call() {
                Map<DateTime, List<Double>> allDaysMap = new HashMap<>();
                Map<DateTime, List<Double>> standardDayMap = new HashMap<>();
                Map<DateTime, List<Double>> holidayDayMap = new HashMap<>();

                Map<DateTime, Double> resultMap = new HashMap<>();
                Map<DateTime, Double> resultStandardDay = new HashMap<>();
                Map<DateTime, Double> resultHolidayDay = new HashMap<>();

                List<DateTime> normalDayList = new ArrayList<>();

                for (JEVisObject object : inputList) {
                    try {
                        JEVisObject valueObject = ds.getObject(object.getID());
                        JEVisAttribute valueAttribute = valueObject.getAttribute("Value");
                        List<JEVisSample> allSamples = valueAttribute.getAllSamples();
                        JEVisObject site = CommonMethods.getFirstParentalObjectOfClass(object, "Building");

                        if (allSamples.size() < 2) {
                            continue;
                        }

                        DateTime startDateTime = allSamples.get(0).getTimestamp();
                        DateTime endDateTime = allSamples.get(allSamples.size() - 1).getTimestamp();
                        LocalDate startDate = LocalDate.of(startDateTime.getYear(), startDateTime.getMonthOfYear(), startDateTime.getDayOfMonth());
                        LocalDate endDate = LocalDate.of(endDateTime.getYear(), endDateTime.getMonthOfYear(), endDateTime.getDayOfMonth());

                        Set<Holiday> holidays = Holidays.getDefaultHolidayManager().getHolidays(startDate, endDate, "");
                        if (Holidays.getSiteHolidayManager(site) != null) {
                            holidays.addAll(Holidays.getSiteHolidayManager(site).getHolidays(startDate, endDate, Holidays.getStateCode()));
                        }

                        if (Holidays.getCustomHolidayManager(site) != null) {
                            holidays.addAll(Holidays.getCustomHolidayManager(site).getHolidays(startDate, endDate, Holidays.getStateCode()));
                        }

                        logger.debug("Found {} holidays in sample period", holidays.size());

                        for (JEVisSample sample : allSamples) {
                            DateTime ts = sample.getTimestamp();
                            DateTime standardTs = new DateTime(2024, 1, 1, ts.getHourOfDay(), ts.getMinuteOfHour(), ts.getMillisOfSecond());
                            Double value = sample.getValueAsDouble();
                            if (allDaysMap.get(ts) != null) {
                                allDaysMap.get(ts).add(value);
                            } else {
                                List<Double> doubleList = new ArrayList<>();
                                doubleList.add(value);
                                allDaysMap.put(ts, doubleList);
                            }

                            if (!workdayEditor.getInputValue().getRules().isEmpty()) {
                                JsonSchedulerRule jsonSchedulerRule = workdayEditor.getInputValue().getRules().get(0);
                                List<Integer> integers = stringToIntList(jsonSchedulerRule.getDayOfWeek());
                                if (integers.contains(ts.getDayOfWeek()) && !isHoliday(holidays, ts)) {
                                    if (standardDayMap.get(standardTs) != null) {
                                        standardDayMap.get(standardTs).add(value);
                                    } else {
                                        List<Double> doubleList = new ArrayList<>();
                                        doubleList.add(value);
                                        standardDayMap.put(standardTs, doubleList);
                                    }
                                    normalDayList.add(ts);
                                } else {
                                    if (holidayDayMap.get(standardTs) != null) {
                                        holidayDayMap.get(standardTs).add(value);
                                    } else {
                                        List<Double> doubleList = new ArrayList<>();
                                        doubleList.add(value);
                                        holidayDayMap.put(standardTs, doubleList);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }

                double max = 0d;
                for (Map.Entry<DateTime, List<Double>> entry : allDaysMap.entrySet()) {
                    DateTime dateTime = entry.getKey();
                    List<Double> doubles = entry.getValue();
                    double result = doubles.stream().mapToDouble(aDouble -> aDouble).sum();
                    double v = result / doubles.size();
                    resultMap.put(dateTime, v);
                    max = Math.max(max, v);
                }

                for (DateTime dateTime : resultMap.keySet()) {
                    resultMap.replace(dateTime, (resultMap.get(dateTime) / max));
                }

                for (Map.Entry<DateTime, List<Double>> entry : standardDayMap.entrySet()) {
                    DateTime dateTime = entry.getKey();
                    List<Double> doubles = entry.getValue();
                    double result = doubles.stream().mapToDouble(aDouble -> aDouble).sum();
                    double v = result / doubles.size();
                    resultStandardDay.put(dateTime, v);
                }

                for (DateTime dateTime : resultStandardDay.keySet()) {
                    resultStandardDay.replace(dateTime, (resultStandardDay.get(dateTime) / max));
                }

                for (Map.Entry<DateTime, List<Double>> entry : holidayDayMap.entrySet()) {
                    DateTime dateTime = entry.getKey();
                    List<Double> doubles = entry.getValue();
                    double result = doubles.stream().mapToDouble(aDouble -> aDouble).sum();
                    double v = result / doubles.size();
                    resultHolidayDay.put(dateTime, v);
                }

                for (DateTime dateTime : resultHolidayDay.keySet()) {
                    resultHolidayDay.replace(dateTime, (resultHolidayDay.get(dateTime) / max));
                }

                List<TableSample> allDaysLoadProfileSamples = new ArrayList<>();
                resultMap.forEach((dateTime, aDouble) -> {
                    JEVisSample sample = new VirtualSample(dateTime, aDouble);
                    TableSample tableSample = new TableSample(sample);
                    tableSample.setHoliday(!normalDayList.contains(dateTime));
                    Double diffValue;
                    if (!tableSample.isHoliday()) {
                        diffValue = getCorrespondingValue(tableSample, resultStandardDay);
                    } else {
                        diffValue = getCorrespondingValue(tableSample, resultHolidayDay);
                    }
                    tableSample.setDiff(diffValue);
                    allDaysLoadProfileSamples.add(tableSample);
                });

                Platform.runLater(() -> {
                    allDaysLoadProfileList.clear();
                    allDaysLoadProfileList.addAll(allDaysLoadProfileSamples);
                    allDaysLoadProfileView.sort();
                });

                List<MixedLoadProfile> standardLoadProfileSamples = new ArrayList<>();
                resultStandardDay.forEach((dateTime, aDouble) -> {
                    MixedLoadProfile tableSample = new MixedLoadProfile(dateTime, aDouble, resultHolidayDay.get(dateTime));
                    standardLoadProfileSamples.add(tableSample);
                });

                Platform.runLater(() -> {
                    mixedLoadProfileList.clear();
                    mixedLoadProfileList.addAll(standardLoadProfileSamples);
                    mixedLoadProfileView.sort();
                });

                try {
                    JEVisAttribute value = outputObject.get().getAttribute("Value");
                    value.deleteAllSample();

                    List<JEVisSample> samples = new ArrayList<>();
                    resultMap.forEach((key, value1) -> {
                        try {
                            samples.add(value.buildSample(key, value1));
                        } catch (JEVisException e) {
                            logger.error(e);
                        }
                    });
                    value.addSamples(samples);
                } catch (Exception e) {
                    logger.error(e);
                }
                return null;
            }
        };

        progressForm.activateProgressBar(calculate);

        calculate.setOnSucceeded(event -> progressForm.getDialogStage().close());

        calculate.setOnCancelled(event -> {
            logger.debug("calculation got cancelled");
            progressForm.getDialogStage().close();
        });

        calculate.setOnFailed(event -> {
            logger.debug("calculation failed");
            progressForm.getDialogStage().close();
        });

        progressForm.getDialogStage().show();
        ControlCenter.getStatusBar().addTask(this.getName(), calculate, null, true);
    }

    private Double getCorrespondingValue(TableSample tableSample, Map<DateTime, Double> resultStandardDay) {
        Double diffValue = null;
        DateTime dateTime = tableSample.getDateTime();
        Double value = tableSample.getValue();

        for (Map.Entry<DateTime, Double> entry : resultStandardDay.entrySet()) {
            DateTime dateTime1 = entry.getKey();
            Double value1 = entry.getValue();

            if (dateTime.getHourOfDay() == dateTime1.getHourOfDay() && dateTime.getMinuteOfHour() == dateTime1.getMinuteOfHour()
                    && dateTime.getSecondOfMinute() == dateTime1.getSecondOfMinute() && dateTime.getMillisOfSecond() == dateTime1.getMillisOfSecond()) {
                diffValue = (1 - value / value1) * 100;
                break;
            }
        }

        return diffValue;
    }

    private Tab createTab(String name, TableView<MixedLoadProfile> tableView) {
        TimeZoneBox timeZoneBox = new TimeZoneBox();
        timeZoneBox.getSelectionModel().select(DateTimeZone.UTC);
        AtomicReference<DateTimeFormatter> dateViewFormat = new AtomicReference<>(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").withZone(DateTimeZone.getDefault()));

        timeZoneBox.getSelectionModel().selectedItemProperty().addListener((observableValue, dateTimeZone, t1) -> {
            dateViewFormat.set(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").withZone(dateTimeZone));
            tableView.refresh();
        });

        VBox loadProfileTabBox = new VBox(6, timeZoneBox, tableView);
        Tab loadProfileTab = new Tab(name, loadProfileTabBox);

        TableColumn<MixedLoadProfile, DateTime> timeStampColumn = createTimeStampColumn(I18n.getInstance().getString("plugins.loadprofile.cols.timestamp"), dateViewFormat);
        TableColumn<MixedLoadProfile, Object> workdayValueCol = createWorkdayValueColumn(I18n.getInstance().getString("plugins.loadprofile.cols.workdayvalue"));
        TableColumn<MixedLoadProfile, Object> holidayValueCol = createHolidayValueColumn(I18n.getInstance().getString("plugins.loadprofile.cols.holidayvalue"));
        tableView.getColumns().addAll(timeStampColumn, workdayValueCol, holidayValueCol);
        tableView.sortPolicyProperty().set(t -> {
            Comparator<MixedLoadProfile> comparator = Comparator.comparing(MixedLoadProfile::getDateTime);
            FXCollections.sort(tableView.getItems(), comparator);
            return true;
        });

        return loadProfileTab;
    }

    private TableColumn<MixedLoadProfile, DateTime> createTimeStampColumn(String columnName, AtomicReference<DateTimeFormatter> dateViewFormat) {
        TableColumn<MixedLoadProfile, DateTime> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        column.setCellFactory(new Callback<TableColumn<MixedLoadProfile, DateTime>, TableCell<MixedLoadProfile, DateTime>>() {
            @Override
            public TableCell<MixedLoadProfile, DateTime> call(TableColumn<MixedLoadProfile, DateTime> param) {
                return new TableCell<MixedLoadProfile, DateTime>() {
                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            MixedLoadProfile tableSample = (MixedLoadProfile) getTableRow().getItem();
                            setText(dateViewFormat.get().print(tableSample.getDateTime()));
                        }
                    }
                };
            }
        });

        return column;
    }

    private TableColumn<MixedLoadProfile, Object> createWorkdayValueColumn(String columnName) {

        TableColumn<MixedLoadProfile, Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>("workdayValue"));

        column.setCellFactory(org.jevis.jecc.plugin.loadprofile.controls.ValueFieldTableCell.forTableColumn());

        return column;
    }

    private TableColumn<MixedLoadProfile, Object> createHolidayValueColumn(String columnName) {

        TableColumn<MixedLoadProfile, Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>("holidayValue"));

        column.setCellFactory(org.jevis.jecc.plugin.loadprofile.controls.ValueFieldTableCell.forTableColumn());

        return column;
    }

    private Tab createTSTab(String name, TableView<TableSample> tableView) {
        TimeZoneBox timeZoneBox = new TimeZoneBox();
        timeZoneBox.getSelectionModel().select(DateTimeZone.UTC);
        AtomicReference<DateTimeFormatter> dateViewFormat = new AtomicReference<>(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").withZone(DateTimeZone.getDefault()));

        timeZoneBox.getSelectionModel().selectedItemProperty().addListener((observableValue, dateTimeZone, t1) -> {
            dateViewFormat.set(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").withZone(dateTimeZone));
            tableView.refresh();
        });

        VBox loadProfileTabBox = new VBox(6, timeZoneBox, tableView);
        Tab loadProfileTab = new Tab(name, loadProfileTabBox);

        TableColumn<TableSample, DateTime> timeStampColumn = createTSTimeStampColumn(I18n.getInstance().getString("plugins.loadprofile.cols.timestamp"), dateViewFormat);
        TableColumn<TableSample, Object> valueCol = createValueColumn(I18n.getInstance().getString("plugins.loadprofile.cols.value"));
        TableColumn<TableSample, Object> diffCol = createDiffColumn(I18n.getInstance().getString("plugins.loadprofile.cols.diff"));
        tableView.getColumns().addAll(timeStampColumn, valueCol, diffCol);
        tableView.sortPolicyProperty().set(t -> {
            Comparator<TableSample> comparator = Comparator.comparing(TableSample::getDateTime);
            FXCollections.sort(tableView.getItems(), comparator);
            return true;
        });

        return loadProfileTab;
    }

    private TableColumn<TableSample, DateTime> createTSTimeStampColumn(String columnName, AtomicReference<DateTimeFormatter> dateViewFormat) {
        TableColumn<TableSample, DateTime> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        column.setCellFactory(new Callback<TableColumn<TableSample, DateTime>, TableCell<TableSample, DateTime>>() {
            @Override
            public TableCell<TableSample, DateTime> call(TableColumn<TableSample, DateTime> param) {
                return new TableCell<TableSample, DateTime>() {


                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            TableSample tableSample = (TableSample) getTableRow().getItem();
                            setText(dateViewFormat.get().print(tableSample.getDateTime()));
                            setAlignment(Pos.CENTER);

                            if (tableSample.isHoliday()) {
                                setTextFill(Color.RED);
                            } else {
                                setTextFill(Color.BLACK);
                            }
                        }
                    }
                };
            }
        });

        return column;
    }

    private TableColumn<TableSample, Object> createValueColumn(String columnName) {

        TableColumn<TableSample, Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>("value"));

        column.setCellFactory(valueCellDouble());

        return column;
    }

    private TableColumn<TableSample, Object> createDiffColumn(String columnName) {

        TableColumn<TableSample, Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>("diff"));

        column.setCellFactory(diffCellDouble());

        return column;
    }

    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellDouble() {
        return new Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>>() {
            @Override
            public TableCell<TableSample, Object> call(TableColumn<TableSample, Object> param) {
                return new TableCell<TableSample, Object>() {

                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            try {
                                TableSample tableSample = (TableSample) getTableRow().getItem();

                                setText(nf.format(tableSample.getValue()) + " " + UnitManager.getInstance().format(tableSample.getJEVisSample().getUnit()));
                                setAlignment(Pos.CENTER_RIGHT);
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        }
                    }
                };
            }

        };
    }

    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> diffCellDouble() {
        return new Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>>() {
            @Override
            public TableCell<TableSample, Object> call(TableColumn<TableSample, Object> param) {
                return new TableCell<TableSample, Object>() {

                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            try {
                                TableSample tableSample = (TableSample) getTableRow().getItem();
                                String text = nf.format(tableSample.getDiff()) + " %";
                                setAlignment(Pos.CENTER_RIGHT);

                                if (tableSample.getDiff() > 0) {
                                    text = "+" + text;
                                    setTextFill(Color.RED);
                                } else {
                                    setTextFill(Color.GREEN);
                                }

                                setText(text);
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        }
                    }
                };
            }

        };
    }

    private void initToolbar() {
        ToggleButton reload = new ToggleButton("", ControlCenter.getSVGImage(Icon.REFRESH, iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.loadprofile.tooptip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> handleRequest(Constants.Plugin.Command.RELOAD));

        toolBar.getItems().setAll(reload, new Separator());

        ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
        ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);

        toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);
        JEVisHelp.getInstance().addHelpItems(this.getClass().getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, toolBar.getItems());
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

    private List<Integer> stringToIntList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            List<Integer> integers = new ArrayList<>();
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");

            for (String str : tempList) {
                integers.add(Integer.parseInt(str));
            }

            return integers;
        } else return new ArrayList<>();
    }
}
