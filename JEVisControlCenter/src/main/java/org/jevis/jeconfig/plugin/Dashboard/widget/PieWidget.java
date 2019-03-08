package org.jevis.jeconfig.plugin.Dashboard.widget;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.PieChart;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

public class PieWidget extends Widget {
    private static final Logger logger = LogManager.getLogger(PieChart.class);
    public static String WIDGET_ID = "Pie";
    //    private final PieChart chart = new PieChart();
    private PieChart chart;
    private DataModelDataHandler sampleHandler;

    public PieWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public PieWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        logger.info("Update: {}", interval);
        //if config changed
        if (config.hasChanged("")) {
            chart.setLegendMode(true);

            chart.setChartSettings(chart1 -> {
                javafx.scene.chart.PieChart pieChart = (javafx.scene.chart.PieChart) chart1;
                pieChart.setLabelsVisible(false);
                pieChart.setLabelLineLength(10);
                pieChart.setLegendSide(Side.BOTTOM);
                pieChart.setAnimated(true);
            });


        }

        sampleHandler.setInterval(interval);
        sampleHandler.update();

        Platform.runLater(() -> {
            chart.updateChart();
//            setChartLabel((MultiAxisLineChart) lineChart.getChart(), config.fontColor.get());


        });


//        /**
//         * Update the data
//         */
////        chart.getData().clear();
//        ObservableList<PieChart.Data> datas = FXCollections.observableArrayList();
//
//        AtomicInteger index = new AtomicInteger(0);
//        sampleHandler.setInterval(interval);
//        sampleHandler.update();
//        sampleHandler.getValuePropertyMap().forEach((s, samplesList) -> {
//            try {
//                logger.info("Data in new Interval: {}", samplesList);
//                String name = sampleHandler.getAttributeMap().get(s).getObject().getName();
//                if (!samplesList.isEmpty()) {
//                    double value = samplesList.get(samplesList.size() - 1).getValueAsDouble();
//                    PieChart.Data pieData = new PieChart.Data(name, value);
////                    chart.getData().add(pieData);
//                    datas.add(pieData);
//                } else {
//                    PieChart.Data pieData = new PieChart.Data(name, 0.0);
////                    chart.getData().add(pieData);
//                    datas.add(pieData);
//                }
//                index.set(index.get() + 1);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//        chart.setData(datas);

//        applyColors(chart, Arrays.asList(ColorColumn.color_list));


    }

//    public void applyColors(PieChart chart, List<Color> colors) {
//        for (int i = 0; i < colors.size(); i++) {
//            try {
//                Color currentColor = colors.get(i);
//                String hexColor = toRGBCode(currentColor);
//                String preIdent = ".default-color" + i;
//                Node node = chart.lookup(preIdent + ".chart-pie");
//                node.setStyle("-fx-pie-color: " + hexColor + ";");
//            } catch (Exception ex) {
//            }
//        }
//    }
//
//    private String toRGBCode(Color color) {
//        return String.format("#%02X%02X%02X",
//                (int) (color.getRed() * 255),
//                (int) (color.getGreen() * 255),
//                (int) (color.getBlue() * 255));
//    }


    @Override
    public void init() {

        sampleHandler = new DataModelDataHandler(getDataSource(), config.getDataHandlerNode());
        sampleHandler.setMultiSelect(true);

        chart = new PieChart(sampleHandler.getDataModel(), false, 0, "");


        setGraphic(chart.getChart());
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }
}
