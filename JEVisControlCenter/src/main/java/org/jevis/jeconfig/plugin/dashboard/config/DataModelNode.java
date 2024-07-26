package org.jevis.jeconfig.plugin.dashboard.config;

import org.jevis.jeconfig.application.Chart.ChartType;

import java.util.ArrayList;
import java.util.List;

public class DataModelNode {

    private List<DataPointNode> data = new ArrayList<>();
    private String type;
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

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

}
