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
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfigEditor;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.joda.time.Interval;

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

        sampleHandler.setAutoAggregation(true);

        sampleHandler.setInterval(interval);
        sampleHandler.update();


        ObservableList<PieChart.Data> series = FXCollections.observableArrayList();
        List<Legend.LegendItem> legendItemList = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        if (config.hasChanged("")) {

            borderPane.setMaxWidth(config.size.getValue().getWidth());
            chart.setLabelsVisible(true);
            chart.setLabelLineLength(18);
            chart.setLegendVisible(false);
            chart.setAnimated(false);
            chart.setMinWidth(320d);/** tmp solution for an unified look**/
            chart.setMaxWidth(320d);

//            nf.setMinimumFractionDigits(config.decimals.getValue());
//            nf.setMaximumFractionDigits(config.decimals.getValue());
            nf.setMinimumFractionDigits(0);/** tmp solution**/
            nf.setMaximumFractionDigits(0);


            Platform.runLater(() -> {
                //            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
                legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));


            });
        }

        /** data Update **/
        AtomicDouble total = new AtomicDouble(0);
        sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
                chartDataModel.setAbsolute(true);
                Double dataModelTotal = DataModelDataHandler.getTotal(chartDataModel.getSamples());
                total.set(total.get() + dataModelTotal);
            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
                double dpSum = 0d;

                String dataName = chartDataModel.getObject().getName();
                double value = 0;


                boolean hasNoData = chartDataModel.getSamples().isEmpty();

                String textValue = "";


                if (!hasNoData) {
                    logger.debug("Samples: ({}) {}", dataName, chartDataModel.getSamples());
                    try {
                        value = DataModelDataHandler.getTotal(chartDataModel.getSamples());

                        double proC = (value / total.get()) * 100;
                        if (Double.isInfinite(proC)) proC = 100;
                        if (Double.isNaN(proC)) proC = 0;


                        textValue = nf.format(value) + " " + UnitManager.getInstance().format(chartDataModel.getUnitLabel()) + "\n" + nf.format(proC) + "%";


                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                } else {
                    logger.debug("Empty Samples for: {}", config.title.get());
                    value = 1;
                    textValue = "n.a.  " + UnitManager.getInstance().format(chartDataModel.getUnitLabel()) + "\n" + nf.format(0) + "%";

                }


                legendItemList.add(legend.buildLegendItem(dataName, chartDataModel.getColor(), config.fontColor.getValue(), config.fontSize.get()));

                javafx.scene.chart.PieChart.Data pieData = new javafx.scene.chart.PieChart.Data(textValue, value);
                series.add(pieData);
                colors.add(chartDataModel.getColor());


            } catch (Exception ex) {
                logger.error(ex);
            }
        });


        /** redrawing **/
        Platform.runLater(() -> {
            legend.getItems().setAll(legendItemList);
            chart.setData(series);
            applyColors(colors);
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

//        graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), null);
//        openAnalysisButton = graphAnalysisLinker.buildLinkerButton();

        legendPane.setPadding(new Insets(10, 5, 5, 0));

        legend.setMaxWidth(100);
        legend.setPrefWidth(100);
        legend.setPrefHeight(10);

//        legendPane.getChildren().setAll(legend);
//        borderPane.setCenter(chart);
//        borderPane.setRight(legendPane);

        Platform.runLater(() -> {
            legendPane.getChildren().setAll(legend);
            borderPane.setCenter(chart);
            borderPane.setRight(legendPane);
            setGraphic(borderPane);
        });


    }


    @Override
    public void openConfig() {
        WidgetConfigEditor widgetConfigEditor = new WidgetConfigEditor(config);

        widgetConfigEditor.addTab(sampleHandler.getConfigTab());

        widgetConfigEditor.show();

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
