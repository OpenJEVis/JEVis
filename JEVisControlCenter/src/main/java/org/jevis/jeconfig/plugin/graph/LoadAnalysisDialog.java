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
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import jfxtras.scene.control.ListView;
import org.jevis.api.*;
import org.jevis.application.jevistree.plugin.ChartDataModel;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
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
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");

        presetDateEntries.addAll(custom, lastDay, last30Days, lastWeek, lastMonth);
        ComboBox<String> comboBoxPresetDates = new ComboBox(presetDateEntries);

        if (!listAnalysisModel.isEmpty()) {
            updateTimeFramePicker();
        }

        comboBoxPresetDates.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {
                switch (newValue.intValue()) {
                    case 0:
                        break;
                    case 1:
                        LocalDate ld_1 = LocalDate.now();
                        LocalDate ld_start_1 = LocalDate.of(ld_1.getYear(), ld_1.getMonth(), ld_1.getDayOfMonth());
                        LocalDate ld_end_1 = LocalDate.of(ld_1.getYear(), ld_1.getMonth(), ld_1.getDayOfMonth());
                        pickerDateStart.valueProperty().setValue(ld_start_1);
                        pickerDateEnd.valueProperty().setValue(ld_end_1);
                        pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                        pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
                        break;
                    case 2:
                        LocalDate ld_2 = LocalDate.now();
                        LocalDate ld_start_2 = LocalDate.of(ld_2.getYear(), ld_2.getMonth(), ld_2.getDayOfMonth());
                        LocalDate ld_end_2 = LocalDate.of(ld_2.getYear(), ld_2.getMonth(), ld_2.getDayOfMonth());
                        pickerDateStart.valueProperty().setValue(ld_start_2.minusDays(30));
                        pickerDateEnd.valueProperty().setValue(ld_end_2);
                        pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                        pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
                        break;
                    case 3:
                        LocalDate ld_3 = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
                        ld_3 = ld_3.minusWeeks(1);
                        LocalDate ld_start_3 = LocalDate.of(ld_3.getYear(), ld_3.getMonth(), ld_3.getDayOfMonth());
                        LocalDate ld_end_3 = LocalDate.of(ld_3.getYear(), ld_3.getMonth(), ld_3.getDayOfMonth());
                        ld_end_3 = ld_end_3.plusDays(6);
                        pickerDateStart.valueProperty().setValue(ld_start_3);
                        pickerDateEnd.valueProperty().setValue(ld_end_3);
                        pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                        pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
                        break;
                    case 4:
                        LocalDate ld_4 = LocalDate.now();
                        ld_4 = ld_4.minusDays(LocalDate.now().getDayOfMonth() - 1);
                        LocalDate ld_start_4 = LocalDate.of(ld_4.getYear(), ld_4.getMonth(), ld_4.getDayOfMonth()).minusMonths(1);
                        LocalDate ld_end_4 = LocalDate.of(ld_4.getYear(), ld_4.getMonth(), ld_4.getDayOfMonth()).minusDays(1);
                        pickerDateStart.valueProperty().setValue(ld_start_4);
                        pickerDateEnd.valueProperty().setValue(ld_end_4);
                        pickerTimeStart.valueProperty().setValue(LocalTime.of(0, 0, 0, 0));
                        pickerTimeEnd.valueProperty().setValue(LocalTime.of(23, 59, 59, 999));
                        break;
                    default:
                        break;
                }
            }
        });

        pickerDateStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                if (selectedStart != null) {
                    selectedStart = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedStart.getHourOfDay(), selectedStart.getMinuteOfHour(), selectedStart.getSecondOfMinute());
                    updateTimeFrame();
                    comboBoxPresetDates.getSelectionModel().select(0);
                }
            }
        });

        pickerDateEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                if (selectedEnd != null) {
                    selectedEnd = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedEnd.getHourOfDay(), selectedEnd.getMinuteOfHour(), selectedEnd.getSecondOfMinute());
                    updateTimeFrame();
                    comboBoxPresetDates.getSelectionModel().select(0);
                }
            }
        });

        pickerTimeStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                if (selectedStart != null) {
                    selectedStart = new DateTime(selectedStart.getYear(), selectedStart.getMonthOfYear(), selectedStart.getDayOfMonth(), newValue.getHour(), newValue.getMinute(), 0, 0);
                    updateTimeFrame();
                    comboBoxPresetDates.getSelectionModel().select(0);
                }
            }
        });

        pickerTimeEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                if (selectedEnd != null) {
                    selectedEnd = new DateTime(selectedEnd.getYear(), selectedEnd.getMonthOfYear(), selectedEnd.getDayOfMonth(), newValue.getHour(), newValue.getMinute(), 0, 0);
                    updateTimeFrame();
                    comboBoxPresetDates.getSelectionModel().select(0);
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
        DateTime start = null;
        DateTime end = null;

        if (data.getSelectedData() == null) {
            for (JsonAnalysisModel mdl : listAnalysisModel) {
                if (start == null || DateTime.parse(mdl.getSelectedStart()).isBefore(start))
                    start = DateTime.parse(mdl.getSelectedStart());
                if (end == null || DateTime.parse(mdl.getSelectedEnd()).isAfter(end))
                    end = DateTime.parse(mdl.getSelectedEnd());
            }
        } else {
            for (ChartDataModel mdl : data.getSelectedData()) {
                if (mdl.getSelected()) {
                    if (start == null || mdl.getSelectedStart().isBefore(start)) start = mdl.getSelectedStart();
                    if (end == null || mdl.getSelectedEnd().isAfter(end)) end = mdl.getSelectedEnd();
                }
            }
        }

        LocalDate ld_start = LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth());
        LocalTime lt_start = LocalTime.of(start.getHourOfDay(), start.getMinuteOfHour());
        pickerDateStart.valueProperty().setValue(ld_start);
        pickerTimeStart.valueProperty().setValue(lt_start);
        selectedStart = start;

        LocalDate ld_end = LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth());
        LocalTime lt_end = LocalTime.of(end.getHourOfDay(), end.getMinuteOfHour());
        pickerDateEnd.valueProperty().setValue(ld_end);
        pickerTimeEnd.valueProperty().setValue(lt_end);
        selectedEnd = end;
    }

    private void updateTimeFrame() {
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


        toolBarView.setCurrentAnalysis(this.currentAnalysis);
        toolBarView.setListAnalyses(this.listAnalyses);
        toolBarView.setListAnalysisModel(this.listAnalysisModel);
        toolBarView.setNameCurrentAnalysis(this.nameCurrentAnalysis);
        toolBarView.setSelectedStart(this.selectedStart);
        toolBarView.setSelectedEnd(this.selectedEnd);
    }

    public ListView<String> getLv() {
        return lv;
    }

    public String getNameCurrentAnalysis() {
        return nameCurrentAnalysis;
    }

    public void updateListAnalyses() {
        List<JEVisObject> listAnalysesDirectories = new ArrayList<>();
        try {
            JEVisClass analysesDirectory = ds.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }

        }
        try {
            listAnalyses = ds.getObjects(ds.getJEVisClass("Analysis"), false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        observableListAnalyses.clear();
        for (JEVisObject obj : listAnalyses) {
            observableListAnalyses.add(obj.getName());
        }
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
                        if (str.endsWith("]")) {
                            listAnalysisModel = new Gson().fromJson(str, new TypeToken<List<JsonAnalysisModel>>() {
                            }.getType());

                        } else {
                            listAnalysisModel = new ArrayList<>();
                            listAnalysisModel.add(new Gson().fromJson(str, JsonAnalysisModel.class));
                        }
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public List<JsonAnalysisModel> getListAnalysisModel() {
        return listAnalysisModel;
    }

    private void updateData() {
        Set<ChartDataModel> selectedData = getChartDataModels();

        data.setSelectedData(selectedData);
    }

    public DateTime getSelectedStart() {
        return selectedStart;
    }

    public DateTime getSelectedEnd() {
        return selectedEnd;
    }

    private Set<ChartDataModel> getChartDataModels() {
        Map<String, ChartDataModel> data = new HashMap<>();

        for (JsonAnalysisModel mdl : listAnalysisModel) {
            ChartDataModel newData = new ChartDataModel();
            try {
                Long id = Long.parseLong(mdl.getObject());
                Long id_dp = null;
                if (mdl.getDataProcessorObject() != null) id_dp = Long.parseLong(mdl.getDataProcessorObject());
                JEVisObject obj = ds.getObject(id);
                JEVisObject obj_dp = null;
                if (mdl.getDataProcessorObject() != null) obj_dp = ds.getObject(id_dp);
                JEVisUnit unit = new JEVisUnitImp(new Gson().fromJson(mdl.getUnit(), JsonUnit.class));
                DateTime start;
                start = DateTime.parse(mdl.getSelectedStart());
                DateTime end;
                end = DateTime.parse(mdl.getSelectedEnd());
                Boolean selected = Boolean.parseBoolean(mdl.getSelected());
                newData.setObject(obj);
                newData.setSelectedStart(start);
                newData.setSelectedEnd(end);
                newData.setColor(Color.valueOf(mdl.getColor()));
                newData.setTitle(mdl.getName());
                if (mdl.getDataProcessorObject() != null) newData.setDataProcessor(obj_dp);
                newData.getAttribute();
                newData.setAggregation(parseAggregation(mdl.getAggregation()));
                newData.setSelected(selected);
                newData.set_somethingChanged(true);
                newData.getSamples();
                newData.set_selectedCharts(stringToList(mdl.getSelectedCharts()));
                newData.setUnit(unit);
                data.put(obj.getID().toString(), newData);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
        Set<ChartDataModel> selectedData = new HashSet<>();
        for (Map.Entry<String, ChartDataModel> entrySet : data.entrySet()) {
            ChartDataModel value = entrySet.getValue();
            if (value.getSelected()) {
                selectedData.add(value);
            }
        }
        return selectedData;
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

    private String listToString(List<String> listString) {
        if (listString != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : listString) {
                sb.append(s);
                sb.append(", ");
            }
            return sb.toString();
        } else return "";
    }

    private List<String> stringToList(String s) {
        if (Objects.nonNull(s)) {
            List<String> tempList = new ArrayList<>(Arrays.asList(s.split(", ")));
            for (String str : tempList) if (str.contains(", ")) str.replace(", ", "");
            return tempList;
        } else return new ArrayList<>();
    }

    private enum DATE_TYPE {
        START, END
    }
}
