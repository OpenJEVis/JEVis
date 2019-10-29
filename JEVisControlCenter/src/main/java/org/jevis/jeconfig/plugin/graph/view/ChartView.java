/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.*;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.tools.TableViewUtils;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.util.*;

import static javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY;

/**
 * @author broder
 */
public class ChartView implements Observer {

    private final GraphDataModel dataModel;
    private static final Logger logger = LogManager.getLogger(ChartView.class);
    private Boolean showTable = true;
    private Chart chart;
    private TableView<TableEntry> tableView;
    private AlphanumComparator alphanumComparator = new AlphanumComparator();
    private ChartType chartType = ChartType.AREA;
    private String chartName = "";
    private boolean changed = false;
    private Integer chartId;
    private boolean firstLogical;
    private ChartDataModel singleRow;
    private List<ChartDataModel> currentSelectedChartDataModels;
    private final double VALUE_COLUMNS_PREF_SIZE = 200;
    private final double VALUE_COLUMNS_MIN_SIZE = VALUE_COLUMNS_PREF_SIZE - 60;
    private TableColumn<TableEntry, String> nameCol;
    private TableColumn<TableEntry, Color> colorCol;
    private TableColumn<TableEntry, String> periodCol;
    private TableColumn<TableEntry, String> dateCol;
    private TableColumn<TableEntry, String> noteCol;
    private TableViewContextMenuHelper contextMenuHelper;

    public ChartView(GraphDataModel dataModel) {
        this.dataModel = dataModel;
    }

    private void init() {
        tableView = new TableView<>();

        tableView.setBorder(null);
        tableView.setStyle(
                ".table-view:focused {" +
                        "-fx-padding: 0; " +
                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: -1.4,0,1;" +
                        "}");
        tableView.getStylesheets().add
                (ChartView.class.getResource("/styles/TableViewNoScrollbar.css").toExternalForm());
        tableView.setSortPolicy(param -> {
            Comparator<TableEntry> comparator = (t1, t2) -> getAlphanumComparator().compare(t1.getName(), t2.getName());
            FXCollections.sort(getTableView().getItems(), comparator);
            return true;
        });

        tableView.setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);

        /** Disabled because of out TableViewNoScrollbar.css

         /**
         * Table Column 0
         */
        colorCol = buildColorColumn();
        colorCol.setSortable(false);
        colorCol.setPrefWidth(25);
        colorCol.setMinWidth(25);

