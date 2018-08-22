package org.jevis.jeconfig.plugin.graph;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
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
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.application.jevistree.plugin.ChartDataModel;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.plugin.graph.view.ToolBarView;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;

public class LoadAnalysisDialog extends Dialog<ButtonType> {
    private String nameCurrentAnalysis;

    private GraphDataModel data = new GraphDataModel();
    private ToolBarView toolBarView;
    private JFXDatePicker pickerDateStart = new JFXDatePicker();
    private JFXTimePicker pickerTimeStart = new JFXTimePicker();
    private JFXDatePicker pickerDateEnd = new JFXDatePicker();
    private JFXTimePicker pickerTimeEnd = new JFXTimePicker();
    private jfxtras.scene.control.ListView<String> lv = new ListView<>();
    private List<JEVisObject> listAnalyses;
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private List<JsonAnalysisModel> listAnalysisModel = new ArrayList<>();
    private DateTime selectedStart;
    private DateTime selectedEnd;
    private JEVisObject currentAnalysis;
    private JEVisDataSource ds;
    private final Logger logger = LogManager.getLogger(LoadAnalysisDialog.class);
    private Boolean initialTimeFrame = true;

    public LoadAnalysisDialog(JEVisDataSource ds, GraphDataModel data, ToolBarView toolBarView) {
        this.data = data;
        this.ds = ds;
        this.toolBarView = toolBarView;

        initialize();
    }

