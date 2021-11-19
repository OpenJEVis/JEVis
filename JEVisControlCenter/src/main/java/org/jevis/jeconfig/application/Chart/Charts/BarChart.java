package org.jevis.jeconfig.application.Chart.Charts;

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
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.BarChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BarChart implements Chart {
    private static final Logger logger = LogManager.getLogger(BarChart.class);
    AtomicReference<DateTime> timeStampOfFirstSample = new AtomicReference<>(DateTime.now());
    AtomicReference<DateTime> timeStampOfLastSample = new AtomicReference<>(new DateTime(1990, 1, 1, 0, 0, 0));
    NumberAxis y1Axis = new NumberAxis();
    NumberAxis y2Axis = new NumberAxis();
    private String unit;
    private final AnalysisDataModel analysisDataModel;
    private final List<ChartDataRow> chartDataRows;
    private final Boolean hideShowIcons;
    private final ChartSetting chartSetting;
    private final List<BarChartSerie> barChartSerieList = new ArrayList<>();
    private javafx.scene.chart.BarChart barChart;
    private final List<Color> hexColors = new ArrayList<>();
    private DateTime valueForDisplay;
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Region barChartRegion;
    private Period period;
    private Region areaChartRegion;
    private final boolean asDuration = false;
    private AtomicReference<ManipulationMode> manipulationMode;
    private DateTime nearest;

    public BarChart(AnalysisDataModel analysisDataModel, List<ChartDataRow> chartDataRows, ChartSetting chartSetting) {
        this.analysisDataModel = analysisDataModel;
        this.chartDataRows = chartDataRows;
        this.hideShowIcons = analysisDataModel.getShowIcons();
        this.chartSetting = chartSetting;

        double totalJob = chartDataRows.size();

        JEConfig.getStatusBar().startProgressJob(org.jevis.jeconfig.application.Chart.Charts.XYChart.JOB_NAME, totalJob, I18n.getInstance().getString("plugin.graph.message.startupdate"));

        init();
    }

    private void init() {
        manipulationMode = new AtomicReference<>(ManipulationMode.NONE);

        chartDataRows.forEach(singleRow -> {
            if (!singleRow.getSelectedcharts().isEmpty()) {
                try {
                    BarChartSerie serie = new BarChartSerie(chartSetting, singleRow, analysisDataModel.getGlobalAnalysisTimeFrame().getTimeFrame() == TimeFrame.CURRENT);
                    barChartSerieList.add(serie);
                    hexColors.add(ColorHelper.toColor(singleRow.getColor()));

                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }
        });

        if (chartDataRows != null && chartDataRows.size() > 0) {
            unit = UnitManager.getInstance().format(chartDataRows.get(0).getUnit());
            if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");
        }

        NumberAxis numberAxis = new NumberAxis();
        CategoryAxis catAxis = new CategoryAxis();

        barChart = new javafx.scene.chart.BarChart<>(numberAxis, catAxis);

        barChart.setTitle(chartSetting.getName());
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
    public void setRegion(Region region) {
        barChartRegion = region;
    }

    @Override
    public List<ChartDataRow> getChartDataRows() {
        return chartDataRows;
    }

    @Override
    public ChartSetting getChartSetting() {
        return chartSetting;
    }

    @Override
    public String getChartName() {
        return chartSetting.getName();
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return chartSetting.getId();
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
    public ChartType getChartType() {
        return ChartType.BAR;
    }

    @Override
    public Region getRegion() {
        return barChart;
    }


}