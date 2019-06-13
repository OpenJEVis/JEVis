package org.jevis.commons.report;

public class ReportPeriodConfiguration {
    private String reportAggregation;
    private PeriodMode periodMode;

    public ReportPeriodConfiguration(String reportAggregation, PeriodMode periodMode) {
        this.reportAggregation = reportAggregation;
        this.periodMode = periodMode;
    }

    public String getReportAggregation() {
        return reportAggregation;
    }

    public void setReportAggregation(String reportAggregation) {
        this.reportAggregation = reportAggregation;
    }

    public PeriodMode getPeriodMode() {
        return periodMode;
    }

    public void setPeriodMode(PeriodMode periodMode) {
        this.periodMode = periodMode;
    }
}
