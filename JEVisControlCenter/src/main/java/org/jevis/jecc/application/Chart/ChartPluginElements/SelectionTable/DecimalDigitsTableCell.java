package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jecc.application.Chart.data.ChartData;
import org.jevis.jecc.tool.NumberSpinner;

public class DecimalDigitsTableCell extends TableCell<ChartData, Integer> {

    private NumberSpinner numberSpinner;

    public DecimalDigitsTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, Integer>, TableCell<ChartData, Integer>> forTableColumn() {
        return list -> new DecimalDigitsTableCell();
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
            if (numberSpinner == null) {
                numberSpinner = NumberSpinner.createNumberSpinner(this);
            }

            NumberSpinner.startEdit(this, null, null, numberSpinner);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        NumberSpinner.cancelEdit(this, null);
    }

    @Override
    public void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        NumberSpinner.updateItem(this, null, null, numberSpinner);
    }

}
