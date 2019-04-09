package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.LineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.plugin.Dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
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

    public ChartWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }


    public ChartWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }

    @Override
    public void update(Interval interval) {
        logger.info("Update: {}", interval);
        //if config changed
        if (config.hasChanged("")) {
            lineChart.setChartSettings(chart1 -> {
                MultiAxisLineChart multiAxisLineChart = (MultiAxisLineChart) chart1;
//                multiAxisLineChart.setAnimated(true);
                lineChart.getChart().setAnimated(false);
                multiAxisLineChart.setLegendSide(Side.BOTTOM);
                multiAxisLineChart.setLegendVisible(true);

            });


            setChartLabel((MultiAxisLineChart) lineChart.getChart(), config.fontColor.get());

//            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
            legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

            try {
                GraphAnalysisLinkerNode dataModelNode = mapper.treeToValue(config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
                graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), dataModelNode);
                graphAnalysisLinker.applyConfig(openAnalysisButton, sampleHandler.getDataModel(), interval);
            } catch (Exception ex) {
                logger.error(ex);
            }

        }


        sampleHandler.setInterval(interval);
        sampleHandler.update();

        legend.getItems().clear();
        sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
                String dataName = chartDataModel.getObject().getName();
                legend.getItems().add(legend.buildLegendItem(dataName + " " + chartDataModel.getUnit(), chartDataModel.getColor(), config.fontColor.getValue(), config.fontSize.get()));
            } catch (Exception ex) {
                logger.error(ex);
            }
        });


        Platform.runLater(() -> {
            lineChart.updateChart();
            lineChart.getChart().layout();
        });

    }

    private void setChartLabel(MultiAxisLineChart chart, Color newValue) {
        chart.getY1Axis().setTickLabelFill(newValue);
        chart.getXAxis().setTickLabelFill(newValue);

        chart.getXAxis().setLabel("");
        chart.getY1Axis().setLabel("");
    }

    @Override
    public void init() {
        sampleHandler = new DataModelDataHandler(getDataSource(), config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        sampleHandler.setMultiSelect(true);

        if (lineChart == null) {
            lineChart = new LineChart(sampleHandler.getDataModel(), false, ManipulationMode.NONE, 0, "");


            legend.setAlignment(Pos.CENTER);

            graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), null);
            openAnalysisButton = graphAnalysisLinker.buildLinkerButton();

            BorderPane borderPane = new BorderPane();

//            borderPane.setCache(true);
//            borderPane.setCacheHint(CalinescheHint.SPEED);

//        HBox hBox = new HBox();
//        hBox.setPadding(new Insets(5, 8, 5, 8));
//        hBox.getChildren().add(legend);

            BorderPane bottomBorderPane = new BorderPane();
            bottomBorderPane.setCenter(legend);
            bottomBorderPane.setRight(openAnalysisButton);

            borderPane.setCenter(lineChart.getChart());
            borderPane.setBottom(bottomBorderPane);
            setGraphic(borderPane);
        } else {
            lineChart.setDataModels(sampleHandler.getDataModel());
        }


    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ChartWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }


    public class ConfigNode {
        private Long graphAnalysisObject = 7904L;

        public Long getGraphAnalysisObject() {
            return graphAnalysisObject;
        }

        public void setGraphAnalysisObject(Long graphAnalysisObject) {
            this.graphAnalysisObject = graphAnalysisObject;
        }
    }

}
