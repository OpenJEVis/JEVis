package org.jevis.jeconfig.application.Chart.Charts;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
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

import java.text.NumberFormat;
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
                        logger.error("Could not build chart {}", chartSetting.getName(), e);
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
                            rowSums.put(jeVisSample.getTimestamp(), jeVisSample.getValueAsDouble());
                        } else {
                            updateSample(nf, columnSums, xyChartSerie, index, jeVisSample, tableSample);
                            double aDouble = rowSums.get(jeVisSample.getTimestamp()) + jeVisSample.getValueAsDouble();
                            rowSums.replace(jeVisSample.getTimestamp(), aDouble);
                        }
                    } catch (JEVisException e) {
                        logger.error(e);
                    }
                }

                TableColumn<TableSample, String> column = new TableColumn<>(xyChartSerie.getTableEntryName());
                column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getColumnValues().get(index)));

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

                                        JFXTextField jfxTextField = new JFXTextField(item);
                                        if (showRowSums && getTableRow() != null && getTableRow().getIndex() == rowSums.size() - 1) {
                                            jfxTextField.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                        } else {
                                            jfxTextField.setStyle("-fx-alignment: CENTER-RIGHT;");
                                        }

                                        if (customCSS != null) {
                                            this.setStyle(customCSS);
                                        }
                                        setGraphic(jfxTextField);

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
                            if (singleRow.getEnPI()) {
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
                                            JFXTextField jfxTextField = new JFXTextField(item);
                                            jfxTextField.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                            setGraphic(jfxTextField);

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

            AlphanumComparator ac = new AlphanumComparator();
            tableColumns.sort((o1, o2) -> ac.compare(o1.getText(), o2.getText()));
            tableColumns.add(0, dateColumn);

            List<TableSample> values = new ArrayList<>(tableSamples.values());
            values.sort((o1, o2) -> DateTimeComparator.getInstance().compare(o1.getTimeStamp(), o2.getTimeStamp()));

            tableHeader.getItems().addAll(values);

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
                JEConfig.getStatusBar().addTask(TableChartV.class.getName(), task, TableChartV.taskImage, true);
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
        } else if (xyChartSerie.getSingleRow().isStringData()) {
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
                                JFXTextField textField = new JFXTextField();

                                if (item.equals(maxDate)) {
                                    textField.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                                    textField.setText(I18n.getInstance().getString("plugin.graph.table.sum"));
                                } else {
                                    textField.setStyle("-fx-alignment: CENTER-RIGHT;");
                                    if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
                                        DateTime modDateTime = item.plusDays(1);
                                        textField.setText(modDateTime.toString(normalPattern));
                                    } else {
                                        textField.setText(item.toString(normalPattern));
                                    }
                                }

                                setGraphic(textField);

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

    public void showRowSums(Boolean showRowSums) {
        this.showRowSums = showRowSums;
    }

    public void showColumnSums(Boolean showColumnSums) {
        this.showColumnSums = showColumnSums;
    }
}
