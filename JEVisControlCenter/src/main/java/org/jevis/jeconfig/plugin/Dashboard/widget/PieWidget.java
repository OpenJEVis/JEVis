package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import com.jfoenix.controls.JFXButton;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PieWidget extends Widget {
    private static final Logger logger = LogManager.getLogger(PieWidget.class);
    public static String WIDGET_ID = "Pie";
    private PieChart chart;
    private NumberFormat nf = NumberFormat.getInstance();
    private JFXButton openAnalysisButton = new JFXButton();
    private DataModelDataHandler sampleHandler;
    private WidgetLegend legend = new WidgetLegend();
    private GraphAnalysisLinker graphAnalysisLinker;
    private ObjectMapper mapper = new ObjectMapper();
    private BorderPane borderPane = new BorderPane();
    private VBox legendPane = new VBox();

    public PieWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public PieWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        logger.debug("Pie.Update: {}", interval);
        chart = new PieChart();
        sampleHandler.setInterval(interval);
        sampleHandler.update();

        //if config changed
        legend.getItems().clear();


        ObservableList<PieChart.Data> series = FXCollections.observableArrayList();
        List<Color> colors = new ArrayList<>();
        if (config.hasChanged("")) {

            borderPane.setMaxWidth(config.size.getValue().getWidth());
            chart.setLabelsVisible(true);
            chart.setLabelLineLength(8);
            chart.setLegendVisible(false);
            chart.setAnimated(false);
            chart.setMinWidth(320d);/** tmp solution for an unified look**/
            chart.setMaxWidth(320d);

//            nf.setMinimumFractionDigits(config.decimals.getValue());
//            nf.setMaximumFractionDigits(config.decimals.getValue());
            nf.setMinimumFractionDigits(0);/** tmp solution**/
            nf.setMaximumFractionDigits(0);


            AtomicDouble total = new AtomicDouble(0);
            sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    if (!chartDataModel.getSamples().isEmpty()) {
//                        System.out.println("Pie Sample: " + chartDataModel.getSamples());
                        total.set(total.get() + chartDataModel.getSamples().get(chartDataModel.getSamples().size() - 1).getValueAsDouble());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

//            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
            legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));


            sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {

                    String dataName = chartDataModel.getObject().getName();
                    double value = 0;


                    if (!chartDataModel.getSamples().isEmpty()) {
                        logger.error("Samples: ({}) {}", dataName, chartDataModel.getSamples());
                        try {
                            value = chartDataModel.getSamples().get(chartDataModel.getSamples().size() - 1).getValueAsDouble();


                        } catch (Exception ex) {
                            value = 0;
                        }
                    } else {
                        logger.warn("Empty Samples for: {}", config.title.get());
                        value = 0;
                    }

                    double proC = (value / total.get()) * 100;
                    if (Double.isInfinite(proC)) proC = 100;
                    if (Double.isNaN(proC)) proC = 0;

//                    String textValue = nf.format(proC) + "%" + " ( " + nf.format(value) + " " + chartDataModel.getUnit() + ")";
//                    String textValue = nf.format(proC) + "% \n test";
                    String textValue = nf.format(value) + " " + UnitManager.getInstance().format(chartDataModel.getUnit()) + "\n" + nf.format(proC) + "%";

                    legend.getItems().add(legend.buildLegendItem(dataName, chartDataModel.getColor(), config.fontColor.getValue(), config.fontSize.get()));
//                    legend.getItems().add(buildLegendItem(
//                            dataName + " " + textValue
//                            , chartDataModel.getColor(), config.fontColor.getValue()));


//                    javafx.scene.chart.PieChart.Data pieData = new javafx.scene.chart.PieChart.Data(dataName + "\n" + textValue, value);
                    javafx.scene.chart.PieChart.Data pieData = new javafx.scene.chart.PieChart.Data(textValue, value);
                    series.add(pieData);
                    colors.add(chartDataModel.getColor());


                } catch (Exception ex) {
                    logger.error(ex);
                }
            });

            try {
                GraphAnalysisLinkerNode dataModelNode = mapper.treeToValue(config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
                graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), dataModelNode);
                graphAnalysisLinker.applyConfig(openAnalysisButton, sampleHandler.getDataModel(), interval);
            } catch (Exception ex) {
                logger.error(ex);
            }


//            chart.layout();
        }


        Platform.runLater(() -> {
            chart.setData(series);
            applyColors(colors);

            legendPane.getChildren().setAll(legend);
            borderPane.setCenter(chart);
            borderPane.setRight(legendPane);


            chart.layout();
        });
    }


    @Override
    public void init() {

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString = mapper.writeValueAsString(config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));

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

        graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), null);
        openAnalysisButton = graphAnalysisLinker.buildLinkerButton();

        legendPane.setPadding(new Insets(10, 5, 5, 0));

        legend.setMaxWidth(100);
        legend.setPrefWidth(100);
        legend.setPrefHeight(10);

//        legendPane.getChildren().setAll(legend);
//        borderPane.setCenter(chart);
//        borderPane.setRight(legendPane);


        setGraphic(borderPane);


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
