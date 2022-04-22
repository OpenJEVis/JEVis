package org.jevis.commons.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class JsonAnalysis {

    private List<JsonAnalysisDataRow> listDataRows = new ArrayList<>();
    private List<JsonChartSetting> listSettings = new ArrayList<>();
    private String isAutoSize = Boolean.TRUE.toString();

    @XmlElement(name = "listDataRows")
    public List<JsonAnalysisDataRow> getListDataRows() {
        return listDataRows;
    }

    public void setListDataRows(List<JsonAnalysisDataRow> listDataRows) {
        this.listDataRows = listDataRows;
    }

    @XmlElement(name = "listSettings")
    public List<JsonChartSetting> getListSettings() {
        return listSettings;
    }

    public void setListSettings(List<JsonChartSetting> listSettings) {
        this.listSettings = listSettings;
    }

    @XmlElement(name = "autoSize")
    public String getAutoSize() {
        return isAutoSize;
    }

    public void setAutoSize(String isAutoSize) {
        this.isAutoSize = isAutoSize;
    }

    @Override
    public String toString() {
        try {
            return JsonTools.prettyObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
