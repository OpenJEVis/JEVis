/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.application.Chart.AnalysisTimeFrame;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.ChartType;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.dialog.ChartSelectionDialog;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.json.JsonChartTimeFrame;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author broder
 */
public class ToolBarView {

    private static final Logger logger = LogManager.getLogger(ToolBarView.class);
    private final JEVisDataSource ds;
    private GraphDataModel model;
    private ComboBox<JEVisObject> listAnalysesComboBoxHidden;
    private ChartView view;
    private List<ChartView> listView;
    private Boolean _initialized = false;
    private LoadAnalysisDialog dialog;
    private JEVisObject currentAnalysisDirectory = null;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds, ChartView chartView, List<ChartView> listChartViews) {
        this.model = model;
        this.ds = ds;
        this.view = chartView;
        this.listView = listChartViews;
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        listAnalysesComboBoxHidden = new ComboBox(model.getObservableListAnalyses());
        listAnalysesComboBoxHidden.setPrefWidth(300);
        setCellFactoryForComboBox();

        listAnalysesComboBoxHidden.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == null) || (Objects.nonNull(newValue))) {
                DateTime now = DateTime.now();
                AtomicReference<DateTime> oldStart = new AtomicReference<>(now);
                AtomicReference<DateTime> oldEnd = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));

                AggregationPeriod oldAggregationPeriod = model.getAggregationPeriod();
                AnalysisTimeFrame oldTimeFrame = model.getAnalysisTimeFrame();

                model.setCurrentAnalysis(newValue);
                if (model.getAnalysisTimeFrame().getTimeFrame().equals(AnalysisTimeFrame.TimeFrame.custom)) {
                    model.getSelectedData().forEach(chartDataModel -> {
                        if (chartDataModel.getSelectedStart() != null && chartDataModel.getSelectedEnd() != null) {
                            if (chartDataModel.getSelectedStart().isBefore(oldStart.get()))
                                oldStart.set(chartDataModel.getSelectedStart());
                            if (chartDataModel.getSelectedEnd().isAfter(oldEnd.get()))
                                oldEnd.set(chartDataModel.getSelectedEnd());
                        }
                    });
                }

                model.setCharts(null);
                model.updateSelectedData();

                if (!model.getAnalysisTimeFrame().getTimeFrame().equals(oldTimeFrame.getTimeFrame())) {
                    model.getSelectedData().forEach(chartDataModel -> {
                        if (!oldStart.get().equals(now)) chartDataModel.setSelectedStart(oldStart.get());
                        if (!oldEnd.get().equals(new DateTime(2001, 1, 1, 0, 0, 0)))
                            chartDataModel.setSelectedEnd(oldEnd.get());
                    });
                }
                model.setAggregationPeriod(oldAggregationPeriod);
                model.setAnalysisTimeFrame(oldTimeFrame);
                model.updateSamples();

                model.setCharts(model.getCharts());
                model.setSelectedData(model.getSelectedData());
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

        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> {
            JEVisObject currentAnalysis = listAnalysesComboBoxHidden.getSelectionModel().getSelectedItem();
            select(null);
            try {
                ds.reloadAttributes();
            } catch (JEVisException e) {
                logger.error(e);
            }
            select(currentAnalysis);
        });

        exportCSV.setOnAction(action -> {
            GraphExport ge = new GraphExport(ds, model);
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

        ToggleButton autoResize = new ToggleButton("", JEConfig.getImage("if_full_screen_61002.png", iconSize, iconSize));
        Tooltip autoResizeTip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.autosize"));
        autoResize.setTooltip(autoResizeTip);
        autoResize.setSelected(true);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(autoResize);
        autoResize.styleProperty().bind(
                Bindings
                        .when(autoResize.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(autoResize.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));


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

        autoResize.setOnAction(event -> autoResizeInGraph());

        toolBar.getItems().addAll(labelComboBox, listAnalysesComboBoxHidden, sep1, loadNew, save, delete, sep2, select, exportCSV, sep3, disableIcons, autoResize, reload);
        _initialized = true;
        return toolBar;
    }

    private void setCellFactoryForComboBox() {
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            if (!model.getMultipleDirectories())
                                setText(obj.getName());
                            else {
                                try {
                                    int indexOfObj = model.getObservableListAnalyses().indexOf(obj);
                                    String prefix = model.getListBuildingsParentOrganisations().get(indexOfObj).getName()
                                            + " / "
                                            + model.getListAnalysesParentBuildings().get(indexOfObj).getName();
                                    setText(prefix + " / " + obj.getName());
                                } catch (Exception e) {
                                }
                            }
                        }

                    }
                };
            }
        };

        listAnalysesComboBoxHidden.setCellFactory(cellFactory);
        listAnalysesComboBoxHidden.setButtonCell(cellFactory.call(null));
    }

    private void loadNewDialog() {


        dialog = new LoadAnalysisDialog(ds, model, this);

        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {

                        GraphDataModel newModel = new GraphDataModel(ds);

                        AnalysisTimeFrame atf = new AnalysisTimeFrame();
                        atf.setTimeFrame(AnalysisTimeFrame.TimeFrame.custom);

                        newModel.setAnalysisTimeFrame(atf);

                        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, newModel);

                        if (selectionDialog.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

                            model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
                            model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());

                        }
                    } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                        model.setAnalysisTimeFrame(model.getAnalysisTimeFrame());
                        model.updateSamples();
                        model.setCharts(model.getCharts());
                        model.setSelectedData(model.getSelectedData());

                    }
                });
    }

    private void hideShowIconsInGraph() {
        model.setHideShowIcons(!model.getHideShowIcons());
    }

    private void autoResizeInGraph() {
        model.setAutoResize(!model.getAutoResize());
    }

    public ComboBox getListAnalysesComboBoxHidden() {
        return listAnalysesComboBoxHidden;
    }

    private void changeSettings(ActionEvent event) {
        ChartSelectionDialog dia = new ChartSelectionDialog(ds, model);

        if (dia.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

            model.setCharts(dia.getChartPlugin().getData().getCharts());
            model.setSelectedData(dia.getChartPlugin().getData().getSelectedData());
        }

        dia = null;

        System.gc();
    }

    public List<ChartView> getChartViews() {
        List<ChartView> charts = new ArrayList<>();

        model.getCharts().forEach(chart -> {
            ChartView view = new ChartView(model);

            ChartType type = chart.getChartType();

            view.drawAreaChart(chart.getId(), type);

            charts.add(view);
        });

        return charts;
    }

    private void saveCurrentAnalysis() {

        Dialog<ButtonType> newAnalysis = new Dialog<>();
        newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        TextField name = new TextField();
        ComboBox<JEVisObject> parentsDirectories = new ComboBox<>(model.getObservableListAnalysesDirectories());

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            if (!model.getMultipleDirectories())
                                setText(obj.getName());
                            else {
                                try {
                                    int indexOfObj = model.getObservableListAnalysesDirectories().indexOf(obj);
                                    String prefix = model.getListAnalysesDirectortiesBuildingsParentOrganisations().get(indexOfObj).getName()
                                            + " / "
                                            + model.getListAnalysesDirectoriesParentBuildings().get(indexOfObj).getName();
                                    setText(prefix + " / " + obj.getName());
                                } catch (Exception e) {
                                }
                            }
                        }

                    }
                };
            }
        };
        parentsDirectories.setCellFactory(cellFactory);
        parentsDirectories.setButtonCell(cellFactory.call(null));

        parentsDirectories.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                currentAnalysisDirectory = newValue;
            }
        });

        if (model.getCurrentAnalysis() != null) {
            try {
                parentsDirectories.getSelectionModel().select(model.getCurrentAnalysis().getParents().get(0));
            } catch (JEVisException e) {
                logger.error("Couldn't select current Analysis Directory: " + e);
            }
        } else {
            parentsDirectories.getSelectionModel().selectFirst();
        }

        if (model.getCurrentAnalysis() != null && model.getCurrentAnalysis().getName() != null && model.getCurrentAnalysis().getName() != "")
            name.setText(model.getCurrentAnalysis().getName());

        name.focusedProperty().addListener((ov, t, t1) -> Platform.runLater(() -> {
            if (name.isFocused() && !name.getText().isEmpty()) {
                name.selectAll();
            }
        }));

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.new.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        VBox vbox = new VBox();
        vbox.setSpacing(4);
        vbox.getChildren().addAll(directoryText, parentsDirectories, newText, name);

        newAnalysis.getDialogPane().setContent(vbox);
        newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);

        newAnalysis.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                        List<String> check = new ArrayList<>();
                        try {
                            currentAnalysisDirectory.getChildren().forEach(jeVisObject -> {
                                if (!check.contains(jeVisObject.getName()))
                                    check.add(jeVisObject.getName());

                            });
                        } catch (JEVisException e) {
                            logger.error("Error in current analysis directory: " + e);
                        }
                        if (!check.contains(name.getText())) {
                            try {
                                JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                                model.setCurrentAnalysis(currentAnalysisDirectory.buildObject(name.getText(), classAnalysis));
                                model.getCurrentAnalysis().commit();

                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            saveDataModel(model.getSelectedData(), model.getCharts());

                            listAnalysesComboBoxHidden.setItems(model.getObservableListAnalyses());
                            setCellFactoryForComboBox();
                            listAnalysesComboBoxHidden.getSelectionModel().select(model.getCurrentAnalysis());
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

                                    listAnalysesComboBoxHidden.setItems(model.getObservableListAnalyses());
                                    setCellFactoryForComboBox();
                                    listAnalysesComboBoxHidden.getSelectionModel().select(model.getCurrentAnalysis());
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
                    ds.deleteObject(model.getCurrentAnalysis().getID());
                } catch (JEVisException e) {
                    logger.error("Error: could not delete current analysis", e);
                }

                model.updateListAnalyses();
                listAnalysesComboBoxHidden.setItems(model.getObservableListAnalyses());
                setCellFactoryForComboBox();
                listAnalysesComboBoxHidden.getSelectionModel().selectFirst();
            }
        });

    }

    private void saveDataModel(Set<ChartDataModel> selectedData, List<ChartSettings> chartSettings) {
        try {
            JEVisAttribute dataModel = model.getCurrentAnalysis().getAttribute("Data Model");

            JsonChartDataModel jsonChartDataModel = new JsonChartDataModel();
            List<JsonAnalysisDataRow> jsonDataModels = new ArrayList<>();
            for (ChartDataModel mdl : selectedData) {
                if (!mdl.getSelectedcharts().isEmpty()) {
                    JsonAnalysisDataRow json = new JsonAnalysisDataRow();
                    json.setName(mdl.getObject().getName() + ":" + mdl.getObject().getID());
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
            jsonChartDataModel.setListDataRows(jsonDataModels);

            JsonChartTimeFrame jctf = new JsonChartTimeFrame();
            jctf.setTimeframe(model.getAnalysisTimeFrame().getTimeFrame().toString());
            jctf.setId(String.valueOf(model.getAnalysisTimeFrame().getId()));

            jsonChartDataModel.setAnalysisTimeFrame(jctf);

            JEVisAttribute charts = model.getCurrentAnalysis().getAttribute("Charts");
            List<JsonChartSettings> jsonChartSettings = new ArrayList<>();
            for (ChartSettings cset : chartSettings) {
                JsonChartSettings set = new JsonChartSettings();
                if (cset.getId() != null) set.setId(cset.getId().toString());
                set.setName(cset.getName());
                set.setChartType(cset.getChartType().toString());
                set.setHeight(cset.getHeight().toString());
                jsonChartSettings.add(set);
            }

            if (jsonChartDataModel.toString().length() < 8192 && jsonChartSettings.toString().length() < 8192) {
                DateTime now = DateTime.now();
                JEVisSample smp = dataModel.buildSample(now.toDateTimeISO(), jsonChartDataModel.toString());
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

    private String listToString(List<Integer> listString) {
        if (listString != null) {
            StringBuilder sb = new StringBuilder();
            if (listString.size() > 1) {
                for (Integer i : listString) {
                    sb.append(i.toString());
                    sb.append(", ");
                }
            } else if (listString.size() == 1) sb.append(listString.get(0));
            return sb.toString();
        } else return "";
    }

    public void selectFirst() {
        if (!_initialized) {
            model.updateListAnalyses();
            listAnalysesComboBoxHidden.setItems(model.getObservableListAnalyses());
            setCellFactoryForComboBox();
        }
        listAnalysesComboBoxHidden.getSelectionModel().selectFirst();
    }

    public void select(JEVisObject obj) {

        listAnalysesComboBoxHidden.getSelectionModel().select(obj);
    }
}
