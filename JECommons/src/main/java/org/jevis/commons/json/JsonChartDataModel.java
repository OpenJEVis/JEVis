package org.jevis.commons.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class JsonChartDataModel {

    private List<JsonAnalysisDataRow> listDataRows;

    @XmlElement(name = "arrayAnalyses")
    public List<JsonAnalysisDataRow> getListAnalyses() {
        return listDataRows;
    }

    public void setListDataRows(List<JsonAnalysisDataRow> listDataRows) {
        this.listDataRows = listDataRows;
    }


    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
