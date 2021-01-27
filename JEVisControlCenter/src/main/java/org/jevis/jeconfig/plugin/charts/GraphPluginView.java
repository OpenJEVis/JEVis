/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.charts;

import com.google.common.util.concurrent.AtomicDouble;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.AxisMode;
import eu.hansolo.fx.charts.MatrixPane;
import eu.hansolo.fx.charts.data.MatrixChartItem;
import eu.hansolo.fx.charts.tools.Helper;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonChartDataModel;
import org.jevis.commons.json.JsonChartSettings;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jeapi.ws.JEVisObjectWS;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.*;
import org.jevis.jeconfig.application.Chart.ChartElements.MultiChartZoomer;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeader;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns.ColorColumn;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DataPointNoteDialog;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DataPointTableViewPointer;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jeconfig.application.Chart.Charts.*;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.dialog.*;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPluginView implements Plugin {

    private static final Logger logger = LogManager.getLogger(GraphPluginView.class);
    public static String PLUGIN_NAME = "Graph Plugin";
    //    private final List<ChartView> charts = new ArrayList<>();
    private final DoubleProperty zoomDurationMillis = new SimpleDoubleProperty(750.0);
    private final ToolBarView toolBarView;
    private final AnalysisDataModel dataModel;
    //private GraphController controller;
    private final StringProperty name = new SimpleStringProperty("Graph");
    private final StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private ToolBar toolBar;
    private final String tooltip = I18n.getInstance().getString("pluginmanager.graph.tooltip");
    private boolean firstStart = true;
    private final ScrollPane sp = new ScrollPane();
    public static String JOB_NAME = "Chart Update";
    private Double xAxisLowerBound;
    private Double xAxisUpperBound;
    private boolean zoomed = false;
    //this.chartView = new ChartView(dataModel);
    private final VBox vBox = new VBox();
    private final BorderPane border = new BorderPane(sp);
    private final Tooltip tp = new Tooltip("");
    private final HashMap<Integer, Chart> allCharts = new HashMap<>();
    private final Image taskImage = JEConfig.getImage("Analysis.png");

    public GraphPluginView(JEVisDataSource ds, String newname) {
        this.dataModel = new AnalysisDataModel(ds, this);
//        this.dataModel.addObserver(this);

        //this.controller = new GraphController(this, dataModel);
        this.toolBarView = new ToolBarView(dataModel, ds, this);
        getToolbar();

        this.vBox.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
        this.sp.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
        this.sp.setFitToWidth(true);
        this.sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        this.border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        this.ds = ds;
        this.name.set(newname);

        /**
         * If scene size changes and old value is not 0.0 (firsts draw) redraw
         * TODO: resizing an window manually will cause a lot of resize changes and so redraws, solve this better
         */
        border.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(0.0) && dataModel.getSelectedData() != null && !dataModel.getSelectedData().isEmpty()) {
                boolean hasHeatMap = false;
                for (Map.Entry<Integer, Chart> entry : allCharts.entrySet()) {
                    if (entry.getValue().getChartType() == ChartType.HEAT_MAP) {
                        hasHeatMap = true;
                    }
                }
                if (!hasHeatMap) {
                    Platform.runLater(this::autoSize);
                } else {
                    update();
                }
            }
        });
        border.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(0.0) && dataModel.getSelectedData() != null && !dataModel.getSelectedData().isEmpty()) {
                boolean hasHeatMap = false;
                for (Map.Entry<Integer, Chart> entry : allCharts.entrySet()) {
                    if (entry.getValue().getChartType() == ChartType.HEAT_MAP) {
                        hasHeatMap = true;
                    }
                }
                if (!hasHeatMap) {
                    Platform.runLater(this::autoSize);
                } else {
                    update();
                }
            }
        });

