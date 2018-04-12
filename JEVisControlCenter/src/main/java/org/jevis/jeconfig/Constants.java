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

    public interface Plugin {

        public static String OBJECT = "OBJECT";

        public interface Command {

            public static int SAVE = 0;
            public static int DELTE = 1;
            public static int NEW = 2;
            public static int COPY = 3;
            public static int PASE = 4;
            public static int EXPAND = 5;
            public static int COLLAPSE = 6;
            public static int RELOAD = 7;
            public static int ADD_TABLE = 8;
            public static int EDIT_TABLE = 9;
            public static int CREATE_WIZARD = 10;
            public static int FIND_OBJECT = 11;
            public static int RENAME = 12;
            public static int PASTE = 12;
        }
    }

    public interface Color {

        public static String MID_BLUE = "#005782";
        public static String MID_GREY = "#666666";
        public static String LIGHT_BLUE = "#1a719c";
        public static String LIGHT_BLUE2 = "#0E8CCC";
        public static String LIGHT_GREY = "#efefef";
        public static String LIGHT_GREY2 = "#f4f4f4";
//public static String LIGHT_GREY2 = "#E2E2E2";
        //7f4f4f4
    }

    public interface JEVisClass {

        public static String GROUP = "Group";
        public static String GROUP_DIRECTORY = "Group Directory";
        public static String USER = "Users";
        public static String ORGANIZATION = "Organization";
        public static String SYSTEM = "System";
        public static String ADMINISTRATION_DIRECTROY = "Administration Directory";

    }
}
