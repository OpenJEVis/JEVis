package org.jevis.jeconfig.application.Chart.Charts;

import com.ibm.icu.text.NumberFormat;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
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
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSample;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TableTopDatePicker;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.application.tools.TableViewUtils;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.plugin.charts.ToolBarSettings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Period;

import java.util.*;

public class TableChartV extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChartV.class);
    private final TableTopDatePicker tableTopDatePicker = new TableTopDatePicker();
    private ChartDataRow singleRow;
    private TableHeaderTable tableHeader;
    private boolean blockDatePickerEvent = false;
    private final DateTime maxDate = new DateTime(9999, 1, 1, 0, 0, 0, 0);
    private Boolean showRowSums = false;
    private Boolean showColumnSums = false;
    private final JFXCheckBox filterEnabledBox = new JFXCheckBox(I18n.getInstance().getString("plugin.dtrc.dialog.limiterlabel"));
    private final HashMap<TableColumn<TableSample, ?>, String> columnFilter = new HashMap<>();
    private final HashMap<TableColumn<TableSample, ?>, Node> newGraphicNodes = new HashMap<>();
    private final HashMap<TableColumn<TableSample, ?>, String> columnTitles = new HashMap<>();
    private final List<String> listColumnTitles = new ArrayList<>();
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

            JEConfig.getStatusBar().addTask(TableChartV.class.getName(), task, taskImage, true);
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
            for (Map.Entry<TableColumn<TableSample, ?>, String> entry : columnTitles.entrySet()) {
                TableColumn<TableSample, ?> column = entry.getKey();
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
            newGraphicNodes.clear();
            columnTitles.clear();
            listColumnTitles.clear();

            Platform.runLater(() -> tableHeader.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY));

            List<TableColumn<TableSample, ?>> tableColumns = new ArrayList<>();
            Map<UUID, TableSample> tableSamples = new HashMap<>();
            Period p = null;
            JEVisObject object = null;
            JEVisSample latestSample = null;

            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                int index = xyChartSerieList.indexOf(xyChartSerie);
                List<JEVisSample> samples = xyChartSerie.getSingleRow().getSamples();

                if (xyChartSerie.getSingleRow().getDataProcessor() != null && !samples.isEmpty()) {
                    p = CleanDataObject.getPeriodForDate(xyChartSerie.getSingleRow().getDataProcessor(), samples.get(0).getTimestamp());
                    setPeriod(p);
                    latestSample = samples.get(0);
                    object = latestSample.getAttribute().getObject();
                    break;
                } else if (xyChartSerie.getSingleRow().getObject() != null && !samples.isEmpty()) {
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

            Map<UUID, Double> rowSums = new HashMap<>();
            boolean nameEqualsExistingColumn = false;

            xyChartSerieList.stream().filter(xyChartSerie -> !listColumnTitles.contains(xyChartSerie.getTableEntryName())).forEach(xyChartSerie -> listColumnTitles.add(xyChartSerie.getTableEntryName()));
            TableSample sumSample = new TableSample(maxDate, listColumnTitles.size());

            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                int index = xyChartSerieList.indexOf(xyChartSerie);

                TableColumn<TableSample, String> column = new TableColumn<>();

                nameEqualsExistingColumn = columnTitles.values().stream().anyMatch(value -> value.equals(xyChartSerie.getTableEntryName()));

                String dataIdentifier = "";

                try {
                    JEVisObject firstParentalDataObject = DataMethods.getFirstParentalDataObject(xyChartSerie.getSingleRow().getObject());
                    JEVisObject parent = firstParentalDataObject.getParent();
                    if (parent != null && listColumnTitles.size() != xyChartSerieList.size()) {
                        dataIdentifier = parent.getName();
                    }
                } catch (Exception e) {
                    logger.error("Could not determine column identifier", e);
                }

                List<JEVisSample> samples = xyChartSerie.getSingleRow().getSamples();

                int columnIndex = listColumnTitles.stream().filter(columnTitle -> columnTitle.equals(xyChartSerie.getTableEntryName())).findFirst().map(listColumnTitles::indexOf).orElse(index);
                if (!nameEqualsExistingColumn) {
                    sumSample.getColumnNumbers().set(columnIndex, 0d);
                    sumSample.getUnits().set(columnIndex, xyChartSerie.getSingleRow().getUnit());
                    sumSample.getColumnNumbersSize().set(columnIndex, 0L);
                }

                for (JEVisSample jeVisSample : samples) {
                    try {
                        DateTime ts = jeVisSample.getTimestamp();

                        TableSample tableSample = null;
                        for (TableSample tableSample1 : tableSamples.values()) {
                            if (tableSample1.getTimeStamp().equals(ts) && tableSample1.getColumnIdentifier().equals(dataIdentifier)) {
                                tableSample = tableSample1;
                                break;
                            }
                        }

                        if (tableSample == null || (nameEqualsExistingColumn && !tableSample.getColumnIdentifier().equals(dataIdentifier))) {
                            TableSample nts = new TableSample(ts, listColumnTitles.size());
                            nts.setColumnIdentifier(dataIdentifier);

                            if (!nts.getChartSeries().contains(xyChartSerie)) {
                                nts.getChartSeries().add(xyChartSerie);
                            }

                            updateSample(xyChartSerie.getNf(), sumSample, xyChartSerie, columnIndex, jeVisSample, nts);

                            tableSamples.put(nts.getUuid(), nts);
                            if (!xyChartSerie.getSingleRow().isStringData()) {
                                rowSums.put(nts.getUuid(), jeVisSample.getValueAsDouble());
                            }
                        } else {
                            updateSample(xyChartSerie.getNf(), sumSample, xyChartSerie, columnIndex, jeVisSample, tableSample);
                            if (!tableSample.getChartSeries().contains(xyChartSerie)) {
                                tableSample.getChartSeries().add(xyChartSerie);
                            }

                            if (!xyChartSerie.getSingleRow().isStringData()) {
                                double aDouble = rowSums.get(tableSample.getUuid()) + jeVisSample.getValueAsDouble();
                                rowSums.replace(tableSample.getUuid(), aDouble);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }

                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getColumnValues().get(columnIndex)));

                Label columnNameLabel = new Label(xyChartSerie.getTableEntryName());
                columnNameLabel.setAlignment(Pos.CENTER);
                HBox nameLabelBox = new HBox(columnNameLabel);
                nameLabelBox.setAlignment(Pos.CENTER);
                JFXTextField filterBox = new JFXTextField();
                filterBox.setPromptText(I18n.getInstance().getString("plugin.chart.tablev.filter.prompt"));
                filterBox.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        String s = columnFilter.get(column);
                        if (s != null) columnFilter.remove(column);
                        columnFilter.put(column, newValue);
                        refreshTable();
                    }
                });

                VBox graphicNode = new VBox(nameLabelBox, filterBox);

                HBox hBox = new HBox(graphicNode);

                if (!nameEqualsExistingColumn) {
                    newGraphicNodes.put(column, hBox);
                    columnTitles.put(column, xyChartSerie.getTableEntryName());
                }


                if (filterEnabledBox.isSelected()) {
                    column.setGraphic(hBox);
                } else {
                    column.setText(xyChartSerie.getTableEntryName());
                }

                column.setCellFactory(new Callback<TableColumn<TableSample, String>, TableCell<TableSample, String>>() {
                    @Override
                    public TableCell<TableSample, String> call(TableColumn<TableSample, String> param) {
                        return new TableCell<TableSample, String>() {

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
                                        logger.error(e);
                                    }
                                }
                            }

                        };
                    }
                });

                if (!nameEqualsExistingColumn) {
                    tableColumns.add(column);
                }
            }

            Map<Integer, JEVisObject> enpis = new HashMap<>();
            if (showSum || showRowSums || showColumnSums) {
                if (showSum) {
                    showRowSums = true;
                    showColumnSums = true;
                }


                QuantityUnits qu = new QuantityUnits();

                List<Double> columnNumbers = sumSample.getColumnNumbers();
                for (int i = 0; i < columnNumbers.size(); i++) {
                    String s = "";
                    Double v = columnNumbers.get(i);
                    Long size = sumSample.getColumnNumbersSize().get(i);
                    JEVisUnit unit = sumSample.getUnits().get(i);
                    NumberFormat nf = sumSample.getNumberFormats().get(i);
                    boolean isCalculation = sumSample.isCalculation().get(i);

                    if (unit != null && !unit.toString().isEmpty()) {
                        boolean isQuantity = qu.isQuantityUnit(unit);
                        isQuantity = qu.isQuantityIfCleanData(singleRow.getAttribute(), isQuantity);

                        if (!isQuantity) {
                            v = v / size;
                            if (isCalculation) {
                                enpis.put(i, sumSample.getCalculationObjects().get(i));
                            }
                        }

                        s = nf.format(v) + " " + unit;
                    } else {
                        s = nf.format(v);
                    }
                    sumSample.getColumnValues().set(i, s);
                }

                if (showRowSums) {
                    tableSamples.put(sumSample.getUuid(), sumSample);
                    rowSums.put(sumSample.getUuid(), sumSample.getColumnNumbers().stream().mapToDouble(aDouble -> aDouble).sum());
                }

                if (showColumnSums) {
                    tableSamples.forEach((uuid, tableSample) -> {
                        if (!xyChartSerieList.get(0).getSingleRow().getUnit().toString().isEmpty()) {
                            tableSample.getColumnValues().add(nf.format(rowSums.get(tableSample.getUuid())) + " " + xyChartSerieList.get(0).getSingleRow().getUnit());
                        } else {
                            tableSample.getColumnValues().add(nf.format(rowSums.get(tableSample.getUuid())));
                        }
                    });
                }

                if (showColumnSums) {
                    TableColumn<TableSample, String> column = new TableColumn<>((I18n.getInstance().getString("plugin.graph.table.sum")));
                    column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getColumnValues().get(param.getValue().getColumnValues().size() - 1)));

                    column.setCellFactory(new Callback<TableColumn<TableSample, String>, TableCell<TableSample, String>>() {
                        @Override
                        public TableCell<TableSample, String> call(TableColumn<TableSample, String> param) {
                            return new TableCell<TableSample, String>() {
                                @Override
                                protected void updateItem(String item, boolean empty) {
                                    super.updateItem(item, empty);

                                    if (!empty) {
                                        try {
                                            setText(item);
                                            setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                    }
                                }

                            };
                        }
                    });

                    tableColumns.add(column);
                }
            }

            tableColumns.add(0, dateColumn);

            if (nameEqualsExistingColumn) {
                TableColumn<TableSample, String> identifierColumn = new TableColumn<>();
                identifierColumn.setStyle("-fx-alignment: CENTER;");
                identifierColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getColumnIdentifier()));
                String identifierColumnTitle = I18n.getInstance().getString("plugin.graph.table.column.identifier");

                Label columnNameLabel = new Label(identifierColumnTitle);
                columnNameLabel.setAlignment(Pos.CENTER);
                HBox nameLabelBox = new HBox(columnNameLabel);
                nameLabelBox.setAlignment(Pos.CENTER);
                JFXTextField filterBox = new JFXTextField();
                filterBox.setPromptText(I18n.getInstance().getString("plugin.chart.tablev.filter.prompt"));
                filterBox.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        String s = columnFilter.get(identifierColumn);
                        if (s != null) columnFilter.remove(identifierColumn);
                        columnFilter.put(identifierColumn, newValue);
                        refreshTable();
                    }
                });

                VBox graphicNode = new VBox(nameLabelBox, filterBox);
                graphicNode.setMinHeight(nameLabelBox.getLayoutBounds().getHeight() + filterBox.getLayoutBounds().getHeight());

                newGraphicNodes.put(identifierColumn, graphicNode);

                if (filterEnabledBox.isSelected()) {
                    identifierColumn.setGraphic(graphicNode);
                } else {
                    identifierColumn.setText(identifierColumnTitle);
                }

                columnTitles.put(identifierColumn, identifierColumnTitle);
                tableColumns.add(1, identifierColumn);
            }

            ObservableList<TableSample> values = FXCollections.observableArrayList(tableSamples.values());
            values.sort((o1, o2) -> DateTimeComparator.getInstance().compare(o1.getTimeStamp(), o2.getTimeStamp()));

            filteredList = new FilteredList<>(values, s -> true);

            tableHeader.setItems(filteredList);

            for (Map.Entry<Integer, JEVisObject> entry : enpis.entrySet()) {
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), singleRow.getObject().getDataSource(), entry.getValue(),
                                    singleRow.getSelectedStart(), singleRow.getSelectedEnd(), true);
                            List<JEVisSample> results = calcJob.getResults();
                            XYChartSerie serie = xyChartSerieList.get(entry.getKey());
                            JEVisUnit unit = serie.getSingleRow().getUnit();

                            if (results.size() == 1) {
                                Platform.runLater(() -> {
                                    try {
                                        values.get(values.size() - 1).getColumnValues().set(entry.getKey(), serie.getNf().format(results.get(0).getValueAsDouble()) + " " + unit);
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
                JEConfig.getStatusBar().addTask(TableChartV.class.getName(), task, TableChartV.taskImage, true);
            }

            Platform.runLater(() -> {
                tableHeader.getColumns().clear();
                tableHeader.getColumns().addAll(tableColumns);
                tableHeader.autoFitTable();
                TableViewUtils.addCustomTableMenu(tableHeader, columnTitles);
            });

            tableHeader.getVisibleLeafColumns().addListener((ListChangeListener) change -> TableViewUtils.addCustomTableMenu(tableHeader, columnTitles));

        } catch (Exception e) {
            logger.error("Error while adding Series to chart", e);
        }
    }

    private void refreshTable() {
        filteredList.setPredicate(tableSample -> {
            boolean showTableSample = true;
            for (Map.Entry<TableColumn<TableSample, ?>, String> entry : columnFilter.entrySet()) {
                TableColumn<TableSample, ?> column = entry.getKey();
                int columnIndex = tableHeader.getColumns().indexOf(column) - 1;
                boolean consolidatedTable = !tableSample.getColumnIdentifier().isEmpty();

                if (consolidatedTable) {
                    columnIndex--;
                }

                String columnFilterValue = entry.getValue();
                if (columnFilterValue == null || columnFilterValue.isEmpty()) continue;

                String columnValue;

                if (consolidatedTable && columnIndex == -1) {
                    columnValue = tableSample.getColumnIdentifier();
                } else {
                    try {
                        Number parse = nf.parse(tableSample.getColumnValues().get(columnIndex));
                        Number parseFilter = nf.parse(columnFilterValue);
                        columnValue = parse.toString();
                        columnFilterValue = parseFilter.toString();
                    } catch (Exception e) {
                        columnValue = tableSample.getColumnValues().get(columnIndex); //TableSample has no date column
                    }
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

    private void updateSample(NumberFormat nf, TableSample sumSample, XYChartSerie xyChartSerie, int index, JEVisSample jeVisSample, TableSample nts) throws JEVisException {
        if (!xyChartSerie.getSingleRow().isStringData()) {
            if (!xyChartSerie.getSingleRow().getUnit().toString().isEmpty()) {
                nts.getColumnValues().set(index, nf.format(jeVisSample.getValueAsDouble()) + " " + xyChartSerie.getSingleRow().getUnit());
                nts.getUnits().set(index, xyChartSerie.getSingleRow().getUnit());
            } else {
                nts.getColumnValues().set(index, nf.format(jeVisSample.getValueAsDouble()));
            }
            nts.getColumnNumbers().set(index, jeVisSample.getValueAsDouble());
            nts.getColumnNumbersSize().set(index, nts.getColumnNumbersSize().get(index) + 1);
            nts.getNumberFormats().set(index, nf);
            boolean isCalculation = xyChartSerie.getSingleRow().isCalculation();
            if (isCalculation) {
                nts.isCalculation().set(index, true);
                nts.getCalculationObjects().set(index, xyChartSerie.getSingleRow().getCalculationObject());
            }


            if (showSum || showRowSums || showColumnSums) {
                Double oldValue = sumSample.getColumnNumbers().get(index);
                if (oldValue != null) {
                    sumSample.getColumnNumbers().set(index, oldValue + jeVisSample.getValueAsDouble());
                    sumSample.getColumnNumbersSize().set(index, sumSample.getColumnNumbersSize().get(index) + 1);
                } else sumSample.getColumnNumbers().set(index, jeVisSample.getValueAsDouble());
            }
        } else {
            if (!xyChartSerie.getSingleRow().getUnit().toString().isEmpty()) {
                nts.getColumnValues().set(index, jeVisSample.getValueAsString() + " " + xyChartSerie.getSingleRow().getUnit());
                nts.getUnits().set(index, xyChartSerie.getSingleRow().getUnit());
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
                                logger.error(e);
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

    public void showRowSums(Boolean showRowSums) {
        this.showRowSums = showRowSums;
    }

    public void showColumnSums(Boolean showColumnSums) {
        this.showColumnSums = showColumnSums;
    }

    public JFXCheckBox getFilterEnabledBox() {
        return filterEnabledBox;
    }
}
