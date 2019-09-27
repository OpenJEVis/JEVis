package org.jevis.jeconfig.application.Chart.Charts;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisException;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYScatterChartSerie;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisScatterChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.regression.RegressionType;

import java.util.List;

public class ScatterChart extends XYChart {

    public ScatterChart(List<ChartDataModel> chartDataModels, Boolean showRawData, Boolean showSum, Boolean showL1L2, Boolean hideShowIcons, Boolean calcRegression, RegressionType regressionType, int polyRegressionDegree, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        super(chartDataModels, showRawData, showSum, showL1L2, hideShowIcons, calcRegression, regressionType, polyRegressionDegree, addSeriesOfType, chartId, chartName);
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataModel singleRow) throws JEVisException {
        XYChartSerie serie = new XYScatterChartSerie(singleRow, hideShowIcons);

        getHexColors().add(singleRow.getColor());
        chart.getData().add(serie.getSerie());
        tableData.add(serie.getTableEntry());

        /**
         * check if timestamps are in serie
         */

        if (serie.getTimeStampFromFirstSample().isBefore(timeStampOfFirstSample.get())) {
            timeStampOfFirstSample.set(serie.getTimeStampFromFirstSample());
            changedBoth[0] = true;
        }

        if (serie.getTimeStampFromLastSample().isAfter(timeStampOfLastSample.get())) {
            timeStampOfLastSample.set(serie.getTimeStampFromLastSample());
            changedBoth[1] = true;
        }

        /**
         * check if theres a manipulation for changing the x axis values into duration instead of concrete timestamps
         */

        checkManipulation(singleRow);
        return serie;
    }

    @Override
    public void initializeChart() {
        setChart(new MultiAxisScatterChart(dateAxis, y1Axis, y2Axis));
    }

    @Override
    public void applyColors() {
        for (int i = 0; i < getHexColors().size(); i++) {
            Color currentColor = getHexColors().get(i);
            String hexColor = toRGBCode(currentColor);

            Node node = getChart().lookup(".default-color" + i + ".chart-symbol");
//            String style = node.getStyle();

            if (node != null) {
                node.setStyle("-fx-background-color: " + hexColor + ";");
            }
        }
    }
}
