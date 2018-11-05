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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.ChartType;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.dialog.ChartSelectionDialog;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.tool.I18n;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPluginView implements Plugin, Observer {

    private final Logger logger = LogManager.getLogger(GraphPluginView.class);
    private ToolBarView toolBarView;
    private GraphDataModel dataModel;
    //private GraphController controller;
    private ChartView chartView;
    private List<ChartView> listChartViews = null;
    private StringProperty name = new SimpleStringProperty("Graph");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border;
    private ToolBar toolBar;
    private LoadAnalysisDialog dialog;
    private ObservableList<String> chartsList = FXCollections.observableArrayList();
    private String tooltip = I18n.getInstance().getString("pluginmanager.graph.tooltip");
    private boolean firstStart = true;

    public GraphPluginView(JEVisDataSource ds, String newname) {
        this.dataModel = new GraphDataModel(ds);
        this.dataModel.addObserver(this);

        //this.controller = new GraphController(this, dataModel);
        this.toolBarView = new ToolBarView(dataModel, ds, chartView, listChartViews);
        //this.chartView = new ChartView(dataModel);

        this.ds = ds;
        this.name.set(newname);
    }

    @Override
    public String getClassName() {
        return "Graph Plugin";
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

            newAnalysis.setOnAction(event -> newAnalysis());

            loadAnalysis.setOnAction(event -> openDialog());

            if (border == null) border = new BorderPane();

            border.setCenter(vBox);
        }
    }

    private void openDialog() {

        dialog = new LoadAnalysisDialog(ds, dataModel);

        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {

                        newAnalysis();

                    } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                        toolBarView.select(dialog.getLv().getSelectionModel().getSelectedItem());
                    }
                });
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
        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, dataModel, null);

        if (selectionDialog.show(JEConfig.getStage()) == ChartSelectionDialog.Response.OK) {

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
        //TODO: implement
        return false;
    }

    @Override
    public void handleRequest(int cmdType) {
        try {
            switch (cmdType) {
                case Constants.Plugin.Command.SAVE:
                    break;
                case Constants.Plugin.Command.DELTE:
                    break;
                case Constants.Plugin.Command.EXPAND:
                    break;
                case Constants.Plugin.Command.NEW:
                    break;
                case Constants.Plugin.Command.RELOAD:
                    break;
                default:
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
        if (dataModel.getSelectedData() != null) chartsList = dataModel.getChartsList();

        if (border == null) {
            border = new BorderPane();

            //chartView.drawDefaultAreaChart();
            if (chartView != null)
                border.setCenter(chartView.getChartRegion());

            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
        }

        return border;

    }

    @Override
    public void update(Observable o, Object arg) {

        if (dataModel.getSelectedData() != null) {
            chartsList = dataModel.getChartsList();

            if (chartsList.size() == 1 || chartsList.isEmpty()) {
                if (chartsList.size() == 1) {
                    chartView = null;
                    chartView = new ChartView(dataModel);
                    String chartTitle = chartsList.get(0);
                    ChartType type = ChartType.AREA;
                    if (dataModel.getCharts() != null && !dataModel.getCharts().isEmpty()) {
                        for (ChartSettings set : dataModel.getCharts()) {
                            if (set.getName().equals(chartTitle)) type = set.getChartType();
                        }
                    }
                    chartView.drawAreaChart(chartTitle, type);

                    //chartView.drawDefaultAreaChart();
                    border.setTop(chartView.getLegend());
                    //border.setCenter(chartView.getChart());
                    border.setCenter(chartView.getChartRegion());
                }
            } else if (chartsList.size() > 1) {
                if (border == null) {
                    border = new BorderPane();
                }
                listChartViews = null;
                listChartViews = toolBarView.getChartViews();
                AlphanumComparator ac = new AlphanumComparator();
                try {
                    listChartViews.sort((s1, s2) -> ac.compare(s1.getChartName(), s2.getChartName()));
                } catch (Exception e) {
                }

                VBox vBox = new VBox();

                ScrollPane sp = new ScrollPane();
                sp.setFitToWidth(true);

                listChartViews.forEach(cv -> {
                    BorderPane bp = new BorderPane();
                    Boolean newChart = false;

                    for (ChartSettings cset : dataModel.getCharts()) {
                        if (cset.getName().equals(cv.getChartName())) {

                            if (cset.getHeight() != null)
                                bp.setPrefHeight(cset.getHeight());
                            newChart = true;
                        }
                    }

                    if (newChart) {
                        Integer numberOfCharts = listChartViews.size();

                        Double newHeight = border.getHeight() / numberOfCharts;

                        bp.setPrefHeight(newHeight);
                    }

                    bp.setMinHeight(200);
                    bp.setTop(cv.getLegend());
                    bp.setCenter(cv.getChartRegion());
                    bp.setBottom(null);
                    bp.setBorder(new Border(new BorderStroke(Color.DARKGRAY,
                            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(4, 4, 4, 4))));

                    DragResizerXY.makeResizable(bp);

                    bp.heightProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null && newValue != oldValue) {
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

                    switch (chartType.toString()) {
                        case ("AREA"):
                            cv.getChart().getChart().setOnMouseMoved(event -> {
                                cv.updateTablesSimultaneously(event, null);
                                notActive.parallelStream().forEach(na -> {
                                    if (na.getChartType().equals(ChartType.AREA) || na.getChartType().equals(ChartType.LINE))
                                        na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                                });
                            });
                            break;
                        case ("LINE"):
                            cv.getChart().getChart().setOnMouseMoved(event -> {
                                cv.updateTablesSimultaneously(event, null);
                                notActive.parallelStream().forEach(na -> {
                                    if (na.getChartType().equals(ChartType.AREA) || na.getChartType().equals(ChartType.LINE))
                                        na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                                });
                            });
                            break;
                        case ("BAR"):
//                        cv.getBarChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                            break;
                        case ("BUBBLE"):
//                        cv.getBubbleChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                            break;
                        case ("SCATTER"):
//                        cv.getScatterChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                            break;
                        case ("PIE"):
//                        cv.getPieChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                            break;
                        default:
                            break;
                    }

                    Separator sep = new Separator();
                    sep.setOrientation(Orientation.HORIZONTAL);

                    vBox.getChildren().add(bp);
                    vBox.getChildren().add(sep);

                });

                sp.setContent(vBox);

                border.setTop(null);
                border.setCenter(sp);
                border.setBottom(null);
                border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
            }
        }
        System.gc();
    }
}
