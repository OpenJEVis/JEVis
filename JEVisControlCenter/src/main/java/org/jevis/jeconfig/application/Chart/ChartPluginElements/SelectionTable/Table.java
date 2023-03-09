package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartModel;

public class Table extends TableView<ChartData> {

    private final TableColumn<ChartData, JEVisObject> objectNameColumn;
    private final TableColumn<ChartData, String> nameColumn;
    private final TableColumn<ChartData, Long> processorObjectColumn;
    private final TableColumn<ChartData, ChartType> chartTypeColumn;
    private final TableColumn<ChartData, Color> colorColumn;
    private final TableColumn<ChartData, JEVisUnit> unitColumn;
    private final TableColumn<ChartData, Boolean> intervalEnabledColumn;

    private final TableColumn<ChartData, String> intervalStartColumn;

    private final TableColumn<ChartData, String> intervalEndColumn;
    private final TableColumn<ChartData, Integer> axisColumn;
    private final TableColumn<ChartData, BubbleType> bubbleTypeColumn;
    private final TableColumn<ChartData, AggregationPeriod> aggregationPeriodColumn;
    private final TableColumn<ChartData, ManipulationMode> manipulationModeColumn;
    private final TableColumn<ChartData, Boolean> mathColumn;
    private final TableColumn<ChartData, String> cssColumn;
    private final StackPane dialogContainer;
    private final ChartModel chartModel;
    private final JEVisDataSource ds;

    public Table(StackPane dialogContainer, JEVisDataSource ds, ChartModel chartModel) {
        super();
        this.dialogContainer = dialogContainer;
        this.chartModel = chartModel;
        this.ds = ds;

        setTableMenuButtonVisible(true);
        setEditable(true);

        objectNameColumn = buildObjectNameColumn();
        nameColumn = buildNameColumn();
        processorObjectColumn = buildProcessorObjectColumn();
        chartTypeColumn = buildChartTypeColumn();
        colorColumn = buildColorColumn();
        unitColumn = buildUnitColumn();
        intervalEnabledColumn = buildIntervalColumn();
        intervalStartColumn = buildIntervalStartColumn();
        intervalEndColumn = buildIntervalEndColumn();
        axisColumn = buildAxisColumn();
        bubbleTypeColumn = buildBubbleTypeColumn();
        aggregationPeriodColumn = buildAggregationPeriodColumn();
        manipulationModeColumn = buildManipulationModeColumn();
        mathColumn = buildMathColumn();
        cssColumn = buildCssColumn();

        getColumns().setAll(objectNameColumn, nameColumn, processorObjectColumn, chartTypeColumn, colorColumn, unitColumn,
                intervalEnabledColumn, intervalStartColumn, intervalEndColumn,
                bubbleTypeColumn, axisColumn, aggregationPeriodColumn, manipulationModeColumn, mathColumn, cssColumn);
        getSortOrder().setAll(nameColumn);
    }

    private TableColumn<ChartData, JEVisObject> buildObjectNameColumn() {
        TableColumn<ChartData, JEVisObject> column = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.objectname"));
        column.setMinWidth(250);
        column.setStyle("-fx-alignment: CENTER-LEFT;");
        column.setCellValueFactory(new PropertyValueFactory<>("objectName"));
        column.setCellFactory(SelectionTableCell.forTableColumn(dialogContainer));
        column.setEditable(true);
        column.setOnEditCommit(chartDataJEVisObjectCellEditEvent -> {
            ChartData chartData = chartDataJEVisObjectCellEditEvent.getTableView().getItems().get(chartDataJEVisObjectCellEditEvent.getTablePosition().getRow());

            JEVisObject newObject = chartDataJEVisObjectCellEditEvent.getNewValue();
            chartData.setId(newObject.getID());
            chartData.setObjectName(newObject);
        });

        return column;
    }

