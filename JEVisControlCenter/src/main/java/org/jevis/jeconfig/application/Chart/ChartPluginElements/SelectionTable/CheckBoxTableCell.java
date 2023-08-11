package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.CheckBox;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class CheckBoxTableCell extends TableCell<ChartData, Boolean> {
    private CheckBox checkBox;


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
            if (checkBox == null) {
                checkBox = CheckBox.createComboBox(this);
            }

            CheckBox.startEdit(this, null, null, checkBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        CheckBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        CheckBox.updateItem(this, null, null, checkBox);
    }
}
