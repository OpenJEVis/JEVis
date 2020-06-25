package org.jevis.jeconfig.application.Chart;

import eu.hansolo.fx.charts.tools.ColorMapping;

import static org.jevis.jeconfig.application.Chart.ChartType.AREA;

public class ChartSetting {

    private Integer id;
    private String name;
    private ChartType chartType;
    private Double height;
    private ColorMapping colorMapping = ColorMapping.GREEN_YELLOW_RED;
    private Long groupingInterval;

    private AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.TODAY);

    public ChartSetting(String name) {
        this.name = name;
        this.chartType = AREA;
    }

    public ChartSetting(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.chartType = AREA;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public ColorMapping getColorMapping() {
        return colorMapping;
    }

    public void setColorMapping(ColorMapping colorMapping) {
        this.colorMapping = colorMapping;
    }

    public Long getGroupingInterval() {
        return groupingInterval;
    }

    public void setGroupingInterval(Long groupingInterval) {
        this.groupingInterval = groupingInterval;
    }

    public AnalysisTimeFrame getAnalysisTimeFrame() {
        return analysisTimeFrame;
    }

    public void setAnalysisTimeFrame(AnalysisTimeFrame analysisTimeFrame) {
        this.analysisTimeFrame = analysisTimeFrame;
    }
}
