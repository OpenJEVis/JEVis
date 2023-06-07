package org.jevis.jecc.plugin.scada.data;

import java.util.List;

public class ScadaAnalysisData {

    private String author;
    private String bgMode;
    private List<ScadaElementData> elements;


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBgMode() {
        return bgMode;
    }

    public void setBgMode(String bgMode) {
        this.bgMode = bgMode;
    }

    public List<ScadaElementData> getElements() {
        return elements;
    }

    public void setElements(List<ScadaElementData> elements) {
        this.elements = elements;
    }


}
