package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

public class ShortColumnCell implements Callback<TableColumn<NonconformityData, String>, TableCell<NonconformityData, String>> {

    Label label = new Label();

    @Override
    public TableCell<NonconformityData, String> call(TableColumn<NonconformityData, String> param) {
        return new TableCell<NonconformityData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    label.setTextOverrun(OverrunStyle.ELLIPSIS);
                    setGraphic(label);
                }
            }
        };
    }
}
