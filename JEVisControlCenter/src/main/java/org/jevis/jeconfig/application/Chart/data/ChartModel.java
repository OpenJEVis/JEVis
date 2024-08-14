package org.jevis.jeconfig.application.Chart.data;

import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.beans.property.*;
import javafx.geometry.Orientation;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.joda.time.LocalTime;

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
    private final SimpleBooleanProperty fixYAxisToZero = new SimpleBooleanProperty(this, "fixYAxisToZero", false);
    private final SimpleBooleanProperty showColumnSums = new SimpleBooleanProperty(this, "showColumnSums", false);
    private final SimpleBooleanProperty showRowSums = new SimpleBooleanProperty(this, "showRowSums", false);
    private final SimpleObjectProperty<LocalTime> dayStart = new SimpleObjectProperty<>(this, "dayStart", null);
    private final SimpleObjectProperty<LocalTime> dayEnd = new SimpleObjectProperty<>(this, "dayEnd", null);
    private final StringProperty xAxisTitle = new SimpleStringProperty(this, "xAxisTitle", null);
    private final StringProperty yAxisTitle = new SimpleStringProperty(this, "yAxisTitle", null);

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

    public boolean isFixYAxisToZero() {
        return fixYAxisToZero.get();
    }

    public void setFixYAxisToZero(boolean fixYAxisToZero) {
        this.fixYAxisToZero.set(fixYAxisToZero);
    }

    public SimpleBooleanProperty fixYAxisToZeroProperty() {
        return fixYAxisToZero;
    }

    public boolean isShowColumnSums() {
        return showColumnSums.get();
    }

    public void setShowColumnSums(boolean showColumnSums) {
        this.showColumnSums.set(showColumnSums);
    }

    public SimpleBooleanProperty showColumnSumsProperty() {
        return showColumnSums;
    }

    public boolean isShowRowSums() {
        return showRowSums.get();
    }

    public void setShowRowSums(boolean showRowSums) {
        this.showRowSums.set(showRowSums);
    }

    public SimpleBooleanProperty showRowSumsProperty() {
        return showRowSums;
    }

    public LocalTime getDayStart() {
        return dayStart.get();
    }

    public void setDayStart(LocalTime dayStart) {
        this.dayStart.set(dayStart);
    }

    public SimpleObjectProperty<LocalTime> dayStartProperty() {
        return dayStart;
    }

    public LocalTime getDayEnd() {
        return dayEnd.get();
    }

    public void setDayEnd(LocalTime dayEnd) {
        this.dayEnd.set(dayEnd);
    }

    public SimpleObjectProperty<LocalTime> dayEndProperty() {
        return dayEnd;
    }

    public String getxAxisTitle() {
        return xAxisTitle.get();
    }

    public void setxAxisTitle(String title) {
        this.xAxisTitle.set(title);
    }

    public StringProperty xAxisTitleProperty() {
        return xAxisTitle;
    }

    public String getyAxisTitle() {
        return yAxisTitle.get();
    }

    public void setyAxisTitle(String title) {
        this.yAxisTitle.set(title);
    }

    public StringProperty yAxisTitleProperty() {
        return yAxisTitle;
    }
}
