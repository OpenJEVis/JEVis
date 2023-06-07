package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.api.JEVisObject;
import org.jevis.jecc.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jecc.application.Chart.data.ChartData;

public class SelectionTableCell extends TableCell<ChartData, JEVisObject> {

    private TreeSelectionDialog selectionDialog;


    public SelectionTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, JEVisObject>, TableCell<ChartData, JEVisObject>> forTableColumn() {
        return list -> new SelectionTableCell();
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
            if (selectionDialog == null) {
                selectionDialog = TreeSelectionDialog.createSelectionDialog(this);
            }

            TreeSelectionDialog.startEdit(this, null, selectionDialog);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        TreeSelectionDialog.cancelEdit(this, null);
    }

    @Override
    public void updateItem(JEVisObject item, boolean empty) {
        super.updateItem(item, empty);
        TreeSelectionDialog.updateItem(this, null, selectionDialog);
    }

}