    private TableColumn<ChartData, String> buildNameColumn() {
        TableColumn<ChartData, String> column = new TableColumn<>(I18n.getInstance().getString("graph.table.name"));
        column.setMinWidth(180);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("name"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setEditable(true);
        column.setOnEditCommit(chartDataStringCellEditEvent -> {
            (chartDataStringCellEditEvent.getTableView().getItems().get(chartDataStringCellEditEvent.getTablePosition().getRow()))
                    .setName(chartDataStringCellEditEvent.getNewValue());
        });

        return column;
    }

    private TableColumn<ChartData, Long> buildProcessorObjectColumn() {
        TableColumn<ChartData, Long> column = new TableColumn<>(I18n.getInstance().getString("graph.table.cleaning"));
        column.setMinWidth(250);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("id"));
        column.setCellFactory(ProcessorTableCell.forTableColumn(ds));
        column.setEditable(true);
        column.setOnEditCommit(chartDataJEVisObjectCellEditEvent -> {
            ChartData chartData = chartDataJEVisObjectCellEditEvent.getTableView().getItems().get(chartDataJEVisObjectCellEditEvent.getTablePosition().getRow());

            try {
                JEVisObject newObject = ds.getObject(chartDataJEVisObjectCellEditEvent.getNewValue());

                chartData.setId(newObject.getID());
                chartData.setObjectName(newObject);
//                refresh();
            } catch (Exception e) {

            }
        });

        return column;
    }

