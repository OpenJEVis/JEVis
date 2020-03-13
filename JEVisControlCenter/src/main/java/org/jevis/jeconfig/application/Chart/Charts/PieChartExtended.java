package org.jevis.jeconfig.application.Chart.Charts;

import com.sun.javafx.charts.Legend;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

public class PieChartExtended extends PieChart {

    private Legend legend;

    public PieChartExtended() {
        super();
        this.legend = (Legend) getLegend();
    }

    public PieChartExtended(ObservableList<Data> data) {
        super(data);
        this.legend = (Legend) getLegend();
    }

    public Legend getPieLegend() {
        return this.legend;
    }
}
