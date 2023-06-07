package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.api.JEVisSample;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.ValueBox;
import org.jevis.jecc.application.Chart.data.RowNote;

public class ValueFieldTableCellForNotes extends TableCell<RowNote, JEVisSample> {

    private ValueBox valueBox;


    public ValueFieldTableCellForNotes() {
        super();
    }

    public static Callback<TableColumn<RowNote, JEVisSample>, TableCell<RowNote, JEVisSample>> forTableColumn() {
        return list -> new ValueFieldTableCellForNotes();
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
            if (valueBox == null) {
                valueBox = ValueBox.createComboBox(this);
            }

            ValueBox.startEdit(this, null, null, valueBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ValueBox.cancelEdit(this, null);
    }

    @Override
    public void updateItem(JEVisSample item, boolean empty) {
        super.updateItem(item, empty);
        ValueBox.updateItem(this, null, null, valueBox);
    }

}
