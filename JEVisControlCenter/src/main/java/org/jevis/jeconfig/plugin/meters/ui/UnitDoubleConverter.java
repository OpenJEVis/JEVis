package org.jevis.jeconfig.plugin.meters.ui;

import javafx.util.converter.DoubleStringConverter;
import org.jevis.commons.i18n.I18n;

import java.text.NumberFormat;
import java.text.ParseException;

public class UnitDoubleConverter extends DoubleStringConverter {

    NumberFormat nf = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());

    @Override
    public Double fromString(String value) {

        Double dvalue = null;
        try {
            dvalue = NumberFormat.getNumberInstance(I18n.getInstance().getLocale()).parse(value).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dvalue;
    }

    @Override
    public String toString(Double value) {
        if (value == null) {
            return "";
        }
        nf.setMinimumFractionDigits(2);

        return nf.format(value);
    }


}