//        border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2 + "; -fx-faint-focus-color: transparent; -fx-focus-color: transparent;");

    }

    private void autoSize() {
        Long chartsPerScreen = dataModel.getChartsPerScreen();

        AtomicDouble autoMinSize = new AtomicDouble(0);
        double autoMinSizeNormal = 230;
        double autoMinSizeLogical = 50;

        if (dataModel.getSelectedData() != null) {
            double maxHeight = border.getHeight();

            for (ChartSetting chartSetting : dataModel.getCharts().getListSettings()) {
                if (chartSetting.getChartType().equals(ChartType.LOGICAL)) {
                    autoMinSize.set(autoMinSizeLogical);
                } else {
                    autoMinSize.set(autoMinSizeNormal);
                }
            }

            autoSize(autoMinSize.get(), maxHeight, chartsPerScreen, vBox);

            for (ChartSetting settings : dataModel.getCharts().getListSettings()) {
                if (settings.getChartType() == ChartType.HEAT_MAP || settings.getChartType() == ChartType.BUBBLE || settings.getChartType() == ChartType.BAR) {
                    Platform.runLater(this::formatCharts);
                }
            }
        }
    }

    @Override
    public String getClassName() {
        return PLUGIN_NAME;
    }

    @Override
    public void setHasFocus() {

        if (firstStart) {
            firstStart = false;

            VBox vBox = new VBox();
            vBox.setAlignment(Pos.CENTER);
            vBox.setSpacing(10);

            String style =
//                    "-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);" +
                    "    -fx-background-insets: 0,1,4,5;\n" +
                            "    -fx-background-radius: 9,8,5,4;\n" +
                            "    -fx-padding: 15 30 15 30;\n" +
                            "    -fx-font-family: \"Cambria\";\n" +
                            "    -fx-font-size: 32px;\n" +
                            "    -fx-text-alignment: left;\n";
//                    "    -fx-text-fill: #0076a3;\n";

            Button newAnalysis = new Button(I18n.getInstance().getString("plugin.graph.analysis.new"), JEConfig.getImage("Data.png", 32, 32));
            newAnalysis.setStyle(style);
            newAnalysis.setAlignment(Pos.CENTER);

            Button loadAnalysis = new Button(I18n.getInstance().getString("plugin.graph.analysis.load"), JEConfig.getImage("1390343812_folder-open.png", 32, 32));
            loadAnalysis.setStyle(style);
            loadAnalysis.setAlignment(Pos.CENTER);

            newAnalysis.setOnAction(event -> newAnalysis());

            loadAnalysis.setOnAction(event -> openDialog());

            Region top = new Region();

            vBox.getChildren().setAll(top, loadAnalysis, newAnalysis);

            this.sp.setContent(vBox);

            Platform.runLater(() -> top.setPrefHeight(border.getHeight() / 3));
        }
    }

    private void openDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(ds, dataModel);
        toolBarView.setDisableToolBarIcons(false);

        dialog.show();

        if (dialog.getResponse() == Response.NEW) {

            newAnalysis();

        } else if (dialog.getResponse() == Response.LOAD) {
            final Preferences previewPref = Preferences.userRoot().node("JEVis.JEConfig.preview");
            if (!previewPref.getBoolean("enabled", true)) {
                dataModel.setAnalysisTimeFrameForAllModels(dataModel.getGlobalAnalysisTimeFrame());
            }

//            dataModel.setGlobalAnalysisTimeFrame(dataModel.getGlobalAnalysisTimeFrame());
//            dataModel.updateSamples();
//            dataModel.setCharts(dataModel.getCharts());
//            dataModel.setSelectedData(dataModel.getSelectedData());
        }
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String value) {
        name.set(value);
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String getUUID() {
        return id.get();
    }

    @Override
    public void setUUID(String newid) {
        id.set(newid);
    }

    @Override
    public String getToolTip() {
        return tooltip;
    }

    @Override
    public StringProperty uuidProperty() {
        return id;
    }

    private void newAnalysis() {

        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, dataModel);

//        AnalysisTimeFrame atf = new AnalysisTimeFrame();
//        atf.setActiveTimeFrame(TimeFrame.CUSTOM);
//
//        dataModel.setAnalysisTimeFrame(atf);

        if (selectionDialog.show() == Response.OK) {
            toolBarView.setDisableToolBarIcons(false);

            dataModel.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
            dataModel.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());
        }
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {
        if (toolBar == null) {
            toolBar = toolBarView.getToolbar(getDataSource());
        }
        return toolBar;
    }

    @Override
    public void updateToolbar() {
        toolBarView.updateLayout();
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {
        this.ds = ds;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        switch (cmdType) {
            case Constants.Plugin.Command.SAVE:
                return true;
            case Constants.Plugin.Command.DELETE:
                return true;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return true;
            case Constants.Plugin.Command.RELOAD:
                return true;
            case Constants.Plugin.Command.ADD_TABLE:
                return false;
            case Constants.Plugin.Command.EDIT_TABLE:
                return false;
            case Constants.Plugin.Command.CREATE_WIZARD:
                return false;
            case Constants.Plugin.Command.FIND_OBJECT:
                return false;
            case Constants.Plugin.Command.PASTE:
                return false;
            case Constants.Plugin.Command.COPY:
                return false;
            case Constants.Plugin.Command.CUT:
                return false;
            case Constants.Plugin.Command.FIND_AGAIN:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void handleRequest(int cmdType) {
        try {
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    new SaveAnalysisDialog(ds, dataModel, toolBarView);
                    break;
                case Constants.Plugin.Command.DELETE:
                    new DeleteAnalysisDialog(ds, dataModel, toolBarView.getListAnalysesComboBox());
                    break;
                case Constants.Plugin.Command.EXPAND:
                    break;
                case Constants.Plugin.Command.NEW:
                    new NewAnalysisDialog(ds, dataModel, this, toolBarView.getChanged());
                    break;
                case Constants.Plugin.Command.RELOAD:
                    JEVisObject currentAnalysis = dataModel.getCurrentAnalysis();
                    ManipulationMode currentManipulationMode = dataModel.getManipulationMode();
                    AggregationPeriod currentAggregationPeriod = dataModel.getAggregationPeriod();
                    AnalysisTimeFrame currentTimeframe = dataModel.getGlobalAnalysisTimeFrame();
                    dataModel.setCurrentAnalysis(null);
                    dataModel.setCurrentAnalysis(currentAnalysis);
                    dataModel.setCharts(new ChartSettings());
                    dataModel.updateSelectedData();

                    dataModel.setManipulationMode(currentManipulationMode);
                    dataModel.setAggregationPeriod(currentAggregationPeriod);
                    dataModel.isGlobalAnalysisTimeFrame(true);
                    dataModel.setAnalysisTimeFrameForAllModels(currentTimeframe);
                    break;
                case Constants.Plugin.Command.ADD_TABLE:
                    break;
                case Constants.Plugin.Command.EDIT_TABLE:
                    break;
                case Constants.Plugin.Command.CREATE_WIZARD:
                    break;
                case Constants.Plugin.Command.FIND_OBJECT:
                    break;
                case Constants.Plugin.Command.PASTE:
                    break;
                case Constants.Plugin.Command.COPY:
                    break;
                case Constants.Plugin.Command.CUT:
                    break;
                case Constants.Plugin.Command.FIND_AGAIN:
                    break;
            }
        } catch (Exception ex) {
        }

    }


    @Override
    public void fireCloseEvent() {
    }

    @Override
    public ImageView getIcon() {
        return JEConfig.getImage("1415314386_Graph.png", 20, 20);
    }

    @Override
    public Node getContentNode() {
        return border;
    }

    public void update() {

        try {
            double totalJob = dataModel.getCharts().getListSettings().size();

            JEConfig.getStatusBar().startProgressJob(GraphPluginView.JOB_NAME, totalJob, I18n.getInstance().getString("plugin.graph.message.startupdate"));
        } catch (Exception ex) {

        }

        allCharts.clear();
        zoomed = false;
        setxAxisLowerBound(null);
        setxAxisUpperBound(null);

        Platform.runLater(() -> {
            vBox.getChildren().clear();
            sp.setContent(vBox);
            try {
                tp.hide();
            } catch (Exception ignored) {
            }
        });

        Long horizontalPies = dataModel.getHorizontalPies();
        Long horizontalTables = dataModel.getHorizontalTables();
        int countOfPies = (int) dataModel.getCharts().getListSettings().stream().filter(charts -> charts.getChartType() == ChartType.PIE).count();
        int countOfTables = (int) dataModel.getCharts().getListSettings().stream().filter(charts -> charts.getChartType() == ChartType.TABLE).count();

//        Platform.runLater(() -> {

        AtomicDouble autoMinSize = new AtomicDouble(0);
        double autoMinSizeNormal = 220;
        double autoMinSizeLogical = 50;

        if (dataModel.getSelectedData() != null) {

            List<HBox> pieFrames = new ArrayList<>();
            List<HBox> tableFrames = new ArrayList<>();

            AlphanumComparator ac = new AlphanumComparator();
            try {
                dataModel.getCharts().getListSettings().sort((s1, s2) -> ac.compare(s1.getName(), s2.getName()));
            } catch (Exception e) {
            }

            int noOfPie = 0;
            int noOfTable = 0;
            int currentPieFrame = 0;
            int currentTableFrame = 0;

            for (ChartSetting chartSetting : dataModel.getCharts().getListSettings()) {
                if (chartSetting.getChartType().equals(ChartType.LOGICAL)) {
                    autoMinSize.set(autoMinSizeLogical);
                } else {
                    autoMinSize.set(autoMinSizeNormal);
                }

                BorderPane bp = new BorderPane();
//                bp.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
                bp.setBorder(new Border(new BorderStroke(Color.TRANSPARENT,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));

//                    bp.setMinHeight(autoMinSize.get());

//                bp.setMaxWidth(sp.getMaxWidth() - 4);

                Chart chart = null;
                if (chartSetting.getChartType() != ChartType.LOGICAL) {
                    chart = getChart(chartSetting, null);
                    allCharts.put(chartSetting.getId(), chart);
                }

                if (chartSetting.getChartType() != ChartType.TABLE && chartSetting.getChartType() != ChartType.TABLE_V) {
                    switch (chartSetting.getChartType()) {
                        case BAR:
                        case PIE:
                        case HEAT_MAP:
//                        case BUBBLE:
                            if (chart != null) {
                                bp.setCenter(chart.getRegion());
                                if (!dataModel.getCharts().getAutoSize()) {
                                    bp.setPrefHeight(chartSetting.getHeight());
                                }
                            }
                            break;
                        case LOGICAL:
                            createLogicalCharts(bp, chartSetting);
                            break;
                        default:
                            if (chart != null) {
                                bp.setCenter(chart.getChart());
                                if (!dataModel.getCharts().getAutoSize()) {
                                    bp.setPrefHeight(chartSetting.getHeight());
                                }
                            }
                            break;
                    }
                } else if (chart != null) {
                    ScrollPane scrollPane = new ScrollPane();

                    TableHeader tableHeader = new TableHeader(chartSetting, chart.getTableData());
                    tableHeader.maxWidthProperty().bind(bp.widthProperty());

                    scrollPane.setContent(tableHeader);
                    scrollPane.setFitToHeight(true);
                    scrollPane.setFitToWidth(true);
                    scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    bp.setCenter(scrollPane);
                }

                if (chartSetting.getChartType() != ChartType.PIE && chartSetting.getChartType() != ChartType.HEAT_MAP
                        && chartSetting.getChartType() != ChartType.LOGICAL && chart != null) {
                    TableHeader tableHeader = new TableHeader(chartSetting, chart.getTableData());
                    tableHeader.maxWidthProperty().bind(bp.widthProperty());

                    if (chartSetting.getChartType() != ChartType.TABLE && chartSetting.getChartType() != ChartType.TABLE_V) {
                        bp.setTop(tableHeader);
                    } else if (chartSetting.getChartType() == ChartType.TABLE) {
                        TableChart tableChart = (TableChart) chart;
                        bp.setTop(tableChart.getTopPicker());

                    } else if (chartSetting.getChartType() == ChartType.TABLE_V) {
                        TableChartV tableChart = (TableChartV) chart;

                        Label titleLabel = new Label(chartSetting.getName());
                        titleLabel.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;");
                        titleLabel.setAlignment(Pos.CENTER);
                        HBox hBox = new HBox(titleLabel);
                        hBox.setAlignment(Pos.CENTER);

                        TableHeaderTable tableHeaderTable = new TableHeaderTable(tableChart.getXyChartSerieList());
                        tableChart.setTableHeader(tableHeaderTable);
                        tableHeaderTable.maxWidthProperty().bind(bp.widthProperty());

                        VBox vBox = new VBox(hBox, tableHeaderTable);
                        VBox.setVgrow(hBox, Priority.NEVER);
                        VBox.setVgrow(tableHeaderTable, Priority.ALWAYS);
                        bp.setCenter(vBox);
                    }
                } else if (chartSetting.getChartType() != ChartType.LOGICAL) {
                    bp.setTop(null);
                }

                bp.setBottom(null);

                DragResizerXY.makeResizable(bp);

                bp.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.equals(oldValue)) {
                        try {
                            chartSetting.setHeight(newValue.doubleValue());
                        } catch (Exception e) {
                        }
                    }
                });

                Separator sep = new Separator();
                sep.setOrientation(Orientation.HORIZONTAL);
                if (chartSetting.getChartType() == ChartType.PIE) {

                    if (pieFrames.isEmpty()) {
                        HBox hBox = new HBox();
                        hBox.setFillHeight(true);
                        pieFrames.add(hBox);
                    }

                    HBox hBox = null;
                    if (currentPieFrame < pieFrames.size()) {
                        hBox = pieFrames.get(currentPieFrame);
                    } else {
                        hBox = new HBox();
                        hBox.setFillHeight(true);
                        pieFrames.add(hBox);
                    }
                    hBox.getChildren().add(bp);
                    HBox.setHgrow(bp, Priority.ALWAYS);
                    noOfPie++;

                    if (noOfPie == horizontalPies || noOfPie == countOfPies) {
                        int finalCurrentPieFrame = currentPieFrame;
                        Platform.runLater(() -> {
                            vBox.getChildren().add(pieFrames.get(finalCurrentPieFrame));
                            vBox.getChildren().add(sep);
                        });
                        currentPieFrame++;
                    }
                } else if (chartSetting.getChartType() == ChartType.TABLE) {

                    if (tableFrames.isEmpty()) {
                        HBox hBox = new HBox();
                        hBox.setFillHeight(true);
                        tableFrames.add(hBox);
                    }

                    HBox hBox;
                    if (currentTableFrame < tableFrames.size()) {
                        hBox = tableFrames.get(currentTableFrame);
                    } else {
                        hBox = new HBox();
                        hBox.setFillHeight(true);
                        tableFrames.add(hBox);
                    }
                    hBox.getChildren().add(bp);
                    HBox.setHgrow(bp, Priority.ALWAYS);
                    noOfTable++;

                    if (noOfTable == horizontalTables || noOfTable == countOfTables) {
                        int finalCurrentTableFrame = currentTableFrame;
                        Platform.runLater(() -> {
                            vBox.getChildren().add(tableFrames.get(finalCurrentTableFrame));
                            vBox.getChildren().add(sep);
                        });

                        currentTableFrame++;
                    }
                } else {
                    Platform.runLater(() -> {
                        vBox.getChildren().add(bp);
                        vBox.getChildren().add(sep);
                    });
                }

                JEConfig.getStatusBar().progressProgressJob(GraphPluginView.JOB_NAME, 1, I18n.getInstance().getString("plugin.graph.message.finishedchart") + " ");
            }
        }

        for (ChartSetting chartSetting : dataModel.getCharts().getListSettings()) {
            int chartId = chartSetting.getId();
            Chart chart = allCharts.get(chartId);
            if (chart instanceof XYChart) {
                XYChart xyChart = (XYChart) chart;
                if (xyChart.getChartType() != ChartType.LOGICAL) {
                    List<ChartDataRow> dataRows = new ArrayList<>();
                    for (ChartDataRow singleRow : dataModel.getSelectedData()) {
                        for (int i : singleRow.getSelectedcharts()) {
                            if (i == chartId) {
                                dataRows.add(singleRow);
                            }
                        }
                    }

                    xyChart.createChart(dataModel, dataRows, chartSetting);
                }
            }
        }

        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    finalUpdates();
                } catch (Exception e) {
                    failed();
                } finally {
                    succeeded();
                }

                return null;
            }
        };


        JEConfig.getStatusBar().addTask(GraphPluginView.class.getName(), task, taskImage, true);
