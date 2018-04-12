/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.api.JEVisSample;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CommonObjectTasks {

    private static final Logger logger = LogManager.getLogger(CommonObjectTasks.class);

    public static JEVisRelationship createLink(JEVisObject linkObj, JEVisObject targetObject) throws Exception {
        //TODO handel inherited
        if (!linkObj.getJEVisClass().getName().equals(CommonClasses.LINK.NAME)) {
            throw new IllegalArgumentException("Link Object is not from the Class " + CommonClasses.LINK.NAME);
        }

        return linkObj.buildRelationship(targetObject, JEVisConstants.ObjectRelationship.LINK, JEVisConstants.Direction.FORWARD);

    }

    public static String buildTargetString(JEVisObject obj) {
        return obj.getID() + ":";
    }

    public static String buildTargetString(JEVisAttribute att) {
        return att.getObject().getID() + ":" + att.getName();
    }

    public static JEVisObject getTargetObject(JEVisAttribute source) throws JEVisException {
        JEVisAttribute att = getTargetAttribute(source);
        if (att != null) {
            return att.getObject();
        } else {
            return null;
        }

    }

    public static Long getTargetObjectID(String value) {
        try {
            return Long.parseLong(parseTarget(value)[0]);
        } catch (Exception ex) {
            return -1l;
        }
    }

    public static String getTargetAttributeName(String value) {
        return parseTarget(value)[1];
    }

    private static String[] parseTarget(String targetStrg) {
        logger.trace("TargetAtrribute: '{}'", targetStrg);
        String objectID = "";
        String attributeID = "";
        try {
            int seperator = targetStrg.indexOf(":");
            if (seperator > -1) {
                objectID = targetStrg.substring(0, seperator);
                attributeID = targetStrg.substring(seperator + 1, targetStrg.length());
            } else {
                objectID = targetStrg;
            }

        } catch (Exception ex) {
            logger.debug("Invalie Attribute String: {}", targetStrg);
        }

        logger.trace("Parsed target: '{}' '{}'", objectID, attributeID);
        return new String[]{objectID, attributeID};
    }

    public static JEVisAttribute getTargetAttribute(JEVisAttribute source) throws JEVisException {
        try {
            logger.trace("getTargetAttribute: {}", source);
            JEVisSample lastValue = source.getLatestSample();
            logger.trace("Sample: {}", lastValue);
            String targetStrg = lastValue.getValueAsString();
            logger.trace("value: {}", targetStrg);
            Long objectID = getTargetObjectID(targetStrg);
            String attributeID = getTargetAttributeName(targetStrg);

            JEVisObject targetObject = source.getDataSource().getObject(objectID);
            JEVisAttribute targetAttribute = targetObject.getAttribute(attributeID);

            return targetAttribute;

        } catch (Exception ex) {
            return null;
        }
    }

}
