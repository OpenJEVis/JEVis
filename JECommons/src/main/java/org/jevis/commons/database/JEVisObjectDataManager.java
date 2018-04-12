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
package org.jevis.commons.database;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author broder
 */
public class JEVisObjectDataManager {

    public List<JEVisObject> getChildren(JEVisObject dataObject, String jevisClassName) {
        List<JEVisObject> children = new ArrayList<>();
        try {
            JEVisClass jeVisClass = dataObject.getDataSource().getJEVisClass(jevisClassName);
            children = dataObject.getChildren(jeVisClass, true);
        } catch (JEVisException ex) {
            Logger.getLogger(JEVisObjectDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return children;
    }
}
