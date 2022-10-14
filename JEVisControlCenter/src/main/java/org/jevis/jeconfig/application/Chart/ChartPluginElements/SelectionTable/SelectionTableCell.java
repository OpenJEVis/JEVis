package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class SelectionTableCell extends TableCell<ChartData, JEVisObject> {

    private final StackPane dialogContainer;
    private TreeSelectionDialog selectionDialog;


    public SelectionTableCell(StackPane dialogContainer) {
        super();
        this.dialogContainer = dialogContainer;
    }

    public static Callback<TableColumn<ChartData, JEVisObject>, TableCell<ChartData, JEVisObject>> forTableColumn(StackPane dialogContainer) {
        return list -> new SelectionTableCell(dialogContainer);
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
                selectionDialog = TreeSelectionDialog.createSelectionDialog(this, dialogContainer);
            }

            TreeSelectionDialog.startEdit(this, null, null, selectionDialog);
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
        TreeSelectionDialog.updateItem(this, null, null, selectionDialog);
    }

}
