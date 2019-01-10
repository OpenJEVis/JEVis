package org.jevis.jeconfig.application.Chart.Charts;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.ChartDataModel;

import java.util.List;

public class LineChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LineChart.class);

    public LineChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        super(chartDataModels, hideShowIcons, addSeriesOfType, chartId, chartName);
    }

    @Override
    public void finalizeChart() {
        setChart(new javafx.scene.chart.LineChart<Number, Number>(dateAxis, numberAxis, series));
    }

    @Override
    public void applyColors() {
        getChart().applyCss();

        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;

            Node node = getChart().lookup(preIdent + ".chart-series-line");
            node.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
        }
    }
}