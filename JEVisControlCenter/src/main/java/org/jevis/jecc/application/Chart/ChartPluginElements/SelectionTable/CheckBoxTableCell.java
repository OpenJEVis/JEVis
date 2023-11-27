package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.CheckBoxForTable;
import org.jevis.jecc.application.Chart.data.ChartData;

public class CheckBoxTableCell extends TableCell<ChartData, Boolean> {
    private CheckBoxForTable checkBoxForTable;


    public CheckBoxTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, Boolean>, TableCell<ChartData, Boolean>> forTableColumn() {
        return list -> new CheckBoxTableCell();

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
            if (checkBoxForTable == null) {
                checkBoxForTable = CheckBoxForTable.createComboBox(this);
            }

            CheckBoxForTable.startEdit(this, null, null, checkBoxForTable);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        CheckBoxForTable.cancelEdit(this, null);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        CheckBoxForTable.updateItem(this, null, null, checkBoxForTable);
    }
}
