package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.LineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.LastValueHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;

import java.util.ArrayList;
import java.util.List;

public class ChartWidget extends Widget {

    private final BorderPane rootNode = new BorderPane();
    ChartDataModel chartDataModel;
    LineChart lc;
    private LastValueHandler lastValueHandler;

    public ChartWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);

        lastValueHandler = new LastValueHandler(getDataSource());
        lastValueHandler.lastUpdate.addListener((observable, oldValue, newValue) -> {

            lastValueHandler.getAttributeMap().forEach((s, jeVisAttribute) -> {
                List<JEVisSample> samplesList = lastValueHandler.getValuePropertyMap().get(s);
                if (!samplesList.isEmpty() && samplesList.size() > 1) {
                    try {
                        System.out.println("New samples: " + samplesList);
                        JEVisObject object = jeVisAttribute.getObject();

                        if (object.getJEVisClassName().equals("Data"))
                            chartDataModel.setObject(object);
                        else if (jeVisAttribute.getObject().getJEVisClassName().equals("Clean Data")) {
                            chartDataModel.setDataProcessor(object);
                            chartDataModel.setObject(object.getParents().get(0));
                        }

                        List<Integer> list = new ArrayList<>();
                        list.add(0);
                        chartDataModel.setSelectedCharts(list);
                        chartDataModel.setAttribute(jeVisAttribute);
                        chartDataModel.setSamples(samplesList);

                        chartDataModel.setSelectedStart(samplesList.get(0).getTimestamp());
                        chartDataModel.setSelectedEnd(samplesList.get(samplesList.size() - 1).getTimestamp());

                        chartDataModel.setColor(Color.BLUE);
//                        chartDataModel.setSomethingChanged(false);
                        Platform.runLater(() -> {
                            lc.updateChart();
                            setChartLabel((MultiAxisLineChart) lc.getChart(), config.fontColor.get());
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("no data");
                }
                //


            });
        });


    }

    @Override
    public SampleHandler getSampleHandler() {
        System.out.println("getSampleHandler");
        return lastValueHandler;
    }

    @Override
    public void setBackgroundColor(Color color) {

    }

    @Override
    public void setTitle(String text) {

    }

    @Override
    public void setFontColor(Color color) {

    }

    @Override
    public void setCustomFont(Font font) {

    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutChart.png", previewSize.getHeight(), previewSize.getWidth());
    }

    @Override
    public void update(WidgetData data, boolean hasNewData) {

    }


    @Override
    public void init() {
        System.out.println("init");

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: transparent");

        chartDataModel = new ChartDataModel();


        List<ChartDataModel> chartDataModelList = new ArrayList<>();
        chartDataModelList.add(chartDataModel);
        lc = new LineChart(chartDataModelList, false, ManipulationMode.NONE, 0, "");

        MultiAxisLineChart chart = (MultiAxisLineChart) lc.getChart();
//        chart.setMaxSize(100, 100);

        chart.setMaxSize(config.size.get().getWidth(), config.size.get().getHeight());
        chart.setPrefSize(config.size.get().getWidth(), config.size.get().getHeight());
        config.size.addListener((observable, oldValue, newValue) -> {
            chart.setMaxSize(newValue.getWidth(), newValue.getHeight());
            chart.setPrefSize(newValue.getWidth(), newValue.getHeight());
        });

        chart.setAnimated(true);
        chart.setLegendVisible(false);


        setChartLabel(chart, config.fontColor.get());
        config.fontColor.addListener((observable, oldValue, newValue) -> {
            setChartLabel(chart, newValue);

        });


        rootNode.setCenter(lc.getChart());
        setGraphic(rootNode);
    }

    private void setChartLabel(MultiAxisLineChart chart, Color newValue) {
        chart.getY1Axis().setTickLabelFill(newValue);
        chart.getXAxis().setTickLabelFill(newValue);

        chart.getXAxis().setLabel("");
        chart.getY1Axis().setLabel("");
    }

    @Override
    public String typeID() {
        return "Chart";
    }
}
