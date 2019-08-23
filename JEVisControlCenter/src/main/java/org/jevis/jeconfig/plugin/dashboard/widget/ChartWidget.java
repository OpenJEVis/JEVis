package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.LineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

public class ChartWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(PieChart.class);
    public static String WIDGET_ID = "Chart";

    private LineChart lineChart;
    private DataModelDataHandler sampleHandler;
    private WidgetLegend legend = new WidgetLegend();
    private JFXButton openAnalysisButton = new JFXButton();
    private ConfigNode configNode = new ConfigNode();
    private ObjectMapper mapper = new ObjectMapper();
    private GraphAnalysisLinker graphAnalysisLinker;
    private BorderPane borderPane = new BorderPane();

    private boolean autoAggregation = true;

    public ChartWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    @Override
    public void updateData(Interval interval) {
        logger.info("Update: {}", interval);

        this.lineChart.setChartSettings(chart1 -> {
            MultiAxisLineChart multiAxisLineChart = (MultiAxisLineChart) chart1;
//                multiAxisLineChart.setAnimated(true);
            this.lineChart.getChart().setAnimated(false);
            multiAxisLineChart.setLegendSide(Side.BOTTOM);
            multiAxisLineChart.setLegendVisible(true);

        });

        Platform.runLater(() -> {
            setChartLabel((MultiAxisLineChart) this.lineChart.getChart(), this.config.getFontColor());
            this.legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
            //            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
        });


        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        try {
            if (this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE) != null) {
                GraphAnalysisLinkerNode dataModelNode = this.mapper.treeToValue(this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
                this.graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), dataModelNode);
                this.graphAnalysisLinker.applyConfig(this.openAnalysisButton, this.sampleHandler.getDataModel(), interval);
            } else {
                this.openAnalysisButton.setVisible(false);
                logger.warn("no linker set");
            }

        } catch (Exception ex) {
            logger.error(ex);
        }

        Platform.runLater(() -> {
            this.legend.getItems().clear();
            this.sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    String dataName = chartDataModel.getObject().getName();
                    this.legend.getItems().add(
                            this.legend.buildLegendItem(dataName + " " + chartDataModel.getUnit(), chartDataModel.getColor(),
                                    this.config.getFontColor(), this.config.getFontSize()));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            /**
             * Linechart does not support updateData so we need to create an new one every time;
             */
            this.lineChart = new LineChart(this.sampleHandler.getDataModel(), false, false, false, false, false, null, -1, ManipulationMode.NONE, 0, "");
//            this.lineChart.updateChart();

            this.lineChart.getChart().layout();
            this.borderPane.setCenter(this.lineChart.getChart());
        });

    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

    }

    private void setChartLabel(MultiAxisLineChart chart, Color newValue) {
        chart.getY1Axis().setTickLabelFill(newValue);
        chart.getXAxis().setTickLabelFill(newValue);

        chart.getXAxis().setLabel("");
        chart.getY1Axis().setLabel("");
    }

    @Override
    public void init() {
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        this.sampleHandler.setMultiSelect(true);

        this.lineChart = new LineChart(this.sampleHandler.getDataModel(), false, false, false, false, false, null, -1, ManipulationMode.NONE, 0, "");


        this.legend.setAlignment(Pos.CENTER);

        this.graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), null);
        this.openAnalysisButton = this.graphAnalysisLinker.buildLinkerButton();


        BorderPane bottomBorderPane = new BorderPane();
        bottomBorderPane.setCenter(this.legend);
        bottomBorderPane.setRight(this.openAnalysisButton);

        this.borderPane.setCenter(this.lineChart.getChart());
        this.borderPane.setBottom(bottomBorderPane);
        setGraphic(this.borderPane);


    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ObjectNode toNode() {
        ObjectNode dashBoardNode = super.createDefaultNode();
        dashBoardNode
                .set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ChartWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    public class ConfigNode {
        private Long graphAnalysisObject = 7904L;

        public Long getGraphAnalysisObject() {
            return this.graphAnalysisObject;
        }

        public void setGraphAnalysisObject(Long graphAnalysisObject) {
            this.graphAnalysisObject = graphAnalysisObject;
        }
    }

}
