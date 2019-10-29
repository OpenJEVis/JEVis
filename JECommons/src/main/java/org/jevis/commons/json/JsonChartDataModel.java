package org.jevis.commons.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class JsonChartDataModel {

    private List<JsonAnalysisDataRow> listDataRows = new ArrayList<>();

    @XmlElement(name = "listDataRows")
    public List<JsonAnalysisDataRow> getListDataRows() {
        return listDataRows;
    }

    public void setListDataRows(List<JsonAnalysisDataRow> listDataRows) {
        this.listDataRows = listDataRows;
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
