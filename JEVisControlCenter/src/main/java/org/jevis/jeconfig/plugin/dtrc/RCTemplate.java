package org.jevis.jeconfig.plugin.dtrc;

import java.util.ArrayList;
import java.util.List;

public class RCTemplate {

    private List<TemplateInput> templateInputs = new ArrayList<>();
    private List<TemplateOutput> templateOutputs = new ArrayList<>();
    private List<TemplateFormula> templateFormulas = new ArrayList<>();

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
}
