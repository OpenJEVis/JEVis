package org.jevis.jeconfig.plugin.dtrc;

import java.util.UUID;

public class TemplateOutput {

    private String id;
    private String name;
    private String variableName;
    private String unit;
    private Boolean showLabel;

    private Boolean link = false;
    private String target;

    private Integer column;
    private Integer row;
    private Integer colSpan = 1;
    private Integer rowSpan = 1;
    private Boolean nameBold = false;
    private Boolean resultBold = false;
    private Boolean separator = false;

    public TemplateOutput() {
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

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getShowLabel() {
        return showLabel;
    }

    public void setShowLabel(Boolean showLabel) {
        this.showLabel = showLabel;
    }

    public Boolean getLink() {
        return link;
    }

    public void setLink(Boolean link) {
        this.link = link;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getColSpan() {
        return colSpan;
    }

    public void setColSpan(Integer colSpan) {
        this.colSpan = colSpan;
    }

    public Integer getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(Integer rowSpan) {
        this.rowSpan = rowSpan;
    }

    public Boolean getResultBold() {
        return resultBold;
    }

    public void setResultBold(Boolean resultBold) {
        this.resultBold = resultBold;
    }

    public Boolean getNameBold() {
        return nameBold;
    }

    public void setNameBold(Boolean nameBold) {
        this.nameBold = nameBold;
    }

    public Boolean getSeparator() {
        return separator;
    }

    public void setSeparator(Boolean separator) {
        this.separator = separator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateOutput) {
            TemplateOutput otherObj = (TemplateOutput) obj;
            return this.getId().equals(otherObj.getId());
        }

        return false;
    }
}
