package org.jevis.jecc.application.Chart.Charts;

import com.ibm.icu.text.NumberFormat;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jecc.application.Chart.ChartElements.TableSerie;
import org.jevis.jecc.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jecc.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.plugin.charts.DataSettings;
import org.jevis.jecc.plugin.charts.ToolBarSettings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableChartV extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChartV.class);
    private final TableTopDatePicker tableTopDatePicker = new TableTopDatePicker();
    private final DateTime maxDate = new DateTime(9999, 1, 1, 0, 0, 0, 0);
    private final MFXCheckbox filterEnabledBox = new MFXCheckbox(I18n.getInstance().getString("plugin.dtrc.dialog.limiterlabel"));
    private final HashMap<TableColumn, String> columnFilter = new HashMap<>();
    private final HashMap<TableColumn, Node> newGraphicNodes = new HashMap<>();
    private final HashMap<TableColumn, String> columnTitles = new HashMap<>();
    private ChartDataRow singleRow;
    private TableHeaderTable tableHeader;
    private boolean blockDatePickerEvent = false;
    private Boolean showRowSums = false;
    private Boolean showColumnSums = false;
    private FilteredList<TableSample> filteredList;

    public TableChartV(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);
    }

    @Override
    public void createChart(ToolBarSettings toolBarSettings, DataSettings dataSettings, boolean instant) {
        this.chartDataRows = new ArrayList<>();

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

            ControlCenter.getStatusBar().addTask(TableChartV.class.getName(), task, taskImage, true);
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
        this.columnFilter.clear();
        this.filterEnabledBox.setSelected(chartModel.isFilterEnabled());
        this.filterEnabledBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            chartModel.setFilterEnabled(newValue);
            for (Map.Entry<TableColumn, String> entry : columnTitles.entrySet()) {
                TableColumn<TableSample, String> column = (TableColumn<TableSample, String>) entry.getKey();
                String columnTitle = entry.getValue();

                Node newGraphic = newGraphicNodes.get(column);
                if (newValue) {
                    column.setGraphic(newGraphic);
                    column.setText(null);
                } else {
                    column.setText(columnTitle);
                    column.setGraphic(null);
                }
            }
        });

        AlphanumComparator alphanumComparator = new AlphanumComparator();
        xyChartSerieList.sort((o1, o2) -> alphanumComparator.compare(o1.getTableEntryName(), o2.getTableEntryName()));

        try {
            tableHeader.getItems().clear();
            Platform.runLater(() -> tableHeader.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY));

            List<TableColumn> tableColumns = new ArrayList<>();
            Map<DateTime, TableSample> tableSamples = new HashMap<>();
            Period p = null;
            JEVisObject object = null;
            JEVisSample latestSample = null;

            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                int index = xyChartSerieList.indexOf(xyChartSerie);
                List<JEVisSample> samples = xyChartSerie.getSingleRow().getSamples();

                if (xyChartSerie.getSingleRow().getDataProcessor() != null && samples.size() > 0) {
                    p = CleanDataObject.getPeriodForDate(xyChartSerie.getSingleRow().getDataProcessor(), samples.get(0).getTimestamp());
                    setPeriod(p);
                    latestSample = samples.get(0);
                    object = latestSample.getAttribute().getObject();
                    break;
                } else if (xyChartSerie.getSingleRow().getObject() != null && samples.size() > 0) {
                    p = CleanDataObject.getPeriodForDate(xyChartSerie.getSingleRow().getObject(), samples.get(0).getTimestamp());
                    setPeriod(p);
                    latestSample = samples.get(0);
                    object = latestSample.getAttribute().getObject();
                    break;
                } else if (p == null && samples.size() > 1) {
                    try {
                        p = new Period(samples.get(0).getTimestamp(),
                                samples.get(1).getTimestamp());
                        setPeriod(p);
                        latestSample = samples.get(0);
                        object = latestSample.getAttribute().getObject();

                        break;
                    } catch (Exception e) {
                        logger.error("Could not get period from samples", e);
                    }
                } else if (p == null && samples.size() == 1) {
                    try {
                        p = xyChartSerie.getSingleRow().getPeriod();
                        setPeriod(p);
                        latestSample = samples.get(0);
                        object = latestSample.getAttribute().getObject();
                        break;
                    } catch (Exception e) {
                        logger.error("Could not get period from attribute", e);
                    }
                }
            }

            boolean isCounter = CleanDataObject.isCounter(object, latestSample);
            String normalPattern = PeriodHelper.getFormatString(p, isCounter);

            TableColumn<TableSample, DateTime> dateColumn = buildDateColumn(normalPattern);

            List<Double> columnSums = new ArrayList<>();
            Map<DateTime, Double> rowSums = new HashMap<>();
            for (int i = 0; i < xyChartSerieList.size(); i++) columnSums.add(0d);

            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                int index = xyChartSerieList.indexOf(xyChartSerie);
                List<JEVisSample> samples = xyChartSerie.getSingleRow().getSamples();

                for (JEVisSample jeVisSample : samples) {
                    try {
                        TableSample tableSample = tableSamples.get(jeVisSample.getTimestamp());
                        if (tableSample == null) {
                            TableSample nts = new TableSample(jeVisSample.getTimestamp(), xyChartSerieList.size());

                            updateSample(nf, columnSums, xyChartSerie, index, jeVisSample, nts);

                            tableSamples.put(jeVisSample.getTimestamp(), nts);
                            if (!xyChartSerie.getSingleRow().isStringData()) {
                                rowSums.put(jeVisSample.getTimestamp(), jeVisSample.getValueAsDouble());
                            }
                        } else {
                            updateSample(nf, columnSums, xyChartSerie, index, jeVisSample, tableSample);

                            if (!xyChartSerie.getSingleRow().isStringData()) {
                                double aDouble = rowSums.get(jeVisSample.getTimestamp()) + jeVisSample.getValueAsDouble();
                                rowSums.replace(jeVisSample.getTimestamp(), aDouble);
                            }
                        }
                    } catch (JEVisException e) {
                        logger.error(e);
                    }
                }

                TableColumn<TableSample, String> column = new TableColumn<>();
                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getColumnValues().get(index)));

                Label columnNameLabel = new Label(xyChartSerie.getTableEntryName());
                columnNameLabel.setAlignment(Pos.CENTER);
                HBox nameLabelBox = new HBox(columnNameLabel);
                nameLabelBox.setAlignment(Pos.CENTER);
                MFXTextField filterBox = new MFXTextField();
                filterBox.setFloatMode(FloatMode.DISABLED);
                filterBox.setPromptText(I18n.getInstance().getString("searchbar.filterinput.prompttext"));
                filterBox.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        String s = columnFilter.get(column);
                        if (s != null) columnFilter.remove(column);
                        columnFilter.put(column, newValue);
                        refreshTable();
                    }
                });

                VBox graphicNode = new VBox(nameLabelBox, filterBox);
                graphicNode.setPadding(new Insets(4));

                newGraphicNodes.put(column, graphicNode);
                columnTitles.put(column, xyChartSerie.getTableEntryName());

                if (filterEnabledBox.isSelected()) {
                    column.setGraphic(graphicNode);
                } else {
                    column.setText(xyChartSerie.getTableEntryName());
                }

                column.setCellFactory(new Callback<TableColumn<TableSample, String>, TableCell<TableSample, String>>() {
                    @Override
                    public TableCell<TableSample, String> call(TableColumn<TableSample, String> param) {
                        TableCell<TableSample, String> cell = new TableCell<TableSample, String>() {

                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);

                                if (!empty) {
                                    try {
                                        String customCSS = xyChartSerie.getSingleRow().getCustomCSS();
                                        setText(item);

                                        if (showRowSums && getTableRow() != null && getTableRow().getIndex() == rowSums.size() - 1) {
                                            setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                        } else {
                                            setStyle("-fx-alignment: CENTER-RIGHT;");
                                        }

                                        if (customCSS != null) {
                                            setStyle(getStyle() + customCSS);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        };

                        return cell;
                    }
                });

                tableColumns.add(column);
            }

            Map<Integer, JEVisObject> enpis = new HashMap<>();
            if (showSum || showRowSums || showColumnSums) {
                if (showSum) {
                    showRowSums = true;
                    showColumnSums = true;
                }

                TableSample sumSample = new TableSample(maxDate, xyChartSerieList.size());
                QuantityUnits qu = new QuantityUnits();

                columnSums.forEach(aDouble -> {
                    String string = "";
                    ChartDataRow singleRow = xyChartSerieList.get(columnSums.indexOf(aDouble)).getSingleRow();
                    JEVisUnit unit = singleRow.getUnit();

                    if (!unit.toString().equals("")) {
                        boolean isQuantity = qu.isQuantityUnit(unit);
                        isQuantity = qu.isQuantityIfCleanData(singleRow.getAttribute(), isQuantity);

                        Double d = aDouble;
                        if (!isQuantity) {
                            d = d / singleRow.getSamples().size();
                            if (singleRow.isCalculation()) {
                                enpis.put(columnSums.indexOf(aDouble), singleRow.getCalculationObject());
                            }
                        }

                        string = nf.format(d) + " " + unit;
                    } else {
                        string = nf.format(aDouble);
                    }
                    sumSample.getColumnValues().set(columnSums.indexOf(aDouble), string);
                });

                if (showRowSums) {
                    tableSamples.put(maxDate, sumSample);
                    rowSums.put(maxDate, columnSums.stream().mapToDouble(aDouble -> aDouble).sum());
                }

                if (showColumnSums) {
                    tableSamples.forEach((dateTime, tableSample) -> {
                        if (!xyChartSerieList.get(0).getSingleRow().getUnit().toString().equals("")) {
                            tableSample.getColumnValues().add(nf.format(rowSums.get(dateTime)) + " " + xyChartSerieList.get(0).getSingleRow().getUnit());
                        } else {
                            tableSample.getColumnValues().add(nf.format(rowSums.get(dateTime)));
                        }
                    });
                }

                if (showColumnSums) {
                    TableColumn<TableSample, String> column = new TableColumn<>((I18n.getInstance().getString("plugin.graph.table.sum")));
                    column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getColumnValues().get(param.getValue().getColumnValues().size() - 1)));

                    column.setCellFactory(new Callback<TableColumn<TableSample, String>, TableCell<TableSample, String>>() {
                        @Override
                        public TableCell<TableSample, String> call(TableColumn<TableSample, String> param) {
                            TableCell<TableSample, String> cell = new TableCell<TableSample, String>() {

                                @Override
                                protected void updateItem(String item, boolean empty) {
                                    super.updateItem(item, empty);

                                    if (!empty) {
                                        try {
                                            setText(item);
                                            setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            };

                            return cell;
                        }
                    });

                    tableColumns.add(column);
                }
            }

            tableColumns.add(0, dateColumn);

            ObservableList<TableSample> values = FXCollections.observableArrayList(tableSamples.values());
            values.sort((o1, o2) -> DateTimeComparator.getInstance().compare(o1.getTimeStamp(), o2.getTimeStamp()));

            filteredList = new FilteredList<>(values, s -> true);

            tableHeader.setItems(filteredList);

            for (Map.Entry<Integer, JEVisObject> entry : enpis.entrySet()) {
                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), singleRow.getObject().getDataSource(), entry.getValue(),
                                    singleRow.getSelectedStart(), singleRow.getSelectedEnd(), true);
                            List<JEVisSample> results = calcJob.getResults();
                            JEVisUnit unit = xyChartSerieList.get(entry.getKey()).getSingleRow().getUnit();

                            if (results.size() == 1) {
                                Platform.runLater(() -> {
                                    try {
                                        values.get(values.size() - 1).getColumnValues().set(entry.getKey(), nf.format(results.get(0).getValueAsDouble()) + " " + unit);
                                    } catch (JEVisException e) {
                                        logger.error("Couldn't get calculation result");
                                    }
                                });
                            } else {
                                values.get(values.size() - 1).getColumnValues().set(entry.getKey(), "- " + unit);
                            }
                        } catch (Exception e) {
                            failed();
                        } finally {
                            succeeded();
                            Platform.runLater(tableHeader::refresh);
                        }
                        return null;
                    }
                };
                ControlCenter.getStatusBar().addTask(TableChartV.class.getName(), task, TableChartV.taskImage, true);
            }

            Platform.runLater(() -> {
                tableHeader.getColumns().clear();
                tableHeader.getColumns().addAll(tableColumns);
                tableHeader.autoFitTable();
            });

        } catch (Exception e) {
            logger.error("Error while adding Series to chart", e);
        }
    }

    private void refreshTable() {
        filteredList.setPredicate(tableSample -> {
            boolean showTableSample = true;
            for (Map.Entry<TableColumn, String> entry : columnFilter.entrySet()) {
                TableColumn<TableSample, String> column = (TableColumn<TableSample, String>) entry.getKey();
                int columnIndex = tableHeader.getColumns().indexOf(column);

                String columnFilterValue = entry.getValue();
                if (columnFilterValue == null || columnFilterValue.equals("")) continue;

                XYChartSerie serie = xyChartSerieList.get(columnIndex);
                ChartDataRow chartDataRow = serie.getSingleRow();
                String columnValue;
                if (!chartDataRow.isStringData()) {
                    try {
                        Number parse = nf.parse(tableSample.getColumnValues().get(columnIndex - 1));
                        Number parseFilter = nf.parse(columnFilterValue);
                        columnValue = parse.toString();
                        columnFilterValue = parseFilter.toString();
                    } catch (Exception e) {
                        columnValue = tableSample.getColumnValues().get(columnIndex - 1);
                        columnFilterValue = entry.getValue();
                    }
                } else {
                    columnValue = tableSample.getColumnValues().get(columnIndex - 1); //TableSample has no date column
                }

                if (!columnValue.toLowerCase().contains(columnFilterValue.toLowerCase())) {
                    showTableSample = false;
                    break;
                }
            }
            return showTableSample;
        });
        tableHeader.refresh();
    }

    private void updateSample(NumberFormat nf, List<Double> sums, XYChartSerie xyChartSerie, int index, JEVisSample jeVisSample, TableSample nts) throws JEVisException {
        if (!xyChartSerie.getSingleRow().isStringData()) {
            if (!xyChartSerie.getSingleRow().getUnit().toString().equals("")) {
                nts.getColumnValues().set(index, nf.format(jeVisSample.getValueAsDouble()) + " " + xyChartSerie.getSingleRow().getUnit());
            } else {
                nts.getColumnValues().set(index, nf.format(jeVisSample.getValueAsDouble()));
            }

            if (showSum || showRowSums || showColumnSums) {
                Double oldValue = sums.get(xyChartSerieList.indexOf(xyChartSerie));
                sums.set(xyChartSerieList.indexOf(xyChartSerie), oldValue + jeVisSample.getValueAsDouble());
            }
        } else {
            if (!xyChartSerie.getSingleRow().getUnit().toString().equals("")) {
                nts.getColumnValues().set(index, jeVisSample.getValueAsString() + " " + xyChartSerie.getSingleRow().getUnit());
            } else {
                nts.getColumnValues().set(index, jeVisSample.getValueAsString());
            }
        }
    }

    private TableColumn<TableSample, DateTime> buildDateColumn(String normalPattern) {
        TableColumn<TableSample, DateTime> column = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.date"));
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTimeStamp()));

        column.setCellFactory(new Callback<TableColumn<TableSample, DateTime>, TableCell<TableSample, DateTime>>() {
            @Override
            public TableCell<TableSample, DateTime> call(TableColumn<TableSample, DateTime> param) {
                return new TableCell<TableSample, DateTime>() {

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            try {
                                if (item.equals(maxDate)) {
                                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                    setText(I18n.getInstance().getString("plugin.graph.table.sum"));
                                } else {
                                    setStyle("-fx-alignment: CENTER-RIGHT;");
                                    if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
                                        DateTime modDateTime = item.plusDays(1);
                                        setText(modDateTime.toString(normalPattern));
                                    } else {
                                        setText(item.toString(normalPattern));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
        });
        return column;
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
        this.tableHeader.getColumns().clear();
    }

    @Override
    public ChartModel getChartModel() {
        return chartModel;
    }

    public void showRowSums(Boolean showRowSums) {
        this.showRowSums = showRowSums;
    }

    public void showColumnSums(Boolean showColumnSums) {
        this.showColumnSums = showColumnSums;
    }

    public MFXCheckbox getFilterEnabledBox() {
        return filterEnabledBox;
    }

    private class TableSample {
        final private DateTime timeStamp;
        private final List<String> columnValues = new ArrayList<>();

        public TableSample(DateTime timeStamp, int size) {
            this.timeStamp = timeStamp;

            for (int i = 0; i < size; i++) columnValues.add("");
        }

        public DateTime getTimeStamp() {
            return timeStamp;
        }

        public List<String> getColumnValues() {
            return columnValues;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TableSample) {
                TableSample otherObj = (TableSample) obj;
                return otherObj.getTimeStamp().equals(this.getTimeStamp());
            }
            return false;
        }
    }
}
