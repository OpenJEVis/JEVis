package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable.ValueWithDateTimeFieldTableCell;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.Chart;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.Chart.data.ValueWithDateTime;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.application.tools.TableViewUtils;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.jevis.jeconfig.plugin.charts.TableViewContextMenuHelper;

import java.util.Collections;
import java.util.List;

public class TableHeader extends TableView<TableEntry> {
    private final double VALUE_COLUMNS_PREF_SIZE = 200;
    private final double VALUE_COLUMNS_MIN_SIZE = VALUE_COLUMNS_PREF_SIZE - 60;
    private final TableColumn<TableEntry, String> nameCol;
    private final TableColumn<TableEntry, Color> colorCol;
    private final ChartModel chartModel;
    private final AlphanumComparator alphanumComparator = new AlphanumComparator();
    private TableColumn<TableEntry, String> periodCol;
    private TableColumn<TableEntry, String> dateCol;
    private TableColumn<TableEntry, String> noteCol;
    private TableViewContextMenuHelper contextMenuHelper;
    private List<XYChartSerie> xyChartSerieList;

    public TableHeader(ChartModel chartModel, Chart chart) {
        this(chartModel, chart.getTableData());

        xyChartSerieList = chart.getXyChartSerieList();
    }

    public TableHeader(ChartModel chartModel, final ObservableList<TableEntry> tableData) {
        this.chartModel = chartModel;

        setBorder(null);
        setStyle(
                ".table-view:focused {" +
                        "-fx-padding: 0; " +
                        "-fx-background-color: transparent, -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: -1.4,0,1;" +
                        "}");
        if (chartModel.getChartType() != ChartType.TABLE) {
            getStylesheets().add(TableHeader.class.getResource("/styles/TableViewNoScrollbar.css").toExternalForm());
            setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        } else {
            setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        tableData.sort((o1, o2) -> getAlphanumComparator().compare(o1.getName(), o2.getName()));


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

        nameCol.setCellFactory(new Callback<TableColumn<TableEntry, String>, TableCell<TableEntry, String>>() {
            @Override
            public TableCell<TableEntry, String> call(TableColumn<TableEntry, String> param) {
                return new TableCell<TableEntry, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        super.setText(item);
                        super.setGraphic(null);

                        this.setTooltip(new Tooltip(item));
                    }
                };
            }
        });
        nameCol.setSortable(true);
        nameCol.setPrefWidth(500);
        nameCol.setMinWidth(100);

        setItems(tableData);

        switch (chartModel.getChartType()) {
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

                getColumns().addAll(colorCol, nameCol, xValue, yValue, standardDeviation, variance);
                break;
            default:
                /**
                 * Table Column 2
                 */
                periodCol = new TableColumn<>();
                periodCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("period"));
                periodCol.setStyle("-fx-alignment: CENTER-RIGHT");
                periodCol.setSortable(false);
                periodCol.setPrefWidth(100);
                periodCol.setMinWidth(100);

                Label periodLabel = new Label(I18n.getInstance().getString("plugin.graph.table.period"));
                periodLabel.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.table.period.tip")));
                periodCol.setGraphic(periodLabel);
                JEVisHelp.getInstance().addInfoControl(ChartPlugin.class.getSimpleName(), "", JEVisHelp.LAYOUT.HORIZONTAL_TOP_LEFT, periodLabel);

