package org.jevis.jeconfig.application.Chart.data;

import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.beans.property.*;
import javafx.geometry.Orientation;
import org.jevis.jeconfig.application.Chart.ChartType;

import java.util.ArrayList;
import java.util.List;

public class ChartModel {

    private final SimpleIntegerProperty chartId = new SimpleIntegerProperty(this, "chartId", -1);
    private final SimpleStringProperty chartName = new SimpleStringProperty(this, "chartName", "");
    private final SimpleObjectProperty<ChartType> chartType = new SimpleObjectProperty<>(this, "chartType", ChartType.LINE);
    private final SimpleDoubleProperty height = new SimpleDoubleProperty(this, "height", -1);
    private final SimpleObjectProperty<ColorMapping> colorMapping = new SimpleObjectProperty<>(this, "colorMapping", ColorMapping.GREEN_YELLOW_RED);
    private final SimpleDoubleProperty groupingInterval = new SimpleDoubleProperty(this, "groupingInterval", 30);
    private final SimpleObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);
    private final SimpleIntegerProperty minFractionDigits = new SimpleIntegerProperty(this, "minFractionDigits", 2);
    private final SimpleIntegerProperty maxFractionDigits = new SimpleIntegerProperty(this, "maxFractionDigits", 2);
    private final SimpleBooleanProperty filterEnabled = new SimpleBooleanProperty(this, "filterEnabled", false);
    private List<ChartData> chartData = new ArrayList<>();

    public List<ChartData> getChartData() {
        return chartData;
    }

    public void setChartData(List<ChartData> chartData) {
        this.chartData = chartData;
    }

    public int getChartId() {
        return chartId.get();
    }

    public void setChartId(int chartId) {
        this.chartId.set(chartId);
    }

    public SimpleIntegerProperty chartIdProperty() {
        return chartId;
    }

    public String getChartName() {
        return chartName.get();
    }

    public void setChartName(String chartName) {
        this.chartName.set(chartName);
    }

    public SimpleStringProperty chartNameProperty() {
        return chartName;
    }

    public ChartType getChartType() {
        return chartType.get();
    }

    public void setChartType(ChartType chartType) {
        this.chartType.set(chartType);
    }

    public SimpleObjectProperty<ChartType> chartTypeProperty() {
        return chartType;
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public SimpleDoubleProperty heightProperty() {
        return height;
    }

    public ColorMapping getColorMapping() {
        return colorMapping.get();
    }

    public void setColorMapping(ColorMapping colorMapping) {
        this.colorMapping.set(colorMapping);
    }

    public SimpleObjectProperty<ColorMapping> colorMappingProperty() {
        return colorMapping;
    }

    public double getGroupingInterval() {
        return groupingInterval.get();
    }

    public void setGroupingInterval(double groupingInterval) {
        this.groupingInterval.set(groupingInterval);
    }

    public SimpleDoubleProperty groupingIntervalProperty() {
        return groupingInterval;
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    public SimpleObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public int getMinFractionDigits() {
        return minFractionDigits.get();
    }

    public void setMinFractionDigits(int minFractionDigits) {
        this.minFractionDigits.set(minFractionDigits);
    }

    public SimpleIntegerProperty minFractionDigitsProperty() {
        return minFractionDigits;
    }

    public int getMaxFractionDigits() {
        return maxFractionDigits.get();
    }

    public void setMaxFractionDigits(int maxFractionDigits) {
        this.maxFractionDigits.set(maxFractionDigits);
    }

    public SimpleIntegerProperty maxFractionDigitsProperty() {
        return maxFractionDigits;
    }

    public boolean isFilterEnabled() {
        return filterEnabled.get();
    }

    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled.set(filterEnabled);
    }

    public SimpleBooleanProperty filterEnabledProperty() {
        return filterEnabled;
    }
}
