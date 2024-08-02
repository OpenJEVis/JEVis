package org.jevis.jeconfig.plugin.loadprofile.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.loadprofile.data.MixedLoadProfile;

public class ValueFieldTableCell extends TableCell<MixedLoadProfile, Object> {

    private ValueField valueField;


    public ValueFieldTableCell() {
        super();
    }

    public static Callback<TableColumn<MixedLoadProfile, Object>, TableCell<MixedLoadProfile, Object>> forTableColumn() {
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
            if (valueField == null) {
                valueField = ValueField.createComboBox(this);
            }

            ValueField.startEdit(this, null, null, valueField);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        ValueField.cancelEdit(this, null);
    }

    @Override
    public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        ValueField.updateItem(this, null, null, valueField);
    }

}
