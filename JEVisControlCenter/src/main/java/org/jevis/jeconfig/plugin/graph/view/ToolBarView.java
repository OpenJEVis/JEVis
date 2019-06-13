/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.ValueAxis;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.json.JsonAnalysisDataRow;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.json.JsonChartTimeFrame;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.DateValueAxis;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.dialog.LoadAnalysisDialog;
import org.jevis.jeconfig.dialog.Response;
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
    private final GraphPluginView graphPluginView;
    private GraphDataModel model;
    private ComboBox<JEVisObject> listAnalysesComboBox;
    private Boolean _initialized = false;
    private JEVisObject currentAnalysisDirectory = null;
    private ToggleButton save;
    private ToggleButton loadNew;
    private ToggleButton exportCSV;
    private ToggleButton exportImage;
    private ToggleButton reload;
    private ToggleButton delete;
    private ToggleButton autoResize;
    private ToggleButton select;
    private ToggleButton disableIcons;
    private ToggleButton zoomOut;
    private PickerCombo pickerCombo;
    private ComboBox<TimeFrame> presetDateBox;
    private JFXDatePicker pickerDateStart;
    private JFXTimePicker pickerTimeStart;
    private JFXDatePicker pickerDateEnd;
    private JFXTimePicker pickerTimeEnd;
    private DateHelper dateHelper = new DateHelper();
    private ToolBar toolBar;


    private ToggleButton runUpdateButton;
    private ChangeListener<JEVisObject> analysisComboBoxChangeListener = (observable, oldValue, newValue) -> {
        if ((oldValue == null) || (Objects.nonNull(newValue))) {

            model.setCurrentAnalysis(newValue);
            model.resetToolbarSettings();
            model.setGlobalAnalysisTimeFrame(model.getGlobalAnalysisTimeFrame());
            Platform.runLater(this::updateLayout);
        }
    };
    private ToggleButton addSeriesRunningMean;
    private ImageView pauseIcon;
    private ImageView playIcon;
    private ToggleButton showRawData;
    private ToggleButton showSum;

    public ToolBarView(GraphDataModel model, JEVisDataSource ds, GraphPluginView graphPluginView) {
        this.model = model;
        this.ds = ds;
        this.graphPluginView = graphPluginView;

    }


    public ToolBar getToolbar(JEVisDataSource ds) {
        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");

            updateLayout();
        }

        return toolBar;
    }


    public void setupAnalysisComboBoxListener() {
        listAnalysesComboBox.valueProperty().addListener(analysisComboBoxChangeListener);
    }

    private void resetZoom() {
        graphPluginView.getCharts().forEach(chartView -> {
            MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();
            DateValueAxis dateValueAxis = (DateValueAxis) chart.getXAxis();
            dateValueAxis.setAutoRanging(true);
            ValueAxis valueAxis1 = (ValueAxis) chart.getY1Axis();
            valueAxis1.setAutoRanging(true);
            ValueAxis valueAxis2 = (ValueAxis) chart.getY2Axis();
            valueAxis2.setAutoRanging(true);
        });
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
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String prefix = "";
                            try {

                                JEVisObject secondParent = obj.getParents().get(0).getParents().get(0);
                                JEVisClass buildingClass = ds.getJEVisClass("Building");
                                JEVisClass organisationClass = ds.getJEVisClass("Organization");

                                if (secondParent.getJEVisClass().equals(buildingClass)) {

                                    try {
                                        JEVisObject organisationParent = secondParent.getParents().get(0).getParents().get(0);

                                        if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                            prefix += organisationParent.getName() + " / " + secondParent.getName() + " / ";
                                        }
                                    } catch (JEVisException e) {
                                        logger.error("Could not get Organization parent of " + secondParent.getName() + ":" + secondParent.getID());

                                        prefix += secondParent.getName() + " / ";
                                    }
                                } else if (secondParent.getJEVisClass().equals(organisationClass)) {

                                    prefix += secondParent.getName() + " / ";

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

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(ds, model);

        dialog.show();

        if (dialog.getResponse() == Response.NEW) {

            GraphDataModel newModel = new GraphDataModel(ds, graphPluginView);

            ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, newModel);

            if (selectionDialog.show() == Response.OK) {

                model.setCurrentAnalysis(null);
                model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
                model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());

            }
        } else if (dialog.getResponse() == Response.LOAD) {

        }

    }

    private void hideShowIconsInGraph() {
        model.setHideShowIcons(!model.getHideShowIcons());
    }

    private void autoResizeInGraph() {
        model.setAutoResize(!model.getAutoResize());
    }

    private void showRawDataInGraph() {
        model.setShowRawData(!model.getShowRawData());
    }

    private void showSumInGraph() {
        model.setShowSum(!model.getShowSum());
    }

    private ComboBox<JEVisObject> getListAnalysesComboBox() {
        return listAnalysesComboBox;
    }

    private void changeSettings() {
        ChartSelectionDialog dia = new ChartSelectionDialog(ds, model);

        if (dia.show() == Response.OK) {

            model.setCharts(dia.getChartPlugin().getData().getCharts());
            model.setSelectedData(dia.getChartPlugin().getData().getSelectedData());
        }
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

                                    JEVisObject firstParent = obj.getParents().get(0);

                                    JEVisClass buildingClass = ds.getJEVisClass("Building");
                                    JEVisClass organisationClass = ds.getJEVisClass("Organization");

                                    if (firstParent.getJEVisClass().equals(buildingClass)) {

                                        try {
                                            List<JEVisObject> parents = firstParent.getParents();
                                            if (!parents.isEmpty()) {
                                                List<JEVisObject> parentsParents = parents.get(0).getParents();
                                                if (!parentsParents.isEmpty()) {
                                                    JEVisObject organisationParent = parentsParents.get(0);

                                                    if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                                        prefix += organisationParent.getName() + " / " + firstParent.getName();
                                                    } else {
                                                        prefix += firstParent.getName();
                                                    }
                                                } else {
                                                    prefix += firstParent.getName();
                                                }
                                            } else {
                                                prefix += firstParent.getName();
                                            }
                                        } catch (JEVisException e) {
                                            logger.error("Could not get Organization parent of " + firstParent.getName() + ":" + firstParent.getID());

                                            prefix += firstParent.getName();
                                        }
                                    } else if (firstParent.getJEVisClass().equals(organisationClass)) {

                                        prefix += firstParent.getName();

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
                            pickerCombo.updateCellFactory();
                            model.updateListAnalyses();
                            model.isGlobalAnalysisTimeFrame(true);
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
                                        pickerCombo.updateCellFactory();
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
            if (response.getButtonData().getTypeCode().equals(ButtonType.YES.getButtonData().getTypeCode())) {
                try {
                    if (ds.getCurrentUser().canDelete(model.getCurrentAnalysis().getID())) {
                        ds.deleteObject(model.getCurrentAnalysis().getID());
                        model.updateListAnalyses();
                        listAnalysesComboBox.getSelectionModel().selectFirst();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("plugin.graph.dialog.delete.error"), cancel);
                        alert.showAndWait();
                    }
                } catch (JEVisException e) {
                    logger.error("Error: could not delete current analysis", e);
                }
            }
        });

    }

    private void saveDataModel(JEVisObject
                                       analysis, Set<ChartDataModel> selectedData, List<ChartSettings> chartSettings) {
        try {
            JEVisAttribute dataModel = analysis.getAttribute("Data Model");
            JEVisAttribute charts = analysis.getAttribute("Charts");
            JEVisAttribute noOfChartsPerScreenAttribute = analysis.getAttribute(GraphDataModel.NUMBER_OF_CHARTS_PER_SCREEN_ATTRIBUTE_NAME);
            Long noOfChartsPerScreen = model.getChartsPerScreen();

            JEVisAttribute horizontalPiesAttribute = analysis.getAttribute(GraphDataModel.NUMBER_OF_HORIZONTAL_PIES_ATTRIBUTE_NAME);
            Long horizontalPies = model.getHorizontalPies();
            JEVisAttribute horizontalTablesAttribute = analysis.getAttribute(GraphDataModel.NUMBER_OF_HORIZONTAL_TABLES_ATTRIBUTE_NAME);
            Long horizontalTables = model.getHorizontalTables();

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
                    json.setUnit(mdl.getUnit().toJSON());
                    json.setSelectedCharts(listToString(mdl.getSelectedcharts()));
                    json.setAxis(mdl.getAxis().toString());
                    json.setIsEnPI(mdl.getEnPI().toString());
                    if (mdl.getCalculationObject() != null)
                        json.setCalculation(mdl.getCalculationObject().getID().toString());
                    if (mdl.getBubbleType() != null) {
                        json.setBubbleType(mdl.getBubbleType().toString());
                    }
                    jsonDataModels.add(json);
                }
            }
            jsonChartDataModel.setListDataRows(jsonDataModels);

            List<JsonChartSettings> jsonChartSettings = new ArrayList<>();
            for (ChartSettings cset : chartSettings) {
                JsonChartSettings set = new JsonChartSettings();
                if (cset.getId() != null) set.setId(cset.getId().toString());
                set.setName(cset.getName());
                set.setChartType(cset.getChartType().toString());
                set.setHeight(cset.getHeight().toString());

                JsonChartTimeFrame jctf = new JsonChartTimeFrame();
                jctf.setTimeframe(cset.getAnalysisTimeFrame().getTimeFrame().toString());
                jctf.setId(String.valueOf(cset.getAnalysisTimeFrame().getId()));

                set.setAnalysisTimeFrame(jctf);

                jsonChartSettings.add(set);
            }

            if (jsonChartDataModel.toString().length() < 16635 && jsonChartSettings.toString().length() < 16635) {
                DateTime now = DateTime.now();
                String dataModelString = jsonChartDataModel.toString();
                JEVisSample smp = dataModel.buildSample(now, dataModelString);
                JEVisSample smp2 = charts.buildSample(now, jsonChartSettings.toString());
                smp.commit();
                smp2.commit();

                if (noOfChartsPerScreen != null && !noOfChartsPerScreen.equals(0L) && !noOfChartsPerScreen.equals(2L)) {
                    JEVisSample smp3 = noOfChartsPerScreenAttribute.buildSample(now, noOfChartsPerScreen);
                    smp3.commit();
                }

                if (horizontalPies != null && !horizontalPies.equals(0L)) {
                    JEVisSample smp4 = horizontalPiesAttribute.buildSample(now, horizontalPies);
                    smp4.commit();
                }

                if (horizontalTables != null && !horizontalTables.equals(0L)) {
                    JEVisSample smp4 = horizontalTablesAttribute.buildSample(now, horizontalTables);
                    smp4.commit();
                }
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
                    int index = listString.indexOf(i);
                    sb.append(i.toString());
                    if (index < listString.size() - 1) sb.append(", ");
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
        getListAnalysesComboBox().getSelectionModel().select(obj);
    }

    public void setDisableToolBarIcons(boolean bool) {
        listAnalysesComboBox.setDisable(bool);
        save.setDisable(bool);
        loadNew.setDisable(bool);
        exportCSV.setDisable(bool);
        exportImage.setDisable(bool);
        reload.setDisable(bool);
        runUpdateButton.setDisable(bool);
        delete.setDisable(bool);
        autoResize.setDisable(bool);
        select.setDisable(bool);
        showRawData.setDisable(bool);
        showSum.setDisable(bool);
        disableIcons.setDisable(bool);
        zoomOut.setDisable(bool);
        presetDateBox.setDisable(bool);
        pickerDateStart.setDisable(bool);
        pickerDateEnd.setDisable(bool);
        pickerTimeStart.setDisable(bool);
        pickerTimeEnd.setDisable(bool);
    }

    public PickerCombo getPickerCombo() {
        return pickerCombo;
    }

    public void updateLayout() {
        Platform.runLater(() -> {

            listAnalysesComboBox = new ComboBox<>(model.getObservableListAnalyses());
            listAnalysesComboBox.setPrefWidth(300);

            setCellFactoryForComboBox();

            if (model.getCurrentAnalysis() != null) {
                listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
            }

            if (!listAnalysesComboBox.getItems().isEmpty()) {

                dateHelper.setStartTime(model.getWorkdayStart());
                dateHelper.setEndTime(model.getWorkdayEnd());
            }

            toolBar.getItems().clear();
            pickerCombo = new PickerCombo(model, null);
            pickerCombo.updateCellFactory();
            presetDateBox = pickerCombo.getPresetDateBox();
            pickerDateStart = pickerCombo.getStartDatePicker();
            pickerTimeStart = pickerCombo.getStartTimePicker();
            pickerDateEnd = pickerCombo.getEndDatePicker();
            pickerTimeEnd = pickerCombo.getEndTimePicker();

            createToolbarIcons();

            Separator sep1 = new Separator();
            Separator sep2 = new Separator();
            Separator sep3 = new Separator();
            Separator sep4 = new Separator();

            if (!JEConfig.getExpert()) {
                toolBar.getItems().addAll(listAnalysesComboBox,
                        sep1, presetDateBox, pickerDateStart, pickerDateEnd,
                        sep2, reload, zoomOut,
                        sep3, loadNew, save, delete, select, exportCSV, exportImage,
                        sep4, showSum, disableIcons, autoResize, runUpdateButton);
            } else {
                toolBar.getItems().addAll(listAnalysesComboBox,
                        sep1, presetDateBox, pickerDateStart, pickerDateEnd,
                        sep2, reload, zoomOut,
                        sep3, loadNew, save, delete, select, exportCSV, exportImage,
                        sep4, showRawData, showSum, disableIcons, autoResize, runUpdateButton);
            }

            setupAnalysisComboBoxListener();
            pickerCombo.addListener();
            startToolbarIconListener();

        });
    }

    private void startToolbarIconListener() {
        reload.selectedProperty().addListener((observable, oldValue, newValue) -> graphPluginView.handleRequest(Constants.Plugin.Command.RELOAD));

        runUpdateButton.setOnAction(action -> {
            if (runUpdateButton.isSelected()) {
                model.setRunUpdate(true);
                runUpdateButton.setGraphic(pauseIcon);
                model.setTimer();
            } else {
                model.setRunUpdate(false);
                runUpdateButton.setGraphic(playIcon);
                model.stopTimer();
            }
        });

        exportCSV.setOnAction(action -> {
            GraphExportCSV ge = new GraphExportCSV(ds, model);
            try {
                ge.export();
            } catch (FileNotFoundException | UnsupportedEncodingException | JEVisException e) {
                logger.error("Error: could not export to file.", e);
            }
        });

        exportImage.setOnAction(action -> {
            GraphExportImage ge = new GraphExportImage(model);

            if (ge.getDestinationFile() != null) {

                ge.export(graphPluginView.getvBox());

                Platform.runLater(() -> {
                    JEConfig.getStage().setMaximized(false);
                    double height = JEConfig.getStage().getHeight();
                    double width = JEConfig.getStage().getWidth();
                    JEConfig.getStage().setWidth(0);
                    JEConfig.getStage().setHeight(0);
                    JEConfig.getStage().setHeight(height);
                    JEConfig.getStage().setWidth(width);
                });

            }

        });

        save.setOnAction(action -> {
            saveCurrentAnalysis();
        });

        loadNew.setOnAction(event -> {
            loadNewDialog();
        });

        zoomOut.setOnAction(event -> resetZoom());

        select.setOnAction(event -> {
            changeSettings();
        });

        delete.setOnAction(event -> deleteCurrentAnalysis());

        showRawData.setOnAction(event -> showRawDataInGraph());

        showSum.setOnAction(event -> showSumInGraph());

        disableIcons.setOnAction(event -> hideShowIconsInGraph());

        addSeriesRunningMean.setOnAction(event -> addSeriesRunningMean());

        autoResize.setOnAction(event -> autoResizeInGraph());
    }

    private void createToolbarIcons() {
        double iconSize = 20;

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

        exportImage = new ToggleButton("", JEConfig.getImage("export-image.png", iconSize, iconSize));
        Tooltip exportImageTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportImage"));
        exportImage.setTooltip(exportImageTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportImage);

        reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        pauseIcon = JEConfig.getImage("pause_32.png", iconSize, iconSize);
        playIcon = JEConfig.getImage("play_32.png", iconSize, iconSize);

        runUpdateButton = new ToggleButton("", playIcon);
        Tooltip runUpdateTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.runupdate"));
        runUpdateButton.setTooltip(runUpdateTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(runUpdateButton);
        runUpdateButton.setSelected(model.getRunUpdate());
        runUpdateButton.styleProperty().bind(
                Bindings
                        .when(runUpdateButton.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(runUpdateButton.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        autoResize = new ToggleButton("", JEConfig.getImage("if_full_screen_61002.png", iconSize, iconSize));
        Tooltip autoResizeTip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.autosize"));
        autoResize.setTooltip(autoResizeTip);
        autoResize.setSelected(model.getAutoResize());
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

        select = new ToggleButton("", JEConfig.getImage("Data.png", iconSize, iconSize));
        Tooltip selectTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select.setTooltip(selectTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(select);

        showRawData = new ToggleButton("", JEConfig.getImage("raw_199316.png", iconSize, iconSize));
        Tooltip showRawDataTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showrawdata"));
        showRawData.setTooltip(showRawDataTooltip);
        showRawData.setSelected(model.getShowRawData());
        showRawData.styleProperty().bind(
                Bindings
                        .when(showRawData.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(showRawData.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        showSum = new ToggleButton("", JEConfig.getImage("Sum_132399.png", iconSize, iconSize));
        Tooltip showSumTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showsum"));
        showSum.setTooltip(showSumTooltip);
        showSum.setSelected(model.getShowSum());
        showSum.styleProperty().bind(
                Bindings
                        .when(showSum.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(showSum.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        disableIcons = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
        Tooltip disableIconsTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        disableIcons.setTooltip(disableIconsTooltip);
        disableIcons.setSelected(model.getHideShowIcons());
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

        addSeriesRunningMean = new ToggleButton("", JEConfig.getImage("1415304498_alert.png", iconSize, iconSize));
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


        zoomOut = new ToggleButton("", JEConfig.getImage("ZoomOut.png", iconSize, iconSize));
        Tooltip zoomOutTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.zoomout"));
        zoomOut.setTooltip(zoomOutTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);

        if (!_initialized) {
            save.setDisable(false);
            delete.setDisable(false);

            setDisableToolBarIcons(true);

            _initialized = true;
        }
    }
}
