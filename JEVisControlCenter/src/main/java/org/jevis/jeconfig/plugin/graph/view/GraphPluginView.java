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
package org.jevis.jeconfig.plugin.graph.view;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartElements.DateValueAxis;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.LogicalChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.Chart.Charts.TableChart;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.dialog.LoadAnalysisDialog;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPluginView implements Plugin {

    private static final Logger logger = LogManager.getLogger(GraphPluginView.class);
    public static String PLUGIN_NAME = "Graph Plugin";
    private final List<ChartView> charts = new ArrayList<>();
    private final DoubleProperty zoomDurationMillis = new SimpleDoubleProperty(750.0);
    private ToolBarView toolBarView;
    private GraphDataModel dataModel;
    //private GraphController controller;
    private StringProperty name = new SimpleStringProperty("Graph");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border = new BorderPane();
    private StackPane stackPane = new StackPane();
    private ToolBar toolBar;
    private String tooltip = I18n.getInstance().getString("pluginmanager.graph.tooltip");
    private boolean firstStart = true;
    private List<ChartView> listChartViews;

    public GraphPluginView(JEVisDataSource ds, String newname) {
        this.dataModel = new GraphDataModel(ds, this);
//        this.dataModel.addObserver(this);

        //this.controller = new GraphController(this, dataModel);
        this.toolBarView = new ToolBarView(dataModel, ds, this);
        getToolbar();
        //this.chartView = new ChartView(dataModel);

        this.ds = ds;
        this.name.set(newname);

        /**
         * If scene size changes and old value is not 0.0 (firsts draw) redraw
         * TODO: resizing an window manually will cause a lot of resize changes and so redraws, solve this better
         */
        border.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(0.0) && dataModel.getSelectedData() != null && !dataModel.getSelectedData().isEmpty()) {
                Platform.runLater(() -> update(false));
            }
        });
        border.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equals(0.0) && dataModel.getSelectedData() != null && !dataModel.getSelectedData().isEmpty()) {
                Platform.runLater(() -> update(false));
            }
        });

        border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2 + "; -fx-faint-focus-color: transparent; -fx-focus-color: transparent;");

        if (!stackPane.getChildren().contains(border)) stackPane.getChildren().add(border);
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

            String style = "-fx-background-color: linear-gradient(#e2e2e2,#eeeeee);" +
                    "    -fx-background-insets: 0,1,4,5;\n" +
                    "    -fx-background-radius: 9,8,5,4;\n" +
                    "    -fx-padding: 15 30 15 30;\n" +
                    "    -fx-font-family: \"Cambria\";\n" +
                    "    -fx-font-size: 32px;\n" +
                    "    -fx-text-alignment: left;\n" +
                    "    -fx-text-fill: #0076a3;\n";

            Button newAnalysis = new Button(I18n.getInstance().getString("plugin.graph.analysis.new"), JEConfig.getImage("Data.png", 32, 32));
            newAnalysis.setStyle(style);

            Button loadAnalysis = new Button(I18n.getInstance().getString("plugin.graph.analysis.load"), JEConfig.getImage("1390343812_folder-open.png", 32, 32));
            loadAnalysis.setStyle(style);

            vBox.getChildren().addAll(loadAnalysis, newAnalysis);

            newAnalysis.setOnAction(event -> {
                toolBarView.getPickerCombo().stopUpdateListener();
                toolBarView.getPickerCombo().stopDateListener();
                newAnalysis();
                toolBarView.getPickerCombo().startUpdateListener();
                toolBarView.getPickerCombo().startDateListener();
            });

            loadAnalysis.setOnAction(event -> {
                toolBarView.getPickerCombo().stopUpdateListener();
                toolBarView.getPickerCombo().stopDateListener();
                openDialog();
                toolBarView.getPickerCombo().startUpdateListener();
                toolBarView.getPickerCombo().startDateListener();
            });

            border.setCenter(vBox);
        }
    }

    private void openDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(ds, dataModel, toolBarView);
        toolBarView.setDisableToolBarIcons(false);

        dialog.show();

        if (dialog.getResponse() == Response.NEW) {

            newAnalysis();

        } else if (dialog.getResponse() == Response.LOAD) {

            dataModel.setGlobalAnalysisTimeFrame(dataModel.getGlobalAnalysisTimeFrame());
            dataModel.updateSamples();
            dataModel.setCharts(dataModel.getCharts());
            dataModel.setSelectedData(dataModel.getSelectedData());
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
//        atf.setTimeFrame(TimeFrame.CUSTOM);
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
                return false;
            case Constants.Plugin.Command.DELETE:
                return false;
            case Constants.Plugin.Command.EXPAND:
                return false;
            case Constants.Plugin.Command.NEW:
                return false;
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
                    break;
                case Constants.Plugin.Command.DELETE:
                    break;
                case Constants.Plugin.Command.EXPAND:
                    break;
                case Constants.Plugin.Command.NEW:
                    break;
                case Constants.Plugin.Command.RELOAD:
                    JEVisObject currentAnalysis = toolBarView.getListAnalysesComboBox().getSelectionModel().getSelectedItem();
                    toolBarView.select(null);
                    try {
                        ds.reloadAttributes();
                    } catch (JEVisException e) {
                        logger.error(e);
                    }
                    toolBarView.select(currentAnalysis);
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
        return stackPane;
    }

    public void update(Boolean getNewChartViews) {

        //border.setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));

        Double autoMinSize = null;
        double autoMinSizeNormal = 220;
        double autoMinSizeLogical = 50;

        if (dataModel.getSelectedData() != null) {

            double maxHeight = border.getHeight();
            double totalPrefHeight = 0d;
            Long chartsPerScreen = dataModel.getChartsPerScreen();

            if (getNewChartViews) {
                listChartViews = null;
                listChartViews = getChartViews();
            }

            AlphanumComparator ac = new AlphanumComparator();
            try {
                listChartViews.sort((s1, s2) -> ac.compare(s1.getChartName(), s2.getChartName()));
            } catch (Exception e) {
            }

            VBox vBox = new VBox();
            vBox.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");

            ScrollPane sp = new ScrollPane();
            sp.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
            sp.setFitToWidth(true);
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            for (ChartView cv : listChartViews) {
                final Integer index = listChartViews.indexOf(cv);
                if (cv.getChartType().equals(ChartType.LOGICAL)) {
                    autoMinSize = autoMinSizeLogical;
                } else {
                    autoMinSize = autoMinSizeNormal;
                }

                BorderPane bp = new BorderPane();
                bp.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");

                bp.setMinHeight(autoMinSize);

                bp.setMaxWidth(border.getMaxWidth());

                if (!cv.getChartType().equals(ChartType.LOGICAL) || cv.getFirstLogical()) {
                    for (ChartSettings cset : dataModel.getCharts()) {
                        if (cset.getName().equals(cv.getChartName())) {

                            /**
                             * Add offset for every data object because of the table legend
                             * Every row has about 25 pixel with the default font
                             */
                            int dataSizeOffset = 30;
                            /** Calculate maxsize based on the amount of Data **/
                            int dataSize = 0;
                            if (cv.getFirstLogical()) dataSize = 4;

                            for (ChartDataModel chartDataModel : dataModel.getSelectedData()) {
                                for (int i : chartDataModel.getSelectedcharts()) {
                                    if (i == cset.getId()) {
                                        dataSize++;
                                    }
                                }

                            }

                            bp.setMinHeight(autoMinSize);
                            bp.setPrefHeight(autoMinSize + (dataSize * dataSizeOffset));
                        }
                    }
                } else {
                    bp.setPrefHeight(autoMinSize + 70);
                }

                cv.getLegend().maxWidthProperty().bind(bp.widthProperty());

                if (cv.getShowTable()) {
                    if (!cv.getChartType().equals(ChartType.TABLE)) {
                        bp.setTop(cv.getLegend());
                    } else {
                        TableChart chart = (TableChart) cv.getChart();


                        bp.setTop(chart.getTopPicker());
                    }
                } else {
                    bp.setTop(null);
                }

                if (!cv.getChartType().equals(ChartType.TABLE)) {
                    bp.setCenter(cv.getChartRegion());
                } else {
                    ScrollPane scrollPane = new ScrollPane(cv.getLegend());
                    scrollPane.setFitToHeight(true);
                    scrollPane.setFitToWidth(true);
                    scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
                    scrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    bp.setCenter(scrollPane);
                }
                bp.setBottom(null);

                DragResizerXY.makeResizable(bp);

                bp.heightProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.equals(oldValue)) {
                        for (ChartSettings cset : dataModel.getCharts()) {
                            if (cset.getName().equals(cv.getChartName())) {
                                try {
                                    cset.setHeight(newValue.doubleValue());
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                });

                List<ChartView> notActive = FXCollections.observableArrayList(listChartViews);
                notActive.remove(cv);
                ChartType chartType = cv.getChartType();

                setupListener(cv, notActive, chartType);

                Separator sep = new Separator();
                sep.setOrientation(Orientation.HORIZONTAL);

                vBox.getChildren().add(bp);
                vBox.getChildren().add(sep);

            }

            sp.setContent(vBox);

            border.setTop(null);
            border.setCenter(sp);
            border.setBottom(null);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
            toolBarView.setBorderPane(border);


            autoSize(autoMinSize, maxHeight, chartsPerScreen, vBox);
        }
    }

    private void autoSize(Double autoMinSize, double maxHeight, Long chartsPerScreen, VBox vBox) {
        double totalPrefHeight; /**
         * If auto size is on or if its only one chart scale the chart to maximize screen size
         */

        if (!hasLogicalCharts()) {
            if (dataModel.getCharts().size() == 1 || dataModel.getAutoResize()) {
                /**
                 * If all children take more space then the maximum available size
                 * set all on min size. after this the free space will be reallocate
                 */
                totalPrefHeight = calculationTotalPrefSize(vBox);

                if (chartsPerScreen != null && dataModel.getCharts().size() > 1) {
                    ObservableList<Node> children = vBox.getChildren();
                    int i = 0;
                    for (Node node : children) {
                        if (node instanceof Separator) i++;
                    }
                    double height = border.getHeight() - (4.5 * i);

                    for (Node node : children) {
                        if (node instanceof BorderPane) {
                            ((BorderPane) node).setPrefHeight((height) / chartsPerScreen);
                        }
                    }
                } else {
                    if (totalPrefHeight > maxHeight) {
                        for (Node node : vBox.getChildren()) {
                            if (node instanceof BorderPane) {
                                ((BorderPane) node).setPrefHeight(autoMinSize);
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
                        vBox.getChildren().forEach(node -> {
                            if (node instanceof Pane) {
                                ((Pane) node).setPrefHeight(((Pane) node).getPrefHeight() + freeSpacePart);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void openObject(Object object) {
        try {
            if (object instanceof AnalysisRequest) {
                AnalysisRequest analysisRequest = (AnalysisRequest) object;
                JEVisObject jeVisObject = analysisRequest.getObject();
                if (jeVisObject.getJEVisClassName().equals("Analysis")) {
                    dataModel.setCurrentAnalysis(jeVisObject);
                    toolBarView.getPickerCombo().updateCellFactory();
                    dataModel.setAggregationPeriod(analysisRequest.getAggregationPeriod());
                    dataModel.setManipulationMode(analysisRequest.getManipulationMode());
                    dataModel.setGlobalAnalysisTimeFrame(analysisRequest.getAnalysisTimeFrame());
                    dataModel.getSelectedData().forEach(chartDataModel -> {
                        chartDataModel.setSelectedStart(analysisRequest.getStartDate());
                        chartDataModel.setSelectedEnd(analysisRequest.getEndDate());
                    });
                    dataModel.isGlobalAnalysisTimeFrame(true);

                    toolBarView.removeAnalysisComboBoxListener();
                    toolBarView.getPickerCombo().stopUpdateListener();
                    toolBarView.getPickerCombo().stopDateListener();

                    toolBarView.select(analysisRequest.getObject());
                    toolBarView.getPresetDateBox().getSelectionModel().select(TimeFrame.CUSTOM);
                    DateTime startDate = analysisRequest.getStartDate();
                    DateTime endDate = analysisRequest.getEndDate();
                    toolBarView.getPickerDateStart().valueProperty().setValue(LocalDate.of(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth()));
                    toolBarView.getPickerDateEnd().valueProperty().setValue(LocalDate.of(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth()));

                    toolBarView.setupAnalysisComboBoxListener();
                    toolBarView.getPickerCombo().startUpdateListener();
                    toolBarView.getPickerCombo().startDateListener();

                    toolBarView.setDisableToolBarIcons(false);

                    dataModel.updateSamples();
                    dataModel.setCharts(dataModel.getCharts());
                    dataModel.setSelectedData(dataModel.getSelectedData());
                } else if (jeVisObject.getJEVisClassName().equals("Data") || jeVisObject.getJEVisClassName().equals("Clean Data")) {
                    ChartDataModel chartDataModel = new ChartDataModel(ds);

                    try {
                        if (jeVisObject.getJEVisClassName().equals("Data"))
                            chartDataModel.setObject(jeVisObject);
                        else if (jeVisObject.getJEVisClassName().equals("Clean Data")) {
                            chartDataModel.setDataProcessor(jeVisObject);
                            chartDataModel.setObject(jeVisObject.getParents().get(0));
                        }
                    } catch (JEVisException e) {

                    }

                    List<Integer> list = new ArrayList<>();
                    list.add(0);
                    chartDataModel.setSelectedCharts(list);
                    chartDataModel.setAttribute(analysisRequest.getAttribute());
                    chartDataModel.setColor(Color.BLUE);
                    chartDataModel.setSomethingChanged(true);

                    Set<ChartDataModel> chartDataModels = Collections.singleton(chartDataModel);

                    ChartSettings chartSettings = new ChartSettings(chartDataModel.getObject().getName());
                    chartSettings.setId(0);
                    chartSettings.setChartType(ChartType.AREA);
                    chartSettings.setAnalysisTimeFrame(new AnalysisTimeFrame(TimeFrame.TODAY));
                    List<ChartSettings> chartSettingsList = Collections.singletonList(chartSettings);

                    dataModel.setCharts(chartSettingsList);
                    dataModel.setData(chartDataModels);
                    toolBarView.getPickerCombo().updateCellFactory();

                    dataModel.setAggregationPeriod(analysisRequest.getAggregationPeriod());
                    dataModel.setManipulationMode(analysisRequest.getManipulationMode());
                    dataModel.setGlobalAnalysisTimeFrame(analysisRequest.getAnalysisTimeFrame());
                    DateTime startDate = analysisRequest.getStartDate();
                    DateTime endDate = analysisRequest.getEndDate();
                    dataModel.getSelectedData().forEach(model -> {
                        model.setSelectedStart(startDate);
                        model.setSelectedEnd(endDate);
                    });
                    dataModel.isGlobalAnalysisTimeFrame(true);

                    toolBarView.getPickerCombo().stopUpdateListener();
                    toolBarView.getPickerCombo().stopDateListener();

                    toolBarView.getPresetDateBox().getSelectionModel().select(TimeFrame.CUSTOM);
                    toolBarView.getPickerDateStart().valueProperty().setValue(LocalDate.of(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth()));
                    toolBarView.getPickerDateEnd().valueProperty().setValue(LocalDate.of(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth()));

                    toolBarView.getPickerCombo().startUpdateListener();
                    toolBarView.getPickerCombo().startDateListener();

                    dataModel.updateSamples();
                    dataModel.setCharts(dataModel.getCharts());
                    dataModel.setSelectedData(dataModel.getSelectedData());
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

    private void setupListener(ChartView cv, List<ChartView> notActive, ChartType chartType) {
        if (cv.getChart() != null) {
            switch (chartType) {
                case AREA:
                    cv.getChart().getChart().setOnMouseMoved(event -> {
                        cv.updateTablesSimultaneously(event, null);
                        notActive.forEach(na -> {
                            if (!na.getChartType().equals(ChartType.PIE)
                                    && !na.getChartType().equals(ChartType.BAR)
                                    && !na.getChartType().equals(ChartType.BUBBLE)) {
                                na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            }
                        });
                    });

                    cv.getChart().getPanner().zoomFinishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(false);
                                    xAxis.setLowerBound(cv.getChart().getPanner().getXAxisLowerBound());
                                    xAxis.setUpperBound(cv.getChart().getPanner().getXAxisUpperBound());
                                }
                            });
                            cv.getChart().getPanner().zoomFinishedProperty().setValue(false);
                        }
                    });

                    cv.getChart().getJfxChartUtil().getZoomManager().zoomFinishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(false);

                                    Timeline zoomAnimation = new Timeline();
                                    zoomAnimation.stop();
                                    zoomAnimation.getKeyFrames().setAll(
                                            new KeyFrame(Duration.ZERO,
                                                    new KeyValue(xAxis.lowerBoundProperty(), xAxis.getLowerBound()),
                                                    new KeyValue(xAxis.upperBoundProperty(), xAxis.getUpperBound())),
                                            new KeyFrame(Duration.millis(zoomDurationMillis.get()),
                                                    new KeyValue(xAxis.lowerBoundProperty(), cv.getChart().getJfxChartUtil().getZoomManager().getXAxisLowerBound()),
                                                    new KeyValue(xAxis.upperBoundProperty(), cv.getChart().getJfxChartUtil().getZoomManager().getXAxisUpperBound()))
                                    );
                                    zoomAnimation.play();
                                }
                            });
                            cv.getChart().getJfxChartUtil().getZoomManager().zoomFinishedProperty().setValue(false);
                        }
                    });

                    cv.getChart().getJfxChartUtil().doubleClickedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(true);
                                }
                            });
                            cv.getChart().getJfxChartUtil().doubleClickedProperty().setValue(false);
                        }
                    });

                    break;
                case LOGICAL:
                    cv.getChart().getChart().setOnMouseMoved(event -> {
                        cv.updateTablesSimultaneously(event, null);
                        notActive.forEach(na -> {
                            if (!na.getChartType().equals(ChartType.PIE)
                                    && !na.getChartType().equals(ChartType.BAR)
                                    && !na.getChartType().equals(ChartType.BUBBLE)) {
                                na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            }
                        });
                    });
                    break;
                case LINE:
                    cv.getChart().getChart().setOnMouseMoved(event -> {
                        cv.updateTablesSimultaneously(event, null);
                        notActive.forEach(na -> {
                            if (!na.getChartType().equals(ChartType.PIE)
                                    && !na.getChartType().equals(ChartType.BAR)
                                    && !na.getChartType().equals(ChartType.BUBBLE)) {
                                na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            }
                        });
                    });

                    cv.getChart().getPanner().zoomFinishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(false);
                                    xAxis.setLowerBound(cv.getChart().getPanner().getXAxisLowerBound());
                                    xAxis.setUpperBound(cv.getChart().getPanner().getXAxisUpperBound());
                                }
                            });
                            cv.getChart().getPanner().zoomFinishedProperty().setValue(false);
                        }
                    });

                    cv.getChart().getJfxChartUtil().getZoomManager().zoomFinishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(false);
                                    Timeline zoomAnimation = new Timeline();
                                    zoomAnimation.stop();
                                    zoomAnimation.getKeyFrames().setAll(
                                            new KeyFrame(Duration.ZERO,
                                                    new KeyValue(xAxis.lowerBoundProperty(), xAxis.getLowerBound()),
                                                    new KeyValue(xAxis.upperBoundProperty(), xAxis.getUpperBound())),
                                            new KeyFrame(Duration.millis(zoomDurationMillis.get()),
                                                    new KeyValue(xAxis.lowerBoundProperty(), cv.getChart().getJfxChartUtil().getZoomManager().getXAxisLowerBound()),
                                                    new KeyValue(xAxis.upperBoundProperty(), cv.getChart().getJfxChartUtil().getZoomManager().getXAxisUpperBound()))
                                    );
                                    zoomAnimation.play();
                                }
                            });
                            cv.getChart().getJfxChartUtil().getZoomManager().zoomFinishedProperty().setValue(false);
                        }
                    });

                    cv.getChart().getJfxChartUtil().doubleClickedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(true);
                                }
                            });
                            cv.getChart().getJfxChartUtil().doubleClickedProperty().setValue(false);
                        }
                    });

                    break;
                case BAR:
                    break;
                case BUBBLE:
                    break;
                case SCATTER:
                    cv.getChart().getChart().setOnMouseMoved(event -> {
                        cv.updateTablesSimultaneously(event, null);
                        notActive.forEach(na -> {
                            if (!na.getChartType().equals(ChartType.PIE)
                                    && !na.getChartType().equals(ChartType.BAR)
                                    && !na.getChartType().equals(ChartType.BUBBLE)) {
                                na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            }
                        });
                    });

                    cv.getChart().getPanner().zoomFinishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(false);
                                    xAxis.setLowerBound(cv.getChart().getPanner().getXAxisLowerBound());
                                    xAxis.setUpperBound(cv.getChart().getPanner().getXAxisUpperBound());
                                }
                            });
                            cv.getChart().getPanner().zoomFinishedProperty().setValue(false);
                        }
                    });

                    cv.getChart().getJfxChartUtil().getZoomManager().zoomFinishedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(false);

                                    Timeline zoomAnimation = new Timeline();
                                    zoomAnimation.stop();
                                    zoomAnimation.getKeyFrames().setAll(
                                            new KeyFrame(Duration.ZERO,
                                                    new KeyValue(xAxis.lowerBoundProperty(), xAxis.getLowerBound()),
                                                    new KeyValue(xAxis.upperBoundProperty(), xAxis.getUpperBound())),
                                            new KeyFrame(Duration.millis(zoomDurationMillis.get()),
                                                    new KeyValue(xAxis.lowerBoundProperty(), cv.getChart().getJfxChartUtil().getZoomManager().getXAxisLowerBound()),
                                                    new KeyValue(xAxis.upperBoundProperty(), cv.getChart().getJfxChartUtil().getZoomManager().getXAxisUpperBound()))
                                    );
                                    zoomAnimation.play();
                                }
                            });
                            cv.getChart().getJfxChartUtil().getZoomManager().zoomFinishedProperty().setValue(false);
                        }
                    });

                    cv.getChart().getJfxChartUtil().doubleClickedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            notActive.forEach(chartView -> {
                                if (!chartView.getChartType().equals(ChartType.PIE)
                                        && !chartView.getChartType().equals(ChartType.BAR)
                                        && !chartView.getChartType().equals(ChartType.BUBBLE)) {
                                    MultiAxisChart chart = (MultiAxisChart) chartView.getChart().getChart();

                                    DateValueAxis xAxis = (DateValueAxis) chart.getXAxis();
                                    xAxis.setAutoRanging(true);
                                }
                            });
                            cv.getChart().getJfxChartUtil().doubleClickedProperty().setValue(false);
                        }
                    });

                    break;
                case PIE:
                    break;
                case TABLE:
                    cv.getChart().getChart().setOnMouseMoved(event -> {
                        cv.updateTablesSimultaneously(event, null);
                        notActive.forEach(na -> {
                            if (!na.getChartType().equals(ChartType.PIE)
                                    && !na.getChartType().equals(ChartType.BAR)
                                    && !na.getChartType().equals(ChartType.BUBBLE)) {
                                na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            }
                        });
                    });
                    break;
                default:
                    break;
            }
        }
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

    private List<ChartView> getChartViews() {

        if (charts.isEmpty() || hasLogicalCharts() || dataModelHasLogicalCharts()) {
            charts.clear();

            dataModel.getCharts().forEach(chart -> {
                int chartID = chart.getId();
                ChartType type = chart.getChartType();

                if (!type.equals(ChartType.LOGICAL)) {
                    ChartView view = new ChartView(dataModel);
                    view.drawAreaChart(chart.getId(), type);
                    charts.add(view);
                } else {
                    createLogicalChart(chart, chartID, type);
                }
            });
        } else {
            if (dataModel.getCharts().size() < charts.size()) {

                if (!hasLogicalCharts() && dataModel.getCharts().size() < charts.size()) {

                    charts.subList(dataModel.getCharts().size(), charts.size()).clear();
                }

                if (hasLogicalCharts()) {
                    charts.removeAll(charts.stream().filter(chartView -> chartView.getChartType().equals(ChartType.LOGICAL)).collect(Collectors.toList()));
                    dataModel.getCharts().forEach(chart -> {
                        int chartID = chart.getId();
                        ChartType type = chart.getChartType();

                        if (type.equals(ChartType.LOGICAL)) {
                            createLogicalChart(chart, chartID, type);
                        }
                    });
                } else {
                    charts.forEach(chart -> {
                        if (!chart.getChartType().equals(ChartType.LOGICAL)) {
                            int newChartIndex = charts.indexOf(chart);
                            chart.setType(dataModel.getCharts().get(newChartIndex).getChartType());
                            chart.setChartId(newChartIndex);
                            chart.updateChart();
                        }
                    });

                    charts.removeAll(charts.stream().filter(chartView -> chartView.getChartType().equals(ChartType.LOGICAL)).collect(Collectors.toList()));
                    dataModel.getCharts().forEach(chart -> {
                        int chartID = chart.getId();
                        ChartType type = chart.getChartType();

                        if (type.equals(ChartType.LOGICAL)) {
                            createLogicalChart(chart, chartID, type);
                        }
                    });
                }
            } else {

                for (ChartView chartView : charts) {
                    chartView.setType(dataModel.getCharts().get(charts.indexOf(chartView)).getChartType());
                    chartView.updateChart();
                }

                if (dataModel.getCharts().size() > charts.size()) {
                    for (int i = charts.size(); i < dataModel.getCharts().size(); i++) {
                        ChartView view = new ChartView(dataModel);

                        ChartType type = dataModel.getCharts().get(i).getChartType();

                        view.drawAreaChart(dataModel.getCharts().get(i).getId(), type);

                        charts.add(view);
                    }
                }

            }

        }

        return charts;
    }

    private boolean dataModelHasLogicalCharts() {
        boolean hasLogicalCharts = false;
        for (ChartSettings chartSettings : dataModel.getCharts()) {
            if (chartSettings.getChartType().equals(ChartType.LOGICAL)) {
                hasLogicalCharts = true;
            }
        }
        return hasLogicalCharts;
    }

    private boolean hasLogicalCharts() {
        boolean hasLogicalCharts = false;
        for (ChartView chartView : charts) {
            if (chartView.getChartType().equals(ChartType.LOGICAL)) {
                hasLogicalCharts = true;
            }
        }
        return hasLogicalCharts;
    }

    private void createLogicalChart(ChartSettings chart, int chartID, ChartType type) {

        List<ChartView> subCharts = new ArrayList<>();
        for (ChartDataModel singleRow : dataModel.getSelectedData()) {
            for (int i : singleRow.getSelectedcharts()) {
                if (i == chart.getId()) {
                    ChartView subView = new ChartView(dataModel);
                    subView.setSingleRow(singleRow);
                    subCharts.add(subView);
                }
            }
        }
        AlphanumComparator ac = new AlphanumComparator();
        subCharts.sort((o1, o2) -> ac.compare(o1.getSingleRow().getObject().getName(), o2.getSingleRow().getObject().getName()));

        boolean firstChart = true;
        ObservableList<TableEntry> allEntries = null;
        Double minValue = Double.MAX_VALUE;
        Double maxValue = -Double.MAX_VALUE;
        for (ChartView chartView : subCharts) {
            if (firstChart) {
                chartView.setFirstLogical(true);
                chartView.setShowTable(true);
            } else {
                chartView.setShowTable(false);
            }

            chartView.drawAreaChart(chartID, chartView.getSingleRow(), type);

//            chartView.getSingleRow().calcMinAndMax();
            double min = ((LogicalChart) chartView.getChart()).getMinValue();
            double max = ((LogicalChart) chartView.getChart()).getMaxValue();
            minValue = Math.min(minValue, min);
            maxValue = Math.max(maxValue, max);

            if (firstChart) {
                allEntries = chartView.getChart().getTableData();
            } else {
                chartView.getChart().getChart().setTitle("");
                allEntries.addAll(chartView.getChart().getTableData());
            }

            firstChart = false;
        }

        if (!minValue.equals(Double.MAX_VALUE) && !maxValue.equals(-Double.MAX_VALUE)) {
            for (ChartView chartView : subCharts) {
                ((MultiAxisChart) chartView.getChart().getChart()).getY1Axis().setAutoRanging(false);
                ((MultiAxisChart) chartView.getChart().getChart()).getY2Axis().setAutoRanging(false);
                ((NumberAxis) ((MultiAxisChart) chartView.getChart().getChart()).getY1Axis()).setLowerBound(minValue);
                ((NumberAxis) ((MultiAxisChart) chartView.getChart().getChart()).getY1Axis()).setUpperBound(maxValue);
                ((NumberAxis) ((MultiAxisChart) chartView.getChart().getChart()).getY2Axis()).setLowerBound(minValue);
                ((NumberAxis) ((MultiAxisChart) chartView.getChart().getChart()).getY2Axis()).setUpperBound(maxValue);
                ((MultiAxisChart) chartView.getChart().getChart()).getY1Axis().layout();
                ((MultiAxisChart) chartView.getChart().getChart()).getY2Axis().layout();
                chartView.getChart().getChart().layout();
            }
        }

        charts.addAll(subCharts);
    }

    public List<ChartView> getCharts() {
        return charts;
    }
}
