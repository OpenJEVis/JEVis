package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisAreaChart;

import java.util.List;

public class AreaChart extends org.jevis.jeconfig.application.Chart.Charts.XYChart {
    private static final Logger logger = LogManager.getLogger(AreaChart.class);


    public AreaChart(List<ChartDataModel> chartDataModels, Boolean showRawData, Boolean hideShowIcons, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        super(chartDataModels, showRawData, hideShowIcons, addSeriesOfType, chartId, chartName);
    }

    @Override
    public void initializeChart() {
        setChart(new MultiAxisAreaChart(dateAxis, y1Axis, y2Axis));
    }
}