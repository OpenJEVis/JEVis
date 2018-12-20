/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.json.JsonChartTimeFrame;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
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
    private ComboBox<JEVisObject> listAnalysesComboBox;
    private Boolean _initialized = false;
    private JEVisObject currentAnalysisDirectory = null;
    private ToggleButton save;
    private ToggleButton loadNew;
    private ToggleButton exportCSV;
    private ToggleButton reload;
    private ToggleButton delete;
    private ToggleButton autoResize;
    private ToggleButton select;
    private ToggleButton disableIcons;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds) {
        this.model = model;
        this.ds = ds;
    }

    public ToolBar getToolbar(JEVisDataSource ds) {
        ToolBar toolBar = new ToolBar();
        toolBar.setId("ObjectPlugin.Toolbar");

        double iconSize = 20;
        Label labelComboBox = new Label(I18n.getInstance().getString("plugin.graph.toolbar.analyses"));

        listAnalysesComboBox = new ComboBox(model.getObservableListAnalyses());
        listAnalysesComboBox.setPrefWidth(300);
        setCellFactoryForComboBox();

        listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue == null) || (Objects.nonNull(newValue))) {
                DateTime now = DateTime.now();
                AtomicReference<DateTime> oldStart = new AtomicReference<>(now);
                AtomicReference<DateTime> oldEnd = new AtomicReference<>(new DateTime(2001, 1, 1, 0, 0, 0));

                AggregationPeriod oldAggregationPeriod = model.getAggregationPeriod();
                AnalysisTimeFrame oldTimeFrame = model.getAnalysisTimeFrame();
                ManipulationMode oldManipulationMode = ManipulationMode.NONE;
                for (ChartDataModel model : model.getSelectedData()) {
                    oldManipulationMode = model.getManipulationMode();
                    break;
                }

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

                model.setAggregationPeriod(oldAggregationPeriod);
                model.setAnalysisTimeFrame(oldTimeFrame);

                ManipulationMode finalOldManipulationMode = oldManipulationMode;
                model.getSelectedData().forEach(chartDataModel -> {
                    if (!oldStart.get().equals(now)) chartDataModel.setSelectedStart(oldStart.get());
                    if (!oldEnd.get().equals(new DateTime(2001, 1, 1, 0, 0, 0)))
                        chartDataModel.setSelectedEnd(oldEnd.get());
                    chartDataModel.setManipulationMode(finalOldManipulationMode);
                });

                model.updateSamples();

                model.setCharts(model.getCharts());
                model.setSelectedData(model.getSelectedData());
            }
        });

        save = new ToggleButton("", JEConfig.getImage("save.gif", iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        Tooltip saveTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.save"));
        save.setTooltip(saveTooltip);

        loadNew = new ToggleButton("", JEConfig.getImage("1390343812_folder-open.png", iconSize, iconSize));
        Tooltip loadNewTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.loadNew"));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(loadNew);
        loadNew.setTooltip(loadNewTooltip);

        exportCSV = new ToggleButton("", JEConfig.getImage("export-csv.png", iconSize, iconSize));
        Tooltip exportCSVTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));
        exportCSV.setTooltip(exportCSVTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportCSV);

        reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        reload.setOnAction(event -> {
            JEVisObject currentAnalysis = listAnalysesComboBox.getSelectionModel().getSelectedItem();
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

        delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        autoResize = new ToggleButton("", JEConfig.getImage("if_full_screen_61002.png", iconSize, iconSize));
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

        select = new ToggleButton("", JEConfig.getImage("Data.png", iconSize, iconSize));
        Tooltip selectTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select.setTooltip(selectTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(select);

        disableIcons = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
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

        ToggleButton addSeriesRunningMean = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
        Tooltip addSeriesRunningMeanTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        addSeriesRunningMean.setTooltip(addSeriesRunningMeanTooltip);
        addSeriesRunningMean.styleProperty().bind(
                Bindings
                        .when(addSeriesRunningMean.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(addSeriesRunningMean.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        select.setOnAction(event -> changeSettings(event));

        delete.setOnAction(event -> deleteCurrentAnalysis());

        disableIcons.setOnAction(event -> hideShowIconsInGraph());

        addSeriesRunningMean.setOnAction(event -> addSeriesRunningMean());

        autoResize.setOnAction(event -> autoResizeInGraph());

        /**
         * addSeriesRunningMean disabled for now
         */
        toolBar.getItems().addAll(labelComboBox, listAnalysesComboBox, sep1, loadNew, save, delete, sep2, select, exportCSV, sep3, disableIcons, autoResize, reload);
        setDisableToolBarIcons(true);
        _initialized = true;

        return toolBar;
    }

    private void addSeriesRunningMean() {
        model.setAddSeries(ManipulationMode.RUNNING_MEAN);
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
                            String prefix = "";
                            try {

                                JEVisObject buildingParent = obj.getParents().get(0).getParents().get(0);
                                JEVisClass buildingClass = ds.getJEVisClass("Building");
                                if (buildingParent.getJEVisClass().equals(buildingClass)) {

                                    try {
                                        JEVisObject organisationParent = buildingParent.getParents().get(0).getParents().get(0);
                                        JEVisClass organisationClass = ds.getJEVisClass("Organization");
                                        if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                            prefix += organisationParent.getName() + " / " + buildingParent.getName() + " / ";
                                        }
                                    } catch (JEVisException e) {
                                        logger.error("Could not get Organization parent of " + buildingParent.getName() + ":" + buildingParent.getID());

                                        prefix += buildingParent.getName() + " / ";
                                    }
                                }

                            } catch (Exception e) {
                            }
                            setText(prefix + obj.getName());
                        }

                    }
                };
            }
        };

        listAnalysesComboBox.setCellFactory(cellFactory);
        listAnalysesComboBox.setButtonCell(cellFactory.call(null));
    }

    private void loadNewDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(ds, model, this);

        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {

                        GraphDataModel newModel = new GraphDataModel(ds);

                        AnalysisTimeFrame atf = new AnalysisTimeFrame();
                        atf.setTimeFrame(AnalysisTimeFrame.TimeFrame.custom);

                        newModel.setAnalysisTimeFrame(atf);

                        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, newModel);

                        if (selectionDialog.show() == ChartSelectionDialog.Response.OK) {

                            model.setCurrentAnalysis(null);
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

    public ComboBox getListAnalysesComboBox() {
        return listAnalysesComboBox;
    }

    private void changeSettings(ActionEvent event) {
        ChartSelectionDialog dia = new ChartSelectionDialog(ds, model);

        if (dia.show() == ChartSelectionDialog.Response.OK) {

            model.setCharts(dia.getChartPlugin().getData().getCharts());
            model.setSelectedData(dia.getChartPlugin().getData().getSelectedData());
        }

        dia = null;

        System.gc();
    }

    private void saveCurrentAnalysis() {

        Dialog<ButtonType> newAnalysis = new Dialog<>();
        newAnalysis.setTitle(I18n.getInstance().getString("plugin.graph.dialog.new.title"));
        Label newText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.name"));
        Label directoryText = new Label(I18n.getInstance().getString("plugin.graph.dialog.new.directory"));
        TextField name = new TextField();

        JEVisClass analysesDirectory = null;
        List<JEVisObject> listAnalysesDirectories = null;
        try {
            analysesDirectory = ds.getJEVisClass("Analyses Directory");
            listAnalysesDirectories = ds.getObjects(analysesDirectory, false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        ComboBox<JEVisObject> parentsDirectories = new ComboBox<>(FXCollections.observableArrayList(listAnalysesDirectories));

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
                                    String prefix = "";

                                    JEVisObject buildingDirParent = obj.getParents().get(0);

                                    JEVisClass buildingClass = ds.getJEVisClass("Building");
                                    if (buildingDirParent.getJEVisClass().equals(buildingClass)) {

                                        try {
                                            JEVisObject organisationParent = buildingDirParent.getParents().get(0).getParents().get(0);
                                            JEVisClass organisationClass = ds.getJEVisClass("Organization");
                                            if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                                prefix += organisationParent.getName() + " / " + buildingDirParent.getName();
                                            }
                                        } catch (JEVisException e) {
                                            logger.error("Could not get Organization parent of " + buildingDirParent.getName() + ":" + buildingDirParent.getID());

                                            prefix += buildingDirParent.getName();
                                        }
                                    }


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

        GridPane gridLayout = new GridPane();
        gridLayout.setPadding(new Insets(10, 10, 10, 10));
        gridLayout.setVgap(10);

        gridLayout.add(directoryText, 0, 0);
        gridLayout.add(parentsDirectories, 0, 1, 2, 1);
        GridPane.setFillWidth(parentsDirectories, true);
        parentsDirectories.setMinWidth(200);
        gridLayout.add(newText, 0, 2);
        gridLayout.add(name, 0, 3, 2, 1);
        GridPane.setFillWidth(name, true);
        name.setMinWidth(200);

        newAnalysis.getDialogPane().setContent(gridLayout);
        newAnalysis.getDialogPane().getButtonTypes().addAll(ok, cancel);
        newAnalysis.getDialogPane().setPrefWidth(450d);
        newAnalysis.initOwner(JEConfig.getStage());

        newAnalysis.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                        List<String> check = new ArrayList<>();
                        AtomicReference<JEVisObject> currentAnalysis = new AtomicReference<>();
                        try {
                            currentAnalysisDirectory.getChildren().forEach(jeVisObject -> {
                                if (!check.contains(jeVisObject.getName())) {
                                    check.add(jeVisObject.getName());
                                }
                            });
                            currentAnalysisDirectory.getChildren().forEach(jeVisObject -> {
                                if (jeVisObject.getName().equals(name.getText())) currentAnalysis.set(jeVisObject);
                            });
                        } catch (JEVisException e) {
                            logger.error("Error in current analysis directory: " + e);
                        }
                        if (!check.contains(name.getText())) {
                            JEVisObject newAnalysisObject = null;
                            try {
                                JEVisClass classAnalysis = ds.getJEVisClass("Analysis");
                                newAnalysisObject = currentAnalysisDirectory.buildObject(name.getText(), classAnalysis);
                                newAnalysisObject.commit();

                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            saveDataModel(newAnalysisObject, model.getSelectedData(), model.getCharts());

                            model.setCurrentAnalysis(newAnalysisObject);
                            model.updateListAnalyses();
                            listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
                        } else {

                            Dialog<ButtonType> dialogOverwrite = new Dialog<>();
                            dialogOverwrite.setTitle(I18n.getInstance().getString("plugin.graph.dialog.overwrite.title"));
                            dialogOverwrite.getDialogPane().setContentText(I18n.getInstance().getString("plugin.graph.dialog.overwrite.message"));
                            final ButtonType overwrite_ok = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.ok"), ButtonBar.ButtonData.OK_DONE);
                            final ButtonType overwrite_cancel = new ButtonType(I18n.getInstance().getString("plugin.graph.dialog.overwrite.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

                            dialogOverwrite.getDialogPane().getButtonTypes().addAll(overwrite_ok, overwrite_cancel);

                            dialogOverwrite.showAndWait().ifPresent(overwrite_response -> {
                                if (overwrite_response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {
                                    if (currentAnalysis.get() != null) {
                                        saveDataModel(currentAnalysis.get(), model.getSelectedData(), model.getCharts());

                                        model.updateListAnalyses();
                                        model.setCurrentAnalysis(currentAnalysis.get());
                                        listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
                                    }
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
                listAnalysesComboBox.getSelectionModel().selectFirst();
            }
        });

    }

    private void saveDataModel(JEVisObject analysis, Set<ChartDataModel> selectedData, List<ChartSettings> chartSettings) {
        try {
            JEVisAttribute dataModel = analysis.getAttribute("Data Model");

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

            JEVisAttribute charts = analysis.getAttribute("Charts");
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
                String dataModelString = jsonChartDataModel.toString();
                JEVisSample smp = dataModel.buildSample(now, dataModelString);
                JEVisSample smp2 = charts.buildSample(now, jsonChartSettings.toString());
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
        }
        listAnalysesComboBox.getSelectionModel().selectFirst();
    }

    public void select(JEVisObject obj) {

        listAnalysesComboBox.getSelectionModel().select(obj);
    }

    public void setDisableToolBarIcons(boolean bool) {
        listAnalysesComboBox.setDisable(bool);
        save.setDisable(bool);
        loadNew.setDisable(bool);
        exportCSV.setDisable(bool);
        reload.setDisable(bool);
        delete.setDisable(bool);
        autoResize.setDisable(bool);
        select.setDisable(bool);
        disableIcons.setDisable(bool);
    }
}
