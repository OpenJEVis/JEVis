package org.jevis.jeconfig.plugin.action.ui.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.action.data.ActionData;
import org.jevis.jeconfig.plugin.action.ui.DoubleConverter;

import java.text.NumberFormat;

public class CurrencyColumnCell implements Callback<TableColumn<ActionData, Double>, TableCell<ActionData, Double>> {

    NumberFormat currencyFormat = DoubleConverter.getInstance().getCurrencyFormat();

    @Override
    public TableCell<ActionData, Double> call(TableColumn<ActionData, Double> param) {
        return new TableCell<ActionData, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    if (item.equals(-0d)) {
                        setText(currencyFormat.format(0d));
                    } else {
                        setText(currencyFormat.format(item));
                    }

                } else {
                    setText(null);
                }
            }
        };
    }
}
