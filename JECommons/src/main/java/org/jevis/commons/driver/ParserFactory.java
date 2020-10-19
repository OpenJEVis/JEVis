/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Broder
 */
public class ParserFactory extends DriverFactory {
    private static final Logger logger = LogManager.getLogger(ParserFactory.class);
    private static Map<String, Class> _parserClasses = new HashMap<>();

    public static void initializeParser(JEVisDataSource client) {
        _parserClasses = initialize(client, DataCollectorTypes.ParserDriverDirectory.NAME, DataCollectorTypes.Driver.ParserDriver.NAME);
    }

    public static void setParserClasses(Map<String, Class> _parserClasses) {
        ParserFactory._parserClasses = _parserClasses;
    }

    public static Parser getParser(JEVisObject jevisParser) {
        Parser parser = null;
        try {
            String identifier = jevisParser.getJEVisClass().getName();
            Class parserClass = _parserClasses.get(identifier);
            parser = (Parser) parserClass.newInstance();

        } catch (Exception ex) {
            logger.fatal("Error while loading parser: {}", ex.getMessage(), ex);
        }
        return parser;
    }

}
