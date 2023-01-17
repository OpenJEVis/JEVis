package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.action.data.ActionData;

public class DoubleColumnCell implements Callback<TableColumn<ActionData, String>, TableCell<ActionData, String>> {

    @Override
    public TableCell<ActionData, String> call(TableColumn<ActionData, String> param) {
        return new TableCell<ActionData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    try {
                        UnitDoubleConverter unitDoubleConverter = new UnitDoubleConverter();
                        setText(unitDoubleConverter.toString(unitDoubleConverter.fromString(item)) + " €");
                    } catch (Exception ex) {
                    }
                } else {
                    setText(null);
                }
            }
        };
    }
}
