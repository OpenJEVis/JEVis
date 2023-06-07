package org.jevis.jecc.application.Chart.Charts;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartElements.BarChartSerie;
import org.jevis.jecc.application.Chart.ChartElements.TableEntry;
import org.jevis.jecc.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jecc.application.Chart.ChartType;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BarChart implements Chart {
    private static final Logger logger = LogManager.getLogger(BarChart.class);
    private final ChartModel chartModel;
    private final JEVisDataSource ds;
    private final List<ChartDataRow> chartDataRows = new ArrayList<>();
    private final List<BarChartSerie> barChartSerieList = new ArrayList<>();
    private final List<Color> hexColors = new ArrayList<>();
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private final boolean asDuration = false;
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(1990, 1, 1, 0, 0, 0));
    NumberAxis y1Axis = new NumberAxis();
    NumberAxis y2Axis = new NumberAxis();
    private String unit;
    private javafx.scene.chart.BarChart barChart;
    private DateTime valueForDisplay;
    private Region barChartRegion;
    private Period period;
    private Region areaChartRegion;
    private AtomicReference<ManipulationMode> manipulationMode;
    private DateTime nearest;

    public BarChart(JEVisDataSource ds, ChartModel chartModel) {
        this.ds = ds;
        this.chartModel = chartModel;

        double totalJob = this.chartModel.getChartData().size();

        ControlCenter.getStatusBar().startProgressJob(org.jevis.jecc.application.Chart.Charts.XYChart.JOB_NAME, totalJob, I18n.getInstance().getString("plugin.graph.message.startupdate"));

        init();
    }

    private void init() {
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        chartModel.getChartData().forEach(chartData -> {
            ChartDataRow chartDataRow = new ChartDataRow(ds, chartData);

            try {
                BarChartSerie serie = new BarChartSerie(chartModel, chartDataRow, true);
                barChartSerieList.add(serie);
                hexColors.add(chartData.getColor());

            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });

        if (chartDataRows != null && chartDataRows.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataRows.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");
        }

        NumberAxis numberAxis = new NumberAxis();
        CategoryAxis catAxis = new CategoryAxis();

        barChart = new javafx.scene.chart.BarChart<>(numberAxis, catAxis);

        barChart.setTitle(chartModel.getChartName());
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
        barChart.getXAxis().setAutoRanging(true);
        //barChart.getXAxis().setLabel(I18n.getInstance().getString("plugin.graph.chart.dateaxis.title"));
        barChart.getXAxis().setTickLabelRotation(-90);
        barChart.getXAxis().setLabel(unit);

        //initializeZoom();
//        setTimer();
        addSeriesToChart();
    }

    public void addSeriesToChart() {
        javafx.scene.chart.XYChart.Series<Number, String> serie = new XYChart.Series<>();
        for (BarChartSerie barChartSerie : barChartSerieList) {
            Platform.runLater(() -> {
                serie.getData().add(barChartSerie.getData());
                tableData.add(barChartSerie.getTableEntry());
            });
        }
        barChart.getData().add(serie);
    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return period;
    }

    @Override
    public void setPeriod(Period period) {
        this.period = period;
    }

    @Override
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }

    @Override
    public List<XYChartSerie> getXyChartSerieList() {
        return null;
    }

    @Override
    public String getChartName() {
        return chartModel.getChartName();
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return chartModel.getChartId();
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {

    }

    @Override
    public void updateTableZoom(double lowerBound, double upperBound) {

    }

    @Override
    public void applyColors() {
        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String hexColor = ColorHelper.toRGBCode(currentColor);
//            String preIdent = ".default-color" + i;
//            Node node = barChart.lookup(preIdent + ".chart-bar");
            Node node = ((javafx.scene.chart.XYChart.Series<Number, String>) barChart.getData().get(0)).getData().get(i).getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + hexColor + ";");
            }
        }
    }

    @Override
    public de.gsi.chart.Chart getChart() {
        return null;
    }

    @Override
    public void setChart(de.gsi.chart.Chart chart) {
        if (chart == null) {
            this.barChart = null;
        }
    }

    @Override
    public ChartType getChartType() {
        return ChartType.BAR;
    }

    @Override
    public Region getRegion() {
        return barChart;
    }

    @Override
    public void setRegion(Region region) {
        barChartRegion = region;
    }


}