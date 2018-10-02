package org.jevis.jeconfig.plugin.graph;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import jfxtras.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.CustomPeriodObject;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.jeconfig.plugin.graph.view.ToolBarView;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */
public class LoadAnalysisDialog extends Dialog<ButtonType> {
    private final Logger logger = LogManager.getLogger(LoadAnalysisDialog.class);
    private String nameCurrentAnalysis;
    private GraphDataModel graphDataModel = new GraphDataModel();
    private ToolBarView toolBarView;
    private JFXDatePicker pickerDateStart = new JFXDatePicker();
    private JFXTimePicker pickerTimeStart = new JFXTimePicker();
    private JFXDatePicker pickerDateEnd = new JFXDatePicker();
    private JFXTimePicker pickerTimeEnd = new JFXTimePicker();
    private jfxtras.scene.control.ListView<String> lv = new ListView<>();
    private List<JEVisObject> listAnalyses;
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private List<JsonAnalysisModel> listAnalysisModel = new ArrayList<>();
    private DateTime selectedStart = DateTime.now().minusDays(7);
    private DateTime selectedEnd = DateTime.now();
    private JEVisObject currentAnalysis;
    private JEVisDataSource ds;
    private Boolean initialTimeFrame = true;
    private DateTime lastSampleTimeStamp;
    private DateHelper dateHelper = new DateHelper();

    public LoadAnalysisDialog(JEVisDataSource ds, GraphDataModel data, ToolBarView toolBarView) {
        this.graphDataModel = data;
        this.ds = ds;
        this.toolBarView = toolBarView;

        initialize();
    }