        /**
         * Table Column 1
         */
        nameCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
        nameCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("name"));
        nameCol.setSortable(false);
        nameCol.setPrefWidth(500);
        nameCol.setMinWidth(100);

        final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
        TableEntry tableEntry = new TableEntry("empty");
        tableData.add(tableEntry);
        tableView.setItems(tableData);

        switch (chartType) {
            case BUBBLE:
                /**
                 * Table Column 2
                 */

                TableColumn<TableEntry, String> xValue = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.xValue"));
                xValue.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("xValue"));
                xValue.setStyle("-fx-alignment: CENTER-RIGHT");
                xValue.setSortable(false);

                xValue.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                xValue.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 3
                 */

                TableColumn<TableEntry, String> yValue = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.yValue"));
                yValue.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("yValue"));
                yValue.setStyle("-fx-alignment: CENTER-RIGHT");
                yValue.setSortable(false);

                yValue.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                yValue.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 4
                 */

                TableColumn<TableEntry, String> standardDeviation = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.standardDeviation"));
                standardDeviation.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("standardDeviation"));
                standardDeviation.setStyle("-fx-alignment: CENTER-RIGHT");
                standardDeviation.setSortable(false);

                standardDeviation.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                standardDeviation.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 5
                 */

                TableColumn<TableEntry, String> variance = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.variance"));
                variance.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("variance"));
                variance.setStyle("-fx-alignment: CENTER-RIGHT");
                variance.setSortable(false);

                variance.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                variance.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                tableView.getColumns().addAll(colorCol, nameCol, xValue, yValue, standardDeviation, variance);
                break;
            default:
                /**
                 * Table Column 2
                 */
                periodCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.period"));
                periodCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("period"));
                periodCol.setStyle("-fx-alignment: CENTER-RIGHT");
                periodCol.setSortable(false);
                periodCol.setPrefWidth(100);
                periodCol.setMinWidth(100);

                /**
                 * Table Column 3
                 */
                TableColumn<TableEntry, String> value = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.value"));
                value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));
                value.setStyle("-fx-alignment: CENTER-RIGHT");
                value.setSortable(false);

                value.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                value.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 4
                 */
                dateCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.date"));
                dateCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("date"));
                dateCol.setStyle("-fx-alignment: CENTER");
                dateCol.setSortable(false);
                dateCol.setPrefWidth(160);
                dateCol.setMinWidth(160);

                /**
                 * Table Column 5
                 */
                noteCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.note"));
                noteCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("note"));
                noteCol.setStyle("-fx-alignment: CENTER");
                noteCol.setPrefWidth(50);
                noteCol.setMinWidth(50);
                noteCol.setSortable(false);

                /**
                 * Table Column 6
                 */
                TableColumn<TableEntry, String> minCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.min"));
                minCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("min"));
                minCol.setStyle("-fx-alignment: CENTER-RIGHT");
                minCol.setSortable(false);
                minCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                minCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 7
                 */
                TableColumn<TableEntry, String> maxCol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.max"));
                maxCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("max"));
                maxCol.setStyle("-fx-alignment: CENTER-RIGHT");
                maxCol.setSortable(false);
                maxCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                maxCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 8
                 */
                TableColumn<TableEntry, String> avgCol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.avg"));
                avgCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avg"));
                avgCol.setStyle("-fx-alignment: CENTER-RIGHT");
                avgCol.setSortable(false);
                avgCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                avgCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 9
                 */
                TableColumn<TableEntry, String> enPICol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.enpi"));
                enPICol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("enpi"));
                enPICol.setStyle("-fx-alignment: CENTER-RIGHT");
                enPICol.setSortable(false);
                enPICol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                enPICol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);
                enPICol.setVisible(false);

                /**
                 * Table Column 10
                 */
                TableColumn<TableEntry, String> sumCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.sum"));
                sumCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("sum"));
                sumCol.setStyle("-fx-alignment: CENTER-RIGHT");
                sumCol.setSortable(false);
                sumCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                sumCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                tableView.getColumns().addAll(colorCol, nameCol, periodCol, value, dateCol, noteCol, minCol, maxCol, avgCol, enPICol, sumCol);
                tableView.setTableMenuButtonVisible(true);
                contextMenuHelper = new TableViewContextMenuHelper(tableView);

                tableView.widthProperty().addListener((observable, oldValue, newValue) -> {
                    Platform.runLater(this::updateColumnCaptionWidths);
                });

                enPICol.visibleProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != oldValue) {
                        Platform.runLater(this::updateColumnCaptionWidths);
                    }
                });
                break;
        }


    }

    public void updateColumnCaptionWidths() {
        TableViewUtils.allToMin(tableView);
        TableViewUtils.growColumns(tableView, Collections.singletonList(nameCol));
        if (contextMenuHelper.getTableHeaderRow() != null) {
            contextMenuHelper.getTableHeaderRow().setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenuHelper.showContextMenu();
                }
            });
        }
    }

    private TableColumn<TableEntry, Color> buildColorColumn() {
        TableColumn<TableEntry, Color> column = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.color"));


        column.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getColor() != null)
                return new SimpleObjectProperty<>(param.getValue().getColor());
            else return new SimpleObjectProperty<>();
        });

        column.setCellFactory(new Callback<TableColumn<TableEntry, Color>, TableCell<TableEntry, Color>>() {
            @Override
            public TableCell<TableEntry, Color> call(TableColumn<TableEntry, Color> param) {
                return new TableCell<TableEntry, Color>() {
                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(null);

                        if (!empty && item != null) {
                            StackPane stackPane = new StackPane();
                            stackPane.setBackground(
                                    new Background(
                                            new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));
//                            }

                            setText(null);
                            setGraphic(stackPane);
                        }
                    }
                };
            }
        });

        return column;

    }

    @Override
    public void update(Observable o, Object arg) {
    }

    public Chart getChart() {
        return chart;
    }

    private void disableTable() {
        showTable = false;
        tableView.setVisible(false);
        tableView.setItems(chart.getTableData());
        tableView.setFixedCellSize(25);
        tableView.prefHeightProperty().unbind();
        tableView.setPrefHeight(0);
    }

    private void setTableStandard() {
        showTable = true;
        tableView.setVisible(true);
        tableView.setItems(chart.getTableData());
        tableView.setFixedCellSize(25);
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(30));
    }

    public Region getChartRegion() {
//        StackPane stackPane = new StackPane();

        if (chart != null) {
            if (chart.getRegion() != null) {
//                stackPane.getChildren().add(chart.getRegion());
                return chart.getRegion();
            } else {
//                stackPane.getChildren().add(chart.getChart());
                return chart.getChart();
            }


            /**
             * disabled
             */
//            if (!getChartType().equals(ChartType.LOGICAL)) {

//                HBox pickerBox = new HBox();
//                CalendarIcon calendarIcon = new CalendarIcon();
//                HBox iconBox = new HBox();
//
//                pickerBox.setPadding(new Insets(2, 2, 2, 2));
//                pickerBox.setPickOnBounds(false);
//                calendarIcon.getIcon().setPickOnBounds(false);
//                iconBox.setPadding(new Insets(4, 4, 4, 4));
//                iconBox.setPickOnBounds(false);
//                PickerCombo pickerCombo = new PickerCombo(dataModel, currentSelectedChartDataModels);
//                JFXDatePicker startDatePicker = pickerCombo.getStartDatePicker();
//                startDatePicker.setPickOnBounds(false);
//                JFXTimePicker startTimePicker = pickerCombo.getStartTimePicker();
//                startTimePicker.setPickOnBounds(false);
//                JFXDatePicker endDatePicker = pickerCombo.getEndDatePicker();
//                endDatePicker.setPickOnBounds(false);
//                JFXTimePicker endTimePicker = pickerCombo.getEndTimePicker();
//                endTimePicker.setPickOnBounds(false);
//
//                ChangeListener<LocalDate> localDateChangeListener = (observable, oldValue, newValue) -> {
//                    pickerBox.setVisible(false);
//                    dataModel.update();
//                    iconBox.setVisible(true);
//                };
//                startDatePicker.valueProperty().addListener(localDateChangeListener);
//                endDatePicker.valueProperty().addListener(localDateChangeListener);
//
//                iconBox.getChildren().addAll(calendarIcon.getIcon());
//                iconBox.setAlignment(Pos.TOP_RIGHT);
//                iconBox.setOnMouseClicked(event -> {
//                    pickerBox.setVisible(true);
//                    iconBox.setVisible(false);
//                });
//
//                pickerBox.getChildren().addAll(startDatePicker, endDatePicker);
//                pickerBox.setAlignment(Pos.TOP_RIGHT);
//                pickerBox.setVisible(false);
//
//                stackPane.getChildren().addAll(pickerBox, iconBox);
//            }

//            return stackPane;
        } else return null;
    }

    public TableView<TableEntry> getLegend() {
        return tableView;
    }

    public void drawAreaChart(Integer chartId, ChartType chartType) {
        this.chartId = chartId;

        chart = null;

        currentSelectedChartDataModels = new ArrayList<>();

        for (ChartDataModel singleRow : dataModel.getSelectedData()) {
            for (int i : singleRow.getSelectedcharts()) {
                if (i == chartId) {
                    currentSelectedChartDataModels.add(singleRow);
                }
            }
        }

        generateChart(chartId, chartType, currentSelectedChartDataModels);
    }

    public void drawAreaChart(Integer chartId, ChartDataModel model, ChartType chartType) {
        this.chartId = chartId;
        this.chart = null;
        this.singleRow = model;

        currentSelectedChartDataModels = new ArrayList<>();
        currentSelectedChartDataModels.add(model);

        generateChart(chartId, chartType, currentSelectedChartDataModels);

        tableView.sort();
    }

    private void generateChart(Integer chartId, ChartType chartType, List<ChartDataModel> chartDataModels) {
        this.chartType = chartType;
        init();
        boolean containsEnPI = chartDataModels.stream().anyMatch(ChartDataModel::getEnPI);
        switch (chartType) {
            case AREA:
                chart = new AreaChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getShowL1L2(), dataModel.getHideShowIcons(), dataModel.calcRegression(), dataModel.getRegressionType(), dataModel.getPolyRegressionDegree(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case LOGICAL:
                chart = new LogicalChart(chartDataModels, dataModel.getHideShowIcons(), dataModel.getAddSeries(), chartId, getChartName());
                if (showTable) {
                    setTableStandard();
                    tableView.getColumns().get(2).setVisible(false);
                    tableView.getColumns().get(5).setVisible(false);
                    tableView.getColumns().get(6).setVisible(false);
                    tableView.getColumns().get(7).setVisible(false);
                    tableView.getColumns().get(8).setVisible(false);
                    tableView.getColumns().get(9).setVisible(false);
                    tableView.getColumns().get(10).setVisible(false);
                } else disableTable();
                break;
            case LINE:
                chart = new LineChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getShowL1L2(), dataModel.getHideShowIcons(), dataModel.calcRegression(), dataModel.getRegressionType(), dataModel.getPolyRegressionDegree(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case BAR:
                chart = new BarChart(chartDataModels, dataModel.getHideShowIcons(), chartId, getChartName());
                setTableStandard();
                tableView.getColumns().get(4).setVisible(false);
                tableView.getColumns().get(5).setVisible(false);
                tableView.getColumns().get(6).setVisible(false);
                tableView.getColumns().get(7).setVisible(false);
                tableView.getColumns().get(8).setVisible(false);
                tableView.getColumns().get(9).setVisible(false);
                tableView.getColumns().get(10).setVisible(false);
                break;
            case COLUMN:
                chart = new ColumnChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getHideShowIcons(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case BUBBLE:
                chart = new BubbleChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getHideShowIcons(), dataModel.calcRegression(), dataModel.getRegressionType(), dataModel.getPolyRegressionDegree(), chartId, getChartName());
                setTableStandard();
                break;
            case SCATTER:
                chart = new ScatterChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getShowL1L2(), dataModel.getHideShowIcons(), dataModel.calcRegression(), dataModel.getRegressionType(), dataModel.getPolyRegressionDegree(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case PIE:
                chart = new PieChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getHideShowIcons(), chartId, getChartName());
                disableTable();
                break;
            case TABLE:
                chart = new TableChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getHideShowIcons(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
                tableView.getColumns().get(0).setVisible(false);
                tableView.getColumns().get(2).setVisible(false);
                tableView.getColumns().get(5).setVisible(false);
                tableView.getColumns().get(6).setVisible(false);
                tableView.getColumns().get(7).setVisible(false);
                tableView.getColumns().get(8).setVisible(false);
                tableView.getColumns().get(9).setVisible(false);
                tableView.getColumns().get(10).setVisible(false);
                break;
            case HEAT_MAP:
                chart = new HeatMapChart(chartDataModels, getChartName());
                disableTable();
                break;
            default:
                chart = new AreaChart(chartDataModels, dataModel.getShowRawData(), dataModel.getShowSum(), dataModel.getShowL1L2(), dataModel.getHideShowIcons(), dataModel.calcRegression(), dataModel.getRegressionType(), dataModel.getPolyRegressionDegree(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
        }
    }

    public void updateTablesSimultaneously(MouseEvent mouseEvent, DateTime valueForDisplay) {
        chart.updateTable(mouseEvent, valueForDisplay);
    }


    public TableView<TableEntry> getTableView() {
        return tableView;
    }


    public DateTime getValueForDisplay() {
        return chart.getValueForDisplay();
    }

    public ChartType getChartType() {
        return chartType;
    }

    public String getChartName() {

        for (ChartSettings set : dataModel.getCharts()) {
            if (set.getId().equals(chartId)) {
                chartName = set.getName();
                break;
            }
        }

        return this.chartName;
    }

    public AlphanumComparator getAlphanumComparator() {
        return alphanumComparator;
    }

    public void updateChart() {
        if (chart != null) {
            currentSelectedChartDataModels = new ArrayList<>();

            for (ChartDataModel singleRow : dataModel.getSelectedData()) {
                for (int i : singleRow.getSelectedcharts()) {
                    if (i == chartId) {
                        currentSelectedChartDataModels.add(singleRow);
                    }
                }
            }
            if (!getChanged()) {
                chart.setTitle(getChartName());
                chart.setHideShowIcons(dataModel.getHideShowIcons());
                chart.setDataModels(currentSelectedChartDataModels);
                boolean containsEnPI = currentSelectedChartDataModels.stream().anyMatch(ChartDataModel::getEnPI);
//                tableView.getColumns().get(9).setVisible(containsEnPI);

                chart.updateChart();
            } else {

                generateChart(getChartId(), getChartType(), currentSelectedChartDataModels);
            }

            tableView.sort();
        }
    }

    public void setType(ChartType chartType) {
        if (!this.chartType.equals(chartType))
            this.setChanged(true);

        this.chartType = chartType;
    }

    public boolean getChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Boolean getShowTable() {
        return showTable;
    }

    public boolean getFirstLogical() {
        return firstLogical;
    }

    public void setFirstLogical(boolean firstLogical) {
        this.firstLogical = firstLogical;
    }

    public ChartDataModel getSingleRow() {
        return singleRow;
    }

    public void setSingleRow(ChartDataModel singleRow) {
        this.singleRow = singleRow;
    }

    public void setShowTable(Boolean showTable) {
        this.showTable = showTable;
    }

    public Integer getChartId() {
        return chartId;
    }

    public void setChartId(Integer chartId) {
        this.chartId = chartId;
    }
}
