package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TableChart extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChart.class);
    private ChartDataRow singleRow;
    private final TableTopDatePicker tableTopDatePicker = new TableTopDatePicker();
    private TableHeaderTable tableHeader;

    public TableChart() {
    }

    @Override
    public void createChart(AnalysisDataModel dataModel, List<ChartDataRow> dataRows, ChartSetting chartSetting, boolean instant) {
        if (!instant) {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        buildChart(dataModel, dataRows, chartSetting);

                        tableTopDatePicker.initialize(singleRow, timeStampOfLastSample.get());
                    } catch (Exception e) {
                        this.failed();
                    } finally {
                        succeeded();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(XYChart.class.getName(), task, taskImage, true);
        } else {
            buildChart(dataModel, dataRows, chartSetting);
        }
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

    @Override
    public void addSeriesToChart() {
        xyChartSerieList.sort(Comparator.comparingDouble(XYChartSerie::getSortCriteria));

        if (chartSetting.getOrientation() == Orientation.HORIZONTAL) {
            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                tableData.add(xyChartSerie.getTableEntry());
            }

            AlphanumComparator ac = new AlphanumComparator();

            Platform.runLater(() -> tableData.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName())));
        } else {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);
            tableHeader.getItems().clear();

            int sampleSize = 0;
            int lastSampleSize = 0;
            int maxSampleSeries = 0;

            List<TableColumn<ObservableList<String>, String>> tableColumns = new ArrayList<>();
            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                int index = xyChartSerieList.indexOf(xyChartSerie);

                sampleSize = Math.max(sampleSize, xyChartSerie.getSingleRow().getSamples().size());

                if (sampleSize != lastSampleSize) {
                    maxSampleSeries = index;
                }

                lastSampleSize = sampleSize;

                TableColumn<ObservableList<String>, String> column = new TableColumn<>(xyChartSerie.getTableEntryName());
                column.setStyle("-fx-alignment: CENTER-RIGHT;");
                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(index + 1)));
                tableColumns.add(column);
            }

            List<ObservableList<String>> valueList = new ArrayList<>();
            for (int i = 0; i < sampleSize; i++) {
                try {
                    List<String> values = new ArrayList<>();
                    String normalPattern = "yyyy-MM-dd HH:mm:ss";

                    JEVisSample jeVisSample = xyChartSerieList.get(maxSampleSeries).getSingleRow().getSamples().get(i);
                    DateTime dateTime = jeVisSample.getTimestamp();

                    if (jeVisSample.getAttribute().getDisplaySampleRate().equals(Period.days(1))) {
                        normalPattern = "dd. MMMM yyyy";
                    } else if (jeVisSample.getAttribute().getDisplaySampleRate().equals(Period.weeks(1))) {
                        normalPattern = "dd. MMMM yyyy";
                    } else if (jeVisSample.getAttribute().getDisplaySampleRate().equals(Period.months(1))) {
                        normalPattern = "MMMM yyyy";
                        Platform.runLater(() -> tableHeader.getColumns().get(0).setStyle("-fx-alignment: CENTER-RIGHT;"));
                    } else if (jeVisSample.getAttribute().getDisplaySampleRate().equals(Period.years(1))) {
                        normalPattern = "yyyy";
                    }

                    values.add(jeVisSample.getTimestamp().toString(normalPattern));


                    for (XYChartSerie xyChartSerie : xyChartSerieList) {
                        JEVisSample sample = xyChartSerie.getSingleRow().getSamplesMap().get(dateTime);
                        if (sample != null) {
                            values.add(nf.format(sample.getValueAsDouble() + " " + singleRow.getUnit()));
                        } else {
                            values.add("");
                        }

                    }

                    valueList.add(FXCollections.observableArrayList(values));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Platform.runLater(() -> {
                tableHeader.getColumns().addAll(tableColumns);
                tableHeader.getItems().addAll(valueList);
                tableHeader.autoFitTable();
            });
        }
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
}
