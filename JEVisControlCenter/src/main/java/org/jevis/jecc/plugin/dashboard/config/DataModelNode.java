package org.jevis.jecc.plugin.dashboard.config;

import org.jevis.jecc.application.Chart.ChartType;

import java.util.ArrayList;
import java.util.List;

public class DataModelNode {

    private List<DataPointNode> data = new ArrayList<>();
    private String type;
    private String forcedInterval = "";
    private ChartType chartType = ChartType.LINE;

    public List<DataPointNode> getData() {
        return this.data;
    }

    public void setData(List<DataPointNode> data) {
        this.data = data;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getForcedInterval() {
        return this.forcedInterval;
    }

    public void setForcedInterval(String forcedInterval) {
        this.forcedInterval = forcedInterval;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

}
