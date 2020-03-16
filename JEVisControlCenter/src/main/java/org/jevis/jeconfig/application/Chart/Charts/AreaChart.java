package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataModel;

import java.util.List;

public class AreaChart extends org.jevis.jeconfig.application.Chart.Charts.XYChart {
    private static final Logger logger = LogManager.getLogger(AreaChart.class);


    public AreaChart(AnalysisDataModel analysisDataModel, List<ChartDataModel> chartDataModels, ChartSetting chartSetting) {
        super(analysisDataModel, chartDataModels, chartSetting);
    }
}