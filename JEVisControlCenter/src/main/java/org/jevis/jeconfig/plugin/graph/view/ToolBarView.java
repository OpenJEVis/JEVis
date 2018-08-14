/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.dialog.ChartSelectionDialog;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.application.jevistree.plugin.ChartDataModel;
import org.jevis.application.jevistree.plugin.ChartPlugin;
import org.jevis.application.jevistree.plugin.ChartSettings;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.DateHelper;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.plugin.graph.ToolBarController;
import org.jevis.jeconfig.plugin.graph.data.GraphDataModel;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
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
    private List<JsonChartSettings> listChartsSettings;
    private BorderPane border;
    private ChartView view;
    private List<ChartView> listView;
    private DateTime selectedStart;
    private DateTime selectedEnd;
    private Boolean _initialized = false;
    private LoadAnalysisDialog dialog;
    private ObservableList<String> chartsList = FXCollections.observableArrayList();
    private final Logger logger = LogManager.getLogger(ToolBarView.class);

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        //load basic stuff
        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        listAnalysesComboBox = new ComboBox();
        listAnalysesComboBox.setPrefWidth(300);
        updateListAnalyses();
        getListAnalysis();

        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == null) || (Objects.nonNull(newValue))) {
                this.nameCurrentAnalysis = newValue.toString();
                setJEVisObjectForCurrentAnalysis(newValue.toString());
                getListAnalysis();
                updateTimeFrame();
                updateChart();
            }
        });

        Button save = new Button("", JEConfig.getImage("save.gif", iconSize, iconSize));

        Tooltip saveTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.save"));
        save.setTooltip(saveTooltip);

        Button loadNew = new Button("", JEConfig.getImage("1390343812_folder-open.png", iconSize, iconSize));
        Tooltip loadNewTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.loadNew"));
        loadNew.setTooltip(loadNewTooltip);

        Button exportCSV = new Button("", JEConfig.getImage("export-csv.png", iconSize, iconSize));
        Tooltip exportCSVTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));
        exportCSV.setTooltip(exportCSVTooltip);

        exportCSV.setOnAction(action -> {
            GraphExport ge = new GraphExport(ds, model, nameCurrentAnalysis);
            try {
                ge.export();
            } catch (FileNotFoundException | UnsupportedEncodingException | JEVisException e) {
                logger.error("Error: could not export to file.", e);
            }
        });

        save.setOnAction(action -> {
            saveCurrentAnalysis();
        });

        loadNew.setOnAction(event -> {
            dialog = new LoadAnalysisDialog(ds, model, this);
            dialog.getLv().getSelectionModel().select(nameCurrentAnalysis);
            dialog.showAndWait().ifPresent(response -> {
                if (response.getButtonData().getTypeCode() == ButtonType.FINISH.getButtonData().getTypeCode()) {
                    ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, null, null);

                    if (selectionDialog.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

                        Set<ChartDataModel> selectedData = new HashSet<>();
                        for (Map.Entry<String, ChartDataModel> entrySet : selectionDialog.getBp().getSelectedData().entrySet()) {
                            ChartDataModel value = entrySet.getValue();
                            if (value.getSelected()) {
                                selectedData.add(value);
                            }
                        }

                        Set<ChartSettings> chartSettings = new HashSet<>();
                        for (Map.Entry<String, ChartSettings> entry : selectionDialog.getBp().getCharts().entrySet()) {
                            chartSettings.add(entry.getValue());
                        }

                        model.setCharts(chartSettings);
                        model.setSelectedData(selectedData);

                    }
                } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                    if (dialog.getInitialTimeFrame()) {
                        DateHelper dh = new DateHelper(DateHelper.TransformType.LAST30DAYS);
                        dialog.setSelectedStart(dh.getDateTimeStartDate());
                        dialog.setSelectedEnd(dh.getDateTimeEndDate());
                        dialog.updateTimeFrame();
                    }

                    model.setCharts(dialog.getData().getCharts());
                    model.setSelectedData(dialog.getData().getSelectedData());
                    select(dialog.getLv().getSelectionModel().getSelectedItem());
                }
            });
        });

        Button delete = new Button("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        save.setDisable(false);
        delete.setDisable(false);

        Button select = new Button("", JEConfig.getImage("Data.png", iconSize, iconSize));
        Tooltip selectTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select.setTooltip(selectTooltip);

        select.setOnAction(event -> changeSettings(event));

        delete.setOnAction(event -> deleteCurrentAnalysis());

        toolBar.getItems().addAll(labelComboBox, listAnalysesComboBox, sep1, loadNew, save, delete, sep2, select, exportCSV);
        _initialized = true;
        return toolBar;
    }

    public void updateTimeFrame() {
        if (selectedStart != null && selectedEnd != null) {
            if (model.getSelectedData() != null && !model.getSelectedData().isEmpty()) {
                for (ChartDataModel mdl : model.getSelectedData()) {
                    if (mdl.getSelected()) {
                        mdl.setSelectedStart(selectedStart);
                        mdl.setSelectedEnd(selectedEnd);
                    }
                }
            }

            if (listAnalysisModel != null && !listAnalysisModel.isEmpty()) {
                for (JsonAnalysisModel mdl : listAnalysisModel) {
                    if (Boolean.parseBoolean(mdl.getSelected())) {
                        mdl.setSelectedStart(selectedStart.toString());
                        mdl.setSelectedEnd(selectedEnd.toString());
                    }
                }
            }
        }
    }

    public ComboBox getListAnalysesComboBox() {
        return listAnalysesComboBox;
    }

    private void changeSettings(ActionEvent event) {
        Map<String, ChartDataModel> dataModelHashMap = new HashMap<>();
        Map<String, ChartSettings> chartSettingsHashMap = new HashMap<>();

        if (model.getSelectedData() != null) {
            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelected()) {
                    dataModelHashMap.put(mdl.getObject().getID().toString(), mdl);
                }
            }
        } else {
            model.setSelectedData(getChartDataModels());

            for (ChartDataModel mdl : model.getSelectedData()) {
                if (mdl.getSelected()) {
                    dataModelHashMap.put(mdl.getObject().getID().toString(), mdl);
                }
            }
        }

        if (model.getCharts() != null) {
            for (ChartSettings settings : model.getCharts()) {
                chartSettingsHashMap.put(settings.getName(), settings);
            }
        } else {
            model.setCharts(getCharts());

            for (ChartSettings settings : model.getCharts()) {
                chartSettingsHashMap.put(settings.getName(), settings);
            }
        }

        ChartSelectionDialog dia = new ChartSelectionDialog(ds, dataModelHashMap, chartSettingsHashMap);

        if (dia.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

            Set<ChartDataModel> selectedData = new HashSet<>();
            for (Map.Entry<String, ChartDataModel> entrySet : dia.getBp().getSelectedData().entrySet()) {
                ChartDataModel value = entrySet.getValue();
                if (value.getSelected()) {
                    selectedData.add(value);
                }
            }

            Set<ChartSettings> chartSettings = new HashSet<>();
            for (Map.Entry<String, ChartSettings> entry : dia.getBp().getCharts().entrySet()) {
                chartSettings.add(entry.getValue());
            }

            model.setCharts(chartSettings);
            model.setSelectedData(selectedData);
            //Platform.runLater(() -> drawChart());
        }
    }

    public ToolBarView(GraphDataModel model, JEVisDataSource ds, ChartView chartView, List<ChartView> listChartViews) {
        this.model = model;
        this.controller = new ToolBarController(this, model, ds);
        this.ds = ds;
        this.view = chartView;
        this.listView = listChartViews;
    }

    public ObservableList<String> getChartsList() {
        List<String> tempList = new ArrayList<>();
        for (ChartDataModel mdl : model.getSelectedData()) {
            if (mdl.getSelected()) {
                for (String s : mdl.get_selectedCharts()) {
                    if (!tempList.contains(s)) tempList.add(s);
                }
            }
        }

        AlphanumComparator ac = new AlphanumComparator();
        tempList.sort(ac);

        chartsList = FXCollections.observableArrayList(tempList);
        return chartsList;
    }

    public List<ChartView> getChartViews() {
        List<ChartView> charts = new ArrayList<>();

        getChartsList();
        for (String s : chartsList) {
            ChartView view = new ChartView(model);
            ChartSettings.ChartType type = ChartSettings.ChartType.AREA;
            if (model.getCharts() != null && !model.getCharts().isEmpty()) {
                for (ChartSettings set : model.getCharts()) {
                    if (set.getName().equals(s)) type = set.getChartType();
                }
            }
            view.drawAreaChart(s, type);
            charts.add(view);
        }

        return charts;
    }

    private void saveCurrentAnalysis() {

        Dialog<ButtonType> newAnalysis = new Dialog<>();
        newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        TextField name = new TextField();
        if (nameCurrentAnalysis != null && nameCurrentAnalysis != "") name.setText(nameCurrentAnalysis);

        name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                nameCurrentAnalysis = newValue;
            }
        });

        name.focusedProperty().addListener((ov, t, t1) -> Platform.runLater(() -> {
            if (name.isFocused() && !name.getText().isEmpty()) {
                name.selectAll();
            }
        }));

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
                            saveDataModel(model.getSelectedData(), model.getCharts());
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
                                    saveDataModel(model.getSelectedData(), model.getCharts());
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
                    logger.error("Error: could not delete current analysis", e);
                }

                updateListAnalyses();
                getListAnalysis();
                listAnalysesComboBox.getSelectionModel().selectFirst();
            }
        });

    }

    private void saveDataModel(Set<ChartDataModel> selectedData, Set<ChartSettings> chartSettings) {
        try {
            JEVisAttribute dataModel = currentAnalysis.getAttribute("Data Model");

            List<JsonAnalysisModel> jsonDataModels = new ArrayList<>();
            for (ChartDataModel mdl : selectedData) {
                if (mdl.getSelected()) {
                    JsonAnalysisModel json = new JsonAnalysisModel();
                    json.setName(mdl.getObject().getName() + ":" + mdl.getObject().getID());
                    json.setSelected(String.valueOf(mdl.getSelected()));
                    json.setColor(mdl.getColor().toString());
                    json.setObject(mdl.getObject().getID().toString());
                    if (mdl.getDataProcessor() != null)
                        json.setDataProcessorObject(mdl.getDataProcessor().getID().toString());
                    json.setAggregation(mdl.getAggregation().toString());
                    json.setSelectedStart(mdl.getSelectedStart().toString());
                    json.setSelectedEnd(mdl.getSelectedEnd().toString());
                    json.setUnit(mdl.getUnit().toJSON());
                    json.setSelectedCharts(listToString(mdl.get_selectedCharts()));
                    jsonDataModels.add(json);
                }
            }

            JEVisAttribute charts = currentAnalysis.getAttribute("Charts");
            List<JsonChartSettings> jsonChartSettings = new ArrayList<>();
            for (ChartSettings cset : chartSettings) {
                JsonChartSettings set = new JsonChartSettings();
                set.setName(cset.getName());
                set.setChartType(cset.getChartType().toString());
                jsonChartSettings.add(set);
            }

            if (jsonDataModels.toString().length() < 4096 && jsonChartSettings.toString().length() < 4096) {
                DateTime now = DateTime.now();
                JEVisSample smp = dataModel.buildSample(now.toDateTimeISO(), jsonDataModels.toString());
                JEVisSample smp2 = charts.buildSample(now.toDateTimeISO(), jsonChartSettings.toString());
                smp.commit();
                smp2.commit();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.graph.alert.toolong"));
                alert.showAndWait();
            }

        } catch (JEVisException e) {
            logger.error("Error: could not save data model and chart settings", e);
        }
    }

    private String listToString(List<String> listString) {
        if (listString != null) {
            StringBuilder sb = new StringBuilder();
            if (listString.size() > 1) {
                for (String s : listString) {
                    sb.append(s);
                    sb.append(", ");
                }
            } else if (listString.size() == 1) sb.append(listString.get(0));
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
        listAnalysesComboBox.setItems(observableListAnalyses);
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
                if (Objects.nonNull(currentAnalysis.getAttribute("Charts"))) {
                    if (currentAnalysis.getAttribute("Charts").hasSample()) {
                        String str = currentAnalysis.getAttribute("Charts").getLatestSample().getValueAsString();
                        try {
                            if (str.endsWith("]")) {
                                listChartsSettings = new Gson().fromJson(str, new TypeToken<List<JsonChartSettings>>() {
                                }.getType());

                            } else {
                                listChartsSettings = new ArrayList<>();
                                listChartsSettings.add(new Gson().fromJson(str, JsonChartSettings.class));
                            }
                        } catch (Exception e) {
                            logger.error("Error: could not read chart settings", e);
                        }
                    }
                }
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }
    }

    public void updateChart() {
        Set<ChartDataModel> selectedData = getChartDataModels();
        Set<ChartSettings> charts = getCharts();

        model.setCharts(charts);
        model.setSelectedData(selectedData);

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
                logger.error("Error: could not get chart data model", e);
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

    private Set<ChartSettings> getCharts() {
        Map<String, ChartSettings> chartSettingsHashMap = new HashMap<>();
        Set<ChartSettings> chartSettings = new HashSet<>();

        if (listChartsSettings != null && !listChartsSettings.isEmpty()) {
            for (JsonChartSettings settings : listChartsSettings) {
                ChartSettings newSettings = new ChartSettings("");
                newSettings.setName(settings.getName());
                newSettings.setChartType(parseChartType(settings.getChartType()));
                chartSettingsHashMap.put(newSettings.getName(), newSettings);
            }

            for (Map.Entry<String, ChartSettings> entrySet : chartSettingsHashMap.entrySet()) {
                ChartSettings value = entrySet.getValue();
                chartSettings.add(value);
            }
        }
        return chartSettings;
    }

    private ChartSettings.ChartType parseChartType(String chartType) {
        switch (chartType) {
            case ("AREA"):
                return ChartSettings.ChartType.AREA;
            case ("LINE"):
                return ChartSettings.ChartType.LINE;
            case ("BAR"):
                return ChartSettings.ChartType.BAR;
            case ("BUBBLE"):
                return ChartSettings.ChartType.BUBBLE;
            case ("SCATTER"):
                return ChartSettings.ChartType.SCATTER;
            case ("PIE"):
                return ChartSettings.ChartType.PIE;
            default:
                return ChartSettings.ChartType.AREA;
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
