package org.jevis.jeconfig.plugin.action.ui.control;

import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.action.data.ActionData;

public class ShortColumnCell implements Callback<TableColumn<ActionData, String>, TableCell<ActionData, String>> {

    Label label = new Label();

    @Override
    public TableCell<ActionData, String> call(TableColumn<ActionData, String> param) {
        return new TableCell<ActionData, String>() {
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
