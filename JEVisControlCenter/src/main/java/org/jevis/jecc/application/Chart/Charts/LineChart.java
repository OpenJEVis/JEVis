package org.jevis.jecc.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jecc.application.Chart.data.ChartModel;

public class LineChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LineChart.class);

    public LineChart(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}