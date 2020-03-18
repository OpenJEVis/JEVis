package org.jevis.jeconfig.application.Chart.Charts;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;

public class TableChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChart.class);
    private ChartDataRow singleRow;
    private TableTopDatePicker tableTopDatePicker;

    public TableChart() {
        super();

        tableTopDatePicker = new TableTopDatePicker(singleRow);
        tableTopDatePicker.setAlignment(Pos.CENTER);
        tableTopDatePicker.initialize(timeStampOfLastSample.get());


    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        this.singleRow = singleRow;
        TableSerie serie = new TableSerie(singleRow, showIcons);

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

    public HBox getTopPicker() {
        return tableTopDatePicker;
    }

    @Override
    public void generateYAxis() {
//        y1Axis.setAutoRanging(false);
//        y2Axis.setAutoRanging(false);
//
//        y1Axis.setLowerBound(0);
//        y2Axis.setLowerBound(0);
//
//        y1Axis.setUpperBound(0);
//        y2Axis.setUpperBound(0);
//
//        y1Axis.setMaxHeight(0);
//        y2Axis.setMaxHeight(0);
//
//        y1Axis.setTickLabelsVisible(false);
//        y2Axis.setTickLabelsVisible(false);
//
//        y1Axis.setVisible(false);
//        y2Axis.setVisible(false);
//
//        y1Axis.setLabel("");
//        y2Axis.setLabel("");

    }

    public TableTopDatePicker getTableTopDatePicker() {
        return tableTopDatePicker;
    }

    public ChartDataRow getSingleRow() {
        return singleRow;
    }

    private boolean blockDatePickerEvent = false;

    public boolean isBlockDatePickerEvent() {
        return blockDatePickerEvent;
    }

    public void setBlockDatePickerEvent(boolean blockDatePickerEvent) {
        this.blockDatePickerEvent = blockDatePickerEvent;
    }
}
