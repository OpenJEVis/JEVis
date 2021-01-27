/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.driver;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.util.List;

/**
 * The interface for the Parser. Each Parser object represents a parser object
 * in the JEVis System.
 *
 * @author Broder
 */
public interface Parser {

    /**
     * Initialize the parser.
     *
     * @param parserObject
     */
    void initialize(JEVisObject parserObject);

    /**
     * Parse the input and collect the results in the result list.
     *
     * @param input
     * @param timezone
     */
    void parse(List<InputStream> input, DateTimeZone timezone);

    /**
     * Gets the results from the parsing process.
     *
     * @return
     */
    List<Result> getResult();

    ParserReport getReport();

//    public void resetResult(); oder neuen parser?
}
