package org.jevis.jecc.application.Chart.data;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class DataModel {


    private final SimpleBooleanProperty autoSize = new SimpleBooleanProperty(this, "autoSize", true);
    private final SimpleIntegerProperty chartsPerScreen = new SimpleIntegerProperty(this, "chartsPerScreen", 2);
    private final SimpleIntegerProperty horizontalPies = new SimpleIntegerProperty(this, "horizontalPies", 3);
    private final SimpleIntegerProperty horizontalTables = new SimpleIntegerProperty(this, "horizontalTables", 3);
    private List<ChartModel> chartModels = new ArrayList<>();

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

    public void reset() {
        autoSize.set(true);
        chartsPerScreen.set(2);
        horizontalPies.set(3);
        horizontalTables.set(3);
        chartModels.clear();
    }
}
