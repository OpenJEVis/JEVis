/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.plugin.charts;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import jfxtras.scene.control.LocalTimePicker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.Constants;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.application.Chart.ChartElements.MultiChartZoomer;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox;
import org.jevis.jecc.application.Chart.ChartPluginElements.NewSelectionDialog;
import org.jevis.jecc.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jecc.application.Chart.ChartPluginElements.PresetDateBox;
import org.jevis.jecc.application.Chart.ChartType;
import org.jevis.jecc.application.Chart.Charts.Chart;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.application.Chart.data.DataModel;
import org.jevis.jecc.application.control.RegressionBox;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.dialog.LoadAnalysisDialog;
import org.jevis.jecc.dialog.Response;
import org.jevis.jecc.dialog.SaveAnalysisDialog;
import org.jevis.jecc.tool.NumberSpinner;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author broder
 */
public class ToolBarView {

    private static final Logger logger = LogManager.getLogger(ToolBarView.class);
    private final JEVisDataSource ds;
    private final DataModel dataModel;
    private final AnalysesComboBox analysesComboBox;
    private final ToolBarSettings toolBarSettings = new ToolBarSettings();
    private final SimpleBooleanProperty disabledIcons = new SimpleBooleanProperty(true);
    private final PickerCombo pickerCombo;
    private final PresetDateBox presetDateBox;
    private final DatePicker pickerDateStart;
    private final LocalTimePicker pickerTimeStart;
    private final DatePicker pickerDateEnd;
    private final LocalTimePicker pickerTimeEnd;
    private final DateHelper dateHelper = new DateHelper();
    private final ToolBarFunctions toolBarFunctions;
    private ToggleButton save;
    private ToggleButton loadNew;
    private MenuItem exportCSV;
    private MenuButton export;
    private ToggleButton exportImage;
    private MenuItem exportPDF;
    private ToggleButton printButton;
    private ToggleButton reload;
    private ToggleButton delete;
    private ToggleButton autoResize;
    private ToggleButton select2;
    private ToggleButton disableIcons;
    private ToggleButton zoomOut;
    private ToggleButton infoButton;
    private ToggleButton helpButton;
    private ToolBar toolBar;
    private Boolean changed = false;
    private ToggleButton runUpdateButton;
    private ToggleButton customWorkDay;
    private Region pauseIcon;
    private Region playIcon;
    private ToggleButton showRawData;

    private MenuButton mathOperation;
    private MenuItem showSum;
    private MenuItem showL1L2;
    private MenuItem calcRegression;
    private MenuItem polyDegree;
    private MenuItem regressionType;
    private MenuItem calcFullLoadHours;
    private MenuItem calcSumAboveBelow;
    private MenuItem calcBaseLoad;

    private MenuItem calcValues;
    private ChartPlugin chartPlugin = null;
    public ToolBarView(DataModel dataModel, JEVisDataSource ds, ChartPlugin chartPlugin) {
        this.dataModel = dataModel;
        this.ds = ds;
        this.chartPlugin = chartPlugin;
        this.toolBarFunctions = new ToolBarFunctions(ds, chartPlugin.getDataSettings(), toolBarSettings, chartPlugin);

        analysesComboBox = new AnalysesComboBox(ds, dataModel);
        analysesComboBox.setPrefWidth(300);

        pickerCombo = new PickerCombo(ds, chartPlugin, true);
        presetDateBox = pickerCombo.getPresetDateBox();
        pickerDateStart = pickerCombo.getStartDatePicker();
        pickerTimeStart = pickerCombo.getStartTimePicker();
        pickerDateEnd = pickerCombo.getEndDatePicker();
        pickerTimeEnd = pickerCombo.getEndTimePicker();

        createToolbarIcons();

        Platform.runLater(() -> setDisableToolBarIcons(true));
    }

    private JEVisDataSource getDs() {
        return ds;
    }

    private void loadNewDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(chartPlugin, ds, analysesComboBox.getItems());

        dialog.setOnCloseRequest(event -> {
            JEVisHelp.getInstance().deactivatePluginModule();
            if (dialog.getResponse() == Response.NEW) {

                getChartPluginView().handleRequest(Constants.Plugin.Command.NEW);
            } else if (dialog.getResponse() == Response.LOAD) {

            }

            if (chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
                Platform.runLater(() -> setDisableToolBarIcons(false));
            } else {
                Platform.runLater(() -> setDisableToolBarIcons(true));
            }
        });

