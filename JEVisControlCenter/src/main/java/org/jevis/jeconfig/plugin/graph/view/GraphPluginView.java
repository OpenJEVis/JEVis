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

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.Plugin;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.graph.LoadAnalysisDialog;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class GraphPluginView implements Plugin, Observer {

    private static final Logger logger = LogManager.getLogger(GraphPluginView.class);
    private final List<ChartView> charts = new ArrayList<>();
    private ToolBarView toolBarView;
    private GraphDataModel dataModel;
    //private GraphController controller;
    private ChartView chartView;
    private StringProperty name = new SimpleStringProperty("Graph");
    private StringProperty id = new SimpleStringProperty("*NO_ID*");
    private JEVisDataSource ds;
    private BorderPane border;
    private StackPane stackPane = new StackPane();
    private ToolBar toolBar;
    private String tooltip = I18n.getInstance().getString("pluginmanager.graph.tooltip");
    private boolean firstStart = true;

    public GraphPluginView(JEVisDataSource ds, String newname) {
        this.dataModel = new GraphDataModel(ds);
        this.dataModel.addObserver(this);

        //this.controller = new GraphController(this, dataModel);
        this.toolBarView = new ToolBarView(dataModel, ds);
        //this.chartView = new ChartView(dataModel);

        this.ds = ds;
        this.name.set(newname);

        getContentNode();
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

            border.setCenter(vBox);
        }
    }

    private void openDialog() {

        LoadAnalysisDialog dialog = new LoadAnalysisDialog(ds, dataModel, toolBarView);
        toolBarView.setDisableToolBarIcons(false);

        dialog.showAndWait()
                .ifPresent(response -> {
                    if (response.getButtonData().getTypeCode() == ButtonType.OK.getButtonData().getTypeCode()) {

                        newAnalysis();

                    } else if (response.getButtonData().getTypeCode() == ButtonType.NO.getButtonData().getTypeCode()) {

                        dataModel.setAnalysisTimeFrame(dataModel.getAnalysisTimeFrame());
                        dataModel.updateSamples();
                        dataModel.setCharts(dataModel.getCharts());
                        dataModel.setSelectedData(dataModel.getSelectedData());
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

        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, dataModel);

        AnalysisTimeFrame atf = new AnalysisTimeFrame();
        atf.setTimeFrame(AnalysisTimeFrame.TimeFrame.custom);

        dataModel.setAnalysisTimeFrame(atf);

        if (selectionDialog.show() == ChartSelectionDialog.Response.OK) {
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

        if (border == null) {
            border = new BorderPane();

            /**
             * If scene size changes and old value is not 0.0 (firsts draw) redraw
             * TODO: resizing an window manually will cause a lot of resize changes and so redraws, solve this better
             */
            border.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (!oldValue.equals(0.0)) {
                    Platform.runLater(() -> {
                        update(null, "Screen size changed");
                    });
                }
            });
            border.heightProperty().addListener((observable, oldValue, newValue) -> {
                if (!oldValue.equals(0.0)) {
                    Platform.runLater(() -> {
                        update(null, "Screen size changed");
                    });
                }
            });

            //chartView.drawDefaultAreaChart();
            if (chartView != null)
                border.setCenter(chartView.getChartRegion());

//            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2 + "; -fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
        }

        if (!stackPane.getChildren().contains(border)) stackPane.getChildren().add(border);

        return stackPane;

    }

    @Override
    public void update(Observable o, Object arg) {
        double autoMinSize = 220;

        if (dataModel.getSelectedData() != null) {

            double maxHeight = border.getHeight();
            double totalPrefHeight = 0;

            List<ChartView> listChartViews = null;
            listChartViews = getChartViews();
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

                BorderPane bp = new BorderPane();
                bp.setStyle("-fx-faint-focus-color: transparent; -fx-focus-color: transparent;");
                bp.setMinHeight(autoMinSize);
                bp.setMaxWidth(border.getMaxWidth());

                for (ChartSettings cset : dataModel.getCharts()) {
                    if (cset.getName().equals(cv.getChartName())) {

                        /**
                         * Add offset for every data object because of the table legend
                         * Every row has about 25 pixel with the default font
                         */
                        int dataSizeOffset = 30;
                        /** Calculate maxsize based on the amount of Data **/
                        int dataSize = 0;
                        for (ChartDataModel chartDataModel : dataModel.getSelectedData()) {
                            for (int i : chartDataModel.getSelectedcharts()) {
                                if (i == cset.getId()) {
                                    dataSize++;
                                }
                            }
                        }
                        bp.setMinHeight(autoMinSize + (dataSize * dataSizeOffset));

                        if (cset.getHeight() != null) {
                            bp.setPrefHeight(cset.getHeight());
                        } else {
                            bp.setPrefHeight(autoMinSize + (dataSize * dataSizeOffset));
                        }
                    }
                }

                cv.getLegend().maxWidthProperty().bind(bp.widthProperty());
                bp.setTop(cv.getLegend());

                bp.setCenter(cv.getChartRegion());
                bp.setBottom(null);

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

                switch (chartType) {
                    case AREA:
                        cv.getChart().getChart().setOnMouseMoved(event -> {
                            cv.updateTablesSimultaneously(event, null);
                            notActive.parallelStream().forEach(na -> {
                                if (na.getChartType().equals(ChartType.AREA) || na.getChartType().equals(ChartType.LINE))
                                    na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            });
                        });
                        break;
                    case LINE:
                        cv.getChart().getChart().setOnMouseMoved(event -> {
                            cv.updateTablesSimultaneously(event, null);
                            notActive.parallelStream().forEach(na -> {
                                if (na.getChartType().equals(ChartType.AREA) || na.getChartType().equals(ChartType.LINE))
                                    na.updateTablesSimultaneously(null, cv.getValueForDisplay());
                            });
                        });
                        break;
                    case BAR:
//                        cv.getBarChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                        break;
                    case BUBBLE:
//                        cv.getBubbleChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                        break;
                    case SCATTER:
//                        cv.getScatterChart().setOnMouseMoved(event -> {
//                            cv.updateTablesSimultaneously(cv.getChartName(), cv.getChartType(), event, null);
//                            for (ChartView na : notActive) {
//                                na.updateTablesSimultaneously(na.getChartName(), na.getChartType(), null, cv.getValueForDisplay());
//                            }
//                        });
                        break;
                    case PIE:
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

            }


            /**
             * If auto size is on or if its only one chart sale the chart to maximize screen size
             */
            if (dataModel.getCharts().size() == 1 || dataModel.getAutoResize()) {
                /**
                 * If all children take more space then the maximum available size
                 * set all on min size. after this the free space will be reallocate
                 */
                totalPrefHeight = calculationTotalPrefSize(vBox);
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

            sp.setContent(vBox);

            border.setTop(null);
            border.setCenter(sp);
            border.setBottom(null);
            border.setStyle("-fx-background-color: " + Constants.Color.LIGHT_GREY2);

        }

        System.gc();
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

        if (charts.isEmpty()) {

            dataModel.getCharts().forEach(chart -> {
                ChartView view = new ChartView(dataModel);

                ChartType type = chart.getChartType();

                view.drawAreaChart(chart.getId(), type);

                charts.add(view);
            });
        } else {
            if (dataModel.getCharts().size() <= charts.size()) {

                if (dataModel.getCharts().size() < charts.size()) {
                    for (int i = charts.size(); i > dataModel.getCharts().size(); i--) {
                        charts.remove(i - 1);
                    }
                }

                for (ChartView chartView : charts) {
                    chartView.setType(dataModel.getCharts().get(charts.indexOf(chartView)).getChartType());
                    chartView.updateChart();
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
}
