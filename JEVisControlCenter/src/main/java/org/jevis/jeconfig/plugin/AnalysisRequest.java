package org.jevis.jeconfig.plugin;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.joda.time.DateTime;

public class AnalysisRequest {

    private AggregationPeriod aggregationPeriod;
    private ManipulationMode manipulationMode;
    private AnalysisTimeFrame analysisTimeFrame;
    private JEVisObject object;
    private JEVisAttribute attribute;
    private DateTime startDate;
    private DateTime endDate;

    public AnalysisRequest(JEVisObject object, AggregationPeriod aggregationPeriod, ManipulationMode manipulationMode, AnalysisTimeFrame analysisTimeFrame, DateTime startDate, DateTime endDate) {
        this.manipulationMode = manipulationMode;
        this.analysisTimeFrame = analysisTimeFrame;
        this.aggregationPeriod = aggregationPeriod;
        this.object = object;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        this.aggregationPeriod = aggregationPeriod;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode;
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        this.manipulationMode = manipulationMode;
    }

    public AnalysisTimeFrame getAnalysisTimeFrame() {
        return analysisTimeFrame;
    }

    public void setAnalysisTimeFrame(AnalysisTimeFrame analysisTimeFrame) {
        this.analysisTimeFrame = analysisTimeFrame;
    }

    public JEVisObject getObject() {
        return object;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public JEVisAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(JEVisAttribute attribute) {
        this.attribute = attribute;
    }
}
