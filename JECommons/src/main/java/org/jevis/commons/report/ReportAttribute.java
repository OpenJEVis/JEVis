package org.jevis.commons.report;

public class ReportAttribute {
    private String attributeName;
    private ReportPeriodConfiguration reportPeriodConfiguration;

    public ReportAttribute(String attributeName, ReportPeriodConfiguration reportPeriodConfiguration) {
        this.attributeName = attributeName;
        this.reportPeriodConfiguration = reportPeriodConfiguration;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public ReportPeriodConfiguration getReportPeriodConfiguration() {
        return reportPeriodConfiguration;
    }

    public void setReportPeriodConfiguration(ReportPeriodConfiguration reportPeriodConfiguration) {
        this.reportPeriodConfiguration = reportPeriodConfiguration;
    }

    @Override
    public String toString() {
        return "ReportAttribute{" +
                "attributeName='" + attributeName + '\'' +
                ", reportPeriodConfiguration=" + reportPeriodConfiguration +
                '}';
    }
}
