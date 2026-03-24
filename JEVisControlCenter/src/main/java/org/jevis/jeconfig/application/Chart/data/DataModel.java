package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level data model for a saved analysis in the ChartPlugin.
 * <p>
 * A {@code DataModel} aggregates all {@link ChartModel} instances that belong to one analysis
 * together with display-level settings that apply globally across charts:
 * <ul>
 *   <li>{@code autoSize} — whether charts should fill the available height automatically</li>
 *   <li>{@code chartsPerScreen} — how many charts are visible without scrolling</li>
 *   <li>{@code horizontalPies} / {@code horizontalTables} — columns in pie/table layouts</li>
 *   <li>{@code forcedInterval} — optional ISO-period string that overrides the time axis</li>
 * </ul>
 * This class is serialised to/from JSON by {@link AnalysisHandler} and is also the target of
 * Jackson's {@code readerForUpdating} when loading a saved analysis file.
 *
 * @see ChartModel
 * @see AnalysisHandler
 */
public class DataModel {


    private final SimpleBooleanProperty autoSize = new SimpleBooleanProperty(this, "autoSize", true);
    private final SimpleIntegerProperty chartsPerScreen = new SimpleIntegerProperty(this, "chartsPerScreen", 2);
    private final SimpleIntegerProperty horizontalPies = new SimpleIntegerProperty(this, "horizontalPies", 3);
    private final SimpleIntegerProperty horizontalTables = new SimpleIntegerProperty(this, "horizontalTables", 3);
    private final SimpleStringProperty forcedInterval = new SimpleStringProperty(this, "forcedInterval", "");
    private List<ChartModel> chartModels = new ArrayList<>();

    /**
     * @return the ordered list of charts that make up this analysis
     */
    public List<ChartModel> getChartModels() {
        return chartModels;
    }

    public void setChartModels(List<ChartModel> chartModels) {
        this.chartModels = chartModels;
    }

    public boolean isAutoSize() {
        return autoSize.get();
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize.set(autoSize);
    }

    public SimpleBooleanProperty autoSizeProperty() {
        return autoSize;
    }

    public int getChartsPerScreen() {
        return chartsPerScreen.get();
    }

    public void setChartsPerScreen(int chartsPerScreen) {
        this.chartsPerScreen.set(chartsPerScreen);
    }

    public SimpleIntegerProperty chartsPerScreenProperty() {
        return chartsPerScreen;
    }

    public int getHorizontalPies() {
        return horizontalPies.get();
    }

    public void setHorizontalPies(int horizontalPies) {
        this.horizontalPies.set(horizontalPies);
    }

    public SimpleIntegerProperty horizontalPiesProperty() {
        return horizontalPies;
    }

    public int getHorizontalTables() {
        return horizontalTables.get();
    }

    public void setHorizontalTables(int horizontalTables) {
        this.horizontalTables.set(horizontalTables);
    }

    public SimpleIntegerProperty horizontalTablesProperty() {
        return horizontalTables;
    }

    public String getForcedInterval() {
        return forcedInterval.get();
    }

    public void setForcedInterval(String forcedInterval) {
        this.forcedInterval.set(forcedInterval);
    }

    public SimpleStringProperty forcedIntervalProperty() {
        return forcedInterval;
    }

    /**
     * Resets this model to its default state, clearing all chart models and
     * restoring all display settings to their defaults.
     * <p>Should be called before loading a new analysis to avoid stale state.</p>
     */
    public void reset() {
        autoSize.set(true);
        chartsPerScreen.set(2);
        horizontalPies.set(3);
        horizontalTables.set(3);
        forcedInterval.set("");
        chartModels.clear();
    }
}
