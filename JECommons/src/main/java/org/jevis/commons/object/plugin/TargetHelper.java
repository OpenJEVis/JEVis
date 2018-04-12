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
package org.jevis.commons.object.plugin;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

/**
 *
 * @author fs
 */
public class TargetHelper {

    private JEVisAttribute sourceAtt;
    private JEVisObject targetObject;
    private JEVisAttribute targetAttribute;
    private JEVisDataSource ds;
    private final static String SEPERATOR = ":";
    private String sourceValue = "";
    private boolean isAttribute = false;
    private boolean isObject = false;
    private boolean targetIsAccessable = false;
    private boolean isValid = true;

    private Long targetObjectID = -1l;
    private String targetAttibute = "";

    /**
     * Creates an new TargetHelper. It will get the last value of the given
     * attribute to parse the target.
     *
     * @param ds
     * @param sourceAtt attribute which holds the target String
     * @throws JEVisException
     */
    public TargetHelper(JEVisDataSource ds, JEVisAttribute sourceAtt) throws JEVisException {
        this.sourceAtt = sourceAtt;
        this.ds = ds;

        if (sourceAtt != null) {
            JEVisSample lastV = sourceAtt.getLatestSample();
            if (lastV != null) {
                sourceValue = lastV.getValueAsString();
            }
        }

        parse();
    }

    /**
     * Creates an new TargetHelper.
     *
     * @param ds
     * @param value String who holds an target string "[ObjectID]:[At]"
     */
    public TargetHelper(JEVisDataSource ds, String value) {
        this.sourceValue = value;
        this.ds = ds;
        parse();
    }

    public TargetHelper(JEVisDataSource ds, JEVisObject newTargetObj, JEVisAttribute newTargetAtt) throws JEVisException {

        if (newTargetObj != null) {
            this.sourceValue = newTargetObj.getID().toString();
        }

        if (newTargetAtt != null) {
            this.sourceValue += ":" + newTargetAtt.getName();
        }

        this.ds = ds;
        parse();
    }

    public String getSourceString() {
        return this.sourceValue;
    }

    private void parse() {
        //is emty
        if (sourceValue.isEmpty() == false) {
            //has seperator
            if (sourceValue.contains(SEPERATOR)) {
                int seperator = sourceValue.indexOf(":");
                String objectStrg = sourceValue.substring(0, seperator);
                try {
                    targetObjectID = Long.parseLong(objectStrg);
                } catch (Exception ex) {
                    isValid = false;
                }

                if ((seperator + 1) < sourceValue.length()) {
                    targetAttibute = sourceValue.substring(seperator + 1, sourceValue.length());
                } else {
                    isValid = false;
                }

            } else {
                try {
                    targetObjectID = Long.parseLong(sourceValue);
                } catch (Exception ex) {
                    isValid = false;
                }
            }

            //check if the target Exist
            try {
                targetObject = ds.getObject(targetObjectID);
                isObject = true;
                targetIsAccessable = true;
            } catch (Exception ex) {
                targetIsAccessable = false;
            }

            if (!targetAttibute.isEmpty() && targetObject != null) {
                try {
                    targetAttribute = targetObject.getAttribute(targetAttibute);
                    if (targetAttribute == null) {
                        isValid = false;
                        targetIsAccessable = false;
                    } else {
                        isAttribute = true;
                    }
                } catch (Exception ex) {
                    isValid = false;

                }
            }

        }

    }

    public boolean isValid() {
        return isValid;
    }

    public boolean targetAccessable() {
        return targetIsAccessable;
    }

    public boolean hasAttribute() {
        return isAttribute;
    }

    public boolean hasObject() {
        return isObject;
    }

    public JEVisAttribute getAttribute() {
        return targetAttribute;
    }

    public JEVisObject getObject() {
        return targetObject;
    }

}
