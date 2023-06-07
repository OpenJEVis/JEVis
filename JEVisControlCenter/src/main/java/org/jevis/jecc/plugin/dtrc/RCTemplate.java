package org.jevis.jecc.plugin.dtrc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RCTemplate {

    private List<TemplateInput> templateInputs = new ArrayList<>();
    private List<TemplateInput> templateFormulaInputs = new ArrayList<>();
    private List<TemplateOutput> templateOutputs = new ArrayList<>();
    private List<TemplateFormula> templateFormulas = new ArrayList<>();
    @JsonSerialize(keyUsing = MapSerializer.class)
    @JsonProperty("intervalSelectorConfiguration")
    private Map<String, Boolean> intervalSelectorConfiguration = new HashMap<>();


    public List<TemplateInput> getTemplateInputs() {
        return templateInputs;
    }

    public void setTemplateInputs(List<TemplateInput> templateInputs) {
        this.templateInputs = templateInputs;
    }

    public List<TemplateOutput> getTemplateOutputs() {
        return templateOutputs;
    }

    public void setTemplateOutputs(List<TemplateOutput> templateOutputs) {
        this.templateOutputs = templateOutputs;
    }

    public List<TemplateFormula> getTemplateFormulas() {
        return templateFormulas;
    }

    public void setTemplateFormulas(List<TemplateFormula> templateFormulas) {
        this.templateFormulas = templateFormulas;
    }

    public List<TemplateInput> getTemplateFormulaInputs() {
        return templateFormulaInputs;
    }

    public void setTemplateFormulaInputs(List<TemplateInput> templateFormulaInputs) {
        this.templateFormulaInputs = templateFormulaInputs;
    }

    public Map<String, Boolean> getIntervalSelectorConfiguration() {
        return intervalSelectorConfiguration;
    }

    public void setIntervalSelectorConfiguration(Map<String, Boolean> intervalSelectorConfiguration) {
        this.intervalSelectorConfiguration = intervalSelectorConfiguration;
    }
}
