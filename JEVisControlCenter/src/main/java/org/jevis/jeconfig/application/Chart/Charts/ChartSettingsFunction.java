package org.jevis.jeconfig.application.Chart.Charts;

import javafx.scene.chart.Chart;

@FunctionalInterface
public interface ChartSettingsFunction {
    void applySetting(Chart chart);
}

