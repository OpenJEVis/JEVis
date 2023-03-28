package org.jevis.jeconfig.plugin.action.ui;


import javafx.util.converter.NumberStringConverter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class NumerFormating {

    private static NumerFormating formatterFactory = null;
    private NumberStringConverter nsc;
    private NumberStringConverter nscNoUnit;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance();

    public NumerFormating() {

        currencyFormat = NumberFormat.getCurrencyInstance();
        numberFormat = NumberFormat.getNumberInstance();
        currencyFormat.setCurrency(Currency.getInstance(Locale.GERMANY));
        NumberFormat doubleFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        doubleFormat.setGroupingUsed(true);

        //doubleFormat.setMinimumFractionDigits(-1);
        doubleFormat.setMaximumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(0);
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
                //System.out.println("toString: " + value + "  f: " + doubleFormat.format(value));
                return doubleFormat.format(value);
            }


            @Override
            public Number fromString(String value) {
                try {
                    //System.out.println("fromString: " + value + "  f: " + doubleFormat.parse(value));
                    return doubleFormat.parse(value);
                } catch (Exception ex) {
                    return 0d;
                }

            }


        };
    }

    public static synchronized NumerFormating getInstance() {
        if (formatterFactory == null) {
            formatterFactory = new NumerFormating();
        }

        return formatterFactory;
    }

    public NumberFormat getCurrencyFormat() {
        return currencyFormat;
    }

    public NumberFormat getDoubleFormate() {
        return numberFormat;
    }

    public NumberStringConverter getCurrencyConverter() {
        return nsc;
    }

    public NumberStringConverter getDoubleConverter() {
        return nscNoUnit;
    }
}
