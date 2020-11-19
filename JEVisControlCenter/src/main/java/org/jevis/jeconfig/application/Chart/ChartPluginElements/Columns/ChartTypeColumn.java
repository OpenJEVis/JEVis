package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class ChartTypeColumn extends TreeTableColumn<JEVisTreeRow, ChartType> implements ChartPluginColumn {
    public static String COLUMN_ID = "ChartTypeColumn";
    private final JEVisTree tree;
    private final String columnName;
    private final JEVisDataSource dataSource;
    private TreeTableColumn<JEVisTreeRow, ChartType> chartTypeColumn;
    private AnalysisDataModel data;

    public ChartTypeColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }


    public TreeTableColumn<JEVisTreeRow, ChartType> getChartTypeColumn() {
        return chartTypeColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;

        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, ChartType> column = new TreeTableColumn();
        column.setPrefWidth(130);
        column.setMinWidth(100);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {

            ChartDataRow data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getChartType());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, ChartType>, TreeTableCell<JEVisTreeRow, ChartType>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, ChartType> call(TreeTableColumn<JEVisTreeRow, ChartType> param) {

                TreeTableCell<JEVisTreeRow, ChartType> cell = new TreeTableCell<JEVisTreeRow, ChartType>() {

                    @Override
                    public void commitEdit(ChartType newValue) {
                        super.commitEdit(newValue);

                        ChartDataRow data = getData(getTreeTableRow().getItem());

                        data.setChartType(newValue);

                        if (newValue == ChartType.BUBBLE || newValue == ChartType.PIE || newValue == ChartType.HEAT_MAP
                                || newValue == ChartType.TABLE || newValue == ChartType.BAR || newValue == ChartType.LOGICAL) {
                            getData().getCharts().getListSettings().forEach(chartSetting -> {
                                if (data.getSelectedcharts().contains(chartSetting.getId())) {
                                    chartSetting.setChartType(newValue);
                                }
                            });
                        }
                    }

                    @Override
                    protected void updateItem(ChartType item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    StackPane stackPane = new StackPane();

                                    ChartDataRow data = getData(getTreeTableRow().getItem());

                                    ChartTypeComboBox comboBoxChartType = new ChartTypeComboBox(data.getChartType());
                                    List<String> disabledItems = new ArrayList<>();
                                    comboBoxChartType.getItems().forEach(o -> {
                                        int i = comboBoxChartType.getItems().indexOf(o);
//                                        if (i == 1 || i == 3 || i == 5 || i == 7 || i == 8 || i == 9) {
                                        if (i == 3 || i == 5 || i == 7 || i == 8 || i == 9) {
                                            disabledItems.add(o);
                                        }
                                    });
                                    comboBoxChartType.setDisabledItems(disabledItems);
                                    comboBoxChartType.setDisable(true);
                                    comboBoxChartType.setPrefWidth(114);

                                    ImageView imageMarkAll = new ImageView(imgMarkAll);
                                    imageMarkAll.fitHeightProperty().set(13);
                                    imageMarkAll.fitWidthProperty().set(13);

                                    Button tb = new Button("", imageMarkAll);

                                    tb.setTooltip(tooltipMarkAll);

                                    comboBoxChartType.valueProperty().addListener((observable, oldValue, newValue) -> {
                                        if (!newValue.equals(oldValue)) {
                                            ChartType type = ChartType.parseChartType(comboBoxChartType.getSelectionModel().getSelectedIndex());
                                            commitEdit(type);
                                        }
                                    });

                                    tb.setOnAction(event -> {
                                        ChartType type = ChartType.parseChartType(comboBoxChartType.getSelectionModel().getSelectedIndex());
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (!mdl.getSelectedcharts().isEmpty()) {
                                                mdl.setChartType(type);
                                            }
                                        });

                                        tree.refresh();
                                    });

                                    HBox hbox = new HBox();
                                    hbox.getChildren().addAll(comboBoxChartType, tb);
                                    stackPane.getChildren().add(hbox);
                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    AtomicBoolean selectable = new AtomicBoolean(false);
                                    getData().getCharts().getListSettings().forEach(chartSetting -> {
                                        ChartType chartType = chartSetting.getChartType();
                                        if (data.getSelectedcharts().contains(chartSetting.getId()) &&
                                                (chartType == ChartType.LINE || chartType == ChartType.AREA
                                                        || chartType == ChartType.COLUMN || chartType == ChartType.SCATTER)
                                                || chartType == ChartType.LOGICAL) {
                                            selectable.set(true);
                                        }
                                    });
                                    comboBoxChartType.setDisable(!selectable.get());
                                    tb.setDisable(!selectable.get());
                                    setGraphic(stackPane);
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

        Platform.runLater(() -> {
            Label label = new Label(columnName);
            label.setWrapText(true);
            //label.setTooltip(new Tooltip(I18n.getInstance().getString("graph.tabs.tab.charttype.tip")));
            chartTypeColumn.setGraphic(label);
            JEVisHelp.getInstance().addHelpControl(GraphPluginView.class.getSimpleName(), ChartSelectionDialog.class.getSimpleName(), JEVisHelp.LAYOUT.HORIZONTAL_TOP_CENTERED, label);

        });

        this.chartTypeColumn = column;
    }

    @Override
    public AnalysisDataModel getData() {
        return this.data;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return dataSource;
    }

}
