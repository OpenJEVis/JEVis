package org.jevis.jeconfig.application.Chart.Charts;

import org.jevis.jeconfig.application.Chart.Charts.jfx.Chart;

@FunctionalInterface
public interface ChartSettingsFunction {
    void applySetting(Chart chart);
}

