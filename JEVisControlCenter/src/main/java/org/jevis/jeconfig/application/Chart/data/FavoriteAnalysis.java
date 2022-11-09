package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.TimeFrame;

public class FavoriteAnalysis {
    private final SimpleLongProperty id = new SimpleLongProperty(this, "id", -1);
    private final SimpleObjectProperty<TimeFrame> timeFrame = new SimpleObjectProperty<>(this, "timeframe", TimeFrame.TODAY);
    private final SimpleStringProperty start = new SimpleStringProperty(this, "start", null);
    private final SimpleStringProperty end = new SimpleStringProperty(this, "end", null);
    private final SimpleObjectProperty<AggregationPeriod> aggregationPeriod = new SimpleObjectProperty<>(this, "aggregationPeriod", AggregationPeriod.NONE);
    private final SimpleObjectProperty<ManipulationMode> manipulationMode = new SimpleObjectProperty<>(this, "manipulationMode", ManipulationMode.NONE);

    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame.get();
    }

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame.set(timeFrame);
    }

    public SimpleObjectProperty<TimeFrame> timeFrameProperty() {
        return timeFrame;
    }

    public String getStart() {
        return start.get();
    }

    public void setStart(String start) {
        this.start.set(start);
    }

    public SimpleStringProperty startProperty() {
        return start;
    }

    public String getEnd() {
        return end.get();
    }

    public void setEnd(String end) {
        this.end.set(end);
    }

    public SimpleStringProperty endProperty() {
        return end;
    }

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
}
