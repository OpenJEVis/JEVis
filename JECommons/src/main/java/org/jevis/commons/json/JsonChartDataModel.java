package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class JsonChartDataModel {

    private List<JsonAnalysisDataRow> listDataRows;
    private JsonChartTimeFrame analysisTimeFrame;

    @XmlElement(name = "arrayAnalyses")
    public List<JsonAnalysisDataRow> getListAnalyses() {
        return listDataRows;
    }

    public void setListDataRows(List<JsonAnalysisDataRow> listDataRows) {
        this.listDataRows = listDataRows;
    }

    @XmlElement(name = "analysisTimeFrame")
    public JsonChartTimeFrame getAnalysisTimeFrame() {
        return analysisTimeFrame;
    }

    public void setAnalysisTimeFrame(JsonChartTimeFrame analysisTimeFrame) {
        this.analysisTimeFrame = analysisTimeFrame;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(this);
        return prettyJson;
    }
}