//        });
    }

    private void finalUpdates() throws InterruptedException {
        AtomicBoolean hasActiveChartTasks = new AtomicBoolean(false);
        ConcurrentHashMap<Task, String> taskList = JEConfig.getStatusBar().getTaskList();
        for (Map.Entry<Task, String> entry : taskList.entrySet()) {
            String s = entry.getValue();
            if (s.equals(XYChart.class.getName())) {
                hasActiveChartTasks.set(true);
                break;
            }
        }
        if (!hasActiveChartTasks.get()) {
            Platform.runLater(() -> {
                toolBarView.updateLayout();

                StringBuilder allFormulas = new StringBuilder();
                for (Map.Entry<Integer, Chart> entry : allCharts.entrySet()) {
                    List<Chart> notActive = new ArrayList<>(allCharts.values());
                    notActive.remove(entry.getValue());
                    ChartType chartType = entry.getValue().getChartType();

                    setupListener(entry.getValue(), notActive, chartType);

                    if (entry.getValue() instanceof XYChart && dataModel.calcRegression()) {
                        allFormulas.append(((XYChart) entry.getValue()).getRegressionFormula().toString());
                    }
                }

                Platform.runLater(this::autoSize);

                if (dataModel.calcRegression()) {
                    Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                    infoBox.setResizable(true);
                    infoBox.setTitle(I18n.getInstance().getString("dialog.regression.title"));
                    infoBox.setHeaderText(I18n.getInstance().getString("dialog.regression.headertext"));
                    TextArea textArea = new TextArea(allFormulas.toString());
                    textArea.setWrapText(true);
                    textArea.setPrefWidth(450);
                    textArea.setPrefHeight(200);
                    infoBox.getDialogPane().setContent(textArea);
                    infoBox.show();
                }

                Platform.runLater(() -> JEConfig.getStatusBar().finishProgressJob(GraphPluginView.class.getName(), ""));
                Platform.runLater(() -> JEConfig.getStatusBar().getPopup().hide());
            });
        } else {
            Thread.sleep(500);
            finalUpdates();
        }
    }

    private Chart getChart(ChartSetting chart, List<ChartDataRow> chartDataRows) {

        if (chartDataRows == null) {
            chartDataRows = new ArrayList<>();

            for (ChartDataRow singleRow : dataModel.getSelectedData()) {
                for (int i : singleRow.getSelectedcharts()) {
                    if (i == chart.getId()) {
                        chartDataRows.add(singleRow);
                    }
                }
            }
        }

        switch (chart.getChartType()) {
            case LOGICAL:
                return new LogicalChart();
            default:
            case LINE:
                return new LineChart();
            case BAR:
                return new BarChart(dataModel, chartDataRows, chart);
            case COLUMN:
                return new ColumnChart();
            case BUBBLE:
                return new BubbleChart();
            case SCATTER:
                return new ScatterChart();
            case PIE:
                return new PieChart(dataModel, chartDataRows, chart);
            case TABLE:
                return new TableChart();
            case TABLE_V:
                return new TableChartV();
            case HEAT_MAP:
                return new HeatMapChart(chartDataRows, chart);
            case AREA:
                return new AreaChart();
        }
    }

    private void formatCharts() {
        for (Map.Entry<Integer, Chart> entry : allCharts.entrySet()) {
            if (entry.getValue().getChartType().equals(ChartType.HEAT_MAP)) {
                ScrollPane sp = (ScrollPane) entry.getValue().getRegion();
                VBox spVer = (VBox) sp.getContent();
                MatrixPane<MatrixChartItem> matrixHeatMap = null;
                for (Node node : spVer.getChildren()) {
                    if (node instanceof HBox) {
                        HBox spHor = (HBox) node;
                        matrixHeatMap = spHor.getChildren().stream().filter(node1 -> node1 instanceof MatrixPane).findFirst().map(node1 -> (MatrixPane<MatrixChartItem>) node1).orElse(matrixHeatMap);
                    }
                }

                if (matrixHeatMap != null) {
                    HeatMapChart chart = (HeatMapChart) entry.getValue();

                    double pixelHeight = matrixHeatMap.getMatrix().getPixelHeight();
                    double pixelWidth = matrixHeatMap.getMatrix().getPixelWidth();
                    double spacerSizeFactor = matrixHeatMap.getMatrix().getSpacerSizeFactor();
                    double width = matrixHeatMap.getMatrix().getWidth() - matrixHeatMap.getMatrix().getInsets().getLeft() - matrixHeatMap.getMatrix().getInsets().getRight();
                    double height = matrixHeatMap.getMatrix().getHeight() - matrixHeatMap.getMatrix().getInsets().getTop() - matrixHeatMap.getMatrix().getInsets().getBottom();
                    double pixelSize = Math.min((width / chart.getCOLS()), (height / chart.getROWS()));
                    double spacer = pixelSize * spacerSizeFactor;

                    double leftAxisWidth = 0;
                    double rightAxisWidth = 0;
                    int bottomAxisIndex = 0;
                    Canvas bottomXAxis = null;

                    for (Node node : spVer.getChildren()) {
                        if (node instanceof HBox) {
                            HBox spHor = (HBox) node;
                            boolean isLeftAxis = true;
                            for (Node node1 : spHor.getChildren()) {
                                if (node1 instanceof GridPane) {
                                    GridPane axis = (GridPane) node1;

                                    for (Node node2 : axis.getChildren()) {
                                        if (node2 instanceof Label) {
                                            boolean isOk = false;
                                            double newHeight = pixelHeight - 2;
                                            Font font = ((Label) node2).getFont();
                                            if (newHeight < 13) {
                                                final Label test = new Label(((Label) node2).getText());
                                                test.setFont(font);
                                                while (!isOk) {
                                                    double height1 = test.getLayoutBounds().getHeight();
                                                    if (height1 > pixelHeight - 2) {
                                                        newHeight = newHeight - 0.05;
                                                        test.setFont(new Font(font.getName(), newHeight));
                                                    } else {
                                                        isOk = true;
                                                    }
                                                }
                                            }

                                            if (newHeight < 12) {
                                                ((Label) node2).setFont(new Font(font.getName(), newHeight));
                                            }

                                            ((Label) node2).setPrefHeight(pixelHeight);

                                            final Label test = new Label(((Label) node2).getText());
                                            test.setFont(((Label) node2).getFont());
                                            double newWidth = test.getLayoutBounds().getWidth();

                                            if (isLeftAxis) {
                                                leftAxisWidth = Math.max(newWidth, axis.getLayoutBounds().getWidth());
                                                isLeftAxis = false;
                                            } else {
                                                rightAxisWidth = Math.max(newWidth, axis.getLayoutBounds().getWidth());
                                            }
                                        }
                                    }
                                }
                            }

                        } else if (node instanceof GridPane || node instanceof Canvas) {

                            List<DateTime> xAxisList = chart.getxAxisList();
                            String X_FORMAT = chart.getX_FORMAT();

                            bottomXAxis = new Canvas(leftAxisWidth + width + rightAxisWidth, 30);
                            GraphicsContext gc = bottomXAxis.getGraphicsContext2D();
                            double x = leftAxisWidth + 4 + spacer + pixelWidth / 2;

                            for (DateTime dateTime : xAxisList) {
                                String ts = dateTime.toString(X_FORMAT);
                                Text text = new Text(ts);
                                Font helvetica = Font.font("Helvetica", 12);
                                text.setFont(helvetica);

                                final double textWidth = text.getLayoutBounds().getWidth();
                                final double textHeight = text.getLayoutBounds().getHeight();

                                gc.setFont(helvetica);

                                if (dateTime.getMinuteOfHour() == 0) {

                                    gc.fillRect(x, 0, 2, 10);
                                    gc.fillText(ts, x - textWidth / 2, 10 + textHeight + 2);

                                } else if (dateTime.getMinuteOfHour() % 15 == 0) {
                                    gc.fillRect(x, 0, 1, 7);
                                }

                                x += pixelWidth;
                            }

                            bottomAxisIndex = spVer.getChildren().indexOf(node);
                        }
                    }

                    if (bottomXAxis != null) {
                        spVer.getChildren().set(bottomAxisIndex, bottomXAxis);
                    }

                    MatrixPane<MatrixChartItem> finalMatrixHeatMap = matrixHeatMap;
                    matrixHeatMap.setOnMouseMoved(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent t) {
                            Node node = (Node) t.getSource();
                            NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
                            nf.setMinimumFractionDigits(2);
                            nf.setMaximumFractionDigits(2);
                            for (Node node1 : finalMatrixHeatMap.getMatrix().getChildren()) {
                                if (node1 instanceof Canvas) {
                                    Canvas canvas = (Canvas) node1;
                                    // listen to only events within the canvas
                                    final Point2D mouseLoc = new Point2D(t.getScreenX(), t.getScreenY());
                                    final Bounds screenBounds = canvas.localToScreen(canvas.getBoundsInLocal());

                                    double pixelHeight = finalMatrixHeatMap.getMatrix().getPixelHeight();
                                    double pixelWidth = finalMatrixHeatMap.getMatrix().getPixelWidth();
                                    double spacerSizeFactor = finalMatrixHeatMap.getMatrix().getSpacerSizeFactor();
                                    double width = finalMatrixHeatMap.getMatrix().getWidth() - finalMatrixHeatMap.getMatrix().getInsets().getLeft() - finalMatrixHeatMap.getMatrix().getInsets().getRight();
                                    double height = finalMatrixHeatMap.getMatrix().getHeight() - finalMatrixHeatMap.getMatrix().getInsets().getTop() - finalMatrixHeatMap.getMatrix().getInsets().getBottom();
                                    double pixelSize = Math.min((width / chart.getCOLS()), (height / chart.getROWS()));
                                    double spacer = pixelSize * spacerSizeFactor;
                                    double pixelWidthMinusDoubleSpacer = pixelWidth - spacer * 2;
                                    double pixelHeightMinusDoubleSpacer = pixelHeight - spacer * 2;

                                    double spacerPlusPixelWidthMinusDoubleSpacer = spacer + pixelWidthMinusDoubleSpacer;
                                    double spacerPlusPixelHeightMinusDoubleSpacer = spacer + pixelHeightMinusDoubleSpacer;

                                    if (screenBounds.contains(mouseLoc)) {
                                        for (int y = 0; y < chart.getROWS(); y++) {
                                            for (int x = 0; x < chart.getCOLS(); x++) {
                                                if (Helper.isInRectangle(t.getX(), t.getY(), x * pixelWidth + spacer, y * pixelHeight + spacer, x * pixelWidth + spacerPlusPixelWidthMinusDoubleSpacer, y * pixelHeight + spacerPlusPixelHeightMinusDoubleSpacer)) {
                                                    Double value = null;
                                                    for (Map.Entry<MatrixXY, Double> entry : chart.getMatrixData().entrySet()) {
                                                        MatrixXY matrixXY = entry.getKey();
                                                        if (matrixXY.getY() == y && matrixXY.getX() == x) {
                                                            value = entry.getValue();
                                                            break;
                                                        }
                                                    }

                                                    if (value != null) {
                                                        Double finalValue = value;
                                                        Platform.runLater(() -> {
                                                            try {
                                                                tp.setText(nf.format(finalValue) + " " + chart.getUnit());
                                                                tp.show(node, finalMatrixHeatMap.getScene().getWindow().getX() + t.getSceneX(), finalMatrixHeatMap.getScene().getWindow().getY() + t.getSceneY());
                                                            } catch (NullPointerException np) {
                                                                logger.warn(np);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Platform.runLater(() -> tp.hide());
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private void autoSize(Double autoMinSize, double maxHeight, Long chartsPerScreen, VBox vBox) {
        double totalPrefHeight; /**
         * If auto size is on or if its only one chart scale the chart to maximize screen size
         */
        int noOfCharts = dataModel.getCharts().getListSettings().size();

        if (noOfCharts == 1 || dataModel.getAutoResize()) {
            /**
             * If all children take more space then the maximum available size
             * set all on min size. after this the free space will be reallocate
             */
            totalPrefHeight = calculationTotalPrefSize(vBox);

            if (chartsPerScreen != null && noOfCharts > 1) {
                ObservableList<Node> children = vBox.getChildren();

                double height = border.getHeight() - (8.5 * noOfCharts);

                for (Node node : children) {
                    if (node instanceof BorderPane) {
                        BorderPane borderPane = (BorderPane) node;
                        boolean isLogical = false;

                        ObservableList<Node> borderChildren = borderPane.getChildren();
                        for (Node node1 : borderChildren) {
                            if (node1 instanceof VBox) {
                                isLogical = true;

                                for (Node node2 : ((VBox) node1).getChildren()) {
                                    if (node2 instanceof de.gsi.chart.XYChart) {
                                        Platform.runLater(() -> ((de.gsi.chart.XYChart) node2).setMaxHeight((height) / (chartsPerScreen * 3)));
                                    }
                                }

                                break;
                            }
                        }

                        if (!isLogical) {
                            TableHeader top = (TableHeader) borderPane.getTop();
                            double heightTop = top.getHeight();

                            if (top.getItems().size() == 0) {
                                Chart chart = allCharts.get(top.getChartId());
                                //30 -> captions, borders and stuff, 25 -> per row
                                heightTop = 30 + (25 * chart.getChartDataRows().size());
                            }

                            for (Node child : borderChildren) {
                                if (child instanceof de.gsi.chart.XYChart) {
                                    de.gsi.chart.XYChart xyChart = (de.gsi.chart.XYChart) child;
                                    double v = (height / chartsPerScreen) - heightTop;
                                    Platform.runLater(() -> xyChart.setPrefHeight(v));
                                }
                            }
                        }
                    } else if (node instanceof HBox) {
                        Platform.runLater(() -> ((HBox) node).setPrefHeight(height / chartsPerScreen));
                    }
                }
            } else {
                if (totalPrefHeight > maxHeight) {
                    for (Node node : vBox.getChildren()) {
                        if (node instanceof BorderPane) {
                            Platform.runLater(() -> ((BorderPane) node).setPrefHeight(autoMinSize));
                        } else if (node instanceof HBox) {
                            Platform.runLater(() -> ((HBox) node).setPrefHeight(autoMinSize));
                        }
                    }
                }

                /**
                 * Recalculate total prefsize
                 */
                totalPrefHeight = calculationTotalPrefSize(vBox);

                /**
                 * Reallocate free space equal to all children
                 */
                if (totalPrefHeight < maxHeight) {
                    /** size/2 because there is an separator for every chart **/
                    final double freeSpacePart = (maxHeight - totalPrefHeight) / (vBox.getChildren().size() / 2);
                    for (Node node : vBox.getChildren()) {
                        if (node instanceof Pane) {
                            Platform.runLater(() -> ((Pane) node).setPrefHeight(((Pane) node).getPrefHeight() + freeSpacePart));
                        }
                    }
                }
            }
        }
//        }

        Platform.runLater(vBox::toFront);
    }

    @Override
    public void openObject(Object object) {
        try {
            firstStart = false;
            if (object instanceof AnalysisRequest) {

                /**
                 * clear old model
                 */
                dataModel.setCharts(new ChartSettings());
                dataModel.setData(new HashSet<>());

                AnalysisRequest analysisRequest = (AnalysisRequest) object;
                JEVisObject jeVisObject = analysisRequest.getObject();
                if (jeVisObject.getJEVisClassName().equals("Analysis")) {

                    dataModel.setCurrentAnalysis(analysisRequest.getObject());
                    dataModel.setCharts(new ChartSettings());
                    dataModel.updateSelectedData();

                    dataModel.setManipulationMode(analysisRequest.getManipulationMode());
                    dataModel.setAggregationPeriod(analysisRequest.getAggregationPeriod());
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    analysisTimeFrame.setStart(analysisRequest.getStartDate());
                    analysisTimeFrame.setEnd(analysisRequest.getEndDate());

                    dataModel.isGlobalAnalysisTimeFrame(true);
                    dataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                    Platform.runLater(() -> {
                        toolBarView.setChanged(false);
                        dataModel.setChanged(false);
                    });

                } else if (jeVisObject.getJEVisClassName().equals("Data") || jeVisObject.getJEVisClassName().equals("Clean Data")
                        || jeVisObject.getJEVisClassName().equals("Math Data") || jeVisObject.getJEVisClassName().equals("Forecast Data")) {

                    JEVisObject buildingObject = CommonMethods.getFirstParentalObjectOfClass(jeVisObject, "Building");
                    JEVisObject analysisDir = null;
                    if (buildingObject != null) {
                        for (JEVisObject child : buildingObject.getChildren()) {
                            if (child.getJEVisClassName().equals("Analyses Directory")) {
                                analysisDir = child;
                                break;
                            }
                        }
                    }

                    ChartDataRow chartDataRow = new ChartDataRow(ds);

                    try {
                        if (jeVisObject.getJEVisClassName().equals("Data")) {
                            chartDataRow.setObject(jeVisObject);
                        } else if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                            chartDataRow.setDataProcessor(jeVisObject);
                            chartDataRow.setObject(jeVisObject.getParents().get(0));
                        }
                    } catch (JEVisException e) {

                    }

                    List<Integer> list = new ArrayList<>();
                    list.add(0);
                    chartDataRow.setSelectedCharts(list);
                    chartDataRow.setColor(ColorHelper.toRGBCode(Color.BLUE));
                    chartDataRow.setSomethingChanged(true);

                    Set<ChartDataRow> chartDataRows = new HashSet<>();
                    chartDataRows.add(chartDataRow);

                    ChartSetting chartSetting = new ChartSetting(chartDataRow.getObject().getName());
                    chartSetting.setId(0);
                    chartSetting.setChartType(ChartType.AREA);
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                    analysisTimeFrame.setStart(analysisRequest.getStartDate());
                    analysisTimeFrame.setEnd(analysisRequest.getEndDate());
                    chartSetting.setAnalysisTimeFrame(analysisTimeFrame);
                    List<ChartSetting> chartSettingList = new ArrayList<>();
                    chartSettingList.add(chartSetting);

                    JEVisObject newObject = null;

                    if (analysisDir == null || !ds.getCurrentUser().canCreate(analysisDir.getID())) {
                        org.jevis.commons.ws.json.JsonObject newJsonObject = new JsonObject();
                        newJsonObject.setName("Temp");
                        newJsonObject.setId(0L);
                        newJsonObject.setJevisClass("Analysis");
                        newObject = new JEVisObjectWS((JEVisDataSourceWS) ds, newJsonObject) {
                            @Override
                            public String toString() {
                                return I18n.getInstance().getString("plugin.graph.analysis.tempanalysis");
                            }
                        };
                    } else {
                        String tempName = I18n.getInstance().getString("plugin.graph.analysis.tempanalysis");
                        for (JEVisObject child : analysisDir.getChildren()) {
                            if (child.getName().equals(tempName)) {
                                newObject = child;
                                break;
                            }
                        }

                        if (newObject == null) {
                            newObject = analysisDir.buildObject(tempName, analysisDir.getDataSource().getJEVisClass("Analysis"));
                            newObject.commit();
                        }

                        JsonChartDataModel jsonChartDataModel = SaveAnalysisDialog.getJsonChartDataModel(chartDataRows);
                        ChartSettings chartSettings = new ChartSettings();
                        chartSettings.setListSettings(chartSettingList);
                        JsonChartSettings jsonChartSettings = SaveAnalysisDialog.getJsonChartSettings(chartSettings);

                        DateTime now = DateTime.now();
                        JEVisAttribute dataModel = newObject.getAttribute("Data Model");
                        JEVisAttribute charts = newObject.getAttribute("Charts");
                        JEVisSample smp = dataModel.buildSample(now, jsonChartDataModel.toString());
                        JEVisSample smp2 = charts.buildSample(now, jsonChartSettings.toString());
                        smp.commit();
                        smp2.commit();

                        if (ds.getCurrentUser().canDelete(analysisDir.getID())) {
                            Long id = newObject.getID();
                            JEConfig.getStage().setOnCloseRequest(event -> {
                                try {
                                    ds.deleteObject(id);
                                } catch (JEVisException e) {
                                    logger.error("Could not delete temporary analysis", e);
                                }
                                Platform.exit();
                                System.exit(0);
                            });
                        }
                    }

                    dataModel.setTemporary(true);
                    dataModel.setCurrentAnalysisNOEVENT(newObject);
                    dataModel.getCharts().setListSettings(chartSettingList);
                    dataModel.setData(chartDataRows);
                    dataModel.setAggregationPeriod(analysisRequest.getAggregationPeriod());
                    dataModel.setManipulationMode(analysisRequest.getManipulationMode());
                    dataModel.isGlobalAnalysisTimeFrame(true);
                    dataModel.setGlobalAnalysisTimeFrameNOEVENT(analysisTimeFrame);
                    toolBarView.getPickerCombo().updateCellFactory();

                    if (analysisDir == null) {
                        dataModel.setAnalysisTimeFrameForAllModels(analysisTimeFrame);

                        Platform.runLater(() -> {
                            toolBarView.setChanged(false);
                            dataModel.setChanged(false);
                        });
                    } else {
                        dataModel.updateListAnalyses();
                        update();
                    }

                }

                toolBarView.setDisableToolBarIcons(false);
            }


        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public int getPrefTapPos() {
        return 2;
    }

    private void setupListener(Chart cv, List<Chart> notActive, ChartType chartType) {
        if (cv.getChart() != null) {
            switch (chartType) {
                /**
                 * Area, Line and Scatter use same listeners -> no break
                 */
                case AREA:
                case LINE:
                case SCATTER:
                case LOGICAL:
                case COLUMN:
                case BUBBLE:
                    setupNoteDialog(cv);

                    setupMouseMoved(cv, notActive);

                    setupLinkedZoom(cv, notActive);

                    break;
                case TABLE:
                    TableChart chart = (TableChart) cv;
                    TableTopDatePicker tableTopDatePicker = chart.getTableTopDatePicker();
                    ComboBox<DateTime> datePicker = tableTopDatePicker.getDatePicker();
                    ChartDataRow singleRow = chart.getSingleRow();
                    datePicker.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        if (datePicker.getSelectionModel().selectedIndexProperty().get() < singleRow.getSamples().size()
                                && datePicker.getSelectionModel().selectedIndexProperty().get() > -1 && !chart.isBlockDatePickerEvent()) {

                            cv.updateTable(null, newValue);

                            notActive.forEach(na -> {
                                if (!na.getChartType().equals(ChartType.PIE)
                                        && !na.getChartType().equals(ChartType.BAR)
                                        && !na.getChartType().equals(ChartType.BUBBLE)
                                        && !na.getChartType().equals(ChartType.COLUMN)) {
                                    na.updateTable(null, newValue);
                                }
                            });
                        }
                    });

                    tableTopDatePicker.getLeftImage().setOnMouseClicked(event -> {
                        int i = datePicker.getSelectionModel().getSelectedIndex() - 1;
                        if (i > -1) {
                            Platform.runLater(() -> datePicker.getSelectionModel().select(i));
                        }
                    });

                    tableTopDatePicker.getRightImage().setOnMouseClicked(event -> {
                        int i = datePicker.getSelectionModel().getSelectedIndex() - 1;
                        if (i < singleRow.getSamples().size()) {
                            Platform.runLater(() -> datePicker.getSelectionModel().select(i));
                        }
                    });
                    break;

                case BAR:
                    break;
                case PIE:
                    break;
                case HEAT_MAP:
                    break;
                default:
                    break;
            }
        }
    }

    private void setupNoteDialog(Chart cv) {
        XYChart xyChart = (XYChart) cv;
        cv.getChart().getPlugins().add(new DataPointNoteDialog(xyChart.getXyChartSerieList(), this));
    }

    private void setupMouseMoved(Chart cv, List<Chart> notActive) {

        DataPointTableViewPointer dataPointTableViewPointer = new DataPointTableViewPointer(cv, notActive);
        cv.getChart().getPlugins().add(dataPointTableViewPointer);
    }

    private void setupLinkedZoom(Chart ac, List<Chart> notActive) {

        MultiChartZoomer multiChartZoomer = new MultiChartZoomer(this, AxisMode.X, notActive, ac);
        multiChartZoomer.setSliderVisible(false);
        ac.getChart().getPlugins().add(multiChartZoomer);
    }

    private double calculationTotalPrefSize(Pane pane) {
        double totalPrefHight = 0;
        for (Node node : pane.getChildren()) {
            if (node instanceof Separator) {
                /** Separator has no preSize so tested a working one, the real size can only be taken after rendering **/
                totalPrefHight += 4.5;
            } else if (node instanceof Region) {
                totalPrefHight += ((Region) node).getPrefHeight();
            }
        }
        return totalPrefHight;
    }

    private void createLogicalCharts(BorderPane bp, ChartSetting chartSetting) {
        List<LogicalChart> subCharts = new ArrayList<>();
        VBox vboxSubs = new VBox();

        AlphanumComparator ac = new AlphanumComparator();
        ObservableList<TableEntry> allEntries = FXCollections.observableArrayList();

        for (ChartDataRow singleRow : dataModel.getSelectedData()) {
            for (int i : singleRow.getSelectedcharts()) {
                if (i == chartSetting.getId()) {
                    List<ChartDataRow> dataRows = Collections.singletonList(singleRow);
                    Chart subView = getChart(chartSetting, dataRows);

                    subView.getTableData().addListener((ListChangeListener<? super TableEntry>) c -> {
                        while (c.next())
                            if (c.wasAdded() && c.getAddedSize() > 0) {
                                allEntries.add(c.getAddedSubList().get(0));
                                Platform.runLater(() -> allEntries.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));
                            }
                    });

                    LogicalChart logicalChart = (LogicalChart) subView;
                    logicalChart.createChart(dataModel, dataRows, chartSetting, true);
                    subCharts.add(logicalChart);
                }
            }
        }

        subCharts.sort((o1, o2) -> ac.compare(o1.getChartDataRows().get(0).getTitle(), o2.getChartDataRows().get(0).getTitle()));
        subCharts.forEach(subChart -> vboxSubs.getChildren().add(subChart.getChart()));

        Double minValue = Double.MAX_VALUE;
        Double maxValue = -Double.MAX_VALUE;

        for (LogicalChart logicalChart : subCharts) {
            if (subCharts.indexOf(logicalChart) > 0) {
                Platform.runLater(() -> logicalChart.getChart().setTitle(null));
            }

            logicalChart.getChartDataRows().get(0).setColor(ColorHelper.toRGBCode(ColorColumn.color_list[subCharts.indexOf(logicalChart)]));
            logicalChart.getChartDataRows().get(0).calcMinAndMax();
            double min = logicalChart.getMinValue();
            double max = logicalChart.getMaxValue();
            minValue = Math.min(minValue, min);
            maxValue = Math.max(maxValue, max);
        }

        allEntries.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        TableHeader tableHeader = new TableHeader(chartSetting, allEntries);
        bp.setTop(tableHeader);

        if (!minValue.equals(Double.MAX_VALUE) && !maxValue.equals(-Double.MAX_VALUE)) {
            for (LogicalChart logicalChart : subCharts) {
                for (Axis axis : logicalChart.getChart().getAxes()) {
                    if (axis.getSide().isVertical()) {
                        axis.setAutoRanging(false);
                    } else {
                        axis.set(minValue, maxValue);
                    }
                }
            }
        }

        bp.setCenter(vboxSubs);
        subCharts.forEach(logicalChart -> allCharts.put((chartSetting.getId() * 10) + subCharts.indexOf(logicalChart), logicalChart));
    }

    public VBox getvBox() {
        return vBox;
    }

    public Double getxAxisLowerBound() {
        return xAxisLowerBound;
    }

    public Double getxAxisUpperBound() {
        return xAxisUpperBound;
    }

    public void setxAxisLowerBound(Double xAxisLowerBound) {
        this.xAxisLowerBound = xAxisLowerBound;
    }

    public void setxAxisUpperBound(Double xAxisUpperBound) {
        this.xAxisUpperBound = xAxisUpperBound;
    }

    public boolean isZoomed() {
        return zoomed;
    }

    public void setZoomed(boolean zoomed) {
        this.zoomed = zoomed;
    }

    @Override
    public void lostFocus() {

    }
}
