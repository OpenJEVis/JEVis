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
package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisOption;

/**
 * The Wildcard represents an dynamic placholder in DataProcessor furmulars.
 *
 * @author Florian Simon
 */
public class Wildcard {
    private static final Logger logger = LogManager.getLogger(Wildcard.class);

    //example  <userselect:'name of var'>
    private static final String REGEX = "[<].[a-zA-Z]*[:.]['.][a-zA-Z ]*['.][>.]";
    private JEVisOption option;
    private String type = "";
    private String name = "";

    public Wildcard(JEVisOption option) {
        logger.info("is wildcard: {}", isWildcard(option.getValue()));
        parseWildcard(option.getValue());
    }

    private void parseWildcard(String text) {
        int startIndex = text.indexOf("<");
        int middelIndex = text.indexOf(":");
        int endIndex = text.indexOf(">");

        type = text.substring(startIndex + 1, middelIndex);
        name = text.substring(middelIndex + 1, endIndex).replace("'", "");
        logger.info("type: {}", type);
        logger.info("name: {}", name);

    }

    public static boolean isWildcard(String text) {

        return text.matches(REGEX);
    }

}
