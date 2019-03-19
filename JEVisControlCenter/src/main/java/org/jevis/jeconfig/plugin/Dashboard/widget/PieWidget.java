package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//import org.jevis.jeconfig.application.Chart.Charts.PieChart;

public class PieWidget extends Widget {
    private static final Logger logger = LogManager.getLogger(PieWidget.class);
    public static String WIDGET_ID = "Pie";
    //    private final PieChart chart = new PieChart();
//    private PieChart chart;
    private PieChart chart;
    private Pane chartPane = new Pane();
    private NumberFormat nf = NumberFormat.getInstance();

    private DataModelDataHandler sampleHandler;
    private Legend legend = new Legend();
    private BorderPane rootPane = new BorderPane();

    public PieWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public PieWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        logger.info("Pie.Update: {}", interval);
        chart = new PieChart();
        sampleHandler.setInterval(interval);
        sampleHandler.update();

        //if config changed
        legend.getItems().clear();

        ObservableList<PieChart.Data> series = FXCollections.observableArrayList();
        List<Color> colors = new ArrayList<>();
        if (config.hasChanged("")) {
            System.out.println("--update settings");

            chart.setLabelsVisible(true);
            chart.setLabelLineLength(20);
            chart.setLegendVisible(false);
            chart.setAnimated(true);

            AtomicDouble total = new AtomicDouble(0);
            sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    if (!chartDataModel.getSamples().isEmpty()) {
                        total.set(total.get() + chartDataModel.getSamples().get(chartDataModel.getSamples().size() - 1).getValueAsDouble());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));


            sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    String dataName = chartDataModel.getObject().getName();
                    double value = 0;


                    if (!chartDataModel.getSamples().isEmpty()) {
                        try {
                            value = chartDataModel.getSamples().get(chartDataModel.getSamples().size() - 1).getValueAsDouble();

                            nf.setMinimumFractionDigits(2);
                            nf.setMaximumFractionDigits(2);

                        } catch (Exception ex) {
                            value = 0;
                        }
                    } else {
                        value = 0;
                    }

                    double proC = (value / total.get()) * 100;
                    if (Double.isInfinite(proC)) proC = 100;
                    if (Double.isNaN(proC)) proC = 0;

                    String textValue = nf.format(proC) + "%" + " ( " + nf.format(value) + " " + chartDataModel.getUnit() + ")";

                    legend.getItems().add(buildLegendItem(
                            dataName + " " + textValue
                            , chartDataModel.getColor(), config.fontColor.getValue()));


                    javafx.scene.chart.PieChart.Data pieData = new javafx.scene.chart.PieChart.Data(dataName + "\n" + textValue, value);

                    series.add(pieData);
                    colors.add(chartDataModel.getColor());


                } catch (Exception ex) {

                }
            });

            chart.setData(series);
            applyColors(colors);
            chart.layout();
        }


        Platform.runLater(() -> {
            rootPane.setCenter(chart);
            chart.layout();
        });
    }


    @Override
    public void init() {

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
            System.out.println("DMH.json1: " + jsonInString);

            sampleHandler = new DataModelDataHandler(getDataSource(), config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
            sampleHandler.setMultiSelect(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        chart = new PieChart();
        /** Dummy data to render pie**/
        ObservableList<PieChart.Data> series = FXCollections.observableArrayList();
        series.add(new javafx.scene.chart.PieChart.Data("A", 1));
        series.add(new javafx.scene.chart.PieChart.Data("B", 1));
        series.add(new javafx.scene.chart.PieChart.Data("C", 1));
        series.add(new javafx.scene.chart.PieChart.Data("D", 1));

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 8, 5, 8));
        hBox.getChildren().add(legend);
        chart.setData(series);
        rootPane.setCenter(chart);
        rootPane.setBottom(hBox);


        legend.setAlignment(Pos.CENTER);
        setGraphic(rootPane);
    }


    private Legend.LegendItem buildLegendItem(String name, Color color, Color fontColor) {
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
            label.setTextFill(fontColor);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }

    public void applyColors(List<Color> colors) {

        for (int i = 0; i < colors.size(); i++) {

            Color currentColor = colors.get(i);
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = chart.lookup(preIdent + ".chart-pie");
            node.setStyle("-fx-pie-color: " + hexColor + ";");

//            System.out.println(preIdent + ".chart-pie " + "-fx-pie-color: " + hexColor + ";" + " color: " + currentColor.toString());
        }
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
