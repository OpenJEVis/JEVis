package org.jevis.jeconfig.application.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class StringListColumnCell<T> implements Callback<TableColumn<T, String>, TableCell<T, String>> {

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    {
        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
    }

    ;

    @Override
    public TableCell<T, String> call(TableColumn<T, String> param) {
        return new TableCell<T, String>() {
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
