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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartElements.TableEntry;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.ChartType;
import org.jevis.application.Chart.Charts.*;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.application.tools.TableViewUntils;
import org.jevis.jeconfig.tool.I18n;

import java.util.*;

import static javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY;

/**
 * @author broder
 */
public class ChartView implements Observer {

    private final GraphDataModel dataModel;
    private static final Logger logger = LogManager.getLogger(ChartView.class);
    private final double VALUE_COLUMNS_PREF_SIZE = 200;
    private final double VALUE_COLUMNS_MIN_SIZE = VALUE_COLUMNS_PREF_SIZE - 70;
    private Chart chart;
    private TableView tableView;
    private AlphanumComparator alphanumComparator = new AlphanumComparator();
    private ChartType chartType = ChartType.AREA;
    private String chartName = "";
    private boolean changed = false;
    private Integer chartId;

    public ChartView(GraphDataModel dataModel) {
        this.dataModel = dataModel;

        tableView = new TableView();

        tableView.setBorder(null);
        tableView.setStyle(
                ".table-view:focused {" +
                        "-fx-padding: 0; " +
                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: -1.4,0,1;" +
                        "}");
        tableView.getStylesheets().add
                (ChartView.class.getResource("/styles/ScrolllesTable.css").toExternalForm());
        tableView.sortPolicyProperty().set((Callback<TableView<TableEntry>, Boolean>) param -> {

            Comparator<TableEntry> comparator = (t1, t2) -> getAlphanumComparator().compare(t1.getName(), t2.getName());
            FXCollections.sort(getTableView().getItems(), comparator);
            return true;
        });

        tableView.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                logger.info("ScrollEvent: " + event.toString());
            }
        });
        tableView.setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);

        /** Disabled because of out ScrolllesTable.css **/
        TableColumn name = new TableColumn(I18n.getInstance().getString("plugin.graph.table.name"));
        name.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("name"));
        name.setSortable(false);
        name.setPrefWidth(500);
        name.setMinWidth(100);

        TableColumn colorCol = buildColorColumn("");
        colorCol.setSortable(false);
        colorCol.setPrefWidth(25);
        colorCol.setMinWidth(25);

        TableColumn periodCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.period"));
        periodCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("period"));
        periodCol.setStyle("-fx-alignment: CENTER-RIGHT");
        periodCol.setSortable(false);
        periodCol.setPrefWidth(100);
        periodCol.setMinWidth(100);

        TableColumn value = new TableColumn(I18n.getInstance().getString("plugin.graph.table.value"));
        value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));
        value.setStyle("-fx-alignment: CENTER-RIGHT");
        value.setSortable(false);
        value.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        value.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        TableColumn dateCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.date"));
        dateCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("date"));
        dateCol.setStyle("-fx-alignment: CENTER");
        dateCol.setSortable(false);
        dateCol.setPrefWidth(160);
        dateCol.setMinWidth(160);

        TableColumn note = new TableColumn(I18n.getInstance().getString("plugin.graph.table.note"));
        note.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("note"));
        note.setStyle("-fx-alignment: CENTER");
        note.setPrefWidth(50);
        note.setMinWidth(50);
        note.setSortable(false);

        TableColumn minCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.min"));
        minCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("min"));
        minCol.setStyle("-fx-alignment: CENTER-RIGHT");
        minCol.setSortable(false);
        minCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        minCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        TableColumn maxCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.max"));
        maxCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("max"));
        maxCol.setStyle("-fx-alignment: CENTER-RIGHT");
        maxCol.setSortable(false);
        maxCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        maxCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        TableColumn avgCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.avg"));
        avgCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avg"));
        avgCol.setStyle("-fx-alignment: CENTER-RIGHT");
        avgCol.setSortable(false);
        avgCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        avgCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        TableColumn sumCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.sum"));
        sumCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("sum"));
        sumCol.setStyle("-fx-alignment: CENTER-RIGHT");
        sumCol.setSortable(false);
        sumCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
        sumCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

        final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
        TableEntry tableEntry = new TableEntry("empty");
        tableData.add(tableEntry);
        tableView.setItems(tableData);

        tableView.getColumns().addAll(colorCol, name, periodCol, value, dateCol, note, minCol, maxCol, avgCol, sumCol);

        TableColumn[] maxSizeColumns = new TableColumn[]{name};

        tableView.widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                TableViewUntils.allToMinButColumn(tableView, Arrays.asList(maxSizeColumns));
            });

        });
    }

    private TableColumn<TableEntry, Color> buildColorColumn(String columnName) {
        TableColumn<TableEntry, Color> column = new TableColumn(columnName);


        column.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getColor() != null)
                return new SimpleObjectProperty<>(param.getValue().getColor());
            else return new SimpleObjectProperty<>();
        });

        column.setCellFactory(new Callback<TableColumn<TableEntry, Color>, TableCell<TableEntry, Color>>() {
            @Override
            public TableCell<TableEntry, Color> call(TableColumn<TableEntry, Color> param) {
                TableCell<TableEntry, Color> cell = new TableCell<TableEntry, Color>() {
                    @Override
                    public void commitEdit(Color newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(Color item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            StackPane stackPane = new StackPane();
                            stackPane.setBackground(
                                    new Background(
                                            new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));
//                            }

                            setText(null);
                            setGraphic(stackPane);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };
                return cell;
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
        tableView.setVisible(false);
        tableView.setItems(chart.getTableData());
        tableView.setFixedCellSize(25);
        tableView.prefHeightProperty().unbind();
        tableView.setPrefHeight(0);
    }

    private void setTableStandard() {
        tableView.setVisible(true);
        tableView.setItems(chart.getTableData());
        tableView.setFixedCellSize(25);
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(30));
    }

    public Region getChartRegion() {
        if (chart != null)
            if (chart.getRegion() != null)
                return chart.getRegion();
            else return chart.getChart();
        else return null;
    }

    public TableView getLegend() {
        return tableView;
    }

    public void drawAreaChart(Integer chartId, ChartType chartType) {
        this.chartId = chartId;

        chart = null;

        List<ChartDataModel> chartDataModels = new ArrayList<>();

        for (ChartDataModel singleRow : dataModel.getSelectedData()) {
            for (int i : singleRow.getSelectedcharts()) {
                if (i == chartId) {
                    chartDataModels.add(singleRow);
                }
            }
        }

        generateChart(chartId, chartType, chartDataModels);

        tableView.sort();
    }

    private void generateChart(Integer chartId, ChartType chartType, List<ChartDataModel> chartDataModels) {
        switch (chartType) {
            case AREA:
                chart = new AreaChart(chartDataModels, dataModel.getHideShowIcons(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
                break;
            case LINE:
                chart = new LineChart(chartDataModels, dataModel.getHideShowIcons(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
                break;
            case BAR:
                chart = new BarChart(chartDataModels, dataModel.getHideShowIcons(), chartId, chartName);
                setTableStandard();
                break;
            case BUBBLE:
                chart = new BubbleChart(chartDataModels, dataModel.getHideShowIcons(), chartId, chartName);
                setTableStandard();
                break;
            case SCATTER:
                chart = new ScatterChart(chartDataModels, dataModel.getHideShowIcons(), chartId, chartName);
                setTableStandard();
                break;
            case PIE:
                chart = new PieChart(chartDataModels, dataModel.getHideShowIcons(), chartId, chartName);
                disableTable();
                break;
            default:
                chart = new AreaChart(chartDataModels, dataModel.getHideShowIcons(), dataModel.getAddSeries(), chartId, getChartName());
                setTableStandard();
                break;
        }
    }

    public void updateTablesSimultaneously(MouseEvent mouseEvent, Number valueForDisplay) {
        chart.updateTable(mouseEvent, valueForDisplay);
    }


    public TableView getTableView() {
        return tableView;
    }


    public Number getValueForDisplay() {
        return chart.getValueForDisplay();
    }

    public ChartType getChartType() {
        return chartType;
    }

    public String getChartName() {

        for (ChartSettings set : dataModel.getCharts()) {
            if (set.getId() == chartId) {
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
            List<ChartDataModel> chartDataModels = new ArrayList<>();

            for (ChartDataModel singleRow : dataModel.getSelectedData()) {
                for (int i : singleRow.getSelectedcharts()) {
                    if (i == chartId) {
                        chartDataModels.add(singleRow);
                    }
                }
            }
            if (!getChanged()) {
                chart.setTitle(getChartName());
                chart.setDataModels(chartDataModels);

                chart.updateChart();
            } else {

                generateChart(chartId, chartType, chartDataModels);
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
}
