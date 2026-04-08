package org.jevis.commons.datasource;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class StationData {
    private long id = -1;
    private String name = "";
    private int column;
    private Map<DateTime, Map<String, String>> data = new HashMap<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Map<DateTime, Map<String, String>> getData() {
        return data;
    }

    public void setData(Map<DateTime, Map<String, String>> data) {
        this.data = data;
    }
}
