/**
 * Copyright (C) 2015 - 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEVis CSV-Driver.
 *
 * JEVis CSV-Driver is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEVis CSV-Driver is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEVis CSV-Driver. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEVis CSV-Driver is part of the OpenJEVis project, further project
 * information are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.csvparser;

/**
 * Data structure to handel the loggin for the CSVParser.Used by CSVReport.
 *
 * @author Florian Simon
 */
public class LineError {

    private int line = -1;
    private int column = -1;
    private Throwable error;
    private String message = "";

    public LineError(int line, int column, Throwable error, String message) {
        this.line = line;
        this.column = column;
        this.error = error;
        this.message = message;

    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Throwable getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

}
