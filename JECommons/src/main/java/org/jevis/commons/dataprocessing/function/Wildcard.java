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
package org.jevis.commons.dataprocessing.function;

import org.jevis.api.JEVisOption;

/**
 * The Wildcard represents an dynamic placholder in DataProcessor furmulars.
 *
 * @author Florian Simon
 */
public class Wildcard {

    //example  <userselect:'name of var'>
    private static String REGEX = "[<].[a-zA-Z]*[:.]['.][a-zA-Z ]*['.][>.]";
    private JEVisOption option;
    private String type = "";
    private String name = "";

    public Wildcard(JEVisOption option) {
        System.out.println("is wildcard: " + isWildcard(option.getValue()));
        parseWildcard(option.getValue());
    }

    private void parseWildcard(String text) {
        int startIndex = text.indexOf("<");
        int middelIndex = text.indexOf(":");
        int endIndex = text.indexOf(">");

        type = text.substring(startIndex + 1, middelIndex);
        name = text.substring(middelIndex + 1, endIndex).replace("'", "");
        System.out.println("type: " + type);
        System.out.println("name: " + name);

    }

    public static boolean isWildcard(String text) {

        return text.matches(REGEX);
    }

}
