package org.jevis.jeconfig.application.Chart.Charts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;

import java.util.List;

public class TableChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChart.class);

    public TableChart(List<ChartDataModel> chartDataModels, Boolean hideShowIcons, ManipulationMode addSeriesOfType, Integer chartId, String chartName) {
        super(chartDataModels, hideShowIcons, addSeriesOfType, chartId, chartName);
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataModel singleRow) throws JEVisException {
        TableSerie serie = new TableSerie(singleRow, hideShowIcons);

        hexColors.add(singleRow.getColor());
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
    public void generateYAxis() {
        y1Axis.setAutoRanging(false);
        y2Axis.setAutoRanging(false);

        y1Axis.setLowerBound(0);
        y2Axis.setLowerBound(0);

        y1Axis.setUpperBound(0);
        y2Axis.setUpperBound(0);

        y1Axis.setMaxHeight(0);
        y2Axis.setMaxHeight(0);

        y1Axis.setTickLabelsVisible(false);
        y2Axis.setTickLabelsVisible(false);

        y1Axis.setVisible(false);
        y2Axis.setVisible(false);

        y1Axis.setLabel("");
        y2Axis.setLabel("");

    }
}
