package org.jevis.jecc.application.Chart.Charts;

import de.gsi.chart.utils.DecimalStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.jecc.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jecc.application.Chart.ChartElements.XYLogicalChartSerie;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.ChartModel;

public class LogicalChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LogicalChart.class);

    public LogicalChart(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        XYLogicalChartSerie serie = new XYLogicalChartSerie(chartModel, singleRow, showIcons);

        getHexColors().add(singleRow.getColor());

        /**
         * check if timestamps are in serie
         */

        if (serie.getTimeStampOfFirstSample().isBefore(timeStampOfFirstSample.get())) {
            timeStampOfFirstSample.set(serie.getTimeStampOfFirstSample());
            changedBoth[0] = true;
        }

        if (serie.getTimeStampOfLastSample().isAfter(timeStampOfLastSample.get())) {
            timeStampOfLastSample.set(serie.getTimeStampOfLastSample());
            changedBoth[1] = true;
        }

        return serie;
    }

    @Override
    public void generateYAxis() {
        y1Axis.setAnimated(false);
        y1Axis.setForceZeroInRange(true);
        y1Axis.setAutoRanging(false);

        y1Axis.setMin(0);
        y1Axis.setMax(getMaxValue() + 1);
        y1Axis.setName("");
        y1Axis.setUnit("/");

        DecimalStringConverter tickLabelFormatter1 = new DecimalStringConverter();
        tickLabelFormatter1.setPrecision(0);
        y1Axis.setTickLabelFormatter(tickLabelFormatter1);
        y1Axis.setTickUnit(1d);
        y1Axis.setMinorTickVisible(false);
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}
