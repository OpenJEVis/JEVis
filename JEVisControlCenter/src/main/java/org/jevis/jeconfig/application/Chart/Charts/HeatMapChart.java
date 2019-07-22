package org.jevis.jeconfig.application.Chart.Charts;

import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.MatrixPane;
import eu.hansolo.fx.charts.data.MatrixChartItem;
import eu.hansolo.fx.charts.series.MatrixItemSeries;
import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.Zoom.ChartPanManager;
import org.jevis.jeconfig.application.Chart.Zoom.JFXChartUtil;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HeatMapChart implements Chart {

    private List<ChartDataModel> chartDataModels;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private Long Y_MAX;
    private Long X_MAX;

    private Region chartRegion;

    public HeatMapChart(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
        this.Y_MAX = 24L;
        this.X_MAX = 4L;

        init();
    }

    private void init() {
        List<MatrixChartItem> matrixData1 = new ArrayList<>();

        ChartDataModel chartDataModel = chartDataModels.get(0);
        Period period = new Period(chartDataModel.getSelectedStart(), chartDataModel.getSelectedEnd());
        HeatMapXY heatMapXY = getHeatMapXY(period);
        X_MAX = heatMapXY.getX();
        Y_MAX = heatMapXY.getY();
        chartDataModel.setAggregationPeriod(heatMapXY.getAggregationPeriod());

        List<JEVisSample> samples = chartDataModel.getSamples();
        Period inputSampleRate = null;
        try {
            inputSampleRate = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());

        } catch (JEVisException e) {
            e.printStackTrace();
        }

        HashMap<DateTime, JEVisSample> sampleHashMap = new HashMap<>();
        samples.forEach(jeVisSample -> {
            try {
                sampleHashMap.put(jeVisSample.getTimestamp(), jeVisSample);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });
        DateTime currentTS = null;
        try {
            currentTS = samples.get(0).getTimestamp();
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        int cellX = 0;
        int cellY = 0;
        for (int y = 0; y < Y_MAX; y++) {
            cellX = 0;
            for (int x = 0; x < X_MAX; x++) {
                try {
                    JEVisSample jeVisSample = sampleHashMap.get(currentTS);

                    if (jeVisSample != null) {
                        matrixData1.add(new MatrixChartItem(cellX, cellY, jeVisSample.getValueAsDouble()));
                    } else {
                        matrixData1.add(new MatrixChartItem(cellX, cellY, 0d));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentTS = currentTS.plus(inputSampleRate);
                cellX++;
            }
            cellY++;
        }

        MatrixItemSeries<MatrixChartItem> matrixItemSeries1 = new MatrixItemSeries<>(matrixData1, ChartType.MATRIX_HEATMAP);

        MatrixPane matrixHeatMap = new MatrixPane<>(matrixItemSeries1);
        matrixHeatMap.setColorMapping(ColorMapping.INFRARED_1);
        matrixHeatMap.getMatrix().setUseSpacer(false);
        matrixHeatMap.getMatrix().setColsAndRows(X_MAX.intValue(), Y_MAX.intValue());
        setRegion(matrixHeatMap);
//        chartRegion.setPrefSize(400, 400);

    }

    @Override
    public String getChartName() {
        return null;
    }

    @Override
    public void setTitle(String s) {

    }

    @Override
    public Integer getChartId() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, DateTime valueForDisplay) {

    }

    @Override
    public void updateTableZoom(Long lowerBound, Long upperBound) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {

    }

    @Override
    public void applyColors() {

    }

    @Override
    public DateTime getValueForDisplay() {
        return null;
    }

    @Override
    public void setValueForDisplay(DateTime valueForDisplay) {

    }

    @Override
    public DateTime getNearest() {
        return null;
    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return null;
    }

    @Override
    public Region getRegion() {
        return chartRegion;
    }

    @Override
    public void setRegion(Region region) {
        this.chartRegion = region;
    }

    @Override
    public void initializeZoom() {

    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return tableData;
    }

    @Override
    public Period getPeriod() {
        return null;
    }

    @Override
    public DateTime getStartDateTime() {
        return null;
    }

    @Override
    public DateTime getEndDateTime() {
        return null;
    }

    @Override
    public void updateChart() {
        init();
    }

    @Override
    public void setDataModels(List<ChartDataModel> chartDataModels) {
        this.chartDataModels = chartDataModels;
    }

    @Override
    public void setHideShowIcons(Boolean hideShowIcons) {

    }

    @Override
    public void setChartSettings(ChartSettingsFunction function) {

    }

    @Override
    public ChartPanManager getPanner() {
        return null;
    }

    @Override
    public JFXChartUtil getJfxChartUtil() {
        return null;
    }

    private HeatMapXY getHeatMapXY(Period period) {
        long y;
        long x;
        AggregationPeriod aggregationPeriod;

        if (period.getYears() > 1) {
            y = period.getYears();
            x = 12;
            aggregationPeriod = AggregationPeriod.MONTHLY;
        } else if (period.getYears() == 1) {
            int months = period.getMonths();
            Period newPeriod = period.minusMonths(months);
            y = newPeriod.toStandardWeeks().getWeeks();
            y += months * 4;
            x = 7;
            aggregationPeriod = AggregationPeriod.DAILY;
        } else if (period.getMonths() > 1) {
            y = period.getMonths();
            x = 31;
            aggregationPeriod = AggregationPeriod.DAILY;
        } else if (period.getMonths() == 1) {
            Period newPeriod = period.minusMonths(1);
            y = newPeriod.toStandardWeeks().getWeeks();
            y += 4;
            x = 7;
            aggregationPeriod = AggregationPeriod.DAILY;
        } else if (period.getWeeks() > 1) {
            y = period.getWeeks();
            x = 7 * 24;
            aggregationPeriod = AggregationPeriod.HOURLY;
        } else if (period.getWeeks() == 1) {
            y = period.toStandardDays().getDays();
            x = 24;
            aggregationPeriod = AggregationPeriod.HOURLY;
        } else if (period.getDays() > 1) {
            y = period.getDays();
            x = 24;
            aggregationPeriod = AggregationPeriod.HOURLY;
        } else if (period.getDays() == 1) {
            y = period.toStandardHours().getHours();
            x = 4;
            aggregationPeriod = AggregationPeriod.NONE;
        } else if (period.getHours() > 1) {
            y = period.getHours();
            x = 4;
            aggregationPeriod = AggregationPeriod.NONE;
        } else {
            y = 1;
            x = 4;
            aggregationPeriod = AggregationPeriod.NONE;
        }

        return new HeatMapXY(x, y, aggregationPeriod);
    }
}
