package org.jevis.jeconfig.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.application.Chart.ChartElements.TableEntry;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.ValueWithDateTimeBox;
import org.jevis.jeconfig.application.Chart.data.ValueWithDateTime;

public class ValueWithDateTimeFieldTableCell extends TableCell<TableEntry, ValueWithDateTime> {

    private ValueWithDateTimeBox valueWithDateTimeBox;


    public ValueWithDateTimeFieldTableCell() {
        super();
    }

    public static Callback<TableColumn<TableEntry, ValueWithDateTime>, TableCell<TableEntry, ValueWithDateTime>> forTableColumn() {
        return list -> new ValueWithDateTimeFieldTableCell();
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
            if (valueWithDateTimeBox == null) {
                valueWithDateTimeBox = ValueWithDateTimeBox.createComboBox(this);
            }

            ValueWithDateTimeBox.startEdit(this, null, null, valueWithDateTimeBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ValueWithDateTimeBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(ValueWithDateTime item, boolean empty) {
        super.updateItem(item, empty);
        ValueWithDateTimeBox.updateItem(this, null, null, valueWithDateTimeBox);
    }

}
