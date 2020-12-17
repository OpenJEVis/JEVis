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
package org.jevis.commons.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The CSVReport controls the logging for the CSVParser.
 *
 * @author Florian Simon
 */
public class ParserReport {

    private static final Logger logger = LogManager.getLogger(ParserReport.class);
    private final Map<Integer, List<LineError>> errorMap = new HashMap<>();
    private int total = 0;
    private int totalOK = 0;

    public ParserReport() {
    }

    public void addSuccess(int line, int column) {
        //TODO: implement an more detailed report per line and column
        totalOK++;
    }

    public void addError(LineError error) {
        List<LineError> lineErrors = null;
        if (errorMap.get(error.getLine()) != null) {
            lineErrors = errorMap.get(error.getLine());
        } else {
            lineErrors = new ArrayList<>();
        }

        total++;
        lineErrors.add(error);

    }

    public Map<Integer, List<LineError>> errors() {
        return errorMap;
    }

    public void print() {
//        Level level = Logger.getLogger(this.getClass().getName()).getLevel();

        logger.info(total + " errors total in {} lines", errorMap.size());

        //all other
//        if (!level.equals(Level.INFO) && !level.equals(Level.OFF)) {
        //Print only the first error per line
        for (Map.Entry<Integer, List<LineError>> entrySet : errorMap.entrySet()) {

            Integer key = entrySet.getKey();
            List<LineError> lineErrors = entrySet.getValue();

            if (lineErrors != null && !lineErrors.isEmpty()) {
                logger.debug("[{}] Total errors:  {} First error: {} Detail:", key, lineErrors.size(),
                        lineErrors.get(0).getMessage() + "" + lineErrors.get(0).getError().toString());
            }

        }
//        }

        //TODO debug level with all errors
        logger.info("{} Values are OK", totalOK);

    }

}
