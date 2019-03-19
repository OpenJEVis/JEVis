package org.jevis.jeconfig.plugin.Dashboard.config;

import java.util.ArrayList;
import java.util.List;

public class DataModelNode {

    private List<DataPointNode> data = new ArrayList<>();
    private String type;

    public List<DataPointNode> getData() {
        return data;
    }

    public void setData(List<DataPointNode> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
