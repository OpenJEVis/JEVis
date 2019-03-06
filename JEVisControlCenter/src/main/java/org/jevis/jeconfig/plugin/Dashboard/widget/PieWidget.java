package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.ColorColumn;
import org.jevis.jeconfig.application.Chart.Charts.PieChart;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SampleHandler;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.SimpleDataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PieWidget extends Widget {

    private final BorderPane rootNode = new BorderPane();

    PieChart chart;
    List<ChartDataModel> chartDataModelList = new ArrayList<>();

    private SimpleDataHandler simpleDataHandler;

    public PieWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource);

        simpleDataHandler = new SimpleDataHandler(getDataSource());
        simpleDataHandler.setMultiSelect(true);
        simpleDataHandler.lastUpdate.addListener((observable, oldValue, newValue) -> {

            chartDataModelList.clear();
            AtomicInteger i = new AtomicInteger(0);
            simpleDataHandler.getAttributeMap().forEach((s, jeVisAttribute) -> {
                List<JEVisSample> samplesList = simpleDataHandler.getValuePropertyMap().get(s);
                if (!samplesList.isEmpty() && samplesList.size() > 1) {
                    try {
                        ChartDataModel chartDataModel = new ChartDataModel();
                        System.out.println("Attribute: " + jeVisAttribute);
                        System.out.println("New samples: " + samplesList);
                        JEVisObject object = jeVisAttribute.getObject();

                        List<Integer> list = new ArrayList<>();
                        list.add(0);
                        chartDataModel.setSelectedCharts(list);

                        if (object.getJEVisClassName().equals("Data"))
                            chartDataModel.setObject(object);
                        else if (jeVisAttribute.getObject().getJEVisClassName().equals("Clean Data")) {
                            chartDataModel.setDataProcessor(object);
                            chartDataModel.setObject(object.getParents().get(0));
                        }

//                        List<Integer> list = new ArrayList<>()s
                        chartDataModel.setAttribute(jeVisAttribute);

                        List<JEVisSample> samples = new ArrayList<>();
                        samples.add(samplesList.get((samplesList.size() - 1)));
                        System.out.println("Value: " + samples);
                        chartDataModel.setSamples(samples);

//                        chartDataModel.setSelectedStart(samplesList.get(samplesList.size() - 1).getTimestamp());
//                        chartDataModel.setSelectedEnd(samplesList.get(samplesList.size() - 1).getTimestamp());
                        chartDataModel.setColor(ColorColumn.color_list[i.get()]);


                        i.set(i.get() + 1);
                        chartDataModel.setSomethingChanged(true);
                        chartDataModelList.add(chartDataModel);


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("no data");
                }
                //
                Platform.runLater(() -> {
                    chart.updateChart();
//                    setChartLabel((javafx.scene.chart.PieChart) chart.getChart(), config.fontColor.get());
                });

            });
        });


    }

    @Override
    public void configChanged() {

    }

    @Override
    public SampleHandler getSampleHandler() {
        System.out.println("getSampleHandler");
        return simpleDataHandler;
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
//        minSize = new Size(250, 250);

        BorderPane bp = new BorderPane();
        bp.setStyle("-fx-background-color: transparent");


//        chart = new LineChart(chartDataModelList, false, ManipulationMode.NONE, 0, "");
        chart = new PieChart(chartDataModelList, false, 0, "");

        chart.setLegendMode(true);
        javafx.scene.chart.PieChart fxPie = (javafx.scene.chart.PieChart) chart.getChart();

//        chart.setMaxSize(100, 100);

        fxPie.setMaxSize(config.size.get().getWidth(), config.size.get().getHeight());
        fxPie.setPrefSize(config.size.get().getWidth(), config.size.get().getHeight());
        config.size.addListener((observable, oldValue, newValue) -> {
            fxPie.setMaxSize(newValue.getWidth(), newValue.getHeight());
            fxPie.setPrefSize(newValue.getWidth(), newValue.getHeight());
        });

        fxPie.setAnimated(true);
//        chart.setLegendVisible(false);


//        setChartLabel(fxPie, config.fontColor.get());
//        config.fontColor.addListener((observable, oldValue, newValue) -> {
//            setChartLabel(fxPie, newValue);
//
//        });


        rootNode.setCenter(fxPie);
        setGraphic(rootNode);
    }

    private void setChartLabel(javafx.scene.chart.PieChart chart, Color newValue) {
//        chart.getY1Axis().setTickLabelFill(newValue);
//        chart.getXAxis().setTickLabelFill(newValue);
//
//        chart.getXAxis().setLabel("");
//        chart.getY1Axis().setLabel("");


    }

    @Override
    public String typeID() {
        return "Pie";
    }
}
