package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
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
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.config2.WidgetPojo;
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
    private DataModelDataHandler sampleHandler;
    private WidgetLegend legend = new WidgetLegend();
    private ObjectMapper mapper = new ObjectMapper();
    private BorderPane borderPane = new BorderPane();
    private VBox legendPane = new VBox();

    public PieWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Pie.Update: {}", interval);

        this.sampleHandler.setAutoAggregation(true);

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();


        ObservableList<PieChart.Data> series = FXCollections.observableArrayList();
        List<Legend.LegendItem> legendItemList = new ArrayList<>();
        List<Color> colors = new ArrayList<>();

        this.borderPane.setMaxWidth(this.config.getSize().getWidth());
        this.chart.setLabelsVisible(true);
        this.chart.setLabelLineLength(18);
        this.chart.setLegendVisible(false);
        this.chart.setAnimated(false);
        this.nf.setMinimumFractionDigits(0);
        this.nf.setMaximumFractionDigits(2);


        Platform.runLater(() -> {
            this.legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        });


        /** data Update **/
        AtomicDouble total = new AtomicDouble(0);
        this.sampleHandler.getDataModel().forEach(chartDataModel -> {
            try {
//                chartDataModel.setAbsolute(true);
                Double dataModelTotal = DataModelDataHandler.getTotal(chartDataModel.getSamples());
                total.set(total.get() + dataModelTotal);

            } catch (Exception ex) {
                logger.error(ex);
            }
        });

        this.sampleHandler.getDataModel().forEach(chartDataModel -> {
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


                        textValue = this.nf.format(value) + " " + UnitManager.getInstance().format(chartDataModel.getUnitLabel()) + "\n" + this.nf.format(proC) + "%";


                    } catch (Exception ex) {
                        logger.error(ex);
                        ex.printStackTrace();
                    }
                } else {
                    logger.debug("Empty Samples for: {}", this.config.getTitle());
                    value = 1;
                    textValue = "n.a.  " + UnitManager.getInstance().format(chartDataModel.getUnitLabel()) + "\n" + this.nf.format(0) + "%";

                }


                legendItemList.add(this.legend.buildLegendItem(
                        dataName, chartDataModel.getColor(), this.config.getFontColor(), this.config.getFontSize()));

                javafx.scene.chart.PieChart.Data pieData = new javafx.scene.chart.PieChart.Data(textValue, value);
                series.add(pieData);
                colors.add(chartDataModel.getColor());


            } catch (Exception ex) {
                logger.error(ex);
            }
        });


        /** redrawing **/
        Platform.runLater(() -> {
            this.legend.getItems().setAll(legendItemList);
            this.chart.setData(series);
            applyColors(colors);
        });
    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

    }


    @Override
    public void init() {

        try {
            this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
            this.sampleHandler.setMultiSelect(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.chart = new PieChart();
        /** Dummy data to render pie**/
        ObservableList<PieChart.Data> series = FXCollections.observableArrayList();
        series.add(new javafx.scene.chart.PieChart.Data("A", 1));
        series.add(new javafx.scene.chart.PieChart.Data("B", 1));
        series.add(new javafx.scene.chart.PieChart.Data("C", 1));
        series.add(new javafx.scene.chart.PieChart.Data("D", 1));


        this.legendPane.setPadding(new Insets(10, 5, 5, 0));

        this.legend.setMaxWidth(100);
        this.legend.setPrefWidth(100);
        this.legend.setPrefHeight(10);


        Platform.runLater(() -> {
            this.legendPane.getChildren().setAll(this.legend);
            this.borderPane.setCenter(this.chart);
            this.borderPane.setRight(this.legendPane);
            setGraphic(this.borderPane);
        });


    }


    @Override
    public void openConfig() {
//        WidgetConfigEditor widgetConfigEditor = new WidgetConfigEditor(this.config);
//
//        widgetConfigEditor.addTab(this.sampleHandler.getConfigTab());
//
//        widgetConfigEditor.show();

    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/DonutWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

    public void applyColors(List<Color> colors) {

        for (int i = 0; i < colors.size(); i++) {

            Color currentColor = colors.get(i);
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = this.chart.lookup(preIdent + ".chart-pie");
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
