package org.jevis.jeconfig.application.Chart.ChartPluginElements.Columns;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationBox;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.dialog.ChartSelectionDialog;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class AggregationColumn extends TreeTableColumn<JEVisTreeRow, AggregationPeriod> implements ChartPluginColumn {
    public static String COLUMN_ID = "AggregationColumn";
    private TreeTableColumn<JEVisTreeRow, AggregationPeriod> aggregationColumn;
    private AnalysisDataModel data;
    private JEVisTree tree;
    private String columnName;
    private final JEVisDataSource dataSource;

    public AggregationColumn(JEVisTree tree, JEVisDataSource dataSource, String columnName) {
        this.tree = tree;
        this.dataSource = dataSource;
        this.columnName = columnName;
    }


    public TreeTableColumn<JEVisTreeRow, AggregationPeriod> getAggregationColumn() {
        return aggregationColumn;
    }

    @Override
    public void setGraphDataModel(AnalysisDataModel analysisDataModel) {
        this.data = analysisDataModel;

        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, AggregationPeriod> column = new TreeTableColumn();
        column.setPrefWidth(130);
        column.setMinWidth(100);
        column.setId(COLUMN_ID);

        column.setCellValueFactory(param -> {

            ChartDataRow data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getAggregationPeriod());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, AggregationPeriod>, TreeTableCell<JEVisTreeRow, AggregationPeriod>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, AggregationPeriod> call(TreeTableColumn<JEVisTreeRow, AggregationPeriod> param) {

                TreeTableCell<JEVisTreeRow, AggregationPeriod> cell = new TreeTableCell<JEVisTreeRow, AggregationPeriod>() {

                    @Override
                    public void commitEdit(AggregationPeriod newValue) {
                        super.commitEdit(newValue);

                        ChartDataRow data = getData(getTreeTableRow().getItem());

                        data.setAggregationPeriod(newValue);
                    }

                    @Override
                    protected void updateItem(AggregationPeriod item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        setGraphic(null);

                        if (!empty) {
                            try {
                                if (getTreeTableRow().getItem() != null && tree != null
                                        && tree.getFilter().showCell(column, getTreeTableRow().getItem())) {
                                    StackPane stackPane = new StackPane();

                                    ChartDataRow data = getData(getTreeTableRow().getItem());

                                    AggregationBox aggBox = new AggregationBox(data.getAggregationPeriod());

                                    ImageView imageMarkAll = new ImageView(imgMarkAll);
                                    imageMarkAll.fitHeightProperty().set(13);
                                    imageMarkAll.fitWidthProperty().set(13);

                                    Button tb = new Button("", imageMarkAll);

                                    tb.setTooltip(tooltipMarkAll);

                                    aggBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> commitEdit(newValue));

                                    tb.setOnAction(event -> {
                                        AggregationPeriod selection = aggBox.getSelectionModel().getSelectedItem();
                                        getData().getSelectedData().forEach(mdl -> {
                                            if (!mdl.getSelectedcharts().isEmpty()) {
                                                mdl.setAggregationPeriod(selection);
                                            }
                                        });

                                        tree.refresh();
                                    });

                                    HBox hbox = new HBox();
                                    hbox.getChildren().addAll(aggBox, tb);
                                    stackPane.getChildren().add(hbox);
                                    StackPane.setAlignment(stackPane, Pos.CENTER_LEFT);

                                    aggBox.setDisable(!data.isSelectable());
                                    tb.setDisable(!data.isSelectable());
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
            Label aggregationLabel = new Label(columnName);
            System.out.println("################################# Table label: " + aggregationLabel);
            aggregationLabel.setTooltip(new Tooltip(I18n.getInstance().getString("graph.table.interval.tip")));
            aggregationColumn.setGraphic(aggregationLabel);
            JEVisHelp.getInstance().addHelpControl(GraphPluginView.class.getSimpleName(), ChartSelectionDialog.class.getSimpleName(), JEVisHelp.LAYOUT.HORIZONTAL_TOP_LEFT, aggregationLabel);

        });

        this.aggregationColumn = column;
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
