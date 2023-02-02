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
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
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
import javafx.concurrent.Service;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartElements.*;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DataPointNoteDialog;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.DataPointTableViewPointer;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.*;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.*;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.*;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ChartPlugin implements Plugin {

    private static final Logger logger = LogManager.getLogger(ChartPlugin.class);
    public static String PLUGIN_NAME = "Graph Plugin";
    public static String ANALYSIS_CLASS = "Analysis";
    public static String JOB_NAME = "Chart Update";

    private final DoubleProperty zoomDurationMillis = new SimpleDoubleProperty(750.0);
    private final ToolBarView toolBarView;
    private final AnalysisHandler analysisHandler = new AnalysisHandler();
    private final FavoriteAnalysisHandler favoriteAnalysisHandler = new FavoriteAnalysisHandler();
    private final DataModel dataModel;
    private final StringProperty name = new SimpleStringProperty("Graph");
    private final StringProperty id = new SimpleStringProperty("*NO_ID*");
    private final String tooltip = I18n.getInstance().getString("pluginmanager.graph.tooltip");
    private final ScrollPane sp = new ScrollPane();
    private final VBox vBox = new VBox();
    private final BorderPane border = new BorderPane(sp);
    private final StackPane dialogContainer = new StackPane(border);
    private final Tooltip tp = new Tooltip("");
    private final HashMap<Integer, Chart> allCharts = new HashMap<>();
    private final Image taskImage = JEConfig.getImage("Analysis.png");
    private final DataSettings dataSettings;
    private JEVisDataSource ds;
    private ToolBar toolBar;
    private boolean firstStart = true;
    private Double xAxisLowerBound;
    private Double xAxisUpperBound;
    private boolean zoomed = false;
    private Boolean temporary = false;
    private WorkDays workDays;
    private Long finalSeconds = 60L;
    private final Service<Void> service = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        TimeUnit.SECONDS.sleep(finalSeconds);
                        Platform.runLater(() -> {
                            handleRequest(Constants.Plugin.Command.RELOAD);
                        });

                    } catch (InterruptedException e) {
                        logger.warn("Reload Service stopped.");
                        cancelled();
                    }
                    succeeded();

                    return null;
                }
            };
        }
    };

    public ChartPlugin(JEVisDataSource ds, String newName) {
        this.dataModel = new DataModel();
        this.dataSettings = new DataSettings();
        this.dataSettings.setAnalysisTimeFrame(new AnalysisTimeFrame(ds, this, TimeFrame.TODAY));

        this.toolBarView = new ToolBarView(dataModel, ds, this);

        getToolbar();

        this.vBox.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
        this.sp.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
        this.sp.setFitToWidth(true);
        this.sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.ds = ds;
        this.name.set(newName);

        /**
         * If scene size changes and old value is not 0.0 (firsts draw) redraw
         * TODO: resizing an window manually will cause a lot of resize changes and so redraws, solve this better
         */
        border.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(0.0) && dataModel.getChartModels() != null && !dataModel.getChartModels().isEmpty()) {
                boolean hasHeatMap = false;
                for (ChartModel chartModel : dataModel.getChartModels()) {
                    if (chartModel.getChartType() == ChartType.HEAT_MAP) {
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
            if (!oldValue.equals(0.0) && dataModel.getChartModels() != null && !dataModel.getChartModels().isEmpty()) {
                boolean hasHeatMap = false;
                for (ChartModel chartModel : dataModel.getChartModels()) {
                    if (chartModel.getChartType() == ChartType.HEAT_MAP) {
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

        dataSettings.currentAnalysisProperty().addListener((observableValue, jeVisObject, t1) -> {
            if (t1 != null) {

                dataSettings.setWorkDays(new WorkDays(t1));
                analysisHandler.loadDataModel(t1, dataModel);

                update();
            }
        });


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

            JFXButton newAnalysis = new JFXButton(I18n.getInstance().getString("plugin.graph.analysis.new"), JEConfig.getSVGImage(Icon.ADD_CHART, 32, 32));
            newAnalysis.setStyle(style);
            newAnalysis.setAlignment(Pos.CENTER);

            JFXButton loadAnalysis = new JFXButton(I18n.getInstance().getString("plugin.graph.analysis.load"), JEConfig.getSVGImage(Icon.FOLDER_OPEN, 32, 32));
            loadAnalysis.setStyle(style);
            loadAnalysis.setAlignment(Pos.CENTER);

            Platform.runLater(() -> toolBarView.getAnalysesComboBox().updateListAnalyses());

            newAnalysis.setOnAction(event -> handleRequest(Constants.Plugin.Command.NEW));

            loadAnalysis.setOnAction(event -> openDialog());

            Region top = new Region();

            vBox.getChildren().setAll(top, loadAnalysis, newAnalysis);

            this.sp.setContent(vBox);

            Platform.runLater(() -> top.setPrefHeight(border.getHeight() / 3));
        }
    }

    private void autoSize() {
        Integer chartsPerScreen = dataModel.getChartsPerScreen();

        AtomicDouble autoMinSize = new AtomicDouble(0);
        double autoMinSizeNormal = 240;
        double autoMinSizeLogical = 50;

        if (!dataModel.getChartModels().isEmpty()) {
            double maxHeight = border.getHeight();

            for (ChartModel chartModel : dataModel.getChartModels()) {
                if (chartModel.getChartType().equals(ChartType.LOGICAL)) {
                    autoMinSize.set(autoMinSizeLogical);
                } else {
                    autoMinSize.set(autoMinSizeNormal);
                }
            }

            autoSize(autoMinSize.get(), maxHeight, chartsPerScreen, vBox);

            for (ChartModel chartModel : dataModel.getChartModels()) {
                if (chartModel.getChartType() == ChartType.HEAT_MAP || chartModel.getChartType() == ChartType.BUBBLE || chartModel.getChartType() == ChartType.BAR) {
                    Platform.runLater(this::formatCharts);
                }
            }
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


    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public Node getToolbar() {
        if (toolBar == null) {
            toolBar = toolBarView.getToolbar();
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

    private void openDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(dialogContainer, this, ds, toolBarView.getAnalysesComboBox().getObservableListAnalyses());

        dialog.setOnDialogClosed(event -> {
            JEVisHelp.getInstance().deactivatePluginModule();
            if (dialog.getResponse() == Response.NEW) {

                handleRequest(Constants.Plugin.Command.NEW);

            } else if (dialog.getResponse() == Response.LOAD) {
                Platform.runLater(() -> toolBarView.setDisableToolBarIcons(false));
            }

            if (dataSettings.getCurrentAnalysis() != null) {
                Platform.runLater(() -> toolBarView.setDisableToolBarIcons(false));
            } else {
                Platform.runLater(() -> toolBarView.setDisableToolBarIcons(true));
            }
        });

        Platform.runLater(() -> toolBarView.setDisableToolBarIcons(true));
        dialog.show();
        Platform.runLater(() -> dialog.getFilterInput().requestFocus());

        JEVisHelp.getInstance().registerHotKey((Stage) dialog.getScene().getWindow());
        JEVisHelp.getInstance().setActiveSubModule(LoadAnalysisDialog.class.getSimpleName());

    }


    @Override
    public void fireCloseEvent() {
    }

    @Override
    public void handleRequest(int cmdType) {
        try {
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    SaveAnalysisDialog saveAnalysisDialog = new SaveAnalysisDialog(dialogContainer, ds, dataSettings, this, toolBarView);
                    saveAnalysisDialog.show();
                    break;
                case Constants.Plugin.Command.DELETE:
                    new DeleteAnalysisDialog(ds, dataSettings, toolBarView.getAnalysesComboBox());
                    break;
                case Constants.Plugin.Command.EXPAND:
                    break;
                case Constants.Plugin.Command.NEW:
                    new NewAnalysisDialog(dialogContainer, ds, dataModel, this, toolBarView.getChanged());
                    break;
                case Constants.Plugin.Command.RELOAD:
                    try {
                        for (ChartModel chartModel : dataModel.getChartModels()) {
                            for (ChartData chartData : chartModel.getChartData()) {
                                ChartDataRow chartDataRow = new ChartDataRow(ds, chartData);
                                ds.reloadAttribute(chartDataRow.getAttribute());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error while reloading attributes", e);
                    }

                    JEVisObject currentAnalysis = dataSettings.getCurrentAnalysis();
                    ManipulationMode currentManipulationMode = dataSettings.getManipulationMode();
                    AggregationPeriod currentAggregationPeriod = dataSettings.getAggregationPeriod();
                    AnalysisTimeFrame currentTimeframe = dataSettings.getAnalysisTimeFrame();
                    currentTimeframe.updateDates();

                    toolBarView.getAnalysesComboBox().updateListAnalyses();

                    dataSettings.setManipulationMode(currentManipulationMode);
                    dataSettings.setAggregationPeriod(currentAggregationPeriod);
                    dataSettings.setAnalysisTimeFrame(currentTimeframe);

                    dataSettings.setCurrentAnalysis(null);
                    dataSettings.setCurrentAnalysis(currentAnalysis);

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
    public Node getContentNode() {
        return dialogContainer;
    }

    @Override
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.GRAPH, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    public void update() {

        try {
            double totalJob = dataModel.getChartModels().size();

            JEConfig.getStatusBar().startProgressJob(ChartPlugin.JOB_NAME, totalJob, I18n.getInstance().getString("plugin.graph.message.startupdate"));
        } catch (Exception ex) {

        }

        JEConfig.getStatusBar().getTaskList().forEach((task, s) -> {
            if (s.equals(XYChart.class.getName())) {
                task.cancel(true);
            }
        });

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

        allCharts.forEach((integer, chart) -> {
            if (chart.getChart() != null) {
                chart.getChart().getPlugins().forEach(chartPlugin -> chartPlugin.getChartChildren().clear());
                chart.getChart().getPlugins().clear();
                chart.getChart().getRenderers().clear();
                chart.getChart().getAllDatasets().clear();
                chart.setChart(null);
                chart.setRegion(null);
            }
        });

        allCharts.clear();

        Integer horizontalPies = dataModel.getHorizontalPies();
        Integer horizontalTables = dataModel.getHorizontalTables();
        int countOfPies = (int) dataModel.getChartModels().stream().filter(charts -> charts.getChartType() == ChartType.PIE).count();
        int countOfTables = (int) dataModel.getChartModels().stream().filter(charts -> charts.getChartType() == ChartType.TABLE).count();

//        Platform.runLater(() -> {

        AtomicDouble autoMinSize = new AtomicDouble(0);
        double autoMinSizeNormal = 220;
        double autoMinSizeLogical = 50;

        if (!dataModel.getChartModels().isEmpty()) {

            List<HBox> pieFrames = new ArrayList<>();
            List<HBox> tableFrames = new ArrayList<>();

            AlphanumComparator ac = new AlphanumComparator();
            try {
                dataModel.getChartModels().sort((s1, s2) -> ac.compare(s1.getChartName(), s2.getChartName()));
            } catch (Exception e) {
            }

            int noOfPie = 0;
            int noOfTable = 0;
            int currentPieFrame = 0;
            int currentTableFrame = 0;

            for (ChartModel chartModel : dataModel.getChartModels()) {
                chartModel.getChartData().forEach(chartData -> {
                    if (!chartData.isIntervalEnabled()) {
                        chartData.setIntervalStart(dataSettings.getAnalysisTimeFrame().getStart());
                        chartData.setIntervalEnd(dataSettings.getAnalysisTimeFrame().getEnd());
                    }

                    chartData.setAggregationPeriod(dataSettings.getAggregationPeriod());
                    chartData.setManipulationMode(dataSettings.getManipulationMode());
                });

                if (chartModel.getChartType().equals(ChartType.LOGICAL)) {
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
                if (chartModel.getChartType() != ChartType.LOGICAL) {
                    chart = getChart(chartModel);
                    allCharts.put(chartModel.getChartId(), chart);
                }

                if (chartModel.getChartType() != ChartType.TABLE && chartModel.getChartType() != ChartType.TABLE_V) {
                    switch (chartModel.getChartType()) {
                        case BAR:
                        case PIE:
                        case HEAT_MAP:
//                        case BUBBLE:
                            if (chart != null) {
                                bp.setCenter(chart.getRegion());
                                if (!dataModel.isAutoSize()) {
                                    bp.setPrefHeight(chartModel.getHeight());
                                }
                            }
                            break;
                        case LOGICAL:
                            createLogicalCharts(bp, chartModel);
                            break;
                        default:
                            if (chart != null) {
                                bp.setCenter(chart.getChart());
                                if (!dataModel.isAutoSize()) {
                                    bp.setPrefHeight(chartModel.getHeight());
                                }
                            }
                            break;
                    }
                } else if (chart != null) {
                    ScrollPane scrollPane = new ScrollPane();

                    TableHeader tableHeader = new TableHeader(chartModel, chart);
                    tableHeader.maxWidthProperty().bind(bp.widthProperty());

                    scrollPane.setContent(tableHeader);
                    scrollPane.setFitToHeight(true);
                    scrollPane.setFitToWidth(true);
                    scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    bp.setCenter(scrollPane);
                }

                if (chartModel.getChartType() != ChartType.PIE && chartModel.getChartType() != ChartType.HEAT_MAP
                        && chartModel.getChartType() != ChartType.LOGICAL && chart != null) {
                    TableHeader tableHeader = new TableHeader(chartModel, chart);
                    tableHeader.maxWidthProperty().bind(bp.widthProperty());

                    if (chartModel.getChartType() != ChartType.TABLE && chartModel.getChartType() != ChartType.TABLE_V) {
                        bp.setTop(tableHeader);
                    } else if (chartModel.getChartType() == ChartType.TABLE) {
                        TableChart tableChart = (TableChart) chart;
                        bp.setTop(tableChart.getTopPicker());

                    } else if (chartModel.getChartType() == ChartType.TABLE_V) {
                        TableChartV tableChart = (TableChartV) chart;

                        Label titleLabel = new Label(chartModel.getChartName());
                        titleLabel.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;");
                        titleLabel.setAlignment(Pos.CENTER);

                        Region spacer = new Region();

                        HBox hBox = new HBox(8, titleLabel, spacer, tableChart.getFilterEnabledBox());
                        hBox.setAlignment(Pos.CENTER);
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        TableHeaderTable tableHeaderTable = new TableHeaderTable(tableChart.getXyChartSerieList());
                        tableChart.setTableHeader(tableHeaderTable);
                        tableHeaderTable.maxWidthProperty().bind(bp.widthProperty());

                        VBox vBox = new VBox(hBox, tableHeaderTable);
                        VBox.setVgrow(hBox, Priority.NEVER);
                        VBox.setVgrow(tableHeaderTable, Priority.ALWAYS);
                        bp.setCenter(vBox);
                    }
                } else if (chartModel.getChartType() != ChartType.LOGICAL) {
                    bp.setTop(null);
                }

                bp.setBottom(null);

                DragResizerXY.makeResizable(bp);

                bp.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.equals(oldValue)) {
                        try {
                            chartModel.setHeight(newValue.doubleValue());
                        } catch (Exception e) {
                        }
                    }
                });

                Separator sep = new Separator();
                sep.setOrientation(Orientation.HORIZONTAL);
                if (chartModel.getChartType() == ChartType.PIE) {

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
                        Platform.runLater(() -> vBox.getChildren().addAll(
                                pieFrames.get(finalCurrentPieFrame),
                                sep));
                        currentPieFrame++;
                    }
                } else if (chartModel.getChartType() == ChartType.TABLE) {

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
                        Platform.runLater(() -> vBox.getChildren().addAll(
                                tableFrames.get(finalCurrentTableFrame),
                                sep));

                        currentTableFrame++;
                    }
                } else {
                    Platform.runLater(() -> vBox.getChildren().addAll(
                            bp,
                            sep));
                }

                JEConfig.getStatusBar().progressProgressJob(ChartPlugin.JOB_NAME, 1, I18n.getInstance().getString("plugin.graph.message.finishedchart") + " ");
            }
        }

        allCharts.forEach((key, value) -> {
            if (value instanceof XYChart) {
                XYChart xyChart = (XYChart) value;
                if (xyChart.getChartType() != ChartType.LOGICAL) {
                    xyChart.createChart(getToolBarView().getToolBarSettings(), getDataSettings());
                }
            }
        });

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


        JEConfig.getStatusBar().addTask(ChartPlugin.class.getName(), task, taskImage, true);
//        });
    }

    Map<Chart, List<ChartDataRow>> dataRowMap = new TreeMap<Chart, List<ChartDataRow>>() {
        @Override
        public Comparator<? super Chart> comparator() {
            return new ChartComparator();
        }
    };

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
                    dataRowMap.put(entry.getValue(), entry.getValue().getChartDataRows());

                    List<Chart> notActive = new ArrayList<>(allCharts.values());
                    notActive.remove(entry.getValue());
                    ChartType chartType = entry.getValue().getChartType();

                    setupListener(entry.getValue(), notActive, chartType);

                    if (entry.getValue() instanceof XYChart && toolBarView.getToolBarSettings().isCalculateRegression()) {
                        allFormulas.append(((XYChart) entry.getValue()).getRegressionFormula().toString());
                    }
                }

                Platform.runLater(this::autoSize);

                if (toolBarView.getToolBarSettings().isCalculateRegression()) {
                    Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
                    infoBox.setResizable(true);
                    infoBox.setTitle(I18n.getInstance().getString("dialog.regression.title"));
                    infoBox.setHeaderText(I18n.getInstance().getString("dialog.regression.headertext"));
                    JFXTextArea textArea = new JFXTextArea(allFormulas.toString());
                    textArea.setWrapText(true);
                    textArea.setPrefWidth(450);
                    textArea.setPrefHeight(200);
                    infoBox.getDialogPane().setContent(textArea);
                    infoBox.show();
                }

                Platform.runLater(() -> {
                    JEConfig.getStatusBar().finishProgressJob(ChartPlugin.class.getName(), "");
                    JEConfig.getStatusBar().getPopup().hide();
                });
            });
        } else {
            Thread.sleep(500);
            finalUpdates();
        }
    }

    public Map<Chart, List<ChartDataRow>> getDataRowMap() {
        return dataRowMap;
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
                                    } else if (tp.isShowing()) {
                                        Platform.runLater(tp::hide);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private Chart getChart(ChartModel chartModel) {

        switch (chartModel.getChartType()) {
            case LOGICAL:
                return new LogicalChart(ds, chartModel);
            default:
            case LINE:
                return new LineChart(ds, chartModel);
            case BAR:
                return new BarChart(ds, chartModel);
            case COLUMN:
                return new ColumnChart(ds, chartModel);
            case STACKED_COLUMN:
                return new StackedColumnChart(ds, chartModel);
            case BUBBLE:
                return new BubbleChart(ds, chartModel);
            case SCATTER:
                return new ScatterChart(ds, chartModel);
            case PIE:
                return new PieChart(ds, chartModel);
            case TABLE:
                return new TableChart(ds, chartModel);
            case TABLE_V:
                return new TableChartV(ds, chartModel);
            case HEAT_MAP:
                return new HeatMapChart(ds, chartModel);
            case AREA:
                return new AreaChart(ds, chartModel);
            case STACKED_AREA:
                return new StackedAreaChart(ds, chartModel);
        }
    }

    private void autoSize(Double autoMinSize, double maxHeight, Integer chartsPerScreen, VBox vBox) {
        double totalPrefHeight; /**
         * If auto size is on or if its only one chart scale the chart to maximize screen size
         */
        int noOfCharts = dataModel.getChartModels().size();

        Map<Node, Double> maxHeightMap = new HashMap<>();
        Map<Node, Double> prefHeightMap = new HashMap<>();

        if (noOfCharts == 1 || dataModel.isAutoSize()) {
            /**
             * If all children take more space than the maximum available size
             * set all on min size. after this the free space will be reallocated
             */
            totalPrefHeight = calculationTotalPrefSize(vBox);

            if (chartsPerScreen != null && noOfCharts > 1) {
                ObservableList<Node> children = vBox.getChildren();

                double height = border.getHeight() - (8.5 * noOfCharts);

                for (Node node : children) {
                    if (node instanceof BorderPane) {
                        BorderPane borderPane = (BorderPane) node;
                        boolean isLogical = false;
                        boolean isTableV = false;

                        ObservableList<Node> borderChildren = borderPane.getChildren();
                        for (Node node1 : borderChildren) {
                            if (node1 instanceof VBox) {
                                isLogical = true;

                                for (Node node2 : ((VBox) node1).getChildren()) {
                                    if (node2 instanceof de.gsi.chart.XYChart) {
//                                        Platform.runLater(() -> ((de.gsi.chart.XYChart) node2).setMaxHeight((height) / (chartsPerScreen * 3)));
                                        maxHeightMap.put(node2, height / (chartsPerScreen * 3));
                                    } else if (node2 instanceof HBox) {
                                        isLogical = false;
                                        isTableV = true;
                                        break;
                                    }
                                }

                                break;
                            }
                        }

                        if (!isLogical && !isTableV) {
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
//                                    Platform.runLater(() -> xyChart.setPrefHeight(v));
                                    prefHeightMap.put(xyChart, v);
                                }
                            }
                        } else if (isTableV) {
                            double v = height / chartsPerScreen;
                            for (Node node1 : borderChildren) {
                                if (node1 instanceof VBox) {
                                    VBox vBox1 = (VBox) node1;
//                                    Platform.runLater(() -> vBox1.setPrefHeight(v));
                                    prefHeightMap.put(vBox1, v);
                                }
                            }
                        }
                    } else if (node instanceof HBox) {
//                        Platform.runLater(() -> ((HBox) node).setPrefHeight(height / chartsPerScreen));
                        prefHeightMap.put(node, height / chartsPerScreen);
                    }
                }
            } else {
                if (totalPrefHeight > maxHeight) {
                    for (Node node : vBox.getChildren()) {
                        if (node instanceof BorderPane) {
//                            Platform.runLater(() -> ((BorderPane) node).setPrefHeight(autoMinSize));
                            prefHeightMap.put(node, autoMinSize);
                        } else if (node instanceof HBox) {
//                            Platform.runLater(() -> ((HBox) node).setPrefHeight(autoMinSize));
                            prefHeightMap.put(node, autoMinSize);
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
                    /** size/2 because there is a separator for every chart **/
                    final double freeSpacePart = (maxHeight - totalPrefHeight) / (vBox.getChildren().size() / 2);
                    for (Node node : vBox.getChildren()) {
                        if (node instanceof Pane) {
//                            Platform.runLater(() -> ((Pane) node).setPrefHeight(((Pane) node).getPrefHeight() + freeSpacePart));
                            prefHeightMap.put(node, ((Pane) node).getPrefHeight() + freeSpacePart);
                        }
                    }
                }
            }
        }
//        }

        Platform.runLater(() -> {
            try {
                maxHeightMap.forEach((node, aDouble) -> {
                    if (node instanceof de.gsi.chart.XYChart) {
                        de.gsi.chart.XYChart xyChart = (de.gsi.chart.XYChart) node;
                        xyChart.setMaxHeight(aDouble);
                    }
                });
                prefHeightMap.forEach((node, aDouble) -> {
                    if (node instanceof HBox) {
                        HBox hBox = (HBox) node;
                        hBox.setPrefHeight(aDouble);
                    } else if (node instanceof VBox) {
                        VBox vBox1 = (VBox) node;
                        vBox1.setPrefHeight(aDouble);
                    } else if (node instanceof BorderPane) {
                        BorderPane borderPane = (BorderPane) node;
                        borderPane.setPrefHeight(aDouble);
                    } else if (node instanceof Pane) {
                        Pane pane = (Pane) node;
                        pane.setPrefHeight(aDouble);
                    } else if (node instanceof de.gsi.chart.XYChart) {
                        de.gsi.chart.XYChart xyChart = (de.gsi.chart.XYChart) node;
                        xyChart.setPrefHeight(aDouble);
                    }
                });
                vBox.toFront();
            } catch (Exception e) {
                logger.error("Could not auto size", e);
            }
        });
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
                case STACKED_AREA:
                case LINE:
                case SCATTER:
                case LOGICAL:
                case COLUMN:
                case STACKED_COLUMN:
                case BUBBLE:
                    setupNoteDialog(cv);

                    setupMouseMoved(cv, notActive);

                    setupLinkedZoom(cv, notActive);

                    break;
                case TABLE:
                    TableChart chart = (TableChart) cv;
                    TableTopDatePicker tableTopDatePicker = chart.getTableTopDatePicker();
                    JFXComboBox<DateTime> datePicker = tableTopDatePicker.getDatePicker();
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

    @Override
    public void openObject(Object object) {
        try {
            if (firstStart) {
                Platform.runLater(() -> toolBarView.getAnalysesComboBox().updateListAnalyses());
            }

            firstStart = false;

            if (object instanceof AnalysisRequest) {

                /**
                 * clear old model
                 */
                dataModel.reset();

                AnalysisRequest analysisRequest = (AnalysisRequest) object;
                JEVisObject jeVisObject = analysisRequest.getObject();
                if (jeVisObject.getJEVisClassName().equals("Analysis")) {

                    dataSettings.setManipulationMode(analysisRequest.getManipulationMode());
                    dataSettings.setAggregationPeriod(analysisRequest.getAggregationPeriod());
                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, this, TimeFrame.CUSTOM);
                    analysisTimeFrame.setStart(analysisRequest.getStartDate());
                    analysisTimeFrame.setEnd(analysisRequest.getEndDate());
                    dataSettings.setAnalysisTimeFrame(analysisTimeFrame);

                    dataSettings.setCurrentAnalysis(analysisRequest.getObject());

                    Platform.runLater(() -> toolBarView.setChanged(false));

                } else if (jeVisObject.getJEVisClassName().equals("Data") || jeVisObject.getJEVisClassName().equals("Clean Data")
                        || jeVisObject.getJEVisClassName().equals("Math Data") || jeVisObject.getJEVisClassName().equals("Forecast Data")) {


                    ChartData chartData = new ChartData();
                    chartData.setId(jeVisObject.getID());
                    chartData.setObjectName(jeVisObject);
                    chartData.setAttributeString("Value");
                    chartData.setUnit(jeVisObject.getAttribute("Value").getDisplayUnit());
                    chartData.setColor(Color.BLUE);

                    chartData.setIntervalStart(analysisRequest.getStartDate());
                    chartData.setIntervalEnd(analysisRequest.getEndDate());

                    ChartModel chartModel = new ChartModel();
                    chartModel.setChartName(jeVisObject.getLocalName(I18n.getInstance().getLocale().getLanguage()));
                    chartModel.setChartId(0);
                    chartModel.setChartType(ChartType.AREA);
                    chartModel.getChartData().add(chartData);

                    AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(ds, this, TimeFrame.CUSTOM);
                    analysisTimeFrame.setStart(analysisRequest.getStartDate());
                    analysisTimeFrame.setEnd(analysisRequest.getEndDate());
                    dataSettings.setAnalysisTimeFrame(analysisTimeFrame);

                    dataModel.getChartModels().add(chartModel);

                    analysisHandler.saveDataModel(ds.getCurrentUser().getUserObject(), dataModel, toolBarView.getToolBarSettings(), dataSettings);

                    Platform.runLater(() -> getToolBarView().getAnalysesComboBox().updateListAnalyses());

                    dataSettings.setCurrentAnalysis(ds.getCurrentUser().getUserObject());

                    update();

                    Platform.runLater(() -> toolBarView.getPickerCombo().updateCellFactory());


                }

                Platform.runLater(() -> toolBarView.setDisableToolBarIcons(false));
            }


        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void openObject(JEVisObject object, DataSettings dataSettings) {
        try {
            if (firstStart) {
                Platform.runLater(() -> toolBarView.getAnalysesComboBox().updateListAnalyses());
            }

            firstStart = false;
            dataModel.reset();

            getDataSettings().setManipulationMode(dataSettings.getManipulationMode());
            getDataSettings().setAggregationPeriod(dataSettings.getAggregationPeriod());
            getDataSettings().setAnalysisTimeFrame(dataSettings.getAnalysisTimeFrame());

            getDataSettings().setCurrentAnalysis(null);
            getDataSettings().setCurrentAnalysis(object);

            Platform.runLater(() -> toolBarView.setChanged(false));
            Platform.runLater(() -> toolBarView.setDisableToolBarIcons(false));

        } catch (Exception ex) {
            logger.error(ex);
        }
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

    private void setupNoteDialog(Chart cv) {
        XYChart xyChart = (XYChart) cv;
        cv.getChart().getPlugins().add(new DataPointNoteDialog(xyChart.getXyChartSerieList(), this, cv));
    }

    public VBox getvBox() {
        return vBox;
    }

    public Double getxAxisLowerBound() {
        return xAxisLowerBound;
    }

    public void setxAxisLowerBound(Double xAxisLowerBound) {
        this.xAxisLowerBound = xAxisLowerBound;
    }

    public Double getxAxisUpperBound() {
        return xAxisUpperBound;
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

    public HashMap<Integer, Chart> getAllCharts() {
        return allCharts;
    }

    public StackPane getDialogContainer() {
        return dialogContainer;
    }

    public ToolBarView getToolBarView() {
        return toolBarView;
    }

    private void createLogicalCharts(BorderPane bp, ChartModel chartModel) {
        List<LogicalChart> subCharts = new ArrayList<>();
        VBox vboxSubs = new VBox();

        AlphanumComparator ac = new AlphanumComparator();
        ObservableList<TableEntry> allEntries = FXCollections.observableArrayList();

        for (ChartData chartData : chartModel.getChartData()) {
            ChartDataRow chartDataRow = new ChartDataRow(ds, chartData);

            List<ChartDataRow> dataRows = Collections.singletonList(chartDataRow);
            Chart subView = getChart(chartModel);

            subView.getTableData().addListener((ListChangeListener<? super TableEntry>) c -> {
                while (c.next())
                    if (c.wasAdded() && c.getAddedSize() > 0) {
                        allEntries.add(c.getAddedSubList().get(0));
                        Platform.runLater(() -> allEntries.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));
                    }
            });

            LogicalChart logicalChart = (LogicalChart) subView;
            logicalChart.createChart(dataRows, getToolBarView().getToolBarSettings(), getDataSettings(), true);
            subCharts.add(logicalChart);
        }

        subCharts.sort((o1, o2) -> ac.compare(o1.getChartDataRows().get(0).getName(), o2.getChartDataRows().get(0).getName()));
        subCharts.forEach(subChart -> vboxSubs.getChildren().add(subChart.getChart()));

        Double minValue = Double.MAX_VALUE;
        Double maxValue = -Double.MAX_VALUE;

        for (LogicalChart logicalChart : subCharts) {
            if (subCharts.indexOf(logicalChart) > 0) {
                Platform.runLater(() -> logicalChart.getChart().setTitle(null));
            }

            ChartDataRow chartDataRow = logicalChart.getChartDataRows().get(0);
            chartDataRow.setColor(ColorTable.color_list[subCharts.indexOf(logicalChart)]);
            chartDataRow.calcMinAndMax();
            double min = chartDataRow.getMin().getValue();
            double max = chartDataRow.getMax().getValue();
            minValue = Math.min(minValue, min);
            maxValue = Math.max(maxValue, max);
        }

        allEntries.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        TableHeader tableHeader = new TableHeader(chartModel, allEntries);
        bp.setTop(tableHeader);

        if (!minValue.equals(Double.MAX_VALUE) && !maxValue.equals(-Double.MAX_VALUE)) {
            for (LogicalChart logicalChart : subCharts) {
                logicalChart.getY1Axis().setAutoRanging(false);
                logicalChart.getY1Axis().set(minValue, maxValue);
            }
        }

        bp.setCenter(vboxSubs);
        subCharts.forEach(logicalChart -> allCharts.put(((chartModel.getChartId() + 1) * 10) + subCharts.indexOf(logicalChart), logicalChart));
    }

    public WorkDays getWorkDays() {
        return workDays;
    }

    public Boolean getTemporary() {
        return temporary;
    }

    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }

    public void setTimer() {
        if (service.isRunning()) {
            service.cancel();
            service.reset();
        }
        Period p = null;
//        for (ChartDataRow chartDataRow : getSelectedData()) {
//            List<JEVisSample> samples = chartDataRow.getSamples();
//            if (samples.size() > 0) {
//                try {
//                    p = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
//                } catch (JEVisException e) {
//                    logger.error(e);
//                }
//                break;
//            }
//        }
        if (p != null) {
            Long seconds = null;
            try {
                seconds = p.toStandardDuration().getStandardSeconds();
                seconds = seconds / 2;
            } catch (Exception e) {
                logger.error(e);
            }
            if (seconds == null) seconds = 60L;

            Alert warning = new Alert(Alert.AlertType.INFORMATION, I18n.getInstance().getString("plugin.graph.toolbar.timer.settimer")
                    + " " + seconds + " " + I18n.getInstance().getString("plugin.graph.toolbar.timer.settimer2"), ButtonType.OK);
            warning.showAndWait();

            finalSeconds = seconds;
        }

        service.start();
    }

    public void stopTimer() {
        service.cancel();
        service.reset();
    }

    public Service<Void> getService() {
        return service;
    }

    public DataSettings getDataSettings() {
        return dataSettings;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public AnalysisHandler getAnalysisHandler() {
        return analysisHandler;
    }

    public FavoriteAnalysisHandler getFavoriteAnalysisHandler() {
        return favoriteAnalysisHandler;
    }

    static class ChartComparator implements Comparator<Chart> {
        private final AlphanumComparator alphanumComparator = new AlphanumComparator();

        @Override
        public int compare(Chart o1, Chart o2) {
            return o1.getChartName().compareTo(o2.getChartName());
        }
    }
}
