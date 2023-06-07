package org.jevis.jecc.application.Chart;

import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.geometry.Orientation;

import static org.jevis.jecc.application.Chart.ChartType.AREA;

public class ChartSetting {

    private Integer id;
    private String name;
    private ChartType chartType;
    private Double height;
    private ColorMapping colorMapping = ColorMapping.GREEN_YELLOW_RED;
    private Double groupingInterval;
    private Orientation orientation;
    private Integer minFractionDigits = 2;
    private Integer maxFractionDigits = 2;

    private Boolean filterEnabled = false;

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

    public Double getGroupingInterval() {
        return groupingInterval;
    }

    public void setGroupingInterval(Double groupingInterval) {
        this.groupingInterval = groupingInterval;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Integer getMinFractionDigits() {
        return minFractionDigits;
    }

    public void setMinFractionDigits(Integer minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    public Integer getMaxFractionDigits() {
        return maxFractionDigits;
    }

    public void setMaxFractionDigits(Integer maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    public Boolean getFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(Boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }
}