    private TableColumn<ChartData, ChartType> buildChartTypeColumn() {
        TableColumn<ChartData, ChartType> column = new TableColumn<>(I18n.getInstance().getString("graph.table.charttype"));
        column.setMinWidth(130);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("chartType"));
        column.setCellFactory(ChartTypeTableCell.forTableColumn());
        column.setEditable(true);
        return column;
    }

    private TableColumn<ChartData, Color> buildColorColumn() {
        TableColumn<ChartData, Color> column = new TableColumn<>(I18n.getInstance().getString("graph.table.color"));
        column.setMinWidth(80);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("color"));
        column.setCellFactory(ColorTableCell.forTableColumn());
        column.setEditable(true);
        return column;
    }

    private TableColumn<ChartData, JEVisUnit> buildUnitColumn() {
        TableColumn<ChartData, JEVisUnit> column = new TableColumn<>(I18n.getInstance().getString("graph.table.unit"));
        column.setMinWidth(130);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("unit"));
        column.setCellFactory(UnitTableCell.forTableColumn());
        column.setEditable(true);
        column.setOnEditCommit(chartDataJEVisObjectCellEditEvent -> {
            ChartData chartData = chartDataJEVisObjectCellEditEvent.getTableView().getItems().get(chartDataJEVisObjectCellEditEvent.getTablePosition().getRow());

            JEVisUnit newUnit = chartDataJEVisObjectCellEditEvent.getNewValue();
            chartData.setUnit(newUnit);
        });

        return column;
    }

    private TableColumn<ChartData, Boolean> buildIntervalColumn() {
        TableColumn<ChartData, Boolean> column = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.date"));
        column.setMinWidth(50);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("intervalEnabled"));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setEditable(true);
        column.setOnEditCommit(chartDataBooleanCellEditEvent -> {
            chartDataBooleanCellEditEvent.getTableView().getItems().get(chartDataBooleanCellEditEvent.getTablePosition().getRow())
                    .setIntervalEnabled(chartDataBooleanCellEditEvent.getNewValue());

            checkForCustomIntervals();
        });
        return column;
    }

    private void checkForCustomIntervals() {
        boolean hasCustomIntervalEnabled = chartModel.getChartData().stream().anyMatch(ChartData::isIntervalEnabled);
        Platform.runLater(() -> {
            intervalStartColumn.setVisible(hasCustomIntervalEnabled);
            intervalEndColumn.setVisible(hasCustomIntervalEnabled);
        });
    }

    private TableColumn<ChartData, String> buildIntervalStartColumn() {
        TableColumn<ChartData, String> column = new TableColumn<>(I18n.getInstance().getString("graph.table.startdate"));
        column.setMinWidth(80);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("intervalStart"));
        column.setCellFactory(IntervalTableCell.forTableColumn());
        column.setEditable(true);
        column.setVisible(false);
        return column;
    }

    private TableColumn<ChartData, String> buildIntervalEndColumn() {
        TableColumn<ChartData, String> column = new TableColumn<>(I18n.getInstance().getString("graph.table.enddate"));
        column.setMinWidth(80);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("intervalEnd"));
        column.setCellFactory(IntervalTableCell.forTableColumn());
        column.setEditable(true);
        column.setVisible(false);
        return column;
    }

    private TableColumn<ChartData, Integer> buildAxisColumn() {
        TableColumn<ChartData, Integer> column = new TableColumn<>(I18n.getInstance().getString("graph.table.axis"));
        column.setMinWidth(80);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("axis"));
        column.setCellFactory(AxisTableCell.forTableColumn());
        column.setEditable(true);
        return column;
    }

    private TableColumn<ChartData, BubbleType> buildBubbleTypeColumn() {
        TableColumn<ChartData, BubbleType> column = new TableColumn<>(I18n.getInstance().getString("plugin.graph.charttype.bubble.name"));
        column.setMinWidth(130);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("bubbleType"));
        column.setCellFactory(BubbleTypeTableCell.forTableColumn());
        column.setEditable(true);
        return column;
    }

    private TableColumn<ChartData, AggregationPeriod> buildAggregationPeriodColumn() {
        TableColumn<ChartData, AggregationPeriod> column = new TableColumn<>(I18n.getInstance().getString("plugin.object.report.dialog.header.aggregation"));
        column.setMinWidth(130);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("aggregationPeriod"));
        column.setCellFactory(AggregationPeriodTableCell.forTableColumn());
        column.setEditable(true);
        return column;
    }

    private TableColumn<ChartData, ManipulationMode> buildManipulationModeColumn() {
        TableColumn<ChartData, ManipulationMode> column = new TableColumn<>(I18n.getInstance().getString("plugin.object.report.dialog.header.manipulation"));
        column.setMinWidth(130);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("manipulationMode"));
        column.setCellFactory(ManipulationModeTableCell.forTableColumn());
        column.setEditable(true);
        return column;
    }

    private TableColumn<ChartData, Boolean> buildMathColumn() {
        TableColumn<ChartData, Boolean> column = new TableColumn<>(I18n.getInstance().getString("plugin.dashboard.datatree.math"));
        column.setMinWidth(120);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("calculation"));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setEditable(true);
        column.setOnEditCommit(chartDataBooleanCellEditEvent ->
                chartDataBooleanCellEditEvent.getTableView().getItems().get(chartDataBooleanCellEditEvent.getTablePosition().getRow())
                        .setCalculation(chartDataBooleanCellEditEvent.getNewValue()));

        return column;
    }

    private TableColumn<ChartData, String> buildCssColumn() {
        TableColumn<ChartData, String> column = new TableColumn<>(I18n.getInstance().getString("graph.table.css"));
        column.setMinWidth(250);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("css"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setEditable(true);
        column.setOnEditCommit(chartDataStringCellEditEvent -> {
            (chartDataStringCellEditEvent.getTableView().getItems().get(chartDataStringCellEditEvent.getTablePosition().getRow()))
                    .setCss(chartDataStringCellEditEvent.getNewValue());
        });

        return column;
    }

    public TableColumn<ChartData, JEVisObject> getObjectNameColumn() {
        return objectNameColumn;
    }

    public TableColumn<ChartData, String> getNameColumn() {
        return nameColumn;
    }

    public TableColumn<ChartData, Long> getProcessorObjectColumn() {
        return processorObjectColumn;
    }

    public TableColumn<ChartData, ChartType> getChartTypeColumn() {
        return chartTypeColumn;
    }

    public TableColumn<ChartData, Color> getColorColumn() {
        return colorColumn;
    }

    public TableColumn<ChartData, JEVisUnit> getUnitColumn() {
        return unitColumn;
    }

    public TableColumn<ChartData, Boolean> getIntervalEnabledColumn() {
        return intervalEnabledColumn;
    }

    public TableColumn<ChartData, String> getIntervalStartColumn() {
        return intervalStartColumn;
    }

    public TableColumn<ChartData, String> getIntervalEndColumn() {
        return intervalEndColumn;
    }

    public TableColumn<ChartData, AggregationPeriod> getAggregationPeriodColumn() {
        return aggregationPeriodColumn;
    }

    public TableColumn<ChartData, ManipulationMode> getManipulationModeColumn() {
        return manipulationModeColumn;
    }

    public TableColumn<ChartData, Integer> getAxisColumn() {
        return axisColumn;
    }

    public TableColumn<ChartData, BubbleType> getBubbleTypeColumn() {
        return bubbleTypeColumn;
    }

    public TableColumn<ChartData, Boolean> getMathColumn() {
        return mathColumn;
    }

    public TableColumn<ChartData, String> getCssColumn() {
        return cssColumn;
    }
}
