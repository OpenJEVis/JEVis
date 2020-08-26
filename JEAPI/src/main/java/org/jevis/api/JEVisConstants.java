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

    interface PrimitiveType {

        int STRING = 0;
        int DOUBLE = 1;
        int LONG = 2;
        int FILE = 3;
        int BOOLEAN = 4;
        int SELECTION = 5;
        int MULTI_SELECTION = 6;
        int PASSWORD_PBKDF2 = 7;
    }

    interface DisplayType {

        int TEXT_FIELD = 1;
        int TEXT_PASSWORD = 2;
        int NUMBER = 3;
        int DATE = 3;
    }

    interface Direction {

        /**
         * From Object to target
         */
        int FORWARD = 0;
        /**
         * From Target to object
         */
        int BACKWARD = 1;
    }

    interface ObjectRelationship {

        /**
         * From child to parent
         */
        int PARENT = 1;
        /**
         * From link to original
         */
        int LINK = 2;
        /**
         * From group to root
         */
        int ROOT = 3;
        /**
         * From object to source
         */
        int SOURCE = 4;
        /**
         * From object to service
         */
        int SERVICE = 5;
        /**
         * from object to input
         */
        int INPUT = 6;
        /**
         * From object to data
         */
        int DATA = 7;
        /**
         * from nested to parent
         */
        int NESTED_CLASS = 8;

        /**
         * From object to group
         */
        int OWNER = 100;
        /**
         * From user to group
         */
        int MEMBER_READ = 101;
        /**
         * From user to group
         */
        int MEMBER_WRITE = 102;
        /**
         * From user to group
         */
        int MEMBER_EXECUTE = 103;
        /**
         * From user to group
         */
        int MEMBER_CREATE = 104;
        /**
         * From user to group
         */
        int MEMBER_DELETE = 105;
        /**
         * From role to user
         */
        int ROLE_MEMBER = 200;
        /**
         * From role to group
         */
        int ROLE_READ = 201;
        /**
         * From role to group
         */
        int ROLE_WRITE = 202;
        /**
         * From role to group
         */
        int ROLE_EXECUTE = 203;
        /**
         * From role to group
         */
        int ROLE_CREATE = 204;
        /**
         * From role to group
         */
        int ROLE_DELETE = 205;
        
    }

    interface ClassRelationship {

        /**
         * From subclass to superclass
         */
        int INHERIT = 0;
        /**
         * From host to nested
         */
        int NESTED = 1;//better name = integrated?
        /**
         * From class to possible parent
         */
        int OK_PARENT = 3;
    }

    interface Class {

        String USER = "User";
        String GROUP = "Group";
    }

    interface Attribute {

        String USER_EMAIL = "Email";
        String USER_SYS_ADMIN = "Sys Admin";
        String USER_PASSWORD = "Password";
        String USER_ENABLED = "Enabled";
        String USER_FIRST_NAME = "First Name";
        String USER_LAST_NAME = "Last Name";

    }

    interface Validity {

        int LAST = 0;
        int AT_DATE = 1;
    }

}
