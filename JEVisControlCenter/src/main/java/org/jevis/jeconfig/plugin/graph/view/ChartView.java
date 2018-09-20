/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.graph.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.ChartSettings;
import org.jevis.application.Chart.Charts.*;
import org.jevis.application.Chart.TableEntry;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.jevistree.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.format.PeriodFormat;

import java.util.*;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

/**
 * @author broder
 */
public class ChartView implements Observer {

    private final GraphDataModel dataModel;

    private Chart chart;

    private TableView tableView;

    private final Logger logger = LogManager.getLogger(ChartView.class);
    private AlphanumComparator alphanumComparator = new AlphanumComparator();

    public ChartView(GraphDataModel dataModel) {
        this.dataModel = dataModel;
        //dataModel.addObserver(this);

        tableView = new TableView();

        tableView.sortPolicyProperty().set((Callback<TableView<TableEntry>, Boolean>) param -> {

            Comparator<TableEntry> comparator = (t1, t2) -> getAlphanumComparator().compare(t1.getName(), t2.getName());
            FXCollections.sort(getTableView().getItems(), comparator);
            return true;
        });

        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
//        tableView.setFixedCellSize(25);
//        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(30));
        TableColumn name = new TableColumn(I18n.getInstance().getString("plugin.graph.table.name"));
        name.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("name"));
        name.setSortable(false);

//        TableColumn colorCol = new TableColumn("Color333");
//        colorCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("color"));
        TableColumn colorCol = buildColorColumn(I18n.getInstance().getString("plugin.graph.table.color"));
        colorCol.setSortable(false);

        TableColumn value = new TableColumn(I18n.getInstance().getString("plugin.graph.table.value"));
        value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));
        value.setStyle("-fx-alignment: CENTER-RIGHT");
        value.setSortable(false);

        TableColumn dateCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.date"));
        dateCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("date"));
        dateCol.setStyle("-fx-alignment: CENTER");
        dateCol.setSortable(false);

        TableColumn note = new TableColumn(I18n.getInstance().getString("plugin.graph.table.note"));
        note.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("note"));
        note.setStyle("-fx-alignment: CENTER");
        note.setPrefWidth(15);
        note.setSortable(false);

        TableColumn minCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.min"));
        minCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("min"));
        minCol.setStyle("-fx-alignment: CENTER-RIGHT");
        minCol.setSortable(false);

        TableColumn maxCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.max"));
        maxCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("max"));
        maxCol.setStyle("-fx-alignment: CENTER-RIGHT");
        maxCol.setSortable(false);

        TableColumn avgCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.avg"));
        avgCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avg"));
        avgCol.setStyle("-fx-alignment: CENTER-RIGHT");
        avgCol.setSortable(false);

        TableColumn sumCol = new TableColumn(I18n.getInstance().getString("plugin.graph.table.sum"));
        sumCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("sum"));
        sumCol.setStyle("-fx-alignment: CENTER-RIGHT");
        sumCol.setSortable(false);

        final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
        TableEntry tableEntry = new TableEntry("empty");
        tableData.add(tableEntry);
        tableView.setItems(tableData);

        tableView.getColumns().addAll(name, colorCol, value, dateCol, note, minCol, maxCol, avgCol, sumCol);
    }

    private TableColumn<TableEntry, Color> buildColorColumn(String columnName) {
        TableColumn<TableEntry, Color> column = new TableColumn(columnName);
        column.setPrefWidth(100);
        column.setMaxWidth(100);
        column.setMinWidth(100);

        column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getColor()));

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
//        try {
//            getChartsList();
//            if (chartsList.size() == 1) this.drawAreaChart("");
//            else if (chartsList.size() > 1) getChartViews();
//
//        } catch (JEVisException ex) {
//            Logger.getLogger(ChartView.class.getName()).log(Level.SEVERE, null, ex);
//        }
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

    private ChartSettings.ChartType chartType = ChartSettings.ChartType.AREA;
    private String chartName = "";

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

    public void drawAreaChart(String chartName, ChartSettings.ChartType chartType) {

        chart = null;

        Set<ChartDataModel> selectedData = dataModel.getSelectedData();

        String title = I18n.getInstance().getString("plugin.graph.chart.title1");

        List<ChartDataModel> chartDataModels = new ArrayList<>();

        for (ChartDataModel singleRow : selectedData) {
            if (Objects.isNull(chartName) || chartName.equals("") || singleRow.get_selectedCharts().contains(chartName)) {
                if (chartName == "" || chartName == null) {
                    if (singleRow.get_selectedCharts().size() == 1) title = singleRow.get_selectedCharts().get(0);
                } else title = chartName;
                chartDataModels.add(singleRow);
            }
        }

        this.chartName = title;

        switch (chartType.toString()) {
            case ("AREA"):
                chart = new AreaChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                setTableStandard();
                addPeriodToValueColumn();
                break;
            case ("LINE"):
                chart = new LineChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                setTableStandard();
                addPeriodToValueColumn();
                break;
            case ("BAR"):
                chart = new BarChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                setTableStandard();
                break;
            case ("BUBBLE"):
                chart = new BubbleChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                setTableStandard();
                break;
            case ("SCATTER"):
                chart = new ScatterChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                setTableStandard();
                break;
            case ("PIE"):
                chart = new PieChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                disableTable();
                break;
            default:
                chart = new AreaChart(chartDataModels, dataModel.getHideShowIcons(), chartName);
                setTableStandard();
                break;
        }

        tableView.sort();
    }

    private void addPeriodToValueColumn() {
        TableColumn tc = (TableColumn) tableView.getColumns().get(2);
        tc.setText(tc.getText() + " [" + chart.getPeriod().toString(PeriodFormat.wordBased().withLocale(JEConfig.getConfig().getLocale())) + "]");
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

    public ChartSettings.ChartType getChartType() {
        return chartType;
    }

    public String getChartName() {
        if (chartName == null) {
            this.chartName = chart.getChartName();
        }
        return this.chartName;
    }

    public AlphanumComparator getAlphanumComparator() {
        return alphanumComparator;
    }
}
