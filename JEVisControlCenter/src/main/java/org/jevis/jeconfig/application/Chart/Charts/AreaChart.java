package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisAreaChart;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;

import java.util.List;

public class AreaChart extends org.jevis.jeconfig.application.Chart.Charts.XYChart {
    private static final Logger logger = LogManager.getLogger(AreaChart.class);


    public AreaChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, Integer chartId, String chartName) {
        super(analysisDataModel, chartDataModels, chartId, chartName);
    }

    @Override
    public void initializeChart() {
        setChart(new MultiAxisAreaChart(dateAxis, y1Axis, y2Axis));
    }
}