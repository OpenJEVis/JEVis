package org.jevis.jeconfig.application.Chart.Charts;

import com.jfoenix.controls.JFXCheckBox;
import eu.hansolo.fx.charts.tools.Helper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHSample;
import org.jevis.jeconfig.application.Chart.ChartElements.TableSerie;
import org.jevis.jeconfig.application.Chart.ChartElements.XYChartSerie;
import org.jevis.jeconfig.application.Chart.Charts.TableCells.ValueTableCell;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.plugin.charts.DataSettings;
import org.jevis.jeconfig.plugin.charts.ToolBarSettings;

import java.util.*;

/**
 * Horizontal table chart — renders each data series as a single row with the series
 * name, a colour-coded value cell, and the measurement unit.
 * <p>
 * Each series contributes exactly one {@link TableHSample} (the latest sample value).
 * An optional sum row is appended when {@code showColumnSums} is enabled on the
 * {@link ChartModel}.
 * <p>
 * Rendering is performed asynchronously on a background {@link Task} submitted to the
 * central status-bar executor. All JavaFX scene-graph mutations are marshalled back to
 * the Application Thread via {@link javafx.application.Platform#runLater}.
 *
 * @see TableChartV
 * @see TableHSample
 * @see ValueTableCell
 */
public class TableChartH extends XYChart {
    private static final Logger logger = LogManager.getLogger(TableChartH.class);
    private final JFXCheckBox filterEnabledBox = new JFXCheckBox(I18n.getInstance().getString("plugin.dtrc.dialog.limiterlabel"));
    private FilteredList<TableHSample> filteredList;
    private TableColumn<TableHSample, String> nameColumn;
    private TableColumn<TableHSample, Double> valueColumn;
    private TextField nameFilterField = new TextField();
    private TextField valueFilterField = new TextField();
    private TableHSample sumSample;

    private TableView<TableHSample> tableHeader;
    private boolean blockDatePickerEvent = false;
    private Boolean showColumnSums = false;
    private final Map<TableColumn<TableHSample, ?>, String> columnFilter = new HashMap<>();


    public TableChartH(JEVisDataSource ds, ChartModel chartModel) {
        super(ds, chartModel);
    }

