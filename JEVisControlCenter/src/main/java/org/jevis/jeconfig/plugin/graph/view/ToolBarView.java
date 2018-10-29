/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
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
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.ChartType;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.dialog.ChartSelectionDialog;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeFactory;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.json.JsonAnalysisModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonUnit;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.*;

/**
 * @author broder
 */
public class ToolBarView {

    private final JEVisDataSource ds;
    private final Logger logger = LogManager.getLogger(ToolBarView.class);
    private GraphDataModel model;
    private String nameCurrentAnalysis;
    private JEVisObject currentAnalysis;
    private List<JEVisObject> listAnalyses = new ArrayList<>();
    private JEVisObject analysesDir;
    private ObservableList<String> observableListAnalyses = FXCollections.observableArrayList();
    private ComboBox listAnalysesComboBoxHidden;

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
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private JEVisTree selectionTree = null;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds, ChartView chartView, List<ChartView> listChartViews) {
        this.model = model;
        this.ds = ds;
        this.view = chartView;
        this.listView = listChartViews;
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        //load basic stuff
        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        listAnalysesComboBoxHidden = new ComboBox();
        listAnalysesComboBoxHidden.setPrefWidth(300);
        updateListAnalyses();
        getListAnalysis();

        listAnalysesComboBoxHidden.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == null) || (Objects.nonNull(newValue))) {
                this.nameCurrentAnalysis = newValue.toString();
                setJEVisObjectForCurrentAnalysis(newValue.toString());
                getListAnalysis();
                updateTimeFrame();
                updateChart();
            }
        });

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        Tooltip saveTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.save"));
        save.setTooltip(saveTooltip);

        ToggleButton loadNew = new ToggleButton("", JEConfig.getImage("1390343812_folder-open.png", iconSize, iconSize));
        Tooltip loadNewTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.loadNew"));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(loadNew);
        loadNew.setTooltip(loadNewTooltip);

        ToggleButton exportCSV = new ToggleButton("", JEConfig.getImage("export-csv.png", iconSize, iconSize));
        Tooltip exportCSVTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));
        exportCSV.setTooltip(exportCSVTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportCSV);

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

        loadNew.setOnAction(event -> loadNewDialog());

        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        Separator sep3 = new Separator();
        save.setDisable(false);
        delete.setDisable(false);

        ToggleButton select = new ToggleButton("", JEConfig.getImage("Data.png", iconSize, iconSize));
        Tooltip selectTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select.setTooltip(selectTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(select);

        ToggleButton disableIcons = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
        Tooltip disableIconsTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        disableIcons.setTooltip(disableIconsTooltip);
        disableIcons.setSelected(true);
        disableIcons.styleProperty().bind(
                Bindings
                        .when(disableIcons.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(disableIcons.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        select.setOnAction(event -> changeSettings(event));

        delete.setOnAction(event -> deleteCurrentAnalysis());

        disableIcons.setOnAction(event -> hideShowIconsInGraph());

        toolBar.getItems().addAll(labelComboBox, listAnalysesComboBoxHidden, sep1, loadNew, save, delete, sep2, select, exportCSV, sep3, disableIcons);
        _initialized = true;
        return toolBar;
    }

    private void loadNewDialog() {


        dialog = new LoadAnalysisDialog(ds, model, this, nameCurrentAnalysis);
        //dialog.getLv().getSelectionModel().select(nameCurrentAnalysis);
        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, new GraphDataModel(), null);

                if (selectionDialog.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

                    model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
                    model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());

                }
            } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                model.setCharts(dialog.getData().getCharts());
                model.setSelectedData(dialog.getData().getSelectedData());
                select(dialog.getLv().getSelectionModel().getSelectedItem());

            }
        });
    }

    private void hideShowIconsInGraph() {
        model.setHideShowIcons(!model.getHideShowIcons());
        updateChart();
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

    public ComboBox getListAnalysesComboBoxHidden() {
        return listAnalysesComboBoxHidden;
    }

    public JEVisTree getSelectionTree() {
        if (selectionTree == null) {
            selectionTree = JEVisTreeFactory.buildDefaultGraphTree(ds);
        }

        return selectionTree;
    }

    private void changeSettings(ActionEvent event) {
        ChartSelectionDialog dia = new ChartSelectionDialog(ds, model, getSelectionTree());

        if (dia.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

            model.setCharts(dia.getChartPlugin().getData().getCharts());
            model.setSelectedData(dia.getChartPlugin().getData().getSelectedData());
        }
    }

    public List<ChartView> getChartViews() {
        List<ChartView> charts = new ArrayList<>();
        chartsList = model.getChartsList();

        chartsList.forEach(s -> {
            ChartView view = new ChartView(model);
            ChartType type = ChartType.AREA;
            if (model.getCharts() != null && !model.getCharts().isEmpty()) {
                for (ChartSettings set : model.getCharts()) {
                    if (set.getName().equals(s)) type = set.getChartType();
                }
            }
            view.drawAreaChart(s, type);

            charts.add(view);
        });

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

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        VBox vbox = new VBox();
        vbox.setSpacing(4);
        vbox.getChildren().addAll(newText, name);

        newAnalysis.getDialogPane().setContent(vbox);
        newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);

        newAnalysis.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
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
                            listAnalysesComboBoxHidden.getSelectionModel().select(nameCurrentAnalysis);
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
                                    listAnalysesComboBoxHidden.getSelectionModel().select(nameCurrentAnalysis);
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
                listAnalysesComboBoxHidden.getSelectionModel().selectFirst();
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
                    json.setAggregation(mdl.getAggregationPeriod().toString());
                    json.setSelectedStart(mdl.getSelectedStart().toString());
                    json.setSelectedEnd(mdl.getSelectedEnd().toString());
                    json.setUnit(mdl.getUnit().toJSON());
                    json.setSelectedCharts(listToString(mdl.getSelectedcharts()));
                    jsonDataModels.add(json);
                }
            }

            JEVisAttribute charts = currentAnalysis.getAttribute("Charts");
            List<JsonChartSettings> jsonChartSettings = new ArrayList<>();
            for (ChartSettings cset : chartSettings) {
                JsonChartSettings set = new JsonChartSettings();
                set.setName(cset.getName());
                set.setChartType(cset.getChartType().toString());
                set.setHeight(cset.getHeight().toString());
                jsonChartSettings.add(set);
            }

            if (jsonDataModels.toString().length() < 8192 && jsonChartSettings.toString().length() < 8192) {
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

    public void setNameCurrentAnalysis(String nameCurrentAnalysis) {
        this.nameCurrentAnalysis = nameCurrentAnalysis;
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
        listAnalysesComboBoxHidden.setItems(observableListAnalyses);
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
                        ds.reloadAttributes();
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
                try {
                    JEVisObject site = currentAnalysis.getParents().get(0).getParents().get(0);
                    LocalTime start = null;
                    LocalTime end = null;
                    try {
                        JEVisAttribute attStart = site.getAttribute("Workday Beginning");
                        JEVisAttribute attEnd = site.getAttribute("Workday End");
                        if (attStart.hasSample()) {
                            String startStr = attStart.getLatestSample().getValueAsString();
                            DateTime dtStart = DateTime.parse(startStr);
                            start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), 0, 0);
                        }
                        if (attEnd.hasSample()) {
                            String endStr = attEnd.getLatestSample().getValueAsString();
                            DateTime dtEnd = DateTime.parse(endStr);
                            end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), 59, 999999999);
                        }
                    } catch (Exception e) {
                    }

                    if (start != null && end != null) {
                        workdayStart = start;
                        workdayEnd = end;
                    }
                } catch (Exception e) {

                }
            }
        } catch (JEVisException e) {
            logger.error("Error: could not get analysis model", e);
        }
    }

    public void updateChart() {
        DateTime currentStart = selectedStart;
        DateTime currentEnd = selectedEnd;
        Set<ChartDataModel> selectedData = getChartDataModels();
        Set<ChartSettings> charts = getCharts();

        for (ChartDataModel mdl : selectedData) {
            if (mdl.getSelected()) {
                mdl.setSelectedStart(currentStart);
                mdl.setSelectedEnd(currentEnd);
            }
        }

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
                newData.setAggregationPeriod(AggregationPeriod.parseAggregation(mdl.getAggregation()));
                newData.setSelected(selected);
                newData.setSomethingChanged(true);
                newData.getSamples();
                newData.setSelectedCharts(stringToList(mdl.getSelectedCharts()));
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
                newSettings.setChartType(ChartType.parseChartType(settings.getChartType()));
                if (settings.getHeight() != null)
                    newSettings.setHeight(Double.parseDouble(settings.getHeight()));
                chartSettingsHashMap.put(newSettings.getName(), newSettings);
            }

            for (Map.Entry<String, ChartSettings> entrySet : chartSettingsHashMap.entrySet()) {
                ChartSettings value = entrySet.getValue();
                chartSettings.add(value);
            }
        }
        return chartSettings;
    }

    public void selectFirst() {
        if (!_initialized) {
            updateListAnalyses();
            getListAnalysis();
        }
        listAnalysesComboBoxHidden.getSelectionModel().selectFirst();
    }

    public void select(String s) {
        listAnalysesComboBoxHidden.getSelectionModel().select(s);
    }

    public void setModel(GraphDataModel model) {
        this.model = model;
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

    public LocalTime getWorkdayStart() {
        return workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        return workdayEnd;
    }


}
