package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.ChartTypeComboBox;
import org.jevis.jecc.application.Chart.ChartType;
import org.jevis.jecc.application.Chart.data.ChartData;

public class ChartTypeTableCell extends TableCell<ChartData, ChartType> {

    private ChartTypeComboBox chartTypeComboBox;


    public ChartTypeTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, ChartType>, TableCell<ChartData, ChartType>> forTableColumn() {
        return list -> new ChartTypeTableCell();
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
            if (chartTypeComboBox == null) {
                chartTypeComboBox = ChartTypeComboBox.createComboBox(this);
            }

            ChartTypeComboBox.startEdit(this, null, null, chartTypeComboBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ChartTypeComboBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(ChartType item, boolean empty) {
        super.updateItem(item, empty);
        ChartTypeComboBox.updateItem(this, null, null, chartTypeComboBox);
    }

}
