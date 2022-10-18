package org.jevis.jeconfig.application.Chart.Charts;

import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.ChartModel;

public class ScatterChart extends XYChart {

    public ScatterChart(JEVisDataSource ds) {
        super(ds);
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}
