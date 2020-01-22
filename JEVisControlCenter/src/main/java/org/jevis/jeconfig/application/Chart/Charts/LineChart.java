package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;

import java.util.List;

public class LineChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LineChart.class);

    public LineChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, Integer chartId, String chartName) {
        super(analysisDataModel, chartDataModels, chartId, chartName);
    }

}