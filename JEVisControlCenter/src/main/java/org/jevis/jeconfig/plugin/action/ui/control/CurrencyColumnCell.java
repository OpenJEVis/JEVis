package org.jevis.jeconfig.plugin.action.ui.control;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.action.data.ActionData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyColumnCell implements Callback<TableColumn<ActionData, Double>, TableCell<ActionData, Double>> {

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    {
        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
    }

    ;

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
