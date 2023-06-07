package org.jevis.jecc.application.Chart.Charts;

import org.jevis.api.JEVisDataSource;
import org.jevis.jecc.application.Chart.data.ChartModel;

public class ScatterChart extends XYChart {

    public ScatterChart(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}
