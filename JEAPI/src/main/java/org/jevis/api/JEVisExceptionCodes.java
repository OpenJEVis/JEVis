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
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
public class JEVisExceptionCodes {

    //------ Range 1000-1999 Datasource problems -----//
    public static int DATASOURCE_FAILD = 1000;
    public static int DATASOURCE_FAILD_MYSQL = 1001;
    //------ Range 2000-2999 User rights problems -----//
    public static int UNAUTHORIZED = 2000;
    //------ Range 3000-3999 parameter problems -----//
    public static int EMPTY_PARAMETER = 3000;
}
