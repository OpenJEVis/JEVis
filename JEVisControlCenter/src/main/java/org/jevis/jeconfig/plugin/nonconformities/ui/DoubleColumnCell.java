package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

public class DoubleColumnCell implements Callback<TableColumn<NonconformityData, String>, TableCell<NonconformityData, String>> {

    @Override
    public TableCell<NonconformityData, String> call(TableColumn<NonconformityData, String> param) {
        return new TableCell<NonconformityData, String>() {
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
