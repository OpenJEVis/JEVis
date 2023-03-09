package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AggregationPeriodBox;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class AggregationPeriodTableCell extends TableCell<ChartData, AggregationPeriod> {

    private AggregationPeriodBox aggregationPeriodBox;


    public AggregationPeriodTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, AggregationPeriod>, TableCell<ChartData, AggregationPeriod>> forTableColumn() {
        return list -> new AggregationPeriodTableCell();
    }

    @Override
    public void startEdit() {
        if (!isEditable()
                || !getTableView().isEditable()
                || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            if (aggregationPeriodBox == null) {
                aggregationPeriodBox = AggregationPeriodBox.createComboBox(this);
            }

            AggregationPeriodBox.startEdit(this, null, null, aggregationPeriodBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        AggregationPeriodBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(AggregationPeriod item, boolean empty) {
        super.updateItem(item, empty);
        AggregationPeriodBox.updateItem(this, null, null, aggregationPeriodBox);
    }

}
