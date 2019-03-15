package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.LineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

import java.lang.reflect.Field;

public class ChartWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(PieChart.class);
    public static String WIDGET_ID = "Chart";

    private LineChart lineChart;
    private DataModelDataHandler sampleHandler;
    private ChartDataModel chartDataModel;
    private Legend legend = new Legend();

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
                multiAxisLineChart.setAnimated(true);
                multiAxisLineChart.setLegendSide(Side.BOTTOM);
                multiAxisLineChart.setLegendVisible(true);

            });

            setChartLabel((MultiAxisLineChart) lineChart.getChart(), config.fontColor.get());

            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
        }


        sampleHandler.setInterval(interval);
        sampleHandler.update();

        legend.getItems().clear();
        sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
                String dataName = chartDataModel.getObject().getName();
                legend.getItems().add(buildLegendItem(dataName + " " + chartDataModel.getUnit(), chartDataModel.getColor(), config.fontColor.getValue()));
            } catch (Exception ex) {
                logger.error(ex);
            }
        });


        Platform.runLater(() -> {
            lineChart.updateChart();

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

        sampleHandler = new DataModelDataHandler(getDataSource(), config.getDataHandlerNode());
        sampleHandler.setMultiSelect(true);
//        chartDataModel = new ChartDataModel();


//        List<ChartDataModel> chartDataModelList = new ArrayList<>();
//        chartDataModelList.add(chartDataModel);
        lineChart = new LineChart(sampleHandler.getDataModel(), false, ManipulationMode.NONE, 0, "");
        legend.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        Region left = new Region();
        Region right = new Region();

        HBox.setHgrow(legend, Priority.SOMETIMES);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        hBox.getChildren().addAll(left, legend, right);

        vBox.getChildren().addAll(lineChart.getChart(), hBox);

        setGraphic(vBox);
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }

    private Legend.LegendItem buildLegendItem(String name, Color color, Color fontcolor) {

        Rectangle r = new Rectangle();
        r.setX(0);
        r.setY(0);
        r.setWidth(12);
        r.setHeight(12);
        r.setArcWidth(20);
        r.setArcHeight(20);
        r.setStroke(color);
        r.setFill(color);

        /**
         * TODO: replace this hack with an own implementation of an legend
         */
        Legend.LegendItem item = new Legend.LegendItem(name, r);
        try {
            Field privateStringField = Legend.LegendItem.class.
                    getDeclaredField("label");
            privateStringField.setAccessible(true);
            Label label = (Label) privateStringField.get(item);
            label.setTextFill(fontcolor);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return item;
    }

}
