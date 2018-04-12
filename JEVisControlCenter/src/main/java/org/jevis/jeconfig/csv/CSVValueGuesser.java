/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.csv;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Simple helper to guess the type of an csv column value by tring some
 * standarts
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVValueGuesser {

    public static enum Type {

        Unkown,
        ddMMyyyy, yyyyMMdd, yyyyMMdd2, yyMMdd, ddMMyyyy2, MMddyyyy,
        ValueString, ValueDouble, ValueDouble2, ValueDoubleLocalUSA, ValueDoubleLocalGER,
        ColumnNr
    };

    public CSVValueGuesser() {
    }

    public static boolean isDate(String value, SimpleDateFormat sdf) {
        try {
            sdf.parse(value);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    public static Type guessType(String value) {
        if (isDouble(value)) {
            return Type.ValueDouble;
        }

        if (isDouble(value.replaceAll(",", "."))) {
            return Type.ValueDouble2;
        }

        //TODO: better do an loop with different bariants
        if (isDate(value, new SimpleDateFormat("dd-MM-yyyy"))) {
            return Type.ddMMyyyy;
        }

        if (isDate(value, new SimpleDateFormat("yyyy-MM-dd"))) {
            return Type.yyyyMMdd;
        }

        if (isDate(value, new SimpleDateFormat("yyyy.MM.dd"))) {
            return Type.yyyyMMdd2;
        }

        if (isDate(value, new SimpleDateFormat("yy-MM-dd"))) {
            return Type.yyMMdd;
        }

        if (isDate(value, new SimpleDateFormat("dd.MM.yyyy"))) {
            return Type.ddMMyyyy2;
        }

        if (isDate(value, new SimpleDateFormat("MM/dd/yyyy"))) {
            return Type.MMddyyyy;
        }

        return Type.Unkown;

    }

    public static boolean isDouble(String value, Locale local) {
        try {
            NumberFormat format = NumberFormat.getInstance(local);
            Number number = format.parse(value);
            return true;
        } catch (ParseException ex) {
            return false;
        }

    }

    public static boolean isDouble(String value) {
        try {
            double d = Double.parseDouble(value);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