        Platform.runLater(() -> setDisableToolBarIcons(true));
        dialog.show();
        Platform.runLater(() -> dialog.getFilterInput().requestFocus());
    }

    private void changeAnalysis(JEVisObject newValue) {
        chartPlugin.setTemporary(false);
        chartPlugin.getDataSettings().setCurrentAnalysis(newValue);
        resetToolbarSettings();
        Platform.runLater(this::updateLayout);
        changed = false;
    }

    private void changeSettings2() {
        NewSelectionDialog dia = new NewSelectionDialog(ds, dataModel);

        dia.setOnCloseRequest(event -> {
            if (dia.getResponse() == Response.OK) {

                changed = true;
                chartPlugin.update();
            }
            JEVisHelp.getInstance().deactivatePluginModule();
        });

        dia.show();
    }

    public void updateLayout() {

        Platform.runLater(() -> {

            removeAnalysisComboBoxListener();

            if (!analysesComboBox.getItems().isEmpty()) {
                dateHelper.setWorkDays(chartPlugin.getWorkDays());
            }

            toolBar.getItems().clear();
            mathOperation.getItems().clear();

            pickerCombo.initialize(customWorkDay.isSelected());
            pickerCombo.updateCellFactory();

            Separator sep1 = new Separator();
            Separator sep2 = new Separator();
            Separator sep3 = new Separator();
            Separator sep4 = new Separator();

            boolean isRegressionPossible = false;

            for (ChartModel chartModel : dataModel.getChartModels()) {
                if (chartModel.getChartType() != ChartType.TABLE && chartModel.getChartType() != ChartType.HEAT_MAP
                        && chartModel.getChartType() != ChartType.BAR && chartModel.getChartType() != ChartType.PIE) {
                    isRegressionPossible = true;
                    break;
                }
            }

            toolBar.getItems().addAll(analysesComboBox,
                    sep1, presetDateBox, pickerDateStart, pickerDateEnd, customWorkDay,
                    sep2, reload, zoomOut,
                    sep3, loadNew, save, delete, select2, export, exportImage, printButton,
                    sep4);

            if (isRegressionPossible) {
                Separator regressionSeparator = new Separator(Orientation.HORIZONTAL);
                regressionSeparator.setHalignment(HPos.RIGHT);
                mathOperation.getItems().addAll(regressionType, polyDegree, calcRegression, new MenuItem("", regressionSeparator));
            }

            //Math buttons now always on
            mathOperation.getItems().addAll(calcFullLoadHours, calcSumAboveBelow, calcBaseLoad, calcValues);

            if (!ControlCenter.getExpert()) {
                mathOperation.getItems().addAll(showL1L2, showSum);
                toolBar.getItems().addAll(mathOperation, disableIcons, autoResize, runUpdateButton);
            } else {

                mathOperation.getItems().addAll(showL1L2, showSum);
                toolBar.getItems().addAll(showRawData, mathOperation, disableIcons, autoResize, runUpdateButton);
            }

//            toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), testButton, helpButton, infoButton);
            toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

            addAnalysisComboBoxListener();
            setDisableToolBarIcons(disabledIcons.get());
        });
    }

    public ToolBar getToolbar() {
        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");

            updateLayout();
        }

        return toolBar;
    }

    public void addAnalysisComboBoxListener() {
        analysesComboBox.getSelectionModel().selectedItemProperty().addListener(analysisComboBoxChangeListener);
    }

    public void removeAnalysisComboBoxListener() {
        analysesComboBox.getSelectionModel().selectedItemProperty().removeListener(analysisComboBoxChangeListener);
    }

    private void createToolbarIcons() {
        double iconSize = 20;

        save = new ToggleButton("", ControlCenter.getSVGImage(Icon.SAVE, iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        Tooltip saveTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.save"));
        save.setTooltip(saveTooltip);

        loadNew = new ToggleButton("", ControlCenter.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
        Tooltip loadNewTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.loadNew"));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(loadNew);
        loadNew.setTooltip(loadNewTooltip);

        export = new MenuButton("", ControlCenter.getSVGImage(Icon.EXPORT, iconSize, iconSize));
        Tooltip exportCSVTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.export"));
        export.setTooltip(exportCSVTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(export);

        exportCSV = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));

        exportImage = new ToggleButton("", ControlCenter.getSVGImage(Icon.SCREENSHOT, iconSize, iconSize));
        Tooltip exportImageTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportImage"));
        exportImage.setTooltip(exportImageTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportImage);

        exportPDF = new MenuItem(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.exportPDF"));

        export.getItems().addAll(exportCSV, exportPDF);

        printButton = new ToggleButton("", ControlCenter.getSVGImage(Icon.PRINT, iconSize, iconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.print"));
        printButton.setTooltip(printTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        reload = new ToggleButton("", ControlCenter.getSVGImage(Icon.REFRESH, iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        pauseIcon = ControlCenter.getSVGImage(Icon.PAUSE, iconSize, iconSize);
        playIcon = ControlCenter.getSVGImage(Icon.PLAY, iconSize, iconSize);

        runUpdateButton = new ToggleButton("", playIcon);
        Tooltip runUpdateTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.runupdate"));
        runUpdateButton.setTooltip(runUpdateTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(runUpdateButton);
        runUpdateButton.setSelected(toolBarSettings.isRunUpdate());
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

        delete = new ToggleButton("", ControlCenter.getSVGImage(Icon.DELETE, iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        autoResize = new ToggleButton("", ControlCenter.getSVGImage(Icon.MAXIMIZE, iconSize, iconSize));
        Tooltip autoResizeTip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.autosize"));
        autoResize.setTooltip(autoResizeTip);
        autoResize.selectedProperty().bindBidirectional(toolBarSettings.autoResizeProperty());
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

        select2 = new ToggleButton("", ControlCenter.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
        Tooltip selectTooltip2 = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select2.setTooltip(selectTooltip2);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(select2);

        showRawData = new ToggleButton("", ControlCenter.getSVGImage(Icon.RAW_ON, iconSize, iconSize));
        Tooltip showRawDataTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showrawdata"));
        showRawData.setTooltip(showRawDataTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(showRawData);
        showRawData.selectedProperty().bindBidirectional(toolBarSettings.showRawDataProperty());
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


        mathOperation = new MenuButton("", ControlCenter.getSVGImage(Icon.SUM, iconSize, iconSize));
        mathOperation.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.mathoperation")));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(mathOperation);
        showSum = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showsum"));

        showL1L2 = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showl1l2"));

        NumberSpinner numberSpinner = new NumberSpinner(new BigDecimal(2), new BigDecimal(1));
        polyDegree = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.regression.degree"), numberSpinner);
        RegressionBox regressionBox = new RegressionBox();
        regressionType = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.regression.type"), regressionBox);
        calcRegression = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcregression"));

        numberSpinner.setMin(new BigDecimal(1));
        numberSpinner.setMax(new BigDecimal(12));

        numberSpinner.numberProperty().addListener((observableValue, bigDecimal, t1) -> toolBarSettings.setPolyRegressionDegree(t1.toBigInteger().intValue()));
        regressionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, regressionType, t1) -> toolBarSettings.setRegressionType(t1));

        calcFullLoadHours = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcfullloadhours"));

        calcSumAboveBelow = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcsumabovebelow") + "\n" + I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calchoursabovebelow"));

        calcBaseLoad = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcbaseloadhours"));

        calcValues = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcvalues"));

        customWorkDay = new ToggleButton("", ControlCenter.getSVGImage(Icon.CALENDAR, iconSize, iconSize));
        Tooltip customWorkDayTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.customworkday"));
        customWorkDay.setTooltip(customWorkDayTooltip);
        customWorkDay.selectedProperty().bindBidirectional(toolBarSettings.customWorkdayProperty());
        customWorkDay.styleProperty().bind(
                Bindings
                        .when(customWorkDay.hoverProperty())
                        .then(
                                new SimpleStringProperty("-fx-background-insets: 1 1 1;"))
                        .otherwise(Bindings
                                .when(customWorkDay.selectedProperty())
                                .then("-fx-background-insets: 1 1 1;")
                                .otherwise(
                                        new SimpleStringProperty("-fx-background-color: transparent;-fx-background-insets: 0 0 0;"))));

        disableIcons = new ToggleButton("", ControlCenter.getSVGImage(Icon.Warning, iconSize, iconSize));
        Tooltip disableIconsTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        disableIcons.setTooltip(disableIconsTooltip);
        disableIcons.selectedProperty().bindBidirectional(toolBarSettings.showIconsProperty());
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

        zoomOut = new ToggleButton("", ControlCenter.getSVGImage(Icon.ZOOM_OUT, iconSize, iconSize));
        Tooltip zoomOutTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.zoomout"));
        zoomOut.setTooltip(zoomOutTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);

        helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
        infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);

        List<Node> nodes = Arrays.asList(analysesComboBox,
                presetDateBox, pickerDateStart, pickerDateEnd, customWorkDay,
                reload, zoomOut,
                loadNew, save, delete, select2, export, mathOperation, showRawData, exportImage, printButton,
                disableIcons, autoResize, runUpdateButton);

        pickerCombo.addListener();
        startToolbarIconListener();

        JEVisHelp.getInstance().addHelpItems(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, nodes);
    }

    private void resetZoom() {
        for (Map.Entry<Integer, Chart> entry : chartPlugin.getAllCharts().entrySet()) {
            Integer integer = entry.getKey();
            Chart chart = entry.getValue();
            if (chart.getChart() != null) {
                for (de.gsi.chart.plugins.ChartPlugin chartPlugin : chart.getChart().getPlugins()) {
                    if (chartPlugin instanceof MultiChartZoomer multiChartZoomer) {
                        multiChartZoomer.setFollowUpZoom(false);
                        Platform.runLater(multiChartZoomer::zoomOrigin);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void startToolbarIconListener() {
        reload.selectedProperty().addListener((observable, oldValue, newValue) -> chartPlugin.handleRequest(Constants.Plugin.Command.RELOAD));

        runUpdateButton.setOnAction(action -> {
            if (runUpdateButton.isSelected()) {
                toolBarSettings.setRunUpdate(true);
                runUpdateButton.setGraphic(pauseIcon);
                chartPlugin.setTimer();
            } else {
                toolBarSettings.setRunUpdate(false);
                runUpdateButton.setGraphic(playIcon);
                chartPlugin.stopTimer();
            }
        });

        exportCSV.setOnAction(action -> {
            if (chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
                ChartExportCSV ge = null;
                if (chartPlugin.isZoomed()) {
                    ge = new ChartExportCSV(
                            ds,
                            chartPlugin.getDataRowMap(),
                            chartPlugin.getDataSettings().getCurrentAnalysis().getName(),
                            new DateTime(chartPlugin.getxAxisLowerBound().longValue() * 1000),
                            new DateTime(chartPlugin.getxAxisUpperBound().longValue() * 1000));
                } else {
                    ge = new ChartExportCSV(
                            ds,
                            chartPlugin.getDataRowMap(),
                            chartPlugin.getDataSettings().getCurrentAnalysis().getName(),
                            chartPlugin.getDataSettings().getAnalysisTimeFrame().getStart(),
                            chartPlugin.getDataSettings().getAnalysisTimeFrame().getEnd());
                }

                try {
                    ge.export();
                } catch (FileNotFoundException | UnsupportedEncodingException | JEVisException e) {
                    logger.error("Error: could not export to file.", e);
                }
            }
        });

        exportImage.setOnAction(action -> {
            if (chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
                ChartExportImage exportImage = new ChartExportImage(dataModel, chartPlugin.getDataSettings().getCurrentAnalysis().getName());

                if (exportImage.getDestinationFile() != null) {

                    exportImage.export(chartPlugin.getvBox());
                }
            }
        });

        exportPDF.setOnAction(event -> {
            if (chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
                ChartExportPDF exportPDF = new ChartExportPDF(dataModel, chartPlugin.getDataSettings().getCurrentAnalysis().getName(), false);

                if (exportPDF.getDestinationFile() != null) {

                    exportPDF.export(chartPlugin.getvBox());
                }
            }
        });

        printButton.setOnAction(event -> {
            if (chartPlugin.getDataSettings().getCurrentAnalysis() != null) {
                ChartExportPDF exportPDF = new ChartExportPDF(dataModel, chartPlugin.getDataSettings().getCurrentAnalysis().getName(), true);

                if (exportPDF.getDestinationFile() != null) {

                    exportPDF.export(chartPlugin.getvBox());
                }
            }
        });

        save.setOnAction(action -> chartPlugin.handleRequest(Constants.Plugin.Command.SAVE));

        loadNew.setOnAction(event -> loadNewDialog());

        zoomOut.setOnAction(event -> resetZoom());

        select2.setOnAction(event -> changeSettings2());

        delete.setOnAction(event -> getChartPluginView().handleRequest(Constants.Plugin.Command.DELETE));

        showRawData.setOnAction(event -> chartPlugin.update());

        showSum.setOnAction(event -> {
            toolBarSettings.setShowSum(!toolBarSettings.isShowSum());
            chartPlugin.update();
        });

        showL1L2.setOnAction(event -> {
            toolBarSettings.setShowL1L2(!toolBarSettings.isShowL1L2());
            chartPlugin.update();
        });

        calcRegression.setOnAction(event -> {
            toolBarSettings.setCalculateRegression(!toolBarSettings.isCalculateRegression());
            chartPlugin.update();
        });

        calcFullLoadHours.setOnAction(event -> toolBarFunctions.calcFullLoadHours());

        calcBaseLoad.setOnAction(event -> toolBarFunctions.calcBaseLoad());

        calcValues.setOnAction(event -> toolBarFunctions.calcValues());

        calcSumAboveBelow.setOnAction(event -> toolBarFunctions.calcSumAboveBelow());

        customWorkDay.setOnAction(event -> chartPlugin.update());

        disableIcons.setOnAction(event -> chartPlugin.update());

        autoResize.setOnAction(event -> chartPlugin.update());

    }    private final ChangeListener<JEVisObject> analysisComboBoxChangeListener = (observable, oldValue, newValue) -> {
        if ((oldValue == null) || (Objects.nonNull(newValue))) {

            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setResizable(true);
                Label text = new Label(I18n.getInstance().getString("plugin.graph.dialog.changed.text"));
                text.setWrapText(true);
                alert.getDialogPane().setContent(text);

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType.equals(ButtonType.OK)) {
                        changed = false;

                        SaveAnalysisDialog saveAnalysisDialog = new SaveAnalysisDialog(getDs(), chartPlugin.getDataSettings(), chartPlugin, this);
                        saveAnalysisDialog.setOnCloseRequest(DialogEvent -> changeAnalysis(newValue));

                        saveAnalysisDialog.show();
                    } else {
                        changeAnalysis(newValue);
                    }
                });
            } else {
                changeAnalysis(newValue);
            }
        }
    };

    public AnalysesComboBox getAnalysesComboBox() {
        return analysesComboBox;
    }

    public void select(JEVisObject obj) {
        getAnalysesComboBox().getSelectionModel().select(obj);
    }

    public void setDisableToolBarIcons(boolean bool) {
        disabledIcons.set(bool);

        analysesComboBox.setDisable(bool);
        save.setDisable(bool);
        loadNew.setDisable(bool);
        exportCSV.setDisable(bool);
        exportImage.setDisable(bool);
        exportPDF.setDisable(bool);
        printButton.setDisable(bool);
        reload.setDisable(bool);
        runUpdateButton.setDisable(bool);
        delete.setDisable(bool);
        autoResize.setDisable(bool);
        select2.setDisable(bool);
        showRawData.setDisable(bool);
        showSum.setDisable(bool);
        showL1L2.setDisable(bool);
        calcRegression.setDisable(bool);
        calcFullLoadHours.setDisable(bool);
        calcSumAboveBelow.setDisable(bool);
        calcBaseLoad.setDisable(bool);
        calcValues.setDisable(bool);
        customWorkDay.setDisable(bool);
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

    private void updateWorkdayTimesFromJEVisObject(JEVisObject jeVisObject) {
        WorkDays wd = new WorkDays(jeVisObject);
        wd.setEnabled(toolBarSettings.isCustomWorkday());
    }

    public ChartPlugin getChartPluginView() {
        return chartPlugin;
    }

    public Boolean getChanged() {
        return changed;
    }

    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    public void resetToolbarSettings() {
        toolBarSettings.setShowIcons(true);
        toolBarSettings.setShowRawData(false);
        toolBarSettings.setShowSum(false);
        toolBarSettings.setShowL1L2(false);
        toolBarSettings.setCalculateRegression(false);
        toolBarSettings.setCustomWorkday(true);
        toolBarSettings.setAutoResize(true);
        toolBarSettings.setRunUpdate(false);
        if (chartPlugin.getService().isRunning()) {
            chartPlugin.getService().cancel();
            chartPlugin.getService().reset();
        }
    }

    public ToolBarSettings getToolBarSettings() {
        return toolBarSettings;
    }


}
