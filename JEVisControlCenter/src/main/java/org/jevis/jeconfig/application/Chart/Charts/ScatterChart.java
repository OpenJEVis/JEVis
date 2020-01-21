package org.jevis.jeconfig.application.Chart.Charts;

import org.jevis.commons.chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;

import java.util.List;

public class ScatterChart extends XYChart {

    public ScatterChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, Integer chartId, String chartName) {
        super(analysisDataModel, chartDataModels, chartId, chartName);
    }
}
