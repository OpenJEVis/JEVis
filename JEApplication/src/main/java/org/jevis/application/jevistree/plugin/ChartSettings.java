package org.jevis.application.jevistree.plugin;

public class ChartSettings {

    private String name;
    private ChartType chartType;

    public ChartSettings() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public enum ChartType {AREA, LINE, BAR, BUBBLE, SCATTER, PIE}
}
