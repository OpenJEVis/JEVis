/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI.
 * <p>
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */

package org.jevis.emaildatasource;

import javax.mail.Folder;
import javax.mail.Session;

/**
 * The EMailConnection interface 
 *
 * @author Artur Iablokov
 */
public interface EMailConnection {

    /**
     * terminate email connection
     */
    void terminate();

    /**
     * set email connection
     */
    void setConnection(Session session, EMailServerParameters param);

    /**
     * get email folder
     */
    Folder getFolder();
}
