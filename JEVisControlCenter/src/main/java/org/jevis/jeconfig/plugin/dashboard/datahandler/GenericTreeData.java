package org.jevis.jeconfig.plugin.dashboard.datahandler;

import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;

import java.util.HashMap;
import java.util.Map;

public class GenericTreeData {

    private JEVisTreeRow row;
    private Map<String, Object> data = new HashMap<>();

    public GenericTreeData(JEVisTreeRow row, Map<String, Object> data) {
        this.row = row;
        this.data = data;
    }

    public JEVisTreeRow getRow() {
        return this.row;
    }

    public Object getDataObject(String key, Object defaultObject) {
        return this.data.getOrDefault(key, defaultObject);
    }

    public void setDataObject(String key, Object object) {
        this.data.put(key, object);
    }
}
