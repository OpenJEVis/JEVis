package org.jevis.jecc.application.Chart.ChartPluginElements.SelectionTable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.api.JEVisSample;
import org.jevis.jecc.application.Chart.ChartPluginElements.Boxes.ValueBox;
import org.jevis.jecc.application.Chart.data.ChartData;

public class ValueFieldTableCell extends TableCell<ChartData, JEVisSample> {

    private ValueBox valueBox;


    public ValueFieldTableCell() {
        super();
    }

    public static Callback<TableColumn<ChartData, JEVisSample>, TableCell<ChartData, JEVisSample>> forTableColumn() {
        return list -> new ValueFieldTableCell();
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
