package org.jevis.jeconfig.application.Chart.Charts;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.regression.RegressionType;
import org.jevis.jeconfig.application.tools.ColorHelper;

import java.util.List;

public class LineChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LineChart.class);

    public LineChart(List<ChartDataModel> chartDataModels, Boolean showRawData, Boolean showSum, Boolean showL1L2, Boolean hideShowIcons, Boolean calcRegression, RegressionType regressionType, int polyRegressionDegree, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        super(chartDataModels, showRawData, showSum, showL1L2, hideShowIcons, calcRegression, regressionType, polyRegressionDegree, addSeriesOfType, chartId, chartName);
    }

    @Override
    public void initializeChart() {
        setChart(new MultiAxisLineChart(dateAxis, y1Axis, y2Axis));
    }

    @Override
    public void applyColors() {

        for (int i = 0; i < getHexColors().size(); i++) {
            Color currentColor = getHexColors().get(i);
            String hexColor = ColorHelper.toRGBCode(currentColor);
            String preIdent = ".default-color" + i;

            Node node = getChart().lookup(preIdent + ".chart-series-line");
            if (node != null) {
                node.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
            }
        }
    }
}