                /**
                 * Table Column 3
                 */
                TableColumn<TableEntry, String> value = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.value"));
                value.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("value"));
                value.setStyle("-fx-alignment: CENTER-RIGHT");
                value.setSortable(true);

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
                TableColumn<TableEntry, ValueWithDateTime> minCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.min"));
                minCol.setCellValueFactory(new PropertyValueFactory<TableEntry, ValueWithDateTime>("min"));
                minCol.setCellFactory(ValueWithDateTimeFieldTableCell.forTableColumn());
                minCol.setStyle("-fx-alignment: CENTER-RIGHT");
                minCol.setSortable(true);
                minCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                minCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 7
                 */
                TableColumn<TableEntry, ValueWithDateTime> maxCol = new TableColumn<TableEntry, ValueWithDateTime>(I18n.getInstance().getString("plugin.graph.table.max"));
                maxCol.setCellValueFactory(new PropertyValueFactory<TableEntry, ValueWithDateTime>("max"));
                maxCol.setCellFactory(ValueWithDateTimeFieldTableCell.forTableColumn());
                maxCol.setStyle("-fx-alignment: CENTER-RIGHT");
                maxCol.setSortable(true);
                maxCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                maxCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 8
                 */
                TableColumn<TableEntry, String> avgCol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.avg"));
                avgCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("avg"));
                avgCol.setStyle("-fx-alignment: CENTER-RIGHT");
                avgCol.setSortable(true);
                avgCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                avgCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                /**
                 * Table Column 9
                 */
                TableColumn<TableEntry, String> enPICol = new TableColumn<TableEntry, String>(I18n.getInstance().getString("plugin.graph.table.enpi"));
                enPICol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("enpi"));
                enPICol.setStyle("-fx-alignment: CENTER-RIGHT");
                enPICol.setSortable(true);
                enPICol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                enPICol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);
                enPICol.setVisible(false);

                /**
                 * Table Column 10
                 */
                TableColumn<TableEntry, String> sumCol = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.sum"));
                sumCol.setCellValueFactory(new PropertyValueFactory<TableEntry, String>("sum"));
                sumCol.setStyle("-fx-alignment: CENTER-RIGHT");
                sumCol.setSortable(true);
                sumCol.setPrefWidth(VALUE_COLUMNS_PREF_SIZE);
                sumCol.setMinWidth(VALUE_COLUMNS_MIN_SIZE);

                getColumns().addAll(colorCol, nameCol, periodCol, value, dateCol, noteCol, minCol, maxCol, avgCol, enPICol, sumCol);
                setTableMenuButtonVisible(true);
                contextMenuHelper = new TableViewContextMenuHelper(this);
                if (contextMenuHelper.getTableHeaderRow() != null) {
                    contextMenuHelper.getTableHeaderRow().setOnMouseClicked(event -> {
                        if (event.getButton() == MouseButton.SECONDARY) {
                            contextMenuHelper.showContextMenu();
                        }
                    });
                }

                widthProperty().addListener((observable, oldValue, newValue) -> {
                    Platform.runLater(this::updateColumnCaptionWidths);
                });

                enPICol.visibleProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != oldValue) {
                        Platform.runLater(this::updateColumnCaptionWidths);
                    }
                });
                break;
        }

        setColumns();
    }

    private void setColumns() {
        switch (chartModel.getChartType()) {
            case LOGICAL:
                setTableStandard();
                getColumns().get(2).setVisible(false);
                getColumns().get(5).setVisible(false);
                getColumns().get(6).setVisible(false);
                getColumns().get(7).setVisible(false);
                getColumns().get(8).setVisible(false);
                getColumns().get(9).setVisible(false);
                getColumns().get(10).setVisible(false);
                break;
            case BAR:
                setTableStandard();
                getColumns().get(4).setVisible(false);
                getColumns().get(5).setVisible(false);
                getColumns().get(6).setVisible(false);
                getColumns().get(7).setVisible(false);
                getColumns().get(8).setVisible(false);
                getColumns().get(9).setVisible(false);
                getColumns().get(10).setVisible(false);
                break;
            case PIE:
            case HEAT_MAP:
                disableTable();
                break;
            case TABLE:
                setTableStandard();
                getColumns().get(0).setVisible(false);
                getColumns().get(1).setVisible(chartModel.getOrientation() == Orientation.HORIZONTAL);
                getColumns().get(2).setVisible(false);
                getColumns().get(5).setVisible(false);
                getColumns().get(6).setVisible(false);
                getColumns().get(7).setVisible(false);
                getColumns().get(8).setVisible(false);
                getColumns().get(9).setVisible(false);
                getColumns().get(10).setVisible(false);
                break;
            case AREA:
            case LINE:
            case COLUMN:
            case BUBBLE:
            case SCATTER:
            default:
                setTableStandard();
                break;
        }
    }

    private void disableTable() {
        setVisible(false);
        setFixedCellSize(25);
        prefHeightProperty().unbind();
        setPrefHeight(0);
    }

    private void setTableStandard() {
        setVisible(true);
        setFixedCellSize(25);
        prefHeightProperty().bind(Bindings.size(getItems()).multiply(getFixedCellSize()).add(30));
    }

    public void updateColumnCaptionWidths() {
        TableViewUtils.allToMin(this);
        TableViewUtils.growColumns(this, Collections.singletonList(nameCol));
    }

    private TableColumn<TableEntry, Color> buildColorColumn() {
//        TableColumn<TableEntry, Color> column = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.color"));
        TableColumn<TableEntry, Color> column = new TableColumn<>();

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
                            stackPane.setBackground(new Background(new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));

                            stackPane.setOnMouseClicked(mouseEvent -> {
                                if (mouseEvent.getClickCount() == 2 && xyChartSerieList != null) {
                                    TableEntry tableEntry = (TableEntry) getTableRow().getItem();
                                    if (!xyChartSerieList.isEmpty()) {
                                        XYChartSerie serie = xyChartSerieList.stream().filter(xyChartSerie -> xyChartSerie.getTableEntry().equals(tableEntry)).findFirst().orElse(null);

                                        if (serie != null) {
                                            boolean isShown = !serie.isShownInRenderer();
                                            serie.setShownInRenderer(isShown);

                                            if (!isShown) {
                                                stackPane.setBackground(new Background(new BackgroundFill(
                                                        item.deriveColor(item.getRed(), item.getBlue(), item.getGreen(), 0.4)
                                                        , CornerRadii.EMPTY, Insets.EMPTY)));
                                            } else {
                                                stackPane.setBackground(new Background(new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));
                                            }
                                        }
                                    }
                                }
                            });

                            setText(null);
                            setGraphic(stackPane);
                        }
                    }
                };
            }
        });

        return column;

    }

    public AlphanumComparator getAlphanumComparator() {
        return alphanumComparator;
    }

    public ChartType getChartType() {
        return chartModel.getChartType();
    }

    public Integer getChartId() {
        return chartModel.getChartId();
    }

    public void setXyChartSerieList(List<XYChartSerie> xyChartSerieList) {
        this.xyChartSerieList = xyChartSerieList;
    }
}
