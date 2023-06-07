/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc;

/**
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
            int CUT = 14;
            int HELP = 15;
            int REPLACE = 101;
            int DELETE_ALL_CLEAN_AND_RAW = 102;
            int CREATE_MULTIPLIER_AND_DIFFERENTIAL = 103;
            int SET_LIMITS = 104;
            int SET_SUBSTITUTION_SETTINGS = 105;
            int SET_UNITS_AND_PERIODS = 106;
            int ENABLE_ALL = 107;
            int DISABLE_ALL = 108;
            int RESET_CALCULATION = 109;
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
        String BUILDING = "Building";
        String MONITORED_OBJECT = "Monitored Object";
        String SYSTEM = "System";
        String ADMINISTRATION_DIRECTORY = "Administration Directory";

    }
}
