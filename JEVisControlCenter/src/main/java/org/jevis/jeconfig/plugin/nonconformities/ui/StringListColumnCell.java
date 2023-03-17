package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class StringListColumnCell implements Callback<TableColumn<NonconformityData, String>, TableCell<NonconformityData, String>> {

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    {
        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
    }

    ;

    @Override
    public TableCell<NonconformityData, String> call(TableColumn<NonconformityData, String> param) {
        return new TableCell<NonconformityData, String>() {
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
