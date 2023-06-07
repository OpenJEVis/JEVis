/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.csv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CSVLine {

    private static final Logger logger = LogManager.getLogger(CSVLine.class);
    private int _coloumCount = 0;
    private int rowCount = 0;
    private int line = -1;
    private String data = "";

    private String _enclosed = "";
    private String _seperator = "";
    private String _regex = "";
    private String[] splitLine;

    private int _rowNumber;
    private boolean isEmpty = false;

    public CSVLine(int rowNumber) {
        _rowNumber = rowNumber;
        isEmpty = true;
    }

    /**
     * @param lineString
     * @param sep
     * @param enclosed
     */
    public CSVLine(String lineString, String enclosed, String sep, int rowNumber) {
        _enclosed = enclosed;
        _rowNumber = rowNumber;
        data = lineString;

        _seperator = escape(sep);

        if (!_enclosed.isEmpty()) {
            String otherThanQuote = " [^" + enclosed + "] ";
            String quotedString = String.format(" \" %s* \" ", otherThanQuote);

            _regex = String.format("(?x) " + // enable comments, ignore white spaces
                            "%s                         " + // match a comma
                            "(?=                       " + // start positive look ahead
                            "  (                       " + //   start group 1
                            "    %s*                   " + //     match 'otherThanQuote' zero or more times
                            "    %s                    " + //     match 'quotedString'
                            "  )*                      " + //   end group 1 and repeat it zero or more times
                            "  %s*                     " + //   match 'otherThanQuote'
                            "  $                       " + // match the end of the string
                            ")                         ", // stop positive look ahead
                    _seperator, otherThanQuote, quotedString, otherThanQuote);
        } else {
            _regex = _seperator;
        }

        parseLine();

    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public int getRowNumber() {
        return _rowNumber;
    }

    private String escape(String text) {
        switch (text) {
            case ".":
                return "\\.";
            case "x":
                return "\\x";
            case "\\":
                return "\\\\";
            case "$":
                return "\\$";
            default:
                return text;
        }
    }

    private void parseLine() {
        try {

            splitLine = data.split(_regex);

//            logger.error("-- "+ Arrays.toString(splitLine));
//            logger.info("----> " + Arrays.toString(splitLine));
            for (int i = 0; i < splitLine.length; i++) {
                splitLine[i] = splitLine[i].replaceAll(_enclosed, "");
            }

            if (splitLine.length == 0) {
                splitLine = new String[]{data};
            }

        } catch (Exception ex) {
            logger.info("#");

        }
    }

    public String getColumn(int i) {
        try {
            return splitLine[i];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return "";
        }

    }

    public int getColumnCount() {
        if (splitLine != null) {
            return splitLine.length;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "CSVLine{" +
                "splitLine=" + Arrays.toString(splitLine) +
                '}';
    }
}