    private void initialize() {

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
                        for (ChartDataModel mdl : data.getSelectedData()) {
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
        final String lastDay = I18n.getInstance().getString("plugin.graph.changedate.buttonlastday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");

        presetDateEntries.addAll(custom, lastDay, last7Days, last30Days, lastWeek, lastMonth);
        ComboBox<String> comboBoxPresetDates = new ComboBox(presetDateEntries);
        comboBoxPresetDates.getSelectionModel().select(3);

        if (!listAnalysisModel.isEmpty()) {
            getTimeFromJsonModel();
            updateTimeFramePicker();
        }

        comboBoxPresetDates.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                DateHelper dh = new DateHelper();
                switch (newValue.intValue()) {
                    //Custom
                    case 0:
                        break;
                    //last day
                    case 1:
                        dh = new DateHelper(DateHelper.TransformType.LASTDAY);
                        pickerDateStart.valueProperty().setValue(dh.getStartDate());
                        pickerDateEnd.valueProperty().setValue(dh.getEndDate());
                        pickerTimeStart.valueProperty().setValue(dh.getStartTime());
                        pickerTimeEnd.valueProperty().setValue(dh.getEndTime());
                        break;
                    //last 7 days
                    case 2:
                        dh = new DateHelper(DateHelper.TransformType.LAST7DAYS);
                        pickerDateStart.valueProperty().setValue(dh.getStartDate());
                        pickerDateEnd.valueProperty().setValue(dh.getEndDate());
                        pickerTimeStart.valueProperty().setValue(dh.getStartTime());
                        break;
                    //last 30 days
                    case 3:
                        dh = new DateHelper(DateHelper.TransformType.LAST30DAYS);
                        pickerDateStart.valueProperty().setValue(dh.getStartDate());
                        pickerDateEnd.valueProperty().setValue(dh.getEndDate());
                        pickerTimeStart.valueProperty().setValue(dh.getStartTime());
                        pickerTimeEnd.valueProperty().setValue(dh.getEndTime());
                        break;
                    //last Week days
                    case 4:
                        dh = new DateHelper(DateHelper.TransformType.LASTWEEK);
                        pickerDateStart.valueProperty().setValue(dh.getStartDate());
                        pickerDateEnd.valueProperty().setValue(dh.getEndDate());
                        pickerTimeStart.valueProperty().setValue(dh.getStartTime());
                        pickerTimeEnd.valueProperty().setValue(dh.getEndTime());
                        break;
                    case 5:
                        //last Month
                        dh = new DateHelper(DateHelper.TransformType.LASTMONTH);
                        pickerDateStart.valueProperty().setValue(dh.getStartDate());
                        pickerDateEnd.valueProperty().setValue(dh.getEndDate());
                        pickerTimeStart.valueProperty().setValue(dh.getStartTime());
                        pickerTimeEnd.valueProperty().setValue(dh.getEndTime());
                        break;
                    default:
                        break;
                }
            }
        });

        pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                initialTimeFrame = false;
                if (selectedStart != null) {
                    selectedStart = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedStart.getHourOfDay(), selectedStart.getMinuteOfHour(), selectedStart.getSecondOfMinute());
                    updateTimeFrame();
                    DateHelper dh = new DateHelper(DateHelper.InputType.STARTDATE, newValue);
                    if (dh.isCustom()) Platform.runLater(() -> comboBoxPresetDates.getSelectionModel().select(0));
                }
            }
        });

        pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                initialTimeFrame = false;
                if (selectedEnd != null) {
                    selectedEnd = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedEnd.getHourOfDay(), selectedEnd.getMinuteOfHour(), selectedEnd.getSecondOfMinute());
                    updateTimeFrame();
                    DateHelper dh = new DateHelper(DateHelper.InputType.ENDDATE, newValue);
                    if (dh.isCustom()) Platform.runLater(() -> comboBoxPresetDates.getSelectionModel().select(0));
                }
            }
        });

        pickerTimeStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                initialTimeFrame = false;
                if (selectedStart != null) {
                    selectedStart = new DateTime(selectedStart.getYear(), selectedStart.getMonthOfYear(), selectedStart.getDayOfMonth(), newValue.getHour(), newValue.getMinute(), 0, 0);
                    updateTimeFrame();
                    DateHelper dh = new DateHelper(DateHelper.InputType.STARTTIME, newValue);
                    if (dh.isCustom()) Platform.runLater(() -> comboBoxPresetDates.getSelectionModel().select(0));
                }
            }
        });

        pickerTimeEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                initialTimeFrame = false;
                if (selectedEnd != null) {
                    selectedEnd = new DateTime(selectedEnd.getYear(), selectedEnd.getMonthOfYear(), selectedEnd.getDayOfMonth(), newValue.getHour(), newValue.getMinute(), 0, 0);
                    updateTimeFrame();
                    DateHelper dh = new DateHelper(DateHelper.InputType.ENDTIME, newValue);
                    if (dh.isCustom()) Platform.runLater(() -> comboBoxPresetDates.getSelectionModel().select(0));
                }
            }
        });

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
        vbox_buttons.getChildren().addAll(comboBoxPresetDates);
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

                selectedStart = DateTime.now().minusDays(7);
                selectedEnd = new DateTime();

                updateTimeFramePicker();
                updateTimeFrame();
                updateToolBarView();
                toolBarView.select(nameCurrentAnalysis);

                getListAnalysis();
                getTimeFromJsonModel();
                updateTimeFramePicker();
                updateTimeFrame();

                initialTimeFrame = true;

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

    private void getTimeFromJsonModel() {
        if (listAnalysisModel != null && !listAnalysisModel.isEmpty()) {
            DateTime start = null;
            DateTime end = null;

            for (JsonAnalysisModel mdl : listAnalysisModel) {
                if (start == null || DateTime.parse(mdl.getSelectedStart()).isBefore(start))
                    start = DateTime.parse(mdl.getSelectedStart());
                if (end == null || DateTime.parse(mdl.getSelectedEnd()).isAfter(end))
                    end = DateTime.parse(mdl.getSelectedEnd());
            }
            selectedStart = start;
            selectedEnd = end;

            if (data.getSelectedData() != null) {
                for (ChartDataModel mdl : data.getSelectedData()) {
                    if (mdl.getSelected()) {
                        mdl.setSelectedStart(start);
                        mdl.setSelectedEnd(end);
                    }
                }
            }
        }
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
        if (data.getSelectedData() != null) {
            for (ChartDataModel mdl : data.getSelectedData()) {
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
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }
    }

    private ChartPlugin.AGGREGATION parseAggregation(String aggrigation) {
        switch (aggrigation) {
            case ("None"):
                return ChartPlugin.AGGREGATION.None;
            case ("Daily"):
                return ChartPlugin.AGGREGATION.Daily;
            case ("Weekly"):
                return ChartPlugin.AGGREGATION.Weekly;
            case ("Monthly"):
                return ChartPlugin.AGGREGATION.Monthly;
            case ("Yearly"):
                return ChartPlugin.AGGREGATION.Yearly;
            default:
                return ChartPlugin.AGGREGATION.None;
        }
    }

    public GraphDataModel getData() {
        return data;
    }

    private List<String> stringToList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");
            return tempList;
        } else return new ArrayList<>();
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
