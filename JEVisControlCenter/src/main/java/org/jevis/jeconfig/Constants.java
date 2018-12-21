/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface Constants {

    interface Plugin {

        String OBJECT = "OBJECT";

        interface Command {


            int SAVE = 0;
            int DELETE = 1;
            int NEW = 2;
            int COPY = 3;
            int PASTE = 4;
            int EXPAND = 5;
            int COLLAPSE = 6;
            int RELOAD = 7;
            int ADD_TABLE = 8;
            int EDIT_TABLE = 9;
            int CREATE_WIZARD = 10;
            int FIND_OBJECT = 11;
            int RENAME = 12;
            int FIND_AGAIN = 13;

        }
    }

    interface Color {

        String MID_BLUE = "#005782";
        String MID_GREY = "#666666";
        String LIGHT_BLUE = "#1a719c";
        String LIGHT_BLUE2 = "#0E8CCC";
        String LIGHT_GREY = "#efefef";
        String LIGHT_GREY2 = "#f4f4f4";
//public static String LIGHT_GREY2 = "#E2E2E2";
        //7f4f4f4
    }

    interface JEVisClass {

        String GROUP = "Group";
        String GROUP_DIRECTORY = "Group Directory";
        String USER = "Users";
        String ORGANIZATION = "Organization";
        String SYSTEM = "System";
        String ADMINISTRATION_DIRECTORY = "Administration Directory";

    }
}
