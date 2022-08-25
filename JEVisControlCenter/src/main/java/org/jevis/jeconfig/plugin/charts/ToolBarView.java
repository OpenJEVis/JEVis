/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.charts;

import com.jfoenix.controls.*;
import com.jfoenix.skins.JFXComboBoxListViewSkin;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.MultiChartZoomer;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PickerCombo;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.PresetDateBox;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.Chart;
import org.jevis.jeconfig.application.Chart.Charts.regression.RegressionType;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.BaseLoadDialog;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.dialog.LoadAnalysisDialog;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.tool.NumberSpinner;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author broder
 */
public class ToolBarView {

    private static final Logger logger = LogManager.getLogger(ToolBarView.class);
    private final JEVisDataSource ds;
    private final ChartPlugin chartPlugin;
    private final ObjectRelations objectRelations;
    private AnalysisDataModel model;
    private final JFXComboBox<JEVisObject> listAnalysesComboBox;
    private final SimpleBooleanProperty disabledIcons = new SimpleBooleanProperty(true);
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
    private ToggleButton select;
    private ToggleButton disableIcons;
    private ToggleButton zoomOut;
    private ToggleButton infoButton;
    private ToggleButton helpButton;
    private ToggleButton testButton;
    private final PickerCombo pickerCombo;
    private final PresetDateBox presetDateBox;
    private final JFXDatePicker pickerDateStart;
    private final JFXTimePicker pickerTimeStart;
    private final JFXDatePicker pickerDateEnd;
    private final JFXTimePicker pickerTimeEnd;

    private final DateHelper dateHelper = new DateHelper();
    private ToolBar toolBar;
    private Boolean changed = false;

    private ToggleButton runUpdateButton;
    private ToggleButton customWorkDay;

    private JEVisDataSource getDs() {
        return ds;
    }

    private ToggleButton addSeriesRunningMean;
    private Region pauseIcon;
    private Region playIcon;
    private ToggleButton showRawData;

    private MenuButton mathOperation;
    private CheckMenuItem showSum;
    private CheckMenuItem showL1L2;
    private CheckMenuItem calcRegression;
    private CheckMenuItem calcFullLoadHours;
    private CheckMenuItem calcHoursAboveBelow;
    private CheckMenuItem calcSumAboveBelow;
    private CheckMenuItem calcBaseLoad;

    public ToolBarView(AnalysisDataModel model, JEVisDataSource ds, ChartPlugin chartPlugin) {
        this.model = model;
        this.ds = ds;
        this.objectRelations = new ObjectRelations(ds);
        this.chartPlugin = chartPlugin;

        listAnalysesComboBox = new JFXComboBox<>(model.getObservableListAnalyses());
        listAnalysesComboBox.setPrefWidth(300);

        pickerCombo = new PickerCombo(model, null, true);
        presetDateBox = pickerCombo.getPresetDateBox();
        presetDateBox.getStyleClass().add("ToolBarDatePicker");
        pickerDateStart = pickerCombo.getStartDatePicker();
        pickerDateStart.getStyleClass().add("ToolBarDatePicker");
        pickerTimeStart = pickerCombo.getStartTimePicker();
        pickerTimeStart.getStyleClass().add("ToolBarDatePicker");
        pickerDateEnd = pickerCombo.getEndDatePicker();
        pickerDateEnd.getStyleClass().add("ToolBarDatePicker");
        pickerTimeEnd = pickerCombo.getEndTimePicker();
        pickerTimeEnd.getStyleClass().add("ToolBarDatePicker");

        setCellFactoryForComboBox();

        createToolbarIcons();

        Platform.runLater(() -> setDisableToolBarIcons(true));
    }

