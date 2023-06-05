package org.jevis.jeconfig.application.Chart.Charts;

import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;

public class PieChartExtended extends PieChart {

    // TODO JFX17
    // private Legend legend;

    public PieChartExtended() {
        super();
        //   this.legend = (Legend) getLegend();
    }

    public PieChartExtended(ObservableList<Data> data) {
        super(data);
        //this.legend = (Legend) getLegend();
    }

    //   public Legend getPieLegend() {
    //       return this.legend;
    //   }
}