    private void initialize() {
        if (toolBarView.getWorkdayStart() != null) dateHelper.setStartTime(toolBarView.getWorkdayStart());
        if (toolBarView.getWorkdayEnd() != null) dateHelper.setEndTime(toolBarView.getWorkdayEnd());

        if (toolBarView.getWorkdayStart() != null && toolBarView.getWorkdayEnd() != null) {
            if (toolBarView.getWorkdayEnd().isAfter(toolBarView.getWorkdayStart())) {
                selectedStart = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        toolBarView.getWorkdayStart().getHour(), toolBarView.getWorkdayStart().getMinute(), 0).minusDays(7);
                selectedEnd = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        toolBarView.getWorkdayEnd().getHour(), toolBarView.getWorkdayEnd().getMinute(), 59, 999);
            } else {
                selectedStart = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        toolBarView.getWorkdayStart().getHour(), toolBarView.getWorkdayStart().getMinute(), 0).minusDays(8);
                selectedEnd = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                        toolBarView.getWorkdayEnd().getHour(), toolBarView.getWorkdayEnd().getMinute(), 59, 999);
            }
        }

        updateListAnalyses();
        getListAnalysis();

        HBox hbox_list = new HBox();
        hbox_list.getChildren().add(lv);
        HBox.setHgrow(lv, Priority.ALWAYS);

        final Callback<DatePicker, DateCell> dayCellFactory
                = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        LocalDate min = null;
                        LocalDate max = null;
                        for (ChartDataModel mdl : graphDataModel.getSelectedData()) {
                            JEVisAttribute att = mdl.getAttribute();

                            LocalDate min_check = LocalDate.of(
                                    att.getTimestampFromFirstSample().getYear(),
                                    att.getTimestampFromFirstSample().getMonthOfYear(),
                                    att.getTimestampFromFirstSample().getDayOfMonth());

                            LocalDate max_check = LocalDate.of(
                                    att.getTimestampFromLastSample().getYear(),
                                    att.getTimestampFromLastSample().getMonthOfYear(),
                                    att.getTimestampFromLastSample().getDayOfMonth());

                            if (min == null || min_check.isBefore(min)) min = min_check;
                            if (max == null || max_check.isAfter(max)) max = max_check;

                        }

                        if (min != null && item.isBefore(min)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (max != null && item.isAfter(max)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };

        Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate"));
        pickerDateStart.setPrefWidth(120d);
        pickerDateStart.setDayCellFactory(dayCellFactory);
        pickerTimeStart.setIs24HourView(true);
        pickerTimeStart.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

        Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        pickerDateEnd.setPrefWidth(120d);
        pickerDateEnd.setDayCellFactory(dayCellFactory);
        pickerTimeEnd.setIs24HourView(true);
        pickerTimeEnd.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

        ObservableList<String> presetDateEntries = FXCollections.observableArrayList();
        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
        final String today = I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String yesterday = I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");

        presetDateEntries.addAll(custom, today, last7Days, last30Days, yesterday, lastWeek, lastMonth);
        ComboBox<String> comboBoxPresetDates = new ComboBox(presetDateEntries);
        comboBoxPresetDates.getSelectionModel().select(2);

        ComboBox<String> comboBoxCustomPeriods = getCustomPeriodsComboBox();

        if (!listAnalysisModel.isEmpty()) {
            updateTimeFramePicker();
        }

        comboBoxPresetDates.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                switch (newValue.intValue()) {
                    //Custom
                    case 0:
                        break;
                    //today
                    case 1:
                        dateHelper.setType(DateHelper.TransformType.TODAY);
                        break;
                    //last 7 days
                    case 2:
                        dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                        break;
                    //last 30 days
                    case 3:
                        dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                        break;
                    //yesterday
                    case 4:
                        dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                        break;
                    //last Week days
                    case 5:
                        dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                        break;
                    case 6:
                        //last Month
                        dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                        break;
                    default:
                        break;
                }
                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                updateTimeFrame();
                updateToolBarView();
            }
        });

        setupPickerListener();

        GridPane gp_date = new GridPane();

        HBox startBox = new HBox();
        startBox.setSpacing(4);
        startBox.getChildren().addAll(pickerDateStart, pickerTimeStart);

        HBox endBox = new HBox();
        endBox.setSpacing(4);
        endBox.getChildren().addAll(pickerDateEnd, pickerTimeEnd);

        VBox vbox_picker = new VBox();
        vbox_picker.setSpacing(4);
        vbox_picker.getChildren().addAll(startText, startBox, endText, endBox);
        VBox vbox_buttons = new VBox();
        vbox_buttons.setSpacing(4);
        Label standardSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.standard"));
        Label customSelectionsLabel = new Label(I18n.getInstance().getString("plugin.graph.analysis.label.custom"));
        vbox_buttons.getChildren().addAll(standardSelectionsLabel, comboBoxPresetDates, customSelectionsLabel, comboBoxCustomPeriods);
        vbox_buttons.setAlignment(Pos.BOTTOM_RIGHT);
        gp_date.add(vbox_picker, 0, 0);
        gp_date.add(vbox_buttons, 1, 0);
        gp_date.setPrefWidth(hbox_list.getWidth());
        gp_date.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        VBox vbox = new VBox();
        vbox.setSpacing(14);
        vbox.getChildren().addAll(hbox_list, gp_date);
        vbox.setPrefWidth(600);

        final ButtonType newGraph = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.new"), ButtonBar.ButtonData.FINISH);
        final ButtonType loadGraph = new ButtonType(I18n.getInstance().getString("plugin.graph.analysis.load"), ButtonBar.ButtonData.NO);

        lv.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                this.nameCurrentAnalysis = newValue;
                setJEVisObjectForCurrentAnalysis(newValue);

                if (lastSampleTimeStamp != null) {
                    if (toolBarView.getWorkdayEnd().isAfter(toolBarView.getWorkdayStart())) {
                        if (selectedStart.minusDays(7).isBefore(lastSampleTimeStamp)) {
                            selectedStart = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                                    toolBarView.getWorkdayStart().getHour(), toolBarView.getWorkdayStart().getMinute(), toolBarView.getWorkdayStart().getSecond());
                            selectedStart = selectedStart.minusDays(7);
                        } else {
                            selectedStart = new DateTime(lastSampleTimeStamp.getYear(), lastSampleTimeStamp.getMonthOfYear(), lastSampleTimeStamp.getDayOfMonth(),
                                    toolBarView.getWorkdayStart().getHour(), toolBarView.getWorkdayStart().getMinute(), toolBarView.getWorkdayStart().getSecond());
                            selectedStart = selectedStart.minusDays(7);
                        }
                        if (selectedEnd.isBefore(lastSampleTimeStamp)) {
                            selectedEnd = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                                    toolBarView.getWorkdayEnd().getHour(), toolBarView.getWorkdayEnd().getMinute(), toolBarView.getWorkdayEnd().getSecond());
                        } else {
                            selectedEnd = new DateTime(lastSampleTimeStamp.getYear(), lastSampleTimeStamp.getMonthOfYear(), lastSampleTimeStamp.getDayOfMonth(),
                                    toolBarView.getWorkdayEnd().getHour(), toolBarView.getWorkdayEnd().getMinute(), toolBarView.getWorkdayEnd().getSecond());
                        }
                    } else {
                        if (selectedStart.minusDays(8).isBefore(lastSampleTimeStamp)) {
                            selectedStart = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                                    toolBarView.getWorkdayStart().getHour(), toolBarView.getWorkdayStart().getMinute(), toolBarView.getWorkdayStart().getSecond());
                            selectedStart = selectedStart.minusDays(8);
                        } else {
                            selectedStart = new DateTime(lastSampleTimeStamp.getYear(), lastSampleTimeStamp.getMonthOfYear(), lastSampleTimeStamp.getDayOfMonth(),
                                    toolBarView.getWorkdayStart().getHour(), toolBarView.getWorkdayStart().getMinute(), toolBarView.getWorkdayStart().getSecond());
                            selectedStart = selectedStart.minusDays(8);
                        }
                        if (selectedEnd.isBefore(lastSampleTimeStamp)) {
                            selectedEnd = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(),
                                    toolBarView.getWorkdayEnd().getHour(), toolBarView.getWorkdayEnd().getMinute(), toolBarView.getWorkdayEnd().getSecond());
                        } else {
                            selectedEnd = new DateTime(lastSampleTimeStamp.getYear(), lastSampleTimeStamp.getMonthOfYear(), lastSampleTimeStamp.getDayOfMonth(),
                                    toolBarView.getWorkdayEnd().getHour(), toolBarView.getWorkdayEnd().getMinute(), toolBarView.getWorkdayEnd().getSecond());
                        }
                    }
                }

                updateTimeFrame();
                updateToolBarView();
                toolBarView.select(nameCurrentAnalysis);

                //getListAnalysis();
                //getTimeFromJsonModel();
                //updateTimeFramePicker();
                //updateTimeFrame();


                if (oldValue == null) {
                    this.getDialogPane().getButtonTypes().clear();
                    this.getDialogPane().getButtonTypes().addAll(newGraph, loadGraph);
                }
            }
        });
        this.setTitle(I18n.getInstance().getString("plugin.graph.analysis.dialog.title"));


        this.getDialogPane().getButtonTypes().add(newGraph);

        this.getDialogPane().setContent(vbox);

    }

    private void setupPickerListener() {
        pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateStart.valueProperty().getValue();
                LocalTime lt = pickerTimeStart.valueProperty().getValue();
                selectedStart = new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                        lt.getHour(), lt.getMinute(), lt.getSecond());
            }
        });

        pickerTimeStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateStart.valueProperty().getValue();
                LocalTime lt = pickerTimeStart.valueProperty().getValue();
                selectedStart = new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                        lt.getHour(), lt.getMinute(), lt.getSecond());
            }
        });

        pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateEnd.valueProperty().getValue();
                LocalTime lt = pickerTimeEnd.valueProperty().getValue();
                selectedEnd = new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                        lt.getHour(), lt.getMinute(), lt.getSecond());
            }
        });

        pickerTimeEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                LocalDate ld = pickerDateEnd.valueProperty().getValue();
                LocalTime lt = pickerTimeEnd.valueProperty().getValue();
                selectedEnd = new DateTime(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(),
                        lt.getHour(), lt.getMinute(), lt.getSecond());
            }
        });
    }


    private ComboBox<String> getCustomPeriodsComboBox() {

        ObservableList<String> customPeriods = FXCollections.observableArrayList();
        List<JEVisObject> listCalendarDirectories = null;
        List<JEVisObject> listCustomPeriods = null;
        List<CustomPeriodObject> listCustomPeriodObjects = null;

        try {
            try {
                JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                listCalendarDirectories = ds.getObjects(calendarDirectoryClass, false);
            } catch (JEVisException e) {
                logger.error("Error: could not get calendar directories", e);
            }
            if (listCalendarDirectories.isEmpty()) {
                List<JEVisObject> listBuildings = new ArrayList<>();
                try {
                    JEVisClass building = ds.getJEVisClass("Building");
                    listBuildings = ds.getObjects(building, false);

                    if (!listBuildings.isEmpty()) {
                        JEVisClass calendarDirectoryClass = ds.getJEVisClass("Calendar Directory");
                        JEVisObject calendarDirectory = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.calendardir.defaultname"), calendarDirectoryClass);
                        calendarDirectory.commit();
                    }
                } catch (JEVisException e) {
                    logger.error("Error: could not create new calendar directory", e);
                }

            }
            try {
                listCustomPeriods = ds.getObjects(ds.getJEVisClass("Custom Period"), false);
            } catch (JEVisException e) {
                logger.error("Error: could not get custom period", e);
            }
        } catch (Exception e) {
        }

        customPeriods.add(I18n.getInstance().getString("plugin.graph.dialog.loadnew.none"));

        for (JEVisObject obj : listCustomPeriods) {
            if (obj != null) {
                if (listCustomPeriodObjects == null) listCustomPeriodObjects = new ArrayList<>();
                CustomPeriodObject cpo = new CustomPeriodObject(obj, new ObjectHandler(ds));
                if (cpo.isVisible()) {
                    listCustomPeriodObjects.add(cpo);
                    customPeriods.add(cpo.getObject().getName());
                }
            }
        }

        ComboBox tempBox = new ComboBox<>(customPeriods);
        tempBox.getSelectionModel().select(0);

        if (customPeriods.size() > 1) {

            List<CustomPeriodObject> finalListCustomPeriodObjects = listCustomPeriodObjects;
            tempBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue == null || newValue != oldValue) {
                    if (newValue.intValue() > 0) {
                        for (CustomPeriodObject cpo : finalListCustomPeriodObjects) {
                            if (finalListCustomPeriodObjects.indexOf(cpo) + 1 == newValue.intValue()) {
                                dateHelper.setCustomPeriodObject(cpo);
                                dateHelper.setType(DateHelper.TransformType.CUSTOM_PERIOD);
                                dateHelper.setStartTime(toolBarView.getWorkdayStart());
                                dateHelper.setEndTime(toolBarView.getWorkdayEnd());

                                setPicker(dateHelper.getStartDate(), dateHelper.getEndDate());
                            }
                        }
                    }
                }
            });
        } else {
            tempBox.setDisable(true);
        }

        return tempBox;
    }

    private void setPicker(DateTime start, DateTime end) {
        selectedStart = start;
        selectedEnd = end;
        pickerDateStart.valueProperty().setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
        pickerDateEnd.valueProperty().setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
        pickerTimeStart.valueProperty().setValue(LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute()));
        pickerTimeEnd.valueProperty().setValue(LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute()));
    }

    private void updateTimeFramePicker() {

        LocalDate ld_start = LocalDate.of(selectedStart.getYear(), selectedStart.getMonthOfYear(), selectedStart.getDayOfMonth());
        LocalTime lt_start = LocalTime.of(selectedStart.getHourOfDay(), selectedStart.getMinuteOfHour());
        pickerDateStart.valueProperty().setValue(ld_start);
        pickerTimeStart.valueProperty().setValue(lt_start);

        LocalDate ld_end = LocalDate.of(selectedEnd.getYear(), selectedEnd.getMonthOfYear(), selectedEnd.getDayOfMonth());
        LocalTime lt_end = LocalTime.of(selectedEnd.getHourOfDay(), selectedEnd.getMinuteOfHour());
        pickerDateEnd.valueProperty().setValue(ld_end);
        pickerTimeEnd.valueProperty().setValue(lt_end);

    }

    public void updateTimeFrame() {
        if (graphDataModel.getSelectedData() != null) {
            for (ChartDataModel mdl : graphDataModel.getSelectedData()) {
                if (mdl.getSelected()) {
                    mdl.setSelectedStart(selectedStart);
                    mdl.setSelectedEnd(selectedEnd);
                }
            }
        }

        if (!listAnalysisModel.isEmpty()) {
            for (JsonAnalysisModel mdl : listAnalysisModel) {
                if (Boolean.parseBoolean(mdl.getSelected())) {
                    mdl.setSelectedStart(selectedStart.toString());
                    mdl.setSelectedEnd(selectedEnd.toString());
                }
            }
        }
    }

    public void updateToolBarView() {
        toolBarView.setSelectedStart(this.selectedStart);
        toolBarView.setSelectedEnd(this.selectedEnd);
    }

    public ListView<String> getLv() {
        return lv;
    }

    public void updateListAnalyses() {
        List<JEVisObject> listAnalysesDirectories = new ArrayList<>();
        try {
            JEVisClass analysesDirectory = ds.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            logger.error("Error: could not get analyses directories", e);
        }
        if (listAnalysesDirectories.isEmpty()) {
            List<JEVisObject> listBuildings = new ArrayList<>();
            try {
                JEVisClass building = ds.getJEVisClass("Building");
                listBuildings = ds.getObjects(building, false);

                if (!listBuildings.isEmpty()) {
                    JEVisClass analysesDirectory = ds.getJEVisClass("Analyses Directory");
                    JEVisObject analysesDir = listBuildings.get(0).buildObject(I18n.getInstance().getString("plugin.graph.analysesdir.defaultname"), analysesDirectory);
                    analysesDir.commit();
                }
            } catch (JEVisException e) {
                logger.error("Error: could not create new analyses directory", e);
            }

        }
        try {
            listAnalyses = ds.getObjects(ds.getJEVisClass("Analysis"), false);
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis", e);
        }
        observableListAnalyses.clear();
        for (JEVisObject obj : listAnalyses) {
            observableListAnalyses.add(obj.getName());
        }
        Collections.sort(observableListAnalyses, new AlphanumComparator());
        lv.setItems(observableListAnalyses);
    }

    private void setJEVisObjectForCurrentAnalysis(String s) {
        JEVisObject currentAnalysis = null;
        for (JEVisObject obj : listAnalyses) {
            if (obj.getName().equals(s)) {
                currentAnalysis = obj;
            }
        }
        this.currentAnalysis = currentAnalysis;
    }

    public void getListAnalysis() {
        try {
            if (currentAnalysis == null) {
                updateListAnalyses();
                if (!observableListAnalyses.isEmpty())
                    setJEVisObjectForCurrentAnalysis(observableListAnalyses.get(0));
            }
            if (currentAnalysis != null) {
                if (Objects.nonNull(currentAnalysis.getAttribute("Data Model"))) {
                    if (currentAnalysis.getAttribute("Data Model").hasSample()) {
                        String str = currentAnalysis.getAttribute("Data Model").getLatestSample().getValueAsString();
                        try {
                            if (str.endsWith("]")) {
                                listAnalysisModel = new Gson().fromJson(str, new TypeToken<List<JsonAnalysisModel>>() {
                                }.getType());

                            } else {
                                listAnalysisModel = new ArrayList<>();
                                listAnalysisModel.add(new Gson().fromJson(str, JsonAnalysisModel.class));
                            }
                        } catch (Exception e) {
                            logger.error("Error: could not read data model", e);
                        }
                    }
                }
                getLastSampleTimestamp();
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }
    }

    private void getLastSampleTimestamp() {
        DateTime current = new DateTime(2001, 1, 1, 0, 0, 0, 0);
        for (JsonAnalysisModel mdl : listAnalysisModel) {
            ChartDataModel newData = new ChartDataModel();
            try {
                Long id = Long.parseLong(mdl.getObject());
                Long id_dp = null;
                if (mdl.getDataProcessorObject() != null) id_dp = Long.parseLong(mdl.getDataProcessorObject());
                JEVisObject obj = ds.getObject(id);
                JEVisObject obj_dp = null;
                if (mdl.getDataProcessorObject() != null) obj_dp = ds.getObject(id_dp);
                newData.setObject(obj);
                newData.setDataProcessor(obj_dp);
                newData.getAttribute();
                DateTime latestSampleTS = newData.getAttribute().getLatestSample().getTimestamp();
                if (latestSampleTS.isAfter(current)) current = latestSampleTS;
            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (!current.equals(new DateTime(2001, 1, 1, 0, 0, 0, 0))) lastSampleTimeStamp = current;
    }

    public GraphDataModel getData() {
        return graphDataModel;
    }

    private List<String> stringToList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");
            return tempList;
        } else return new ArrayList<>();
    }

    public DateTime getCustomDateStart() {
        LocalDate startDate = pickerDateStart.valueProperty().getValue();
        LocalTime startTime = pickerTimeStart.valueProperty().getValue();
        return new DateTime(startDate.getYear(), startDate.getMonth().getValue(), startDate.getDayOfMonth(),
                startTime.getHour(), startTime.getMinute(), startTime.getSecond());
    }

    public DateTime getCustomDateEnd() {
        LocalDate endDate = pickerDateEnd.valueProperty().getValue();
        LocalTime endTime = pickerTimeEnd.valueProperty().getValue();
        return new DateTime(endDate.getYear(), endDate.getMonth().getValue(), endDate.getDayOfMonth(),
                endTime.getHour(), endTime.getMinute(), endTime.getSecond());
    }

    public Boolean getInitialTimeFrame() {
        return initialTimeFrame;
    }

    public void setSelectedStart(DateTime selectedStart) {
        this.selectedStart = selectedStart;
    }

    public void setSelectedEnd(DateTime selectedEnd) {
        this.selectedEnd = selectedEnd;
    }
}
