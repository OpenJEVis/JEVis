package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.ChartModel;

public class ColumnChart extends org.jevis.jeconfig.application.Chart.Charts.XYChart {
    private static final Logger logger = LogManager.getLogger(ColumnChart.class);

    public ColumnChart(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}