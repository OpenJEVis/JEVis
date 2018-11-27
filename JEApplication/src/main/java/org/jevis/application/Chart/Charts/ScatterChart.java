package org.jevis.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartElements.TableEntry;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class ScatterChart implements Chart {
    private javafx.scene.chart.ScatterChart<Number, Number> scatterChart;
    private List<Color> hexColors = new ArrayList<>();

    public ScatterChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, Integer chartId, String chartName) {

    }

    @Override
    public String getChartName() {
        return null;
    }

    @Override
    public Integer getChartId() {
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
            Color brighter = currentColor.deriveColor(1, 1, 50, 0.3);
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = scatterChart.lookup(preIdent + ".chart-symbol");
            node.setStyle("-fx-background-color: " + hexColor + ";");
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
    public DateTime getStartDateTime() {
        return null;
    }

    @Override
    public DateTime getEndDateTime() {
        return null;
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
