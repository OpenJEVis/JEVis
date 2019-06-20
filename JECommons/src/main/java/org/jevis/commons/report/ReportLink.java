package org.jevis.commons.report;

public class ReportLink {
    private String name;
    private Long jEVisID;
    private boolean optional;
    private String templateVariableName;

    private ReportAttribute reportAttribute;

    public ReportLink(String name, Long jEVisID, boolean optional, String templateVariableName, ReportAttribute reportAttribute) {
        this.name = name;
        this.jEVisID = jEVisID;
        this.optional = optional;
        this.templateVariableName = templateVariableName;
        this.reportAttribute = reportAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getjEVisID() {
        return jEVisID;
    }

    public void setjEVisID(Long jEVisID) {
        this.jEVisID = jEVisID;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getTemplateVariableName() {
        return templateVariableName;
    }

    public void setTemplateVariableName(String templateVariableName) {
        this.templateVariableName = templateVariableName;
    }


    public ReportAttribute getReportAttribute() {
        return reportAttribute;
    }

    public void setReportAttribute(ReportAttribute reportAttribute) {
        this.reportAttribute = reportAttribute;
    }

    public ReportLink clone() {
        ReportLink clonedReportLink = new ReportLink("", null, false, "", new ReportAttribute("Value", new ReportPeriodConfiguration("NONE", PeriodMode.CURRENT)));
        clonedReportLink.setName(this.getName());
        clonedReportLink.setTemplateVariableName(this.getTemplateVariableName());
        clonedReportLink.setjEVisID(this.getjEVisID());
        clonedReportLink.setOptional(this.isOptional());

        clonedReportLink.getReportAttribute().setAttributeName(getReportAttribute().getAttributeName());

        clonedReportLink.getReportAttribute().getReportPeriodConfiguration().setPeriodMode(getReportAttribute().getReportPeriodConfiguration().getPeriodMode());
        clonedReportLink.getReportAttribute().getReportPeriodConfiguration().setReportAggregation(getReportAttribute().getReportPeriodConfiguration().getReportAggregation());

        return clonedReportLink;
    }
}