    private final ChangeListener<JEVisObject> analysisComboBoxChangeListener = (observable, oldValue, newValue) -> {
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
                        getChartPluginView().handleRequest(Constants.Plugin.Command.SAVE);
                    }
                });
            }
            model.setTemporary(false);
            model.setCurrentAnalysis(newValue);
            model.resetToolbarSettings();
            model.setGlobalAnalysisTimeFrame(model.getGlobalAnalysisTimeFrame());
            Platform.runLater(this::updateLayout);
            changed = false;
        }
    };

    public ToolBar getToolbar() {
        if (toolBar == null) {
            toolBar = new ToolBar();
            toolBar.setId("ObjectPlugin.Toolbar");

            updateLayout();
        }

        return toolBar;
    }

    private void loadNewDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(chartPlugin.getDialogContainer(), ds, model);

        dialog.setOnDialogClosed(event -> {
            JEVisHelp.getInstance().deactivatePluginModule();
            if (dialog.getResponse() == Response.NEW) {

                getChartPluginView().handleRequest(Constants.Plugin.Command.NEW);
            } else if (dialog.getResponse() == Response.LOAD) {

                final Preferences previewPref = Preferences.userRoot().node("JEVis.JEConfig.preview");
                if (!previewPref.getBoolean("enabled", true)) {
                    model.setAnalysisTimeFrameForAllModels(model.getGlobalAnalysisTimeFrame());
                }
            }

            if (model.getCurrentAnalysis() != null) {
                Platform.runLater(() -> setDisableToolBarIcons(false));
            } else {
                Platform.runLater(() -> setDisableToolBarIcons(true));
            }
        });

        Platform.runLater(() -> setDisableToolBarIcons(true));
        dialog.show();
        Platform.runLater(() -> dialog.getFilterInput().requestFocus());
    }


    public void addAnalysisComboBoxListener() {
        listAnalysesComboBox.valueProperty().addListener(analysisComboBoxChangeListener);
    }

    public void removeAnalysisComboBoxListener() {
        listAnalysesComboBox.valueProperty().removeListener(analysisComboBoxChangeListener);
    }

    private void resetZoom() {
        for (Map.Entry<Integer, Chart> entry : chartPlugin.getAllCharts().entrySet()) {
            Integer integer = entry.getKey();
            Chart chart = entry.getValue();
            if (chart.getChart() != null) {
                for (de.gsi.chart.plugins.ChartPlugin chartPlugin : chart.getChart().getPlugins()) {
                    if (chartPlugin instanceof MultiChartZoomer) {
                        MultiChartZoomer multiChartZoomer = (MultiChartZoomer) chartPlugin;
                        multiChartZoomer.setFollowUpZoom(false);
                        Platform.runLater(multiChartZoomer::zoomOrigin);
                        break;
                    }
                }
                break;
            }
        }
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
                            if (!model.isMultiSite() && !model.isMultiDir())
                                setText(obj.getName());
                            else {
                                String prefix = "";
                                if (model.isMultiSite()) {
                                    prefix += objectRelations.getObjectPath(obj);
                                }
                                if (model.isMultiDir()) {
                                    prefix += objectRelations.getRelativePath(obj);
                                }

                                setText(prefix + obj.getName());
                            }
                        }

                    }
                };
            }
        };

        listAnalysesComboBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.list")));
        listAnalysesComboBox.setCellFactory(cellFactory);
        listAnalysesComboBox.setButtonCell(cellFactory.call(null));
        listAnalysesComboBox.setId("Graph Analysis List");

        listAnalysesComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                JFXComboBoxListViewSkin<?> skin = (JFXComboBoxListViewSkin<?>) listAnalysesComboBox.getSkin();
                if (skin != null) {
                    ListView<?> popupContent = (ListView<?>) skin.getPopupContent();
                    if (popupContent != null) {
                        popupContent.scrollTo(model.getObservableListAnalyses().indexOf(model.getCurrentAnalysis()));
                    }
                }
            });
        });
        JEVisHelp.getInstance().addHelpControl(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, listAnalysesComboBox);
    }

    public void updateLayout() {

        Platform.runLater(() -> {

            removeAnalysisComboBoxListener();

            if (model.getCurrentAnalysis() != null) {
                listAnalysesComboBox.getSelectionModel().select(model.getCurrentAnalysis());
            }

            if (!listAnalysesComboBox.getItems().isEmpty()) {
                dateHelper.setWorkDays(model.getWorkDays());
            }

            toolBar.getItems().clear();
            mathOperation.getItems().clear();

            pickerCombo.initialize(model, null, customWorkDay.isSelected());
            pickerCombo.updateCellFactory();

            Separator sep1 = new Separator();
            Separator sep2 = new Separator();
            Separator sep3 = new Separator();
            Separator sep4 = new Separator();

            boolean isRegressionPossible = false;
            boolean isAdditionalCalcPossible = false;

            for (ChartSetting chartSetting : model.getCharts().getListSettings()) {
                if (chartSetting.getChartType() != ChartType.TABLE && chartSetting.getChartType() != ChartType.HEAT_MAP
                        && chartSetting.getChartType() != ChartType.BAR && chartSetting.getChartType() != ChartType.PIE) {
                    isRegressionPossible = true;
                    break;
                }
            }

            QuantityUnits qu = new QuantityUnits();
            for (ChartDataRow chartDataRow : model.getSelectedData()) {
                if (qu.isSumCalculable(chartDataRow.getUnit())) {
                    isAdditionalCalcPossible = true;
                    break;
                }
            }

            toolBar.getItems().addAll(listAnalysesComboBox,
                    sep1, presetDateBox, pickerDateStart, pickerDateEnd, customWorkDay,
                    sep2, reload, zoomOut,
                    sep3, loadNew, save, delete, select, export,exportImage, printButton,
                    sep4);

            if (isRegressionPossible) {
                mathOperation.getItems().add(calcRegression);
            }

            if (isAdditionalCalcPossible) {
                mathOperation.getItems().addAll(calcFullLoadHours, calcHoursAboveBelow, calcSumAboveBelow, calcBaseLoad);
            }

            if (!JEConfig.getExpert()) {

                mathOperation.getItems().addAll(showL1L2, showSum);
                toolBar.getItems().addAll(mathOperation, disableIcons, autoResize, runUpdateButton);
            } else {

                mathOperation.getItems().addAll(showL1L2, showSum);
                toolBar.getItems().addAll(showRawData,mathOperation, disableIcons, autoResize, runUpdateButton);
            }

//            toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), testButton, helpButton, infoButton);
            toolBar.getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton);

            addAnalysisComboBoxListener();
            setDisableToolBarIcons(disabledIcons.get());
        });
    }

    private void hideShowIconsInGraph() {
        model.setShowIcons(!model.getShowIcons());
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

    private void showL1L2InGraph() {
        model.setShowL1L2(!model.getShowL1L2());
    }

    private void calcRegression() {
        if (!model.calcRegression()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            Label polyDegreeLabel = new Label(I18n.getInstance().getString("plugin.graph.toolbar.regression.degree"));
            NumberSpinner polyDegreeNumberSpinner = new NumberSpinner(new BigDecimal(1), new BigDecimal(1));
            polyDegreeNumberSpinner.setMin(new BigDecimal(1));
            polyDegreeNumberSpinner.setMax(new BigDecimal(11));

//            Label regressionTypeLabel = new Label(I18n.getInstance().getString("plugin.graph.toolbar.regression.type"));
            Label regressionTypeLabel = new Label(I18n.getInstance().getString("dialog.regression.type.poly"));
//            RegressionBox regressionTypeComboBox = new RegressionBox();

            GridPane gridPane = new GridPane();
            gridPane.setVgap(4);
            gridPane.setHgap(4);

            gridPane.add(regressionTypeLabel, 0, 0);
//            gridPane.add(regressionTypeComboBox, 1, 0);

            gridPane.add(polyDegreeLabel, 0, 1);
            gridPane.add(polyDegreeNumberSpinner, 1, 1);

//            regressionTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//                if (!newValue.equals(oldValue)) {
//                    if (newValue.equals(RegressionType.POLY)) {
//                        gridPane.add(polyDegreeLabel, 0, 1);
//                        gridPane.add(polyDegreeNumberSpinner, 1, 1);
//                    } else {
//                        gridPane.getChildren().removeAll(polyDegreeLabel, polyDegreeNumberSpinner);
//                    }
//                }
//            });

            alert.getDialogPane().setContent(gridPane);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType.getButtonData().isDefaultButton()) {
                    model.setPolyRegressionDegree(polyDegreeNumberSpinner.getNumber().toBigInteger().intValue());
//                    model.setRegressionType(regressionTypeComboBox.getSelectionModel().getSelectedItem());
                    model.setRegressionType(RegressionType.POLY);
                    model.setCalcRegression(!model.calcRegression());
                }
            });
        } else {
            model.setPolyRegressionDegree(-1);
            model.setRegressionType(RegressionType.NONE);
            model.setCalcRegression(!model.calcRegression());
        }
    }

    private void calcFullLoadHours() {
        Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
        infoBox.setResizable(true);
        infoBox.setTitle(I18n.getInstance().getString("dialog.fullloadhours.title"));
        infoBox.setHeaderText(I18n.getInstance().getString("dialog.fullloadhours.headertext"));
        TableView<ChartDataRow> fullLoadHours = new TableView<>();

        NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);

        TableColumn<ChartDataRow, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
        nameColumn.setSortable(true);
        nameColumn.setPrefWidth(400);
        nameColumn.setMinWidth(100);
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getTitle()));

        nameColumn.setCellFactory(new Callback<TableColumn<ChartDataRow, String>, TableCell<ChartDataRow, String>>() {
            @Override
            public TableCell<ChartDataRow, String> call(TableColumn<ChartDataRow, String> param) {
                return new TableCell<ChartDataRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText("");
                        setGraphic(null);

                        if (item != null && !empty) {
                            setText(item);
                        }
                    }
                };
            }
        });


        TableColumn<ChartDataRow, String> valueColumn = new TableColumn<>(I18n.getInstance().getString("dialog.fullloadhours.title"));
        valueColumn.setStyle("-fx-alignment: CENTER");
        valueColumn.setSortable(true);
        valueColumn.setPrefWidth(125);
        valueColumn.setMinWidth(75);
        valueColumn.setCellValueFactory(param -> {
            Double result = 0d;

            if (param.getValue().getSum() != null && param.getValue().getMax() != null && !param.getValue().getMax().equals(0d)) {
                result = param.getValue().getSum() / param.getValue().getMax();
            }

            return new SimpleObjectProperty<>(nf.format(result));
        });

        valueColumn.setCellFactory(new Callback<TableColumn<ChartDataRow, String>, TableCell<ChartDataRow, String>>() {
            @Override
            public TableCell<ChartDataRow, String> call(TableColumn<ChartDataRow, String> param) {
                return new TableCell<ChartDataRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText("");
                        setGraphic(null);

                        if (item != null && !empty) {
                            setText(item);
                        }
                    }
                };
            }
        });

        fullLoadHours.getColumns().addAll(nameColumn, valueColumn);
        fullLoadHours.getItems().addAll(model.getSelectedData());

        AlphanumComparator ac = new AlphanumComparator();
        fullLoadHours.getItems().sort((o1, o2) -> ac.compare(o1.getTitle(), o2.getTitle()));

        infoBox.getDialogPane().setContent(fullLoadHours);
        infoBox.show();
    }

    private void calcBaseLoad() {
        JFXDialog infoBox = new JFXDialog();
        infoBox.setDialogContainer(chartPlugin.getDialogContainer());
        infoBox.setTransitionType(JFXDialog.DialogTransition.NONE);

        Label baseLoadTimeFrame = new Label(I18n.getInstance().getString("dialog.baseload.timeframe"));

        JFXDatePicker baseLoadStartDate = new JFXDatePicker(LocalDate.now());
        baseLoadStartDate.setPrefWidth(120d);

        JFXTimePicker baseLoadStartTime = new JFXTimePicker(LocalTime.now());
        baseLoadStartTime.setPrefWidth(100d);
        baseLoadStartTime.setMaxWidth(100d);
        baseLoadStartTime.set24HourView(true);
        baseLoadStartTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        JFXDatePicker baseLoadEndDate = new JFXDatePicker(LocalDate.now());
        baseLoadEndDate.setPrefWidth(120d);

        JFXTimePicker baseLoadEndTime = new JFXTimePicker(LocalTime.now());
        baseLoadEndTime.setPrefWidth(100d);
        baseLoadEndTime.setMaxWidth(100d);
        baseLoadEndTime.set24HourView(true);
        baseLoadEndTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        Label baseLoadBinding = new Label(I18n.getInstance().getString("dialog.baseload.repeatingtimeframe"));
        ObservableList<Integer> list = FXCollections.observableArrayList(0, 1, 2, 3, 4);
        JFXComboBox<Integer> boundSpecificBox = new JFXComboBox<>(list);

        Callback<ListView<Integer>, ListCell<Integer>> cellFactoryBoundToSpecificBox = new Callback<javafx.scene.control.ListView<Integer>, ListCell<Integer>>() {
            @Override
            public ListCell<Integer> call(javafx.scene.control.ListView<Integer> param) {
                return new ListCell<Integer>() {
                    @Override
                    protected void updateItem(Integer no, boolean empty) {
                        super.updateItem(no, empty);
                        if (empty || no == null) {
                            setText("");
                        } else {
                            String text = "";
                            switch (no) {
                                case 0:
                                    text = I18n.getInstance().getString("plugin.object.attribute.gapfillingeditor.boundtospecific.none");
                                    break;
                                case 1:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.day");
                                    break;
                                case 2:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.week");
                                    break;
                                case 3:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.month");
                                    break;
                                case 4:
                                    text = I18n.getInstance().getString("plugin.object.report.dialog.aggregation.year");
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        boundSpecificBox.setCellFactory(cellFactoryBoundToSpecificBox);
        boundSpecificBox.setButtonCell(cellFactoryBoundToSpecificBox.call(null));
        boundSpecificBox.getSelectionModel().select(0);

        Label resultTimeFrame = new Label(I18n.getInstance().getString("dialog.baseload.resulttimeframe"));

        JFXDatePicker resultStartDate = new JFXDatePicker(pickerDateStart.getValue());
        resultStartDate.setPrefWidth(120d);

        JFXTimePicker resultStartTime = new JFXTimePicker(pickerTimeStart.getValue());
        resultStartTime.setPrefWidth(100d);
        resultStartTime.setMaxWidth(100d);
        resultStartTime.set24HourView(true);
        resultStartTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        JFXDatePicker resultEndDate = new JFXDatePicker(pickerDateEnd.getValue());
        resultEndDate.setPrefWidth(120d);

        JFXTimePicker resultEndTime = new JFXTimePicker(pickerTimeEnd.getValue());
        resultEndTime.setPrefWidth(100d);
        resultEndTime.setMaxWidth(100d);
        resultEndTime.set24HourView(true);
        resultEndTime.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        GridPane gp = new GridPane();
        gp.setVgap(6);
        gp.setHgap(6);

        int row = 0;
        gp.add(baseLoadTimeFrame, 0, row, 4, 1);
        row++;

        gp.add(baseLoadStartDate, 0, row);
        gp.add(baseLoadStartTime, 1, row);
        gp.add(baseLoadEndDate, 2, row);
        gp.add(baseLoadEndTime, 3, row);
        row++;

        gp.add(baseLoadBinding, 0, row, 4, 1);
        row++;

        gp.add(boundSpecificBox, 0, row, 2, 1);
        row++;

        gp.add(resultTimeFrame, 0, row, 4, 1);
        row++;

        gp.add(resultStartDate, 0, row);
        gp.add(resultStartTime, 1, row);
        gp.add(resultEndDate, 2, row);
        gp.add(resultEndTime, 3, row);

        final JFXButton ok = new JFXButton(I18n.getInstance().getString("newobject.ok"));
        ok.setDefaultButton(true);
        final JFXButton cancel = new JFXButton(I18n.getInstance().getString("newobject.cancel"));
        cancel.setCancelButton(true);

        HBox buttonBar = new HBox(6, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(12));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, gp, separator, buttonBar);
        vBox.setPadding(new Insets(12));

        ok.setOnAction(event -> {
            try {
                BaseLoadSetting setting = new BaseLoadSetting();
                setting.setBaseLoadStart(baseLoadStartDate.getValue(), baseLoadStartTime.getValue());
                setting.setBaseLoadEnd(baseLoadEndDate.getValue(), baseLoadEndTime.getValue());
                setting.setRepeatType(boundSpecificBox.getSelectionModel().getSelectedItem());
                setting.setResultStart(resultStartDate.getValue(), resultStartTime.getValue());
                setting.setResultEnd(resultEndDate.getValue(), resultEndTime.getValue());

                BaseLoadDialog dialog = new BaseLoadDialog(chartPlugin.getDialogContainer(), setting, model);

                dialog.show();

            } catch (Exception e) {
                logger.error(e);
            }
            infoBox.close();
        });

        cancel.setOnAction(event -> infoBox.close());

        infoBox.setContent(vBox);
        infoBox.show();
    }

    private void calcHoursAboveBelow() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(I18n.getInstance().getString("dialog.calchoursabovebelow.entervalue"));

        Label limitLabel = new Label(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.limitvalue"));
        JFXTextField limitField = new JFXTextField();

        DoubleValidator validator = DoubleValidator.getInstance();
        limitField.textProperty().addListener((observable, oldValue, newValue) -> {

            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
            } catch (Exception e) {
                limitField.setText(oldValue);
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setVgap(4);
        gridPane.setHgap(12);

        gridPane.add(limitLabel, 0, 0);
        gridPane.add(limitField, 1, 0);

        alert.getDialogPane().setContent(gridPane);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData().isDefaultButton() && !limitField.getText().isEmpty()) {
                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Double limit = validator.validate(limitField.getText());
                            TableView<HoursAbove> fullLoadHours = new TableView<>();

                            TableColumn<HoursAbove, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
                            nameColumn.setSortable(true);
                            nameColumn.setPrefWidth(400);
                            nameColumn.setMinWidth(100);
                            nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChartDataRow().getTitle()));

                            nameColumn.setCellFactory(new Callback<TableColumn<HoursAbove, String>, TableCell<HoursAbove, String>>() {
                                @Override
                                public TableCell<HoursAbove, String> call(TableColumn<HoursAbove, String> param) {
                                    return new TableCell<HoursAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<HoursAbove, String> belowColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.below"));
                            belowColumn.setStyle("-fx-alignment: CENTER");
                            belowColumn.setSortable(true);
                            belowColumn.setPrefWidth(200);
                            belowColumn.setMinWidth(150);
                            belowColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBelow()));

                            belowColumn.setCellFactory(new Callback<TableColumn<HoursAbove, String>, TableCell<HoursAbove, String>>() {
                                @Override
                                public TableCell<HoursAbove, String> call(TableColumn<HoursAbove, String> param) {
                                    return new TableCell<HoursAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                JFXTextArea jfxTextArea = new JFXTextArea(item);
                                                jfxTextArea.setWrapText(true);
                                                setGraphic(jfxTextArea);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<HoursAbove, String> aboveColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.above"));
                            aboveColumn.setStyle("-fx-alignment: CENTER");
                            aboveColumn.setSortable(true);
                            aboveColumn.setPrefWidth(200);
                            aboveColumn.setMinWidth(150);
                            aboveColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAbove()));

                            aboveColumn.setCellFactory(new Callback<TableColumn<HoursAbove, String>, TableCell<HoursAbove, String>>() {
                                @Override
                                public TableCell<HoursAbove, String> call(TableColumn<HoursAbove, String> param) {
                                    return new TableCell<HoursAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                JFXTextArea jfxTextArea = new JFXTextArea(item);
                                                jfxTextArea.setWrapText(true);
                                                setGraphic(jfxTextArea);
                                            }
                                        }
                                    };
                                }
                            });

                            fullLoadHours.getColumns().addAll(nameColumn, belowColumn, aboveColumn);

                            List<HoursAbove> hoursAbove = new ArrayList<>();
                            model.getSelectedData().forEach(chartDataRow -> hoursAbove.add(new HoursAbove(chartDataRow, limit)));

                            AlphanumComparator ac = new AlphanumComparator();
                            hoursAbove.sort((o1, o2) -> ac.compare(o1.getChartDataRow().getTitle(), o2.getChartDataRow().getTitle()));
                            fullLoadHours.getItems().addAll(hoursAbove);

                            Platform.runLater(() -> {
                                Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                                infoBox.setResizable(true);
                                infoBox.setTitle(I18n.getInstance().getString("dialog.calchoursabovebelow.title"));
                                infoBox.setHeaderText(I18n.getInstance().getString("dialog.calchoursabovebelow.headertext") + " " + limit);
                                infoBox.getDialogPane().setContent(fullLoadHours);
                                infoBox.show();
                            });

                        } catch (Exception e) {
                            this.failed();
                            logger.error("Could not calculate times", e);
                        } finally {
                            succeeded();
                        }
                        return null;
                    }
                };

                JEConfig.getStatusBar().addTask(ToolBarView.class.getName(), task, JEConfig.getImage("hoursabove.png"), true);
            }
        });
    }

    private void calcSumAboveBelow() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(I18n.getInstance().getString("dialog.calchoursabovebelow.entervalue"));

        Label limitLabel = new Label(I18n.getInstance().getString("plugin.scada.element.setting.label.lowerlimit.limitvalue"));
        JFXTextField limitField = new JFXTextField();
        DoubleValidator validator = DoubleValidator.getInstance();
        limitField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
            } catch (Exception e) {
                limitField.setText(oldValue);
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setVgap(4);
        gridPane.setHgap(12);

        gridPane.add(limitLabel, 0, 0);
        gridPane.add(limitField, 1, 0);

        alert.getDialogPane().setContent(gridPane);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getButtonData().isDefaultButton() && !limitField.getText().isEmpty()) {
                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            Double limit = validator.validate(limitField.getText());

                            TableView<SumsAbove> fullLoadHours = new TableView<>();

                            TableColumn<SumsAbove, String> nameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
                            nameColumn.setSortable(true);
                            nameColumn.setPrefWidth(400);
                            nameColumn.setMinWidth(100);
                            nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChartDataRow().getTitle()));

                            nameColumn.setCellFactory(new Callback<TableColumn<SumsAbove, String>, TableCell<SumsAbove, String>>() {
                                @Override
                                public TableCell<SumsAbove, String> call(TableColumn<SumsAbove, String> param) {
                                    return new TableCell<SumsAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<SumsAbove, String> belowColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.below"));
                            belowColumn.setStyle("-fx-alignment: CENTER-RIGHT");
                            belowColumn.setSortable(true);
                            belowColumn.setPrefWidth(200);
                            belowColumn.setMinWidth(150);
                            belowColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBelow()));

                            belowColumn.setCellFactory(new Callback<TableColumn<SumsAbove, String>, TableCell<SumsAbove, String>>() {
                                @Override
                                public TableCell<SumsAbove, String> call(TableColumn<SumsAbove, String> param) {
                                    return new TableCell<SumsAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            TableColumn<SumsAbove, String> aboveColumn = new TableColumn<>(I18n.getInstance().getString("dialog.calchoursabovebelow.above"));
                            aboveColumn.setStyle("-fx-alignment: CENTER-RIGHT");
                            aboveColumn.setSortable(true);
                            aboveColumn.setPrefWidth(200);
                            aboveColumn.setMinWidth(150);
                            aboveColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAbove()));

                            aboveColumn.setCellFactory(new Callback<TableColumn<SumsAbove, String>, TableCell<SumsAbove, String>>() {
                                @Override
                                public TableCell<SumsAbove, String> call(TableColumn<SumsAbove, String> param) {
                                    return new TableCell<SumsAbove, String>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);
                                            setText("");
                                            setGraphic(null);

                                            if (item != null && !empty) {
                                                setText(item);
                                            }
                                        }
                                    };
                                }
                            });

                            fullLoadHours.getColumns().addAll(nameColumn, belowColumn, aboveColumn);

                            List<SumsAbove> hoursAbove = new ArrayList<>();
                            model.getSelectedData().forEach(chartDataRow -> hoursAbove.add(new SumsAbove(chartDataRow, limit)));

                            AlphanumComparator ac = new AlphanumComparator();
                            hoursAbove.sort((o1, o2) -> ac.compare(o1.getChartDataRow().getTitle(), o2.getChartDataRow().getTitle()));
                            fullLoadHours.getItems().addAll(hoursAbove);

                            Platform.runLater(() -> {
                                Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                                infoBox.setResizable(true);
                                infoBox.setTitle(I18n.getInstance().getString("dialog.calcsumabovebelow.title"));
                                infoBox.setHeaderText(I18n.getInstance().getString("dialog.calcsumabovebelow.headertext") + " " + limit);
                                infoBox.getDialogPane().setContent(fullLoadHours);
                                infoBox.show();
                            });

                        } catch (Exception e) {
                            this.failed();
                            logger.error("Could not calculate times", e);
                        } finally {
                            succeeded();
                        }
                        return null;
                    }
                };

                JEConfig.getStatusBar().addTask(ToolBarView.class.getName(), task, JEConfig.getImage("sumabove.png"), true);
            }
        });
    }

    public JFXComboBox<JEVisObject> getListAnalysesComboBox() {
        return listAnalysesComboBox;
    }

    private void changeSettings() {
        ChartSelectionDialog dia = new ChartSelectionDialog(chartPlugin.getDialogContainer(), ds, model);

        dia.setOnDialogClosed(event -> {
            if (dia.getResponse() == Response.OK) {

                model.setCharts(dia.getChartPlugin().getData().getCharts());
                model.setSelectedData(dia.getChartPlugin().getData().getSelectedData());
                changed = true;
            }
            JEVisHelp.getInstance().deactivatePluginModule();
        });


        dia.show();


    }

    public void select(JEVisObject obj) {
        getListAnalysesComboBox().getSelectionModel().select(obj);
    }

    public void setDisableToolBarIcons(boolean bool) {
        disabledIcons.set(bool);

        listAnalysesComboBox.setDisable(bool);
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
        select.setDisable(bool);
        showRawData.setDisable(bool);
        showSum.setDisable(bool);
        showL1L2.setDisable(bool);
        calcRegression.setDisable(bool);
        calcFullLoadHours.setDisable(bool);
        calcHoursAboveBelow.setDisable(bool);
        calcSumAboveBelow.setDisable(bool);
        calcBaseLoad.setDisable(bool);
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

    private void startToolbarIconListener() {
        reload.selectedProperty().addListener((observable, oldValue, newValue) -> chartPlugin.handleRequest(Constants.Plugin.Command.RELOAD));

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
            ChartExportCSV ge = null;
            if (chartPlugin.isZoomed()) {
                ge = new ChartExportCSV(
                        ds,
                        model,
                        new DateTime(chartPlugin.getxAxisLowerBound().longValue() * 1000),
                        new DateTime(chartPlugin.getxAxisUpperBound().longValue() * 1000));
            } else {
                ge = new ChartExportCSV(
                        ds,
                        model,
                        model.getGlobalAnalysisTimeFrame().getStart(),
                        model.getGlobalAnalysisTimeFrame().getEnd());
            }

            try {
                ge.export();
            } catch (FileNotFoundException | UnsupportedEncodingException | JEVisException e) {
                logger.error("Error: could not export to file.", e);
            }
        });

        exportImage.setOnAction(action -> {
            ChartExportImage exportImage = new ChartExportImage(model);

            if (exportImage.getDestinationFile() != null) {

                exportImage.export(chartPlugin.getvBox());
            }

        });

        exportPDF.setOnAction(event -> {
            ChartExportPDF exportPDF = new ChartExportPDF(model, false);

            if (exportPDF.getDestinationFile() != null) {

                exportPDF.export(chartPlugin.getvBox());
            }
        });

        printButton.setOnAction(event -> {

            ChartExportPDF exportPDF = new ChartExportPDF(model, true);

            if (exportPDF.getDestinationFile() != null) {

                exportPDF.export(chartPlugin.getvBox());
            }

        });

        save.setOnAction(action -> chartPlugin.handleRequest(Constants.Plugin.Command.SAVE));

        loadNew.setOnAction(event -> {
            loadNewDialog();
        });

        zoomOut.setOnAction(event -> resetZoom());

        select.setOnAction(event -> {
            changeSettings();
        });

        delete.setOnAction(event -> getChartPluginView().handleRequest(Constants.Plugin.Command.DELETE));

        showRawData.setOnAction(event -> showRawDataInGraph());

        showSum.setOnAction(event -> showSumInGraph());

        showL1L2.setOnAction(event -> showL1L2InGraph());

        calcRegression.setOnAction(event -> calcRegression());

        calcFullLoadHours.setOnAction(event -> calcFullLoadHours());

        calcBaseLoad.setOnAction(event -> calcBaseLoad());

        calcHoursAboveBelow.setOnAction(event -> calcHoursAboveBelow());

        calcSumAboveBelow.setOnAction(event -> calcSumAboveBelow());

        customWorkDay.setOnAction(event -> customWorkDay());

        disableIcons.setOnAction(event -> hideShowIconsInGraph());

        addSeriesRunningMean.setOnAction(event -> addSeriesRunningMean());

        autoResize.setOnAction(event -> autoResizeInGraph());

    }

    private void createToolbarIcons() {
        double iconSize = 20;

        save = new ToggleButton("", JEConfig.getSVGImage(Icon.SAVE, iconSize, iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        Tooltip saveTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.save"));
        save.setTooltip(saveTooltip);

        loadNew = new ToggleButton("", JEConfig.getSVGImage(Icon.FOLDER_OPEN, iconSize, iconSize));
        Tooltip loadNewTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.loadNew"));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(loadNew);
        loadNew.setTooltip(loadNewTooltip);

        export = new MenuButton("", JEConfig.getSVGImage(Icon.EXPORT, iconSize, iconSize));
        Tooltip exportCSVTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));
        export.setTooltip(exportCSVTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(export);

        exportCSV = new MenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportCSV"));

        exportImage = new ToggleButton("", JEConfig.getSVGImage(Icon.SCREENSHOT, iconSize, iconSize));
        Tooltip exportImageTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.exportImage"));
        exportImage.setTooltip(exportImageTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportImage);

        exportPDF = new MenuItem(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.exportPDF"));

        export.getItems().addAll(exportCSV, exportPDF);

        printButton = new ToggleButton("", JEConfig.getSVGImage(Icon.PRINT, iconSize, iconSize));
        Tooltip printTooltip = new Tooltip(I18n.getInstance().getString("plugin.reports.toolbar.tooltip.print"));
        printButton.setTooltip(printTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(printButton);

        reload = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);

        pauseIcon = JEConfig.getSVGImage(Icon.PAUSE, iconSize, iconSize);
        playIcon = JEConfig.getSVGImage(Icon.PLAY, iconSize, iconSize);

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

        delete = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, iconSize, iconSize));
        Tooltip deleteTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.delete"));
        delete.setTooltip(deleteTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        autoResize = new ToggleButton("", JEConfig.getSVGImage(Icon.MAXIMIZE, iconSize, iconSize));
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

        select = new ToggleButton("", JEConfig.getSVGImage(Icon.SETTINGS, iconSize, iconSize));
        Tooltip selectTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.select"));
        select.setTooltip(selectTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(select);

        showRawData = new ToggleButton("", JEConfig.getSVGImage(Icon.RAW_ON, iconSize, iconSize));
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


        mathOperation = new MenuButton("",JEConfig.getSVGImage(Icon.SUM, iconSize, iconSize));
        showSum = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showsum"));
        showSum.setSelected(model.getShowSum());

        showL1L2 = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showl1l2"));
        showL1L2.setSelected(model.getShowL1L2());


        calcRegression = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcregression"));
        calcRegression.setSelected(model.calcRegression());

        calcFullLoadHours = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcfullloadhours"));


        calcHoursAboveBelow = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calchoursabovebelow"));

        calcSumAboveBelow = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcsumabovebelow"));


        calcBaseLoad = new CheckMenuItem(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.calcbaseloadhours"));


        customWorkDay = new ToggleButton("", JEConfig.getSVGImage(Icon.CALENDAR, iconSize, iconSize));
        Tooltip customWorkDayTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.customworkday"));
        customWorkDay.setTooltip(customWorkDayTooltip);
        customWorkDay.setSelected(model.isCustomWorkDay());
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

        disableIcons = new ToggleButton("", JEConfig.getSVGImage(Icon.Warning, iconSize, iconSize));
        Tooltip disableIconsTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.disableicons"));
        disableIcons.setTooltip(disableIconsTooltip);
        disableIcons.setSelected(model.getShowIcons());
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

        addSeriesRunningMean = new ToggleButton("", JEConfig.getSVGImage(Icon.ALARM, iconSize, iconSize));
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


        zoomOut = new ToggleButton("", JEConfig.getSVGImage(Icon.ZOOM_OUT, iconSize, iconSize));
        Tooltip zoomOutTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.zoomout"));
        zoomOut.setTooltip(zoomOutTooltip);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);

        helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
        infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
        testButton = new ToggleButton("X");
        testButton.setOnAction(actionEvent -> {
//            try {
//                List<JEVisClass> classFilter = new ArrayList<>();
//                classFilter.add(ds.getJEVisClass("Data"));
//                classFilter.add(ds.getJEVisClass("Clean Data"));
//                classFilter.add(ds.getJEVisClass("Math Data"));
//                classFilter.add(ds.getJEVisClass("Base Data"));
//
//                TreeSelectionDialog selectionDialog = new TreeSelectionDialog(getChartPluginView().getDialogContainer(), ds, classFilter, SelectionMode.SINGLE);
//
//                selectionDialog.setOnDialogClosed(jfxDialogEvent -> {
//                    StringBuilder stringBuilder = new StringBuilder();
//
//
//                    for (JEVisObject object : selectionDialog.getTreeView().getSelectedObjects()) {
//                        stringBuilder.append("\n").append(object.getName());
//                    }
//                    Alert selectionShow = new Alert(Alert.AlertType.INFORMATION, "Selected Objects: " + stringBuilder);
//                    selectionShow.show();
//                });
//
//                selectionDialog.show();
//            } catch (Exception e) {
//                logger.error("Error while testing", e);
//            }
        });

        List<Node> nodes = Arrays.asList(listAnalysesComboBox,
                presetDateBox, pickerDateStart, pickerDateEnd, customWorkDay,
                reload, zoomOut,
                loadNew, save, delete, select,export, exportImage, printButton,
                disableIcons, autoResize, runUpdateButton);

        pickerCombo.addListener();
        startToolbarIconListener();

        JEVisHelp.getInstance().addHelpItems(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, nodes);
    }

    private void customWorkDay() {
        model.setCustomWorkDay(!model.isCustomWorkDay());
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


}
