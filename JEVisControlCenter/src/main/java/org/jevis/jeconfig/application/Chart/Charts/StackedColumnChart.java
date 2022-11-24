package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.ChartModel;

public class StackedColumnChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(StackedColumnChart.class);

    public StackedColumnChart(JEVisDataSource ds) {
        super(ds);
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}