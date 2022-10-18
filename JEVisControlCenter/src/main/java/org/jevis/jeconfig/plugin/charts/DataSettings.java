package org.jevis.jeconfig.plugin.charts;

import javafx.beans.property.SimpleObjectProperty;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;

public class DataSettings {
    private final SimpleObjectProperty<AggregationPeriod> aggregationPeriod = new SimpleObjectProperty<>(this, "aggregationPeriod", AggregationPeriod.NONE);
    private final SimpleObjectProperty<ManipulationMode> manipulationMode = new SimpleObjectProperty<>(this, "manipulatonMode", ManipulationMode.NONE);
    private final SimpleObjectProperty<AnalysisTimeFrame> analysisTimeFrame = new SimpleObjectProperty<>(this, "analysisTimeFrame", null);
    private final SimpleObjectProperty<WorkDays> workDays = new SimpleObjectProperty<>(this, "workDays", new WorkDays(null));
    private final SimpleObjectProperty<JEVisObject> currentAnalysis = new SimpleObjectProperty<>(this, "currentAnalysis", null);

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod.get();
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.aggregationPeriod.set(aggregationPeriod);
    }

    public SimpleObjectProperty<AggregationPeriod> aggregationPeriodProperty() {
        return aggregationPeriod;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode.get();
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.manipulationMode.set(manipulationMode);
    }

    public SimpleObjectProperty<ManipulationMode> manipulationModeProperty() {
        return manipulationMode;
    }

    public AnalysisTimeFrame getAnalysisTimeFrame() {
        return analysisTimeFrame.get();
    }

    public void setAnalysisTimeFrame(AnalysisTimeFrame analysisTimeFrame) {
        this.analysisTimeFrame.set(analysisTimeFrame);
    }

    public SimpleObjectProperty<AnalysisTimeFrame> analysisTimeFrameProperty() {
        return analysisTimeFrame;
    }

    public JEVisObject getCurrentAnalysis() {
        return currentAnalysis.get();
    }

    public void setCurrentAnalysis(JEVisObject currentAnalysis) {
        this.currentAnalysis.set(currentAnalysis);
        if (currentAnalysis != null) {
            setWorkDays(new WorkDays(currentAnalysis));
        }
    }

    public SimpleObjectProperty<JEVisObject> currentAnalysisProperty() {
        return currentAnalysis;
    }

    public WorkDays getWorkDays() {
        return workDays.get();
    }

    public void setWorkDays(WorkDays workDays) {
        this.workDays.set(workDays);
    }

    public SimpleObjectProperty<WorkDays> workDaysProperty() {
        return workDays;
    }
}
