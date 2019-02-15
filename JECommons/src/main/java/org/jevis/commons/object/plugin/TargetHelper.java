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

    private JEVisDataSource ds;
    private final static String SEPARATOR = ":";
    private final static String MULTI_SELECT_SEPARATOR = ";";
    private String sourceValue = "";
    private Boolean isAttribute = false;
    private Boolean isObject = false;
    private List<Boolean> targetIsAccessible = new ArrayList<>();
    private List<Boolean> isValid = new ArrayList<>();

    private List<JEVisObject> targetObject = new ArrayList<>();
    private List<JEVisAttribute> targetAttribute = new ArrayList<>();
    private List<Long> targetObjectID = new ArrayList<>();
    private List<String> targetAttributeString = new ArrayList<>();

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
                    try {
                        targetObjectID.add(Long.parseLong(objectString));
                    } catch (Exception ex) {
                        isValid.add(false);
                    }

                    if ((separator + 1) < t.length()) {
                        targetAttributeString.add(t.substring(separator + 1));
                    } else {
                        isValid.add(false);
                    }

                } else {
                    try {
                        targetObjectID.add(Long.parseLong(t));
                    } catch (Exception ex) {
                        isValid.add(false);
                    }
                }
            }

            //check if the target Exist
            for (Long objID : targetObjectID) {
                int index = targetObjectID.indexOf(objID);
                JEVisObject tar = null;
                try {
                    tar = ds.getObject(objID);
                    if (tar != null) {
                        targetObject.add(tar);
                    }
                } catch (Exception ex) {
                    targetIsAccessible.add(false);
                }

                if (!targetAttributeString.isEmpty() && !targetObject.isEmpty()) {
                    try {
                        JEVisObject obj = targetObject.get(index);
                        JEVisAttribute att = obj.getAttribute(targetAttributeString.get(index));
                        if (att != null)
                            targetAttribute.add(att);
                        if (att == null) {
                            isValid.add(false);
                            targetIsAccessible.add(false);
                        } else {
                            isValid.add(true);
                            isAttribute = true;
                            targetIsAccessible.add(true);
                        }
                    } catch (Exception ex) {
                        isValid.add(false);
                    }
                } else {
                    if (tar != null) {
                        isObject = true;
                        isValid.add(true);
                        targetIsAccessible.add(true);
                    } else {
                        isObject = false;
                        isValid.add(false);
                        targetIsAccessible.add(false);
                    }
                }
            }
        }
    }

    public boolean isValid() {
        if (isValid.isEmpty()) return false;
        boolean isvalid = true;
        for (boolean b : isValid) if (!b) isvalid = false;
        return isvalid;
    }

    public boolean targetAccessible() {
        if (targetIsAccessible.isEmpty()) return false;
        boolean isaccessible = true;
        for (boolean b : targetIsAccessible) if (!b) isaccessible = false;
        return isaccessible;
    }

    public boolean hasAttribute() {
        return isAttribute;
    }

    public boolean hasObject() {
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
