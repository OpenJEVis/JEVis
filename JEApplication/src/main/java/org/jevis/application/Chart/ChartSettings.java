package org.jevis.application.Chart;

public class ChartSettings {

    private String name;
    private ChartType chartType;
    private Double height;

    public ChartSettings(String name) {
        this.name = name;
        this.chartType = ChartType.AREA;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public enum ChartType {AREA, LINE, BAR, BUBBLE, SCATTER, PIE}

}
