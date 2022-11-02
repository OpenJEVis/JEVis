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
package org.jevis.commons.object.plugin;

import org.jevis.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fs
 */
public class TargetHelper {

    private JEVisAttribute sourceAtt;

    private final JEVisDataSource ds;
    private final static String SEPARATOR = ":";
    private final static String MULTI_SELECT_SEPARATOR = ";";
    private String sourceValue = "";
    private final Boolean isAttribute = false;
    private Boolean isObject = false;
    private final List<Boolean> targetObjectIsAccessible = new ArrayList<>();
    private final List<Boolean> targetAttributeIsAccessible = new ArrayList<>();
    private final List<Boolean> isValid = new ArrayList<>();

    private final List<JEVisObject> targetObject = new ArrayList<>();
    private final List<JEVisAttribute> targetAttribute = new ArrayList<>();
    private final List<Long> targetObjectID = new ArrayList<>();
    private final List<String> targetAttributeString = new ArrayList<>();

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

    public TargetHelper(JEVisDataSource ds, List<JEVisObject> newTargetObjects, List<JEVisAttribute> newTargetAttributes) throws JEVisException {

        if (newTargetObjects != null && newTargetAttributes != null) {
            StringBuilder sv = new StringBuilder();
            for (JEVisObject obj : newTargetObjects) {
                int index = newTargetObjects.indexOf(obj);
                sv.append(obj.getID().toString());
                sv.append(":");
                sv.append(newTargetAttributes.get(index).getName());
            }

            this.sourceValue = sv.toString();

        } else if (newTargetObjects != null && newTargetAttributes == null) {
            StringBuilder sv = new StringBuilder();
            for (JEVisObject obj : newTargetObjects) {
                sv.append(obj.getID().toString());
            }
            this.sourceValue = sv.toString();
        }

        this.ds = ds;
        parse();
    }

    public String getSourceString() {
        return this.sourceValue;
    }

    private void parse() {
        //is empty
        if (!sourceValue.isEmpty()) {
            //has separator
            List<String> targets = new ArrayList<>();
            if (sourceValue.contains(MULTI_SELECT_SEPARATOR)) {
                targets = multiSelectStringToList(sourceValue);

            } else {
                targets.add(sourceValue);
            }

            for (String t : targets) {
                if (t.contains(SEPARATOR)) {
                    int separator = t.indexOf(":");
                    String objectString = t.substring(0, separator);
                    boolean validObject = true;
                    boolean validAttribute = true;
                    try {
                        targetObjectID.add(Long.parseLong(objectString));
                    } catch (Exception ex) {
                        validObject = false;
                    }

                    if ((separator + 1) < t.length()) {
                        targetAttributeString.add(t.substring(separator + 1));
                    } else {
                        validAttribute = false;
                    }

                    isValid.add(validObject && validAttribute);

                } else {
                    try {
                        targetObjectID.add(Long.parseLong(t));
                        isValid.add(true);
                    } catch (Exception ex) {
                        isValid.add(false);
                    }
                }
            }

            //check if the target Exist
            for (Long objID : targetObjectID) {
                int index = targetObjectID.indexOf(objID);
                JEVisObject targetObject = null;
                JEVisAttribute targetAttribute = null;
                boolean objectAccessible = true;
                boolean attributeAccessible = true;
                try {
                    targetObject = ds.getObject(objID);
                    if (targetObject != null) {
                        this.targetObject.add(targetObject);
                    }
                } catch (Exception ex) {
                    objectAccessible = false;
                }

                if (targetObject != null && !targetAttributeString.isEmpty()) {
                    try {
                        targetAttribute = targetObject.getAttribute(targetAttributeString.get(index));

                        if (targetAttribute != null) {
                            this.targetAttribute.add(targetAttribute);
                        } else {
                            attributeAccessible = false;
                        }
                    } catch (Exception ex) {
                        attributeAccessible = false;
                    }
                }

                if (targetObject != null) {
                    isObject = true;
                    targetObjectIsAccessible.add(objectAccessible);

                    if (targetAttribute != null) {
                        isObject = false;
                        targetAttributeIsAccessible.add(attributeAccessible);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        if (isValid.isEmpty()) return false;
        boolean isValid = true;
        for (boolean b : this.isValid)
            if (!b) {
                isValid = false;
                break;
            }
        return isValid;
    }

    public boolean targetObjectAccessible() {
        if (targetObjectIsAccessible.isEmpty()) return false;
        boolean isAccessible = true;
        for (boolean b : targetObjectIsAccessible)
            if (!b) {
                isAccessible = false;
                break;
            }
        return isAccessible;
    }

    public boolean targetAttributeAccessible() {
        if (targetAttributeIsAccessible.isEmpty()) return false;
        boolean isAccessible = true;
        for (boolean b : targetAttributeIsAccessible)
            if (!b) {
                isAccessible = false;
                break;
            }
        return isAccessible;
    }

    public boolean isAttribute() {
        return isAttribute;
    }

    public boolean isObject() {
        return isObject;
    }

    public List<JEVisAttribute> getAttribute() {
        return targetAttribute;
    }

    public List<JEVisObject> getObject() {
        return targetObject;
    }


    private List<String> multiSelectStringToList(String s) {
        return new ArrayList<>(Arrays.asList(s.split(";")));
    }
}
