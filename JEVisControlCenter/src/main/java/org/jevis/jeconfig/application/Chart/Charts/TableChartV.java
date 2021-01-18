package org.jevis.jeconfig.application.Chart.Charts;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
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
import org.joda.time.DateTimeComparator;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TableChartV extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChartV.class);
    private final TableTopDatePicker tableTopDatePicker = new TableTopDatePicker();
    private ChartDataRow singleRow;
    private TableHeaderTable tableHeader;
    private boolean blockDatePickerEvent = false;

    public TableChartV() {
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

            JEConfig.getStatusBar().addTask(TableChartV.class.getName(), task, taskImage, true);
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

        try {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);
            tableHeader.getItems().clear();

            List<TableColumn<List<String>, String>> tableColumns = new ArrayList<>();
            List<DateTime> dateTimes = new ArrayList<>();
            Period p = null;

            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                int index = xyChartSerieList.indexOf(xyChartSerie);
                List<JEVisSample> samples = xyChartSerie.getSingleRow().getSamples();

                if (p == null && samples.size() > 1) {
                    try {
                        p = new Period(samples.get(0).getTimestamp(),
                                samples.get(1).getTimestamp());
                        setPeriod(p);
                    } catch (Exception e) {
                        logger.error("Could not get period from samples", e);
                    }
                } else if (p == null && samples.size() == 1) {
                    try {
                        p = xyChartSerie.getSingleRow().getPeriod();
                        setPeriod(p);
                    } catch (Exception e) {
                        logger.error("Could not get period from attribute", e);
                    }
                }

                for (JEVisSample jeVisSample : samples) {
                    try {
                        if (!dateTimes.contains(jeVisSample.getTimestamp())) {
                            dateTimes.add(jeVisSample.getTimestamp());
                        }
                    } catch (JEVisException e) {
                        logger.error(e);
                    }
                }

                TableColumn<List<String>, String> column = new TableColumn<>(xyChartSerie.getTableEntryName());
                column.setStyle("-fx-alignment: CENTER-RIGHT;");
                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(index + 1)));
                tableColumns.add(column);
            }

            AlphanumComparator ac = new AlphanumComparator();
            tableColumns.sort((o1, o2) -> ac.compare(o1.getText(), o2.getText()));
            Platform.runLater(() -> tableHeader.getColumns().addAll(tableColumns));

            dateTimes.sort(DateTimeComparator.getInstance());

            String normalPattern = DateTimeFormat.patternForStyle("SS", I18n.getInstance().getLocale());

            try {
                if (getPeriod().equals(Period.days(1))) {
                    normalPattern = "dd. MMMM yyyy";
                    Platform.runLater(() -> tableHeader.getColumns().get(0).setStyle("-fx-alignment: CENTER-RIGHT;"));
                } else if (getPeriod().equals(Period.weeks(1))) {
                    normalPattern = "dd. MMMM yyyy";
                    Platform.runLater(() -> tableHeader.getColumns().get(0).setStyle("-fx-alignment: CENTER-RIGHT;"));
                } else if (getPeriod().equals(Period.months(1))) {
                    normalPattern = "MMMM yyyy";
                    Platform.runLater(() -> tableHeader.getColumns().get(0).setStyle("-fx-alignment: CENTER-RIGHT;"));
                } else if (getPeriod().equals(Period.years(1))) {
                    normalPattern = "yyyy";
                    Platform.runLater(() -> tableHeader.getColumns().get(0).setStyle("-fx-alignment: CENTER-RIGHT;"));
                }
            } catch (Exception e) {
                logger.error("Could not determine sample rate, fall back to standard", e);
            }

            List<Double> sums = new ArrayList<>();
            for (int i = 0; i < xyChartSerieList.size(); i++) sums.add(0d);

            for (DateTime dateTime : dateTimes) {
                List<String> values = new ArrayList<>();

                values.add(dateTime.toString(normalPattern));

                for (XYChartSerie xyChartSerie : xyChartSerieList) {
                    JEVisSample sample = xyChartSerie.getSingleRow().getSamplesMap().get(dateTime);
                    if (sample != null && !xyChartSerie.getSingleRow().isStringData()) {
                        if (!xyChartSerie.getSingleRow().getUnit().toString().equals("")) {
                            values.add(nf.format(sample.getValueAsDouble()) + " " + xyChartSerie.getSingleRow().getUnit());
                        } else {
                            values.add(nf.format(sample.getValueAsDouble()));
                        }

                        if (showSum) {
                            Double oldValue = sums.get(xyChartSerieList.indexOf(xyChartSerie));
                            sums.set(xyChartSerieList.indexOf(xyChartSerie), oldValue + sample.getValueAsDouble());
                        }
                    } else if (sample != null && xyChartSerie.getSingleRow().isStringData()) {
                        if (!xyChartSerie.getSingleRow().getUnit().toString().equals("")) {
                            values.add(sample.getValueAsString() + " " + xyChartSerie.getSingleRow().getUnit());
                        } else {
                            values.add(sample.getValueAsString());
                        }
                    } else {
                        values.add("");
                    }
                }

                tableHeader.getItems().add(values);

                if (dateTimes.indexOf(dateTime) == dateTimes.size() - 1) {
                    Platform.runLater(() -> tableHeader.autoFitTable());
                }
            }

            if (showSum) {
                List<String> values = new ArrayList<>();
                values.add(I18n.getInstance().getString("plugin.graph.table.sum"));
                for (Double sum : sums) {
                    if (!xyChartSerieList.get(sums.indexOf(sum)).getSingleRow().getUnit().toString().equals("")) {
                        values.add(nf.format(sum) + " " + xyChartSerieList.get(sums.indexOf(sum)).getSingleRow().getUnit());
                    } else {
                        values.add(nf.format(sum));
                    }
                }

                tableHeader.getItems().add(values);
            }
        } catch (JEVisException e) {
            logger.error("Error while adding Series to chart", e);
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
