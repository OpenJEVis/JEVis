package org.jevis.jeconfig.plugin.dtrc;

import java.util.ArrayList;
import java.util.List;

public class TemplateFormula {

    private String name = "";
    private String formula = "";
    private List<TemplateInput> inputs = new ArrayList<>();
    private String output = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public List<TemplateInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TemplateInput> inputs) {
        this.inputs = inputs;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateFormula) {
            TemplateFormula otherObj = (TemplateFormula) obj;
            return this.getFormula().equals(otherObj.getFormula());
        }

        return false;
    }
}
