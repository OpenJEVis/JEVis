package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AxisBox;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class AxisTableCell extends TableCell<ChartData, Integer> {

    private AxisBox axisBox;


    public AxisTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, Integer>, TableCell<ChartData, Integer>> forTableColumn() {
        return list -> new AxisTableCell();
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
            if (axisBox == null) {
                axisBox = AxisBox.createComboBox(this);
            }

            AxisBox.startEdit(this, null, null, axisBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        AxisBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        AxisBox.updateItem(this, null, null, axisBox);
    }

}
