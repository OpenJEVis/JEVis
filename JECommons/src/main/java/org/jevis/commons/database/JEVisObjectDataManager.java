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
package org.jevis.commons.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author broder
 */
public class JEVisObjectDataManager {
    private static final Logger logger = LogManager.getLogger(JEVisObjectDataManager.class);

    public List<JEVisObject> getChildren(JEVisObject dataObject, String jevisClassName) {
        List<JEVisObject> children = new ArrayList<>();
        try {
            JEVisClass jeVisClass = dataObject.getDataSource().getJEVisClass(jevisClassName);
            children = dataObject.getChildren(jeVisClass, true);
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return children;
    }
}
