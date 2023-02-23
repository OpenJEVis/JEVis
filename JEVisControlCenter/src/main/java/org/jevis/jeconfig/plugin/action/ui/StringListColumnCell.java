package org.jevis.jeconfig.plugin.action.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.action.data.ActionData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class StringListColumnCell implements Callback<TableColumn<ActionData, String>, TableCell<ActionData, String>> {

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    {
        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
    }

    ;

    @Override
    public TableCell<ActionData, String> call(TableColumn<ActionData, String> param) {
        return new TableCell<ActionData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(item.replace(";", ", "));
                } else {
                    setText(null);
                }
            }
        };
    }
}
