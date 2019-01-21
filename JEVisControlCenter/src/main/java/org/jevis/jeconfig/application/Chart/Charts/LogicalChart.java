package org.jevis.jeconfig.application.Chart.Charts;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartElements.LogicalXYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.LogicalYAxisStringConverter;

import java.util.List;

public class LogicalChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LogicalChart.class);

    public LogicalChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        super(chartDataModels, hideShowIcons, addSeriesOfType, chartId, chartName);
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataModel singleRow) throws JEVisException {
        XYChartSerie serie = new LogicalXYChartSerie(singleRow, hideShowIcons);

        hexColors.add(singleRow.getColor());
        series.add(serie.getSerie());
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
    public void applyColors() {

        for (int i = 0; i < hexColors.size(); i++) {
            Color currentColor = hexColors.get(i);
            String hexColor = toRGBCode(currentColor);
            String preIdent = ".default-color" + i;
            Node node = getChart().lookup(preIdent + ".chart-series-area-fill");
            Node nodew = getChart().lookup(preIdent + ".chart-series-area-line");

            node.setStyle("-fx-fill: " + hexColor + ";");
            nodew.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2px; ");
        }
    }

    @Override
    public void generateYAxis() {
        super.generateYAxis();

        y1Axis.setAutoRanging(false);
        y1Axis.setLowerBound(0d);
        y1Axis.setUpperBound(1d);
        y1Axis.setTickUnit(1d);
        y1Axis.setMinorTickVisible(false);
        y1Axis.setTickLabelFormatter(new LogicalYAxisStringConverter());
    }

    @Override
    public void generateXAxis(Boolean[] changedBoth) {

    }
}
