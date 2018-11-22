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
package org.jevis.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of helpful JEVisObject operations
 *
 * @author Florian Simon
 */
public class ObjectHelper {
    private static final Logger logger = LogManager.getLogger(ObjectHelper.class);

    /**
     * Returns an list with all parents for the object
     *
     * @param obj list of parents
     * @return
     */
    public static List<JEVisObject> getAllParents(JEVisObject obj) {
        List<JEVisObject> parents = new ArrayList<>();
        findNodeParent(parents, obj);
        return parents;
    }

    /**
     * Internal helper to travers the parents of an object. Adds every parent to
     * the list
     *
     * @param parents
     * @param obj
     */
    private static void findNodeParent(List<JEVisObject> parents, JEVisObject obj) {
//        logger.info("findNodePath: " + parents.size() + " obj: " + obj);
        try {
            if (obj.getParents().size() >= 1) {
                JEVisObject parent = obj.getParents().get(0);
                parents.add(parent);
                findNodeParent(parents, parent);
            }
        } catch (Exception ex) {
            logger.info("Error while searching parent: " + ex);
        }
    }

}
