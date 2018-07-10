/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.api.*;
import org.jevis.application.dialog.GraphSelectionDialog;
import org.jevis.application.jevistree.plugin.BarChartDataModel;
import org.jevis.application.jevistree.plugin.BarchartPlugin;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.plugin.graph.ToolBarController;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * @author broder
 */
public class ToolBarView {

    private final JEVisDataSource ds;
    private GraphDataModel model;
    private ToolBarController controller;
    private String nameCurrentAnalysis;
    private JEVisObject currentAnalysis;
    private List<JEVisObject> listAnalyses = new ArrayList<>();
    private JEVisObject analysesDir;
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private ComboBox listAnalysesComboBox;
    private List<JsonAnalysisModel> listAnalysisModel;
    private BorderPane border;
    private AreaChartView view;
    DateTime selectedStart;
    DateTime selectedEnd;
    private Boolean _initialized = false;
    private JFXDatePicker pickerDateStart = new JFXDatePicker();
    private JFXTimePicker pickerTimeStart = new JFXTimePicker();
    private JFXDatePicker pickerDateEnd = new JFXDatePicker();
    private JFXTimePicker pickerTimeEnd = new JFXTimePicker();
    LoadAnalysisDialog dialog;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds, AreaChartView chartView) {
        this.model = model;
        this.controller = new ToolBarController(this, model, ds);
        this.ds = ds;
        this.view = chartView;
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        //load basic stuff
        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        listAnalysesComboBox = new ComboBox();
        listAnalysesComboBox.setPrefWidth(300);
        updateListAnalyses();
        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || !newValue.equals(oldValue)) {
                this.nameCurrentAnalysis = newValue.toString();
                setJEVisObjectForCurrentAnalysis(newValue.toString());
                getListAnalysis();

                updateChart();
            }
        });

        Button newB = new Button("", JEConfig.getImage("list-add.png", iconSize, iconSize));

        Button save = new Button("", JEConfig.getImage("save.gif", iconSize, iconSize));

        Button loadNew = new Button("", JEConfig.getImage("1390343812_folder-open.png", iconSize, iconSize));

        save.setOnAction(action -> {
            saveCurrentAnalysis();
        });

        loadNew.setOnAction(event -> {
            dialog = new LoadAnalysisDialog(ds, model, this);
            dialog.getLv().getSelectionModel().select(nameCurrentAnalysis);
            dialog.showAndWait().ifPresent(response -> {
                if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                    GraphSelectionDialog selectionDialog = new GraphSelectionDialog(ds);

                    if (selectionDialog.show(JEConfig.getStage()) == GraphSelectionDialog.Response.OK) {

                        Set<BarChartDataModel> selectedData = new HashSet<>();
                        for (Map.Entry<String, BarChartDataModel> entrySet : selectionDialog.getSelectedData().entrySet()) {
                            BarChartDataModel value = entrySet.getValue();
                            if (value.getSelected()) {
                                selectedData.add(value);
                            }
                        }
                        model.setSelectedData(selectedData);
                    }
                } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                    //dialog.updateToolBarView();
                    select(dialog.getLv().getSelectionModel().getSelectedItem());
                }
            });
        });

        Button delete = new Button("", JEConfig.getImage("list-remove.png", iconSize, iconSize));

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        save.setDisable(false);
        newB.setDisable(false);
        delete.setDisable(false);

        Button select = new Button("", JEConfig.getImage("Data.png", iconSize, iconSize));

        Button date = new Button("", JEConfig.getImage("date.png", iconSize, iconSize));

        newB.setOnAction(event -> {
            controller = new ToolBarController(this, model, ds);
            controller.handle(event);
        });

        date.setOnAction(event -> dialogDate());

        select.setOnAction(event -> changeSettings(event));

        delete.setOnAction(event -> deleteCurrentAnalysis());

        toolBar.getItems().addAll(labelComboBox, listAnalysesComboBox, sep1, loadNew, save, delete, sep2, date, select);
        _initialized = true;
        return toolBar;
    }

    private void changeSettings(ActionEvent event) {
        GraphSelectionDialog dia = new GraphSelectionDialog(ds);
        Map<String, BarChartDataModel> map = new HashMap<>();

//        if (listAnalysisModel != null) {
//            for (BarChartDataModel mdl : getBarChartDataModels()) {
//                map.put(mdl.getObject().getName(), mdl);
//            }
//        } else {
        for (BarChartDataModel mdl : model.getSelectedData()) {
            map.put(mdl.getObject().getName(), mdl);
        }
//        }


        dia.setData(map);
        if (dia.show(JEConfig.getStage()) == GraphSelectionDialog.Response.OK) {

            Set<BarChartDataModel> selectedData = new HashSet<>();
            for (Map.Entry<String, BarChartDataModel> entrySet : dia.getSelectedData().entrySet()) {
                BarChartDataModel value = entrySet.getValue();
                if (value.getSelected()) {
                    selectedData.add(value);
                }
            }
            model.setSelectedData(selectedData);
            if (view == null) view = new AreaChartView(model);
            try {
                view.drawAreaChart();

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
    }


    private void saveCurrentAnalysis() {

        Dialog<ButtonType> newAnalysis = new Dialog<>();
        newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        TextField name = new TextField();
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                nameCurrentAnalysis = newValue;
            }
        });
        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.ok"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        VBox vbox = new VBox();
        vbox.setSpacing(4);
        vbox.getChildren().addAll(newText, name);

        newAnalysis.getDialogPane().setContent(vbox);
        newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);
        newAnalysis.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                        if (!observableListAnalyses.contains(nameCurrentAnalysis)) {
                            try {
                                for (JEVisObject obj : ds.getObjects(ds.getJEVisClass("Analyses Directory"), false)) {
                                    analysesDir = obj;
                                    JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                                    currentAnalysis = obj.buildObject(nameCurrentAnalysis, classAnalysis);
                                    currentAnalysis.commit();
                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            saveDataModel(model.getSelectedData());
                            updateListAnalyses();
                            listAnalysesComboBox.getSelectionModel().select(nameCurrentAnalysis);
                        } else {
                            Dialog<ButtonType> dialogOverwrite = new Dialog<>();
                            dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                            dialogOverwrite.getDialogPane().setContentText(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                            final ButtonType overwrite_ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), ButtonBar.ButtonData.OK_DONE);
                            final ButtonType overwrite_cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                            dialogOverwrite.getDialogPane().getButtonTypes().addAll(overwrite_ok, overwrite_cancel);

                            dialogOverwrite.showAndWait().ifPresent(overwrite_response -> {
                                if (overwrite_response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                                    saveDataModel(model.getSelectedData());
                                    updateListAnalyses();
                                    listAnalysesComboBox.getSelectionModel().select(nameCurrentAnalysis);
                                } else {

                                }
                            });

                        }

                    }
                });
    }

    private void deleteCurrentAnalysis() {
        Dialog<ButtonType> reallyDelete = new Dialog<>();
        reallyDelete.setTitle(I18n.getInstance().getString("plugin.graph.dialog.delete.title"));
        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.ok"), ButtonBar.ButtonData.YES);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.delete.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        reallyDelete.setContentText(I18n.getInstance().getString("plugin.graph.dialog.delete.message"));
        reallyDelete.getDialogPane().getButtonTypes().addAll(ok, cancel);
        reallyDelete.showAndWait().ifPresent(response -> {
            if (response.getButtonData().getTypeCode() == ButtonType.YES.getButtonData().getTypeCode()) {
                try {
                    ds.deleteObject(currentAnalysis.getID());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                updateListAnalyses();
                getListAnalysis();
                listAnalysesComboBox.getSelectionModel().selectFirst();
            }
        });

    }

    private void dialogDate() {
        Dialog<ButtonType> changeDate = new Dialog<>();
        changeDate.setTitle(I18n.getInstance().getString("plugin.graph.changedate.dialog.title"));
        changeDate.setWidth(300);

        Label startText = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate"));
        pickerDateStart.setPrefWidth(120d);
        pickerTimeStart.setIs24HourView(true);
        pickerTimeStart.setConverter(new LocalTimeStringConverter(FormatStyle.MEDIUM));

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
                        for (BarChartDataModel mdl : model.getSelectedData()) {
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
        pickerDateStart.setDayCellFactory(dayCellFactory);

        Label endText = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        pickerDateEnd.setPrefWidth(120d);
        pickerDateEnd.setDayCellFactory(dayCellFactory);
        pickerTimeEnd.setIs24HourView(true);
        pickerTimeEnd.setConverter(new

                LocalTimeStringConverter(FormatStyle.MEDIUM));

        ObservableList<String> presetDateEntries = FXCollections.observableArrayList();
        presetDateEntries.add(I18n.getInstance().

                getString("plugin.graph.changedate.buttonlastday"));
        presetDateEntries.add(I18n.getInstance().

                getString("plugin.graph.changedate.buttonlast30days"));
        presetDateEntries.add(I18n.getInstance().

                getString("plugin.graph.changedate.buttonlastweek"));
        presetDateEntries.add(I18n.getInstance().

                getString("plugin.graph.changedate.buttonlastmonth"));
        ComboBox<String> comboBoxPresetDates = new ComboBox(presetDateEntries);
//            ToggleButton lastDay = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlastday"));
//            ToggleButton last30Days = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days"));
//            ToggleButton lastWeek = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek"));
//            ToggleButton lastMonth = new ToggleButton(I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth"));

        if (!listAnalysisModel.isEmpty())

        {
            DateTime start = null;
            DateTime end = null;
            for (JsonAnalysisModel mdl : listAnalysisModel) {
                if (start == null || DateTime.parse(mdl.getSelectedStart()).isBefore(start))
                    start = DateTime.parse(mdl.getSelectedStart());
                if (end == null || DateTime.parse(mdl.getSelectedEnd()).isAfter(start))
                    end = DateTime.parse(mdl.getSelectedEnd());
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

        comboBoxPresetDates.getSelectionModel().

                selectedIndexProperty().

                addListener((observable, oldValue, newValue) ->

                {
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


        pickerDateStart.valueProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (!newValue.equals(oldValue)) {
                        selectedStart = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedStart.getHourOfDay(), selectedStart.getMinuteOfHour(), selectedStart.getMillisOfSecond());
                        comboBoxPresetDates.getSelectionModel().select(0);
                    }
                });

        pickerDateEnd.valueProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (!newValue.equals(oldValue)) {
                        selectedEnd = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), selectedEnd.getHourOfDay(), selectedEnd.getMinuteOfHour(), selectedEnd.getMillisOfSecond());
                        comboBoxPresetDates.getSelectionModel().select(0);
                    }
                });

        pickerTimeStart.valueProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (!newValue.equals(oldValue)) {
                        selectedStart = new DateTime(selectedStart.getYear(), selectedStart.getMonthOfYear(), selectedStart.getDayOfMonth(), newValue.getHour(), newValue.getMinute(), 0, 0);
                        comboBoxPresetDates.getSelectionModel().select(0);
                    }
                });

        pickerTimeEnd.valueProperty().

                addListener((observable, oldValue, newValue) ->

                {
                    if (!newValue.equals(oldValue)) {
                        selectedEnd = new DateTime(selectedEnd.getYear(), selectedEnd.getMonthOfYear(), selectedEnd.getDayOfMonth(), newValue.getHour(), newValue.getMinute(), 0, 0);
                        comboBoxPresetDates.getSelectionModel().select(0);
                    }
                });

        GridPane gp_date = new GridPane();
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        sep1.setOrientation(Orientation.HORIZONTAL);
        sep2.setOrientation(Orientation.HORIZONTAL);

        HBox startBox = new HBox();
        startBox.setSpacing(4);
        startBox.getChildren().

                addAll(pickerDateStart, pickerTimeStart);

        HBox endBox = new HBox();
        endBox.setSpacing(4);
        endBox.getChildren().

                addAll(pickerDateEnd, pickerTimeEnd);

        VBox vbox_picker = new VBox();
        vbox_picker.setSpacing(4);
        vbox_picker.getChildren().

                addAll(startText, startBox, sep1, endText, endBox);

        VBox vbox_buttons = new VBox();
        vbox_buttons.setSpacing(4);
        vbox_buttons.getChildren().

                addAll(comboBoxPresetDates);

        gp_date.add(vbox_picker, 0, 0);
        gp_date.add(vbox_buttons, 1, 0);

        changeDate.getDialogPane().

                setContent(gp_date);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.changedate.confirm"), ButtonBar.ButtonData.FINISH);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.changedate.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        changeDate.getDialogPane().

                getButtonTypes().

                addAll(ok, cancel);
        changeDate.showAndWait()
                .

                        ifPresent(response ->

                        {
                            if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {

                                for (JsonAnalysisModel mdl : listAnalysisModel) {
                                    mdl.setSelectedStart(selectedStart.toString());
                                    mdl.setSelectedEnd(selectedEnd.toString());
                                }
                                updateChart();
                            }
                        });
    }


    private void updateJsonDataModel(Set<BarChartDataModel> selectedData) {
        List<JsonAnalysisModel> jsonDataModels = new ArrayList<>();
        for (BarChartDataModel mdl : selectedData) {
            JsonAnalysisModel json = new JsonAnalysisModel();
            json.setName(mdl.getTitle());
            json.setSelected(String.valueOf(mdl.getSelected()));
            json.setColor(mdl.getColor().toString());
            json.setObject(mdl.getObject().getID().toString());
            json.setDataProcessorObject(mdl.getDataProcessor().getID().toString());
            json.setAggregation(mdl.getAggregation().toString());
            json.setSelectedStart(mdl.getSelectedStart().toString());
            json.setSelectedEnd(mdl.getSelectedEnd().toString());
            jsonDataModels.add(json);
        }
        this.listAnalysisModel = jsonDataModels;
    }

    private void saveDataModel(Set<BarChartDataModel> selectedData) {
        try {
            JEVisAttribute dataModel = currentAnalysis.getAttribute("Data Model");

            List<JsonAnalysisModel> jsonDataModels = new ArrayList<>();
            for (BarChartDataModel mdl : selectedData) {
                JsonAnalysisModel json = new JsonAnalysisModel();
                json.setName(mdl.getTitle());
                json.setSelected(String.valueOf(mdl.getSelected()));
                json.setColor(mdl.getColor().toString());
                json.setObject(mdl.getObject().getID().toString());
                json.setDataProcessorObject(mdl.getDataProcessor().getID().toString());
                json.setAggregation(mdl.getAggregation().toString());
                json.setSelectedStart(mdl.getSelectedStart().toString());
                json.setSelectedEnd(mdl.getSelectedEnd().toString());
                json.setUnit(mdl.getUnit().toJSON());
                jsonDataModels.add(json);
            }
            DateTime now = DateTime.now();
            JEVisSample smp = dataModel.buildSample(now.toDateTimeISO(), jsonDataModels.toString());
            smp.commit();

        } catch (JEVisException e) {
            e.printStackTrace();
        }
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

    public String getNameCurrentAnalysis() {
        return nameCurrentAnalysis;
    }

    public void updateListAnalyses() {
        try {
            listAnalyses = ds.getObjects(ds.getJEVisClass("Analysis"), false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        observableListAnalyses.clear();
        for (JEVisObject obj : listAnalyses) {
            observableListAnalyses.add(obj.getName());
        }
        listAnalysesComboBox.setItems(observableListAnalyses);
    }

    public void getListAnalysis() {
        try {
            if (currentAnalysis == null) {
                updateListAnalyses();
                setJEVisObjectForCurrentAnalysis(observableListAnalyses.get(0));
            }
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
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public void updateChart() {
        Set<BarChartDataModel> selectedData = getBarChartDataModels();

        model.setSelectedData(selectedData);

        if (view == null) view = new AreaChartView(model);
        try {
            view.drawAreaChart();

        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private Set<BarChartDataModel> getBarChartDataModels() {
        Map<String, BarChartDataModel> data = new HashMap<>();

        for (JsonAnalysisModel mdl : listAnalysisModel) {
            BarChartDataModel newData = new BarChartDataModel();
            try {
                Long id = Long.parseLong(mdl.getObject());
                Long id_dp = Long.parseLong(mdl.getDataProcessorObject());
                JEVisObject obj = ds.getObject(id);
                JEVisObject obj_dp = ds.getObject(id_dp);
                JEVisUnit unit = new JEVisUnitImp(new Gson().fromJson(mdl.getUnit(), JsonUnit.class));
                DateTime start;
                if (selectedStart != null)
                    start = selectedStart;
                else start = DateTime.parse(mdl.getSelectedStart());
                DateTime end;
                if (selectedEnd != null)
                    end = selectedEnd;
                else end = DateTime.parse(mdl.getSelectedEnd());
                Boolean selected = Boolean.parseBoolean(mdl.getSelected());
                newData.setObject(obj);
                newData.setSelectedStart(start);
                newData.setSelectedEnd(end);
                newData.setColor(Color.valueOf(mdl.getColor()));
                newData.setTitle(mdl.getName());
                newData.setDataProcessor(obj_dp);
                newData.getAttribute();
                newData.setAggregation(parseAggrigation(mdl.getAggregation()));
                newData.setSelected(selected);
                newData.set_somethingChanged(true);
                newData.getSamples();
                newData.setUnit(unit);
                data.put(obj.getName(), newData);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }
        Set<BarChartDataModel> selectedData = new HashSet<>();
        for (Map.Entry<String, BarChartDataModel> entrySet : data.entrySet()) {
            BarChartDataModel value = entrySet.getValue();
            if (value.getSelected()) {
                selectedData.add(value);
            }
        }
        return selectedData;
    }

    private BarchartPlugin.AGGREGATION parseAggrigation(String aggrigation) {
        switch (aggrigation) {
            case ("None"):
                return BarchartPlugin.AGGREGATION.None;
            case ("Daily"):
                return BarchartPlugin.AGGREGATION.Daily;
            case ("Weekly"):
                return BarchartPlugin.AGGREGATION.Weekly;
            case ("Monthly"):
                return BarchartPlugin.AGGREGATION.Monthly;
            case ("Yearly"):
                return BarchartPlugin.AGGREGATION.Yearly;
            default:
                return BarchartPlugin.AGGREGATION.None;
        }
    }

    public void selectFirst() {
        if (!_initialized) {
            updateListAnalyses();
            getListAnalysis();
        }
        listAnalysesComboBox.getSelectionModel().selectFirst();
    }

    public void select(String s) {
        listAnalysesComboBox.getSelectionModel().select(s);
    }

    public void setModel(GraphDataModel model) {
        this.model = model;
    }

    public void setNameCurrentAnalysis(String nameCurrentAnalysis) {
        this.nameCurrentAnalysis = nameCurrentAnalysis;
    }

    public void setCurrentAnalysis(JEVisObject currentAnalysis) {
        this.currentAnalysis = currentAnalysis;
    }

    public void setListAnalyses(List<JEVisObject> listAnalyses) {
        this.listAnalyses = listAnalyses;
    }

    public void setAnalysesDir(JEVisObject analysesDir) {
        this.analysesDir = analysesDir;
    }

    public void setListAnalysisModel(List<JsonAnalysisModel> listAnalysisModel) {
        this.listAnalysisModel = listAnalysisModel;
    }

    public void setSelectedStart(DateTime selectedStart) {
        this.selectedStart = selectedStart;
    }

    public void setSelectedEnd(DateTime selectedEnd) {
        this.selectedEnd = selectedEnd;
    }
}
