package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.plugin.charts.ToolBarSettings;

import java.util.Comparator;
import java.util.List;

public class TableChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChart.class);
    private ChartDataRow singleRow;
    private final TableTopDatePicker tableTopDatePicker = new TableTopDatePicker();
    private TableHeaderTable tableHeader;

    public TableChart(JEVisDataSource ds) {
        super(ds);
    }

    @Override
    public void createChart(ChartModel chartModel, List<ChartDataRow> dataRows, ToolBarSettings toolBarSettings, DataSettings dataSettings, boolean instant) {
        this.chartModel = chartModel;
        this.chartDataRows = dataRows;

        if (!instant) {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        buildChart(toolBarSettings, dataSettings);

                        tableTopDatePicker.initialize(singleRow, timeStampOfLastSample.get());
                    } catch (Exception e) {
                        this.failed();
                        logger.error("Could not build chart {}", chartModel.getChartName(), e);
                    } finally {
                        succeeded();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(TableChart.class.getName(), task, taskImage, true);
        } else {
            buildChart(toolBarSettings, dataSettings);
        }
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {
        this.singleRow = singleRow;
        TableSerie serie = new TableSerie(chartModel, singleRow, showIcons);

        getHexColors().add(singleRow.getColor());

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

        return serie;
    }

    @Override
    public void addSeriesToChart() {
        xyChartSerieList.sort(Comparator.comparingDouble(XYChartSerie::getSortCriteria));


        for (XYChartSerie xyChartSerie : xyChartSerieList) {
            tableData.add(xyChartSerie.getTableEntry());
        }

        AlphanumComparator ac = new AlphanumComparator();

        Platform.runLater(() -> tableData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));
    }

    public HBox getTopPicker() {
        return tableTopDatePicker;
    }

    @Override
    public void generateYAxis() {
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

    public TableHeaderTable getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(TableHeaderTable tableHeader) {
        this.tableHeader = tableHeader;
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }
}