    @Override
    public void createChart(ToolBarSettings toolBarSettings, DataSettings dataSettings, boolean instant) {
        this.chartDataRows = new ArrayList<>();

        if (!instant) {
            Task<?> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        showColumnSums(chartModel.isShowColumnSums());
                        buildChart(toolBarSettings, dataSettings);
                    } catch (Exception e) {
                        this.failed();
                        logger.error("Could not build chart {}", chartModel.getChartName(), e);
                    } finally {
                        succeeded();
                    }
                    return null;
                }
            };

            JEConfig.getStatusBar().addTask(TableChartH.class.getName(), task, taskImage, true);
        } else {
            buildChart(toolBarSettings, dataSettings);
        }
    }

    @Override
    public XYChartSerie generateSerie(Boolean[] changedBoth, ChartDataRow singleRow) throws JEVisException {

        TableSerie serie = new TableSerie(chartModel, singleRow, showIcons);

        getHexColors().add(singleRow.getColor());

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

    /**
     * Builds the horizontal table from the current list of {@link XYChartSerie} instances.
     * <p>
     * For each series, the latest sample value is read and wrapped in a {@link TableHSample}.
     * A colour-coded value cell is rendered using the series colour and a normalised
     * value ratio ({@code (value - min) / range}); when all values are equal the ratio
     * defaults to {@code 0.5} to avoid division by zero.
     * An optional sum row is appended when {@code showColumnSums} is enabled.
     * <p>
     * All scene-graph work is dispatched to the JavaFX Application Thread.
     */
    @Override
    public void addSeriesToChart() {
        try {
            tableHeader.getItems().clear();
            Platform.runLater(() -> tableHeader.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY));

            nameColumn = new TableColumn<>();
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("sampleName"));
            nameColumn.setComparator(alphanumComparator);
            nameColumn.setSortable(true);
            nameColumn.setId("name");

            valueColumn = new TableColumn<>();
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("sampleValue"));
            valueColumn.setCellFactory(new ValueTableCell<>());
            valueColumn.setComparator(Double::compare);
            valueColumn.setSortable(true);
            valueColumn.setId("value");

            // Name column header with filter field
            String nameTitleStr = I18n.getInstance().getString("plugin.graph.table.name");
            String valueTitleStr = I18n.getInstance().getString("plugin.graph.table.value");

            nameFilterField = new TextField();
            nameFilterField.setPromptText(I18n.getInstance().getString("plugin.chart.tablev.filter.prompt"));
            VBox nameGraphic = new VBox(2, new Label(nameTitleStr), nameFilterField);
            nameFilterField.textProperty().addListener((obs, o, n) -> {
                columnFilter.put(nameColumn, n);
                refreshTable();
            });

            // Value column header with filter field
            valueFilterField = new TextField();
            valueFilterField.setPromptText(I18n.getInstance().getString("plugin.chart.tablev.filter.prompt"));
            VBox valueGraphic = new VBox(2, new Label(valueTitleStr), valueFilterField);
            valueFilterField.textProperty().addListener((obs, o, n) -> {
                columnFilter.put(valueColumn, n);
                refreshTable();
            });

            // Apply initial header state based on checkbox
            if (filterEnabledBox.isSelected()) {
                nameColumn.setGraphic(nameGraphic);
                nameColumn.setText("");
                valueColumn.setGraphic(valueGraphic);
                valueColumn.setText("");
            } else {
                nameColumn.setText(nameTitleStr);
                valueColumn.setText(valueTitleStr);
            }

            // Wire filterEnabledBox — toggle column headers and clear filters when disabled
            filterEnabledBox.selectedProperty().addListener((obs, o, enabled) -> {
                if (enabled) {
                    nameColumn.setGraphic(nameGraphic);
                    nameColumn.setText("");
                    valueColumn.setGraphic(valueGraphic);
                    valueColumn.setText("");
                } else {
                    nameColumn.setText(nameTitleStr);
                    nameColumn.setGraphic(null);
                    valueColumn.setText(valueTitleStr);
                    valueColumn.setGraphic(null);
                    columnFilter.clear();
                    refreshTable();
                }
            });

            List<TableHSample> items = new ArrayList<>();

            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            String firstUnit = "";

            for (XYChartSerie xyChartSerie : xyChartSerieList) {
                try {
                    xyChartSerie.getSingleRow().setAbsolute(true);
                    xyChartSerie.getSingleRow().setSomethingChanged(true);

                    List<JEVisSample> samples = xyChartSerie.getSingleRow().getSamples();
                    if (samples.isEmpty()) continue;
                    Double value = samples.get(0).getValueAsDouble();

                    TableHSample tableHSample = new TableHSample(xyChartSerie);
                    if (firstUnit.isEmpty()) firstUnit = tableHSample.getSampleUnit();
                    min = Math.min(min, value);
                    max = Math.max(max, value);

                    items.add(tableHSample);
                } catch (Exception e) {
                    logger.error("Could not build row for series '{}'", xyChartSerie.getTableEntryName(), e);
                }
            }

            double rangeOfValues = max - min;

            if (chartModel.isColoringEnabled()) {
                for (TableHSample data : items) {
                    // Guard against division by zero when all values are equal
                    double v = (rangeOfValues > 0) ? (data.getSampleValue() - min) / rangeOfValues : 0.5;
                    Color color = Helper.getColorAt(chartModel.getColorMapping().getGradient(), v);
                    data.setSampleColor(color);
                }
            }

            if (showColumnSums) {
                double sum = items.stream().mapToDouble(TableHSample::getSampleValue).sum();
                sumSample = new TableHSample(sum, I18n.getInstance().getString("plugin.graph.table.sum"), firstUnit);
                items.add(sumSample);
            } else {
                sumSample = null;
            }

            ObservableList<TableHSample> values = FXCollections.observableArrayList(items);
            filteredList = new FilteredList<>(values, tableHSample -> true);
            SortedList<TableHSample> sortedList = new SortedList<>(filteredList);
            tableHeader.comparatorProperty().addListener((obs, oldComp, newComp) ->
                    sortedList.setComparator(wrapComparator(newComp)));

            tableHeader.setItems(sortedList);

            Platform.runLater(() -> {
                tableHeader.getColumns().addAll(nameColumn, valueColumn);
                tableHeader.getSortOrder().setAll(valueColumn);
            });

        } catch (Exception e) {
            logger.error("Error while adding Series to chart", e);
        }
    }

    private Comparator<TableHSample> wrapComparator(Comparator<TableHSample> base) {
        return (a, b) -> {
            if (a == sumSample) return 1;
            if (b == sumSample) return -1;
            return base != null ? base.compare(a, b) : 0;
        };
    }

    /**
     * Reapplies the name and value column filters to the table's {@link FilteredList}.
     */
    private void refreshTable() {
        String nameFilter = columnFilter.getOrDefault(nameColumn, "").toLowerCase();
        String valueFilter = columnFilter.getOrDefault(valueColumn, "").toLowerCase();

        filteredList.setPredicate(s -> {
            if (s == sumSample) return true;
            boolean nameOk = nameFilter.isEmpty()
                    || s.getSampleName().toLowerCase().contains(nameFilter);
            boolean valueOk = valueFilter.isEmpty()
                    || s.getNf().format(s.getSampleValue()).contains(valueFilter)
                    || s.getSampleUnit().toLowerCase().contains(valueFilter);
            return nameOk && valueOk;
        });
    }

    @Override
    public void generateYAxis() {
    }

    public boolean isBlockDatePickerEvent() {
        return blockDatePickerEvent;
    }

    public void setBlockDatePickerEvent(boolean blockDatePickerEvent) {
        this.blockDatePickerEvent = blockDatePickerEvent;
    }

    public TableView<TableHSample> getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(TableView<TableHSample> tableHeader) {
        this.tableHeader = tableHeader;
        this.tableHeader.getColumns().clear();
    }

    public void showColumnSums(Boolean showColumnSums) {
        this.showColumnSums = showColumnSums;
    }

    public JFXCheckBox getFilterEnabledBox() {
        return filterEnabledBox;
    }
}
