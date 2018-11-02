package org.jevis.commons.json;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class JsonChartDataModel {

    private List<JsonAnalysisDataRow> listDataRows;
    private String analysisTimeFrame;

    @XmlElement(name = "arrayAnalyses")
    public List<JsonAnalysisDataRow> getListAnalyses() {
        return listDataRows;
    }

    public void setListDataRows(List<JsonAnalysisDataRow> listDataRows) {
        this.listDataRows = listDataRows;
    }

    @XmlElement(name = "analysisTimeFrame")
    public String getAnalysisTimeFrame() {
        return analysisTimeFrame;
    }

    public void setAnalysisTimeFrame(String analysisTimeFrame) {
        this.analysisTimeFrame = analysisTimeFrame;
    }
}
