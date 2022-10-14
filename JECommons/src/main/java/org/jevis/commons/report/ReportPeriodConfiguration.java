package org.jevis.commons.report;

import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;

public class ReportPeriodConfiguration {
    private AggregationPeriod reportAggregation;
    private ManipulationMode reportManipulation;
    private PeriodMode periodMode;
    private FixedPeriod fixedPeriod;

    public ReportPeriodConfiguration(AggregationPeriod reportAggregation, ManipulationMode reportManipulation, PeriodMode periodMode, FixedPeriod fixedPeriod) {
        this.reportAggregation = reportAggregation;
        this.reportManipulation = reportManipulation;
        this.periodMode = periodMode;
        this.fixedPeriod = fixedPeriod;
    }

    public AggregationPeriod getReportAggregation() {
        return reportAggregation;
    }

    public void setReportAggregation(AggregationPeriod reportAggregation) {
        this.reportAggregation = reportAggregation;
    }

    public ManipulationMode getReportManipulation() {
        return reportManipulation;
    }

    public void setReportManipulation(ManipulationMode reportManipulation) {
        this.reportManipulation = reportManipulation;
    }

    public PeriodMode getPeriodMode() {
        return periodMode;
    }

    public void setPeriodMode(PeriodMode periodMode) {
        this.periodMode = periodMode;
    }

    public FixedPeriod getFixedPeriod() {
        return fixedPeriod;
    }

    public void setFixedPeriod(FixedPeriod fixedPeriod) {
        this.fixedPeriod = fixedPeriod;
    }

    @Override
    public String toString() {
        return "ReportPeriodConfiguration{" +
                "reportAggregation=" + reportAggregation +
                ", reportManipulation=" + reportManipulation +
                ", periodMode=" + periodMode +
                ", fixedPeriod=" + fixedPeriod +
                '}';
    }
}
