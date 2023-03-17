package org.jevis.jeconfig.plugin.action.ui;


import javafx.util.converter.NumberStringConverter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class DoubleConverter {

    private static DoubleConverter formatterFactory = null;
    private NumberStringConverter nsc;
    private NumberStringConverter nscNoUnit;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public DoubleConverter() {

        currencyFormat = NumberFormat.getCurrencyInstance();
        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setGroupingUsed(true);

        //doubleFormat.setMinimumFractionDigits(-1);
        doubleFormat.setMaximumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        //currencyFormat.setMinimumFractionDigits(0);

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

    public NumberFormat getCurrencyFormat() {
        return currencyFormat;
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
