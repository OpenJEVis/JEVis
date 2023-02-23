package org.jevis.jeconfig.plugin.action.ui;


import javafx.util.converter.NumberStringConverter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class DoubleConverter {

    private static DoubleConverter formatterFactory = null;
    private NumberStringConverter nsc;
    private NumberStringConverter nscNoUnit;

    public DoubleConverter() {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(2);
        doubleFormat.setMaximumFractionDigits(2);
        doubleFormat.setGroupingUsed(true);

        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));

        nsc = new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return currencyFormat.format(value);
            }
        };

        nscNoUnit = new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                return doubleFormat.format(value);
            }
        };
    }

    public static synchronized DoubleConverter getInstance() {
        if (formatterFactory == null) {
            formatterFactory = new DoubleConverter();
        }

        return formatterFactory;
    }

    public NumberStringConverter getCurrencyConverter() {
        return nsc;
    }

    public NumberStringConverter getDoubleConverter() {
        return nscNoUnit;
    }
}
