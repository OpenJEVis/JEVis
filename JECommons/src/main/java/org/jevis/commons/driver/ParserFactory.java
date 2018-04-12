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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;

/**
 *
 * @author Broder
 */
public class ParserFactory extends DriverFactory {

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

        } catch (JEVisException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ParserFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parser;
    }

}
