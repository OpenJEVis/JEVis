package org.jevis.jeconfig.application.Chart.Charts;

import de.gsi.chart.utils.DecimalStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYLogicalChartSerie;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;

public class LogicalChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(LogicalChart.class);

    public LogicalChart() {
        super();
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        XYLogicalChartSerie serie = new XYLogicalChartSerie(singleRow, showIcons);
        setMinValue(Math.min(minValue, serie.getMinValue()));
        setMaxValue(Math.max(maxValue, serie.getMaxValue()));

        getHexColors().add(ColorHelper.toColor(singleRow.getColor()));

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
}
