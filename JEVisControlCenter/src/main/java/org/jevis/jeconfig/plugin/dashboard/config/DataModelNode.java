package org.jevis.jeconfig.plugin.dashboard.config;

import java.util.ArrayList;
import java.util.List;

public class DataModelNode {

    private List<DataPointNode> data = new ArrayList<>();
    private String type;
    private String forcedInterval = "";

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
}
