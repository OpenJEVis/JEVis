package org.jevis.jeconfig.application.Chart.Charts;

import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataModel;

import java.util.List;

public class ScatterChart extends XYChart {

    public ScatterChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, ChartSetting chartSetting) {
        super(analysisDataModel, chartDataModels, chartSetting);
    }
}
