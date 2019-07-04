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
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartSettings;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.*;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.application.jevistree.AlphanumComparator;
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
    private final double VALUE_COLUMNS_MIN_SIZE = this.VALUE_COLUMNS_PREF_SIZE - 70;
    private TableColumn<TableEntry, String> nameCol;
    private TableColumn<TableEntry, Color> colorCol;
    private TableColumn<TableEntry, String> periodCol;
    private TableColumn<TableEntry, String> dateCol;
    private TableColumn<TableEntry, String> noteCol;
    private TableViewContextMenuHelper contextMenuHelper;

    public ChartView(GraphDataModel dataModel) {
        this.dataModel = dataModel;

        init();
    }

    public ChartView(GraphDataModel dataModel, Boolean showTable) {
        this.dataModel = dataModel;
        this.showTable = showTable;

        init();
    }

    private void init() {
        this.tableView = new TableView<>();

        this.tableView.setBorder(null);
        this.tableView.setStyle(
                ".table-view:focused {" +
                        "-fx-padding: 0; " +
                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: -1.4,0,1;" +
                        "}");
        this.tableView.getStylesheets().add
                (ChartView.class.getResource("/styles/TableViewNoScrollbar.css").toExternalForm());
        this.tableView.setSortPolicy(param -> {
            Comparator<TableEntry> comparator = (t1, t2) -> getAlphanumComparator().compare(t1.getName(), t2.getName());
            FXCollections.sort(getTableView().getItems(), comparator);
            return true;
        });

        this.tableView.setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);

        /** Disabled because of out TableViewNoScrollbar.css

         /**
         * Table Column 0
         */
        this.colorCol = buildColorColumn();
        this.colorCol.setSortable(false);
        this.colorCol.setPrefWidth(25);
        this.colorCol.setMinWidth(25);

        /**
         * Table Column 1
         */
        this.nameCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.name"));
        this.nameCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("name"));
        this.nameCol.setSortable(false);
        this.nameCol.setPrefWidth(500);
        this.nameCol.setMinWidth(100);

        /**
         * Table Column 2
         */
        this.periodCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.period"));
        this.periodCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("period"));
        this.periodCol.setStyle("-fx-alignment: CENTER-RIGHT");
        this.periodCol.setSortable(false);
        this.periodCol.setPrefWidth(100);
        this.periodCol.setMinWidth(100);

        /**
         * Table Column 3
         */
        TableColumn<TableEntry, String> value = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.value"));
        value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));
        value.setStyle("-fx-alignment: CENTER-RIGHT");
        value.setSortable(false);

        value.setPrefWidth(this.VALUE_COLUMNS_PREF_SIZE);
        value.setMinWidth(this.VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 4
         */
        this.dateCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.date"));
        this.dateCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("date"));
        this.dateCol.setStyle("-fx-alignment: CENTER");
        this.dateCol.setSortable(false);
        this.dateCol.setPrefWidth(160);
        this.dateCol.setMinWidth(160);

        /**
         * Table Column 5
         */
        this.noteCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.note"));
        this.noteCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("note"));
        this.noteCol.setStyle("-fx-alignment: CENTER");
        this.noteCol.setPrefWidth(50);
        this.noteCol.setMinWidth(50);
        this.noteCol.setSortable(false);

        /**
         * Table Column 6
         */
        TableColumn<TableEntry, String> minCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.min"));
        minCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("min"));
        minCol.setStyle("-fx-alignment: CENTER-RIGHT");
        minCol.setSortable(false);
        minCol.setPrefWidth(this.VALUE_COLUMNS_PREF_SIZE);
        minCol.setMinWidth(this.VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 7
         */
        TableColumn<TableEntry, String> maxCol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.max"));
        maxCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("max"));
        maxCol.setStyle("-fx-alignment: CENTER-RIGHT");
        maxCol.setSortable(false);
        maxCol.setPrefWidth(this.VALUE_COLUMNS_PREF_SIZE);
        maxCol.setMinWidth(this.VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 8
         */
        TableColumn<TableEntry, String> avgCol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.avg"));
        avgCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avg"));
        avgCol.setStyle("-fx-alignment: CENTER-RIGHT");
        avgCol.setSortable(false);
        avgCol.setPrefWidth(this.VALUE_COLUMNS_PREF_SIZE);
        avgCol.setMinWidth(this.VALUE_COLUMNS_MIN_SIZE);

        /**
         * Table Column 9
         */
        TableColumn<TableEntry, String> enPICol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.enpi"));
        enPICol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("enpi"));
        enPICol.setStyle("-fx-alignment: CENTER-RIGHT");
        enPICol.setSortable(false);
        enPICol.setPrefWidth(this.VALUE_COLUMNS_PREF_SIZE);
        enPICol.setMinWidth(this.VALUE_COLUMNS_MIN_SIZE);
        enPICol.setVisible(false);

        /**
         * Table Column 10
         */
        TableColumn<TableEntry, String> sumCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.sum"));
        sumCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("sum"));
        sumCol.setStyle("-fx-alignment: CENTER-RIGHT");
        sumCol.setSortable(false);
        sumCol.setPrefWidth(this.VALUE_COLUMNS_PREF_SIZE);
        sumCol.setMinWidth(this.VALUE_COLUMNS_MIN_SIZE);

        final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
        TableEntry tableEntry = new TableEntry("empty");
        tableData.add(tableEntry);
        this.tableView.setItems(tableData);

        this.tableView.getColumns().addAll(this.colorCol, this.nameCol, this.periodCol, value, this.dateCol, this.noteCol, minCol, maxCol, avgCol, enPICol, sumCol);
        this.tableView.setTableMenuButtonVisible(true);
        this.contextMenuHelper = new TableViewContextMenuHelper(this.tableView);

        this.tableView.widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::updateColumnCaptionWidths);
        });

        enPICol.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                Platform.runLater(this::updateColumnCaptionWidths);
            }
        });
    }

    public void updateColumnCaptionWidths() {
        TableViewUtils.allToMin(this.tableView);
        TableViewUtils.growColumns(this.tableView, Collections.singletonList(this.nameCol));
        if (this.contextMenuHelper.getTableHeaderRow() != null) {
            this.contextMenuHelper.getTableHeaderRow().setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    this.contextMenuHelper.showContextMenu();
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
        return this.chart;
    }

    private void disableTable() {
        this.tableView.setVisible(false);
        this.tableView.setItems(this.chart.getTableData());
        this.tableView.setFixedCellSize(25);
        this.tableView.prefHeightProperty().unbind();
        this.tableView.setPrefHeight(0);
    }

    private void setTableStandard() {
        this.tableView.setVisible(true);
        this.tableView.setItems(this.chart.getTableData());
        this.tableView.setFixedCellSize(25);
        this.tableView.prefHeightProperty().bind(Bindings.size(this.tableView.getItems()).multiply(this.tableView.getFixedCellSize()).add(30));
    }

    public Region getChartRegion() {
//        StackPane stackPane = new StackPane();

        if (this.chart != null) {
            if (this.chart.getRegion() != null) {
//                stackPane.getChildren().add(chart.getRegion());
                return this.chart.getRegion();
            } else {
//                stackPane.getChildren().add(chart.getChart());
                return this.chart.getChart();
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
//                    dataModel.updateData();
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
        return this.tableView;
    }

    public void drawAreaChart(Integer chartId, ChartType chartType) {
        this.chartId = chartId;

        this.chart = null;

        this.currentSelectedChartDataModels = new ArrayList<>();

        for (ChartDataModel singleRow : this.dataModel.getSelectedData()) {
            for (int i : singleRow.getSelectedcharts()) {
                if (i == chartId) {
                    this.currentSelectedChartDataModels.add(singleRow);
                }
            }
        }

        generateChart(chartId, chartType, this.currentSelectedChartDataModels);

        this.tableView.sort();
    }

    public void drawAreaChart(Integer chartId, ChartDataModel model, ChartType chartType) {
        this.chartId = chartId;
        this.chart = null;
        this.singleRow = model;

        this.currentSelectedChartDataModels = new ArrayList<>();
        this.currentSelectedChartDataModels.add(model);

        generateChart(chartId, chartType, this.currentSelectedChartDataModels);

        this.tableView.sort();
    }

    private void generateChart(Integer chartId, ChartType chartType, List<ChartDataModel> chartDataModels) {
        this.chartType = chartType;
        boolean containsEnPI = chartDataModels.stream().anyMatch(ChartDataModel::getEnPI);
        switch (chartType) {
            case AREA:
                this.chart = new AreaChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), this.dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case LOGICAL:
                this.chart = new LogicalChart(chartDataModels, this.dataModel.getHideShowIcons(), this.dataModel.getAddSeries(), chartId, getChartName());
                if (this.showTable) {
                    setTableStandard();
                    this.tableView.getColumns().get(2).setVisible(false);
                    this.tableView.getColumns().get(5).setVisible(false);
                    this.tableView.getColumns().get(6).setVisible(false);
                    this.tableView.getColumns().get(7).setVisible(false);
                    this.tableView.getColumns().get(8).setVisible(false);
                    this.tableView.getColumns().get(9).setVisible(false);
                    this.tableView.getColumns().get(10).setVisible(false);
                } else disableTable();
                break;
            case LINE:
                this.chart = new LineChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), this.dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case BAR:
                this.chart = new BarChart(chartDataModels, this.dataModel.getHideShowIcons(), chartId, getChartName());
                setTableStandard();
                this.tableView.getColumns().get(4).setVisible(false);
                this.tableView.getColumns().get(5).setVisible(false);
                this.tableView.getColumns().get(6).setVisible(false);
                this.tableView.getColumns().get(7).setVisible(false);
                this.tableView.getColumns().get(8).setVisible(false);
                this.tableView.getColumns().get(9).setVisible(false);
                this.tableView.getColumns().get(10).setVisible(false);
                break;
            case COLUMN:
                this.chart = new ColumnChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case BUBBLE:
                this.chart = new BubbleChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), chartId, getChartName());
                disableTable();
                break;
            case SCATTER:
                this.chart = new ScatterChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), this.dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
            case PIE:
                this.chart = new PieChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), chartId, getChartName());
                disableTable();
                break;
            case TABLE:
                this.chart = new TableChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), this.dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
                this.tableView.getColumns().get(0).setVisible(false);
                this.tableView.getColumns().get(2).setVisible(false);
                this.tableView.getColumns().get(5).setVisible(false);
                this.tableView.getColumns().get(6).setVisible(false);
                this.tableView.getColumns().get(7).setVisible(false);
                this.tableView.getColumns().get(8).setVisible(false);
                this.tableView.getColumns().get(9).setVisible(false);
                this.tableView.getColumns().get(10).setVisible(false);
                break;
            default:
                this.chart = new AreaChart(chartDataModels, this.dataModel.getShowRawData(), this.dataModel.getShowSum(), this.dataModel.getHideShowIcons(), this.dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
//                tableView.getColumns().get(9).setVisible(containsEnPI);
                break;
        }
    }

    public void updateTablesSimultaneously(MouseEvent mouseEvent, DateTime valueForDisplay) {
        this.chart.updateTable(mouseEvent, valueForDisplay);
    }


    public TableView<TableEntry> getTableView() {
        return this.tableView;
    }


    public DateTime getValueForDisplay() {
        return this.chart.getValueForDisplay();
    }

    public ChartType getChartType() {
        return this.chartType;
    }

    public String getChartName() {

        for (ChartSettings set : this.dataModel.getCharts()) {
            if (set.getId().equals(this.chartId)) {
                this.chartName = set.getName();
                break;
            }
        }

        return this.chartName;
    }

    public AlphanumComparator getAlphanumComparator() {
        return this.alphanumComparator;
    }

    public void updateChart() {
        if (this.chart != null) {
            this.currentSelectedChartDataModels = new ArrayList<>();

            for (ChartDataModel singleRow : this.dataModel.getSelectedData()) {
                for (int i : singleRow.getSelectedcharts()) {
                    if (i == this.chartId) {
                        this.currentSelectedChartDataModels.add(singleRow);
                    }
                }
            }
            if (!getChanged()) {
                this.chart.setTitle(getChartName());
                this.chart.setHideShowIcons(this.dataModel.getHideShowIcons());
                this.chart.setDataModels(this.currentSelectedChartDataModels);
                boolean containsEnPI = this.currentSelectedChartDataModels.stream().anyMatch(ChartDataModel::getEnPI);
//                tableView.getColumns().get(9).setVisible(containsEnPI);

                this.chart.updateChart();
            } else {

                generateChart(getChartId(), getChartType(), this.currentSelectedChartDataModels);
            }

            this.tableView.sort();
        }
    }

    public void setType(ChartType chartType) {
        if (!this.chartType.equals(chartType))
            this.setChanged(true);

        this.chartType = chartType;
    }

    public boolean getChanged() {
        return this.changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Boolean getShowTable() {
        return this.showTable;
    }

    public boolean getFirstLogical() {
        return this.firstLogical;
    }

    public void setFirstLogical(boolean firstLogical) {
        this.firstLogical = firstLogical;
    }

    public ChartDataModel getSingleRow() {
        return this.singleRow;
    }

    public void setSingleRow(ChartDataModel singleRow) {
        this.singleRow = singleRow;
    }

    public void setShowTable(Boolean showTable) {
        this.showTable = showTable;
    }

    public Integer getChartId() {
        return this.chartId;
    }

    public void setChartId(Integer chartId) {
        this.chartId = chartId;
    }
}
