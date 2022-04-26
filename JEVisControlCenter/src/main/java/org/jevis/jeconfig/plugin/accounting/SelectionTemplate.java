package org.jevis.jeconfig.plugin.accounting;

import org.jevis.jeconfig.plugin.dtrc.TemplateInput;
import org.jevis.jeconfig.plugin.dtrc.TemplateOutput;

import java.util.ArrayList;
import java.util.List;

public class SelectionTemplate {
    private List<TemplateInput> selectedInputs = new ArrayList<>();
    private List<TemplateOutput> linkedOutputs = new ArrayList<>();
    private Long templateSelection;
    private String contractNumber;
    private String contractType;
    private String marketLocationNumber;
    private String contractDate;
    private String firstRate;
    private String periodOfNotice;
    private String contractStart;
    private String contractEnd;


    public List<TemplateInput> getSelectedInputs() {
        return selectedInputs;
    }

    public void setSelectedInputs(List<TemplateInput> selectedInputs) {
        this.selectedInputs = selectedInputs;
    }

    public List<TemplateOutput> getLinkedOutputs() {
        return linkedOutputs;
    }

    public void setLinkedOutputs(List<TemplateOutput> linkedOutputs) {
        this.linkedOutputs = linkedOutputs;
    }

    public Long getTemplateSelection() {
        return templateSelection;
    }

    public void setTemplateSelection(Long templateSelection) {
        this.templateSelection = templateSelection;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getMarketLocationNumber() {
        return marketLocationNumber;
    }

    public void setMarketLocationNumber(String marketLocationNumber) {
        this.marketLocationNumber = marketLocationNumber;
    }

    public String getContractStart() {
        return contractStart;
    }

    public void setContractStart(String contractStart) {
        this.contractStart = contractStart;
    }

    public String getContractEnd() {
        return contractEnd;
    }

    public void setContractEnd(String contractEnd) {
        this.contractEnd = contractEnd;
    }

    public String getFirstRate() {
        return firstRate;
    }

    public void setFirstRate(String firstRate) {
        this.firstRate = firstRate;
    }

    public String getContractDate() {
        return contractDate;
    }

    public void setContractDate(String contractDate) {
        this.contractDate = contractDate;
    }

    public String getPeriodOfNotice() {
        return periodOfNotice;
    }

    public void setPeriodOfNotice(String periodOfNotice) {
        this.periodOfNotice = periodOfNotice;
    }
}
