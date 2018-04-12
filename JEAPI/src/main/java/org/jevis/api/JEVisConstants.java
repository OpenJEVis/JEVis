/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI.
 *
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

/**
 * This interface holds all JEVis constants.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisConstants {

    public interface PrimitiveType {

        public static final int STRING = 0;
        public static final int DOUBLE = 1;
        public static final int LONG = 2;
        public static final int FILE = 3;
        public static final int BOOLEAN = 4;
        public static final int SELECTION = 5;
        public static final int MULTI_SELECTION = 6;
        public static final int PASSWORD_PBKDF2 = 7;
    }

    public interface DisplayType {

        public static final int TEXT_FIELD = 1;
        public static final int TEXT_PASSWORD = 2;
        public static final int NUMBER = 3;
        public static final int DATE = 3;
    }

    public interface Direction {

        /**
         * From Object to target
         */
        public static final int FORWARD = 0;
        /**
         * From Target to object
         */
        public static final int BACKWARD = 1;
    }

    public interface ObjectRelationship {

        /**
         * From child to parent
         */
        public static final int PARENT = 1;
        /**
         * From link to original
         */
        public static final int LINK = 2;
        /**
         * From group to root
         */
        public static final int ROOT = 3;
        /**
         * From object to source
         */
        public static final int SOURCE = 4;
        /**
         * From object to service
         */
        public static final int SERVICE = 5;
        /**
         * from object to input
         */
        public static final int INPUT = 6;
        /**
         * From object to data
         */
        public static final int DATA = 7;
        /**
         * from nested to parent
         */
        public static final int NESTED_CLASS = 8;
        /**
         * From object to group
         */
        public static final int OWNER = 100;
        /**
         * From user to group
         */
        public static final int MEMBER_READ = 101;
        /**
         * From user to group
         */
        public static final int MEMBER_WRITE = 102;
        /**
         * From user to group
         */
        public static final int MEMBER_EXECUTE = 103;
        /**
         * From user to group
         */
        public static final int MEMBER_CREATE = 104;
        /**
         * From user to group
         */
        public static final int MEMBER_DELETE = 105;
        
    }

    public interface ClassRelationship {

        /**
         * From subclass to superclass
         */
        public static final int INHERIT = 0;
        /**
         * From host to nested
         */
        public static final int NESTED = 1;//better name = integrated?
        /**
         * From class to possible parent
         */
        public static final int OK_PARENT = 3;
    }

    public interface Class {

        public static final String USER = "User";
        public static final String GROUP = "Group";
    }

    public interface Attribute {

        public static final String USER_EMAIL = "Email";
        public static final String USER_SYS_ADMIN = "Sys Admin";
        public static final String USER_PASSWORD = "Password";
        public static final String USER_ENABLED = "Enabled";
        public static final String USER_FIRST_NAME = "First Name";
        public static final String USER_LAST_NAME = "Last Name";

    }

    public interface Validity {

        public static final int LAST = 0;
        public static final int AT_DATE = 1;
    }

}
