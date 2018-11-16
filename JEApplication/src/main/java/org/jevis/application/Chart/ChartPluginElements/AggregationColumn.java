package org.jevis.application.Chart.ChartPluginElements;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.Chart.data.GraphDataModel;
import org.jevis.application.jevistree.JEVisTree;
import org.jevis.application.jevistree.JEVisTreeRow;
import org.jevis.commons.dataprocessing.AggregationPeriod;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class AggregationColumn extends TreeTableColumn<JEVisTreeRow, AggregationPeriod> implements ChartPluginColumn {
    private TreeTableColumn<JEVisTreeRow, AggregationPeriod> aggregationColumn;
    private GraphDataModel data;
    private JEVisTree tree;
    private String columnName;

    public AggregationColumn(JEVisTree tree, String columnName) {
        this.tree = tree;
        this.columnName = columnName;
    }


    public TreeTableColumn<JEVisTreeRow, AggregationPeriod> getAggregationColumn() {
        return aggregationColumn;
    }

    @Override
    public void setGraphDataModel(GraphDataModel graphDataModel) {
        this.data = graphDataModel;

        update();
    }

    @Override
    public void buildColumn() {
        TreeTableColumn<JEVisTreeRow, AggregationPeriod> column = new TreeTableColumn(columnName);
        column.setPrefWidth(100);
        column.setCellValueFactory(param -> {

            ChartDataModel data = getData(param.getValue().getValue());

            return new ReadOnlyObjectWrapper<>(data.getAggregationPeriod());
        });

        column.setCellFactory(new Callback<TreeTableColumn<JEVisTreeRow, AggregationPeriod>, TreeTableCell<JEVisTreeRow, AggregationPeriod>>() {

            @Override
            public TreeTableCell<JEVisTreeRow, AggregationPeriod> call(TreeTableColumn<JEVisTreeRow, AggregationPeriod> param) {

                TreeTableCell<JEVisTreeRow, AggregationPeriod> cell = new TreeTableCell<JEVisTreeRow, AggregationPeriod>() {

                    @Override
                    public void commitEdit(AggregationPeriod newValue) {
                        super.commitEdit(newValue);
                    }

                    @Override
                    protected void updateItem(AggregationPeriod item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            StackPane hbox = new StackPane();

                            if (getTreeTableRow().getItem() != null && tree != null && tree.getFilter().showColumn(getTreeTableRow().getItem(), columnName)) {
                                ChartDataModel data = getData(getTreeTableRow().getItem());

                                AggregationBox aggBox = new AggregationBox(data);

                                hbox.getChildren().setAll(aggBox.getAggregationBox());
                                StackPane.setAlignment(aggBox.getAggregationBox(), Pos.CENTER_LEFT);

                                aggBox.getAggregationBox().setDisable(!data.isSelectable());

                            }

                            setText(null);
                            setGraphic(hbox);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }

                    }

                };

                return cell;
            }
        });

        this.aggregationColumn = column;
    }

    @Override
    public GraphDataModel getData() {
        return this.data;
    }
}
