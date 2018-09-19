package org.jevis.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.TableEntry;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class BubbleChart implements Chart {
    private javafx.scene.chart.BubbleChart<Number, Number> bubbleChart;
    private List<Color> hexColors = new ArrayList<>();

    public BubbleChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, String chartName) {

    }

    @Override
    public String getChartName() {
        return null;
    }

    @Override
    public void updateTable(MouseEvent mouseEvent, Number valueForDisplay) {

    }

    @Override
    public void showNote(MouseEvent mouseEvent) {

    }

    @Override
    public void applyColors() {
        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String preIdent = ".default-color" + i;
            Node node = bubbleChart.lookup(preIdent + ".chart-series-area-fill");
            Node nodew = bubbleChart.lookup(preIdent + ".chart-series-area-line");
        }
    }

    @Override
    public Number getValueForDisplay() {
        return null;
    }

    @Override
    public void setValueForDisplay(Number valueForDisplay) {

    }

    @Override
    public javafx.scene.chart.Chart getChart() {
        return null;
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public void initializeZoom() {

    }

    @Override
    public ObservableList<TableEntry> getTableData() {
        return null;
    }

    @Override
    public Period getPeriod() {
        return null;
    }
}
