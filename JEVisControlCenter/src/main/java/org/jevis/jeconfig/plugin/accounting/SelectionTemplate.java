package org.jevis.jeconfig.plugin.accounting;

import org.jevis.jeconfig.plugin.dtrc.TemplateInput;

import java.util.ArrayList;
import java.util.List;

public class SelectionTemplate {
    private List<TemplateInput> selectedInputs = new ArrayList<>();
    private Long templateSelection;


    public List<TemplateInput> getSelectedInputs() {
        return selectedInputs;
    }

    public void setSelectedInputs(List<TemplateInput> selectedInputs) {
        this.selectedInputs = selectedInputs;
    }

    public Long getTemplateSelection() {
        return templateSelection;
    }

    public void setTemplateSelection(Long templateSelection) {
        this.templateSelection = templateSelection;
    }
}
