package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.IntervalBox;
import org.jevis.jeconfig.application.Chart.data.ChartData;

public class IntervalTableCell extends TableCell<ChartData, String> {

    private IntervalBox intervalBox;


    public IntervalTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, String>, TableCell<ChartData, String>> forTableColumn() {
        return list -> new IntervalTableCell();
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
            if (intervalBox == null) {
                intervalBox = IntervalBox.createComboBox(this);
            }

            IntervalBox.startEdit(this, null, null, intervalBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        IntervalBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        IntervalBox.updateItem(this, null, null, intervalBox);
    }

}
