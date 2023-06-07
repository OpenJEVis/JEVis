package org.jevis.jecc.plugin.dtrc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemplateFormula {

    private String id;
    private String name = "";
    private String formula = "";
    private List<String> inputIds = new ArrayList<>();
    private String output = "";
    private Boolean timeRestrictionEnabled = false;
    private String fixedTimeFrame;
    private String reducingTimeFrame;

    public TemplateFormula() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public List<String> getInputIds() {
        return inputIds;
    }

    public void setInputIds(List<String> inputIds) {
        this.inputIds = inputIds;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Boolean getTimeRestrictionEnabled() {
        return timeRestrictionEnabled;
    }

    public void setTimeRestrictionEnabled(Boolean timeRestrictionEnabled) {
        this.timeRestrictionEnabled = timeRestrictionEnabled;
    }

    public String getFixedTimeFrame() {
        return fixedTimeFrame;
    }

    public void setFixedTimeFrame(String fixedTimeFrame) {
        this.fixedTimeFrame = fixedTimeFrame;
    }

    public String getReducingTimeFrame() {
        return reducingTimeFrame;
    }

    public void setReducingTimeFrame(String reducingTimeFrame) {
        this.reducingTimeFrame = reducingTimeFrame;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateFormula) {
            TemplateFormula otherObj = (TemplateFormula) obj;
            return this.getId().equals(otherObj.getId());
        }

        return false;
    }
}
